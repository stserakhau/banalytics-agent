package com.banalytics.box.module.storage.filestorage;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.events.EventHistoryThing;
import com.banalytics.box.module.standard.FileStorage;
import com.banalytics.box.module.storage.FileVO;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.TimerTask;

import static com.banalytics.box.module.events.EventHistoryThingConfig.THING_UUID;

@Slf4j
public class DataCleanerJob extends TimerTask {
    private final BoxEngine engine;
    private final FileStorage targetFileStorage;
    private final FileStorage backupFileStorage;
    private final FileStorageConfig fileStorageConfig;

    public DataCleanerJob(BoxEngine engine, FileStorage targetFileStorage, FileStorage backupFileStorage, FileStorageConfig fileStorageConfig) {
        this.engine = engine;
        this.targetFileStorage = targetFileStorage;
        this.backupFileStorage = backupFileStorage;
        this.fileStorageConfig = fileStorageConfig;
    }

    @Override
    public void run() {
        log.debug("Cleanup file storage '{}' (uuid:{}) started...", fileStorageConfig.title, fileStorageConfig.uuid);
        log.debug("Cleanup type: {}", fileStorageConfig.limitType);
        cleanupLevel("/", 0);
    }

    private void cleanupLevel(String contextPath, int currentLevel) {
        try {
            int targetLevel = fileStorageConfig.applyByHierarchyLevel;

            if (currentLevel < targetLevel) {//if not target level, then go inside
                List<FileVO> currentLevelFolders = targetFileStorage.list(contextPath);
                for (FileVO f : currentLevelFolders) {
                    cleanupLevel(f.contextPath, currentLevel + 1);
                }
                return;
            } else {//if target level do cleanup
                //1. accumulates level data's
                List<FileVO> allFilesOfTheTargetLevel = targetFileStorage.findFiles(contextPath);

                long limitValue = fileStorageConfig.limitValue;

                switch (fileStorageConfig.limitType) {// apply cleanup rule
                    case BY_SIZE_MB:
                        long sizeAccumulator = 0;
                        long maxSize = limitValue * 1024 * 1024;
                        boolean startRemove = false;
                        allFilesOfTheTargetLevel.sort(FileVO.SORT_DESC_BY_CREATION_TIME); //descending sort latest - firsts, oldest - latest
                        for (FileVO toRemove : allFilesOfTheTargetLevel) {
                            if (!startRemove) {
                                sizeAccumulator += toRemove.getSize();  //sum totel size
                                if (sizeAccumulator > maxSize) {        // if total > limit
                                    startRemove = true;                 // starts to remove all next files
                                }
                            } else {
                                process(toRemove);
                            }
                        }
                        break;
                    case BY_TIME_HOURS:
                        long now = System.currentTimeMillis();// server time in the same zone as file creation time
                        long expirationTime = now - limitValue * 3600 * 1000;
                        for (FileVO toRemove : allFilesOfTheTargetLevel) {
                            long fileCreationTime = toRemove.getCreationTime();
                            if (fileCreationTime < expirationTime) {// if file oldest than expiration time
                                process(toRemove);                  // delete file
                            }
                        }
                        break;
                    case BY_OBJECT_COUNT:
                        allFilesOfTheTargetLevel.sort(FileVO.SORT_DESC_BY_CREATION_TIME); //descending sort latest - firsts, oldest - latest
                        for (int i = 0; i < allFilesOfTheTargetLevel.size(); i++) {
                            if (i > limitValue) { // drop files from sorted list which position great then limit amount
                                FileVO toRemove = allFilesOfTheTargetLevel.get(i);
                                process(toRemove);
                            }
                        }
                        break;
                }
            }

            targetFileStorage.cleanEmptyFolders();

/*            EventHistoryThing eht = engine.getThing(THING_UUID);

            if (backupFileStorage != null) {
                eht.updateFileCreatedEventsStorage(
                        targetFileStorage.getUuid(),
                        fileStorageConfig.backupFileStorageUuid,
                );
            } else {
                eht.deleteFileCreatedEventsStorage(targetFileStorage.getUuid());
            }*/

            log.debug("Cleanup done.");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private void process(FileVO toRemove) throws Exception {
        if (backupFileStorage != null) {
            log.debug("Backup started: {}", toRemove);
            String uri = toRemove.getUri();
            String targetContextPath = uri.substring(fileStorageConfig.destinationUri.length() - 1);
            File srcFile = new File(new URI(uri).getPath());
            backupFileStorage.pushFile(srcFile, targetContextPath);
            log.info("Backup finished: {} -- moved --> {}", toRemove, targetContextPath);
//            File backupToFile = backupFileStorage.startOutputTransaction(targetContextPath);
//            backupToFile.delete();
//            FileUtils.moveFile(srcFile, backupToFile, StandardCopyOption.ATOMIC_MOVE);
//            backupFileStorage.commitOutputTransaction(targetContextPath, (pair) -> {
//                log.info("Backup finished: {} -- moved --> {}", toRemove, pair.getRight());
//            });
        } else {
            targetFileStorage.delete(toRemove);
            log.debug("File deleted: {}", toRemove);
        }
    }
}
