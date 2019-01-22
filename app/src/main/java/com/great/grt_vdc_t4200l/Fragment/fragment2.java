package com.great.grt_vdc_t4200l.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.great.grt_vdc_t4200l.ListView.longItem;
import com.great.grt_vdc_t4200l.ListView.longItemAdapter;
import com.great.grt_vdc_t4200l.R;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import static android.content.Context.MODE_PRIVATE;

//import android.view.MotionEvent;
//import com.github.mikephil.charting.listener.ChartTouchListener;
//import com.github.mikephil.charting.listener.OnChartGestureListener;
//import com.great.grt_vdc_t4200l.MPLineChart.fragment2LineChartManager;


/**
 * fragment2 录波记录
 * 控制流程：
 *      1、初始化List
 *      2、初始化图表
 *      3、根据筛选的editText动态更新List
 *      4、当List发生点击事件时，获取点击List中携带的文件路径作为句柄，查找文件夹中的excel文件，并读取数据更新到图表
 */
public class fragment2 extends Fragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "fragment2";
    //TextView容器----------------------------------------------------
    private TextView[] fragment2TempRow = new TextView[2];
    private TextView fragment2_Null;
    //MPAndroidChart----------------------------------------------------
//    private fragment2LineChartManager[] fragment2ChartManager = new fragment2LineChartManager[3];
    private LineChart[] fragment2LineChart = new LineChart[3];
    private List<Integer> list = new ArrayList<>();             //数据集合
    //listView----------------------------------------------------
    private Context fragment2_Context;
    private ListView fragment2_ListView;
    private List<longItem> fragment2_Data  = new LinkedList<>();
    //    private shortItemAdapter fragment2_RecordAdapter;
    //正则筛选----------------------------------------------------
    private EditText Search_EditText;
    private ImageView Search_Delete;
    //    private TextView Search_Inquire;
    //进度条----------------------------------------------------
    private NumberProgressBar fragment2_Loading;            //进度条实例
    private int iProgress;                                         //进度
    private boolean _isLoadFlag = true;                     //控制进度条显示位
    //控制线程----------------------------------------------------
//    private volatile boolean _isUpData = false;                      //控制线程
    //加载文件内容----------------------------------------------------
    private String pickFileName = null;                            //文件路径
    private int fileTime = 0;                                      //文件数量，当变化时才进行刷新
    //    private boolean ListFlushFlag = false;                         //防止并行
    private Context mContext;

    /**
     * fragment生命周期
     */
    //创建----------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment2,container,false);

        mContext = getContext();

        //图表实例化
        fragment2LineChart[0] = view.findViewById(R.id.f2_LineChart_1);
        fragment2LineChart[1] = view.findViewById(R.id.f2_LineChart_2);
        fragment2LineChart[2] = view.findViewById(R.id.f2_LineChart_3);

        //告警信息TV初始化
        fragment2TempRow[0] = view.findViewById(R.id.fragment2TVtime);
        fragment2TempRow[1] = view.findViewById(R.id.fragment2TVcontent);
        fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),0,""));
        fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),"0.0.0_00:00:00",0,0,""));

        //List内容
        fragment2_Context = view.getContext();
        fragment2_ListView = view.findViewById(R.id.fragment2_ListView);

        //筛选实例化
        Search_EditText = view.findViewById(R.id.fragment2_search_EditText);
        Search_Delete = view.findViewById(R.id.fragment2_search_delete);
//        Search_Inquire = view.findViewById(R.id.fragment2_search_inquire);

        //进度条实例化
        fragment2_Loading = view.findViewById(R.id.f2_Progress_bar);

        //未选择录波hint
        fragment2_Null = view.findViewById(R.id.f2_nullFileHint);

        //初始化筛选
        initSearch();
        //执行筛选，实际功能是更新list
        SearchListData(null,"jog");

        //初始化图表
        initLineChart();

        return view;
    }
    //重载----------------------------------------------------
    @Override
    public void onResume(){
        super.onResume();
        //注册定时
        f2_UiHandler.post(f2_UiRunable);
    }
    //中止----------------------------------------------------
    @Override
    public void onPause(){
        super.onPause();
        //注销定时
        f2_UiHandler.removeCallbacks(f2_UiRunable);
    }

    /**
     * List
     */
    //初始化List----------------------------------------------------
    private void initSearch(){

        //点击X清零
        Search_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Search_EditText.setText("");
                Search_Delete.setVisibility(View.GONE);
            }
        });

        //监听输入事件
        Search_EditText.addTextChangedListener(new TextWatcher() {

            //文本改变前
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            //文本改变
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0 ){
                    Search_Delete.setVisibility(View.GONE);             //隐藏X
                }else {
                    Search_Delete.setVisibility(View.VISIBLE);          //显示X
                }
                SearchListData(Search_EditText.getText().toString(),"jog");
//                Log.e(TAG, "onTextChanged: " );
            }

            //文本改变后
            @Override
            public void afterTextChanged(Editable s) { }

        });

        //搜索点击事件（已废弃）
