package com.aokp.romcontrol.fragments.navbar;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;


public class NavbarSettingsFragment extends Fragment implements OnSettingChangedListener {

    public NavbarSettingsFragment() {

    }

    CheckboxSetting mToggleNavbar;

    int hardwareKeyMask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hardwareKeyMask = getActivity().getResources()
                .getInteger(com.android.internal.R.integer.config_deviceHardwareKeys);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navbar_settings, container, false);

        if (hardwareKeyMask == 0) {
            mToggleNavbar = (CheckboxSetting) v.findViewById(R.id.setting_toggle_navbar);
            mToggleNavbar.setVisibility(View.GONE);
        }

        return v;
    }


    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
    }
}
