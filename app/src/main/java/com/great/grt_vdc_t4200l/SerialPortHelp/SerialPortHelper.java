package com.great.grt_vdc_t4200l.SerialPortHelp;

import com.great.grt_vdc_t4200l.SerialPortHelp.bean.ComBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

public class SerialPortHelper {

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private ReadThread mReadThread;
    private SendThread mSendThread;

    private String sPortName = "/dev/ttyS2";
    private int iBaudRate = 9600;

    private boolean _isOpen = false;
    private int iDelay = 300;

    private byte[] _bLoopData = new byte[]{};

    //指定串口、指定波特率的实例化----------------------------------------------------
    public SerialPortHelper(String sPortName , int iBaudRate){
        this.sPortName = sPortName;
        this.iBaudRate = iBaudRate;
    }
    //默认值实例化----------------------------------------------------
    public SerialPortHelper(){
        this("/dev/ttyS2",9600);
    }
    //指定串口的实例化----------------------------------------------------
    public SerialPortHelper(String sPortName){
        this(sPortName,9600);
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
                    if (size > 0 ){
                        ComBean comRecData = new ComBean(sPortName,buffer,size);
//                        onDataReceived(comRecData);
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

//    protected abstract void onDataReceived(ComBean comRecData);



}
