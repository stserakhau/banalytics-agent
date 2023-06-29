package com.banalytics.box.module.media.thing;

import com.banalytics.box.module.*;
import com.banalytics.box.module.constants.MediaFormat;
import com.banalytics.box.module.media.task.ffmpeg.SimpleRTSPGrabberTask;
import com.banalytics.box.module.standard.UrlMediaStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.banalytics.box.module.Thing.StarUpOrder.*;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Order(BUSINESS_LONG_START)
public class UrlMediaStreamThing extends AbstractThing<UrlMediaStreamThingConfiguration> implements UrlMediaStream, NetworkThing, AutoAddTasks {
    public UrlMediaStreamThing(BoxEngine engine) {
        super(engine);
    }

    @Override
    public String getTitle() {
        return configuration.title;
    }

    @Override
    public Object uniqueness() {
        return configuration.host + ":" + configuration.port + "/" + configuration.path;
    }

    @Override
    public String getUrl() {
        StringBuilder sb = new StringBuilder(255);
        sb.append(configuration.schema.name());
        sb.append("://");
        if (isNotEmpty(configuration.username)) {
            sb.append(configuration.username).append(":");
            if (isNotEmpty(configuration.password)) {
                sb.append(configuration.password);
            }
            sb.append("@");
        }
        sb.append(configuration.host).append(':').append(configuration.port);
        if (isNotEmpty(configuration.path)) {
            sb.append(configuration.path);
        }
        return sb.toString();
    }

    @Override
    public MediaFormat getStreamFormat() {
        return switch (configuration.schema) {
            case rtsp -> MediaFormat.rtsp;
            case http, https -> configuration.getStreamFormat();
        };
    }

    @Override
    public Set<String> generalPermissions() {
        Set<String> p = new HashSet<>(super.generalPermissions());
        p.add(PERMISSION_VIDEO);
        return p;
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of();
    }

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }

    @Override
    public String ipAddress() {
        return configuration.getHost();
    }

    @Override
    public String macAddress() {
        return configuration.getMac();
    }

    @Override
    public void onIpChanged(String newIp) {
        configuration.setHost(newIp);
    }

    @Override
    public void onMacFound(String mac) {
        configuration.setMac(mac);
    }

    @Override
    public Collection<AbstractTask<?>> autoAddTasks() {
        SimpleRTSPGrabberTask mediaGrabber = new SimpleRTSPGrabberTask(engine, engine.getPrimaryInstance());
        mediaGrabber.configuration.urlUuid = this.getUuid();
        if (configuration.getStreamFormat() == MediaFormat.mjpeg) {
            mediaGrabber.configuration.fpsControl = 5;
        } else {
            mediaGrabber.configuration.fpsControl = 0;
        }
        return List.of(mediaGrabber);
    }


}
