package cz.mendelu.tomas.graphpef.fragments;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tomas on 12.08.2018.
 */


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private HashMap<Integer,Fragment> mFragmentMap = new HashMap<>();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return mFragmentMap.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentMap.get(position);
    }

    public void addFragment(Fragment fragment){
        mFragmentMap.put(getCount(),fragment);
    }

    public void setFragmetAtPosition(Integer position, Fragment fragment) {
        mFragmentMap.remove(position);
        mFragmentMap.put(position,fragment);
    }
}

