
package com.roman.romcontrol.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.roman.romcontrol.R;
import com.roman.romcontrol.SettingsPreferenceFragment;
import com.roman.romcontrol.util.ShortcutPickerHelper;

public class Lockscreens extends SettingsPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    private static final String TAG = "Lockscreens";

    private static final String PREF_MENU = "pref_lockscreen_menu_unlock";
    private static final String PREF_USER_OVERRIDE = "lockscreen_user_timeout_override";
    private static final String PREF_LOCKSCREEN_LAYOUT = "pref_lockscreen_layout";

    private static final String PREF_VOLUME_WAKE = "volume_wake";
    private static final String PREF_VOLUME_MUSIC = "volume_music_controls";

    private static final String PREF_LOCKSCREEN_BATTERY = "lockscreen_battery";
    private static final String PREF_LOCKSCREEN_WEATHER = "lockscreen_weather";

    public static final int REQUEST_PICK_WALLPAPER = 199;
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

    Preference mLockscreenWallpaper;

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

        mVolumeWake = (CheckBoxPreference) findPreference(PREF_VOLUME_WAKE);
        mVolumeWake.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOLUME_WAKE_SCREEN, 0) == 1);

        mVolumeMusic = (CheckBoxPreference) findPreference(PREF_VOLUME_MUSIC);
        mVolumeMusic.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.VOLUME_MUSIC_CONTROLS, 0) == 1);

        mLockscreenWallpaper = findPreference("wallpaper");

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

        refreshSettings();
        setHasOptionsMenu(true);
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
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getLockscreenExternalUri());
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
            return true;

        } else if (keys.contains(preference.getKey())) {
            Log.e("RC_Lockscreens", "key: " + preference.getKey());
            return Settings.System.putInt(getActivity().getContentResolver(), preference.getKey(),
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
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

        for (int i = 0; i < lockscreenTargets; i++) {
            ListPreference p = new ListPreference(getActivity());
            String dialogTitle = String.format(
                    getResources().getString(R.string.custom_app_n_dialog_title), i + 1);
            ;
            p.setDialogTitle(dialogTitle);
            p.setEntries(R.array.lockscreen_choice_entries);
            p.setEntryValues(R.array.lockscreen_choice_values);
            String title = String.format(getResources().getString(R.string.custom_app_n), i + 1);
            p.setTitle(title);
            p.setKey("lockscreen_target_" + i);
            p.setSummary(getProperSummary(i));
            p.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(p);
        }

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
            mCurrentCustomActivityPreference.setSummary(friendlyName);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockscreenOption) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_LAYOUT, val);
            refreshSettings();
            return true;

        } else if (preference.getKey().startsWith("lockscreen_target")) {
            int index = Integer.parseInt(preference.getKey().substring(
                    preference.getKey().lastIndexOf("_") + 1));
            Log.e("ROMAN", "lockscreen target, index: " + index);

            if (newValue.equals("**app**")) {
                mCurrentCustomActivityPreference = preference;
                mCurrentCustomActivityString = Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[index];
                mPicker.pickShortcut();
            } else {
                Settings.System.putString(getContentResolver(),
                        Settings.System.LOCKSCREEN_CUSTOM_APP_ACTIVITIES[index], (String) newValue);
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
                    wallpaperStream = mContext.openFileOutput(WALLPAPER_NAME, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                // should use intent.getData() here but it keeps returning null
                Uri selectedImageUri = getLockscreenExternalUri();
                Log.e(TAG, "Selected image uri: " + selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, wallpaperStream);

            } else if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION) {
                mPicker.onActivityResult(requestCode, resultCode, data);
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

}
