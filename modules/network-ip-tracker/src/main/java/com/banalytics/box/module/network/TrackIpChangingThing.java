package com.banalytics.box.module.network;

import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.NetworkThing;
import com.banalytics.box.module.Thing;
import com.banalytics.box.service.SystemThreadsService;
import com.banalytics.box.service.discovery.DeviceDiscoveryService;
import com.banalytics.box.service.discovery.DiscoveryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.banalytics.box.module.Thing.StarUpOrder.CORE;
import static com.banalytics.box.module.Thing.StarUpOrder.INTEGRATION;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;
import static com.banalytics.box.service.discovery.DiscoveryUtils.getArpIpMacMap;
import static com.banalytics.box.service.discovery.DiscoveryUtils.getArpMacIpMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Order(INTEGRATION)
public class TrackIpChangingThing extends AbstractThing<TrackIpChangingConfiguration> {
    private DeviceDiscoveryService deviceDiscoveryService;

    public TrackIpChangingThing(BoxEngine engine) {
        super(engine);
    }

    @Override
    public Object uniqueness() {
        return configuration.networkInterface;
    }

    @Override
    protected void doInit() throws Exception {
    }

    private ThreadPoolExecutor pingExecutor;
    private TimerTask pingArpScanTask;

    @Override
    protected void doStart() throws Exception {
        deviceDiscoveryService = engine.getBean(DeviceDiscoveryService.class);

        if (StringUtils.isEmpty(configuration.networkInterface)) {
            return;
        }

        List<DiscoveryUtils.NetworkDetails> interfaces = deviceDiscoveryService.availableSubnets();
        int selectedInterfaceIndex = -1;

        for (int i = 0; i < interfaces.size(); i++) {
            DiscoveryUtils.NetworkDetails iface = interfaces.get(i);
            if (iface.name().equals(configuration.networkInterface)) {
                selectedInterfaceIndex = i;
                break;
            }
        }

        if (selectedInterfaceIndex == -1) {
            throw new Exception("Network Interface not found: " + configuration.networkInterface);
        }

        final DiscoveryUtils.NetworkDetails selectedInterface = interfaces.get(selectedInterfaceIndex);

        arpAfterPing(selectedInterface);
    }

    @Override
    protected void doStop() throws Exception {
        if (pingArpScanTask != null) {
            pingArpScanTask.cancel();
            pingArpScanTask = null;
            pingExecutor.shutdownNow();
        }
    }

    @Override
    public String getTitle() {
        return configuration.getNetworkInterface();
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        throw new Exception("Api not supporting");
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of();
    }

    public void arpAfterPing(DiscoveryUtils.NetworkDetails selectedInterface) throws Exception {
        pingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(configuration.pingThreadPoolSize);
        pingArpScanTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    List<Thing<?>> networkThings = engine.findThings(NetworkThing.class);

                    final Map<String, String> currentMacIpAssocs = new HashMap<>();
                    Set<NetworkThing> reachableThings = new HashSet<>();
                    Set<NetworkThing> unreachableThings = new HashSet<>();
                    for (Thing<?> t : networkThings) { // split to reachable and unreachable things
                        NetworkThing nt = (NetworkThing) t;
                        String mac = nt.macAddress();
                        String ip = nt.ipAddress();
                        currentMacIpAssocs.put(mac, ip);

                        InetAddress address = InetAddress.getByName(ip);
                        if (address.isReachable(configuration.hostReachableTimeoutSec * 1000)) {
                            reachableThings.add(nt);
                        } else {
                            unreachableThings.add(nt);
                        }
                    }

                    Set<String> reachableIps = Set.of();
                    if (!unreachableThings.isEmpty()) {//if unreachable things exists, refresh arp table via ping
                        String addr = selectedInterface.address();
                        String mask = selectedInterface.mask();
                        reachableIps = DiscoveryUtils.pingMyNetwork(addr, mask);
                    }

                    List<Thing<?>> thingsToRestart = new ArrayList<>();
                    boolean instanceChanged = false;
                    {
                        {// apply mac if found but not configured in thing
                            Map<String, String> ipMacMap = getArpIpMacMap();
                            for (NetworkThing reachableThing : reachableThings) {
                                if (StringUtils.isEmpty(reachableThing.macAddress())) {
                                    String mac = ipMacMap.get(reachableThing.ipAddress());
                                    if (isNotEmpty(mac)) {
                                        reachableThing.onMacFound(mac);
                                        instanceChanged = true;
                                    }
                                }
                            }
                        }

                        if (!unreachableThings.isEmpty()) {// for unreachable things change ip if was changed
                            List<Pair<String, String>> macIpsAssoc = getArpMacIpMap();

                            for (NetworkThing unReachableThing : unreachableThings) {
                                String mac = unReachableThing.macAddress();
                                if (isNotEmpty(mac)) {
                                    for (Pair<String, String> macIpAssoc : macIpsAssoc) {
                                        if (macIpAssoc.getLeft().equals(mac) && reachableIps.contains(macIpAssoc.getRight())) {
                                            unReachableThing.onIpChanged(macIpAssoc.getRight());
                                            instanceChanged = true;
                                            thingsToRestart.add((Thing<?>) unReachableThing);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (instanceChanged) {
                        engine.persistPrimaryInstance();
                    }

                    for (Thing<?> thing : thingsToRestart) {
                        SystemThreadsService.execute(this, thing::restart);
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
        SYSTEM_TIMER.schedule(pingArpScanTask, 30000, configuration.scanTimeSec);
    }
}
