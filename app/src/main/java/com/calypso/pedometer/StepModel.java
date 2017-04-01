package com.calypso.pedometer;

import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BaseEntry;

import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;

/**
 * Project：StepPedometer
 * Created：jianz
 * Date：2017/3/31 16:47
 * Summry：
 */

public class StepModel extends BarEntry {

    private long stepCount;

    private String date;

    private String creteTime;

    private long previousStepCount;

    public String xAxisValue;

    public float yValue;

    public float xValue;

    public StepModel(float x, float y) {
        super(x, y);
    }

    public StepModel(float x, float y, Object data) {
        super(x, y, data);
    }

    public StepModel(float x, float y, Drawable icon) {
        super(x, y, icon);
    }

    public StepModel(float x, float y, Drawable icon, Object data) {
        super(x, y, icon, data);
    }

    public StepModel(float x, float[] vals) {
        super(x, vals);
    }

    public StepModel(float x, float[] vals, Object data) {
        super(x, vals, data);
    }

    public StepModel(float x, float[] vals, Drawable icon) {
        super(x, vals, icon);
    }

    public StepModel(float x, float[] vals, Drawable icon, Object data) {
        super(x, vals, icon, data);
    }

    public long getStepCount() {
        return stepCount;
    }

    public void setStepCount(long stepCount) {
        this.stepCount = stepCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreteTime() {
        return creteTime;
    }

    public void setCreteTime(String creteTime) {
        this.creteTime = creteTime;
    }

    public long getPreviousStepCount() {
        return previousStepCount;
    }

    public void setPreviousStepCount(long previousStepCount) {
        this.previousStepCount = previousStepCount;
    }

    public String getxAxisValue() {
        return xAxisValue;
    }

    public void setxAxisValue(String xAxisValue) {
        this.xAxisValue = xAxisValue;
    }

    public float getyValue() {
        return yValue;
    }

    public void setyValue(float yValue) {
        this.yValue = yValue;
    }

    public float getxValue() {
        return xValue;
    }

    public void setxValue(float xValue) {
        this.xValue = xValue;
    }

    @Override
    public String toString() {
        return "StepModel{" +
                "stepCount=" + stepCount +
                ", date='" + date + '\'' +
                ", creteTime='" + creteTime + '\'' +
                ", previousStepCount=" + previousStepCount +
                '}';
    }
}
