package com.banalytics.box.service.discovery;

import com.banalytics.box.service.discovery.model.Device;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.banalytics.box.service.discovery.DiscoveryUtils.ipToScan;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDiscoveryService {
    private final ThreadPoolExecutor findHostsExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private final CheckPortService checkPortService;
    private final DeviceStorage deviceStorage;

    public Set<String> availableSubnetsForSelect() throws Exception {
        return DiscoveryUtils.availableSubnets().stream()
                .map(v -> v.name() + "~" + v.name() + " (" + v.address() + "/" + v.mask() + ")")
                .collect(Collectors.toSet());
    }

    public List<DiscoveryUtils.NetworkDetails> availableSubnets() throws Exception {
        return DiscoveryUtils.availableSubnets();
    }

    private boolean scanInProgress = false;
    private SearchStage searchStage;
    /**
     * Method scan network
     */
    public void scanSubnets(String ip, String mask, int pingTimeout) throws Exception {
        if(scanInProgress) {
            return;
        }
        scanInProgress = true;
        try {
            searchStage = SearchStage.PING;

            List<String> ipToScan = ipToScan(ip, mask);
            for (String hostIP : ipToScan) {
                findHostsExecutor.execute(() -> {
                    try {
                        final InetAddress address = InetAddress.getByName(hostIP);
                        if (address.isReachable(pingTimeout)) {
                            log.info(hostIP + " is on the network");
                            Device device = deviceStorage.findByIp(hostIP);
                            if (device == null) {
                                device = deviceStorage.createWithIP(hostIP);
                            }
                        } else {
                            deviceStorage.remove(hostIP);
                        }
                    } catch (Throwable e) {
                        log.info("Failed check of address", e);
                    }
                });
            }

            while (findHostsExecutor.getActiveCount() > 0) {
                Thread.sleep(1000);
            }
            searchStage = SearchStage.CHECK_PORT;
            for (Device device : deviceStorage.findAll()) {
                if (StringUtils.isEmpty(device.getMac())) {
                    String mac = DiscoveryUtils.getMacByHost(device.getIp());
                    device.setMac(mac);
                }
                findHostsExecutor.execute(() -> {
                    checkPortService.checkPorts(device);
                });
            }

            while (findHostsExecutor.getActiveCount() > 0) {
                Thread.sleep(1000);
            }
        } finally {
            scanInProgress = false;
        }
    }

    public Result listDiscoveredDevices() {
        return new Result(
                deviceStorage.findAll(),
                scanInProgress,
                searchStage,
                findHostsExecutor.getActiveCount(),
                findHostsExecutor.getQueue().size()
        );
    }

    public record Result(Collection<Device> devices, boolean inProgress, SearchStage stage, int active, int queueSize) {
    }

    public enum SearchStage {
        PING, CHECK_PORT
    }
}
