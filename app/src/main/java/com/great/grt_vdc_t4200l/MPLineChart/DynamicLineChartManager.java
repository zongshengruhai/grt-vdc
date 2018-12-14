package com.great.grt_vdc_t4200l.MPLineChart;

import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DynamicLineChartManager {

    private LineChart lineChart;
    private YAxis leftAxis;
    private YAxis rightAxis;
    private XAxis xAxis;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<ILineDataSet> lineDataSets = new ArrayList<>();
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
    private List<String> timeList = new ArrayList<>(); //存储x轴的时间

    //一条曲线
    public DynamicLineChartManager(LineChart mLineChart, String name, int color) {
        this.lineChart = mLineChart;
        leftAxis = lineChart.getAxisLeft();
        rightAxis = lineChart.getAxisRight();
        xAxis = lineChart.getXAxis();
        initLineChart();
        initLineDataSet(name, color);
    }

    //多条曲线
    public DynamicLineChartManager(LineChart mLineChart, List<String> names, List<Integer> colors) {
        this.lineChart = mLineChart;
        leftAxis = lineChart.getAxisLeft();
        rightAxis = lineChart.getAxisRight();
        xAxis = lineChart.getXAxis();
        initLineChart();
        initLineDataSet(names, colors);
    }

    //初始化LineChar
    private void initLineChart() {

        //图表设置
        lineChart.setDrawGridBackground(false);                                                     //背景网格
        lineChart.setDrawBorders(true);                                                             //背景边界
        lineChart.setDragEnabled(false);                                                            //拖拽
        lineChart.setTouchEnabled(false);                                                           //触摸
        lineChart.setScaleEnabled(false);                                                           //缩放
        lineChart.setPinchZoom(false);                                                              //多点缩放
        lineChart.getDescription().setEnabled(false);                                               //隐藏描述

        //图例设置
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);                                                   //图例形式：线、圆、正方形
        legend.setTextSize(18f);                                                                    //图例字体大小
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);                         //垂直对齐：顶部、底部
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);                       //水平对齐：左、右
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);                                 //文字方向：垂直、水平
        legend.setDrawInside(false);                                                                //是否在表格内部

        //X轴设置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);                                              //X轴位置：顶部、底部
        xAxis.setGranularity(1f);                                                                   //X轴间隔
        xAxis.setLabelCount(6);                                                                    //X轴条目
        //设置X轴条目名称
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return timeList.get((int) value % timeList.size());
            }
        });

        //Y轴设置
        leftAxis.setAxisMinimum(0f);                                                                //最小值
        rightAxis.setAxisMinimum(0f);
        rightAxis.setEnabled(false);

//        setYAxis(500,0,5);

    }

    //初始化折线(一条线)
    private void initLineDataSet(String name, int color) {

        lineDataSet = new LineDataSet(null, name);                                           //线条名称
        lineDataSet.setLineWidth(1.5f);                                                             //线条宽度
        lineDataSet.setCircleRadius(1.5f);                                                          //曲线半径
        lineDataSet.setColor(color);                                                                //线条颜色
        lineDataSet.setCircleColor(color);                                                          //曲线颜色
        lineDataSet.setHighLightColor(color);
        lineDataSet.setDrawFilled(true);                                                            //曲线填充
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setValueTextSize(10f);                                                          //曲线标注文字大小
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //添加一个空的 LineData
        lineData = new LineData();
        lineChart.setData(lineData);
        lineChart.invalidate();

    }

    //初始化折线（多条线）
    private void initLineDataSet(List<String> names, List<Integer> colors) {

        for (int i = 0; i < names.size(); i++) {

            lineDataSet = new LineDataSet(null, names.get(i));                               //线条名称

            lineDataSet.setLineWidth(2f);                                                           //线条宽度
            lineDataSet.setCircleRadius(1.5f);                                                      //曲线点半径

            lineDataSet.setColor(colors.get(i));                                                    //线条颜色
            lineDataSet.setCircleColor(colors.get(i));                                              //曲线颜色
            lineDataSet.setHighLightColor(colors.get(i));                                           //点击时横竖坐标线的颜色

            lineDataSet.setDrawFilled(false);                                                       //曲线填充

            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

            lineDataSet.setValueTextSize(16f);                                                      //曲线标注文字大小
            lineDataSet.setDrawValues(false);                                                        //线上的值

            lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);                                //设置曲线模式 弯曲程度
            //lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            lineDataSets.add(lineDataSet);

        }
        //添加一个空的 LineData
        lineData = new LineData();
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    //动态添加数据（一条折线图）
    public void addEntry(int number) {

        //最开始的时候才添加 lineDataSet（一个lineDataSet 代表一条线）
        if (lineDataSet.getEntryCount() == 0) {
            lineData.addDataSet(lineDataSet);
        }
        lineChart.setData(lineData);

        //避免集合数据过多，及时清空（做这样的处理，并不知道有没有用，但还是这样做了）
        if (timeList.size() > 60) {
            timeList.clear();
        }

        timeList.add(df.format(System.currentTimeMillis()));

        Entry entry = new Entry(lineDataSet.getEntryCount(), number);
        lineData.addEntry(entry, 0);
        //通知数据已经改变
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
        lineChart.setVisibleXRangeMaximum(10);
        //移到某个位置
        lineChart.moveViewToX(lineData.getEntryCount() - 5);
    }

    //动态添加数据（多条折线图）
    public void addEntry(List<Integer> numbers) {

        if (lineDataSets.get(0).getEntryCount() == 0) {
            lineData = new LineData(lineDataSets);
            lineChart.setData(lineData);
        }

        if (timeList.size() > 60) {
            timeList.clear();
        }

        timeList.add(df.format(System.currentTimeMillis()));
        for (int i = 0; i < numbers.size(); i++) {

            Entry entry = new Entry(lineDataSet.getEntryCount(), numbers.get(i));
            lineData.addEntry(entry, i);

            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();

            lineChart.setVisibleXRangeMaximum(60);

            lineChart.moveViewToX(lineData.getEntryCount());
        }
    }

    public void changeEntry(int[] a){

    }

    //设置Y轴值
    public void setYAxis(float max, float min, int labelCount) {
        if (max < min) {
            return;
        }
        leftAxis.setAxisMaximum(max);
        leftAxis.setAxisMinimum(min);
        leftAxis.setLabelCount(labelCount, false);

        rightAxis.setAxisMaximum(max);
        rightAxis.setAxisMinimum(min);
        rightAxis.setLabelCount(labelCount, false);
        lineChart.invalidate();
    }

    //设置高限制线
    public void setHightLimitLine(float high, String name, int color) {
        if (name == null) {
            name = "高限制线";
        }
        LimitLine hightLimit = new LimitLine(high, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        hightLimit.setLineColor(color);
        hightLimit.setTextColor(color);
        leftAxis.addLimitLine(hightLimit);
        lineChart.invalidate();
    }

    //设置低限制线
    public void setLowLimitLine(int low, String name) {
        if (name == null) {
            name = "低限制线";
        }
        LimitLine hightLimit = new LimitLine(low, name);
        hightLimit.setLineWidth(4f);
        hightLimit.setTextSize(10f);
        leftAxis.addLimitLine(hightLimit);
        lineChart.invalidate();
    }

    // 设置描述信息
    public void setDescription(String str) {
        Description description = new Description();
        description.setText(str);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }

    public void clear(){
        lineChart.clearValues();
    }

}
