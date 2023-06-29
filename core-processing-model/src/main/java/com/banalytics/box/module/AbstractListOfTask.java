package com.banalytics.box.module;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public abstract class AbstractListOfTask<CONFIGURATION extends AbstractConfiguration> extends AbstractTask<CONFIGURATION> {
    private List<AbstractTask<?>> subTasks = new ArrayList<>();

    public AbstractListOfTask(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    public List<AbstractTask<?>> getSubTasks() {
        return Collections.unmodifiableList(subTasks);
    }

    public synchronized void addSubTask(AbstractTask<?> task) {
        List<AbstractTask<?>> subTasks = new ArrayList<>(this.subTasks);
        subTasks.add(task);
        this.subTasks = subTasks;
    }

    public synchronized void addSubTaskWithRule(AbstractTask<?> task) {
        List<AbstractTask<?>> subTasks = new ArrayList<>(this.subTasks);

        boolean wasAdded = false;
        Set<Class<? extends AbstractTask<?>>> addAfter = task.shouldAddAfter();
        Set<Class<? extends AbstractTask<?>>> addBefore = task.shouldAddBefore();
        if (!subTasks.isEmpty() && !addAfter.isEmpty()) {
            for (int i = subTasks.size() - 1; i >= 0; i--) {
                ITask<?> t = subTasks.get(i);
                if (addAfter.contains(t.getClass())) {
                    subTasks.add(i + 1, task);
                    wasAdded = true;
                    break;
                }
            }
        } else if (!subTasks.isEmpty() && !addBefore.isEmpty()) {
            for (int i = 0; i < subTasks.size(); i++) {
                ITask<?> t = subTasks.get(i);
                if (addBefore.contains(t.getClass())) {
                    subTasks.add(i, task);
                    wasAdded = true;
                    break;
                }
            }
        }
        if(!wasAdded){
            subTasks.add(task);
        }

        this.subTasks = subTasks;
    }

    public synchronized void addSubTaskFirst(AbstractTask<?> task) {
        List<AbstractTask<?>> subTasks = new ArrayList<>(this.subTasks);
        subTasks.add(0, task);

        this.subTasks = subTasks;
    }

    public synchronized void removeSubTask(AbstractTask<?> task) {
        List<AbstractTask<?>> subTasks = new ArrayList<>(this.subTasks);
        subTasks.remove(task);

        this.subTasks = subTasks;
    }

    @Override
    public void copyTo(ITask<CONFIGURATION> copy) {
        super.copyTo(copy);
        try {
            AbstractListOfTask<CONFIGURATION> _copy = (AbstractListOfTask<CONFIGURATION>) copy;

            _copy.subTasks = new ArrayList<>();
            for (AbstractTask<?> t : subTasks) {
                AbstractTask copyTask = ITask.blankOf(t.getClass(), null, this);
                t.copyTo(copyTask);
                _copy.subTasks.add(copyTask);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        boolean continueProcessing = true;
        if (state == State.RUN) {
            for (ITask<?> subTask : subTasks) {
                if (subTask.getState() != State.RUN) {
                    continue;
                }
                continueProcessing &= subTask.process(executionContext);
                if (!continueProcessing) {
                    break;
                }
            }
        }
        return continueProcessing;
    }

    @Override
    public void doInit() throws Exception {
        for (AbstractTask<?> subTask : subTasks) {
            subTask.init();
        }
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        if (!startChildren) {
            return;
        }
        for (AbstractTask<?> subTask : subTasks) {
            subTask.start(ignoreAutostartProperty, startChildren);
        }
    }

    @Override
    public void doStop() throws Exception {
        log.debug("Child shutdown started.");
        for (AbstractTask<?> subTask : subTasks) {
            subTask.stop();
        }
        log.debug("Child shutdown finished.");
    }

    public Collection<AbstractTask<?>> selfAndSubTasks() {
        Collection<AbstractTask<?>> result = new ArrayList<>(subTasks.size());
        result.add(this);
        for (AbstractTask<?> subtask : subTasks) {
            if (subtask instanceof AbstractListOfTask) {
                result.addAll(((AbstractListOfTask<?>) subtask).selfAndSubTasks());
            } else {
                result.add(subtask);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    public synchronized Set<UUID> selfDelete() {
        stop();
        Set<UUID> uuids = new HashSet<>();
        for (ITask<?> task : new ArrayList<>(subTasks)) {
            uuids.addAll(
                    task.selfDelete()
            );
        }

        uuids.addAll(super.selfDelete());
        return uuids;
    }

    @Override
    public Collection<ITask<?>> subtasksAndMe() {
        List<ITask<?>> result = new ArrayList<>();
        for (AbstractTask<?> subTask : subTasks) {
            result.addAll(subTask.subtasksAndMe());
        }
        result.addAll(super.subtasksAndMe());

        return Collections.unmodifiableCollection(result);
    }
}
