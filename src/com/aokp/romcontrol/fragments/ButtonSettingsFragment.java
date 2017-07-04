/*
 * Copyright (C) 2015 The CyanogenMod project
 * Copyright (C) 2017 The AOKP project
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

package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.aokp.romcontrol.util.ButtonBacklightBrightness;
import com.aokp.romcontrol.util.Utils;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.SeekBarPreferenceCham;

import cyanogenmod.hardware.CMHardwareManager;
import cyanogenmod.providers.CMSettings;

import java.util.List;

import org.cyanogenmod.internal.logging.CMMetricsLogger;
import org.cyanogenmod.internal.util.ScreenType;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

public class ButtonSettingsFragment extends Fragment {

    public ButtonSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.container, new ButtonSettingsPreferenceFragment())
                .commit();
    }

    public static class ButtonSettingsPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

        public ButtonSettingsPreferenceFragment() {

        }

        private static final String TAG = "ButtonSettingsFragment";

        private static final String CATEGORY_HW_KEYS = "hw_keys";
        private static final String KEY_ENABLE_HW_KEYS = "enable_hw_keys";

        private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
        private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
        private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
        private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
        private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
        private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
        private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
        private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
        private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
        private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
        private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";

        private static final String KEY_POWER_END_CALL = "power_end_call";
        private static final String KEY_HOME_ANSWER_CALL = "home_answer_call";
        private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
        private static final String KEY_VOLUME_CONTROL_RING_STREAM = "volume_keys_control_ring_stream";
        private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE = "camera_double_tap_power_gesture";
        private static final String DT2L_CAMERA_VIBRATE_CONFIG = "dt2l_camera_vibrate_config";

        private static final String CATEGORY_POWER = "power_key";
        private static final String CATEGORY_HOME = "home_key";
        private static final String CATEGORY_BACK = "back_key";
        private static final String CATEGORY_MENU = "menu_key";
        private static final String CATEGORY_ASSIST = "assist_key";
        private static final String CATEGORY_APPSWITCH = "app_switch_key";
        private static final String CATEGORY_CAMERA = "camera_key";
        private static final String CATEGORY_VOLUME = "volume_keys";
        private static final String CATEGORY_BACKLIGHT = "key_backlight";

        // Available custom actions to perform on a key press.
        // Must match values for KEY_HOME_LONG_PRESS_ACTION in:
        // cm_platform_sdk/sdk/src/java/cyanogenmod/providers/CMSettings.java
        private static final int ACTION_NOTHING = 0;
        private static final int ACTION_MENU = 1;
        private static final int ACTION_APP_SWITCH = 2;
        private static final int ACTION_SEARCH = 3;
        private static final int ACTION_VOICE_SEARCH = 4;
        private static final int ACTION_IN_APP_SEARCH = 5;
        private static final int ACTION_LAUNCH_CAMERA = 6;
        private static final int ACTION_SLEEP = 7;
        private static final int ACTION_LAST_APP = 8;
        private static final int ACTION_SPLIT_SCREEN = 9;
        private static final int ACTION_SINGLE_HAND_LEFT = 10;
        private static final int ACTION_SINGLE_HAND_RIGHT = 11;

        // Masks for checking presence of hardware keys.
        // Must match values in frameworks/base/core/res/res/values/config.xml
        public static final int KEY_MASK_HOME = 0x01;
        public static final int KEY_MASK_BACK = 0x02;
        public static final int KEY_MASK_MENU = 0x04;
        public static final int KEY_MASK_ASSIST = 0x08;
        public static final int KEY_MASK_APP_SWITCH = 0x10;
        public static final int KEY_MASK_CAMERA = 0x20;
        public static final int KEY_MASK_VOLUME = 0x40;

        private static final int DLG_KEYBOARD_ROTATION = 0;

        private static final String PREF_DISABLE_FULLSCREEN_KEYBOARD = "disable_fullscreen_keyboard";
        private static final String KEYBOARD_ROTATION_TOGGLE = "keyboard_rotation_toggle";
        private static final String KEYBOARD_ROTATION_TIMEOUT = "keyboard_rotation_timeout";
        private static final String SHOW_ENTER_KEY = "show_enter_key";

        private static final int KEYBOARD_ROTATION_TIMEOUT_DEFAULT = 5000; // 5s

        private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
        private static final String KILL_APP_LONGPRESS_TIMEOUT = "kill_app_longpress_timeout";

        private ListPreference mHomeLongPressAction;
        private ListPreference mHomeDoubleTapAction;
        private ListPreference mMenuPressAction;
        private ListPreference mMenuLongPressAction;
        private ListPreference mAssistPressAction;
        private ListPreference mAssistLongPressAction;
        private ListPreference mAppSwitchPressAction;
        private ListPreference mAppSwitchLongPressAction;
        private SwitchPreference mCameraWakeScreen;
        private SwitchPreference mCameraSleepOnRelease;
        private SwitchPreference mCameraLaunch;
        private ListPreference mVolumeKeyCursorControl;
        private SwitchPreference mVolumeWakeScreen;
        private SwitchPreference mVolumeMusicControls;
        private SwitchPreference mSwapVolumeButtons;
        private SwitchPreference mPowerEndCall;
        private SwitchPreference mHomeAnswerCall;
        private SwitchPreference mCameraDoubleTapPowerGesture;

        private SwitchPreference mEnableHwKeys;

        private SwitchPreference mDisableFullscreenKeyboard;
        private SwitchPreference mKeyboardRotationToggle;
        private ListPreference mKeyboardRotationTimeout;
        private SwitchPreference mShowEnterKey;

        private SwitchPreference mKillAppLongPressBack;
        private SeekBarPreferenceCham mKillAppLongpressTimeout;
        private SeekBarPreferenceCham mDt2lCameraVibrateConfig;

        private Handler mHandler;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.fragment_button_settings);
            ContentResolver mResolver = getActivity().getContentResolver();
            final Resources res = getResources();
            final PreferenceScreen prefScreen = getPreferenceScreen();

            final int deviceKeys = getResources().getInteger(
                    com.android.internal.R.integer.config_deviceHardwareKeys);
            final int deviceWakeKeys = getResources().getInteger(
                    com.android.internal.R.integer.config_deviceHardwareWakeKeys);

            final boolean hasPowerKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER);
            final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
            final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
            final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
            final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
            final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
            final boolean hasCameraKey = (deviceKeys & KEY_MASK_CAMERA) != 0;
            final boolean hasVolumeKeys = (deviceKeys & KEY_MASK_VOLUME) != 0;

            final boolean showHomeWake = (deviceWakeKeys & KEY_MASK_HOME) != 0;
            final boolean showBackWake = (deviceWakeKeys & KEY_MASK_BACK) != 0;
            final boolean showMenuWake = (deviceWakeKeys & KEY_MASK_MENU) != 0;
            final boolean showAssistWake = (deviceWakeKeys & KEY_MASK_ASSIST) != 0;
            final boolean showAppSwitchWake = (deviceWakeKeys & KEY_MASK_APP_SWITCH) != 0;
            final boolean showCameraWake = (deviceWakeKeys & KEY_MASK_CAMERA) != 0;
            final boolean showVolumeWake = (deviceWakeKeys & KEY_MASK_VOLUME) != 0;

            boolean hasAnyBindableKey = false;
            final PreferenceCategory powerCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_POWER);
            final PreferenceCategory homeCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
            final PreferenceCategory backCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
            final PreferenceCategory menuCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
            final PreferenceCategory assistCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
            final PreferenceCategory appSwitchCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
            final PreferenceCategory volumeCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);
            final PreferenceCategory cameraCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_CAMERA);

            // Power button ends calls.
            mPowerEndCall = (SwitchPreference) findPreference(KEY_POWER_END_CALL);

            // Double press power to launch camera.
            mCameraDoubleTapPowerGesture
                        = (SwitchPreference) findPreference(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);

            // Home button answers calls.
            mHomeAnswerCall = (SwitchPreference) findPreference(KEY_HOME_ANSWER_CALL);

            mHandler = new Handler();

            // Enable/disable hw keys
            boolean enableHwKeys = Settings.Secure.getInt(mResolver,
                    Settings.Secure.ENABLE_HW_KEYS, 1) == 1;
            mEnableHwKeys = (SwitchPreference) findPreference(KEY_ENABLE_HW_KEYS);
            mEnableHwKeys.setChecked(enableHwKeys);
            mEnableHwKeys.setOnPreferenceChangeListener(this);
            // Check if this feature is enable through device config
            if(!getResources().getBoolean(com.android.internal.R.bool.config_hwKeysPref)) {
                PreferenceCategory hwKeysPref = (PreferenceCategory)
                        getPreferenceScreen().findPreference(CATEGORY_HW_KEYS);
                getPreferenceScreen().removePreference(hwKeysPref);
            }
            final CMHardwareManager hardware = CMHardwareManager.getInstance(getActivity());

            // Kill-app long press back
            mKillAppLongPressBack = (SwitchPreference) findPreference(KILL_APP_LONGPRESS_BACK);
            mKillAppLongPressBack.setOnPreferenceChangeListener(this);
            int killAppLongPressBack = Settings.Secure.getInt(mResolver,
                    Settings.Secure.KILL_APP_LONGPRESS_BACK, 0);
            mKillAppLongPressBack.setChecked(killAppLongPressBack != 0);

            // Kill-app long press back delay
            mKillAppLongpressTimeout = (SeekBarPreferenceCham) findPreference(KILL_APP_LONGPRESS_TIMEOUT);
            int killconf = Settings.Secure.getInt(mResolver,
                    Settings.Secure.KILL_APP_LONGPRESS_TIMEOUT, 1000);
            mKillAppLongpressTimeout.setValue(killconf);
            mKillAppLongpressTimeout.setOnPreferenceChangeListener(this);

            mDt2lCameraVibrateConfig = (SeekBarPreferenceCham) findPreference(DT2L_CAMERA_VIBRATE_CONFIG);
            mDt2lCameraVibrateConfig.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.DT2L_CAMERA_VIBRATE_CONFIG, 1));
            mDt2lCameraVibrateConfig.setOnPreferenceChangeListener(this);

            if (hasPowerKey) {
                if (!Utils.isVoiceCapable(getActivity())) {
                    powerCategory.removePreference(mPowerEndCall);
                    mPowerEndCall = null;
                    prefScreen.removePreference(powerCategory);
                }
                if (mCameraDoubleTapPowerGesture != null &&
                        isCameraDoubleTapPowerGestureAvailable(getResources())) {
                    // Update double tap power to launch camera if available.
                    mCameraDoubleTapPowerGesture.setOnPreferenceChangeListener(this);
                    int cameraDoubleTapPowerDisabled = Settings.Secure.getInt(mResolver,
                            Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0);
                    mCameraDoubleTapPowerGesture.setChecked(cameraDoubleTapPowerDisabled == 0);
                } else {
                    powerCategory.removePreference(mCameraDoubleTapPowerGesture);
                    mCameraDoubleTapPowerGesture = null;
                }
            } else {
                prefScreen.removePreference(powerCategory);
            }

            if (hasHomeKey) {
                if (!showHomeWake) {
                    homeCategory.removePreference(findPreference(CMSettings.System.HOME_WAKE_SCREEN));
                }

                if (!Utils.isVoiceCapable(getActivity())) {
                    homeCategory.removePreference(mHomeAnswerCall);
                    mHomeAnswerCall = null;
                }

                int defaultLongPressAction = res.getInteger(
                        com.android.internal.R.integer.config_longPressOnHomeBehavior);
                if (defaultLongPressAction < ACTION_NOTHING ||
                        defaultLongPressAction > ACTION_LAST_APP) {
                    defaultLongPressAction = ACTION_NOTHING;
                }

                int defaultDoubleTapAction = res.getInteger(
                        com.android.internal.R.integer.config_doubleTapOnHomeBehavior);
                if (defaultDoubleTapAction < ACTION_NOTHING ||
                        defaultDoubleTapAction > ACTION_LAST_APP) {
                    defaultDoubleTapAction = ACTION_NOTHING;
                }

                int longPressAction = CMSettings.System.getInt(mResolver,
                        CMSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                        defaultLongPressAction);
                mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS, longPressAction);

                int doubleTapAction = CMSettings.System.getInt(mResolver,
                        CMSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                        defaultDoubleTapAction);
                mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP, doubleTapAction);

                hasAnyBindableKey = true;
            } else {
                prefScreen.removePreference(homeCategory);
            }

            if (hasBackKey) {
                if (!showBackWake) {
                    backCategory.removePreference(findPreference(CMSettings.System.BACK_WAKE_SCREEN));
                    prefScreen.removePreference(backCategory);
                }
            } else {
                prefScreen.removePreference(backCategory);
            }

            if (hasMenuKey) {
                if (!showMenuWake) {
                    menuCategory.removePreference(findPreference(CMSettings.System.MENU_WAKE_SCREEN));
                }

                int pressAction = CMSettings.System.getInt(mResolver,
                        CMSettings.System.KEY_MENU_ACTION, ACTION_MENU);
                mMenuPressAction = initList(KEY_MENU_PRESS, pressAction);

                int longPressAction = CMSettings.System.getInt(mResolver,
                            CMSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                            hasAssistKey ? ACTION_NOTHING : ACTION_SEARCH);
                mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS, longPressAction);

                hasAnyBindableKey = true;
            } else {
                prefScreen.removePreference(menuCategory);
            }

            if (hasAssistKey) {
                if (!showAssistWake) {
                    assistCategory.removePreference(findPreference(CMSettings.System.ASSIST_WAKE_SCREEN));
                }

                int pressAction = CMSettings.System.getInt(mResolver,
                        CMSettings.System.KEY_ASSIST_ACTION, ACTION_SEARCH);
                mAssistPressAction = initList(KEY_ASSIST_PRESS, pressAction);

                int longPressAction = CMSettings.System.getInt(mResolver,
                        CMSettings.System.KEY_ASSIST_LONG_PRESS_ACTION, ACTION_VOICE_SEARCH);
                mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS, longPressAction);

                hasAnyBindableKey = true;
            } else {
                prefScreen.removePreference(assistCategory);
            }

            if (hasAppSwitchKey) {
                if (!showAppSwitchWake) {
                    appSwitchCategory.removePreference(findPreference(
                            CMSettings.System.APP_SWITCH_WAKE_SCREEN));
                }

                int pressAction = CMSettings.System.getInt(mResolver,
                        CMSettings.System.KEY_APP_SWITCH_ACTION, ACTION_APP_SWITCH);
                mAppSwitchPressAction = initList(KEY_APP_SWITCH_PRESS, pressAction);

                int longPressAction = CMSettings.System.getInt(mResolver,
                        CMSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION, ACTION_SPLIT_SCREEN);
                mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS, longPressAction);

                hasAnyBindableKey = true;
            } else {
                prefScreen.removePreference(appSwitchCategory);
            }

            if (hasCameraKey) {
                mCameraWakeScreen = (SwitchPreference) findPreference(CMSettings.System.CAMERA_WAKE_SCREEN);
                mCameraSleepOnRelease =
                        (SwitchPreference) findPreference(CMSettings.System.CAMERA_SLEEP_ON_RELEASE);
                mCameraLaunch = (SwitchPreference) findPreference(CMSettings.System.CAMERA_LAUNCH);

                if (!showCameraWake) {
                    prefScreen.removePreference(mCameraWakeScreen);
                }
                // Only show 'Camera sleep on release' if the device has a focus key
                if (res.getBoolean(com.android.internal.R.bool.config_singleStageCameraKey)) {
                    prefScreen.removePreference(mCameraSleepOnRelease);
                }
            } else {
                prefScreen.removePreference(cameraCategory);
            }

            if (Utils.hasVolumeRocker(getActivity())) {
                if (!showVolumeWake) {
                    volumeCategory.removePreference(findPreference(CMSettings.System.VOLUME_WAKE_SCREEN));
                }

                int cursorControlAction = Settings.System.getInt(mResolver,
                        Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
                mVolumeKeyCursorControl = initList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                        cursorControlAction);

                int swapVolumeKeys = CMSettings.System.getInt(mResolver,
                        CMSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0);
                mSwapVolumeButtons = (SwitchPreference)
                        prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS);
                if (mSwapVolumeButtons != null) {
                    mSwapVolumeButtons.setChecked(swapVolumeKeys > 0);
                }
            } else {
                prefScreen.removePreference(volumeCategory);
            }

            final ButtonBacklightBrightness backlight =
                    (ButtonBacklightBrightness) findPreference(KEY_BUTTON_BACKLIGHT);
            if (!backlight.isButtonSupported() && !backlight.isKeyboardSupported()) {
                prefScreen.removePreference(backlight);
            }

            if (mCameraWakeScreen != null) {
                if (mCameraSleepOnRelease != null && !getResources().getBoolean(
                        com.android.internal.R.bool.config_singleStageCameraKey)) {
                    mCameraSleepOnRelease.setDependency(CMSettings.System.CAMERA_WAKE_SCREEN);
                }
            }
            mVolumeWakeScreen = (SwitchPreference) findPreference(CMSettings.System.VOLUME_WAKE_SCREEN);
            mVolumeMusicControls = (SwitchPreference) findPreference(KEY_VOLUME_MUSIC_CONTROLS);

            if (mVolumeWakeScreen != null) {
                if (mVolumeMusicControls != null) {
                    mVolumeMusicControls.setDependency(CMSettings.System.VOLUME_WAKE_SCREEN);
                    mVolumeWakeScreen.setDisableDependentsState(true);
                }
            }

            mDisableFullscreenKeyboard =
                (SwitchPreference) findPreference(PREF_DISABLE_FULLSCREEN_KEYBOARD);
            mDisableFullscreenKeyboard.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.DISABLE_FULLSCREEN_KEYBOARD, 0) == 1);
            mDisableFullscreenKeyboard.setOnPreferenceChangeListener(this);

            mKeyboardRotationToggle = (SwitchPreference) findPreference(KEYBOARD_ROTATION_TOGGLE);
            mKeyboardRotationToggle.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.KEYBOARD_ROTATION_TIMEOUT, 0) > 0);
            mKeyboardRotationToggle.setOnPreferenceChangeListener(this);

            mKeyboardRotationTimeout = (ListPreference) findPreference(KEYBOARD_ROTATION_TIMEOUT);
            mKeyboardRotationTimeout.setOnPreferenceChangeListener(this);
            updateRotationTimeout(Settings.System.getInt(
                    mResolver, Settings.System.KEYBOARD_ROTATION_TIMEOUT,
                    KEYBOARD_ROTATION_TIMEOUT_DEFAULT));

            mShowEnterKey = (SwitchPreference) findPreference(SHOW_ENTER_KEY);
            mShowEnterKey.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.FORMAL_TEXT_INPUT, 0) == 1);
            mShowEnterKey.setOnPreferenceChangeListener(this);

            updateDisableHwKeysOption();
        }

        public void updateRotationTimeout(int timeout) {
            if (timeout == 0) {
                timeout = KEYBOARD_ROTATION_TIMEOUT_DEFAULT;
            }
            mKeyboardRotationTimeout.setValue(Integer.toString(timeout));
            mKeyboardRotationTimeout.setSummary(
                getString(R.string.keyboard_rotation_timeout_summary,
                mKeyboardRotationTimeout.getEntry()));
        }

        @Override
        public void onResume() {
            super.onResume();
            ContentResolver mResolver = getActivity().getContentResolver();

            // Power button ends calls.
            if (mPowerEndCall != null) {
                final int incallPowerBehavior = Settings.Secure.getInt(mResolver,
                        Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                        Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
                final boolean powerButtonEndsCall =
                        (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
                mPowerEndCall.setChecked(powerButtonEndsCall);
            }

            // Home button answers calls.
            if (mHomeAnswerCall != null) {
                final int incallHomeBehavior = CMSettings.Secure.getInt(mResolver,
                        CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR,
                        CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DEFAULT);
                final boolean homeButtonAnswersCall =
                    (incallHomeBehavior == CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER);
                mHomeAnswerCall.setChecked(homeButtonAnswersCall);
            }
        }

        private ListPreference initList(String key, int value) {
            ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
            if (list == null) return null;
            list.setValue(Integer.toString(value));
            list.setSummary(list.getEntry());
            list.setOnPreferenceChangeListener(this);
            return list;
        }

        private void handleListChange(ListPreference pref, Object newValue, String setting) {
            String value = (String) newValue;
            int index = pref.findIndexOfValue(value);
            pref.setSummary(pref.getEntries()[index]);
            CMSettings.System.putInt(getActivity().getContentResolver(), setting, Integer.valueOf(value));
        }

        private void handleSystemActionListChange(ListPreference pref, Object newValue, String setting) {
            String value = (String) newValue;
            int index = pref.findIndexOfValue(value);
            pref.setSummary(pref.getEntries()[index]);
            Settings.System.putInt(getActivity().getContentResolver(), setting, Integer.valueOf(value));
        }

        protected int getMetricsCategory() {
            return CMMetricsLogger.BUTTON_SETTINGS;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver mResolver = getActivity().getContentResolver();

            if (preference == mEnableHwKeys) {
                boolean hWkeysValue = (Boolean) newValue;
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.ENABLE_HW_KEYS, hWkeysValue ? 1 : 0);
                writeDisableHwKeysOption(getActivity(), hWkeysValue);
                updateDisableHwKeysOption();
                return true;
            } else if (preference == mHomeLongPressAction) {
                handleListChange(mHomeLongPressAction, newValue,
                        CMSettings.System.KEY_HOME_LONG_PRESS_ACTION);
                return true;
            } else if (preference == mHomeDoubleTapAction) {
                handleListChange(mHomeDoubleTapAction, newValue,
                        CMSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
                return true;
            } else if (preference == mMenuPressAction) {
                handleListChange(mMenuPressAction, newValue,
                        CMSettings.System.KEY_MENU_ACTION);
                return true;
            } else if (preference == mMenuLongPressAction) {
                handleListChange(mMenuLongPressAction, newValue,
                        CMSettings.System.KEY_MENU_LONG_PRESS_ACTION);
                return true;
            } else if (preference == mAssistPressAction) {
                handleListChange(mAssistPressAction, newValue,
                        CMSettings.System.KEY_ASSIST_ACTION);
                return true;
            } else if (preference == mAssistLongPressAction) {
                handleListChange(mAssistLongPressAction, newValue,
                        CMSettings.System.KEY_ASSIST_LONG_PRESS_ACTION);
                return true;
            } else if (preference == mAppSwitchPressAction) {
                handleListChange(mAppSwitchPressAction, newValue,
                        CMSettings.System.KEY_APP_SWITCH_ACTION);
                return true;
            } else if (preference == mAppSwitchLongPressAction) {
                handleListChange(mAppSwitchLongPressAction, newValue,
                        CMSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
                return true;
            } else if (preference == mVolumeKeyCursorControl) {
                handleSystemActionListChange(mVolumeKeyCursorControl, newValue,
                        Settings.System.VOLUME_KEY_CURSOR_CONTROL);
                return true;
            } else if (preference == mCameraDoubleTapPowerGesture) {
                boolean value = (Boolean) newValue;
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                        value ? 0 : 1 /* Backwards because setting is for disabling */);
            } else if (preference == mDisableFullscreenKeyboard) {
                Settings.System.putInt(mResolver,
                        Settings.System.DISABLE_FULLSCREEN_KEYBOARD,
                        (Boolean) newValue ? 1 : 0);
                return true;
            } else if (preference == mKeyboardRotationToggle) {
                boolean isAutoRotate = (Settings.System.getIntForUser(mResolver,
                        Settings.System.ACCELEROMETER_ROTATION, 0,
                        UserHandle.USER_CURRENT) == 1);
                if (isAutoRotate && (Boolean) newValue) {
                    showDialogInner(DLG_KEYBOARD_ROTATION);
                }
                Settings.System.putInt(mResolver,
                        Settings.System.KEYBOARD_ROTATION_TIMEOUT,
                        (Boolean) newValue ? KEYBOARD_ROTATION_TIMEOUT_DEFAULT : 0);
                updateRotationTimeout(KEYBOARD_ROTATION_TIMEOUT_DEFAULT);
                return true;
            } else if (preference == mShowEnterKey) {
                Settings.System.putInt(mResolver,
                        Settings.System.FORMAL_TEXT_INPUT,
                        (Boolean) newValue ? 1 : 0);
                return true;
            } else if (preference == mKeyboardRotationTimeout) {
                int timeout = Integer.parseInt((String) newValue);
                Settings.System.putInt(mResolver,
                        Settings.System.KEYBOARD_ROTATION_TIMEOUT, timeout);
                updateRotationTimeout(timeout);
                return true;
            } else if (preference == mKillAppLongPressBack) {
                boolean value = (Boolean) newValue;
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.KILL_APP_LONGPRESS_BACK, value ? 1 : 0);
                return true;
            } else if (preference == mKillAppLongpressTimeout) {
                int killconf = (Integer) newValue;
                Settings.Secure.putInt(mResolver,
                        Settings.Secure.KILL_APP_LONGPRESS_TIMEOUT, killconf);
                return true;
            } else if (preference == mDt2lCameraVibrateConfig) {
                int dt2lcameravib = (Integer) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.DT2L_CAMERA_VIBRATE_CONFIG, dt2lcameravib * 10);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mSwapVolumeButtons) {
                int value = mSwapVolumeButtons.isChecked()
                        ? (ScreenType.isTablet(getActivity()) ? 2 : 1) : 0;
            if (value == 2) {
                Display defaultDisplay = getActivity().getWindowManager().getDefaultDisplay();

                DisplayInfo displayInfo = new DisplayInfo();
                defaultDisplay.getDisplayInfo(displayInfo);

                // Not all tablets are landscape
                if (displayInfo.getNaturalWidth() < displayInfo.getNaturalHeight()) {
                    value = 1;
                }
            }
                CMSettings.System.putInt(getActivity().getContentResolver(),
                    CMSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
            } else if (preference == mPowerEndCall) {
                handleTogglePowerButtonEndsCallPreferenceClick();
                return true;
            } else if (preference == mHomeAnswerCall) {
                handleToggleHomeButtonAnswersCallPreferenceClick();
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private static void writeDisableHwKeysOption(Context context, boolean enabled) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final int defaultBrightness = context.getResources().getInteger(
                    com.android.internal.R.integer.config_buttonBrightnessSettingDefault);

            Settings.Secure.putInt(context.getContentResolver(),
                    Settings.Secure.ENABLE_HW_KEYS, enabled ? 1 : 0);

            CMHardwareManager hardware = CMHardwareManager.getInstance(context);
            hardware.set(CMHardwareManager.FEATURE_KEY_DISABLE, !enabled);

            /* Save/restore button timeouts to disable them in softkey mode */
            if (!enabled) {
                CMSettings.Secure.putInt(context.getContentResolver(),
                        CMSettings.Secure.BUTTON_BRIGHTNESS, 0);
            } else {
                int oldBright = prefs.getInt(ButtonBacklightBrightness.KEY_BUTTON_BACKLIGHT,
                        defaultBrightness);
                CMSettings.Secure.putInt(context.getContentResolver(),
                        CMSettings.Secure.BUTTON_BRIGHTNESS, oldBright);
            }
        }

        private void updateDisableHwKeysOption() {
            boolean enabled = Settings.Secure.getInt(getActivity().getContentResolver(),
                    Settings.Secure.ENABLE_HW_KEYS, 1) == 1;
            mEnableHwKeys.setChecked(enabled);
            final PreferenceScreen prefScreen = getPreferenceScreen();

            /* Disable hw-key options if they're disabled */
            final PreferenceCategory homeCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
            final PreferenceCategory backCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
            final PreferenceCategory menuCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
            final PreferenceCategory assistCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
            final PreferenceCategory appSwitchCategory =
                    (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
            final ButtonBacklightBrightness backlight =
                    (ButtonBacklightBrightness) prefScreen.findPreference(KEY_BUTTON_BACKLIGHT);

            /* Toggle backlight control depending on hw keys state, force it to
               off if enabling */
            if (backlight != null) {
                backlight.setEnabled(enabled);
                backlight.updateSummary();
            }

            if (homeCategory != null) {
                homeCategory.setEnabled(enabled);
            }
            if (backCategory != null) {
                backCategory.setEnabled(enabled);
            }
            if (menuCategory != null) {
                menuCategory.setEnabled(enabled);
            }
            if (assistCategory != null) {
                assistCategory.setEnabled(enabled);
            }
            if (appSwitchCategory != null) {
                appSwitchCategory.setEnabled(enabled);
            }
        }

        public static void restoreKeyDisabler(Context context) {
            CMHardwareManager hardware = CMHardwareManager.getInstance(context);
            if (!hardware.isSupported(CMHardwareManager.FEATURE_KEY_DISABLE)) {
                return;
            }

            writeDisableHwKeysOption(context, Settings.System.getInt(context.getContentResolver(),
                    Settings.Secure.ENABLE_HW_KEYS, 1) == 1);
        }

        private void handleTogglePowerButtonEndsCallPreferenceClick() {
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, (mPowerEndCall.isChecked()
                            ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                            : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
        }

        private void handleToggleHomeButtonAnswersCallPreferenceClick() {
            CMSettings.Secure.putInt(getActivity().getContentResolver(),
                    CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR, (mHomeAnswerCall.isChecked()
                            ? CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER
                            : CMSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DO_NOTHING));
        }

        private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
            return res.getBoolean(
                    com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
        }

        private void showDialogInner(int id) {
            DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
            newFragment.setTargetFragment(this, 0);
            newFragment.show(getFragmentManager(), "dialog " + id);
        }

        public static class MyAlertDialogFragment extends DialogFragment {

            public static MyAlertDialogFragment newInstance(int id) {
                MyAlertDialogFragment frag = new MyAlertDialogFragment();
                Bundle args = new Bundle();
                args.putInt("id", id);
                frag.setArguments(args);
                return frag;
            }

            ButtonSettingsFragment.ButtonSettingsPreferenceFragment getOwner() {
                return (ButtonSettingsFragment.ButtonSettingsPreferenceFragment) getTargetFragment();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int id = getArguments().getInt("id");
                switch (id) {
                    case DLG_KEYBOARD_ROTATION:
                        return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.attention)
                        .setMessage(R.string.keyboard_rotation_dialog)
                        .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create();
                }
                throw new IllegalArgumentException("unknown id " + id);
            }

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        }
    }
}
