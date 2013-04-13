
package com.aokp.romcontrol.torch;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Application;
import android.hardware.Camera;
import android.graphics.SurfaceTexture;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.R;

public class TorchApp extends Application {
    public static final String TAG = "AOKPTorchApp";
    private String mFlashDevice;
    private CameraManager mCamManager = new CameraManager();

    public CameraManager getCameraManager() {
        return mCamManager;
    }
    public void getConfigValues () {
        mFlashDevice = getResources().getString(R.string.flashDevice);
        if (!new File(mFlashDevice).exists()) {
            Log.e(TAG, "wrong device config and sysfs values in overlay");
        }
    }

    public void handleTorchSwitch(boolean value) {
        Settings.System.putBoolean(getContentResolver(),
                Settings.System.TORCH_STATE, value);
        FileWriter torchWriter = null;
        try {
            torchWriter = new FileWriter(mFlashDevice);
            torchWriter.write(String.valueOf(value));
        } catch (IOException e) {
            Log.e(TAG, "Failed to write sysfs value", e);
        } finally {
            if (torchWriter != null) {
                try {
                    torchWriter.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public class CameraManager {
        public Camera mCamera;
        public void connectToCam() {
            if (mCamera == null) {
                try {
                    mCamera = Camera.open();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open camera!", e);
                }
            }
            if (mCamera != null) {
                try {
                    mCamera.reconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to reconnect to camera!", e);
                }
            }
        }

        public void turnOn(SurfaceTexture texture) {
            connectToCam();
            if (mCamera != null) {
                try {
                    mCamera.stopPreview();
                    mCamera.setPreviewTexture(texture);
                    mCamera.startPreview();
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(params);
                    Settings.System.putBoolean(getContentResolver(),
                            Settings.System.TORCH_STATE, true);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to turn on Torch", e);
                }
            }
        }

        public void turnOff() {
            connectToCam();
            if (mCamera != null) {
                try {
                    Camera.Parameters params = mCamera.getParameters();
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    mCamera.setParameters(params);
                    mCamera.stopPreview();
                    Settings.System.putBoolean(getContentResolver(),
                            Settings.System.TORCH_STATE, false);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to turn off Torch", e);
                }
                releaseCam();
            }
        }

        public void releaseCam() {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
            }
        }
    }
}
