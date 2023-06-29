package com.banalytics.box.module.onvif.thing;

import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.NetworkThing;
import com.banalytics.box.module.onvif.client.OnvifDevice;
import com.banalytics.box.module.onvif.client.Utils;
import com.banalytics.box.module.standard.Onvif;
import lombok.extern.slf4j.Slf4j;
import org.onvif.ver10.media.wsdl.Media;
import org.onvif.ver10.schema.*;
import org.onvif.ver20.ptz.wsdl.MoveAndStartTracking;
import org.springframework.core.annotation.Order;

import javax.xml.ws.Holder;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.banalytics.box.module.State.RUN;
import static com.banalytics.box.module.Thing.StarUpOrder.BUSINESS_LONG_START;

@Slf4j
@Order(BUSINESS_LONG_START)
public class OnvifThing extends AbstractThing<OnvifConfiguration> implements Onvif, NetworkThing {

    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final String RX = "rx";
    private static final String RY = "ry";
    private static final String RZ = "rz";
    private static final String SX = "sx";
    private static final String SY = "sy";
    private static final String SZ = "sz";
    private static final String DELAY = "delay";
    private static final String PRESET_ID = "presetId";

    private OnvifDevice onvif;

    @Override
    public String getTitle() {
        return configuration.title;
    }

    @Override
    public Object uniqueness() {
        return configuration.host + ":" + configuration.port;
    }

    @Override
    public String streamURI(String profile) {
        StreamSetup streamSetup = new StreamSetup();
        streamSetup.setStream(configuration.streamType);
        Transport transport = new Transport();
        transport.setProtocol(configuration.transportProtocol);
        streamSetup.setTransport(transport);
        Media media = onvif.media();
        MediaUri mediaUri = media.getStreamUri(streamSetup, profile);
        String uri = mediaUri.getUri();

        if (configuration.overrideRtspPort == null || configuration.overrideRtspPort < 1) {
            int beginOfIp = uri.indexOf("://") + 3;
            int endOfIp = uri.indexOf(":", beginOfIp);
            uri = uri.substring(0, beginOfIp)
                            + configuration.username + ":" + configuration.password + "@"
                            + configuration.host +
                            uri.substring(endOfIp);
        } else {
            int beginOfIp = uri.indexOf("://") + 3;
            int endOfIp = uri.indexOf(":", beginOfIp);
            int endOfPort = uri.indexOf("/", endOfIp);
            uri = uri.substring(0, beginOfIp)
                            + configuration.username + ":" + configuration.password + "@"
                            + configuration.host
                            + ":"
                            + configuration.overrideRtspPort +
                            uri.substring(endOfPort);
        }

        return uri;
    }

    @Override
    public MediaParams mediaParams(String profileToken) {
        if (profiles == null) {
            throw new RuntimeException("error.thing.notInitialized");
        }
        Profile p = profiles.get(profileToken);
        VideoResolution resolution = p.getVideoEncoderConfiguration().getResolution();
        return new MediaParams(resolution.getWidth(), resolution.getHeight());
    }

    @Override
    public boolean hasVideo(String profileToken) {
        if (profiles == null) {
            throw new RuntimeException("error.thing.notInitialized");
        }
        Profile p = profiles.get(profileToken);
        return p.getVideoEncoderConfiguration() != null;
    }

    @Override
    public boolean hasAudio(String profileToken) {
        if (profiles == null) {
            throw new RuntimeException("error.thing.notInitialized");
        }
        Profile p = profiles.get(profileToken);
        return p.getAudioEncoderConfiguration() != null;
    }

    @Override
    public String getUsername() {
        return configuration.getUsername();
    }

    @Override
    public String getPassword() {
        return configuration.getPassword();
    }

    @Override
    public boolean supportsPTZ() {
        return onvif.ptz() != null;
    }

    public OnvifThing(BoxEngine metricDeliveryService) {
        super(metricDeliveryService);
    }

    private String defaultProfileToken;

    private Map<String, Profile> profiles;

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
        this.onvif = new OnvifDevice(
                configuration.host,
                configuration.port,
                configuration.username,
                configuration.password/*,
                configuration.timeType*/
        );

