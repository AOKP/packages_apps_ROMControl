
package com.aokp.romcontrol.tools;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.R;

import java.io.File;

public class Performance extends Activity {

    public static final String TAG = "Performance";
    public static final String KEY_MAX_CPU = "max_cpu";
    public static final String KEY_MIN_CPU = "min_cpu";
    public static final String KEY_GOV = "gov";
    public static final String KEY_CPU_BOOT = "cpu_boot";
    public static final String KEY_MINFREE = "free_memory";

    private static final String STEPS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    private static final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    private static final String GETALL_GOV = "sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    private static final String CUR_GOV = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String MINFREE = "/sys/module/lowmemorykiller/parameters/minfree";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PerformancePreferences()).commit();
    }

    public class PerformancePreferences extends PreferenceFragment implements
            OnSharedPreferenceChangeListener {

        private String[] ALL_GOV;
        private int[] SPEED_STEPS;
        private ListPreference mMinCpu;
        private ListPreference mMaxCpu;
        private ListPreference mSetGov;
        private ListPreference mFreeMem;
        private SharedPreferences preferences;
        private boolean doneLoading = false;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            super.onCreate(savedInstanceState);
            preferences.registerOnSharedPreferenceChangeListener(this);
            addPreferencesFromResource(R.xml.performance);

            final int frequencies[] = getFrequencies();
            final String freqList[] = getMHz(frequencies);
            final String freqValues[] = getValues(frequencies);
            final String maxFreq = (Helpers.getFile(MAX_FREQ).trim());
            final String minFreq = (Helpers.getFile(MIN_FREQ).trim());
            final String maxInMhz = (Integer.toString((Integer.parseInt(maxFreq) / 1000)) + " MHz");
            final String minInMhz = (Integer.toString((Integer.parseInt(minFreq) / 1000)) + " MHz");
            final String govs[] = getAllGovs();
            final String currentGov = (Helpers.getFile(CUR_GOV).trim());

            mMaxCpu = (ListPreference) findPreference(KEY_MAX_CPU);
            mMaxCpu.setEntries(freqList);
            mMaxCpu.setEntryValues(freqValues);
            mMaxCpu.setValue(maxFreq);
            mMaxCpu.setSummary(getString(R.string.ps_set_max, maxInMhz));

            mMinCpu = (ListPreference) findPreference(KEY_MIN_CPU);
            mMinCpu.setEntries(freqList);
            mMinCpu.setEntryValues(freqValues);
            mMinCpu.setValue(minFreq);
            mMinCpu.setSummary(getString(R.string.ps_set_min, minInMhz));

            mSetGov = (ListPreference) findPreference(KEY_GOV);
            mSetGov.setEntries(govs);
            mSetGov.setEntryValues(govs);
            mSetGov.setValue(currentGov);
            mSetGov.setSummary(getString(R.string.ps_set_gov, currentGov));

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

            PreferenceScreen ps = (PreferenceScreen) findPreference("volt_control");
            if (!new File(VoltageControl.MV_TABLE0).exists()) {
                ((PreferenceCategory) getPreferenceScreen().findPreference("cpu"))
                        .removePreference(ps);
            }

            doneLoading = true;
        }

        @Override
        public void onSharedPreferenceChanged(
                final SharedPreferences sharedPreferences, String key) {
            if (doneLoading) {
                if (key.equals(KEY_MAX_CPU)) {
                    final String value = preferences.getString(key, null);
                    final String maxInMhz = (Integer.toString((Integer.parseInt(value) / 1000)) + " MHz");
                    if (!sendCpu(key, value, MAX_FREQ))
                        Helpers.sendMsg(getApplicationContext(),
                                getString(R.string.toast_min_max_error01));
                    else
                        mMaxCpu.setSummary(getString(R.string.ps_set_max, maxInMhz));
                } else if (key.equals(KEY_MIN_CPU)) {
                    final String value = preferences.getString(key, null);
                    final String minInMhz = (Integer.toString((Integer.parseInt(value) / 1000)) + " MHz");
                    if (!sendCpu(key, value, MIN_FREQ))
                        Helpers.sendMsg(getApplicationContext(),
                                getString(R.string.toast_min_max_error02));
                    else
                        mMinCpu.setSummary(getString(R.string.ps_set_min, minInMhz));
                } else if (key.equals(KEY_GOV)) {
                    final String value = preferences.getString(key, null);
                    if ((new CMDProcessor().su
                            .runWaitFor("busybox echo " + value + " > " + CUR_GOV)).success())
                        mSetGov.setSummary(getString(R.string.ps_set_gov, value));
                } else if (key.equals(KEY_MINFREE)) {
                    String values = preferences.getString(key, null);
                    if (!values.equals(null))
                        new CMDProcessor().su
                                .runWaitFor("busybox echo " + values + " > " + MINFREE);
                    mFreeMem.setSummary(getString(R.string.ps_free_memory, getMinFreeValue() + "mb"));
                }
            }

        }

        String[] getMHz(int freqs[]) {
            int freqInMHz[] = new int[freqs.length];
            for (int i = 0; i < freqs.length; i++)
                freqInMHz[i] = freqs[i] / 1000;
            String steps[] = new String[freqs.length];
            for (int i = 0; i < freqs.length; i++)
                steps[i] = Integer.toString(freqInMHz[i]) + " MHz";
            return steps;
        }

        String[] getValues(int freqs[]) {
            final String steps[] = new String[freqs.length];
            for (int i = 0; i < freqs.length; i++)
                steps[i] = Integer.toString(freqs[i]);
            return steps;
        }

        int[] getFrequencies() {
            String freqs = Helpers.getFile(STEPS);
            if (freqs != null && freqs != "") {
                String[] freqList = freqs.trim().split("[ \n]+");
                SPEED_STEPS = new int[freqList.length];
                for (int i = 0; i < freqList.length; i++) {
                    SPEED_STEPS[i] = (Integer.parseInt(freqList[i]));
                }
            } else {
                CMDProcessor cmd = new CMDProcessor();
                CMDProcessor.CommandResult r = cmd.su
                        .runWaitFor("busybox cut -d ' ' -f1 /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
                freqs = r.stdout;
                if (freqs != null && freqs != "") {
                    String[] freqList = freqs.trim().split("[ \n]+");
                    SPEED_STEPS = new int[freqList.length];
                    for (int i = 0; i < freqList.length; i++) {
                        SPEED_STEPS[i] = (Integer.parseInt(freqList[i]));
                    }
                } else {
                    SPEED_STEPS = new int[] {
                            1000000, 800000, 600000, 300000
                    };
                    Log.d(TAG, "Failed getting steps");
                }
            }
            return SPEED_STEPS;
        }

        public String[] getAllGovs() {
            String govs = Helpers.getFile(GETALL_GOV);
            if (govs != null && govs != "") {
                String[] govList = govs.trim().split(" ");
                ALL_GOV = new String[govList.length];
                for (int i = 0; i < govList.length; i++) {
                    ALL_GOV[i] = govList[i];
                }
            } else {
                ALL_GOV = new String[] {
                        "ondemand", "userspace", "performance"
                };
            }
            return ALL_GOV;
        }

        private int getMinFreeValue() {
            int emptyApp = 0;
            String MINFREE_LINE = Helpers.getFile(MINFREE);
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

        private boolean sendCpu(final String key, final String value, final String fname) {
            final int maxCpu = Integer.parseInt((Helpers.getFile(MAX_FREQ).trim()));
            final int minCpu = Integer.parseInt((Helpers.getFile(MIN_FREQ).trim()));
            final int newCpu = Integer.parseInt(value);
            final CMDProcessor cmd = new CMDProcessor();

            Boolean isOk = true;
            String goodCpu = value;

            if (key.equals(KEY_MAX_CPU)) {
                if (newCpu < minCpu) {
                    isOk = false;
                    goodCpu = Integer.toString(maxCpu);
                }
            } else if (key.equals(KEY_MIN_CPU)) {
                if (newCpu > maxCpu) {
                    isOk = false;
                    goodCpu = Integer.toString(minCpu);
                }
            }

            if (isOk) {
                cmd.su.runWaitFor("busybox echo " + value + " > " + fname);
                if (new File("/sys/devices/system/cpu/cpu1").isDirectory()) {
                    String cpu1 = fname.replace("cpu0", "cpu1");
                    cmd.su.runWaitFor("busybox echo " + value + " > " + cpu1);
                }
            } else {
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putString(key, goodCpu);
                editor.commit();

                if (key.equals(KEY_MAX_CPU))
                    mMaxCpu.setValue(goodCpu);
                else if (key.equals(KEY_MIN_CPU))
                    mMinCpu.setValue(goodCpu);
            }

            return isOk;
        }

    }

}
