package com.great.grt_vdc_t4200l.SerialPortHelp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.great.grt_vdc_t4200l.SystemFunc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android_serialport_api.SerialPort;

import static android.content.Context.MODE_PRIVATE;

/**
 * 串口管理类
 */
public class SerialPortHelper {

    //关联------------------------------------------------------------
    private Context mContext;

    //主要实例申明----------------------------------------------------
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    //读写线程申明----------------------------------------------------
    private ReadThread mReadThread;
    private SendThread mSendThread;

    //端口设置申明----------------------------------------------------
    private String sPortName = "/dev/ttyS2";
    private int iBaudRate = 38400;
    private boolean _isOpen = false;
    private int iDelay = 300;
    private byte[] _bLoopData = new byte[]{};

    //数据地址--------------------------------------------------------
    private int[] iYc = new int[14];
    private boolean[] _isYx = new boolean[8];
    private boolean[] _isYk = new boolean[2];
    private String sSystemTime;
    private boolean _isCommFlag = false;
    private int iCommErrTime = 0;

    private boolean _isTemp = false;

    //接收数据用------------------------------------------------------
    private int iReadLength = 0;
    private byte[] bBuffer = new byte[0];

    //excel默认表头---------------------------------------------------
//    private List<List<Object>> excel = new ArrayList<>();


    /**
     * 串口的几种实例化方法
     */
    //指定串口、指定波特率（int）的实例化
    public SerialPortHelper(String sPortName , int iBaudRate){
        this.sPortName = sPortName;
        this.iBaudRate = iBaudRate;
    }
    //默认实例化
    public SerialPortHelper(){
        this("/dev/ttyS2",38400);
    }
    // 指定串口实例化
    public SerialPortHelper(String sPortName){
        this(sPortName,38400);
    }
    //指定串口、指定波特率（String）的实例化
    public SerialPortHelper(String sPortName,String sBaudRate){
        this(sPortName,Integer.parseInt(sBaudRate));
    }

    /**
     * set串口数据
     */
    //设置环境
    public void setmContext(Context mContext){
        this.mContext = mContext;
    }
    //设置波特率（int）
    public boolean setBaudRate(int iBaud){
        if (_isOpen){
            return false;
        }else {
            iBaudRate = iBaud;
            return true;
        }
    }
    //设置波特率（String）
    public boolean setBaudRate(String sBaud){
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }
    //设置串口
    public boolean setPortName(String sPortName){
        if (_isOpen){
            return false;
        }else {
            this.sPortName = sPortName;
            return true;
        }
    }
    //设置延时时间
    public void setDelay(int iDelay) {
        this.iDelay = iDelay;
    }

    //设置发送数据（byte）
    public void setbyteLoopData(byte[] bLoopData){
        this._bLoopData = bLoopData;
    }
    //设置发送数据（txt）
    public void setTxtLoopData(String sTxt){
        this._bLoopData = sTxt.getBytes();
    }
    //设置发送数据（hex）
    public void setHexLoopData(String sHex){
        this._bLoopData = MyFunc.HexToByteArr(sHex);
    }

    //设置com状态
    public void  setisCommFlag(boolean _isCommFlag){
        this._isCommFlag = _isCommFlag;
    }

    /**
     * get串口数据
     */
    //获取当前波特率（int）
    public int getBaudRate(){
        return iBaudRate;
    }
    //获取当前串口
    public String getPortName(){
        return sPortName;
    }
    //获取串口开关标志
    public boolean getIsOpen(){
        return _isOpen;
    }
    //获取延时时间
    public int getDelay() {
        return iDelay;
    }

    //获取遥信
    public int[] getiTelemetry(){
        return iYc;
    }
    //获取遥测
    public boolean[] getisTelecommand(){
        return _isYx;
    }
    //获取遥控
    public boolean[] getisTelecontrol(){
        return _isYk;
    }
    //获取系统时间
    public String getSystemTime() {
        return sSystemTime;
    }

    //获取发送数据
    public byte[] getbyteLoopData(){
        return _bLoopData;
    }
    //获取com状态
    public boolean getisCommFlag(){
        return _isCommFlag;
    }

