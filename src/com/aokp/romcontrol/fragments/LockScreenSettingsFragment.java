/*
 * Copyright (C) 2016 The Android Open Kang Project
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

import android.app.Activity;
import android.app.WallpaperManager;
import android.app.Fragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.content.res.Configuration;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import cyanogenmod.providers.CMSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.util.Utils;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

public class LockScreenSettingsFragment extends Fragment {

    public LockScreenSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {

        }

        private static final String TAG = "LockScreenSettings";
        private static final String LOCKSCREEN_MAX_NOTIF_CONFIG = "lockscreen_max_notif_cofig";
        private static final String PREF_KEYGUARD_TORCH = "keyguard_toggle_torch";
        private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
        private static final String WEATHER_CATEGORY = "lock_screen_weather_category";
        private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";

        private ContentResolver mResolver;

        private FingerprintManager mFingerprintManager;
        private SwitchPreference mFingerprintVib;
        private SwitchPreference mFpKeystore;
        private SeekBarPreferenceCham mMaxKeyguardNotifConfig;
        private SwitchPreference mKeyguardTorch;
        private PreferenceCategory mWeatherCategory;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_lockscreen_settings);
            mResolver = getActivity().getContentResolver();
            PreferenceScreen prefSet = getPreferenceScreen();
            PackageManager pm = getActivity().getPackageManager();

            // Fingerprint vibration
            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            mFingerprintVib = (SwitchPreference) prefSet.findPreference("fingerprint_success_vib");
            if (!mFingerprintManager.isHardwareDetected()){
                prefSet.removePreference(mFingerprintVib);
            }

            // Fingerprint unlock keystore
           mFpKeystore = (SwitchPreference) prefSet.findPreference("fp_unlock_keystore");
            if (!mFingerprintManager.isHardwareDetected()){
                prefSet.removePreference(mFpKeystore);
            }

            mMaxKeyguardNotifConfig = (SeekBarPreferenceCham) findPreference(LOCKSCREEN_MAX_NOTIF_CONFIG);
            int kgconf = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, 5);
            mMaxKeyguardNotifConfig.setValue(kgconf);
            mMaxKeyguardNotifConfig.setOnPreferenceChangeListener(this);

            // Keyguard Torch
            mKeyguardTorch = (SwitchPreference) prefSet.findPreference(PREF_KEYGUARD_TORCH);
            if (!Utils.deviceSupportsFlashLight(getActivity())) {
                prefSet.removePreference(mKeyguardTorch);
            }
            // LockScreen weather category
            if (mWeatherCategory != null && (!Helpers.isPackageInstalled(WEATHER_SERVICE_PACKAGE, pm))) {
                prefSet.removePreference(mWeatherCategory);
            }

            setHasOptionsMenu(true);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mMaxKeyguardNotifConfig) {
                int kgconf = (Integer) newValue;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, kgconf);
                return true;
            }
        return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
