
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.roman.romcontrol.R;
import com.roman.romcontrol.utils.ShortcutPickerHelper;

public class Lockscreens extends Activity {

    private ShortcutPickerHelper mPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new LockscreenPreferenceFragment()).commit();
    }

    public class LockscreenPreferenceFragment extends PreferenceFragment implements
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
        Preference mSmsPicker1;
        Preference mSmsPicker2;
        Preference mSmsPicker3;

        private Preference mCurrentCustomActivityPreference;
        private String mCurrentCustomActivityString;
        private String mSmsIntentUri;
        private String mSmsIntentUri1;
        private String mSmsIntentUri2;
        private String mSmsIntentUri3;

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
            
            mSmsPicker1 = findPreference(PREF_SMS_PICKER_1);
            
            mSmsPicker2 = findPreference(PREF_SMS_PICKER_2);
            
            mSmsPicker3 = findPreference(PREF_SMS_PICKER_3);

            mPicker = new ShortcutPickerHelper(this.getActivity(), this);

            mSmsIntentUri = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT);
            
            mSmsIntentUri1 = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT_1);
            
            mSmsIntentUri2 = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT_2);
            
            mSmsIntentUri3 = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT_3);
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
            } else if (preference == mSmsPicker1) {
                mCurrentCustomActivityPreference = preference;
                mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT_1;
                mPicker.pickShortcut();
                return true;    
            } else if (preference == mSmsPicker2) {
                mCurrentCustomActivityPreference = preference;
                mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT_2;
                mPicker.pickShortcut();
                return true;
            } else if (preference == mSmsPicker3) {
                mCurrentCustomActivityPreference = preference;
                mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT_3;
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
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public void refreshSettings() {
            mSmsPicker.setSummary(mPicker.getFriendlyNameForUri(mSmsIntentUri));
            
            mSmsPicker1.setSummary(mPicker.getFriendlyNameForUri(mSmsIntentUri1));
            
            mSmsPicker2.setSummary(mPicker.getFriendlyNameForUri(mSmsIntentUri2));
            
            mSmsPicker3.setSummary(mPicker.getFriendlyNameForUri(mSmsIntentUri3));
        }
        
        @Override
        public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
            Log.e("ROMAN", "shortcut picked");
            if (Settings.System.putString(getContentResolver(), mCurrentCustomActivityString, uri)) {
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

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ROMAN", "on activity result");
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

}

