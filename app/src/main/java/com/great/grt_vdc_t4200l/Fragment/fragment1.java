package com.great.grt_vdc_t4200l.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import com.great.grt_vdc_t4200l.ListView.longItem;
import com.great.grt_vdc_t4200l.ListView.longItemAdapter;
import com.great.grt_vdc_t4200l.MPLineChart.DynamicLineChartManager;
import com.great.grt_vdc_t4200l.R;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.github.mikephil.charting.charts.LineChart;
import java.util.LinkedList;

//public class fragment1 extends Fragment implements AdapterView.OnItemClickListener{
public class fragment1 extends Fragment{

    //MPAndroidChart----------------------------------------------------
    private LineChart[] fragment1Lc = new LineChart[4];
    private DynamicLineChartManager[] dynamicLineChartManager = new DynamicLineChartManager[4];
    private List<Integer> list = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合

    private int[][] dataArray = new int[10][120];
    private int[] data = new int[10];
    private int dataTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment1,container,false);


        fragment1Lc[0] = view.findViewById(R.id.in_v_chart);
        fragment1Lc[1] = view.findViewById(R.id.cap_v_chart);
        fragment1Lc[2] = view.findViewById(R.id.out_v_chart);
        fragment1Lc[3] = view.findViewById(R.id.out_i_chart);

        initLineChart();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        //开始Handler
        fragment1Handler.post(fragment1Runnable);
    }

    @Override
    public void onPause(){
        super.onPause();
        //注销Handler
        fragment1Handler.removeCallbacks(fragment1Runnable);
    }

    /** 初始化图表 */
    private void initLineChart(){

        names.add("R相");
        names.add("S相 ");
        names.add("T相");
        colour.add(Color.YELLOW);
        colour.add(Color.GREEN);
        colour.add(Color.RED);
//        colour.add(0xFFC23531);
//        colour.add(0xFF61A0A8);
//        colour.add(0xFF2F4554);
        dynamicLineChartManager[0] = new  DynamicLineChartManager(fragment1Lc[0],names,colour);
        dynamicLineChartManager[0].setYAxis(450,0,6);

        names.clear();
        colour.clear();
        names.add("电容");
        colour.add(Color.YELLOW);
//        colour.add(0xFFC23531);
        dynamicLineChartManager[1] = new  DynamicLineChartManager(fragment1Lc[1],names,colour);
        dynamicLineChartManager[1].setYAxis(450,0,6);

        names.clear();
        colour.clear();
        names.add("U相");
        names.add("V相 ");
        names.add("W相");
        colour.add(Color.YELLOW);
        colour.add(Color.GREEN);
        colour.add(Color.RED);
//        colour.add(0xFFC23531);
//        colour.add(0xFF61A0A8);
//        colour.add(0xFF2F4554);
        dynamicLineChartManager[2] = new  DynamicLineChartManager(fragment1Lc[2],names,colour);
        dynamicLineChartManager[2].setYAxis(450,0,6);

        dynamicLineChartManager[3] = new  DynamicLineChartManager(fragment1Lc[3],names,colour);
        dynamicLineChartManager[3].setYAxis(600,0,6);
    }

    /**
     * 计算一分钟内采集到的数据
     * 输入 一分钟数据的二维数组
     * 输出 根据输入计算出的和平均数差异最大的值
     */
    private void countDataArray(){

        for (int i = 0; i < 10 ; i++) {
            int tempData = 0;

//            System.out.print("第" + i + "组 \r\n");

            //求平均数
            for (int j = 0; j < 120  ; j++) {
                tempData += dataArray[i][j];
            }
            tempData = tempData/120;

//            System.out.print("平均数：" + tempData + "\r\n 数组:");
//            System.out.println(Arrays.toString(dataArray[i]));
//            System.out.print("\r\n");

            //冒泡法排列后，求差异最大的数
            for (int j = 0; j < dataArray[i].length - 1 ; j++) {
                for (int k = 0; k < dataArray[i].length - j - 1 ; k++) {
                    if (dataArray[i][k] > dataArray[i][k+1]){
                        int temp = dataArray[i][k];
                        dataArray[i][k] = dataArray[i][k+1];
                        dataArray[i][k+1] = temp;
                    }
                }
            }
            if (tempData - dataArray[i][0] >= dataArray[i][119] - tempData){
                tempData = dataArray[i][0];
            }else if (tempData - dataArray[i][0] < dataArray[i][119] - tempData){
                tempData = dataArray[i][119];
            }

//            System.out.print("冒泡数组:");
//            System.out.println(Arrays.toString(dataArray[i]));
//            System.out.print("\r\n");
//            System.out.print("\"差异数 ：" + tempData + "\r\n ");

            data[i] = tempData;
//            System.out.println("差异数组："+Arrays.toString(data));

        }

    }

    /**
     * 对图表填充描绘新的点
     */
    private void addChartEntry(){

        //输入电压
        for (int i = 0; i < 3 ; i++) {
            list.add(data[i]);
            Log.e("addChartEntry: ","输入电压"+data[i] );
        }
        dynamicLineChartManager[0].addEntry(list);
        list.clear();

        //电容
        list.add(data[3]);
        Log.e("addChartEntry: ","电容"+data[3] );
        dynamicLineChartManager[1].addEntry(list);

        //输出电压
        for (int i = 4; i < 7 ; i++) {
            list.add(data[i]);
            Log.e("addChartEntry: ","输出电压"+data[i] );
        }
        dynamicLineChartManager[2].addEntry(list);
        list.clear();

        //输出电流
        for (int i = 7; i < 10 ; i++) {
            list.add(data[i]);
            Log.e("addChartEntry: ","输出电流"+data[i] );
        }
        dynamicLineChartManager[3].addEntry(list);
        list.clear();

    }


    /**
     * 定时中断事件
     */
    Handler fragment1Handler = new Handler();
    Runnable fragment1Runnable = new Runnable() {
        @Override
        public void run() {
            fragment1Handler.postDelayed(this,500);

            SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
//            Log.e("run: ",rStateData.getBoolean("is_CommFlag",false)+"" );
            if (rStateData.getBoolean("is_CommFlag",false)) {

                //填充数据
                SharedPreferences rRealData = getActivity().getSharedPreferences("RealData", 0);

                dataArray[0][dataTime] = rRealData.getInt("i_Rv", 0);
                dataArray[1][dataTime] = rRealData.getInt("i_Sv", 0);
                dataArray[2][dataTime] = rRealData.getInt("i_Tv", 0);

                dataArray[3][dataTime] = rRealData.getInt("i_Capv", 0);

                dataArray[4][dataTime] = rRealData.getInt("i_Uv", 0);
                dataArray[5][dataTime] = rRealData.getInt("i_Vv", 0);
                dataArray[6][dataTime] = rRealData.getInt("i_Wv", 0);

                dataArray[7][dataTime] = rRealData.getInt("i_Ua", 0);
                dataArray[8][dataTime] = rRealData.getInt("i_Va", 0);
                dataArray[9][dataTime] = rRealData.getInt("i_Wa", 0);

                //存储数据计次
                dataTime++;

                if (dataTime >= 120) {
//                    System.out.println("Rv:" + Arrays.toString(dataArray[0]) +"\r\n");
//                    System.out.println("Sv:" + Arrays.toString(dataArray[1]) +"\r\n");
//                    System.out.println("Tv:" + Arrays.toString(dataArray[2]) +"\r\n");
//                    System.out.println("Capv:" + Arrays.toString(dataArray[3]) +"\r\n");
//                    System.out.println("Uv:" + Arrays.toString(dataArray[4]) +"\r\n");
//                    System.out.println("Vv:" + Arrays.toString(dataArray[5]) +"\r\n");
//                    System.out.println("Wv:" + Arrays.toString(dataArray[6]) +"\r\n");
//                    System.out.println("Ua:" + Arrays.toString(dataArray[7]) +"\r\n");
//                    System.out.println("Va:" + Arrays.toString(dataArray[8]) +"\r\n");
//                    System.out.println("Wa:" + Arrays.toString(dataArray[9]) +"\r\n");
                    dataTime = 0;
                    countDataArray();
                    addChartEntry();
                }

            }


//
//            SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
//
//            if (rStateData.getInt("layPage",0) == 1){
//
////                if (rStateData.getBoolean("is_CommFlag",false)) {
//
//                    SharedPreferences rRealData = getActivity().getSharedPreferences("RealData", 0);
////                    SharedPreferences rAlarmData = getActivity().getSharedPreferences("AlarmData",0);
//
//                    switch (selectTabType) {
//                        case "输出电流":
//                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Ua), rRealData.getInt("i_Ua", 0)));
//                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Va), rRealData.getInt("i_Va", 0)));
//                            fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Wa), rRealData.getInt("i_Wa", 0)));
//                            list.add(rRealData.getInt("i_Ua", 0));
//                            list.add(rRealData.getInt("i_Va", 0));
//                            list.add(rRealData.getInt("i_Wa", 0));
//                            dynamicLineChartManager.setYAxis(300,0,10);
//                            break;
//                        case "输出电压":
//                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Uv), rRealData.getInt("i_Uv", 0)));
//                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Vv), rRealData.getInt("i_Vv", 0)));
//                            fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Wv), rRealData.getInt("i_Wv", 0)));
//                            list.add(rRealData.getInt("i_Uv", 0));
//                            list.add(rRealData.getInt("i_Vv", 0));
//                            list.add(rRealData.getInt("i_Wv", 0));
//                            dynamicLineChartManager.setYAxis(500,0,5);
//                            break;
//                        case "输入电压":
//                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Rv), rRealData.getInt("i_Rv", 0)));
//                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Sv), rRealData.getInt("i_Sv", 0)));
//                            fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Tv), rRealData.getInt("i_Tv", 0)));
//                            list.add(rRealData.getInt("i_Rv", 0));
//                            list.add(rRealData.getInt("i_Sv", 0));
//                            list.add(rRealData.getInt("i_Tv", 0));
//                            dynamicLineChartManager.setYAxis(500,0,5);
//                            break;
////                        case "其他数据":
////                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Capv), rRealData.getInt("i_Capv", 0)));
////                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1hz), rRealData.getInt("i_Hz", 0)));
////                            fragment1TempRow[2].setText("");
////                            list.add(rRealData.getInt("i_Capv", 0));
////                            list.add(0);
////                            list.add(0);
////                            break;
//                    }
//
//                    dynamicLineChartManager.addEntry(list);
//                    list.clear();
//
//                    List<longItem> fragment1_Data = new LinkedList<>();
//                    fragment1_Data.add(new longItem("","","R相电压",rRealData.getInt("i_Rv", 0)+" V"));
//                    fragment1_Data.add(new longItem("","","S相电压",rRealData.getInt("i_Sv", 0)+" V"));
//                    fragment1_Data.add(new longItem("","","T相电压",rRealData.getInt("i_Tv", 0)+" V"));
//                    fragment1_Data.add(new longItem("","","U相电压",rRealData.getInt("i_Uv", 0)+" V"));
//                    fragment1_Data.add(new longItem("","","V相电压",rRealData.getInt("i_Vv", 0)+" V"));
//                    fragment1_Data.add(new longItem("","","W相电压",rRealData.getInt("i_Wv", 0)+" V"));
//                    fragment1_Data.add(new longItem("","","U相电流",rRealData.getInt("i_Ua", 0)+" A"));
//                    fragment1_Data.add(new longItem("","","V相电流",rRealData.getInt("i_Va", 0)+" A"));
//                    fragment1_Data.add(new longItem("","","W相电流",rRealData.getInt("i_Wa", 0)+" A"));
//                    fragment1_Data.add(new longItem("","","电容电压",rRealData.getInt("i_Capv", 0)+" A"));
//
//                    longItemAdapter fragment1_RecordAdapter = new longItemAdapter((LinkedList<longItem>) fragment1_Data,fragment1_Context,"fragment1");
//                    fragment1_ListView.setAdapter(fragment1_RecordAdapter);
//
////                    fragment1TempRow[3].setText(String.format(getResources().getString(R.string.fragment1AlarmTime), rAlarmData.getInt("i_AlarmTime", 0)));
////                    fragment1TempRow[4].setText(String.format(getResources().getString(R.string.fragment1RecordTime), rAlarmData.getInt("i_RecordTime", 0)));
////                }
//            }
        }
    };

}

