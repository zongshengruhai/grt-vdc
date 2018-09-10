package com.great.grt_vdc_t4200l.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.great.grt_vdc_t4200l.ListView.record;
import com.great.grt_vdc_t4200l.ListView.recordAdapter;
import com.great.grt_vdc_t4200l.MPLineChart.fragment2LineChartManager;
import com.great.grt_vdc_t4200l.R;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Color;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.w3c.dom.Text;

import android_serialport_api.SerialPort;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;


public class fragment2 extends Fragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "fragment2";

    private TextView[] fragment2TempRow = new TextView[2];

    //MPAndroidChart
    private fragment2LineChartManager[] fragment2ChartManager = new fragment2LineChartManager[3];
    private LineChart[] fragment2LineChart = new LineChart[3];
    private List<Integer> list = new ArrayList<>();             //数据集合
    private List<Integer> colour = new ArrayList<>();           //折线颜色
    private List<String> names = new ArrayList<>();             //折线名称

    //listView
    private Context fragment2_Context;
    private ListView fragment2_ListView;
    private List<record> fragment2_Data  = new LinkedList<>();
    private recordAdapter fragment2_RecordAdapter;

    //筛选
    private EditText Search_EditText;
    private ImageView Search_Delete;
    private TextView Search_Inquire;

    //进度条
    private NumberProgressBar fragment2_Loading;            //进度条实例
    private int jd;                                         //进度
    private boolean _isLoadFlag = true;                     //控制进度条显示位

    //加载文件内容
    private String pickFileName = null;                            //选择的文件名

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment2,container,false);

        //图表实例化
        fragment2LineChart[0] = view.findViewById(R.id.f2_LineChart_1);
        fragment2LineChart[1] = view.findViewById(R.id.f2_LineChart_2);
        fragment2LineChart[2] = view.findViewById(R.id.f2_LineChart_3);

        //告警信息TV初始化
        fragment2TempRow[0] = view.findViewById(R.id.fragment2TVtime);
        fragment2TempRow[1] = view.findViewById(R.id.fragment2TVcontent);
        fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),0));
        fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),"0.0.0_00:00",0,0,""));

        //List内容
        fragment2_Context = view.getContext();
        fragment2_ListView = view.findViewById(R.id.fragment2_ListView);

        //筛选实例化
        Search_EditText = view.findViewById(R.id.fragment2_search_EditText);
        Search_Delete = view.findViewById(R.id.fragment2_search_delete);
        Search_Inquire = view.findViewById(R.id.fragment2_search_inquire);

        //进度条实例化
        fragment2_Loading = view.findViewById(R.id.f2_Progress_bar);

        initSearch();
        SearchListData(null);
        initLineChart();

        return view;
    }


    /**
     * 重载生命周期
     */
    @Override
    public void onResume(){
        super.onResume();

        f2_UiHandler.post(f2_UiRunable);
    }

    /**
     * 中止生命周期
     */
    @Override
    public void onPause(){
        super.onPause();

        f2_UiHandler.removeCallbacks(f2_UiRunable);
    }

    /**
     * 初始化搜索框
     */
    private void initSearch(){

        //添加输入清零事件
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
                SearchListData(Search_EditText.getText().toString());
            }

            //文本改变后
            @Override
            public void afterTextChanged(Editable s) { }

        });

        //搜索点击事件（准备废弃）
        Search_Inquire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(Search_EditText.getText().toString().trim())){
                    Log.e("fragment2","请输入指定日期或编号的事件记录以搜索");
                }else {
                    Log.e("fragment2","已点击");
                }
            }
        });

    }

    /**
     * 初始化图表
     */
    private void initLineChart(){

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

        colour.add(Color.YELLOW);
        colour.add(Color.GREEN);
        colour.add(Color.RED);

        names.add("Rv");
        names.add("Sv");
        names.add("Tv");
        fragment2ChartManager[0] = new fragment2LineChartManager(fragment2LineChart[0],names,colour);
        fragment2ChartManager[0].setYAxis(500,-500,10);
        fragment2ChartManager[0].setDescription("输入电压");
        fragment2LineChart[0].setOnChartGestureListener(chartListener);
        names.clear();

        names.add("Uv");
        names.add("Vv");
        names.add("Wv");
        fragment2ChartManager[1] = new fragment2LineChartManager(fragment2LineChart[1],names,colour);
        fragment2ChartManager[1].setYAxis(500,-500,10);
        fragment2ChartManager[1].setDescription("输出电压");
        fragment2LineChart[1].setOnChartGestureListener(chartListener);
        names.clear();

        names.add("Ua");
        names.add("Va");
        names.add("Wa");
        fragment2ChartManager[2] = new fragment2LineChartManager(fragment2LineChart[2],names,colour);
        fragment2ChartManager[2].setYAxis(500,-500,10);
        fragment2ChartManager[2].setDescription("输出电流");
        fragment2LineChart[2].setOnChartGestureListener(chartListener);

    }

    /**
     *  将所有图表设置一样的回调（暂时废弃）
     */
    private OnChartGestureListener chartListener = new OnChartGestureListener() {

        //手势开始
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
//            Log.e(TAG,"手势开始");
        }

        //手势结束
        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