//        Search_Inquire.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (TextUtils.isEmpty(Search_EditText.getText().toString().trim())){
//                    Log.e("fragment2","请输入指定日期或编号的事件记录以搜索");
//                }else {
//                    Log.e("fragment2","已点击");
//                }
//            }
//        });

    }
    //筛选List----------------------------------------------------
    private void SearchListData(String SearchFileName,String Type){

        //录波记录存放路径
        String PATH = fragment2_Context.getFilesDir().getPath() + "/record_log/";

        //存放填充数据的集合
        String[] temp = new String[3];

        String str;

        //历遍路径中的所有文件夹
        File file = new File(PATH);
        if (file.exists()){

            File[] files = file.listFiles();
            longItemAdapter fragment2_RecordAdapter;

            //防止List不断更新
            if (files.length == fileTime && files.length != 0 && Type.equals("auto")){
                return;
            }else {
                fileTime = files.length;
                if (files.length == 0){
                    fileTime = 0;
                }
            }

            //清空ListView
            fragment2_Data.clear();

            //路径中没有文件
            if (files.length <= 0){

                fragment2_Data.clear();

                fragment2_Data.add(new longItem("","当前暂无录波记录","",""));
                fragment2_RecordAdapter = new longItemAdapter((LinkedList<longItem>) fragment2_Data,fragment2_Context,"fragment2");
                fragment2_ListView.setAdapter(fragment2_RecordAdapter);
                fragment2_ListView.setOnItemClickListener(this);

            }else {

                int SearchNull = 0;
                int filesLength = files.length;

                SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);

                if (rStateData.getBoolean("is_RecordFlag",false))filesLength = filesLength - 1;

                //历遍所有文件名
                if (filesLength > 0 ) {
                    for (int i = 0; i < filesLength; i++) {
//                for(File i : files){

                        str = files[i].getAbsolutePath().replace(PATH, "");
//                    str = i.getAbsolutePath().replace(PATH, "");

                        String[] regroupFiles;

                        //根据"_"斩开数据
                        regroupFiles = (str.replace(".xls", "")).split("_");

                        //筛选条目
                        if (SearchFileName == null) {

                            temp[0] = regroupFiles[0];
                            temp[1] = regroupFiles[4] + " " + regroupFiles[5].substring(0, regroupFiles[5].length() - 3).trim().replace("：", ":");

                        } else {

                            //正则筛选
                            Pattern SearchPattern = Pattern.compile(SearchFileName);
                            Matcher SearchMatcher = SearchPattern.matcher(str);

                            if (SearchMatcher.find()) {
                                temp[0] = regroupFiles[0];
//                                temp[1] = regroupFiles[4] + " " + regroupFiles[5].substring(0, regroupFiles[5].length() - 3).trim().replace("：", ":");
                                temp[1] = regroupFiles[4] + " " + regroupFiles[5].trim().replace("：", ":");

                            } else {
                                SearchNull++;
                                if (SearchNull < files.length) {
                                    continue;
                                }
                                if (SearchNull == files.length) {
                                    temp[0] = "";
                                    temp[1] = "没有匹配的录波记录，请重新筛选";
                                }
                            }
                        }

                        //更新ListView
                        fragment2_Data.add(new longItem(temp[0], temp[1], "", str));

                    }
                }else if (filesLength == 0){
                    fragment2_Data.add(new longItem("","当前暂无录波记录","",""));
                }
                //旋转List
                Collections.reverse(fragment2_Data);
                fragment2_RecordAdapter = new longItemAdapter((LinkedList<longItem>) fragment2_Data, fragment2_Context,"fragment2");
                fragment2_ListView.setAdapter(fragment2_RecordAdapter);
                fragment2_ListView.setOnItemClickListener(this);
            }
        }
    }
    //List点击事件----------------------------------------------------
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView pickTextUrl = view.findViewById(R.id.event_end_time);
        String pickName = pickTextUrl.getText().toString();

        if (pickName.contains(".xls")){
            if (pickFileName == null)pickFileName = pickName;
            if (_isLoadFlag){
                _isLoadFlag = false;
                fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),0,""));
                fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),"0.0.0_00:00:00",0,0,""));
