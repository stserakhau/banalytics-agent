package com.banalytics.box.module.storage.filestorage;

import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.constants.RestartOnFailure;
import com.banalytics.box.module.standard.FileStorage;
import com.banalytics.box.module.storage.FileSystem;
import com.banalytics.box.module.storage.FileVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.banalytics.box.module.constants.CleanupInterval.NA;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

/**
 * https://commons.apache.org/proper/commons-vfs/filesystems.html
 */
@Slf4j
@Order(Thing.StarUpOrder.BUSINESS)
public class FileStorageThing extends AbstractThing<FileStorageConfig> implements FileStorage {

    @Override
    public String getTitle() {
        if (StringUtils.isEmpty(configuration.title)) {
            int lastFolderIndex = configuration.destinationUri.lastIndexOf('/', configuration.destinationUri.length() - 2);
            return configuration.destinationUri.substring(lastFolderIndex).replaceAll("/", "");
        } else {
            return configuration.title;
        }
    }

    public FileStorageThing(BoxEngine metricDeliveryService) {
        super(metricDeliveryService);
    }

    @Override
    public Object uniqueness() {
        return configuration.getFileSystemUuid().toString() + "/" + configuration.destinationUri;
    }

    private DataCleanerJob dataCleanerJob;
    private FileSystem fileSystem;
    private FileStorage backupFileSystem;

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
        this.fileSystem = engine.getThingAndSubscribe(configuration.fileSystemUuid, this);

        validateFreeSpace();

        if (configuration.backupFileStorageUuid != null) {
            this.backupFileSystem = engine.getThingAndSubscribe(configuration.backupFileStorageUuid, this);
        }

