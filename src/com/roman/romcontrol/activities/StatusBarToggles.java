
package com.roman.romcontrol.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.NumberPicker;

import com.android.internal.telephony.Phone;
import com.roman.romcontrol.R;

public class StatusBarToggles extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new StatusBarTogglePreference()).commit();
    }

    public class StatusBarTogglePreference extends PreferenceFragment implements
            OnPreferenceChangeListener {

        private static final String PREF_ENABLE_TOGGLES = "enable_toggles";
        private static final String PREF_BRIGHTNESS_LOC = "brightness_location";
        private static final String PREF_TOGGLES_STYLE = "toggle_style";
        private static final String PREF_TOGGLES_PER_ROW = "toggles_per_row";
        private static final String PREF_ALT_BUTTON_LAYOUT = "alternate_button_layout";

        Preference mEnabledToggles;
        ListPreference mBrightnessLocation;
        CheckBoxPreference mAlternateButtonLayout;
        ListPreference mToggleStyle;

        private final String[] availableGsmToggles = {
                "ROTATE", "BT", "GPS", "DATA", "WIFI", "2G", "AP", "AIRPLANE_MODE", "VIBRATE", "SILENT", "TORCH"
        };

        private final String[] availableCdmaToggles = {
                "ROTATE", "BT", "GPS", "LTE", "DATA", "WIFI", "AP", "AIRPLANE_MODE", "VIBRATE", "SILENT", "TORCH"
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_statusbar_toggles);

            mEnabledToggles = findPreference(PREF_ENABLE_TOGGLES);

            mBrightnessLocation = (ListPreference) findPreference(PREF_BRIGHTNESS_LOC);
            mBrightnessLocation.setOnPreferenceChangeListener(this);
            mBrightnessLocation.setValue(Integer.toString(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC,
                    1)));

            mToggleStyle = (ListPreference) findPreference(PREF_TOGGLES_STYLE);
            mToggleStyle.setOnPreferenceChangeListener(this);
            mToggleStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_STYLE,
                    3)));

            mAlternateButtonLayout = (CheckBoxPreference) findPreference(PREF_ALT_BUTTON_LAYOUT);
            mAlternateButtonLayout.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS, 0) == 1);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            if (preference == mEnabledToggles) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StatusBarToggles.this);

                TelephonyManager telephony = (TelephonyManager) getApplicationContext()
                        .getSystemService(Context.TELEPHONY_SERVICE);

                final boolean isCdma = (telephony.getCurrentPhoneType() == Phone.PHONE_TYPE_CDMA);

                ArrayList<String> enabledToggles = TogglesLayout
                        .getTogglesStringArray(getApplicationContext());

                boolean checkedToggles[] = new boolean[isCdma ? availableCdmaToggles.length
                        : availableGsmToggles.length];

                for (int i = 0; i < checkedToggles.length; i++) {
                    if (isCdma && enabledToggles.contains(availableCdmaToggles[i])) {
                        checkedToggles[i] = true;
                    } else if (!isCdma && enabledToggles.contains(availableGsmToggles[i])) {
                        checkedToggles[i] = true;
                    }
                }

                builder.setTitle("Choose which toggles to use");
                builder.setCancelable(false);
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setMultiChoiceItems(isCdma ? availableCdmaToggles : availableGsmToggles,
                        checkedToggles,
                        new OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                String toggleKey = (isCdma ? availableCdmaToggles[which]
                                        : availableGsmToggles[which]);

                                if (isChecked)
                                    addToggle(getApplicationContext(), toggleKey);
                                else
                                    removeToggle(getApplicationContext(), toggleKey);
                            }
                        });

                AlertDialog d = builder.create();

                d.show();

                return true;
            } else if (preference == mAlternateButtonLayout) {

                Settings.System.putInt(getContentResolver(),
                        Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean result = false;

            if (preference == mBrightnessLocation) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC, val);

            } else if (preference == mToggleStyle) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUSBAR_TOGGLES_STYLE, val);

            }
            return result;
        }
    }

    public static void addToggle(Context context, String key) {
        ArrayList<String> enabledToggles = TogglesLayout
                .getTogglesStringArray(context);
        enabledToggles.add(key);
        TogglesLayout.setTogglesFromStringArray(context, enabledToggles);
    }

    public static void removeToggle(Context context, String key) {
        ArrayList<String> enabledToggles = TogglesLayout
                .getTogglesStringArray(context);
        enabledToggles.remove(key);
        TogglesLayout.setTogglesFromStringArray(context, enabledToggles);
    }

}
