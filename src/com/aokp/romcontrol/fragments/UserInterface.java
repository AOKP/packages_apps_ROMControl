
package com.aokp.romcontrol.fragments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.AbstractAsyncSuCMDProcessor;
import com.aokp.romcontrol.util.Helpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.StringBuilder;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UserInterface extends AOKPPreferenceFragment {

    public static final String TAG = "UserInterface";

    private static final String PREF_180 = "rotate_180";
    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
    private static final String PREF_NOTIFICATION_WALLPAPER_ALPHA = "notification_wallpaper_alpha";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_IME_SWITCHER = "ime_switcher";
    private static final String PREF_LEFTY_MODE = "lefty_mode";
    private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";
    private static final String PREF_RAM_USAGE_BAR = "ram_usage_bar";
    private static final String PREF_KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String PREF_MODE_TABLET_UI = "mode_tabletui";
    private static final String PREF_HIDE_EXTRAS = "hide_extras";
    private static final String PREF_FORCE_DUAL_PANEL = "force_dualpanel";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String PREF_SHOW_OVERFLOW = "show_overflow";
    private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";
    private static final String PREF_NOTIFICATION_SHOW_WIFI_SSID = "notification_show_wifi_ssid";
    private static final int REQUEST_PICK_WALLPAPER = 201;
    private static final int REQUEST_PICK_CUSTOM_ICON = 202;
    private static final int REQUEST_PICK_BOOT_ANIMATION = 203;
    private static final int SELECT_ACTIVITY = 4;
    private static final int SELECT_WALLPAPER = 5;

    private static final String WALLPAPER_NAME = "notification_wallpaper.jpg";

    CheckBoxPreference mAllow180Rotation;
    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mStatusBarNotifCount;
    Preference mNotificationWallpaper;
    Preference mCustomBootAnimation;
    Preference mWallpaperAlpha;
    Preference mCustomLabel;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mRecentKillAll;
    CheckBoxPreference mRamBar;
    CheckBoxPreference mKillAppLongpressBack;
    CheckBoxPreference mUseAltResolver;
    CheckBoxPreference mLeftyMode;
    ImageView view;
    TextView error;
    CheckBoxPreference mShowActionOverflow;
    CheckBoxPreference mTabletui;
    CheckBoxPreference mHideExtras;
    CheckBoxPreference mDualpane;
    CheckBoxPreference mVibrateOnExpand;
    Preference mLcdDensity;
    CheckBoxPreference mShowWifiName;

    private AnimationDrawable mAnimationPart1;
    private AnimationDrawable mAnimationPart2;
    private String mPartName1;
    private String mPartName2;
    private int delay;
    private int height;
    private int width;
    private String errormsg;
    private String bootAniPath;

    private Random randomGenerator = new Random();
    // previous random; so we don't repeat
    private static int mLastRandomInsultIndex = -1;
    private String[] mInsults;

    private int seekbarProgress;
    String mCustomLabelText = null;

    int newDensityValue;

    DensityChanger densityFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_ui);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        PreferenceScreen prefs = getPreferenceScreen();
        mInsults = mContext.getResources().getStringArray(
                R.array.disable_bootanimation_insults);

        mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
        mAllow180Rotation.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));


        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_NOTIF_COUNT,
                false));

        mDisableBootAnimation = (CheckBoxPreference)findPreference("disable_bootanimation");
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
        if (mDisableBootAnimation.isChecked()) {
            Resources res = mContext.getResources();
            String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
            int randomInt = randomGenerator.nextInt(insults.length);
            mDisableBootAnimation.setSummary(insults[randomInt]);
        }

        mLcdDensity = findPreference("lcd_density_setup");
        String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }

        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + currentProperty);

        mCustomBootAnimation = findPreference("custom_bootanimation");

        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
        mShowImeSwitcher.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.SHOW_STATUSBAR_IME_SWITCHER, true));

        mLeftyMode = (CheckBoxPreference) findPreference(PREF_LEFTY_MODE);
        mLeftyMode.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_LEFTY_MODE, false));

        mRecentKillAll = (CheckBoxPreference) findPreference(PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getBoolean(getActivity  ().getContentResolver(),
                Settings.System.RECENT_KILL_ALL_BUTTON, false));

        mRamBar = (CheckBoxPreference) findPreference(PREF_RAM_USAGE_BAR);
        mRamBar.setChecked(Settings.System.getBoolean(getActivity  ().getContentResolver(),
                Settings.System.RAM_USAGE_BAR, false));

        mKillAppLongpressBack = (CheckBoxPreference) findPreference(PREF_KILL_APP_LONGPRESS_BACK);
                updateKillAppLongpressBackOptions();

        mShowActionOverflow = (CheckBoxPreference) findPreference(PREF_SHOW_OVERFLOW);
        mShowActionOverflow.setChecked((Settings.System.getInt(getActivity().
                        getApplicationContext().getContentResolver(),
                        Settings.System.UI_FORCE_OVERFLOW_BUTTON, 0) == 1));

        mTabletui = (CheckBoxPreference) findPreference(PREF_MODE_TABLET_UI);
        mTabletui.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                        Settings.System.MODE_TABLET_UI, false));

        mHideExtras = (CheckBoxPreference) findPreference(PREF_HIDE_EXTRAS);
        mHideExtras.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                        Settings.System.HIDE_EXTRAS_SYSTEM_BAR, false));

        mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                        Settings.System.ACTIVITY_RESOLVER_USE_ALT, false));

        mDualpane = (CheckBoxPreference) findPreference(PREF_FORCE_DUAL_PANEL);
        mDualpane.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                        Settings.System.FORCE_DUAL_PANEL, getResources().getBoolean(
                        com.android.internal.R.bool.preferences_prefer_dual_pane)));

        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);

        if (hasNavBarByDefault || mTablet) {
            ((PreferenceGroup) findPreference("navbar")).removePreference(mKillAppLongpressBack);
        }

        mNotificationWallpaper = findPreference(PREF_NOTIFICATION_WALLPAPER);

        mWallpaperAlpha = (Preference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);

        mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.VIBRATE_NOTIF_EXPAND, true));

        mShowWifiName = (CheckBoxPreference) findPreference(PREF_NOTIFICATION_SHOW_WIFI_SSID);
        mShowWifiName.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.NOTIFICATION_SHOW_WIFI_SSID, false));

        if (mTablet) {
            prefs.removePreference(mNotificationWallpaper);
            prefs.removePreference(mWallpaperAlpha);
        } else {
            prefs.removePreference(mTabletui);
        }

        setHasOptionsMenu(true);
    }

    private void writeKillAppLongpressBackOptions() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.KILL_APP_LONGPRESS_BACK, mKillAppLongpressBack.isChecked() ? 1 : 0);
    }

    private void updateKillAppLongpressBackOptions() {
        mKillAppLongpressBack.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.KILL_APP_LONGPRESS_BACK, 0) != 0);
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            final Preference preference) {
        if (preference == mAllow180Rotation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, checked ? (1 | 2 | 4 | 8) : (1 | 2 | 8 ));
            return true;
        } else if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mDisableBootAnimation) {
            CMDProcessor term = new CMDProcessor();
            if (!term.su.runWaitFor(
                    "grep -q \"debug.sf.nobootanimation\" /system/build.prop")
                    .success()) {
                // if not add value
                Helpers.getMount("rw");
                term.su.runWaitFor("echo debug.sf.nobootanimation="
                    + String.valueOf(mDisableBootAnimation.isChecked() ? 1 : 0)
                    + " >> /system/build.prop");
                Helpers.getMount("ro");
            }
            // preform bootanimation operations off UI thread
            AbstractAsyncSuCMDProcessor processor = new AbstractAsyncSuCMDProcessor(true) {
                @Override
                protected void onPostExecute(String result) {
                    if (mDisableBootAnimation.isChecked()) {
                        // do not show same insult as last time
                        int newInsult = randomGenerator.nextInt(mInsults.length);
                        while (newInsult == mLastRandomInsultIndex)
                            newInsult = randomGenerator.nextInt(mInsults.length);

                        // update our static index reference
                        mLastRandomInsultIndex = newInsult;
                        preference.setSummary(mInsults[newInsult]);
                    } else {
                        preference.setSummary("");
                    }
                }
            };
            processor.execute(getBootAnimationCommand(mDisableBootAnimation.isChecked()));
            return true;
        } else if (preference == mShowActionOverflow) {
            boolean enabled = mShowActionOverflow.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                    enabled ? 1 : 0);
            // Show toast appropriately
            if (enabled) {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_enable,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.show_overflow_toast_disable,
                        Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (preference == mTabletui) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.MODE_TABLET_UI,
                    ((CheckBoxPreference) preference).isChecked());
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC, 3);
            return true;
        } else if (preference == mHideExtras) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.HIDE_EXTRAS_SYSTEM_BAR,
                    ((CheckBoxPreference) preference).isChecked());
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mDualpane) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.FORCE_DUAL_PANEL,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mCustomBootAnimation) {
            PackageManager packageManager = getActivity().getPackageManager();
            Intent test = new Intent(Intent.ACTION_GET_CONTENT);
            test.setType("file/*");
            List<ResolveInfo> list = packageManager.queryIntentActivities(test, PackageManager.GET_ACTIVITIES);
            if(list.size() > 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("file/*");
                startActivityForResult(intent, REQUEST_PICK_BOOT_ANIMATION);
            } else {
                //No app installed to handle the intent - file explorer required
                Toast.makeText(mContext, R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
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
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getNotificationExternalUri());
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
            return true;
        } else if (preference == mWallpaperAlpha) {
            Resources res = getActivity().getResources();
            String cancel = res.getString(R.string.cancel);
            String ok = res.getString(R.string.ok);
            String title = res.getString(R.string.alpha_dialog_title);
            float savedProgress = Settings.System.getFloat(getActivity()
                        .getContentResolver(), Settings.System.NOTIF_WALLPAPER_ALPHA, 1.0f);

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
                        Settings.System.NOTIF_WALLPAPER_ALPHA, val);
                    Helpers.restartSystemUI();
                }
            })
            .create()
            .show();
            return true;
        } else if (preference == mLeftyMode) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_LEFTY_MODE,
                    isCheckBoxPrefernceChecked(preference));
            Helpers.restartSystemUI();
            return true;
        }else if (preference == mShowImeSwitcher) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SHOW_STATUSBAR_IME_SWITCHER,
                    isCheckBoxPrefernceChecked(preference));
            return true;
        } else if (preference == mRecentKillAll) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.RECENT_KILL_ALL_BUTTON, checked ? true : false);
            return true;
        } else if (preference == mRamBar) {
            boolean checked = ((CheckBoxPreference)preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.RAM_USAGE_BAR, checked ? true : false);
            return true;
        } else if (preference == mKillAppLongpressBack) {
            writeKillAppLongpressBackOptions();
        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);

            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.aokp.romcontrol.LABEL_CHANGED");
                    mContext.sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        } else if (preference == mLcdDensity) {
            ((PreferenceActivity) getActivity())
                    .startPreferenceFragment(new DensityChanger(), true);
            return true;
        } else if (preference == mUseAltResolver) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.ACTIVITY_RESOLVER_USE_ALT,
                    isCheckBoxPrefernceChecked(preference));
            return true;
        } else if (preference == mVibrateOnExpand) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.VIBRATE_NOTIF_EXPAND,
                    ((CheckBoxPreference) preference).isChecked());
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mShowWifiName) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.NOTIFICATION_SHOW_WIFI_SSID,
                    ((CheckBoxPreference) preference).isChecked());
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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.remove_wallpaper:
                File f = new File(mContext.getFilesDir(), WALLPAPER_NAME);
                mContext.deleteFile(WALLPAPER_NAME);
                Helpers.restartSystemUI();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private Uri getNotificationExternalUri() {
        File dir = mContext.getExternalCacheDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);

        return Uri.fromFile(wallpaper);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_WALLPAPER) {

                FileOutputStream wallpaperStream = null;
                try {
                    wallpaperStream = mContext.openFileOutput(WALLPAPER_NAME,
                            Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri selectedImageUri = getNotificationExternalUri();
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                Helpers.restartSystemUI();
            } else if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                if (data==null) {
                    //Nothing returned by user, probably pressed back button in file manager
                    return;
                }

                bootAniPath = data.getData().getEncodedPath();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.bootanimation_preview);
                builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Helpers.getMount("rw");
                        //backup old boot animation
                        new CMDProcessor().su.runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.backup");

                        //Copy new bootanimation, give proper permissions
                        new CMDProcessor().su.runWaitFor("cp "+ bootAniPath +" /system/media/bootanimation.zip");
                        new CMDProcessor().su.runWaitFor("chmod 644 /system/media/bootanimation.zip");

                        //Update setting to reflect that boot animation is now enabled
                        mDisableBootAnimation.setChecked(false);

                        Helpers.getMount("ro");

                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(com.android.internal.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_bootanimation_preview,
                        (ViewGroup) getActivity().findViewById(R.id.bootanimation_layout_root));

                error = (TextView) layout.findViewById(R.id.textViewError);

                view = (ImageView) layout.findViewById(R.id.imageViewPreview);
                view.setVisibility(View.GONE);

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);

                view.setLayoutParams(new LinearLayout.LayoutParams(size.x/2, size.y/2));

                error.setText(R.string.creating_preview);

                builder.setView(layout);

                AlertDialog dialog = builder.create();

                dialog.setOwnerActivity(getActivity());

                dialog.show();

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        createPreview(bootAniPath);
                    }
                });
                thread.start();

            }
        }
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void createPreview(String path) {
        File zip = new File(path);

        ZipFile zipfile = null;
        String desc = "";
        try {
            zipfile = new ZipFile(zip);
            ZipEntry ze = zipfile.getEntry("desc.txt");
            InputStream in = zipfile.getInputStream(ze);
            InputStreamReader is = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(is);
            String read = br.readLine();
            while(read != null) {
                sb.append(read);
                sb.append("\n");
                read = br.readLine();
            }
            desc = sb.toString();
            br.close();
            is.close();
            in.close();

        } catch (Exception e1) {
            errormsg = getActivity().getString(R.string.error_reading_zip_file);
            errorHandler.sendEmptyMessage(0);
            return;
        }

        String[] info = desc.replace("\\r", "").split("\\n");

        width = Integer.parseInt(info[0].split(" ")[0]);
        height = Integer.parseInt(info[0].split(" ")[1]);
        delay = Integer.parseInt(info[0].split(" ")[2]);

        mPartName1 = info[1].split(" ")[3];

        try {
            if (info.length > 2) {
                mPartName2 = info[2].split(" ")[3];
            }
            else {
                mPartName2 = "";
            }
        }
        catch (Exception e)
        {
            mPartName2 = "";
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 4;

        mAnimationPart1 = new AnimationDrawable();
        mAnimationPart2 = new AnimationDrawable();

        try
        {
            for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String partname = entry.getName().split("/")[0];
                if (mPartName1.equalsIgnoreCase(partname)) {
                    InputStream is = zipfile.getInputStream(entry);
                    mAnimationPart1.addFrame(new BitmapDrawable(getResources(), BitmapFactory.decodeStream(is, null, opt)), delay);
                    is.close();
                }
                else if (mPartName2.equalsIgnoreCase(partname)) {
                    InputStream is = zipfile.getInputStream(entry);
                    mAnimationPart2.addFrame(new BitmapDrawable(getResources(), BitmapFactory.decodeStream(is, null, opt)), delay);
                    is.close();
                }
            }
        } catch (IOException e1) {
            errormsg = getActivity().getString(R.string.error_creating_preview);
            errorHandler.sendEmptyMessage(0);
            return;
        }

        if (mPartName2.length() > 0) {
            Log.d(TAG, "Multipart Animation");
            mAnimationPart1.setOneShot(false);
            mAnimationPart2.setOneShot(false);

            mAnimationPart1.setOnAnimationFinishedListener(new AnimationDrawable.OnAnimationFinishedListener() {

                @Override
                public void onAnimationFinished() {
                    Log.d(TAG, "First part finished");
                    view.setImageDrawable(mAnimationPart2);
                    mAnimationPart1.stop();
                    mAnimationPart2.start();
                }
            });

        }
        else {
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
        String[] cmds = new String[2];
        String storedLocation = "/system/media/bootanimation.backup";
        String activeLocation = "/system/media/bootanimation.zip";
        if (checked) {
            /* make backup */
            cmds[0] = "mv " + activeLocation + " " + storedLocation + "; ";
        } else {
            /* apply backup */
            cmds[0] = "mv " + storedLocation + " " + activeLocation + "; ";
        }
        /*
         * use sed to replace build.prop property
         * debug.sf.nobootanimation=[1|0]
         *
         * without we get the Android shine animation when
         * /system/media/bootanimation.zip is not found
         */
        cmds[1] = "busybox sed -i \"/debug.sf.nobootanimation/ c "
                + "debug.sf.nobootanimation=" + String.valueOf(checked ? 1 : 0)
                + "\" " + "/system/build.prop";
        return cmds;
    }

    private Handler errorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            view.setVisibility(View.GONE);
            error.setText(errormsg);
        }
    };

    private Handler finishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            view.setImageDrawable(mAnimationPart1);
            view.setVisibility(View.VISIBLE);
            error.setVisibility(View.GONE);
            mAnimationPart1.start();
        }
    };
}
