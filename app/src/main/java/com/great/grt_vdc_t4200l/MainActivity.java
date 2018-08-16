package com.great.grt_vdc_t4200l;

/*
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.great.grt_vdc_t4200l.TabLayoutHalp.MyFragmentAdapter;
import com.great.grt_vdc_t4200l.TabLayoutHalp.InputVoltage;
import com.great.grt_vdc_t4200l.TabLayoutHalp.OutputVoltage;
import com.great.grt_vdc_t4200l.TabLayoutHalp.OutputCurrent;
import com.great.grt_vdc_t4200l.TabLayoutHalp.OtherData;
*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


//public class MainActivity extends BaseCourse implements OnGestureListener{
public class MainActivity extends BaseCourse{

    public static Fragment[] fragments;
    public static LinearLayout[] linearLayouts;
    public static TextView[] textViews;

    public static GestureDetector detector;

    public int MARK=0;

    //滑动距离
    final int DISTANT=50;

    private static final String TAG = "main";

    Intent fragment4Intent = new Intent("drc.xxx.yyy.fragment4");
    Intent dataChange = new Intent("drc.xxx.yyy.MainActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //分别实例化和初始化fragement、lineatlayout、textview
        setfragment();
        setlinearLayouts();
        settextview();

        readLayType();

        //创建手势检测器
        //detector=new GestureDetector(this);
    }

    @Override
    public void onResume(){
        super.onResume();

        /*
        mbroad oBaseActivity_Broad;
        oBaseActivity_Broad = new mbroad();
        IntentFilter intentFilter = new IntentFilter("drc.xxx.yyy.MainActivity");
        registerReceiver(oBaseActivity_Broad,intentFilter);*/

        handler.post(task);
    }

    /*
    public class mbroad extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent){
            //接收到广播
            Log.e(TAG,"接收到广播");

            int dataChange = intent.getExtras().getInt("dataChange");
            Log.e(TAG,""+dataChange);

        }
    }*/

    Handler handler = new Handler();
    Runnable task = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this,1000);
            int text = (int) (Math.random() * 100);
            //Log.e(TAG,""+text);
            dataChange.putExtra("dataChange",text);
            sendBroadcast(dataChange);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**初始化fragment*/
    public void setfragment()
    {
        fragments=new Fragment[5];
        fragments[0]=getSupportFragmentManager().findFragmentById(R.id.fragment5);
        fragments[1]=getSupportFragmentManager().findFragmentById(R.id.fragment1);
        fragments[2]=getSupportFragmentManager().findFragmentById(R.id.fragment2);
        fragments[3]=getSupportFragmentManager().findFragmentById(R.id.fragment3);
        fragments[4]=getSupportFragmentManager().findFragmentById(R.id.fragment4);
        getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[0]).commit();

    }

    /**初始化linerlayout*/
    public void setlinearLayouts()
    {
        linearLayouts=new LinearLayout[5];
        linearLayouts[0]=(LinearLayout)findViewById(R.id.lay5);
        linearLayouts[1]=(LinearLayout)findViewById(R.id.lay1);
        linearLayouts[2]=(LinearLayout)findViewById(R.id.lay2);
        linearLayouts[3]=(LinearLayout)findViewById(R.id.lay3);
        linearLayouts[4]=(LinearLayout)findViewById(R.id.lay4);
        //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
    }
    /**初始化textview*/
    public void settextview()
    {
        textViews=new TextView[5];
        textViews[0]=(TextView)findViewById(R.id.fratext5);
        textViews[1]=(TextView)findViewById(R.id.fratext1);
        textViews[2]=(TextView)findViewById(R.id.fratext2);
        textViews[3]=(TextView)findViewById(R.id.fratext3);
        textViews[4]=(TextView)findViewById(R.id.fratext4);
        resetlaybg();
        textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
    }

    /**点击底部linerlayout实现切换fragment的效果*/
    public void LayoutOnclick(View v)
    {
        resetlaybg();//每次点击都重置linearLayouts的背景、textViews字体颜色
        switch (v.getId()) {
            case R.id.lay5:
                //Log.e(TAG,"lay1 selected");
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[0]).commit();
                //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=0;
                break;
            case R.id.lay1:
                //Log.e(TAG,"lay2 selected");
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[1]).commit();
                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=1;
                break;
            case R.id.lay2:
                //Log.e(TAG,"lay3 selected");
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[2]).commit();
                //linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=2;
                break;
            case R.id.lay3:
                //Log.e(TAG,"lay3 selected");
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[3]).commit();
                //linearLayouts[3].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[3].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=3;
                break;
            case R.id.lay4:
                //Log.e(TAG,"lay3 selected");
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[4]).commit();
                //linearLayouts[3].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[4].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=4;
                break;
            default:
                break;
        }
        //写数据
        readLayType();
        /*
        fragment4Intent.putExtra("layChane",MARK);
        sendBroadcast(fragment4Intent);*/
    }

    private void readLayType(){
        SharedPreferences.Editor Maineditor = getSharedPreferences("temp",MODE_WORLD_READABLE).edit();
        Maineditor.putInt("layType",MARK);
        Maineditor.commit();
    }


    /**重置linearLayouts、textViews*/
    public void resetlaybg()
    {
        for(int i=0;i<5;i++)
        {
            //linearLayouts[i].setBackgroundResource(R.drawable.tabfootbg);
            textViews[i].setTextColor(getResources().getColor(R.color.black));
        }

    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        //将该Activity上触碰事件交给GestureDetector处理
        return detector.onTouchEvent(event);
    }
    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    //滑动切换效果的实现//
    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        resetlaybg();
        //当是Fragment0的时候
        if(MARK==0) {
            //Log.e(TAG,"lay1 thocd");
            if(arg1.getX()<arg0.getX()+DISTANT)
            {
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[1]).commit();
                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=1;
            } else {
                //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
                //当超出边界时，不能判断为移动同样选择在原位
                //textViews[0].setTextColor(getResources().getColor(R.color.black));
                textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=0;
            }
        }
        //当是Fragment1的时候
        else if (MARK==1) {
            //Log.e(TAG,"lay2 thocd");
            if(arg1.getX()<arg0.getX()+DISTANT)
            {
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[2]).commit();
                //linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=2;
            } else if(arg0.getX()<arg1.getX()+DISTANT) {
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[0]).commit();
                //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=0;
            }
            else
            {
                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
                //textViews[1].setTextColor(getResources().getColor(R.color.black));
                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=1;
            }
        }
        //当是Fragment2的时候
        else if(MARK==2) {
            //Log.e(TAG,"lay3 thocd");
            if(arg1.getX()<arg0.getX()+DISTANT)
            {
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[3]).commit();
                //linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[3].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=3;
            } else if(arg0.getX()<arg1.getX()+DISTANT) {
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[1]).commit();
                //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=1;
            }
            else
            {
                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
                //textViews[1].setTextColor(getResources().getColor(R.color.black));
                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=2;
            }
        }
        //当是Fragment3的时候
        else if(MARK==3) {
            //Log.e(TAG,"lay3 thocd");
            if(arg0.getX()<arg1.getX()+DISTANT)
            {
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[2]).commit();
                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=2;
            } else {
                //linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
                //textViews[2].setTextColor(getResources().getColor(R.color.black));
                textViews[3].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=3;
            }
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }*/

    /*
    private static final String TAG = "MainActivity";


    //TabLayout
    private static final String[] sTitle = new String[]{"输出电压","输出电流","输入电压","其他数据"};
    private TabLayout tl;
    private ViewPager vp;

    //Toolbar
    private Toolbar mToolbar;

    private int newPager;
    String tabSelectType = "";
    private TextView Adata;
    private TextView Bdata;
    private TextView Cdata;

    //跳转
    private Button oldDataButton;

    //MPAndroidChart
    LineChart mNewDatalineChart;
    private List<Integer> list = new ArrayList<>();         //数据集合
    private List<Integer> colour = new ArrayList<>();       //折线颜色
    private List<String> names = new ArrayList<>();          //折线名称

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relate();

        initTabLayout();
        initToolbar();
        initLineChart();

        //handler.post(task);
    }

    //关联activity控件
    private void relate(){

        tl = (TabLayout) findViewById(R.id.tl);
        //vp = (ViewPager) findViewById(R.id.vp);

        Adata = (TextView) findViewById(R.id.dataA);
        Bdata = (TextView) findViewById(R.id.dataB);
        Cdata = (TextView) findViewById(R.id.dataC);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mNewDatalineChart = (LineChart) findViewById(R.id.newDataChart);

        oldDataButton = (Button) findViewById(R.id.oldDataButton);
        oldDataButton.setOnClickListener(listener);
    }

    //响应按钮事件
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button view = (Button) v;
            switch (view.getId()){
                case R.id.oldDataButton:
                    Intent intent = new Intent(MainActivity.this,ActivityOldData.class);
                    //overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    startActivity(intent);
                    //handler.removeCallbacks(task);
                    //finish();
                    break;
            }
        }
    };

    //初始化TabLayout
    private void initTabLayout(){

        //更新tl
        for (int i = 0; i < 4 ; i++) {
            tl.addTab(tl.newTab().setText(sTitle[i]));
        }

        //监听tl事件
        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            //挑选事件
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                //Log.i(TAG,"onTabSelected"+tab.getText());
                tabSelectType = (String) tab.getText();
            }
            //离开事件
            @Override
            public void onTabUnselected(TabLayout.Tab tab){
                Log.i(TAG,"onTabUnselected"+tab.getText());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab){
                Log.i(TAG,"onTabReselected"+tab.getText());
            }
        });

        /*
        //TabLayout关联ViewPager
        tl.setupWithViewPager(vp);
        //添加内容
        List<Fragment> mFragment = new ArrayList<>();
        //mFragment.add(InputVoltage.newInstance());
        mFragment.add(OutputVoltage.newInstance());
        mFragment.add(OutputCurrent.newInstance());
        mFragment.add(InputVoltage.newInstance());
        mFragment.add(OtherData.newInstance());


        //添加适配器
        MyFragmentAdapter adapter = new MyFragmentAdapter(getSupportFragmentManager(),mFragment, Arrays.asList(sTitle));
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //挑选事件
            @Override
            public void onPageSelected(int position) {
                //Log.i(TAG,"select page:"+position);
                newPager = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        handler.post(task);

        //
    }

    //初始化initToolbar
    private void initToolbar(){

        //setSupportActionBar(mToolbar);
        //mToolbar.setOnMenuItemClickListener(onMenuItemClick);

    }

    /*
    //导入Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Toolbar点击事件
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            String msg = "";
            switch(item.getItemId()){
                case R.id.action_settings:
                    msg += "Click settinge";
                    break;
            }
            if (!msg.equals("")){
                Log.e(TAG,msg+"");
            }
            return true;
        }
    };//

    //初始化LineChart
    private void initLineChart(){

        names.add("U相");
        names.add("V相");
        names.add("W相");

        colour.add(Color.CYAN);
        colour.add(Color.GREEN);
        colour.add(Color.BLUE);

        //边界
        mNewDatalineChart.setDrawBorders(true);
        //触摸
        mNewDatalineChart.setTouchEnabled(false);
        //缩放
        mNewDatalineChart.setScaleEnabled(false);
        //拖拽
        mNewDatalineChart.setDragEnabled(false);

        LineData mNewLineData = new LineData();
        mNewLineData.setValueTextColor(ColorTemplate.getHoloBlue());
        mNewDatalineChart.setData(mNewLineData);
        mNewDatalineChart.setVisibleXRangeMaximum(10);
        mNewDatalineChart.moveViewToX(mNewLineData.getXValCount()-10);
        mNewDatalineChart.animateX(2500);

        //x轴
        XAxis xAxis= mNewDatalineChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        // 如果设置为true，则在绘制时会避免“剪掉”在x轴上的图表或屏幕边缘的第一个和最后一个坐标轴标签项。
        xAxis.setAvoidFirstLastClipping(true);
        // 几个x坐标轴之间才绘制？
//        xAxis.setSpaceBetweenLabels(5);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setEnabled(true);

        // 图表左边的y坐标轴线
        YAxis leftAxis= mNewDatalineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);

        //最大值
        leftAxis.setAxisMinValue(100f);

        //最小值
        leftAxis.setAxisMinValue(0f);

        // 不一定要从0开始
        leftAxis.setStartAtZero(false);

        //绘制网格线
        leftAxis.setDrawGridLines(true);
        //右侧y轴
        YAxis rightAxis=mNewDatalineChart.getAxisRight();
        //不显示右侧y轴
        rightAxis.setEnabled(false);

    }

    //定时事件
    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        @Override
        public void run() {

            //对Activity的弱引用，防止内存泄漏



            handler.postDelayed(this,500);
            newPager++;
            if (newPager > 400){
                newPager = 0;
            }
            switch (tabSelectType){
                case "输出电压":
                    //Log.e(TAG,"1");
                    Adata.setText("U相："+ newPager +"V");
                    Bdata.setText("V相："+ newPager +"V");
                    Cdata.setText("W相："+ newPager +"V");
                    break;
                case "输出电流":
                    //Log.e(TAG,"2");
                    Adata.setText("U相："+ newPager +"A");
                    Bdata.setText("V相："+ newPager +"A");
                    Cdata.setText("W相："+ newPager +"A");
                    break;
                case "输入电压":
                    //Log.e(TAG,"3");
                    Adata.setText("R相："+ newPager +"V");
                    Bdata.setText("S相："+ newPager +"V");
                    Cdata.setText("T相："+ newPager +"V");
                    break;
                case "其他数据":
                    //Log.e(TAG,"4");
                    Adata.setText("电容电压："+newPager+"V");
                    Bdata.setText("");
                    Cdata.setText("频率："+newPager+"HZ");
                    break;
                case "":
                    //Log.e(TAG,"5");
                    Adata.setText("U相："+ newPager +"V");
                    Bdata.setText("V相："+ newPager +"V");
                    Cdata.setText("W相："+ newPager +"V");
                    break;
            }
        }
    };

    //开始
    @Override
    protected void onStart(){
        super.onStart();
        Log.e(TAG,"主界面开始");
    }

    //重启
    @Override
    protected void onRestart(){
        super.onRestart();
        Log.e(TAG,"主界面重启");
    }

    //重载
    @Override
    protected void onResume(){
        super.onResume();
        Log.e(TAG,"主界面重载");
    }
    //中止
    @Override
    protected void onPause(){
        super.onPause();
        Log.e(TAG,"主界面中止");
    }

    //停止
    @Override
    protected void onStop(){
        super.onStop();
        Log.e(TAG,"主界面停止");
    }

    //退出
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.e(TAG,"主界面退出");
    }

*/
}
