/*
 * Copyright (C) 2015 The Android Open Kang Project
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

package com.aokp.romcontrol.fragments.navbar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SlimSeekBarPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.internal.util.aokp.AOKPUtils;
import com.aokp.romcontrol.R;
import cyanogenmod.providers.CMSettings;
import java.util.List;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import org.cyanogenmod.internal.logging.CMMetricsLogger;
import org.cyanogenmod.internal.util.ScreenType;

public class NavbarSettingsFragment extends Fragment {

    public NavbarSettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navbar_settings_main, container, false);

        Resources res = getResources();

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.navbar_settings_main, new SettingsPreferenceFragment())
                .commit();
    }
    public static class SettingsPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {

        }

        private static final String TAG = "NavbarSettingsFragment";

        private static final String CATEGORY_NAVBAR = "nav_cat";
        private static final String KEY_ENABLE_NAVIGATION_BAR = "enable_nav_bar";

        private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
        private static final String KEY_NAVIGATION_RECENTS_LONG_PRESS = "navigation_recents_long_press";
        private static final String NAVIGATION_BAR_TINT = "navigation_bar_tint";
        private static final String DIM_NAV_BUTTONS = "dim_nav_buttons";
        private static final String DIM_NAV_BUTTONS_TOUCH_ANYWHERE = "dim_nav_buttons_touch_anywhere";
        private static final String DIM_NAV_BUTTONS_TIMEOUT = "dim_nav_buttons_timeout";
        private static final String DIM_NAV_BUTTONS_ALPHA = "dim_nav_buttons_alpha";
        private static final String DIM_NAV_BUTTONS_ANIMATE = "dim_nav_buttons_animate";
        private static final String DIM_NAV_BUTTONS_ANIMATE_DURATION = "dim_nav_buttons_animate_duration";

        private static final String PREF_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
        private static final String PREF_NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";
        private static final String PREF_NAVIGATION_BAR_WIDTH = "navigation_bar_width";
        private static final String KEY_DIMEN_OPTIONS = "navbar_dimen";

        private static final int MENU_RESET = Menu.FIRST;
        private static final int DLG_RESET = 0;

        private SwitchPreference mEnableNavigationBar;
        private ColorPickerPreference mNavbarButtonTint;
        private SwitchPreference mNavigationBarLeftPref;
        private ListPreference mNavigationRecentsLongPressAction;
        SwitchPreference mDimNavButtons;
        SwitchPreference mDimNavButtonsTouchAnywhere;
        SlimSeekBarPreference mDimNavButtonsTimeout;
        SlimSeekBarPreference mDimNavButtonsAlpha;
        SwitchPreference mDimNavButtonsAnimate;
        SlimSeekBarPreference mDimNavButtonsAnimateDuration;

        private ListPreference mNavigationBarHeight;
        private ListPreference mNavigationBarHeightLandscape;
        private ListPreference mNavigationBarWidth;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_navbar_settings);
            final PreferenceScreen prefSet = getPreferenceScreen();
            final Resources res = getResources();

            // Navigation bar category
            final PreferenceCategory navBarCategory = (PreferenceCategory) findPreference(CATEGORY_NAVBAR);

            // Navigation bar keys switch
            mEnableNavigationBar = (SwitchPreference) findPreference(KEY_ENABLE_NAVIGATION_BAR);

            // Navigation bar left
            mNavigationBarLeftPref = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);

            // Internal bool to check if the device have a navbar by default or not!
            boolean hasNavBarByDefault = getResources().getBoolean(
                    com.android.internal.R.bool.config_showNavigationBar);
            boolean enableNavigationBar = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1;
            mEnableNavigationBar.setChecked(enableNavigationBar);
            mEnableNavigationBar.setOnPreferenceChangeListener(this);

            // Navigation bar button color
            mNavbarButtonTint = (ColorPickerPreference) findPreference(NAVIGATION_BAR_TINT);
            mNavbarButtonTint.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_TINT, 0xffffffff);
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mNavbarButtonTint.setSummary(hexColor);
            mNavbarButtonTint.setNewPreviewColor(intColor);

            // Navigation bar recents long press activity needs custom setup
            mNavigationRecentsLongPressAction =
                    initRecentsLongPressAction(KEY_NAVIGATION_RECENTS_LONG_PRESS);
            mDimNavButtons = (SwitchPreference) findPreference(DIM_NAV_BUTTONS);
            mDimNavButtons.setOnPreferenceChangeListener(this);

            mDimNavButtonsTouchAnywhere = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_TOUCH_ANYWHERE);
            mDimNavButtonsTouchAnywhere.setOnPreferenceChangeListener(this);

            mDimNavButtonsTimeout = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_TIMEOUT);
            mDimNavButtonsTimeout.setDefault(3000);
            mDimNavButtonsTimeout.isMilliseconds(true);
            mDimNavButtonsTimeout.setInterval(1);
            mDimNavButtonsTimeout.minimumValue(100);
            mDimNavButtonsTimeout.multiplyValue(100);
            mDimNavButtonsTimeout.setOnPreferenceChangeListener(this);

            mDimNavButtonsAlpha = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ALPHA);
            mDimNavButtonsAlpha.setDefault(50);
            mDimNavButtonsAlpha.setInterval(1);
            mDimNavButtonsAlpha.setOnPreferenceChangeListener(this);

            mDimNavButtonsAnimate = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE);
            mDimNavButtonsAnimate.setOnPreferenceChangeListener(this);

            mDimNavButtonsAnimateDuration = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE_DURATION);
            mDimNavButtonsAnimateDuration.setDefault(2000);
            mDimNavButtonsAnimateDuration.isMilliseconds(true);
            mDimNavButtonsAnimateDuration.setInterval(1);
            mDimNavButtonsAnimateDuration.minimumValue(100);
            mDimNavButtonsAnimateDuration.multiplyValue(100);
            mDimNavButtonsAnimateDuration.setOnPreferenceChangeListener(this);

            mNavigationBarHeight =
                (ListPreference) findPreference(PREF_NAVIGATION_BAR_HEIGHT);
            mNavigationBarHeight.setOnPreferenceChangeListener(this);

            mNavigationBarHeightLandscape =
                (ListPreference) findPreference(PREF_NAVIGATION_BAR_HEIGHT_LANDSCAPE);

            if (ScreenType.isPhone(getActivity())) {
                prefSet.removePreference(mNavigationBarHeightLandscape);
                mNavigationBarHeightLandscape = null;
            } else {
                mNavigationBarHeightLandscape.setOnPreferenceChangeListener(this);
            }

           mNavigationBarWidth =
                (ListPreference) findPreference(PREF_NAVIGATION_BAR_WIDTH);

            if (!ScreenType.isPhone(getActivity())) {
                prefSet.removePreference(mNavigationBarWidth);
                mNavigationBarWidth = null;
            } else {
                mNavigationBarWidth.setOnPreferenceChangeListener(this);
            }

            updateDimensionValues();
            setHasOptionsMenu(true);
            updateNavBarSettings();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        private ListPreference initRecentsLongPressAction(String key) {
            ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
            list.setOnPreferenceChangeListener(this);

            // Read the componentName from Settings.Secure, this is the user's prefered setting
            String componentString = CMSettings.Secure.getString(getActivity().getContentResolver(),
                    CMSettings.Secure.RECENTS_LONG_PRESS_ACTIVITY);
            ComponentName targetComponent = null;
            if (componentString == null) {
                list.setSummary(getString(R.string.hardware_keys_action_last_app));
            } else {
                targetComponent = ComponentName.unflattenFromString(componentString);
            }

            // Dyanamically generate the list array,
            // query PackageManager for all Activites that are registered for ACTION_RECENTS_LONG_PRESS
            PackageManager pm = getActivity().getPackageManager();
            Intent intent = new Intent(cyanogenmod.content.Intent.ACTION_RECENTS_LONG_PRESS);
            List<ResolveInfo> recentsActivities = pm.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (recentsActivities.size() == 0) {
                // No entries available, disable
                list.setSummary(getString(R.string.hardware_keys_action_last_app));
                CMSettings.Secure.putString(getActivity().getContentResolver(),
                        CMSettings.Secure.RECENTS_LONG_PRESS_ACTIVITY, null);
                list.setEnabled(false);
                return list;
            }

            CharSequence[] entries = new CharSequence[recentsActivities.size() + 1];
            CharSequence[] values = new CharSequence[recentsActivities.size() + 1];
            // First entry is always default last app
            entries[0] = getString(R.string.hardware_keys_action_last_app);
            values[0] = "";
            list.setValue(values[0].toString());
            int i = 1;
            for (ResolveInfo info : recentsActivities) {
                try {
                    // Use pm.getApplicationInfo for the label,
                    // we cannot rely on ResolveInfo that comes back from queryIntentActivities.
                    entries[i] = pm.getApplicationInfo(info.activityInfo.packageName, 0).loadLabel(pm);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Error package not found: " + info.activityInfo.packageName, e);
                    // Fallback to package name
                    entries[i] = info.activityInfo.packageName;
                }

                // Set the value to the ComponentName that will handle this intent
                ComponentName entryComponent = new ComponentName(info.activityInfo.packageName,
                        info.activityInfo.name);
                values[i] = entryComponent.flattenToString();
                if (targetComponent != null) {
                    if (entryComponent.equals(targetComponent)) {
                        // Update the selected value and the preference summary
                        list.setSummary(entries[i]);
                        list.setValue(values[i].toString());
                    }
                }
                i++;
            }
            list.setEntries(entries);
            list.setEntryValues(values);
            return list;
        }

        protected int getMetricsCategory() {
            return CMMetricsLogger.DONT_LOG;
        }

       private void updateNavBarSettings() {
            boolean enableNavigationBar = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                    AOKPUtils.isNavBarDefault(getActivity()) ? 1 : 0) == 1;
            mEnableNavigationBar.setChecked(enableNavigationBar);

            if (mDimNavButtons != null) {
                mDimNavButtons.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.DIM_NAV_BUTTONS, 0) == 1);
            }

            if (mDimNavButtonsTouchAnywhere != null) {
                mDimNavButtonsTouchAnywhere.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.DIM_NAV_BUTTONS_TOUCH_ANYWHERE, 0) == 1);
            }

            if (mDimNavButtonsTimeout != null) {
                final int dimTimeout = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.DIM_NAV_BUTTONS_TIMEOUT, 3000);
                // minimum 100 is 1 interval of the 100 multiplier
                mDimNavButtonsTimeout.setInitValue((dimTimeout / 100) - 1);
            }

            if (mDimNavButtonsAlpha != null) {
                int alphaScale = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.DIM_NAV_BUTTONS_ALPHA, 50);
                mDimNavButtonsAlpha.setInitValue(alphaScale);
            }

            if (mDimNavButtonsAnimate != null) {
                mDimNavButtonsAnimate.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.DIM_NAV_BUTTONS_ANIMATE, 0) == 1);
            }

            if (mDimNavButtonsAnimateDuration != null) {
                final int animateDuration = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.DIM_NAV_BUTTONS_ANIMATE_DURATION, 2000);
                // minimum 100 is 1 interval of the 100 multiplier
                mDimNavButtonsAnimateDuration.setInitValue((animateDuration / 100) - 1);
            }

            updateNavbarPreferences(enableNavigationBar);
        }

        private void updateNavbarPreferences(boolean show) {
            mDimNavButtons.setEnabled(show);
            mDimNavButtonsTimeout.setEnabled(show);
            mDimNavButtonsAlpha.setEnabled(show);
            mDimNavButtonsAnimate.setEnabled(show);
            mDimNavButtonsAnimateDuration.setEnabled(show);
        }

        private void updateDimensionValues() {
            int navigationBarHeight = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT, -1);
            if (navigationBarHeight == -1) {
                navigationBarHeight = (int) (getResources().getDimension(
                        com.android.internal.R.dimen.navigation_bar_height)
                        / getResources().getDisplayMetrics().density);
            }
            mNavigationBarHeight.setValue(String.valueOf(navigationBarHeight));
            mNavigationBarHeight.setSummary(mNavigationBarHeight.getEntry());

            if (mNavigationBarHeightLandscape != null) {
                int navigationBarHeightLandscape = Settings.System.getInt(getActivity().getContentResolver(),
                                    Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, -1);
                if (navigationBarHeightLandscape == -1) {
                    navigationBarHeightLandscape = (int) (getResources().getDimension(
                            com.android.internal.R.dimen.navigation_bar_height_landscape)
                            / getResources().getDisplayMetrics().density);
                }
                mNavigationBarHeightLandscape.setValue(String.valueOf(navigationBarHeightLandscape));
                mNavigationBarHeightLandscape.setSummary(mNavigationBarHeightLandscape.getEntry());
            }

            if (mNavigationBarWidth != null) {
                int navigationBarWidth = Settings.System.getInt(getActivity().getContentResolver(),
                                Settings.System.NAVIGATION_BAR_WIDTH, -1);
                if (navigationBarWidth == -1) {
                    navigationBarWidth = (int) (getResources().getDimension(
                                com.android.internal.R.dimen.navigation_bar_width)
                            / getResources().getDisplayMetrics().density);
                }
                mNavigationBarWidth.setValue(String.valueOf(navigationBarWidth));
                mNavigationBarWidth.setSummary(mNavigationBarWidth.getEntry());
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            menu.add(0, MENU_RESET, 0, R.string.reset)
                    .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_RESET:
                    showDialogInner(DLG_RESET);
                    return true;
                 default:
                    return super.onContextItemSelected(item);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mEnableNavigationBar) {
                mEnableNavigationBar.setEnabled(true);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_SHOW,
                            ((Boolean) newValue) ? 1 : 0);
                return true;
            } else if (preference == mNavigationRecentsLongPressAction) {
                // RecentsLongPressAction is handled differently because it intentionally uses
                // Settings.System
                String putString = (String) newValue;
                int index = mNavigationRecentsLongPressAction.findIndexOfValue(putString);
                CharSequence summary = mNavigationRecentsLongPressAction.getEntries()[index];
                // Update the summary
                mNavigationRecentsLongPressAction.setSummary(summary);
                if (putString.length() == 0) {
                    putString = null;
                }
                CMSettings.Secure.putString(getActivity().getContentResolver(),
                        CMSettings.Secure.RECENTS_LONG_PRESS_ACTIVITY, putString);
                return true;
            } else if (preference == mNavbarButtonTint) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_TINT, intHex);
                return true;
            } else if (preference == mDimNavButtons) {
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS,
                        ((Boolean) newValue) ? 1 : 0);
                return true;
            } else if (preference == mDimNavButtonsTouchAnywhere) {
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_TOUCH_ANYWHERE,
                        ((Boolean) newValue) ? 1 : 0);
                return true;
            } else if (preference == mDimNavButtonsTimeout) {
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_TIMEOUT, Integer.parseInt((String) newValue));
                return true;
            } else if (preference == mDimNavButtonsAlpha) {
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_ALPHA, Integer.parseInt((String) newValue));
                return true;
            } else if (preference == mDimNavButtonsAnimate) {
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_ANIMATE,
                        ((Boolean) newValue) ? 1 : 0);
                return true;
            } else if (preference == mDimNavButtonsAnimateDuration) {
                Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DIM_NAV_BUTTONS_ANIMATE_DURATION,
                    Integer.parseInt((String) newValue));
                return true;
            } else if (preference == mNavigationBarWidth) {
                int index = mNavigationBarWidth.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_WIDTH, Integer.parseInt((String) newValue));
                updateDimensionValues();
                return true;
            } else if (preference == mNavigationBarHeight) {
                int index = mNavigationBarHeight.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_HEIGHT, Integer.parseInt((String) newValue));
                updateDimensionValues();
                return true;
            } else if (preference == mNavigationBarHeightLandscape) {
                int index = mNavigationBarHeightLandscape.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, Integer.parseInt((String) newValue));
                updateDimensionValues();
                return true;
            }
            return false;
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

            NavbarSettingsFragment.SettingsPreferenceFragment getOwner() {
                return (NavbarSettingsFragment.SettingsPreferenceFragment) getTargetFragment();
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int id = getArguments().getInt("id");
                switch (id) {
                    case DLG_RESET:
                        return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.reset)
                        .setMessage(R.string.navbar_dimensions_reset_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Settings.System.putInt(getActivity().getContentResolver(),
                                        Settings.System.NAVIGATION_BAR_HEIGHT, -2);
                                Settings.System.putInt(getActivity().getContentResolver(),
                                        Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, -2);
                                Settings.System.putInt(getActivity().getContentResolver(),
                                        Settings.System.NAVIGATION_BAR_WIDTH, -2);
                                getOwner().updateDimensionValues();
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
