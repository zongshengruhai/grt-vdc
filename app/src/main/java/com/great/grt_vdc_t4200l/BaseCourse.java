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
import com.great.grt_vdc_t4200l.SerialPortHelp.MyFunc;
import com.great.grt_vdc_t4200l.SerialPortHelp.SerialPortHelper;
import com.great.grt_vdc_t4200l.SerialPortHelp.bean.ComBean;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android_serialport_api.SerialPort;

import static com.great.grt_vdc_t4200l.SystemFunc.checkFileExist;
import static com.great.grt_vdc_t4200l.SystemFunc.createExcel;
import static com.great.grt_vdc_t4200l.SystemFunc.createFile;
import static com.great.grt_vdc_t4200l.SystemFunc.restart;

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
    private String sOutData = "7E0000000000000D";
    private byte[] bOutData = new byte[]{(byte)0x7E,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0x0D};
    //数据声明----------------------------------------------------
    private int[] iTelemetry = new int[14];                     //遥测
    private boolean[] _isTelecommand = new boolean[8];          //遥信
    private boolean[] _isTelecontrol = new boolean[2];          //遥控
    private String sSystemTime;                                 //时间
    private int iCommError = 0;
    //硬件声明----------------------------------------------------

    //其他声明----------------------------------------------------
    boolean globalError = false;                                //全局错误
    private Context mContext;
    //系统自检声明----------------------------------------------------
    static private String faultPath ;
    static private String faultName;
    static private String recordPath;
    public boolean _isSystem = false;

    /**
     * 活动生命周期
     */
    //活动创建----------------------------------------------------
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mContext = getBaseContext();

        //串口
        downCom = new SerialControl();
        downCom.setmContext(mContext);

        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //申明路径
        faultPath = this.getFilesDir().getPath()+"/fault_log/";
        faultName = faultPath + "fault_record.xls";
        recordPath = this.getFilesDir().getPath()+"/record_log/";

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
//    //重启设备----------------------------------------------------
//    private void restart(){
//        Toast.makeText(this,"即将重启！",Toast.LENGTH_SHORT).show();
//        try {
//            Log.v(TAG, "root Runtime->reboot");
//            Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","reboot "});
//            proc.waitFor();
//        }catch (Exception ex){
//            ex.printStackTrace();
//        }
//    }
//    //提示音----------------------------------------------------
//    private void Beep(){
//        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        if (vibrator != null){
//            if (globalError){
//                vibrator.vibrate(new long[]{500,500},0);
//            }else {
//                vibrator.cancel();
//            }
//        }
//    }
//    //设置系统时间----------------------------------------------------
//    private void setSystemTime(String sTime){
//        if (sTime.length() == 15){
//            try {
//                Process process = Runtime.getRuntime().exec("su");
//                DataOutputStream os = new DataOutputStream(process.getOutputStream());
//                os.writeBytes("setprop persist.sys.timezone GMT\n");
//                os.writeBytes("/system/bin/date -s" + sTime + "\n");
//                os.writeBytes("clock -w\n");
//                os.writeBytes("exit\n");
//                os.flush();
//            }catch (IOException e){
//                Log.e(TAG, "setSystemTime: loser" );
//                e.printStackTrace();
//            }
//        }else {
//            Log.e(TAG, "setSystemTime: loser ,length is error" );
//        }
//
//    }

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
    private void sendPortData(SerialPortHelper ComPort,String type){
        if (ComPort != null && ComPort.getIsOpen()){
            switch (type){
                case "Hex":
                    ComPort.sendHex(sOutData);
                    break;
                case "Txt":
                    ComPort.sendTxt(sOutData);
                    break;
                case "bArr":
                    ComPort.send(bOutData);
                    break;
            }
        }
    }
    //继承串口工具----------------------------------------------------
    private class SerialControl extends SerialPortHelper{
        public SerialControl(){
        }
    }

    /**
     *  软件流程方法
     */
    //重载流程----------------------------------------------------
    private void loadSOP(){

        for (int i = 0; i < 3; i++) {

            //故障文件夹
            if (!checkFileExist(faultPath)){ createFile(faultPath); }
            //故障文件
            if (!checkFileExist(faultName)){
                List<List<Object>> mList = new ArrayList<>();
                List<Object> mRow = new ArrayList<>();
                mRow.add("编号");
                mRow.add("事件类型");
                mRow.add("开始时间");
                mRow.add("结束时间");
                mList.add(mRow);
                createExcel(faultName,mList);
            }
            //录波文件夹
            if (!checkFileExist(recordPath)){createFile(recordPath);}

            //串口
            if (!downCom.getIsOpen()){ openCom(downCom); }
        }

        if (!checkFileExist(faultName)&&!checkFileExist(recordPath)&&!downCom.getIsOpen()){
//            restart();//重启
        }else {
            _isSystem = true;
        }

//        String fileName;
//        boolean _fileExists;
//
//        //1、检测root权限
//
//        //2、打开串口
//        for (int i = 0; i < 3 ; i++) {
//            if (!downCom.getIsOpen()){ openCom(downCom); }
//        }
//
//        //3.1、检测录波记录文件夹
////        fileName = this.getFilesDir().getPath()+"/record_log/";
////        if (!checkFileExist(fileName)){
////            createFile(fileName);
////        }
//
//
//        //3.2、检测故障记录文件
//        fileName = "/fault_log/";
//        _fileExists = checkFile(fileName);
//        if (!_fileExists){
////            Log.e(TAG,"SOP, "+fileNaem+" 文件夹不存在");
//        }else {
////            Log.e(TAG,"SOP, "+fileNaem+" 文件夹存在");
//        }
//
////        fileNaem = "/fault_log/fault_record.xls";
////        _fileExists = checkFile(fileNaem);
////        if (!_fileExists){
//////            Log.e(TAG,"SOP, "+fileNaem+" 文件不存在");
////        }else {
//////            Log.e(TAG,"SOP, "+fileNaem+" 文件存在");
////        }

    }

