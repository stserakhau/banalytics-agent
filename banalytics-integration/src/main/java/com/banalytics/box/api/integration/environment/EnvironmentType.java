package com.banalytics.box.api.integration.environment;

public enum EnvironmentType {
    development,

    /**
     * Module uploaded to google artifactory.
     * Record in this state appeared after execution <b>maven publish</b> for module.
     * Module version available only for environments which marked as development.
     */
    qa,

    /**
     * QA executed validation tests
     * Module version available only for environments which marked as la.
     */
    staging,

    /**
     * QA executed release tests
     */
    production
}
