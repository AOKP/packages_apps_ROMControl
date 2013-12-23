
package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.CommandResult;
import com.aokp.romcontrol.util.Helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class InstallerSettingsFragment extends Fragment implements OnClickListener {

    private static final String TAG = "Installer";

    private static final String CONF_FILE = "/system/etc/persist.conf";

    public static final String RO_SF_LCD_DENSITY = "ro.sf.lcd_density";
    public static final String ETC_HOSTS = "etc/hosts";
    public static final String BIN_APP_PROCESS = "bin/app_process";
    public static final String PERSIST_ENABLE = "persist_enable";
    public static final String PERSIST_PROPS = "persist_props";
    public static final String PERSIST_FILES = "persist_files";
    public static final String PREF_PERSIST_FILE_XPOSED = "persist_file_xposed";

    CheckboxSetting mPrefPersistEnable;
    CheckboxSetting mPrefPersistDensity;
    CheckboxSetting mPrefPersistHosts;
    CheckboxSetting mPrefPersistXposed;

    boolean mPersistEnable;
    ArrayList<String> mPersistProps;
    ArrayList<String> mPersistFiles;
    ArrayList<String> mPersistTrailer;

    boolean loadPrefs() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
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
                                if (fields[0].equals(PERSIST_ENABLE)) {
                                    mPersistEnable = stringToBool(fields[1]);
                                }
                                if (fields[0].equals(PERSIST_PROPS)) {
                                    mPersistProps = stringToStringArray(fields[1]);
                                }
                                if (fields[0].equals(PERSIST_FILES)) {
                                    mPersistFiles = stringToStringArray(fields[1]);
                                }
                            }
                        } else {
                            mPersistTrailer.add(line);
                        }
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Config file not found");
                } catch (IOException e) {
                    Log.e(TAG, "Exception reading config file: " + e.getMessage());
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            // Igonre
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mPrefPersistDensity != null) {
                    mPrefPersistDensity.setChecked(mPersistProps.contains(RO_SF_LCD_DENSITY));
                }
                if (mPrefPersistEnable != null) {
                    mPrefPersistEnable.setChecked(mPersistEnable);
                }
                if (mPrefPersistHosts != null) {
                    mPrefPersistHosts.setChecked(mPersistFiles.contains(ETC_HOSTS));
                }
                if (mPrefPersistXposed != null) {
                    mPrefPersistXposed.setChecked(mPersistFiles.contains(BIN_APP_PROCESS));
                }
            }
        }.execute((Void) null);

        return true;
    }

    boolean savePrefs() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_installer_settings, container, false);

        mPrefPersistEnable = (CheckboxSetting) v.findViewById(R.id.enable_persist);
        mPrefPersistEnable.setOnClickListener(this);
        mPrefPersistDensity = (CheckboxSetting) v.findViewById(R.id.persist_prop_density);
        mPrefPersistDensity.setOnClickListener(this);
        mPrefPersistHosts = (CheckboxSetting) v.findViewById(R.id.persist_file_hosts);
        mPrefPersistHosts.setOnClickListener(this);
        mPrefPersistXposed = (CheckboxSetting) v.findViewById(R.id.persist_file_xposed);
        mPrefPersistXposed.setOnClickListener(this);

        if(!isAppInstalled("de.robv.android.xposed.installer")) {
            mPrefPersistXposed.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadPrefs();
    }

    @Override
    public void onClick(View view) {
        boolean isChecked = false;
        if (view instanceof CheckboxSetting) {
            isChecked = ((CheckboxSetting) view).isChecked();
        }

        switch (view.getId()) {
            case R.id.enable_persist:
                mPersistEnable = isChecked;

                break;


            case R.id.persist_prop_density:
                if (isChecked) {
                    if (!mPersistProps.contains(RO_SF_LCD_DENSITY)) {
                        mPersistProps.add(RO_SF_LCD_DENSITY);
                    }
                } else {
                    mPersistProps.remove(RO_SF_LCD_DENSITY);
                }

                break;


            case R.id.persist_file_hosts:
                if (isChecked) {
                    if (!mPersistFiles.contains(ETC_HOSTS)) {
                        mPersistFiles.add(ETC_HOSTS);
                    }
                } else {
                    mPersistFiles.remove(ETC_HOSTS);
                }
                break;


            case R.id.persist_file_xposed:
                if (isChecked) {
                    if (!mPersistFiles.contains(BIN_APP_PROCESS)) {
                        mPersistFiles.add(BIN_APP_PROCESS);
                    }
                } else {
                    mPersistFiles.remove(BIN_APP_PROCESS);
                }
                break;

        }
        savePrefs();
        loadPrefs(); // refresh
    }

    private static boolean stringToBool(String val) {
        if (val.equals("0") ||
                val.equals("false") ||
                val.equals("False")) {
            return false;
        }
        return true;
    }

    private static String boolToString(boolean val) {
        return (val ? "true" : "false");
    }

    private static ArrayList<String> stringToStringArray(String val) {
        ArrayList<String> ret = new ArrayList<String>();
        int p1 = val.indexOf("\"");
        int p2 = val.lastIndexOf("\"");
        if (p1 >= 0 && p2 > p1 + 1) {
            String dqval = val.substring(p1 + 1, p2);
            for (String s : dqval.split(" +")) {
                ret.add(s);
            }
        }
        return ret;
    }

    private static String stringArrayToString(ArrayList<String> val) {
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

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getActivity().getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
