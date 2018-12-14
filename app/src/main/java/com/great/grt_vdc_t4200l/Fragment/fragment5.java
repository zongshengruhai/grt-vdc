package com.great.grt_vdc_t4200l.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.ListView.longItem;
import com.great.grt_vdc_t4200l.ListView.longItemAdapter;
import com.great.grt_vdc_t4200l.R;
import com.great.grt_vdc_t4200l.SystemFunc;

import java.util.LinkedList;
import java.util.List;


/**
 * fragment5 主界面
 */
public class fragment5 extends Fragment {

    private Context fragment5_Context;

    private ImageView fragment5Map;
    private TextView[] fragment5Tv = new TextView[14];
    private boolean _isType =false;
    private boolean[] _isYxOld = new boolean[6];

    private ListView fragment5_Alarm_List;


    /**
     * fragment生命周期
     */
    //创建
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment5,container,false);

        fragment5_Context = getContext();

        fragment5Tv[0] = view.findViewById(R.id.nowSystemTime);

        fragment5Tv[1] = view.findViewById(R.id.fragment5_out_Uv);
        fragment5Tv[2] = view.findViewById(R.id.fragment5_out_Vv);
        fragment5Tv[3] = view.findViewById(R.id.fragment5_out_Wv);

        fragment5Tv[4] = view.findViewById(R.id.fragment5_out_Ua);
        fragment5Tv[5] = view.findViewById(R.id.fragment5_out_Va);
        fragment5Tv[6] = view.findViewById(R.id.fragment5_out_Wa);

        fragment5Tv[7] = view.findViewById(R.id.fragment5_in_Rv);
        fragment5Tv[8] = view.findViewById(R.id.fragment5_in_Sv);
        fragment5Tv[9] = view.findViewById(R.id.fragment5_in_Tv);

        fragment5Tv[10] = view.findViewById(R.id.fragment5_cap_v);
        fragment5Tv[11] = view.findViewById(R.id.fragment5_event_time);
        fragment5Tv[12] = view.findViewById(R.id.fragment5_hz);

        fragment5Tv[13] = view.findViewById(R.id.fragment5_cap);

        //系统流向
        fragment5Map = view.findViewById(R.id.fragment5_map);

        fragment5_Alarm_List = view.findViewById(R.id.fragment5_alarm_list);



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
                            fragment5Map.setImageDrawable(getResources().getDrawable(R.mipmap.state_3));
                        }else {
                            fragment5Map.setImageDrawable(getResources().getDrawable(R.mipmap.state_4));
                        }

                    }else if (!_isYx[0] && _isYx[1]) {
                        if (_isType) {
                            fragment5Map.setImageDrawable(getResources().getDrawable(R.mipmap.state_5));
                        }else {
                            fragment5Map.setImageDrawable(getResources().getDrawable(R.mipmap.state_6));
                        }
                    }else {
                        if (_isType) {
                            fragment5Map.setImageDrawable(getResources().getDrawable(R.mipmap.state_1));
                        }else {
                            fragment5Map.setImageDrawable(getResources().getDrawable(R.mipmap.state_2));
                        }
                    }
                    _isType = !_isType;
                    fragment5Tv[13].setText(String.format(getResources().getString(R.string.fragment5CapI), rRealData.getInt("i_CapAh", 0)));

                    //遥信
                    fragment5Tv[0].setText(SystemFunc.getNewTimeString());

                    fragment5Tv[1].setText(String.format(getResources().getString(R.string.fragment5_v),rRealData.getInt("i_Uv", 0)));
                    fragment5Tv[2].setText(String.format(getResources().getString(R.string.fragment5_v),rRealData.getInt("i_Vv", 0)));
                    fragment5Tv[3].setText(String.format(getResources().getString(R.string.fragment5_v),rRealData.getInt("i_Wv", 0)));

                    fragment5Tv[4].setText(String.format(getResources().getString(R.string.fragment5_a),rRealData.getInt("i_Ua", 0)));
                    fragment5Tv[5].setText(String.format(getResources().getString(R.string.fragment5_a),rRealData.getInt("i_Va", 0)));
                    fragment5Tv[6].setText(String.format(getResources().getString(R.string.fragment5_a),rRealData.getInt("i_Wa", 0)));

                    fragment5Tv[7].setText(String.format(getResources().getString(R.string.fragment5_v),rRealData.getInt("i_Rv", 0)));
                    fragment5Tv[8].setText(String.format(getResources().getString(R.string.fragment5_v),rRealData.getInt("i_Sv", 0)));
                    fragment5Tv[9].setText(String.format(getResources().getString(R.string.fragment5_v),rRealData.getInt("i_Tv", 0)));

                    fragment5Tv[10].setText(String.format(getResources().getString(R.string.fragment5_v),rAlarmData.getInt("i_Capv", 0)));
                    fragment5Tv[11].setText(String.format(getResources().getString(R.string.fragment5_Time),rRealData.getInt("i_RecordTime", 0)));
                    fragment5Tv[12].setText(String.format(getResources().getString(R.string.fragment5_Hz),(rRealData.getFloat("f_Hz",0))+""));

                    //遥信
                    _isYx[2] = rRealData.getBoolean("is_InAlarm",false);                //输入异常
                    _isYx[3] = rRealData.getBoolean("is_OutOC",false);                  //输出过流
                    _isYx[4] = rRealData.getBoolean("is_OutRl",false);                  //输出短路
                    _isYx[5] = rRealData.getBoolean("is_AhLose",false);                 //容量失效
                    _isYx[6] = rRealData.getBoolean("is_ComError",false);               //通讯异常

