
package com.aokp.romcontrol.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.ROMControlActivity;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.NavBarItemPreference;
import com.aokp.romcontrol.widgets.SeekBarPreference;
import com.aokp.romcontrol.fragments.NavRingTargets;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class Navbar extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickerHelper.OnPickListener {

    // move these later
    private static final String PREF_MENU_UNLOCK = "pref_menu_display";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
    private static final String PREF_NAV_COLOR = "nav_button_color";
    private static final String PREF_NAV_GLOW_COLOR = "nav_button_glow_color";
    private static final String PREF_GLOW_TIMES = "glow_times";
    private static final String PREF_NAVBAR_QTY = "navbar_qty";
    private static final String ENABLE_NAVIGATION_BAR = "enable_navigation_bar";
    private static final String NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";
    private static final String NAVIGATION_BAR_WIDTH = "navigation_bar_width";
    private static final String PREF_NAVRING_AMOUNT = "pref_navring_amount";
    private static final String NAVIGATION_BAR_WIDGETS = "navigation_bar_widgets";
    private static final String ENABLE_NAVRING_LONG = "enable_navring_long";

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;
    private static final int DIALOG_NAVBAR_ENABLE = 203;
    private static final int DIALOG_NAVBAR_HEIGHT_REBOOT = 204;

    public static final String PREFS_NAV_BAR = "navbar";

    Preference mNavRingTargets;

    // move these later
    ColorPickerPreference mNavigationBarColor;
    ColorPickerPreference mNavigationBarGlowColor;
    ListPreference mGlowTimes;
    ListPreference menuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    ListPreference mNavBarButtonQty;
    ListPreference mNavRingButtonQty;
    CheckBoxPreference mEnableNavigationBar;
    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarHeightLandscape;
    ListPreference mNavigationBarWidth;
    SeekBarPreference mButtonAlpha;
    Preference mWidthHelp;
    SeekBarPreference mWidthPort;
    SeekBarPreference mWidthLand;
    Preference mConfigureWidgets;
    CheckBoxPreference mEnableNavringLong;

    private int mPendingIconIndex = -1;
    private int mPendingWidgetDrawer = -1;
    private NavBarCustomAction mPendingNavBarCustomAction = null;

    private static class NavBarCustomAction {
        String activitySettingName;
        Preference preference;
        int iconIndex = -1;
    }

    Preference mPendingPreference;
    private ShortcutPickerHelper mPicker;

    private static final String TAG = "NavBar";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_navbar);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_navbar);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

        mNavRingTargets = findPreference("navring_settings");

        menuDisplayLocation = (ListPreference) findPreference(PREF_MENU_UNLOCK);
        menuDisplayLocation.setOnPreferenceChangeListener(this);
        menuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_LOCATION,
                0) + "");

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);
        mNavBarMenuDisplay.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_VISIBILITY,
                0) + "");

        mNavRingButtonQty = (ListPreference) findPreference(PREF_NAVRING_AMOUNT);
        mNavRingButtonQty.setOnPreferenceChangeListener(this);
        mNavRingButtonQty.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1) + "");

        mNavBarButtonQty = (ListPreference) findPreference(PREF_NAVBAR_QTY);
        mNavBarButtonQty.setOnPreferenceChangeListener(this);
        mNavBarButtonQty.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3) + "");

        mEnableNavringLong = (CheckBoxPreference) findPreference("enable_navring_long");
        mEnableNavringLong.setChecked(Settings.System.getBoolean(getContentResolver(),
                Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE, false));

        mPicker = new ShortcutPickerHelper(this, this);

        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        mEnableNavigationBar = (CheckBoxPreference) findPreference("enable_nav_bar");
        mEnableNavigationBar.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1);

        mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);

        mNavigationBarGlowColor = (ColorPickerPreference) findPreference(PREF_NAV_GLOW_COLOR);
        mNavigationBarGlowColor.setOnPreferenceChangeListener(this);

        mGlowTimes = (ListPreference) findPreference(PREF_GLOW_TIMES);
        mGlowTimes.setOnPreferenceChangeListener(this);

        float defaultAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                0.6f);
        mButtonAlpha = (SeekBarPreference) findPreference("button_transparency");
        mButtonAlpha.setInitValue((int) (defaultAlpha * 100));
        mButtonAlpha.setOnPreferenceChangeListener(this);

        mWidthHelp = (Preference) findPreference("width_help");

        float defaultPort = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.NAVIGATION_BAR_WIDTH_PORT,
                0f);
        mWidthPort = (SeekBarPreference) findPreference("width_port");
        mWidthPort.setInitValue((int) (defaultPort * 2.5f));
        mWidthPort.setOnPreferenceChangeListener(this);

        float defaultLand = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.NAVIGATION_BAR_WIDTH_LAND,
                0f);
        mWidthLand = (SeekBarPreference) findPreference("width_land");
        mWidthLand.setInitValue((int) (defaultLand * 2.5f));
        mWidthLand.setOnPreferenceChangeListener(this);

        // don't allow devices that must use a navigation bar to disable it
        if (hasNavBarByDefault || mTablet) {
            prefs.removePreference(mEnableNavigationBar);
        }

        mNavigationBarHeight = (ListPreference) findPreference("navigation_bar_height");
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        mNavigationBarHeightLandscape = (ListPreference) findPreference("navigation_bar_height_landscape");
        mNavigationBarHeightLandscape.setOnPreferenceChangeListener(this);

        mNavigationBarWidth = (ListPreference) findPreference("navigation_bar_width");
        mNavigationBarWidth.setOnPreferenceChangeListener(this);

        mConfigureWidgets = findPreference(NAVIGATION_BAR_WIDGETS);
        if (mTablet) {
            prefs.removePreference(mNavBarMenuDisplay);
        } else {
            ((PreferenceGroup) findPreference("advanced_cat")).removePreference(mWidthHelp);
            ((PreferenceGroup) findPreference("advanced_cat")).removePreference(mWidthLand);
            ((PreferenceGroup) findPreference("advanced_cat")).removePreference(mWidthPort);
        }
        refreshSettings();
        setHasOptionsMenu(true);
        updateGlowTimesSummary();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.nav_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_TINT, Integer.MIN_VALUE);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_GLOW_TINT, Integer.MIN_VALUE);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3);

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[0], "**back**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[1], "**home**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[2], "**recents**");

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[0], "**null**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[1], "**null**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[2], "**null**");

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[0], "");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[1], "");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[2], "");
                refreshSettings();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableNavigationBar) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mEnableNavringLong) {

            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            resetNavRingLong();
            return true;
        } else if (preference == mNavRingTargets) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NavRingTargets fragment = new NavRingTargets();
            ft.addToBackStack("config_nav_ring");
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        } else if (preference == mConfigureWidgets) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            WidgetConfigurationFragment fragment = new WidgetConfigurationFragment();
            ft.addToBackStack("config_widgets");
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == menuDisplayLocation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_VISIBILITY, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavRingButtonQty) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_AMOUNT, val);
            resetNavRing();
            resetNavRingLong();
            refreshSettings();
            return true;
        } else if (preference == mNavBarButtonQty) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTONS_QTY, val);
            refreshSettings();
            return true;
        } else if (preference == mNavigationBarWidth) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int width = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_WIDTH,
                    width);
            //showDialog(DIALOG_NAVBAR_HEIGHT_REBOOT);
            return true;
        } else if (preference == mNavigationBarHeight) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT,
                    height);
            //showDialog(DIALOG_NAVBAR_HEIGHT_REBOOT);
            return true;
        } else if (preference == mNavigationBarHeightLandscape) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE,
                    height);
            //showDialog(DIALOG_NAVBAR_HEIGHT_REBOOT);
            return true;

        } else if ((preference.getKey().startsWith("navbar_action"))
                || (preference.getKey().startsWith("navbar_longpress"))) {
            boolean longpress = preference.getKey().startsWith("navbar_longpress_");
            int index = Integer.parseInt(preference.getKey().substring(
                    preference.getKey().lastIndexOf("_") + 1));

            if (newValue.equals("**app**")) {
                mPendingNavBarCustomAction = new NavBarCustomAction();
                mPendingNavBarCustomAction.preference = preference;
                if (longpress) {
                    mPendingNavBarCustomAction.activitySettingName = Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[index];
                    mPendingNavBarCustomAction.iconIndex = -1;
                } else {
                    mPendingNavBarCustomAction.activitySettingName = Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index];
                    mPendingNavBarCustomAction.iconIndex = index;
                }
                mPicker.pickShortcut();
            } else {
                if (longpress) {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[index],
                            (String) newValue);
                } else {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index],
                            (String) newValue);
                    Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_CUSTOM_APP_ICONS[index], "");
                }
            }
            refreshSettings();
            return true;
        } else if (preference == mNavigationBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_TINT, intHex);
            return true;
        } else if (preference == mNavigationBarGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_TINT, intHex);
            return true;
        } else if (preference == mGlowTimes) {
            // format is (on|off) both in MS
            String value = (String) newValue;
            String[] breakIndex = value.split("\\|");
            int onTime = Integer.valueOf(breakIndex[0]);
            int offTime = Integer.valueOf(breakIndex[1]);

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[0], offTime);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[1], onTime);
            updateGlowTimesSummary();
            return true;
        } else if (preference == mButtonAlpha) {
            float val = Float.parseFloat((String) newValue);
            Log.e("R", "value: " + val * 0.01f);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                    val * 0.01f);
            return true;
        } else if (preference == mWidthPort) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_WIDTH_PORT,
                    val * 0.4f);
            return true;
        } else if (preference == mWidthLand) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_WIDTH_LAND,
                    val * 0.4f);
            return true;

        }
        return false;
    }

    public void resetNavRing() {
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_1, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_2, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_3, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_4, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_5, "none");
    }

    public void resetNavRingLong() {
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_1, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_2, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_3, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_4, "none");
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.SYSTEMUI_NAVRING_LONG_5, "none");
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case DIALOG_NAVBAR_HEIGHT_REBOOT:
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.navbar_height_dialog_title))
                        .setMessage(
                                getResources().getString(R.string.navbar_height_dialog_summary))
                        .setCancelable(false)
                        .setNeutralButton(
                                getResources()
                                        .getString(R.string.navbar_height_dialog_button_later),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .setPositiveButton(
                                getResources().getString(
                                        R.string.navbar_height_dialog_button_reboot),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PowerManager pm = (PowerManager) getActivity()
                                                .getSystemService(Context.POWER_SERVICE);
                                        pm.reboot("Rebooting with new bar height");
                                    }
                                })
                        .create();
        }
        return null;
    }

    private void updateGlowTimesSummary() {
        int resId;
        String combinedTime = Settings.System.getString(getContentResolver(),
                Settings.System.NAVIGATION_BAR_GLOW_DURATION[1]) + "|" +
                Settings.System.getString(getContentResolver(),
                        Settings.System.NAVIGATION_BAR_GLOW_DURATION[0]);

        String[] glowArray = getResources().getStringArray(R.array.glow_times_values);

        if (glowArray[0].equals(combinedTime)) {
            resId = R.string.glow_times_off;
            mGlowTimes.setValueIndex(0);
        } else if (glowArray[1].equals(combinedTime)) {
            resId = R.string.glow_times_superquick;
            mGlowTimes.setValueIndex(1);
        } else if (glowArray[2].equals(combinedTime)) {
            resId = R.string.glow_times_quick;
            mGlowTimes.setValueIndex(2);
        } else {
            resId = R.string.glow_times_normal;
            mGlowTimes.setValueIndex(3);
        }
        mGlowTimes.setSummary(getResources().getString(resId));
    }

    public int mapChosenDpToPixels(int dp) {
        switch (dp) {
            case 48:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_48);
            case 44:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_44);
            case 42:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_42);
            case 40:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_40);
            case 36:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_36);
            case 30:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_30);
            case 24:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_24);
        }
        return -1;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if ((requestCode == REQUEST_PICK_CUSTOM_ICON)
                    || (requestCode == REQUEST_PICK_LANDSCAPE_ICON)) {

                String iconName = getIconFileName(mPendingIconIndex);
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
                Settings.System.putString(
                        getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingIconIndex], "");
                Settings.System.putString(
                        getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingIconIndex],
                        Uri.fromFile(
                                new File(mContext.getFilesDir(), iconName)).getPath());

                File f = new File(selectedImageUri.getPath());
                if (f.exists())
                    f.delete();

                Toast.makeText(
                        getActivity(),
                        mPendingIconIndex
                                + getResources().getString(
                                        R.string.custom_app_icon_successfully),
                        Toast.LENGTH_LONG).show();
                refreshSettings();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void refreshSettings() {

        int navbarQuantity = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3);

        PreferenceGroup targetGroup = (PreferenceGroup) findPreference("navbar_buttons");
        targetGroup.removeAll();

        PackageManager pm = mContext.getPackageManager();
        Resources res = mContext.getResources();

        for (int i = 0; i < navbarQuantity; i++) {
            final int index = i;
            NavBarItemPreference pAction = new NavBarItemPreference(getActivity());
            ListPreference mLongPress = new ListPreference(getActivity());
            // NavBarItemPreference pLongpress = new
            // NavBarItemPreference(getActivity());
            String dialogTitle = String.format(
                    getResources().getString(R.string.navbar_action_title), i + 1);
            pAction.setDialogTitle(dialogTitle);
            pAction.setEntries(R.array.navbar_button_entries);
            pAction.setEntryValues(R.array.navbar_button_values);
            String title = String.format(getResources().getString(R.string.navbar_action_title),
                    i + 1);
            pAction.setTitle(title);
            pAction.setKey("navbar_action_" + i);
            pAction.setSummary(getProperSummary(i, false));
            pAction.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(pAction);

            dialogTitle = String.format(
                    getResources().getString(R.string.navbar_longpress_title), i + 1);
            mLongPress.setDialogTitle(dialogTitle);
            mLongPress.setEntries(R.array.navbar_button_entries);
            mLongPress.setEntryValues(R.array.navbar_button_values);
            title = String.format(getResources().getString(R.string.navbar_longpress_title), i + 1);
            mLongPress.setTitle(title);
            mLongPress.setKey("navbar_longpress_" + i);
            mLongPress.setSummary(getProperSummary(i, true));
            mLongPress.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(mLongPress);

            pAction.setImageListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPendingIconIndex = index;
                    int width = 100;
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
                }
            });

            String customIconUri = Settings.System.getString(getContentResolver(),
                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[i]);
            if (customIconUri != null && customIconUri.length() > 0) {
                File f = new File(Uri.parse(customIconUri).getPath());
                if (f.exists())
                    pAction.setIcon(resize(new BitmapDrawable(res, f.getAbsolutePath())));
            }

            if (customIconUri != null && !customIconUri.equals("")
                    && customIconUri.startsWith("file")) {
                // it's an icon the user chose from the gallery here
                File icon = new File(Uri.parse(customIconUri).getPath());
                if (icon.exists())
                    pAction.setIcon(resize(new BitmapDrawable(getResources(), icon
                            .getAbsolutePath())));

            } else if (customIconUri != null && !customIconUri.equals("")) {
                // here they chose another app icon
                try {
                    pAction.setIcon(resize(pm.getActivityIcon(Intent.parseUri(customIconUri, 0))));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                // ok use default icons here
                pAction.setIcon(resize(getNavbarIconImage(i, false)));
            }
        }
    }

    private Drawable resize(Drawable image) {
        int size = 50;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources()
                .getDisplayMetrics());

        Bitmap d = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, px, px, false);
        return new BitmapDrawable(mContext.getResources(), bitmapOrig);
    }

    private Drawable getNavbarIconImage(int index, boolean landscape) {
        String uri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index]);

        if (uri == null)
            return getResources().getDrawable(R.drawable.ic_sysbar_null);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_home);
            } else if (uri.equals("**back**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_back);
            } else if (uri.equals("**recents**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_recent);
            } else if (uri.equals("**search**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_search);
            } else if (uri.equals("**screenshot**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_screenshot);
            } else if (uri.equals("**menu**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_menu_big);
             } else if (uri.equals("**ime**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_ime_switcher);
            } else if (uri.equals("**kill**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_killtask);
            } else if (uri.equals("**power**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_power);
            } else if (uri.equals("**notifications**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_notifications);
            } else if (uri.equals("**widgets**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_widget);
            }
        } else {
            try {
                return mContext.getPackageManager().getActivityIcon(Intent.parseUri(uri, 0));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return getResources().getDrawable(R.drawable.ic_sysbar_null);
    }

    private String getProperSummary(int i, boolean longpress) {
        String uri = "";
        if (longpress)
            uri = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[i]);
        else
            uri = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[i]);
        if (uri == null)
            return getResources().getString(R.string.navbar_action_none);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**"))
                return getResources().getString(R.string.navbar_action_home);
            else if (uri.equals("**back**"))
                return getResources().getString(R.string.navbar_action_back);
            else if (uri.equals("**recents**"))
                return getResources().getString(R.string.navbar_action_recents);
            else if (uri.equals("**search**"))
                return getResources().getString(R.string.navbar_action_search);
            else if (uri.equals("**screenshot**"))
                return getResources().getString(R.string.navbar_action_screenshot);
            else if (uri.equals("**menu**"))
                return getResources().getString(R.string.navbar_action_menu);
            else if (uri.equals("**ime**"))
                return getResources().getString(R.string.navbar_action_ime);
            else if (uri.equals("**kill**"))
                return getResources().getString(R.string.navbar_action_kill);
            else if (uri.equals("**power**"))
                return getResources().getString(R.string.navbar_action_power);
            else if (uri.equals("**notifications**"))
                return getResources().getString(R.string.navbar_action_notifications);
            else if (uri.equals("**null**"))
                return getResources().getString(R.string.navbar_action_none);
            else if (uri.equals("**widgets**"))
                return getResources().getString(R.string.navbar_widgets);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        if (Settings.System.putString(getActivity().getContentResolver(),
                mPendingNavBarCustomAction.activitySettingName, uri)) {
            if (mPendingNavBarCustomAction.iconIndex != -1) {
                if (bmp == null) {
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingNavBarCustomAction.iconIndex],
                                    "");
                } else {
                    String iconName = getIconFileName(mPendingNavBarCustomAction.iconIndex);
                    FileOutputStream iconStream = null;
                    try {
                        iconStream = mContext.openFileOutput(iconName, Context.MODE_WORLD_READABLE);
                    } catch (FileNotFoundException e) {
                        return; // NOOOOO
                    }
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingNavBarCustomAction.iconIndex], "");
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[mPendingNavBarCustomAction.iconIndex],
                                    Uri.fromFile(mContext.getFileStreamPath(iconName)).toString());
                }
            }
            mPendingNavBarCustomAction.preference.setSummary(friendlyName);
        }
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mPendingIconIndex + ".png"));

    }

    private String getIconFileName(int index) {
        return "navbar_icon_" + index + ".png";
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSettings();
    }

    public static class NavbarLayout extends ListFragment {
        private static final String TAG = "NavbarLayout";

        Context mContext;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            mContext = getActivity().getBaseContext();
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        };

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }

}
