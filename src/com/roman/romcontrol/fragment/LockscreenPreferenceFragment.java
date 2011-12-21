
package com.roman.romcontrol.fragment;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.roman.romcontrol.R;

public class LockscreenPreferenceFragment extends PreferenceFragment {

    private static final String PREF_MENU = "pref_lockscreen_menu_unlock";
    private static final String PREF_USER_OVERRIDE = "lockscreen_user_timeout_override";

    CheckBoxPreference menuButtonLocation;
    CheckBoxPreference mLockScreenTimeoutUserOverride;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_lockscreens);

        menuButtonLocation = (CheckBoxPreference) findPreference(PREF_MENU);
        menuButtonLocation.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_ENABLE_MENU_KEY,
                1) == 1);

        mLockScreenTimeoutUserOverride = (CheckBoxPreference) findPreference(PREF_USER_OVERRIDE);
        mLockScreenTimeoutUserOverride.setChecked(Settings.Secure.getInt(getActivity()
                .getContentResolver(), Settings.Secure.LOCK_SCREEN_LOCK_USER_OVERRIDE,
                0) == 1);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == menuButtonLocation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_ENABLE_MENU_KEY,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockScreenTimeoutUserOverride) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_LOCK_USER_OVERRIDE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
