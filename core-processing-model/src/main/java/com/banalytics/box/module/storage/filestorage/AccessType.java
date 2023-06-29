package com.banalytics.box.module.storage.filestorage;

public enum AccessType {
    READ_ONLY(true, false, false),

    READ_WRITE(true, true, false),

    READ_WRITE_DELETE(true, true, true);

    public final boolean read;
    public final boolean write;
    public final boolean delete;

    AccessType(boolean read, boolean write, boolean delete) {
        this.read = read;
        this.write = write;
        this.delete = delete;
    }
}
