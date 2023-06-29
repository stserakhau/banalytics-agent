package com.banalytics.box.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.util.DigestUtils;

import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.UUID;

@Slf4j
public class SystemOptionsService {
    public static final String MOTHERBOARD_SERIAL;
    public static final String VCARD_NAME;
    public static final String ACCELERATION_PROVIDE;

    static {
        MOTHERBOARD_SERIAL = getMotherBoardSerial();
        VCARD_NAME = getVCardName();
        ACCELERATION_PROVIDE = getAccelerationProvider();
    }

    public static UUID generatedUuid() {
        return UUID.randomUUID();
    }

    public static String environmentHash() {
        return DigestUtils.md5DigestAsHex(MOTHERBOARD_SERIAL.getBytes());
    }

    /**
     * + h264_amf to access AMD gpu, (windows only)
     * + h264_nvenc use nvidia gpu cards (work with windows and linux)
     * h264_omx raspberry pi encoder
     * + h264_qsv use Intel Quick Sync Video (hardware embedded in modern Intel CPU)
     * + h264_v4l2m2m use V4L2 Linux kernel api to access hardware codecs
     * h264_vaapi use VAAPI which is another abstraction API to access video acceleration hardware (Linux only)
     * + h264_videotoolbox use videotoolbox an API to access hardware on macOS
     */
    public static String getAccelerationProvider() {
        String vcard = VCARD_NAME;
        if (vcard == null) {
            return null;
        }
        if (vcard.contains("UHD")) {
            return "qsv";
        }
        if (vcard.contains("Nvidia")) {
            return "nvenc";
        }
        if (vcard.contains("Radeon")) {
            return "amf";
        }
        if (vcard.contains("linux")) {
            return "v4l2m2m";
        }
        if (vcard.contains("macos")) {
            return "videotoolbox";
        }

        return null;
    }


    private static String getVCardName() {
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                String res = commandResponse(new String[]{"wmic", "PATH", "Win32_videocontroller", "GET", "description"});
                String cardName = res.split("\n")[1].trim();
                return cardName;
            } else if (SystemUtils.IS_OS_LINUX) {
                return "linux";
            } else if (SystemUtils.IS_OS_MAC) {
                return "macos";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

        return null;
    }

    private static String getMotherBoardSerial() {
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                String res = commandResponse(new String[]{"wmic", "BASEBOARD", "get", "SerialNumber"});
                String motherBoardSerial = res.split("\n")[1].trim();
                return motherBoardSerial;
            } else if (SystemUtils.IS_OS_LINUX) {
                return "linux";
            } else if (SystemUtils.IS_OS_MAC) {
                return "macos";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
        return null;
    }

    private static String commandResponse(String[] command) throws Exception {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
            return IOUtils.toString(new InputStreamReader(p.getInputStream()));
        } finally {
            if (p != null) {
                p.destroyForcibly();
            }
        }
    }
}
