package com.banalytics.box.model.task;

import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.AbstractTask;
import com.banalytics.box.module.IAction;
import com.banalytics.box.module.ITask;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.banalytics.box.module.utils.Utils.isSupportsMediaStream;

@Getter
public class EnvironmentNode {
    private final UUID uuid;
    private final String title;
    private final String nodeClass;
    private final boolean hasMediaStream;
    private final boolean action;
    private final List<EnvironmentNode> subNodes = new ArrayList<>(3);

    public EnvironmentNode(UUID uuid, String title, String nodeClass, boolean hasMediaStream, boolean action) {
        this.uuid = uuid;
        this.title = title;
        this.nodeClass = nodeClass;
        this.hasMediaStream = hasMediaStream;
        this.action = action;
    }

    public static EnvironmentNode build(ITask<?> task) {
        EnvironmentNode node = new EnvironmentNode(
                task.getUuid(), task.getTitle(),
                task.getSelfClassName(), isSupportsMediaStream(task.getClass()),
                task instanceof IAction
        );

        if (task instanceof AbstractListOfTask<?> listOfTask) {
            for (AbstractTask<?> subTask : listOfTask.getSubTasks()) {
                EnvironmentNode subNode = build(subTask);
                node.subNodes.add(subNode);
            }
        }

        return node;
    }
}
