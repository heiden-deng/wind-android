package com.akaxin.client.maintab.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.akaxin.client.maintab.BaseFragment;

/**
 * Created by yichao on 2018/1/12.
 */

public class MainTabPagerAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments;

    public MainTabPagerAdapter(FragmentManager fm, Fragment[] fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}
