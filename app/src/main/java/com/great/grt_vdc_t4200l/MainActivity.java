package com.great.grt_vdc_t4200l;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 */
//public class MainActivity extends BaseCourse implements OnGestureListener{
public class MainActivity extends BaseCourse{

    //容器----------------------------------------------------
    private Context mContext;
    public static Fragment[] fragments;
    public static LinearLayout[] linearLayouts;
    public static TextView[] textViews;
    //滑动手势----------------------------------------------------
//    public static GestureDetector detector;
//    final int DISTANT=50;                             /滑动距离
    //当前页面----------------------------------------------------
    public int MARK = 0;
    //广播----------------------------------------------------
    private Intent fragment4Intent = new Intent("drc.xxx.yyy.fragment4");
//    private Intent dataChange = new Intent("drc.xxx.yyy.MainActivity");
    //小红点----------------------------------------------------
    private static TextView[] redDor;
    private static int iOldAlarm = 0;
    private static int iOldRecord = 0;

    /**
     * 生命周期
     */
    //创建----------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //等待BaseCourse加载完成
        while (_isSystem){
            Toast.makeText(this,"正在初始化",Toast.LENGTH_SHORT).show();
        }

        mContext = getBaseContext();
//        mActivity = mActivity.getA();

        SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData", 0);
        iOldRecord = rAlarmData.getInt("i_RecordTime",0);
        iOldAlarm = rAlarmData.getInt("i_AlarmTime",0);


        //分别实例化和初始化fragement、lineatlayout、textview
        setFragment();
        setLinearLayouts();
        setTextView();

        //写入当前页
        readLayType();

        //创建手势检测器
        //detector=new GestureDetector(this);
    }
    //重载----------------------------------------------------
    @Override
    public void onResume(){
        super.onResume();
        MainHandler.post(MainRunnable);
    }
    //中止----------------------------------------------------
    @Override
    protected void onPause(){
        super.onPause();
        MainHandler.removeCallbacks(MainRunnable);
    }

//    /**
//     * 获取当前屏幕亮度
//     * @return 0~255 表示当前屏幕亮度值
//     */
//    private int getSystemBrightness(){
//        int systemBrightness = 0;
//        try {
//            systemBrightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
//        }catch (Settings.SettingNotFoundException e){
//            e.printStackTrace();
//        }
//        return systemBrightness;
//    }

    /**
     * 获取当前屏幕亮度
     * @return 0~1 当前屏幕亮度值
     */
    private float getActivityBrightness(){
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        return lp.screenBrightness;
    }

    /**
     * 设置当前屏幕亮度
     * @param brightness 0~255 当前屏幕亮度值
     */
    private void setActivityBrightness(int brightness){
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness == -1){
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness ) / 255f;
        }
        window.setAttributes(lp);
    }

    /**
     * 初始化
     **/
    //初始化fragment----------------------------------------------------
    public void setFragment() {
        fragments=new Fragment[5];
        fragments[0]=getSupportFragmentManager().findFragmentById(R.id.fragment5);
        fragments[1]=getSupportFragmentManager().findFragmentById(R.id.fragment1);
        fragments[2]=getSupportFragmentManager().findFragmentById(R.id.fragment2);
        fragments[3]=getSupportFragmentManager().findFragmentById(R.id.fragment3);
        fragments[4]=getSupportFragmentManager().findFragmentById(R.id.fragment4);
        getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[0]).commit();
    }
    //初始化底部导航栏----------------------------------------------------
    public void setLinearLayouts() {
        linearLayouts=new LinearLayout[5];
        linearLayouts[0] = findViewById(R.id.lay5);
        linearLayouts[1] = findViewById(R.id.lay1);
        linearLayouts[2] = findViewById(R.id.lay2);
        linearLayouts[3] = findViewById(R.id.lay3);
        linearLayouts[4] = findViewById(R.id.lay4);
        redDor = new TextView[2];
        redDor[0] = findViewById(R.id.lay2_dot);
        redDor[1] = findViewById(R.id.lay3_dot);
    }
    //初始化Text----------------------------------------------------
    public void setTextView() {
        textViews=new TextView[5];
        textViews[0] = findViewById(R.id.fratext5);
        textViews[1] = findViewById(R.id.fratext1);
        textViews[2] = findViewById(R.id.fratext2);
        textViews[3] = findViewById(R.id.fratext3);
        textViews[4] = findViewById(R.id.fratext4);
        resetLaybg();
        textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
        linearLayouts[0].setBackgroundColor(getResources().getColor(R.color.lay_select_bg));
    }

    /**
     * 切换fragment
     */
    //底部导航栏点击事件切换fragment----------------------------------------------------
    public void LayoutOnclick(View v) {

        resetLaybg();//每次点击都重置linearLayouts的背景、textViews字体颜色

        switch (v.getId()) {
            case R.id.lay5:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[0]).commit();
//                linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
                linearLayouts[0].setBackgroundColor(getResources().getColor(R.color.lay_select_bg));
                textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=0;
                break;
            case R.id.lay1:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[1]).commit();
//                linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
                linearLayouts[1].setBackgroundColor(getResources().getColor(R.color.lay_select_bg));
                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=1;
                break;
            case R.id.lay2:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[2]).commit();
//                linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
                linearLayouts[2].setBackgroundColor(getResources().getColor(R.color.lay_select_bg));
                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
                redDor[0].setVisibility(View.GONE);
                MARK=2;
                break;
            case R.id.lay3:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[3]).commit();
//                linearLayouts[3].setBackgroundResource(R.drawable.lay_select_bg);
                linearLayouts[3].setBackgroundColor(getResources().getColor(R.color.lay_select_bg));
                textViews[3].setTextColor(getResources().getColor(R.color.lightseagreen));
                redDor[1].setVisibility(View.GONE);
                MARK=3;
                break;
            case R.id.lay4:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).hide(fragments[4]).show(fragments[4]).commit();
