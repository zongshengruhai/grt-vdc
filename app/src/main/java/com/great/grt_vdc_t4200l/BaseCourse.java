package com.great.grt_vdc_t4200l;

/* ------------- BaseCourse 说明 -------------
    主要描述：所有Activity集成父类
    创建日期：2018年7月27日 10:02:22
*/

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class BaseCourse extends FragmentActivity {

    private static final String TAG = "BaseCourse";

    //广播声明
    //Intent lifeCycleChange = new Intent("drc.xxx.yyy.baseActivity");
    Intent dataChange = new Intent("drc.xxx.yyy.fragment1");
    private MyBaseActivity_Broad baseCourseBroad = null;
    private IntentFilter baseCourseIntentFilter = new IntentFilter("drc.xxx.yyy.baseActivity");
    int text[] = new int[5];


    // ------------- 重写生命周期 -------------//
    //创建
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Log.e(TAG,"底层创建");

        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    //开始
    @Override
    protected void onStart(){
        super.onStart();
        //Log.e(TAG,"底层开始");
    }

    //重启
    @Override
    protected void onRestart(){
        super.onRestart();
        //Log.e(TAG,"底层重启");
    }

    //重载
    @Override
    protected void onResume(){
        super.onResume();
        //Log.e(TAG,"底层重载");

        hideNavigation();

        handler.post(task);

        //注册广播
        if (baseCourseBroad == null){
            baseCourseBroad = new MyBaseActivity_Broad();
            registerReceiver(baseCourseBroad,baseCourseIntentFilter);
            Log.e(TAG,"baseCourse，已注册广播");
        }

    }
    //中止
    @Override
    protected void onPause(){
        super.onPause();
        //Log.e(TAG,"底层中止");

        showNavigation();

        handler.removeCallbacks(task);

        //注销广播
        if (baseCourseBroad != null){
            unregisterReceiver(baseCourseBroad);
            baseCourseBroad = null;
            Log.e(TAG,"baseCourse，已注销广播");
        }
    }

    //停止
    @Override
    protected void onStop(){
        super.onStop();
        //Log.e(TAG,"底层停止");
    }

    //退出
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //Log.e(TAG,"底层退出");
    }

    Handler handler = new Handler();
    Runnable task = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this,500);

            //int text[] = new int[3];
            text[0] = (int)(Math.random()*400);
            text[1] = (int)(Math.random()*400);
            text[2] = (int)(Math.random()*400);
            text[3] = (int)(Math.random()*400);

            text[4]++;
            if(text[4]>100) text[4]=0;
            /*
            dataChange.putExtra("dataChange",text);
            sendBroadcast(dataChange);*/

            SharedPreferences.Editor editor = getSharedPreferences("temp",MODE_PRIVATE).edit();
            editor.putInt("Uv",text[0]);
            editor.putInt("Vv",text[1]);
            editor.putInt("Wv",text[2]);
            editor.putInt("Capv",text[3]);
            editor.putInt("batterCapacity",text[4]);
            editor.commit();

        }
    };

    // ------------- 总线广播 ------------- //
    /*主要实现功能：
        1）退出app，注销所有继承 BaseCourse 的子程活动
            方法：
                1.new 一个  Intent 类型的 intent;
                2.对intent执行putExtra，增加name:closeALL,value:1;
                3.SendBroadcast（intent）.
        2）生命周期改变事件，监控底层activity生命周期，根据不同的事件执行任务
            方法：

    */
    public class MyBaseActivity_Broad extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent){

            //关闭APP广播
            int closeAll = intent.getIntExtra("closeAll",0);
            if (closeAll == 1){
                //showNavigation();
                finish();
                System.exit(0);
            }

            //底层生命周期改变事件广播
            int lifeCycleFlag = intent.getIntExtra("lifeCycleChange",0);
            switch (lifeCycleFlag){
                //底层重载
                case 1:
                    Log.e(TAG,"广播接收到底层重装");
                    break;

                 //底层中止
                case 2:
                    //restart();
                    Log.e(TAG,"广播接收到底层中止");
                    break;
            }

            //fragment Toast事件
            int fragmentToast = intent.getIntExtra("fragmentToast",0);
            if (fragmentToast > 0){
                Toast(fragmentToast);
            }

        }
    }

    //
    private void Toast(int ToastType){
        switch (ToastType){
            case 1:
                Toast.makeText(this,"输入的密码长度不足，请输入8位数字密码",Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this,"请输入密码",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this,"输入密码错误，请重新输入",Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(this,"管理员,登录成功",Toast.LENGTH_SHORT).show();
                break;
            case 5:
                Toast.makeText(this,"巡检员，登录成功",Toast.LENGTH_SHORT).show();
                break;
        }
    }


    //------------- 硬件服务类方法 -------------//
    //隐藏虚拟按键
    public void hideNavigation(){

        View v = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION     //隐藏导航栏
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY          //
                | View.SYSTEM_UI_FLAG_FULLSCREEN               //全屏
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        v.setSystemUiVisibility(uiOptions);

        //boolean ishide;
//        try{
//            String command;
//            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui";
//            Process proc =  Runtime.getRuntime().exec(new String[] { "su", " -c", command });
//            proc.waitFor();
//        }catch (Exception ex){
//            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
//        }

    }

    //显示虚拟按键
    public void showNavigation(){

//        try {
//            String command;
//            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
//            Process proc = Runtime.getRuntime().exec(new String[] { "su", " -c", command } );
//            proc.waitFor();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }


    //重启设备
    /*
    public void restart(){
        PowerManager myManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        myManager.reboot("重启");
    }*/


    //打开串口
    public void openSerial(){

    }

    //关闭串口
    public void closeSerial(){

    }

    // ------------- 软件流程类方法 -------------//



}
