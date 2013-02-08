
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Spinner;
import android.widget.Switch;

import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.util.ShortcutPickerHelper;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LEDControl extends Fragment implements ColorPickerDialog.OnColorChangedListener,
        ShortcutPickerHelper.OnPickListener {

    private static final String TAG = "LEDControl";
    private static final boolean DEBUG = false;

    private static final String PROP_CHARGING_LED = "persist.sys.enable-charging-led";
    private static final String PROP_LED_BRIGHTNESS = "persist.sys.led-brightness";

    private Button mOnTime;
    private Button mOffTime;
    private Button mEditApp;
    private Button mLedTest;
    private Switch mLedScreenOn;
    private Switch mChargingLedOn;
    private ImageView mLEDButton;
    private Spinner mListApps;
    private Button mLedBrightness;
    private NumberPicker mBrightnessNumberpicker;
    private ArrayAdapter<CharSequence> listAdapter;

    private ViewGroup mContainer;
    private Activity mActivity;
    private Resources mResources;
    private ShortcutPickerHelper mPicker;

    private int defaultColor;
    private int userColor;
    private String[] timeArray;
    private int[] timeOutput;
    private String[] brightnessArray;
    private int[] brightnessOutput;
    private boolean blinkOn;
    private boolean stopLed;
    private boolean mRegister = false;
    private int onBlink;
    private int offBlink;
    private int currentSelectedApp;
    private boolean hasBrightnessFeature;
    private boolean hasChargingFeature;

    private HashMap<String, CustomApps> customAppList;
    private ArrayList<String> unicornApps;
    private ArrayList<String> appList;

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
        getActivity().setTitle(R.string.title_led);
        mOnTime = (Button) mActivity.findViewById(R.id.ontime);
        mOffTime = (Button) mActivity.findViewById(R.id.offtime);
        mEditApp = (Button) mActivity.findViewById(R.id.edit_button);
        mLedTest = (Button) mActivity.findViewById(R.id.led_test);
        mLEDButton = (ImageView) mActivity.findViewById(R.id.ledbutton);
        mLedScreenOn = (Switch) mActivity.findViewById(R.id.led_screen_on);
        mChargingLedOn = (Switch) mActivity.findViewById(R.id.charging_led_on);
        mListApps = (Spinner) mActivity.findViewById(R.id.custom_apps);
        mLedBrightness = (Button) mActivity.findViewById(R.id.button_led_brightness);
        timeArray = mResources.getStringArray(R.array.led_entries);
        timeOutput = mResources.getIntArray(R.array.led_values);
        brightnessArray = mResources.getStringArray(R.array.led_brightness_entries);
        brightnessOutput = mResources.getIntArray(R.array.led_brightness_values);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        defaultColor = mResources
                .getColor(com.android.internal.R.color.config_defaultNotificationColor);
        userColor = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_COLOR, defaultColor);

        mPicker = new ShortcutPickerHelper(this, this);

        customAppList = new HashMap<String, CustomApps>();
        unicornApps = new ArrayList<String>();
        appList = new ArrayList<String>();
        currentSelectedApp = 0;

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
                        Settings.System.putInt(mActivity.getContentResolver(),
                                Settings.System.NOTIFICATION_LIGHT_ON, timeOutput[item]);
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
                        Settings.System.putInt(mActivity.getContentResolver(),
                                Settings.System.NOTIFICATION_LIGHT_OFF, timeOutput[item]);
                        refreshSettings();
                    }
                });
                AlertDialog alert = b.create();
                alert.show();
            }
        });

        mLedScreenOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.Secure.putInt(mActivity.getContentResolver(),
                        Settings.Secure.LED_SCREEN_ON, checked ? 1 : 0);
                if (DEBUG)
                    Log.i(TAG, "LED flash when screen ON is set to: " + checked);
            }
        });

        hasChargingFeature = getResources().getBoolean(R.bool.has_led_charging_feature);

        if (hasChargingFeature) {
            mChargingLedOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    Helpers.setSystemProp(PROP_CHARGING_LED, checked ? "1" : "0");
                    if (DEBUG)
                        Log.i(TAG, "Charging LED is set to: " + checked);
                }
            });
        }
        else {
            mChargingLedOn.setVisibility(View.GONE);
        }

        parseExistingAppList();

        listAdapter = new ArrayAdapter<CharSequence>(mActivity,
                android.R.layout.simple_spinner_item);
        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterRefreshSetting();
        mListApps.setAdapter(listAdapter);

        mListApps.setSelection(0);
        mListApps.setLongClickable(false);
        mListApps.setOnItemSelectedListener(new customAppSpinnerSelected());

        mEditApp.setOnClickListener(new buttonEditApp());
        editButtonVisibility();

        mLedTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRegister) {
                    mActivity.unregisterReceiver(testLedReceiver);
                    mRegister = false;
                }

                if (!mRegister) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_SCREEN_OFF);
                    filter.addAction(Intent.ACTION_SCREEN_ON);

                    mActivity.registerReceiver(testLedReceiver, filter);
                    mRegister = true;
                }

                final int place = currentSelectedApp;
                AlertDialog.Builder ad = new AlertDialog.Builder(mActivity);
                ad.setTitle(R.string.led_test_notification);
                ad.setIcon(R.mipmap.ic_launcher);
                String appName = unicornApps.get(place);
                ad.setMessage(getResources().getString(R.string.led_test_notification_message_now) + appName
                        + getResources().getString(R.string.led_test_notification_message_note));
                ad.setPositiveButton(R.string.led_test_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mRegister) {
                                    mActivity.unregisterReceiver(testLedReceiver);
                                    mRegister = false;
                                }
                                dialog.dismiss();
                            }
                        });
                ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mRegister) {
                            mActivity.unregisterReceiver(testLedReceiver);
                            mRegister = false;
                        }
                    }
                });
                ad.show();
            }
        });

        hasBrightnessFeature = getResources().getBoolean(R.bool.has_led_brightness_feature);

        if (hasBrightnessFeature) {
            mLedBrightness.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder b = new AlertDialog.Builder(mActivity);
                    b.setTitle(R.string.led_change_brightness);
                    b.setSingleChoiceItems(brightnessArray, Settings.System.getInt(mActivity.getContentResolver(), Settings.System.LED_BRIGHTNESS, 1), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            Helpers.setSystemProp(PROP_LED_BRIGHTNESS, String.valueOf(brightnessOutput[item]));
                            Settings.System.putInt(mActivity.getContentResolver(),
                                    Settings.System.LED_BRIGHTNESS, item);
                        }
                    });
                    b.setPositiveButton(com.android.internal.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                        }
                    });

                    AlertDialog alert = b.create();
                    alert.show();
                }
            });
        }
        else {
            mLedBrightness.setVisibility(View.GONE);
        }

        refreshSettings();
        startLed();
    }

    private void adapterRefreshSetting() {
        if (listAdapter != null) {
            if (listAdapter.getCount() != 0) {
                listAdapter.clear();
            }
            listAdapter.addAll(unicornApps);
        }
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
        if (DEBUG)
            Log.d(TAG, "currentSelectedApp = " + currentSelectedApp);
        if (currentSelectedApp == 0) {
            Settings.System.putInt(mActivity.getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_COLOR, color);
            userColor = Settings.System.getInt(mActivity.getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_COLOR, defaultColor);
        } else if (currentSelectedApp == unicornApps.size()) {
            // do nothing on this one
            // it really should never even be the currentSelectedApp but just in
            // case!
        } else {
            userColor = color;
            int realAppInt = currentSelectedApp - 1;
            String hashKey = appList.get(realAppInt);
            appList.remove(realAppInt);
            unicornApps.remove(currentSelectedApp);
            customAppList.remove(hashKey);
            addCustomApp(hashKey);
            saveCustomApps();
        }
        refreshSettings();
    }

    private String getTimeString(int milliSeconds) {
        float seconds = (float) milliSeconds / 1000;
        DecimalFormat df = new DecimalFormat("0.#");
        String time = df.format(seconds) + getResources().getString(R.string.led_time_seconds);

        return time;
    }

    private void refreshSettings() {
        int on = mResources
                .getInteger(com.android.internal.R.integer.config_defaultNotificationLedOn);
        int off = mResources
                .getInteger(com.android.internal.R.integer.config_defaultNotificationLedOff);
        onBlink = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_ON, on);
        offBlink = Settings.System.getInt(mActivity.getContentResolver(),
                Settings.System.NOTIFICATION_LIGHT_OFF, off);

        mOnTime.setText(getTimeString(onBlink));
        mOffTime.setText(getTimeString(offBlink));
        mLEDButton.setColorFilter(userColor, PorterDuff.Mode.MULTIPLY);
        mLedScreenOn.setChecked(Settings.Secure.getInt(mActivity.getContentResolver(),
                Settings.Secure.LED_SCREEN_ON, 0) == 1);

        String charging_led_enabled = Helpers.getSystemProp(PROP_CHARGING_LED, "0");
        if (charging_led_enabled.length() == 0) {
            charging_led_enabled = "0";
        }
        mChargingLedOn.setChecked(Integer.parseInt(charging_led_enabled) == 1);
    }

    private void saveCustomApps() {
        List<String> moveToSettings = new ArrayList<String>();
        if (customAppList != null) {
            for (CustomApps ca : customAppList.values()) {
                moveToSettings.add(ca.toString());
            }
            final String value = TextUtils.join("|", moveToSettings);
            if (DEBUG)
                Log.e(TAG, "Saved to Settings: " + value);
            Settings.System.putString(mActivity.getContentResolver(),
                    Settings.System.LED_CUSTOM_VALUES, value);
        }
    }

    private void addCustomApp(String app) {
        CustomApps custom = customAppList.get(app);
        if (custom == null) {
            custom = new CustomApps(app, userColor);
            customAppList.put(app, custom);
            appList.add(app);
            putAppInUnicornList(app);
        }
    }

    private void addCustomApp(String app, int color) {
        CustomApps custom = customAppList.get(app);
        if (custom == null) {
            custom = new CustomApps(app, color);
            customAppList.put(app, custom);
            appList.add(app);
            putAppInUnicornList(app);
        }
    }

    private void putAppInUnicornList(String packageName) {
        final PackageManager pm = mActivity.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, 0);
        } catch (final NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai)
                : "unknown");

        unicornApps.add(unicornApps.size() - 1, applicationName);
        adapterRefreshSetting();
    }

    private void parseExistingAppList() {
        String currentApps = Settings.System.getString(mActivity.getContentResolver(),
                Settings.System.LED_CUSTOM_VALUES);

        unicornApps.add(getResources().getString(R.string.led_custom_default));
        unicornApps.add("+App");

        if (DEBUG)
            Log.e(TAG, "currentApps parsed: " + currentApps);

        if (currentApps != null) {
            final String[] array = TextUtils.split(currentApps, "\\|");
            for (String item : array) {
                if (TextUtils.isEmpty(item)) {
                    continue;
                }
                CustomApps app = CustomApps.fromString(item);
                if (app != null) {
                    addCustomApp(app.appPackage, app.appColor);
                }
            }
        }
    }

    private void updateLEDforNew(int id) {
        if (id != 0 && id != unicornApps.size()) {
            int key = id - 1;
            String hashKey = appList.get(key);
            CustomApps custom = customAppList.get(hashKey);
            userColor = custom.appColor;
            if (DEBUG)
                Log.d(TAG, "user color from Hash = " + userColor);
        } else if (id == 0) {
            userColor = Settings.System.getInt(mActivity.getContentResolver(),
                    Settings.System.NOTIFICATION_LIGHT_COLOR, defaultColor);
            if (DEBUG)
                Log.d(TAG, "user color from Hash = " + userColor);
        }

    }

    /**
     * Let's make a spinner class for the listed apps Default should always be
     * on top +App should be at bottom
     */
    class customAppSpinnerSelected implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (pos == unicornApps.size() - 1) {
                mPicker.pickShortcut();
                if (DEBUG)
                    Log.e(TAG, "Pick a shortcut from click!");
            }
            if (pos != unicornApps.size() - 1) {
                updateLEDforNew(pos);
            }

            currentSelectedApp = pos;
            editButtonVisibility();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // do nothing
        }
    }

    class buttonEditApp implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            final int place = currentSelectedApp;
            String selectedApp = unicornApps.get(place);
            AlertDialog.Builder ad = new AlertDialog.Builder(mActivity);
            ad.setTitle(R.string.led_custom_title);
            ad.setIcon(R.mipmap.ic_launcher);
            ad.setMessage(getResources().getString(R.string.led_custom_message) + selectedApp + "?");
            ad.setPositiveButton(R.string.led_change_app,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPicker.pickShortcut();
                            int key = place - 1;
                            unicornApps.remove(place);
                            String hashKey = appList.get(key);
                            appList.remove(key);
                            customAppList.remove(hashKey);
                            adapterRefreshSetting();
                            saveCustomApps();
                        }
                    });
            ad.setNeutralButton(R.string.led_delete_app,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int key = place - 1;
                            unicornApps.remove(place);
                            String hashKey = appList.get(key);
                            appList.remove(key);
                            customAppList.remove(hashKey);
                            adapterRefreshSetting();
                            saveCustomApps();
                        }
                    });
            ad.setNegativeButton(R.string.led_keep_app,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            ad.show();
        }
    }

    private BroadcastReceiver testLedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Notification.Builder nb = new Notification.Builder(context);
                nb.setAutoCancel(true);
                nb.setLights(userColor, onBlink, offBlink);
                Notification test = nb.getNotification();
                nm.notify(1, test);
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                nm.cancel(1);
            }
        }
    };

    /**
     * Create our own class for custom apps for easier time with HashMap storage
     * also make it static so it doesnt destroy the information when the class
     * is not in use
     */
    static class CustomApps {
        public String appPackage;
        public int appColor;

        public CustomApps(String appPackage, int appColor) {
            this.appPackage = appPackage;
            this.appColor = appColor;
        }

        // create a string to return with a char to split from later
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(appPackage);
            sb.append(";");
            sb.append(appColor);
            return sb.toString();
        }

        public static CustomApps fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            String[] app = value.split(";", -1);
            if (app.length != 2)
                return null;

            try {
                CustomApps item = new CustomApps(app[0], Integer.parseInt(app[1]));
                return item;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private void editButtonVisibility() {
        if (currentSelectedApp == 0 || currentSelectedApp == unicornApps.size()) {
            mEditApp.setVisibility(View.GONE);
        } else {
            mEditApp.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap icon, boolean isApplication) {
        String packageName = null;
        final PackageManager pm = mActivity.getPackageManager();
        try {
            Intent intent = Intent.getIntent(uri);
            packageName = intent.resolveActivity(pm).getPackageName();
            if (DEBUG)
                Log.e(TAG, uri);
            if (DEBUG)
                Log.e(TAG, packageName);
        } catch (URISyntaxException e) { }

        if (packageName != null) {
            addCustomApp(packageName);
            saveCustomApps();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            mPicker.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            // do nothing
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
