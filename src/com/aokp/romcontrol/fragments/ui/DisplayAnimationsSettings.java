/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import com.aokp.romcontrol.R;

public class DisplayAnimationsSettings extends Fragment {

    public DisplayAnimationsSettings() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_display_animation_main, container, false);

        Resources res = getResources();
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.display_animation_main, new DisplayAnimationsSettingsPreferenceFragment())
                .commit();
        return v;
    }

    public static class DisplayAnimationsSettingsPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

        public DisplayAnimationsSettingsPreferenceFragment() {

        }

        private static final String TAG = "DisplayAnimationsSettingsPreference";

        private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
        private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";

        private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
        private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
        private static final String SCROLLINGCACHE_DEFAULT = "1";
        private static final String PREF_TRANSITION_ANIMATION = "disable_transition_animations";
        private static final String LIST_SCREEN_OFF_ANIMATION = "screen_off_animation";

        private ListPreference mListViewAnimation;
        private ListPreference mListViewInterpolator;
        private ListPreference mScrollingCachePref;
        private SwitchPreference mTransitionAnimations;
        private ListPreference mScreenOffAnimation;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            createCustomView();
        }

        private PreferenceScreen createCustomView() {
            ContentResolver resolver = getActivity().getContentResolver();

            addPreferencesFromResource(R.xml.fragment_display_animations_settings);
            PreferenceScreen prefSet = getPreferenceScreen();

            // ListView Animations
            mListViewAnimation = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_ANIMATION);
            int listviewanimation = Settings.System.getInt(resolver,
                    Settings.System.LISTVIEW_ANIMATION, 0);
            mListViewAnimation.setValue(String.valueOf(listviewanimation));
            mListViewAnimation.setSummary(mListViewAnimation.getEntry());
            mListViewAnimation.setOnPreferenceChangeListener(this);

            mListViewInterpolator = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_INTERPOLATOR);
            int listviewinterpolator = Settings.System.getInt(resolver,
                    Settings.System.LISTVIEW_INTERPOLATOR, 0);
            mListViewInterpolator.setValue(String.valueOf(listviewinterpolator));
            mListViewInterpolator.setSummary(mListViewInterpolator.getEntry());
            mListViewInterpolator.setOnPreferenceChangeListener(this);
            mListViewInterpolator.setEnabled(listviewanimation > 0);

            // Scrolling cache
            mScrollingCachePref = (ListPreference) prefSet.findPreference(SCROLLINGCACHE_PREF);
            mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                    SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
            mScrollingCachePref.setOnPreferenceChangeListener(this);

            mTransitionAnimations = (SwitchPreference) findPreference(PREF_TRANSITION_ANIMATION);
            mTransitionAnimations.setChecked(Settings.System.getInt(resolver,
                    Settings.System.DISABLE_TRANSITION_ANIMATIONS, 1) != 0);
            mTransitionAnimations.setOnPreferenceChangeListener(this);

            //ScreenOff Animation
            mScreenOffAnimation = (ListPreference) prefSet.findPreference(LIST_SCREEN_OFF_ANIMATION);
            mScreenOffAnimation.setValue(SystemProperties.get(SCREEN_OFF_ANIMATION,
                    SystemProperties.get(SCREEN_OFF_ANIMATION, 0)));
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry());
            mScreenOffAnimation.setOnPreferenceChangeListener(this);

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
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object objValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mListViewAnimation) {
                int value = Integer.parseInt((String) objValue);
                int index = mListViewAnimation.findIndexOfValue((String) objValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_ANIMATION,
                        value);
                mListViewAnimation.setSummary(mListViewAnimation.getEntries()[index]);
                mListViewInterpolator.setEnabled(value > 0);
                return true;
            } else if (preference == mListViewInterpolator) {
                int value = Integer.parseInt((String) objValue);
                int index = mListViewInterpolator.findIndexOfValue((String) objValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_INTERPOLATOR,
                        value);
                mListViewInterpolator.setSummary(mListViewInterpolator.getEntries()[index]);
                return true;
            } else if (preference == mScrollingCachePref) {
                if (objValue != null) {
                    SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String)objValue);
                return true;
                }
            } else if (preference == mTransitionAnimations) {
                boolean value = (Boolean) objValue;
                Settings.System.putInt(resolver,
                        Settings.System.DISABLE_TRANSITION_ANIMATIONS, value ? 1 : 0);
                return true;
            } else if (preference == mScreenOffAnimation) {
                int value = Integer.parseInt((String) objValue);
                int index = mScreenOffAnimation.findIndexOfValue((String) objValue);
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_OFF_ANIMATION,
                        value);
                mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[index]);
                return true;
            }
            return false;
        }
    }
}
