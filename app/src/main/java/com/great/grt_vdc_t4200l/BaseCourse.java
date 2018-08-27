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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class BaseCourse extends FragmentActivity {

    private static final String TAG = "BaseCourse";
    //广播声明----------------------------------------------------
    //Intent lifeCycleChange = new Intent("drc.xxx.yyy.baseActivity");
    Intent dataChange = new Intent("drc.xxx.yyy.fragment1");
    private MyBaseActivity_Broad baseCourseBroad = null;
    private IntentFilter baseCourseIntentFilter = new IntentFilter("drc.xxx.yyy.baseActivity");
    int text[] = new int[5];
    //串口声明----------------------------------------------------
    private SerialPort mSerialPort;                                      //串口
    private OutputStream mOutput;                                       //发送
    private InputStream mInput;                                         //接收
    private byte[] mbuffer = new byte[2];                               //接收缓存区
    private boolean _isopen = false;                                    //串口开关标志

    /**
     * 活动生命周期
     */
    //活动创建----------------------------------------------------
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Log.e(TAG,"底层创建");

        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    //活动开始----------------------------------------------------
    @Override
    protected void onStart(){
        super.onStart();
        //Log.e(TAG,"底层开始");
    }
    //活动重启----------------------------------------------------
    @Override
    protected void onRestart(){
        super.onRestart();
        //Log.e(TAG,"底层重启");
    }
    //活动重载----------------------------------------------------
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

        loadSOP();

    }
    //活动中止----------------------------------------------------
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

        pauseSOP();
    }
    //活动退出----------------------------------------------------
    @Override
    protected void onStop(){
        super.onStop();
        //Log.e(TAG,"底层停止");
    }
    //活动注销----------------------------------------------------
    @Override
    protected void onDestroy(){
        super.onDestroy();
        //Log.e(TAG,"底层退出");
    }
    //定时主线程----------------------------------------------------
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

    /**
     * 总线广播
     */
    //广播处理----------------------------------------------------
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
    //Toast处理----------------------------------------------------
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

    /**
     * 硬件服务方法
     */
    //隐藏虚拟按键----------------------------------------------------
    private void hideNavigation(){

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
    //显示虚拟按键----------------------------------------------------
    private void showNavigation(){

//        try {
//            String command;
//            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
//            Process proc = Runtime.getRuntime().exec(new String[] { "su", " -c", command } );
//            proc.waitFor();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
    //重启设备----------------------------------------------------
    /*
    public void restart(){
        PowerManager myManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        myManager.reboot("重启");
    }*/

    /**
     * 串口通讯方法
     */
    //打开com----------------------------------------------------
    private void openCom(){
        //尝试打开串口
        try {
            mSerialPort = new SerialPort(new File("/dev/ttyS2"),38400,0);//设置串口、波特率、校验位
            mOutput = mSerialPort.getOutputStream();
            mInput = mSerialPort.getInputStream();
            _isopen = true;
        }catch (SecurityException|IOException e) {
            _isopen = false;
        }
    }
    //关闭com----------------------------------------------------
    private void colseCom(){
        try {
            mOutput.close();
            mInput.close();
            mSerialPort.close();
            _isopen = false;
        }catch (IOException e){
            _isopen = true;
        }
    }
    //读取线程----------------------------------------------------
    class ReadRunner implements Runnable{
        @Override
        public void run(){
//            while (_isopen){
//                try {
//                    if (mInput != null){
//
//                    }
//                }catch (IOException e){
//
//                }
//            }
        }
    }

    /**
     *  软件流程方法
     */
    //流程检测----------------------------------------------------
    private void loadSOP(){

        //1、打开串口
        for (int i = 0; i < 3 ; i++) {
            if (!_isopen){
                openCom();
            }else {
                break;
            }
        }
        if (!_isopen){ Log.e(TAG,"SOP,串口打开失败");}else{ Log.e(TAG,"SOP,串口打开成功");}

        //2、检测文件



    }

    //流程检测----------------------------------------------------
    private void pauseSOP(){

        colseCom();
        if (!_isopen){ Log.e(TAG,"SOP,串口关闭成功");}else{ Log.e(TAG,"SOP,串口关闭失败");}

    }




}
