package com.nike.accel.bo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import com.nike.accel.R;
import com.nike.accel.data.local.Preferences;
import com.nike.accel.ui.widgets.gauges.IGauge;
import com.nike.accel.ui.widgets.gauges.IGaugeData;
import com.nike.accel.ui.widgets.gauges.IGaugeUI;

/**
 * Encapsulates the business logic that retrieves accelerometer motion and displays it on the
 * screen. It also manages the different modes on the gauge where the user can switch between
 * displaying acceleration, current distance traveled, total distance traveled and running an
 * automated demo mode.
 * <p/>
 * This gauge is independent of the UI that is used to display the data. Communicating with
 * the gauge that handles UI logic is done through the IGauge interface.
 */
public class AccelGauge implements IGauge, SensorEventListener {
    public static final int GAUGE_MODE_SPEED = 0;
    public static final int GAUGE_MODE_CURRENT_DISTANCE = 1;
    public static final int GAUGE_MODE_TOTAL_DISTANCE = 2;
    public static final int GAUGE_MODE_DEMO = 3;

    private final int MAX_GAUGE_VALUE = 10;
    private final int SENSOR_READ_RATE = 10; // ms
    public static final int TEXT_UPDATE_INTERVAL = 500; // ms

    private IGaugeUI mIGaugeUI;
    private IGaugeData mIGaugeData;
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mPrevSpeedValues = new float[3];

    private GaugeTimerTask mGaugeTimerTask;
    private Timer mTimerUpdate;
    private float mTotalDistance;
    private float mTotalDistanceSinceAppFirstStarted;
    private int mGaugeMode;
    private long mTimeForLastDisplayUpdate;
    private float mAveSpeed;
    private float mAccumulatedSpeed;
    private boolean mDisplayRequiresUpdate;
    private int mTotalAveSpeedPoints;
    private float mDemoSpeedValue;
    private boolean mDemoDecrement;
    private boolean mIgnoreSensorData;
    private float mMaxSensorVal;
    private float mSpeed;
    private boolean mFirstAccelDataRead;


    /**
     * The constructor for the gauge.
     *
     * @param iGaugeUI   This is the interface that is used to do the actual display of data.
     * @param iGaugeData This interface allows this gauge to communicate back to the client
     *                   to notify it whenever data is available. The client can then execute
     *                   other tasks such as storing the data to a database or sending it to
     *                   a server.
     * @param context
     */
    public AccelGauge(IGaugeUI iGaugeUI, IGaugeData iGaugeData, Context context) {
        mIGaugeUI = iGaugeUI;
        mIGaugeData = iGaugeData;
        mContext = context;
        init();
    }

    /**
     * Initializes the gauge.
     */
    private void init() {
        // Set the gauge mode to whatever it was when the app last shut down .
        mGaugeMode = Preferences.getGaugeMode(mContext);
        mTotalDistanceSinceAppFirstStarted = Preferences.getTotalDistance(mContext);

        mIGaugeUI.setIGauge(this);
        setDisplayMode();
        startTimerTask();
        registerSensor();
    }

    /**
     * Gets called whenever the user taps on the gauge's mode button.
     */
    @Override
    public void onClick() {
        mGaugeMode = mGaugeMode == GAUGE_MODE_DEMO ? GAUGE_MODE_SPEED : ++mGaugeMode;
        Preferences.saveGaugeMode(mGaugeMode, mContext);
        setDisplayMode();
    }

    /**
     * Initializes the gauge based upon the display mode: speed, current distance, total distance,
     * demo.
     */
    private void setDisplayMode() {

        switch (mGaugeMode) {
            case GAUGE_MODE_SPEED:
                mIGaugeUI.set7SegmentLabel(mContext.getResources().getString(R.string.m_per_second));
                mIGaugeUI.setMajorLabel(mContext.getResources().getString(R.string.speed));
                mIGaugeUI.setMinorLabel("");

                break;

            case GAUGE_MODE_CURRENT_DISTANCE:
            case GAUGE_MODE_TOTAL_DISTANCE:
                mIGaugeUI.set7SegmentLabel(mContext.getResources().getString(R.string.meters_abbrev));
                mIGaugeUI.setMajorLabel(mContext.getResources().getString(R.string.distance));

                mIGaugeUI.setMinorLabel(mGaugeMode == GAUGE_MODE_CURRENT_DISTANCE ?
                        mContext.getResources().getString(R.string.current) :
                        mContext.getResources().getString(R.string.total));

                break;

            case GAUGE_MODE_DEMO:
                mIGaugeUI.set7SegmentLabel(mContext.getResources().getString(R.string.m_per_second));
                mIGaugeUI.setMajorLabel(mContext.getResources().getString(R.string.demo));
                mIGaugeUI.setMinorLabel("");
                break;
        }

        mDisplayRequiresUpdate = true;
        updateGauge();
    }


