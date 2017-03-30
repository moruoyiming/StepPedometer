package com.calypso.pedometer.utils;

import java.text.DecimalFormat;

/**
 * Project：Pedometer
 * Created：jianz
 * Date：2017/3/27 14:47
 * Summry：
 */

public class ConversionUtil {

    public static float step2Calories(long paramInt) {
        return (float) (paramInt * 65 * 0.6D * 65.0D / 100000.0D);
    }

    public static float step2Mileage(long paramInt) {
        return (paramInt * 65 / 100.0F);
    }
}
