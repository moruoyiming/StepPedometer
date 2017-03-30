package com.calypso.pedometer.greendao;

import com.calypso.pedometer.application.PedometerAppliacation;
import com.calypso.pedometer.constant.Constant;
import com.calypso.pedometer.greendao.gen.DaoMaster;
import com.calypso.pedometer.greendao.gen.DaoSession;

/**
 * Project：StepPedometer
 * Created：jianz
 * Date：2017/3/30 11:11
 * Summry：
 */

public class GreenDaoManager {
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static GreenDaoManager mInstance;

    private GreenDaoManager() {
        init();
    }

    /**
     * return greendaomanage instance
     *
     * @return
     */
    public static GreenDaoManager getInstance() {
        if (mInstance == null) {
            synchronized (GreenDaoManager.class) {
                if (mInstance == null) {
                    mInstance = new GreenDaoManager();
                }
            }
        }
        return mInstance;
    }


    private void init() {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(PedometerAppliacation.getInstance(),
                Constant.DB_NAME);
        mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoMaster getmDaoMaster() {
        return mDaoMaster;
    }

    public DaoSession getmDaoSession() {
        return mDaoSession;
    }

    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }
}
