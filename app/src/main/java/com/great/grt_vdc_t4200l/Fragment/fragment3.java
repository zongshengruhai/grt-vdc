package com.great.grt_vdc_t4200l.Fragment;

import android.content.Context;
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

public class fragment3 extends Fragment{

    private String TAG = "fragment3";

    private String PATH;
    private Context fragment3_Context;
    private ListView fragment3_ListView;
    private List<fragment3Item> fragment3_Data  = new LinkedList<>();
    private fragment3ItemAdapter fragment3_RecordAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment3,container,false);

        fragment3_Context = view.getContext();
        fragment3_ListView = view.findViewById(R.id.fragment3_ListView);

        PATH = fragment3_Context.getFilesDir().getPath() + "/record_file/event_record.xls";
        PATH = PATH.replace("/files","");

        findEventTime();

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
        findEventTime();
    }

    //中止
    @Override
    public void onPause(){
        super.onPause();
    }

    //停止
    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //更新告警数据
    private void findEventTime(){

        int rows;
        String[] temp = new String[4];

        if (PATH != null){
           try {

               FileInputStream mfis = new FileInputStream(PATH);
               Workbook mbook = Workbook.getWorkbook(mfis);
               int msheer = mbook.getNumberOfSheets();                     //表数量
               Sheet[] mSheetlist = mbook.getSheets();                     //表内容

               for (int i = 0; i < msheer; i++) {
                   rows = mSheetlist[i].getRows();
                   Log.e(TAG,"行数："+rows);
                   for (int j = 0; j < rows; j++) {
                       Cell[] cellList = mSheetlist[i].getRow(j);
                       for (Cell cell : cellList) {
                           temp[cell.getColumn()] = cell.getContents();
                       }
                       fragment3_Data.add(new fragment3Item(temp[0],temp[2],temp[3],temp[1]));
                       fragment3_RecordAdapter = new fragment3ItemAdapter((LinkedList<fragment3Item>) fragment3_Data,fragment3_Context);
                       fragment3_ListView.setAdapter(fragment3_RecordAdapter);
//                       fragment3_ListView.setOnItemClickListener(this);
                   }
               }

               mbook.close();





           }catch (Exception e){
               System.out.println("fragment3,Exception: " + e);
           }
        }

    }

}

