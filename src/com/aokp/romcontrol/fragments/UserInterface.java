
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
    private static final String PREF_MENU_PERMANENT_OVERRIDE = "menu_permanent_override";

    CheckBoxPreference mStatusBarNotifCount;
    CheckBoxPreference mMenuOverride;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_NOTIF_COUNT, false));

        mMenuOverride = (CheckBoxPreference) findPreference(PREF_MENU_PERMANENT_OVERRIDE);
        mMenuOverride.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.MENU_PERMANENT_OVERRIDE, false));
        if (!hasHardwareButtons) {
            // these devices will always have the menu button
            getPreferenceScreen().removePreference(mMenuOverride);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT,
                    isCheckBoxPrefernceChecked(preference));
            return true;
        } else if (preference == mMenuOverride) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.MENU_PERMANENT_OVERRIDE,
                    isCheckBoxPrefernceChecked(preference));
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
