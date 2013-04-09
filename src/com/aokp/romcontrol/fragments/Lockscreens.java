/*
 * Copyright (C) 2013 Android Open Kang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.preference.PreferenceFragment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import static com.android.internal.util.aokp.AwesomeConstants.*;
import com.android.internal.util.aokp.LockScreenHelpers;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.TargetDrawable;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.ROMControlActivity;
import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.lang.NumberFormatException;

public class Lockscreens extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, ColorPickerDialog.OnColorChangedListener,
        GlowPadView.OnTriggerListener {
    private static final String TAG = "Lockscreen";
    private static final boolean DEBUG = false;

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;

    private Context mContext;
    private Resources mResources;

    private ContentResolver cr;

    private GlowPadView mGlowPadView;
    private TextView mHelperText;
    private View mLockscreenOptions;
    private boolean mIsLandscape;

    private Switch mLongPressStatus;
    private Switch mLockBatterySwitch;
    private Switch mLockRotateSwitch;
    private Switch mLockVolControlSwitch;
    private Switch mLockVolWakeSwitch;
    private Switch mLockPageHintSwitch;
    private Switch mLockMinimizeChallangeSwitch;
    private Switch mLockCarouselSwitch;
    private Switch mLockAllWidgetsSwitch;
    private Switch mLockUnlimitedWidgetsSwitch;
    private Button mLockTextColorButton;

    private TextView mLongPressText;
    private TextView mLockTextColorText;
    private TextView mLockBatteryText;
    private TextView mLockRotateText;
    private TextView mLockVolControlText;
    private TextView mLockVolWakeText;
    private TextView mLockPageHintText;
    private TextView mLockMinimizeChallangeText;
    private TextView mLockCarouselText;
    private TextView mLockAllWidgetsText;
    private TextView mLockUnlimitedWidgetsText;

    private ShortcutPickerHelper mPicker;
    private String[] targetActivities = new String[8];
    private String[] longActivities = new String[8];
    private String[] customIcons = new String[8];
    private ViewGroup mContainer;

    private int defaultColor;
    private int textColor;

    private boolean mBoolLongPress;
    private int mTargetIndex;
    private int mTarget = 0;

    public static enum DialogConstant {
        ICON_ACTION {
            @Override
            public String value() {
                return "**icon**";
            }
        },
        LONG_ACTION {
            @Override
            public String value() {
                return "**long**";
            }
        },
        SHORT_ACTION {
            @Override
            public String value() {
                return "**short**";
            }
        },
        CUSTOM_APP {
            @Override
            public String value() {
                return "**app**";
            }
        },
        NOT_IN_ENUM {
            @Override
            public String value() {
                return "**notinenum**";
            }
        };
        public String value() {
            return this.value();
        }
    }

    public static DialogConstant funcFromString(String string) {
        DialogConstant[] allTargs = DialogConstant.values();
        for (int i = 0; i < allTargs.length; i++) {
            if (string.equals(allTargs[i].value())) {
                return allTargs[i];
            }
        }
        // not in ENUM must be custom
        return DialogConstant.NOT_IN_ENUM;
    }

    private String mString;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContainer = container;
        setHasOptionsMenu(true);
        mContext = getActivity();
        mResources = getResources();
        cr = mContext.getContentResolver();
        mPicker = new ShortcutPickerHelper(this, this);
        return inflater.inflate(R.layout.lockscreen_targets, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGlowPadView = ((GlowPadView) getActivity().findViewById(R.id.lock_target));
        mGlowPadView.setOnTriggerListener(this);
        mLockscreenOptions = ((View) getActivity().findViewById(R.id.lockscreen_options));
        if (mLockscreenOptions != null) {
            mLockscreenOptions.getParent().bringChildToFront(mLockscreenOptions);
            mIsLandscape = false;
        } else {
            mIsLandscape = true;
        }
        mHelperText = ((TextView) getActivity().findViewById(R.id.helper_text));
        defaultColor = mResources
                .getColor(com.android.internal.R.color.config_defaultNotificationColor);
        textColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, defaultColor);

        mLockTextColorText = ((TextView) getActivity().findViewById(R.id.lockscreen_button_id));
        mLockTextColorText.setOnClickListener(mLockTextColorTextListener);
        mLockTextColorButton = ((Button) getActivity().findViewById(R.id.lockscreen_color_button));
        mLockTextColorButton.setBackgroundColor(textColor);
        mLockTextColorButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog picker = new ColorPickerDialog(mContext, textColor);
                picker.setOnColorChangedListener(Lockscreens.this);
                picker.show();
            }
        });

        mLockBatteryText = ((TextView) getActivity().findViewById(R.id.lockscreen_battery_id));
        mLockBatteryText.setOnClickListener(mLockBatteryTextListener);
        mLockBatterySwitch = (Switch) getActivity().findViewById(R.id.lockscreen_battery_switch);
        mLockBatterySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_BATTERY, checked);
                updateSwitches();
            }
        });

        mLockRotateText = ((TextView) getActivity().findViewById(R.id.lockscreen_rotate_id));
        mLockRotateText.setOnClickListener(mLockRotateTextListener);
        mLockRotateSwitch = (Switch) getActivity().findViewById(R.id.lockscreen_rotate_switch);
        mLockRotateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_AUTO_ROTATE, checked);
                updateSwitches();
            }
        });

        mLockVolControlText = ((TextView) getActivity().findViewById(
                R.id.lockscreen_vol_controls_id));
        mLockVolControlText.setOnClickListener(mLockVolControlTextListener);
        mLockVolControlSwitch = (Switch) getActivity().findViewById(
                R.id.lockscreen_vol_controls_switch);
        mLockVolControlSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr, Settings.System.VOLUME_MUSIC_CONTROLS,
                                checked);
                        updateSwitches();
                    }
                });

        mLockVolWakeText = ((TextView) getActivity().findViewById(R.id.lockscreen_vol_wake_id));
        mLockVolWakeText.setOnClickListener(mLockVolWakeTextListener);
        mLockVolWakeSwitch = (Switch) getActivity().findViewById(R.id.lockscreen_vol_wake_switch);
        mLockVolWakeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.VOLUME_WAKE_SCREEN, checked);
                updateSwitches();
            }
        });

        mLockAllWidgetsText = ((TextView) getActivity()
                .findViewById(R.id.lockscreen_all_widgets_id));
        mLockAllWidgetsText.setOnClickListener(mLockAllWidgetsTextListener);
        mLockAllWidgetsSwitch = (Switch) getActivity().findViewById(
                R.id.lockscreen_all_widgets_switch);
        mLockAllWidgetsSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_ALL_WIDGETS,
                                checked);
                        updateSwitches();
                    }
                });

        mLockUnlimitedWidgetsText = ((TextView) getActivity().findViewById(
                R.id.lockscreen_unlimited_widgets_id));
        mLockUnlimitedWidgetsText.setOnClickListener(mLockUnlimitedWidgetsTextListener);
        mLockUnlimitedWidgetsSwitch = (Switch) getActivity().findViewById(
                R.id.lockscreen_unlimited_widgets_switch);
        mLockUnlimitedWidgetsSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, checked);
                        updateSwitches();
                    }
                });

        mLockPageHintText = ((TextView) getActivity().findViewById(
                R.id.lockscreen_hide_page_hints_id));
        mLockPageHintText.setOnClickListener(mLockPageHintTextListener);
        mLockPageHintSwitch = (Switch) getActivity().findViewById(
                R.id.lockscreen_hide_page_hints_switch);
        mLockPageHintSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, checked);
                        updateSwitches();
                    }
                });

        mLockMinimizeChallangeText = ((TextView) getActivity().findViewById(
                R.id.lockscreen_minimize_challange_id));
        mLockMinimizeChallangeText.setOnClickListener(mLockMinimizeChallangeTextListener);
        mLockMinimizeChallangeSwitch = (Switch) getActivity().findViewById(
                R.id.lockscreen_minimize_challange_switch);
        mLockMinimizeChallangeSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, checked);
                        updateSwitches();
                    }
                });

        if (isTablet(mContext) || isPhablet(mContext)) {
            Settings.System.putBoolean(cr,
                Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, false);
            mLockMinimizeChallangeText.setVisibility(View.GONE);
            mLockMinimizeChallangeSwitch.setVisibility(View.GONE);
        }

        mLockCarouselText = ((TextView) getActivity().findViewById(R.id.lockscreen_carousel_id));
        mLockCarouselText.setOnClickListener(mLockCarouselTextListener);
        mLockCarouselSwitch = (Switch) getActivity().findViewById(R.id.lockscreen_carousel_switch);
        mLockCarouselSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton v, boolean checked) {
                        Settings.System.putBoolean(cr,
                                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, checked);
                        updateSwitches();
                    }
                });

        mLongPressText = ((TextView) getActivity()
                .findViewById(R.id.lockscreen_target_longpress_id));
        mLongPressText.setOnClickListener(mLongPressTextListener);
        mLongPressStatus = (Switch) getActivity().findViewById(R.id.longpress_switch);
        mLongPressStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_TARGETS_LONGPRESS,
                        checked);
                updateDrawables();
            }
        });
        updateSwitches();
        updateDrawables();
    }

    private TextView.OnClickListener mLockTextColorTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_text_color_title),
                    getResources().getString(R.string.lockscreen_text_color_summary));
        }
    };

    private TextView.OnClickListener mLongPressTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_target_longpress_text),
                    getResources().getString(R.string.lockscreen_target_longpress_summary));
        }
    };

    private TextView.OnClickListener mLockBatteryTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_battery_title),
                    getResources().getString(R.string.lockscreen_battery_summary));
        }
    };

    private TextView.OnClickListener mLockRotateTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_auto_rotate_title),
                    getResources().getString(R.string.lockscreen_auto_rotate_summary));
        }
    };

    private TextView.OnClickListener mLockVolControlTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.volume_music_controls_title),
                    getResources().getString(R.string.volume_music_controls_summary));
        }
    };

    private TextView.OnClickListener mLockVolWakeTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.volume_rocker_wake_title),
                    getResources().getString(R.string.volume_rocker_wake_summary));
        }
    };

    private TextView.OnClickListener mLockUnlimitedWidgetsTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_unlimited_widgets_title),
                    getResources().getString(R.string.lockscreen_unlimited_widgets_summary));
        }
    };

    private TextView.OnClickListener mLockAllWidgetsTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_all_widgets_title),
                    getResources().getString(R.string.lockscreen_all_widgets_summary));
        }
    };

    private TextView.OnClickListener mLockPageHintTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_hide_initial_page_hints_title),
                    getResources().getString(R.string.lockscreen_hide_initial_page_hints_summary));
        }
    };

    private TextView.OnClickListener mLockMinimizeChallangeTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(R.string.lockscreen_minimize_challenge_title),
                    getResources().getString(R.string.lockscreen_minimize_challenge_summary));
        }
    };

    private TextView.OnClickListener mLockCarouselTextListener = new TextView.OnClickListener() {
        public void onClick(View v) {
            createMessage(
                    getResources().getString(
                            R.string.lockscreen_use_widget_container_carousel_title),
                    getResources().getString(
                            R.string.lockscreen_use_widget_container_carousel_summary));
        }
    };

    private void updateSwitches() {
        mLockBatterySwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_BATTERY, false));
        mLockRotateSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_AUTO_ROTATE, false));
        mLockVolControlSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.VOLUME_MUSIC_CONTROLS, false));
        mLockVolWakeSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.VOLUME_WAKE_SCREEN, false));
        mLockAllWidgetsSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_ALL_WIDGETS, false));
        mLockUnlimitedWidgetsSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_UNLIMITED_WIDGETS, false));
        mLockPageHintSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_HIDE_INITIAL_PAGE_HINTS, false));
        mLockMinimizeChallangeSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_MINIMIZE_LOCKSCREEN_CHALLENGE, false));
        mLockCarouselSwitch.setChecked(Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, false));
    }


    private void setDrawables() {
        mLongPressStatus.setChecked(mBoolLongPress);

        // Custom Targets
        ArrayList<TargetDrawable> storedDraw = new ArrayList<TargetDrawable>();

        // Add User Targets
        for (int i = 0; i < 8; i++) {
            TargetDrawable drawable;
            if (!TextUtils.isEmpty(customIcons[i])) {
                drawable = LockScreenHelpers.getCustomDrawable(mContext, customIcons[i]);
            } else {
                drawable = LockScreenHelpers.getTargetDrawable(mContext, targetActivities[i]);
            }
            drawable.setEnabled(true);
            storedDraw.add(drawable);
        }
        mGlowPadView.setTargetResources(storedDraw);
        maybeSwapSearchIcon();
    }

    private void maybeSwapSearchIcon() {
        // Update the search icon with drawable from the search .apk
        Intent intent = ((SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE))
                .getAssistIntent(mContext, UserHandle.USER_CURRENT);
        if (intent != null) {
            ComponentName component = intent.getComponent();
            boolean replaced = mGlowPadView.replaceTargetDrawablesIfPresent(component,
                    ASSIST_ICON_METADATA_NAME + "_google",
                    com.android.internal.R.drawable.ic_action_assist_generic);
            if (!replaced && !mGlowPadView.replaceTargetDrawablesIfPresent(component,
                    ASSIST_ICON_METADATA_NAME,
                    com.android.internal.R.drawable.ic_action_assist_generic)) {
                Log.v(TAG, "Couldn't grab icon from package " + component);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
                .setIcon(R.drawable.ic_settings_backup)
                .setAlphabeticShortcut('r')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENU_SAVE, 0, R.string.wifi_save)
                .setIcon(R.drawable.ic_menu_save)
                .setAlphabeticShortcut('s')
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                        MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetAll();
                return true;
            case MENU_SAVE:
                saveAll();
                Toast.makeText(mContext, R.string.lockscreen_target_save, Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    /**
     * Resets the target layout to stock
     */
    private void resetAll() {
        final AlertDialog d = new AlertDialog.Builder(mContext)
                .setTitle(R.string.lockscreen_target_reset_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.lockscreen_target_reset_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < 8; i++) {
                            Settings.System.putString(cr,
                                    Settings.System.LOCKSCREEN_TARGETS_SHORT[i], null);
                            Settings.System.putString(cr,
                                    Settings.System.LOCKSCREEN_TARGETS_LONG[i], null);
                            Settings.System.putString(cr,
                                    Settings.System.LOCKSCREEN_TARGETS_ICON[i], null);

                        }
                        updateDrawables();
                        Toast.makeText(mContext,
                                R.string.lockscreen_target_reset,
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        d.show();
    }

    /**
     * Save targets to settings provider
     */
    private void saveAll() {
        if (mUnlockCounter() > 0) {
            for (int i = 0; i < 8; i++) {
                Settings.System.putString(cr,
                        Settings.System.LOCKSCREEN_TARGETS_SHORT[i], targetActivities[i]);
                Settings.System.putString(cr,
                        Settings.System.LOCKSCREEN_TARGETS_LONG[i], longActivities[i]);
                Settings.System.putString(cr,
                        Settings.System.LOCKSCREEN_TARGETS_ICON[i], customIcons[i]);
            }
            updateDrawables();
        } else {
            Toast.makeText(mContext, getResources()
                    .getString(R.string.save_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        switch (mTarget) {
            case 0:
                targetActivities[mTargetIndex] = uri;
                break;
            case 1:
                longActivities[mTargetIndex] = uri;
                Toast.makeText(mContext, getProperSummary(uri) + "  "
                        + getResources().getString(R.string.action_long_save),
                        Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }

        setDrawables();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if ((requestCode == REQUEST_PICK_CUSTOM_ICON)
                    || (requestCode == REQUEST_PICK_LANDSCAPE_ICON)) {

                String iconName = getIconFileName(mTargetIndex);
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput(iconName, Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri selectedImageUri = getTempFileUri();
                try {
                    Log.e(TAG, "Selected image path: " + selectedImageUri.getPath());
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                } catch (NullPointerException npe) {
                    Log.e(TAG, "SeletedImageUri was null.");
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                customIcons[mTargetIndex] = Uri
                        .fromFile(new File(mContext.getFilesDir(), iconName)).getPath();

                File f = new File(selectedImageUri.getPath());
                if (f.exists())
                    f.delete();

                Toast.makeText(
                        mContext,
                        mTargetIndex
                                + getResources().getString(
                                        R.string.custom_app_icon_successfully),
                        Toast.LENGTH_LONG).show();
                setDrawables();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateDrawables() {
        for (int i = 0; i < 8; i++) {
            targetActivities[i] = Settings.System.getString(cr,
                    Settings.System.LOCKSCREEN_TARGETS_SHORT[i]);
            longActivities[i] = Settings.System.getString(cr,
                    Settings.System.LOCKSCREEN_TARGETS_LONG[i]);
            customIcons[i] = Settings.System.getString(cr,
                    Settings.System.LOCKSCREEN_TARGETS_ICON[i]);
        }
        mBoolLongPress = (Settings.System.getBoolean(cr,
                Settings.System.LOCKSCREEN_TARGETS_LONGPRESS, false));

        if (mUnlockCounter() < 1) {
            targetActivities[0] = AwesomeConstant.ACTION_UNLOCK.value();
        }
        setDrawables();
    }

    public void onValueChange(String uri) {
        DialogConstant mFromString = funcFromString(uri);
        switch (mFromString) {
            case CUSTOM_APP:
                mPicker.pickShortcut();
                break;
            case SHORT_ACTION:
                mTarget = 0;
                mString = Settings.System.LOCKSCREEN_TARGETS_SHORT[mTargetIndex];
                createDialog(
                        getResources().getString(R.string.choose_action_short_title),
                        getResources().getStringArray(R.array.lockscreen_dialog_entries),
                        getResources().getStringArray(R.array.lockscreen_dialog_values));
                break;
            case LONG_ACTION:
                mTarget = 1;
                mString = Settings.System.LOCKSCREEN_TARGETS_LONG[mTargetIndex];
                createDialog(
                        getResources().getString(R.string.choose_action_long_title),
                        getResources().getStringArray(R.array.lockscreen_dialog_entries),
                        getResources().getStringArray(R.array.lockscreen_dialog_values));
                break;
            case ICON_ACTION:
                int width = 90;
                int height = width;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", width);
                intent.putExtra("aspectY", height);
                intent.putExtra("outputX", width);
                intent.putExtra("outputY", height);
                intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempFileUri());
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                Log.i(TAG, "started for result, should output to: " + getTempFileUri());
                startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON);
                break;
            case NOT_IN_ENUM:
                switch (mTarget) {
                    case 0:
                        targetActivities[mTargetIndex] = uri;
                        break;
                    case 1:
                        longActivities[mTargetIndex] = uri;
                        Toast.makeText(mContext, getProperSummary(uri)
                                + "  " + getResources().getString(R.string.action_long_save),
                                Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;

        }
        setDrawables();
    }

    @Override
    public void onTrigger(View v, final int target) {
        mTargetIndex = target;
        if (mBoolLongPress) {
            final String[] stringArray = mContext.getResources().getStringArray(
                    R.array.navring_long_dialog_entries);
            stringArray[0] = stringArray[0] + "  :  "
                    + getProperSummary(targetActivities[mTargetIndex]);
            stringArray[1] = stringArray[1] + "  :  "
                    + getProperSummary(longActivities[mTargetIndex]);
            createDialog(
                    getResources().getString(R.string.choose_action_title), stringArray,
                    getResources().getStringArray(R.array.navring_long_dialog_values));
        } else {
            final String[] stringArray = mContext.getResources().getStringArray(
                    R.array.navring_short_dialog_entries);
            stringArray[0] = stringArray[0] + "  :  "
                    + getProperSummary(targetActivities[mTargetIndex]);
            createDialog(
                    getResources().getString(R.string.choose_action_title), stringArray,
                    getResources().getStringArray(R.array.navring_short_dialog_values));
        }
    }

    @Override
    public void onGrabbed(View v, int handle) {
        if (!mIsLandscape) {
            updateVisiblity(false);
        }
    }

    @Override
    public void onReleased(View v, int handle) {
        if (!mIsLandscape) {
            updateVisiblity(true);
        }
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
    }

    public void onTargetChange(View v, final int target) {
    }

    public void createDialog(final String title, final String[] entries, final String[] values) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onValueChange(values[item]);
                dialog.dismiss();
            }
        };

        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setItems(entries, l)
                .create();

        dialog.show();
    }

    public void createMessage(final String title, final String summary) {
        AlertDialog ad = new AlertDialog.Builder(mContext).create();
        ad.setTitle(title);
        ad.setCancelable(false);
        ad.setMessage(summary);
        ad.setButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    private String getProperSummary(String uri) {

        if (TextUtils.isEmpty(uri) || AwesomeConstant.ACTION_NULL.equals(uri)) {
            return getResources().getString(R.string.none);
        }

        String newSummary = mContext.getResources().getString(R.string.none);
        AwesomeConstant stringEnum = fromString(uri);
        switch (stringEnum) {
            case ACTION_UNLOCK:
                newSummary = getResources().getString(R.string.lockscreen_unlock);
                break;
            case ACTION_CAMERA:
                newSummary = getResources().getString(R.string.lockscreen_camera);
                break;
            case ACTION_ASSIST:
                newSummary = getResources().getString(R.string.google_now);
                break;
            case ACTION_APP:
                newSummary = mPicker.getFriendlyNameForUri(uri);
                break;
        }
        return newSummary;
    }

    private int mUnlockCounter() {
        int counter = 0;
        for (int i = 0; i < 8; i++) {
            if (!TextUtils.isEmpty(targetActivities[i])) {
                if (targetActivities[i].equals(AwesomeConstant.ACTION_UNLOCK.value())) {
                    counter += 1;
                }
            }
        }
        return counter;
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mTargetIndex + ".png"));

    }

    private String getIconFileName(int index) {
        return "lockscreen_icon_" + index + ".png";
    }

    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
            }
        }
    }

    private H mHandler = new H();

    private void updateVisiblity(boolean visible) {
        if (visible) {
            mLongPressStatus.setVisibility(View.VISIBLE);
            mLockBatterySwitch.setVisibility(View.VISIBLE);
            mLockRotateSwitch.setVisibility(View.VISIBLE);
            mLockVolControlSwitch.setVisibility(View.VISIBLE);
            mLockVolWakeSwitch.setVisibility(View.VISIBLE);
            mLockPageHintSwitch.setVisibility(View.VISIBLE);
            mLockMinimizeChallangeSwitch.setVisibility(View.VISIBLE);
            mLockCarouselSwitch.setVisibility(View.VISIBLE);
            mLockAllWidgetsSwitch.setVisibility(View.VISIBLE);
            mLockUnlimitedWidgetsSwitch.setVisibility(View.VISIBLE);
            mLongPressText.setVisibility(View.VISIBLE);
            mLockBatteryText.setVisibility(View.VISIBLE);
            mLockRotateText.setVisibility(View.VISIBLE);
            mLockVolControlText.setVisibility(View.VISIBLE);
            mLockVolWakeText.setVisibility(View.VISIBLE);
            mLockPageHintText.setVisibility(View.VISIBLE);
            mLockMinimizeChallangeText.setVisibility(View.VISIBLE);
            mLockCarouselText.setVisibility(View.VISIBLE);
            mLockAllWidgetsText.setVisibility(View.VISIBLE);
            mLockUnlimitedWidgetsText.setVisibility(View.VISIBLE);
            mLockTextColorText.setVisibility(View.VISIBLE);
            mLockTextColorButton.setVisibility(View.VISIBLE);
            mHelperText.setText(getResources().getString(R.string.lockscreen_options_info));
        } else {
            mLongPressStatus.setVisibility(View.GONE);
            mLockBatterySwitch.setVisibility(View.GONE);
            mLockRotateSwitch.setVisibility(View.GONE);
            mLockVolControlSwitch.setVisibility(View.GONE);
            mLockVolWakeSwitch.setVisibility(View.GONE);
            mLockPageHintSwitch.setVisibility(View.GONE);
            mLockMinimizeChallangeSwitch.setVisibility(View.GONE);
            mLockCarouselSwitch.setVisibility(View.GONE);
            mLockAllWidgetsSwitch.setVisibility(View.GONE);
            mLockUnlimitedWidgetsSwitch.setVisibility(View.GONE);
            mLongPressText.setVisibility(View.GONE);
            mLockBatteryText.setVisibility(View.GONE);
            mLockRotateText.setVisibility(View.GONE);
            mLockVolControlText.setVisibility(View.GONE);
            mLockVolWakeText.setVisibility(View.GONE);
            mLockPageHintText.setVisibility(View.GONE);
            mLockMinimizeChallangeText.setVisibility(View.GONE);
            mLockCarouselText.setVisibility(View.GONE);
            mLockAllWidgetsText.setVisibility(View.GONE);
            mLockUnlimitedWidgetsText.setVisibility(View.GONE);
            mLockTextColorText.setVisibility(View.GONE);
            mLockTextColorButton.setVisibility(View.GONE);
            mHelperText.setText(getResources().getString(R.string.lockscreen_target_info));
        }
    }

    @Override
    public void onColorChanged(int color) {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.LOCKSCREEN_CUSTOM_TEXT_COLOR, color);
        textColor = color;
        mLockTextColorButton.setBackgroundColor(textColor);
    }

    @Override
    public void onFinishFinalAnimation() {
    }
}
