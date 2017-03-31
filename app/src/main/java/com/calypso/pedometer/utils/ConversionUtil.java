package com.calypso.pedometer.utils;


/**
 * Project：Pedometer
 * Created：jianz
 * Date：2017/3/27 14:47
 * Summry：
 */

public class ConversionUtil {

    public static float step2Calories(long paramInt, int benchmark) {
        return (float) (paramInt * benchmark * 0.6D * 65.0D / 100000.0D);
    }

    public static float step2Mileage(long paramInt, int benchmark) {
        return (paramInt * benchmark / 100.0F);
    }
}