//                fragment2ChartManager[0].clearLineChart();
//                fragment2ChartManager[1].clearLineChart();
//                fragment2ChartManager[2].clearLineChart();
                new LongThread().start();
//                setData();
            }
        }else {
            pickFileName = null;
        }
    }

    /**
     * 图表
     */
    //初始化图表----------------------------------------------------
    private void initLineChart(){

        //图表设置
        for (int i = 0; i < fragment2LineChart.length ; i++) {

            fragment2LineChart[i].setDrawGridBackground(false);                                                     //图表背景
            fragment2LineChart[i].setDrawBorders(true);                                                             //背景边界
            fragment2LineChart[i].setExtraOffsets(0, 0, 5, 0);

            //缩放拖动设置
            fragment2LineChart[i].setDragEnabled(true);                                                             //拖动
            fragment2LineChart[i].setPinchZoom(true);
            fragment2LineChart[i].setDoubleTapToZoomEnabled(false);                                                 //双击缩放
            fragment2LineChart[i].setScaleXEnabled(false);                                                           //X轴缩放
            fragment2LineChart[i].setScaleYEnabled(false);                                                          //Y轴缩放

            //图例设置
            Legend legend = fragment2LineChart[i].getLegend();
            legend.setForm(Legend.LegendForm.SQUARE);                                                   //图例形式：线、圆、正方形
            legend.setTextSize(20f);                                                                    //图例字体大小
//        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);                         //垂直对齐：顶部、底部
//        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);                       //水平对齐：左、右
//        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);                                 //文字方向：垂直、水平
//        legend.setDrawInside(true);                                                                //是否在表格内部
            legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

            fragment2LineChart[i].getXAxis().setEnabled(false);                                                                    //X轴使能
            fragment2LineChart[i].getAxisLeft().enableGridDashedLine(10, 5, 0);                  //左Y轴网格虚线

//            setData();

            fragment2LineChart[i].invalidate();
        }

        Description description = new Description();
        description.setTextSize(16);
        description.setText("输入电压");
        fragment2LineChart[0].setDescription(description);

        description = new Description();
        description.setTextSize(16);
        description.setText("输出电压");
        fragment2LineChart[1].setDescription(description);

        description = new Description();
        description.setTextSize(16);
        description.setText("输出电流");
        fragment2LineChart[2].setDescription(description);


//        names.add("Uv");
//        names.add("Vv");
//        names.add("Wv");
//        names.add("Ua");
//        names.add("Va");
//        names.add("Wa");
//        names.add("Rv");
//        names.add("Sv");
//        names.add("Tv");
//
//        colour.add(Color.YELLOW);
//        colour.add(Color.GREEN);
//        colour.add(Color.RED);
//        colour.add(Color.YELLOW);
//        colour.add(Color.GREEN);
//        colour.add(Color.RED);
//        colour.add(Color.YELLOW);
//        colour.add(Color.GREEN);
//        colour.add(Color.RED);

//        fragment2ChartManager[0] = new fragment2LineChartManager(fragment2LineChart[0],names,colour);
//        fragment2ChartManager[0].setYAxis(500,-500,10);

//        List<Integer> colour = new ArrayList<>();
//        colour.add(Color.YELLOW);
//        colour.add(Color.GREEN);
//        colour.add(Color.RED);
//
//        List<String> names = new ArrayList<>();
//        names.add("Rv");
//        names.add("Sv");
//        names.add("Tv");
//        fragment2ChartManager[0] = new fragment2LineChartManager(fragment2LineChart[0],names,colour);
////        fragment2ChartManager[0].setYAxis(520,-500,10);
//        fragment2ChartManager[0].setDescription("输入电压");
//        fragment2LineChart[0].setOnChartGestureListener(chartListener);
//
//        names = new ArrayList<>();
//        names.add("Uv");
//        names.add("Vv");
//        names.add("Wv");
//        fragment2ChartManager[1] = new fragment2LineChartManager(fragment2LineChart[1],names,colour);
////        fragment2ChartManager[1].setYAxis(520,-520,10);
//        fragment2ChartManager[1].setDescription("输出电压");
//        fragment2LineChart[1].setOnChartGestureListener(chartListener);
//
//        names = new ArrayList<>();
//        names.add("Ua");
//        names.add("Va");
//        names.add("Wa");
//        fragment2ChartManager[2] = new fragment2LineChartManager(fragment2LineChart[2],names,colour);
////        fragment2ChartManager[2].setYAxis(500,-500,10);
//        fragment2ChartManager[2].setDescription("输出电流");
//        fragment2LineChart[2].setOnChartGestureListener(chartListener);

    }
    //图表手势回调（暂时弃用）
