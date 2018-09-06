package com.great.grt_vdc_t4200l;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * 系统公共方法
 */
public class SystemFunc {

    static private String TAG = "System method";
    static private boolean _isBeep = false;             //防止并发

    /**
     * getNewTime 获取当前系统时间
     * @return String:"yyyy.MM.dd HH:mm:ss"
     */
    static public String getNewTime(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.DAY_OF_MONTH)+" "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
    }

    /**
     * setNewTime 设置当前时间
     * @param sTime String:"20180906.095000"
     */
    static public void setNewTime(String sTime){
        if (sTime.length() == 15){
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("setprop persist.sys.timezone GMT\n");
                os.writeBytes("/system/bin/date -s" + sTime + "\n");
                os.writeBytes("clock -w\n");
                os.writeBytes("exit\n");
                os.flush();
            }catch (IOException e){
                Log.e(TAG, "set System Time: lose" );
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "set System Time: lose ,length is error" );
        }
    }


    /**
     *  Beep 振铃
     *  @param mContext 执行活动的上下文
     *  @param flag true振铃 false取消振铃
     */
    static public void Beep(Context mContext,boolean flag){
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null){
            if (flag){
                if (!_isBeep)//防止并发
                {
                    _isBeep = true;
                    vibrator.vibrate(new long[]{500,500},0);
                }
            }else {
                _isBeep = false;
                vibrator.cancel();
            }
        }
    }

    /**
     * restart 重启设备
     */
    static public void restart(){
        try {
            Process mRestart =Runtime.getRuntime().exec(new String[]{"su","-c","reboot "});
            mRestart.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
