package com.banalytics.box.module.storage.filesystem;

import com.banalytics.box.module.AbstractConfiguration;

import java.util.UUID;

public class ServerLocalFileSystemNavigatorConfig extends AbstractConfiguration {
    public static UUID SERVER_LOCAL_FS_NAVIGATOR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    @Override
    public UUID getUuid() {
        return SERVER_LOCAL_FS_NAVIGATOR_UUID;
    }
}
