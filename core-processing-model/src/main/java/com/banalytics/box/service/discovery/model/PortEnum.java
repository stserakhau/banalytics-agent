package com.banalytics.box.service.discovery.model;


public enum PortEnum {
    HTTP_80("http", 80, new String[]{"com.banalytics.box.module.onvif.discovery.OnvifAPIDiscovery"}),
    HTTP_8080("http", 8080, new String[]{"com.banalytics.box.module.onvif.discovery.OnvifAPIDiscovery"}),
    HTTPS_443("https", 443),
    HTTPS_8443("https", 8443),
    RTSP_554("rtsp", 554),
    RTSP_8554("rtsp", 8554);

    String urlPrefix;

    int port;

    String[] apiDiscoveries;

    PortEnum(int port) {
        this(null, port, null);
    }

    PortEnum(String urlPrefix, int port) {
        this(urlPrefix, port, null);
    }

    PortEnum(int port, String[] apiDiscoveries) {
        this(null, port, apiDiscoveries);
    }

    PortEnum(String urlPrefix, int port, String[] apiDiscoveries) {
        this.urlPrefix = urlPrefix;
        this.port = port;
        this.apiDiscoveries = apiDiscoveries;
    }

    public String urlPrefix() {
        return urlPrefix;
    }

    public int port() {
        return port;
    }

    public String[] apiDiscoveries() {
        return apiDiscoveries;
    }
}
