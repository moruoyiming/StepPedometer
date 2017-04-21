package com.calypso.pedometer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.calypso.pedometer.constant.Preferences;
import com.calypso.pedometer.R;
import com.calypso.pedometer.constant.Constant;
import com.calypso.pedometer.greendao.DBHelper;
import com.calypso.pedometer.greendao.entry.StepInfo;
import com.calypso.pedometer.stepdetector.StepService;
import com.calypso.pedometer.utils.ConversionUtil;
import com.calypso.pedometer.utils.DateUtil;
import com.calypso.pedometer.utils.Timber;
import com.calypso.pedometer.view.MyYAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Project：Pedometer
 * Created：jianz
 * Date：2017/3/27 14:47
 * Summry：
 */
public class MainActivity extends AppCompatActivity implements Handler.Callback {

    private TextView textView;
    private Messenger messenger;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));
    private Handler delayHandler;
    private DecimalFormat df1 = new DecimalFormat("0.00");
    private int stepBenchmark = 0;
    protected Typeface mTfLight;
    private int countsize = 0;
    private LineChart lineChart;
    //以bind形式开启service，故有ServiceConnection接收回调
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = mGetReplyMessenger;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag("MainActivity");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        stepBenchmark = Preferences.getStepBenchmark(MainActivity.this);
        delayHandler = new Handler(this);
        textView = (TextView) findViewById(R.id.step);
        lineChart = (LineChart) findViewById(R.id.ll_chart);
        Intent intent = new Intent(this, StepService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        initBarChatData();
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_FROM_SERVER:
                long step = msg.getData().getLong("step");
                String mileages = String.valueOf(ConversionUtil.step2Mileage(step, stepBenchmark));
                String calorie = df1.format(ConversionUtil.step2Calories(step, stepBenchmark));
                textView.setText("今日步数：" + step + " 步" + "消耗卡路里：" + calorie + " 卡" + "大约行走: " + mileages + " 米");
                delayHandler.sendEmptyMessageDelayed(Constant.REQUEST_SERVER, Constant.TIME_INTERVAL);
                break;
            case Constant.REQUEST_SERVER:
                try {
                    Message msgl = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                    msgl.replyTo = mGetReplyMessenger;
                    messenger.send(msgl);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
        return false;
    }

    private void initChart(final List<String> xList, final List<String> yList, List<Long> yData) {
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.setNoDataText("没有数据");
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setDrawGridBackground(false);
        lineChart.setTouchEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setExtraRightOffset(20f);
        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(tf);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setXOffset(10f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisMaximum(xList.size() - 1);
        xAxis.setAxisMinimum(0);
        xAxis.setLabelCount(xList.size(), true);//设置标尺的最大数量
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String str = (String) xList.get((int) value);
                return str.substring(5, str.length());
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.enableGridDashedLine(10f, 10f, 0f); // 设置标尺线的格式
        leftAxis.setAxisMaximum(yList.size() - 1);// 设置最大值
        leftAxis.setAxisMinimum(0);// 设置最小值
        leftAxis.setLabelCount(10, true);// 设置标尺的最大数量
        leftAxis.setValueFormatter(new MyYAxisValueFormatter(yList));
        leftAxis.setSpaceTop(5f);
        leftAxis.setStartAtZero(true);
        lineChart.getAxisRight().setEnabled(false);

        setData(yData);
    }

    private void setData(List<Long> ydata) {
        //Y轴数据集合
        ArrayList<Entry> yVals = new ArrayList<>();
        for (int i = 0; i < ydata.size(); i++) {
            float val = ydata.get(i);
            yVals.add(new Entry(i, val));
        }
        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(Color.RED);
        set1.setCircleColor(Color.RED);
        set1.setLineWidth(1.5f);
        set1.setCircleRadius(2f);
        set1.setDrawHighlightIndicators(false);
        set1.setValueTextColor(Color.RED);
        set1.setDrawCircleHole(false);
        LineData data = new LineData(set1);
        lineChart.setData(data);
    }

    public void initBarChatData() {
        StepInfo stepInfo = DBHelper.getStepInfo(DateUtil.getTodayDate());
        if (stepInfo != null) {
            long step = stepInfo.getStepCount();
            String mileages = String.valueOf(ConversionUtil.step2Mileage(step, stepBenchmark));
            String calorie = df1.format(ConversionUtil.step2Calories(step, stepBenchmark));
            textView.setText("今日步数：" + step + " 步" + "消耗卡路里：" + calorie + " 卡" + "大约行走: " + mileages + " 米");
        }
        List<StepInfo> steps = DBHelper.getAllStepInfo();
        if (steps.size() > 7) {
            steps = steps.subList(steps.size() - 7, steps.size());
        }
        countsize = steps.size() < 7 ? steps.size() : 7;
        List<String> xList = new ArrayList<>();
        List<String> yList = new ArrayList<>();
        List<Long> mList = new ArrayList<>();
        yList.add("0");
        yList.add("2000");
        yList.add("4000");
        yList.add("6000");
        yList.add("8000");
        yList.add("10000");
        yList.add("12000");
        yList.add("14000");
        yList.add("16000");
        yList.add("18000");
        for (int i = 0; i < countsize; i++) {
            xList.add(steps.get(i).getDate());
            mList.add(steps.get(i).getStepCount());
            Timber.i("what" +steps.get(i).getStepCount());
        }
        initChart(xList, yList, mList);
    }

}