//            Log.e(TAG,"手势结束");
        }

        //长按
        @Override
        public void onChartLongPressed(MotionEvent me) {
//            Log.e(TAG,"长按");
        }

        //双击
        @Override
        public void onChartDoubleTapped(MotionEvent me) {
//            Log.e(TAG,"双击图表");
        }

//        单击
        @Override
        public void onChartSingleTapped(MotionEvent me) {
//            Log.e(TAG,"单击图表");
        }

        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
            Log.e(TAG,"now is onChartFling");
        }

        //缩放
        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
//            Log.e(TAG,"缩放图表, X轴："+scaleX+", Y轴："+scaleY);
        }

        //拖动
        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {
//            Log.e(TAG,"拖动图表, X轴："+dX+", Y轴："+dY+",me:"+me);
//            fragment2LineChart[0].setTranslationX(dX);
//            fragment2LineChart[0].setTranslationY(dY);
        }
    };

    /**
     *  SearchListData
     *  根据EditText输入数据更新ListView列表数据
     */
    private void SearchListData(String SearchFileName){

        //录波记录存放路径
        String PATH = fragment2_Context.getFilesDir().getPath() + "/record_log/";

        //存放填充数据的集合
        String[] temp = new String[3];

        String str = "";

        //历遍路径中的所有文件夹
        File file = new File(PATH);
        if (file.exists()){

            File[] files = file.listFiles();

            //清空ListView
            fragment2_Data.clear();
            fragment2_RecordAdapter = new recordAdapter((LinkedList<record>) fragment2_Data,fragment2_Context);
            fragment2_ListView.setAdapter(fragment2_RecordAdapter);

            //路径中没有文件
            if (files.length <= 0){

                fragment2_Data.add(new record("","当前暂无录波记录","",""));
                fragment2_RecordAdapter = new recordAdapter((LinkedList<record>) fragment2_Data,fragment2_Context);
                fragment2_ListView.setAdapter(fragment2_RecordAdapter);
                fragment2_ListView.setOnItemClickListener(this);

            }else {

                int SearchNull = 0;

                //历遍所有文件名
                for (int i = 0; i < files.length; i++) {

                    str = files[i].getAbsolutePath().replace(PATH, "");

                    String[] regroupFiles;

                    //根据"_"斩开数据
                    regroupFiles = (str.replace(".xls", "")).split("_");

                    //筛选条目
                    if (SearchFileName == null) {

                        temp[0] = regroupFiles[0];
                        temp[1] = regroupFiles[4] + " " + regroupFiles[5];

                    } else {

                        //正则筛选
                        Pattern SearchPattern = Pattern.compile(SearchFileName);
                        Matcher SearchMatcher = SearchPattern.matcher(str);

                        if (SearchMatcher.find()) {
                            temp[0] = regroupFiles[0];
                            temp[1] = regroupFiles[4] + " " + regroupFiles[5];
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
                    fragment2_Data.add(new record(temp[0], temp[1], "", str));
                    fragment2_RecordAdapter = new recordAdapter((LinkedList<record>) fragment2_Data, fragment2_Context);
                    fragment2_ListView.setAdapter(fragment2_RecordAdapter);
                    fragment2_ListView.setOnItemClickListener(this);

                }
            }
        }
    }

    /**
     * 点击List事件
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView pickTextUrl = view.findViewById(R.id.txt_mUrl);
        String pickFileName = pickTextUrl.getText().toString();

        if (pickFileName.contains(".xls")){
            this.pickFileName = pickFileName;
            _isLoadFlag = false;
//            new LongThread().start();
            String[] fillContent;
            fillContent = (pickFileName.replace(".xls","")).split("_");
            Log.e(TAG, "run: "+fillContent.length );
        }else {
            this.pickFileName = null;
        }
    }

    /**
     * 更新图表
     * @param fileName 文件的绝对路径
     */
    private void fillLineChart(String fileName){

        String PATH = fragment2_Context.getFilesDir().getPath() + "/record_log/";

        int rows;                                                           //行数量
        int columns;                                                        //列数量

        if (fileName == null){
            Log.e(TAG,"没有文件名");
        }else {
            //填充表头
            String[] fillContent;
            fillContent = (fileName.replace(".xls","")).split("_");
            if (fillContent.length > 0){
                fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),Integer.parseInt(fillContent[0])));
                fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),(fillContent[4] + "_" + fillContent[5]),(Integer.parseInt(fillContent[2])),(Integer.parseInt(fillContent[3])),""));
            }

            fileName = PATH + fileName;
            Log.e(TAG,"准备加载："+fileName);

            try {

                FileInputStream mfis = new FileInputStream(fileName);
                Workbook mbook = Workbook.getWorkbook(mfis);
                int msheer = mbook.getNumberOfSheets();                     //表数量
                Sheet[] mSheetlist = mbook.getSheets();                     //表内容

                String temp;

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
                            fragment2ChartManager[k].addEntry(list);
                            list.clear();
                        }

