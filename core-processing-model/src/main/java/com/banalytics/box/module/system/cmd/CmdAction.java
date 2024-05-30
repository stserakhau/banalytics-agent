package com.banalytics.box.module.system.cmd;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.ActionEvent;
import com.banalytics.box.module.AbstractAction;
import com.banalytics.box.module.AbstractListOfTask;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ExecutionContext;
import com.banalytics.box.service.ScriptExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class CmdAction extends AbstractAction<CmdActionConfiguration> {

    public CmdAction(BoxEngine engine, AbstractListOfTask<?> parent) {
        super(engine, parent);
    }

    private String[] commandLines;

    private StringBuilder response = new StringBuilder(10000);

    @Override
    public void doInit() throws Exception {
        this.commandLines = configuration.commandLine.split("\n");
    }

    @Override
    protected boolean isFireActionEvent() {
        return true;
    }

    @Override
    protected boolean doProcess(ExecutionContext executionContext) throws Exception {
        return false;
    }

    @Override
    public Object uniqueness() {
        return configuration.title;
    }

    @Override
    public String doAction(ExecutionContext ctx) throws Exception {
        response.setLength(0);

        ProcessBuilder processBuilder = new ProcessBuilder();

        for (String commandLine : commandLines) {
            commandLine = commandLine.trim();
            if(commandLine.isEmpty()) {
                response.append("\n");
                continue;
            }
            response.append("Command:\n").append(commandLine).append("\n");
            String[] cmd = commandLine.split(" ");
            Process process = processBuilder.command(cmd).start();
            try (InputStream is = process.getInputStream()) {
                String result = IOUtils.toString(is);
                response.append("Result:\n").append(result).append("\n");
            }
            try (InputStream is = process.getErrorStream()) {
                String result = IOUtils.toString(is);
                response.append("Error:\n").append(result);
            }
            int exitCode = process.waitFor();
            response.append("Exit code: ").append(exitCode);
        }

        System.out.println(response.toString());

        return null;
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(ActionEvent.class);
        return events;
    }

    @Override
    public String getTitle() {
        return configuration.title;
    }
}
