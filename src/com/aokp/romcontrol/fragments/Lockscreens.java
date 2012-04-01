
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.LockscreenItemPreference;
import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class Lockscreens extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private static final String TAG = "Lockscreens";
    private static final boolean DEBUG = true;

    private static final String PREF_MENU = "pref_lockscreen_menu_unlock";
    private static final String PREF_USER_OVERRIDE = "lockscreen_user_timeout_override";
    private static final String PREF_LOCKSCREEN_LAYOUT = "pref_lockscreen_layout";

    private static final String PREF_VOLUME_WAKE = "volume_wake";
    private static final String PREF_VOLUME_MUSIC = "volume_music_controls";

    private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";
    private static final String PREF_LOCKSCREEN_WEATHER = "lockscreen_weather";
    private static final String PREF_LOCKSCREEN_TEXT_COLOR = "lockscreen_text_color";

    private static final String PREF_LOCKSCREEN_CALENDAR = "enable_calendar";
    private static final String PREF_LOCKSCREEN_CALENDAR_FLIP = "lockscreen_calendar_flip";
    private static final String PREF_LOCKSCREEN_CALENDAR_SOURCES = "lockscreen_calendar_sources";
    private static final String PREF_LOCKSCREEN_CALENDAR_RANGE = "lockscreen_calendar_range";
    private static final String PREF_LOCKSCREEN_CALENDAR_HIDE_ONGOING = "lockscreen_calendar_hide_ongoing";
    private static final String PREF_LOCKSCREEN_CALENDAR_USE_COLORS = "lockscreen_calendar_use_colors";
    private static final String PREF_LOCKSCREEN_CALENDAR_INTERVAL = "lockscreen_calendar_interval";

    private static final String PREF_SHOW_LOCK_BEFORE_UNLOCK = "show_lock_before_unlock";

    public static final int REQUEST_PICK_WALLPAPER = 199;
    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int SELECT_ACTIVITY = 2;
    public static final int SELECT_WALLPAPER = 3;

    private static final String WALLPAPER_NAME = "lockscreen_wallpaper.jpg";

    CheckBoxPreference menuButtonLocation;
    CheckBoxPreference mLockScreenTimeoutUserOverride;
    ListPreference mLockscreenOption;
    CheckBoxPreference mVolumeWake;
    CheckBoxPreference mVolumeMusic;
    CheckBoxPreference mLockscreenLandscape;
    CheckBoxPreference mLockscreenBattery;
    CheckBoxPreference mLockscreenWeather;
    CheckBoxPreference mShowLockBeforeUnlock;
    ColorPickerPreference mLockscreenTextColor;
    CheckBoxPreference mLockscreenCalendar;
    CheckBoxPreference mLockscreenCalendarFlip;
    Preference mCalendarSources;
    ListPreference mCalendarInterval;
    ListPreference mCalendarRange;
    CheckBoxPreference mLockscreenCalendarHideOngoing;
    CheckBoxPreference mLockscreenCalendarUseColors;

    Preference mLockscreenWallpaper;

    private int currentIconIndex;
    private Preference mCurrentCustomActivityPreference;
    private String mCurrentCustomActivityString;

    private ShortcutPickerHelper mPicker;

    ArrayList<String> keys = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keys.add(Settings.System.LOCKSCREEN_HIDE_NAV);
        keys.add(Settings.System.LOCKSCREEN_LANDSCAPE);
        keys.add(Settings.System.LOCKSCREEN_QUICK_UNLOCK_CONTROL);
        keys.add(Settings.System.ENABLE_FAST_TORCH);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_lockscreens);

        menuButtonLocation = (CheckBoxPreference) findPreference(PREF_MENU);
        menuButtonLocation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_ENABLE_MENU_KEY, 1) == 1);

        mLockScreenTimeoutUserOverride = (CheckBoxPreference) findPreference(PREF_USER_OVERRIDE);
        mLockScreenTimeoutUserOverride.setChecked(Settings.Secure.getInt(getActivity()
                .getContentResolver(), Settings.Secure.LOCK_SCREEN_LOCK_USER_OVERRIDE, 0) == 1);

        mLockscreenOption = (ListPreference) findPreference(PREF_LOCKSCREEN_LAYOUT);
        mLockscreenOption.setOnPreferenceChangeListener(this);
        mLockscreenOption.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_LAYOUT, 0) + "");

        mLockscreenBattery = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_BATTERY);
        mLockscreenBattery.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_BATTERY, 0) == 1);

        mLockscreenWeather = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_WEATHER);
        mLockscreenWeather.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_WEATHER, 0) == 1);

        mShowLockBeforeUnlock = (CheckBoxPreference) findPreference(PREF_SHOW_LOCK_BEFORE_UNLOCK);
        mShowLockBeforeUnlock.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_LOCK_BEFORE_UNLOCK, 0) == 1);

        mVolumeWake = (CheckBoxPreference) findPreference(PREF_VOLUME_WAKE);
        mVolumeWake.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOLUME_MUSIC_CONTROLS, 0) == 1);

        mLockscreenWallpaper = findPreference("wallpaper");

        mLockscreenCalendar = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR);
        mLockscreenCalendar.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR, 0) == 1);

        mLockscreenCalendarFlip = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_FLIP);
        mLockscreenCalendarFlip.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_CALENDAR_FLIP, 0) == 1);

        mLockscreenCalendarHideOngoing = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_HIDE_ONGOING);
        mLockscreenCalendarHideOngoing.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_CALENDAR_HIDE_ONGOING, 0) == 1);

        mLockscreenCalendarUseColors = (CheckBoxPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_USE_COLORS);
        mLockscreenCalendarUseColors.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.LOCKSCREEN_CALENDAR_USE_COLORS, 0) == 1);

        mCalendarSources = findPreference(PREF_LOCKSCREEN_CALENDAR_SOURCES);

        mCalendarInterval = (ListPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_INTERVAL);
        mCalendarInterval.setOnPreferenceChangeListener(this);
        mCalendarInterval.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_INTERVAL, 2500) + "");

        mCalendarRange = (ListPreference) findPreference(PREF_LOCKSCREEN_CALENDAR_RANGE);
        mCalendarRange.setOnPreferenceChangeListener(this);
        mCalendarRange.setValue(Settings.System.getLong(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CALENDAR_RANGE, 86400000) + "");

        mPicker = new ShortcutPickerHelper(this, this);

        for (String key : keys) {
            try {
                ((CheckBoxPreference) findPreference(key)).setChecked(Settings.System.getInt(
                        getActivity().getContentResolver(), key) == 1);
            } catch (SettingNotFoundException e) {
            }
        }

        ((PreferenceGroup) findPreference("advanced_cat"))
                .removePreference(findPreference(Settings.System.LOCKSCREEN_HIDE_NAV));

        mLockscreenTextColor = (ColorPickerPreference) findPreference(PREF_LOCKSCREEN_TEXT_COLOR);
        mLockscreenTextColor.setOnPreferenceChangeListener(this);

        refreshSettings();
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (!isSDPresent) {
            mLockscreenWallpaper.setEnabled(false);
            mLockscreenWallpaper
                    .setSummary("No external storage available (/sdcard) to use this feature. Please insert it or fix your ROM!");

        }
        refreshSettings();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == menuButtonLocation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_ENABLE_MENU_KEY,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mLockScreenTimeoutUserOverride) {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_LOCK_USER_OVERRIDE,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mShowLockBeforeUnlock) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_LOCK_BEFORE_UNLOCK,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mLockscreenBattery) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_BATTERY,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mLockscreenWeather) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_WEATHER,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mVolumeWake) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_WAKE_SCREEN,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        } else if (preference == mVolumeMusic) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.VOLUME_MUSIC_CONTROLS,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mLockscreenWallpaper) {

            int width = getActivity().getWallpaperDesiredMinimumWidth();
            int height = getActivity().getWallpaperDesiredMinimumHeight();
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            float spotlightX = (float) display.getWidth() / width;
            float spotlightY = (float) display.getHeight() / height;

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", width);
            intent.putExtra("aspectY", height);
            intent.putExtra("outputX", width);
            intent.putExtra("outputY", height);
            intent.putExtra("scale", true);
            // intent.putExtra("return-data", false);
            intent.putExtra("spotlightX", spotlightX);
            intent.putExtra("spotlightY", spotlightY);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempFileUri());
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
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

            builder.setTitle("Choose which calendars to use");
            builder.setCancelable(false);
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

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

        } else if (keys.contains(preference.getKey())) {
            Log.e("RC_Lockscreens", "key: " + preference.getKey());
            return Settings.System.putInt(getActivity().getContentResolver(), preference.getKey(),
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        } else if (preference.getKey().startsWith("lockscreen_icon")) {

            return true;
        } else if (preference.getKey().startsWith("lockscreen_target")) {

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
        File dir = mContext.getFilesDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);
        Log.i(TAG, "wallpaper loc: " + wallpaper.getAbsolutePath());
        return Uri.fromFile(wallpaper);
    }

    private Uri getTempFileUri() {
        File dir = mContext.getFilesDir();
        File wallpaper = new File(dir, "temp");

        return Uri.fromFile(wallpaper);
    }

    private Uri getExternalIconUri() {
        File dir = mContext.getFilesDir();
        File icon = new File(dir, "icon_" + currentIconIndex + ".png");

        return Uri.fromFile(icon);
    }

    public void refreshSettings() {

        int lockscreenTargets = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_LAYOUT, 2);

        PreferenceGroup targetGroup = (PreferenceGroup) findPreference("lockscreen_targets");
        targetGroup.removeAll();

        // quad only uses first 4, but we make the system think there's 6 for
        // the alternate layout
        // so only show 4
        if (lockscreenTargets == 6) {
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[4], "**null**");
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[5], "**null**");
            lockscreenTargets = 4;
        }

        PackageManager pm = mContext.getPackageManager();
        Resources res = mContext.getResources();

        for (int i = 0; i < lockscreenTargets; i++) {
            LockscreenItemPreference p = new LockscreenItemPreference(getActivity());
            String dialogTitle = String.format(
                    getResources().getString(R.string.custom_app_n_dialog_title), i + 1);
            p.setDialogTitle(dialogTitle);
            p.setEntries(R.array.lockscreen_choice_entries);
            p.setEntryValues(R.array.lockscreen_choice_values);
            String title = String.format(getResources().getString(R.string.custom_app_n), i + 1);
            p.setTitle(title);
            p.setKey("lockscreen_target_" + i);
            p.setSummary(getProperSummary(i));
            p.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(p);

            final int index = i;
            p.setImageListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(
                            android.os.Environment.MEDIA_MOUNTED);
                    if (!isSDPresent) {
                        Toast.makeText(v.getContext(), "Insert SD card to use this feature",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentIconIndex = index;

                    int width = 100;
                    int height = width;

                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                    intent.setType("image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", width);
                    intent.putExtra("aspectY", height);
                    intent.putExtra("outputX", width);
                    intent.putExtra("outputY", height);
                    intent.putExtra("scale", true);
                    // intent.putExtra("return-data", false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getExternalIconUri());
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

                    Log.i(TAG, "started for result, should output to: " + getExternalIconUri());

                    startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON);
                }
            });

            String customIconUri = Settings.System.getString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ICONS[i]);
            if (customIconUri != null && customIconUri.length() > 0) {
                File f = new File(Uri.parse(customIconUri).getPath());
                if (f.exists())
                    p.setIcon(new BitmapDrawable(res, f.getAbsolutePath()));
            }

            if (customIconUri != null && !customIconUri.equals("")
                    && customIconUri.startsWith("file")) {
                // it's an icon the user chose from the gallery here
                File icon = new File(Uri.parse(customIconUri).getPath());
                if (icon.exists())
                    p.setIcon(resize(new BitmapDrawable(getResources(), icon.getAbsolutePath())));

            } else if (customIconUri != null && !customIconUri.equals("")) {
                // here they chose another app icon
                try {
                    p.setIcon(resize(pm.getActivityIcon(Intent.parseUri(customIconUri, 0))));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                // ok use default icons here
                p.setIcon(resize(getLockscreenIconImage(i)));
            }
        }

    }

    private Drawable resize(Drawable image) {
        int size = 50;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources()
                .getDisplayMetrics());

        Bitmap d = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, px, px, false);
        return new BitmapDrawable(mContext.getResources(), bitmapOrig);
    }

    private Drawable getLockscreenIconImage(int index) {
        String uri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[index]);

        if (uri == null)
            return getResources().getDrawable(R.drawable.ic_null);

        if (uri.startsWith("**")) {
            if (uri.equals("**unlock**"))
                return getResources().getDrawable(R.drawable.ic_lockscreen_unlock);
            else if (uri.equals("**sound**"))
                return getResources().getDrawable(R.drawable.ic_lockscreen_soundon);
            else if (uri.equals("**camera**"))
                return getResources().getDrawable(R.drawable.ic_lockscreen_camera);
            else if (uri.equals("**phone**"))
                return getResources().getDrawable(R.drawable.ic_lockscreen_phone);
            else if (uri.equals("**mms**"))
                return getResources().getDrawable(R.drawable.ic_lockscreen_sms);
            else if (uri.equals("**null**"))
                return getResources().getDrawable(R.drawable.ic_null);
        } else {
            try {
                return mContext.getPackageManager().getActivityIcon(Intent.parseUri(uri, 0));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return getResources().getDrawable(R.drawable.ic_null);
    }

    private String getProperSummary(int i) {
        String uri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[i]);

        if (uri == null)
            return getResources().getString(R.string.lockscreen_action_none);

        if (uri.startsWith("**")) {
            if (uri.equals("**unlock**"))
                return getResources().getString(R.string.lockscreen_action_unlock);
            else if (uri.equals("**sound**"))
                return getResources().getString(R.string.lockscreen_action_sound);
            else if (uri.equals("**camera**"))
                return getResources().getString(R.string.lockscreen_action_camera);
            else if (uri.equals("**phone**"))
                return getResources().getString(R.string.lockscreen_action_phone);
            else if (uri.equals("**mms**"))
                return getResources().getString(R.string.lockscreen_action_mms);
            else if (uri.equals("**null**"))
                return getResources().getString(R.string.lockscreen_action_none);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getActivity().getContentResolver(),
                mCurrentCustomActivityString, uri)) {

            String i = mCurrentCustomActivityString.substring(mCurrentCustomActivityString
                    .lastIndexOf("_") + 1);
            Log.i(TAG, "shortcut picked, index: " + i);
            Settings.System.putString(getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_APP_ICONS[Integer.parseInt(i)], "");
            mCurrentCustomActivityPreference.setSummary(friendlyName);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean handled = false;
        if (preference == mLockscreenOption) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_LAYOUT, val);
            refreshSettings();
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

        } else if (preference == mLockscreenTextColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, intHex);
            if (DEBUG)
                Log.d(TAG, String.format("new color hex value: %d", intHex));
            return true;

        } else if (preference.getKey().startsWith("lockscreen_target")) {
            int index = Integer.parseInt(preference.getKey().substring(
                    preference.getKey().lastIndexOf("_") + 1));

            if (newValue.equals("**app**")) {
                mCurrentCustomActivityPreference = preference;
                mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[index];
                mPicker.pickShortcut();
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[index], (String) newValue);
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_CUSTOM_APP_ICONS[index], "");
                refreshSettings();
            }
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
                            Context.MODE_WORLD_WRITEABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                // should use intent.getData() here but it keeps returning null
                Uri selectedImageUri = getTempFileUri();
                Log.e(TAG, "Selected image path: " + selectedImageUri.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, wallpaperStream);

            } else if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if (requestCode == REQUEST_PICK_CUSTOM_ICON) {

                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput("icon_" + currentIconIndex + ".png",
                            Context.MODE_WORLD_WRITEABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri selectedImageUri = getTempFileUri();
                Log.e(TAG, "Selected image path: " + selectedImageUri.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);

                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_CUSTOM_APP_ICONS[currentIconIndex],
                        getExternalIconUri().toString());
                Toast.makeText(getActivity(), currentIconIndex + "'s icon set successfully!",
                        Toast.LENGTH_LONG).show();
                refreshSettings();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
