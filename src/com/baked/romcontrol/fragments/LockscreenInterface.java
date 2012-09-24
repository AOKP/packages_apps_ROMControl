/*
 * Copyright (C) 2012 CyanogenMod
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baked.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.Utils;

import net.margaritov.preference.colorpicker.ColorPickerView;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockscreenInterface extends BAKEDPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "LockscreenInterface";
    private static final boolean DEBUG = true;
    public static final int REQUEST_PICK_WALLPAPER = 199;
    public static final int SELECT_WALLPAPER = 3;
    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";
    public static final String KEY_BACKGROUND_PREF = "lockscreen_background";
    private static final String PREF_LOCKSCREEN_WALLPAPER_ALPHA = "lockscreen_wallpaper_alpha";
    private static final String KEY_ALWAYS_BATTERY_PREF = "lockscreen_battery_status";
    private static final String KEY_LOCKSCREEN_ROTATION = "lockscreen_rotation";
    private static final String LOCKSCREEN_ROTATION_MODE = "Lock screen";

    private File wallpaperImage;
    private File wallpaperTemporary;

    private ListPreference mCustomBackground;
    private ListPreference mBatteryStatus;
    private CheckBoxPreference mLockScreenRotation;
    private Activity mActivity;
    private Preference mWallpaperAlpha;
    ContentResolver mResolver;

    private boolean mIsScreenLarge;

    private int seekbarProgress;

    ArrayList<String> keys = new ArrayList<String>();

    ColorPickerPreference mLockscreenTextColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mResolver = mActivity.getContentResolver();

        keys.add(Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL);

        addPreferencesFromResource(R.xml.prefs_lockscreen);

        mLockScreenRotation = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_ROTATION);
        mLockScreenRotation.setChecked(Settings.System.getInt(mResolver,
                Settings.System.LOCKSCREEN_ROTATION, 0) == 1);

        mCustomBackground = (ListPreference) findPreference(KEY_BACKGROUND_PREF);
        mCustomBackground.setOnPreferenceChangeListener(this);

        mWallpaperAlpha = (Preference) findPreference(PREF_LOCKSCREEN_WALLPAPER_ALPHA);

        wallpaperImage = new File(mActivity.getFilesDir()+"/lockwallpaper");
        wallpaperTemporary = new File(mActivity.getCacheDir()+"/lockwallpaper.tmp");

        mBatteryStatus = (ListPreference) findPreference(KEY_ALWAYS_BATTERY_PREF);
        mBatteryStatus.setOnPreferenceChangeListener(this);

        for (String key : keys) {
            try {
                ((CheckBoxPreference) findPreference(key)).setChecked(Settings.System.getInt(
                        getActivity().getContentResolver(), key) == 1);
             } catch (SettingNotFoundException e) {
             }
        }

        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);

        mIsScreenLarge = Utils.isTablet(getActivity());

        updateCustomBackgroundSummary();
    }

    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_BACKGROUND);
        if (value == null) {
            resId = R.string.lockscreen_background_default_wallpaper;
            mCustomBackground.setValueIndex(2);
        } else if (value.isEmpty()) {
            resId = R.string.lockscreen_background_custom_image;
            mCustomBackground.setValueIndex(1);
        } else {
            resId = R.string.lockscreen_background_color_fill;
            mCustomBackground.setValueIndex(0);
        }
        mCustomBackground.setSummary(getResources().getString(resId));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateState() {
        int resId;

        // Set the battery status description text
        if (mBatteryStatus != null) {
            boolean batteryStatusAlwaysOn = Settings.System.getInt(mResolver,
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, 0) == 1;
            if (batteryStatusAlwaysOn) {
                mBatteryStatus.setValueIndex(1);
            } else {
                mBatteryStatus.setValueIndex(0);
            }
            mBatteryStatus.setSummary(mBatteryStatus.getEntry());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == Activity.RESULT_OK) {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.renameTo(wallpaperImage);
                }
                wallpaperImage.setReadable(true, false);
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_successful), Toast.LENGTH_LONG).show();
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND,"");
                updateCustomBackgroundSummary();
            } else {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.delete();
                }
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_not_successful), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockScreenRotation) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_ROTATION,
                    mLockScreenRotation.isChecked() ? 1 : 0);
            return true;

        } else if (keys.contains(preference.getKey())) {
            Log.e("RC_Lockscreens", "key: " + preference.getKey());
            return Settings.System.putInt(getActivity().getContentResolver(), preference.getKey(),
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);

        } else if (preference == mWallpaperAlpha) {
            Resources res = getActivity().getResources();
            String cancel = res.getString(R.string.cancel);
            String ok = res.getString(R.string.ok);
            String title = res.getString(R.string.alpha_dialog_title);
            float savedProgress = Settings.System.getFloat(getActivity()
                        .getContentResolver(), Settings.System.LOCKSCREEN_WALLPAPER_ALPHA, 1.0f);

            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);
            SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
            OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                    seekbarProgress = seekbar.getProgress();
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekbar) {
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekbar) {
                }
            };
            seekbar.setProgress((int) (savedProgress * 100));
            seekbar.setMax(100);
            seekbar.setOnSeekBarChangeListener(seekBarChangeListener);
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setView(alphaDialog)
                    .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // nothing
                }
            })
            .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    float val = ((float) seekbarProgress / 100);
                    Settings.System.putFloat(getActivity().getContentResolver(),
                        Settings.System.LOCKSCREEN_WALLPAPER_ALPHA, val);
                }
            })
            .create()
            .show();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mCustomBackground) {
            int indexOf = mCustomBackground.findIndexOfValue(objValue.toString());
            switch (indexOf) {
            //Displays color dialog when user has chosen color fill
            case 0:
                final ColorPickerView colorView = new ColorPickerView(mActivity);
                int currentColor = Settings.System.getInt(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND, -1);
                if (currentColor != -1) {
                    colorView.setColor(currentColor);
                }
                colorView.setAlphaSliderVisible(true);
                new AlertDialog.Builder(mActivity)
                .setTitle(R.string.lockscreen_custom_background_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_BACKGROUND, colorView.getColor());
                        updateCustomBackgroundSummary();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setView(colorView).show();
                return false;
            //Launches intent for user to select an image/crop it to set as background
            case 1:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("scale", true);
                intent.putExtra("scaleUpIfNeeded", false);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                Display display = mActivity.getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();
                Rect rect = new Rect();
                Window window = mActivity.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                int titleBarHeight = contentViewTop - statusBarHeight;
                // Lock screen for tablets visible section are different in landscape/portrait,
                // image need to be cropped correctly, like wallpaper setup for scrolling in background in home screen
                // other wise it does not scale correctly
                if (mIsScreenLarge) {
                    width = mActivity.getWallpaperDesiredMinimumWidth();
                    height = mActivity.getWallpaperDesiredMinimumHeight();
                    float spotlightX = (float) display.getWidth() / width;
                    float spotlightY = (float) display.getHeight() / height;
                    intent.putExtra("aspectX", width);
                    intent.putExtra("aspectY", height);
                    intent.putExtra("outputX", width);
                    intent.putExtra("outputY", height);
                    intent.putExtra("spotlightX", spotlightX);
                    intent.putExtra("spotlightY", spotlightY);

                } else {
                    boolean isPortrait = getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_PORTRAIT;
                    intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
                    intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
                }
                try {
                    wallpaperTemporary.createNewFile();
                    wallpaperTemporary.setWritable(true, false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(wallpaperTemporary));
                    intent.putExtra("return-data", false);
                    mActivity.startActivityFromFragment(this, intent, REQUEST_PICK_WALLPAPER);
                } catch (IOException e) {
                } catch (ActivityNotFoundException e) {
                }
                return false;
            //Sets background color to default
            case 2:
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_BACKGROUND, null);
                updateCustomBackgroundSummary();
                break;
            }
            return true;

        } else if (preference == mBatteryStatus) {
            int value = Integer.valueOf((String) objValue);
            int index = mBatteryStatus.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALWAYS_SHOW_BATTERY, value);
            mBatteryStatus.setSummary(mBatteryStatus.getEntries()[index]);
            return true;

        }else if (preference == mLockscreenTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(objValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, intHex);
            if (DEBUG) Log.d(TAG, String.format("new color hex value: %d", intHex));
            return true;
        }
        return false;
    }
}