//                        for (int k = 0; k < 3 ; k++) {
//                            Cell cell = mSheetlist[i].getCell(k,j);
//                            temp =(cell.getContents()).trim();
//                            list.add(Integer.parseInt(temp));
//                        }
////                        Cell[] cellList = mSheetlist[i].getRow(j);
////                        for (Cell cell : cellList) {
////                            temp = (cell.getContents()).trim();
////                            Log.e(TAG,Integer.parseInt(temp)+"");
////                            list.add(Integer.parseInt(temp));
////                        }
//                        fragment2ChartManager[0].addEntry(list);
//                        list.clear();
                    }
                }
                mbook.close();

            }catch (Exception e){
                System.out.println("fragment2,Exception: " + e);
            }
        }
    }

    //主线程
    Handler f2_UiHandler = new Handler();
    Runnable f2_UiRunable = new Runnable() {
        @Override
        public void run() {
            f2_UiHandler.postDelayed(this,500);

            SearchListData(Search_EditText.getText().toString());

            if (!_isLoadFlag){
                //隐藏图表，显示进度条
                fragment2LineChart[0].setVisibility(View.GONE);
                fragment2LineChart[1].setVisibility(View.GONE);
                fragment2LineChart[2].setVisibility(View.GONE);
                fragment2_Loading.setVisibility(View.VISIBLE);
                fragment2_Loading.setProgress(jd);
            }else {
                if (pickFileName != null){
                    //更新录波的详细数据
                    String[] fillContent;
                    fillContent = (pickFileName.replace(".xls","")).split("_");
                    Log.e(TAG, "run: "+fillContent.length );
//                    fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),Integer.parseInt(fillContent[0])));
//                    fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),(fillContent[4] + "_" + fillContent[5]),(Integer.parseInt(fillContent[2])),(Integer.parseInt(fillContent[3])),""));
                    //显示图表，隐藏进度条
                    fragment2LineChart[0].setVisibility(View.VISIBLE);
                    fragment2LineChart[1].setVisibility(View.VISIBLE);
                    fragment2LineChart[2].setVisibility(View.VISIBLE);
                    fragment2_Loading.setVisibility(View.GONE);
                    //清零选择
                    pickFileName = null;
                }
            }
        }
    };

    /**
     * 子线程，处理耗时的加载图表
     */
    class LongThread extends Thread{
        public void run(){
//            for (int i = 0; i < 16000; i++) {
//                jd = (((100000/16000)*i)/1000);
//                Log.e(TAG, "run: " + jd);
//                try {
//                    Thread.sleep(1);
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
            String PATH = fragment2_Context.getFilesDir().getPath() + "/record_log/";

            int rows;                                                           //行数量
            int columns;                                                        //列数量

            if (pickFileName == null){
                Log.e(TAG,"没有文件名");
            }else {
                //填充表头
                String[] fillContent;
                fillContent = (pickFileName.replace(".xls","")).split("_");
                if (fillContent.length > 0){
//                    fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),Integer.parseInt(fillContent[0])));
//                    fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),(fillContent[4] + "_" + fillContent[5]),(Integer.parseInt(fillContent[2])),(Integer.parseInt(fillContent[3])),""));
                }

                pickFileName = PATH + pickFileName;
                Log.e(TAG,"准备加载："+pickFileName);

                try {

                    FileInputStream mfis = new FileInputStream(pickFileName);
                    Workbook mbook = Workbook.getWorkbook(mfis);
                    int msheer = mbook.getNumberOfSheets();                     //表数量
                    Sheet[] mSheetlist = mbook.getSheets();                     //表内容

                    String temp;

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
                                fragment2ChartManager[k].addEntry(list);
                                list.clear();
                            }
                            jd = (((100000/rows)*j)/1000);
                        }
                    }
                    mbook.close();

                }catch (Exception e){
                    System.out.println("fragment2,Exception: " + e);
                }
            }
            _isLoadFlag = true;
        }
    }

}

