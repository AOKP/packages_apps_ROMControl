
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;

import java.net.URISyntaxException;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;

public class SoftKeyOptions extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    ListPreference mHomeLong;
    ListPreference mHomeShort;
    ListPreference mMenuLong;
    ListPreference mMenuShort;
    ListPreference mBackLong;
    ListPreference mBackShort;
    ListPreference mSearchLong;
    ListPreference mSearchShort;
    ListPreference mRecentsLong;
    ListPreference mRecentsShort;
    CheckBoxPreference mEnableSoftKey;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_softkey);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_softkey);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

        mEnableSoftKey = (CheckBoxPreference) findPreference("enable_softkey");
        mEnableSoftKey.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.SOFT_KEY_ENABLE, false));

        mHomeLong = (ListPreference) findPreference("softkey_home_long");
        mHomeLong.setOnPreferenceChangeListener(this);
        mHomeLong.setSummary(getProperSummary(mHomeLong));

        mHomeShort = (ListPreference) findPreference("softkey_home_short");
        mHomeShort.setOnPreferenceChangeListener(this);
        mHomeShort.setSummary(getProperSummary(mHomeShort));

        mMenuLong = (ListPreference) findPreference("softkey_menu_long");
        mMenuLong.setOnPreferenceChangeListener(this);
        mMenuLong.setSummary(getProperSummary(mMenuLong));

        mMenuShort = (ListPreference) findPreference("softkey_menu_short");
        mMenuShort.setOnPreferenceChangeListener(this);
        mMenuShort.setSummary(getProperSummary(mMenuShort));

        mBackLong = (ListPreference) findPreference("softkey_back_long");
        mBackLong.setOnPreferenceChangeListener(this);
        mBackLong.setSummary(getProperSummary(mBackLong));

        mBackShort = (ListPreference) findPreference("softkey_back_short");
        mBackShort.setOnPreferenceChangeListener(this);
        mBackShort.setSummary(getProperSummary(mBackShort));

        mSearchLong = (ListPreference) findPreference("softkey_search_long");
        mSearchLong.setOnPreferenceChangeListener(this);
        mSearchLong.setSummary(getProperSummary(mSearchLong));

        mSearchShort = (ListPreference) findPreference("softkey_search_short");
        mSearchShort.setOnPreferenceChangeListener(this);
        mSearchShort.setSummary(getProperSummary(mSearchShort));

        mRecentsLong = (ListPreference) findPreference("softkey_recents_long");
        mRecentsLong.setOnPreferenceChangeListener(this);
        mRecentsLong.setSummary(getProperSummary(mRecentsLong));

        mRecentsShort = (ListPreference) findPreference("softkey_recents_short");
        mRecentsShort.setOnPreferenceChangeListener(this);
        mRecentsShort.setSummary(getProperSummary(mRecentsShort));

        boolean hasHome = getResources().getBoolean(R.bool.has_home_key);
        boolean hasBack = getResources().getBoolean(R.bool.has_back_key);
        boolean hasMenu = getResources().getBoolean(R.bool.has_menu_key);
        boolean hasRecents = getResources().getBoolean(R.bool.has_recents_key);
        boolean hasSearch = getResources().getBoolean(R.bool.has_search_key);

        if (!hasHome) {
        prefs.removePreference(mHomeLong);
        prefs.removePreference(mHomeShort);
        }

        if (!hasMenu) {
        prefs.removePreference(mMenuLong);
        prefs.removePreference(mMenuShort);
        }

        if (!hasBack) {
        prefs.removePreference(mBackLong);
        prefs.removePreference(mBackShort);
        }

        if (!hasRecents) {
        prefs.removePreference(mRecentsLong);
        prefs.removePreference(mRecentsShort);
        }

        if (!hasSearch) {
        prefs.removePreference(mSearchLong);
        prefs.removePreference(mSearchShort);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableSoftKey) {

            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SOFT_KEY_ENABLE,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mHomeLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_HOME_LONG_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_HOME_LONG_PRESS, (String) newValue);
            mHomeLong.setSummary(getProperSummary(mHomeLong));
            }
        }else if (preference == mHomeShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_HOME_SHORT_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_HOME_SHORT_PRESS, (String) newValue);
            mHomeShort.setSummary(getProperSummary(mHomeShort));
            }
        } else if (preference == mMenuLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_MENU_LONG_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_MENU_LONG_PRESS, (String) newValue);
            mMenuLong.setSummary(getProperSummary(mMenuLong));
            }
        }else if (preference == mMenuShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_MENU_SHORT_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_MENU_SHORT_PRESS, (String) newValue);
            mMenuShort.setSummary(getProperSummary(mMenuShort));
            }
        } else if (preference == mBackLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_BACK_LONG_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_BACK_LONG_PRESS, (String) newValue);
            mBackLong.setSummary(getProperSummary(mBackLong));
            }
        }else if (preference == mBackShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_BACK_SHORT_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_BACK_SHORT_PRESS, (String) newValue);
            mBackShort.setSummary(getProperSummary(mBackShort));
            }

        } else if (preference == mSearchLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_SEARCH_LONG_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_SEARCH_LONG_PRESS, (String) newValue);
            mSearchLong.setSummary(getProperSummary(mSearchLong));
            }
        }else if (preference == mSearchShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_SEARCH_SHORT_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_SEARCH_SHORT_PRESS, (String) newValue);
            mSearchShort.setSummary(getProperSummary(mSearchShort));
            }
        } else if (preference == mRecentsLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_APPSWITCH_LONG_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_APPSWITCH_LONG_PRESS, (String) newValue);
            mRecentsLong.setSummary(getProperSummary(mRecentsLong));
            }
        }else if (preference == mRecentsShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_APPSWITCH_SHORT_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SOFT_KEY_APPSWITCH_SHORT_PRESS, (String) newValue);
            mRecentsShort.setSummary(getProperSummary(mRecentsShort));
            }
        }
        return result;
    }

    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
          mPreference.setSummary(friendlyName);
          Settings.System.putString(getContentResolver(), mString, (String) uri);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getProperSummary(Preference preference) {
        if (preference == mHomeLong) {
            mString = Settings.System.SOFT_KEY_HOME_LONG_PRESS;
        } else if (preference == mHomeShort) {
            mString = Settings.System.SOFT_KEY_HOME_SHORT_PRESS;
        } else if (preference == mMenuLong) {
            mString = Settings.System.SOFT_KEY_MENU_LONG_PRESS;
        } else if (preference == mMenuShort) {
            mString = Settings.System.SOFT_KEY_MENU_SHORT_PRESS;
        } else if (preference == mBackLong) {
            mString = Settings.System.SOFT_KEY_BACK_LONG_PRESS;
        } else if (preference == mBackShort) {
            mString = Settings.System.SOFT_KEY_BACK_SHORT_PRESS;
        } else if (preference == mSearchLong) {
            mString = Settings.System.SOFT_KEY_SEARCH_LONG_PRESS;
        } else if (preference == mSearchShort) {
            mString = Settings.System.SOFT_KEY_SEARCH_SHORT_PRESS;
        } else if (preference == mRecentsLong) {
            mString = Settings.System.SOFT_KEY_APPSWITCH_LONG_PRESS;
        } else if (preference == mRecentsShort) {
            mString = Settings.System.SOFT_KEY_APPSWITCH_SHORT_PRESS;
        }

        String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
        if (uri == null)
            return getResources().getString(R.string.navbar_action_none);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**"))
                return getResources().getString(R.string.navbar_action_home);
            else if (uri.equals("**back**"))
                return getResources().getString(R.string.navbar_action_back);
            else if (uri.equals("**recents**"))
                return getResources().getString(R.string.navbar_action_recents);
            else if (uri.equals("**search**"))
                return getResources().getString(R.string.navbar_action_search);
            else if (uri.equals("**screenshot**"))
                return getResources().getString(R.string.navbar_action_screenshot);
            else if (uri.equals("**menu**"))
                return getResources().getString(R.string.navbar_action_menu);
            else if (uri.equals("**ime**"))
                return getResources().getString(R.string.navbar_action_ime);
            else if (uri.equals("**kill**"))
                return getResources().getString(R.string.navbar_action_kill);
            else if (uri.equals("**power**"))
                return getResources().getString(R.string.navbar_action_power);
            else if (uri.equals("**notifications**"))
                return getResources().getString(R.string.navbar_action_notifications);
            else if (uri.equals("**null**"))
                return getResources().getString(R.string.navbar_action_none);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
     return null;
   }
}
