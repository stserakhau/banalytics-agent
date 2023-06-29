package com.banalytics.box;

public class BanalyticsBoxInstanceState {
    private final static BanalyticsBoxInstanceState BANALYTICS_BOX_INSTANCE_STATE = new BanalyticsBoxInstanceState();

    public static BanalyticsBoxInstanceState getInstance() {
        return BANALYTICS_BOX_INSTANCE_STATE;
    }

    private boolean showBanalyticsWatermark = false;

    public boolean isShowBanalyticsWatermark() {
        return showBanalyticsWatermark;
    }

    public void setShowBanalyticsWatermark(boolean showBanalyticsWatermark) {
        this.showBanalyticsWatermark = showBanalyticsWatermark;
    }
}
