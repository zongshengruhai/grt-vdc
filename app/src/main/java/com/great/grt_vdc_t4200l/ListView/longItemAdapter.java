package com.great.grt_vdc_t4200l.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.R;

import java.util.LinkedList;

public class longItemAdapter extends BaseAdapter {

    private LinkedList<longItem> mData;
    private Context mContext;

    public longItemAdapter(LinkedList<longItem> mData, Context mContext){
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

        convertView = LayoutInflater.from(mContext).inflate(R.layout.long_item,parent,false);

        TextView mNumber = (TextView) convertView.findViewById(R.id.event_Number);
        TextView mContent = (TextView) convertView.findViewById(R.id.event_Content);
        TextView mStartTime = (TextView) convertView.findViewById(R.id.event_start_time);
        TextView mEndTime = (TextView) convertView.findViewById(R.id.event_end_time);

        mNumber.setText(mData.get(position).getNumber());
        mContent.setText(mData.get(position).getContent());
        mStartTime.setText(mData.get(position).getStartTime());
        mEndTime.setText(mData.get(position).getEndTime());

        return convertView;
    }

}
