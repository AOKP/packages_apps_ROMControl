package com.aokp.romcontrol.fragments.headsup;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost.OnTabChangeListener;

import com.aokp.romcontrol.fragments.headsup.HeadsUpDndSettingsFragment;
import com.aokp.romcontrol.fragments.headsup.HeadsUpBlacklistSettingsFragment;

import com.aokp.romcontrol.R;


public class HeadsUpTabHostFragment extends Fragment implements OnTabChangeListener {

    private FragmentTabHost mTabHost;

    static String sLastTab;

    public HeadsUpTabHostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_headsup_tabhost, container, false);

        mTabHost = (FragmentTabHost)view.findViewById(R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("dnd").setIndicator(getString(R.string.heads_up_dnd_title)),
                HeadsUpDndSettingsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("blacklist").setIndicator(getString(R.string.heads_up_blacklist_title)),
                HeadsUpBlacklistSettingsFragment.class, null);

        mTabHost.setOnTabChangedListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sLastTab != null) {
            mTabHost.setCurrentTabByTag(sLastTab);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

    @Override
    public void onTabChanged(String s) {
        sLastTab = s;
    }
}
