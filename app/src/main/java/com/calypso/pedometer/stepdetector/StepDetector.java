package com.calypso.pedometer.stepdetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Project：Pedometer
 * Created：jianz
 * Date：2017/3/27 14:47
 * Summry：
 * 加速度传感器监测步数的方法
 * <p>
 * 在这个方法中，会根据加速度传感器三轴的平均值得到行走过程中的位置信息，如果满足走一步
 * 所需要的条件（运动波峰与波谷之间的差值在给定的阈值范围内，具体条件在下面有）则就计为1步
 * 同时为防止微小震动对计步的影响，我们将计步分为3个状态——准备计时、计时中、计步中。所谓
 * “计时中”是在3.5秒内每隔0.7秒对步数进行一次判断，看步数是否仍然在增长，如果不在增长说明
 * 之前是无效的震动并没有走路，得到的步数不计入总步数中；反之则将这3.5秒内的步数加入总步数中。
 * 之后进入“计步中”状态进行持续计步，并且每隔2秒去判断一次当前步数和2秒前的步数是否相同，如
 * 果相同则说明步数不在增长，计步结束
 */
public class StepDetector implements SensorEventListener {
    private final String TAG = "StepDetector";
    //存放三轴数据(x,y,z)的个数
    private final int valueNum = 5;
    //用于存放计算阈值的波峰波谷差值
    private float[] tempValue = new float[valueNum];
    private int tempCount = 0;
    //是否上升的标志位
    private boolean isDirectionUp = false;
    //持续上升的次数
    private int continueUpCount = 0;
    //上一点的持续上升的次数，为了记录波峰的上升次数
    private int continueUpFormerCount = 0;
    //上一点的状态，上升还是下降
    private boolean lastStatus = false;
    //波峰值
    private float peakOfWave = 0;
    //波谷值
    private float valleyOfWave = 0;
    //此次波峰的时间
    private long timeOfThisPeak = 0;
    //上次波峰的时间
    private long timeOfLastPeak = 0;
    //当前的时间
    private long timeOfNow = 0;
    //上次传感器的值
    private float gravityOld = 0;
    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    private final float initialValue = (float) 1.7;
    //初始阈值
    private float ThreadValue = (float) 2.0;

    //初始范围
    private float minValue = 11f;
    private float maxValue = 19.6f;

    /**
     * 0-准备计时，1-计时中，2-正常计步中
     */
    private int CountTimeState = 0;
    //记录当前的步数
    public static long CURRENT_STEP = 0;
    //记录临时的步数
    public static long TEMP_STEP = 0;
    //记录上一次临时的步数
    private long lastStep = -1;
    //用x,y,z轴三个维度算出平均值
    public static float average = 0;
    private Timer timer;
    //倒计时3.5秒，3.5秒内不会显示计步，用于屏蔽细微波动
    private long duration = 3500;
    private TimeCount time;
    OnSensorChangeListener onSensorChangeListener;

    // 定义回调函数
    public interface OnSensorChangeListener {
        void onChange();
    }

    //构造函数
    public StepDetector(Context context) {
        super();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    //监听器set方法
    public void setOnSensorChangeListener(OnSensorChangeListener onSensorChangeListener) {
        this.onSensorChangeListener = onSensorChangeListener;
    }

    //当传感器发生改变后调用的函数
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        //同步块
        synchronized (this) {
            //获取加速度传感器
            if (sensor.getType() == sensor.TYPE_ACCELEROMETER) {
                calc_step(event);
            }
        }
    }

