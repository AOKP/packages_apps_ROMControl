package com.aokp.romcontrol.fragments;

import com.aokp.romcontrol.R;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RibbonsFragment extends Fragment {
    private static Context mContext;

    private TabAdapter mAdapter;
    private PagerSlidingTabStrip mTabs;
    private ViewPager mPager;

    private static final String[] mFragments = new String[] {
        "Left Ribbon Items", "Left Ribbon Settings", "Right Ribbon Items", "Right Ribbon Settings"
    };

    public RibbonsFragment(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        View main = inflater.inflate(R.layout.fragment_ribbons_main, container, false);
        mAdapter = new TabAdapter(getChildFragmentManager());
        mPager = (ViewPager) main.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mTabs = (PagerSlidingTabStrip) main.findViewById(R.id.tabs);
        mTabs.setViewPager(mPager);
        return main;
    }

    private static class TabAdapter extends FragmentPagerAdapter {
        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
        
        @Override
        public float getPageWidth(int position) {
                return (1.0f);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RibbonItems(mContext, "left");
                case 1:
                    return new RibbonSettings(mContext, "left");
                case 2:
                    return new RibbonItems(mContext, "right");
                case 3:
                    return new RibbonSettings(mContext, "right");
                default:
            }
            return null;
        }
    }
}