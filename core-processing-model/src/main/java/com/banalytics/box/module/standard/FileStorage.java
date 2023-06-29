package com.banalytics.box.module.standard;

import com.banalytics.box.module.storage.FileVO;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface FileStorage {
    UUID getUuid();

    void pushFile(File srcFile, String targetContextPath) throws Exception;

    File startOutputTransaction(String fileName) throws Exception;

    /**
     * Method returns context path of the committed file
     */
    void commitOutputTransaction(String fileName, Consumer<Pair<String, File>> consumer) throws Exception;

    void rollbackOutputTransaction(String fileName, Consumer<Pair<String, File>> consumer) throws Exception;

    List<FileVO> findFiles() throws Exception;

    List<FileVO> findFiles(String contextPath) throws Exception;

    void delete(FileVO file) throws Exception;

    List<FileVO> list(String contextPath) throws Exception;

    File file(String contextPath) throws Exception;

    void cleanEmptyFolders() throws Exception;
}
