package com.great.grt_vdc_t4200l.TabLayoutHalp;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.great.grt_vdc_t4200l.MainActivity;
import com.great.grt_vdc_t4200l.R;

public class InputVoltage extends Fragment {

    private static final String TAG = "in";
    private TextView testadd;
    int text;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //testadd.findViewById(R.id.inU);
        //Log.e(TAG,"ViewPAGE");
    }

    @Override
    public void onResume(){
        super.onResume();
        //testadd.setText("");
        Log.e(TAG,"重载");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.e(TAG,"中止");
    }


    public static Fragment newInstance(){
        InputVoltage fragment = new InputVoltage();
        return fragment;
    }
    /*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.input_voltage,null);
        return view;
    }*/

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}