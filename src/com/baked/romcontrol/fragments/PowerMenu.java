
package com.baked.romcontrol.fragments;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;

public class PowerMenu extends BAKEDPreferenceFragment {

    private static final String PREF_SCREENSHOT = "show_screenshot";
    private static final String PREF_REBOOT_CHOOSER = "show_reboot_chooser";
    private static final String PREF_SHOW_EXPANDED_DESKTOP_TOGGLE = "show_expanded_desktop_toggle";
    private static final String PREF_NAVBAR_HIDE = "show_navbar_hide";
    private static final String PREF_AIRPLANE_TOGGLE = "show_airplane_toggle";
    private static final String PREF_SHOW_PROFILE_CHOOSER = "show_profile_chooser";
    private static final String PREF_SHOW_SOUND_CHOOSER = "show_sound_chooser";

    CheckBoxPreference mShowScreenShot;
    CheckBoxPreference mShowRebootChooser;
    CheckBoxPreference mShowExpandedDesktopToggle;
    CheckBoxPreference mShowAirplaneToggle;
    CheckBoxPreference mShowNavBarHide;
    CheckBoxPreference mShowProfileChooser;
    CheckBoxPreference mShowSoundChooser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_powermenu);

        mShowScreenShot = (CheckBoxPreference) findPreference(PREF_SCREENSHOT);
        mShowScreenShot.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SCREENSHOT, 0) == 1);

        mShowRebootChooser = (CheckBoxPreference) findPreference(PREF_REBOOT_CHOOSER);
        mShowRebootChooser.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_REBOOT_CHOOSER, 1) == 1);

        mShowExpandedDesktopToggle = (CheckBoxPreference) findPreference(PREF_SHOW_EXPANDED_DESKTOP_TOGGLE);
        mShowExpandedDesktopToggle.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_EXPANDED_DESKTOP_TOGGLE, 0) == 1);

        mShowAirplaneToggle = (CheckBoxPreference) findPreference(PREF_AIRPLANE_TOGGLE);
        mShowAirplaneToggle.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_AIRPLANE_TOGGLE, 1) == 1);

        mShowNavBarHide = (CheckBoxPreference) findPreference(PREF_NAVBAR_HIDE);
        mShowNavBarHide.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_NAVBAR_HIDE, 0) == 1);

        mShowProfileChooser = (CheckBoxPreference) findPreference(PREF_SHOW_PROFILE_CHOOSER);
        mShowProfileChooser.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_PROFILE_CHOOSER, 0) == 1);

        mShowSoundChooser = (CheckBoxPreference) findPreference(PREF_SHOW_SOUND_CHOOSER);
        mShowSoundChooser.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.POWER_DIALOG_SHOW_SOUND_CHOOSER, 1) == 1);

        // I took this code out to allow Tablets the ability to Hide Navbar.
        // Note going to completely delete it incase an issue arises and needs to
        // be re-implemented - Z
        //if (mTablet) {
        //    getPreferenceScreen().removePreference(mShowNavBarHide);
        //}
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        if (preference == mShowScreenShot) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_SCREENSHOT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mShowRebootChooser) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_REBOOT_CHOOSER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mShowExpandedDesktopToggle) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_EXPANDED_DESKTOP_TOGGLE,
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

        } else if (preference == mShowProfileChooser) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_PROFILE_CHOOSER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mShowSoundChooser) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.POWER_DIALOG_SHOW_SOUND_CHOOSER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
