package com.nike.accel.ui.widgets.gauges;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.nike.accel.R;

/**
 * Draws the gradient scale on the gauge.
 */
public class GradientScale extends ImageView {
    private Paint mPaintBg;
    private float mAngle;

    /*
     * The sweep angles indicate the rotational degree that a color will be applied to on the
     * segments. Normally, the value for red and yellow would be 30 and 90 degrees respectively.
     * However, the gauge's png image has a few segment lines that don't line up exactly along
     * 30 and 90 degrees. As a result, the color red would overlap into the segment where yellow
     * starts and yellow will overlap into the area where green starts. To get an exact
     * alignment, the sweep angles are adjusted.
     */
    private final float RED_SWEEP_ANGLE = 29.6f;
    private final float YELLOW_SWEEP_ANGLE = 90.2f;

    public GradientScale(Context context) {
        super(context);
        init();
    }

    public GradientScale(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF rect = new RectF(getResources().getDimension(R.dimen.segment_margin),
                getResources().getDimension(R.dimen.segment_margin),
                getWidth() - getResources().getDimension(R.dimen.segment_margin),
                getHeight() - getResources().getDimension(R.dimen.segment_margin));

        // Draw the red segments
        float startAngle = 120;
        float sweepAngle = mAngle > RED_SWEEP_ANGLE ? RED_SWEEP_ANGLE : mAngle;
        mPaintBg.setColor(getResources().getColor(R.color.red_segments));

        canvas.drawArc(rect, startAngle, sweepAngle, true, mPaintBg);

        if (mAngle <= RED_SWEEP_ANGLE)
            return;

        // Draw the yellow segments
        startAngle = startAngle + sweepAngle;
        sweepAngle = mAngle - RED_SWEEP_ANGLE > YELLOW_SWEEP_ANGLE ? YELLOW_SWEEP_ANGLE : mAngle - RED_SWEEP_ANGLE;
        mPaintBg.setColor(getResources().getColor(R.color.yellow_segments));

        canvas.drawArc(rect, startAngle, sweepAngle, true, mPaintBg);

        if (mAngle <= (RED_SWEEP_ANGLE + YELLOW_SWEEP_ANGLE))
            return;

        // Draw the green segments
        startAngle = startAngle + sweepAngle;
        sweepAngle = mAngle - (RED_SWEEP_ANGLE + YELLOW_SWEEP_ANGLE);

        if (sweepAngle > 180)
            sweepAngle = 180;

        mPaintBg.setColor(getResources().getColor(R.color.green_segments));

        canvas.drawArc(rect, startAngle, sweepAngle, true, mPaintBg);
    }

    public void setArcAngle(float angle) {
        mAngle = angle;
    }

    private void init() {
        setWillNotDraw(false);
        mPaintBg = new Paint();
        mPaintBg.setStrokeWidth(30);
        mPaintBg.setStrokeCap(Paint.Cap.SQUARE);
    }
}
