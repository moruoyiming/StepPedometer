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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.calypso.pedometer.constant.Preferences;
import com.calypso.pedometer.R;
import com.calypso.pedometer.constant.Constant;
import com.calypso.pedometer.greendao.DBHelper;
import com.calypso.pedometer.greendao.entry.StepInfo;
import com.calypso.pedometer.stepdetector.StepService;
import com.calypso.pedometer.utils.ConversionUtil;
import com.calypso.pedometer.utils.DateUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.FileUtils;

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
    private BarChart mChart;
    protected Typeface mTfLight;
    private List<BarEntry> mSinusData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepBenchmark = Preferences.getStepBenchmark(MainActivity.this);
        delayHandler = new Handler(this);
        textView = (TextView) findViewById(R.id.step);
        mChart = (BarChart) findViewById(R.id.chart1);

        mSinusData = FileUtils.loadBarEntriesFromAssets(getAssets(), "othersine.txt");
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.getDescription().setEnabled(false);
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        mChart.setMaxVisibleValueCount(60);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawBarShadow(false);

        XAxis xAxis =mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(6, false);
        leftAxis.setAxisMaximum(20000);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(0.1f);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(mTfLight);
        rightAxis.setLabelCount(6, false);
        rightAxis.setAxisMaximum(20000);
        rightAxis.setGranularity(0.1f);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(7f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        mChart.animateXY(2000, 2000);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = new Intent(this, StepService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setData(6);
        mChart.invalidate();
        StepInfo stepInfo = DBHelper.getStepInfo(DateUtil.getTodayDate());
        if (stepInfo != null) {
            long step = stepInfo.getStepCount();
            String mileages = String.valueOf(ConversionUtil.step2Mileage(step, stepBenchmark));
            String calorie = df1.format(ConversionUtil.step2Calories(step, stepBenchmark));
            textView.setText("今日步数：" + step + " 步" + "\n" + "消耗卡路里：" + calorie + " 卡" + "\n" + "大约行走: " + mileages + " 米");
        }

    }
    private void setData(int count) {

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        for (int i = 0; i < count; i++) {
            entries.add(mSinusData.get(i));
        }

        BarDataSet set;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set.setValues(entries);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set = new BarDataSet(entries, "Sinus Function");
            set.setColor(Color.rgb(240, 120, 124));
        }

        BarData data = new BarData(set);
        data.setValueTextSize(10f);
        data.setValueTypeface(mTfLight);
        data.setDrawValues(false);
        data.setBarWidth(0.8f);

        mChart.setData(data);
    }

    //以bind形式开启service，故有ServiceConnection接收回调
    ServiceConnection conn = new ServiceConnection() {
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
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_FROM_SERVER:
                long step = msg.getData().getLong("step");
                String mileages = String.valueOf(ConversionUtil.step2Mileage(step, stepBenchmark));
                String calorie = df1.format(ConversionUtil.step2Calories(step, stepBenchmark));
                textView.setText("今日步数：" + step + " 步" + "\n" + "消耗卡路里：" + calorie + " 卡" + "\n" + "大约行走: " + mileages + " 米");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
