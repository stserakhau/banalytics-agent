package com.banalytics.box.module.media.thing;

import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.module.*;
import com.banalytics.box.module.media.task.ffmpeg.LocalMediaGrabberTask;
import com.banalytics.box.module.standard.LocalMediaStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;

import java.util.*;

import static com.banalytics.box.module.State.RUN;

@Order(Thing.StarUpOrder.BUSINESS)
public class LocalMediaThing extends AbstractThing<LocalMediaThingConfig> implements LocalMediaStream, AutoAddTasks {
    public LocalMediaThing(BoxEngine engine) {
        super(engine);
    }

    @Override
    public Object uniqueness() {
        return configuration.camera;
    }

    @Override
    public String getTitle() {
        return configuration.title;
    }

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    public Map<String, ?> options() {
        Map<String, Object> options = new HashMap<>();
        if (StringUtils.isNotEmpty(this.configuration.microphone)) {
            options.put("audio", true);
        }
        return options;
    }

    @Override
    protected void doStop() throws Exception {

    }

    @Override
    public Collection<AbstractTask<?>> autoAddTasks() {
        LocalMediaGrabberTask mediaGrabber = new LocalMediaGrabberTask(engine, engine.getPrimaryInstance());
        mediaGrabber.configuration.localMediaUuid = this.getUuid();
        return List.of(mediaGrabber);
    }

    @Override
    public Set<String> generalPermissions() {
        Set<String> p = new HashSet<>(super.generalPermissions());
        p.add(PERMISSION_VIDEO);
        p.add(PERMISSION_AUDIO);
        return p;
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of();
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        if (getState() != RUN) {
            throw new Exception("error.thing.notInitialized");
        }
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);
        switch (method) {
            case "readResolutions" -> {
                //bean:localMediaDeviceDiscoveryService:cameraResolutionFpsCases
                // camera
                return engine.serviceCall("localMediaDeviceDiscoveryService",
                        "cameraResolutionFpsCases",
                        configuration.camera
                );
            }
            case "readMicrophones" -> {
                //bean:localMediaDeviceDiscoveryService:cameraResolutionFpsCases
                // camera
                return engine.serviceCall("localMediaDeviceDiscoveryService",
                        "audioSupportedSampleRates",
                        configuration.microphone
                );
            }
            default -> throw new Exception("Method not supported: " + method);
        }
    }
}