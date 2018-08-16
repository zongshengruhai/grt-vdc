package com.great.grt_vdc_t4200l.Fragment;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
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
    LineChart fragment2LineChar;
    private List<Integer> list = new ArrayList<>();         //数据集合
    private List<Integer> colour = new ArrayList<>();       //折线颜色
    private List<String> names = new ArrayList<>();          //折线名称

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment2,container,false);

        fragment2LineChar = (LineChart) view.findViewById(R.id.fragment2LineChart);

        fragment2TempRow[0] = (TextView) view.findViewById(R.id.fragment2TVtime);
        fragment2TempRow[1] = (TextView) view.findViewById(R.id.fragment2TVcontent);
        fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),0));
        fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),0,0,0,0,0,0,0,""));

        //initFragment2LineChart();
        return view;
    }


    //初始化LineChart
    private void initFragment2LineChart(){


    }
}

