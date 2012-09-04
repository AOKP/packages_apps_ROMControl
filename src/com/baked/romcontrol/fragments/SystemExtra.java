
package com.baked.romcontrol.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.util.CMDProcessor;
import com.baked.romcontrol.util.Helpers;

public class SystemExtra extends BAKEDPreferenceFragment {

    public static final String TAG = "SystemExtra";

    private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";

    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mRecentKillAll;

    Random randomGenerator = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_system_extra);

        PreferenceScreen prefs = getPreferenceScreen();

        mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getInt(getActivity  ().getContentResolver(),
                Settings.System.RECENT_KILL_ALL_BUTTON, 0) == 1);

        mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
        if (mDisableBootAnimation.isChecked()) {
            Resources res = mContext.getResources();
            String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
            int randomInt = randomGenerator.nextInt(insults.length);
            mDisableBootAnimation.setSummary(insults[randomInt]);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mDisableBootAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.baked");
                Helpers.getMount("ro");
                Resources res = mContext.getResources();
                String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                int randomInt = randomGenerator.nextInt(insults.length);
                preference.setSummary(insults[randomInt]);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.baked /system/media/bootanimation.zip");
                Helpers.getMount("ro");
                preference.setSummary("");
            }
            return true;

        } else if (preference == mRecentKillAll) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENT_KILL_ALL_BUTTON, checked ? 1 : 0);
            Helpers.restartSystemUI();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
