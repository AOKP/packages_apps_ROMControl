
package com.roman.romcontrol.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;

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

        Preference mEnabledToggles;
        ListPreference mBrightnessLocation;

        private final String[] availableGsmToggles = {
                "ROTATE", "BT", "GPS", "DATA", "WIFI", "2G"
        };

        private final String[] availableCdmaToggles = {
                "ROTATE", "BT", "GPS", "LTE", "DATA", "WIFI"
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
