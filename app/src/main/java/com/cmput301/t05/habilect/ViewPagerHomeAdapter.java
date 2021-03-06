package com.cmput301.t05.habilect;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * @author ioltuszy
 */

public class ViewPagerHomeAdapter extends FragmentPagerAdapter {
    static final int NUM_PAGES = 2;

    public ViewPagerHomeAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new HomePrimaryFragment();

            case 1:
                return new HomeStatisticsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}

