package com.great.grt_vdc_t4200l.Fragment;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.anderson.dashboardview.view.DashboardView;
import com.great.grt_vdc_t4200l.R;

import java.io.File;

public class fragment5 extends Fragment {

    private static final String TAG = "fragment5";
    private ImageView[] fragment5Map = new ImageView[6];
    private TextView[] fragment5Tv = new TextView[1];
//    private DashboardView[] fragment5Dv = new DashboardView[1];
    private boolean _isType =false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment5,container,false);

        fragment5Map[0] = view.findViewById(R.id.fragment5_map);

        fragment5Map[1] = view.findViewById(R.id.fragment5Yx_1);
        fragment5Map[2] = view.findViewById(R.id.fragment5Yx_2);
        fragment5Map[3] = view.findViewById(R.id.fragment5Yx_3);
        fragment5Map[4] = view.findViewById(R.id.fragment5Yx_4);
        fragment5Map[5] = view.findViewById(R.id.fragment5Yx_5);

        fragment5Tv[0] = view.findViewById(R.id.fragment5_cap);

//        fragment5Dv[0] = view.findViewById(R.id.fragment5_in);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        fragment5Handler.post(fragment5Runable);
    }

    @Override
    public void onPause(){
        super.onPause();
        fragment5Handler.removeCallbacks(fragment5Runable);
    }

    Handler fragment5Handler = new Handler();
    Runnable fragment5Runable = new Runnable() {
        @Override
        public void run() {
            fragment5Handler.postDelayed(this,500);

            SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
            int layPage = rStateData.getInt("layPage",0);

            if (layPage == 0){
                SharedPreferences rRealData = getActivity().getSharedPreferences("RealData", 0);

                boolean[] _isYx = new boolean[8];
                _isYx[0] = rRealData.getBoolean("is_RechargeFlag",false);           //充电状态
                _isYx[1] = rRealData.getBoolean("is_CompensateFlag",false);         //补偿状态

                //假装GIF
                if (_isYx[0] && !_isYx[1]){
                    if (_isType) {
                        fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_3));
                    }else {
                        fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_4));
                    }
                }else if (!_isYx[0] && _isYx[1]){
                    if (_isType) {
                        fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_5));
                    }else {
                        fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_6));
                    }
                }else {
                    if (_isType) {
                        fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_1));
                    }else {
                        fragment5Map[0].setImageDrawable(getResources().getDrawable(R.mipmap.state_2));
                    }
                }
                _isType = !_isType;
                fragment5Tv[0].setText(rRealData.getInt("i_Capv",0)+"%");

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

//                fragment5Dv[0].setPercent((int)(Math.random()*100));


            }

        }
    };
}
