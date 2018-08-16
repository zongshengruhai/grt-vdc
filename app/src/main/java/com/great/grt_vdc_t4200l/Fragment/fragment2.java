package com.great.grt_vdc_t4200l.Fragment;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.great.grt_vdc_t4200l.MPLineChart.DynamicLineChartManager;
import com.great.grt_vdc_t4200l.R;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


public class fragment2 extends Fragment{

    private TextView[] fragment2TempRow = new TextView[2];

    //MPAndroidChart
    private DynamicLineChartManager fragment2ChartManager;
    private LineChart fragment2LineChar;
    private List<Integer> list = new ArrayList<>();         //数据集合
    private List<Integer> colour = new ArrayList<>();       //折线颜色
    private List<String> names = new ArrayList<>();          //折线名称

    private int[] tempData[] = new int[9][30];

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment2,container,false);

        fragment2LineChar = (LineChart) view.findViewById(R.id.fragment2LineChart);

        fragment2TempRow[0] = (TextView) view.findViewById(R.id.fragment2TVtime);
        fragment2TempRow[1] = (TextView) view.findViewById(R.id.fragment2TVcontent);
        fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),0));
        fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),0,0,0,0,0,0,0,""));

        initFragment2LineChart();
        initData();
        return view;
    }

    private void  initData(){
        int[][] tempData = {{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30},{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30},{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30},{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30},{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30},{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30},{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30},{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30}};
    }

    //初始化LineChart
    private void initFragment2LineChart(){

        names.add("U相电压");
        names.add("V相电压");
        names.add("W相电压");
        names.add("U相电流");
        names.add("V相电流");
        names.add("W相电流");
        names.add("R相电压");
        names.add("S相电压");
        names.add("T相电压");

        colour.add(Color.YELLOW);
        colour.add(Color.GREEN);
        colour.add(Color.RED);
        colour.add(Color.YELLOW);
        colour.add(Color.GREEN);
        colour.add(Color.RED);
        colour.add(Color.YELLOW);
        colour.add(Color.GREEN);
        colour.add(Color.RED);

        fragment2ChartManager = new DynamicLineChartManager(fragment2LineChar,names,colour);
        fragment2ChartManager.setYAxis(500,-500,100);

        //fragment2LineChar.setDragEnabled(false);                                        //拖拽
        //fragment2LineChar.setTouchEnabled(false);                                       //触摸
        //fragment2LineChar.setScaleEnabled(false);                                       //缩放
        //fragment2LineChar.setPinchZoom(false);                                          //多点缩放
        fragment2LineChar.getDescription().setEnabled(false);                           //隐藏描述

    }
}

