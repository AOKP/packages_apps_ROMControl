/*
 * Copyright (C) 2014 AOKP
 *
 * Modified by crDroid Android
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

package com.aokp.romcontrol.fragments.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.aokp.romcontrol.R;

import com.android.internal.util.crdroid.AwesomeAnimationHelper;

import java.util.Arrays;

public class AnimationControls extends Fragment {

    public AnimationControls() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_aokp_animation_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.aokp_animation_main, new AnimationControlsSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class AnimationControlsSettingsPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

        public AnimationControlsSettingsPreferenceFragment() {

        }

        private static final String ACTIVITY_OPEN = "activity_open";
        private static final String ACTIVITY_CLOSE = "activity_close";
        private static final String TASK_OPEN = "task_open";
        private static final String TASK_CLOSE = "task_close";
        private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
        private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
        private static final String ANIMATION_DURATION = "animation_duration";
        private static final String WALLPAPER_OPEN = "wallpaper_open";
        private static final String WALLPAPER_CLOSE = "wallpaper_close";
        private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
        private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";
        private static final String TASK_OPEN_BEHIND = "task_open_behind";

        ListPreference mActivityOpenPref;
        ListPreference mActivityClosePref;
        ListPreference mTaskOpenPref;
        ListPreference mTaskClosePref;
        ListPreference mTaskMoveToFrontPref;
        ListPreference mTaskMoveToBackPref;
        ListPreference mWallpaperOpen;
        ListPreference mWallpaperClose;
        ListPreference mWallpaperIntraOpen;
        ListPreference mWallpaperIntraClose;
        ListPreference mTaskOpenBehind;
        AnimBarPreference mAnimationDuration;

        private int[] mAnimations;
        private String[] mAnimationsStrings;
        private String[] mAnimationsNum;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            setTitle(R.string.aokp_animation_title);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_aokp_animation_controls);

            PreferenceScreen prefSet = getPreferenceScreen();
            mAnimations = AwesomeAnimationHelper.getAnimationsList();
            int animqty = mAnimations.length;
            mAnimationsStrings = new String[animqty];
            mAnimationsNum = new String[animqty];
            for (int i = 0; i < animqty; i++) {
                mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(getActivity().getApplicationContext(), mAnimations[i]);
                mAnimationsNum[i] = String.valueOf(mAnimations[i]);
            }

            mActivityOpenPref = (ListPreference) findPreference(ACTIVITY_OPEN);
            mActivityOpenPref.setOnPreferenceChangeListener(this);
            mActivityOpenPref.setSummary(getProperSummary(mActivityOpenPref));
            mActivityOpenPref.setEntries(mAnimationsStrings);
            mActivityOpenPref.setEntryValues(mAnimationsNum);

            mActivityClosePref = (ListPreference) findPreference(ACTIVITY_CLOSE);
            mActivityClosePref.setOnPreferenceChangeListener(this);
            mActivityClosePref.setSummary(getProperSummary(mActivityClosePref));
            mActivityClosePref.setEntries(mAnimationsStrings);
            mActivityClosePref.setEntryValues(mAnimationsNum);

            mTaskOpenPref = (ListPreference) findPreference(TASK_OPEN);
            mTaskOpenPref.setOnPreferenceChangeListener(this);
            mTaskOpenPref.setSummary(getProperSummary(mTaskOpenPref));
            mTaskOpenPref.setEntries(mAnimationsStrings);
            mTaskOpenPref.setEntryValues(mAnimationsNum);

            mTaskClosePref = (ListPreference) findPreference(TASK_CLOSE);
            mTaskClosePref.setOnPreferenceChangeListener(this);
            mTaskClosePref.setSummary(getProperSummary(mTaskClosePref));
            mTaskClosePref.setEntries(mAnimationsStrings);
            mTaskClosePref.setEntryValues(mAnimationsNum);

            mTaskMoveToFrontPref = (ListPreference) findPreference(TASK_MOVE_TO_FRONT);
            mTaskMoveToFrontPref.setOnPreferenceChangeListener(this);
            mTaskMoveToFrontPref.setSummary(getProperSummary(mTaskMoveToFrontPref));
            mTaskMoveToFrontPref.setEntries(mAnimationsStrings);
            mTaskMoveToFrontPref.setEntryValues(mAnimationsNum);

            mTaskMoveToBackPref = (ListPreference) findPreference(TASK_MOVE_TO_BACK);
            mTaskMoveToBackPref.setOnPreferenceChangeListener(this);
            mTaskMoveToBackPref.setSummary(getProperSummary(mTaskMoveToBackPref));
            mTaskMoveToBackPref.setEntries(mAnimationsStrings);
            mTaskMoveToBackPref.setEntryValues(mAnimationsNum);

            mWallpaperOpen = (ListPreference) findPreference(WALLPAPER_OPEN);
            mWallpaperOpen.setOnPreferenceChangeListener(this);
            mWallpaperOpen.setSummary(getProperSummary(mWallpaperOpen));
            mWallpaperOpen.setEntries(mAnimationsStrings);
            mWallpaperOpen.setEntryValues(mAnimationsNum);

            mWallpaperClose = (ListPreference) findPreference(WALLPAPER_CLOSE);
            mWallpaperClose.setOnPreferenceChangeListener(this);
            mWallpaperClose.setSummary(getProperSummary(mWallpaperClose));
            mWallpaperClose.setEntries(mAnimationsStrings);
            mWallpaperClose.setEntryValues(mAnimationsNum);

            mWallpaperIntraOpen = (ListPreference) findPreference(WALLPAPER_INTRA_OPEN);
            mWallpaperIntraOpen.setOnPreferenceChangeListener(this);
            mWallpaperIntraOpen.setSummary(getProperSummary(mWallpaperIntraOpen));
            mWallpaperIntraOpen.setEntries(mAnimationsStrings);
            mWallpaperIntraOpen.setEntryValues(mAnimationsNum);

            mWallpaperIntraClose = (ListPreference) findPreference(WALLPAPER_INTRA_CLOSE);
            mWallpaperIntraClose.setOnPreferenceChangeListener(this);
            mWallpaperIntraClose.setSummary(getProperSummary(mWallpaperIntraClose));
            mWallpaperIntraClose.setEntries(mAnimationsStrings);
            mWallpaperIntraClose.setEntryValues(mAnimationsNum);

            mTaskOpenBehind = (ListPreference) findPreference(TASK_OPEN_BEHIND);
            mTaskOpenBehind.setOnPreferenceChangeListener(this);
            mTaskOpenBehind.setSummary(getProperSummary(mTaskOpenBehind));
            mTaskOpenBehind.setEntries(mAnimationsStrings);
            mTaskOpenBehind.setEntryValues(mAnimationsNum);

            int defaultDuration = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.ANIMATION_CONTROLS_DURATION, 0);
            mAnimationDuration = (AnimBarPreference) findPreference(ANIMATION_DURATION);
            mAnimationDuration.setInitValue((int) (defaultDuration));
            mAnimationDuration.setOnPreferenceChangeListener(this);
            return prefSet;
        }

        public void setTitle(int resId) {
            getActivity().setTitle(resId);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean result = false;
            if (preference == mActivityOpenPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[0], val);
            } else if (preference == mActivityClosePref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[1], val);
            } else if (preference == mTaskOpenPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[2], val);
            } else if (preference == mTaskClosePref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[3], val);
            } else if (preference == mTaskMoveToFrontPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[4], val);
            } else if (preference == mTaskMoveToBackPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[5], val);
            } else if (preference == mWallpaperOpen) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[6], val);
            } else if (preference == mWallpaperClose) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[7], val);
            } else if (preference == mWallpaperIntraOpen) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[8], val);
            } else if (preference == mWallpaperIntraClose) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[9], val);
            } else if (preference == mTaskOpenBehind) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], val);
            } else if (preference == mAnimationDuration) {
                int val = Integer.parseInt((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ANIMATION_CONTROLS_DURATION, val);
            }
            preference.setSummary(getProperSummary(preference));
            return result;
        }

        private String getProperSummary(Preference preference) {
            String mString = "";
            if (preference == mActivityOpenPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[0];
            } else if (preference == mActivityClosePref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[1];
            } else if (preference == mTaskOpenPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[2];
            } else if (preference == mTaskClosePref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[3];
            } else if (preference == mTaskMoveToFrontPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[4];
            } else if (preference == mTaskMoveToBackPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[5];
            } else if (preference == mWallpaperOpen) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[6];
            } else if (preference == mWallpaperClose) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[7];
            } else if (preference == mWallpaperIntraOpen) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[8];
            } else if (preference == mWallpaperIntraClose) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[9];
            } else if (preference == mTaskOpenBehind) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[10];
            }

            int mNum = Settings.System.getInt(getActivity().getContentResolver(), mString, 0);
            return mAnimationsStrings[mNum];
        }
    }
}
