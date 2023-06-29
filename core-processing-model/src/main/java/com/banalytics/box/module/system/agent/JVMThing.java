package com.banalytics.box.module.system.agent;

import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.Thing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.io.FileWriter;
import java.util.Set;

@Slf4j
@Order(Thing.StarUpOrder.CORE)
public class JVMThing extends AbstractThing<JVMConfiguration> implements Singleton {
    private File vmOptionsFile;

    @Override
    public String getTitle() {
        return getSelfClassName();
    }

    public JVMThing(BoxEngine metricDeliveryService) {
        super(metricDeliveryService);
    }

    @Override
    protected void doInit() throws Exception {
        File applicationConfigFolder = engine.applicationConfigFolder();
        this.vmOptionsFile = new File(applicationConfigFolder, "banalytics.vmoptions");
    }

    @Override
    public void doStart() throws Exception {
    }

    @Override
    public void onSave() throws Exception {
        try (FileWriter fw = new FileWriter(vmOptionsFile)) {
            fw.write("-Xmx" + configuration.xmx.value + "\n");
            fw.write("-Xms" + configuration.xms.value + "\n");
        }
    }

    @Override
    public void doStop() throws Exception {
    }

    @Override
    public Set<String> generalPermissions() {
        return super.generalPermissions();
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of();
    }
}
