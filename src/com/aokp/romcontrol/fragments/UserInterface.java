
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.TwoStatePreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.service.CodeReceiver;
import com.aokp.romcontrol.util.AbstractAsyncSuCMDProcessor;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.widgets.AlphaSeekBar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class UserInterface extends AOKPPreferenceFragment implements OnPreferenceChangeListener {
    public final String TAG = getClass().getSimpleName();
    private static final boolean DEBUG = false;

    private static final CharSequence PREF_180 = "rotate_180";
    private static final CharSequence PREF_270 = "rotate_270";
    private static final CharSequence PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final CharSequence PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
    private static final CharSequence PREF_NOTIFICATION_WALLPAPER_ALPHA = "notification_wallpaper_alpha";
    private static final CharSequence PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final CharSequence PREF_SHOW_OVERFLOW = "show_overflow";
    private static final CharSequence PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
    private static final CharSequence PREF_LONGPRESS_TO_KILL = "longpress_to_kill";
    private static final CharSequence PREF_RECENT_KILL_ALL = "recent_kill_all";
    private static final CharSequence PREF_RAM_USAGE_BAR = "ram_usage_bar";
    private static final CharSequence PREF_IME_SWITCHER = "ime_switcher";
    private static final CharSequence PREF_STATUSBAR_BRIGHTNESS = "statusbar_brightness_slider";
    private static final CharSequence PREF_USER_MODE_UI = "user_mode_ui";
    private static final CharSequence PREF_HIDE_EXTRAS = "hide_extras";
    private static final CharSequence PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED = "wakeup_when_plugged_unplugged";
    private static final CharSequence PREF_FORCE_DUAL_PANEL = "force_dualpanel";
    private static final CharSequence PREF_DISABLE_BOOTANIM = "disable_bootanimation";
    private static final CharSequence PREF_CUSTOM_BOOTANIM = "custom_bootanimation";
    private static final CharSequence PREF_NOTIFICATION_VIBRATE = "notification";
    private static final CharSequence PREF_NAVBAR = "navbar";
    private static final CharSequence PREF_MISC = "misc";
    private static final CharSequence PREF_DISPLAY = "display";
    private static final CharSequence PREF_POWER_CRT_MODE = "system_power_crt_mode";
    private static final CharSequence PREF_POWER_CRT_SCREEN_OFF = "system_power_crt_screen_off";
    private static final CharSequence PREF_STATUSBAR_HIDDEN = "statusbar_hidden";

    private static int STOCK_FONT_SIZE = 16;
    private static final int REQUEST_PICK_WALLPAPER = 201;
    //private static final int REQUEST_PICK_CUSTOM_ICON = 202; //unused
    private static final int REQUEST_PICK_BOOT_ANIMATION = 203;

    private static final String WALLPAPER_NAME = "notification_wallpaper.jpg";
    private static final String BOOTANIMATION_USER_PATH = "/data/local/bootanimation.zip";
    private static final String BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip";

    CheckBoxPreference mAllow180Rotation;
    CheckBoxPreference mAllow270Rotation;
    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mStatusBarNotifCount;
    Preference mNotificationWallpaper;
    Preference mWallpaperAlpha;
    Preference mCustomLabel;
    Preference mCustomBootAnimation;
    ImageView mView;
    TextView mError;
    CheckBoxPreference mShowActionOverflow;
    CheckBoxPreference mVibrateOnExpand;
    CheckBoxPreference mLongPressToKill;
    CheckBoxPreference mRecentKillAll;
    CheckBoxPreference mRamBar;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mStatusbarSliderPreference;
    AlertDialog mCustomBootAnimationDialog;
    ListPreference mUserModeUI;
    CheckBoxPreference mHideExtras;
    CheckBoxPreference mWakeUpWhenPluggedOrUnplugged;
    CheckBoxPreference mDualpane;
    ListPreference mCrtMode;
    CheckBoxPreference mCrtOff;
    CheckBoxPreference mStatusBarHide;
    ListPreference mFontsize;

    private AnimationDrawable mAnimationPart1;
    private AnimationDrawable mAnimationPart2;
    private String mErrormsg;
    private String mBootAnimationPath;

    private static ContentResolver mContentResolver;
    private Random mRandomGenerator = new SecureRandom();
    // previous random; so we don't repeat
    private static int mLastRandomInsultIndex = -1;
    private String[] mInsults;

    private int mSeekbarProgress;
    String mCustomLabelText = null;
    int mUserRotationAngles = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_ui);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        mContentResolver = getContentResolver();
        PreferenceScreen prefs = getPreferenceScreen();
        mInsults = mContext.getResources().getStringArray(
                R.array.disable_bootanimation_insults);

        mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
        mAllow270Rotation = (CheckBoxPreference) findPreference(PREF_270);
        mUserRotationAngles = Settings.System.getInt(mContentResolver,
                Settings.System.ACCELEROMETER_ROTATION_ANGLES, -1);
        if (mUserRotationAngles < 0) {
            // Not set by user so use these defaults
            boolean mAllowAllRotations = mContext.getResources().getBoolean(
                            com.android.internal.R.bool.config_allowAllRotations) ? true : false;
            mUserRotationAngles = mAllowAllRotations  ?
                (1 | 2 | 4 | 8) : // All angles
                (1 | 2 | 8); // All except 180
        }
        mAllow180Rotation.setChecked((mUserRotationAngles & 4) != 0);
        mAllow270Rotation.setChecked((mUserRotationAngles & 8) != 0);

        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.STATUSBAR_NOTIF_COUNT, false));

        mDisableBootAnimation = (CheckBoxPreference)findPreference(PREF_DISABLE_BOOTANIM);

        mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM);

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
            ((PreferenceGroup)findPreference(PREF_NOTIFICATION_VIBRATE)).removePreference(mVibrateOnExpand);
        }

        mLongPressToKill = (CheckBoxPreference)findPreference(PREF_LONGPRESS_TO_KILL);
        mLongPressToKill.setChecked(Settings.System.getInt(mContentResolver,
                Settings.System.KILL_APP_LONGPRESS_BACK, 0) == 1);
        if (!hasHardwareButtons) {
            getPreferenceScreen().removePreference(((PreferenceGroup) findPreference(PREF_MISC)));
        }

        mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.RECENT_KILL_ALL_BUTTON, false));

        mRamBar = (CheckBoxPreference) findPreference(PREF_RAM_USAGE_BAR);
        mRamBar.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.RAM_USAGE_BAR, false));

        mHideExtras = (CheckBoxPreference) findPreference(PREF_HIDE_EXTRAS);
        mHideExtras.setChecked(Settings.System.getBoolean(mContentResolver,
                        Settings.System.HIDE_EXTRAS_SYSTEM_BAR, false));

        mShowActionOverflow = (CheckBoxPreference) findPreference(PREF_SHOW_OVERFLOW);
        mShowActionOverflow.setChecked(Settings.System.getBoolean(mContentResolver,
                        Settings.System.UI_FORCE_OVERFLOW_BUTTON, false));

        mStatusBarHide = (CheckBoxPreference) findPreference(PREF_STATUSBAR_HIDDEN);
        mStatusBarHide.setChecked(Settings.System.getBoolean(mContentResolver,
                Settings.System.STATUSBAR_HIDDEN, false));

        mUserModeUI = (ListPreference) findPreference(PREF_USER_MODE_UI);
        int uiMode = Settings.System.getInt(mContentResolver,
                Settings.System.CURRENT_UI_MODE, 0);
        mUserModeUI.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.USER_UI_MODE, uiMode)));
        mUserModeUI.setOnPreferenceChangeListener(this);

        mDualpane = (CheckBoxPreference) findPreference(PREF_FORCE_DUAL_PANEL);
        mDualpane.setChecked(Settings.System.getBoolean(mContentResolver,
                        Settings.System.FORCE_DUAL_PANEL, getResources().getBoolean(
                        com.android.internal.R.bool.preferences_prefer_dual_pane)));

        boolean isCrtOffChecked = (Settings.System.getBoolean(mContentResolver,
                        Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF, true));
        mCrtOff = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_OFF);
        mCrtOff.setChecked(isCrtOffChecked);

        mCrtMode = (ListPreference) findPreference(PREF_POWER_CRT_MODE);
        int crtMode = Settings.System.getInt(mContentResolver,
                Settings.System.SYSTEM_POWER_CRT_MODE, 0);
        mCrtMode.setValue(Integer.toString(Settings.System.getInt(mContentResolver,
                Settings.System.SYSTEM_POWER_CRT_MODE, crtMode)));
        mCrtMode.setOnPreferenceChangeListener(this);

        mWakeUpWhenPluggedOrUnplugged = (CheckBoxPreference) findPreference(PREF_WAKEUP_WHEN_PLUGGED_UNPLUGGED);
        mWakeUpWhenPluggedOrUnplugged.setChecked(Settings.System.getBoolean(mContentResolver,
                        Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED, true));
        // hide option if device is already set to never wake up
        if(!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_unplugTurnsOnScreen)) {
            ((PreferenceGroup) findPreference(PREF_DISPLAY)).removePreference(mWakeUpWhenPluggedOrUnplugged);
        }

        mFontsize = (ListPreference) findPreference("status_bar_fontsize");
        mFontsize.setOnPreferenceChangeListener(this);
        mFontsize.setValue(Integer.toString(Settings.System.getInt(mContentRes,
                Settings.System.STATUSBAR_FONT_SIZE, STOCK_FONT_SIZE)));

        if (isTablet(mContext)) {
            mStatusbarSliderPreference.setEnabled(false);
            mStatusBarHide.setEnabled(false);
        } else {
            mHideExtras.setEnabled(false);
        }

        setHasOptionsMenu(true);
        resetBootAnimation();
        findWallpaperStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDisableBootAnimation != null) {
            if (mDisableBootAnimation.isChecked()) {
                Resources res = mContext.getResources();
                String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                int randomInt = mRandomGenerator.nextInt(insults.length);
                mDisableBootAnimation.setSummary(insults[randomInt]);
            } else {
                mDisableBootAnimation.setSummary(null);
            }
        }
    }

    /**
     * Resets boot animation path. Essentially clears temporary-set boot animation
     * set by the user from the dialog.
     * @return returns true if a boot animation exists (user or system). false otherwise.
     */
    private boolean resetBootAnimation() {
        boolean bootAnimationExists = false;
        if(new File(BOOTANIMATION_USER_PATH).exists()) {
            mBootAnimationPath = BOOTANIMATION_USER_PATH;
            bootAnimationExists = true;
        } else if (new File(BOOTANIMATION_SYSTEM_PATH).exists()) {
            mBootAnimationPath = BOOTANIMATION_SYSTEM_PATH;
            bootAnimationExists = true;
        } else {
            mBootAnimationPath = "";
        }
        mCustomBootAnimation.setEnabled(!mDisableBootAnimation.isChecked());
        return bootAnimationExists;
    }

    private void resetSwaggedOutBootAnimation() {
        if(new File("/data/local/bootanimation.user").exists()) {
            // we're using the alt boot animation
            String moveAnimCommand = "mv /data/local/bootanimation.user /data/local/bootanimation.zip";
            // we must wait for this command to finish before we continue
            CMDProcessor.runSuCommand(moveAnimCommand);
        }
        CodeReceiver.setSwagInitiatedPref(mContext, false);
    }

    private void openTransparencyDialog() {
        getFragmentManager().beginTransaction().add(new AdvancedTransparencyDialog(), null)
                .commit();
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mAllow180Rotation || preference == mAllow270Rotation) {
            boolean checked180 = ((CheckBoxPreference) mAllow180Rotation).isChecked();
            boolean checked270 = ((CheckBoxPreference) mAllow270Rotation).isChecked();
            int result = (1 | 2);
            if (checked180) result |= 4;
            if (checked270) result |= 8;
            Settings.System.putInt(mContentResolver,
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, result);
            return true;
        } else if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.STATUSBAR_NOTIF_COUNT,
                    ((TwoStatePreference) preference).isChecked());
            return true;
        } else if (preference == mDisableBootAnimation) {
            DisableBootAnimation();
            return true;
        } else if (preference == mHideExtras) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.HIDE_EXTRAS_SYSTEM_BAR,
                    ((TwoStatePreference) preference).isChecked());
            return true;
        } else if (preference == mDualpane) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.FORCE_DUAL_PANEL,
                    ((TwoStatePreference) preference).isChecked());
            return true;
        } else if (preference == mCustomBootAnimation) {
            openBootAnimationDialog();
            return true;
        } else if (preference == mShowActionOverflow) {
            boolean enabled = mShowActionOverflow.isChecked();
            Settings.System.putBoolean(mContentResolver, Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                    enabled);
            // Show toast appropriately
            if (enabled) {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_enable,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_disable,
                        Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (preference == mNotificationWallpaper) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            boolean isPortrait = getResources()
                    .getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT;
            intent.putExtra("aspectX", isPortrait ? width : height);
            intent.putExtra("aspectY", isPortrait ? height : width);
            intent.putExtra("outputX", width);
            intent.putExtra("outputY", height);
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    getNotificationExternalUri());
            intent.putExtra("outputFormat",
                    Bitmap.CompressFormat.PNG.toString());
            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
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
            OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {
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
        } else if (preference == mLongPressToKill) {
            boolean checked = ((TwoStatePreference) preference).isChecked();
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.KILL_APP_LONGPRESS_BACK, checked);
            return true;
        } else if (preference == mRecentKillAll) {
            boolean checked = ((TwoStatePreference) preference).isChecked();
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.RECENT_KILL_ALL_BUTTON, checked);
            return true;
        } else if (preference == mRamBar) {
            boolean checked = ((TwoStatePreference) preference).isChecked();
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.RAM_USAGE_BAR, checked);
            return true;
        } else if (preference == mWakeUpWhenPluggedOrUnplugged) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.WAKEUP_WHEN_PLUGGED_UNPLUGGED,
                    ((TwoStatePreference) preference).isChecked());
        } else if ("transparency_dialog".equals(preference.getKey())) {
            openTransparencyDialog();
            return true;
        } else if (preference == mCrtOff) {
            Settings.System.putBoolean(mContentResolver,
                    Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                    ((TwoStatePreference) preference).isChecked());
        } else if (preference == mStatusBarHide) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_HIDDEN, checked ? true : false);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.user_interface, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_wallpaper:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mContext.deleteFile(WALLPAPER_NAME);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findWallpaperStatus();
                            }
                        });
                        Helpers.restartSystemUI();
                    }
                }).start();
                return true;
            default:
                // call to super is implicit
                return onContextItemSelected(item);
        }
    }

    private Uri getNotificationExternalUri() {
        File dir = mContext.getExternalCacheDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);
        return Uri.fromFile(wallpaper);
    }

    public void findWallpaperStatus() {
        File wallpaper = new File(mContext.getFilesDir(), WALLPAPER_NAME);
        mWallpaperAlpha.setEnabled(wallpaper.exists() ? true : false);
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
                        if (wallpaperStream != null)
                            wallpaperStream.close();
                    } catch (IOException e) {
                        // let it go
                    }
                }
                findWallpaperStatus();
                Helpers.restartSystemUI();
            } else if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                if (data==null) {
                    //Nothing returned by user, probably pressed back button in file manager
                    return;
                }
                mBootAnimationPath = data.getData().getPath();
                openBootAnimationDialog();
            }
        }
    }

    private void openBootAnimationDialog() {
        resetSwaggedOutBootAnimation();
        Log.e(TAG, "boot animation path: " + mBootAnimationPath);
        if(mCustomBootAnimationDialog != null) {
            mCustomBootAnimationDialog.cancel();
            mCustomBootAnimationDialog = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.bootanimation_preview);
        if (!mBootAnimationPath.isEmpty()
                && (!BOOTANIMATION_SYSTEM_PATH.equalsIgnoreCase(mBootAnimationPath)
                && !BOOTANIMATION_USER_PATH.equalsIgnoreCase(mBootAnimationPath))) {
            builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    installBootAnim(dialog, mBootAnimationPath);
                    resetBootAnimation();
                }
            });
        } else if (new File(BOOTANIMATION_USER_PATH).exists()) {
            builder.setPositiveButton(R.string.clear_custom_bootanimation, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AbstractAsyncSuCMDProcessor() {
                        @Override
                        protected void onPostExecute(String result) {
                            resetBootAnimation();
                        }
                    }.execute("rm '" + BOOTANIMATION_USER_PATH + "'", "rm '/data/media/bootanimation.backup'");
                }
            });
        }
        builder.setNeutralButton(R.string.set_custom_bootanimation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PackageManager packageManager = getActivity().getPackageManager();
                Intent test = new Intent(Intent.ACTION_GET_CONTENT);
                test.setType("file/*");
                List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                        PackageManager.GET_ACTIVITIES);
                if (!list.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                    intent.setType("file/*");
                    startActivityForResult(intent, REQUEST_PICK_BOOT_ANIMATION);
                } else {
                    //No app installed to handle the intent - file explorer required
                    Toast.makeText(mContext, R.string.install_file_manager_error,
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton(com.android.internal.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        resetBootAnimation();
                        dialog.dismiss();
                    }
                });
        LayoutInflater inflater =
                (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_bootanimation_preview,
                (ViewGroup) getActivity()
                        .findViewById(R.id.bootanimation_layout_root));
        mError = (TextView) layout.findViewById(R.id.textViewError);
        mView = (ImageView) layout.findViewById(R.id.imageViewPreview);
        mView.setVisibility(View.GONE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mView.setLayoutParams(new LinearLayout.LayoutParams(size.x / 2, size.y / 2));
        mError.setText(R.string.creating_preview);
        builder.setView(layout);
        mCustomBootAnimationDialog = builder.create();
        mCustomBootAnimationDialog.setOwnerActivity(getActivity());
        mCustomBootAnimationDialog.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                createPreview(mBootAnimationPath);
            }
        });
        thread.start();
    }

    public void copy(File src, File dst) throws IOException {
        // use file channels for faster byte transfers
        FileChannel inChannel = new
                FileInputStream(src).getChannel();
        FileChannel outChannel = new
                FileOutputStream(dst).getChannel();
        try {
            // move the bytes from in to out
            inChannel.transferTo(0,
                    inChannel.size(),
                    outChannel);
        } finally {
            // ensure closure
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
        }
    }

    private void createPreview(String path) {
        File zip = new File(path);
        ZipFile zipfile = null;
        String desc = "";
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            zipfile = new ZipFile(zip);
            ZipEntry ze = zipfile.getEntry("desc.txt");
            inputStream = zipfile.getInputStream(ze);
            inputStreamReader = new InputStreamReader(inputStream);
            StringBuilder sb = new StringBuilder(0);
            bufferedReader = new BufferedReader(inputStreamReader);
            String read = bufferedReader.readLine();
            while (read != null) {
                sb.append(read);
                sb.append('\n');
                read = bufferedReader.readLine();
            }
            desc = sb.toString();
        } catch (Exception handleAllException) {
            mErrormsg = getActivity().getString(R.string.error_reading_zip_file);
            errorHandler.sendEmptyMessage(0);
            return;
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                // we tried
            }
            try {
                if (inputStreamReader != null)
                    inputStreamReader.close();
            } catch (IOException e) {
                // we tried
            }
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                // moving on...
            }
        }

        String[] info = desc.replace("\\r", "").split("\\n");
        // ignore first two ints height and width
        int delay = Integer.parseInt(info[0].split(" ")[2]);
        String partName1 = info[1].split(" ")[3];
        String partName2;
        try {
            if (info.length > 2) {
                partName2 = info[2].split(" ")[3];
            }
            else {
                partName2 = "";
            }
        } catch (Exception e) {
            partName2 = "";
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 4;
        mAnimationPart1 = new AnimationDrawable();
        mAnimationPart2 = new AnimationDrawable();
        try {
            for (Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
                    enumeration.hasMoreElements();) {
                ZipEntry entry = enumeration.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String partname = entry.getName().split("/")[0];
                if (partName1.equalsIgnoreCase(partname)) {
                    InputStream partOneInStream = null;
                    try {
                        partOneInStream = zipfile.getInputStream(entry);
                        mAnimationPart1.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partOneInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partOneInStream != null)
                            partOneInStream.close();
                    }
                } else if (partName2.equalsIgnoreCase(partname)) {
                    InputStream partTwoInStream = null;
                    try {
                        partTwoInStream = zipfile.getInputStream(entry);
                        mAnimationPart2.addFrame(new BitmapDrawable(getResources(),
                                BitmapFactory.decodeStream(partTwoInStream,
                                        null, opt)), delay);
                    } finally {
                        if (partTwoInStream != null)
                            partTwoInStream.close();
                    }
                }
            }
        } catch (IOException e1) {
            mErrormsg = getActivity().getString(R.string.error_creating_preview);
            errorHandler.sendEmptyMessage(0);
            return;
        }

        if (!partName2.isEmpty()) {
            Log.d(TAG, "Multipart Animation");
            mAnimationPart1.setOneShot(false);
            mAnimationPart2.setOneShot(false);
            mAnimationPart1.setOnAnimationFinishedListener(
                    new AnimationDrawable.OnAnimationFinishedListener() {
                @Override
                public void onAnimationFinished() {
                    Log.d(TAG, "First part finished");
                    mView.setImageDrawable(mAnimationPart2);
                    mAnimationPart1.stop();
                    mAnimationPart2.start();
                }
            });
        } else {
            mAnimationPart1.setOneShot(false);
        }
        finishedHandler.sendEmptyMessage(0);
    }

    /**
     * creates a couple commands to perform all root
     * operations needed to disable/enable bootanimations
     *
     * @param checked state of CheckBox
     * @return script to turn bootanimations on/off
     */
    private String[] getBootAnimationCommand(boolean checked) {
        String[] cmds = new String[3];
        String storedLocation = "/system/media/bootanimation.backup";
        String storedUserLocation = "/data/local/bootanimation.backup";
        String activeLocation = "/system/media/bootanimation.zip";
        String activeUserLocation = "/data/local/bootanimation.zip";
        if (checked) {
            /* make backup */
            cmds[0] = "mv " + activeLocation + ' ' + storedLocation + "; ";
            cmds[1] = "mv " + activeUserLocation + ' ' + storedUserLocation + "; ";
        } else {
            /* apply backup */
            cmds[0] = "mv " + storedLocation + ' ' + activeLocation + "; ";
            cmds[1] = "mv " + activeUserLocation + ' ' + storedUserLocation + "; ";
        }
        /*
         * use sed to replace build.prop property
         * debug.sf.nobootanimation=[1|0]
         *
         * without we get the Android shine animation when
         * /system/media/bootanimation.zip is not found
         */
        cmds[2] = "busybox sed -i \"/debug.sf.nobootanimation/ c "
                + "debug.sf.nobootanimation=" + (checked ? 1 : 0)
                + "\" " + "/system/build.prop";
        return cmds;
    }

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mView.setVisibility(View.GONE);
            mError.setText(mErrormsg);
        }
    };

    private Handler finishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mView.setImageDrawable(mAnimationPart1);
            mView.setVisibility(View.VISIBLE);
            mError.setVisibility(View.GONE);
            mAnimationPart1.start();
        }
    };

    private void installBootAnim(DialogInterface dialog, String bootAnimationPath) {
        //Update setting to reflect that boot animation is now enabled
        mDisableBootAnimation.setChecked(false);
        DisableBootAnimation();
        dialog.dismiss();
        new AbstractAsyncSuCMDProcessor() {
          @Override
          protected void onPostExecute(String result) {
          }
        }.execute("cp " + bootAnimationPath + " /data/local/bootanimation.zip",
                  "chmod 644 /data/local/bootanimation.zip");
    }

    private void DisableBootAnimation() {
        resetSwaggedOutBootAnimation();
        if (!CMDProcessor.runSuCommand(
                "grep -q \"debug.sf.nobootanimation\" /system/build.prop")
                .success()) {
            // if not add value
            Helpers.getMount("rw");
            CMDProcessor.runSuCommand(String.format("echo debug.sf.nobootanimation=%d >> /system/build.prop",
                    mDisableBootAnimation.isChecked() ? 1 : 0));
            Helpers.getMount("ro");
        }
        // preform bootanimation operations off UI thread
        AbstractAsyncSuCMDProcessor processor = new AbstractAsyncSuCMDProcessor(true) {
            @Override
            protected void onPostExecute(String result) {
                if (mDisableBootAnimation.isChecked()) {
                    // do not show same insult as last time
                    int newInsult = mRandomGenerator.nextInt(mInsults.length);
                    while (newInsult == mLastRandomInsultIndex)
                        newInsult = mRandomGenerator.nextInt(mInsults.length);

                    // update our static index reference
                    mLastRandomInsultIndex = newInsult;
                    mDisableBootAnimation.setSummary(mInsults[newInsult]);
                } else {
                    mDisableBootAnimation.setSummary(null);
                }
                resetBootAnimation();
            }
        };
        processor.execute(getBootAnimationCommand(mDisableBootAnimation.isChecked()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mUserModeUI) {
            int val = Integer.valueOf((String) newValue);
            Settings.System.putInt(mContentResolver,
                    Settings.System.USER_UI_MODE, val);
            mStatusbarSliderPreference.setEnabled(val == 1 ? false : true);
            mStatusBarHide.setEnabled(val == 1 ? false : true);
            mHideExtras.setEnabled(val == 1 ? true : false);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mCrtMode) {
            int crtMode = Integer.valueOf((String) newValue);
            int index = mCrtMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEM_POWER_CRT_MODE, crtMode);
            mCrtMode.setSummary(mCrtMode.getEntries()[index]);
            return true;
        } else if (preference == mFontsize) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.STATUSBAR_FONT_SIZE, val);
            Helpers.restartSystemUI();
            return true;
        }
        return false;
    }

    public static class AdvancedTransparencyDialog extends DialogFragment {
        private static final int KEYGUARD_ALPHA = 112;

        private static final int STATUSBAR_ALPHA = 0;
        private static final int STATUSBAR_KG_ALPHA = 1;
        private static final int NAVBAR_ALPHA = 2;
        private static final int NAVBAR_KG_ALPHA = 3;
        boolean linkTransparencies = true;

        CheckBox mLinkCheckBox, mMatchStatusbarKeyguard, mMatchNavbarKeyguard;
        ViewGroup mNavigationBarGroup;
        TextView mSbLabel;
        AlphaSeekBar mSeekBars[] = new AlphaSeekBar[4];

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setShowsDialog(true);
            setRetainInstance(true);
            linkTransparencies = getSavedLinkedState();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View layout = View.inflate(getActivity(), R.layout.dialog_transparency, null);
            mLinkCheckBox = (CheckBox) layout.findViewById(R.id.transparency_linked);
            mLinkCheckBox.setChecked(linkTransparencies);
            mNavigationBarGroup = (ViewGroup) layout.findViewById(R.id.navbar_layout);
            mSbLabel = (TextView) layout.findViewById(R.id.statusbar_label);
            mSeekBars[STATUSBAR_ALPHA] = (AlphaSeekBar) layout.findViewById(R.id.statusbar_alpha);
            mSeekBars[STATUSBAR_KG_ALPHA] = (AlphaSeekBar) layout
                    .findViewById(R.id.statusbar_keyguard_alpha);
            mSeekBars[NAVBAR_ALPHA] = (AlphaSeekBar) layout.findViewById(R.id.navbar_alpha);
            mSeekBars[NAVBAR_KG_ALPHA] = (AlphaSeekBar) layout
                    .findViewById(R.id.navbar_keyguard_alpha);
            mMatchStatusbarKeyguard = (CheckBox) layout.findViewById(R.id.statusbar_match_keyguard);
            mMatchNavbarKeyguard = (CheckBox) layout.findViewById(R.id.navbar_match_keyguard);

            try {
                // restore any saved settings
                int alphas[] = new int[2];
                ContentResolver resolver = getActivity().getContentResolver();
                String sbConfig = Settings.System.getString(resolver,
                        Settings.System.STATUS_BAR_ALPHA_CONFIG);
                if (sbConfig != null) {
                    String split[] = sbConfig.split(";");
                    alphas[0] = Integer.parseInt(split[0]);
                    alphas[1] = Integer.parseInt(split[1]);
                    mSeekBars[STATUSBAR_ALPHA].setCurrentAlpha(alphas[0]);
                    mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                    mMatchStatusbarKeyguard.setChecked(alphas[1] == KEYGUARD_ALPHA);
                    if (linkTransparencies) {
                        mSeekBars[NAVBAR_ALPHA].setCurrentAlpha(alphas[0]);
                        mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                    } else {
                        String navConfig = Settings.System.getString(resolver,
                                Settings.System.NAVIGATION_BAR_ALPHA_CONFIG);
                        if (navConfig != null) {
                            split = navConfig.split(";");
                            alphas[0] = Integer.parseInt(split[0]);
                            alphas[1] = Integer.parseInt(split[1]);
                            mSeekBars[NAVBAR_ALPHA].setCurrentAlpha(alphas[0]);
                            mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                            mMatchNavbarKeyguard.setChecked(alphas[1] == KEYGUARD_ALPHA);
                        }
                    }
                }
            } catch (Exception e) {
                resetSettings();
            }

            updateToggleState();
            mMatchStatusbarKeyguard.setOnCheckedChangeListener(mUpdateStatesListener);
            mMatchNavbarKeyguard.setOnCheckedChangeListener(mUpdateStatesListener);
            mLinkCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    linkTransparencies = isChecked;
                    saveSavedLinkedState(isChecked);
                    updateToggleState();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(layout);
            builder.setTitle(getString(R.string.transparency_dialog_title));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (linkTransparencies) {
                        String config = mSeekBars[STATUSBAR_ALPHA].getCurrentAlpha() + ";" +
                                mSeekBars[STATUSBAR_KG_ALPHA].getCurrentAlpha();
                        Settings.System.putString(mContentResolver,
                                Settings.System.STATUS_BAR_ALPHA_CONFIG, config);
                        Settings.System.putString(mContentResolver,
                                Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, config);
                    } else {
                        String sbConfig = mSeekBars[STATUSBAR_ALPHA].getCurrentAlpha() + ";" +
                                mSeekBars[STATUSBAR_KG_ALPHA].getCurrentAlpha();
                        Settings.System.putString(mContentResolver,
                                Settings.System.STATUS_BAR_ALPHA_CONFIG, sbConfig);

                        String nbConfig = mSeekBars[NAVBAR_ALPHA].getCurrentAlpha() + ";" +
                                mSeekBars[NAVBAR_KG_ALPHA].getCurrentAlpha();
                        Settings.System.putString(mContentResolver,
                                Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, nbConfig);
                    }
                }
            });
            return builder.create();
        }

        private void resetSettings() {
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_ALPHA_CONFIG, null);
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, null);
        }

        private void updateToggleState() {
            if (linkTransparencies) {
                mSbLabel.setText(R.string.transparency_dialog_transparency_sb_and_nv);
                mNavigationBarGroup.setVisibility(View.GONE);
            } else {
                mSbLabel.setText(R.string.transparency_dialog_statusbar);
                mNavigationBarGroup.setVisibility(View.VISIBLE);
            }

            mSeekBars[STATUSBAR_KG_ALPHA]
                    .setEnabled(!mMatchStatusbarKeyguard.isChecked());
            mSeekBars[NAVBAR_KG_ALPHA]
                    .setEnabled(!mMatchNavbarKeyguard.isChecked());

            // disable keyguard alpha if needed
            if (!mSeekBars[STATUSBAR_KG_ALPHA].isEnabled()) {
                mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(KEYGUARD_ALPHA);
            }
            if (!mSeekBars[NAVBAR_KG_ALPHA].isEnabled()) {
                mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(KEYGUARD_ALPHA);
            }
        }

        @Override
        public void onDestroyView() {
            if (getDialog() != null && getRetainInstance())
                getDialog().setDismissMessage(null);
            super.onDestroyView();
        }

        private CompoundButton.OnCheckedChangeListener mUpdateStatesListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateToggleState();
            }
        };

        private boolean getSavedLinkedState() {
            return getActivity().getSharedPreferences("transparency", Context.MODE_PRIVATE)
                    .getBoolean("link", true);
        }

        private void saveSavedLinkedState(boolean v) {
            getActivity().getSharedPreferences("transparency", Context.MODE_PRIVATE).edit()
                    .putBoolean("link", v).commit();
        }
    }
}
