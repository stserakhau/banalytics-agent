package com.banalytics.box.service.utility;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPUtils {
    public static byte[] compress(byte[] data) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (GZIPOutputStream inputStream = new GZIPOutputStream(baos)) {
                inputStream.write(data);
                inputStream.flush();
            }
            return baos.toByteArray();
        }
    }

    public static String decompress(byte[] data) throws IOException {
        try (GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(data));
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            return IOUtils.toString(reader);
        }
    }

    public static void main(String[] args) throws IOException {
        String val = RandomStringUtils.randomAlphabetic(500);

        byte[] data = compress(val.getBytes(StandardCharsets.UTF_8));

        String decomp = decompress(data);

        System.out.println(val);
        System.out.println(decomp);
    }
}
