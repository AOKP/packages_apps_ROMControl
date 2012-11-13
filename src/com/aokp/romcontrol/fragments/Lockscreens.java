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
import com.aokp.romcontrol.fragments.LockscreenTargets;
import com.aokp.romcontrol.weather.WeatherRefreshService;
import com.aokp.romcontrol.weather.WeatherService;
import com.aokp.romcontrol.widgets.SeekBarPreference;

public class Lockscreens extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "Lockscreens";
    private static final boolean DEBUG = true;

    private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";
    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";
    private static final String PREF_LOCKSCREEN_MENU_UNLOCK = "lockscreen_menu_unlock";
    private static final String PREF_VOLUME_ROCKER_WAKE = "volume_rocker_wake";
    private static final String PREF_LOCKSCREEN_WEATHER = "lockscreen_weather";
    private static final String PREF_LOCKSCREEN_WEATHER_TYPE = "lockscreen_weather_type";
    private static final String PREF_LOCKSCREEN_CALENDAR = "enable_calendar";
    private static final String PREF_LOCKSCREEN_CALENDAR_FLIP = "lockscreen_calendar_flip";
    private static final String PREF_LOCKSCREEN_CALENDAR_SOURCES = "lockscreen_calendar_sources";
    private static final String PREF_LOCKSCREEN_CALENDAR_RANGE = "lockscreen_calendar_range";
    private static final String PREF_LOCKSCREEN_CALENDAR_HIDE_ONGOING = "lockscreen_calendar_hide_ongoing";
    private static final String PREF_LOCKSCREEN_CALENDAR_USE_COLORS = "lockscreen_calendar_use_colors";
    private static final String PREF_LOCKSCREEN_CALENDAR_INTERVAL = "lockscreen_calendar_interval";
    private static final String PREF_NUMBER_OF_TARGETS = "number_of_targets";
    private static final String PREF_VOLUME_MUSIC = "volume_music_controls";
    private static final String PREF_LOCKSCREEN_AUTO_ROTATE = "lockscreen_auto_rotate";
    private static final String PREF_STOCK_MUSIC_LAYOUT = "lockscreen_stock_music_layout";
    private static final String PREF_CIRCLES_LOCK_BG_COLOR = "circles_lock_bg_color";
    private static final String PREF_CIRCLES_LOCK_RING_COLOR = "circles_lock_ring_color";
    private static final String PREF_CIRCLES_LOCK_HALO_COLOR = "circles_lock_halo_color";
    private static final String PREF_CIRCLES_LOCK_WAVE_COLOR = "circles_lock_wave_color";
    private static final String PREF_CIRCLES_LOCK_RING_ALPHA = "circles_lock_ring_alpha";
    private static final String PREF_CIRCLES_LOCK_HALO_ALPHA = "circles_lock_halo_alpha";
    private static final String PREF_CIRCLES_LOCK_WAVE_ALPHA = "circles_lock_wave_alpha";

    public static final int REQUEST_PICK_WALLPAPER = 199;
    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int SELECT_ACTIVITY = 2;
    public static final int SELECT_WALLPAPER = 3;

    private static final String WALLPAPER_NAME = "lockscreen_wallpaper.jpg";

    Preference mLockscreenWallpaper;
    Preference mLockscreenTargets;

    CheckBoxPreference mLockscreenBattery;
    ColorPickerPreference mLockscreenTextColor;
    CheckBoxPreference mLockscreenMenuUnlock;
    CheckBoxPreference mVolumeMusic;
    CheckBoxPreference mVolumeRockerWake;
    CheckBoxPreference mLockscreenWeather;
    ListPreference mLockscreenWeatherType;
    CheckBoxPreference mLockscreenCalendar;
    CheckBoxPreference mLockscreenCalendarFlip;
    Preference mCalendarSources;
    ListPreference mCalendarInterval;
    ListPreference mCalendarRange;
    CheckBoxPreference mLockscreenCalendarHideOngoing;
    CheckBoxPreference mLockscreenCalendarUseColors;
    CheckBoxPreference mLockscreenAutoRotate;
    CheckBoxPreference mStockMusicLayout;
    ColorPickerPreference mCirclesLockBgColor;
    ColorPickerPreference mCirclesLockRingColor;
    ColorPickerPreference mCirclesLockHaloColor;
    ColorPickerPreference mCirclesLockWaveColor;
    SeekBarPreference mCirclesRingAlpha;
    SeekBarPreference mCirclesHaloAlpha;
    SeekBarPreference mCirclesWaveAlpha;

    ListPreference mTargetNumber;

    ArrayList<String> keys = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_lockscreens);
        keys.add(Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_lockscreens);

        mLockscreenAutoRotate = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_AUTO_ROTATE);
        mLockscreenAutoRotate.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.LOCKSCREEN_AUTO_ROTATE, false));

        mLockscreenBattery = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_BATTERY);
        mLockscreenBattery.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY, false));

        mLockscreenMenuUnlock = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_MENU_UNLOCK);
        mLockscreenMenuUnlock.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_MENU_UNLOCK, 0) == 1);

        mVolumeRockerWake = (CheckBoxPreference) findPreference(PREF_VOLUME_ROCKER_WAKE);
        mVolumeRockerWake.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.VOLUME_WAKE_SCREEN, false));

        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);

        mLockscreenWeather = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_WEATHER);
        mLockscreenWeather.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_WEATHER, false));

        mLockscreenWeatherType = (ListPreference) findPreference(PREF_LOCKSCREEN_WEATHER_TYPE);
        mLockscreenWeatherType.setOnPreferenceChangeListener(this);
        mLockscreenWeatherType.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_WEATHER_TYPE, 0) + "");

        mLockscreenCalendar = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR);
        mLockscreenCalendar.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR, false));

        mLockscreenCalendarFlip = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_FLIP);
        mLockscreenCalendarFlip.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_CALENDAR_FLIP, false));

        mLockscreenCalendarHideOngoing = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_HIDE_ONGOING);
        mLockscreenCalendarHideOngoing.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_CALENDAR_HIDE_ONGOING, false));

        mLockscreenCalendarUseColors = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_USE_COLORS);
        mLockscreenCalendarUseColors.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_CALENDAR_USE_COLORS, false));

        mCalendarSources = findPreference(PREF_LOCKSCREEN_CALENDAR_SOURCES);

        mCalendarInterval = (ListPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_INTERVAL);
        mCalendarInterval.setOnPreferenceChangeListener(this);
        mCalendarInterval.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_INTERVAL, 2500) + "");

        mCalendarRange = (ListPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_RANGE);
        mCalendarRange.setOnPreferenceChangeListener(this);
        mCalendarRange.setValue(Settings.System.getLong(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_RANGE, 86400000) + "");

        mTargetNumber = (ListPreference) findPreference(PREF_NUMBER_OF_TARGETS);
        mTargetNumber.setOnPreferenceChangeListener(this);
        mTargetNumber.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_TARGET_AMOUNT
                ,2)));

        mLockscreenTargets = findPreference("lockscreen_targets");

        mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOLUME_MUSIC_CONTROLS, 0) == 1);

        mStockMusicLayout = (CheckBoxPreference) findPreference(PREF_STOCK_MUSIC_LAYOUT);
        mStockMusicLayout.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_STOCK_MUSIC_LAYOUT, 0) == 1);

        mCirclesLockBgColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_BG_COLOR);
        mCirclesLockBgColor.setOnPreferenceChangeListener(this);

        mCirclesLockRingColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_RING_COLOR);
        mCirclesLockRingColor.setOnPreferenceChangeListener(this);

        mCirclesLockHaloColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_HALO_COLOR);
        mCirclesLockHaloColor.setOnPreferenceChangeListener(this);

        mCirclesLockWaveColor = (ColorPickerPreference) findPreference(PREF_CIRCLES_LOCK_WAVE_COLOR);
        mCirclesLockWaveColor.setOnPreferenceChangeListener(this);

        float ringAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(),
                Settings.System.CIRCLES_LOCK_RING_ALPHA, 1.0f);
        mCirclesRingAlpha = (SeekBarPreference) findPreference(PREF_CIRCLES_LOCK_RING_ALPHA);
        mCirclesRingAlpha.setInitValue((int) (ringAlpha * 100));
        mCirclesRingAlpha.setOnPreferenceChangeListener(this);

        float haloAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(),
                Settings.System.CIRCLES_LOCK_HALO_ALPHA, 1.0f);
        mCirclesHaloAlpha = (SeekBarPreference) findPreference(PREF_CIRCLES_LOCK_HALO_ALPHA);
        mCirclesHaloAlpha.setInitValue((int) (haloAlpha * 100));
        mCirclesHaloAlpha.setOnPreferenceChangeListener(this);

        float waveAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(),
                Settings.System.CIRCLES_LOCK_WAVE_ALPHA, 0.15f);
        mCirclesWaveAlpha = (SeekBarPreference) findPreference(PREF_CIRCLES_LOCK_WAVE_ALPHA);
        mCirclesWaveAlpha.setInitValue((int) (waveAlpha * 100));
        mCirclesWaveAlpha.setOnPreferenceChangeListener(this);

        mLockscreenWallpaper = findPreference("wallpaper");

        for (String key : keys) {
            try {
                ((CheckBoxPreference) findPreference(key)).setChecked(Settings.System.getInt(
                        getActivity().getContentResolver(), key) == 1);
             } catch (SettingNotFoundException e) {
             }
        }

        boolean circlesEnabled = Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.USE_CIRCLES_LOCKSCREEN, false);

        if (circlesEnabled) {
            PreferenceCategory targetsCategory = (PreferenceCategory) findPreference("targets");
            getPreferenceScreen().removePreference(targetsCategory);
            PreferenceCategory musicCat = (PreferenceCategory) findPreference ("music");
            musicCat.removePreference(mStockMusicLayout);
        }
        if (!circlesEnabled) {
            PreferenceCategory circlesCategory = (PreferenceCategory) findPreference("circles_lockscreen");
            getPreferenceScreen().removePreference(circlesCategory);
        }


        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockscreenBattery) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_BATTERY,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenAutoRotate) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.LOCKSCREEN_AUTO_ROTATE,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenMenuUnlock) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_MENU_UNLOCK,
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mVolumeRockerWake) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.VOLUME_WAKE_SCREEN,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLockscreenTargets) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            LockscreenTargets fragment = new LockscreenTargets();
            ft.addToBackStack("lockscreen_targets");
            ft.replace(this.getId(), fragment);
            ft.commit();
            Intent w = new Intent(getActivity().getApplicationContext(),
                    WeatherRefreshService.class);
            w.setAction(WeatherService.INTENT_WEATHER_REQUEST);
            w.putExtra(WeatherService.INTENT_EXTRA_ISMANUAL, true);
            getActivity().getApplicationContext().startService(w);
            return true;
        } else if (preference == mVolumeMusic) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_MUSIC_CONTROLS,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenWeather) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_WEATHER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenCalendar) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenCalendarFlip) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR_FLIP,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenCalendarHideOngoing) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR_HIDE_ONGOING,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mLockscreenCalendarUseColors) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR_USE_COLORS,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mCalendarSources) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

            ArrayList<Integer> enabledCalendars = getCalendarSources(this.getActivity()
                    .getApplicationContext());

            final ArrayList<CalendarBundle> availableCalendars = getAvailableCalendars(this
                    .getActivity().getApplicationContext());

            boolean checkedCalendars[] = new boolean[availableCalendars.size()];

            for (int i = 0; i < checkedCalendars.length; i++) {
                if (enabledCalendars.contains((int) availableCalendars.get(i).ID)) {
                    checkedCalendars[i] = true;
                }
            }

            builder.setTitle(getResources().getString(R.string.lockscreen_calendar_dialog));
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.lockscreen_calendar_close), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            final String[] availableCalendarStrings = new String[availableCalendars.size()];

            for (int i = 0; i < availableCalendars.size(); i++) {
                availableCalendarStrings[i] = availableCalendars.get(i).name;
            }

            builder.setMultiChoiceItems(availableCalendarStrings, checkedCalendars,
                    new OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            int toggleKey = (int) availableCalendars.get(which).ID;

                            if (isChecked)
                                addCalendar(getActivity(), toggleKey);
                            else
                                removeCalendar(getActivity(), toggleKey);
                        }
                    });

            AlertDialog d = builder.create();

            d.show();

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
        } else if (preference == mStockMusicLayout) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_STOCK_MUSIC_LAYOUT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (keys.contains(preference.getKey())) {
            Log.e("RC_Lockscreens", "key: " + preference.getKey());
            return Settings.System.putInt(getActivity().getContentResolver(), preference.getKey(),
                    ((CheckBoxPreference)preference).isChecked() ? 1 : 0);
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
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


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean handled = false;
        if (preference == mLockscreenTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, intHex);
            if (DEBUG) Log.d(TAG, String.format("new color hex value: %d", intHex));
        } else if (preference == mLockscreenWeatherType) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_WEATHER_TYPE, val);
            return true;
        } else if (preference == mCalendarInterval) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR_INTERVAL, val);
            return true;
        } else if (preference == mCalendarRange) {
            long val = Long.parseLong((String) newValue);
            Settings.System.putLong(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CALENDAR_RANGE, val);
            return true;
        } else if (preference == mTargetNumber) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_TARGET_AMOUNT, val);
            return true;
        } else if (preference == mCirclesLockBgColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_BG_COLOR, intHex);
            return true;
        } else if (preference == mCirclesLockRingColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_RING_COLOR, intHex);
            return true;
        } else if (preference == mCirclesLockHaloColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_HALO_COLOR, intHex);
            return true;
        } else if (preference == mCirclesLockWaveColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_WAVE_COLOR, intHex);
            return true;
        } else if (preference == mCirclesRingAlpha) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_RING_ALPHA, val / 100);
            return true;
        } else if (preference == mCirclesHaloAlpha) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_HALO_ALPHA, val / 100);
            return true;
        } else if (preference == mCirclesWaveAlpha) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.CIRCLES_LOCK_WAVE_ALPHA, val / 100);
            return true;
        }
        return false;
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

    public static void setCalendarSources(Context c, ArrayList<Integer> calendars) {
        String result = "";
        if (calendars.size() > 0) {
            for (int i : calendars)
                result += String.valueOf(i) + ",";

            result = result.substring(0, result.length() - 1);
        }
        Settings.System.putString(c.getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_SOURCES, result);
    }

    public static ArrayList<Integer> getCalendarSources(Context c) {
        String calString = Settings.System.getString(c.getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_SOURCES);
        ArrayList<Integer> result = new ArrayList<Integer>();

        if (calString == null) {
            calString = "";
        }
        if (!calString.isEmpty()) {
            String[] calArray = calString.split(",");
            for (String s : calArray) {
                result.add(Integer.parseInt(s));
            }
        }
        return result;
    }

    public static ArrayList<CalendarBundle> getAvailableCalendars(Context c) {
        ArrayList<CalendarBundle> result = new ArrayList<CalendarBundle>();
        final String[] PROJECTION = new String[] {
                Calendars._ID, Calendars.CALENDAR_DISPLAY_NAME
        };

        Cursor cur = null;
        ContentResolver cr = c.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;

        cur = cr.query(uri, PROJECTION, null, null, null);

        while (cur.moveToNext()) {
            result.add(new CalendarBundle(cur.getLong(0), cur.getString(1)));
        }
        return result;
    }

    public static void addCalendar(Context context, int key) {
        ArrayList<Integer> enabledCalendars = getCalendarSources(context);
        enabledCalendars.add(key);
        setCalendarSources(context, enabledCalendars);
    }

    public static void removeCalendar(Context context, int key) {
        ArrayList<Integer> enabledCalendars = getCalendarSources(context);
        int keyLocation = enabledCalendars.indexOf(key);
        enabledCalendars.remove(keyLocation);
        setCalendarSources(context, enabledCalendars);
    }

    private static class CalendarBundle {
        public long ID;
        public String name;

        public CalendarBundle(long id, String n) {
            ID = id;
            name = n;
        }
    }
}

