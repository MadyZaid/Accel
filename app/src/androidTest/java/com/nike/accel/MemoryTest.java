package com.nike.accel;

import android.app.ActivityManager;
import android.test.ActivityInstrumentationTestCase2;

/**
 * Tests to see if any unusal amount of memory is being used by the app. This test lasts
 * 30 seconds.
 */
public class MemoryTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private ActivityManager.MemoryInfo mMemoryInfo;
    private ActivityManager mActivityManager;

    public MemoryTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
        mMemoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager = (ActivityManager) getActivity().getSystemService(getActivity().getBaseContext().ACTIVITY_SERVICE);
    }

    /**
     * Tests how much memory is used over a period of time. Note: This is not used to test for
     * memory leaks but rather for a decrease in available memory as the app runs. This could be
     * used to check if any testing code was accidentally left in the production code that was
     * eating away at memory.
     */
    public void testMemory() {
        // Get the average amount of available memory during the first minute that the app runs.

        float avail1 = getPercentMemoryAvailable();
        float avail2 = getPercentMemoryAvailable();
        float avail3 = getPercentMemoryAvailable();

        assertTrue ("Excessive memory use. avail1: " + avail1 + " avail2: " + avail2 + " avail3: " + avail3, !((avail2 < avail1) && (avail3 < avail2) && (avail1 -  avail3 > 1)));

    }

    private float getPercentMemoryAvailable() {

        long startTime = System.currentTimeMillis();
        float totalReadings = 0;
        float totalMemoryAvail = 0;

        do {
            mActivityManager.getMemoryInfo(mMemoryInfo);
            //long availableMegs = mi.availMem / 1048576L;
            totalMemoryAvail += mMemoryInfo.availMem;
            totalReadings++;
        } while(System.currentTimeMillis() - startTime < 10000);

        float percentAvailable = (totalMemoryAvail / totalReadings) / (float) mMemoryInfo.totalMem * 100f;
        return percentAvailable;
    }
}

