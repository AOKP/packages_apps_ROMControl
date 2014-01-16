package com.aokp.romcontrol.fragments.navbar;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.SingleChoiceSetting;


public class NavbarSettingsFragment extends Fragment implements OnSettingChangedListener {

    protected Context mContext;

    SingleChoiceSetting navbar_width, navbar_height, navbar_height_landscape;

    public NavbarSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navbar_settings, container, false);

        mContext = getActivity();

        navbar_width = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_width);
        navbar_height = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_height);
        navbar_height_landscape = (SingleChoiceSetting) v.findViewById(R.id.navigation_bar_height_landscape);

        if (!hasHWbuttons()) {
            if (isTablet()) {
                navbar_width.setVisibility(View.GONE);
            } else {
                navbar_height_landscape.setVisibility(View.GONE);
            }
        } else {
            navbar_width.setVisibility(View.GONE);
            navbar_height.setVisibility(View.GONE);
            navbar_height_landscape.setVisibility(View.GONE);
        }

        return v;
    }


    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
    }

    private boolean hasHWbuttons() {
        int hardwareKeyMask = mContext.getResources()
                .getInteger(com.android.internal.R.integer.config_deviceHardwareKeys);
        return (hardwareKeyMask != 0);
    }

    private boolean isTablet() {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        float density = displayMetrics.density;
        if (widthPixels < heightPixels) {
            return ((widthPixels / density) >= 600);
        } else {
            return ((heightPixels / density) >= 600);
        }
    }
}
