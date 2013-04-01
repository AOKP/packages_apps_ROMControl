
package com.aokp.romcontrol.fragments;

import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.CommandResult;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import java.net.URISyntaxException;


import static com.android.internal.util.aokp.AwesomeConstants.*;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.aokp.romcontrol.util.Helpers;

public class Installer extends AOKPPreferenceFragment {

    private static final String TAG = "Installer";

    private static final String CONF_FILE = "/system/etc/persist.conf";

    private static final String PREF_PERSIST_ENABLE = "enable_persist";
    private static final String PREF_PERSIST_PROP_DENSITY = "persist_prop_density";
    private static final String PREF_PERSIST_FILE_HOSTS = "persist_file_hosts";

    private Preference mPreference;

    CheckBoxPreference mPrefPersistEnable;
    CheckBoxPreference mPrefPersistDensity;
    CheckBoxPreference mPrefPersistHosts;

    boolean mPersistEnable;
    ArrayList<String> mPersistProps;
    ArrayList<String> mPersistFiles;
    ArrayList<String> mPersistTrailer;

    private boolean stringToBool(String val) {
        if (val.equals("0") ||
            val.equals("false") ||
            val.equals("False")) {
            return false;
        }
        return true;
    }
    private String boolToString(boolean val) {
        return (val ? "true" : "false");
    }
    private ArrayList<String> stringToStringArray(String val) {
        ArrayList<String> ret = new ArrayList<String>();
        int p1 = val.indexOf("\"");
        int p2 = val.lastIndexOf("\"");
        if (p1 >= 0 && p2 > p1+1) {
            String dqval = val.substring(p1+1, p2);
            for (String s : dqval.split(" +")) {
                ret.add(s);
            }
        }
        return ret;
    }
    private String stringArrayToString(ArrayList<String> val) {
        String ret = "";
        boolean first = true;
        ret += "\"";
        for (String s : val) {
            if (!first) {
                ret += " ";
            }
            ret += s;
            first = false;
        }
        ret += "\"";
        return ret;
    }

    boolean loadPrefs() {
        mPersistEnable = true;
        mPersistProps = new ArrayList<String>();
        mPersistFiles = new ArrayList<String>();
        mPersistTrailer = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(CONF_FILE), 1024);
            boolean inTrailer = false;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("# END REPLACE")) {
                    inTrailer = true;
                }
                if (!inTrailer) {
                    if (line.startsWith("persist_")) {
                        String[] fields = line.split("=", 2);
                        if (fields[0].equals("persist_enable")) {
                            mPersistEnable = stringToBool(fields[1]);
                        }
                        if (fields[0].equals("persist_props")) {
                            mPersistProps = stringToStringArray(fields[1]);
                        }
                        if (fields[0].equals("persist_files")) {
                            mPersistFiles = stringToStringArray(fields[1]);
                        }
                    }
                }
                else {
                    mPersistTrailer.add(line);
                }
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "Config file not found");
        }
        catch (IOException e) {
            Log.e(TAG, "Exception reading config file: " + e.getMessage());
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                    // Igonre
                }
            }
        }
        return true;
    }

    boolean savePrefs() {
        BufferedWriter bw = null;
        Helpers.getMount("rw");
        String[] cmdarray = new String[3];
        cmdarray[0] = "su";
        cmdarray[1] = "-c";
        cmdarray[2] = "cat > " + CONF_FILE;
        StringBuffer childStdin = new StringBuffer();
        childStdin.append("# /system/etc/persist.conf\n");
        childStdin.append("persist_enable=" + boolToString(mPersistEnable) + "\n");
        childStdin.append("persist_props=" + stringArrayToString(mPersistProps) + "\n");
        childStdin.append("persist_files=" + stringArrayToString(mPersistFiles) + "\n");
        for (String line : mPersistTrailer) {
            childStdin.append(line + "\n");
        }
        CommandResult cr = CMDProcessor.runSysCmd(cmdarray, childStdin.toString());
        Log.i(TAG, "savePrefs: result=" + cr.getExitValue());
        Log.i(TAG, "savePrefs: stdout=" + cr.getStdout());
        Log.i(TAG, "savePrefs: stderr=" + cr.getStderr());
        Helpers.getMount("ro");
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_installer);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_installer);

        PreferenceScreen prefs = getPreferenceScreen();

        loadPrefs();

        mPrefPersistEnable = (CheckBoxPreference)findPreference(PREF_PERSIST_ENABLE);
        mPrefPersistEnable.setChecked(mPersistEnable);
        mPrefPersistDensity = (CheckBoxPreference)findPreference(PREF_PERSIST_PROP_DENSITY);
        mPrefPersistDensity.setChecked(mPersistProps.contains("ro.sf.lcd_density"));
        mPrefPersistHosts = (CheckBoxPreference)findPreference(PREF_PERSIST_FILE_HOSTS);
        mPrefPersistHosts.setChecked(mPersistFiles.contains("etc/hosts"));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        boolean isChecked = ((CheckBoxPreference)preference).isChecked();
        if (preference == mPrefPersistEnable) {
            mPersistEnable = isChecked;
            savePrefs();
            return true;
        }
        if (preference == mPrefPersistDensity) {
            if (isChecked) {
                if (!mPersistProps.contains("ro.sf.lcd_density")) {
                    mPersistProps.add("ro.sf.lcd_density");
                }
            }
            else {
                mPersistProps.remove("ro.sf.lcd_density");
            }
            savePrefs();
            return true;
        }
        if (preference == mPrefPersistHosts) {
            if (isChecked) {
                if (!mPersistFiles.contains("etc/hosts")) {
                    mPersistFiles.add("etc/hosts");
                }
            }
            else {
                mPersistFiles.remove("etc/hosts");
            }
            savePrefs();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
