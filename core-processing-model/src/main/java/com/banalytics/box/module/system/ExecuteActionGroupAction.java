package com.banalytics.box.module.system;

import com.banalytics.box.api.integration.utils.CommonUtils;
import com.banalytics.box.module.*;
import com.banalytics.box.service.SystemThreadsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ExecuteActionGroupAction extends AbstractAction<ExecuteActionGroupActionConfiguration> {
    public ExecuteActionGroupAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    private final List<IAction> actionsGroup = new ArrayList<>();

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        if (configuration.parallelExecution) {
            for (IAction action : actionsGroup) {
                SystemThreadsService.execute(this, () -> {
                    try {
                        action.action(executionContext);
                    } catch (Throwable e) {
                        onWarnException(e);
                    }
                });
            }
        } else {
            for (IAction action : actionsGroup) {
                try {
                    SystemThreadsService.execute(this, () -> {
                        try {
                            action.action(executionContext);
                        } catch (Throwable e) {
                            onWarnException(e);
                        }
                    });
                    if (configuration.executionDelayMillis > 0) {
                        Thread.sleep(configuration.executionDelayMillis);
                    }
                } catch (Throwable e) {
                    onWarnException(e);
                }
            }
        }
        return false;
    }

    @Override
    public String doAction(ExecutionContext ctx) throws Exception {
        this.process(ctx);

        return null;
    }

    @Override
    public String getTitle() {
        return configuration.title;
    }

    @Override
    public void doStart(boolean ignoreAutostartProperty, boolean startChildren) throws Exception {
        actionsGroup.clear();
        if (StringUtils.isNotEmpty(configuration.fireActionsUuids)) {
            List<String> uuids = CommonUtils.DEFAULT_OBJECT_MAPPER.readValue(configuration.fireActionsUuids, ConverterTypes.TYPE_LIST_STRING);
            for (String uuid : uuids) {
                actionsGroup.add(
                        engine.findTask(UUID.fromString(uuid))
                );
            }
        }
    }
}
