/*
 * Copyright (C) 2014 The Dirty Unicorns Project
 * Copyright (c) 2018, The AOKP Project
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

package com.aokp.romcontrol.fragments.general;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

public class SuspendActionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_suspend_actions_settings_main, container, false);

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.suspend_action_settings_main, new SuspendActionsSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class SuspendActionsSettingsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        public SuspendActionsSettingsPreferenceFragment() {
        }

        private static final String TAG = "SuspendActions";

        private static final String SCREEN_STATE_TOOGLES_ENABLE = "screen_state_toggles_enable_key";
        private static final String SCREEN_STATE_TOOGLES_TWOG = "screen_state_toggles_twog";
        private static final String SCREEN_STATE_TOOGLES_GPS = "screen_state_toggles_gps";
        private static final String SCREEN_STATE_TOOGLES_MOBILE_DATA = "screen_state_toggles_mobile_data";
        private static final String SCREEN_STATE_ON_DELAY = "screen_state_on_delay";
        private static final String SCREEN_STATE_OFF_DELAY = "screen_state_off_delay";
        private static final String SCREEN_STATE_CATGEGORY_LOCATION = "screen_state_toggles_location_key";
        private static final String SCREEN_STATE_CATGEGORY_MOBILE_DATA = "screen_state_toggles_mobile_key";

        private Context mContext;

        private SwitchPreference mEnableScreenStateToggles;
        private SwitchPreference mEnableScreenStateTogglesTwoG;
        private SwitchPreference mEnableScreenStateTogglesGps;
        private SwitchPreference mEnableScreenStateTogglesMobileData;
        private SeekBarPreferenceCham mMinutesOffDelay;
        private SeekBarPreferenceCham mMinutesOnDelay;
        private PreferenceCategory mMobileDateCategory;
        private PreferenceCategory mLocationCategory;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_suspend_actions_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mContext = (Context) getActivity();

            mEnableScreenStateToggles = (SwitchPreference) findPreference(
                    SCREEN_STATE_TOOGLES_ENABLE);

            int enabled = Settings.System.getInt(resolver, Settings.System.START_SCREEN_STATE_SERVICE, 0);

            mEnableScreenStateToggles.setChecked(enabled != 0);
            mEnableScreenStateToggles.setOnPreferenceChangeListener(this);

            mMinutesOffDelay = (SeekBarPreferenceCham) findPreference(SCREEN_STATE_OFF_DELAY);
            int offd = Settings.System.getInt(resolver,
                    Settings.System.SCREEN_STATE_OFF_DELAY, 0);
            mMinutesOffDelay.setValue(offd / 60);
            mMinutesOffDelay.setOnPreferenceChangeListener(this);

            mMinutesOnDelay = (SeekBarPreferenceCham) findPreference(SCREEN_STATE_ON_DELAY);
            int ond = Settings.System.getInt(resolver,
                    Settings.System.SCREEN_STATE_ON_DELAY, 0);
            mMinutesOnDelay.setValue(ond / 60);
            mMinutesOnDelay.setOnPreferenceChangeListener(this);

            mMobileDateCategory = (PreferenceCategory) findPreference(
                    SCREEN_STATE_CATGEGORY_MOBILE_DATA);
            mLocationCategory = (PreferenceCategory) findPreference(
                    SCREEN_STATE_CATGEGORY_LOCATION);

            mEnableScreenStateTogglesTwoG = (SwitchPreference) findPreference(
                    SCREEN_STATE_TOOGLES_TWOG);

            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (!cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)){
            getPreferenceScreen().removePreference(mEnableScreenStateTogglesTwoG);
            } else {
                mEnableScreenStateTogglesTwoG.setChecked((
                        Settings.System.getInt(resolver, Settings.System.SCREEN_STATE_TWOG, 0) == 1));
                mEnableScreenStateTogglesTwoG.setOnPreferenceChangeListener(this);
            }

            mEnableScreenStateTogglesMobileData = (SwitchPreference) findPreference(
                    SCREEN_STATE_TOOGLES_MOBILE_DATA);

            if (!cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)){
                getPreferenceScreen().removePreference(mEnableScreenStateTogglesMobileData);
            } else {
                mEnableScreenStateTogglesMobileData.setChecked((
                        Settings.System.getInt(resolver, Settings.System.SCREEN_STATE_MOBILE_DATA, 0) == 1));
                mEnableScreenStateTogglesMobileData.setOnPreferenceChangeListener(this);
            }

            // Only enable these controls if this user is allowed to change location
            // sharing settings.
            final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
            boolean isLocationChangeAllowed = !um.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION);

            // TODO: check if gps is available on this device?
            mEnableScreenStateTogglesGps = (SwitchPreference) findPreference(
                    SCREEN_STATE_TOOGLES_GPS);

            if (!isLocationChangeAllowed){
                getPreferenceScreen().removePreference(mEnableScreenStateTogglesGps);
                mEnableScreenStateTogglesGps = null;
            } else {
                mEnableScreenStateTogglesGps.setChecked((
                        Settings.System.getInt(getActivity().getContentResolver(), Settings.System.SCREEN_STATE_GPS, 0) == 1));
                mEnableScreenStateTogglesGps.setOnPreferenceChangeListener(this);
            }

            mMobileDateCategory.setEnabled(enabled != 0);
            mLocationCategory.setEnabled(enabled != 0);

            return prefSet;
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mEnableScreenStateToggles) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.START_SCREEN_STATE_SERVICE, value ? 1 : 0);

                Intent service = (new Intent())
                        .setClassName("com.android.systemui", "com.android.systemui.screenstate.ScreenStateService");
                if (value) {
                    getActivity().stopService(service);
                    getActivity().startService(service);
                } else {
                    getActivity().stopService(service);
                }

                mMobileDateCategory.setEnabled(value);
                mLocationCategory.setEnabled(value);

                return true;
            } else if (preference == mEnableScreenStateTogglesTwoG) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_STATE_TWOG, value ? 1 : 0);

                Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
                mContext.sendBroadcast(intent);

                return true;
            } else if (preference == mEnableScreenStateTogglesGps) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_STATE_GPS, value ? 1 : 0);

                Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
                mContext.sendBroadcast(intent);

                return true;
            } else if (preference == mEnableScreenStateTogglesMobileData) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_STATE_MOBILE_DATA, value ? 1 : 0);

                Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
                mContext.sendBroadcast(intent);

                return true;
            } else if (preference == mMinutesOffDelay) {
                int delay = ((Integer) newValue) * 60;
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_STATE_OFF_DELAY, delay);

                return true;
            } else if (preference == mMinutesOnDelay) {
                int delay = ((Integer) newValue) * 60;
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_STATE_ON_DELAY, delay);

                return true;
            }

            return false;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private void restartService(){
            Intent service = (new Intent())
                    .setClassName("com.android.systemui", "com.android.systemui.screenstate.ScreenStateService");
            getActivity().stopService(service);
            getActivity().startService(service);
        }
    }
}
