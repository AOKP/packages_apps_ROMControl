
package com.baked.romcontrol.fragments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.baked.romcontrol.R;
import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.util.CMDProcessor;
import com.baked.romcontrol.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import net.margaritov.preference.colorpicker.ColorPickerView;

public class StatusBarExtra extends BAKEDPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "StatusBarExtra";

    private static final String PREF_STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String PREF_IME_SWITCHER = "ime_switcher";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_NOTIFICATION_WALLPAPER = "notification_wallpaper";
    private static final String PREF_NOTIFICATION_WALLPAPER_ALPHA = "notification_wallpaper_alpha";
    private static final String PREF_EXPANDED_CLOCK_COLOR = "expanded_clock_color";
    private static final String PREF_STATUSBAR_BACKGROUND_STYLE = "statusbar_background_style";
    private static final String PREF_STATUSBAR_BACKGROUND_COLOR = "statusbar_background_color";
    private static final String PREF_STATUSBAR_BRIGHTNESS_SLIDER = "statusbar_brightness_slider";

    private static final int REQUEST_PICK_WALLPAPER = 201;

    CheckBoxPreference mStatusBarNotifCount;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mStatusBarBrightnessSlider;
    Preference mCustomLabel;
    ListPreference mNotificationBackground;
    ListPreference mStatusbarBgStyle;
    Preference mWallpaperAlpha;
    ColorPickerPreference mExpandedClockColor;
    ColorPickerPreference mStatusbarBgColor;

    private Activity mActivity;
    private File wallpaperImage;
    private File wallpaperTemporary;

    private int seekbarProgress;

    String mCustomLabelText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_extra);

        PreferenceScreen prefs = getPreferenceScreen();

        mStatusBarNotifCount = (CheckBoxPreference) findPreference(PREF_STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getInt(mContext
                .getContentResolver(), Settings.System.STATUS_BAR_NOTIF_COUNT,
                0) == 1);

        mStatusBarBrightnessSlider = (CheckBoxPreference) findPreference(PREF_STATUSBAR_BRIGHTNESS_SLIDER);
        mStatusBarBrightnessSlider.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_BRIGHTNESS_SLIDER, true));

        mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
        mShowImeSwitcher.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.SHOW_STATUSBAR_IME_SWITCHER, true));

        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mNotificationBackground = (ListPreference) findPreference(PREF_NOTIFICATION_WALLPAPER);
        mNotificationBackground.setOnPreferenceChangeListener(this);

        wallpaperImage = new File(mActivity.getFilesDir()+"/notifwallpaper");
        wallpaperTemporary = new File(mActivity.getCacheDir()+"/notifwallpaper.tmp");

        mWallpaperAlpha = (Preference) findPreference(PREF_NOTIFICATION_WALLPAPER_ALPHA);

        mExpandedClockColor = (ColorPickerPreference) findPreference(PREF_EXPANDED_CLOCK_COLOR);
        mExpandedClockColor.setOnPreferenceChangeListener(this);

        mStatusbarBgColor = (ColorPickerPreference) findPreference(PREF_STATUSBAR_BACKGROUND_COLOR);
        mStatusbarBgColor.setOnPreferenceChangeListener(this);

        mStatusbarBgStyle = (ListPreference) findPreference(PREF_STATUSBAR_BACKGROUND_STYLE);
        mStatusbarBgStyle.setOnPreferenceChangeListener(this);

        if (mTablet) {
            prefs.removePreference(mNotificationBackground);
            prefs.removePreference(mWallpaperAlpha);
            prefs.removePreference(mStatusbarBgColor);
        }

        updateCustomBackgroundSummary();
        updateVisibility();
    }

    private void updateVisibility() {
        int visible = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BACKGROUND_STYLE, 2);
        if (visible == 2) {
            mStatusbarBgColor.setEnabled(false);
        } else {
            mStatusbarBgColor.setEnabled(true);
        }
    }

    private void updateCustomBackgroundSummary() {
        int resId;
        String value = Settings.System.getString(getContentResolver(),
                Settings.System.NOTIF_BACKGROUND);
        if (value == null) {
            resId = R.string.notif_background_default;
            mNotificationBackground.setValueIndex(2);
        } else if (value.isEmpty()) {
            resId = R.string.notif_background_custom_image;
            mNotificationBackground.setValueIndex(1);
        } else {
            resId = R.string.notif_background_color_fill;
            mNotificationBackground.setValueIndex(0);
        }
        mNotificationBackground.setSummary(getResources().getString(resId));
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
            Preference preference) {
         if (preference == mStatusBarNotifCount) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;

        } else if (preference == mShowImeSwitcher) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SHOW_STATUSBAR_IME_SWITCHER,
                    checkBoxChecked(preference));
            return true;

        } else if (preference == mStatusBarBrightnessSlider) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_SLIDER,
                    checkBoxChecked(preference));
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
                    i.setAction("com.baked.romcontrol.LABEL_CHANGED");
                    mContext.sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mExpandedClockColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_EXPANDED_CLOCK_COLOR, intHex);
            Log.e("BAKED", intHex + "");

        } else if (preference == mStatusbarBgStyle) {
            int value = Integer.valueOf((String) newValue);
            int index = mStatusbarBgStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUSBAR_BACKGROUND_STYLE, value);
            preference.setSummary(mStatusbarBgStyle.getEntries()[index]);
            updateVisibility();
            return true;

        } else if (preference == mStatusbarBgColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BACKGROUND_COLOR, intHex);
            Log.e("BAKED", intHex + "");

        } else if (preference == mNotificationBackground) {
            int indexOf = mNotificationBackground.findIndexOfValue(newValue.toString());
            switch (indexOf) {
                //Displays color dialog when user has chosen color fill
                case 0:
                    final ColorPickerView colorView = new ColorPickerView(mActivity);
                    int currentColor = Settings.System.getInt(getContentResolver(),
                            Settings.System.NOTIF_BACKGROUND, -1);
                    if (currentColor != -1) {
                        colorView.setColor(currentColor);
                    }
                    colorView.setAlphaSliderVisible(true);
                    new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.lockscreen_custom_background_dialog_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getContentResolver(), Settings.System.NOTIF_BACKGROUND, colorView.getColor());
                            updateCustomBackgroundSummary();
                            Helpers.restartSystemUI();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setView(colorView).show();
                    return false;
                 //Launches intent for user to select an image/crop it to set as background
                case 1:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                    intent.setType("image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("scale", true);
                    intent.putExtra("scaleUpIfNeeded", false);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                    Display display = mActivity.getWindowManager().getDefaultDisplay();
                    int width = display.getWidth();
                    int height = display.getHeight();
                    Rect rect = new Rect();
                    Window window = mActivity.getWindow();
                    window.getDecorView().getWindowVisibleDisplayFrame(rect);
                    int statusBarHeight = rect.top;
                    int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                    int titleBarHeight = contentViewTop - statusBarHeight;
                    boolean isPortrait = getResources().getConfiguration().orientation ==
                            Configuration.ORIENTATION_PORTRAIT;
                    intent.putExtra("aspectX", isPortrait ? width : height - titleBarHeight);
                    intent.putExtra("aspectY", isPortrait ? height - titleBarHeight : width);

                    try {
                        wallpaperTemporary.createNewFile();
                        wallpaperTemporary.setWritable(true, false);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(wallpaperTemporary));
                        intent.putExtra("return-data", false);
                        mActivity.startActivityFromFragment(this, intent, REQUEST_PICK_WALLPAPER);
                    } catch (IOException e) {
                    } catch (ActivityNotFoundException e) {
                    }
                    return false;
                //Sets background color to default
                case 2:
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NOTIF_BACKGROUND, null);
                    updateCustomBackgroundSummary();
                    break;
            }
            Helpers.restartSystemUI();
            return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_WALLPAPER) {
            if (resultCode == Activity.RESULT_OK) {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.renameTo(wallpaperImage);
                }
                wallpaperImage.setReadable(true, false);
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_successful), Toast.LENGTH_LONG).show();
                Settings.System.putString(getContentResolver(),
                        Settings.System.NOTIF_BACKGROUND,"");
                updateCustomBackgroundSummary();
                Helpers.restartSystemUI();
            } else {
                if (wallpaperTemporary.exists()) {
                    wallpaperTemporary.delete();
                }
                Toast.makeText(mActivity, getResources().getString(R.string.
                        lockscreen_background_result_not_successful), Toast.LENGTH_LONG).show();
            }
        }
    }
}
