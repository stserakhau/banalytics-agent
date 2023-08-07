package com.banalytics.box.module.webrtc.client.channel;

import com.banalytics.box.module.cloud.portal.PortalIntegrationConfiguration;
import com.banalytics.box.module.cloud.portal.suc.SoftwareUpgradeCenterConfiguration;
import com.banalytics.box.module.events.EventHistoryThingConfig;
import com.banalytics.box.module.network.DeviceDiscoveryConfiguration;
import com.banalytics.box.module.storage.filesystem.ServerLocalFileSystemNavigatorConfig;
import com.banalytics.box.module.system.agent.JVMConfiguration;
import com.banalytics.box.module.system.monitor.SystemMonitorConfiguration;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationConfiguration;

import java.util.Set;
import java.util.UUID;

public interface Constants {
    Set<UUID> ALWAYS_REQUIRED_THINGS_UUID_SET = Set.of(
            JVMConfiguration.THING_UUID,
            DeviceDiscoveryConfiguration.THING_UUID,
//            EventHistoryThingConfig.THING_UUID,
            PortalIntegrationConfiguration.THING_UUID,
            PortalWebRTCIntegrationConfiguration.WEB_RTC_UUID,
            SoftwareUpgradeCenterConfiguration.THING_UUID,
            ServerLocalFileSystemNavigatorConfig.SERVER_LOCAL_FS_NAVIGATOR_UUID
//            SystemMonitorConfiguration.THING_UUID
    );
    Set<UUID> MODIFICATION_DISABLED_THINGS_UUID_SET = Set.of(
            PortalIntegrationConfiguration.THING_UUID,
            SoftwareUpgradeCenterConfiguration.THING_UUID,
            PortalWebRTCIntegrationConfiguration.WEB_RTC_UUID
    );
}
