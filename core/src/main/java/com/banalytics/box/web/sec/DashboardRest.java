package com.banalytics.box.web.sec;

import com.banalytics.box.module.cloud.portal.PortalIntegrationConfiguration;
import com.banalytics.box.module.cloud.portal.PortalIntegrationThing;
import com.banalytics.box.service.TaskService;
import com.banalytics.box.service.discovery.DiscoveryUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/secured/dashboard")
public class DashboardRest {
    private final TaskService taskService;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public DashboardView dashboard() throws Exception {
        PortalIntegrationThing portalIntegrationThing = taskService.getThing(PortalIntegrationConfiguration.THING_UUID);

        return new DashboardView(
                portalIntegrationThing.getEnvironmentUUID(),
                DiscoveryUtils.availableSubnets()
        );
    }

    @RequiredArgsConstructor
    @Getter
    static class DashboardView {
        final UUID productUuid;

        final List<DiscoveryUtils.NetworkDetails> networkDetails;
    }
}
