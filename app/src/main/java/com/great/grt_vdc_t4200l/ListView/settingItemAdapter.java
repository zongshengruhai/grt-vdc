package com.great.grt_vdc_t4200l.ListView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.RecognitionService;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.R;
import com.great.grt_vdc_t4200l.SystemFunc;

import java.util.LinkedList;

public class settingItemAdapter extends BaseAdapter{

    private LinkedList<settingItem> mData;
    private Context mContext;
    private String mType;

    private Intent fragment4List = new Intent("drc.xxx.yyy.baseActivity");

    public settingItemAdapter(LinkedList<settingItem> mData,Context mContext,String mType){
        this.mData = mData;
        this.mContext = mContext;
        this.mType = mType;
    }

    @Override
    public int getCount(){
        return mData.size();
    }

    @Override
    public Object getItem(int position){
        return null;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){

//        ViewContentHolder holder;
//        if (convertView != null){
//            convertView = LayoutInflater.from(mContext).inflate(R.layout.setting_item,parent,false);
//            holder = new ViewContentHolder();
//            holder.txt_Name = convertView.findViewById(R.id.SettingName);
//            holder.txt_Value = convertView.findViewById(R.id.SettingEd);
//            holder.txt_But = convertView.findViewById(R.id.SettingBut);
//            convertView.setTag(holder);
//        }else {
//            holder = (ViewContentHolder) convertView.getTag();
//        }
//
//        holder.txt_Name.setText(mData.get(position).getmName());
//        holder.txt_But.setText(mData.get(position).getmBtName());

        convertView = LayoutInflater.from(mContext).inflate(R.layout.setting_item, parent, false);
        //基础关联
        final TextView txt_Name = convertView.findViewById(R.id.SettingName);
        final EditText txt_Value = convertView.findViewById(R.id.SettingEd);
        final Button txt_But = convertView.findViewById(R.id.SettingBut);
        final Switch txt_Swtich =convertView.findViewById(R.id.SettingSwitch);

        //遥调
        if (mData.get(position).getmBtName().equals("校准")|| mData.get(position).getmName().equals("设置")) {

            txt_But.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] data = new String[3];
                    data[0] = txt_Name.getText().toString();
                    data[1] = txt_Value.getText().toString();
                    data[2] = txt_But.getText().toString();
                    fragment4List.putExtra("UserSet", data);
                    mContext.sendBroadcast(fragment4List);
                }
            });

            txt_Name.setText(mData.get(position).getmName());
            txt_But.setText(mData.get(position).getmBtName());

            //处理控件
            txt_Name.setVisibility(View.VISIBLE);
            txt_Value.setVisibility(View.VISIBLE);
            txt_But.setVisibility(View.VISIBLE);
            txt_Swtich.setVisibility(View.GONE);

            if (txt_Name.getText().toString().equals("系统时间:")){
                txt_Value.setInputType(4);
                txt_Value.setTextSize(20);
                txt_Value.setInputType(InputType.TYPE_NULL);
                txt_Value.setFilters(new InputFilter[]{new InputFilter.LengthFilter(19)});
                txt_Value.setHint(SystemFunc.getNewTime());
            }

        }//遥控
        else{

            //获取当前的设置状态
            SharedPreferences rRealData = mContext.getSharedPreferences("RealData", 0);
            SharedPreferences rStateData = mContext.getSharedPreferences("StateData", 0);
            switch (mData.get(position).getmName()){
                case "系统模式:":
//                    boolean flag = rRealData.getBoolean("is_SystemMode",false);
                    txt_Swtich.setChecked((rRealData.getBoolean("is_SystemMode",false)));
                    txt_Swtich.setTextOff("手动");
                    txt_Swtich.setTextOn("自动");
                    break;
                case "补偿使能:":
                    txt_Swtich.setChecked((rRealData.getBoolean("is_CompensateEnabled",false)));
                    txt_Swtich.setTextOff("禁止");
                    txt_Swtich.setTextOn("使能");
                    break;
                case "告警提示:":
                    txt_Swtich.setChecked((rStateData.getBoolean("is_SystemBeep",false)));
                    txt_Swtich.setTextOff("关闭");
                    txt_Swtich.setTextOn("打开");
                    break;
            }

            //设置switchUi
            if (txt_Swtich.isChecked()){
                txt_Swtich.setSwitchTextAppearance(mContext,R.style.s_true);
            }else {
                txt_Swtich.setSwitchTextAppearance(mContext,R.style.s_false);
            }

            //switch监听
            txt_Swtich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        txt_Value.setText("1");
                    } else {
                        txt_Value.setText("0");
                    }

                    String[] data = new String[3];
                    data[0] = txt_Name.getText().toString();
                    data[1] = txt_Value.getText().toString();
                    data[2] = txt_But.getText().toString();
                    fragment4List.putExtra("UserSet", data);
                    mContext.sendBroadcast(fragment4List);
                }
            });

            txt_Name.setText(mData.get(position).getmName());
            txt_But.setText(mData.get(position).getmBtName());

            //处理控件
            txt_Name.setVisibility(View.VISIBLE);
            txt_Value.setVisibility(View.GONE);
            txt_But.setVisibility(View.GONE);
            txt_Swtich.setVisibility(View.VISIBLE);
        }

        //设置List高度和宽度
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(640, 70);
        convertView.setLayoutParams(params);

        return convertView;
    }

    static class ViewContentHolder{
        TextView txt_Name;
        EditText txt_Value;
        Button txt_But;
    }
}
