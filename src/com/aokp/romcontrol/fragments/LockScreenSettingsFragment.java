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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;

public class LockScreenSettingsFragment extends Fragment {

    public LockScreenSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lockscreen_settings, container, false);

        Resources res = getResources();

        return v;
    }
}
