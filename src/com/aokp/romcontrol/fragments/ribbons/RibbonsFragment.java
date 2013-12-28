package com.aokp.romcontrol.fragments.ribbons;

import com.android.internal.util.aokp.AokpRibbonHelper;
import com.aokp.romcontrol.R;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
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

    private static final int[] mFragments = new int[] {
        R.string.left_ribbon_items,
        R.string.left_ribbon_settings,
        R.string.right_ribbon_items,
        R.string.right_ribbon_settings,
        R.string.lockscreen_ribbon_items,
        R.string.lockscreen_ribbon_settings,
        R.string.window_settings
    };

    public RibbonsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        mContext = getActivity();
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
            return mContext.getString(mFragments[position]);
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
                    return new LeftRibbonItems();
                case 1:
                    return new LeftRibbonSettings();
                case 2:
                    return new RightRibbonItems();
                case 3:
                    return new RightRibbonSettings();
                case 4:
                    return new LockscreenRibbonItems();
                case 5:
                    return new LockscreenRibbonSettings();
                case 6:
                    return new WindowSettings();
                default:
            }
            return null;
        }
    }
}
