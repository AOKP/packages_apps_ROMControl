
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
import android.os.SystemProperties;
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
    private static final String PREF_KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String PREF_FORCE_TABLET_UI = "force_tablet_ui";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
    private static final String PREF_CLOCK_DATE_OPENS = "clock_date_opens";
    private static final String PREF_FORCE_DUAL_PANEL = "force_dualpanel";

    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mRecentKillAll;
    CheckBoxPreference mKillAppLongpressBack;
    CheckBoxPreference mForceTabletUI;
    CheckBoxPreference mUseAltResolver;
    CheckBoxPreference mVibrateOnExpand;
    CheckBoxPreference mClockDateOpens;
    CheckBoxPreference mDualpane;
    Preference mLcdDensity;

    Random randomGenerator = new Random();

    int newDensityValue;
    DensityChanger densityFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_system_extra);

        PreferenceScreen prefs = getPreferenceScreen();

        mLcdDensity = findPreference("lcd_density_setup");
        String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }
        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + currentProperty);

        mKillAppLongpressBack = (CheckBoxPreference) findPreference(
                PREF_KILL_APP_LONGPRESS_BACK);
                updateKillAppLongpressBackOptions();

        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        if (hasNavBarByDefault || mTablet) {
            ((PreferenceGroup) findPreference("misc")).removePreference(mKillAppLongpressBack);
        }

        mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.RECENT_KILL_ALL_BUTTON, 0) == 1);

        mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
        if (mDisableBootAnimation.isChecked()) {
            Resources res = mContext.getResources();
            String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
            int randomInt = randomGenerator.nextInt(insults.length);
            mDisableBootAnimation.setSummary(insults[randomInt]);
        }

        mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.VIBRATE_NOTIF_EXPAND, true));

        mClockDateOpens = (CheckBoxPreference) findPreference(PREF_CLOCK_DATE_OPENS);
        mClockDateOpens.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.CLOCK_DATE_OPENS, true));

        mForceTabletUI = (CheckBoxPreference) findPreference(PREF_FORCE_TABLET_UI);
        mForceTabletUI.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.FORCE_TABLET_UI, 0) == 1);

        mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, false));

        mDualpane = (CheckBoxPreference) findPreference(PREF_FORCE_DUAL_PANEL);
        mDualpane.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.FORCE_DUAL_PANEL, getResources().getBoolean(
                com.android.internal.R.bool.preferences_prefer_dual_pane)));

        if (mTablet) {
            // if it's a tablet not reason to show the force of a tablet ui
            prefs.removePreference(mForceTabletUI);
        }
    }

    private void writeKillAppLongpressBackOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.KILL_APP_LONGPRESS_BACK, mKillAppLongpressBack.isChecked() ? 1 : 0);
    }

    private void updateKillAppLongpressBackOptions() {
        mKillAppLongpressBack.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.KILL_APP_LONGPRESS_BACK, 0) != 0);
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

        } else if (preference == mKillAppLongpressBack) {
            writeKillAppLongpressBackOptions();

        } else if (preference == mRecentKillAll) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENT_KILL_ALL_BUTTON, checked ? 1 : 0);
            Helpers.restartSystemUI();
            return true;

        } else if (preference == mForceTabletUI) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.FORCE_TABLET_UI, checked ? 1 : 0);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC, 3);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_BACKGROUND, 0 / 100);
            return true;

        } else if (preference == mDualpane) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                Settings.System.FORCE_DUAL_PANEL,
                ((CheckBoxPreference) preference).isChecked());
            return true;

        }  else if (preference == mLcdDensity) {
            ((PreferenceActivity) getActivity())
                    .startPreferenceFragment(new DensityChanger(), true);
            return true;

        } else if (preference == mUseAltResolver) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.ACTIVITY_RESOLVER_USE_ALT,
                    checkBoxChecked(preference));
            return true;

        } else if (preference == mVibrateOnExpand) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.VIBRATE_NOTIF_EXPAND,
                    ((CheckBoxPreference) preference).isChecked());
            Helpers.restartSystemUI();
            return true;

        } else if (preference == mClockDateOpens) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.CLOCK_DATE_OPENS,
                    ((CheckBoxPreference) preference).isChecked());
            Helpers.restartSystemUI();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
