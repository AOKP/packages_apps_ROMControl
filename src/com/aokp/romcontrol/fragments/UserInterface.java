
package com.aokp.romcontrol.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.Helpers;

public class UserInterface extends AOKPPreferenceFragment {

    public static final String TAG = "UserInterface";

    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";

    private static final int REQUEST_PICK_WALLPAPER = 201;
    private static final int REQUEST_PICK_CUSTOM_ICON = 202;
    private static final int SELECT_ACTIVITY = 4;
    private static final int SELECT_WALLPAPER = 5;

    private static final String WALLPAPER_NAME = "notification_wallpaper.jpg";

    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mStatusBarNotifCount;

    Random randomGenerator = new Random();

    Preference mNotificationWallpaper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

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

        mNotificationWallpaper = findPreference(PREF_NOTIFICATION_WALLPAPER);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mStatusBarNotifCount) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mDisableBootAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.unicorn");
                Helpers.getMount("ro");
                Resources res = mContext.getResources();
                String[] insults = res.getStringArray(R.array.disable_bootanimation_insults);
                int randomInt = randomGenerator.nextInt(insults.length);
                preference.setSummary(insults[randomInt]);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.unicorn /system/media/bootanimation.zip");
                Helpers.getMount("ro");
                preference.setSummary("");
            }
            return true;
        } else if (preference == mNotificationWallpaper) {
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            Rect rect = new Rect();
            Window window = getActivity().getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            int statusBarHeight = rect.top;
            int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;
	
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            boolean isPortrait = getResources()
                    .getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT;
            intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
            intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);
            intent.putExtra("outputX", width);
            intent.putExtra("outputY", height);
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getNotificationExternalUri());
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
            return true;
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
