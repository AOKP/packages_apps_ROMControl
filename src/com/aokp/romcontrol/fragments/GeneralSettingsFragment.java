/*
 * Copyright (C) 2013-2014 The Android Open Kang Project
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

import android.app.ActivityManager;
import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;

public class GeneralSettingsFragment extends Fragment {

    private static final String FORCE_HIGHEND_GFX_PERSIST_PROP = "persist.sys.force_highendgfx";

    CheckboxSetting mForceHighEndGfx;

    public GeneralSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_settings, container, false);

        mForceHighEndGfx = (CheckboxSetting) v.findViewById(R.id.setting_force_highend_gfx);
        if (ActivityManager.isLowRamDeviceStatic()) {
            String forceHighendGfx = SystemProperties.get(FORCE_HIGHEND_GFX_PERSIST_PROP, "false");
            mForceHighEndGfx.setChecked("true".equals(forceHighendGfx));
            mForceHighEndGfx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SystemProperties.set(FORCE_HIGHEND_GFX_PERSIST_PROP,
                            mForceHighEndGfx.isChecked() ? "true" : "false");
                }
            });
        } else {
            mForceHighEndGfx.setVisibility(View.GONE);
        }

        return v;
    }
}
