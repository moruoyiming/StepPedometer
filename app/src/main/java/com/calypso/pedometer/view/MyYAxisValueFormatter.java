package com.calypso.pedometer.view;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.List;

public class MyYAxisValueFormatter implements IAxisValueFormatter {

    private List<String> mLetters;

    public MyYAxisValueFormatter(List<String> mLetters) {
        // TODO Auto-generated constructor stub
        this.mLetters = mLetters;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String what = mLetters.get((int) value);
        return what;
    }
}
