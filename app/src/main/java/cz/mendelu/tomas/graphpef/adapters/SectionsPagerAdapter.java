package cz.mendelu.tomas.graphpef.adapters;


import java.io.Serializable;
import java.util.HashMap;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by tomas on 12.08.2018.
 */


public class SectionsPagerAdapter extends FragmentPagerAdapter implements Serializable{

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