//    private OnChartGestureListener chartListener = new OnChartGestureListener() {
//
//        //手势开始
//        @Override
//        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
////            Log.e(TAG,"手势开始");
//        }
//
//        //手势结束
//        @Override
//        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
////            Log.e(TAG,"手势结束");
//        }
//
//        //长按
//        @Override
//        public void onChartLongPressed(MotionEvent me) {
////            Log.e(TAG,"长按");
//        }
//
//        //双击
//        @Override
//        public void onChartDoubleTapped(MotionEvent me) {
////            Log.e(TAG,"双击图表");
//        }
//
//        //        单击
//        @Override
//        public void onChartSingleTapped(MotionEvent me) {
////            Log.e(TAG,"单击图表");
//        }
//
//        @Override
//        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
////            Log.e(TAG,"now is onChartFling");
//        }
//
//        //缩放
//        @Override
//        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
////            Log.e(TAG,"缩放图表, X轴："+scaleX+", Y轴："+scaleY);
//        }
//
//        //拖动
//        @Override
//        public void onChartTranslate(MotionEvent me, float dX, float dY) {
////            Log.e(TAG,"拖动图表, X轴："+dX+", Y轴："+dY+",me:"+me);
////            fragment2LineChart[0].setTranslationX(dX);
////            fragment2LineChart[0].setTranslationY(dY);
//        }
//    };
//    /*//更新图表（弃用）*/
//    private void fillLineChart(String fileName){
//
//        String PATH = fragment2_Context.getFilesDir().getPath() + "/record_log/";
//
//        int rows;                                                           //行数量
//        int columns;                                                        //列数量
//
//        if (fileName == null){
//            Log.e(TAG,"没有文件名");
//        }else {
//            //填充表头
//            String[] fillContent;
//            fillContent = (fileName.replace(".xls","")).split("_");
//            if (fillContent.length > 0){
//                fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),Integer.parseInt(fillContent[0])));
//                fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),(fillContent[4] + "_" + fillContent[5]),(Integer.parseInt(fillContent[2])),(Integer.parseInt(fillContent[3])),""));
//            }
//
//            fileName = PATH + fileName;
//            Log.e(TAG,"准备加载："+fileName);
//
//            try {
//
//                FileInputStream mfis = new FileInputStream(fileName);
//                Workbook mbook = Workbook.getWorkbook(mfis);
//                int msheer = mbook.getNumberOfSheets();                     //表数量
//                Sheet[] mSheetlist = mbook.getSheets();                     //表内容
//
//                String temp;
//
//                for (int i = 0; i < msheer; i++) {
//                    rows = mSheetlist[i].getRows();
//                    columns = mSheetlist[i].getColumns();
//
//                    Log.e(TAG,"行数："+ rows + "列数："+columns);
//                    for (int j = 1; j < rows; j++) {
//                        int min = 0;
//                        for (int k = 0; k < 3; k++) {
//                            for (int z = min; z < min + 3 ; z++) {
//                                Cell cell = mSheetlist[i].getCell(z,j);
//                                temp =(cell.getContents()).trim();
//                                list.add(Integer.parseInt(temp));
//                            }
//                            min = min + 3;
//                            fragment2ChartManager[k].addEntry(list);
//                            list.clear();
//                        }
//
////                        for (int k = 0; k < 3 ; k++) {
////                            Cell cell = mSheetlist[i].getCell(k,j);
////                            temp =(cell.getContents()).trim();
////                            list.add(Integer.parseInt(temp));
////                        }
//////                        Cell[] cellList = mSheetlist[i].getRow(j);
//////                        for (Cell cell : cellList) {
//////                            temp = (cell.getContents()).trim();
//////                            Log.e(TAG,Integer.parseInt(temp)+"");
//////                            list.add(Integer.parseInt(temp));
//////                        }
////                        fragment2ChartManager[0].addEntry(list);
////                        list.clear();
//                    }
//                }
//                mbook.close();
//
//            }catch (Exception e){
//                System.out.println("fragment2,Exception: " + e);
//            }
//        }
//    }

    /**
     * 定时线程
     */
    Handler f2_UiHandler = new Handler();
    Runnable f2_UiRunable = new Runnable() {
        @Override
        public void run() {
            f2_UiHandler.postDelayed(this,500);

            SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
            if (rStateData.getInt("layPage",0) == 2) {

                if (rStateData.getBoolean("changeList",false)){
                    SearchListData(Search_EditText.getText().toString(), "jog");
                    SharedPreferences.Editor wStateData = getActivity().getSharedPreferences("StateData",MODE_PRIVATE).edit();
                    wStateData.putBoolean("changeList",false);
                    if (wStateData.commit())wStateData.commit();
                }else {
                    SearchListData(Search_EditText.getText().toString(), "auto");
                }
                if (!_isLoadFlag) {
                    //隐藏图表，显示进度条
                    fragment2LineChart[0].setVisibility(View.GONE);
                    fragment2LineChart[1].setVisibility(View.GONE);
                    fragment2LineChart[2].setVisibility(View.GONE);
                    fragment2_Loading.setVisibility(View.VISIBLE);
                    fragment2_Loading.setProgress(iProgress);
                    //提示点击
                    fragment2_Null.setVisibility(View.GONE);
                    fragment2TempRow[0].setVisibility(View.VISIBLE);
                    fragment2TempRow[1].setVisibility(View.VISIBLE);
                } else {
                    if (pickFileName != null) {
                        iProgress = 0;
                        //填充表头
                        String[] fillContent;
                        fillContent = (pickFileName.replace(".xls", "")).split("_");
                        String eventType = "电压暂降";
//                        switch (fillContent[1]){
//                            case "1":
//                                eventType = "电压暂降";
//                                break;
////                            case "2":
////                                eventType = "输出过流";
////                                break;
////                            case "3":
////                                eventType = "输出短路";
////                                break;
//                        }
                        fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime), Integer.parseInt(fillContent[0]),eventType));
                        fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent), (fillContent[4] + " " + fillContent[5]).replace("：",":"), (Integer.parseInt(fillContent[2])), (Integer.parseInt(fillContent[3])), ""));
                        //移动到表头
                        fragment2LineChart[0].moveViewToX(0);
                        fragment2LineChart[1].moveViewToX(0);
                        fragment2LineChart[2].moveViewToX(0);
                        //显示图表，隐藏进度条
                        fragment2LineChart[0].setVisibility(View.VISIBLE);
                        fragment2LineChart[1].setVisibility(View.VISIBLE);
                        fragment2LineChart[2].setVisibility(View.VISIBLE);
                        fragment2_Loading.setVisibility(View.GONE);
                        //提示点击
                        fragment2_Null.setVisibility(View.GONE);
                        fragment2TempRow[0].setVisibility(View.VISIBLE);
                        fragment2TempRow[1].setVisibility(View.VISIBLE);
                        //清零选择
                        pickFileName = null;
                    }
