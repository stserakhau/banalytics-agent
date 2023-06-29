package com.banalytics.box.module.standard;

public interface Onvif {
    String streamURI(String profileToken);

    MediaParams mediaParams(String profileToken);

    boolean hasVideo(String profileToken);

    boolean hasAudio(String profileToken);

    String getUsername();

    String getPassword();

    boolean supportsPTZ();

    void gotoPreset(String presetToken, float sx, float sy, float sz) throws Exception;

    PTZ ptzState() throws Exception ;

    public record MediaParams(int width, int height) {
    }

    public record PTZ(float pan, float tilt, float zoom, boolean moving, boolean zooming) {
    }
}
