package com.banalytics.box.module.system.agent;

import com.banalytics.box.LocalizedException;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.module.IConfiguration;
import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import lombok.Setter;

import java.lang.management.ManagementFactory;
import java.util.UUID;

@Getter
@Setter
public class JVMConfiguration implements IConfiguration {
    public static UUID THING_UUID = UUID.fromString("00000000-0000-0000-0000-000000000009");

    @Override
    public UUID getUuid() {
        return THING_UUID;
    }

    @Override
    public void setUuid(UUID uuid) {
    }

    @UIComponent(index = 10, type = ComponentType.drop_down, required = true)
    public MemSize xms = MemSize.M300; //started with memory allocation

    @UIComponent(index = 20, type = ComponentType.drop_down, required = true)
    public MemSize xmx = MemSize.G1; //maximum memory allocation

    @Override
    public void validate() throws Exception {
        if (xms.sizeInBytes > xmx.sizeInBytes) {
            throw new LocalizedException("error.xmsGreatXmx");
        }
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long freeMemory = operatingSystemMXBean.getFreeMemorySize();
        if (xmx.sizeInBytes > freeMemory) {
            throw new LocalizedException("error.xmxGreatFreeMem", freeMemory);
        }
    }

    public enum MemSize {
        M300("300m", 300 * 1024 * 1024),
        M512("512m", 512 * 1024 * 1024),
        M768("768m", 768 * 1024 * 1024),
        G1("1g", 1024 * 1024 * 1024L),
        G2("2g", 2 * 1024 * 1024 * 1024L),
        G3("3g", 3 * 1024 * 1024 * 1024L),
        G4("4g", 4 * 1024 * 1024 * 1024L);

        public final String value;
        public final long sizeInBytes;

        MemSize(String value, long sizeInBytes) {
            this.value = value;
            this.sizeInBytes = sizeInBytes;
        }
    }


}