        Media media = onvif.media();
        if (media == null) {
            throw new Exception("error.thing.notInitialized");
        }
        List<Profile> profiles = media.getProfiles();

        this.profiles = profiles.stream().collect(Collectors.toMap(Profile::getToken, Function.identity()));

        if (!profiles.isEmpty()) {
            Profile p = profiles.get(0);
            defaultProfileToken = p.getToken();
        }
    }

    @Override
    protected void doStop() throws Exception {
    }

    @Override
    public Map<String, ?> options() {
        Map<String, Object> options = new HashMap<>();
        if (onvif != null) {
            if (onvif.ptz() != null) {
                options.put("ptz", true);
            }
            if (hasAudio(defaultProfileToken)) {
                options.put("audio", true);
            }
        }
        return options;
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
        if (onvif != null && onvif.ptz() != null) {
            return Set.of(
                    "ptz*",
                    "updatePtz*"
            );
        } else {
            return Set.of();
        }
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        if (getState() != RUN) {
            throw new Exception("error.thing.notInitialized");
        }
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);
        switch (method) {
            case "readProfiles": {
                return profiles();
            }
            case "readProfilesList": {
                List<String> p = new ArrayList<>();
                if (profiles != null) {
                    profiles.forEach((k, v) -> {
                        VideoEncoderConfiguration vec = v.getVideoEncoderConfiguration();
                        VideoEncoding ve = vec.getEncoding();
                        VideoResolution vr = vec.getResolution();
                        p.add(k + "~" + vr.getWidth() + "x" + vr.getHeight() + ' ' + ve.value());
                    });
                }
                return p;
            }
            case "readProfilesMap": {
                Map<String, String> map = new HashMap<>();
                profiles.forEach((k, v) -> {
                    map.put(k, v.getName());
                });
                return map;
            }
            case "ptzAbsoluteMove":
                absoluteMove(
                        ((Number) params.get(X)).floatValue(),
                        ((Number) params.get(Y)).floatValue(),
                        ((Number) params.get(Z)).floatValue(),
                        ((Number) params.get(SX)).floatValue(),
                        ((Number) params.get(SY)).floatValue(),
                        ((Number) params.get(SZ)).floatValue()
                );
                return Collections.emptyMap();
            case "ptzRotateRelative":
                rotateRelative(
                        ((Number) params.get(RX)).floatValue(),
                        ((Number) params.get(RY)).floatValue(),
                        ((Number) params.get(RZ)).floatValue(),
                        ((Number) params.get(SX)).floatValue(),
                        ((Number) params.get(SY)).floatValue(),
                        ((Number) params.get(SZ)).floatValue()
                );
                return Collections.emptyMap();
            case "ptzRotateContinuouslyWithDelay":
                rotateContinuouslyWithDelay(
                        ((Number) params.get(SX)).floatValue(),
                        ((Number) params.get(SY)).floatValue(),
                        ((Number) params.get(SZ)).floatValue(),
                        ((Number) params.get(DELAY)).intValue()
                );
                return Collections.emptyMap();
            case "ptzRotateContinuouslyStart":
                rotateContinuouslyStart(
                        ((Number) params.get(SX)).floatValue(),
                        ((Number) params.get(SY)).floatValue(),
                        ((Number) params.get(SZ)).floatValue()
                );
                return Collections.emptyMap();
            case "ptzRotateContinuouslyStop":
                rotateContinuouslyStop();
                return Collections.emptyMap();
            case "ptzPresets":
                return presets();
            case "ptzGotoHome":
                gotoHome();
                return Collections.emptyMap();
            case "updatePtzHome":
                saveHome();
                return Collections.emptyMap();
            case "updatePtzPreset":
                String presetId = params.get(PRESET_ID).toString();
                savePreset(presetId);
                return Collections.emptyMap();
            case "ptzGotoPreset":
                gotoPreset(
                        params.get(PRESET_ID).toString(),
                        ((Number) params.get(SX)).floatValue(),
                        ((Number) params.get(SY)).floatValue(),
                        ((Number) params.get(SZ)).floatValue()
                );
                return Collections.emptyMap();
            default:
                throw new Exception("Method not supported: " + method);
        }
    }

    public Map<String, Profile> profiles() {
        return this.profiles;
    }

    public void absoluteMove(
            float x, float y, float z,
            float sx, float sy, float sz
    ) {
        onvif.ptz().absoluteMove(
                defaultProfileToken,
                Utils.buildPTZVector(x, y, z),
                Utils.buildPTZSpeed(sx, sy, sz)
        );
    }

    public void rotateRelative(
            float rx, float ry, float rz,
            float sx, float sy, float sz
    ) {
        onvif.ptz().relativeMove(
                defaultProfileToken,
                Utils.buildPTZVector(rx, ry, rz),
                Utils.buildPTZSpeed(sx, sy, sz)
        );
    }

    //     onvif.media.getSnapshotUri(profileToken)
