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
import android.database.SQLException;
import android.os.Bundle;

import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.github.mikephil.charting.utils.FileUtils;
import com.great.grt_vdc_t4200l.SerialPortHelp.SerialPortHelper;
import com.great.grt_vdc_t4200l.SerialPortHelp.bean.ComBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;

import android_serialport_api.SerialPort;

public class BaseCourse extends FragmentActivity {

    private static final String TAG = "BaseCourse";
    //广播声明----------------------------------------------------
    //Intent lifeCycleChange = new Intent("drc.xxx.yyy.baseActivity");
    Intent dataChange = new Intent("drc.xxx.yyy.fragment1");
    MyBaseActivity_Broad baseCourseBroad = null;
    IntentFilter baseCourseIntentFilter = new IntentFilter("drc.xxx.yyy.baseActivity");
    int text[] = new int[5];
    //串口声明----------------------------------------------------
    SerialControl downCom;                                      //串口
//    private OutputStream mOutput;                                       //发送
//    private InputStream mInput;                                         //接收
//    private byte[] mbuffer = new byte[2];                               //接收缓存区
//    boolean _isOpen = false;                                    //串口开关标志
//    DispQueueThred DispQueue;
    //硬件声明----------------------------------------------------

    //其他声明----------------------------------------------------
    boolean globalError = false;                                  //全局错误

    /**
     * 活动生命周期
     */
    //活动创建----------------------------------------------------
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //串口
        downCom = new SerialControl();


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

        showNavigation();
        handler.removeCallbacks(task);

