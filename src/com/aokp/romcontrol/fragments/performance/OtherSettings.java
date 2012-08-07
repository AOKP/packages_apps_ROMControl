package com.aokp.romcontrol.fragments.performance;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.aokp.romcontrol.R;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class OtherSettings extends AOKPPreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    public static final String TAG = "OtherSettings";

    public static final String KEY_MINFREE = "free_memory";
    public static final String KEY_FASTCHARGE = "fast_charge_boot";
    public static final String MINFREE = "/sys/module/lowmemorykiller/parameters/minfree";

    private ListPreference mFreeMem;
    private CheckBoxPreference mFastCharge;
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
}
