
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
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
            ShortcutPickerHelper.OnPickListener {

        private static final String PREF_MENU = "pref_lockscreen_menu_unlock";
        private static final String PREF_USER_OVERRIDE = "lockscreen_user_timeout_override";
        private static final String QUAD_TARGETS = "pref_lockscreen_quad_targets";
        private static final String PREF_SMS_PICKER = "sms_picker";

        CheckBoxPreference menuButtonLocation;
        CheckBoxPreference mLockScreenTimeoutUserOverride;
        CheckBoxPreference mQuadTargets;

        Preference mSmsPicker;

        private Preference mCurrentCustomActivityPreference;
        private String mCurrentCustomActivityString;
        private String mSmsIntentUri;

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

            mQuadTargets = (CheckBoxPreference) findPreference(QUAD_TARGETS);
            mQuadTargets.setChecked(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.LOCKSCREEN_QUAD_TARGETS,
                    0) == 1);

            mSmsPicker = findPreference(PREF_SMS_PICKER);

            mPicker = new ShortcutPickerHelper(this.getActivity(), this);

            mSmsIntentUri = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT);
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
            } else if (preference == mQuadTargets) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.LOCKSCREEN_QUAD_TARGETS,
                        ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
                return true;
            } else if (preference == mSmsPicker) {
                mCurrentCustomActivityPreference = preference;
                mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_SMS_INTENT;
                mPicker.pickShortcut();
                return true;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public void refreshSettings() {
            mSmsPicker.setSummary(mPicker.getFriendlyNameForUri(mSmsIntentUri));
        }

        @Override
        public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
            Log.e("ROMAN", "shortcut picked");
            if (Settings.System.putString(getContentResolver(), mCurrentCustomActivityString, uri)) {
                mCurrentCustomActivityPreference.setSummary(friendlyName);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ROMAN", "on activity result");
        mPicker.onActivityResult(requestCode, resultCode, data);
    }

}
