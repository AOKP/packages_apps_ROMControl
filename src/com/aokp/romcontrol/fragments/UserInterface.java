
package com.aokp.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

public class UserInterface extends AOKPPreferenceFragment {

    public static final String TAG = "UserInterface";

    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";

    CheckBoxPreference mStatusBarNotifCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_ui);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_NOTIF_COUNT,
                false));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
