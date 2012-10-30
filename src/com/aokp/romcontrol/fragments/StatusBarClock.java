
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
import android.util.Log;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarClock extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private static final String PREF_ENABLE = "clock_style";
    private static final String PREF_AM_PM_STYLE = "clock_am_pm_style";
    private static final String PREF_COLOR_PICKER = "clock_color";
    private static final String PREF_CLOCK_WEEKDAY = "clock_weekday";
    private static final String PREF_DATE_SHORTCLICK = "date_shortclick";
    private static final String PREF_DATE_LONGCLICK = "date_longclick";
    private static final String PREF_CLOCK_SHORTCLICK = "clock_shortclick";
    private static final String PREF_CLOCK_LONGCLICK = "clock_longclick";

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    ListPreference mClockStyle;
    ListPreference mClockAmPmstyle;
    ColorPickerPreference mColorPicker;
    ListPreference mClockWeekday;
    ListPreference mDateShortClick;
    ListPreference mDateLongClick;
    ListPreference mClockShortClick;
    ListPreference mClockLongClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_clock);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_clock);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

        mClockStyle = (ListPreference) findPreference(PREF_ENABLE);
        mClockStyle.setOnPreferenceChangeListener(this);
        mClockStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_STYLE,
                1)));

        mClockAmPmstyle = (ListPreference) findPreference(PREF_AM_PM_STYLE);
        mClockAmPmstyle.setOnPreferenceChangeListener(this);
        mClockAmPmstyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE,
                2)));

        mColorPicker = (ColorPickerPreference) findPreference(PREF_COLOR_PICKER);
        mColorPicker.setOnPreferenceChangeListener(this);

        mClockWeekday = (ListPreference) findPreference(PREF_CLOCK_WEEKDAY);
        mClockWeekday.setOnPreferenceChangeListener(this);
        mClockWeekday.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_CLOCK_WEEKDAY,
                0)));

        mDateShortClick = (ListPreference) findPreference(PREF_DATE_SHORTCLICK);
        mDateShortClick.setOnPreferenceChangeListener(this);
        mDateShortClick.setSummary(getProperSummary(mDateShortClick));

        mDateLongClick = (ListPreference) findPreference(PREF_DATE_LONGCLICK);
        mDateLongClick.setOnPreferenceChangeListener(this);
        mDateLongClick.setSummary(getProperSummary(mDateLongClick));

        mClockShortClick = (ListPreference) findPreference(PREF_CLOCK_SHORTCLICK);
        mClockShortClick.setOnPreferenceChangeListener(this);
        mClockShortClick.setSummary(getProperSummary(mClockShortClick));

        mClockLongClick = (ListPreference) findPreference(PREF_CLOCK_LONGCLICK);
        mClockLongClick.setOnPreferenceChangeListener(this);
        mClockLongClick.setSummary(getProperSummary(mClockLongClick));

        if (mTablet) {
            prefs.removePreference(mDateShortClick);
            prefs.removePreference(mDateLongClick);
            prefs.removePreference(mClockShortClick);
            prefs.removePreference(mClockLongClick);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mClockAmPmstyle) {

            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_AM_PM_STYLE, val);

        } else if (preference == mClockStyle) {

            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_STYLE, val);

        } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_COLOR, intHex);
            Log.e("ROMAN", intHex + "");
        } else if (preference == mClockWeekday) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_CLOCK_WEEKDAY, val);
        } else if (preference == mDateShortClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_DATE_SHORTCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_DATE_SHORTCLICK, (String) newValue);
            mDateShortClick.setSummary(getProperSummary(mDateShortClick));
            }
        } else if (preference == mDateLongClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_DATE_LONGCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_DATE_LONGCLICK, (String) newValue);
            mDateLongClick.setSummary(getProperSummary(mDateLongClick));
            }
        } else if (preference == mClockShortClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_CLOCK_SHORTCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK_SHORTCLICK, (String) newValue);
            mClockShortClick.setSummary(getProperSummary(mClockShortClick));
            }
        } else if (preference == mClockLongClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_CLOCK_LONGCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK_LONGCLICK, (String) newValue);
            mClockLongClick.setSummary(getProperSummary(mClockLongClick));
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
        if (preference == mDateLongClick) {
            mString = Settings.System.NOTIFICATION_DATE_LONGCLICK;
        } else if (preference == mClockLongClick) {
            mString = Settings.System.NOTIFICATION_CLOCK_LONGCLICK;
        } else if (preference == mDateShortClick) {
            mString = Settings.System.NOTIFICATION_DATE_SHORTCLICK;
        } else if (preference == mClockShortClick) {
            mString = Settings.System.NOTIFICATION_CLOCK_SHORTCLICK;
        }

        String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
        String empty = "";

        if (uri == null)
            return empty;

        if (uri.startsWith("**")) {
            if (uri.equals("**alarm**"))
                return getResources().getString(R.string.alarm);
            else if (uri.equals("**event**"))
                return getResources().getString(R.string.event);
            else if (uri.equals("**assist**"))
                return getResources().getString(R.string.voiceassist);
            else if (uri.equals("**today**"))
                return getResources().getString(R.string.today);
            else if (uri.equals("**nothing**"))
                return getResources().getString(R.string.nothing);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }
}
