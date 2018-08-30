package com.great.grt_vdc_t4200l.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;


public class fragment2 extends Fragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "fragment2";

    private TextView[] fragment2TempRow = new TextView[2];

    String PATH;

    //MPAndroidChart
//    private fragment2LineChartManager fragment2ChartManager;
//    private LineChart fragment2LineChar;
    private fragment2LineChartManager[] fragment2ChartManager = new fragment2LineChartManager[3];
    private LineChart[] fragment2LineChart = new LineChart[3];
    private List<Integer> list = new ArrayList<>();         //数据集合
    private List<Integer> colour = new ArrayList<>();       //折线颜色
    private List<String> names = new ArrayList<>();          //折线名称

    //listView
    private Context fragment2_Context;
    private ListView fragment2_ListView;
    private List<record> fragment2_Data  = new LinkedList<>();
    private recordAdapter fragment2_RecordAdapter;

    private EditText Search_EditText;
    private ImageView Search_Delete;
    private TextView Search_Inquire;

//    private ProgressBar fragment2_Loading;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment2,container,false);

//        fragment2LineChar = view.findViewById(R.id.f2_LineChart_1);
        fragment2LineChart[0] = view.findViewById(R.id.f2_LineChart_1);
        fragment2LineChart[1] = view.findViewById(R.id.f2_LineChart_2);
        fragment2LineChart[2] = view.findViewById(R.id.f2_LineChart_3);

        fragment2TempRow[0] = view.findViewById(R.id.fragment2TVtime);
        fragment2TempRow[1] = view.findViewById(R.id.fragment2TVcontent);
        fragment2TempRow[0].setText(String.format(getResources().getString(R.string.fragment2RecordTime),0));
        fragment2TempRow[1].setText(String.format(getResources().getString(R.string.fragment2RecordContent),"0.0.0_00:00",0,0,""));

        //fragment2Liner = view.findViewById(R.id.fragment2Llayout);

        fragment2_Context = view.getContext();
        fragment2_ListView = view.findViewById(R.id.fragment2_ListView);

        Search_EditText = view.findViewById(R.id.fragment2_search_EditText);
        Search_Delete = view.findViewById(R.id.fragment2_search_delete);
        Search_Inquire = view.findViewById(R.id.fragment2_search_inquire);

//        fragment2_Loading = view.findViewById(R.id.fragment2_loading);

        //默认路径
        PATH = fragment2_Context.getFilesDir().getPath() + "/fault_record_file/";
        PATH = PATH.replace("/files","");

        initSearch();
