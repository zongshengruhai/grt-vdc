package com.great.grt_vdc_t4200l.Fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.components.Legend;
import com.great.grt_vdc_t4200l.ListView.record;
import com.great.grt_vdc_t4200l.ListView.recordAdapter;
import com.great.grt_vdc_t4200l.MPLineChart.DynamicLineChartManager;
import com.great.grt_vdc_t4200l.R;

//
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

//tablayout
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.github.mikephil.charting.charts.LineChart;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class fragment1 extends Fragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "fragment1";

    //TabLayout
    private static final String[] sTitle = new String[]{"输出电压","输出电流","输入电压","其他数据"};
    private TabLayout tl;

    private TextView[] fragment1TempRow = new TextView[5];

    //MPAndroidChart
    private LineChart fragment1Lc;
    private DynamicLineChartManager dynamicLineChartManager;
    private List<Integer> list = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合

    //广播声明
    //Intent dataChange = new Intent("drc.xxx.yyy.fragment1");
    private fragment1Broad fragment1ActivityBroad = null;
    private IntentFilter fragment1IntentFilter = new IntentFilter("drc.xxx.yyy.fragment1");

    //更新UI
    private String selectTabType="输出电压";

    //listView
    private Context fragment1_Context;
    private ListView fragment1_ListView;


    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment1,container,false);

        //关联控件
        tl = view.findViewById(R.id.tl);
        //lineChart = view.findViewById(R.id.fragment1Chart);
        fragment1Lc = view.findViewById(R.id.fragment1Chart);
        fragment1TempRow[0] = view.findViewById(R.id.dataA);
        fragment1TempRow[1] = view.findViewById(R.id.dataB);
        fragment1TempRow[2] = view.findViewById(R.id.dataC);
        fragment1TempRow[3] = view.findViewById(R.id.fragment1alarmTV);
        fragment1TempRow[4] = view.findViewById(R.id.fragment1RecorTV);

        fragment1TempRow[3].setText(String.format(getResources().getString(R.string.fragment1AlarmTime),0));
        fragment1TempRow[4].setText(String.format(getResources().getString(R.string.fragment1RecordTime),0));

        fragment1_Context = view.getContext();
//        fragment1_ListView = view.findViewById(R.id.fragment1_ListView);

        initTabLayout();
        initLineChart();
