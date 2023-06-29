package com.banalytics.box.module;

import java.util.Collection;
import java.util.Collections;

public interface AutoAddTasks {
    /**
     * Method execuited after thing started
     */
    default Collection<AbstractTask<?>> autoAddTasks() {
        return Collections.emptyList();
    }
}
