package com.great.grt_vdc_t4200l.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.great.grt_vdc_t4200l.R;

public class fragment5 extends Fragment {

    private ImageView batterIv;
    private ImageView[] arrow = new ImageView[4];

    private static final String TAG = "fragment5";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment5,container,false);

        batterIv = (ImageView) view.findViewById(R.id.batter);
        //arrow[0] = (ImageView) view.findViewById(R.id.rightAcdcArrow);
        //arrow[1] = (ImageView) view.findViewById(R.id.leftAcdcArrow);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    //重载
    @Override
    public void onResume(){
        super.onResume();

        fragment5Handler.post(fragment5Runable);
    }

    //中止
    @Override
    public void onPause(){
        super.onPause();

        fragment5Handler.removeCallbacks(fragment5Runable);
    }

    @Override
    public void onStop(){
        super.onStop();
        //Log.e(TAG, "碎片1，停止");
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        //Log.e(TAG, "碎片1，销毁视图");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //Log.e(TAG, "碎片1，销毁");
    }

    @Override
    public void onDetach(){
        super.onDetach();
        // Log.e(TAG, "碎片1，解除活动绑定");
    }

    Handler fragment5Handler = new Handler();
    Runnable fragment5Runable = new Runnable() {
        @Override
        public void run() {
            fragment5Handler.postDelayed(this,500);

            SharedPreferences fragment5readSp = getActivity().getSharedPreferences("realTimeData", 0);
            int layPage = fragment5readSp.getInt("layPage",0);

            if (layPage == 0) {

                //电池
                int batterCapacity = fragment5readSp.getInt("batterCapacity", 0);
                if (batterCapacity == 0) {
                    batterIv.setImageDrawable(getResources().getDrawable(R.mipmap.batter_0));
                } else if (batterCapacity > 0 && batterCapacity < 20) {
                    batterIv.setImageDrawable(getResources().getDrawable(R.mipmap.batter_1));
                } else if (batterCapacity > 19 && batterCapacity < 40) {
                    batterIv.setImageDrawable(getResources().getDrawable(R.mipmap.batter_2));
                } else if (batterCapacity > 39 && batterCapacity < 60) {
                    batterIv.setImageDrawable(getResources().getDrawable(R.mipmap.batter_3));
                } else if (batterCapacity > 59 && batterCapacity < 80) {
                    batterIv.setImageDrawable(getResources().getDrawable(R.mipmap.batter_4));
                } else if (batterCapacity > 79) {
                    batterIv.setImageDrawable(getResources().getDrawable(R.mipmap.batter_5));
                }

                int vdcSignal = fragment5readSp.getInt("signal",0);


            }
        }
    };
}
