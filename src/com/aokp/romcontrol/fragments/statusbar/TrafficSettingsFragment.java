/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

package com.aokp.romcontrol.fragments.statusbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.cyanogenmod.internal.logging.CMMetricsLogger;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class TrafficSettingsFragment extends Fragment {

    public TrafficSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_traffic_settings_main, container, false);

        Resources res = getResources();

        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.traffic_settings_main, new TrafficSettingsPreferenceFragment())
                .commit();

        return v;
    }

    public static class TrafficSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public TrafficSettingsPreferenceFragment() {

        }

        private static final String TAG = "TrafficSettingsFragment";

        private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
        private static final String NETWORK_TRAFFIC_COLOR = "network_traffic_color";
        private static final String NETWORK_TRAFFIC_UNIT = "network_traffic_unit";
        private static final String NETWORK_TRAFFIC_PERIOD = "network_traffic_period";
        private static final String NETWORK_TRAFFIC_AUTOHIDE = "network_traffic_autohide";
        private static final String NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold";
        private static final String NETWORK_TRAFFIC_HIDEARROW = "network_traffic_hidearrow";

        private ListPreference mNetTrafficState;
        private ColorPickerPreference mNetTrafficColor;
        private ListPreference mNetTrafficUnit;
        private ListPreference mNetTrafficPeriod;
        private SwitchPreference mNetTrafficAutohide;
        private SwitchPreference mNetTrafficHidearrow;
        private SeekBarPreferenceCham mNetTrafficAutohideThreshold;

        private static final int MENU_RESET = Menu.FIRST;
        private static final int DEFAULT_TRAFFIC_COLOR = 0xffffffff;

        private int mNetTrafficVal;
        private int MASK_UP;
        private int MASK_DOWN;
        private int MASK_UNIT;
        private int MASK_PERIOD;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_traffic_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            loadResources();

            mNetTrafficState = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_STATE);
            mNetTrafficUnit = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_UNIT);
            mNetTrafficPeriod = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_PERIOD);

            mNetTrafficAutohide =
                (SwitchPreference) prefSet.findPreference(NETWORK_TRAFFIC_AUTOHIDE);
            mNetTrafficAutohide.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE, 0) == 1));
            mNetTrafficAutohide.setOnPreferenceChangeListener(this);

            mNetTrafficAutohideThreshold =
                (SeekBarPreferenceCham) prefSet.findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
            int netTrafficAutohideThreshold = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 10);
                mNetTrafficAutohideThreshold.setValue(netTrafficAutohideThreshold / 1);
                mNetTrafficAutohideThreshold.setOnPreferenceChangeListener(this);

            mNetTrafficHidearrow =
                (SwitchPreference) prefSet.findPreference(NETWORK_TRAFFIC_HIDEARROW);
            mNetTrafficHidearrow.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_HIDEARROW, 0) == 1));
            mNetTrafficHidearrow.setOnPreferenceChangeListener(this);

            mNetTrafficColor =
                (ColorPickerPreference) prefSet.findPreference(NETWORK_TRAFFIC_COLOR);
            mNetTrafficColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_COLOR, 0xffffffff);
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
                mNetTrafficColor.setSummary(hexColor);
                mNetTrafficColor.setNewPreviewColor(intColor);

            if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED &&
                    TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED) {
                mNetTrafficVal = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_STATE, 0);
                int intIndex = mNetTrafficVal & (MASK_UP + MASK_DOWN);
                intIndex = mNetTrafficState.findIndexOfValue(String.valueOf(intIndex));
                updateNetworkTrafficState(intIndex);

                mNetTrafficState.setValueIndex(intIndex >= 0 ? intIndex : 0);
                mNetTrafficState.setSummary(mNetTrafficState.getEntry());
                mNetTrafficState.setOnPreferenceChangeListener(this);

                mNetTrafficUnit.setValueIndex(getBit(mNetTrafficVal, MASK_UNIT) ? 1 : 0);
                mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntry());
                mNetTrafficUnit.setOnPreferenceChangeListener(this);

                intIndex = (mNetTrafficVal & MASK_PERIOD) >>> 16;
                intIndex = mNetTrafficPeriod.findIndexOfValue(String.valueOf(intIndex));
                mNetTrafficPeriod.setValueIndex(intIndex >= 0 ? intIndex : 1);
                mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntry());
                mNetTrafficPeriod.setOnPreferenceChangeListener(this);
            }
            return prefSet;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        protected int getMetricsCategory() {
           return CMMetricsLogger.DONT_LOG;
        }

        private void updateNetworkTrafficState(int mIndex) {
            if (mIndex <= 0) {
                mNetTrafficUnit.setEnabled(false);
                mNetTrafficColor.setEnabled(false);
                mNetTrafficPeriod.setEnabled(false);
                mNetTrafficAutohide.setEnabled(false);
                mNetTrafficAutohideThreshold.setEnabled(false);
                mNetTrafficHidearrow.setEnabled(false);
            } else {
                mNetTrafficUnit.setEnabled(true);
                mNetTrafficColor.setEnabled(true);
                mNetTrafficPeriod.setEnabled(true);
                mNetTrafficAutohide.setEnabled(true);
                mNetTrafficAutohideThreshold.setEnabled(true);
                mNetTrafficHidearrow.setEnabled(true);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add(0, MENU_RESET, 0, R.string.network_traffic_color_reset)
                    .setIcon(R.drawable.ic_settings_backup)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_RESET:
                    resetToDefault();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }

        private void resetToDefault() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(R.string.network_traffic_color_reset);
            alertDialog.setMessage(R.string.network_traffic_color_reset_message);
            alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    NetworkTrafficColorReset();
                }
            });
            alertDialog.setNegativeButton(R.string.cancel, null);
            alertDialog.create().show();
     }

        private void NetworkTrafficColorReset() {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_COLOR, DEFAULT_TRAFFIC_COLOR);

            mNetTrafficColor.setNewPreviewColor(DEFAULT_TRAFFIC_COLOR);
            String hexColor = String.format("#%08x", (0xffffffff & DEFAULT_TRAFFIC_COLOR));
            mNetTrafficColor.setSummary(hexColor);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mNetTrafficState) {
                int intState = Integer.valueOf((String)newValue);
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
                int index = mNetTrafficState.findIndexOfValue((String) newValue);
                mNetTrafficState.setSummary(mNetTrafficState.getEntries()[index]);
                updateNetworkTrafficState(index);
                return true;
            } else if (preference == mNetTrafficColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_COLOR, intHex);
                return true;
            } else if (preference == mNetTrafficUnit) {
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_UNIT, ((String)newValue).equals("1"));
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
                int index = mNetTrafficUnit.findIndexOfValue((String) newValue);
                mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntries()[index]);
                return true;
            } else if (preference == mNetTrafficPeriod) {
                int intState = Integer.valueOf((String)newValue);
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_PERIOD, false) + (intState << 16);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
                int index = mNetTrafficPeriod.findIndexOfValue((String) newValue);
                mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntries()[index]);
                return true;
            } else if (preference == mNetTrafficAutohide) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_AUTOHIDE, value ? 1 : 0);
                return true;
            } else if (preference == mNetTrafficAutohideThreshold) {
                int threshold = (Integer) newValue;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, threshold * 1);
                return true;
            } else if (preference == mNetTrafficHidearrow) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NETWORK_TRAFFIC_HIDEARROW, value ? 1 : 0);
                return true;
            }
            return false;
        }

        private void loadResources() {
            Resources resources = getActivity().getResources();
            MASK_UP = resources.getInteger(R.integer.maskUp);
            MASK_DOWN = resources.getInteger(R.integer.maskDown);
            MASK_UNIT = resources.getInteger(R.integer.maskUnit);
            MASK_PERIOD = resources.getInteger(R.integer.maskPeriod);
        }

        private int setBit(int intNumber, int intMask, boolean blnState) {
            if (blnState) {
                return (intNumber | intMask);
            }
            return (intNumber & ~intMask);
        }

        private boolean getBit(int intNumber, int intMask) {
            return (intNumber & intMask) == intMask;
        }
    }
}
