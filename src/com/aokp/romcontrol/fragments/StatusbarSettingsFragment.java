/*
 * Copyright (C) 2013 The Android Open Kang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;

public class StatusbarSettingsFragment extends Fragment implements OnSettingChangedListener {

    BaseSetting mBatteryIndicator, mBatteryIndicatorPlugged;

    public StatusbarSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statusbar_settings, container, false);

        mBatteryIndicator = (BaseSetting) v.findViewById(R.id.battery_percentage_indicator);
        mBatteryIndicatorPlugged = (BaseSetting) v.findViewById(R.id.battery_percentage_indicator_plugged);

        return v;
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if (table.equals("aokp") && key.equals(mBatteryIndicator.getKey())) {
            if (value == null || value.isEmpty()) {
                mBatteryIndicatorPlugged.setVisibility(View.GONE);
            } else {
                mBatteryIndicatorPlugged.setVisibility(View.VISIBLE);
            }
        }
    }
}
