package com.great.grt_vdc_t4200l.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.R;

import java.util.LinkedList;

public class recordAdapter extends BaseAdapter {

    private LinkedList<record> mData;
    private Context mContext;

    public recordAdapter(LinkedList<record> mData,Context mContext){
        this.mData = mData;
        this.mContext = mContext;
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
    public View getView(int position, View convertView, ViewGroup parent){
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_record,parent,false);

        TextView txt_mNumbar = (TextView) convertView.findViewById(R.id.txt_mNumbar);
        TextView txt_mContent = (TextView) convertView.findViewById(R.id.txt_mContent);
        TextView txt_mHint = (TextView) convertView.findViewById(R.id.txt_mHint);
        TextView txt_mUrl = (TextView) convertView.findViewById(R.id.txt_mUrl);

        txt_mNumbar.setText(mData.get(position).getNumber());
        txt_mContent.setText(mData.get(position).getContent());
        txt_mHint.setText(mData.get(position).getHint());
        txt_mUrl.setText(mData.get(position).getUrl());

        return convertView;
    }

}
