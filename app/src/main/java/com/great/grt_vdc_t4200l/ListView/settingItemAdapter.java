package com.great.grt_vdc_t4200l.ListView;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.R;

import java.util.LinkedList;

public class settingItemAdapter extends BaseAdapter{

    private LinkedList<settingItem> mData;
    private Context mContext;
    private String mType;
    Intent fragment4List = new Intent("drc.xxx.yyy.baseActivity");

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
        final TextView txt_Name = convertView.findViewById(R.id.SettingName);
        final EditText txt_Value = convertView.findViewById(R.id.SettingEd);
        final Button txt_But = convertView.findViewById(R.id.SettingBut);
        txt_But.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txt_Value.getText().toString().equals("")){
                    String[] data = new String[3];
                    data[0] = txt_Name.getText().toString();
                    data[1] = txt_Value.getText().toString();
                    data[2] = txt_But.getText().toString();
                    fragment4List.putExtra("UserSet",data);
                    mContext.sendBroadcast(fragment4List);
                }
            }
        });

        txt_Name.setText(mData.get(position).getmName());
        txt_But.setText(mData.get(position).getmBtName());

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
