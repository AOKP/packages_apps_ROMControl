/*
* Copyright (C) 2015 The Android Open Kang Project
* Copyright (C) 2013 SlimRoms Project
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

package com.aokp.romcontrol.fragments.sound;

import android.app.Activity;
import android.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;

public class SoundSettingsFragment extends Fragment {

    public SoundSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sound_settings_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.sound_settings_main, new SoundSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class SoundSettingsPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {

        public SoundSettingsPreferenceFragment() {

        }

        private static final String TAG = "SoundSettingsPreferenceFragment";

        private static final int DLG_SAFE_HEADSET_VOLUME = 0;
        private static final int DLG_CAMERA_SOUND = 1;

        private static final String KEY_SAFE_HEADSET_VOLUME = "safe_headset_volume";
        private static final String PREF_LESS_NOTIFICATION_SOUNDS = "less_notification_sounds";

        private SwitchPreference mSafeHeadsetVolume;
        private ListPreference mAnnoyingNotifications;
        private SwitchPreference mCameraSounds;

        private static final String KEY_CAMERA_SOUNDS = "camera_sounds";
        private static final String PROP_CAMERA_SOUND = "persist.sys.camera-sound";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_sound_settings);
            PreferenceScreen prefSet = getPreferenceScreen();

            mSafeHeadsetVolume = (SwitchPreference) findPreference(KEY_SAFE_HEADSET_VOLUME);
            mSafeHeadsetVolume.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.SAFE_HEADSET_VOLUME, 1) != 0);
            mSafeHeadsetVolume.setOnPreferenceChangeListener(this);

            mAnnoyingNotifications = (ListPreference) findPreference(PREF_LESS_NOTIFICATION_SOUNDS);
            int notificationThreshold = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD,
                    0);
            mAnnoyingNotifications.setValue(Integer.toString(notificationThreshold));
            mAnnoyingNotifications.setOnPreferenceChangeListener(this);
            mCameraSounds = (SwitchPreference) findPreference(KEY_CAMERA_SOUNDS);
            mCameraSounds.setChecked(SystemProperties.getBoolean(PROP_CAMERA_SOUND, true));
            mCameraSounds.setOnPreferenceChangeListener(this);
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
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public boolean onPreferenceChange(Preference preference, Object objValue) {
            final String key = preference.getKey();
            if (KEY_SAFE_HEADSET_VOLUME.equals(key)) {
                if ((Boolean) objValue) {
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.SAFE_HEADSET_VOLUME, 1);
                } else {
                    showDialogInner(DLG_SAFE_HEADSET_VOLUME);
                }
            }
            if (PREF_LESS_NOTIFICATION_SOUNDS.equals(key)) {
                final int val = Integer.valueOf((String) objValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, val);
            }
            if (KEY_CAMERA_SOUNDS.equals(key)) {
               if ((Boolean) objValue) {
                   SystemProperties.set(PROP_CAMERA_SOUND, "1");
               } else {
                   showDialogInner(DLG_CAMERA_SOUND);
               }
            }
            return true;
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

            SoundSettingsFragment.SoundSettingsPreferenceFragment getOwner() {
                return (SoundSettingsFragment.SoundSettingsPreferenceFragment) getTargetFragment();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int id = getArguments().getInt("id");
                switch (id) {
                    case DLG_SAFE_HEADSET_VOLUME:
                        return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.attention)
                        .setMessage(R.string.safe_headset_volume_warning_dialog_text)
                        .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.System.putInt(getOwner().getActivity().getContentResolver(),
                                        Settings.System.SAFE_HEADSET_VOLUME, 0);

                                }
                        })
                        .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
                    case DLG_CAMERA_SOUND:
                        return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.attention)
                        .setMessage(R.string.camera_sound_warning_dialog_text)
                        .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SystemProperties.set(PROP_CAMERA_SOUND, "0");
                            }
                        })
                        .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create();
                }
                throw new IllegalArgumentException("unknown id " + id);
            }

            @Override
            public void onCancel(DialogInterface dialog) {
                int id = getArguments().getInt("id");
                switch (id) {
                    case DLG_SAFE_HEADSET_VOLUME:
                        getOwner().mSafeHeadsetVolume.setChecked(true);
                        break;
                    case DLG_CAMERA_SOUND:
                        getOwner().mCameraSounds.setChecked(true);
                        break;
                }
            }
        }
    }
}
