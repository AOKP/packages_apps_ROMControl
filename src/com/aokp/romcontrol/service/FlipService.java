package com.aokp.romcontrol.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

public class FlipService extends Service {

    final static String TAG = "FlipService";
    public final static boolean DEBUG = false;

    public static final String KEY_FLIP_MODE = "flip_mode";
    public static final String DEFAULT_FLIP = "-1";
    public static final int MODE_VIBRATE = AudioManager.RINGER_MODE_VIBRATE;
    public static final int MODE_SILENT = AudioManager.RINGER_MODE_SILENT;

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
    static boolean mRegistered = false;

    // added to sort out context issues from inner classes
    private FlipService service = this;
    private AudioManager am;

    //lets add some vibrate when you flip over!
    Vibrator vib;
    private int quick = 150;
    private int fastQuick = 50;
    private int pause = 150;
    private long[] pattern = {0, quick, pause, fastQuick, 0, quick};

    private SensorEventListener sl = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float y = event.values[1];
            float z = event.values[2];

            if (getUserFlipAudioMode(service) != -1){
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
                        //change back needs to be in here to detect when it
                        // has hit the limit to be face up
                        if (wasFaceDown) {
                            wasFaceDown = false;
                            if (switchSoundBack && am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                                switchSoundBack = false;
                                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                log("Flipped back face up! Ringer Normal!");
                            }
                        }
                        wasFaceUp = true;
                        for (int i = 0; i < SENSOR_SAMPLES; i++)
                            mSamples[i] = false;
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

                mSampleIndex = ((mSampleIndex + 1) % SENSOR_SAMPLES);
            }

        }

    };

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onDestroy() {
        getSensorManager().unregisterListener(sl);
        mRegistered = false;
        super.onDestroy();
    }

    public static int getUserFlipAudioMode(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return Integer.parseInt(prefs.getString(KEY_FLIP_MODE, DEFAULT_FLIP));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "User Flip Mode= " + getUserFlipAudioMode(service));
        if (!mRegistered) {
            am = (AudioManager) service
                    .getSystemService(Context.AUDIO_SERVICE);
            vib = (Vibrator) service
                    .getSystemService(Context.VIBRATOR_SERVICE);
            getSensorManager().registerListener(sl,
                    getSensorManager().getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_NORMAL);

            mRegistered = true;
            log("register sensor manager");
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