    synchronized private void calc_step(SensorEvent event) {
        //算出加速度传感器的x、y、z三轴的平均数值（为了平衡在某一个方向数值过大造成的数据误差）
        average = (float) Math.sqrt(Math.pow(event.values[0], 2)
                + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        detectorNewStep(average);
    }

    /**
     * 监测新的步数
     * <p>
     * 1.传入sersor中的数据
     * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定位1步
     * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
     *
     * @param values 加速传感器三轴的平均值
     */
    private void detectorNewStep(float values) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (DetectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();

                if (timeOfNow - timeOfLastPeak >= 200 && (peakOfWave - valleyOfWave >= ThreadValue)
                        && (timeOfNow - timeOfLastPeak) <= 2000) {
                    timeOfThisPeak = timeOfNow;
                    //更新界面的处理，不涉及算法
                    preStep();
                }
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
    }

    /**
     * 判断状态并计步
     */
    private void preStep() {
        if (CountTimeState == 0) {
            //开启计时器(倒计时3.5秒,倒计时时间间隔为0.7秒)  是在3.5秒内每0.7面去监测一次。
            time = new TimeCount(duration, 700);
            time.start();
            CountTimeState = 1;  //计时中
            Log.v(TAG, "开启计时器");
        } else if (CountTimeState == 1) {
            TEMP_STEP++;          //如果传感器测得的数据满足走一步的条件则步数加1
            Log.v(TAG, "计步中 TEMP_STEP:" + TEMP_STEP);
        } else if (CountTimeState == 2) {
            CURRENT_STEP++;
            Log.v(TAG, "计步回调:" + CURRENT_STEP);
            if (onSensorChangeListener != null) {
                //在这里调用onChange()  因此在StepService中会不断更新状态栏的步数
                onSensorChangeListener.onChange();
            }
        }
    }

    /**
     * 监测波峰
     * 以下四个条件判断为波峰
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于1.2g,小于2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰作对比
     *
     * @param newValue
     * @param oldValue
     * @return
     */
    public boolean DetectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }
        if (!isDirectionUp && lastStatus && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue))) {
            //满足上面波峰的四个条件，此时为波峰状态
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            //满足波谷条件，此时为波谷状态
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

    /**
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     *
     * @param value
     * @return
     */
    public float Peak_Valley_Thread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            //此时tempCount=valueNum=5
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;
    }

    /**
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * <p>
     * 这些数据是通过大量的统计得到的
     *
     * @param value
     * @param n
     * @return
     */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;  //计算数组均值
        if (ave >= 8) {
            Log.v(TAG, "超过8");
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
            Log.v(TAG, "7-8");
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
            Log.v(TAG, "4-7");
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
            Log.v(TAG, "3-4");
            ave = (float) 2.0;
        } else {
            Log.v(TAG, "else (ave<3)");
            ave = (float) 1.7;
        }
        return ave;
    }


    class TimeCount extends CountDownTimer {
        /**
         * 构造函数
         *
         * @param millisInFuture    倒计时时间
         * @param countDownInterval 倒计时时间间隔
         */
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (lastStep == TEMP_STEP) {
                //一段时间内，TEMP_STEP没有步数增长，则计时停止，同时计步也停止
                Log.v(TAG, "onTick 计时停止");
                time.cancel();
                CountTimeState = 0;
                lastStep = -1;
                TEMP_STEP = 0;
            } else {
                lastStep = TEMP_STEP;
            }

        }

        @Override
        public void onFinish() {
            //如果计时器正常结束，则开始计步
            time.cancel();
            CURRENT_STEP += TEMP_STEP;
            lastStep = -1;
            Log.v(TAG, "计时器正常结束");

            timer = new Timer(true);
            TimerTask task = new TimerTask() {
                public void run() {
                    //当步数不在增长的时候停止计步
                    if (lastStep == CURRENT_STEP) {
                        timer.cancel();
                        CountTimeState = 0;
                        lastStep = -1;
                        TEMP_STEP = 0;
                        Log.v(TAG, "停止计步：" + CURRENT_STEP);
                    } else {
                        lastStep = CURRENT_STEP;
                    }
                }
            };
            timer.schedule(task, 0, 2000);   //每隔两秒执行一次，不断监测是否已经停止运动了。
            CountTimeState = 2;
        }
    }
}
