
package com.aokp.romcontrol.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import java.io.IOException;

public class StatusBarGeneral extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_SETTINGS_BUTTON_BEHAVIOR = "settings_behavior";
    private static final String PREF_AUTO_HIDE_TOGGLES = "auto_hide_toggles";
    private static final String PREF_BRIGHTNESS_TOGGLE = "status_bar_brightness_toggle";
    private static final String PREF_ADB_ICON = "adb_icon";
    private static final String PREF_TRANSPARENCY = "status_bar_transparency";
    private static final String PREF_LAYOUT = "status_bar_layout";

    CheckBoxPreference mDefaultSettingsButtonBehavior;
    CheckBoxPreference mAutoHideToggles;
    CheckBoxPreference mStatusBarBrightnessToggle;
    CheckBoxPreference mAdbIcon;
    ListPreference mTransparency;
    ListPreference mLayout;

    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_general);

        mDefaultSettingsButtonBehavior = (CheckBoxPreference) findPreference(PREF_SETTINGS_BUTTON_BEHAVIOR);
        mDefaultSettingsButtonBehavior.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_SETTINGS_BEHAVIOR,
                0) == 1);

        mAutoHideToggles = (CheckBoxPreference) findPreference(PREF_AUTO_HIDE_TOGGLES);
        mAutoHideToggles.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE,
                0) == 1);

        mStatusBarBrightnessToggle = (CheckBoxPreference) findPreference(PREF_BRIGHTNESS_TOGGLE);
        mStatusBarBrightnessToggle.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE,
                0) == 1);

        mAdbIcon = (CheckBoxPreference) findPreference(PREF_ADB_ICON);
        mAdbIcon.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.ADB_ICON, 1) == 1);
        
        mTransparency = (ListPreference) findPreference(PREF_TRANSPARENCY);
        mTransparency.setOnPreferenceChangeListener(this);
        mTransparency.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_TRANSPARENCY,
                100)));

        mLayout = (ListPreference) findPreference(PREF_LAYOUT);
        mLayout.setOnPreferenceChangeListener(this);
        mLayout.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_LAYOUT,
                0)));


        if (mTablet) {
            PreferenceScreen prefs = getPreferenceScreen();
            prefs.removePreference(mStatusBarBrightnessToggle);
            prefs.removePreference(mAutoHideToggles);
            prefs.removePreference(mDefaultSettingsButtonBehavior);
            prefs.removePreference(mTransparency);
            prefs.removePreference(mLayout);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mDefaultSettingsButtonBehavior) {

            Log.e("LOL", "b");
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_SETTINGS_BEHAVIOR,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mAutoHideToggles) {

            Log.e("LOL", "m");
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_QUICKTOGGLES_AUTOHIDE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mStatusBarBrightnessToggle) {

            Log.e("LOL", "m");
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_TOGGLE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mAdbIcon) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.ADB_ICON, checked ? 1 : 0);
            return true;
            
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mTransparency) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TRANSPARENCY, val);
            restartSystemUI();
        } else if (preference == mLayout) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_LAYOUT, val);
            restartSystemUI();
        }
        return result;
    }
    private void restartSystemUI() {
        try {
            Runtime.getRuntime().exec("pkill -TERM -f  com.android.systemui");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
