package com.great.grt_vdc_t4200l.SerialPortHelp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.great.grt_vdc_t4200l.SystemFunc;

import static android.content.Context.MODE_PRIVATE;

public class Response  {

    private int[] iTelemetry = new int[14];
    private boolean[] _isTelecommand = new boolean[8];
    private boolean[] _isTelecontrol = new boolean[2];
    private String sSystemTime;

    public Response (Context mContext,int size, byte[] buffer){

        byte[] bRec;
        String temp = MyFunc.ByteArrToHex(buffer);
        Log.e("串口信息","处理中");
        if (size > 0 && size< 500 && buffer[0] == 0x7E && buffer[size-1] == 0x0D){

            bRec = new byte[size];
            System.arraycopy(buffer,0,bRec,0,size);

            byte bCrc = MyFunc.addCrc(bRec);
            if (bCrc == bRec[size - 2]){
                switch (bRec[1]){
                    case 0x03:
                        if (size == 48 && MyFunc.ByteArrToInt(bRec,2) == 0 && MyFunc.ByteArrToInt(bRec,4) == 0x0013){
                            //遥测
                            int k = 6;
                            for (int i = 0; i < 13 ; i++) {
                                iTelemetry[i] = MyFunc.ByteArrToInt(bRec,k);
                                k += 2;
                            }
                            //遥信
                            _isTelecommand = MyFunc.ByteToBoolArr(bRec[42]);
                            //遥控
                            _isTelecontrol[0] = MyFunc.ByteToBool(bRec[44],0);
                            _isTelecontrol[1] = MyFunc.ByteToBool(bRec[46],0);
                            //时间
                            byte[] bTime = new byte[6];
                            System.arraycopy(bRec,35,bTime,0,6);
                            sSystemTime = MyFunc.BCDArrToTime(bTime,"SystemTime");

                            SharedPreferences.Editor editor = mContext.getSharedPreferences("realTimeData",MODE_PRIVATE).edit();
//                            SharedPreferences sharedPreferences = get
                            // 遥测
                            editor.putInt("i_Rv",iTelemetry[0]);                             //R相电压
                            editor.putInt("i_Sv",iTelemetry[1]);                             //S相电压
                            editor.putInt("i_Tv",iTelemetry[2]);                             //T相电压
                            editor.putInt("i_Uv",iTelemetry[3]);                             //U相电压
                            editor.putInt("i_Vv",iTelemetry[4]);                             //V相电压
                            editor.putInt("i_Wv",iTelemetry[5]);                             //W相电压
                            editor.putInt("i_Ua",iTelemetry[6]);                             //U相电流
                            editor.putInt("i_Va",iTelemetry[7]);                             //V相电流
                            editor.putInt("i_Wa",iTelemetry[8]);                             //W相电流
                            editor.putInt("i_Hz",iTelemetry[9]);                             //频率
                            editor.putInt("i_SagTime",iTelemetry[10]);                       //录波次数
                            editor.putInt("i_Capv",iTelemetry[11]);                          //电容电压
                            editor.putInt("i_CapAh",(((iTelemetry[11]-232)/142)));           //电容容量
                            editor.putInt("i_NewSagSite",iTelemetry[12]);                    //当前录波位置
                            editor.putInt("i_SagSum",iTelemetry[13]);                        //录波总数
                            //系统事件
                            editor.putString("s_SystemTime",sSystemTime);                    //下位机系统时间
                            //遥信
                            editor.putBoolean("is_RechargeFlag",_isTelecommand[0]);          //充电状态
                            editor.putBoolean("is_CompensateFlag",_isTelecommand[1]);        //补偿状态
                            editor.putBoolean("is_InAlarm",_isTelecommand[3]);               //输入异常
                            editor.putBoolean("is_OutOC",_isTelecommand[4]);                 //输出过流
                            editor.putBoolean("is_OutRl",_isTelecommand[5]);                 //输出短路
                            editor.putBoolean("is_AhLose",_isTelecommand[6]);                //容量失效
                            editor.putBoolean("is_ComError",_isTelecommand[7]);              //通讯异常
                            //遥控
                            editor.putBoolean("is_SystemMode",_isTelecontrol[0]);            //系统模式
                            editor.putBoolean("is_CompensateEnabled",_isTelecontrol[1]);     //补偿使能
                            editor.commit();

                            alarmHand();

                        }else { Log.e("串口信息","0x03回送帧出错，数据内容："+temp);}
                        break;
                    case 0x10:

                        break;
                }
            }else {
                Log.e("串口信息","数据校验错误，数据内容："+temp);
            }
        }else {
            Log.e("串口信息","数据帧错误，数据内容："+temp);
        }
    }

    private void alarmHand(){

    }

}
