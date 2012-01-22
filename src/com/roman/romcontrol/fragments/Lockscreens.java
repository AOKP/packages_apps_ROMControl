
package com.roman.romcontrol.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.roman.romcontrol.R;
import com.roman.romcontrol.util.ShortcutPickerHelper;

public class Lockscreens extends PreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private static final String PREF_MENU = "pref_lockscreen_menu_unlock";
    private static final String PREF_USER_OVERRIDE = "lockscreen_user_timeout_override";
    private static final String PREF_LOCKSCREEN_LAYOUT = "pref_lockscreen_layout";
    private static final String PREF_SMS_PICKER = "sms_picker";
    private static final String PREF_SMS_PICKER_1 = "sms_picker_1";
    private static final String PREF_SMS_PICKER_2 = "sms_picker_2";
    private static final String PREF_SMS_PICKER_3 = "sms_picker_3";
    private static final String PREF_VOLUME_WAKE = "volume_wake";
    private static final String PREF_VOLUME_MUSIC = "volume_music_controls";

    CheckBoxPreference menuButtonLocation;
    CheckBoxPreference mLockScreenTimeoutUserOverride;
    ListPreference mLockscreenOption;
    CheckBoxPreference mVolumeWake;
    CheckBoxPreference mVolumeMusic;
    Preference mSmsPicker;
    Preference mAppPicker1;
    Preference mAppPicker2;
    Preference mAppPicker3;

    private Preference mCurrentCustomActivityPreference;
    private String mCurrentCustomActivityString;
    private String mSmsIntentUri;
    private String mCustomAppUri1;
    private String mCustomAppUri2;
    private String mCustomAppUri3;

    private ShortcutPickerHelper mPicker;

    ArrayList<String> keys = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keys.add("lockscreen_show_nav");

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

        mLockscreenOption = (ListPreference) findPreference(PREF_LOCKSCREEN_LAYOUT);
        mLockscreenOption.setOnPreferenceChangeListener(this);
        mLockscreenOption.setValue(Settings.System.getInt(
                getActivity().getContentResolver(), Settings.System.LOCKSCREEN_LAYOUT,
                0) + "");

        mVolumeWake = (CheckBoxPreference) findPreference(PREF_VOLUME_WAKE);
        mVolumeWake.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN,
                0) == 1);

        mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.VOLUME_MUSIC_CONTROLS,
                0) == 1);

        mSmsPicker = findPreference(PREF_SMS_PICKER);

        mAppPicker1 = findPreference(PREF_SMS_PICKER_1);

        mAppPicker2 = findPreference(PREF_SMS_PICKER_2);

        mAppPicker3 = findPreference(PREF_SMS_PICKER_3);

        mPicker = new ShortcutPickerHelper(this.getActivity(), this);

        mSmsIntentUri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT);

        mCustomAppUri1 = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_INTENT_1);

        mCustomAppUri2 = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_INTENT_2);

        mCustomAppUri3 = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_INTENT_3);

        for (String key : keys) {
            try {
                ((CheckBoxPreference) findPreference(key))
                        .setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                                key) == 1);
            } catch (SettingNotFoundException e) {
            }
        }
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
        } else if (preference == mSmsPicker) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT;
            mPicker.pickShortcut();
            return true;
        } else if (preference == mAppPicker1) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_INTENT_1;
            mPicker.pickShortcut();
            return true;
        } else if (preference == mAppPicker2) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_INTENT_2;
            mPicker.pickShortcut();
            return true;
        } else if (preference == mAppPicker3) {
            mCurrentCustomActivityPreference = preference;
            mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_INTENT_3;
            mPicker.pickShortcut();
            return true;
        } else if (preference == mVolumeWake) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_WAKE_SCREEN,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mVolumeMusic) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_MUSIC_CONTROLS,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (keys.contains(preference.getKey())) {
            return Settings.System.putInt(getActivity().getContentResolver(),
                    preference.getKey(),
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void refreshSettings() {
        mSmsPicker.setSummary(mPicker.getFriendlyNameForUri(mSmsIntentUri));

        mAppPicker1.setSummary(mPicker.getFriendlyNameForUri(mCustomAppUri1));

        mAppPicker2.setSummary(mPicker.getFriendlyNameForUri(mCustomAppUri2));

        mAppPicker3.setSummary(mPicker.getFriendlyNameForUri(mCustomAppUri3));
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        Log.e("ROMAN", "shortcut picked");
        if (Settings.System.putString(getActivity().getContentResolver(),
                mCurrentCustomActivityString, uri)) {
            mCurrentCustomActivityPreference.setSummary(friendlyName);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenOption) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_LAYOUT, val);
            return true;

        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ROMAN", "on activity result");
        mPicker.onActivityResult(requestCode, resultCode, data);
    }
}
