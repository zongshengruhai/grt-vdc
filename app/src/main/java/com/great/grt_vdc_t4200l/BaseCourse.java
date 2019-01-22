package com.great.grt_vdc_t4200l;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.great.grt_vdc_t4200l.SerialPortHelp.MyFunc;
import com.great.grt_vdc_t4200l.SerialPortHelp.SerialPortHelper;
import com.great.grt_vdc_t4200l.SerialPortHelp.UpSerialPortHelper;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * BaseCourse , System base tier
 *
 * @author zongshengruhai
 *
 * @version 1.0
 *
 * 这是系统层，所有通信都将交给这一层处理;
 * 如果您想处理或增加要下发的通信内容，可以在这个{@link #task}主线程中处理;
 * 如果您想配置通讯层或通讯配置，请到{@link SerialPortHelper}配置，
 * 该层只创建一个新的{@link SerialPortHelper}类。
 *
 * date：2018年11月29日 15:17:19
 */
public class BaseCourse extends FragmentActivity {

    /** 这是这个层的 context */
    public Context mContext;

    /** 用于输出此层 log 的 tag */
    private static final String TAG = "BaseCourse";

    /** 系统正常标志 */
    public boolean _isSystem = false;

    /**
     * 此层的广播
     * 用于  {@link MyBaseActivity_Broad} 这个方法 , 用于接收其他层无法处理的工作、或是其他层触发的通讯请求
     */
    MyBaseActivity_Broad baseCourseBroad = null;
    IntentFilter baseCourseIntentFilter = new IntentFilter("drc.xxx.yyy.baseActivity");
//    int text[] = new int[5];

    /**
     * 创建实例化这个系统的串口
     * 用于 {@link SerialPortHelper} 这个方法
     */
    SerialControl downCom;
    /** 存储通讯下发的数组 */
    private byte[] bOutData = new byte[]{(byte)0x7E,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0x0D};

    /**
     * 用户触发遥调事件标志
     * 用户将会在 @Fragment/fragment4.java 这个类中触发一些遥调事件
     * 在 @Fragment/fragment4.java 不允许直接处理下发通讯，因此将会以广播的形式发送给本类
     * 由{@link MyBaseActivity_Broad} 广播接收到后交给{@link #CorrectEvent(String[])}进行分类，最后在{@link #task}中按级处理
     * Param _isCorrect 是否有遥调事件标志
     * Param iCorrect[] 用户触发遥调时的相关参数
     */
    private boolean _isCorrect = false;
    private int[] iCorrect = new int[2];
    private String sCorrect = "";

//    /** 通讯失败次数 */
//    private int iCommError = 0;

    /** 遥测故障录波数据失败次数 */
    private int iReadRecordError = 0;
    private int iReadRecordError_2 = 0;

    /** 系统存储数据相关的路径申明 */
    static private String systemPath;
    static private String faultPath ;
    static private String faultName;
    static private String recordPath;
    static private String sharedPrefsPath;

    /** 无操作计时 */
    public int iNotAction;
    private boolean _isActionFlag = false;

    /** Test */
    DownThread downThread;
    private boolean ThreadRun = false ;

    /** beep */
    BeepThread beepThread;
    private boolean is_BeepFlag = false;

    UpSerialControl upCom;

    /** 创建层，初始化一些数据*/
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mContext = getBaseContext();

        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //实例化串口
        downCom = new SerialControl();
        downCom.setmContext(mContext);

//        upCom = new UpSerialControl();
//        upCom.setmContext(mContext);

        //初始化存储相关路径
        systemPath = this.getFilesDir().getPath();
        faultPath = systemPath + "/fault_log/";
        faultName = faultPath + "fault_record.xls";
        recordPath = systemPath + "/record_log/";
        sharedPrefsPath = systemPath.trim().replace("/files","/shared_prefs");
//        faultPath = this.getFilesDir().getPath()+"/fault_log/";
//        faultName = faultPath + "fault_record.xls";
//        recordPath = this.getFilesDir().getPath()+"/record_log/";
//        sharedPrefsPath = this.getFilesDir().toString().trim().replace("/files","/shared_prefs/");

        downThread = new DownThread();
        beepThread = new BeepThread();

//        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
//        for (int i = 0; i < 5 ; i++) {
//            ArrayList<String> arrayList = new ArrayList<>();
//            arrayList.add(i+",1");
//            arrayList.add(i+",2");
//            arrayLists.add(arrayList);
//        }
//        Log.e("test",arrayLists.get(0) + "\r\n"
//                + arrayLists.get(1) + "\r\n"
//                + arrayLists.get(2) + "\r\n"
//                + arrayLists.get(3) + "\r\n"
//                + arrayLists.get(4) + "\r\n");
//        Collections.swap(arrayLists,0,3);
//        Log.e("test",arrayLists.get(0) + "\r\n"
//                + arrayLists.get(1) + "\r\n"
//                + arrayLists.get(2) + "\r\n"
//                + arrayLists.get(3) + "\r\n"
//                + arrayLists.get(4));
    }

    /** 层重载，执行{@link #loadSOP()} 登录流程 */
    @Override
    protected void onResume(){
        super.onResume();
        loadSOP();
    }

    /** 层中止，执行{@link #pauseSOP()} 中止流程 */
    @Override
    protected void onPause(){
        super.onPause();
        pauseSOP();

    }

    /** 隐藏下边栏虚拟按键 */
    private void hideNavigation(){

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

    /**
     * 登录流程
     * 以下是登录流程顺序
     * 1、关闭虚拟导航键盘，并全屏
     * 2、获取系统的最高权限
     * 3、检查、创建系统所需文件、文件夹
     * 4、打开串口
     * 5、注册广播、开始定时主线程
     */
    private void loadSOP(){

        //隐藏虚拟按键
        hideNavigation();

//        SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);
//        if (rStateData.getBoolean("is_SystemDebug",false)){
//            SystemFunc.setStatusBar(mContext,DISABLE_NONE);
//        }else {
//            SystemFunc.setStatusBar(mContext,DISABLE_EXPAND);
//        }

        //获取权限
        SystemFunc.getRoot(getPackageCodePath());

        for (int i = 0; i < 3; i++) {
            //检测创建系统文件夹
            if (!SystemFunc.checkFileExist(systemPath)){ SystemFunc.createFile(systemPath); }
            //检测创建故障文件夹
            if (!SystemFunc.checkFileExist(faultPath)){ SystemFunc.createFile(faultPath); }
            //检测创建故障文件
            if (!SystemFunc.checkFileExist(faultName)){
                List<List<Object>> mList = new ArrayList<>();
                List<Object> mRow = new ArrayList<>();
                mRow.add("编号");
                mRow.add("事件类型");
                mRow.add("开始时间");
                mRow.add("结束时间");
                mList.add(mRow);
                SystemFunc.createExcel(faultName,mList);
            }
            //录波文件夹
            if (!SystemFunc.checkFileExist(recordPath)){SystemFunc.createFile(recordPath);}

            //串口
            if (!downCom.getIsOpen()){ openCom(downCom); }

            if(!SystemFunc.checkFileExist(faultName)&&!SystemFunc.checkFileExist(recordPath)&&!downCom.getIsOpen()){
                break;
            }

        }

        if (!SystemFunc.checkFileExist(faultName)&&!SystemFunc.checkFileExist(recordPath)&&!downCom.getIsOpen()){

            Toast.makeText(this,"系统初始化失败，设备即将重启",Toast.LENGTH_SHORT).show();
            SystemFunc.restart(mContext);

        }else {

            //注册广播
            if (baseCourseBroad == null){
                baseCourseBroad = new MyBaseActivity_Broad();
                registerReceiver(baseCourseBroad,baseCourseIntentFilter);
            }

            _isSystem = true;

            //开始定时线程
            handler.post(task);

            if (!ThreadRun) {
                ThreadRun = true;
                new Thread(downThread).start();
                new Thread(beepThread).start();
            }
        }

    }

    /**
     * 中止流程
     * 以下是中止流程顺序
     * 1、停止主线程
     * 2、注销广播
     * 3、关闭串口
     * 4、重启设备
     */
    private void pauseSOP(){

        //停止定时线程
        handler.removeCallbacks(task);

        ThreadRun = false;

        //注销广播
        if (baseCourseBroad != null){
            unregisterReceiver(baseCourseBroad);
            baseCourseBroad = null;
        }

        _isSystem = false;

        //重启
        closeCom(downCom);
//        if (!downCom.getIsOpen()){ Log.e(TAG,"SOP,串口关闭成功");}else{ Log.e(TAG,"SOP,串口关闭失败");}

        SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
        wStateData.putInt("i_CommError",0);
        if (wStateData.commit())wStateData.commit();

        //重启
        Toast.makeText(this,"系统发生中止，即将重启",Toast.LENGTH_SHORT).show();
        SystemFunc.restart(mContext);

    }

//    /**
//     * 初始化系统流程
//     * 以下是初始化系统流程顺序
//     * 1、等待用户确认初始化
//     * 2、删除所有相关存储文件
//     * 3、重启设备
//     */
//    private void initSystemSOP(){
//        new AlertDialog.Builder(this).setTitle("初始化系统，系统内的数据将会被全部删除，确认执行初始化？")
//                .setPositiveButton("确认", new DialogInterface.OnClickListener() //初始化
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        pauseSOP();
//
//                        if (SystemFunc.checkFileExist(faultPath)) {
//                            SystemFunc.deleteFile(faultPath);
//                        }
//
//                        if (SystemFunc.checkFileExist(recordPath)) {
//                            SystemFunc.deleteFile(recordPath);
//                        }
//
//                        if (SystemFunc.checkFileExist(systemPath)) {
//                            SystemFunc.deleteFile(systemPath);
//                        }
//
//                        if (SystemFunc.checkFileExist(sharedPrefsPath)){
//                            SystemFunc.deleteFile(sharedPrefsPath);
//                        }
//
//                        Toast.makeText(mContext,"系统初始化，即将重启",Toast.LENGTH_SHORT).show();
//
//                        SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
//                        wStateData.putBoolean("is_SystemBeep",true);
//                        wStateData.putBoolean("is_SystemDebug",false);
//                        wStateData.putInt("i_OldSagSite",0);
//                        wStateData.putInt("i_RecordAddress_2",0);
//                        wStateData.putBoolean("is_SystemInit",true);
//
//
//                        if (wStateData.commit())wStateData.commit();
//
//                        SystemFunc.restart(mContext);
//
//                    }
//                })
//                .setNegativeButton("返回", new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Log.e("init System","取消");
//                    }
//                }).show();
//    }

//    //SOP启动错误处理---------------------------------------------
//    private void errorSOP(int type){
////        switch (type){
////            case 1:
////                Toast.makeText(this,"串口打开失败！",Toast.LENGTH_SHORT).show();
////                break;
////            case 10:
////                Toast.makeText(this,"录波记录文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
////                break;
////            case 11:
////                Toast.makeText(this,"故障记录文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
////                break;
////            case 12:
////                Toast.makeText(this,"实时采样文件夹不存在或创建失败！",Toast.LENGTH_SHORT).show();
////                break;
////            case 15:
////                Toast.makeText(this,"故障记录文件不存在或创建失败！",Toast.LENGTH_SHORT).show();
////                break;
////        }
////        Toast.makeText(this,"初始化失败，即将重启！",Toast.LENGTH_SHORT).show();
//    }


    /**
     * 底层广播
     * 接收其他层无法处理的事件，如有些类无法持有 context 则通过发送广播的方式交由此层 来处理 {@link Toast}
     * 处理其他层触发的通讯事件，本系统不允许其他层持有 {@link SerialPortHelper},所以若其他类需要通讯，都以广播的方式通此层处理
     */
    public class MyBaseActivity_Broad extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent){

            //Toast事件
            String fragmentToast = intent.getStringExtra("fragmentToast");
            if (fragmentToast != null){
                Toast.makeText(mContext,fragmentToast,Toast.LENGTH_SHORT).show();
            }

            //遥调事件
            String[] test = intent.getStringArrayExtra("UserSet");
            if (test != null ){
                CorrectEvent(test);
            }

        }
    }

    /**
     * 遥调事件处理
     * 其他类无法处理的遥调，从{@link MyBaseActivity_Broad}接收到后，在这里进行分类处理
     * 最后将通讯内容填充到 {@link #iCorrect {寄存器地址 ， 值}} ，并置位 {@link #_isCorrect} ,程序将会在 {@link #task} 线程内处理
     * @param sData { 名称 ， 值 ， 类型}
     */
    private void CorrectEvent(String[] sData){
        int[] iData = new int[2];
        if (sData.length == 3){

            //遥调、遥控
            if (sData[2].equals("校准")||sData[2].equals("遥控")||sData[2].equals("对时")||sData[2].equals("启动")||sData[2].equals("停止")){

                sCorrect = "";

                //地址转换
                switch (sData[0]){
                    case "U相输出电压:":
                        iData[0] = 0;
                        break;
                    case "V相输出电压:":
                        iData[0] = 1;
                        break;
                    case "W相输出电压:":
                        iData[0] = 2;
                        break;
                    case "U相输出电流:":
                        iData[0] = 3;
                        break;
                    case "V相输出电流:":
                        iData[0] = 4;
                        break;
                    case "W相输出电流:":
                        iData[0] = 5;
                        break;
                    case "R相输入电压:":
                        iData[0] = 7;
                        break;
                    case "S相输入电压:":
                        iData[0] = 8;
                        break;
                    case "T相输入电压:":
                        iData[0] = 9;
                        break;
                    case "电容容量:":
                        iData[0] = 11;
                        break;
                    case "秒:":
                        iData[0] = 14;
                        sCorrect = sData[1];
                        sData[1] = "1";
                        break;
                    case "系统模式:":
                        iData[0] = 18;
                        break;
                    case "补偿使能:":
                        iData[0] = 19;
                        break;
                }

                //数据填充
                if (!sData[1].equals("")){
                    iData[1] =  Integer.parseInt(sData[1]);
                }else {
                    iData[1] = 0;
                }

                if (sData[0].equals("启动补偿:")){
                    iData[0] = 17;
                    iData[1] = 0x4000;
                }else if (sData[0].equals("停止补偿:")){
                    iData[0] = 17;
                    iData[1] = 0x8000;
                }

                System.arraycopy(iData,0,iCorrect,0,2);
                _isCorrect = true;

            } else if (sData[2].equals("开关")){

                SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
                boolean eventFlag = false;
                boolean valueFlag = false;

                switch (sData[0]){
                    case "告警提示:":
                        if (Integer.parseInt(sData[1]) == 1){ valueFlag = true;}
                        wStateData.putBoolean("is_SystemBeep",valueFlag);
                        eventFlag = true;
                        break;
                    case "调试模式:":
                        if (Integer.parseInt(sData[1]) == 1){ valueFlag = true;}
                        wStateData.putBoolean("is_SystemDebug",valueFlag);
                        eventFlag = true;
                        break;
//                    case "Loge输出:":
//                        if (Integer.parseInt(sData[1]) == 1){ valueFlag = true;}
//                        wStateData.putBoolean("is_SystemOutLoge",valueFlag);
//                        eventFlag = true;
//                        break;
                    case "初始化系统:":
//                        Toast.makeText(this,"will be init this system,delete this system all data!",Toast.LENGTH_SHORT).show();
//                        if (Integer.parseInt(sData[1]) == 1){ initSystemSOP();}
                        if (Integer.parseInt(sData[1]) == 1){ OnDeleteEvent("系统","初始化系统 \r\n 系统内的数据将会被全部删除，确认执行初始化？");}
                        break;
                    case "初始化录波:":
                        if (Integer.parseInt(sData[1]) == 1){ OnDeleteEvent("录波","初始化录波记 \r\n 系统内的录波数据将会被全部删除，确认执行初始化？");}
                        break;
                    case "初始化故障:":
                        if (Integer.parseInt(sData[1]) == 1){ OnDeleteEvent("故障","初始化故障记录 \r\n 系统内的故障数据将会被全部删除，确认执行初始化？");}
                        break;
                    case "退出程序:":
                        if (Integer.parseInt(sData[1]) == 1 ){
                            System.exit(0);
                        }
                        break;
                }

                if (eventFlag){
                    if (!wStateData.commit()){
                        wStateData.commit();
                    }
                }

            }
        }
    }

    /** 初始化事件 */
    private void OnDeleteEvent(final String type ,String tag){
        new AlertDialog.Builder(this).setTitle(tag)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (type){
                            case "系统":
                                initSystem();
                                pauseSOP();
                                break;
                            case "录波":
                                DeleteRecord();
                                pauseSOP();
                                break;
                            case "故障":
                                DeleteFault();
                                pauseSOP();
                                break;
                        }
                        Toast.makeText(mContext,type + "记录已删除",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    /** 初始化系统 */
    private void initSystem(){
        //pauseSOP();

        if (!DeleteFault())DeleteFault();
        if (!DeleteRecord())DeleteRecord();

        if (SystemFunc.checkFileExist(systemPath))SystemFunc.deleteFile(systemPath);
        if (SystemFunc.checkFileExist(sharedPrefsPath))SystemFunc.deleteFile(sharedPrefsPath);

        SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
        wStateData.putBoolean("is_SystemBeep",true);
        wStateData.putBoolean("is_SystemDebug",false);
        if (wStateData.commit() ) wStateData.commit();

        Toast.makeText(mContext,"系统初始化，即将重启",Toast.LENGTH_SHORT).show();
//        SystemFunc.restart(mContext);
    }

    /** 删除录波相关文件 */
    private boolean DeleteRecord(){
        if (SystemFunc.checkFileExist(recordPath)) {
            SystemFunc.deleteFile(recordPath);
        }

        SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
        SharedPreferences.Editor wAlarmData = mContext.getSharedPreferences("AlarmData",MODE_PRIVATE).edit();
        SharedPreferences rRealData = mContext.getSharedPreferences("RealData", 0);
//        SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);

        wStateData.putInt("i_OldSagSite",rRealData.getInt("i_NewSagSite",0));
        wStateData.putInt("i_RecordAddress_2",0);
        wStateData.putBoolean("is_RecordFlag",false);
        wAlarmData.putInt("i_RecordTime",0);


        if (wStateData.commit() || wAlarmData.commit()){
            wStateData.commit();
            wAlarmData.commit();
        }

        if (!SystemFunc.checkFileExist(recordPath)){SystemFunc.createFile(recordPath);}
        return true;
//        Toast.makeText(mContext,"删除录波记录成功，即将重启",Toast.LENGTH_SHORT).show();
//        SystemFunc.restart(mContext);
    }

    /** 删除故障相关文件 */
    private boolean DeleteFault(){
        SharedPreferences.Editor wAlarmData = mContext.getSharedPreferences("AlarmData",MODE_PRIVATE).edit();

        for (int i = 3; i < 12; i++) {
            wAlarmData.putBoolean("_isYxError_"+i,false);
        }
        wAlarmData.putInt("i_AlarmTime",0);
        if (wAlarmData.commit())wAlarmData.commit();

        if (SystemFunc.checkFileExist(faultPath)) SystemFunc.deleteFile(faultPath);
        if (!SystemFunc.checkFileExist(faultPath)) SystemFunc.createFile(faultPath);
        if (!SystemFunc.checkFileExist(faultName)){
            List<List<Object>> mList = new ArrayList<>();
            List<Object> mRow = new ArrayList<>();
            mRow.add("编号");
            mRow.add("事件类型");
            mRow.add("开始时间");
            mRow.add("结束时间");
            mList.add(mRow);
            SystemFunc.createExcel(faultName,mList);
        }
        return true;
//        Toast.makeText(mContext,"删除故障记录成功，即将重启",Toast.LENGTH_SHORT).show();
//        SystemFunc.restart(mContext);
    }


    /** 打开串口 */
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

    /** 关闭串口 */
    private void closeCom(SerialPortHelper ComPort){
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

    /** 发送数据 */
    private void sendPortData(SerialPortHelper ComPort /*String type*/ ){
        if (ComPort != null && ComPort.getIsOpen()){
//            switch (type){
//                case "Hex":
////                    ComPort.sendHex(sOutData);
//                    break;
//                case "Txt":
////                    ComPort.sendTxt(sOutData);
//                    break;
//                case "bArr":
//                    ComPort.send(bOutData);
//                    break;
//            }
            ComPort.send(bOutData);
        }
    }

    /** 实例化串口工具 */
    private class SerialControl extends SerialPortHelper{
        private SerialControl(){
        }
    }

    private class UpSerialControl extends UpSerialPortHelper {
        private UpSerialControl(){
        }
    }

    /**
     * 监听用户操作触摸屏
     * 1、标志用户是否有操作
     * 2、点击空白处时执行隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                //用户操作计时
                _isActionFlag = true;
                iNotAction = 0;

                //隐藏虚拟键盘
                View view = getCurrentFocus();
                if (isShouldHideKeyboard(view,event)) hideKeyBoard(view.getWindowToken());

                break;
            case MotionEvent.ACTION_UP:
                _isActionFlag = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 判断用户点击位置是否处在EditText内
     * @return true 不在EditText内 ，false 在EditText内
     */
    private boolean isShouldHideKeyboard(View v,MotionEvent event){
        if (v instanceof EditText){
            int[] l = {0,0};
            v.getLocationInWindow(l);
            int left = l[0],
                    right = left + v.getWidth(),
                    top = l[1],
                    bottom = top + v.getHeight();
            return  !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    /**
     * 隐藏虚拟键盘
     */
    private void hideKeyBoard(IBinder token){
        if (token != null){
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im !=null) im.hideSoftInputFromWindow(token,InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    /**
     * 底层主线程
     * 定时处理下位机通讯
     * 本系统无论什么层触发通讯后，最终都会传递到这里，按队列依次进行通讯
     * 这里只是处理一些标准位和一些状态字，最后实际下发通讯都是交由 {@link SerialPortHelper} 类处理
     */
    Handler handler = new Handler();
    Runnable task = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this,500);
            if (downCom.getIsOpen()){

                Log.e(TAG, "run: mian thread"  );

                //无操作计时
                if (!_isActionFlag && iNotAction < 9999) iNotAction ++;
//                //通讯错误计次
//                boolean _isCommFlag = downCom.getisCommFlag();
//                if (!_isCommFlag){
//                    if (iCommError < 99){iCommError ++;}
//                    downCom.deleteErrRealData();
//                }else {
////                    SystemFunc.Beep(getApplicationContext(),false);
//                    iCommError = 0;
//                }

                //无通讯重启
                SharedPreferences rStateData = mContext.getSharedPreferences("StateData",0);
                SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();

                int iCommError = rStateData.getInt("i_CommError",0);
                if (iCommError < 99) iCommError = iCommError + 1;
                if (iCommError > 40 && iCommError < 59 && ((60-iCommError)%4) == 0 ){
                    Toast.makeText(mContext,"通讯长时间无响应，若持续异常，系统将会在"+((60-iCommError)/4)+"后重启",Toast.LENGTH_SHORT).show();
                }
                wStateData.putInt("i_CommError",iCommError);
                if (wStateData.commit())wStateData.commit();
                //30s无有效通讯，重启设备
                if (iCommError > 60){
                    wStateData.putInt("i_CommError",0);
                    if (wStateData.commit())wStateData.commit();
                    SystemFunc.restart(mContext);
                }

//                if (iCommError > 5){Beep(mContext,true);}else {Beep(mContext,false);}

                //写入
//                SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
////                SharedPreferences.Editor wRealData = mContext.getSharedPreferences("RealData",MODE_PRIVATE).edit();
//
//                //读取
//                SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);
//                SharedPreferences rRealData = mContext.getSharedPreferences("RealData", 0);
                SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData",0);
                //告警
                boolean[] _isNewYX = new boolean[12];
                for (int i = 3; i < 12 ; i++) {
                    _isNewYX[i] = rAlarmData.getBoolean("_isYxError_"+i,false);
                }
                is_BeepFlag = (_isNewYX[3] || _isNewYX[4] || _isNewYX[5] || _isNewYX[6] || _isNewYX[7] || _isNewYX[8] || _isNewYX[9] || _isNewYX[10] || _isNewYX[11] || (iCommError >= 40));
//                //判断是否需要读取录波
//                if ((rRealData.getInt("i_NewSagSite",0) != rStateData.getInt("i_OldSagSite",0)) && !rStateData.getBoolean("is_RecordFlag",false)){
//                    wStateData.putBoolean("is_RecordFlag",true);       //开始录波标志
//                    if (!wStateData.commit()){
//                        wStateData.commit();
//                    }
//                }
//
//                if (!rStateData.getBoolean("is_ComThreadRun",false)) {
//
//                    //需要读录波，且没有在读录波
//                    if (rStateData.getBoolean("is_RecordFlag", false) && !rStateData.getBoolean("is_ReadRecordFlag", false) && !_isCorrect) {
//
//                        bOutData = new byte[]{(byte) 0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x0D};
//
//                        iReadRecordError = 0;
//                        wStateData.putBoolean("is_ReadRecordFlag", true);
//                        bOutData[1] = (byte) 0x10;
//                        //地址1
//                        int recordAddress_1 = rStateData.getInt("i_OldSagSite", 0) + 256;
//                        bOutData[2] = MyFunc.InToByteArr(recordAddress_1)[2];
//                        bOutData[3] = MyFunc.InToByteArr(recordAddress_1)[3];
//                        //地址2
//                        int recordAddress_2 = rStateData.getInt("i_RecordAddress_2", 0) + 1;
//                        bOutData[4] = MyFunc.InToByteArr(recordAddress_2)[2];
//                        bOutData[5] = MyFunc.InToByteArr(recordAddress_2)[3];
//                        //CRC
//                        bOutData[6] = MyFunc.addCrc(bOutData);
////                        new ComThread().start();
//                        sendPortData(downCom);
//
//
//                    }//不需要读录波，或已经在处理录波
//                    else if ((!rStateData.getBoolean("is_RecordFlag", false) || rStateData.getBoolean("is_ReadRecordFlag", false)) && !_isCorrect) {
//
//                        bOutData = new byte[]{(byte) 0x7E, 0x03, 0x00, 0x00, 0x00, 0x14, 0x00, (byte) 0x0D};
//                        bOutData[6] = MyFunc.addCrc(bOutData);
////                        new ComThread().start();
//                        sendPortData(downCom);
//
//                    }//用户触发遥控、遥调
//                    else if (_isCorrect) {
//
//                        //地址
//                        if (iCorrect[0] == 14) {
//
//                            bOutData = new byte[]{(byte) 0x7E, 0x06, 0x00, (byte) 0x0E, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x0D};
//
//                            System.arraycopy(MyFunc.timeToBCD(sCorrect), 0, bOutData, 6, 6);
//
//                            //CRC
//                            bOutData[12] = MyFunc.addCrc(bOutData);
////                            new ComThread().start();
//                            sendPortData(downCom);
//
//                        } else {
//
//                            bOutData = new byte[]{(byte) 0x7E, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x0D};
//
//                            //寄存器地址
//                            bOutData[2] = MyFunc.InToByteArr(iCorrect[0])[2];
//                            bOutData[3] = MyFunc.InToByteArr(iCorrect[0])[3];
//
//                            //寄存器长度
//                            bOutData[5] = 0x01;
//
//                            //值
//                            bOutData[6] = MyFunc.InToByteArr(iCorrect[1])[2];
//                            bOutData[7] = MyFunc.InToByteArr(iCorrect[1])[3];
//
//                            //CRC
//                            bOutData[8] = MyFunc.addCrc(bOutData);
//
//                            System.out.print(Arrays.toString(bOutData));
////                            new ComThread().start();
//                            sendPortData(downCom);
//
//                            //复位遥调标志
//                            iCorrect = new int[2];
//                        }
//
//                        _isCorrect = false;
//
//                    }
//
//                    //防止遥测录波时通讯失败
//                    iReadRecordError++;
//                    if (iReadRecordError > 3) {
//                        iReadRecordError = 0;
//                        wStateData.putBoolean("is_ReadRecordFlag", false);
//                    }
//
////                wStateData.putBoolean("mComThreadRunning",mComThreadRunning);
//                    wStateData.commit();
//                }

            }
        }
    };

    //定时通讯子线程
    class ComThread extends Thread{
        @Override
        public void run(){
            Log.e(TAG, "run: com thread"  );
            if (downCom.getIsOpen()) sendPortData(downCom);
        }
    }

    class DownThread implements Runnable{
        @Override
        public void run() {
            while (ThreadRun && downCom.getIsOpen()) {

                Log.e(TAG, "run: down thread"  );

                //写入
                SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
//                SharedPreferences.Editor wRealData = mContext.getSharedPreferences("RealData",MODE_PRIVATE).edit();

                //读取
                SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);
                SharedPreferences rRealData = mContext.getSharedPreferences("RealData", 0);
//                SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData",0);
//                Log.e(TAG, "DeleteRecord: " +rStateData.getInt("i_OldSagSite",0) + "," + rRealData.getInt("i_NewSagSite",0) );

                //判断是否需要读取录波
                if ((rRealData.getInt("i_NewSagSite",0) != rStateData.getInt("i_OldSagSite",0))
                        && !rStateData.getBoolean("is_RecordFlag",false)){

                    wStateData.putBoolean("is_RecordFlag",true);
                    if (!wStateData.commit()) wStateData.commit();

                }

                if (!rStateData.getBoolean("is_ComThreadRun",false)) {

                    //需要读录波，且没有在读录波
                    if (rStateData.getBoolean("is_RecordFlag", false) && !rStateData.getBoolean("is_ReadRecordFlag", false) && !_isCorrect) {

                        bOutData = new byte[]{(byte) 0x7E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x0D};

                        iReadRecordError = 0;
                        wStateData.putBoolean("is_ReadRecordFlag", true);
                        bOutData[1] = (byte) 0x10;
                        //地址1
                        int recordAddress_1 = rStateData.getInt("i_OldSagSite", 0) + 256;
                        if (recordAddress_1 == 260) recordAddress_1 = 256;
                        bOutData[2] = MyFunc.InToByteArr(recordAddress_1)[2];
                        bOutData[3] = MyFunc.InToByteArr(recordAddress_1)[3];
                        //地址2
                        int recordAddress_2 = rStateData.getInt("i_RecordAddress_2", 0) + 1;
                        bOutData[4] = MyFunc.InToByteArr(recordAddress_2)[2];
                        bOutData[5] = MyFunc.InToByteArr(recordAddress_2)[3];
                        //CRC
                        bOutData[6] = MyFunc.addCrc(bOutData);
//                        new ComThread().start();
                        sendPortData(downCom);

                        iReadRecordError_2 ++;
                        if (iReadRecordError_2 > 5){

                            iReadRecordError_2 = 0;
                            if (SystemFunc.checkFileExist(rStateData.getString("s_RecordFileName",""))){
                                SystemFunc.deleteFile(rStateData.getString("s_RecordFileName",""));
                            }
                            wStateData.putInt("i_RecordAddress_1",0);           //复位当前录波地址1
                            wStateData.putInt("i_RecordAddress_2",0);           //复位当前录波地址2
                            wStateData.putBoolean("is_RecordFlag",false);       //复位开始录波标志
                            wStateData.putBoolean("is_ReadRecordFlag",false);   //复位开始录波在读标志
                            wStateData.putString("s_RecordFileName","");        //复位当前录波文件名
                        }


                    }//不需要读录波，或已经在处理录波
                    else if ((!rStateData.getBoolean("is_RecordFlag", false) || rStateData.getBoolean("is_ReadRecordFlag", false)) && !_isCorrect) {

                        iReadRecordError_2 = 0;
                        bOutData = new byte[]{(byte) 0x7E, 0x03, 0x00, 0x00, 0x00, 0x14, 0x00, (byte) 0x0D};
                        bOutData[6] = MyFunc.addCrc(bOutData);
//                        new ComThread().start();
                        sendPortData(downCom);

                    }//用户触发遥控、遥调
                    else if (_isCorrect) {

                        iReadRecordError_2 = 0;

                        //地址
                        if (iCorrect[0] == 14) {

                            bOutData = new byte[]{(byte) 0x7E, 0x06, 0x00, (byte) 0x0E, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x0D};

                            System.arraycopy(MyFunc.timeToBCD(sCorrect), 0, bOutData, 6, 6);

                            //CRC
                            bOutData[12] = MyFunc.addCrc(bOutData);
//                            new ComThread().start();
                            sendPortData(downCom);

                        } else {

                            bOutData = new byte[]{(byte) 0x7E, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x0D};

                            //寄存器地址
                            bOutData[2] = MyFunc.InToByteArr(iCorrect[0])[2];
                            bOutData[3] = MyFunc.InToByteArr(iCorrect[0])[3];

                            //寄存器长度
                            bOutData[5] = 0x01;

                            //值
                            bOutData[6] = MyFunc.InToByteArr(iCorrect[1])[2];
                            bOutData[7] = MyFunc.InToByteArr(iCorrect[1])[3];

                            //CRC
                            bOutData[8] = MyFunc.addCrc(bOutData);

                            System.out.print(Arrays.toString(bOutData));
//                            new ComThread().start();
                            sendPortData(downCom);

                            //复位遥调标志
                            iCorrect = new int[2];
                        }

                        _isCorrect = false;

                    }

                    //防止遥测录波时通讯失败
                    iReadRecordError++;
                    if (iReadRecordError > 3) {
                        iReadRecordError = 0;
                        wStateData.putBoolean("is_ReadRecordFlag", false);
                    }

//                wStateData.putBoolean("mComThreadRunning",mComThreadRunning);
                    wStateData.commit();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }//end run
    }

    //蜂鸣线程
    class BeepThread implements Runnable{
        @Override
        public void run(){
            while (ThreadRun) {
//                Log.e(TAG, "run: beep thread"  );
//                while (is_BeepFlag) {
//                    Log.e(TAG, "beep " );
                if (is_BeepFlag) SystemFunc.Beep(getApplicationContext(),500);
//                    try {
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
