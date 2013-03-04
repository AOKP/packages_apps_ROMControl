package com.aokp.romcontrol.fragments;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.CalendarContract.Calendars;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.ROMControlActivity;

public class Lockscreens extends AOKPPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Lockscreens";
    private static final boolean DEBUG = true;

    private static final String PREF_VOLUME_ROCKER_WAKE = "volume_rocker_wake";
    private static final String PREF_VOLUME_MUSIC = "volume_music_controls";
    private static final String PREF_QUICK_UNLOCK = "lockscreen_quick_unlock_control";
    private static final String PREF_LOCKSCREEN_AUTO_ROTATE = "lockscreen_auto_rotate";
    private static final String PREF_LOCKSCREEN_ALL_WIDGETS = "lockscreen_all_widgets";
    private static final String PREF_LOCKSCREEN_UNLIMITED_WIDGETS = "lockscreen_unlimited_widgets";
    private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";
    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";
    private static final String PREF_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS = "lockscreen_hide_initial_page_hints";
    private static final String PREF_LOCKSCREEN_MINIMIZE_CHALLENGE = "lockscreen_minimize_challenge";
    private static final String PREF_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";
    private static final String PREF_LOCKSCREEN_LONGPRESS_CHALLENGE = "lockscreen_longpress_challenge";

    public static final int REQUEST_PICK_WALLPAPER = 199;
    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int SELECT_ACTIVITY = 2;
    public static final int SELECT_WALLPAPER = 3;

    private static final String WALLPAPER_NAME = "lockscreen_wallpaper.jpg";

    Preference mLockscreenWallpaper;
    Preference mLockscreenTargets;

    CheckBoxPreference mVolumeMusic;
    CheckBoxPreference mVolumeRockerWake;
    CheckBoxPreference mQuickUnlock;
    CheckBoxPreference mLockscreenBattery;
    CheckBoxPreference mLockscreenAllWidgets;
    CheckBoxPreference mLockscreenUnlimitedWidgets;
    ColorPickerPreference mLockscreenTextColor;
    CheckBoxPreference mLockscreenAutoRotate;
    CheckBoxPreference mLockscreenHideInitialPageHints;
    CheckBoxPreference mLockscreenMinChallenge;
    CheckBoxPreference mLockscreenUseCarousel;
    CheckBoxPreference mLockscreenLongpressChallenge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_lockscreens);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_lockscreens);

        mVolumeRockerWake = (CheckBoxPreference) findPreference(PREF_VOLUME_ROCKER_WAKE);
        mVolumeRockerWake.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN, false));

        mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.VOLUME_MUSIC_CONTROLS, false));

        mQuickUnlock = (CheckBoxPreference) findPreference(PREF_QUICK_UNLOCK);
        mQuickUnlock.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL, false));

        mLockscreenAutoRotate = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_AUTO_ROTATE);
        mLockscreenAutoRotate.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.LOCKSCREEN_AUTO_ROTATE, false));

        mLockscreenBattery = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_BATTERY);
        mLockscreenBattery.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY, false));

        mLockscreenAllWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_ALL_WIDGETS);
        mLockscreenAllWidgets.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ALL_WIDGETS, false));

        mLockscreenUnlimitedWidgets = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_UNLIMITED_WIDGETS);
        mLockscreenUnlimitedWidgets.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, false));

        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);

        mLockscreenWallpaper = findPreference("wallpaper");

        mLockscreenHideInitialPageHints = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS);
        mLockscreenHideInitialPageHints.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, false));

        mLockscreenMinChallenge = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_MINIMIZE_CHALLENGE);
        mLockscreenMinChallenge.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, false));

        mLockscreenUseCarousel = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_USE_CAROUSEL);
        mLockscreenUseCarousel.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, false));

        mLockscreenLongpressChallenge = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_LONGPRESS_CHALLENGE);
        mLockscreenLongpressChallenge.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE, false));

        if (isSW600DPScreen(mContext)) {
            ((PreferenceGroup)findPreference("layout")).removePreference((Preference)findPreference(PREF_LOCKSCREEN_MINIMIZE_CHALLENGE));
            ((PreferenceGroup)findPreference("layout")).removePreference((Preference)findPreference(PREF_LOCKSCREEN_LONGPRESS_CHALLENGE));
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mVolumeRockerWake) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.VOLUME_WAKE_SCREEN,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mVolumeMusic) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.VOLUME_MUSIC_CONTROLS,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mQuickUnlock) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenAllWidgets) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_ALL_WIDGETS,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenUnlimitedWidgets) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenWallpaper) {
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
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getLockscreenExternalUri());

            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
            return true;
        } else if (preference == mLockscreenBattery) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_BATTERY,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenMinChallenge) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenLongpressChallenge) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_LONGPRESS_CHALLENGE,
                    ((CheckBoxPreference)preference).isChecked());
            return true;
        } else if (preference == mLockscreenAutoRotate) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_AUTO_ROTATE,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenHideInitialPageHints) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenUseCarousel) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean handled = false;
        if (preference == mLockscreenTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.lockscreens, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.remove_wallpaper:
                File f = new File(mContext.getFilesDir(), WALLPAPER_NAME);
                Log.e(TAG, mContext.deleteFile(WALLPAPER_NAME) + "");
                Log.e(TAG, mContext.deleteFile(WALLPAPER_NAME) + "");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private Uri getLockscreenExternalUri() {
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

                Uri selectedImageUri = getLockscreenExternalUri();
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
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

}

