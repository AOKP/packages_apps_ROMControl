package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Switch;

import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.text.DecimalFormat;

public class LEDControl extends Fragment implements ColorPickerDialog.OnColorChangedListener {

    private static final String TAG = "LEDControl";

    private Button mOnTime;
    private Button mOffTime;
    private Switch mLedScreenOn;
    private ImageView mLEDButton;

    private ViewGroup mContainer;
    private Activity mActivity;
    private Resources mResources;

    private int defaultColor;
    private int userColor;
    private String[] timeArray;
    private int[] timeOutput;
    private boolean blinkOn;
    private boolean stopLed;
    private int onBlink;
    private int offBlink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;
        mActivity = getActivity();
        mResources = getResources();
        return inflater.inflate(R.layout.led_control, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mOnTime = (Button) mActivity.findViewById(R.id.ontime);
        mOffTime = (Button) mActivity.findViewById(R.id.offtime);
        mLEDButton = (ImageView) mActivity.findViewById(R.id.ledbutton);
        mLedScreenOn = (Switch) mActivity.findViewById(R.id.led_screen_on);
        timeArray = mResources.getStringArray(R.array.led_entries);
        timeOutput = mResources.getIntArray(R.array.led_values);

        defaultColor = mResources.getColor(com.android.internal.R.color.config_defaultNotificationColor);

        mLEDButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ColorPickerDialog picker = new ColorPickerDialog(mActivity, userColor);
                picker.setOnColorChangedListener(LEDControl.this);
                picker.show();
            }
        });

        mLEDButton.setImageResource(R.drawable.led_circle_button);

        mOnTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
                b.setTitle(R.string.led_time_on);
                b.setItems(timeArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Settings.System.putInt(mActivity.getContentResolver(), Settings.System.NOTIFICATION_LIGHT_ON, timeOutput[item]);
                        refreshSettings();
                    }
                });
                AlertDialog alert = b.create();
                alert.show();
            }
        });

        mOffTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
                b.setTitle(R.string.led_time_off);
                b.setItems(timeArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Settings.System.putInt(mActivity.getContentResolver(), Settings.System.NOTIFICATION_LIGHT_OFF, timeOutput[item]);
                        refreshSettings();
                    }
                });
                AlertDialog alert = b.create();
                alert.show();
            }
        });

        mLedScreenOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean checked = mLedScreenOn.isChecked();
                Settings.Secure.putInt(mActivity.getContentResolver(),
                    Settings.Secure.LED_SCREEN_ON, checked ? 1 : 0);
                Log.i(TAG, "LED flash when screen ON is set to: " + checked);
            }
        });

        refreshSettings();
        startLed();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 0:
                if (blinkOn) {
                    mLEDButton.setColorFilter(userColor);
                } else {
                    mLEDButton.setColorFilter(Color.BLACK);
                }
                break;
            }
        }
    };

    public void startLed() {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                while (!stopLed) {
                    mHandler.sendEmptyMessage(0);
                    int delay = blinkOn ? onBlink : offBlink;
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {

                    }
                    blinkOn = !blinkOn;
                }

            }
        }.start();
    }

    public void onDestroy() {
        super.onDestroy();
        stopLed = true;
    }

    @Override
    public void onColorChanged(int color) {
        Settings.System.putInt(mActivity.getContentResolver(), Settings.System.NOTIFICATION_LIGHT_COLOR, color);
        refreshSettings();
    }

    private String getTimeString(int milliSeconds){
        float seconds = (float) milliSeconds / 1000;
        DecimalFormat df = new DecimalFormat("0.#");
        String time = df.format(seconds) + " seconds";

        return time;
    }
 
    private void refreshSettings() {
        int on = mResources.getInteger(com.android.internal.R.integer.config_defaultNotificationLedOn);
        int off = mResources.getInteger(com.android.internal.R.integer.config_defaultNotificationLedOff);
        onBlink = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_ON, on);
        offBlink = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_OFF, off);
        userColor = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_COLOR, defaultColor);

        mOnTime.setText(getTimeString(onBlink));
        mOffTime.setText(getTimeString(offBlink));
        mLEDButton.setColorFilter(userColor, PorterDuff.Mode.MULTIPLY);
        mLedScreenOn.setChecked(Settings.Secure.getInt(mActivity.getContentResolver(),
            Settings.Secure.LED_SCREEN_ON, 0) == 1);

    }
}
