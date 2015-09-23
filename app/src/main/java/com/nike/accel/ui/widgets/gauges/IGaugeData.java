package com.nike.accel.ui.widgets.gauges;

/**
 * This interface allows a gauge to communicate with its clients to provide data when
 * it is available.
 */
public interface IGaugeData {
    void dataAvailable(float speed, float mAveSpeed, float currentDistance, float totalDistance);
}
