/*
 * Copyright (C) 2013 The Android Open Kang Project
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
import com.aokp.romcontrol.fragments.about.AboutFragment;
import com.aokp.romcontrol.fragments.AnimationsFragment;
import com.aokp.romcontrol.fragments.GeneralSettingsFragment;
import com.aokp.romcontrol.fragments.HardwareKeysFragment;
import com.aokp.romcontrol.fragments.InstallerSettingsFragment;
import com.aokp.romcontrol.fragments.LedSettingsFragment;
import com.aokp.romcontrol.fragments.LockscreenSettingsFragment;
import com.aokp.romcontrol.fragments.NavRingTargets;
import com.aokp.romcontrol.fragments.NavigationDrawerFragment;
import com.aokp.romcontrol.fragments.PowerMenuSettingsFragment;
import com.aokp.romcontrol.fragments.ribbons.RibbonsFragment;
import com.aokp.romcontrol.fragments.StatusbarSettingsFragment;
import com.aokp.romcontrol.fragments.SoundSettingsFragment;
import com.aokp.romcontrol.fragments.about.AboutTabHostFragment;
import com.aokp.romcontrol.fragments.navbar.NavbarTabHostFragment;
import com.aokp.romcontrol.fragments.toggles.TogglesTabHostFragment;
import com.aokp.romcontrol.fragments.AutoImmersiveSettingsFragment;
import com.aokp.romcontrol.fragments.headsup.HeadsUpTabHostFragment;


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
        String item = mDrawerValues[index];
        Fragment fragment = null;

        // blame Google for not using Java 7 yet
        if ("about_aokp".equals(item)) {
            fragment = new AboutTabHostFragment();
        }
        else if ("general".equals(item)) {
            fragment = new GeneralSettingsFragment();
        }
        else if ("lockscreen".equals(item)) {
            fragment = new LockscreenSettingsFragment();
        }
        else if ("statusbar".equals(item)) {
            fragment = new StatusbarSettingsFragment();
        }
        else if ("toggles".equals(item)) {
            fragment = new TogglesTabHostFragment();
        }
        else if ("hardware_keys".equals(item)) {
            fragment = new HardwareKeysFragment();
        }
        else if ("power_menu".equals(item)) {
            fragment = new PowerMenuSettingsFragment();
        }
        else if ("navbar".equals(item)) {
            fragment = new NavbarTabHostFragment();
        }
        else if ("navring".equals(item)) {
            fragment = new NavRingTargets();
        }
        else if ("sound".equals(item)) {
            fragment = new SoundSettingsFragment();
        }
        else if ("installer".equals(item)) {
            fragment = new InstallerSettingsFragment();
        }
        else if ("ribbons".equals(item)) {
            fragment = new RibbonsFragment();
        }
        else if ("animations".equals(item)) {
            fragment = new AnimationsFragment();
        }
        else if ("led".equals(item)) {
            fragment = new LedSettingsFragment();
        }
        else if ("auto_immersive".equals(item)) {
            fragment = new AutoImmersiveSettingsFragment();
        }
        else if ("headsup".equals(item)) {
            fragment = new HeadsUpTabHostFragment();
        }
        else {
            // who knows
            fragment = new AboutTabHostFragment();
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
