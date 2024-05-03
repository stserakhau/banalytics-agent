package com.banalytics.box.module.media.task;

import com.banalytics.box.module.AbstractConfiguration;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;

public abstract class AbstractMediaGrabberTask<T extends AbstractConfiguration> extends AbstractStreamingMediaTask<T> {
    public AbstractMediaGrabberTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
        taskInitializationDelay = 500;
    }
}