        //注销广播
        if (baseCourseBroad != null){
            unregisterReceiver(baseCourseBroad);
            baseCourseBroad = null;
//            Log.e(TAG,"baseCourse，已注销广播");
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
            if (downCom.getIsOpen()){
                //int text[] = new int[3];
                text[0] = (int)(Math.random()*400);
                text[1] = (int)(Math.random()*400);
                text[2] = (int)(Math.random()*400);
                text[3] = (int)(Math.random()*400);

                text[4]++;
                if(text[4]>100) text[4]=0;
//                dataChange.putExtra("dataChange",text);
//                sendBroadcast(dataChange);

                sendPortData(downCom,"01 02 03 04 05 06 07");


                SharedPreferences.Editor editor = getSharedPreferences("temp",MODE_PRIVATE).edit();
                editor.putInt("Uv",text[0]);
                editor.putInt("Vv",text[1]);
                editor.putInt("Wv",text[2]);
                editor.putInt("Capv",text[3]);
                editor.putInt("batterCapacity",text[4]);
                editor.commit();

            }
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

//        View v = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION     //隐藏导航栏
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY          //
//                | View.SYSTEM_UI_FLAG_FULLSCREEN               //全屏
//                | View.SYSTEM_UI_FLAG_IMMERSIVE
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//        v.setSystemUiVisibility(uiOptions);

        //boolean ishide;
//        try{
//            String command;
//            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui";
//            Process proc =  Runtime.getRuntime().exec(new String[] { "su", " -c", command });
//            proc.waitFor();
//        }catch (Exception ex){
//            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
//        }

        Window window;
        window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE|View.SYSTEM_UI_FLAG_FULLSCREEN;
        window.setAttributes(params);


        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }

//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);

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
    private void restart(){
        Toast.makeText(this,"即将重启！",Toast.LENGTH_SHORT).show();
        try {
            Log.v(TAG, "root Runtime->reboot");
            Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","reboot "});
            proc.waitFor();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    //提示音----------------------------------------------------
    private void Beep(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null){
            if (globalError){
                vibrator.vibrate(new long[]{500,500},0);
            }else {
                vibrator.cancel();
            }
        }
    }

    /**
     * 串口通讯方法
     */
    //打开com----------------------------------------------------
    private void openCom(SerialPortHelper ComPort){
        /*
        //尝试打开串口
        try {
//            mSerialPort = new SerialPort(new File("/dev/ttyS2"),38400,0);//设置串口、波特率、校验位
//            mOutput = mSerialPort.getOutputStream();
//            mInput = mSerialPort.getInputStream();
            _isopen = true;
        }catch (SecurityException|IOException e) {
            _isopen = false;
        }*/
        try {
            ComPort.open();
        }catch (SecurityException e){
            Toast.makeText(this,"打开串口失败，没有权限",Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            Toast.makeText(this,"打开串口失败，未知错误",Toast.LENGTH_SHORT).show();
        }catch (InvalidParameterException e){
            Toast.makeText(this,"打开串口失败，参数错误",Toast.LENGTH_SHORT).show();
        }
    }
    //关闭com----------------------------------------------------
    private void colseCom(SerialPortHelper ComPort){
//        try {
//            mOutput.close();
////            mInput.close();
////            mSerialPort.close();
//            _isopen = false;
//        }catch (IOException e){
//            _isopen = true;
//        }
        if (ComPort != null){
            ComPort.stopSend();
            ComPort.close();
        }
    }
    //发送数据----------------------------------------------------
    private void sendPortData(SerialPortHelper ComPort,String sOut){
        if (ComPort != null && ComPort.getIsOpen()){
            ComPort.sendTxt(sOut);
        }
    }
    //继承串口工具----------------------------------------------------
    private class SerialControl extends SerialPortHelper{
        public SerialControl(){
        }
    }
    /*
    private class DispQueueThred extends Thread{
        private Queue<ComBean> QueueList = new LinkedList<ComBean>();
        @Override
        public void run(){
            super.run();
            while (!isInterrupted()){
                final ComBean ComData;
                while ((ComData = QueueList.poll())!=null){
                    runOnUiThread(new Runnable(){
                        public void run(){
                            Log.e(TAG, "run: ");
                        }
                    });
                    try {
                        Thread.sleep(100);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        public synchronized void AddQueue(ComBean ComData){
            QueueList.add(ComData);
        }
    }*/

    /**
     *  软件流程方法
     */
    //重载流程----------------------------------------------------
    private void loadSOP(){
        String fileNaem;
        boolean _fileExists;

        //1、检测root权限

        //2、打开串口
        for (int i = 0; i < 3 ; i++) {
            if (!downCom.getIsOpen()){ openCom(downCom); }else { break; }
        }


        //3.1、检测录波记录文件夹
        fileNaem = "/record_log/";
        _fileExists = checkFile(fileNaem);
        if (!_fileExists){
//            Log.e(TAG,"SOP, "+fileNaem+" 文件夹不存在");
        }else {
//            Log.e(TAG,"SOP, "+fileNaem+" 文件夹存在");
        }

        //3.2、检测故障记录文件
        fileNaem = "/fault_log/";
        _fileExists = checkFile(fileNaem);
        if (!_fileExists){
//            Log.e(TAG,"SOP, "+fileNaem+" 文件夹不存在");
        }else {
//            Log.e(TAG,"SOP, "+fileNaem+" 文件夹存在");
        }

        fileNaem = "/fault_log/fault_record.xls";
        _fileExists = checkFile(fileNaem);
        if (!_fileExists){
//            Log.e(TAG,"SOP, "+fileNaem+" 文件不存在");
        }else {
//            Log.e(TAG,"SOP, "+fileNaem+" 文件存在");
        }

        //3.3、检测实时采样文件
        fileNaem = "/sampling_now/";
        _fileExists = checkFile(fileNaem);
        if (!_fileExists){
//            Log.e(TAG,"SOP, "+fileNaem+" 文件夹不存在");
        }else {
//            Log.e(TAG,"SOP, "+fileNaem+" 文件夹存在");
        }



    }
    //SOP启动错误处理----------------------------------------------------
    private void errorSOP(int type){
        switch (type){
            case 1:
                Toast.makeText(this,"串口打开失败！",Toast.LENGTH_SHORT).show();
                break;
            case 10:
                Toast.makeText(this,"录波记录文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
                break;
            case 11:
                Toast.makeText(this,"故障记录文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
                break;
            case 12:
                Toast.makeText(this,"实时采样文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
                break;
            case 15:
                Toast.makeText(this,"故障记录文件不存在或创建失败！",Toast.LENGTH_SHORT).show();
                break;
        }
        Toast.makeText(this,"初始化失败，即将重启！",Toast.LENGTH_SHORT).show();
    }
    //中止流程----------------------------------------------------
    private void pauseSOP(){
        colseCom(downCom);
        if (!downCom.getIsOpen()){ Log.e(TAG,"SOP,串口关闭成功");}else{ Log.e(TAG,"SOP,串口关闭失败");}
    }
    //检测文件----------------------------------------------------
    private boolean checkFile(String fileName){
        String PATH = this.getFilesDir().getPath()+fileName;
        try {
            File file = new File(PATH);
            if (!file.exists()){
                if (fileName.contains(".")){
                    Log.e(TAG,"文件不存在,开始创建");
                    return createFile(fileName);
//                    file.createNewFile();
                }else{
                    Log.e(TAG,"文件夹不存在,开始创建");
                    file.mkdir();
                }
            }
        }catch (Exception e){ return false; }
        return true;
    }
    //创建文件----------------------------------------------------
    private boolean createFile(String fileName){
        String PATH = this.getFilesDir().getPath()+fileName;
        try {
            File file = new File(PATH);
            if (!file.exists()){
                file.createNewFile();
            }
        }catch (Exception e){ return false; }
        return true;
    }


}