    /**
     * 操作串口
     */
    //打开串口
    public void open() throws SecurityException,IOException,InvalidParameterException{

        //实例化串口
        mSerialPort = new SerialPort(new File(sPortName),iBaudRate,0);

        //关联收发实例
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();

        //读取线程开始
        mReadThread = new ReadThread();
        mReadThread.start();

        //写入线程开始
        mSendThread = new SendThread();
        mSendThread.setSuspendFlag();
        mSendThread.start();

        //串口打开标志
        _isOpen = true;
    }
    //关闭串口
    public void close(){

        //关闭读线程
        if (mReadThread != null){
            mReadThread.interrupt();
        }

        //关闭串口
        if (mSerialPort != null){
            mSerialPort.close();
            mSerialPort = null;
        }

        //串口关闭标志
        _isOpen = false;
   }
    //开始发送
    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }
    //停止发送
    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }

    /**
     * 发送
     */
    //发送（byte）
    public void send(byte[] bOutArray){
        try {
            if (bOutArray.length > 0 ){
                switch (bOutArray[1]){
                    case 0x03:
                        iReadLength = 48;
                        break;
                    case 0x06:
                        iReadLength = 0;
                        break;
                    case 0x10:
                        if (MyFunc.ByteArrToInt(bOutArray,4) == 1){
                            iReadLength = 26;
                        }else {
                            iReadLength = 558;
                        }
                        break;
                }
                bBuffer = new byte[0];
                mOutputStream.write(bOutArray);
                _isCommFlag = false;
                Log.e("串口消息", "发送数据：" + MyFunc.ByteArrToHex(bOutArray));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
   }
    //发送（hex）
    public void sendHex(String sHex){
        byte[] bOutArray = MyFunc.HexToByteArr(sHex);
        send(bOutArray);
    }
    //发送（txt）
    public void sendTxt(String sTxt){
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }

    //发送线程
    private class SendThread extends Thread{
        public boolean suspendFlag = true;
        @Override
        public void run(){
            super.run();
            while (!isInterrupted()){
                synchronized (this){
                    while (suspendFlag){
                        try {
                            wait();
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
                send(getbyteLoopData());
                try {
                    Thread.sleep(iDelay);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        public void setSuspendFlag(){
            this.suspendFlag = true;
        }

        public synchronized void setResume(){
            this.suspendFlag = false;
            notify();
        }
    }

    /**
     * 接收
     */
    private class ReadThread extends Thread{
        @Override
        public void run(){
            super.run();
            while (!isInterrupted()){
                try {
                    if (mInputStream == null) return;

                    //由于接收缓存区无法一次读完指定的byte，所以每次只读接收缓存区内的2位，最后统一处理粘包
                    byte[] buffer1 = new byte[2];
                    int size = mInputStream.read(buffer1);

                    //粘包，整理数据
                    byte[] buffer2 = new byte[size];
                    System.arraycopy(buffer1,0,buffer2,0,size);                     //先将从接收缓存区读取到的数据copy
                    byte[] buffer3 = new byte[bBuffer.length];
                    System.arraycopy(bBuffer,0,buffer3,0,bBuffer.length);           //再将原来已经缓存好的数据copy
                    bBuffer =  new byte[buffer3.length+buffer2.length];
                    bBuffer = MyFunc.addByteArr(buffer3,buffer2);                                     //最后将新的数据添加到原来数据的末尾

                    //当粘合后的数据长度等于上次下发报文中所要求时，进入接收数据处理流程
                    if ((bBuffer.length == iReadLength)&& _isOpen){
                        if (mContext != null){
                            Log.e("串口消息", "接收数据：" + MyFunc.ByteArrToHex(bBuffer));
                            readData(bBuffer.length,bBuffer);
                        }else {
                            Log.e("串口消息","环境未来准备好");
                        }
                    }else {
                        _isCommFlag = false;
                    }
//                    try {
//                        Thread.sleep(300);
//                    }catch (InterruptedException e){
//                        e.printStackTrace();
//                    }
                }catch (Throwable e){
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 接收处理
     */
    private void readData(int size,byte[] buffer){
        String temp = MyFunc.ByteArrToHex(buffer);

        //写入Shared Preferences
        SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
        SharedPreferences.Editor wRealData = mContext.getSharedPreferences("RealData",MODE_PRIVATE).edit();
        SharedPreferences.Editor wAlarmData = mContext.getSharedPreferences("AlarmData",MODE_PRIVATE).edit();

        //读取Shared Preferences
        SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);
//        SharedPreferences rRealData = mContext.getSharedPreferences("RealData", 0);
        SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData",0);

        //第一次筛选接收包
        if (size > 0 && size< 600 && buffer[0] == 0x7E && buffer[size-1] == 0x0D){

            //匹配CRC
            if (MyFunc.addCrc(buffer) == buffer[size - 2]){

                if (!_isCommFlag){
                    _isCommFlag = true;//通讯成功标志
                }

                //根据功能码分类处理接受包
                switch (buffer[1]){
                    case 0x03:
                        //赛选03功能码，数据帧格式
                        if (size == 48 && MyFunc.ByteArrToInt(buffer,2) == 0 && MyFunc.ByteArrToInt(buffer,4) == 0x0028){

                            //遥测
                            int k = 6;
                            for (int i = 0; i < 13 ; i++) {
                                iYc[i] = MyFunc.ByteArrToInt(buffer,k);
                                k += 2;
                            }

                            //遥信
                            _isYx = MyFunc.ByteToBoolArr(buffer[41]);

                            //遥控
                            _isYk[0] = MyFunc.ByteToBool(buffer[43],0);
                            _isYk[1] = MyFunc.ByteToBool(buffer[45],0);

                            //时间
                            byte[] bTime = new byte[6];
                            System.arraycopy(buffer,34,bTime,0,6);
                            sSystemTime = MyFunc.BCDArrToTime(bTime,"SystemTime");

                            //将数据写入Shared Preferences
                            //遥测
                            wRealData.putInt("i_Uv",iYc[0]);                             //U相电压
                            wRealData.putInt("i_Vv",iYc[1]);                             //V相电压
                            wRealData.putInt("i_Wv",iYc[2]);                             //W相电压
                            wRealData.putInt("i_Ua",iYc[3]);                             //U相电流
                            wRealData.putInt("i_Va",iYc[4]);                             //V相电流
                            wRealData.putInt("i_Wa",iYc[5]);                             //W相电流

                            //频率
                            float fHz = (float)iYc[6]/10;
                            wRealData.putInt("i_Hz",iYc[6]);
                            wRealData.putFloat("f_Hz",fHz);

                            wRealData.putInt("i_Rv",iYc[7]);                             //R相电压
                            wRealData.putInt("i_Sv",iYc[8]);                             //S相电压
                            wRealData.putInt("i_Tv",iYc[9]);                             //T相电压
                            wRealData.putInt("i_SagTime",iYc[10]);                       //录波次数
                            wRealData.putInt("i_Capv",iYc[11]);                          //电容电压

                            //电容容量
                            int iCapAh = 0;
                            if(iYc[11] > 232 ) iCapAh = ((iYc[11]*100)-23200)/142;
                            wRealData.putInt("i_CapAh",iCapAh);

                            wRealData.putInt("i_NewSagSite",iYc[12]);                    //当前录波位置
                            wRealData.putInt("i_SagSum",iYc[13]);                        //录波总数

                            //遥信
                            wRealData.putBoolean("is_RechargeFlag",_isYx[7]);          //充电状态
                            wRealData.putBoolean("is_CompensateFlag",_isYx[6]);        //补偿状态
                            wRealData.putBoolean("is_InAlarm",_isYx[4]);               //输入异常
                            wRealData.putBoolean("is_OutOC",_isYx[3]);                 //输出过流
                            wRealData.putBoolean("is_OutRl",_isYx[2]);                 //输出短路
                            wRealData.putBoolean("is_AhLose",_isYx[1]);                //容量失效
                            wRealData.putBoolean("is_ComError",_isYx[0]);              //通讯异常

                            //遥控
                            wRealData.putBoolean("is_SystemMode",_isYk[0]);            //系统模式
                            wRealData.putBoolean("is_CompensateEnabled",_isYk[1]);     //补偿使能


                            //系统时间
                            wRealData.putString("s_SystemTime",sSystemTime);                    //下位机系统时间
                            if (!_isTemp) {
                                SystemFunc.setNewTime(sSystemTime);
                                _isTemp = true;
                            }
                            //处理告警
                            alarmHand(_isYx);

                            iCommErrTime = 0;

                        }else {
                            Log.e("串口信息","0x03回送帧出错，数据内容："+temp);
                            _isCommFlag = false; //通讯失败标志
                        }
                        break;
                    case 0x10:
                        //根据长度处理10功能码
                        if (size == 26){

                            //Excel默认表头
                            List<List<Object>> excel = new ArrayList<>();
                            List<Object> excelHead = new ArrayList<>();
//                            excel.clear();
                            excelHead.add("R相输入电压");
                            excelHead.add("S相输入电压");
                            excelHead.add("T相输入电压");
                            excelHead.add("U相输出电压");
                            excelHead.add("V相输出电压");
                            excelHead.add("W相输出电压");
                            excelHead.add("U相输出电流");
                            excelHead.add("V相输出电流");
                            excelHead.add("W相输出电流");
                            excel.add(excelHead);

                            //处理10功能码，01地址的数据
                            int eventType = MyFunc.ByteArrToInt(buffer,6);                      //事件类型
                            int eventRow = MyFunc.ByteArrToInt(buffer,8);                       //事件内容
                            int eventTime = MyFunc.ByteArrToInt(buffer,10);                     //事件持续时长
                            //事件开始时间
                            byte[] bTime = new byte[6];
                            System.arraycopy(buffer,12,bTime,0,6);
                            String eventStartTime  = MyFunc.BCDArrToTime(bTime,"EventTime");

                            //录波记录编号
                            int recordNum = rAlarmData.getInt("i_RecordTime",0) + 1;

                            //文件名
                            String fileName = mContext.getFilesDir().getPath() + "/record_log/"+recordNum+"_"+eventType+"_"+eventRow+"_"+eventTime+"_"+eventStartTime+".xls";

                            //尝试创建文件
                            if(SystemFunc.createExcel(fileName,excel)){
                                wStateData.putInt("i_RecordAddress_2",1);                               //写入当前录波地址，读完当前录波后，此地址清零
                                wStateData.putString("s_RecordFileName",fileName);                      //写入当前录波数据存储的文件名
                            }

                        }else if(size == 558) {

                            //读取接收包携带的录波数据地址
                            int[] eventAddress = new int[2];
                            eventAddress[0] = MyFunc.ByteArrToInt(buffer,2);
                            eventAddress[1] = MyFunc.ByteArrToInt(buffer,4);

                            //读取当前录波文件名
                            String fileName = rStateData.getString("s_RecordFileName","");

                            //防止文件路径空指针
                            if (fileName.contains(".xls")) {

                                //数据存储数组
                                List<List<Object>> recordEvent = new ArrayList<>();
                                List<Object> eventData = new ArrayList<>();

                                //读取录波数据的起始地址，用于循环
                                int dataAddress = 6;

                                //回送的录波数据地址处于第一列时（即回送是R相电压数据时），每次数据以追加的方式填入Excel，其余暂无数据的列填0，用于创建指针
                                if (eventAddress[1] <= 7) {

                                    //直接将byte转int后写入list
                                    for (int i = 0; i < 275; i++) {
//                                        eventData.add(MyFunc.ByteArrToInt(buffer, dataAddress));

                                        //为保证数据在范围内，对数据做限制处理
                                        int tempData = MyFunc.ByteArrToInt(buffer, dataAddress);
                                        if (tempData > 1000 || tempData < -1000){ tempData = 0;}
                                        eventData.add(tempData);

                                        for (int j = 0; j < 8; j++) {
                                            eventData.add(0);
                                        }

                                        recordEvent.add(eventData);
                                        eventData = new ArrayList<>();
                                        dataAddress = dataAddress + 2;
                                    }

                                    //写入数值，写入成功后，将录波地址+1
                                    if (SystemFunc.addExcelData(fileName,recordEvent)){
                                        wStateData.putInt("i_RecordAddress_2",(rStateData.getInt("i_RecordAddress_2",1) + 1));
                                    }

                                    //复位录波在读的标志
                                    wStateData.putBoolean("is_ReadRecordFlag",false);

                                }// 第二列开始，以修改数据的方式，指定起始行，指定修改长度的方式，修改数据
                                else if (eventAddress[1] <= 55) {

                                    //算出行指针
                                    int row_index = (((eventAddress[1]-2)%6)*275)+1;
                                    //算出列指针
                                    int col_mod = (eventAddress[1]-13)%6;
                                    if (col_mod > 0){col_mod = 1;}else {col_mod = 0;}
                                    int col_index =((eventAddress[1]-13)/6) + col_mod + 1;

                                    //将数值存入缓存
                                    for (int i = 0; i < 275; i++) {
//                                        eventData.add(MyFunc.ByteArrToInt(buffer, dataAddress));

                                        //为保证数据在范围内，对数据做限制处理
                                        int tempData = MyFunc.ByteArrToInt(buffer, dataAddress);
                                        if (tempData > 1000 || tempData < -1000){ tempData = 0;}
                                        eventData.add(tempData);

                                        dataAddress = dataAddress + 2;
                                    }

                                    //写入数值，写入成功后，将录波地址+1
                                    if (SystemFunc.alterExcelDatas(fileName,row_index,col_index,eventData)){

                                        wStateData.putInt("i_RecordAddress_2",(rStateData.getInt("i_RecordAddress_2",1) + 1));

                                        //当一条录波完全读完后，复位相关标志位
                                        if (eventAddress[1] == 55){

                                            wStateData.putInt("i_RecordAddress_1",0);           //复位当前录波地址1
                                            wStateData.putInt("i_RecordAddress_2",0);           //复位当前录波地址2
                                            wStateData.putBoolean("is_RecordFlag",false);       //复位开始录波标志
                                            wStateData.putString("s_RecordFileName","");        //复位当前录波文件名

                                            //上位机录波地址往前移动
                                            int oldSagSite = rStateData.getInt("i_OldSagSite",0);
                                            if (oldSagSite == 3){ oldSagSite = 0; }else{ oldSagSite = oldSagSite + 1; }
                                            wStateData.putInt("i_OldSagSite",oldSagSite);

                                            //录波记录数+1
                                            wAlarmData.putInt("i_RecordTime",(rAlarmData.getInt("i_RecordTime",0)+1));
                                        }
                                    }

                                    //复位录波在读的标志
                                    wStateData.putBoolean("is_ReadRecordFlag",false);

                                }
                            }else {
                                Log.e("串口信息","没有找到对应的EXCEL文件");
                                _isCommFlag = false; //通讯失败标志
                            }

                        }else {
                            Log.e("串口信息","0x10回送帧出错，数据内容："+temp);
                            _isCommFlag = false; //通讯失败标志
                        }
                        break;
                    default:
                        Log.e("readData: " ,""+buffer[2] );
                        break; //通讯失败标志
                }
            }else {
                Log.e("串口信息","数据校验错误，数据内容："+ temp +"CRC："+ MyFunc.addCrc(buffer));
                _isCommFlag = false; //通讯失败标志
            }

        }else {
            Log.e("串口信息","数据帧错误，数据内容："+temp);
            _isCommFlag = false; //通讯失败标志

        }

        if (!_isCommFlag && iCommErrTime < 50) iCommErrTime++;

        //03功能码通讯是失败时复位一些实时数据
        if(buffer[1] == 0x03 && iCommErrTime > 5 && _isYx[0]){
            deleteErrRealData();
        }

        //写入通讯标志
        wStateData.putBoolean("is_CommFlag",_isCommFlag);

        //统一提交Shared Preferences
        if (!wRealData.commit() & !wStateData.commit() & !wAlarmData.commit()){
            wRealData.commit();
            wStateData.commit();
            wAlarmData.commit();
        }
    }

    /**
     * 告警处理
     */
    private void alarmHand(boolean[] _isYxError){

        //读写SharePreferences
        SharedPreferences.Editor wAlarmData = mContext.getSharedPreferences("AlarmData",MODE_PRIVATE).edit();
        SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData", 0);

        //文件路径
        String faultPath = mContext.getFilesDir().getPath()+"/fault_log/";
        String faultName = faultPath + "fault_record.xls";

//        List<List<Object>> alarmContent;
//        List<Object> alarmRow;

        //循环处理所有的遥信信号（比较耗时）
        for (int i = 0; i < 5; i++) {

            if (_isYxError[i] && !rAlarmData.getBoolean("_isYxError_"+i,false)){

                List<List<Object>> alarmContent = new ArrayList<>();
                List<Object> alarmRow = new ArrayList<>();

                alarmRow.add(rAlarmData.getInt("i_AlarmTime",0)+1);
                switch (i){
                    case 4:
                        alarmRow.add("输入异常");
                        break;
                    case 3:
                        alarmRow.add("输出过流");
                        break;
                    case 2:
                        alarmRow.add("输出短路");
                        break;
                    case 1:
                        alarmRow.add("容量失效");
                        break;
                    case 0:
                        alarmRow.add("通讯异常");
                        break;
                }
                alarmRow.add(SystemFunc.getNewTime());
                alarmRow.add("N/A");
                alarmContent.add(alarmRow);

                if (SystemFunc.addExcelData(faultName,alarmContent)){
                    wAlarmData.putBoolean("_isYxError_"+i,true);
                    wAlarmData.putString("i_YxError_"+i+"_Num",(rAlarmData.getInt("i_AlarmTime",0)+1+""));
                    wAlarmData.putInt("i_AlarmTime",rAlarmData.getInt("i_AlarmTime",0)+1);
                    if (!wAlarmData.commit())wAlarmData.commit();
                }

            }else if (!_isYxError[i] && rAlarmData.getBoolean("_isYxError_"+i,false)){

                if (SystemFunc.alterExcelData(faultName,rAlarmData.getString("i_YxError_"+i+"_Num",""),SystemFunc.getNewTime())){
                    wAlarmData.putBoolean("_isYxError_"+i,false);
                    wAlarmData.putString("i_YxError_"+i+"_Num","");
                    wAlarmData.commit();
                }
            }

        }

//        //输入异常
//        if (_isYxError[3] && !rAlarmData.getBoolean("_isYxError_3",false)){
//
//            alarmRow.add(rAlarmData.getInt("i_AlarmTime",0) + 1);
//            alarmRow.add("输入异常");
//            alarmRow.add(SystemFunc.getNewTime());
//            alarmRow.add("");
//            alarmContent.add(alarmRow);
//
//            if (SystemFunc.addExcelData(faultName,alarmContent)){
//                wAlarmData.putBoolean("_isYxError_3",true);
//                wAlarmData.putString("i_YxError_3_Num",(rAlarmData.getInt("i_AlarmTime",0)+1+""));
//                wAlarmData.putInt("i_AlarmTime",rAlarmData.getInt("i_AlarmTime",0)+1);
//            }
//
//        }else if (!_isYxError[3] && rAlarmData.getBoolean("_isYxError_3",false)){
//            if (SystemFunc.alterExcelData(faultName,rAlarmData.getString("i_YxError_3_Num",""),SystemFunc.getNewTime())){
//                wAlarmData.putBoolean("_isYxError_3",false);
//                wAlarmData.putString("i_YxError_3_Num","");
//            }
//        }
//
//        wAlarmData.commit();
    }

    public void deleteErrRealData(){

        SharedPreferences.Editor wRealData = mContext.getSharedPreferences("RealData",MODE_PRIVATE).edit();

        wRealData.putInt("i_Uv",iYc[0]);                             //U相电压
        wRealData.putInt("i_Vv",iYc[1]);                             //V相电压
        wRealData.putInt("i_Wv",iYc[2]);                             //W相电压
        wRealData.putInt("i_Ua",iYc[3]);                             //U相电流
        wRealData.putInt("i_Va",iYc[4]);                             //V相电流
        wRealData.putInt("i_Wa",iYc[5]);                             //W相电流
        wRealData.putInt("i_Hz",iYc[6]);                             //频率
        wRealData.putFloat("f_Hz",(float) 0.0);
        wRealData.putInt("i_Rv",iYc[7]);                             //R相电压
        wRealData.putInt("i_Sv",iYc[8]);                             //S相电压
        wRealData.putInt("i_Tv",iYc[9]);                             //T相电压
        wRealData.putInt("i_Capv",iYc[11]);                          //电容电压
        wRealData.putInt("i_CapAh",0);           //电容容量

        //统一提交Shared Preferences
        if (!wRealData.commit()) wRealData.commit();


    }

}
