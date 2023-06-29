package com.banalytics.box.module.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Windows:
 * 1. C:\>net share mysharedfolder=c:\shared
 * mysharedfolder was shared successfully.
 * 2. net share
 * 3. net share /delete mysharedfolder
 * <p>
 * Linux:
 * https://linuxhint.com/share-folder-on-local-network-with-ubuntu/
 */
@Slf4j
public final class NetworkUtils {
    public static boolean createNetworkFolder(String networkName, File folder) {
        return executeCommand("net share {}={}", networkName, folder.getAbsolutePath());

    }

    public static boolean deleteNetworkFolder(String networkName) {
        return executeCommand("net share /delete {}", networkName);
    }

    /**
     * https://www.howtogeek.com/118452/how-to-map-network-drives-from-the-command-prompt-in-windows/
     */
    public static boolean mountNetworkFolderAsLocalDrive(String drive, String networkPath) {
        return executeCommand("net use {} {}", drive, networkPath);
    }

    public static boolean unMountNetworkFolderAsLocalDrive(String drive) {
        return executeCommand("net use {} /delete", drive);
    }

    private static boolean executeCommand(String cmd, String... args) {
        try {
            Runtime runtime = Runtime.getRuntime();
            cmd = cmd.formatted(args);
            Process process = runtime.exec(cmd);
            while (process.isAlive()) {
                log.info("Waiting...");
                Thread.sleep(1000);
            }
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                log.error("Process returned error code: {}", exitValue);
            }
            return exitValue == 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        createNetworkFolder("test", new File("e:\\out"));
    }
}
