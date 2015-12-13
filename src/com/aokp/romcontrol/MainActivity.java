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

package com.aokp.romcontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;

import com.aokp.romcontrol.fragments.about.AboutCrewFragment;
import com.aokp.romcontrol.fragments.about.AboutFragment;
import com.aokp.romcontrol.fragments.about.AboutTabHostFragment;

import com.aokp.romcontrol.fragments.applauncher.AppLauncherTabHostFragment;
import com.aokp.romcontrol.fragments.applauncher.AppCircleBarSettings;

import com.aokp.romcontrol.fragments.general.GeneralTabHostFragment;
import com.aokp.romcontrol.fragments.general.GeneralSettingsFragment;
import com.aokp.romcontrol.fragments.general.RecentsSettingsFragment;
import com.aokp.romcontrol.fragments.general.WakelockBlockerFragment;

import com.aokp.romcontrol.fragments.navbar.NavbarTabHostFragment;
import com.aokp.romcontrol.fragments.navbar.NavbarArrangeFragment;
import com.aokp.romcontrol.fragments.navbar.NavbarSettingsFragment;

import com.aokp.romcontrol.fragments.pie.PieColorFragment;
import com.aokp.romcontrol.fragments.pie.PieControlFragment;
import com.aokp.romcontrol.fragments.pie.PieTabHostFragment;
import com.aokp.romcontrol.fragments.pie.PieTargetsFragment;

import com.aokp.romcontrol.fragments.sound.SoundTabHostFragment;
import com.aokp.romcontrol.fragments.sound.SoundSettingsFragment;
import com.aokp.romcontrol.fragments.sound.LiveVolumeFragment;

import com.aokp.romcontrol.fragments.statusbar.StatusBarTabHostFragment;
import com.aokp.romcontrol.fragments.statusbar.StatusbarSettingsFragment;
import com.aokp.romcontrol.fragments.statusbar.ClockSettingsFragment;
import com.aokp.romcontrol.fragments.statusbar.BatterySettingsFragment;
import com.aokp.romcontrol.fragments.statusbar.TrafficSettingsFragment;

import com.aokp.romcontrol.fragments.ui.AnimationControls;
import com.aokp.romcontrol.fragments.ui.AnimBarPreference;
import com.aokp.romcontrol.fragments.ui.DisplayAnimationsSettings;
import com.aokp.romcontrol.fragments.ui.GestureAnywhereBuilderActivity;
import com.aokp.romcontrol.fragments.ui.GestureAnywhereCreateGestureActivity;
import com.aokp.romcontrol.fragments.ui.GestureAnywhereGestureOverlayView;
import com.aokp.romcontrol.fragments.ui.GestureAnywhereSettings;
import com.aokp.romcontrol.fragments.ui.UITabHostFragment;

import com.aokp.romcontrol.fragments.ButtonSettingsFragment;
import com.aokp.romcontrol.fragments.LockScreenSettingsFragment;
import com.aokp.romcontrol.fragments.NavigationDrawerFragment;
import com.aokp.romcontrol.fragments.NotificationsDrawerFragment;
import com.aokp.romcontrol.fragments.PowerMenuSettingsFragment;

import cyanogenmod.providers.CMSettings;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private Fragment mSelectedFragment;
    private String[] mDrawerEntries;
    private String[] mDrawerValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDrawerEntries = getResources().getStringArray(R.array.navigation_drawer_entries);
        mDrawerValues = getResources().getStringArray(R.array.navigation_drawer_values);

        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, getFragmentToAttach(position))
                .commit();
    }

    public Fragment getFragmentToAttach(int position) {
        int index = position;
        mTitle = mDrawerEntries[index];
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new AboutTabHostFragment();
                break;

            case 1:
                fragment = new GeneralTabHostFragment();
                break;

            case 2:
                fragment = new LockScreenSettingsFragment();
                break;

            case 3:
                fragment = new StatusBarTabHostFragment();
                break;

            case 4:
                fragment = new NotificationsDrawerFragment();
                break;

            case 5:
                fragment = new ButtonSettingsFragment();
                break;

            case 6:
                fragment = new PowerMenuSettingsFragment();
                break;

            case 7:
                fragment = new NavbarTabHostFragment();
                break;

            case 8:
                fragment = new PieTabHostFragment();
                break;

            case 9:
                fragment = new SoundTabHostFragment();
                break;

            case 10:
                fragment = new UITabHostFragment();
                break;

            case 11:
                fragment = new AppCircleBarSettings();
                break;
        }
        return fragment;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();

            MenuItem showDrawerIcon = menu.findItem(R.id.action_show_drawer_icon);

            showDrawerIcon.setChecked(isLauncherIconEnabled());

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_show_drawer_icon) {
            boolean checked = item.isChecked();
            item.setChecked(!checked);
            setLauncherIconEnabled(!checked);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setLauncherIconEnabled(boolean enabled) {
        PackageManager p = getPackageManager();
        int newState = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        p.setComponentEnabledSetting(new ComponentName(this, LauncherActivity.class), newState, PackageManager.DONT_KILL_APP);
    }

    public boolean isLauncherIconEnabled() {
        PackageManager p = getPackageManager();
        int componentStatus = p.getComponentEnabledSetting(new ComponentName(this, LauncherActivity.class));
        return componentStatus != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }
}
