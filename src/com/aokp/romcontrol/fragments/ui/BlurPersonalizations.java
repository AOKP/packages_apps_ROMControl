/*
 * Copyright (C) 2016 The Xperia Open Source Project
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

package com.aokp.romcontrol.fragments.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class BlurPersonalizations extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_blurpersonalizations_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.blurpersonalizations_settings_main, new BlurPersonalizationsSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class BlurPersonalizationsSettingsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        public BlurPersonalizationsSettingsPreferenceFragment() {
        }

        private static final String TAG = "BlurUI";

        private SharedPreferences mBlurUISettings;
        private Editor toEditBlurUISettings;
        private String sNotifications = "false";
        private String sHeader = "false";
        private String sQuickSett = "false";

        //Switch Preferences
        private SwitchPreference mExpand;
        private SwitchPreference mNotiTrans;
        private SwitchPreference mHeadSett;
        private SwitchPreference mQuickSett;
        private TwoStatePreference mRecentsSett;

        //Transluency,Radius and Scale
        private SeekBarPreferenceCham mScale;
        private SeekBarPreferenceCham mRadius;
        private SeekBarPreferenceCham mQuickSettPerc;
        private SeekBarPreferenceCham mHeaderSettPerc;
        private SeekBarPreferenceCham mNotSettPerc;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_blurpersonalizations_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mBlurUISettings = getActivity().getSharedPreferences("BlurUI", Context.MODE_PRIVATE);

            mExpand = (SwitchPreference) prefSet.findPreference("blurred_status_bar_expanded_enabled_pref");
            mExpand.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, 0) == 1));

            mScale = (SeekBarPreferenceCham) findPreference("statusbar_blur_scale");
            mScale.setValue(Settings.System.getInt(resolver, Settings.System.BLUR_SCALE_PREFERENCE_KEY, 10));
            mScale.setOnPreferenceChangeListener(this);

            mRadius = (SeekBarPreferenceCham) findPreference("statusbar_blur_radius");
            mRadius.setValue(Settings.System.getInt(resolver, Settings.System.BLUR_RADIUS_PREFERENCE_KEY, 5));
            mRadius.setOnPreferenceChangeListener(this);

            mNotiTrans = (SwitchPreference) prefSet.findPreference("translucent_notifications_pref");
            mNotiTrans.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, 0) == 1));

            mHeadSett = (SwitchPreference) prefSet.findPreference("translucent_header_pref");
            mHeadSett.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.TRANSLUCENT_HEADER_PREFERENCE_KEY, 0) == 1));

            mQuickSett = (SwitchPreference) prefSet.findPreference("translucent_quick_settings_pref");
            mQuickSett.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, 0) == 1));

            mQuickSettPerc = (SeekBarPreferenceCham) findPreference("quick_settings_transluency");
            mQuickSettPerc.setValue(Settings.System.getInt(resolver, Settings.System.TRANSLUCENT_QUICK_SETTINGS_PRECENTAGE_PREFERENCE_KEY, 60));
            mQuickSettPerc.setOnPreferenceChangeListener(this);

            mHeaderSettPerc = (SeekBarPreferenceCham) findPreference("header_transluency");
            mHeaderSettPerc.setValue(Settings.System.getInt(resolver, Settings.System.TRANSLUCENT_HEADER_PRECENTAGE_PREFERENCE_KEY, 60));
            mHeaderSettPerc.setOnPreferenceChangeListener(this);

            mNotSettPerc = (SeekBarPreferenceCham) findPreference("notifications_transluency");
            mNotSettPerc.setValue(Settings.System.getInt(resolver, Settings.System.TRANSLUCENT_NOTIFICATIONS_PRECENTAGE_PREFERENCE_KEY, 60));
            mNotSettPerc.setOnPreferenceChangeListener(this);

        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mScale) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.BLUR_SCALE_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mRadius) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.BLUR_RADIUS_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mQuickSettPerc) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.TRANSLUCENT_QUICK_SETTINGS_PRECENTAGE_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mHeaderSettPerc) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.TRANSLUCENT_HEADER_PRECENTAGE_PREFERENCE_KEY, value);
                return true;
            } else if (preference == mNotSettPerc) {
                int value = ((Integer)newValue).intValue();
                Settings.System.putInt(
                        resolver, Settings.System.TRANSLUCENT_NOTIFICATIONS_PRECENTAGE_PREFERENCE_KEY, value);
                return true;
            }
            return false;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             final Preference preference) {
            final ContentResolver resolver = getActivity().getContentResolver();
            if  (preference == mExpand) {
                boolean enabled = ((SwitchPreference)preference).isChecked();
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, enabled ? 1:0);
                updatePrefs();
            } else if (preference == mNotiTrans) {
                boolean enabled = ((SwitchPreference)preference).isChecked();
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, enabled ? 1:0);
                if (enabled) {
                    sNotifications = "true";
                } else {
                    sNotifications = "false";
                }
                toEditBlurUISettings = mBlurUISettings.edit();
                toEditBlurUISettings.putString("notifications_transluency", sNotifications);
                toEditBlurUISettings.commit();
            } else if (preference == mHeadSett) {
                boolean enabled = ((SwitchPreference)preference).isChecked();
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_HEADER_PREFERENCE_KEY, enabled ? 1:0);
                if (enabled) {
                    sHeader = "true";
                } else {
                    sHeader = "false";
                }
                toEditBlurUISettings = mBlurUISettings.edit();
                toEditBlurUISettings.putString("header_transluency", sHeader);
                toEditBlurUISettings.commit();
            } else if (preference == mQuickSett) {
                boolean enabled = ((SwitchPreference)preference).isChecked();
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, enabled ? 1:0);
                if (enabled) {
                    sQuickSett = "true";
                } else {
                    sQuickSett = "false";
                }
                toEditBlurUISettings = mBlurUISettings.edit();
                toEditBlurUISettings.putString("quick_settings_transluency", sQuickSett);
                toEditBlurUISettings.commit();
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public void sharedPreferences() {
            toEditBlurUISettings = mBlurUISettings.edit();
            toEditBlurUISettings.putString("notifications_transluency", sNotifications);
            toEditBlurUISettings.putString("header_transluency", sHeader);
            toEditBlurUISettings.putString("quick_settings_transluency", sQuickSett);
            toEditBlurUISettings.commit();
        }

        private void updatePrefs() {
            final ContentResolver resolver = getActivity().getContentResolver();

            boolean tempNotification = mBlurUISettings.getString("notifications_transluency", "").equals("true");
            boolean tempHeader = mBlurUISettings.getString("header_transluency", "").equals("true");
            boolean tempQuickSett = mBlurUISettings.getString("quick_settings_transluency", "").equals("true");

            if (Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, 0) == 1) {
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, tempNotification ? 1:0);
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_HEADER_PREFERENCE_KEY, tempHeader ? 1:0);
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, tempQuickSett ? 1:0);
            } else {
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, 0);
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_HEADER_PREFERENCE_KEY, 0);
                Settings.System.putInt(resolver,
                        Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, 0);
            }
        }
    }
}
