package com.great.grt_vdc_t4200l.SerialPortHelp.bean;

import com.great.grt_vdc_t4200l.SerialPortHelp.MyFunc;

import java.text.SimpleDateFormat;

public class ComBean {
    public byte[] bRec = null;
    public String sRec;
    public String sRecTime = "";
    public String sComPort = "";

    public ComBean(String sPort, byte[] buffer, int size) {
        sComPort = sPort;
        bRec = new byte[size];
        for (int i = 0; i < size; i++) {
            bRec[i] = buffer[i];
//            sRec[i] = MyFunc.Byte2Hex(buffer[i]);
        }
        sRec = MyFunc.ByteArrToHex(bRec);
        SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");
        sRecTime = sDateFormat.format(new java.util.Date());
    }
}
