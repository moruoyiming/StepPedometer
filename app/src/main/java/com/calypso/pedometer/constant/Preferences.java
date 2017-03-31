package com.calypso.pedometer.constant;

import android.content.Context;
import android.content.SharedPreferences;

import com.calypso.pedometer.constant.Constant;
import com.calypso.pedometer.utils.PreferencesUtil;

/**
 * Project：StepPedometer
 * Created：jianz
 * Date：2017/3/31 8:55
 * Summry：
 */

public class Preferences {

    public static void IsShowNotification(Context context, boolean flag) {
        PreferencesUtil.putBoolean(context, Constant.WHETHER_NOTIFICATION, flag);
    }

    public static boolean getIsShowNotification(Context context) {
        return PreferencesUtil.getBoolean(context, Constant.WHETHER_NOTIFICATION, true);
    }

    public static void setStepBenchmark(Context context, int benchmark) {
        PreferencesUtil.putInt(context, Constant.STEP_BENCHMARK, benchmark);
    }

    public static int getStepBenchmark(Context context) {
        return PreferencesUtil.getInt(context, Constant.STEP_BENCHMARK, Constant.BASE_STEP);
    }
}
