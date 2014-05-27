package com.aokp.romcontrol.fragments.navbar;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.util.aokp.DeviceUtils;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;


public class NavbarSettingsFragment extends Fragment implements OnSettingChangedListener {

    protected Context mContext;

    SingleChoiceSetting navbar_width, navbar_height, navbar_height_landscape;
    CheckboxSetting navbar_on_bottom;

    public NavbarSettingsFragment() {

    }

    CheckboxSetting mToggleNavbar;

    boolean hasNavbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hasNavbar = getActivity().getResources()
                .getBoolean(com.android.internal.R.bool.config_showNavigationBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navbar_settings, container, false);

        mContext = getActivity();

        mToggleNavbar = (CheckboxSetting) v.findViewById(R.id.setting_toggle_navbar);
        mToggleNavbar.setChecked(Settings.AOKP.getBoolean(mContext.getContentResolver(),
                Settings.AOKP.ENABLE_NAVIGATION_BAR, hasNavbar));

        navbar_on_bottom = (CheckboxSetting) v.findViewById(R.id.navigation_bar_on_bottom);
        navbar_width = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_width);
        navbar_height = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_height);
        navbar_height_landscape = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_height_landscape);

        if (!DeviceUtils.isPhone(mContext)) {
            navbar_width.setVisibility(View.GONE);
            navbar_on_bottom.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
    }
}
