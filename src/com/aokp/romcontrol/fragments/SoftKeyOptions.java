
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
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavBarHelpers;
import static com.android.internal.util.aokp.AwesomeConstants.*;

public class SoftKeyOptions extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    private String[] mActions;
    private String[] mActionCodes;

    ListPreference mHomeLong;
    ListPreference mHomeShort;
    ListPreference mHomeDouble;
    ListPreference mMenuLong;
    ListPreference mMenuShort;
    ListPreference mMenuDouble;
    ListPreference mBackLong;
    ListPreference mBackShort;
    ListPreference mBackDouble;
    ListPreference mSearchLong;
    ListPreference mSearchShort;
    ListPreference mSearchDouble;
    ListPreference mRecentsLong;
    ListPreference mRecentsShort;
    ListPreference mRecentsDouble;
    CheckBoxPreference mEnableSoftKey;
    CheckBoxPreference mEnableSoftKeyDoubleClick;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_softkey);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_softkey);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

                // Get NavBar Actions
        mActionCodes = NavBarHelpers.getNavBarActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext, mActionCodes[i]);
        }

        mEnableSoftKey = (CheckBoxPreference) findPreference("enable_softkey");
        mEnableSoftKey.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.SOFT_KEY_ENABLE, false));

        mEnableSoftKeyDoubleClick = (CheckBoxPreference) findPreference("enable_softkey_double_click");
        mEnableSoftKeyDoubleClick.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.SOFT_KEY_ENABLE_DOUBLE_CLICK, false));

        mHomeLong = (ListPreference) findPreference("softkey_home_long");
        mHomeLong.setOnPreferenceChangeListener(this);
        mHomeLong.setSummary(getProperSummary(mHomeLong));
        mHomeLong.setEntries(mActions);
        mHomeLong.setEntryValues(mActionCodes);

        mHomeShort = (ListPreference) findPreference("softkey_home_short");
        mHomeShort.setOnPreferenceChangeListener(this);
        mHomeShort.setSummary(getProperSummary(mHomeShort));
        mHomeShort.setEntries(mActions);
        mHomeShort.setEntryValues(mActionCodes);

        mHomeDouble = (ListPreference) findPreference("softkey_home_double");
        mHomeDouble.setOnPreferenceChangeListener(this);
        mHomeDouble.setSummary(getProperSummary(mHomeDouble));
        mHomeDouble.setEntries(mActions);
        mHomeDouble.setEntryValues(mActionCodes);

        mMenuLong = (ListPreference) findPreference("softkey_menu_long");
        mMenuLong.setOnPreferenceChangeListener(this);
        mMenuLong.setSummary(getProperSummary(mMenuLong));
        mMenuLong.setEntries(mActions);
        mMenuLong.setEntryValues(mActionCodes);

        mMenuShort = (ListPreference) findPreference("softkey_menu_short");
        mMenuShort.setOnPreferenceChangeListener(this);
        mMenuShort.setSummary(getProperSummary(mMenuShort));
        mMenuShort.setEntries(mActions);
        mMenuShort.setEntryValues(mActionCodes);

        mMenuDouble = (ListPreference) findPreference("softkey_menu_double");
        mMenuDouble.setOnPreferenceChangeListener(this);
        mMenuDouble.setSummary(getProperSummary(mMenuDouble));
        mMenuDouble.setEntries(mActions);
        mMenuDouble.setEntryValues(mActionCodes);

        mBackLong = (ListPreference) findPreference("softkey_back_long");
        mBackLong.setOnPreferenceChangeListener(this);
        mBackLong.setSummary(getProperSummary(mBackLong));
        mBackLong.setEntries(mActions);
        mBackLong.setEntryValues(mActionCodes);

        mBackShort = (ListPreference) findPreference("softkey_back_short");
        mBackShort.setOnPreferenceChangeListener(this);
        mBackShort.setSummary(getProperSummary(mBackShort));
        mBackShort.setEntries(mActions);
        mBackShort.setEntryValues(mActionCodes);

        mBackDouble = (ListPreference) findPreference("softkey_back_double");
        mBackDouble.setOnPreferenceChangeListener(this);
        mBackDouble.setSummary(getProperSummary(mBackDouble));
        mBackDouble.setEntries(mActions);
        mBackDouble.setEntryValues(mActionCodes);

        mSearchLong = (ListPreference) findPreference("softkey_search_long");
        mSearchLong.setOnPreferenceChangeListener(this);
        mSearchLong.setSummary(getProperSummary(mSearchLong));
        mSearchLong.setEntries(mActions);
        mSearchLong.setEntryValues(mActionCodes);

        mSearchShort = (ListPreference) findPreference("softkey_search_short");
        mSearchShort.setOnPreferenceChangeListener(this);
        mSearchShort.setSummary(getProperSummary(mSearchShort));
        mSearchShort.setEntries(mActions);
        mSearchShort.setEntryValues(mActionCodes);

        mSearchDouble = (ListPreference) findPreference("softkey_search_double");
        mSearchDouble.setOnPreferenceChangeListener(this);
        mSearchDouble.setSummary(getProperSummary(mSearchDouble));
        mSearchDouble.setEntries(mActions);
        mSearchDouble.setEntryValues(mActionCodes);

        mRecentsLong = (ListPreference) findPreference("softkey_recents_long");
        mRecentsLong.setOnPreferenceChangeListener(this);
        mRecentsLong.setSummary(getProperSummary(mRecentsLong));
        mRecentsLong.setEntries(mActions);
        mRecentsLong.setEntryValues(mActionCodes);

        mRecentsShort = (ListPreference) findPreference("softkey_recents_short");
        mRecentsShort.setOnPreferenceChangeListener(this);
        mRecentsShort.setSummary(getProperSummary(mRecentsShort));
        mRecentsShort.setEntries(mActions);
        mRecentsShort.setEntryValues(mActionCodes);

        mRecentsDouble = (ListPreference) findPreference("softkey_recents_double");
        mRecentsDouble.setOnPreferenceChangeListener(this);
        mRecentsDouble.setSummary(getProperSummary(mRecentsDouble));
        mRecentsDouble.setEntries(mActions);
        mRecentsDouble.setEntryValues(mActionCodes);

        boolean hasHome = getResources().getBoolean(R.bool.has_home_key);
        boolean hasBack = getResources().getBoolean(R.bool.has_back_key);
        boolean hasMenu = getResources().getBoolean(R.bool.has_menu_key);
        boolean hasRecents = getResources().getBoolean(R.bool.has_recents_key);
        boolean hasSearch = getResources().getBoolean(R.bool.has_search_key);

        if (!hasHome) {
            prefs.removePreference(mHomeLong);
            prefs.removePreference(mHomeShort);
            prefs.removePreference(mHomeDouble);
        }

        if (!hasMenu) {
            prefs.removePreference(mMenuLong);
            prefs.removePreference(mMenuShort);
            prefs.removePreference(mMenuDouble);
        }

        if (!hasBack) {
            prefs.removePreference(mBackLong);
            prefs.removePreference(mBackShort);
            prefs.removePreference(mBackDouble);
        }

        if (!hasRecents) {
            prefs.removePreference(mRecentsLong);
            prefs.removePreference(mRecentsShort);
            prefs.removePreference(mRecentsDouble);
        }

        if (!hasSearch) {
            prefs.removePreference(mSearchLong);
            prefs.removePreference(mSearchShort);
            prefs.removePreference(mSearchDouble);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableSoftKey) {

            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SOFT_KEY_ENABLE,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mEnableSoftKeyDoubleClick) {

            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SOFT_KEY_ENABLE_DOUBLE_CLICK,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mHomeLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_HOME[1];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_HOME[1], (String) newValue);
                mHomeLong.setSummary(getProperSummary(mHomeLong));
            }
        }else if (preference == mHomeShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_HOME[0];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_HOME[0], (String) newValue);
                mHomeShort.setSummary(getProperSummary(mHomeShort));
            }
        }else if (preference == mHomeDouble) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_HOME[2];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_HOME[2], (String) newValue);
                mHomeDouble.setSummary(getProperSummary(mHomeDouble));
            }
        } else if (preference == mMenuLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_MENU[1];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_MENU[1], (String) newValue);
                mMenuLong.setSummary(getProperSummary(mMenuLong));
            }
        }else if (preference == mMenuShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_MENU[0];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_MENU[0], (String) newValue);
                mMenuShort.setSummary(getProperSummary(mMenuShort));
            }
        }else if (preference == mMenuDouble) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_MENU[2];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_MENU[2], (String) newValue);
                mMenuDouble.setSummary(getProperSummary(mMenuDouble));
            }
        } else if (preference == mBackLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_BACK[1];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_BACK[1], (String) newValue);
                mBackLong.setSummary(getProperSummary(mBackLong));
            }
        }else if (preference == mBackShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_BACK[0];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_BACK[0], (String) newValue);
                mBackShort.setSummary(getProperSummary(mBackShort));
            }
        }else if (preference == mBackDouble) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_BACK[2];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_BACK[2], (String) newValue);
                mBackDouble.setSummary(getProperSummary(mBackDouble));
            }
        } else if (preference == mSearchLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_SEARCH[1];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_SEARCH[1], (String) newValue);
                mSearchLong.setSummary(getProperSummary(mSearchLong));
            }
        }else if (preference == mSearchShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_SEARCH[0];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_SEARCH[0], (String) newValue);
                mSearchShort.setSummary(getProperSummary(mSearchShort));
            }
        }else if (preference == mSearchDouble) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_SEARCH[2];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_SEARCH[2], (String) newValue);
                mSearchDouble.setSummary(getProperSummary(mSearchDouble));
            }
        } else if (preference == mRecentsLong) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_APPSWITCH[1];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_APPSWITCH[1], (String) newValue);
                mRecentsLong.setSummary(getProperSummary(mRecentsLong));
            }
        }else if (preference == mRecentsShort) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_APPSWITCH[0];
            if (newValue.equals("**app**")) {
                 mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_APPSWITCH[0], (String) newValue);
                mRecentsShort.setSummary(getProperSummary(mRecentsShort));
            }
        }else if (preference == mRecentsDouble) {
            mPreference = preference;
            mString = Settings.System.SOFT_KEY_APPSWITCH[2];
            if (newValue.equals("**app**")) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(getContentResolver(),
                           Settings.System.SOFT_KEY_APPSWITCH[2], (String) newValue);
                mRecentsDouble.setSummary(getProperSummary(mRecentsDouble));
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
            mString = Settings.System.SOFT_KEY_HOME[1];
        } else if (preference == mHomeShort) {
            mString = Settings.System.SOFT_KEY_HOME[0];
        } else if (preference == mHomeDouble) {
            mString = Settings.System.SOFT_KEY_HOME[2];
        } else if (preference == mMenuLong) {
            mString = Settings.System.SOFT_KEY_MENU[1];
        } else if (preference == mMenuShort) {
            mString = Settings.System.SOFT_KEY_MENU[0];
        } else if (preference == mMenuDouble) {
            mString = Settings.System.SOFT_KEY_MENU[2];
        } else if (preference == mBackLong) {
            mString = Settings.System.SOFT_KEY_BACK[1];
        } else if (preference == mBackShort) {
            mString = Settings.System.SOFT_KEY_BACK[0];
        } else if (preference == mBackDouble) {
            mString = Settings.System.SOFT_KEY_BACK[2];
        } else if (preference == mSearchLong) {
            mString = Settings.System.SOFT_KEY_SEARCH[1];
        } else if (preference == mSearchShort) {
            mString = Settings.System.SOFT_KEY_SEARCH[0];
        } else if (preference == mSearchDouble) {
            mString = Settings.System.SOFT_KEY_SEARCH[2];
        } else if (preference == mRecentsLong) {
            mString = Settings.System.SOFT_KEY_APPSWITCH[1];
        } else if (preference == mRecentsShort) {
            mString = Settings.System.SOFT_KEY_APPSWITCH[0];
        } else if (preference == mRecentsDouble) {
            mString = Settings.System.SOFT_KEY_APPSWITCH[2];
        }

        String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
        if (uri == null) {
            return getResources().getString(R.string.navbar_action_none);
        } else {
            return NavBarHelpers.getProperSummary(getActivity(), uri);
        }
   }
}
