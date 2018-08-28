package com.great.grt_vdc_t4200l.TabLayoutHalp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.great.grt_vdc_t4200l.R;

public class OutputVoltage extends Fragment {

    public static Fragment newInstance(){
        OutputVoltage fragment = new OutputVoltage();
        return fragment;
    }
    /*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.output_voltage,null);
        //return view;
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