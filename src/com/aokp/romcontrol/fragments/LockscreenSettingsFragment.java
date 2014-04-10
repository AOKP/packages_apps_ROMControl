package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.ColorPickerSetting;
import com.aokp.romcontrol.settings.ImageSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LockscreenSettingsFragment extends Fragment implements OnClickListener, OnSettingChangedListener {

    private static final String TAG = "LockscreenSettings";
    public static final int REQUEST_PICK_WALLPAPER = 199;
    private static final String WALLPAPER_NAME = "lockscreen_wallpaper.png";

    private ImageSetting mImageSettingLockscreenWallpaper;
    CheckboxSetting mLockscreenNotifications, mPocketMode, mShowAlways, mWakeOnNotification,
        mHideLowPriority, mHideNonClearable, mDismissAll, mPrivacyMode, mExpandedView, mExpandedViewForce;
    ColorPickerSetting mNotificationColor;
    SingleChoiceSetting mOffsetTop, mNotificationHeight;
    boolean mHasProximitySensor;

    public LockscreenSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHasProximitySensor = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lockscreen_settings, container, false);

        mImageSettingLockscreenWallpaper = (ImageSetting) v.findViewById(R.id.lockscreen_wallpaper);
        mImageSettingLockscreenWallpaper.setOnClickListener(this);

        mLockscreenNotifications = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications);
        mPocketMode = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_pocket_mode);
        mShowAlways = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_show_always);
        if (!mHasProximitySensor) {
            mPocketMode.setVisibility(View.GONE);
            mShowAlways.setVisibility(View.GONE);
        }
        mWakeOnNotification = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_wake_on_notification);
        mHideLowPriority = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_hide_low_priority);
        mHideNonClearable = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_hide_non_clearable);
        mDismissAll = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_dismiss_all);
        mOffsetTop = (SingleChoiceSetting) v.findViewById(R.id.lockscreen_notifications_offset_top);
        mNotificationHeight = (SingleChoiceSetting) v.findViewById(R.id.lockscreen_notifications_height);
        mNotificationColor = (ColorPickerSetting) v.findViewById(R.id.lockscreen_notifications_color);
        mPrivacyMode = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_privacy_mode);
        mExpandedView = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_expanded_view);
        mExpandedViewForce = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_force_expanded_view);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLockscreenNotifications.setOnSettingChangedListener(this);
        mPocketMode.setOnSettingChangedListener(this);
        mHideNonClearable.setOnSettingChangedListener(this);
        mPrivacyMode.setOnSettingChangedListener(this);
        mExpandedView.setOnSettingChangedListener(this);
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if ("aokp".equals(table)) {
            if (mHasProximitySensor) {
                mPocketMode.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
                // Display only if pocket mode is enabled
                mShowAlways.setVisibility(mLockscreenNotifications.isChecked() ? (mPocketMode.isChecked() ? View.VISIBLE : View.GONE) : View.GONE);
            } else {
                mPocketMode.setVisibility(View.GONE);
                mShowAlways.setVisibility(View.GONE);
            }
            mWakeOnNotification.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mHideLowPriority.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mHideNonClearable.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            // Display only if hide non clearable is disabled
            mDismissAll.setVisibility(mLockscreenNotifications.isChecked() ? (mHideNonClearable.isChecked() ? View.GONE : View.VISIBLE) : View.GONE);
            mOffsetTop.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mNotificationHeight.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mNotificationColor.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mPrivacyMode.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            // Display only if privacy mode is disabled
            mExpandedView.setVisibility(mLockscreenNotifications.isChecked() ? (mPrivacyMode.isChecked() ? View.GONE : View.VISIBLE) : View.GONE);
            // Display only if expanded view is enabled
            mExpandedViewForce.setVisibility(mLockscreenNotifications.isChecked() ? (mExpandedView.isChecked() ? View.VISIBLE : View.GONE) : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (wallpaperExists()) {
            mImageSettingLockscreenWallpaper.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_exists));
        } else {
            mImageSettingLockscreenWallpaper.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_none));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lockscreen_wallpaper:
                if (wallpaperExists()) {
                    buildWallpaperAlert();
                } else {
                    prepareAndSetWallpaper();
                }
                break;
        }
    };

    private Uri getLockscreenExternalUri() {
        File dir = getActivity().getExternalCacheDir();
        File wallpaper = new File(dir, WALLPAPER_NAME);
        return Uri.fromFile(wallpaper);
    }

    private void buildWallpaperAlert() {
        Drawable myWall = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.lockscreen_wallpaper_title);
        builder.setPositiveButton(R.string.lockscreen_wallpaper_pick,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prepareAndSetWallpaper();
                    }
                });
        builder.setNegativeButton(R.string.lockscreen_wallpaper_remove,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeWallpaper();
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
        File wallpaper = new File(getActivity().getFilesDir(), WALLPAPER_NAME);
        myWall = new BitmapDrawable(getActivity().getResources(), wallpaper.getAbsolutePath());
        wallView.setImageDrawable(myWall);
        builder.setView(layout);
        builder.show();
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
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getLockscreenExternalUri());

        startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
    }

    private void removeWallpaper() {
        getActivity().deleteFile(WALLPAPER_NAME);

        /*Some "no wallpaper" image (grayed out?)*/
         mImageSettingLockscreenWallpaper.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_none));
    }

    private boolean wallpaperExists() {
        File wallpaper = new File(getActivity().getFilesDir(), WALLPAPER_NAME);
        return wallpaper.exists();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_WALLPAPER) {
                FileOutputStream wallpaperStream = null;
                try {
                    wallpaperStream = getActivity().openFileOutput(WALLPAPER_NAME,
                            Context.MODE_WORLD_READABLE);

                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }
                Uri selectedImageUri = getLockscreenExternalUri();
                Bitmap bitmap;
                if (data != null) {
                    Uri mUri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                                mUri);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, wallpaperStream);
                    } catch (NullPointerException npe) {
                        Log.e(TAG, "SeletedImageUri was null.");
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                }
                /*Some "wallpaper exists" image (full color?)*/
                mImageSettingLockscreenWallpaper.setImageDrawable(getResources().getDrawable(R.drawable.ic_wallpaper_exists));
                buildWallpaperAlert();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