//                linearLayouts[4].setBackgroundResource(R.drawable.lay_select_bg);
                linearLayouts[4].setBackgroundColor(getResources().getColor(R.color.lay_select_bg));
                textViews[4].setTextColor(getResources().getColor(R.color.lightseagreen));
                MARK=4;
                break;
            default:
                break;
        }

        //写数据
        readLayType();
    }
    //写入当前fragment编号----------------------------------------------------
    private void readLayType(){

        SharedPreferences.Editor wStateData = getSharedPreferences("StateData",MODE_PRIVATE).edit();
        wStateData.putInt("layPage",MARK);
        if (!wStateData.commit()) wStateData.commit();

        fragment4Intent.putExtra("layChange",MARK);
        sendBroadcast(fragment4Intent);

    }
    //复位导航栏样式----------------------------------------------------
    public void resetLaybg() {
        for(int i=0;i<5;i++)
        {
            linearLayouts[i].setBackgroundColor(getResources().getColor(R.color.lay_select_lose));
//            linearLayouts[i].setBackgroundResource(R.drawable.lay_select_lose);
            textViews[i].setTextColor(getResources().getColor(R.color.black));
        }

    }



    /*滑动切换fragment（已弃用）*/
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // TODO Auto-generated method stub
//        //将该Activity上触碰事件交给GestureDetector处理
//        return detector.onTouchEvent(event);
//    }
//    @Override
//    public boolean onDown(MotionEvent arg0) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    //滑动切换效果的实现//
//    @Override
//    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
//        // TODO Auto-generated method stub
//        resetlaybg();
//        //当是Fragment0的时候
//        if(MARK==0) {
//            //Log.e(TAG,"lay1 thocd");
//            if(arg1.getX()<arg0.getX()+DISTANT)
//            {
//                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[1]).commit();
//                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
//                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=1;
//            } else {
//                //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
//                //当超出边界时，不能判断为移动同样选择在原位
//                //textViews[0].setTextColor(getResources().getColor(R.color.black));
//                textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=0;
//            }
//        }
//        //当是Fragment1的时候
//        else if (MARK==1) {
//            //Log.e(TAG,"lay2 thocd");
//            if(arg1.getX()<arg0.getX()+DISTANT)
//            {
//                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[2]).commit();
//                //linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
//                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=2;
//            } else if(arg0.getX()<arg1.getX()+DISTANT) {
//                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[0]).commit();
//                //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
//                textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=0;
//            }
//            else
//            {
//                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
//                //textViews[1].setTextColor(getResources().getColor(R.color.black));
//                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=1;
//            }
//        }
//        //当是Fragment2的时候
//        else if(MARK==2) {
//            //Log.e(TAG,"lay3 thocd");
//            if(arg1.getX()<arg0.getX()+DISTANT)
//            {
//                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[3]).commit();
//                //linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
//                textViews[3].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=3;
//            } else if(arg0.getX()<arg1.getX()+DISTANT) {
//                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[1]).commit();
//                //linearLayouts[0].setBackgroundResource(R.drawable.lay_select_bg);
//                textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=1;
//            }
//            else
//            {
//                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
//                //textViews[1].setTextColor(getResources().getColor(R.color.black));
//                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=2;
//            }
//        }
//        //当是Fragment3的时候
//        else if(MARK==3) {
//            //Log.e(TAG,"lay3 thocd");
//            if(arg0.getX()<arg1.getX()+DISTANT)
//            {
//                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2]).hide(fragments[3]).show(fragments[2]).commit();
//                //linearLayouts[1].setBackgroundResource(R.drawable.lay_select_bg);
//                textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=2;
//            } else {
//                //linearLayouts[2].setBackgroundResource(R.drawable.lay_select_bg);
//                //textViews[2].setTextColor(getResources().getColor(R.color.black));
//                textViews[3].setTextColor(getResources().getColor(R.color.lightseagreen));
//                MARK=3;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public void onLongPress(MotionEvent arg0) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public void onShowPress(MotionEvent arg0) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public boolean onSingleTapUp(MotionEvent arg0) {
//        // TODO Auto-generated method stub
//        return false;
//    }



    /**
     * Main定时线程
     * 1、定时判断Lay2、Lay3红点是否需要显示
     */
    Handler MainHandler = new Handler();
    Runnable MainRunnable = new Runnable() {
        @Override
        public void run() {
            MainHandler.postDelayed(this,100);

            //5分钟无操作时，回到主页面并降低屏幕亮度
            if (iNotAction > 600 ){
                if ( MARK != 0) linearLayouts[0].callOnClick();
                if ( Math.round(getActivityBrightness()) > 0){
                    setActivityBrightness(0);
                }
            }else if ( iNotAction < 600 && Math.round( getActivityBrightness() ) < 1){
                setActivityBrightness(255);
            }

            //仿微信朋友圈红点
            SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData", 0);
            //lay2
            if (rAlarmData.getInt("i_RecordTime",0) != iOldRecord && MARK != 2){
                redDor[0].setVisibility(View.VISIBLE);
                iOldRecord = rAlarmData.getInt("i_RecordTime",0);
            }
            if (MARK == 2){
                iOldRecord = rAlarmData.getInt("i_RecordTime",0);
            }
            //lay3
            if (rAlarmData.getInt("i_AlarmTime",0) != iOldAlarm && MARK != 3){
                redDor[1].setVisibility(View.VISIBLE);
                iOldAlarm = rAlarmData.getInt("i_AlarmTime",0);
            }
            if (MARK == 3){
                iOldAlarm = rAlarmData.getInt("i_AlarmTime",0);
            }

        }
    };

}
