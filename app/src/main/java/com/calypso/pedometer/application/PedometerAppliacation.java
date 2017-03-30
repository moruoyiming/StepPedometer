package com.calypso.pedometer.application;

import android.app.Application;

import com.calypso.pedometer.greendao.GreenDaoManager;

/**
 * Project：StepPedometer
 * Created：jianz
 * Date：2017/3/30 11:12
 * Summry：
 */

public class PedometerAppliacation extends Application {

    private static PedometerAppliacation instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        GreenDaoManager.getInstance();
    }

    public static PedometerAppliacation getInstance() {
        return instance;
    }
}
