package com.banalytics.box.module;

import com.banalytics.box.api.integration.form.FormModel;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.service.JpaService;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public interface BoxEngine {
//    default Resource[] i18nResources() {
//        return new Resource[0];
//    }

    default UUID getEnvironmentUUID() {
        throw new RuntimeException("Method not implemented");
    }

    default <T extends Thing<?>> T getThingAndSubscribe(UUID uuid, InitShutdownSupport initShutdownSupport) throws Exception {
        return null;
    }

    default <T extends Thing<?>> T getThing(UUID uuid) {
        return null;
    }

    default List<Thing<?>> findThings(Class<?>... standards) throws Exception {
        return List.of();
    }

    default List<Thing<?>> findThingsByStandard(Class<?>... interfaces) {
        return List.of();
    }

    default <T> List<T> findTasksByInterfaceSupport(Class<T> interfaceClass) {
        return List.of();
    }

    default File applicationHomeFolder() {
        return new File(".");
    }
    default File applicationConfigFolder() {
        return new File(".");
    }

    default BuildProperties getBuildProperties() {
        return null;
    }

    default void shutdownApplication() {
    }

    default <T> List<T> findThingInstances(Class<T> instanceClass) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default Collection<Class<? extends ITask>> findTaskClassesByInterface(Class<?> iface) {
        throw new RuntimeException("Method not implemented");
    }

    default Collection<? extends Thing<?>> findThings() throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default <T extends ITask<?>> T findTask(UUID taskUuid) {
        throw new RuntimeException("Method not implemented");
    }

    default Collection<AbstractTask<?>> instances() {
        throw new RuntimeException("Method not implemented");
    }

    default List<AbstractTask<?>> findSubTasks(UUID parentTaskUuid) {
        throw new RuntimeException("Method not implemented");
    }

    default FormModel describeClass(String className) {
        throw new RuntimeException("Method not implemented");
    }

    default AbstractTask<?> saveOrUpdateTask(UUID parentTaskUuid, UUID taskUuid, String taskClass, Map<String, Object> configuration) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default Thing<?> saveOrUpdateThing(UUID thingUuid, String thingClass, Map<String, Object> configuration) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void deleteThing(UUID thingUuid) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void deleteTask(UUID taskUuid) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default Collection<Class<?>> supportedThings() {
        throw new RuntimeException("Method not implemented");
    }

/*    default Collection<String> supportedSubtasks(UUID parentTaskUuid) {
        throw new RuntimeException("Method not implemented");
    }*/

    default Collection<AbstractAction<?>> findActionTasks() {
        throw new RuntimeException("Method not implemented");
    }

    default <T> T getBean(Class<T> beanClass) {
        throw new RuntimeException("Method not implemented");
    }

    default void addEventConsumer(Consumer<AbstractEvent> eventConsumer) {
        throw new RuntimeException("Method not implemented");
    }

    default void removeEventConsumer(Consumer<AbstractEvent> eventConsumer) {
        throw new RuntimeException("Method not implemented");
    }

    default void fireEvent(AbstractEvent event) {
        throw new RuntimeException("Method not implemented");
    }

    default Set<Class<? extends ITask>> supportedTaskClasses() {
        throw new RuntimeException("Method not implemented");
    }

    default Set<Class<? extends AbstractEvent>> eventTypeClasses() {
        throw new RuntimeException("Method not implemented");
    }

    default void addPostInitializingHook(Runnable hook) {
        throw new RuntimeException("Method not implemented");
    }

    default JpaService getJpaService() {
        throw new RuntimeException("Method not implemented");
    }

    default Instance getPrimaryInstance() {
        throw new RuntimeException("Method not implemented");
    }

    default void startTask(UUID taskUuid) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void stopTask(UUID taskUuid) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void startThing(UUID thingUuid) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void stopThing(UUID thingUuid) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void startBillableFeatures() throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void stopBillableFeatures() throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default Object serviceCall(String serviceName, String methodName, String arg) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default void persistPrimaryInstance() throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default File getModelPath(String modelsRoot, String subModelName) throws Exception {
        throw new RuntimeException("Method not implemented");
    }

    default boolean verifyPassword(String password) {
        throw new RuntimeException("Method not implemented");
    }

    default void changePassword(String oldPassword, String newPassword) {
        throw new RuntimeException("Method not implemented");
    }

    default void resetPassword(String newPassword) {
        throw new RuntimeException("Method not implemented");
    }

    default AbstractTask<?> buildTask(String clazz, Map<String, Object> configuration, AbstractListOfTask<?> parent) throws Exception {
        throw new RuntimeException("Method not implemented");
    }
}