        if (configuration.accessType.delete) {
            if (configuration.cleanUpTime != NA && configuration.limitType != LimitType.NO_LIMIT) {
                this.dataCleanerJob = new DataCleanerJob(engine, this, this.backupFileSystem, this.configuration);
                SYSTEM_TIMER.schedule(
                        this.dataCleanerJob,
                        10000, configuration.cleanUpTime.intervalMillis
                );
            }
        }
    }

    private void validateFreeSpace() throws Exception {
        if (configuration.accessType.write) {
            long freeSpace = fileSystem.getFreeSpace(configuration.getDestinationUri());
            if (freeSpace < 200 * 1024 * 1024L) {
                configuration.restartOnFailure = RestartOnFailure.STOP_ON_FAILURE;
                throw new Exception("error.lowStorageSpace");
            }
        }
    }

    @Override
    protected void doStop() throws Exception {
        ((Thing<?>) fileSystem).unSubscribe(this);

        if (dataCleanerJob != null) {
            this.dataCleanerJob.cancel();
        }

        log.debug("Shutdown finished");
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of(PERMISSION_READ, PERMISSION_UPDATE, PERMISSION_DELETE);
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);

        switch (method) {
            case "readUiOptions" -> {
                return Map.of(
                        "cacheEnabled", configuration.useBrowserCache,
                        "cacheTTLMinutes", configuration.browserCacheTTLMinutes,
                        "safeOps", configuration.safeOps,
                        "uploadEnabled", configuration.uploadEnabled,
                        "moveEnabled", configuration.moveEnabled
                );
            }
            default -> {
                if (fileSystem == null) {
                    throw new Exception("error.thing.notInitialized");
                }
                if (!configuration.accessType.delete && method.startsWith("delete")) {
                    throw new Exception("error.delete.notSupported");
                }
                if (!configuration.accessType.read && method.startsWith("read")) {
                    throw new Exception("error.read.restricted");
                }
                if (!configuration.accessType.write && (method.startsWith("write") || method.startsWith("create"))) {
                    throw new Exception("error.write.restricted");
                }


                String contextPath = (String) params.get("contextPath");
                if (StringUtils.isEmpty(contextPath)) {
                    contextPath = "/";
                }
                String pathUri = configuration.getDestinationUri() + contextPath;
                params.put(FileSystem.PATH_URI, pathUri);

                String moveToPath = (String) params.get("moveToPath");
                if (!StringUtils.isEmpty(moveToPath)) {
                    String moveToPathUri = configuration.getDestinationUri() + moveToPath;
                    moveToPathUri = moveToPathUri.replaceAll("//", "/");
                    moveToPathUri = moveToPathUri.replaceAll(" ", "%20");
                    params.put(FileSystem.MOVE_TO_PATH_URI, moveToPathUri);
                }

                params.put(FileSystem.CACHE_TTL_MILLIS, configuration.browserCacheTTLMinutes * 60 * 1000L);

                return fileSystem.call(params);
            }
        }
    }

    @Override
    public List<FileVO> findFiles() throws Exception {
        if (!configuration.accessType.read) {
            throw new Exception("error.read.restricted");
        }
        String rootPathUri = configuration.getDestinationUri();
        return applyContextPath(fileSystem.findFiles(rootPathUri), "");
    }

    @Override
    public List<FileVO> findFiles(String contextPath) throws Exception {
        if (!configuration.accessType.read) {
            throw new Exception("error.read.restricted");
        }
        String rootPathUri = configuration.getDestinationUri() + contextPath;
        return applyContextPath(
                fileSystem.findFiles(rootPathUri),
                contextPath
        );
    }

    private static List<FileVO> applyContextPath(List<FileVO> files, String contextPath) {
        for (FileVO f : files) {
            applyContextPath(f, contextPath);
        }
        return files;
    }

    private static void applyContextPath(FileVO f, String contextPath) {
        if (contextPath.endsWith("/")) {
            f.contextPath = contextPath + f.getName();
        } else {
            f.contextPath = contextPath + "/" + f.getName();
        }
    }

    @Override
    public void delete(FileVO file) throws Exception {
        if (!configuration.accessType.delete) {
            throw new Exception("error.delete.notSupported");
        }
        fileSystem.delete(file);
    }

    @Override
    public List<FileVO> list(String contextPath) throws Exception {
        if (!configuration.accessType.read) {
            throw new Exception("error.read.restricted");
        }
        return applyContextPath(
                fileSystem.filesList(this.configuration.destinationUri + contextPath, null),
                contextPath
        );
    }

    @Override
    public File file(String contextPath) throws Exception {
        if (!configuration.accessType.read) {
            throw new Exception("error.read.restricted");
        }
        return fileSystem.getLocalFile(this.configuration.destinationUri + contextPath);
    }

    @Override
    public void cleanEmptyFolders() throws Exception {
        if (!configuration.accessType.write) {
            throw new Exception("error.write.restricted");
        }
        fileSystem.cleanEmptyFolders(this.configuration.destinationUri);
    }

    @Override
    public void pushFile(File srcFile, String targetContextPath) throws Exception {
        if (!configuration.accessType.write) {
            throw new Exception("error.write.restricted");
        }
        validateFreeSpace();
        fileSystem.pushFile(srcFile, configuration.destinationUri, targetContextPath);
    }

    @Override
    public File startOutputTransaction(String fileName) throws Exception {
        if (!configuration.accessType.write) {
            throw new Exception("error.write.restricted");
        }
        validateFreeSpace();
        return fileSystem.startOutputTransaction(fileName);
    }

    @Override
    public void commitOutputTransaction(String fileName, Consumer<Pair<String, File>> consumer) throws Exception {
        String destinationUri = configuration.destinationUri;
        fileSystem.commitOutputTransaction(fileName, destinationUri, consumer);
    }

    @Override
    public void rollbackOutputTransaction(String fileName, Consumer<Pair<String, File>> consumer) throws Exception {
        String destinationUri = configuration.destinationUri;
        fileSystem.rollbackOutputTransaction(fileName, destinationUri, consumer);
    }
}
