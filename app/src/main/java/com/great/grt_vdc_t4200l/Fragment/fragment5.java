package com.great.grt_vdc_t4200l.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.great.grt_vdc_t4200l.R;
import com.great.grt_vdc_t4200l.SystemFunc;


/**
 * fragment5 主界面
 */
public class fragment5 extends Fragment {

    //容器
    private ImageView[] fragment5Map = new ImageView[6];
    private TextView[] fragment5Tv = new TextView[7];
    private boolean _isType =false;

    /**
     * fragment生命周期
     */
    //创建
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment5,container,false);

        //系统流向
        fragment5Map[0] = view.findViewById(R.id.fragment5_map);
//        fragment5Map[0] = view.findViewById(R.id.fragment5_cap_status);
//        fragment5Map[1] = view.findViewById(R.id.fragment5_in_status);
//        fragment5Map[2] = view.findViewById(R.id.fragment5_out_status);
//
        //告警灯
        fragment5Map[1] = view.findViewById(R.id.fragment5Yx_1);
        fragment5Map[2] = view.findViewById(R.id.fragment5Yx_2);
        fragment5Map[3] = view.findViewById(R.id.fragment5Yx_3);
        fragment5Map[4] = view.findViewById(R.id.fragment5Yx_4);
        fragment5Map[5] = view.findViewById(R.id.fragment5Yx_5);
//
//        //电容容量
        fragment5Tv[0] = view.findViewById(R.id.fragment5_cap);

        fragment5Tv[1] = view.findViewById(R.id.fragment5AlarmTime);
        fragment5Tv[2] = view.findViewById(R.id.fragment5RecordTime);
        fragment5Tv[3] = view.findViewById(R.id.fragment5Hz);
        fragment5Tv[4] = view.findViewById(R.id.nowSystemTime);


        return view;
    }
    //重载
    @Override
    public void onResume(){
        super.onResume();
        //注册定时
        fragment5Handler.post(fragment5Runnable);
    }
    //中止
    @Override
    public void onPause(){
        super.onPause();
        //注销定时
        fragment5Handler.removeCallbacks(fragment5Runnable);
    }

    /**
     * fragment5定时线程
     */
    Handler fragment5Handler = new Handler();
    Runnable fragment5Runnable = new Runnable() {
        @Override
        public void run() {
            fragment5Handler.postDelayed(this,500);

                SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
                int layPage = rStateData.getInt("layPage",0);

                if (layPage == 0){
                    SharedPreferences rRealData = getActivity().getSharedPreferences("RealData", 0);
                    SharedPreferences rAlarmData = getActivity().getSharedPreferences("AlarmData", 0);

                    boolean[] _isYx = new boolean[8];
                    _isYx[0] = rRealData.getBoolean("is_RechargeFlag",false);           //充电状态
                    _isYx[1] = rRealData.getBoolean("is_CompensateFlag",false);         //补偿状态


                    //假装GIF
                    if (_isYx[0] && !_isYx[1]) {
                        if (_isType) {
//                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.capin_1));
//                            fragment5Map[1].setImageDrawable(getResources().getDrawable(R.mipmap.in_1));
//                            fragment5Map[2].setImageDrawable(getResources().getDrawable(R.mipmap.out_1));
                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_3));
                        }else {
//                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.capin_2));
//                            fragment5Map[1].setImageDrawable(getResources().getDrawable(R.mipmap.in_2));
//                            fragment5Map[2].setImageDrawable(getResources().getDrawable(R.mipmap.out_2));
                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_4));
                        }

                    }else if (!_isYx[0] && _isYx[1]) {
                        if (_isType) {
//                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.capout_1));
//                            fragment5Map[1].setImageDrawable(getResources().getDrawable(R.mipmap.in_3));
//                            fragment5Map[2].setImageDrawable(getResources().getDrawable(R.mipmap.out_1));
                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_5));
                        }else {
//                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.capout_2));
//                            fragment5Map[1].setImageDrawable(getResources().getDrawable(R.mipmap.in_3));
//                            fragment5Map[2].setImageDrawable(getResources().getDrawable(R.mipmap.out_2));
                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_6));
                        }
                    }else {
                        if (_isType) {
                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_1));
//                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.cap));
//                            fragment5Map[1].setImageDrawable(getResources().getDrawable(R.mipmap.in_1));
//                            fragment5Map[2].setImageDrawable(getResources().getDrawable(R.mipmap.out_1));
                        }else {
                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_2));
//                            fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.cap));
//                            fragment5Map[1].setImageDrawable(getResources().getDrawable(R.mipmap.in_2));
//                            fragment5Map[2].setImageDrawable(getResources().getDrawable(R.mipmap.out_2));
                        }
                    }
                    _isType = !_isType;
                    fragment5Tv[0].setText(String.format(getResources().getString(R.string.fragment5CapI), rRealData.getInt("i_CapAh", 0)));

                    //遥信
                    _isYx[2] = rRealData.getBoolean("is_InAlarm",false);                //输入异常
                    _isYx[3] = rRealData.getBoolean("is_OutOC",false);                  //输出过流
                    _isYx[4] = rRealData.getBoolean("is_OutRl",false);                  //输出短路
                    _isYx[5] = rRealData.getBoolean("is_AhLose",false);                 //容量失效
                    _isYx[6] = rRealData.getBoolean("is_ComError",false);               //通讯异常

                    for (int i = 0; i < 5; i++) {
                        if (!_isYx[i+2]){
                            fragment5Map[i+1].setImageDrawable(getResources().getDrawable(R.mipmap.led_1));
                        }else {
                            fragment5Map[i+1].setImageDrawable(getResources().getDrawable(R.mipmap.led_2));
                        }
                    }

                    fragment5Tv[1].setText(String.format(getResources().getString(R.string.fragment5_Time),rAlarmData.getInt("i_AlarmTime",0)));
                    fragment5Tv[2].setText(String.format(getResources().getString(R.string.fragment5_Time),rAlarmData.getInt("i_RecordTime",0)));
                    fragment5Tv[3].setText(String.format(getResources().getString(R.string.fragment5_Hz),rRealData.getFloat("f_Hz",0)));
                    fragment5Tv[4].setText(SystemFunc.getNewTimeString());

                }


        }
    };
}
