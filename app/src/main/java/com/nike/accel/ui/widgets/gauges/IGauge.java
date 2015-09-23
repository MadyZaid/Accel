package com.nike.accel.ui.widgets.gauges;

/**
 * This interface lets the main activity communicate with a mGauge component during the app's
 * lifecycle.
 */
public interface IGauge {
    void onClick();
    void onAppPause();
    void onAppResume();
    void onAppStop();
    void onAppDestroy();
}