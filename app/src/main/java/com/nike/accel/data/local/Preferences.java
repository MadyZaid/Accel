package com.nike.accel.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Used for saving and retrieving data from Shared Preferences.
 */
public class Preferences {
    private final static String KEY_TOTAL_DISTANCE = "TotalDistance";
    private final static String KEY_GAUGE_MODE = "GaugeMode";


    /**
     * Saves the mGauge mode.
     */
    public static void saveGaugeMode(int mode, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(KEY_GAUGE_MODE, mode);
        editor.apply();
    }

    /**
     * Returns the mGauge mode.
     */
    public static int getGaugeMode(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(KEY_GAUGE_MODE, 0);
    }

    /**
     * Saves the total distance.
     */
    public static void saveTotalDistance(float distance, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(KEY_TOTAL_DISTANCE, distance);
        editor.apply();
    }

    /**
     * Returns the total distance.
     */
    public static float getTotalDistance(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getFloat(KEY_TOTAL_DISTANCE, 0);
    }
}
