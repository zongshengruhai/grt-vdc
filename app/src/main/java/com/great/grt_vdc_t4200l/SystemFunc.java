package com.great.grt_vdc_t4200l;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.Key;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * 系统级公共方法
 * @author zongshengruhai
 * @version 1.0
 */
public class SystemFunc {

    final static private String TAG = "SystemMethod";
    static private boolean _isBeep = false;             //防止并发

    /**
     * getNewTime 获取当前系统时间
     * @return String:"yyyy.MM.dd HH:mm:ss"
     */
    static public String getNewTime(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.DAY_OF_MONTH)+" "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
    }

    /**
     * setNewTime 设置当前时间
     * @param sTime String:"20180906.095000"
     */
    static public void setNewTime(String sTime){
        if (sTime.length() == 15){
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("setprop persist.sys.timezone GMT\n");
                os.writeBytes("/system/bin/date -s" + sTime + "\n");
                os.writeBytes("clock -w\n");
                os.writeBytes("exit\n");
                os.flush();
            }catch (IOException e){
                Log.e(TAG, "set System Time: lose" );
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "set System Time: lose ,length is error" );
        }
    }


    /**
     *  Beep 振铃
     *  @param mContext 执行活动的上下文
     *  @param flag true振铃 false取消振铃
     */
    static public void Beep(Context mContext,boolean flag){
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null){
            if (flag){
                if (!_isBeep)//防止并发
                {
                    _isBeep = true;
                    vibrator.vibrate(new long[]{500,500},0);
                }
            }else {
                _isBeep = false;
                vibrator.cancel();
            }
        }
    }

    /**
     * restart 重启设备
     */
    static public void restart(){
        try {
            Process mRestart =Runtime.getRuntime().exec(new String[]{"su","-c","reboot "});
            mRestart.waitFor();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检测文件是否存在
     * @param path 检测文件路径
     * @return true 文件存在，false 文件不存在
     */
    static public boolean checkFileExist(String path){
        if (path != null){
            try {
                File files = new File(path);
                return files.exists();
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 创建文件
     * @param path 创建文件路径(支持文件夹、txt文件)
     * @return true 创建成功，false 创建失败
     */
    static public boolean createFile(String path){
        if (path != null ){
            if (!checkFileExist(path)){
                try {
                    File files = new File(path);
                    if (path.contains(".txt")){
                        files.createNewFile();
                        return checkFileExist(path);
                    }else {
                        files.mkdir();
                        return checkFileExist(path);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 删除文件
     * @param path 删除文件路径
     * @return true 删除成功、或文件本身不存在，false 删除失败 文件还是存在
     */
    static public boolean deleteFile(String path){
        if (checkFileExist(path)){
            File file = new File(path);
            if (file.isFile()){
                file.delete();
            }else if (file.isDirectory()){
                File files[] = file.listFiles();
                for(int i = 0; i < files.length ; i++) {
                    files[i].delete();
                }
                file.delete();
            }
            return !checkFileExist(path);
        }
        return true;
    }

    /**
     * 读取Execl表格
     * @param file_Name 文件绝对路径
     * @return 读取成功时 返回 Object型list，读取失败时 返回 null
     */
    public static List<List<Object>> readExcel(String file_Name){
        String extension = file_Name.lastIndexOf(".") == -1 ? "" : file_Name.substring(file_Name.lastIndexOf(".") + 1);
        if ("xls".equals(extension)){
            List<List<Object>> dataList = new ArrayList<List<Object>>();
            try {

                Workbook workbook = Workbook.getWorkbook(new File(file_Name));
                Sheet sheet = workbook.getSheet(0);

                int Rows = sheet.getRows();
                int Cols = sheet.getColumns();
                Log.e(TAG, "当前工作表名："+sheet.getName() );
                Log.e(TAG, "总行数：" + Rows +" ，总列数：" + Cols);

                List<Object> objects = new ArrayList<Object>();
                String val;
                for (int i = 0; i < Rows ; i++){
                    boolean null_row = true;
                    for (int j = 0; j < Cols ; j++) {
                        val = (sheet.getCell(j,i).getContents());
                        if (val == null || val.equals("")){
                            val = "null";
                        }else {
                            null_row = false;
                        }
                        objects.add(val);
                    }
                    if (!null_row){
                        dataList.add(objects);
                        null_row = true;
                    }
                    objects = new ArrayList<Object>();
                }
                workbook.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            return dataList;
        }
        Log.e(TAG, "不支持的文件类型");
        return null;
    }

    /**
     * 写入Execl
     * 实际是直接创建了一个新的xls文件
     * @param file_Name 文件绝对路径
     * @param data_List 文件内容
     * @return true 写入成功 ，false 写入失败
     */
    public static boolean writeExecl(String file_Name,List<List<Object>> data_List){
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(new File(file_Name));
            WritableSheet sheet = workbook.createSheet("录波数据",0);
            for (int i = 0; i < data_List.size() ; i++) {
                List<Object> obj_list = data_List.get(i);
                for (int j = 0; j < obj_list.size(); j++) {
                    Label label = new Label(j,i,obj_list.get(j).toString());
                    sheet.addCell(label);
                }
            }
            workbook.write();
            workbook.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 写入txt
     * @param path 写入路径
     * @return true 写入成功，false 写入失败
     */
    static public boolean writeTxt(String path,int[] context){
        if (path != null && path.contains(".txt")){
            if (checkFileExist(path)){
                try {
                    PrintWriter mPw = new PrintWriter(new BufferedWriter(new FileWriter(path)));
                    mPw.print(1);
                    for (int i:context) {
                        mPw.print(i);
                        mPw.print(",");
                    }
                    mPw.close();
                    return true;
                }catch (IOException e){
                    e.printStackTrace();
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 读取Txt文件
     * @param path 文件路径
     * @param row 文件行数
     */
    static public void readTxt(String path, int row){
        if (path !=null && path.contains(".txt")){
            if (checkFileExist(path)){
                try {
                    InputStream mIn = new FileInputStream(path);

//                    InputStreamReader rIn = new InputStreamReader(mIn);
                    BufferedReader rBn = new BufferedReader(new InputStreamReader(mIn));
                    byte[] temp = new byte[300];
                    StringBuffer t = new StringBuffer("");
                    int len = 0;
                    while ((len  = mIn.read(temp))>0 ){
                        Log.e(TAG, "readTxt: length =" + temp.length );
                        for (int i = 0; i < temp.length ; i++) {
//                            Log.e(TAG, "readTxt: content = "+temp[i] );
                        }
                    }
                    mIn.close();
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }



}
