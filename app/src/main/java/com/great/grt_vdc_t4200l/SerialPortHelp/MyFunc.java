package com.great.grt_vdc_t4200l.SerialPortHelp;

import android.util.Log;

/**
 *数据类型转换工具
 */
public class MyFunc {
    //-------------------------------------------------------
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num)
    {
        return num & 0x1;
    }
    //-------------------------------------------------------
    static public int HexToInt(String inHex)//Hex字符串转int
    {
        return Integer.parseInt(inHex, 16);
    }
    //-------------------------------------------------------
    static public byte HexToByte(String inHex)//Hex字符串转byte
    {
        return (byte)Integer.parseInt(inHex,16);
    }
    //-------------------------------------------------------
    static public String Byte2Hex(Byte inByte)//1字节转2个Hex字符
    {
        return String.format("%02x", inByte).toUpperCase();
    }
    //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inByteArr)//字节数组转转hex字符串
    {
        StringBuilder strBuilder=new StringBuilder();
        int j=inByteArr.length;
        for (int i = 0; i < j; i++)
        {
            strBuilder.append(Byte2Hex(inByteArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }
    //-------------------------------------------------------
    static public String ByteArrToHex(byte[] inByteArr,int offset,int byteCount)//字节数组转转hex字符串，可选长度
    {
        StringBuilder strBuilder=new StringBuilder();
        int j=byteCount;
        for (int i = offset; i < j; i++)
        {
            strBuilder.append(Byte2Hex(inByteArr[i]));
        }
        return strBuilder.toString();
    }
    //-------------------------------------------------------
    //转hex字符串转字节数组
    static public byte[] HexToByteArr(String inHex)//hex字符串转字节数组
    {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen)==1)
        {//奇数
            hexlen++;
            result = new byte[(hexlen/2)];
            inHex="0"+inHex;
        }else {//偶数
            result = new byte[(hexlen/2)];
        }
        int j=0;
        for (int i = 0; i < hexlen; i+=2)
        {
            result[j]=HexToByte(inHex.substring(i,i+2));
            j++;
        }
        return result;
    }
    //-------------------------------------------------------
    //转2个byte转int
    static public int ByteArrToInt(byte[] inByteArr,int site){
        int result = 0;
        if (inByteArr.length > site){
            result += (inByteArr[site] & 0xff) << 8;
            result += (inByteArr[site+1] & 0xff);
        }
        return result;
    }
    //-------------------------------------------------------
    //字节转8位boolen数值
    static public boolean[] ByteToBoolArr(byte inByte){
        boolean[] result = new boolean[8];
        for (int i = 7; i >= 0 ; i--) {
            result[i] = (inByte & 0x01) == 1;
            inByte = (byte)(inByte >> 1 );
        }
        return result;
    }
    //-------------------------------------------------------
    //int转Byte
    static public byte[] InToByteArr(int inInt){
        return new byte[]{
                (byte)((inInt >> 24) & 0xFF),
                (byte)((inInt >> 16) & 0xFF),
                (byte)((inInt >> 8) & 0xFF),
                (byte)(inInt & 0xFF),
        };
    }
    //-------------------------------------------------------
    //字节指定位置转boolen
    static public boolean ByteToBool(byte inByte,int site){
        boolean result;
        result = (byte)((inByte >> site) & 0x01) == 1;
        return result;
    }
    //-------------------------------------------------------
    //BCD数组转String
    static public String BCDArrtoString(byte[] inByte){
//        StringBuffer result = new StringBuffer(inByte.length * 2);
//        for (int i = 0; i < inByte.length ; i++) {
//            result.append((byte)(inByte[i] & 0xf0) >>> 4);
//            result.append((byte)(inByte[i] & 0xf0));
//        }
//        return result.toString().substring(0,1).equalsIgnoreCase("0")?result.toString().substring(1):result.toString();
        String result = "20180905.080000";
        String year,month,day,hour,min,sec;
        year = "20"+inByte[0];
        if (inByte[1] >= 10){ month = ""+inByte[1];}else {month = "0" + inByte[1];}
        if (inByte[2] >= 10){ day = ""+inByte[2];}else {day = "0" + inByte[2];}
        if (inByte[3] >= 10){ hour = ""+inByte[3];}else {hour = "0" + inByte[3];}
        if (inByte[4] >= 10){ min = ""+inByte[4];}else {min = "0" + inByte[4];}
        if (inByte[5] >= 10){ sec = ""+inByte[5];}else {sec = "0" + inByte[5];}
        result = year + month + day + "." + hour + min +sec;
        return result;
    }
    //-------------------------------------------------------
    //累加CRC
    static public byte addCrc(byte[] bytes){
        byte bCrc = 0;
        if (bytes.length > 0){
            bCrc = bytes[1];
            for (int i = 2; i < bytes.length - 2 ; i++) {
                bCrc = (byte) (bCrc + bytes[i]);
//                bCrc += bytes[i];
            }
        }
        return bCrc;
    }
    //-------------------------------------------------------
    //byte数组相加
    static public byte[] addByteArr(byte[] bytes1,byte[] bytes2){
        byte[] bytes3 = new byte[bytes1.length+bytes2.length];
        System.arraycopy(bytes1,0,bytes3,0,bytes1.length);
        System.arraycopy(bytes2,0,bytes3,bytes1.length,bytes2.length);
        return bytes3;
    }
}
