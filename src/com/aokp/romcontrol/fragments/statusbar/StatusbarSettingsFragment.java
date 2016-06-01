/*
* Copyright (C) 2015 The Android Open Kang Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.aokp.romcontrol.fragments.statusbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import cyanogenmod.providers.CMSettings;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreference;

public class StatusbarSettingsFragment extends Fragment {

    public StatusbarSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statusbar_settings_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.statusbar_settings_main, new StatusBarSettingsPreferenceFragment())
                .commit();

        return v;
    }

    public static class StatusBarSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public StatusBarSettingsPreferenceFragment() {

        }

        private static final String TAG = "StatusBar";

        private ContentResolver mContentResolver;

        private static final String SHOW_CARRIER_LABEL = "status_bar_show_carrier";
        private static final String CUSTOM_CARRIER_LABEL = "custom_carrier_label";
        private static final String STATUS_BAR_CARRIER_COLOR = "status_bar_carrier_color";
        private static final String STATUS_BAR_CARRIER_FONT_SIZE  = "status_bar_carrier_font_size";
        private static final String MISSED_CALL_BREATH = "missed_call_breath";
        private static final String VOICEMAIL_BREATH = "voicemail_breath";
        private static final String KEY_AOKP_LOGO_COLOR = "status_bar_aokp_logo_color";

        static final int DEFAULT_STATUS_CARRIER_COLOR = 0xffffffff;

        private ListPreference mStatusBarTemperature;
        private PreferenceScreen mCustomCarrierLabel;
        private ListPreference mShowCarrierLabel;
        private String mCustomCarrierLabelText;
        private ColorPickerPreference mCarrierColorPicker;
        private SeekBarPreference mStatusBarCarrierSize;
        private SwitchPreference mMissedCallBreath;
        private SwitchPreference mVoicemailBreath;
        private ColorPickerPreference mAokpLogoColor;

        private boolean mCheckPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            mCheckPreferences = false;
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_statusbar_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();

            int intColorCarrierColor;
            String hexColorCarrierColor;

            PackageManager pm = getActivity().getPackageManager();
            Resources systemUiResources;
            try {
                systemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                Log.e(TAG, "can't access systemui resources",e);
                return null;
            }

            mShowCarrierLabel =
                    (ListPreference) findPreference(SHOW_CARRIER_LABEL);
            int showCarrierLabel = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_CARRIER, 1, UserHandle.USER_CURRENT);
            mShowCarrierLabel.setValue(String.valueOf(showCarrierLabel));
            mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntry());
            mShowCarrierLabel.setOnPreferenceChangeListener(this);
            mCustomCarrierLabel = (PreferenceScreen) prefSet.findPreference(CUSTOM_CARRIER_LABEL);

            mCarrierColorPicker = (ColorPickerPreference) findPreference(STATUS_BAR_CARRIER_COLOR);
            mCarrierColorPicker.setOnPreferenceChangeListener(this);
            intColorCarrierColor = Settings.System.getInt(getContentResolver(),
                        Settings.System.STATUS_BAR_CARRIER_COLOR, DEFAULT_STATUS_CARRIER_COLOR);
            hexColorCarrierColor = String.format("#%08x", (0xffffffff & intColorCarrierColor));
            mCarrierColorPicker.setSummary(hexColorCarrierColor);
            mCarrierColorPicker.setNewPreviewColor(intColorCarrierColor);

            mStatusBarCarrierSize = (SeekBarPreference) findPreference(STATUS_BAR_CARRIER_FONT_SIZE);
            mStatusBarCarrierSize.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, 14));
            mStatusBarCarrierSize.setOnPreferenceChangeListener(this);

            // Breathing Notifications
            mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
            mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);

            ConnectivityManager cm = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {

                mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                        Settings.System.KEY_MISSED_CALL_BREATH, 0) == 1);
                mMissedCallBreath.setOnPreferenceChangeListener(this);

                mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                        Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
                mVoicemailBreath.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mMissedCallBreath);
                prefSet.removePreference(mVoicemailBreath);
            }

            // Aokp logo color
            mAokpLogoColor =
                (ColorPickerPreference) prefSet.findPreference(KEY_AOKP_LOGO_COLOR);
            mAokpLogoColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AOKP_LOGO_COLOR, 0xffffffff);
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mAokpLogoColor.setSummary(hexColor);
            mAokpLogoColor.setNewPreviewColor(intColor);

            setHasOptionsMenu(true);
            mCheckPreferences = true;
            return prefSet;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        private void updateCustomLabelTextSummary() {
            mCustomCarrierLabelText = Settings.System.getString(
                this.getContentResolver(), Settings.System.CUSTOM_CARRIER_LABEL);

            if (TextUtils.isEmpty(mCustomCarrierLabelText)) {
                mCustomCarrierLabel.setSummary(R.string.custom_carrier_label_notset);
            } else {
                mCustomCarrierLabel.setSummary(mCustomCarrierLabelText);
            }
        }

        protected ContentResolver getContentResolver() {
            Context context = getActivity();
            if (context != null) {
                mContentResolver = context.getContentResolver();
            }
            return mContentResolver;
        }

        protected int getMetricsCategory() {
            // todo add a constant in MetricsLogger.java
            return CMMetricsLogger.DONT_LOG;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                final Preference preference) {
            final ContentResolver resolver = this.getContentResolver();
            if (preference.getKey().equals(CUSTOM_CARRIER_LABEL)) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.custom_carrier_label_title);
                alert.setMessage(R.string.custom_carrier_label_explain);

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                input.setText(TextUtils.isEmpty(mCustomCarrierLabelText) ? "" : mCustomCarrierLabelText);
                input.setSelection(input.getText().length());
                alert.setView(input);
                alert.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = ((Spannable) input.getText()).toString().trim();
                                Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value);
                                updateCustomLabelTextSummary();
                                Intent i = new Intent();
                                i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                                getActivity().sendBroadcast(i);
                    }
                });
                alert.setNegativeButton(getString(android.R.string.cancel), null);
                alert.show();
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (!mCheckPreferences) {
                return false;
            }
            AlertDialog dialog;
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mCarrierColorPicker) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.STATUS_BAR_CARRIER_COLOR, intHex);
                return true;
             } else if (preference == mShowCarrierLabel) {
                int showCarrierLabel = Integer.valueOf((String) newValue);
                int index = mShowCarrierLabel.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.
                    STATUS_BAR_SHOW_CARRIER, showCarrierLabel, UserHandle.USER_CURRENT);
                mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntries()[index]);
                return true;
             } else if (preference == mStatusBarCarrierSize) {
                int width = ((Integer)newValue).intValue();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, width);
                return true;
            } else if (preference == mMissedCallBreath) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.KEY_MISSED_CALL_BREATH, value ? 1 : 0);
                return true;
            } else if (preference == mVoicemailBreath) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.KEY_VOICEMAIL_BREATH, value ? 1 : 0);
                return true;
            } else if (preference == mAokpLogoColor) {
              String hex = ColorPickerPreference.convertToARGB(
                      Integer.valueOf(String.valueOf(newValue)));
              preference.setSummary(hex);
              int intHex = ColorPickerPreference.convertToColorInt(hex);
              Settings.System.putInt(resolver,
                      Settings.System.STATUS_BAR_AOKP_LOGO_COLOR, intHex);
              return true;
            }
            return false;
        }
    }
}
