package com.great.grt_vdc_t4200l.SerialPortHelp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.great.grt_vdc_t4200l.SerialPortHelp.bean.ComBean;
import com.great.grt_vdc_t4200l.SystemFunc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.InvalidParameterException;
import android_serialport_api.SerialPort;

import static android.content.Context.MODE_PRIVATE;

public class SerialPortHelper {

    private Context mContext;

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private ReadThread mReadThread;
    private SendThread mSendThread;

    private String sPortName = "/dev/ttyS2";
    private int iBaudRate = 38400;
    private boolean _isOpen = false;
    private int iDelay = 300;
    private byte[] _bLoopData = new byte[]{};

    //数据地址----------------------------------------------------
    private int[] iYc = new int[14];
    private boolean[] _isYx = new boolean[8];
    private boolean[] _isYxFlag = new boolean[8];
    private boolean[] _isYk = new boolean[2];
    private String sSystemTime;
    private boolean _isCommFlag = false;


    //指定串口、指定波特率的实例化----------------------------------------------------
    public SerialPortHelper(String sPortName , int iBaudRate){
        this.sPortName = sPortName;
        this.iBaudRate = iBaudRate;
    }
    //默认值实例化----------------------------------------------------
    public SerialPortHelper(){
        this("/dev/ttyS2",38400);
    }
    //指定串口的实例化----------------------------------------------------
    public SerialPortHelper(String sPortName){
        this(sPortName,38400);
    }
    //指定串口、指定波特率的实例化----------------------------------------------------
    public SerialPortHelper(String sPortName,String sBaudRate){
        this(sPortName,Integer.parseInt(sBaudRate));
    }

    //打开串口----------------------------------------------------
    public void open() throws SecurityException,IOException,InvalidParameterException{

        mSerialPort = new SerialPort(new File(sPortName),iBaudRate,0);

        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();

        mReadThread = new ReadThread();
        mReadThread.start();

        mSendThread = new SendThread();
        mSendThread.setSuspendFlag();
        mSendThread.start();

        _isOpen = true;

    }
    //关闭串口----------------------------------------------------
   public void close(){

        if (mReadThread != null){
            mReadThread.interrupt();
        }

        if (mSerialPort != null){
            mSerialPort.close();
            mSerialPort = null;
        }

        _isOpen = false;
   }

    //发送----------------------------------------------------
   public void send(byte[] bOutArray){
        try {
            mOutputStream.write(bOutArray);
        }catch (IOException e){
            e.printStackTrace();
        }
   }
    //Hex格式发送----------------------------------------------------
    public void sendHex(String sHex){
        byte[] bOutArray = MyFunc.HexToByteArr(sHex);
        send(bOutArray);
    }
    //Txt格式发送----------------------------------------------------
    public void sendTxt(String sTxt){
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }

