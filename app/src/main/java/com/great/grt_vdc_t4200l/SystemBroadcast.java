package com.great.grt_vdc_t4200l;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 系统广播监听
 */
public class SystemBroadcast extends BroadcastReceiver{

    private static String  TAG = "系统广播监听";

    @Override
    public void onReceive(Context context, Intent intent) {

        //开机自启动
        context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.great.grt_vdc_t4200l"));

    }


}
