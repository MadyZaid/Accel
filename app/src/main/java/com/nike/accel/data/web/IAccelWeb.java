package com.nike.accel.data.web;

/**
 * This interface allows the main activity to communicate with any component that needs to
 * transmit data over the web. This avoids the app from being tied to a single service like
 * PubNub.
 */
public interface IAccelWeb {
    void onAppPause();
    void onAppResume();
    void onAppStop();
    void onAppDestroy();
}