    //设置上下文----------------------------------------------------
    public void setmContext(Context mContext){
        this.mContext = mContext;
    }
    //接收线程----------------------------------------------------
    private class ReadThread extends Thread{
        @Override
        public void run(){
            super.run();
            while (!isInterrupted()){
                try {
                    if (mInputStream == null) return;
                    byte[] buffer = new byte[512];
                    int size = mInputStream.read(buffer);
                    Log.e("串口消息",size+"");
                    if (size > 0 && _isOpen){
//                        ComBean comRecData = new ComBean(sPortName,buffer,size)
                        if (mContext != null){
                            readData(size,buffer);
                        }else {
                            Log.e("串口消息","环境未来准备好");
                        }
                    }
                    try {
                        Thread.sleep(300);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
    //发送线程----------------------------------------------------
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

    //获取波特率----------------------------------------------------
    public int getBaudRate(){
        return iBaudRate;
    }
    //设置波特率，整数类型----------------------------------------------------
    public boolean setBaudRate(int iBaud){
        if (_isOpen){
            return false;
        }else {
            iBaudRate = iBaud;
            return true;
        }
    }
    //设置波特率，字符类型----------------------------------------------------
    public boolean setBaudRate(String sBaud){
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }

    //获取Com----------------------------------------------------
    public String getPortName(){
        return sPortName;
    }
    //设置Com----------------------------------------------------
    public boolean setPortName(String sPortName){
        if (_isOpen){
            return false;
        }else {
            this.sPortName = sPortName;
            return true;
        }
    }
    //获取串口状态----------------------------------------------------
    public boolean getIsOpen(){
        return _isOpen;
    }

    //获取发送数据----------------------------------------------------
    public byte[] getbyteLoopData(){
        return _bLoopData;
    }
    //设置发送数据，byte类型----------------------------------------------------
    public void setbyteLoopData(byte[] bLoopData){
        this._bLoopData = bLoopData;
    }
    //设置发送数据，文本类型----------------------------------------------------
    public void setTxtLoopData(String sTxt){
        this._bLoopData = sTxt.getBytes();
    }
    //设置发送数据,16进制类型----------------------------------------------------
    public void setHexLoopData(String sHex){
        this._bLoopData = MyFunc.HexToByteArr(sHex);
    }

    //获取延时时间----------------------------------------------------
    public int getDelay() {
        return iDelay;
    }
    //设置延时时间----------------------------------------------------
    public void setDelay(int iDelay) {
        this.iDelay = iDelay;
    }

    //开始发送----------------------------------------------------
    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }
    //停止发送----------------------------------------------------
    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }

    //返回遥信----------------------------------------------------
    public int[] getiTelemetry(){
        return iYc;
    }
    //返回遥测----------------------------------------------------
    public boolean[] getisTelecommand(){
        return _isYx;
    }
    //返回遥控----------------------------------------------------
    public boolean[] getisTelecontrol(){
        return _isYk;
    }
    //返回系统时间----------------------------------------------------
    public String getSystemTime() {
        return sSystemTime;
    }

    //获取com状态----------------------------------------------------
    public boolean getisCommFlag(){
        return _isCommFlag;
    }
    //设置com状态----------------------------------------------------
    public void  setisCommFlag(boolean _isCommFlag){
        this._isCommFlag = _isCommFlag;
    }

    //接收类型处理----------------------------------------------------
    private void readData(int size,byte[] buffer){
        byte[] bRec;

        String temp = MyFunc.ByteArrToHex(buffer);

        SharedPreferences.Editor wRealData = mContext.getSharedPreferences("RealData",MODE_PRIVATE).edit();
        SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();

        if (size > 0 && size< 500 && buffer[0] == 0x7E && buffer[size-1] == 0x0D){

            bRec = new byte[size];
            System.arraycopy(buffer,0,bRec,0,size);

            byte bCrc = MyFunc.addCrc(bRec);
            if (bCrc == bRec[size - 2]){

                _isCommFlag = true;

                switch (bRec[1]){
                    case 0x03:
                        if (size == 48 && MyFunc.ByteArrToInt(bRec,2) == 0 && MyFunc.ByteArrToInt(bRec,4) == 0x0013){
                            //遥测
                            int k = 6;
                            for (int i = 0; i < 13 ; i++) {
                                iYc[i] = MyFunc.ByteArrToInt(bRec,k);
                                k += 2;
                            }
                            //遥信
                            _isYx = MyFunc.ByteToBoolArr(bRec[42]);
                            //遥控
                            _isYk[0] = MyFunc.ByteToBool(bRec[44],0);
                            _isYk[1] = MyFunc.ByteToBool(bRec[46],0);
                            //时间
                            byte[] bTime = new byte[6];
                            System.arraycopy(bRec,35,bTime,0,6);
                            sSystemTime = MyFunc.BCDArrtoString(bTime);

                            // 遥测
                            wRealData.putInt("i_Rv",iYc[0]);                             //R相电压
                            wRealData.putInt("i_Sv",iYc[1]);                             //S相电压
                            wRealData.putInt("i_Tv",iYc[2]);                             //T相电压
                            wRealData.putInt("i_Uv",iYc[3]);                             //U相电压
                            wRealData.putInt("i_Vv",iYc[4]);                             //V相电压
                            wRealData.putInt("i_Wv",iYc[5]);                             //W相电压
                            wRealData.putInt("i_Ua",iYc[6]);                             //U相电流
                            wRealData.putInt("i_Va",iYc[7]);                             //V相电流
                            wRealData.putInt("i_Wa",iYc[8]);                             //W相电流
                            wRealData.putInt("i_Hz",iYc[9]);                             //频率
                            wRealData.putInt("i_SagTime",iYc[10]);                       //录波次数
                            wRealData.putInt("i_Capv",iYc[11]);                          //电容电压
                            wRealData.putInt("i_CapAh",(((iYc[11]-232)/142)));           //电容容量
                            wRealData.putInt("i_NewSagSite",iYc[12]);                    //当前录波位置
                            wRealData.putInt("i_SagSum",iYc[13]);                        //录波总数
                            //系统事件
                            wRealData.putString("s_SystemTime",sSystemTime);                    //下位机系统时间
                            SystemFunc.setNewTime(sSystemTime);
                            //遥信
                            wRealData.putBoolean("is_RechargeFlag",_isYx[0]);          //充电状态
                            wRealData.putBoolean("is_CompensateFlag",_isYx[1]);        //补偿状态
                            wRealData.putBoolean("is_InAlarm",_isYx[3]);               //输入异常
                            wRealData.putBoolean("is_OutOC",_isYx[4]);                 //输出过流
                            wRealData.putBoolean("is_OutRl",_isYx[5]);                 //输出短路
                            wRealData.putBoolean("is_AhLose",_isYx[6]);                //容量失效
                            wRealData.putBoolean("is_ComError",_isYx[7]);              //通讯异常
                            //遥控
                            wRealData.putBoolean("is_SystemMode",_isYk[0]);            //系统模式
                            wRealData.putBoolean("is_CompensateEnabled",_isYk[1]);     //补偿使能

                            alarmHand();

                        }else {
                            Log.e("串口信息","0x03回送帧出错，数据内容："+temp);
                            _isCommFlag = false;
                        }
                        break;
                    case 0x10:

                        break;
                }
            }else {
                Log.e("串口信息","数据校验错误，数据内容："+temp);
                _isCommFlag = false;
            }
        }else {
            Log.e("串口信息","数据帧错误，数据内容："+temp);
            _isCommFlag = false;

        }

        wStateData.putBoolean("is_CommFlag",_isCommFlag);

//        writeAlarmShared.commit();
        wRealData.commit();
        wStateData.commit();
    }

    private void alarmHand(){
        SharedPreferences.Editor wAlarmData = mContext.getSharedPreferences("AlarmData",MODE_PRIVATE).edit();
        SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData", 0);

        for (int i = 0; i < 8 ; i++) {
            if (i == 2){ continue;}
            _isYxFlag[i] = rAlarmData.getBoolean("is_yxError"+(1+i),false);
            if (_isYx[i] && !_isYxFlag[i]){
                _isYxFlag[i] = true;
            }else if (!_isYx[i] && _isYxFlag[i]){
                _isYxFlag[i] = false;
            }
            wAlarmData.putBoolean("is_yxError"+(1+i),_isYxFlag[i]);
        }

        wAlarmData.commit();
    }

}
