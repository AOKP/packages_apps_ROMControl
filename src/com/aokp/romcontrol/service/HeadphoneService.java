package com.aokp.romcontrol.service;

import android.app.Service;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class HeadphoneService extends Service {

    final static String TAG = "AudioReciver";
    public final static boolean DEBUG = false;

    public static final String KEY_BT_AUDIO_MODE = "bt_audio_mode";
    public static final String KEY_HEADPHONE_AUDIO_MODE = "headphone_audio_mode";
    public static final int MODE_UNTOUCHED = -1;
    public static final int MODE_VIBRATE = AudioManager.RINGER_MODE_VIBRATE;
    public static final int MODE_SILENT = AudioManager.RINGER_MODE_SILENT;

    static boolean mRegistered = false;
    boolean mShouldSwitchBack = false;
    boolean mRunningOwnRingerModeChange = false;

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                final int userPreferenceAudioMode = getUserHeadphoneAudioMode(context);

                if (userPreferenceAudioMode == MODE_UNTOUCHED)
                    return;

                final int state = intent.getIntExtra("state", 0);

                final AudioManager am = (AudioManager) context
                        .getSystemService(Context.AUDIO_SERVICE);

                if (state == 1) {
                    // plugged in
                    if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        mShouldSwitchBack = true;
                        am.setRingerMode(userPreferenceAudioMode);
                        log("plugged in");
                    }
                } else {
                    // unplugged
                    if (mShouldSwitchBack && am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                        mShouldSwitchBack = false;
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        log("unplugged");
                    }
                }
                mRunningOwnRingerModeChange = true;
            } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                log("BT Action Called");
                final int userPreferenceAudioMode = getUserBTAudioMode(context);

                if (DEBUG) {
                    Log.d(TAG, "user picked audio mode = " + userPreferenceAudioMode);
                }

                if (userPreferenceAudioMode == MODE_UNTOUCHED)
                    return;

                final int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);

                final AudioManager am = (AudioManager) context
                        .getSystemService(Context.AUDIO_SERVICE);

                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    // connected
                    if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        mShouldSwitchBack = true;
                        am.setRingerMode(userPreferenceAudioMode);
                        log("bt connected");
                    }
                } else {
                    // disconnected or others
                    if (mShouldSwitchBack && am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                        mShouldSwitchBack = false;
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        log("bt not connected");
                    }
                }
                mRunningOwnRingerModeChange = true;
            } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                log("ringer mode changed");

                if (mRunningOwnRingerModeChange) {
                    mRunningOwnRingerModeChange = false;
                } else if (mShouldSwitchBack) {
                    mShouldSwitchBack = false;
                    log("not switching back");
                }
            }

        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(headsetReceiver);
        mRegistered = false;
        super.onDestroy();
    }

    public static int getUserHeadphoneAudioMode(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        // stored as strings from listpreference
        return Integer.parseInt(prefs.getString(KEY_HEADPHONE_AUDIO_MODE, String.valueOf(MODE_UNTOUCHED)));
    }

    public static int getUserBTAudioMode(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        // stored as strings from listpreference
        return Integer.parseInt(prefs.getString(KEY_BT_AUDIO_MODE, String.valueOf(MODE_UNTOUCHED)));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRegistered) {
            IntentFilter inf = new IntentFilter();
            inf.addAction(Intent.ACTION_HEADSET_PLUG);
            inf.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            inf.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);

            registerReceiver(headsetReceiver, inf);
            mRegistered = true;
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static void log(String s) {
        if (DEBUG)
            Log.e(TAG, s);
    }
}
