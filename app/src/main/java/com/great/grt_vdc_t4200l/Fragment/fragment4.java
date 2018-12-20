package com.great.grt_vdc_t4200l.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.ListView.longItem;
import com.great.grt_vdc_t4200l.ListView.longItemAdapter;
import com.great.grt_vdc_t4200l.ListView.settingItem;
import com.great.grt_vdc_t4200l.ListView.settingItemAdapter;
import com.great.grt_vdc_t4200l.R;

import java.util.LinkedList;
import java.util.List;

/**
 * fragment4 设置
 */
public class fragment4 extends Fragment implements AdapterView.OnItemClickListener{

    //define
    private int USER_TYPE = 0;    //log in user type

    //define row
    private Context fragment4_Context;
    private View fragment4_View;
    private RelativeLayout[] fragment4RowLay = new RelativeLayout[2];

    //define broadcast
    private fragment4.fragment4Broad fragment4ActivityBroad = null;
    private IntentFilter fragment4IntentFilter = new IntentFilter("drc.xxx.yyy.fragment4");
    Intent fragment4Intent = new Intent("drc.xxx.yyy.baseActivity");

    //define login
    private EditText fragment4Password;

    //define ListView
    private ListView pickList;
    private ListView pickContent;
    private List<settingItem> pickContentData = new LinkedList<>();
//    private settingItemAdapter pickContentAdapter;
    private TextView fragment4SetHint;

    /**
     * Fragment Life Cycle
     * dispose life flow
     */
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment4,container,false);
        fragment4_View = view;
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
     * login event
     */
    private void LoginClick()// login click event
    {
        String passwordIn = fragment4Password.getText().toString();
//        SharedPreferences rStateData = fragment4_Context.getSharedPreferences("StateData", 0);

        int passwordSize = passwordIn.length();

        if (passwordSize == 0){
            fragment4Intent.putExtra("fragmentToast","请输入密码");
        }else if (passwordSize < 8){
            fragment4Intent.putExtra("fragmentToast","输入的密码长度不足，请输入8位数字密码");
        } else if (passwordIn.equals("12345678")){
            USER_TYPE = 1;
            fragment4Intent.putExtra("fragmentToast","管理员,登录成功");
//        }else if (passwordIn.equals(rStateData.getString("i_UserPassword", "999999999"))){
        }else if (passwordIn.equals("99999999")){
            USER_TYPE = 2;
            fragment4Intent.putExtra("fragmentToast","登录成功");
        } else {
            fragment4Intent.putExtra("fragmentToast","输入密码错误，请重新输入");
        }

//        SystemFunc.changeKeyboardView(fragment4_Context,fragment4_View,"hide");
        showFragment4RL(USER_TYPE);
        fragment4Password.setText("");
        getActivity().sendBroadcast(fragment4Intent);

    }

    private void hideFragment4Rl()// hide set view
    {
        USER_TYPE = 0;
//        SystemFunc.changeKeyboardView(fragment4_Context,fragment4_View,"hide");
//        fragment4RowLay[0].setVisibility(View.VISIBLE);
//        fragment4RowLay[1].setVisibility(View.GONE);
        fragment4RowLay[0].setVisibility(View.GONE);
        fragment4RowLay[1].setVisibility(View.VISIBLE);
    }

    private void showFragment4RL(int userType)// show set view
    {
        if (userType > 0){
//            fragment4RowLay[0].setVisibility(View.GONE);
//            fragment4RowLay[1].setVisibility(View.VISIBLE);
            fragment4RowLay[0].setVisibility(View.GONE);
            fragment4RowLay[1].setVisibility(View.VISIBLE);
            ChangeSetLay(0);
        }
    }

    /**
     * list event
     */
    private void initPickList() // init right list
    {
        List<longItem> pickListData = new LinkedList<>();
        longItemAdapter pickListAdapter;

        pickListData.add(new longItem("","输入校准","",""));
        pickListData.add(new longItem("","输出校准","",""));
        pickListData.add(new longItem("","电流校准","",""));
        pickListData.add(new longItem("","电容校准","",""));
        pickListData.add(new longItem("","遥控设置","",""));
        pickListData.add(new longItem("","系统时间","",""));
        pickListData.add(new longItem("","系统设置","",""));

        pickListAdapter = new longItemAdapter((LinkedList<longItem>)pickListData,fragment4_Context,"fragment4");
        pickList.setAdapter(pickListAdapter);
        pickList.setOnItemClickListener(this);
        ChangeSetLay(0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent,View view,int position,long id) // select list event
    {
        ChangeSetLay(position);
    }

    private void ChangeSetLay(int id) // change set list
    {
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
                break;
            case 2:
                pickContentData.add(new settingItem("U相输出电流:","校准"));
                pickContentData.add(new settingItem("V相输出电流:","校准"));
                pickContentData.add(new settingItem("W相输出电流:","校准"));
                break;
            case 3:
                fragment4SetHint.setText("电容校准");
                pickContentData.add(new settingItem("电容容量:","校准"));
                break;
            case 4:
                fragment4SetHint.setText("遥控设置");
                pickContentData.add(new settingItem("系统模式:","遥控"));
                pickContentData.add(new settingItem("补偿使能:","遥控"));
                pickContentData.add(new settingItem("hide","校准"));
                pickContentData.add(new settingItem("启动补偿:","校准"));
//                pickContentData.add(new settingItem("停止补偿:","校准"));
                break;
            case 5:
                fragment4SetHint.setText("系统时间");
                pickContentData.add(new settingItem("年:","对时"));
                pickContentData.add(new settingItem("月:","对时"));
                pickContentData.add(new settingItem("日:","对时"));
                pickContentData.add(new settingItem("时:","对时"));
                pickContentData.add(new settingItem("分:","对时"));
                pickContentData.add(new settingItem("秒:","对时"));
                break;
            case 6:
                fragment4SetHint.setText("系统设置");
                pickContentData.add(new settingItem("告警提示:","开关"));
//                if (USER_TYPE == 1){
                    pickContentData.add(new settingItem("调试模式:","开关"));
//                    pickContentData.add(new settingItem("Loge输出:","开关"));
                    pickContentData.add(new settingItem("初始化系统:","开关"));
                    pickContentData.add(new settingItem("退出程序:","开关"));
//                    pickContentData.add(new settingItem("用户密码:","更改"));
//                }
//                else if (USER_TYPE == 2){
//                    pickContentData.add(new settingItem("用户密码:","更改"));
//                }
//                pickContentData.add(new settingItem("系统时间:","校准"));
                break;
        }

        settingItemAdapter pickContentAdapter;
        pickContentAdapter = new settingItemAdapter((LinkedList<settingItem>)pickContentData,fragment4_Context,fragment4_View);
        pickContentAdapter.clearEditorData();
        pickContent.setAdapter(pickContentAdapter);
//        pickContent.setOnItemClickListener(this);
    }

    /**
     * fragment4广播
     * 接受MainActivity发来的广播
     * 用于hide设置
     */
    public class fragment4Broad extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent){

            int layType = intent.getIntExtra("layChange",0);
            if (layType != 4){
                hideFragment4Rl();
            }

        }
    }



}

