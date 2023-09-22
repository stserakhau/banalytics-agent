package com.banalytics.box.module.storage;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface FileSystem {
    String CACHE_TTL_MILLIS = "cacheTtlMillis";
    String PATH_URI = "pathUri";
    String MOVE_TO_PATH_URI = "moveToPathUri";
    String FILE_NAME_FILTER = "fileNameFilter";

    void pushFile(File srcFile, String destinationUri, String targetContextPath) throws Exception;

    File startOutputTransaction(UUID sourceUuid, String fileName) throws Exception;

    void commitOutputTransaction(UUID sourceUuid, String fileName, String destinationUri, Consumer<Pair<String, File>> consumer) throws Exception;

    void rollbackOutputTransaction(UUID sourceUuid, String fileName, String destinationUri, Consumer<Pair<String, File>> consumer) throws Exception;

    Object call(Map<String, Object> params) throws Exception;

    List<FileVO> filesList(String pathUri, String fileNameFilter) throws Exception;

    List<FileVO> findFiles(String pathUri) throws Exception;

    void delete(FileVO file) throws Exception;

    File getLocalFile(String pathUri) throws Exception;

    void cleanEmptyFolders(String pathUri) throws Exception;

    long getFreeSpace(String pathUri) throws URISyntaxException;
}
