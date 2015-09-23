package com.nike.accel;

import android.test.ActivityInstrumentationTestCase2;

import com.nike.accel.data.web.IWebAccess;
import com.nike.accel.data.web.pubnub.PubNubWeb;

/**
 * Tests PubNubWeb.java
 */
public class PubNubWebTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private boolean mDataSent;

    public PubNubWebTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testPubNubWeb() {

        PubNubWeb pubNubWeb = new PubNubWeb(new IWebAccess() {
            @Override
            public void onConnectionChange(boolean connected) {
                mDataSent = connected;
            }
        });

        try {
            Thread.sleep(4000);
        } catch (Exception ex) {
        }

        assertTrue("Data sent to PubNub failed. Check that the wifi is enabled.", mDataSent);
    }
}

