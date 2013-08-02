package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavBarHelpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;

import static com.android.internal.util.aokp.AwesomeConstants.*;

public class SPenGestures extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    private boolean mSPenChecked;
    private String[] mActions;
    private String[] mActionCodes;

    ListPreference mLeft;
    ListPreference mRight;
    ListPreference mUp;
    ListPreference mDown;
    ListPreference mDouble;
    ListPreference mLong;
    CheckBoxPreference mEnableSPen;
    CheckBoxPreference mEnableIcon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_spen);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_spen);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

        // Get NavBar Actions
        mActionCodes = NavBarHelpers.getNavBarActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext, mActionCodes[i]);
        }

        mEnableSPen = (CheckBoxPreference) findPreference("enable_spen");
        mEnableSPen.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_SPEN_ACTIONS, false));
        mSPenChecked = mEnableSPen.isChecked();

        mEnableIcon = (CheckBoxPreference) findPreference("enable_stylus pointer");
        mEnableIcon.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.STYLUS_ICON_ENABLED, true));

        mLeft = (ListPreference) findPreference("spen_left");
        mLeft.setOnPreferenceChangeListener(this);
        mLeft.setEntries(mActions);
        mLeft.setEntryValues(mActionCodes);

        mRight = (ListPreference) findPreference("spen_right");
        mRight.setOnPreferenceChangeListener(this);
        mRight.setEntries(mActions);
        mRight.setEntryValues(mActionCodes);

        mUp = (ListPreference) findPreference("spen_up");
        mUp.setOnPreferenceChangeListener(this);
        mUp.setEntries(mActions);
        mUp.setEntryValues(mActionCodes);

        mDown = (ListPreference) findPreference("spen_down");
        mDown.setOnPreferenceChangeListener(this);
        mDown.setEntries(mActions);
        mDown.setEntryValues(mActionCodes);

        mDouble = (ListPreference) findPreference("spen_double");
        mDouble.setOnPreferenceChangeListener(this);
        mDouble.setEntries(mActions);
        mDouble.setEntryValues(mActionCodes);

        mLong = (ListPreference) findPreference("spen_long");
        mLong.setOnPreferenceChangeListener(this);
        mLong.setEntries(mActions);
        mLong.setEntryValues(mActionCodes);

        setSummaries();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mEnableSPen) {
            mSPenChecked = mEnableSPen.isChecked();
            Settings.System.putBoolean(mContentRes,
                    Settings.System.ENABLE_SPEN_ACTIONS,
                    mSPenChecked);
            setSummaries();
            return true;
        } else if (preference == mEnableIcon) {

            Settings.System.putBoolean(mContentRes,
                    Settings.System.STYLUS_ICON_ENABLED,
                    mEnableIcon.isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mLeft) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_LEFT];
            if (newValue.equals(AwesomeConstant.ACTION_APP.value())) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(mContentRes,
                        Settings.System.SPEN_ACTIONS[SWIPE_LEFT], (String) newValue);
                mLeft.setSummary(getProperSummary(mLeft));
            }
        } else if (preference == mRight) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_RIGHT];
            if (newValue.equals(AwesomeConstant.ACTION_APP.value())) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(mContentRes,
                        Settings.System.SPEN_ACTIONS[SWIPE_RIGHT], (String) newValue);
                mRight.setSummary(getProperSummary(mRight));
            }
        } else if (preference == mUp) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_UP];
            if (newValue.equals(AwesomeConstant.ACTION_APP.value())) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(mContentRes,
                        Settings.System.SPEN_ACTIONS[SWIPE_UP], (String) newValue);
                mUp.setSummary(getProperSummary(mUp));
            }
        } else if (preference == mDown) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[SWIPE_DOWN];
            if (newValue.equals(AwesomeConstant.ACTION_APP.value())) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(mContentRes,
                        Settings.System.SPEN_ACTIONS[SWIPE_DOWN], (String) newValue);
                mDown.setSummary(getProperSummary(mDown));
            }
        } else if (preference == mDouble) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[TAP_DOUBLE];
            if (newValue.equals(AwesomeConstant.ACTION_APP.value())) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(mContentRes,
                        Settings.System.SPEN_ACTIONS[TAP_DOUBLE], (String) newValue);
                mDouble.setSummary(getProperSummary(mDouble));
            }
        } else if (preference == mLong) {
            mPreference = preference;
            mString = Settings.System.SPEN_ACTIONS[PRESS_LONG];
            if (newValue.equals(AwesomeConstant.ACTION_APP.value())) {
                mPicker.pickShortcut();
            } else {
                result = Settings.System.putString(mContentRes,
                        Settings.System.SPEN_ACTIONS[PRESS_LONG], (String) newValue);
                mLong.setSummary(getProperSummary(mLong));
            }
        }
        return result;
    }

    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        mPreference.setSummary(friendlyName);
        Settings.System.putString(mContentRes, mString, (String) uri);
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

    private void setSummaries() {
        if (mSPenChecked) {
            mLeft.setSummary(getProperSummary(mLeft));
            mRight.setSummary(getProperSummary(mRight));
            mUp.setSummary(getProperSummary(mUp));
            mDown.setSummary(getProperSummary(mDown));
            mDouble.setSummary(getProperSummary(mDouble));
            mLong.setSummary(getProperSummary(mLong));
        } else {
            mLeft.setSummary(R.string.enable_spen_gestures);
            mRight.setSummary(R.string.enable_spen_gestures);
            mUp.setSummary(R.string.enable_spen_gestures);
            mDown.setSummary(R.string.enable_spen_gestures);
            mDouble.setSummary(R.string.enable_spen_gestures);
            mLong.setSummary(R.string.enable_spen_gestures);
        }
    }

    private String getProperSummary(Preference preference) {
        if (preference == mLeft) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_LEFT];
        } else if (preference == mRight) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_RIGHT];
        } else if (preference == mUp) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_UP];
        } else if (preference == mDown) {
            mString = Settings.System.SPEN_ACTIONS[SWIPE_DOWN];
        } else if (preference == mDouble) {
            mString = Settings.System.SPEN_ACTIONS[TAP_DOUBLE];
        } else if (preference == mLong) {
            mString = Settings.System.SPEN_ACTIONS[PRESS_LONG];
        }

        String uri = Settings.System.getString(mContentRes, mString);
        return NavBarHelpers.getProperSummary(mContext, uri);
    }
}
