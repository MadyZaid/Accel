package com.nike.accel;

import android.os.Vibrator;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;


/**
 * Tests methods in AccelGauge.java
 *
 * Because an accelerometer outputs random values, it makes it impossible to run a consistent
 * test to check that the code is correctly processing accelerometer values. For this reason,
 * the accelerometer data is ignored and fake values are used instead in order to achieve the
 * expected results. The only exception to this is the vibration test. For this test, the
 * accelerometer's data is used and the device's vibrator is turned on. The vibration of the
 * device should create a large amount of swings in accelerometer data. So a random value other
 * than zero should be expected.
 *
 * IMPORTANT: If you exit the app in demo mode and then run this test, the test will hang. This
 * appears to be because the Instrumentation runner is waiting for the UI to settle. In demo mode
 * the gauge's needle and 7 segment display updates continuously and this appears to prevent the
 * setUp() method from completing. Currently the only work around to this is to make sure you
 * exit the app in any mode other than Demo mode.
 */
public class AccelGaugeTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private TextView mTextView_tv_7_segment_value;
    private TextView mTextView_tv_7_segment_label;
    private TextView mTextView_tv_top_label;
    private TextView mTextView_tv_bottom_label;

    public AccelGaugeTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTextView_tv_7_segment_value = (TextView) getActivity().mGauge.findViewById(R.id.tv_7_segment_value);
        mTextView_tv_7_segment_label = (TextView) getActivity().mGauge.findViewById(R.id.tv_7_segment_label);
        mTextView_tv_top_label = (TextView) getActivity().mGauge.findViewById(R.id.tv_top_label);
        mTextView_tv_bottom_label = (TextView) getActivity().mGauge.findViewById(R.id.tv_bottom_label);
    }

    public void testPreconditions() {

        assertNotNull("mTextView_tv_7_segment_value is null.", mTextView_tv_7_segment_value);
        assertNotNull("mTextView_tv_7_segment_label is null.", mTextView_tv_7_segment_label);
        assertNotNull("mTextView_tv_top_label is null.", mTextView_tv_top_label);
        assertNotNull("mTextView_tv_bottom_label is null.", mTextView_tv_bottom_label);
    }

    public void testGaugeModes() {

        // Disable the gauge from processing sensor data.
        getActivity().mAccelGauge.setIgnoreSensorData(true);

        // Reset all the data in the gauge as though it just started for the first time.
        getActivity().mAccelGauge.resetGaugeForFirstUse();

        // Wait for the gauge to update itself.
        waitForGaugeToUpdate();

        // Check the speed mode labels.
        labelTests("Speed mode label not set", getActivity().getString(R.string.m_per_second), "", getActivity().getString(R.string.speed));

        // Simulate the first sensor data.
        float[] data1 = {.3f, .3f, .3f};
        getActivity().mAccelGauge.processSensorData(data1);

        waitForGaugeToUpdate();

        // Simulate the second sensor data.
        float[] data2 = {.15f, .15f, .15f};
        getActivity().mAccelGauge.processSensorData(data2);

        waitForGaugeToUpdate();

        // Read back the speed that was displayed on the gauge.
        float actualFakeSpeed = Float.valueOf(mTextView_tv_7_segment_value.getText().toString());
        assertEquals("Setting speed value failed.", 9.0f, actualFakeSpeed);

        // Switch to current distance mode.
        // NOTE: Avoid using TouchUtils.clickView(). This blocks for unknown reasons.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().mAccelGauge.onClick();
            }
        });

        waitForGaugeToUpdate();

        // Check the current distance mode labels.
        labelTests("Current distance mode label not set", getActivity().getString(R.string.meters_abbrev), getActivity().getString(R.string.current), getActivity().getString(R.string.distance));

        // Generate 400 fake data points in order to get a distance of 3.6
        for (int i = 0; i < 200; i++) {
            getActivity().mAccelGauge.processSensorData(data1);

            // This loop happens very quickly and will probably be faster than
            // the time interval needed for the gauge to update its display.
            // On the last data point, just wait long enough for the gauge's
            // update interval to expire.
            if (i == 199)
                waitForGaugeToUpdate();

            getActivity().mAccelGauge.processSensorData(data2);
        }

        waitForGaugeToUpdate();

        // Read back the current distance that was displayed on the gauge.
        float actualCurrentDistance = Float.valueOf(mTextView_tv_7_segment_value.getText().toString());
        assertEquals("Current distance not set to expected value.", 3.6f, actualCurrentDistance);

        // Switch to total distance mode.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().mAccelGauge.onClick();
            }
        });

        waitForGaugeToUpdate();

        // Check the total distance mode labels.
        labelTests("Total distance mode label not set", getActivity().getString(R.string.meters_abbrev), getActivity().getString(R.string.total), getActivity().getString(R.string.distance));

        // Read back the total distance that was displayed on the gauge.
        float actualTotalDistance = Float.valueOf(mTextView_tv_7_segment_value.getText().toString());
        assertEquals("Total distance not set to expected value.", 3.6f, actualTotalDistance);

        // Add some fake distance to the total distance since the app first started.
        getActivity().mAccelGauge.setTotalDistanceSinceAppFirstStarted(1.4f);
        waitForGaugeToUpdate();

        // Read back the total distance that was displayed on the gauge.
        actualTotalDistance = Float.valueOf(mTextView_tv_7_segment_value.getText().toString());
        assertEquals("Total distance since app first started not set to expected value.", 5.0f, actualTotalDistance);

        // Switch to speed mode.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // First click advances to Demo mode. Second one advances to Speed mode.
                getActivity().mAccelGauge.onClick();
                getActivity().mAccelGauge.onClick();
            }
        });

        // Re-enable using the sensor. Vibrate the device. This will cause the accelerometer to pick
        // up motion.
        getActivity().mAccelGauge.setIgnoreSensorData(false);
        Vibrator v = (Vibrator) getActivity().getBaseContext().getSystemService(getActivity().getBaseContext().VIBRATOR_SERVICE);
        v.vibrate(2000);

        // Wait for the gauge to update itself.
        waitForGaugeToUpdate();

        float actualSensorSpeed = Float.valueOf(mTextView_tv_7_segment_value.getText().toString());
        assertTrue("Sensor read zero. Vibrator should cause motion. Speed: " + actualSensorSpeed, actualSensorSpeed != 0);
        v.cancel();
        assertNotSame("Fake speed is not different from real speed", actualFakeSpeed, actualSensorSpeed);

    }

    private void labelTests(String message, String expected7SegLabel, String expectedTopLabel, String expectedBottomLabel) {
        String actualLabelText = mTextView_tv_7_segment_label.getText().toString();
        assertEquals(message + " (mTextView_tv_7_segment_label).", expected7SegLabel, actualLabelText);

        actualLabelText = mTextView_tv_top_label.getText().toString();
        assertEquals(message + " (mTextView_tv_top_label).", expectedTopLabel, actualLabelText);

        actualLabelText = mTextView_tv_bottom_label.getText().toString();
        assertEquals(message + " (mTextView_tv_bottom_label).", expectedBottomLabel, actualLabelText);
    }

    /**
     * Provides a delay of one second. A loop is used instead of a Thread.sleep because
     * Thread.sleep will interfere with the UI thread causing blockage.
     */
    private void waitForGaugeToUpdate() {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < 1000) {

        }
    }
}