//        initListView();
        SearchListData(null);
        initLineChart();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    //重载
    @Override
    public void onResume(){
        super.onResume();
    }

    //中止
    @Override
    public void onPause(){
        super.onPause();
    }

    //停止
    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //初始化搜索部分
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
                    Search_Delete.setVisibility(View.GONE);
                }else {
                    Search_Delete.setVisibility(View.VISIBLE);
                    //changeListView();
                }
                SearchListData(Search_EditText.getText().toString());
            }

            //文本改变后
            @Override
            public void afterTextChanged(Editable s) { }

        });

        //搜索点击事件
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

    //初始化LineChart
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
     *  将所有图表设置一样的回调
     */
    private OnChartGestureListener chartListener = new OnChartGestureListener() {

        //手势开始
        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            Log.e(TAG,"手势开始");
        }

        //手势结束
        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            Log.e(TAG,"手势结束");
        }

        //长按
        @Override
        public void onChartLongPressed(MotionEvent me) {
            Log.e(TAG,"长按");
        }

        //双击
        @Override
        public void onChartDoubleTapped(MotionEvent me) {
            Log.e(TAG,"双击图表");
        }

        //单击
        @Override
        public void onChartSingleTapped(MotionEvent me) {
            Log.e(TAG,"单击图表");
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
            Log.e(TAG,"拖动图表, X轴："+dX+", Y轴："+dY+",me:"+me);
//            Log.e(TAG, "x[0]="+me.getX()+"y[0]="+me.getY());
//            fragment2LineChart[0].setTranslationX(dX);
//            fragment2LineChart[0].moveViewToX(me.getX());
//            fragment2LineChart[0].moveViewTo(me.getX(),me.getY(),YAxis.AxisDependency.LEFT);
        }
    };

    /**
     *  SearchListData
     *  根据EditText输入数据更新ListView列表数据
     */
    private void SearchListData(String SearchFileName){

        //存放录波记录的绝对路径
//        String PATH = "//data/data/com.great.grt_vdc_t4200l/fault_record_file/";
//        String PATH = fragment2_Context.getFilesDir().getPath() + "/fault_record_file/";
//        PATH = PATH.replace("/files","");

        //存放填充数据的集合
        String[] temp = new String[3];

        //历遍路径中的所有文件夹
        File file = new File(PATH);
        File[] files = file.listFiles();

        //清空ListView
//        fragment2_Data.removeAll(fragment2_Data);
        fragment2_Data.clear();
        fragment2_RecordAdapter = new recordAdapter((LinkedList<record>) fragment2_Data,fragment2_Context);
        fragment2_ListView.setAdapter(fragment2_RecordAdapter);

        //路径中没有文件
        if (files.length <= 0){

//            Log.e(TAG,"当前文件夹内没有记录文件");
            fragment2_Data.add(new record("","当前暂无录波记录","",""));
            fragment2_RecordAdapter = new recordAdapter((LinkedList<record>) fragment2_Data,fragment2_Context);
            fragment2_ListView.setAdapter(fragment2_RecordAdapter);
            fragment2_ListView.setOnItemClickListener(this);

            //路径中有文件
        }else{

//            Log.e(TAG,"文件夹内有"+files.length+"条记录文件");
            int SearchNull = 0;

            //历遍所有文件名
            for (int i = 0; i < files.length; i++) {

                String str = files[i].getAbsolutePath().replace(PATH,"");
//                Log.e(TAG,files[i].getAbsolutePath());
                String[] regroupFiles;

                //根据"_"斩开数据
                regroupFiles = (str.replace(".xls","")).split("_");

                //没有筛选条件
                if (SearchFileName == null){

//                    temp[0] = Integer.toString(i);
//                    temp[1] = files[i].getAbsolutePath();
                    temp[0] = regroupFiles[0];
                    temp[1] = regroupFiles[4]+" "+regroupFiles[5];

                    //有筛选条件
                }else {

                    //正则筛选
                    Pattern SearchPattern = Pattern.compile(SearchFileName);
                    Matcher SearchMatcher = SearchPattern.matcher(str);

                    //符合条件
                    if (SearchMatcher.find()){

//                        temp[0] = Integer.toString(i);
//                        temp[1] = files[i].getAbsolutePath();
                        temp[0] = regroupFiles[0];
                        temp[1] = regroupFiles[4]+" "+regroupFiles[5];

                        //不符合条件
                    }else {

                        SearchNull++;
                        if (SearchNull < files.length){continue;}
                        if (SearchNull == files.length){
                            temp[0] = "";
                            temp[1] = "没有匹配的录波记录，请重新筛选";
                        }
                    }
                }

                //更新ListView
                fragment2_Data.add(new record(temp[0],temp[1],"",str));
                fragment2_RecordAdapter = new recordAdapter((LinkedList<record>) fragment2_Data,fragment2_Context);
                fragment2_ListView.setAdapter(fragment2_RecordAdapter);
                fragment2_ListView.setOnItemClickListener(this);

            }
        }

    }

    //ListView点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        TextView pickTextUrl = view.findViewById(R.id.txt_mUrl);
        String pickFileName = pickTextUrl.getText().toString();

//        fragment2_Loading.setVisibility(View.VISIBLE);

        if (pickFileName.contains(".xls")){
//            fragment2ChartManager[0].test();
//            fragment2ChartManager[0].clear();
//            fragment2ChartManager[1].clear();
//            fragment2ChartManager[2].clear();
//            initLineChart();
            fillLineChart(pickFileName);
        }
    }

    //更新LinerChar
    private void fillLineChart(String fileName){

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
//                            Log.e(TAG,list.toString()+"");
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

}

