package com.nike.accel.data.web.pubnub;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import com.nike.accel.data.web.IAccelWeb;
import com.nike.accel.data.web.IWebAccess;
import com.nike.accel.ui.widgets.gauges.IGaugeData;

/**
 * Periodically sends speed and distance data to PubNub.
 */
public class PubNubWeb implements IAccelWeb, IGaugeData {

    private Pubnub mPubNub;
    private boolean mIsTerminated;
    private Thread mThreadPubNub;
    private float mAveSpeed;
    private float mCurrentDistance;
    private IWebAccess mIWebAccess;
    private boolean mConnected;

    private final String CHANNEL_SPEED = "speed";
    private final String CHANNEL_DISTANCE = "distance";
    private final int UPDATE_INTERVAL = 250; // ms

    public PubNubWeb(IWebAccess iWebAccess) {
        mIWebAccess = iWebAccess;

        mThreadPubNub = new Thread(null, new UploadToPubNubRunnable(), "UploadToPubNubRunnable_" + UUID.randomUUID());
        mThreadPubNub.start();
    }

    private class UploadToPubNubRunnable implements Runnable {
        private boolean mReadyToSend = true;
        private long mStartTime;

        @Override
        public void run() {

            // Avoid recreating the JSON object each time we need to send data. We only need
            // to update the value sent.
            JSONObject objJSON = new JSONObject();
            JSONArray arrayInner = new JSONArray();
            JSONArray arrayOuter = new JSONArray();

            mPubNub = new Pubnub("demo", "demo");

            try {
                arrayInner.put("data");
                arrayInner.put(0);
                arrayOuter.put(arrayInner);
                objJSON.put("columns", arrayOuter);
            } catch (Exception ex) {
            }

            mStartTime = System.currentTimeMillis();

            while (!mIsTerminated) {
                try {
                    // PubNub only allows data to be sent to it at a maximum rate of 5 messages
                    // per second.
                    if ((System.currentTimeMillis() - mStartTime < UPDATE_INTERVAL) && mReadyToSend)
                        continue;

                    arrayInner.put(1, mAveSpeed);

                    mReadyToSend = false;

                    mStartTime = System.currentTimeMillis();
                    mReadyToSend = true;

                    mPubNub.publish(CHANNEL_SPEED, objJSON, new Callback() {
                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            super.errorCallback(channel, error);
                            notifyConnectionChange(false);
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            super.successCallback(channel, message);
                            notifyConnectionChange(true);
                        }
                    });

                    arrayInner.put(1, mCurrentDistance);

                    mPubNub.publish(CHANNEL_DISTANCE, objJSON, new Callback() {
                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            super.errorCallback(channel, error);
                            notifyConnectionChange(false);
                            mStartTime = System.currentTimeMillis();
                            mReadyToSend = true;
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            super.successCallback(channel, message);
                            notifyConnectionChange(true);
                            mStartTime = System.currentTimeMillis();
                            mReadyToSend = true;
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mPubNub.shutdown();
            mPubNub = null;
        }

        /**
         * Notify the client if there's a change in the connection to the server.
         * @param success Set to true if a connection was established.
         */
        private void notifyConnectionChange(boolean success) {
            if (mConnected && !success)
                mIWebAccess.onConnectionChange(false);
            else if (!mConnected && success)
                mIWebAccess.onConnectionChange(true);

            mConnected = success;
        }
    }

    @Override
    public void onAppDestroy() {
        mIsTerminated = true;
        mThreadPubNub.interrupt();
        mThreadPubNub = null;
    }

    @Override
    public void onAppStop() {
    }

    @Override
    public void onAppPause() {
    }

    @Override
    public void onAppResume() {
    }

    @Override
    public void dataAvailable(float speed, float aveSpeed, float currentDistance, float totalDistance) {
        mAveSpeed = aveSpeed;
        mCurrentDistance = currentDistance;
    }
}
