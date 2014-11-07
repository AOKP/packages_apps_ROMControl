package com.aokp.romcontrol.fragments.navbar;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;


public class ArrangeNavbarFragment extends Fragment implements OnSettingChangedListener {

    protected Context mContext;

    public ArrangeNavbarFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navbar_settings, container, false);

        mContext = getActivity();

        return v;
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
    }

}