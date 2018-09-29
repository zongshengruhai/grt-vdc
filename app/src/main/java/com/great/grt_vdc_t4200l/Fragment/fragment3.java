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
import com.great.grt_vdc_t4200l.ListView.longItem;
import com.great.grt_vdc_t4200l.ListView.longItemAdapter;
import com.great.grt_vdc_t4200l.R;
import com.great.grt_vdc_t4200l.SystemFunc;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import static com.great.grt_vdc_t4200l.SystemFunc.checkFileExist;

/**
 * fragment3 历史告警
 * 程序流程：
 *      1、创建时执行更新List
 *      2、当显示此界面时，若Alarm内bool发生改变，更新List
 * 可优化事宜：
 *      1、findEventTime()方法未做报错处理，可执行一定次数不成功通知BaseCourse重启
 */
public class fragment3 extends Fragment{

    //容器
    private Context fragment3_Context;
    private ListView fragment3_ListView;
    //List Data
    private List<longItem> fragment3_Data  = new LinkedList<>();
//    private longItemAdapter fragment3_RecordAdapter;
    //更新List依据
    private boolean[] _isOldYx = new boolean[5];
    //防止并发
//    private boolean findFlag = false;

    /**
     * fragment生命周期
     */
    //创建
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment3,container,false);

        //容器关联
        fragment3_Context = view.getContext();
        fragment3_ListView = view.findViewById(R.id.fragment3_ListView);

        //更新List
        findEventTime();

        return view;
    }
    //重载
    @Override
    public void onResume(){
        super.onResume();
        //注册广播
        f3_UiHandler.post(f3_UiRunnable);
    }
    //中止
    @Override
    public void onPause(){
        super.onPause();
        //注销广播
        f3_UiHandler.removeCallbacks(f3_UiRunnable);
    }

    /**
     * 更新数据方法
     */
    private void findEventTime() {
//    class findEventThread extends Thread {
//        public void run() {

//            findFlag = true;

            //行
            int rows;
            //路径
            String PATH = fragment3_Context.getFilesDir().getPath() + "/fault_log/fault_record.xls";
            //数据缓存
            String[] temp = new String[4];

            //防止空指针
            if (checkFileExist(PATH)) {

                longItemAdapter fragment3_RecordAdapter;

                fragment3_Data.clear();

                try {

                    //获取文件流
                    FileInputStream mfis = new FileInputStream(PATH);
                    //用jxl打开流
                    Workbook mbook = Workbook.getWorkbook(mfis);

                    int msheer = mbook.getNumberOfSheets();                     //表数量
                    Sheet[] mSheetList = mbook.getSheets();                     //表内容

                    //历遍所有内容（在SystemFunc有读Excel封装的方法，但此处未更新）
                    for (int i = 0; i < msheer; i++) //表
                    {

                        rows = mSheetList[i].getRows();
                        for (int j = 1; j < rows; j++) //行
                        {
                            Cell[] cellList = mSheetList[i].getRow(j);

                            for (Cell cell : cellList)//列
                            {
                                temp[cell.getColumn()] = cell.getContents();
                            }

                            fragment3_Data.add(new longItem(temp[0], temp[1], temp[2], temp[3]));
//                       fragment3_ListView.setOnItemClickListener(this);

                        }

                        //当没有数据时
                        if (rows == 1) {
                            temp[0] = "提示";
                            temp[1] = "当前暂无故障记录";
                            temp[2] = "";
                            temp[3] = "";
                            fragment3_Data.add(new longItem(temp[0], temp[1], temp[2], temp[3]));
                        }

                        //实现倒序，旋转List
                        Collections.reverse(fragment3_Data);

                        //填充
                        fragment3_RecordAdapter = new longItemAdapter((LinkedList<longItem>) fragment3_Data, fragment3_Context,"fragment3");
                        fragment3_ListView.setAdapter(fragment3_RecordAdapter);

                    }

                    //关闭流
                    mbook.close();

                } catch (Exception e) //抛错处理，暂时为处理，考虑是否累积一定次数通知BaseCourse重启
                {
                    System.out.println("fragment3,Exception: " + e);
                }
            }

//            findFlag = false;
//        }

    }

    /**
     * 定时线程
     */
    Handler f3_UiHandler = new Handler();
    Runnable f3_UiRunnable = new Runnable() {
        @Override
        public void run() {
            f3_UiHandler.postDelayed(this,500);

//            SharedPreferences rStateData = getActivity().getSharedPreferences("StateData", 0);
            SharedPreferences rAlarmData = getActivity().getSharedPreferences("AlarmData", 0);

            //周期更新
//            if(rStateData.getInt("layPage",0) == 3){

                boolean[] _isNewYX = new boolean[5];
                for (int i = 0; i < 5 ; i++) {
                    _isNewYX[i] = rAlarmData.getBoolean("_isYxError_"+i,false);
                }

                //判断Alarm内的Bool值，若发生变化便执行更新
                if (_isOldYx[0] != _isNewYX[0] || _isOldYx[1] != _isNewYX[1] || _isOldYx[2] != _isNewYX[2] || _isOldYx[3] != _isNewYX[3] || _isOldYx[4] != _isNewYX[4] ){
                    findEventTime();
                    _isOldYx = _isNewYX;
                }

//                if (_isNewYX[0] || _isNewYX[1] || _isNewYX[2] || _isNewYX[3] || _isNewYX[4]){
//                    SystemFunc.Beep(fragment3_Context,true);
//                }else {
//                    SystemFunc.Beep(fragment3_Context,false);
//                }

//            }
        }
    };

}

