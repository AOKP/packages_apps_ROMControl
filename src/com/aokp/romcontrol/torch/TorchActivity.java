
package com.aokp.romcontrol.torch;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.provider.Settings;
import android.util.Log;
import android.os.Bundle;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.aokp.romcontrol.R;

public class TorchActivity extends Activity {
    public static final String TAG = "AOKPTorchAct";
    private boolean mUseCameraInterface;
    private TorchApp mTorchApp;
    private SurfaceTexture mSurfaceTexture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: This layout does not exist! Fix me!
        //setContentView(R.layout.main);
        mUseCameraInterface = getResources().getBoolean(R.bool.useCameraInterface);
        boolean torchOn = Settings.System.getBoolean(getContentResolver(),
                Settings.System.TORCH_STATE, false);
        if (mUseCameraInterface) {
            mTorchApp = (TorchApp) getApplicationContext();
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    textures[0]);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            mSurfaceTexture = new SurfaceTexture(textures[0]);
            if (torchOn) {
                mTorchApp.getCameraManager().turnOff();
            } else {
                mTorchApp.getCameraManager().turnOn(mSurfaceTexture);
            }
            finish();
        } else {
            //Log.d(TAG, "we are not using the camera interface");
            mTorchApp = (TorchApp) getApplicationContext();
            mTorchApp.getConfigValues();
            // handle writing torch settings in one method
            mTorchApp.handleTorchSwitch(torchOn);
            finish();
        }
    }
}
