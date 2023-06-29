package com.banalytics.box.module.storage.filesystem;

import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.storage.FileSystem;
import com.banalytics.box.module.storage.FileVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.DosFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.banalytics.box.module.Thing.DownloadStream.mimeTypeOf;
import static com.banalytics.box.service.SystemThreadsService.getExecutorService;

@Slf4j
@Order(Thing.StarUpOrder.CORE)
public class ServerLocalFileSystemNavigator extends AbstractThing<ServerLocalFileSystemNavigatorConfig> implements FileSystem, Singleton {
    private Statistics statistics = new Statistics();

    public ServerLocalFileSystemNavigator(BoxEngine metricDeliveryService) {
        super(metricDeliveryService);
    }

    @Override
    public String getTitle() {
        return getSelfClassName();
    }

    /**
     * Commit need wait while external process release the file lock.
     */

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
        Thread.sleep(500);
        outputStreamMap.values().forEach(File::delete);
        outputStreamMap.clear();
    }

    private final Map<String, File> outputStreamMap = new ConcurrentHashMap<>();

    @Override
    public void pushFile(File srcFile, String destinationUri, String targetContextPath) throws Exception {
        String destinationPath = destinationUri + targetContextPath;
        destinationPath = fixUri(destinationPath);
        URI destUri = new URI(destinationPath);
        File destinationFile = new File(destUri);
        File destFolder = destinationFile.getParentFile();
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        FileUtils.moveFile(srcFile, destinationFile, StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public File startOutputTransaction(String fileName) throws Exception {
        File localTemp = File.createTempFile(getUuid() + "-", ".tmp");
        outputStreamMap.put(fileName, localTemp);
        log.debug("Start file transaction {}: {}", fileName, localTemp.toURI());
        return localTemp;
    }

    @Override
    public void commitOutputTransaction(String fileName, String destinationUri, Consumer<Pair<String, File>> consumer) throws Exception {
        finishOutputTransaction(fileName, destinationUri, true, consumer);
    }

    @Override
    public void rollbackOutputTransaction(String fileName, String destinationUri, Consumer<Pair<String, File>> consumer) throws Exception {
        finishOutputTransaction(fileName, destinationUri, false, consumer);
    }


    private void finishOutputTransaction(String fileName, String destinationUri, boolean persistFile, Consumer<Pair<String, File>> consumer) throws Exception {
        getExecutorService(this).submit(() -> {
            try {
                log.debug("Commit {}", fileName);
                File localTemp = outputStreamMap.remove(fileName);
                if (localTemp == null || !localTemp.exists()) {
                    return;
                }
                int unlockTryCounter = 0;
                // wait file lock released
                try (FileChannel channel = new RandomAccessFile(localTemp, "rw").getChannel()) {
                    boolean lockedByAnotherProcess;
                    do {
                        Thread.sleep(300);
                        log.debug("Getting the file lock {}", localTemp);
                        try (FileLock lock = channel.tryLock()) {
                            lockedByAnotherProcess = false;
                            log.debug("File lock released {}", localTemp);
                        } catch (OverlappingFileLockException e) {
                            log.warn("File {} locked by another process waiting lock release", localTemp);
                            lockedByAnotherProcess = true;
                        }
                        unlockTryCounter++;
                        if (unlockTryCounter > 100) {
                            log.warn("Unlock file timed out. Trying to continue without unlock.");
                            break;
                        }
                    } while (lockedByAnotherProcess);
                }

                if (persistFile) {
                    Path source = Path.of(localTemp.toURI());
                    String destinationPath = destinationUri + fileName;
                    destinationPath = fixUri(destinationPath);
                    URI destUri = new URI(destinationPath);
                    File destinationFile = new File(destUri);
                    File destFolder = destinationFile.getParentFile();
                    if (!destFolder.exists()) {
                        destFolder.mkdirs();
                    }
                    Thread.sleep(2000);
                    Files.move(
                            source,
                            Path.of(destinationFile.toURI()),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    statistics.committed++;
                    statistics.committedSize += destinationFile.length();
                    log.debug("File transaction committed {} > {}", fileName, destinationFile);

                    consumer.accept(ImmutablePair.of(fileName, destinationFile));
                } else {
                    statistics.rolledBack++;
                    statistics.rolledBackSize += localTemp.length();
                    if (localTemp.delete()) {
                        log.debug("File transaction rolled back: {}", fileName);
                    }
                }
            } catch (Throwable t) {
                log.error("Failed to process file transaction " + fileName + ": " + destinationUri, t);
            }
        });
    }

//    @Override
//    public FileObject resolveFile(String destinationPath) throws Exception {
//        return fileSystemManager.resolveFile(destinationPath);
//    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of(PERMISSION_CREATE, PERMISSION_READ, PERMISSION_DELETE);
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);
        String pathUri = (String) params.get(PATH_URI);
        Long cacheTtlMillis = (Long) params.get(CACHE_TTL_MILLIS);
        cacheTtlMillis = cacheTtlMillis == null ? 0 : cacheTtlMillis;
        pathUri = fixUri(pathUri);
        String fileNameFilter = (String) params.get(FILE_NAME_FILTER);
        switch (method) {
            case "readDescribe" -> {
                return filesList(pathUri, fileNameFilter);
            }
            case "readStream" -> {
                URI uri = new URI(pathUri);
                File f = new File(uri);
                return new DownloadStream(
                        cacheTtlMillis,
                        mimeTypeOf(f.getName()),
                        f
                );
            }
            case "deleteFile" -> {
                return deletePath(pathUri);
            }
            case "createFolder" -> {
                return createFolder(pathUri);
            }
            case "updateMoveFile" -> {
                String moveToPathUri = (String) params.get(MOVE_TO_PATH_URI);
                moveFile(pathUri, moveToPathUri);
                return "success";
            }
            case "readStatistics" -> {
                return statistics;
            }
            default -> throw new RuntimeException("Method not supported: " + method);
        }
    }

    private static String fixUri(String uri) {
        if (uri == null) {
            return null;
        }
        return uri.replaceAll(" ", "%20");
    }

    @Override
    public List<FileVO> filesList(String pathUri, String fileNameFilter) throws Exception {
        pathUri = fixUri(pathUri);
        final Pattern pattern;
        if (StringUtils.isNotEmpty(fileNameFilter)) {
            pattern = Pattern.compile(fileNameFilter);
        } else {
            pattern = null;
        }
        File[] children;
        if (StringUtils.isEmpty(pathUri)
                || (SystemUtils.IS_OS_WINDOWS && pathUri.equals("file:/"))
        ) {
            children = File.listRoots();
        } else {
            URI uri = new URI(pathUri);
            File file = new File(uri);
            children = file.listFiles(pathname -> {
                        try {
                            DosFileAttributes attr = Files.readAttributes(pathname.toPath(), DosFileAttributes.class);
                            String name = pathname.getName();
                            return !pathname.isHidden() && !attr.isSystem() && !name.toLowerCase().contains("windows") && (
                                    pathname.isDirectory()
                                            || pattern == null
                                            || pattern.matcher(pathname.getName()).matches()
                            );
                        } catch (IOException e) {
                            log.warn(e.getMessage());
                            return false;
                        }
                    }
            );
        }
        if (children == null) {
            return List.of();
        }
        List<FileVO> result = new ArrayList<>(children.length);
        for (File f : children) {
            try {
                result.add(FileVO.of(f));
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        result.sort(Comparator.comparing(o -> o.name));
        return result;
    }

    @Override
    public List<FileVO> findFiles(String pathUri) throws Exception {
        pathUri = fixUri(pathUri);
        try {
            File root = new File(new URI(pathUri));
            List<FileVO> accumulator = new ArrayList<>(1000);

            recursiveSearch(root, accumulator);

            return accumulator;
        } catch (Exception e) {
            throw e;
        }
    }

    private void moveFile(String pathUri, String moveToPathUri) throws Exception {
        File from = new File(new URI(pathUri));
        File to = new File(new URI(moveToPathUri+ '/' + from.getName()));
        Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
    }

    private boolean createFolder(String pathUri) throws Exception {
        pathUri = fixUri(pathUri);
        File root = new File(new URI(pathUri));
        return root.mkdirs();
    }

    private void recursiveSearch(File root, List<FileVO> accumulator) throws Exception {
        if (root.isFile()) {
            accumulator.add(FileVO.of(root));
            return;
        }
        for (File file : root.listFiles()) {
            recursiveSearch(file, accumulator);
        }
    }

    @Override
    public void delete(FileVO file) throws Exception {
        File f = new File(new URI(file.getUri()).getPath());
        deleteFile(f);
    }

    private Object deletePath(String pathUri) throws Exception {
        File f = getLocalFile(pathUri);
        deleteFile(f);
        return null;
    }

    private static void deleteFile(File f) throws Exception {
        if (f.isDirectory()) {
            FileUtils.forceDelete(f);
        } else {
            FileUtils.forceDelete(f);
        }
        log.debug("File removed: {}", f);
    }

    @Override
    public File getLocalFile(String pathUri) throws Exception {
        pathUri = fixUri(pathUri);
        File file = new File(new URI(pathUri).getPath());
        if (!file.exists()) {
            throw new FileNotFoundException(pathUri);
        }
        return file;
    }

    @Override
    public void cleanEmptyFolders(String pathUri) throws Exception {
        pathUri = fixUri(pathUri);
        File root = new File(new URI(pathUri).getPath());
        for (File f : root.listFiles()) {
            if (f.isDirectory()) {
                recursiveClean(f);
            }
        }
    }

    private void recursiveClean(File folder) {
        File[] subFiles = folder.listFiles();
        if (subFiles.length > 0) {//if exists subfiles
            boolean hasFiles = false;
            for (File f : subFiles) {//scan
                if (f.isDirectory()) {//and go inside subfolders
                    recursiveClean(f);
                } else if (f.isFile()) {
                    hasFiles = true;
                }
            }
            if (!hasFiles) {
                folder.delete();
            }
        } else {// if folder is empty, then remove it
            folder.delete();
        }
    }

    @Override
    public long getFreeSpace(String pathUri) throws URISyntaxException {
        pathUri = fixUri(pathUri);
        File root = new File(new URI(pathUri).getPath());
        return root.getFreeSpace();
    }

    @Getter
    public static class Statistics {
        int committed;
        int rolledBack;
        long committedSize;
        long rolledBackSize;
    }
}
