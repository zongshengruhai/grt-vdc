package com.great.grt_vdc_t4200l.SerialPortHelp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.mikephil.charting.formatter.IFillFormatter;
import com.great.grt_vdc_t4200l.SerialPortHelp.bean.ComBean;
import com.great.grt_vdc_t4200l.SystemFunc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

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

    //接收数据用
    private int iReadLength = 0;
    private int iReadNewLength = 0;
    private byte[] bBuffer = new byte[0];

    //excel默认表头
    private List<List<Object>> execel = new ArrayList<>();




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
            if (bOutArray.length == 8){
                switch (bOutArray[1]){
                    case 0x03:
                        iReadLength = 48;
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
                Log.e("串口消息", "发送数据：" + MyFunc.ByteArrToHex(bOutArray));
            }
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

                    byte[] buffer1 = new byte[2];
                    int size = mInputStream.read(buffer1);

                    //整理数据
                    byte[] buffer2 = new byte[size];
                    System.arraycopy(buffer1,0,buffer2,0,size);
                    byte[] buffer3 = new byte[bBuffer.length];
                    System.arraycopy(bBuffer,0,buffer3,0,bBuffer.length);
                    bBuffer =  new byte[buffer3.length+buffer2.length];
                    bBuffer = MyFunc.addByteArr(buffer3,buffer2);

                    iReadNewLength = bBuffer.length;

//                    Log.e("串口消息","当前长度："+size+"，累积长度"+iReadNewLength+"，数据缓存长度"+bBuffer.length + "数据" + MyFunc.ByteArrToHex(bBuffer));
//                    iReadLength = 558;
                    if ((iReadNewLength == iReadLength)&& _isOpen){
//                        ComBean comRecData = new ComBean(sPortName,buffer,size)
                        if (mContext != null){
                            Log.e("串口消息", "接收数据：" + MyFunc.ByteArrToHex(bBuffer));
                            readData(bBuffer.length,bBuffer);
                        }else {
                            Log.e("串口消息","环境未来准备好");
                        }
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
        String temp = MyFunc.ByteArrToHex(buffer);

        //写入
        SharedPreferences.Editor wStateData = mContext.getSharedPreferences("StateData",MODE_PRIVATE).edit();
        SharedPreferences.Editor wRealData = mContext.getSharedPreferences("RealData",MODE_PRIVATE).edit();
        SharedPreferences.Editor wAlarmData = mContext.getSharedPreferences("AlarmData",MODE_PRIVATE).edit();

        //读取
        SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);
        SharedPreferences rRealData = mContext.getSharedPreferences("RealData", 0);
        SharedPreferences rAlarmData = mContext.getSharedPreferences("AlarmData",0);

        if (size > 0 && size< 600 && buffer[0] == 0x7E && buffer[size-1] == 0x0D){

            if (MyFunc.addCrc(buffer) == buffer[size - 2]){
                _isCommFlag = true;

                switch (buffer[1]){
                    case 0x03:
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
                            sSystemTime = MyFunc.BCDArrtoString(bTime);

                            // 遥测
                            wRealData.putInt("i_Uv",iYc[0]);                             //U相电压
                            wRealData.putInt("i_Vv",iYc[1]);                             //V相电压
                            wRealData.putInt("i_Wv",iYc[2]);                             //W相电压
                            wRealData.putInt("i_Ua",iYc[3]);                             //U相电流
                            wRealData.putInt("i_Va",iYc[4]);                             //V相电流
                            wRealData.putInt("i_Wa",iYc[5]);                             //W相电流
                            wRealData.putInt("i_Hz",iYc[6]);                             //频率
                            wRealData.putInt("i_Rv",iYc[7]);                             //R相电压
                            wRealData.putInt("i_Sv",iYc[8]);                             //S相电压
                            wRealData.putInt("i_Tv",iYc[9]);                             //T相电压
                            wRealData.putInt("i_SagTime",iYc[10]);                       //录波次数
                            wRealData.putInt("i_Capv",iYc[11]);                          //电容电压
                            wRealData.putInt("i_CapAh",(((iYc[11]-232)/142)));           //电容容量
                            wRealData.putInt("i_NewSagSite",iYc[12]);                    //当前录波位置
                            wRealData.putInt("i_SagSum",iYc[13]);                        //录波总数
                            //系统事件
//                            wRealData.putString("s_SystemTime",sSystemTime);                    //下位机系统时间
//                            SystemFunc.setNewTime(sSystemTime);
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

//                            alarmHand();
                        }else {
                            Log.e("串口信息","0x03回送帧出错，数据内容："+temp);
                            _isCommFlag = false;
                        }
                        break;
                    case 0x10:
                        if (size == 26){
                            //表头
                            execel.clear();
                            List<Object> excelHead = new ArrayList<>();
                            excelHead.add("R相输入电压");
                            excelHead.add("S相输入电压");
                            excelHead.add("T相输入电压");
                            excelHead.add("U相输出电压");
                            excelHead.add("V相输出电压");
                            excelHead.add("W相输出电压");
                            excelHead.add("U相输出电流");
                            excelHead.add("V相输出电流");
                            excelHead.add("W相输出电流");
                            execel.add(excelHead);

                            //数据
                            int eventType = MyFunc.ByteArrToInt(buffer,6);
                            int eventRow = MyFunc.ByteArrToInt(buffer,8);
                            int eventTime = MyFunc.ByteArrToInt(buffer,10);
                            String eventStartTime = "20"+buffer[12]+"."+buffer[13]+"."+buffer[14]+"_"+buffer[15]+":"+buffer[16];
//                            String eventEndTime = "20"+buffer[18]+"."+buffer[19]+"."+buffer[20]+"_"+buffer[21]+":"+buffer[22];

                            //录波记录编号
                            int recordNum = rAlarmData.getInt("i_RecordTime",0) + 1;

                            //文件名
                            String fileName = mContext.getFilesDir().getPath() + "/record_log/"+recordNum+"_"+eventType+"_"+eventRow+"_"+eventTime+"_"+eventStartTime+".xls";
                            if(SystemFunc.createExcel(fileName,execel)){
                                //写入当前录波地址，读完所有地址后，应当将此数值复0
                                wStateData.putInt("i_RecordAddress_2",1);
                                //写入当前录波数据存储的文件名
                                wStateData.putString("s_RecordFileName",fileName);
                            }

                        }else if(size == 558) {

                            //读取数据
                            int[] eventAddress = new int[2];
                            eventAddress[0] = MyFunc.ByteArrToInt(buffer,2);
                            eventAddress[1] = MyFunc.ByteArrToInt(buffer,4);

                            //读取当前文件名
                            String fileName = rStateData.getString("s_RecordFileName","");

                            //防止空指针
                            if (fileName.contains(".xls")) {

                                List<List<Object>> recordEvent = new ArrayList<>();
                                List<Object> eventData = new ArrayList<>();
                                int dataAddress = 6;

                                // 第一列，直接以追加数据方式存入数据，其他列均填充0
                                if (eventAddress[1] <= 7) {
                                    //直接将byte转int后写入list
                                    for (int i = 0; i < 275; i++) {
                                        eventData.add(MyFunc.ByteArrToInt(buffer, dataAddress));
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

                                    //算出指定行
                                    int row_index = (((eventAddress[1]-2)%6)*275)+1;
                                    //算出指定列
                                    int col_mod = (eventAddress[1]-13)%6;
                                    if (col_mod > 0){col_mod = 1;}else {col_mod = 0;}
                                    int col_index =((eventAddress[1]-13)/6) + col_mod + 1;

                                    //将数值存入缓存
                                    for (int i = 0; i < 275; i++) {
                                        eventData.add(MyFunc.ByteArrToInt(buffer, dataAddress));
                                        dataAddress = dataAddress + 2;
                                    }

                                    //写入数值，写入成功后，将录波地址+1
                                    if (SystemFunc.alterExcelDatas(fileName,row_index,col_index,eventData)){

                                        wStateData.putInt("i_RecordAddress_2",(rStateData.getInt("i_RecordAddress_2",1) + 1));

                                        //当一条录波读完后，复位相关标志位
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
                                _isCommFlag = false;
                            }

                        }else {
                            Log.e("串口信息","0x10回送帧出错，数据内容："+temp);
                            _isCommFlag = false;
                        }
                        break;
                    default:
                        Log.e("readData: " ,""+buffer[2] );
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

        //通讯标志
        wStateData.putBoolean("is_CommFlag",_isCommFlag);

        wRealData.commit();
        wStateData.commit();
        wAlarmData.commit();
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
