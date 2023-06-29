package com.banalytics.box.module.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

@RequiredArgsConstructor
@Getter
@Setter
public class FileVO {
    public final boolean isFolder;
    public final String uri;
    public String contextPath;
    public final String name;
    public final long size;
    public final long creationTime;

    public static FileVO of(File file) throws Exception {
        BasicFileAttributes bfa = Files.readAttributes(
                file.toPath(),
                BasicFileAttributes.class
        );
        String name = file.getName();
        if (StringUtils.isEmpty(name)) {
            name = file.getCanonicalPath();
        }

        return new FileVO(
                file.isDirectory(),
                file.toURI().toString(),
                name,
                file.length(),
                bfa.lastModifiedTime().toMillis()
        );
    }

    @Override
    public String toString() {
        return "FileVO{" +
                "isFolder=" + isFolder +
                ", name='" + name + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }

    public static final Comparator<FileVO> SORT_DESC_BY_CREATION_TIME = new Comparator<FileVO>() {
        @Override
        public int compare(FileVO o1, FileVO o2) {
            int timeResult = Long.compare(o2.creationTime, o1.creationTime);

            if (timeResult == 0) {
                int sizeResult = Long.compare(o2.size, o1.size);
                if (sizeResult == 0) {
                    return o2.contextPath.compareTo(o1.contextPath);
                } else {
                    return sizeResult;
                }
            } else {
                return timeResult;
            }
        }
    };
}
