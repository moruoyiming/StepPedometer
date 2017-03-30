package com.calypso.pedometer.greendao;

import com.calypso.pedometer.greendao.entry.StepInfo;
import com.calypso.pedometer.greendao.gen.StepInfoDao;

import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Project：StepPedometer
 * Created：jianz
 * Date：2017/3/30 12:32
 * Summry：
 */

public class DBHelper {

    public static StepInfoDao getStepInfoDao() {
        return GreenDaoManager.getInstance().getmDaoSession().getStepInfoDao();
    }

    public static void insertStepInfo(StepInfo stepInfo) {
        if (stepInfo == null) {
            return;
        }
        StepInfo stepInfo1 = getStepInfo(stepInfo.getDate());
        if (stepInfo1 != null) {
            stepInfo.setId(stepInfo1.getId());
        }
        getStepInfoDao().insertOrReplace(stepInfo);
    }

    public static void updateStepInfo(StepInfo stepInfo) {
        if (stepInfo == null) {
            return;
        }
        getStepInfoDao().update(stepInfo);
    }

    public static StepInfo getStepInfo(String date) {
        Query<StepInfo> query = getStepInfoDao().queryBuilder().where(StepInfoDao.Properties.Date.eq(date)).build();
        return query.unique();
    }

    public static List<StepInfo> getAllStepInfo() {
        return getStepInfoDao().loadAll();
    }

}
