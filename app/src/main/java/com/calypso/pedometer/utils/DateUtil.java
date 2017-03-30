package com.calypso.pedometer.utils;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Jian on 2016/8/25.
 * Email: 798774875@qq.com
 * Github: https://github.com/moruoyiming
 * Log日志的工具类
 */
public class DateUtil {
    public static final String DATE_JFP_STR = "yyyyMM";
    public static final String DATE_FULL_STR = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_SMALL_STR = "yyyy-MM-dd";
    public static final String DATE_TIME_STR = "HH:mm:ss";
    public static final String DATE_KEY_STR = "yyMMddHHmmss";
    private static final long MILLIS_PER_DAY = 86400000L;
    private static final long INTERVAL_IN_MILLISECONDS = 30000L;

    public static String getTimestampString(Date paramDate) {
        String str = null;
        long l1 = paramDate.getTime();
        long l2 = System.currentTimeMillis();
        if (isSameDay(l1, l2)) {
            Calendar localCalendar = GregorianCalendar.getInstance();
            localCalendar.setTime(paramDate);
            int i = localCalendar.get(11);
            if (i > 17)
                str = "晚上 hh:mm";
            else if ((i >= 0) && (i <= 6))
                str = "凌晨 hh:mm";
            else if ((i > 11) && (i <= 17))
                str = "下午 hh:mm";
            else
                str = "上午 hh:mm";
        } else if (isYesterday(l1, l2)) {
            str = "昨天 HH:mm";
        } else {
            str = "M月d日 HH:mm";
        }
        return new SimpleDateFormat(str, Locale.CHINA).format(paramDate);
    }

    /**
     * 获取当前时间的string类型值"yyyy-MM-dd"
     */
    public static String getTodayDate() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);
        return df.format(new Date(System.currentTimeMillis()));
    }

    public static String getTodayTime(String dateformat) {
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        return df.format(new Date(System.currentTimeMillis()));
    }

    public static boolean isCloseEnough(long paramLong1, long paramLong2) {
        long l = paramLong1 - paramLong2;
        if (l < 0L)
            l = -l;
        return l < 300000L;
    }

    public static boolean isCloseEnough(String params1, String params2) throws ParseException {
        long paramLong1 = stringToLong(params1, "yyyy-MM-dd");
        long paramLong2 = stringToLong(params2, "yyyy-MM-dd");
        long l = paramLong1 - paramLong2;
        if (l < 0L)
            l = -l;
        return l < 300000L;
    }

    /**
     * @param strTime    要转换的String类型的时间
     * @param formatType 时间格式
     * @return
     * @throws ParseException
     */
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    /**
     * @param date
     * @return
     */
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * @param strTime
     * @param formatType
     * @return
     * @throws ParseException
     */
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    private static boolean isSameDay(long paramLong1, long paramLong2) {
        long l1 = paramLong1 / 86400000L;
        long l2 = paramLong2 / 86400000L;
        return l1 == l2;
    }

    private static boolean isYesterday(long paramLong1, long paramLong2) {
        long l1 = paramLong1 / 86400000L;
        long l2 = paramLong2 / 86400000L;
        return l1 + 1L == l2;
    }

    public static Date StringToDate(String paramString1, String paramString2) {
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(paramString2);
        Date localDate = null;
        try {
            localDate = localSimpleDateFormat.parse(paramString1);
        } catch (ParseException localParseException) {
            localParseException.printStackTrace();
        }
        return localDate;
    }

    public static String toTime(int paramInt) {
        paramInt /= 1000;
        int i = paramInt / 60;
        int j = 0;
        if (i >= 60) {
            j = i / 60;
            i %= 60;
        }
        int k = paramInt % 60;
        return String.format("%02d:%02d", new Object[]{Integer.valueOf(i), Integer.valueOf(k)});
    }

    public static String getTimestampStr() {
        return Long.toString(System.currentTimeMillis());
    }
}
