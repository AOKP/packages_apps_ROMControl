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
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.IncreasingRingPreference;
// import com.aokp.romcontrol.settings.BaseSetting;

public class SoundSettingsFragment extends Fragment {

    protected Context mContext;

    IncreasingRingPreference increasing_ring;

    public SoundSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sound_settings, container, false);

        mContext = getActivity();

        increasing_ring = (IncreasingRingPreference) v.findViewById(R.id.increasing_ring);

        if (!hasPhoneAbility(mContext)) {
            increasing_ring.setVisibility(View.GONE);
        }

        return v;
    }

    public static boolean hasPhoneAbility(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            return false;
        }

        return true;
    }
}
