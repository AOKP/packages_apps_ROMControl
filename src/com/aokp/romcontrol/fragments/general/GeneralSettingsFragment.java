/*
 * Copyright (C) 2017 The Android Open Kang Project
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

package com.aokp.romcontrol.fragments.general;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.util.AbstractAsyncSuCMDProcessor;
import com.aokp.romcontrol.util.CMDProcessor;

import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

public class GeneralSettingsFragment extends Fragment {

    public GeneralSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_general_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.general_settings_main, new GeneralSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class GeneralSettingsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        public GeneralSettingsPreferenceFragment() {

        }

        private static final String TAG = "GeneralSettingsPreferenceFragment";

        private static final int REQUEST_PICK_BOOT_ANIMATION = 201;

        private static final String KEY_LOCKCLOCK = "lock_clock";
        // Package name of the cLock app
        public static final String LOCKCLOCK_PACKAGE_NAME = "com.cyanogenmod.lockclock";
        private static final String SCREENSHOT_TYPE = "screenshot_type";
        private static final String SCREENSHOT_DELAY = "screenshot_delay";
        private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
        private static final String PREF_CUSTOM_BOOTANIM = "custom_bootanimation";
        private static final String BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip";
        private static final String BACKUP_PATH = new File(Environment
                .getExternalStorageDirectory(), "/AOKP_ota").getAbsolutePath();

        private Context mContext;
        private Preference mLockClock;
        private ListPreference mScreenshotType;
        private SeekBarPreferenceCham mScreenshotDelay;
        private Preference mCustomBootAnimation;
        private ImageView mView;
        private TextView mError;
        private AlertDialog mCustomBootAnimationDialog;
        private AnimationDrawable mAnimationPart1;
        private AnimationDrawable mAnimationPart2;
        private String mErrormsg;
        private String mBootAnimationPath;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_general_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            mContext = getActivity().getApplicationContext();
            ContentResolver resolver = getActivity().getContentResolver();
            PackageManager pm = getActivity().getPackageManager();

            // cLock app check
            mLockClock = (Preference)
                    prefSet.findPreference(KEY_LOCKCLOCK);
            if (!Helpers.isPackageInstalled(LOCKCLOCK_PACKAGE_NAME, pm)) {
                prefSet.removePreference(mLockClock);
            }

            mScreenshotType = (ListPreference) findPreference(SCREENSHOT_TYPE);
            int mScreenshotTypeValue = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, 0);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            mScreenshotType.setSummary(mScreenshotType.getEntry());
            mScreenshotType.setOnPreferenceChangeListener(this);

            mScreenshotDelay = (SeekBarPreferenceCham) findPreference(SCREENSHOT_DELAY);
            int screenshotDelay = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_DELAY, 100);
            mScreenshotDelay.setValue(screenshotDelay / 1);
            mScreenshotDelay.setOnPreferenceChangeListener(this);

            // Custom bootanimation
            mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM);

            resetBootAnimation();

            return prefSet;
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mCustomBootAnimation) {
                openBootAnimationDialog();
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();

            if  (preference == mScreenshotType) {
                int mScreenshotTypeValue = Integer.parseInt(((String) newValue).toString());
                mScreenshotType.setSummary(
                        mScreenshotType.getEntries()[mScreenshotTypeValue]);
                Settings.System.putInt(resolver,
                        Settings.System.SCREENSHOT_TYPE, mScreenshotTypeValue);
                mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
                return true;
            } else if (preference == mScreenshotDelay) {
                int screenshotDelay = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.SCREENSHOT_DELAY, screenshotDelay * 1);
                return true;
            }
            return false;
        }

        /**
         * Resets boot animation path. Essentially clears temporary-set boot animation
         * set by the user from the dialog.
         *
         * @return returns true if a boot animation exists (user or system). false otherwise.
         */
        private boolean resetBootAnimation() {
            boolean bootAnimationExists = false;
            if (new File(BOOTANIMATION_SYSTEM_PATH).exists()) {
                mBootAnimationPath = BOOTANIMATION_SYSTEM_PATH;
                bootAnimationExists = true;
            } else {
                mBootAnimationPath = "";
            }
            return bootAnimationExists;
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                    if (data == null) {
                        //Nothing returned by user, probably pressed back button in file manager
                        return;
                    }
                    mBootAnimationPath = data.getData().getPath();
                    openBootAnimationDialog();
                }
            }
        }

        private void openBootAnimationDialog() {
            Log.e(TAG, "boot animation path: " + mBootAnimationPath);
            if (mCustomBootAnimationDialog != null) {
                mCustomBootAnimationDialog.cancel();
                mCustomBootAnimationDialog = null;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.bootanimation_preview);
            if (!mBootAnimationPath.isEmpty()
                    && (!BOOTANIMATION_SYSTEM_PATH.equalsIgnoreCase(mBootAnimationPath))) {
                builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        installBootAnim(dialog, mBootAnimationPath);
                        resetBootAnimation();
                    }
                });
            }
            builder.setNeutralButton(R.string.set_custom_bootanimation,
                    new DialogInterface.OnClickListener() {
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
                                Snackbar.make(getView(), R.string.install_file_manager_error,
                                        Snackbar.LENGTH_SHORT).show();
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
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    // we tried
                }
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException e) {
                    // we tried
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
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
                } else {
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
                     enumeration.hasMoreElements(); ) {
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
                            if (partOneInStream != null) {
                                partOneInStream.close();
                            }
                        }
                    } else if (partName2.equalsIgnoreCase(partname)) {
                        InputStream partTwoInStream = null;
                        try {
                            partTwoInStream = zipfile.getInputStream(entry);
                            mAnimationPart2.addFrame(new BitmapDrawable(getResources(),
                                    BitmapFactory.decodeStream(partTwoInStream,
                                            null, opt)), delay);
                        } finally {
                            if (partTwoInStream != null) {
                                partTwoInStream.close();
                            }
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
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
            Date date = new Date();
            String current = (dateFormat.format(date));
            new AbstractAsyncSuCMDProcessor() {
                @Override
                protected void onPostExecute(String result) {
                }
            }.execute("mount -o rw,remount /system",
                    "cp -f /system/media/bootanimation.zip " + BACKUP_PATH + "/bootanimation_backup_" + current + ".zip",
                    "cp -f " + bootAnimationPath + " /system/media/bootanimation.zip",
                    "chmod 644 /system/media/bootanimation.zip",
                    "mount -o ro,remount /system");
        }
    }
}
