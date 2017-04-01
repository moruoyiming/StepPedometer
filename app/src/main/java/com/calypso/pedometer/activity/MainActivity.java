package com.calypso.pedometer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.calypso.pedometer.StepModel;
import com.calypso.pedometer.constant.Preferences;
import com.calypso.pedometer.R;
import com.calypso.pedometer.constant.Constant;
import com.calypso.pedometer.greendao.DBHelper;
import com.calypso.pedometer.greendao.entry.StepInfo;
import com.calypso.pedometer.stepdetector.StepService;
import com.calypso.pedometer.utils.ConversionUtil;
import com.calypso.pedometer.utils.DateUtil;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Project：Pedometer
 * Created：jianz
 * Date：2017/3/27 14:47
 * Summry：
 */
public class MainActivity extends AppCompatActivity implements Handler.Callback, OnChartValueSelectedListener {

    private TextView textView;
    private Messenger messenger;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));
    private Handler delayHandler;
    private DecimalFormat df1 = new DecimalFormat("0.00");
    private int stepBenchmark = 0;
    private BarChart mChart;
    private PieChart mPieChart;
    protected Typeface mTfLight;
    private List<StepModel> mSinusData;
    private int countsize = 0;
    protected String[] mParties = new String[]{"步数", "热量", "米数"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        stepBenchmark = Preferences.getStepBenchmark(MainActivity.this);
        delayHandler = new Handler(this);
        textView = (TextView) findViewById(R.id.step);
        mChart = (BarChart) findViewById(R.id.chart1);
        mPieChart = (PieChart) findViewById(R.id.chart);
        mSinusData = new ArrayList<>();
        Intent intent = new Intent(this, StepService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        initBarChart();
        initPieChart();
        setBarChartData(countsize);
        mChart.invalidate();
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
                textView.setText("今日步数：" + step + " 步" + "消耗卡路里：" + calorie + " 卡" + "大约行走: " + mileages + " 米");
                mPieChart.setCenterText(generateCenterSpannableText(step));
                mPieChart.notifyDataSetChanged();
                mPieChart.invalidate();
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

    private SpannableString generateCenterSpannableText(long step) {

        SpannableString s = new SpannableString("今日步数：\n"+step);
        s.setSpan(new RelativeSizeSpan(1.4f), 0, 4, 0);
//        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
//        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
//        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
//        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

    public void initBarChart() {
        List<StepInfo> steps = DBHelper.getAllStepInfo();
        if (steps.size() > 7) {
            steps = steps.subList(steps.size() - 7, steps.size());
        }
        countsize = steps.size() < 7 ? steps.size() : 7;
        for (int i = 0; i < countsize; i++) {
            StepModel barEntry = new StepModel(i, steps.get(i).getStepCount(), steps.get(i).getDate());
            mSinusData.add(barEntry);
        }
        mChart.setPinchZoom(false);
        mChart.setDragEnabled(false);// 是否可以拖拽
        mChart.setScaleEnabled(false);// 是否可以缩放
        mChart.setDrawBarShadow(false);//是否显示上层bar
        mChart.setDrawValueAboveBar(true);//设置内容到above
        mChart.setMaxVisibleValueCount(10);
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);//是否显示右下角description
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setTextColor(R.color.colorAccent);//设置轴标签文本颜色。
//        xAxis.setTextSize(20);//设置轴标签的字体大小。
//        xAxis.setTypeface(mTfLight);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(countsize);
        xAxis.setDrawAxisLine(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String str = (String) mSinusData.get(Math.min(Math.max((int) value, 0), mSinusData.size() - 1)).getData();
                return str.substring(5, str.length());
            }
        });
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(7, false);
        leftAxis.setAxisMaximum(15000);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(0.1f);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(7, false);
        rightAxis.setAxisMaximum(15000);
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
        StepInfo stepInfo = DBHelper.getStepInfo(DateUtil.getTodayDate());
        if (stepInfo != null) {
            long step = stepInfo.getStepCount();
            String mileages = String.valueOf(ConversionUtil.step2Mileage(step, stepBenchmark));
            String calorie = df1.format(ConversionUtil.step2Calories(step, stepBenchmark));
            textView.setText("今日步数：" + step + " 步" + "消耗卡路里：" + calorie + " 卡" + "大约行走: " + mileages + " 米");
        }
    }

    public void initPieChart() {
        mPieChart.setUsePercentValues(false);
        mPieChart.getDescription().setEnabled(true);
        Description description=new Description();
        description.setText("运动数据");
        description.setTextColor(Color.RED);
        description.setTextAlign(Paint.Align.RIGHT);
        description.setTextSize(10);
        mPieChart.setDescription(description);
        mPieChart.setExtraOffsets(5, 10, 5, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        mPieChart.setCenterTextTypeface(mTfLight);


        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(61f);

        mPieChart.setDrawCenterText(true);
        mPieChart.setCenterTextColor(Color.RED);
        mPieChart.setCenterTextSize(20);
        mPieChart.setRotationAngle(0);
        // mPieChartble rotation of the chart by touch
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);

        mPieChart.setOnChartValueSelectedListener(this);

        setPieChartData(3, 100);

        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l1 = mPieChart.getLegend();
        l1.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l1.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l1.setOrientation(Legend.LegendOrientation.VERTICAL);
        l1.setDrawInside(false);
        l1.setXEntrySpace(7f);
        l1.setYEntrySpace(0f);
        l1.setYOffset(0f);

        // entry label styling
        mPieChart.setEntryLabelColor(Color.WHITE);
        mPieChart.setEntryLabelTextSize(12f);
    }

    private void setBarChartData(int count) {
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        for (int i = 0; i < count; i++) {
            entries.add(mSinusData.get(i));
        }
        BarDataSet set;
        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set.setValues(entries);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set = new BarDataSet(entries, "最近一周运动情况");
            set.setColor(Color.rgb(240, 120, 124));
        }

        set.setDrawValues(true);
        BarData data = new BarData(set);
        data.setValueTextSize(10f);
        data.setValueTypeface(mTfLight);
        data.setDrawValues(true);
        data.setBarWidth(0.8f);
        mChart.setData(data);
    }


    private void setPieChartData(int count, float range) {

        float mult = range;

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < count; i++) {
            entries.add(new PieEntry((float) ((Math.random() * mult) + mult / 5),mParties[i % mParties.length],getResources().getDrawable(R.mipmap.star)));
        }

        PieDataSet dataSet = new PieDataSet(entries, " 数据");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();


        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(mTfLight);
        mPieChart.setData(data);

        // undo all highlights
        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }
}
