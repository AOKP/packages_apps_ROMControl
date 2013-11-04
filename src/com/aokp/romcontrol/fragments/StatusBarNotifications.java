package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.TwoStatePreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.util.ShortcutPickerHelper;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class StatusBarNotifications extends AOKPPreferenceFragment implements OnPreferenceChangeListener {

    private static final CharSequence PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final CharSequence PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
    private static final CharSequence PREF_NOTIFICATION_WALLPAPER_ALPHA =
            "notification_wallpaper_alpha";
    private static final CharSequence PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final CharSequence PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
    private static final CharSequence PREF_IME_SWITCHER = "ime_switcher";
    private static final CharSequence PREF_STATUSBAR_BRIGHTNESS = "statusbar_brightness_slider";
    private static final CharSequence PREF_NOTIFICATION_VIBRATE = "notification";
    private static final CharSequence PREF_STATUSBAR_HIDDEN = "statusbar_hidden";

    private static final int REQUEST_PICK_WALLPAPER = 201;

    private static final String WALLPAPER_NAME = "notification_wallpaper.jpg";



    CheckBoxPreference mStatusBarNotifCount;
    Preference mNotificationWallpaper;
    Preference mWallpaperAlpha;
    Preference mCustomLabel;
    CheckBoxPreference mVibrateOnExpand;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mStatusbarSliderPreference;
    CheckBoxPreference mStatusBarHide;
    String mCustomLabelText = null;

    private int mUiMode;
    private int mSeekbarProgress;



    private static ContentResolver mContentResolver;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_notifications);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_notifications);
        mContentResolver = getContentResolver();


        PreferenceScreen prefs = getPreferenceScreen();
        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.STATUSBAR_NOTIF_COUNT, false));

        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
        mShowImeSwitcher.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.SHOW_STATUSBAR_IME_SWITCHER, true));

        mStatusbarSliderPreference = (CheckBoxPreference) findPreference(PREF_STATUSBAR_BRIGHTNESS);
        mStatusbarSliderPreference.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.STATUSBAR_BRIGHTNESS_SLIDER, true));

        mNotificationWallpaper = findPreference(PREF_NOTIFICATION_WALLPAPER);

        mWallpaperAlpha = (Preference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);

        mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.VIBRATE_NOTIF_EXPAND, true));
        if (!hasVibration) {
            ((PreferenceGroup) findPreference(PREF_NOTIFICATION_VIBRATE))
                    .removePreference(mVibrateOnExpand);
        }

        mStatusBarHide = (CheckBoxPreference) findPreference(PREF_STATUSBAR_HIDDEN);
        mStatusBarHide.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.STATUSBAR_HIDDEN, false));

        mUiMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.CURRENT_UI_MODE, 0);

        if (mUiMode == 1) {
            mStatusbarSliderPreference.setEnabled(false);
            mStatusBarHide.setEnabled(false);
            mNotificationWallpaper.setEnabled(false);
            mStatusbarSliderPreference.setSummary(R.string.enable_phone_or_phablet);
            mStatusBarHide.setSummary(R.string.enable_phone_or_phablet);
            mNotificationWallpaper.setSummary(R.string.enable_phone_or_phablet);
        }
        findWallpaperStatus();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.STATUSBAR_NOTIF_COUNT,
                    ((TwoStatePreference) preference).isChecked());
            return true;
        } else if (preference == mNotificationWallpaper) {
            File wallpaper = new File(mContext.getFilesDir(), WALLPAPER_NAME);
            if (wallpaper.exists()) {
                buildWallpaperAlert();
            } else {
                prepareAndSetWallpaper();
            }
            return true;
        } else if (preference == mWallpaperAlpha) {
            Resources res = getActivity().getResources();
            String cancel = res.getString(R.string.cancel);
            String ok = res.getString(R.string.ok);
            String title = res.getString(R.string.alpha_dialog_title);
            float savedProgress = Settings.System.getFloat(mContentResolver,
                    Settings.System.NOTIF_WALLPAPER_ALPHA, 1.0f);

            LayoutInflater factory = LayoutInflater.from(getActivity());
            View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);
            SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
            SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekbar,
                                              int progress, boolean fromUser) {
                    mSeekbarProgress = seekbar.getProgress();
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
                            float val = (float) mSeekbarProgress / 100;
                            Settings.System.putFloat(mContentResolver,
                                    Settings.System.NOTIF_WALLPAPER_ALPHA, val);
                            Helpers.restartSystemUI();
                        }
                    })
                    .create()
                    .show();
            return true;
        } else if (preference == mShowImeSwitcher) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.SHOW_STATUSBAR_IME_SWITCHER,
                    isCheckBoxPrefernceChecked(preference));
            return true;
        } else if (preference == mStatusbarSliderPreference) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.STATUSBAR_BRIGHTNESS_SLIDER,
                    isCheckBoxPrefernceChecked(preference));
            return true;
        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText mView to get user input
            final EditText input = new EditText(getActivity());
            final InputFilter[] filter = new InputFilter[1];
            filter[0] = new InputFilter.LengthFilter(40);

            input.setFilters(filter);
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);
            alert.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = input.getText().toString();
                            Settings.System.putString(mContentResolver,
                                    Settings.System.CUSTOM_CARRIER_LABEL, value);
                            updateCustomLabelTextSummary();
                            Intent i = new Intent();
                            i.setAction("com.aokp.romcontrol.LABEL_CHANGED");
                            mContext.sendBroadcast(i);
                        }
                    });
            alert.setNegativeButton(getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });
            alert.show();
        } else if (preference == mVibrateOnExpand) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.VIBRATE_NOTIF_EXPAND,
                    ((TwoStatePreference) preference).isChecked());
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mStatusBarHide) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_HIDDEN, checked ? true : false);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        return false;
    }

    private Uri getNotificationExternalUri() {
        File dir = mContext.getExternalCacheDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);
        return Uri.fromFile(wallpaper);
    }

    public void findWallpaperStatus() {
        File wallpaper = new File(mContext.getFilesDir(), WALLPAPER_NAME);
        if (mUiMode != 1 && wallpaper.exists()) {
            mWallpaperAlpha.setEnabled(true);
            mWallpaperAlpha.setSummary(null);
        } else {
            mWallpaperAlpha.setEnabled(false);
            mWallpaperAlpha.setSummary(R.string.enable_noti_wallpaper);
        }
    }

    private void prepareAndSetWallpaper() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int width = getActivity().getWallpaperDesiredMinimumWidth();
        int height = getActivity().getWallpaperDesiredMinimumHeight();
        float spotlightX = (float)display.getWidth() / width;
        float spotlightY = (float)display.getHeight() / height;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("spotlightX", spotlightX);
        intent.putExtra("spotlightY", spotlightY);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                getNotificationExternalUri());
        intent.putExtra("outputFormat",
                Bitmap.CompressFormat.PNG.toString());
        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    private void resetWallpaper() {
        mContext.deleteFile(WALLPAPER_NAME);
        findWallpaperStatus();
        Helpers.restartSystemUI();
    }




    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(mContentResolver,
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.isEmpty()) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    private void buildWallpaperAlert() {
        Drawable myWall = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.notification_wallpaper_dialog);
        builder.setPositiveButton(R.string.notification_wallpaper_pick,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prepareAndSetWallpaper();
                    }
                });
        builder.setNegativeButton(R.string.notification_wallpaper_reset,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetWallpaper();
                        dialog.dismiss();
                    }
                });
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.dialog_shade_wallpaper, null);
        ImageView wallView = (ImageView) layout.findViewById(R.id.shade_wallpaper_preview);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        wallView.setLayoutParams(new LinearLayout.LayoutParams(size.x / 2, size.y / 2));
        File wallpaper = new File(mContext.getFilesDir(), WALLPAPER_NAME);
        myWall = new BitmapDrawable(mContext.getResources(), wallpaper.getAbsolutePath());
        wallView.setImageDrawable(myWall);
        builder.setView(layout);
        builder.show();
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_WALLPAPER) {
                FileOutputStream wallpaperStream = null;
                try {
                    wallpaperStream = mContext.openFileOutput(WALLPAPER_NAME,
                            Context.MODE_WORLD_READABLE);
                    Uri selectedImageUri = getNotificationExternalUri();
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            selectedImageUri.getPath());
                    bitmap.compress(Bitmap.CompressFormat.PNG,
                            100,
                            wallpaperStream);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                } finally {
                    try {
                        if (wallpaperStream != null) {
                            wallpaperStream.close();
                        }
                    } catch (IOException e) {
                        // let it go
                    }
                }
                findWallpaperStatus();
                buildWallpaperAlert();
                Helpers.restartSystemUI();
            }
        }
    }


}
