package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;

public class LockscreenSettingsFragment extends Fragment implements OnSettingChangedListener {

    CheckboxSetting mSeeThrough;
    SingleChoiceSetting mBlurRadius;

    public LockscreenSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lockscreen_settings, container, false);

        mSeeThrough = (CheckboxSetting) v.findViewById(R.id.lockscreen_see_through);
        mBlurRadius = (SingleChoiceSetting) v.findViewById(R.id.lockscreen_blur_radius);

        return v;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSeeThrough.setOnSettingChangedListener(this);
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if (key.equals("lockscreen_see_through")) {
            mBlurRadius.setVisibility(mSeeThrough.isChecked() ? View.VISIBLE : View.GONE);
        }
    }

}
