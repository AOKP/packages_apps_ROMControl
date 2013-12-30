package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost.OnTabChangeListener;
import com.aokp.romcontrol.R;

public class TogglesTabHostFragment extends Fragment implements OnTabChangeListener {

    private FragmentTabHost mTabHost;

    static String sLastTab;

    public TogglesTabHostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.container);

        mTabHost.addTab(mTabHost.newTabSpec("rearrange").setIndicator(getString(R.string.toggle_tab_arrange)),
                ArrangeTogglesFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("settings").setIndicator(getString(R.string.toggles_tab_settings)),
                ToggleSettingsFragment.class, null);

        mTabHost.setOnTabChangedListener(this);
        return mTabHost;
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
