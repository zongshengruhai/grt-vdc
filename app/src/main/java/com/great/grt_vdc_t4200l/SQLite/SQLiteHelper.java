//package com.great.grt_vdc_t4200l.SQLite;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//public class SQLiteHelper extends SQLiteOpenHelper {
//
//    public static final String DB_NAME ="test.db";
//
//    public static final int DB_VERSION = 1;
//
//    public static final String TABLE_1 = "table1";
//
//    private static final String TEST_CREATE_TABLE_SQL = "create table" + TABLE_1 + "("+
//            "id integer primary key autoincrement," +
//            "name varchar(20) not null," +
//            "test varchar(11) not null," +
//            ");";
//
//    public SQLiteDbHelper(Context context){
//        super(context,DB_NAME,null,DB_VERSION);
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db){
//        db.execSQL(TEST_CREATE_TABLE_SQL);
//    }
//
//
//
//}
