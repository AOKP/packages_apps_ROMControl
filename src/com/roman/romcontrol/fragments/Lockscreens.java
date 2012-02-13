
package com.roman.romcontrol.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.roman.romcontrol.R;
import com.roman.romcontrol.SettingsPreferenceFragment;
import com.roman.romcontrol.util.ShortcutPickerHelper;

public class Lockscreens extends SettingsPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private static final String PREF_MENU = "pref_lockscreen_menu_unlock";
    private static final String PREF_USER_OVERRIDE = "lockscreen_user_timeout_override";
    private static final String PREF_LOCKSCREEN_LAYOUT = "pref_lockscreen_layout";

    private static final String PREF_VOLUME_WAKE = "volume_wake";
    private static final String PREF_VOLUME_MUSIC = "volume_music_controls";
    
    private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";

    CheckBoxPreference menuButtonLocation;
    CheckBoxPreference mLockScreenTimeoutUserOverride;
    ListPreference mLockscreenOption;
    CheckBoxPreference mVolumeWake;
    CheckBoxPreference mVolumeMusic;
    CheckBoxPreference mLockscreenLandscape;
    CheckBoxPreference mLockscreenBattery;

    private Preference mCurrentCustomActivityPreference;
    private String mCurrentCustomActivityString;

    private ShortcutPickerHelper mPicker;

    ArrayList<String> keys = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keys.add(Settings.System.LOCKSCREEN_HIDE_NAV);
        keys.add(Settings.System.LOCKSCREEN_LANDSCAPE);
        keys.add(Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        keys.add(Settings.System.ENABLE_FAST_TORCH);

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
        
        mLockscreenBattery = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_BATTERY);
        mLockscreenBattery.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_BATTERY,
                0) == 1);

        mVolumeWake = (CheckBoxPreference) findPreference(PREF_VOLUME_WAKE);
        mVolumeWake.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN,
                0) == 1);

        mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.VOLUME_MUSIC_CONTROLS,
                0) == 1);

        mPicker = new ShortcutPickerHelper(this, this);

        for (String key : keys) {
            try {
                ((CheckBoxPreference) findPreference(key))
                        .setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                                key) == 1);
            } catch (SettingNotFoundException e) {
            }
        }

        ((PreferenceGroup) findPreference("advanced_cat"))
                .removePreference(findPreference(Settings.System.LOCKSCREEN_HIDE_NAV));

        refreshSettings();
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
            
        } else if (preference == mLockscreenBattery) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_BATTERY,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
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
            Log.e("RC_Lockscreens", "key: " + preference.getKey());
            return Settings.System.putInt(getActivity().getContentResolver(),
                    preference.getKey(),
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void refreshSettings() {

        int lockscreenTargets = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_LAYOUT, 2);

        PreferenceGroup targetGroup = (PreferenceGroup) findPreference("lockscreen_targets");
        targetGroup.removeAll();

        // quad only uses first 4, but we make the system think there's 6 for the alternate layout
        // so only show 4
        if (lockscreenTargets == 6) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[4], "**null**");
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[5], "**null**");
            lockscreenTargets = 4;
        }

        for (int i = 0; i < lockscreenTargets; i++) {
            ListPreference p = new ListPreference(getActivity());
            String dialogTitle = String.format(
                    getResources().getString(R.string.custom_app_n_dialog_title), i + 1);
            ;
            p.setDialogTitle(dialogTitle);
            p.setEntries(R.array.lockscreen_choice_entries);
            p.setEntryValues(R.array.lockscreen_choice_values);
            String title = String.format(getResources().getString(R.string.custom_app_n), i + 1);
            p.setTitle(title);
            p.setKey("lockscreen_target_" + i);
            p.setSummary(getProperSummary(i));
            p.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(p);
        }

    }

    private String getProperSummary(int i) {
        String uri = Settings.System.getString(getActivity()
                .getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[i]);

        if (uri == null)
            return getResources().getString(R.string.lockscreen_action_none);

        if (uri.startsWith("**")) {
            if (uri.equals("**unlock**"))
                return getResources().getString(R.string.lockscreen_action_unlock);
            else if (uri.equals("**sound**"))
                return getResources().getString(R.string.lockscreen_action_sound);
            else if (uri.equals("**camera**"))
                return getResources().getString(R.string.lockscreen_action_camera);
            else if (uri.equals("**phone**"))
                return getResources().getString(R.string.lockscreen_action_phone);
            else if (uri.equals("**null**"))
                return getResources().getString(R.string.lockscreen_action_none);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
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
            refreshSettings();
            return true;

        } else if (preference.getKey().startsWith("lockscreen_target")) {
            int index = Integer.parseInt(preference.getKey().substring(
                    preference.getKey().lastIndexOf("_") + 1));
            Log.e("ROMAN", "lockscreen target, index: " + index);

            if (newValue.equals("**app**")) {
                mCurrentCustomActivityPreference = preference;
                mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[index];
                mPicker.pickShortcut();
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[index], (String) newValue);
                refreshSettings();
            }
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ROMAN", "ACTIVITY RESULT");

        mPicker.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }
}
