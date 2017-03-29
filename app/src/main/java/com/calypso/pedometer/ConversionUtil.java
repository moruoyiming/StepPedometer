package com.calypso.pedometer;

/**
 * Project：Pedometer
 * Created：jianz
 * Date：2017/3/27 14:47
 * Summry：
 */

public class ConversionUtil {

    public static float step2Calories(int paramInt) {
        return (float) (paramInt * 65 * 0.6D * 65.0D / 100000.0D);
    }

    public static float step2Mileage(int paramInt) {
        return (paramInt * 65 / 100.0F);
    }
}