//    //SOP启动错误处理----------------------------------------------------
//    private void errorSOP(int type){
//        switch (type){
//            case 1:
//                Toast.makeText(this,"串口打开失败！",Toast.LENGTH_SHORT).show();
//                break;
//            case 10:
//                Toast.makeText(this,"录波记录文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
//                break;
//            case 11:
//                Toast.makeText(this,"故障记录文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
//                break;
//            case 12:
//                Toast.makeText(this,"实时采样文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
//                break;
//            case 15:
//                Toast.makeText(this,"故障记录文件不存在或创建失败！",Toast.LENGTH_SHORT).show();
//                break;
//        }
//        Toast.makeText(this,"初始化失败，即将重启！",Toast.LENGTH_SHORT).show();
//    }

    //中止流程----------------------------------------------------
    private void pauseSOP(){
        _isSystem = false;
        colseCom(downCom);
        if (!downCom.getIsOpen()){ Log.e(TAG,"SOP,串口关闭成功");}else{ Log.e(TAG,"SOP,串口关闭失败");}
    }


//    //检测文件----------------------------------------------------
//    private boolean checkFile(String fileName){
//        String PATH = this.getFilesDir().getPath()+fileName;
//        try {
//            File file = new File(PATH);
//            if (!file.exists()){
//                if (fileName.contains(".")){
//                    Log.e(TAG,"文件不存在,开始创建");
//                    return createFile(fileName);
////                    file.createNewFile();
//                }else{
//                    Log.e(TAG,"文件夹不存在,开始创建");
//                    file.mkdir();
//                }
//            }
//        }catch (Exception e){ return false; }
//        return true;
//    }
//    //创建文件----------------------------------------------------
//    private boolean createFile(String fileName){
//        String PATH = this.getFilesDir().getPath()+fileName;
//        try {
//            File file = new File(PATH);
//            if (!file.exists()){
//                file.createNewFile();
//            }
//        }catch (Exception e){ return false; }
//        return true;
//    }
//

    //定时主线程----------------------------------------------------
    Handler handler = new Handler();
    Runnable task = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this,500);
            if (downCom.getIsOpen()){

                //通讯错误振铃：5次定时未接受到数据、接收数据错误、校验错误、帧错误等
                boolean _isCommFlag = downCom.getisCommFlag();
                if (!_isCommFlag){ if (iCommError < 10){iCommError ++;} }else { iCommError = 0;}
//                if (iCommError > 5){SystemFunc.Beep(mContext,true);}else {SystemFunc.Beep(mContext,false);}

                bOutData[1] = (byte)0x03;
                bOutData[5] = (byte)0x13;
                bOutData[6] = MyFunc.addCrc(bOutData);
                sendPortData(downCom,"bArr");

                SharedPreferences.Editor wRealData = mContext.getSharedPreferences("RealData",MODE_PRIVATE).edit();
                wRealData.putInt("i_Rv",(int)(Math.random()*400));                             //R相电压
                wRealData.putInt("i_Sv",(int)(Math.random()*400));                             //S相电压
                wRealData.putInt("i_Tv",(int)(Math.random()*400));                             //T相电压
                wRealData.putInt("i_Uv",(int)(Math.random()*400));                             //U相电压
                wRealData.putInt("i_Vv",(int)(Math.random()*400));                             //V相电压
                wRealData.putInt("i_Wv",(int)(Math.random()*400));                             //W相电压
                wRealData.putInt("i_Ua",(int)(Math.random()*100));                             //U相电流
                wRealData.putInt("i_Va",(int)(Math.random()*100));                             //V相电流
                wRealData.putInt("i_Wa",(int)(Math.random()*100));                             //W相电流
                wRealData.putInt("i_Capv",(int)(Math.random()*100));
                wRealData.putBoolean("is_RechargeFlag",true);
                wRealData.putBoolean("is_CompensateFlag",false);

                wRealData.commit();
//                //int text[] = new int[3];
//                text[0] = (int)(Math.random()*400);
//                text[1] = (int)(Math.random()*400);
//                text[2] = (int)(Math.random()*400);
//                text[3] = (int)(Math.random()*400);
//
//                text[4]++;
//                if(text[4]>100) text[4]=0;
////                dataChange.putExtra("dataChange",text);
////                sendBroadcast(dataChange);
//
//                SharedPreferences.Editor editor = getSharedPreferences("temp",MODE_PRIVATE).edit();
//                editor.putInt("Uv",text[0]);
//                editor.putInt("Vv",text[1]);
//                editor.putInt("Wv",text[2]);
//                editor.putInt("Capv",text[3]);
//                editor.putInt("batterCapacity",text[4]);
//                editor.commit()
//
//                iTelemetry = downCom.getiTelemetry();
//                _isTelecommand = downCom.getisTelecommand();
//                _isTelecontrol = downCom.getisTelecontrol();
//                sSystemTime = downCom.getSystemTime();
//
//                SharedPreferences.Editor editor = getSharedPreferences("realTimeData",MODE_PRIVATE).edit();
//                // 遥测
//                editor.putInt("i_Rv",iTelemetry[0]);                             //R相电压
//                editor.putInt("i_Sv",iTelemetry[1]);                             //S相电压
//                editor.putInt("i_Tv",iTelemetry[2]);                             //T相电压
//                editor.putInt("i_Uv",iTelemetry[3]);                             //U相电压
//                editor.putInt("i_Vv",iTelemetry[4]);                             //V相电压
//                editor.putInt("i_Wv",iTelemetry[5]);                             //W相电压
//                editor.putInt("i_Ua",iTelemetry[6]);                             //U相电流
//                editor.putInt("i_Va",iTelemetry[7]);                             //V相电流
//                editor.putInt("i_Wa",iTelemetry[8]);                             //W相电流
//                editor.putInt("i_Hz",iTelemetry[9]);                             //频率
//                editor.putInt("i_SagTime",iTelemetry[10]);                       //录波次数
//                editor.putInt("i_Capv",iTelemetry[11]);                          //电容电压
//                editor.putInt("i_CapAh",(((iTelemetry[11]-232)/142)));           //电容容量
//                editor.putInt("i_NewSagSite",iTelemetry[12]);                    //当前录波位置
//                editor.putInt("i_SagSum",iTelemetry[13]);                        //录波总数
//                //系统事件
//                editor.putString("s_SystemTime",sSystemTime);                    //下位机系统时间
//                //遥信
//                editor.putBoolean("is_RechargeFlag",_isTelecommand[0]);          //充电状态
//                editor.putBoolean("is_CompensateFlag",_isTelecommand[1]);        //补偿状态
//                editor.putBoolean("is_InAlarm",_isTelecommand[3]);               //输入异常
//                editor.putBoolean("is_OutOC",_isTelecommand[4]);                 //输出过流
//                editor.putBoolean("is_OutRl",_isTelecommand[5]);                 //输出短路
//                editor.putBoolean("is_AhLose",_isTelecommand[6]);                //容量失效
//                editor.putBoolean("is_ComError",_isTelecommand[7]);              //通讯异常
//                //遥控
//                editor.putBoolean("is_SystemMode",_isTelecontrol[0]);            //系统模式
//                editor.putBoolean("is_CompensateEnabled",_isTelecontrol[1]);     //补偿使能
//                editor.commit();
//                sendPortData(downCom,"01020304050607");


            }
        }
    };

}
