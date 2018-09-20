package com.great.grt_vdc_t4200l.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.ListView;


import com.great.grt_vdc_t4200l.ListView.fragment3Item;
import com.great.grt_vdc_t4200l.ListView.fragment3ItemAdapter;
import com.great.grt_vdc_t4200l.R;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import static com.great.grt_vdc_t4200l.SystemFunc.checkFileExist;

public class fragment3 extends Fragment{

//    private String TAG = "fragment3";

    private Context fragment3_Context;
    private ListView fragment3_ListView;
    private List<fragment3Item> fragment3_Data  = new LinkedList<>();
    private fragment3ItemAdapter fragment3_RecordAdapter;

    //防止并发
//    private boolean findFlag = false;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment3,container,false);

        fragment3_Context = view.getContext();
        fragment3_ListView = view.findViewById(R.id.fragment3_ListView);

        findEventTime();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        findEventTime();
//        f3_UiHandler.post(f3_UiRunnable);
    }

    @Override
    public void onPause(){
        super.onPause();
//        f3_UiHandler.removeCallbacks(f3_UiRunnable);
    }

//    Handler f3_UiHandler = new Handler();
//    Runnable f3_UiRunnable = new Runnable() {
//        @Override
//        public void run() {
//            f3_UiHandler.postDelayed(this,1000);
//
//            SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
//
//            if(rStateData.getInt("layPage",0) == 3){
//                //            findEventTime();
//                if (!findFlag){ new findEventThread().start(); }
//            }
//        }
//    };

    /**
     * 更新数据子线程
     */
    private void findEventTime() {
//    class findEventThread extends Thread {
//        public void run() {

//            findFlag = true;

            int rows;
            String PATH = fragment3_Context.getFilesDir().getPath() + "/fault_log/fault_record.xls";

            String[] temp = new String[4];

            if (checkFileExist(PATH)) {
                fragment3_Data.clear();
                try {

                    FileInputStream mfis = new FileInputStream(PATH);
                    Workbook mbook = Workbook.getWorkbook(mfis);
                    int msheer = mbook.getNumberOfSheets();                     //表数量
                    Sheet[] mSheetlist = mbook.getSheets();                     //表内容

                    for (int i = 0; i < msheer; i++) {
                        rows = mSheetlist[i].getRows();
                        for (int j = 0; j < rows; j++) {
                            Cell[] cellList = mSheetlist[i].getRow(j);
                            for (Cell cell : cellList) {
                                temp[cell.getColumn()] = cell.getContents();
                            }
                            fragment3_Data.add(new fragment3Item(temp[0], temp[1], temp[2], temp[3]));
//                       fragment3_ListView.setOnItemClickListener(this);
                        }
                        if (rows == 1) {
                            temp[0] = "提示";
                            temp[1] = "当前暂无故障记录";
                            temp[2] = "";
                            temp[3] = "";
                            fragment3_Data.add(new fragment3Item(temp[0], temp[1], temp[2], temp[3]));
                        }
                        fragment3_RecordAdapter = new fragment3ItemAdapter((LinkedList<fragment3Item>) fragment3_Data, fragment3_Context);
                        fragment3_ListView.setAdapter(fragment3_RecordAdapter);
                    }

                    mbook.close();


                } catch (Exception e) {
                    System.out.println("fragment3,Exception: " + e);
                }
            }

//            findFlag = false;
//        }
    }

}

