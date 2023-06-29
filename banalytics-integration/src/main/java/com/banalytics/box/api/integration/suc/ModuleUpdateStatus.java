package com.banalytics.box.api.integration.suc;

public enum ModuleUpdateStatus {
    /**
     * Module added to Environment and {@link SynchronizeSoftwareEvent} sent to start Upgrade
     */
    installation_planned,

    /**
     * Environment started to download module
     */
    downloading,

    /**
     * Module download failed Environment upgrade stopped.
     * Portal move status to INSTALLATION_CANCELLED
     */
    download_error,

    /**
     * Environment downloaded module successfully
     */
    downloaded,

    /**
     * Portal move status for all modules in DB when receive CRC_ERROR from Environment
     */
    installation_cancelled,

    /**
     * Environment downloaded all modules successfully and successfully restarted.
     */
    installed
}
