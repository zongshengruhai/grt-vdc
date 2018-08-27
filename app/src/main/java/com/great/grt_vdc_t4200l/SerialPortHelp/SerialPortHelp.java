package com.great.grt_vdc_t4200l.SerialPortHelp;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * description: 串口帮助类
 * data：2018年8月27日 11:05:16
 */
public class SerialPortHelp {

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private ReadThread mRedaThread;

    private String sPath = "/dev/ttyS2";
    private int iBaudRate = 38400;

    private String inBuffer;
    private byte[] mbuffer = new byte[1];
    private boolean mIsOpen = false;

    //----------------------------------------------------
    public void open() throws SecurityException,IOException,InvalidParameterException{
        mSerialPort = new SerialPort(new File(sPath),iBaudRate,0);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        mRedaThread = new ReadThread();
        mRedaThread.start();
        mIsOpen = true;
    }
    //----------------------------------------------------
    public void close(){
//        if (mRedaThread != null){ mRedaThread.interrupt(); }
//        if (mSerialPort != null){
//            mSerialPort.close();
//            mSerialPort = null;
//        }
        try {
            mOutputStream.close();
            mInputStream.close();
            mSerialPort.close();
        }catch (IOException e){
            return;
        }
        mIsOpen = false;
    }
    //----------------------------------------------------
    public boolean send(byte[] bOutArray){
        try {
            mOutputStream.write(bOutArray);
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    //----------------------------------------------------


    private class ReadThread extends Thread{
        @Override
        public void run(){
            super.run();

            int inputSize = 0;

            while (mIsOpen){
                try {
                    if (mInputStream != null){
                        int size = mInputStream.read(mbuffer);
                        inputSize = inputSize + size;
                        Log.e("SerialPortHelp:","Size="+inputSize);
                        if (inputSize > 50){
                            inputSize = 0;
                            inBuffer = "";
                        }else {
                            for (int i = 0; i <size ; i++) {
                                inBuffer += ByteArrToHex(mbuffer[i]);
                                Log.e("SerialPortHelp:","content="+inBuffer);
                            }
                        }
                    }
                }catch (IOException e){
                        return;
                }
            }
        }
    }


    //----------------------------------------------------
    static private String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
        return String.format("%02x", inByte).toUpperCase();
    }
    //----------------------------------------------------
    static private String ByteArrToHex(byte inBytArr)//字节数组转转hex字符串
    {
        StringBuilder strBuilder =new StringBuilder();
        //int j=inBytArr.length;
        //for (int i = 0; i < j; i++)
        //{
        strBuilder.append(Byte2Hex(inBytArr));
        strBuilder.append("");
        //}
        return strBuilder.toString();
    }

}
