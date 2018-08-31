package com.great.grt_vdc_t4200l.MPLineChart;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class mLineChartManager {

    private LineChart lineChart;
    private int type;

    public mLineChartManager(LineChart mlineChart,int Type){
        this.lineChart = mlineChart;
        this.type = Type;
        initLineChart();
    }

    private void initLineChart(){

        lineChart.setDrawGridBackground(false);

        Legend legend = lineChart.getLegend();

        XAxis xAxis = lineChart.getXAxis();
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();

        switch (type){
            //不可拖拽
            case 0:
                lineChart.setDragEnabled(false);
                lineChart.setTouchEnabled(false);
                lineChart.setScaleEnabled(false);
                lineChart.setPinchZoom(false);

                legend.setForm(Legend.LegendForm.SQUARE);
                legend.setTextSize(18f);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setDrawInside(false);

//                //设置X轴条目名称
//                xAxis.setValueFormatter(new IAxisValueFormatter() {
//                    @Override
//                    public String getFormattedValue(float value, AxisBase axis) {
//                        return timeList.get((int) value % timeList.size());
//                    }
//                });


                break;
            //可拖拽
            case 1:
                lineChart.setDragEnabled(true);
                lineChart.setPinchZoom(true);
                lineChart.setDoubleTapToZoomEnabled(false);

                legend.setForm(Legend.LegendForm.SQUARE);
                legend.setTextSize(20f);
                legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

                break;
        }

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(10);

        leftAxis.setAxisMinimum(0f);
        rightAxis.setEnabled(false);
    }

    // 设置描述信息
    public void setDescription(String str) {
        Description description = new Description();
        description.setText(str);
        lineChart.setDescription(description);
        lineChart.invalidate();
    }
}
