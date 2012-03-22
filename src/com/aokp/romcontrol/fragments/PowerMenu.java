
package com.aokp.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

public class PowerMenu extends AOKPPreferenceFragment {

    private static final String PREF_POWER_SAVER = "show_power_saver";
    private static final String PREF_SCREENSHOT = "show_screenshot";
    private static final String PREF_TORCH_TOGGLE = "show_torch_toggle";
    private static final String PREF_AIRPLANE_TOGGLE = "show_airplane_toggle";
    private static final String PREF_NAVBAR_HIDE = "show_navbar_hide";

    CheckBoxPreference mShowPowerSaver;
    CheckBoxPreference mShowScreenShot;
    CheckBoxPreference mShowTorchToggle;
    CheckBoxPreference mShowAirplaneToggle;
    CheckBoxPreference mShowNavBarHide;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_powermenu);

        mShowPowerSaver = (CheckBoxPreference) findPreference(PREF_POWER_SAVER);
        int powerSaverVal = 0;
        try {
            powerSaverVal = Settings.Secure.getInt(getActivity()
                    .getContentResolver(), Settings.Secure.POWER_SAVER_MODE);
        } catch (SettingNotFoundException e) {
            mShowPowerSaver.setEnabled(false);
            mShowPowerSaver
                    .setSummary("You need to enable power saver before you can see it in the power menu.");
        }
        mShowPowerSaver.setChecked(powerSaverVal != 0);

        mShowScreenShot = (CheckBoxPreference) findPreference(PREF_SCREENSHOT);
        mShowScreenShot.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                0) == 1);

        mShowTorchToggle = (CheckBoxPreference) findPreference(PREF_TORCH_TOGGLE);
        mShowTorchToggle.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_TORCH_TOGGLE,
                0) == 1);

        mShowAirplaneToggle = (CheckBoxPreference) findPreference(PREF_AIRPLANE_TOGGLE);
        mShowAirplaneToggle.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_AIRPLANE_TOGGLE,
                1) == 1);

        mShowNavBarHide = (CheckBoxPreference) findPreference(PREF_NAVBAR_HIDE);
        mShowNavBarHide.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE,
                0) == 1);
        
        if(mTablet) {
            getPreferenceScreen().removePreference(mShowNavBarHide);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mShowPowerSaver) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_POWER_SAVER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowScreenShot) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowTorchToggle) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_TORCH_TOGGLE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowAirplaneToggle) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_AIRPLANE_TOGGLE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mShowNavBarHide) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
