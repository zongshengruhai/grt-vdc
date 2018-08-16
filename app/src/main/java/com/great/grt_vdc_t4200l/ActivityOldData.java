package com.great.grt_vdc_t4200l;

import android.os.Bundle;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ActivityOldData extends BaseCourse {

    private static final String TAG = "OldDataActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_old_data);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/


        Log.e(TAG,"设置层创建成功");

    }


}
