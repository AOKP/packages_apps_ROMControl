package com.aokp.romcontrol.fragments;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

public class Performance extends AOKPPreferenceFragment
        {

    public static final String TAG = "Performance";
    public static final String KEY_FASTCHARGE = "fast_charge_boot";

    private SharedPreferences preferences;
    private boolean doneLoading = false;

    private CheckBoxPreference mFastCharge;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance);

        mFastCharge = (CheckBoxPreference) findPreference(KEY_FASTCHARGE);
        mFastCharge.setChecked(preferences.getBoolean(KEY_FASTCHARGE, false));
        if (!hasFastCharge) {
            ((PreferenceGroup) findPreference("kernel")).removePreference(mFastCharge);
        }

        doneLoading = true;
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

}
