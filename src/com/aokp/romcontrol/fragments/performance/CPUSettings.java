package com.aokp.romcontrol.fragments.performance;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.aokp.romcontrol.R;

import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class CPUSettings extends Fragment implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = "CPUSettings";

    public static final String CURRENT_CPU = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String STEPS = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String GOVERNORS_LIST = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String GOVERNOR = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String IO_SCHEDULER = "/sys/block/mmcblk0/queue/scheduler";

    public static final String MAX_CPU = "max_cpu";
    public static final String MIN_CPU = "min_cpu";
    public static final String GOV_PREF = "gov";
    public static final String IO_PREF = "io";
    public static final String SOB = "cpu_boot";

    private SeekBar mMaxSlider;
    private SeekBar mMinSlider;
    private Spinner mGovernor;
    private Spinner mIo;
    private Switch mSetOnBoot;
    private TextView mCurFreq;
    private TextView mMaxSpeedText;
    private TextView mMinSpeedText;
    private String[] availableFrequencies;
    private Activity mActivity;

    private String mMaxFreqSetting;
    private String mMinFreqSetting;
    private String cpu1Max;
    private String cpu1Min;

    private CurCPUThread mCurCPUThread;
    private static SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root,
            Bundle savedInstanceState) {
        mActivity = getActivity();
        View view = inflater.inflate(R.layout.cpu_settings, root, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        availableFrequencies = new String[0];
        String availableFrequenciesLine = Helpers.readOneLine(STEPS);
        if (availableFrequencies != null) {
            availableFrequencies = availableFrequenciesLine.split(" ");
        }
        int frequenciesNum = availableFrequencies.length - 1;

        String currentGovernor = Helpers.readOneLine(GOVERNOR);
        String currentIo = Helpers.getIOScheduler();
        String curMaxSpeed = Helpers.readOneLine(MAX_FREQ);
        String curMinSpeed = Helpers.readOneLine(MIN_FREQ);

        mCurFreq = (TextView) view.findViewById(R.id.current_speed);

        mMaxSlider = (SeekBar) view.findViewById(R.id.max_slider);
        mMaxSlider.setMax(frequenciesNum);
        mMaxSpeedText = (TextView) view.findViewById(R.id.max_speed_text);
        mMaxSpeedText.setText(toMHz(curMaxSpeed));
        mMaxSlider.setProgress(Arrays.asList(availableFrequencies).indexOf(curMaxSpeed));
        mMaxSlider.setOnSeekBarChangeListener(this);

        mMinSlider = (SeekBar) view.findViewById(R.id.min_slider);
        mMinSlider.setMax(frequenciesNum);
        mMinSpeedText = (TextView) view.findViewById(R.id.min_speed_text);
        mMinSpeedText.setText(toMHz(curMinSpeed));
        mMinSlider.setProgress(Arrays.asList(availableFrequencies).indexOf(curMinSpeed));
        mMinSlider.setOnSeekBarChangeListener(this);

        mGovernor = (Spinner) view.findViewById(R.id.governor);
        String[] availableGovernors = Helpers.readOneLine(GOVERNORS_LIST).split(" ");
        ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence> (mActivity,
                android.R.layout.simple_spinner_item);
        governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < availableGovernors.length; i++) {
            governorAdapter.add(availableGovernors[i]);
        }
        mGovernor.setAdapter(governorAdapter);
        mGovernor.setSelection(Arrays.asList(availableGovernors).indexOf(currentGovernor));
        mGovernor.setOnItemSelectedListener(new GovListener());

        mIo = (Spinner) view.findViewById(R.id.io);
        String[] availableIo = Helpers.getAvailableIOSchedulers();
        ArrayAdapter<CharSequence> ioAdapter = new ArrayAdapter<CharSequence> (mActivity,
                android.R.layout.simple_spinner_item);
        ioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < availableIo.length; i++) {
            ioAdapter.add(availableIo[i]);
        }
        mIo.setAdapter(ioAdapter);
        mIo.setSelection(Arrays.asList(availableIo).indexOf(currentIo));
        mIo.setOnItemSelectedListener(new IOListener());

        mSetOnBoot = (Switch) view.findViewById(R.id.set_on_boot);
        mSetOnBoot.setChecked(preferences.getBoolean(SOB, false));
        mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(SOB, checked);
                editor.commit();
            }
        });

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
        if(fromUser) {
            switch(seekBar.getId()) {
                case R.id.max_slider:
                    setMaxSpeed(seekBar, progress);
                    break;
                case R.id.min_slider:
                    setMinSpeed(seekBar, progress);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // we have a break now, write the values..
        CMDProcessor cmd = new CMDProcessor();
        cmd.su.runWaitFor("busybox echo " + mMaxFreqSetting + " > " + MAX_FREQ);
        cmd.su.runWaitFor("busybox echo " + mMinFreqSetting + " > " + MIN_FREQ);
        String cpu1 = MAX_FREQ.replace("cpu0", "cpu1");
        cmd.su.runWaitFor("busybox echo " + cpu1Max + " > " + cpu1);
        cmd.su.runWaitFor("busybox echo " + cpu1Max + " > " + cpu1);
    }

    public class GovListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            CMDProcessor cmd = new CMDProcessor();
            cmd.su.runWaitFor("busybox echo " + selected + " > " + GOVERNOR);
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(GOV_PREF, selected);
            editor.commit();
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

    public class IOListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            CMDProcessor cmd = new CMDProcessor();
            cmd.su.runWaitFor("busybox echo " + selected + " > " + IO_SCHEDULER);
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(IO_PREF, selected);
            editor.commit();
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurCPUThread == null) {
            mCurCPUThread = new CurCPUThread();
            mCurCPUThread.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCurCPUThread.isAlive()) {
            mCurCPUThread.interrupt();
            try {
                mCurCPUThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public void setMaxSpeed(SeekBar seekBar, int progress) {
        String current = "";
        current = availableFrequencies[progress];
        CMDProcessor cmd = new CMDProcessor();
        int minSliderProgress = mMinSlider.getProgress();
        if (progress <= minSliderProgress) {
            mMinSlider.setProgress(progress);
            mMinSpeedText.setText(toMHz(current));
            mMinFreqSetting = current;
        }
        mMaxSpeedText.setText(toMHz(current));
        mMaxFreqSetting = current;
        if (new File("/sys/devices/system/cpu/cpu1").isDirectory()) {
            cpu1Max = current;
        }
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MAX_CPU, current);
        editor.commit();
    }

    public void setMinSpeed(SeekBar seekBar, int progress) {
        String current = "";
        current = availableFrequencies[progress];
        CMDProcessor cmd = new CMDProcessor();
        int maxSliderProgress = mMaxSlider.getProgress();
        if (progress >= maxSliderProgress) {
            mMaxSlider.setProgress(progress);
            mMaxSpeedText.setText(toMHz(current));
            mMaxFreqSetting = current;
        }
        mMinSpeedText.setText(toMHz(current));
        mMinFreqSetting = current;
        if (new File("/sys/devices/system/cpu/cpu1").isDirectory()) {
            cpu1Min = current;
        }
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(MIN_CPU, current);
        editor.commit();
    }

    private String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000).append(" MHz").toString();
    }

    protected class CurCPUThread extends Thread {
        private boolean mInterrupt = false;

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    sleep(500);
                    final String curFreq = Helpers.readOneLine(CURRENT_CPU);
                    mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, curFreq));
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    };

    protected Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
            mCurFreq.setText(toMHz((String) msg.obj));
        }
    };
}
