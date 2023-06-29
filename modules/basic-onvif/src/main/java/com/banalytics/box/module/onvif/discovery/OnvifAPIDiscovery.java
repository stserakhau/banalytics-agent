package com.banalytics.box.module.onvif.discovery;

import com.banalytics.box.service.discovery.api.APIDiscovery;
import com.banalytics.box.service.discovery.model.Device;
import com.banalytics.box.service.discovery.model.PortEnum;
import com.banalytics.box.service.discovery.model.api.APIEnum;
import com.banalytics.box.module.onvif.client.Utils;

import javax.xml.ws.BindingProvider;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.onvif.ver10.schema.*;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
public class OnvifAPIDiscovery implements APIDiscovery {
    private static final String DEVICE_SERVICE = "/onvif/device_service";

    public void discovery(Device device, PortEnum port) {
        final String ip = device.getIp();
        final String deviceUrl = port.urlPrefix() + "://" + ip + ":" + port.port();

        Capabilities capabilities;

//        final Thread currentThread = Thread.currentThread();
//        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        try {
//            final ClassLoader targetClassLoader = JaxWsProxyFactoryBean.class.getClassLoader();
//            currentThread.setContextClassLoader(targetClassLoader);
            org.onvif.ver10.device.wsdl.Device d = Utils.getServiceProxy(
                    (BindingProvider) javax.xml.ws.Service.create(
                            new QName("http://www.onvif.org/ver10/device/wsdl", "DeviceService")
                    ).getPort(new QName("http://www.onvif.org/ver10/device/wsdl", "DevicePort"), org.onvif.ver10.device.wsdl.Device.class),
                    deviceUrl + DEVICE_SERVICE
            ).create(org.onvif.ver10.device.wsdl.Device.class);
            capabilities = d.getCapabilities(List.of(CapabilityCategory.ALL));
        } catch (Exception e) {
            log.info("Device '"+deviceUrl+"' doesn't support Onvif.", e);
            return;
//        } finally {
//            currentThread.setContextClassLoader(contextClassLoader);
        }

        Set<String> caps = device.getApiCapabilities().computeIfAbsent(APIEnum.ONVIF, apiEnum -> new HashSet<>());
        caps.add(port.name());

        AnalyticsCapabilities analyticsCapabilities = capabilities.getAnalytics();
        if (analyticsCapabilities != null) {
            caps.add(APIEnum.OnvifCapability.ANALYTICS.name());
        }
        DeviceCapabilities deviceCapabilities = capabilities.getDevice();
        if (deviceCapabilities != null) {
            caps.add(APIEnum.OnvifCapability.DEVICE.name());
        }
        EventCapabilities eventCapabilities = capabilities.getEvents();
        if (eventCapabilities != null) {
            caps.add(APIEnum.OnvifCapability.EVENT.name());
        }
        ImagingCapabilities imagingCapabilities = capabilities.getImaging();
        if (imagingCapabilities != null) {
            caps.add(APIEnum.OnvifCapability.IMAGING.name());
        }
        MediaCapabilities mediaCapabilities = capabilities.getMedia();
        if (mediaCapabilities != null) {
            caps.add(APIEnum.OnvifCapability.MEDIA.name());
        }
        PTZCapabilities ptzCapabilities = capabilities.getPTZ();
        if (ptzCapabilities != null) {
            caps.add(APIEnum.OnvifCapability.PTZ.name());
        }
        CapabilitiesExtension capabilitiesExtension = capabilities.getExtension();
        if (capabilitiesExtension != null) {
            caps.add(APIEnum.OnvifCapability.EXTENSION.name());
        }
    }
}
