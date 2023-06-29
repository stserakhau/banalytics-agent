package com.banalytics.box.module;

public interface AutoBalancingSupport {
    /**
     * based on task
     */
    int intensityMin();

    /**
     * Current intensity task value.
     */
    int intensityCurrent();

    /**
     * based on task
     */
    int intensityMax();

    /**
     *
     * @param scale value 0.0-1.0 where 0 is min intensity and 1 is max intensity
     */
    void setIntensity(double scale);
}
