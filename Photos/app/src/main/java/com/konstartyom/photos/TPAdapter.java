package com.konstartyom.photos;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TPAdapter extends FragmentPagerAdapter {
    TabBase[] mTabs;
    private static final int TABS_COUNT = 2;
    private FragmentManager mFragmentManager;

    public TPAdapter(FragmentManager fm) {
        super(fm);
        mTabs = new TabBase[TABS_COUNT];
        mTabs[0] = new Tab1();
        mTabs[1] = new Tab2();
        mFragmentManager = fm;
    }

    @Override
    public Fragment getItem(int position) {
        return mTabs[position];
    }

    @Override
    public int getCount() {
        return mTabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return GlobalContext.getApplicationContext()
                        .getResources().getString(R.string.menu_goto1);
            case 1:
                return GlobalContext.getApplicationContext()
                        .getResources().getString(R.string.menu_goto2);
        }
        return "Unknown";
    }

}
