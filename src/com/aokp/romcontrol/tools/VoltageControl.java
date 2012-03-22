
package com.aokp.romcontrol.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VoltageControl extends Activity {

    private static final String TAG = "VoltageControlActivity";
    public static final String KEY_APPLY_BOOT = "apply_voltages_at_boot";
    public static final String MV_TABLE0 = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    public static final String MV_TABLE1 = "/sys/devices/system/cpu/cpu1/cpufreq/UV_mV_table";
    private static final int DIALOG_EDIT_VOLT = 0;
    private List<Voltage> mVoltages;
    private ListAdapter mAdapter;
    private static SharedPreferences preferences;
    private Voltage mVoltage;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.voltage);

        final ListView listView = (ListView) findViewById(R.id.ListView);
        final CheckBox cb = (CheckBox) findViewById(R.id.applyAtBoot);
        mAdapter = new ListAdapter(getApplicationContext());
        mVoltages = getVolts(preferences);

        if (mVoltages.isEmpty()) {
            ((TextView) findViewById(R.id.emptyList))
                    .setVisibility(View.VISIBLE);
            ((LinearLayout) findViewById(R.id.BottomBar))
                    .setVisibility(View.GONE);
        }

        cb.setChecked(preferences.getBoolean(KEY_APPLY_BOOT, false));
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_APPLY_BOOT, isChecked);
                editor.commit();
            }
        });

        ((Button) findViewById(R.id.applyBtn))
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        final StringBuilder sb = new StringBuilder();
                        for (final Voltage volt : mVoltages) {
                            sb.append(volt.getSavedMV() + " ");
                        }
                        new CMDProcessor().su.runWaitFor("busybox echo " + sb.toString() + " > "
                                + MV_TABLE0);
                        if (new File(MV_TABLE1).exists()) {
                            new CMDProcessor().su.runWaitFor("busybox echo " + sb.toString()
                                    + " > " + MV_TABLE1);
                        }
                        final List<Voltage> volts = getVolts(preferences);
                        mVoltages.clear();
                        mVoltages.addAll(volts);
                        mAdapter.notifyDataSetChanged();
                    }
                });

        mAdapter.setListItems(mVoltages);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                mVoltage = mVoltages.get(position);
                showDialog(DIALOG_EDIT_VOLT);
            }
        });
    }

    public static List<Voltage> getVolts(final SharedPreferences preferences) {
        final List<Voltage> volts = new ArrayList<Voltage>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(MV_TABLE0), 256);
            String line = "";
            while ((line = br.readLine()) != null) {
                final String[] values = line.split("\\s+");
                if (values != null) {
                    if (values.length >= 2) {
                        final String freq = values[0].replace("mhz:", "");
                        final String currentMv = values[1];
                        final String savedMv = preferences.getString(freq, currentMv);
                        final Voltage voltage = new Voltage();
                        voltage.setFreq(freq);
                        voltage.setCurrentMV(currentMv);
                        voltage.setSavedMV(savedMv);
                        volts.add(voltage);
                    }
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, MV_TABLE0 + " does not exist");
        } catch (IOException e) {
            Log.d(TAG, "Error reading " + MV_TABLE0);
        }
        return volts;
    }

    private static final int[] STEPS = new int[] {
            600, 625, 650, 675, 700, 725, 750, 775, 800, 825, 850,
            875, 900, 925, 950, 975, 1000, 1025, 1050, 1075, 1100,
            1125, 1150, 1175, 1200, 1225, 1250, 1275, 1300, 1325,
            1350, 1375, 1400, 1425, 1450, 1475, 1500, 1525, 1550,
            1575, 1600
    };

    private static int getNearestStepIndex(final int value) {
        int index = 0;
        for (int i = 0; i < STEPS.length; i++) {
            if (value > STEPS[i]) {
                index++;
            } else {
                break;
            }
        }
        return index;
    }

    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case DIALOG_EDIT_VOLT:
                final LayoutInflater factory = LayoutInflater.from(this);
                final View voltageDialog = factory.inflate(R.layout.voltage_dialog, null);

                final EditText voltageEdit = (EditText) voltageDialog
                        .findViewById(R.id.voltageEdit);
                final SeekBar voltageSeek = (SeekBar) voltageDialog.findViewById(R.id.voltageSeek);
                final TextView voltageMeter = (TextView) voltageDialog
                        .findViewById(R.id.voltageMeter);

                final String savedMv = mVoltage.getSavedMV();
                final int savedVolt = Integer.parseInt(savedMv);
                voltageEdit.setText(savedMv);
                voltageEdit.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable arg0) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1,
                            int arg2, int arg3) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onTextChanged(CharSequence arg0, int arg1,
                            int arg2, int arg3) {
                        final String text = voltageEdit.getText().toString();
                        int value = 0;
                        try {
                            value = Integer.parseInt(text);
                        } catch (NumberFormatException nfe) {
                            return;
                        }
                        voltageMeter.setText(text + " mV");
                        final int index = getNearestStepIndex(value);
                        voltageSeek.setProgress(index);
                    }

                });

                voltageMeter.setText(savedMv + " mV");
                voltageSeek.setMax(40);
                voltageSeek.setProgress(getNearestStepIndex(savedVolt));
                voltageSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar sb, int progress,
                            boolean fromUser) {
                        if (fromUser) {
                            final String volt = Integer.toString(STEPS[progress]);
                            voltageMeter.setText(volt + " mV");
                            voltageEdit.setText(volt);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                });

                return new AlertDialog.Builder(VoltageControl.this)
                        .setTitle(mVoltage.getFreq() + " MHz Voltage")
                        .setView(voltageDialog)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeDialog(id);
                                final String value = voltageEdit.getText().toString();
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(mVoltage.getFreq(), value);
                                editor.commit();
                                mVoltage.setSavedMV(value);
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface arg0) {
                                removeDialog(id);
                            }
                        })
                        .create();
            default:
                return null;
        }
    };

    public class ListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private List<Voltage> results;

        public ListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Object getItem(int position) {
            return results.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_volt, null);
                holder = new ViewHolder();
                holder.mFreq = (TextView) convertView.findViewById(R.id.Freq);
                holder.mCurrentMV = (TextView) convertView.findViewById(R.id.mVCurrent);
                holder.mSavedMV = (TextView) convertView.findViewById(R.id.mVSaved);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Voltage voltage = mVoltages.get(position);

            holder.setFreq(voltage.getFreq());
            holder.setCurrentMV(voltage.getCurrentMV());
            holder.setSavedMV(voltage.getSavedMV());

            return convertView;
        }

        public void setListItems(List<Voltage> mVoltages) {
            results = mVoltages;
        }

        public class ViewHolder {

            private TextView mFreq;
            private TextView mCurrentMV;
            private TextView mSavedMV;

            public void setFreq(final String freq) {
                mFreq.setText(freq + " MHz");
            }

            public void setCurrentMV(final String currentMv) {
                mCurrentMV.setText(currentMv + " mV current");
            }

            public void setSavedMV(final String savedMv) {
                mSavedMV.setText(savedMv + " mV saved");
            }

        }
    }

}
