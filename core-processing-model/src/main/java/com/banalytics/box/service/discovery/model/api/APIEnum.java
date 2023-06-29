package com.banalytics.box.service.discovery.model.api;

public enum APIEnum {
    ONVIF;

    public enum OnvifCapability {
        ANALYTICS, DEVICE, EVENT, IMAGING, MEDIA, PTZ, EXTENSION
    }
}
