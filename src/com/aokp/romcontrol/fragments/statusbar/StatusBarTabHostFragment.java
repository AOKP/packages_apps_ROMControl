/*
 * Copyright (c) 2015, The Android Open Kang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments.statusbar;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

public class StatusBarTabHostFragment extends Fragment {
    private static Context mContext;

    private TabAdapter mAdapter;
    private PagerSlidingTabStrip mTabs;
    private ViewPager mPager;

    private static final int[] mFragments = new int[] {
            R.string.status_bar_title,
            R.string.status_bar_battery_style_title,
            R.string.status_bar_clock_style_title,
            R.string.network_traffic_title
    };

    public StatusBarTabHostFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        View main = inflater.inflate(R.layout.fragment_statusbar_main, container, false);
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
                    return new StatusbarSettingsFragment();
                case 1:
                    return new BatterySettingsFragment();
                case 2:
                    return new ClockSettingsFragment();
                case 3:
                    return new TrafficSettingsFragment();
            }
            return null;
        }
    }
}
