package com.great.grt_vdc_t4200l.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.R;

import java.util.LinkedList;

public class shortItemAdapter extends BaseAdapter {

    private LinkedList<shortItem> mData;
    private Context mContext;
    private String mType;

    public shortItemAdapter(LinkedList<shortItem> mData, Context mContext,String type){
        this.mData = mData;
        this.mContext = mContext;
        this.mType = type;
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
        ViewHolder holder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.short_item,parent,false);
            holder = new ViewHolder();
            holder.txt_mNumbar = convertView.findViewById(R.id.txt_mNumbar);
            holder.txt_mContent = convertView.findViewById(R.id.txt_mContent);
            holder.txt_mHint = convertView.findViewById(R.id.txt_mHint);
            holder.txt_mUrl = convertView.findViewById(R.id.txt_mUrl);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txt_mNumbar.setText(mData.get(position).getNumber());
        holder.txt_mContent.setText(mData.get(position).getContent());
        holder.txt_mHint.setText(mData.get(position).getHint());
        holder.txt_mUrl.setText(mData.get(position).getUrl());

        switch (mType){
            case "SearchItem":

                holder.txt_mNumbar.setVisibility(View.VISIBLE);
                holder.txt_mContent.setVisibility(View.VISIBLE);

                holder.txt_mHint.setVisibility(View.VISIBLE);
                holder.txt_mUrl.setVisibility(View.VISIBLE);

//                AbsListView.LayoutParams params = new AbsListView.LayoutParams(400,);
//                convertView.setLayoutParams(params);
                break;
            case "SetItem":

                holder.txt_mNumbar.setVisibility(View.GONE);
                holder.txt_mContent.setVisibility(View.VISIBLE);
                holder.txt_mContent.setTextSize(32);
                holder.txt_mHint.setVisibility(View.GONE);
                holder.txt_mUrl.setVisibility(View.GONE);

                AbsListView.LayoutParams params = new AbsListView.LayoutParams(400,55);
                convertView.setLayoutParams(params);
                break;
        }

        return convertView;
    }

    static class ViewHolder{
        TextView txt_mNumbar;
        TextView txt_mContent;
        TextView txt_mHint;
        TextView txt_mUrl;
    }
}
