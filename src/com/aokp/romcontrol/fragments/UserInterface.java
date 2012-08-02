
package com.aokp.romcontrol.fragments;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Spannable;
import android.widget.EditText;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class UserInterface extends AOKPPreferenceFragment {

    public static final String TAG = "UserInterface";

    private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";

    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mEnableVolumeOptions;
    CheckBoxPreference mStatusBarNotifCount;

    Random randomGenerator = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
        mEnableVolumeOptions.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ENABLE_VOLUME_OPTIONS, 0) == 1);

        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_NOTIF_COUNT,
                0) == 1);

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
         if (preference == mEnableVolumeOptions) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ENABLE_VOLUME_OPTIONS, checked ? 1 : 0);
            return true;

        } else if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mDisableBootAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.unicorn");
                Helpers.getMount("ro");
                Resources res = mContext.getResources();
                String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                int randomInt = randomGenerator.nextInt(insults.length);
                preference.setSummary(insults[randomInt]);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.unicorn /system/media/bootanimation.zip");
                Helpers.getMount("ro");
                preference.setSummary("");
            }
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
