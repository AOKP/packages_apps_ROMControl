
package com.aokp.romcontrol.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.ROMControlActivity;
import com.aokp.romcontrol.widgets.NavBarItemPreference;
import com.aokp.romcontrol.widgets.SeekBarPreference;
import com.aokp.romcontrol.fragments.NavRingTargets;

public class Navbar extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PREF_NAVRING_AMOUNT = "pref_navring_amount";
    private static final String ENABLE_NAVRING_LONG = "enable_navring_long";

    public static final String PREFS_NAV_BAR = "navbar";

    Preference mNavRingTargets;

    CheckBoxPreference mEnableNavringLong;
    ListPreference mNavRingButtonQty;

    Preference mPendingPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_navbar);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_navbar);

        PreferenceScreen prefs = getPreferenceScreen();


        mNavRingTargets = findPreference("navring_settings");

        mNavRingButtonQty = (ListPreference) findPreference(PREF_NAVRING_AMOUNT);
        mNavRingButtonQty.setOnPreferenceChangeListener(this);
        mNavRingButtonQty.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1) + "");


        mEnableNavringLong = (CheckBoxPreference) findPreference("enable_navring_long");
        mEnableNavringLong.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE, false));




    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableNavringLong) {

            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            resetNavRingLong();
            return true;
        } else if (preference == mNavRingTargets) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NavRingTargets fragment = new NavRingTargets();
            ft.addToBackStack("config_nav_ring");
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mNavRingButtonQty) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_AMOUNT, val);
            resetNavRing();
            resetNavRingLong();
            return true;
        }
        return false;
    }

    public void resetNavRing() {
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING[0], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING[1], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING[2], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING[3], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING[4], "**null**");
    }

    public void resetNavRingLong() {
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG[0], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG[1], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG[2], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG[3], "**null**");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG[4], "**null**");
    }

}
