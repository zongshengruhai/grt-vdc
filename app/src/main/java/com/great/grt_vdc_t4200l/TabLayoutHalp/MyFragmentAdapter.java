package com.great.grt_vdc_t4200l.TabLayoutHalp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/* ------------- my fargment adpter 说明 -------------
    主要描述：用于TabLayout与View类的适配器
    创建日期：2018年7月31日 14:14:39
 */
public class MyFragmentAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mFragments ;
    private List<String> mTitles ;
    public MyFragmentAdapter(FragmentManager fm,List<Fragment> fragments,List<String> titles) {
        super(fm);
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    //返回当前有效视图数量
    @Override
    public int getCount() {
        return mFragments == null ?0:mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    @Override
    public void notifyDataSetChanged(){
        //this.notifyDataSetChanged();
    }
}
