package com.nike.accel.ui.widgets.gauges;

/**
 * This interface lets a gauge business object communicate with the guage's UI component to
 * have it display data.
 */
public interface IGaugeUI {
    void setPointerValue(float value);
    void set7SegmentDisplayValue(String value);
    void set7SegmentLabel(String text);
    void setMinorLabel(String text);
    void setMajorLabel(String text);
    void setIGauge(IGauge iGauge);
    void setConnected(boolean connected);
}
