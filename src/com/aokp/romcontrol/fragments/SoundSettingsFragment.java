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
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;

public class SoundSettingsFragment extends Fragment implements OnSettingChangedListener {

    protected Context mContext;

    CheckboxSetting mIncreasingRing;
    SingleChoiceSetting mIncreasingRingMinVol, mIncreasingRingInterval;
    private AudioManager mAudioManager;
    int mVolumeLevel;

    private ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateView();
        }
    };

    public SoundSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        if (hasPhoneAbility(mContext)) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.CONTENT_URI, true, mSettingsObserver);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sound_settings, container, false);

        mIncreasingRing = (CheckboxSetting) v.findViewById(R.id.increasing_ring);
        mIncreasingRingMinVol = (SingleChoiceSetting) v.findViewById(R.id.increasing_ring_min_vol);
        mIncreasingRingInterval = (SingleChoiceSetting) v.findViewById(R.id.increasing_ring_interval);

        updateView();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIncreasingRing.setOnSettingChangedListener(this);
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if ("aokp".equals(table)) {
            mIncreasingRingMinVol.setVisibility(mIncreasingRing.isChecked() ? View.VISIBLE : View.GONE);
            mIncreasingRingInterval.setVisibility(mIncreasingRing.isChecked() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    public void updateView() {
        mVolumeLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_RING)-1;
        if (mVolumeLevel > 1) {
            mFirstRun = false;
            int[] increasing_ring_min_volume_values = getResources().getIntArray(R.array.increasing_ring_min_volume_values);
            String[] volumeValues = new String[mVolumeLevel];
            String[] increasing_ring_interval_entries = getResources().getStringArray(R.array.increasing_ring_min_volume_entries);
            String[] volumeEntries = new String[mVolumeLevel];
            for (int i = 0; i < mVolumeLevel; i++) {
                volumeValues[i] = String.valueOf(increasing_ring_min_volume_values[i]);
                volumeEntries[i] = increasing_ring_interval_entries[i];
            }
            mIncreasingRingMinVol.setEntryValues(volumeValues);
            mIncreasingRingMinVol.setEntries(volumeEntries);
            mIncreasingRingMinVol.updateSummary();

            mIncreasingRingInterval.setEntryValues(R.array.increasing_ring_interval_values);
            mIncreasingRingInterval.setEntries(R.array.increasing_ring_interval_entries);
            mIncreasingRingInterval.updateSummary();
        } else {
            mIncreasingRing.setChecked(false);
            mIncreasingRing.setEnabled(false);
            mIncreasingRingMinVol.setVisibility(View.GONE);
            mIncreasingRingInterval.setVisibility(View.GONE);
        }
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
