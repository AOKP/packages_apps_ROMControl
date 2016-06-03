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
import android.net.Uri;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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
import com.aokp.romcontrol.widgets.SeekBarPreference;

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

        public static final int IMAGE_PICK = 1;

        private static final String KEY_WALLPAPER_SET = "lockscreen_wallpaper_set";
        private static final String KEY_WALLPAPER_CLEAR = "lockscreen_wallpaper_clear";
        private static final String KEY_LOCKSCREEN_BLUR_RADIUS = "lockscreen_blur_radius";

        private static final String LOCKSCREEN_MAX_NOTIF_CONFIG =
                "lockscreen_max_notif_cofig";

        private Preference mSetWallpaper;
        private Preference mClearWallpaper;
        private SeekBarPreference mBlurRadius;
        private SeekBarPreference mMaxKeyguardNotifConfig;
        private ContentResolver mResolver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_lockscreen_settings);
            mResolver = getActivity().getContentResolver();

            mSetWallpaper = (Preference) findPreference(KEY_WALLPAPER_SET);
            mClearWallpaper = (Preference) findPreference(KEY_WALLPAPER_CLEAR);

            mMaxKeyguardNotifConfig = (SeekBarPreference) findPreference(LOCKSCREEN_MAX_NOTIF_CONFIG);
            int kgconf = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, 5);
            mMaxKeyguardNotifConfig.setValue(kgconf);
            mMaxKeyguardNotifConfig.setOnPreferenceChangeListener(this);

            mBlurRadius = (SeekBarPreference) findPreference(KEY_LOCKSCREEN_BLUR_RADIUS);
            mBlurRadius.setValue(CMSettings.Secure.getInt(mResolver,
                    CMSettings.Secure.LOCKSCREEN_BLUR_RADIUS, 14));
            mBlurRadius.setOnPreferenceChangeListener(this);

            setHasOptionsMenu(true);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mBlurRadius) {
                int width = ((Integer)newValue).intValue();
                CMSettings.Secure.putInt(mResolver,
                        CMSettings.Secure.LOCKSCREEN_BLUR_RADIUS, width);
                return true;
            } else if (preference == mMaxKeyguardNotifConfig) {
                int kgconf = (Integer) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCKSCREEN_MAX_NOTIF_CONFIG, kgconf);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mSetWallpaper) {
                setKeyguardWallpaper();
                return true;
            } else if (preference == mClearWallpaper) {
                clearKeyguardWallpaper();
                Toast.makeText(getView().getContext(), getString(R.string.reset_lockscreen_wallpaper),
                Toast.LENGTH_LONG).show();
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Intent intent = new Intent();
                    intent.setClassName("com.android.wallpapercropper", "com.android.wallpapercropper.WallpaperCropActivity");
                    intent.putExtra("keyguardMode", "1");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }

        private void setKeyguardWallpaper() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK);
        }

        private void clearKeyguardWallpaper() {
            WallpaperManager wallpaperManager = null;
            wallpaperManager = WallpaperManager.getInstance(getActivity());
            wallpaperManager.clearKeyguardWallpaper();
        }
    }
}
