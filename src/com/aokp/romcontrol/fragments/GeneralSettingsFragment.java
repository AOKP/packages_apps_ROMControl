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

import android.net.ConnectivityManager;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;

private static final String SMS_BREATH = "sms_breath";
private static final String MISSED_CALL_BREATH = "missed_call_breath";
private static final String VOICEMAIL_BREATH = "voicemail_breath";

private CheckBoxPreference mSMSBreath;
private CheckBoxPreference mMissedCallBreath;
private CheckBoxPreference mVoicemailBreath;

public class GeneralSettingsFragment extends Fragment {

    public GeneralSettingsFragment() {

    }

    mSMSBreath = (CheckBoxPreference) findPreference(SMS_BREATH);
    mMissedCallBreath = (CheckBoxPreference) findPreference(MISSED_CALL_BREATH);
    mVoicemailBreath = (CheckBoxPreference) findPreference(VOICEMAIL_BREATH);

    Context context = getActivity();
    ConnectivityManager cm = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);

    if(cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
        mSMSBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_SMS_BREATH, 0) == 1);
        mSMSBreath.setOnPreferenceChangeListener(this);

        mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_MISSED_CALL_BREATH, 0) == 1);
        mMissedCallBreath.setOnPreferenceChangeListener(this);

        mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
	mVoicemailBreath.setOnPreferenceChangeListener(this);
    } else {
        prefSet.removePreference(mSMSBreath);
        prefSet.removePreference(mMissedCallBreath); 
        prefSet.removePreference(mVoicemailBreath);
    }	
 }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_settings, container, false);

        return v;
    }
}
