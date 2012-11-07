
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

public class SPenGestures extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    ListPreference mLeft;
    ListPreference mRight;
    ListPreference mUp;
    ListPreference mDown;
    ListPreference mDouble;
    ListPreference mLong;
    CheckBoxPreference mEnableSPen;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_spen);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_spen);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

        mEnableSPen = (CheckBoxPreference) findPreference("enable_spen");
        mEnableSPen.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.ENABLE_STYLUS_GESTURES, false));

        mLeft = (ListPreference) findPreference("spen_left");
        mLeft.setOnPreferenceChangeListener(this);
        mLeft.setSummary(getProperSummary(mLeft));

        mRight = (ListPreference) findPreference("spen_right");
        mRight.setOnPreferenceChangeListener(this);
        mRight.setSummary(getProperSummary(mRight));

        mUp = (ListPreference) findPreference("spen_up");
        mUp.setOnPreferenceChangeListener(this);
        mUp.setSummary(getProperSummary(mUp));

        mDown = (ListPreference) findPreference("spen_down");
        mDown.setOnPreferenceChangeListener(this);
        mDown.setSummary(getProperSummary(mDown));

        mDouble = (ListPreference) findPreference("spen_double");
        mDouble.setOnPreferenceChangeListener(this);
        mDouble.setSummary(getProperSummary(mDouble));

        mLong = (ListPreference) findPreference("spen_long");
        mLong.setOnPreferenceChangeListener(this);
        mLong.setSummary(getProperSummary(mLong));


    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableSPen) {

            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.ENABLE_STYLUS_GESTURES,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mLeft) {
            mPreference = preference;
            mString = Settings.System.GESTURES_LEFT_SWIPE;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.GESTURES_LEFT_SWIPE, (String) newValue);
            mLeft.setSummary(getProperSummary(mLeft));
            }
        } else if (preference == mRight) {
            mPreference = preference;
            mString = Settings.System.GESTURES_RIGHT_SWIPE;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.GESTURES_RIGHT_SWIPE, (String) newValue);
            mRight.setSummary(getProperSummary(mRight));
            }
        } else if (preference == mUp) {
            mPreference = preference;
            mString = Settings.System.GESTURES_UP_SWIPE;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.GESTURES_UP_SWIPE, (String) newValue);
            mUp.setSummary(getProperSummary(mUp));
            }
        } else if (preference == mDown) {
            mPreference = preference;
            mString = Settings.System.GESTURES_DOWN_SWIPE;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.GESTURES_DOWN_SWIPE, (String) newValue);
            mDown.setSummary(getProperSummary(mDown));
            }
        } else if (preference == mDouble) {
            mPreference = preference;
            mString = Settings.System.GESTURES_DOUBLE_TAP;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.GESTURES_DOUBLE_TAP, (String) newValue);
            mDouble.setSummary(getProperSummary(mDouble));
            }
        } else if (preference == mLong) {
            mPreference = preference;
            mString = Settings.System.GESTURES_LONG_PRESS;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.GESTURES_LONG_PRESS, (String) newValue);
            mLong.setSummary(getProperSummary(mLong));
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
        if (preference == mLeft) {
            mString = Settings.System.GESTURES_LEFT_SWIPE;
        } else if (preference == mRight) {
            mString = Settings.System.GESTURES_RIGHT_SWIPE;
        } else if (preference == mUp) {
            mString = Settings.System.GESTURES_UP_SWIPE;
        } else if (preference == mDown) {
            mString = Settings.System.GESTURES_DOWN_SWIPE;
        } else if (preference == mDouble) {
            mString = Settings.System.GESTURES_DOUBLE_TAP;
        } else if (preference == mLong) {
            mString = Settings.System.GESTURES_LONG_PRESS;
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
            else if (uri.equals("**menu**"))
                return getResources().getString(R.string.navbar_action_menu);
            else if (uri.equals("**ime**"))
                return getResources().getString(R.string.navbar_action_ime);
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
