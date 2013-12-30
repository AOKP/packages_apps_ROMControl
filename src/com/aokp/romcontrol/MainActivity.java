package com.aokp.romcontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import com.aokp.romcontrol.fragments.AboutFragment;
import com.aokp.romcontrol.fragments.GeneralSettingsFragment;
import com.aokp.romcontrol.fragments.HardwareKeysFragment;
import com.aokp.romcontrol.fragments.InstallerSettingsFragment;
import com.aokp.romcontrol.fragments.NavigationDrawerFragment;
import com.aokp.romcontrol.fragments.StatusbarSettingsFragment;
import com.aokp.romcontrol.fragments.TogglesTabHostFragment;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDrawerEntries = getResources().getStringArray(R.array.navigation_drawer_entries);

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
                fragment = new AboutFragment();
                break;

            case 1:
                fragment = new GeneralSettingsFragment();
                break;

            case 2:
                fragment = new HardwareKeysFragment();
                break;

            case 3:
                fragment = new StatusbarSettingsFragment();
                break;

            case 4:
                fragment = new TogglesTabHostFragment();
                break;

            case 5:
                fragment = new InstallerSettingsFragment();
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
