
package com.aokp.romcontrol.vibrations;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.VibrationPattern;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

public class VibrationRecorder {
    private static final String TAG = "VibrationRecorder";
    Vibrator mVibrator;
    Thread mVibratorThread;
    boolean mContinueVibrating;
    boolean mRecordInProgress;
    boolean mLoadedPattern;
    VibrationPattern mCurrentPattern;
    ArrayList<Long> mCapturedTimes;
    Context mContext;

    public VibrationRecorder(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mContext = context;
    }

    public void startVibration() {
        if (mVibratorThread == null) {
            mContinueVibrating = true;
            mVibratorThread = new VibratorThread();
            mVibratorThread.start();
        }
    }

    public void stopVibration() {
        if (mVibratorThread != null) {
            mContinueVibrating = false;
            mVibratorThread = null;
        }
        // Also immediately cancel any vibration in progress.
        mVibrator.cancel();
    }

    public void startRecording() {
        mRecordInProgress = true;
        mCapturedTimes = new ArrayList<Long>();
        processTime(android.os.SystemClock.uptimeMillis());
    }

    public void stopRecording() {
        stopVibration();
        mRecordInProgress = false;
        if (mCapturedTimes.size() > 2) {
            if (mCurrentPattern != null) {
                Uri tempUri = mCurrentPattern.getUri();
                mCurrentPattern = new VibrationPattern(mCurrentPattern.getName(), mCapturedTimes,
                        mContext);
                mCurrentPattern.setUri(tempUri);
            } else {
                mCurrentPattern = new VibrationPattern("<New>", mCapturedTimes, mContext);
            }
        } else {
            mCurrentPattern = null;
        }
    }

    public void processTime(long time) {
        if (mRecordInProgress) {
            mCapturedTimes.add(time);
        }
    }

    public void resetCapture() {
        mCapturedTimes = null;
        mCurrentPattern = null;
        mLoadedPattern = false;
    }

    public void playCapturedPattern() {
        if (mCurrentPattern != null)
            mVibrator.vibrate(mCurrentPattern.getPattern(), -1);
    }

    private class VibratorThread extends Thread {
        public void run() {
            while (mContinueVibrating) {
                mVibrator.vibrate(10000);
                SystemClock.sleep(10000);
            }
        }
    }

    public void saveCapturedPattern(String name) {
        if (mCurrentPattern != null) {
            ContentValues values = new ContentValues();
            mCurrentPattern.setName(name);
            values.put(VibrationsProvider.NAME, name);
            values.put(VibrationsProvider.PATTERN, mCurrentPattern.getPatternString());
            if (mLoadedPattern) {
                int updated = mContext.getContentResolver().update(
                        mCurrentPattern.getUri(), values, null, null);
            } else {
                mCurrentPattern.setUri(mContext.getContentResolver().insert(
                        VibrationsProvider.CONTENT_URI, values));
            }
        }
    }

    public void delPattern(VibrationPattern pattern) {
        if (pattern != null) {
            int deleted = mContext.getContentResolver().delete(pattern.getUri(), null, null);
        }
    }

    public void loadPattern(Uri uri) {
        mCurrentPattern = new VibrationPattern(uri, mContext);
        mLoadedPattern = true;
    }

    public String getLoadedPatternName() {
        if (mCurrentPattern != null) {
            return mCurrentPattern.getName();
        } else {
            return "-";
        }

    }

    public VibrationPattern getCurrentPattern() {
        return mCurrentPattern;
    }
}
