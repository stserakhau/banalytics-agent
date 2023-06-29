package com.banalytics.box.module.cloud.portal.suc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class DownloadUtil {
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 32 * 1024;


    public static void copyUrlToFile(URL url, File destination) throws IOException {
        InputStream source = null;
        try {
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            int fileSize = urlConnection.getContentLength();
            source = urlConnection.getInputStream();
            final FileOutputStream output = FileUtils.openOutputStream(destination, false);
            try {
                final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int counter = 0;
                long count = 0;
                int divider = 100;
                int readBytes;
                while (EOF != (readBytes = source.read(buffer))) {
                    output.write(buffer, 0, readBytes);
                    count += readBytes;
                    if (counter % divider == 0) {
                        log.info("Completed " + count * 100 / fileSize + "%");
                    }
                    counter++;
                }
                log.info("Download completed:\n{}\nto\n{}", url.getPath(), destination);
                output.close(); // don't swallow close Exception if copy completes normally
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(source);
        }
    }

    public static void unzip(File zipSourceFile, File destFolder) throws IOException {
        Path destFolderPath = destFolder.toPath();

        try (ZipFile zipFile = new ZipFile(zipSourceFile, ZipFile.OPEN_READ)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path entryPath = destFolderPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        try (OutputStream out = new FileOutputStream(entryPath.toFile())) {
                            IOUtils.copy(in, out, 128 * 1024);
                        }
                    }
                }
                log.info("Extracted: {}", entryPath);
            }
        }
    }
}
