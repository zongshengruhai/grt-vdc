package com.great.grt_vdc_t4200l.ListView;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.R;

import java.util.LinkedList;

public class longItemAdapter extends BaseAdapter {

    private LinkedList<longItem> mData;
    private Context mContext;
    private String mType;

    public longItemAdapter(LinkedList<longItem> mData, Context mContext,String mType){
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
    public View getView(int position, View convertView, ViewGroup parent){

        mViewHolder mHolder;

        if (convertView == null){

            convertView = LayoutInflater.from(mContext).inflate(R.layout.long_item,parent,false);

            mHolder = new mViewHolder();
            mHolder.mTxt_1 = convertView.findViewById(R.id.event_Number);
            mHolder.mTxt_2 = convertView.findViewById(R.id.event_Content);
            mHolder.mTxt_3 = convertView.findViewById(R.id.event_start_time);
            mHolder.mTxt_4 = convertView.findViewById(R.id.event_end_time);

            convertView.setTag(mHolder);

        }else {
            mHolder = (mViewHolder) convertView.getTag();
        }

        //获取数据
        mHolder.mTxt_1.setText(mData.get(position).getNumber());
        mHolder.mTxt_2.setText(mData.get(position).getContent());
        mHolder.mTxt_3.setText(mData.get(position).getStartTime());
        mHolder.mTxt_4.setText(mData.get(position).getEndTime());

        switch (mType){
            //fragment1 实时数据
            case "fragment1":

                //设置字体大小
                mHolder.mTxt_1.setTextSize(26);
                mHolder.mTxt_2.setTextSize(26);
                mHolder.mTxt_3.setTextSize(26);
                mHolder.mTxt_4.setTextSize(26);

                //设置权重
                mHolder.mTxt_1.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0f));
                mHolder.mTxt_2.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0f));
                mHolder.mTxt_3.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3f));
                mHolder.mTxt_4.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3f));

                //对齐方式
                mHolder.mTxt_1.setGravity(Gravity.CENTER);
                mHolder.mTxt_2.setGravity(Gravity.CENTER);
                mHolder.mTxt_3.setGravity(Gravity.CENTER);
                mHolder.mTxt_4.setGravity(Gravity.CENTER);

                //处理内容
                mHolder.mTxt_1.setVisibility(View.GONE);
                mHolder.mTxt_2.setVisibility(View.GONE);
                mHolder.mTxt_3.setVisibility(View.VISIBLE);
                mHolder.mTxt_4.setVisibility(View.VISIBLE);

                break;
            //fragment2 录波
            case "fragment2":

                //设置字体大小
                mHolder.mTxt_1.setTextSize(20);
                mHolder.mTxt_2.setTextSize(20);
                mHolder.mTxt_3.setTextSize(20);
                mHolder.mTxt_4.setTextSize(20);

                //设置权重
                mHolder.mTxt_1.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
                mHolder.mTxt_2.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,6f));
                mHolder.mTxt_3.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3f));
                mHolder.mTxt_4.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0f));

                //处理内容
                mHolder.mTxt_1.setVisibility(View.VISIBLE);
                mHolder.mTxt_2.setVisibility(View.VISIBLE);
                mHolder.mTxt_3.setVisibility(View.GONE);
                mHolder.mTxt_4.setVisibility(View.GONE);

                break;
            //fragment3 告警
            case "fragment3":

                //设置字体大小
                mHolder.mTxt_1.setTextSize(22);
                mHolder.mTxt_2.setTextSize(22);
                mHolder.mTxt_3.setTextSize(22);
                mHolder.mTxt_4.setTextSize(22);

                //设置权重
                mHolder.mTxt_1.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
                mHolder.mTxt_2.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2f));
                mHolder.mTxt_3.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3.5f));
                mHolder.mTxt_4.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3.5f));

                //对齐方式
                mHolder.mTxt_1.setGravity(Gravity.CENTER);
                mHolder.mTxt_2.setGravity(Gravity.CENTER);
                mHolder.mTxt_3.setGravity(Gravity.CENTER);
                mHolder.mTxt_4.setGravity(Gravity.CENTER);

                break;
            //fragment4 设置
            case "fragment4":

                //设置字体大小
                mHolder.mTxt_1.setTextSize(32);
                mHolder.mTxt_2.setTextSize(32);
                mHolder.mTxt_3.setTextSize(32);
                mHolder.mTxt_4.setTextSize(32);

                //设置权重
                mHolder.mTxt_1.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
                mHolder.mTxt_2.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,6f));
                mHolder.mTxt_3.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3f));
                mHolder.mTxt_4.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0f));

                //处理内容
                mHolder.mTxt_1.setVisibility(View.GONE);
                mHolder.mTxt_2.setVisibility(View.VISIBLE);
                mHolder.mTxt_3.setVisibility(View.GONE);
                mHolder.mTxt_4.setVisibility(View.GONE);

                break;
            case "fragment5":
                //设置字体大小
                mHolder.mTxt_1.setTextSize(21);
                mHolder.mTxt_2.setTextSize(21);
                mHolder.mTxt_3.setTextSize(21);
                mHolder.mTxt_4.setTextSize(21);

                //设置权重
                mHolder.mTxt_1.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0f));
                mHolder.mTxt_2.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,0f));
                mHolder.mTxt_3.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3f));
                mHolder.mTxt_4.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3f));

                //对齐方式
                mHolder.mTxt_1.setGravity(Gravity.CENTER);
                mHolder.mTxt_2.setGravity(Gravity.CENTER);
                mHolder.mTxt_3.setGravity(Gravity.CENTER);
                mHolder.mTxt_4.setGravity(Gravity.CENTER);

                //处理内容
                mHolder.mTxt_1.setVisibility(View.GONE);
                mHolder.mTxt_2.setVisibility(View.GONE);
                mHolder.mTxt_3.setVisibility(View.VISIBLE);
                mHolder.mTxt_4.setVisibility(View.VISIBLE);
                break;
            case "fragment5_alarm":
                //设置字体大小
                mHolder.mTxt_1.setTextSize(21);
                mHolder.mTxt_2.setTextSize(21);
                mHolder.mTxt_3.setTextSize(21);
                mHolder.mTxt_4.setTextSize(21);

                //设置权重
                mHolder.mTxt_1.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2f));
                mHolder.mTxt_2.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2f));
                mHolder.mTxt_3.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,2f));
                mHolder.mTxt_4.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f));

                if (!mHolder.mTxt_4.getText().toString().equals("当前暂无异常告警")) mHolder.mTxt_4.setTextColor(Color.RED);

                //对齐方式
                mHolder.mTxt_1.setGravity(Gravity.CENTER);
                mHolder.mTxt_2.setGravity(Gravity.CENTER);
                mHolder.mTxt_3.setGravity(Gravity.CENTER);
                mHolder.mTxt_4.setGravity(Gravity.CENTER);

                //处理内容
                mHolder.mTxt_1.setVisibility(View.GONE);
                mHolder.mTxt_2.setVisibility(View.GONE);
                mHolder.mTxt_3.setVisibility(View.GONE);
                mHolder.mTxt_4.setVisibility(View.VISIBLE);
                break;
        }

        //List高度
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        convertView.setLayoutParams(params);

        return convertView;
    }

    static class mViewHolder{
        TextView mTxt_1;
        TextView mTxt_2;
        TextView mTxt_3;
        TextView mTxt_4;
    }

}
