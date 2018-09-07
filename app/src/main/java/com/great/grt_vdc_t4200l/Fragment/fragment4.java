package com.great.grt_vdc_t4200l.Fragment;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TimePicker;

import com.great.grt_vdc_t4200l.R;
import com.great.grt_vdc_t4200l.SystemFunc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.JxlWriteException;

public class fragment4 extends Fragment implements View.OnClickListener{

    private static final String TAG = "fragment4";

    private RelativeLayout fragment4Rlayout[];

    //广播声明
    private fragment4.fragment4Broad fragment4ActivityBroad = null;
    private IntentFilter fragment4IntentFilter = new IntentFilter("drc.xxx.yyy.fragment4");
    Intent fragment4Intent = new Intent("drc.xxx.yyy.baseActivity");

    private Context fragment4_Context;

    //login
    private EditText fragment4Password;
    private Button fragment4Login;

    //button
    private Button[] but = new Button[2];

    //test
    private boolean flga1 = false;
    private boolean flag2 = false;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment4,container,false);

        fragment4_Context = view.getContext();

        but[0] = view.findViewById(R.id.openMiuse);
        but[0].setOnClickListener(this);
        but[1] = view.findViewById(R.id.closeMiuse);
        but[1].setOnClickListener(this);

        fragment4Rlayout = new RelativeLayout[2];
        fragment4Rlayout[0] = view.findViewById(R.id.fragment4Lay1);
        fragment4Rlayout[1] = view.findViewById(R.id.fragment4Lay2);
        hideFragment4Rl();

        fragment4Password = view.findViewById(R.id.fragment4PasswordIn);
        fragment4Login = view.findViewById(R.id.fragment4LoginBut);
        fragment4Login.setOnClickListener(this);
//        fragment4Login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LoginClick();
//            }
//        });

//        pickTime = view.findViewById(R.id.pickTime);
//        pickTime.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
//            case R.id.pickTime:
//                break;
            case R.id.fragment4LoginBut:
                LoginClick();
                break;
            case R.id.openMiuse:
                test1();
                break;
            case R.id.closeMiuse:
                test2();
                break;
        }

    }

    private void test1(){
//        String file =  fragment4_Context.getFilesDir().getPath()+"123.xls";
//        WritableWorkbook test;
//        try {
//            OutputStream os = new FileOutputStream(file);
//            test = Workbook.createWorkbook(os);
//            WritableSheet mSheet = test.createSheet("test",0);
//            String[] list = {"1","2","3"};
//            Label label;
//            for (int i = 0; i < list.length ; i++) {
//                label = new Label(i,0,list[0]);
//                mSheet.addCell(label);
//            }
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }catch (WriteException e){
//            e.printStackTrace();
//        }
        String path = fragment4_Context.getFilesDir().getPath()+"/test.txt";
        if (!SystemFunc.checkFileExist(path)){
            Log.e(TAG, "test1: 没有文件" );
            if (SystemFunc.createFile(path)){
                Log.e(TAG, "test1: 创建文件" );
                boolean a = SystemFunc.writeTxt(path);
                Log.e(TAG, "test1: 写入文件 "+a );
            }
        }
    }
    private void test2(){
        String path = fragment4_Context.getFilesDir().getPath()+"/test.txt";
        SystemFunc.readTxt(path,0);
    }

    Handler mHandler = new Handler();
    Runnable mRunable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this,500);

        }
    };
    //登录事件
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
//        fragment4Rlayout[0].setVisibility(View.VISIBLE);
//        fragment4Rlayout[1].setVisibility(View.GONE);
        //tempshow
        fragment4Rlayout[0].setVisibility(View.GONE);
        fragment4Rlayout[1].setVisibility(View.VISIBLE);
    }

    //显示设置界面
    private void showFragment4RL(int userType){

        if (userType > 0){
            fragment4Rlayout[0].setVisibility(View.GONE);
            fragment4Rlayout[1].setVisibility(View.VISIBLE);
        }

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

    @Override
    public void onResume(){
        super.onResume();
        //注册广播
        if (fragment4ActivityBroad == null){
            fragment4ActivityBroad = new fragment4Broad();
            getActivity().registerReceiver(fragment4ActivityBroad,fragment4IntentFilter);
        }
        mHandler.post(mRunable);
    }

    @Override
    public void onPause(){
        super.onPause();
        //注销广播
        if (fragment4ActivityBroad != null){
            getActivity().unregisterReceiver(fragment4ActivityBroad);
            fragment4ActivityBroad = null;
        }
        mHandler.removeCallbacks(mRunable);
    }

}

