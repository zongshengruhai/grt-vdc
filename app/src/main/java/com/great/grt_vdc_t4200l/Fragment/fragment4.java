package com.great.grt_vdc_t4200l.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.ListView.settingItem;
import com.great.grt_vdc_t4200l.ListView.settingItemAdapter;
import com.great.grt_vdc_t4200l.ListView.shortItem;
import com.great.grt_vdc_t4200l.ListView.shortItemAdapter;
import com.great.grt_vdc_t4200l.R;
import com.great.grt_vdc_t4200l.SerialPortHelp.MyFunc;
import com.great.grt_vdc_t4200l.SystemFunc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.great.grt_vdc_t4200l.SystemFunc.Beep;


public class fragment4 extends Fragment implements AdapterView.OnItemClickListener{

    private static final String TAG = "fragment4";

    private RelativeLayout[] fragment4RowLay = new RelativeLayout[2];

    //广播声明
    private fragment4.fragment4Broad fragment4ActivityBroad = null;
    private IntentFilter fragment4IntentFilter = new IntentFilter("drc.xxx.yyy.fragment4");
    Intent fragment4Intent = new Intent("drc.xxx.yyy.baseActivity");

    private Context fragment4_Context;

    //login
    private EditText fragment4Password;

    //ListView
    private ListView pickList;
    private ListView pickContent;

    private List<settingItem> pickContentData = new LinkedList<>();
    private settingItemAdapter pickContentAdapter;

    private TextView fragment4SetHint;


    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment4,container,false);

        fragment4_Context = view.getContext();

        fragment4RowLay[0] = view.findViewById(R.id.fragment4Lay1);
        fragment4RowLay[1] = view.findViewById(R.id.fragment4Lay2);
        hideFragment4Rl();

        fragment4Password = view.findViewById(R.id.fragment4PasswordIn);
        Button fragment4Login = view.findViewById(R.id.fragment4LoginBut);
        fragment4Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginClick();
            }
        });

        fragment4SetHint = view.findViewById(R.id.SettingHint);
        pickContent = view.findViewById(R.id.pickContent);

        pickList = view.findViewById(R.id.pickSet);
        initPickList();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        //注册广播
        if (fragment4ActivityBroad == null){
            fragment4ActivityBroad = new fragment4Broad();
            getActivity().registerReceiver(fragment4ActivityBroad,fragment4IntentFilter);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        //注销广播
        if (fragment4ActivityBroad != null){
            getActivity().unregisterReceiver(fragment4ActivityBroad);
            fragment4ActivityBroad = null;
        }
    }

    /**
     * 登录
     */
    //登录触发
    private void LoginClick(){

        String passwordIn = fragment4Password.getText().toString();
        int passwordSize = passwordIn.length();
        int userType = 0;

        if (passwordSize < 8){
            fragment4Intent.putExtra("fragmentToast",1);
        }else if (passwordSize == 0){
            fragment4Intent.putExtra("fragmentToast",2);
        } else if (passwordIn.equals("12345678")){
            userType = 1;
            fragment4Intent.putExtra("fragmentToast",4);
        }else if (passwordIn.equals("99999999")){
            userType = 2;
            fragment4Intent.putExtra("fragmentToast",5);
        } else {
            fragment4Intent.putExtra("fragmentToast",3);
        }
        showFragment4RL(userType);
        fragment4Password.setText("");
        getActivity().sendBroadcast(fragment4Intent);
    }
    //隐藏设置界面
    private void hideFragment4Rl(){
//        fragment4RowLay[0].setVisibility(View.VISIBLE);
//        fragment4RowLay[1].setVisibility(View.GONE);
        fragment4RowLay[0].setVisibility(View.GONE);
        fragment4RowLay[1].setVisibility(View.VISIBLE);
    }
    //显示设置界面
    private void showFragment4RL(int userType){
        if (userType > 0){
            fragment4RowLay[0].setVisibility(View.GONE);
            fragment4RowLay[1].setVisibility(View.VISIBLE);
            ChangeSetLay(0);
        }
    }

    /**
     * 选择设置
     */
    //初始化选择list
    private void initPickList(){
        List<shortItem> pickListData = new LinkedList<>();
        shortItemAdapter pickListAdapter;

        pickListData.add(new shortItem("","输入校准","",""));
        pickListData.add(new shortItem("","输出校准","",""));
        pickListData.add(new shortItem("","电容校准","",""));
        pickListData.add(new shortItem("","数据限制","",""));
        pickListData.add(new shortItem("","遥控设置","",""));
        pickListData.add(new shortItem("","系统设置","",""));

        pickListAdapter = new shortItemAdapter((LinkedList<shortItem>)pickListData,fragment4_Context,"SetItem");
        pickList.setAdapter(pickListAdapter);
        pickList.setOnItemClickListener(this);
        ChangeSetLay(0);

    }
    //选择list点击事件
    @Override
    public void onItemClick(AdapterView<?> parent,View view,int position,long id){
        ChangeSetLay(position);
    }
    //更新设置界面
    private void ChangeSetLay(int id){
        pickContentData.clear();
        switch (id){
            case 0:
                fragment4SetHint.setText("输入校准");
                pickContentData.add(new settingItem("R相输入电压:","校准"));
                pickContentData.add(new settingItem("S相输入电压:","校准"));
                pickContentData.add(new settingItem("T相输入电压:","校准"));
                break;
            case 1:
                fragment4SetHint.setText("输出校准");
                pickContentData.add(new settingItem("U相输出电压:","校准"));
                pickContentData.add(new settingItem("V相输出电压:","校准"));
                pickContentData.add(new settingItem("W相输出电压:","校准"));
                pickContentData.add(new settingItem("U相输出电流:","校准"));
                pickContentData.add(new settingItem("V相输出电流:","校准"));
                pickContentData.add(new settingItem("W相输出电流:","校准"));
                break;
            case 2:
                fragment4SetHint.setText("电容校准");
                pickContentData.add(new settingItem("电容容量:","校准"));
                break;
            case 3:
                fragment4SetHint.setText("数据限制");
                pickContentData.add(new settingItem("输入电压告警上限:","设置"));
                pickContentData.add(new settingItem("输出电压告警上限:","设置"));
                pickContentData.add(new settingItem("输出电流告警上限:","设置"));
                break;
            case 4:
                fragment4SetHint.setText("遥控设置");
                break;
            case 5:
                fragment4SetHint.setText("系统设置");
                break;
        }
        pickContentAdapter = new settingItemAdapter((LinkedList<settingItem>)pickContentData,fragment4_Context,"0");
        pickContent.setAdapter(pickContentAdapter);
//        pickContent.setOnItemClickListener(this);
    }

    /**
     * Adapter返送
     */
    public void controlDispose(String type,int value){

    }

    //----- fragment4广播 -----//
    //描述：fragment4层接收广播，用于接收BaseCourse底层广播的遥测数据
    //方法：
    //     1）创建方法：fragment活动Resume创建，pause销毁
    //     2）接收Main的Lay切换消息，用于隐藏设置页面
    public class fragment4Broad extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent){

            int layType = intent.getIntExtra("layChange",0);
            if (layType != 4){
                hideFragment4Rl();
            }

        }
    }

}

