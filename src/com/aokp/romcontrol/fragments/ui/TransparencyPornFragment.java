/*
 * Copyright (c) 2016, The AICP Project
 * Copyright (c) 2016, The AOKP Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class TransparencyPornFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transparencyporn_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.transparencyporn_settings_main, new TransparencyPornSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class TransparencyPornSettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public TransparencyPornSettingsPreferenceFragment() {
        }

        private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
        //private static final String PREF_QS_TRANSPARENT_HEADER = "qs_transparent_header";
        private static final String PREF_TRANSPARENT_VOLUME_DIALOG = "transparent_volume_dialog";
        private static final String PREF_TRANSPARENT_POWER_MENU = "transparent_power_menu";
        private static final String PREF_TRANSPARENT_POWER_DIALOG_DIM = "transparent_power_dialog_dim";
        private static final String PREF_VOLUME_DIALOG_STROKE = "volume_dialog_stroke";
        private static final String PREF_VOLUME_DIALOG_STROKE_COLOR = "volume_dialog_stroke_color";
        private static final String PREF_VOLUME_DIALOG_STROKE_THICKNESS = "volume_dialog_stroke_thickness";
        private static final String PREF_VOLUME_DIALOG_CORNER_RADIUS = "volume_dialog_corner_radius";
        private static final String PREF_VOLUME_DIALOG_STROKE_DASH_WIDTH = "volume_dialog_dash_width";
        private static final String PREF_VOLUME_DIALOG_STROKE_DASH_GAP = "volume_dialog_dash_gap";
        private static final String PREF_QS_STROKE = "qs_stroke";
        private static final String PREF_QS_STROKE_COLOR = "qs_stroke_color";
        private static final String PREF_QS_STROKE_THICKNESS = "qs_stroke_thickness";
        private static final String PREF_QS_CORNER_RADIUS = "qs_corner_radius";
        private static final String PREF_QS_STROKE_DASH_WIDTH = "qs_dash_width";
        private static final String PREF_QS_STROKE_DASH_GAP = "qs_dash_gap";
        //private static final String PREF_NOTIFICATION_ALPHA = "notification_alpha";

        private SeekBarPreferenceCham mQSShadeAlpha;
        //private SeekBarPreferenceCham mQSHeaderAlpha;
        private SeekBarPreferenceCham mVolumeDialogAlpha;
        private SeekBarPreferenceCham mPowerMenuAlpha;
        private SeekBarPreferenceCham mPowerDialogDim;
        private ListPreference mVolumeDialogStroke;
        private ColorPickerPreference mVolumeDialogStrokeColor;
        private SeekBarPreferenceCham mVolumeDialogStrokeThickness;
        private SeekBarPreferenceCham mVolumeDialogCornerRadius;
        private SeekBarPreferenceCham mVolumeDialogDashWidth;
        private SeekBarPreferenceCham mVolumeDialogDashGap;
        private ListPreference mQSStroke;
        private ColorPickerPreference mQSStrokeColor;
        private SeekBarPreferenceCham mQSStrokeThickness;
        private SeekBarPreferenceCham mQSCornerRadius;
        private SeekBarPreferenceCham mQSDashWidth;
        private SeekBarPreferenceCham mQSDashGap;
        //private SeekBarPreferenceCham mNotificationsAlpha;

        static final int DEFAULT_VOLUME_DIALOG_STROKE_COLOR = 0xFF4285F4;
        static final int DEFAULT_QS_STROKE_COLOR = 0xFF4285F4;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_transparencyporn_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();

            // QS shade alpha
            mQSShadeAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_QS_TRANSPARENT_SHADE);
            int qSShadeAlpha = Settings.System.getInt(resolver,
                    Settings.System.QS_TRANSPARENT_SHADE, 255);
            mQSShadeAlpha.setValue(qSShadeAlpha / 1);
            mQSShadeAlpha.setOnPreferenceChangeListener(this);

            // QS header alpha
            /*mQSHeaderAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_QS_TRANSPARENT_HEADER);
            int qSHeaderAlpha = Settings.System.getInt(resolver,
                    Settings.System.QS_TRANSPARENT_HEADER, 255);
            mQSHeaderAlpha.setValue(qSHeaderAlpha / 1);
            mQSHeaderAlpha.setOnPreferenceChangeListener(this);*/

            // Volume dialog alpha
            mVolumeDialogAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_TRANSPARENT_VOLUME_DIALOG);
            int volumeDialogAlpha = Settings.System.getInt(resolver,
                    Settings.System.TRANSPARENT_VOLUME_DIALOG, 255);
            mVolumeDialogAlpha.setValue(volumeDialogAlpha / 1);
            mVolumeDialogAlpha.setOnPreferenceChangeListener(this);

            // Power menu alpha
            mPowerMenuAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_TRANSPARENT_POWER_MENU);
            int powerMenuAlpha = Settings.System.getInt(resolver,
                    Settings.System.TRANSPARENT_POWER_MENU, 100);
            mPowerMenuAlpha.setValue(powerMenuAlpha / 1);
            mPowerMenuAlpha.setOnPreferenceChangeListener(this);

            // Power/reboot dialog dim
            mPowerDialogDim =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_TRANSPARENT_POWER_DIALOG_DIM);
            int powerDialogDim = Settings.System.getInt(resolver,
                    Settings.System.TRANSPARENT_POWER_DIALOG_DIM, 50);
            mPowerDialogDim.setValue(powerDialogDim / 1);
            mPowerDialogDim.setOnPreferenceChangeListener(this);

            // Volume dialog stroke
            mVolumeDialogStroke =
                    (ListPreference) findPreference(PREF_VOLUME_DIALOG_STROKE);
            int volumeDialogStroke = Settings.System.getIntForUser(resolver,
                            Settings.System.VOLUME_DIALOG_STROKE, 0,
                            UserHandle.USER_CURRENT);
            mVolumeDialogStroke.setValue(String.valueOf(volumeDialogStroke));
            mVolumeDialogStroke.setSummary(mVolumeDialogStroke.getEntry());
            mVolumeDialogStroke.setOnPreferenceChangeListener(this);

            // Volume dialog stroke color
            mVolumeDialogStrokeColor =
                    (ColorPickerPreference) findPreference(PREF_VOLUME_DIALOG_STROKE_COLOR);
            mVolumeDialogStrokeColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_DIALOG_STROKE_COLOR, DEFAULT_VOLUME_DIALOG_STROKE_COLOR);
            String hexColor = String.format("#%08x", (0xFF4285F4 & intColor));
            mVolumeDialogStrokeColor.setSummary(hexColor);
            mVolumeDialogStrokeColor.setNewPreviewColor(intColor);

            // Volume dialog stroke thickness
            mVolumeDialogStrokeThickness =
                    (SeekBarPreferenceCham) findPreference(PREF_VOLUME_DIALOG_STROKE_THICKNESS);
            int volumeDialogStrokeThickness = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_DIALOG_STROKE_THICKNESS, 4);
            mVolumeDialogStrokeThickness.setValue(volumeDialogStrokeThickness / 1);
            mVolumeDialogStrokeThickness.setOnPreferenceChangeListener(this);

            // Volume dialog corner radius
            mVolumeDialogCornerRadius =
                    (SeekBarPreferenceCham) findPreference(PREF_VOLUME_DIALOG_CORNER_RADIUS);
            int volumeDialogCornerRadius = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_DIALOG_CORNER_RADIUS, 10);
            mVolumeDialogCornerRadius.setValue(volumeDialogCornerRadius / 1);
            mVolumeDialogCornerRadius.setOnPreferenceChangeListener(this);

            // Volume dialog dash width
            mVolumeDialogDashWidth =
                    (SeekBarPreferenceCham) findPreference(PREF_VOLUME_DIALOG_STROKE_DASH_WIDTH);
            int volumeDialogDashWidth = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_DIALOG_STROKE_DASH_WIDTH, 0);
            if (volumeDialogDashWidth != 0) {
                mVolumeDialogDashWidth.setValue(volumeDialogDashWidth / 1);
            } else {
                mVolumeDialogDashWidth.setValue(0);
            }
            mVolumeDialogDashWidth.setOnPreferenceChangeListener(this);

            // Volume dialog dash gap
            mVolumeDialogDashGap =
                    (SeekBarPreferenceCham) findPreference(PREF_VOLUME_DIALOG_STROKE_DASH_GAP);
            int volumeDialogDashGap = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_DIALOG_STROKE_DASH_GAP, 10);
            mVolumeDialogDashGap.setValue(volumeDialogDashGap / 1);
            mVolumeDialogDashGap.setOnPreferenceChangeListener(this);

            // QS stroke
            mQSStroke =
                    (ListPreference) findPreference(PREF_QS_STROKE);
            int qSStroke = Settings.System.getIntForUser(resolver,
                            Settings.System.QS_STROKE, 0,
                            UserHandle.USER_CURRENT);
            mQSStroke.setValue(String.valueOf(qSStroke));
            mQSStroke.setSummary(mQSStroke.getEntry());
            mQSStroke.setOnPreferenceChangeListener(this);

            // QS stroke color
            mQSStrokeColor =
                    (ColorPickerPreference) findPreference(PREF_QS_STROKE_COLOR);
            mQSStrokeColor.setOnPreferenceChangeListener(this);
            int qSIntColor = Settings.System.getInt(resolver,
                    Settings.System.QS_STROKE_COLOR, DEFAULT_QS_STROKE_COLOR);
            String qSHexColor = String.format("#%08x", (0xFF4285F4 & qSIntColor));
            mQSStrokeColor.setSummary(qSHexColor);
            mQSStrokeColor.setNewPreviewColor(qSIntColor);

            // QS stroke thickness
            mQSStrokeThickness =
                    (SeekBarPreferenceCham) findPreference(PREF_QS_STROKE_THICKNESS);
            int qSStrokeThickness = Settings.System.getInt(resolver,
                    Settings.System.QS_STROKE_THICKNESS, 4);
            mQSStrokeThickness.setValue(qSStrokeThickness / 1);
            mQSStrokeThickness.setOnPreferenceChangeListener(this);

            // QS corner radius
            mQSCornerRadius =
                    (SeekBarPreferenceCham) findPreference(PREF_QS_CORNER_RADIUS);
            int qSCornerRadius = Settings.System.getInt(resolver,
                    Settings.System.QS_CORNER_RADIUS, 5);
            mQSCornerRadius.setValue(qSCornerRadius / 1);
            mQSCornerRadius.setOnPreferenceChangeListener(this);

            // QS dash width
            mQSDashWidth =
                    (SeekBarPreferenceCham) findPreference(PREF_QS_STROKE_DASH_WIDTH);
            int qSDialogDashWidth = Settings.System.getInt(resolver,
                    Settings.System.QS_STROKE_DASH_WIDTH, 0);
            if (qSDialogDashWidth != 0) {
                mQSDashWidth.setValue(qSDialogDashWidth / 1);
            } else {
                mQSDashWidth.setValue(0);
            }
            mQSDashWidth.setOnPreferenceChangeListener(this);

            // QS dash gap
            mQSDashGap =
                    (SeekBarPreferenceCham) findPreference(PREF_QS_STROKE_DASH_GAP);
            int qSDialogDashGap = Settings.System.getInt(resolver,
                    Settings.System.QS_STROKE_DASH_GAP, 10);
            mQSDashGap.setValue(qSDialogDashGap / 1);
            mQSDashGap.setOnPreferenceChangeListener(this);

            // Notifications alpha
            /*mNotificationsAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_NOTIFICATION_ALPHA);
            int notificationsAlpha = Settings.System.getInt(resolver,
                    Settings.System.NOTIFICATION_ALPHA, 255);
            mNotificationsAlpha.setValue(notificationsAlpha / 1);
            mNotificationsAlpha.setOnPreferenceChangeListener(this);*/

            VolumeDialogSettingsDisabler(volumeDialogStroke);
            QSSettingsDisabler(qSStroke);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mQSShadeAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_TRANSPARENT_SHADE, alpha * 1);
                return true;
            /*} else if (preference == mQSHeaderAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_TRANSPARENT_HEADER, alpha * 1);
                return true;*/
            } else if (preference == mVolumeDialogAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.TRANSPARENT_VOLUME_DIALOG, alpha * 1);
                return true;
            } else if (preference == mPowerMenuAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.TRANSPARENT_POWER_MENU, alpha * 1);
                return true;
            } else if (preference == mPowerDialogDim) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.TRANSPARENT_POWER_DIALOG_DIM, alpha * 1);
                return true;
            } else if (preference == mVolumeDialogStroke) {
                int volumeDialogStroke = Integer.parseInt((String) newValue);
                int index = mVolumeDialogStroke.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.
                        VOLUME_DIALOG_STROKE, volumeDialogStroke, UserHandle.USER_CURRENT);
                mVolumeDialogStroke.setSummary(mVolumeDialogStroke.getEntries()[index]);
                VolumeDialogSettingsDisabler(volumeDialogStroke);
                return true;
            } else if (preference == mVolumeDialogStrokeColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.VOLUME_DIALOG_STROKE_COLOR, intHex);
                return true;
            } else if (preference == mVolumeDialogStrokeThickness) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.VOLUME_DIALOG_STROKE_THICKNESS, val * 1);
                return true;
            } else if (preference == mVolumeDialogCornerRadius) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.VOLUME_DIALOG_CORNER_RADIUS, val * 1);
                return true;
            } else if (preference == mVolumeDialogDashWidth) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.VOLUME_DIALOG_STROKE_DASH_WIDTH, val * 1);
                return true;
            } else if (preference == mVolumeDialogDashGap) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.VOLUME_DIALOG_STROKE_DASH_GAP, val * 1);
                return true;
            } else if (preference == mQSStroke) {
                int qSStroke = Integer.parseInt((String) newValue);
                int index = mQSStroke.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.
                        QS_STROKE, qSStroke, UserHandle.USER_CURRENT);
                mQSStroke.setSummary(mQSStroke.getEntries()[index]);
                QSSettingsDisabler(qSStroke);
                if (index == 0) {
                    Helpers.showSystemUIrestartDialog(getActivity());
                }
                return true;
            } else if (preference == mQSStrokeColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.QS_STROKE_COLOR, intHex);
                return true;
            } else if (preference == mQSStrokeThickness) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_STROKE_THICKNESS, val * 1);
                return true;
            } else if (preference == mQSCornerRadius) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_CORNER_RADIUS, val * 1);
                return true;
            } else if (preference == mQSDashWidth) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_STROKE_DASH_WIDTH, val * 1);
                return true;
            } else if (preference == mQSDashGap) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_STROKE_DASH_GAP, val * 1);
                return true;
            /*} else if (preference == mNotificationsAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.NOTIFICATION_ALPHA, alpha * 1);
                return true;*/
            }
            return false;
        }

        private void VolumeDialogSettingsDisabler(int volumeDialogStroke) {
            if (volumeDialogStroke == 0) {
                mVolumeDialogStrokeColor.setEnabled(false);
                mVolumeDialogStrokeThickness.setEnabled(false);
                mVolumeDialogDashWidth.setEnabled(false);
                mVolumeDialogDashGap.setEnabled(false);
            } else if (volumeDialogStroke == 1) {
                mVolumeDialogStrokeColor.setEnabled(false);
                mVolumeDialogStrokeThickness.setEnabled(true);
                mVolumeDialogDashWidth.setEnabled(true);
                mVolumeDialogDashGap.setEnabled(true);
            } else {
                mVolumeDialogStrokeColor.setEnabled(true);
                mVolumeDialogStrokeThickness.setEnabled(true);
                mVolumeDialogDashWidth.setEnabled(true);
                mVolumeDialogDashGap.setEnabled(true);
            }
        }

        private void QSSettingsDisabler(int qSStroke) {
            if (qSStroke == 0) {
                mQSStrokeColor.setEnabled(false);
                mQSStrokeThickness.setEnabled(false);
                mQSCornerRadius.setEnabled(false);
                mQSDashWidth.setEnabled(false);
                mQSDashGap.setEnabled(false);
            } else if (qSStroke == 1) {
                mQSStrokeColor.setEnabled(false);
                mQSStrokeThickness.setEnabled(true);
                mQSCornerRadius.setEnabled(true);
                mQSDashWidth.setEnabled(true);
                mQSDashGap.setEnabled(true);
            } else {
                mQSStrokeColor.setEnabled(true);
                mQSStrokeThickness.setEnabled(true);
                mQSCornerRadius.setEnabled(true);
                mQSDashWidth.setEnabled(true);
                mQSDashGap.setEnabled(true);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}