    /**
     * Starts a timer task that periodically updates the gauge's UI with new sensor data.
     */
    private void startTimerTask() {
        mGaugeTimerTask = new GaugeTimerTask();
        mTimerUpdate = new Timer();
        mTimerUpdate.scheduleAtFixedRate(mGaugeTimerTask, 0, SENSOR_READ_RATE);
    }

    @Override
    public void onAppDestroy() {
        shutdown();
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

    /**
     * Handles cleaning up resources when the app is being shut down.
     */
    private void shutdown() {
        unregisterSensor();
        mTimerUpdate.cancel();
    }

    /**
     * Registers a listener to retrieve data from the accelerometer.
     */
    private void registerSensor() {
        // Get the acceleromter sensor.
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMaxSensorVal = mSensor.getMaximumRange();
            mSensorManager.registerListener(this, mSensor, SENSOR_READ_RATE * 1000);
        }
    }

    /**
     * Unregisters the listener used for retrieving accelerometer data.
     */
    private void unregisterSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensor = null;
            mSensorManager = null;
        }
    }

    /**
     * Enables or disables the processing of sensor data. Used internally for test purposes only.
     *
     * @param ignore If set to true, sensor data will be ignored.
     */
    public void setIgnoreSensorData(boolean ignore) {
        mMaxSensorVal = mSensor.getMaximumRange();
        mIgnoreSensorData = ignore;
    }

    /**
     * Resets all data values to their normal initialized state as though the app was being
     * started for the very first time. Used internally for test purposes only.
     */
    public void resetGaugeForFirstUse() {
        mGaugeMode = GAUGE_MODE_SPEED;
        mTotalDistance = 0;
        mAccumulatedSpeed = 0;
        mTotalAveSpeedPoints = 0;
        mTimeForLastDisplayUpdate = System.currentTimeMillis();
        mDisplayRequiresUpdate = false;
        mTotalDistanceSinceAppFirstStarted = 0;
        mMaxSensorVal = 1;
        mFirstAccelDataRead = false;
        Preferences.saveGaugeMode(GAUGE_MODE_SPEED, mContext);
        Preferences.saveTotalDistance(0, mContext);

        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                setDisplayMode();
            }
        });
    }

    /**
     * Sets the total distance since the app first started. Used internally for test purposes only.
     *
     * @param distance The total distance.
     */
    public void setTotalDistanceSinceAppFirstStarted(float distance) {
        mTotalDistanceSinceAppFirstStarted = distance;
        mDisplayRequiresUpdate = true;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (mIgnoreSensorData)
            return;

        processSensorData(event.values);
    }

    /**
     * Processes data from the sensor.
     *
     * @param data A 3 dimensional array containing acceleration data for each axis: x, y, z
     */
    public void processSensorData(float[] data) {
        /*
         * For this demo, we won't calculate true speed because it involves removing the force of
         * gravity from the acceleration and this is a process that involves tweaking a formula
         * that itself is dependent upon a specific acceleromter chip used to retrieve sensor data.
         *
         * Since our goal is just to show total movement, we'll calculate speed by pretending
         * that the acceleration values are "axis location" values and calculate a virtual speed by
         * taking the difference between the current locations on each axis and their previous
         * locations and scaling up to the sensor's maximum range and then scaling again to
         * get the value within a range of zero to ten.
         */

        if (!mFirstAccelDataRead) {
            mPrevSpeedValues[0] = data[0];
            mPrevSpeedValues[1] = data[1];
            mPrevSpeedValues[2] = data[2];

            mTimeForLastDisplayUpdate = System.currentTimeMillis();

            mFirstAccelDataRead = true;

            return;
        }

        float speed = (Math.abs(data[0] - mPrevSpeedValues[0]) +
                Math.abs(data[1] - mPrevSpeedValues[1]) +
                Math.abs(data[2] - mPrevSpeedValues[2])) / mMaxSensorVal * 10 * 2;

        // When the device is not moving, there is a minimal amount of internal jitter that
        // will show up on the gauge's needle. To prevent the needle from moving when no
        // real motion is applied, only record the speed when it exceeds a minimal threshold.

        if (speed < .4)
            speed = 0;

        if (speed > 0)
            mTotalDistance += speed * .001;

        if (speed > MAX_GAUGE_VALUE)
            speed = MAX_GAUGE_VALUE;

        mSpeed = speed;

        mPrevSpeedValues[0] = data[0];
        mPrevSpeedValues[1] = data[1];
        mPrevSpeedValues[2] = data[2];

        mAccumulatedSpeed += speed;
        mTotalAveSpeedPoints++;

        // Update the display at a regular time interval.
        long timeDiff = System.currentTimeMillis() - mTimeForLastDisplayUpdate;

        if ((timeDiff >= TEXT_UPDATE_INTERVAL) && (mTotalAveSpeedPoints > 0) && !mDisplayRequiresUpdate) {
            if (mAccumulatedSpeed > 0)
                Preferences.saveTotalDistance(getTotalDistanceSinceAppFirstStarted(), mContext);

            mAveSpeed = mAccumulatedSpeed / mTotalAveSpeedPoints;
            mAccumulatedSpeed = 0;
            mTotalAveSpeedPoints = 0;
            mDisplayRequiresUpdate = true;
        }

        mIGaugeData.dataAvailable(speed, mAveSpeed, mTotalDistance, getTotalDistanceSinceAppFirstStarted());

    }

    /**
     * Returns the total distance traveled since the app first started. By "first started", this
     * means the very first time the app started. If you close the app and then start it again,
     * this would be the second time it was started.
     *
     * @return
     */
    private float getTotalDistanceSinceAppFirstStarted() {
        return mTotalDistanceSinceAppFirstStarted + mTotalDistance;
    }

    /**
     * Updates the gauge's value on the 7 segment display.
     *
     * @param value The value to display.
     */
    private void updateDisplayValue(float value) {
        DecimalFormat df;

        if (value < 1)
            df = new DecimalFormat("0.0");
        else
            df = new DecimalFormat("###.0");

        String text = df.format(value);
        mIGaugeUI.set7SegmentDisplayValue(text);

        mTimeForLastDisplayUpdate = System.currentTimeMillis();
        mDisplayRequiresUpdate = false;
    }

    /**
     * Updates the gauge with data which includes the data on the 7 segment display as well
     * as the position of the pointer.
     */
    private void updateGauge() {
        switch (mGaugeMode) {
            case GAUGE_MODE_SPEED:

                mIGaugeUI.setPointerValue(mSpeed);

                if (mDisplayRequiresUpdate)
                    updateDisplayValue(mAveSpeed);

                break;

            case GAUGE_MODE_CURRENT_DISTANCE:
            case GAUGE_MODE_TOTAL_DISTANCE:

                if (mDisplayRequiresUpdate) {
                    if (mGaugeMode == GAUGE_MODE_CURRENT_DISTANCE) {
                        updateDisplayValue(mTotalDistance);
                        setGaugePointerValue(mTotalDistance);
                    } else {
                        updateDisplayValue(getTotalDistanceSinceAppFirstStarted());
                        setGaugePointerValue(getTotalDistanceSinceAppFirstStarted());
                    }
                }

                break;

            case GAUGE_MODE_DEMO:

                if (!mDemoDecrement && (mDemoSpeedValue >= MAX_GAUGE_VALUE))
                    mDemoDecrement = true;
                else if (mDemoDecrement && (mDemoSpeedValue <= 0))
                    mDemoDecrement = false;

                if (mDemoDecrement)
                    mDemoSpeedValue = mDemoSpeedValue - (10f / 300f);
                else
                    mDemoSpeedValue = mDemoSpeedValue + (10f / 300f);

                setGaugePointerValue(mDemoSpeedValue);
                updateDisplayValue(mDemoSpeedValue);

                break;

        }
    }


    /**
     * Sets the gauge's pointer to the specified value.
     *
     * @param value The value to set.
     */
    private void setGaugePointerValue(float value) {
        if (value > MAX_GAUGE_VALUE)
            mIGaugeUI.setPointerValue(MAX_GAUGE_VALUE);
        else
            mIGaugeUI.setPointerValue(value);
    }

    /**
     * Used to periodically update the gauge.
     */
    class GaugeTimerTask extends TimerTask {
        @Override
        public void run() {
            updateGauge();
        }
    }
}