//        initListView();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
//        Log.e(TAG, "碎片1，已完成初始化");
    }

    @Override
    public void onStart(){
        super.onStart();
//        Log.e(TAG, "碎片1，启动");
    }

    //重载
    @Override
    public void onResume(){
        super.onResume();

        //注册广播
        if (fragment1ActivityBroad == null){
            fragment1ActivityBroad = new fragment1Broad();
            getActivity().registerReceiver(fragment1ActivityBroad,fragment1IntentFilter);
//            Log.e(TAG,"fragment1，已注册广播");
        }

        //开始Handler
        fragment1Handler.post(fragment1Runnable);

    }
    //中止
    @Override
    public void onPause(){
        super.onPause();

        //注销广播
        if (fragment1ActivityBroad != null){
            getActivity().unregisterReceiver(fragment1ActivityBroad);
            fragment1ActivityBroad = null;
            //Log.e(TAG,"fragment1,已注销广播");
        }

        //注销Handler
        fragment1Handler.removeCallbacks(fragment1Runnable);

    }
    @Override
    public void onStop(){
        super.onStop();
        //Log.e(TAG, "碎片1，停止");
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        //Log.e(TAG, "碎片1，销毁视图");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        //Log.e(TAG, "碎片1，销毁");
    }
    @Override
    public void onDetach(){
        super.onDetach();
        // Log.e(TAG, "碎片1，解除活动绑定");
    }

    /**fragment1广播
     * 描述：fragment1层接收广播，用于接收BaseCourse底层广播的遥测数据
     * 方法：
     *      1）创建方法：fragment活动Resume创建，pause销毁
     **/
    public class fragment1Broad extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent){
            //接收到广播
            //Log.e(TAG,"接收到广播");
            /*
            int dataChange = intent.getExtras().getInt("dataChange");
            //Log.e(TAG,""+dataChange);
            if (changeUIflag == 0){
                fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Uv),dataChange));
                fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Vv),dataChange));
                fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Wv),dataChange));
            }else if (changeUIflag == 1){
                fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Ua),dataChange));
                fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Va),dataChange));
                fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Wa),dataChange));
            }else if (changeUIflag == 2){
                fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Rv),dataChange));
                fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Sv),dataChange));
                fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Tv),dataChange));
            }else if (changeUIflag == 3){
                fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Capv),dataChange));
                fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1hz),dataChange));
                fragment1TempRow[2].setText("");
            }
            */
        }
    }

    //初始化TabLayout
    private void initTabLayout() {

        //更新tl
        for (int i = 0; i < 4; i++) {
            tl.addTab(tl.newTab().setText(sTitle[i]));
        }


        //监听tl事件
        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            //挑选事件
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                //initLineChart();
                clearChart();

                selectTabType = (String) tab.getText();

            }

            //离开事件
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //Log.i(TAG, "onTabUnselected" + tab.getText());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //Log.i(TAG, "onTabReselected" + tab.getText());
            }
        });
    }
    //初始化LineChart
    private void initLineChart(){

        names.add("U相（R相、电容电压） ");
        names.add("V相（S相） ");
        names.add("W相（T相）");

        colour.add(Color.YELLOW);
        colour.add(Color.GREEN);
        colour.add(Color.RED);

        dynamicLineChartManager = new DynamicLineChartManager(fragment1Lc,names,colour);
        dynamicLineChartManager.setYAxis(500,0,10);

        fragment1Lc.setDragEnabled(false);                                        //拖拽
        fragment1Lc.setTouchEnabled(false);                                       //触摸
        fragment1Lc.setScaleEnabled(false);                                       //缩放
        fragment1Lc.setPinchZoom(false);                                          //多点缩放
        fragment1Lc.getDescription().setEnabled(false);                           //隐藏描述

        //dynamicLineChartManager.setLowLimitLine(0,"0");
    }
    //清除画布
    private void clearChart(){
//        dynamicLineChartManager.clear();
        for (int i = 0; i < 9; i++) {
            list.add(0);
            list.add(0);
            list.add(0);
            dynamicLineChartManager.addEntry(list);
            list.clear();
        }
    }

    //ListView点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.e(TAG,"you pick :"+ position + "项");
    }
    //初始化ListView
    private void initListView(){

        String PATH = fragment1_Context.getFilesDir().getPath() + "/fault_log/";
        String pathFileName = fragment1_Context.getFilesDir().getPath() + "/fault_log/fault_record.xls";

        File file = new File(PATH);
        if (file.exists()){
            Log.e(TAG, "initListView: 文件夹存在" );
            File[] files = file.listFiles();
            if (files != null){
//                for (int i = 0; i < files.length; i++) {
//                    pathFileName = files[i].getAbsolutePath();
                for (File i : files){
                    pathFileName = i.getAbsolutePath();
//                    Log.e(TAG, "initListView: "+pathFileName);
                    if (pathFileName.equals(fragment1_Context.getFilesDir().getPath() + "/fault_log/fault_record.xls")){
                        loadListData(pathFileName,true);
                    }
                }
            }else {
                Log.e(TAG, "initListView: 文件不存在");
                loadListData(pathFileName,false);
            }
        }else {
            loadListData(pathFileName,false);
            Log.e(TAG, "initListView: 文件夹不存在" );
        }
    }
    //载入ListDat
    private void loadListData(String fileName,boolean type){
        int rows;                                                           //行数量
        String[] temp = new String[3];
        List<record> fragment1_Data;
        recordAdapter fragment1_RecordAdapter;
        fragment1_Data = new LinkedList<>();

        fragment1_Data.clear();
        fragment1_RecordAdapter = new recordAdapter((LinkedList<record>) fragment1_Data,fragment1_Context);
        fragment1_ListView.setAdapter(fragment1_RecordAdapter);

        if (fileName != null && type){
            try {
                FileInputStream mfis = new FileInputStream(fileName);
                Workbook mbook = Workbook.getWorkbook(mfis);
                int msheer = mbook.getNumberOfSheets();                     //表数量
                Sheet[] mSheetlist = mbook.getSheets();                     //表内容

                for (int i = 0; i < msheer; i++) {
                    rows = mSheetlist[i].getRows();
                    for (int j  = 0; j < rows; j++) {
                        Cell[] cellList = mSheetlist[i].getRow(j);
                        for (Cell cell : cellList) {
                            temp[cell.getColumn()] = cell.getContents();
                        }
                        fragment1_Data.add(new record(temp[0],temp[1],temp[2],""));
                        fragment1_RecordAdapter = new recordAdapter((LinkedList<record>) fragment1_Data,fragment1_Context);
                        fragment1_ListView.setAdapter(fragment1_RecordAdapter);
                        fragment1_ListView.setOnItemClickListener(this);
                    }
                }
                mbook.close();
            } catch (Exception e) {
                System.out.println("fragment1,Exception:  " + e);

                fragment1_Data.add(new record("","最近没有故障记录","",""));
                fragment1_RecordAdapter = new recordAdapter((LinkedList<record>) fragment1_Data,fragment1_Context);
                fragment1_ListView.setAdapter(fragment1_RecordAdapter);
                fragment1_ListView.setOnItemClickListener(this);
            }
        } else {

            fragment1_Data.add(new record("","最近没有故障记录","",""));
            fragment1_RecordAdapter = new recordAdapter((LinkedList<record>) fragment1_Data,fragment1_Context);
            fragment1_ListView.setAdapter(fragment1_RecordAdapter);
            fragment1_ListView.setOnItemClickListener(this);
        }
    }

    //fragment1定时时间
    Handler fragment1Handler = new Handler();
    Runnable fragment1Runnable = new Runnable() {
        @Override
        public void run() {
            //定时时间
            fragment1Handler.postDelayed(this,500);

            SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
            boolean _isCommFlag = rStateData.getBoolean("is_CommFlag",false);
            int layPage = rStateData.getInt("layPage",0);

            if (layPage == 1){
                if (!_isCommFlag) {

                    SharedPreferences rRealData = getActivity().getSharedPreferences("RealData", 0);
                    SharedPreferences rAlarmData = getActivity().getSharedPreferences("AlarmData",0);

                    switch (selectTabType) {
                        case "输出电压":
                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Uv), rRealData.getInt("i_Uv", 0)));
                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Vv), rRealData.getInt("i_Vv", 0)));
                            fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Wv), rRealData.getInt("i_Wv", 0)));
                            list.add(rRealData.getInt("i_Uv", 0));
                            list.add(rRealData.getInt("i_Vv", 0));
                            list.add(rRealData.getInt("i_Wv", 0));
                            break;
                        case "输出电流":
                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Ua), rRealData.getInt("i_Ua", 0)));
                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Va), rRealData.getInt("i_Va", 0)));
                            fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Wa), rRealData.getInt("i_Wa", 0)));
                            list.add(rRealData.getInt("i_Ua", 0));
                            list.add(rRealData.getInt("i_Va", 0));
                            list.add(rRealData.getInt("i_Wa", 0));
                            break;
                        case "输入电压":
                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Rv), rRealData.getInt("i_Rv", 0)));
                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1Sv), rRealData.getInt("i_Sv", 0)));
                            fragment1TempRow[2].setText(String.format(getResources().getString(R.string.fragment1Tv), rRealData.getInt("i_Tv", 0)));
                            list.add(rRealData.getInt("i_Rv", 0));
                            list.add(rRealData.getInt("i_Sv", 0));
                            list.add(rRealData.getInt("i_Tv", 0));
                            break;
                        case "其他数据":
                            fragment1TempRow[0].setText(String.format(getResources().getString(R.string.fragment1Capv), rRealData.getInt("i_Capv", 0)));
                            fragment1TempRow[1].setText(String.format(getResources().getString(R.string.fragment1hz), rRealData.getInt("i_Hz", 0)));
                            fragment1TempRow[2].setText("");
                            list.add(rRealData.getInt("i_Capv", 0));
                            list.add(0);
                            list.add(0);
                            break;
                    }

                    dynamicLineChartManager.addEntry(list);
                    list.clear();

                    fragment1TempRow[3].setText(String.format(getResources().getString(R.string.fragment1AlarmTime), rAlarmData.getInt("i_AlarmTime", 0)));
                    fragment1TempRow[4].setText(String.format(getResources().getString(R.string.fragment1RecordTime), rAlarmData.getInt("i_RecordTime", 0)));
                }
            }
        }
    };

}

