package com.aokp.romcontrol;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aokp.romcontrol.R;

public class FragmentDrawer extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mHeaders;

    private static boolean hasNotificationLed;
    private static boolean hasSPen;
    private static boolean hasHardwareButtons;
    Vibrator mVibrator;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mHeaders = getResources().getStringArray(R.array.header_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        hasNotificationLed = getResources().getBoolean(R.bool.has_notification_led);
        hasSPen = getResources().getBoolean(R.bool.config_stylusGestures);
        hasHardwareButtons = getResources().getBoolean(R.bool.has_hardware_buttons);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mHeaders));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) return true;

        // Default to false unless the drawer view is selected
        return false;
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new com.aokp.romcontrol.fragments.About();
                break;
            case 1:
                fragment = new com.aokp.romcontrol.fragments.UserInterface();
                break;
            case 2:
                fragment = new com.aokp.romcontrol.fragments.Lockscreens();
                break;
            case 3:
                fragment = new com.aokp.romcontrol.fragments.PowerMenu();
                break;
            case 4:
                fragment = new com.aokp.romcontrol.fragments.Navbar();
                break;
            case 5:
                fragment = new com.aokp.romcontrol.fragments.NavRingTargets();
                break;
            case 6:
                fragment = new com.aokp.romcontrol.fragments.RibbonTargets();
                break;
            case 7:
                fragment = new com.aokp.romcontrol.fragments.ActiveDisplaySettings();
                break;
            case 8:
                fragment = new com.aokp.romcontrol.fragments.StatusBarToggles();
                break;
            case 9:
                fragment = new com.aokp.romcontrol.fragments.StatusBarBattery();
                break;
            case 10:
                fragment = new com.aokp.romcontrol.fragments.StatusBarClock();
                break;
            case 11:
                fragment = new com.aokp.romcontrol.fragments.StatusBarSignal();
                break;
            case 12:
                fragment = new com.aokp.romcontrol.fragments.AnimationControls();
                break;
            case 13:
                fragment = new com.aokp.romcontrol.fragments.Sound();
                break;
            case 14:
                fragment = new com.aokp.romcontrol.fragments.Vibrations();
                break;
            case 15:
                fragment = new com.aokp.romcontrol.fragments.Installer();
                break;
        }


        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mHeaders[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