//                    _isYx[2] = true;
//                    _isYx[3] = true;
//                    _isYx[4] = true;
//                    _isYx[5] = true;
//                    _isYx[6] = true;

                    if (_isYxOld[0] != _isYx[2] || _isYxOld[1] != _isYx[3] || _isYxOld[2] != _isYx[4] || _isYxOld[3] != _isYx[5] || _isYxOld[4] != _isYx[6]){
                        List<longItem> fragment5_Data = new LinkedList<>();

                        System.arraycopy(_isYx,2,_isYxOld,0,5);

                        if (_isYx[2]) fragment5_Data.add(new longItem("     ","     ","     ","输入异常"));

                        if (_isYx[3])fragment5_Data.add(new longItem("     ","     ","     ","输出过流"));

                        if (_isYx[4])fragment5_Data.add(new longItem("     ","     ","     ","输出短路"));

                        if (_isYx[5])fragment5_Data.add(new longItem("     ","     ","     ","容量失效"));

                        if (_isYx[6])fragment5_Data.add(new longItem("     ","     ","     ","通讯异常"));

                        if (!_isYx[2] && !_isYx[3] && !_isYx[4] && !_isYx[5] && !_isYx[6])fragment5_Data.add(new longItem("     ","     ","     ","当前暂无异常告警"));


                        longItemAdapter fragment1_RecordAdapter = new longItemAdapter((LinkedList<longItem>) fragment5_Data,fragment5_Context,"fragment5_alarm");
                        fragment5_Alarm_List.setAdapter(fragment1_RecordAdapter);

                    }else if (!_isYx[2] && !_isYx[3] && !_isYx[4] && !_isYx[5] && !_isYx[6]){
                        List<longItem> fragment5_Data = new LinkedList<>();
                        fragment5_Data.add(new longItem("     ","     ","     ","当前暂无异常告警"));
                        longItemAdapter fragment1_RecordAdapter = new longItemAdapter((LinkedList<longItem>) fragment5_Data,fragment5_Context,"fragment5_alarm");
                        fragment5_Alarm_List.setAdapter(fragment1_RecordAdapter);
                    }


                }


        }
    };
}