//    StreamSetup ss = new StreamSetup();
//    ss.setStream(StreamType.RTP_MULTICAST);
//    Transport t = new Transport();
//    t.setProtocol(TransportProtocol.HTTP);
//    ss.setTransport(t);
//    onvif.media.getStreamUri(ss, profileToken)
    private final Executor moveExecutor = Executors.newSingleThreadExecutor();

    public void rotateContinuouslyWithDelay(
            float sx, float sy, float sz,
            int delay
    ) {
        moveExecutor.execute(() -> {
            onvif.ptz().continuousMove(
                    defaultProfileToken,
                    Utils.buildPTZSpeed(sx, sy, sz),
                    null
            );
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            } finally {
                onvif.ptz().stop(defaultProfileToken, true, true);
            }
        });
    }

    public void rotateContinuouslyStart(float sx, float sy, float sz) {
        onvif.ptz().continuousMove(
                defaultProfileToken,
                Utils.buildPTZSpeed(sx, sy, sz),
                null
        );
    }

    public void rotateContinuouslyStop() {
        onvif.ptz().stop(defaultProfileToken, true, true);
    }

    public List<String> presets() {
        List<PTZPreset> presets = onvif.ptz().getPresets(defaultProfileToken);

        return presets.stream()
                .map(p -> p.getToken() + '~' + p.getName())
                .collect(Collectors.toList());
    }

    public void savePreset(String presetId) {
        //defaultProfileToken;
        Holder<String> presetToken = new Holder<>();
        onvif.ptz().setPreset(defaultProfileToken, presetId, presetToken);
        log.info("Set preset {}", presetToken.value);
    }

    @Override
    public void gotoPreset(
            String presetToken,
            float sx, float sy, float sz
    ) throws Exception {
        log.info("Moving {} to Preset {}", configuration.title, presetToken);
        if (onvif.ptz() == null) {
            throw new Exception("error.thing.notInitialized");
        }
        onvif.ptz().gotoPreset(
                defaultProfileToken,
                presetToken,
                Utils.buildPTZSpeed(sx, sy, sz)
        );
    }

    @Override
    public PTZ ptzState() throws Exception {
        MoveAndStartTracking m = new MoveAndStartTracking();

        PTZStatus ptz = onvif.ptz().getStatus(defaultProfileToken);
        PTZVector vector = ptz.getPosition();
        Vector2D panTilt = vector.getPanTilt();
        Vector1D zoom = vector.getZoom();

        PTZMoveStatus moveStatus = ptz.getMoveStatus();

        return new PTZ(
                panTilt.getX(), panTilt.getY(), zoom.getX(),
                moveStatus.getPanTilt() == MoveStatus.MOVING,
                moveStatus.getZoom() == MoveStatus.MOVING
        );
    }

    public void gotoHome() {
        log.info("Goto home");
        onvif.ptz().gotoHomePosition(
                defaultProfileToken,
                Utils.buildPTZSpeed(1, 1, 1)
        );
    }

    public void saveHome() {
        log.info("Save home position");
        onvif.ptz().setHomePosition(defaultProfileToken);
    }

    @Override
    public String ipAddress() {
        return configuration.host;
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
}
