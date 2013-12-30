package com.aokp.romcontrol.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;

public class TogglesTabHostFragment extends Fragment {

    private FragmentTabHost mTabHost;

    public TogglesTabHostFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_toggles_tabhost, container, false);

        mTabHost = (FragmentTabHost) v.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("setup").setIndicator("Setup"),
                ToggleSettingsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("rearrange").setIndicator("Rearrange"),
                ArrangeTogglesFragment.class, null);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;

    }

}
