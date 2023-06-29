package com.banalytics.box.module;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ITask<CONFIGURATION extends IConfiguration> extends Cloneable, Serializable, StateSupport, InitShutdownSupport {
    default String getTitle() {
        return this.getSelfClassName();
    }

    /**
     * when returns null, then task is a self-sufficient task which can be started without any specific context
     * otherwise task requires each variable in context with specified type
     */
    default Map<String, Class<?>> inSpec() {
        return null;
    }

    String getSelfClassName();

    UUID getSourceThingUuid();

    default Thing<?> getSourceThing() {
        return null;
    }

    AbstractListOfTask<?> parent();

    AbstractListOfTask<?> root();

    void parent(AbstractListOfTask<?> parent);

    CONFIGURATION getConfiguration();

    UUID getUuid();

    default Object uniqueness() {
        return null;
    }

    default void doInit() throws Exception {
    }

    default void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {}

    default void doStop() throws Exception {}

    boolean process(ExecutionContext executionContext) throws Exception;

    void copyTo(ITask<CONFIGURATION> copy);

    static AbstractTask<?> blankOf(Class<? extends AbstractTask> taskClazz, BoxEngine engine, AbstractListOfTask<?> parent) throws Exception {
        return taskClazz.getDeclaredConstructor(new Class[]{BoxEngine.class, AbstractListOfTask.class}).newInstance(engine, parent);
    }

//    Map<String, Class<?>> runtimeOutSpec();

    default void destroy() {
    }

    Set<UUID> selfDelete();

    default boolean billable() {
        return false;
    }

    default boolean hidden() {
        return false;
    }

    default Collection<ITask<?>> subtasksAndMe() {
        return Set.of(this);
    }
}
