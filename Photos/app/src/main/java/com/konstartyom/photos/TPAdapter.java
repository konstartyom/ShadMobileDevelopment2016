package com.konstartyom.photos;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TPAdapter extends FragmentPagerAdapter {
    Tab1[] mTabs;
    private static final int TABS_COUNT = 3;
    private static String mTabPrefix;

    public TPAdapter(FragmentManager fm, String tabPrefix) {
        super(fm);
        mTabs = new Tab1[TABS_COUNT];
        for (int index = 0; index < mTabs.length; ++index) {
            mTabs[index] = new Tab1();
        }
        mTabPrefix = tabPrefix + " ";
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
        return mTabPrefix + (position + 1);
    }

}
