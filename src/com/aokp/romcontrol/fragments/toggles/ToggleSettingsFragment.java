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

package com.aokp.romcontrol.fragments.toggles;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.SingleChoiceSetting;

/**
 * Created by roman on 12/30/13.
 */
public class ToggleSettingsFragment extends Fragment implements OnSettingChangedListener {

    BaseSetting mTogglesFast, mSwipeToSwitch;
    SingleChoiceSetting mTogglesPerRow, mToggleStyle, mToggleSide;

    public ToggleSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_toggle_setup, container, false);

        mTogglesFast = (BaseSetting) v.findViewById(R.id.toggles_fast_toggle);
        mSwipeToSwitch = (BaseSetting) v.findViewById(R.id.toggles_swipe_to_switch);
        mTogglesPerRow = (SingleChoiceSetting) v.findViewById(R.id.toggles_per_row);
        mToggleStyle = (SingleChoiceSetting) v.findViewById(R.id.toggles_style);
        mToggleSide = (SingleChoiceSetting) v.findViewById(R.id.toggles_fast_side);

        mToggleStyle.setOnSettingChangedListener(this);


        return v;
    }


    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if (table.equals("aokp") && key.equals(mToggleStyle.getKey())) {
            if (value == null || value.isEmpty()) {
                // defualt state
                mTogglesPerRow.setVisibility(View.VISIBLE);
                mTogglesFast.setVisibility(View.VISIBLE);
                mToggleSide.setVisibility(View.VISIBLE);
                mSwipeToSwitch.setVisibility(View.VISIBLE);
            } else {
                mTogglesPerRow.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
                mTogglesFast.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
                mToggleSide.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
                mSwipeToSwitch.setVisibility(value.equals("0" /* 0 is the tile */)
                        ? View.VISIBLE : View.GONE);
            }
        }
    }
}
