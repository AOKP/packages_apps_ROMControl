package com.aokp.romcontrol.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FlipService extends Service {

    final static String TAG = "FlipService";
    public final static boolean DEBUG = false;

    public static final String KEY_FLIP_MODE = "flip_mode";
    public static final String DEFAULT_FLIP = "-1";
    public static final String KEY_USER_TIMEOUT = "user_timeout";
    public static final String KEY_USER_DOWN_MS = "user_down_ms";
    public static final int MODE_VIBRATE = AudioManager.RINGER_MODE_VIBRATE;
    public static final int MODE_SILENT = AudioManager.RINGER_MODE_SILENT;
    public static final String TIMEOUT_MS_DEFAULT = "15000";
    public static final String DOWN_MS_DEFAULT = "1500";
    public static final int INSTANT_OFF = 0;
    public static final String KEY_PHONE_RING_SILENCE = "phone_ring_silence";
    public static final String PHONE_SILENCE_OFF = "0";

    // int for limits on flip, thanks CM
    private static final int FACE_UP_LOWER_LIMIT = -45;
    private static final int FACE_UP_UPPER_LIMIT = 45;
    private static final int FACE_DOWN_UPPER_LIMIT = 135;
    private static final int FACE_DOWN_LOWER_LIMIT = -135;
    private static final int TILT_UPPER_LIMIT = 45;
    private static final int TILT_LOWER_LIMIT = -45;
    private static final int SENSOR_SAMPLES = 3;

    private boolean[] mSamples = new boolean[SENSOR_SAMPLES];
    private int mSampleIndex;
    private boolean wasFaceUp;
    private boolean wasFaceDown = false;
    boolean switchSoundBack = false;
    boolean wentSilentFromRing = false;
    static boolean mRegistered = false;
    static boolean mSecondReg = true;
    Handler handler = new Handler();
    private boolean faceDownIsRunning = false;
    private boolean cancelRunDown = false;
    private boolean callIncoming = false;

    // added to sort out context issues from inner classes
    private FlipService service = this;
    private AudioManager am;

    // lets add some vibrate when you flip over!
    Vibrator vib;
    private int quick = 150;
    private int fastQuick = 50;
    private int pause = 150;
    private long[] pattern = {
            0, quick, pause, fastQuick, 0, quick
    };

    private SensorEventListener sl = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float y = event.values[1];
            float z = event.values[2];

            if (getUserFlipAudioMode(service) != -1 || getUserCallSilent(service) == 1) {
                log("start looking!");
                if (!wasFaceUp) {
                    // Check if its face up enough.
                    mSamples[mSampleIndex] =
                            y > FACE_UP_LOWER_LIMIT && y < FACE_UP_UPPER_LIMIT
                                    && z > TILT_LOWER_LIMIT && z < TILT_UPPER_LIMIT;

                    // The device first needs to be face up.
                    boolean faceUp = true;
                    log("device is face up!");
                    for (boolean sample : mSamples) {
                        faceUp = faceUp && sample;
                    }

                    if (faceUp) {
                        for (int i = 0; i < SENSOR_SAMPLES; i++)
                            mSamples[i] = false;
                        if (wasFaceDown) {
                            wasFaceDown = false;
                            if (switchSoundBack
                                    && am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                                switchSoundBack = false;
                                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                log("Flipped back face up! Ringer Normal!");
                            }
                        }
                        wasFaceUp = true;
                    }
                } else if (wasFaceUp) {
                    // Check if its face down enough. Note that wanted
                    // values go from FACE_DOWN_UPPER_LIMIT to 180
                    // and from -180 to FACE_DOWN_LOWER_LIMIT
                    mSamples[mSampleIndex] =
                            (y > FACE_DOWN_UPPER_LIMIT || y < FACE_DOWN_LOWER_LIMIT)
                                    && z > TILT_LOWER_LIMIT && z < TILT_UPPER_LIMIT;

                    boolean faceDown = true;
                    log("device is face down!");
                    for (boolean sample : mSamples) {
                        faceDown = faceDown && sample;
                        log("device is face down, from the limits!");
                    }

                    // what to do when face down
                    // do not change to wasFaceDown till it has changed
                    // or else it will not ever get to it
                    if (faceDown) {
                        for (int i = 0; i < SENSOR_SAMPLES; i++)
                            mSamples[i] = false;
                        cancelRunDown = true;
                        if (getUserFlipAudioMode(service) != -1 && !callIncoming) {
                            handler.postDelayed(faceDownTimer, getUserDownMS(service));
                        }
                        // we can use the flip down to silent calls too!
                        if (getUserCallSilent(service) == 1) {
                            log("silent mode turned down");
                            if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL
                                    && callIncoming) {
                                log("silent mode on for incoming call!");
                                am.setRingerMode(MODE_SILENT);
                                wentSilentFromRing = true;
                                callIncoming = false;
                                wasFaceUp = false;
                            }
                        }

                    } else {
                        if (faceDownIsRunning) {
                            cancelRunDown = false;
                            wasFaceUp = false;
                            faceDownIsRunning = false;
                        }
                    }
                }

                mSampleIndex = ((mSampleIndex + 1) % SENSOR_SAMPLES);
            }
        }
    };

    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (!mSecondReg) {
                    if (getUserFlipAudioMode(service) != -1) {
                        getSensorManager().registerListener(sl,
                                getSensorManager().getDefaultSensor(Sensor.TYPE_ORIENTATION),
                                SensorManager.SENSOR_DELAY_UI);
                        mSecondReg = true;
                    }
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if (mSecondReg) {
                    handler.postDelayed(screenOffTimer, getUserScreenTimeout(context));
                }
            } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

                if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    if (mSecondReg) {
                        handler.postDelayed(screenOffTimer, INSTANT_OFF);
                    }
                } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    log("the phone is ringing");
                    if (getUserCallSilent(context) == 0) {
                        log("phone doesnt need sensor when ringing");
                        if (mSecondReg) {
                            handler.postDelayed(screenOffTimer, INSTANT_OFF);
                        }
                    } else {
                        log("phone needs the sensor when ringing");
                        if (!mSecondReg) {
                            getSensorManager().registerListener(sl,
                                    getSensorManager().getDefaultSensor(Sensor.TYPE_ORIENTATION),
                                    SensorManager.SENSOR_DELAY_UI);
                            mSecondReg = true;
                        }
                        callIncoming = true;
                    }
                } else {
                    log("phone idle");
                    if (!mSecondReg) {
                        if (getUserFlipAudioMode(service) != -1) {
                            getSensorManager().registerListener(sl,
                                    getSensorManager().getDefaultSensor(Sensor.TYPE_ORIENTATION),
                                    SensorManager.SENSOR_DELAY_UI);
                            mSecondReg = true;
                        }
                    }
                    if (wentSilentFromRing) {
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        wentSilentFromRing = false;
                    }
                }

            }
        }
    };

    private Runnable faceDownTimer = new Runnable() {
        public void run() {
            faceDownIsRunning = true;
            if (cancelRunDown) {
                switch (getUserFlipAudioMode(service)) {
                    case MODE_SILENT:
                        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            am.setRingerMode(MODE_SILENT);
                            switchSoundBack = true;
                            wasFaceUp = false;
                            wasFaceDown = true;
                            vib.vibrate(pattern, -1);
                            log("face over, lets go silent!");
                        }
                        break;
                    case MODE_VIBRATE:
                        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            am.setRingerMode(MODE_VIBRATE);
                            switchSoundBack = true;
                            wasFaceUp = false;
                            wasFaceDown = true;
                            vib.vibrate(pattern, -1);
                            log("face over, lets go vibrate!");
                        }
                        break;
                }
            }
        }
    };

    private Runnable screenOffTimer = new Runnable() {
        @Override
        public void run() {
            getSensorManager().unregisterListener(sl);
            mSecondReg = false;
        }
    };

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onDestroy() {
        if (mRegistered) {
            if (mSecondReg) {
                getSensorManager().unregisterListener(sl);
            }
            unregisterReceiver(screenReceiver);
            mRegistered = false;
        }
        super.onDestroy();
    }

    public static int getUserFlipAudioMode(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(prefs.getString(KEY_FLIP_MODE, DEFAULT_FLIP));
    }

    public static int getUserScreenTimeout(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(prefs.getString(KEY_USER_TIMEOUT, TIMEOUT_MS_DEFAULT));
    }

    public static int getUserDownMS(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(prefs.getString(KEY_USER_DOWN_MS, DOWN_MS_DEFAULT));
    }

    public static int getUserCallSilent(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(prefs.getString(KEY_PHONE_RING_SILENCE, PHONE_SILENCE_OFF));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            am = (AudioManager) service
                    .getSystemService(Context.AUDIO_SERVICE);
            vib = (Vibrator) service
                    .getSystemService(Context.VIBRATOR_SERVICE);

            if (getUserFlipAudioMode(service) != -1) {
                getSensorManager().registerListener(sl,
                        getSensorManager().getDefaultSensor(Sensor.TYPE_ORIENTATION),
                        SensorManager.SENSOR_DELAY_UI);
            }

            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            registerReceiver(screenReceiver, filter);

            mRegistered = true;
            log("register sensor manager");
        }
        return START_STICKY;
    }

    public static boolean isStarted() {
        return mRegistered;
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
