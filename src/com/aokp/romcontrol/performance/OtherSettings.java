
package com.aokp.romcontrol.performance;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class OtherSettings extends AOKPPreferenceFragment implements
        OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    public static final String TAG = "OtherSettings";

    public static final String KEY_MINFREE = "free_memory";
    public static final String KEY_FASTCHARGE = "fast_charge_boot";
    public static final String MINFREE = "/sys/module/lowmemorykiller/parameters/minfree";
    public static final String KEY_DAILY_REBOOT = "daily_reboot";

    private ListPreference mFreeMem;
    private CheckBoxPreference mFastCharge;
    private CheckBoxPreference mDailyReboot;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.performance_other_settings);

        final int minFree = getMinFreeValue();
        final String values[] = getResources().getStringArray(R.array.minfree_values);
        String closestValue = preferences.getString(KEY_MINFREE, values[0]);

        if (minFree < 37)
            closestValue = values[0];
        else if (minFree < 62)
            closestValue = values[1];
        else if (minFree < 77)
            closestValue = values[2];
        else if (minFree < 90)
            closestValue = values[3];
        else
            closestValue = values[4];

        mFreeMem = (ListPreference) findPreference(KEY_MINFREE);
        mFreeMem.setValue(closestValue);
        mFreeMem.setSummary(getString(R.string.ps_free_memory, minFree + "mb"));

        mFastCharge = (CheckBoxPreference) findPreference(KEY_FASTCHARGE);
        mFastCharge.setChecked(preferences.getBoolean(KEY_FASTCHARGE, false));
        if (!hasFastCharge) {
            ((PreferenceGroup) findPreference("kernel")).removePreference(mFastCharge);
        }

        mDailyReboot = (CheckBoxPreference) findPreference(KEY_DAILY_REBOOT);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRebootSummary();
    }

    public void updateRebootSummary() {
        if (mDailyReboot.isChecked()) {
            int[] rebootTime = getUserSpecifiedRebootTime(mContext);
            java.text.DateFormat f = DateFormat.getTimeFormat(mContext);
            GregorianCalendar d = new GregorianCalendar();
            d.set(Calendar.HOUR_OF_DAY, rebootTime[0]);
            d.set(Calendar.MINUTE, rebootTime[1]);
            Resources res = getResources();
            mDailyReboot
                    .setSummary(String.format(
                            res.getString(R.string.performance_daily_reboot_summary),
                            f.format(d.getTime())));
        } else {
            mDailyReboot.setSummary(mContext
                    .getString(R.string.performance_daily_reboot_summary_unscheduled));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();

        if (KEY_FASTCHARGE.equals(key)) {
            if (preferences.getBoolean(KEY_FASTCHARGE, false)) {
                Resources res = getActivity().getResources();
                String warningMessage = res.getString(R.string.fast_charge_warning);
                String cancel = res.getString(R.string.cancel);
                String ok = res.getString(R.string.ok);

                new AlertDialog.Builder(getActivity())
                        .setMessage(warningMessage)
                        .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preferences.edit().putBoolean(KEY_FASTCHARGE, false).apply();
                                mFastCharge.setChecked(false);
                            }
                        })
                        .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preferences.edit().putBoolean(KEY_FASTCHARGE, true).apply();
                                mFastCharge.setChecked(true);
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        } else if (preference == mDailyReboot) {
            if (((CheckBoxPreference) preference).isChecked()) {
                getFragmentManager().beginTransaction()
                        .addToBackStack("timepicker").add(new TimePickerFragment(), "timepicker")
                        .commit();
            } else {
                updateRebootSummary();
                // send intent to unschedule
                Intent schedule = new Intent(getActivity(),
                        DailyRebootScheduleService.class);
                getActivity().startService(schedule);
            }
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_MINFREE)) {
            String values = preferences.getString(key, null);
            if (!values.equals(null))
                new CMDProcessor().su
                        .runWaitFor("busybox echo " + values + " > " + MINFREE);
            mFreeMem.setSummary(getString(R.string.ps_free_memory, getMinFreeValue() + "mb"));
        }
    }

    private int getMinFreeValue() {
        int emptyApp = 0;
        String MINFREE_LINE = Helpers.readOneLine(MINFREE);
        String EMPTY_APP = MINFREE_LINE.substring(MINFREE_LINE.lastIndexOf(",") + 1);

        if (!EMPTY_APP.equals(null) || !EMPTY_APP.equals("")) {
            try {
                int mb = Integer.parseInt(EMPTY_APP.trim()) * 4 / 1024;
                emptyApp = (int) Math.ceil(mb);
            } catch (NumberFormatException nfe) {
                Log.i(TAG, "error processing " + EMPTY_APP);
            }
        }
        return emptyApp;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setUserSpecifiedRebootTime(getActivity(), hourOfDay, minute);
            Intent schedule = new Intent(getActivity(),
                    DailyRebootScheduleService.class);
            getActivity().startService(schedule);
            OtherSettings.this.updateRebootSummary();
        }
    }

    public static boolean isDailyRebootEnabled(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(OtherSettings.KEY_DAILY_REBOOT, false);
    }

    public static int[] getUserSpecifiedRebootTime(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        int[] time = new int[2];
        time[0] = prefs.getInt(OtherSettings.KEY_DAILY_REBOOT + "_hour", 1);
        time[1] = prefs.getInt(OtherSettings.KEY_DAILY_REBOOT + "_minute", 0);
        return time;
    }

    public static void setUserSpecifiedRebootTime(Context c, int hour, int minutes) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putInt(OtherSettings.KEY_DAILY_REBOOT + "_hour", hour).
                putInt(OtherSettings.KEY_DAILY_REBOOT + "_minute", minutes).commit();
    }

}
