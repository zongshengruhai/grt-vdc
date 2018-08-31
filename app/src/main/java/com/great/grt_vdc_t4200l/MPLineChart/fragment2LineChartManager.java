package com.great.grt_vdc_t4200l.MPLineChart;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class fragment2LineChartManager {

    private LineChart lineChart;
    private YAxis leftAxis;
    private YAxis rightAxis;
    private XAxis xAxis;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<ILineDataSet> lineDataSets = new ArrayList<>();
    private SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
    private List<String> timeList = new ArrayList<>(); //存储x轴的时间

    //多条曲线
    public fragment2LineChartManager(LineChart mLineChart, List<String> names, List<Integer> colors) {
        this.lineChart = mLineChart;
        leftAxis = lineChart.getAxisLeft();
        lineChart.getAxisRight().setEnabled(false);
        xAxis = lineChart.getXAxis();
        initLineChart();
        initLineDataSet(names, colors);
    }

//    private void initLineChart(){
//
//        lineChart.setDrawGridBackground(false);
//        lineChart.setDrawBorders(true);
//        lineChart.setExtraOffsets(0,0,5,0);
//
//        //标签
//        Legend legend = lineChart.getLegend();
//        legend.setForm(Legend.LegendForm.SQUARE);
//        legend.setTextSize(20f);
//        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
//
//        //交互
//        lineChart.setDragEnabled(true);                                                             //拖动
//        lineChart.setPinchZoom(false);
//        lineChart.setDoubleTapToZoomEnabled(false);                                                 //双击缩放
//        lineChart.setScaleXEnabled(true);                                                           //X轴缩放
//        lineChart.setScaleYEnabled(false);                                                          //Y轴缩放
//
//
//        //X轴
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setDrawAxisLine(false);
//        xAxis.setEnabled(false);
//
//        //Y轴
//        leftAxis.setAxisMinimum(0f);                                                                //最小值
//        leftAxis.enableGridDashedLine(10,10,0);
//        leftAxis.setDrawZeroLine(false);
//    }

    //初始化LineChar
    private void initLineChart() {

        //图表设置
        lineChart.setDrawGridBackground(false);                                                     //图表背景
        lineChart.setDrawBorders(true);                                                             //背景边界
        lineChart.setExtraOffsets(0,0,5,0);

        //缩放拖动设置
        lineChart.setDragEnabled(true);                                                             //拖动
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(false);                                                 //双击缩放
        lineChart.setScaleXEnabled(true);                                                           //X轴缩放
//        lineChart.setScaleYEnabled(false);                                                          //Y轴缩放

        //图例设置
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);                                                   //图例形式：线、圆、正方形
        legend.setTextSize(20f);                                                                    //图例字体大小
//        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);                         //垂直对齐：顶部、底部
//        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);                       //水平对齐：左、右
//        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);                                 //文字方向：垂直、水平
//        legend.setDrawInside(true);                                                                //是否在表格内部
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

//        //X轴设置
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);                                              //X轴位置：顶部、底部
//        xAxis.setGranularity(1f);                                                                   //X轴间隔
//        xAxis.setLabelCount(10);                                                                    //X轴条目
//        //设置X轴条目名称
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return timeList.get((int) value % timeList.size());
////            }
//        });
        xAxis.setEnabled(false);                                                                    //X轴使能

        //Y轴设置
        leftAxis.setAxisMinimum(0f);                                                                //最小值
//        rightAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10,5,0);                    //左Y轴网格虚线
//        rightAxis.setEnabled(false);                                                                //右Y轴使能

    }

    //初始化折线（多条线）
    private void initLineDataSet(List<String> names, List<Integer> colors) {

        for (int i = 0; i < names.size(); i++) {

            lineDataSet = new LineDataSet(null, names.get(i));                               //线条名称
            lineDataSet.setDrawCircles(false);                                                      //线上的点
            lineDataSet.setDrawValues(true);                                                        //线上的值
            lineDataSet.setLineWidth(2f);                                                           //线条宽度
            lineDataSet.setColor(colors.get(i));                                                    //线条颜色
            lineDataSet.setCircleColor(colors.get(i));                                              //曲线颜色
            lineDataSet.setHighLightColor(colors.get(i));                                           //点击某个点时的十字坐标线
            lineDataSet.setDrawFilled(false);                                                       //曲线填充
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setValueTextSize(8f);                                                       //曲线标注文字大小
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSets.add(lineDataSet);

        }
        //添加一个空的 LineData
        lineData = new LineData();
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    //动态添加数据（多条折线图）
    public void addEntry(List<Integer> numbers) {

        if (lineDataSets.get(0).getEntryCount() == 0) {
            lineData = new LineData(lineDataSets);
            lineChart.setData(lineData);
        }

        if (timeList.size() > 11) {
            timeList.clear();
        }

        timeList.add(df.format(System.currentTimeMillis()));
        for (int i = 0; i < numbers.size(); i++) {
            Entry entry = new Entry(lineDataSet.getEntryCount(), numbers.get(i));
            lineData.addEntry(entry, i);
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
//            lineChart.setVisibleXRangeMaximum(7);
            lineChart.setVisibleXRangeMaximum(150);                                                   //X轴最大显示条目数
            lineChart.moveViewToX(lineData.getEntryCount() - 5);
        }
    }

    //设置Y轴值
    public void setYAxis(float max, float min, int labelCount) {
        if (max < min) {
            return;
        }
        leftAxis.setAxisMaximum(max);
        leftAxis.setAxisMinimum(min);
        leftAxis.setLabelCount(labelCount, false);

//        rightAxis.setAxisMaximum(max);
//        rightAxis.setAxisMinimum(min);
//        rightAxis.setLabelCount(labelCount, false);
        lineChart.invalidate();
    }

    //设置原点
    public void setOrigin(int origin ,String name){
        if (name == null) {
            name = "原点";
        }
        LimitLine hightLimit = new LimitLine(origin, name);
        hightLimit.setLineWidth(3f);
        hightLimit.setTextSize(18f);
        leftAxis.addLimitLine(hightLimit);
        lineChart.invalidate();
    }

    // 设置描述信息
    public void setDescription(String str) {
        Description description = new Description();
        description.setTextSize(16);
        description.setText(str);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }
}
