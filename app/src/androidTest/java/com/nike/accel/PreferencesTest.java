package com.nike.accel;

import android.test.ActivityInstrumentationTestCase2;

import java.util.Random;

import com.nike.accel.bo.AccelGauge;
import com.nike.accel.data.local.Preferences;

/**
 * Tests methods in Preferences.java
 */
public class PreferencesTest extends ActivityInstrumentationTestCase2<MainActivity> {


    public PreferencesTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testPreferences() {
        // Test each gauge mode.
        gaugeModeTest(AccelGauge.GAUGE_MODE_CURRENT_DISTANCE);
        gaugeModeTest(AccelGauge.GAUGE_MODE_TOTAL_DISTANCE);
        gaugeModeTest(AccelGauge.GAUGE_MODE_DEMO);
        gaugeModeTest(AccelGauge.GAUGE_MODE_SPEED);

        // Test the saving of the total distance since the app first started.
        Random random = new Random();
        float expectedDistance = random.nextFloat();

        Preferences.saveTotalDistance(expectedDistance, getActivity().getBaseContext());
        assertEquals("Saving/reading total distance in preferences failed", expectedDistance, Preferences.getTotalDistance(getActivity().getBaseContext()));
    }

    private void gaugeModeTest(int expectedMode) {
        Preferences.saveGaugeMode(expectedMode, getActivity().getBaseContext());
        assertEquals("Saving/reading gauge mode in preferences failed", expectedMode, Preferences.getGaugeMode(getActivity().getBaseContext()));
    }
}

