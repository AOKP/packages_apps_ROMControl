
package com.aokp.romcontrol.fragments;

import java.io.File;
import java.util.Random;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class UserInterface extends AOKPPreferenceFragment {

    public static final String TAG = "UserInterface";

    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String PREF_MENU_PERMANENT_OVERRIDE = "menu_permanent_override";

    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mStatusBarNotifCount;
    CheckBoxPreference mMenuOverride;

    Random randomGenerator = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        mStatusBarNotifCount = (CheckBoxPreference)findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_NOTIF_COUNT, false));

        mMenuOverride = (CheckBoxPreference)findPreference(PREF_MENU_PERMANENT_OVERRIDE);
        mMenuOverride.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.MENU_PERMANENT_OVERRIDE, false));
        if (!hasHardwareButtons) {
            // these devices will always have the menu button
            getPreferenceScreen().removePreference(mMenuOverride);
        }

        mDisableBootAnimation = (CheckBoxPreference)findPreference("disable_bootanimation");
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
        if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT,
                    isCheckBoxPrefernceChecked(preference));
            return true;
        } else if (preference == mMenuOverride) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.MENU_PERMANENT_OVERRIDE,
                    isCheckBoxPrefernceChecked(preference));
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
