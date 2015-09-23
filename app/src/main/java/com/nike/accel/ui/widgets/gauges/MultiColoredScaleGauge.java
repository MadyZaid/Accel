package com.nike.accel.ui.widgets.gauges;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nike.accel.R;

/**
 * A gauge that is displayed with colored segments arranged in a circular arc
 * with values increasing in the clockwise direction. The gauge also contains
 * a 7 segment numerical display in the middle with a small text label to the right of
 * it that can be used for things like units. Beneath the numerical display are two
 * labels. The label directly beneath the numerical display has a smaller text
 * size while the one below this is larger. A button at the bottom of the gauge
 * can be used to provide feedback to the hosting client which can perform
 * whatever logic is necessary that corresponds to what is displayed.
 */
public class MultiColoredScaleGauge extends LinearLayout implements IGaugeUI {
    private Context mContext;
    private ImageView mImageViewPointer;
    private ImageView mImageViewGauge;
    private ImageView mImageViewScaleBg;
    private ImageView mImageViewConnection;
    private TextView mTextView7SegmentValue;
    private TextView mTextView7SegmentLabel;
    private TextView mTextViewTopLabel;
    private TextView mTextViewBottomLabel;
    private IGauge mIGauge;
    private boolean mConnected;


    public MultiColoredScaleGauge(Context context) {
        super(context);
        this.mContext = context;
    }

    public MultiColoredScaleGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    private void initialize(Context context) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View vRoot = inflater.inflate(R.layout.gauge_multi_colored_scale, null, false);


        mTextView7SegmentLabel = (TextView) vRoot.findViewById(R.id.tv_7_segment_label);
        mTextViewTopLabel = (TextView) vRoot.findViewById(R.id.tv_top_label);
        mTextViewBottomLabel = (TextView) vRoot.findViewById(R.id.tv_bottom_label);

        // Load the 7 segment font.
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/dseg7modern_regular.ttf");
        mTextView7SegmentValue = (TextView) vRoot.findViewById(R.id.tv_7_segment_value);
        mTextView7SegmentValue.setTypeface(font);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        vRoot.setLayoutParams(layoutParams);
        addView(vRoot);

        // Preload any views that will require updating during runtime.
        mImageViewGauge = (ImageView) vRoot.findViewById(R.id.iv_gauge);
        mImageViewGauge.setDrawingCacheEnabled(true);

        mImageViewScaleBg = (ImageView) vRoot.findViewById(R.id.iv_scale_bg);
        mImageViewScaleBg.setDrawingCacheEnabled(true);

        mImageViewPointer = (ImageView) vRoot.findViewById(R.id.iv_pointer);
        mImageViewConnection = (ImageView) vRoot.findViewById(R.id.iv_connection);

        final ImageView iv_button = (ImageView) vRoot.findViewById(R.id.iv_button);

        vRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                vRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float xCenter = getWidth() / 2;
                float yCenter = getHeight() / 2;

                mImageViewPointer.setX(xCenter - (mImageViewPointer.getWidth() / 2));
                mImageViewPointer.setY(yCenter - getResources().getDimension(R.dimen.pointer_y_position));

                mImageViewPointer.setPivotX(mImageViewPointer.getWidth() / 2);
                mImageViewPointer.setPivotY(getResources().getDimension(R.dimen.pointer_y_position));

                iv_button.setX(xCenter - iv_button.getWidth() / 2 + getResources().getDimension(R.dimen.gauge_button_left_margin));
                iv_button.setY(getHeight() - iv_button.getHeight() - getResources().getDimension(R.dimen.gauge_button_bottom_margin));

                mImageViewConnection.setX(xCenter + getResources().getDimension(R.dimen.connection_icon_left_margin));
                mImageViewConnection.setY(yCenter - getResources().getDimension(R.dimen.connection_icon_bottom_margin));

                setPointerValue(0);
            }
        });

        iv_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIGauge != null)
                    mIGauge.onClick();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initialize(mContext);
    }


    /**
     * Sets the gauge's value. Must be a number between 0 and 10 (inclusively).
     *
     * @param value
     */
    @Override
    public void setPointerValue(final float value) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                // Calculate the angle that corresponds to the value.
                float angle = 30 * value + 210;

                ((GradientScale) mImageViewScaleBg).setArcAngle(angle - 210);
                mImageViewScaleBg.invalidate();
                mImageViewPointer.setRotation(angle);
            }
        });

    }

    @Override
    public void set7SegmentDisplayValue(final String value) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                mTextView7SegmentValue.setText(value);
            }
        });
    }

    @Override
    public void set7SegmentLabel(String text) {
        mTextView7SegmentLabel.setText(text);
    }

    @Override
    public void setMinorLabel(String text) {
        mTextViewTopLabel.setText(text);
    }

    @Override
    public void setMajorLabel(String text) {
        mTextViewBottomLabel.setText(text);
    }

    @Override
    public void setIGauge(IGauge iGauge) {
        mIGauge = iGauge;
    }

    @Override
    public void setConnected(boolean connected) {
        mConnected = connected;

        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                if (mConnected)
                    mImageViewConnection.setImageDrawable(getResources().getDrawable(R.drawable.connected));
                else
                    mImageViewConnection.setImageDrawable(getResources().getDrawable(R.drawable.not_connected));
            }
        });
    }
}