//                    else {
//                        fragment2LineChart[0].setVisibility(View.GONE);
//                        fragment2LineChart[1].setVisibility(View.GONE);
//                        fragment2LineChart[2].setVisibility(View.GONE);
//                        fragment2_Loading.setVisibility(View.GONE);
//                        fragment2_Null.setVisibility(View.VISIBLE);
//                        fragment2TempRow[0].setVisibility(View.GONE);
//                        fragment2TempRow[1].setVisibility(View.GONE);
//                    }
                }
            }
        }
    };

    /**
     * 子线程
     * 更新图表（耗时）
     */
    class LongThread extends Thread{
        public void run(){

            setData();
//            int rows;                                                           //行数量
//            int columns;                                                        //列数量
//
//            if (pickFileName == null){
//
//                Log.e(TAG,"没有文件名");
//
//            }else {
//
//                String pickFilePath = fragment2_Context.getFilesDir().getPath() + "/record_log/" + pickFileName;
//                Log.e(TAG,"准备加载："+ pickFilePath);
//                try {
//
//                    FileInputStream mfis = new FileInputStream(pickFilePath);
//                    Workbook mbook = Workbook.getWorkbook(mfis);
//                    int msheer = mbook.getNumberOfSheets();                     //表数量
//                    Sheet[] mSheetlist = mbook.getSheets();                     //表内容
//
//                    String temp;
//
//                    for (int i = 0; i < msheer; i++) {
//                        rows = mSheetlist[i].getRows();
//                        columns = mSheetlist[i].getColumns();
//
//                        Log.e(TAG,"行数："+ rows + "列数："+columns);
//                        for (int j = 1; j < rows; j++) {
//                            int min = 0;
//                            for (int k = 0; k < 3; k++) {
//                                for (int z = min; z < min + 3 ; z++) {
//                                    Cell cell = mSheetlist[i].getCell(z,j);
//                                    temp =(cell.getContents()).trim();
//                                    list.add(Integer.parseInt(temp));
//                                }
//                                min = min + 3;
//                                fragment2ChartManager[k].addEntry(list);
//                                list.clear();
//                            }
//                            iProgress = (((100000/rows)*j)/1000);
//                        }
//                    }
//                    mbook.close();
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//            _isLoadFlag = true;
        }
    }

    private void setData(){

        ArrayList<ArrayList<Entry>> value = new ArrayList<>();

        int rows;                                                           //行数量
        int columns;                                                        //列数量

        if (pickFileName == null){

            for (int i = 0; i < 9; i++) {
                ArrayList<Entry> val = new ArrayList<>();
                for (int j = 0; j < 1650 ; j++) {
                    val.add(new Entry(j,0));
                }
                value.add(val);
            }

        }else {

            String pickFilePath = fragment2_Context.getFilesDir().getPath() + "/record_log/" + pickFileName;
            Log.e(TAG,"准备加载："+ pickFilePath);
            try {

                FileInputStream mfis = new FileInputStream(pickFilePath);
                Workbook mbook = Workbook.getWorkbook(mfis);
                int msheer = mbook.getNumberOfSheets();                     //表数量
                Sheet[] mSheetlist = mbook.getSheets();                     //表内容

                String temp;

                list.clear();
                for (int i = 0; i < msheer; i++) {
                    rows = mSheetlist[i].getRows();
                    columns = mSheetlist[i].getColumns();
                    Log.e(TAG,"行数："+ rows + "列数："+columns);
                    for (int j = 1; j < rows; j++) {
                        int min = 0;
                        for (int k = 0; k < 3; k++) {
                            for (int z = min; z < min + 3 ; z++) {
                                Cell cell = mSheetlist[i].getCell(z,j);
                                temp =(cell.getContents()).trim();
                                list.add(Integer.parseInt(temp));
                            }
                            min = min + 3;
                        }
                        iProgress = (((100000/rows)*j)/1000);
                    }
                }
                mbook.close();

            }catch (Exception e){
                e.printStackTrace();
            }

            for (int i = 0; i < 9; i++) {
                int k = 0;
                ArrayList<Entry> val = new ArrayList<>();
                for (int j = i; j < list.size() ; j++) {
                    val.add(new Entry(k,list.get(j)));
                    j=j+8;
                    k++;
                }
                value.add(val);
            }

            //位置变化
            for (int i = 0; i < 3 ; i++) {
                Collections.swap(value,i,i+3);
            }

            _isLoadFlag = true;
        }

        for (int i = 0; i < 3  ; i++) {
            int k = i * 3;

            LineDataSet[] set = new LineDataSet[3];

            if (fragment2LineChart[i].getData() != null && fragment2LineChart[i].getData().getDataSetCount() > 0) {

                set[0] = (LineDataSet) fragment2LineChart[i].getData().getDataSetByIndex(0);
                set[1] = (LineDataSet) fragment2LineChart[i].getData().getDataSetByIndex(1);
                set[2] = (LineDataSet) fragment2LineChart[i].getData().getDataSetByIndex(2);
                set[0].setValues(value.get(k));
                set[1].setValues(value.get(k+1));
                set[2].setValues(value.get(k+2));
                fragment2LineChart[i].getData().notifyDataChanged();
                fragment2LineChart[i].notifyDataSetChanged();
                fragment2LineChart[i].setVisibleXRangeMaximum(150);
                fragment2LineChart[i].moveViewToX(0);

            } else {
                // create a dataset and give it a type
                String[] ChartName = new String[]{"DataSet 1","DataSet 2","DataSet 3"};
                switch (k){
                    case 0:
                        ChartName[0] = "Rv";
                        ChartName[1] = "Sv";
                        ChartName[2] = "Tv";
                        break;
                    case 3:
                        ChartName[0] = "Uv";
                        ChartName[1] = "Vv";
                        ChartName[2] = "Wv";
                        break;
                    case 6:
                        ChartName[0] = "Ia";
                        ChartName[1] = "Ib";
                        ChartName[2] = "Ic";
                        break;
                }

                Integer[] colors = new Integer[]{Color.YELLOW,Color.GREEN,Color.RED};

                for (int j = 0; j < 3 ; j++) {

                    set[j] = new LineDataSet(value.get(j+k), ChartName[j]);
                    set[j].setDrawCircles(false);                                                      //线上的点
                    set[j].setDrawValues(true);                                                        //线上的值
                    set[j].setLineWidth(2f);                                                           //线条宽度
                    set[j].setColor(colors[j]);                                                    //线条颜色
                    set[j].setCircleColor(colors[j]);                                              //曲线颜色
                    set[j].setHighLightColor(colors[j]);                                           //点击某个点时的十字坐标线
                    set[j].setDrawFilled(false);                                                       //曲线填充
                    set[j].setAxisDependency(YAxis.AxisDependency.LEFT);
                    set[j].setValueTextSize(8f);                                                       //曲线标注文字大小
                    set[j].setMode(LineDataSet.Mode.CUBIC_BEZIER);

                }

//                // create a dataset and give it a type
//                set1 = new LineDataSet(value.get(k), ChartName[1]);
////            set1.setAxisDependency(AxisDependency.LEFT);
////            set1.setColor(ColorTemplate.getHoloBlue());
//                set1.setColor(Color.YELLOW);
//                set1.setCircleColor(Color.WHITE);
//                set1.setLineWidth(2f);
//                set1.setCircleRadius(3f);
//                set1.setFillAlpha(65);
////            set1.setFillColor(ColorTemplate.getHoloBlue());
//                set1.setHighLightColor(Color.rgb(244, 117, 117));
//                set1.setDrawCircleHole(false);
//                //set1.setFillFormatter(new MyFillFormatter(0f));
//                //set1.setDrawHorizontalHighlightIndicator(false);
//                //set1.setVisible(false);
//                //set1.setCircleHoleColor(Color.WHITE);
//
//                // create a dataset and give it a type
//                set2 = new LineDataSet(value.get(k+1), ChartName[1]);
////            set2.setAxisDependency(AxisDependency.RIGHT);
//                set2.setColor(Color.GREEN);
//                set2.setCircleColor(Color.WHITE);
//                set2.setLineWidth(2f);
//                set2.setCircleRadius(3f);
//                set2.setFillAlpha(65);
//                set2.setFillColor(Color.RED);
//                set2.setDrawCircleHole(false);
//                set2.setHighLightColor(Color.rgb(244, 117, 117));
//                //set2.setFillFormatter(new MyFillFormatter(900f));
//
//                set3 = new LineDataSet(value.get(k+2), ChartName[2]);
////            set3.setAxisDependency(AxisDependency.RIGHT);
//                set3.setColor(Color.RED);
//                set3.setCircleColor(Color.WHITE);
//                set3.setLineWidth(2f);
//                set3.setCircleRadius(3f);
//                set3.setFillAlpha(65);
////            set3.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
//                set3.setDrawCircleHole(false);
//                set3.setHighLightColor(Color.rgb(244, 117, 117));

                // create a data object with the data sets
//                LineData data = new LineData(set1, set2, set3);
                LineData data = new LineData(set[0],set[1],set[2]);
                data.setValueTextColor(Color.WHITE);
                data.setValueTextSize(9f);

                // set data
                fragment2LineChart[i].setData(data);

                fragment2LineChart[i].setVisibleXRangeMaximum(150);
                fragment2LineChart[i].moveViewToX(0);
            }
        }
    }

}