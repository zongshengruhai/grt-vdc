package com.great.grt_vdc_t4200l.SerialPortHelp;

import android.util.Log;

import java.util.Arrays;

/**
 *数据类型转换工具
 */
public class MyFunc {

    /**
     * 判断奇偶
     * @return  true 奇数 ，false偶数
     */
    static public int isOdd(int num)
    {
        return num & 0x1;
    }

    /**
     * Hex字符串转int
     */
    static public int HexToInt(String inHex){
        return Integer.parseInt(inHex, 16);
    }

    /**
     * Hex转Byte
     */
    static public byte HexToByte(String inHex) {
        return (byte)Integer.parseInt(inHex,16);
    }

    /**
     * 1字节转2个Hex字符
     */
    static public String Byte2Hex(Byte inByte) { return String.format("%02x", inByte).toUpperCase(); }

    /**
     * byte数组转转hex字符串
     */
    static public String ByteArrToHex(byte[] inByteArr) {
        StringBuilder strBuilder=new StringBuilder();
        int j=inByteArr.length;
        for (int i = 0; i < j; i++)
        {
            strBuilder.append(Byte2Hex(inByteArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }


    /**
     * byte数组转转hex字符串，可选长度
     * @param inByteArr 需要转换的Byte数组
     * @param offset    转换开始地址
     * @param byteCount 转换长度
     */
    static public String ByteArrToHex(byte[] inByteArr,int offset,int byteCount) {
        StringBuilder strBuilder=new StringBuilder();
        int j=byteCount;
        for (int i = offset; i < j; i++)
        {
            strBuilder.append(Byte2Hex(inByteArr[i]));
        }
        return strBuilder.toString();
    }

    /**
     * hex字符串转byte数组
     */
    static public byte[] HexToByteArr(String inHex) {
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

    /**
     * 选择byte数组中的2个byte合并为int
     * @param site 指定合并起始位置
     */
    static public int ByteArrToInt(byte[] inByteArr,int site){
        int result = 0;
        if (inByteArr.length > site){
            result += (inByteArr[site] & 0xff) << 8;
            result += (inByteArr[site+1] & 0xff);
        }
        //取反
        if (result > 32768){ result = (result - 65536); }
        return result;
    }

    /**
     * byte转8位boolean数组
     */
    static public boolean[] ByteToBoolArr(byte inByte){
        boolean[] result = new boolean[8];
        for (int i = 7; i >= 0 ; i--) {
            result[i] = (inByte & 0x01) == 1;
            inByte = (byte)(inByte >> 1 );
        }
        return result;
    }

    /**
     * int转4个byte的数组
     */
    static public byte[] InToByteArr(int inInt){
        return new byte[]{
                (byte)((inInt >> 24) & 0xFF),
                (byte)((inInt >> 16) & 0xFF),
                (byte)((inInt >> 8) & 0xFF),
                (byte)(inInt & 0xFF),
        };
    }

    /**
     * 指定byte的某个位转boolean
     */
    static public boolean ByteToBool(byte inByte,int site){
        return ((byte)((inByte >> site) & 0x01) == 1);
    }

    /**
     * BCD转时间戳
     * @param Type 时间戳需要的类型
     */
    static public String BCDArrToTime(byte[] bytes,String Type){
        String year,month,day,hour,min,sec;
        int[] ints = new int[6];
        for (int i = 0; i < 6 ; i++) { ints[i] = BCDToInt(bytes[i]); }
        year = "20"+ints[0];
        if (ints[1] >= 10) { month = "" + ints[1]; } else { month = "0" + ints[1]; }
        if (ints[2] >= 10) { day = "" + ints[2]; } else { day = "0" + ints[2]; }
        if (ints[3] >= 10) { hour = "" + ints[3]; } else { hour = "0" + ints[3]; }
        if (ints[4] >= 10) { min = "" + ints[4]; } else { min = "0" + ints[4]; }
        if (ints[5] >= 10) { sec = "" + ints[5]; } else { sec = "0" + ints[5]; }

        if (Type.equals("SystemTime")){
            return  year + month + day + "." + hour + min +sec;
        }else {
            return  year +"."+ month +"."+ day + "_" + hour+"："+min+"："+sec;
//            return  year +"."+ month +"."+ day + "_" + hour+"："+min+"："+sec;
        }
    }

    static private int BytesToUnix(byte[]  bytes){
        int a[] = new int[5];
        a[0] = (bytes[5] - 1) * 365 + bytes[5] / 4 - bytes[5] / 100 + bytes[5] / 400;
        a[1] = 367 * (bytes[4]-2) / 12 - 30 + 59;
        a[2] = bytes[3] - 1;
        a[3] = a[0] + a[1] + a[2] - 719162;
        return  ((a[3] * 24 + (bytes[2]-8)) * 60 + bytes[1]) * 60 + bytes[0];
    }

    /**
     * BCD转Int
     */
    static public int BCDToInt(byte inByte){
        return (0xff & (inByte>>4))*10 +(0xf & inByte);
    }

    /**
     * 累加和CRC运算
     */
    static public byte addCrc(byte[] bytes){
        byte bCrc = 0;
        if (bytes.length > 0){
            bCrc = bytes[1];
            for (int i = 2; i < bytes.length - 2 ; i++) {
                bCrc = (byte) (bCrc + bytes[i]);
            }
        }
        return bCrc;
    }

    /**
     * 时间转BCD
     * @param s 需要转换的时间 格式 YYYY_MM_DD_HH_mm_ss
     * @return BCD数组 bytes[]{YY,MM,DD,HH,mm,ss}
     */
    static public byte[] timeToBCD(String s){
        byte[] bytes = new byte[6];

        String[] strings = s.split("_");

        byte[] temp;

        temp = StringToBCD(strings[0]);
        bytes[0] = temp[1];

        for (int i = 1; i < 6 ; i++) {
            temp = StringToBCD(strings[i]);
            bytes[i] = temp[0];
        }

        return bytes;
    }

    /**
     * 字符串转BCD
     * @param asc 输入字符串
     * @return bcd数组
     */
    static private byte[] StringToBCD(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[];
        if (len >= 2) {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }


    /**
     * byte数组粘包
     */
    static public byte[] addByteArr(byte[] bytes1,byte[] bytes2){
        byte[] bytes3 = new byte[bytes1.length+bytes2.length];
        System.arraycopy(bytes1,0,bytes3,0,bytes1.length);
        System.arraycopy(bytes2,0,bytes3,bytes1.length,bytes2.length);
        return bytes3;
    }


}
