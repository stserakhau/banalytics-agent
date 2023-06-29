package com.banalytics.box.module.system.monitor;

import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.SystemMonitorEvent;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.Thing;
import com.banalytics.box.service.utility.TrafficControl;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;

import static com.banalytics.box.module.utils.Utils.nodeType;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
@Order(Thing.StarUpOrder.CORE)
public class SystemMonitorThing extends AbstractThing<SystemMonitorConfiguration> implements Singleton {
    @Override
    public String getTitle() {
        return getSelfClassName();
    }

    public SystemMonitorThing(BoxEngine metricDeliveryService) {
        super(metricDeliveryService);
    }

    private SystemMonitorEvent systemMonitorEvent;
    private TimerTask sysMonTask;

    @Override
    protected void doInit() throws Exception {
    }

    long dataAccumulationTimeout = 0;
    // https://www.baeldung.com/java-metrics

    int counter;
    double cpuLoad = 0;
    double processCpuLoad = 0;

    @Override
    public void doStart() throws Exception {
        systemMonitorEvent = new SystemMonitorEvent(
                nodeType(this.getClass()),
                this.getUuid(),
                getSelfClassName(),
                getTitle());

        Map<String, Object> general = systemMonitorEvent.getGeneral();
        Map<String, Object> cpu = systemMonitorEvent.getCpu();
        Map<String, Object> ram = systemMonitorEvent.getRam();
        Map<String, Object> disk = systemMonitorEvent.getDisk();
        Map<String, Object> net = systemMonitorEvent.getNet();

        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        FileSystemView fsv = FileSystemView.getFileSystemView();

        this.sysMonTask = new TimerTask() {
            @Override
            public void run() {
                counter++;
                cpuLoad += operatingSystemMXBean.getCpuLoad();
                processCpuLoad += operatingSystemMXBean.getProcessCpuLoad();

                if (counter % configuration.updateSpeed == 0) {
                    double avgCpu = cpuLoad / configuration.updateSpeed;
                    double avgProcessCpu = processCpuLoad / configuration.updateSpeed;
                    systemMonitorEvent.setMessageUuid(UUID.randomUUID());

                    general.clear();

                    cpu.clear();
                    ram.clear();
                    disk.clear();
                    net.clear();

                    general.put("jvmUpTime", runtimeMXBean.getUptime());
                    general.put("threadCount", threadMXBean.getThreadCount());
                    general.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
                    general.put("peakThreadCount", threadMXBean.getPeakThreadCount());
                    general.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());

                    cpu.put("cpuLoad", avgCpu);
                    cpu.put("processCpuLoad", avgProcessCpu);

                    ram.put("freeMemorySize", operatingSystemMXBean.getFreeMemorySize());
                    ram.put("totalMemorySize", operatingSystemMXBean.getTotalMemorySize());
                    ram.put("heapMemoryUsage", memoryMXBean.getHeapMemoryUsage());

                    net.put("outbound", TrafficControl.INSTANCE.outboundTraffic.get());

                    {
                        File[] roots = File.listRoots();
                        for (File f : roots) {
                            String name = f.getPath();
                            disk.put(
                                    name,
                                    Map.of(
                                            "totalSpace", f.getTotalSpace(),
                                            "usableSpace", f.getUsableSpace(),
                                            "isDrive", fsv.isDrive(f)
                                    )
                            );
                        }
                    }
                    engine.fireEvent(systemMonitorEvent);

                    cpuLoad = 0;
                    processCpuLoad = 0;
                }
//
//                log.info("======================================================");
//                log.info("================= Executors stats ====================");
//
//                log.info("Tasks: {}\nActive: {}\nPool size: {}", SYSTEM_EXECUTOR.getTaskCount(), SYSTEM_EXECUTOR.getActiveCount(), SYSTEM_EXECUTOR.getPoolSize());
//                log.info("Task execution metrics:\n{}", SystemThreadsService.EXECUTION_METRICS);
//
//                log.info("======================================================");
//                log.info("======================================================");
            }
        };

        SYSTEM_TIMER.schedule(sysMonTask, 0, 1000L);
    }

    @Override
    public void doStop() throws Exception {
        sysMonTask.cancel();
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(SystemMonitorEvent.class);
        return events;
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
