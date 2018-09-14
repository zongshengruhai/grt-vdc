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
import android.widget.TextView;

import com.great.grt_vdc_t4200l.R;

public class fragment5 extends Fragment {

    private static final String TAG = "fragment5";
    private ImageView[] fragment5Map = new ImageView[1];
    private TextView[] fragment5Tv = new TextView[1];
    private boolean _isType =false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment5,container,false);

        fragment5Map[0] = view.findViewById(R.id.fragment5_map);
        fragment5Tv[0] = view.findViewById(R.id.fragment5_cap);

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

            }

        }
    };
}
