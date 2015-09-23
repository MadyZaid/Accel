package com.nike.accel;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewTreeObserver;

import com.nike.accel.bo.AccelGauge;
import com.nike.accel.data.web.IWebAccess;
import com.nike.accel.data.web.pubnub.PubNubWeb;
import com.nike.accel.ui.widgets.gauges.IGaugeData;
import com.nike.accel.ui.widgets.gauges.MultiColoredScaleGauge;

public class MainActivity extends AppCompatActivity {

    public AccelGauge mAccelGauge;
    public MultiColoredScaleGauge mGauge;
    private PubNubWeb mPubNubWeb;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        // Although the gauge component is available, it will not have completed its initialization
        // at this stage. We need to wait until Android has loaded it before we can access it.

        mGauge = (MultiColoredScaleGauge) findViewById(R.id.gauge);

        mGauge.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mGauge.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mPubNubWeb = new PubNubWeb(new IWebAccess() {
                    @Override
                    public void onConnectionChange(boolean connected) {
                        if (mGauge != null)
                            mGauge.setConnected(connected);
                    }
                });

                mAccelGauge = new AccelGauge(mGauge, new IGaugeData() {
                    @Override
                    public void dataAvailable(float speed, float aveSpeed, float currentDistance, float totalDistance) {
                        // Send gauge data to PubNub.
                        if (mPubNubWeb != null)
                            mPubNubWeb.dataAvailable(speed, aveSpeed, currentDistance, totalDistance);
                    }
                }, mContext);
            }
        });
    }

    /**
     * "Note: There's no guarantee that onSaveInstanceState() will be called before your activity is destroyed,
     * because there are cases in which it won't be necessary to save the state (such as when the user leaves
     * your activity using the Back button, because the user is explicitly closing the activity). If the system
     * calls onSaveInstanceState(), it does so before onStop() and possibly before onPause()."
     * <p/>
     * Taken from: http://developer.android.com/guide/components/activities.html
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAccelGauge != null) {
            mAccelGauge.onAppDestroy();
            mAccelGauge = null;
        }

        if (mPubNubWeb != null) {
            mPubNubWeb.onAppDestroy();
            mPubNubWeb = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
