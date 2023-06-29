package com.banalytics.box.module.storage.filestorage;

public enum LimitType {
    /**
     * Data accumulation only. No cleaning.
     */
    NO_LIMIT,

    /**
     * Order files by date desc. Start total size calculation and after when total size stands great than limit remove all another.
     */
    BY_SIZE_MB,

    /**
     * Remove all files which were expired by time
     */
    BY_TIME_HOURS,

    /**
     * Order files by date desc and skip first N and remove all another
     */
    BY_OBJECT_COUNT
}
