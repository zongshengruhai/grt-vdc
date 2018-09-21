package com.great.grt_vdc_t4200l.ListView;

public class settingItem {

    private String mName;
    private String mBtName;

    public settingItem(){
    }

    public settingItem(String mName,String mBtName){
        this.mName = mName;
        this.mBtName =mBtName;
    }


    public String getmName() {
        return mName;
    }

    public String getmBtName() {
        return mBtName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmBtName(String mBtName) {
        this.mBtName = mBtName;
    }
}


