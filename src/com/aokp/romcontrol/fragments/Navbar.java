
package com.aokp.romcontrol.fragments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.Activity;
import android.app.AlertDialog;
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
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.NavBarItemPreference;
import com.aokp.romcontrol.widgets.SeekBarPreference;
import com.aokp.romcontrol.widgets.TouchInterceptor;

public class Navbar extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickerHelper.OnPickListener {

    // move these later
    private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
    private static final String PREF_NAV_COLOR = "nav_button_color";
    private static final String PREF_NAV_GLOW_COLOR = "nav_button_glow_color";
    private static final String PREF_MENU_UNLOCK = "pref_menu_display";
    private static final String PREF_NAVBAR_QTY = "navbar_qty";
    private static final String COMBINED_BAR_AUTO_HIDE = "combined_bar_auto_hide";

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    public static final int APP_WIDGET_HOST_ID = 2112;

    public static final String ACTION_ALLOCATE_ID = "com.android.systemui.ACTION_ALLOCATE_ID";
    public static final String ACTION_DEALLOCATE_ID = "com.android.systemui.ACTION_DEALLOCATE_ID";
    public static final String ACTION_SEND_ID = "com.android.systemui.ACTION_SEND_ID";
    public int mWidgetIdQty = 0;

    public static final String PREFS_NAV_BAR = "navbar";

    // move these later
    ColorPickerPreference mNavigationBarColor;
    ColorPickerPreference mNavigationBarGlowColor;
    ListPreference menuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    ListPreference mGlowTimes;
    ListPreference mNavBarButtonQty;
    SeekBarPreference mButtonAlpha;

    CheckBoxPreference mEnableNavigationBar;
    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarWidth;
    CheckBoxPreference mCombinedBarAutoHide;

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

    BroadcastReceiver mWidgetIdReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "widget id receiver go!");

            // Need to De-Allocate the ID that this was replacing.
            if (widgetIds[mPendingWidgetDrawer] != -1) {
            	Intent delete = new Intent();
            	delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetIds[mPendingWidgetDrawer]);
                delete.setAction(ACTION_DEALLOCATE_ID);
                mContext.sendBroadcast(delete);
            }
            widgetIds[mPendingWidgetDrawer] = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            String summary = intent.getStringExtra("summary");
            mPendingPreference.setSummary(summary);

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAV_BAR,
                    Context.MODE_WORLD_WRITEABLE);
            prefs.edit().putString(mPendingPreference.getKey(), summary).apply();

            saveWidgets();
        };
    };

    private static final String TAG = "NavBar";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_navbar);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

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

        mNavBarButtonQty = (ListPreference) findPreference(PREF_NAVBAR_QTY);
        mNavBarButtonQty.setOnPreferenceChangeListener(this);
        mNavBarButtonQty.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3) + "");

        mPicker = new ShortcutPickerHelper(this, this);

        mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);

        mNavigationBarGlowColor = (ColorPickerPreference) findPreference(PREF_NAV_GLOW_COLOR);
        mNavigationBarGlowColor.setOnPreferenceChangeListener(this);

        mGlowTimes = (ListPreference) findPreference("glow_times");
        mGlowTimes.setOnPreferenceChangeListener(this);
        // mGlowTimes.setValue(Settings.System.getInt(getActivity()

        float defaultAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                0.6f);
        mButtonAlpha = (SeekBarPreference) findPreference("button_transparency");
        mButtonAlpha.setInitValue((int) (defaultAlpha * 100));
        mButtonAlpha.setOnPreferenceChangeListener(this);

        mCombinedBarAutoHide = (CheckBoxPreference) findPreference(COMBINED_BAR_AUTO_HIDE);
        mCombinedBarAutoHide.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.COMBINED_BAR_AUTO_HIDE, 0) == 1);

        // Only tablets need this
        if (!mTablet) {
            ((PreferenceGroup) findPreference("advanced_cat")).removePreference(mCombinedBarAutoHide);
        }

        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        mEnableNavigationBar = (CheckBoxPreference) findPreference("enable_nav_bar");
        mEnableNavigationBar.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_SHOW, hasNavBarByDefault ? 1 : 0) == 1);

        // don't allow devices that must use a navigation bar to disable it
        if (hasNavBarByDefault || mTablet) {
            prefs.removePreference(mEnableNavigationBar);
        }
        mNavigationBarHeight = (ListPreference) findPreference("navigation_bar_height");
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        mNavigationBarWidth = (ListPreference) findPreference("navigation_bar_width");
        mNavigationBarWidth.setOnPreferenceChangeListener(this);
        
        if (mTablet) {
            Log.e("NavBar", "is tablet");
            prefs.removePreference(mNavBarMenuDisplay);
        }
        refreshSettings();
        setHasOptionsMenu(true);
        updateGlowTimesSummary();

        IntentFilter filter = new IntentFilter(ACTION_SEND_ID);
        mContext.registerReceiver(mWidgetIdReceiver, filter);
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
                Settings.System.putFloat(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                        0.6f);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BUTTONS_SHOW, mContext.getResources()
                                .getBoolean(
                                        com.android.internal.R.bool.config_showNavigationBar) ? 1
                                : 0);
                mButtonAlpha.setValue(60);

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
                resetNavBarWidgets();
                refreshSettings();
                return true;
            case R.id.reset_widgets:
            	resetNavBarWidgets();
            	refreshSettings();
				return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        String key = preference.getKey();
        if (preference == mEnableNavigationBar) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTONS_SHOW,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);

            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.navbar_enable_dialog_title))
                    .setMessage(getResources().getString(R.string.navbar_enable_dialog_msg))
                    .setNegativeButton(
                            getResources().getString(R.string.navbar_enable_dialog_negative), null)
                    .setCancelable(false)
                    .setPositiveButton(
                            getResources().getString(R.string.navbar_enable_dialog_Positive),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PowerManager pm = (PowerManager) getActivity()
                                            .getSystemService(Context.POWER_SERVICE);
                                    pm.reboot("New navbar");
                                }
                            })
                    .create()
                    .show();

            return true;

        } else if (key.startsWith("navbar_widget_")) {
        	if (key.startsWith("navbar_widget_add")) {
        		PreferenceGroup targetGroup = (PreferenceGroup) findPreference("navbar_widgets");
        		Preference p = new Preference(mContext);
        		p.setKey("navbar_widget_add");
                p.setTitle(getResources().getString(R.string.navigation_bar_add_new_widget_title));
                p.setSummary(getResources().getString(R.string.navigation_bar_add_new_widget_summary));
                targetGroup.addPreference(p);
                mPendingWidgetDrawer = mWidgetIdQty;    
        		mPendingPreference = preference;
        		mPendingPreference.setKey("navbar_widget_" + mWidgetIdQty);
        		mPendingPreference.setTitle(getResources().getString(R.string.navigation_bar_widget) + (mWidgetIdQty + 1));
        		mWidgetIdQty++;
        	} else {
        		mPendingPreference = preference;
        		mPendingWidgetDrawer = Integer.parseInt(key.substring("navbar_widget_".length()));
        	}
            Log.i(TAG, "pending widget: " + mPendingWidgetDrawer);
            // selectWidget();
            // send intent to pick a new widget
            Intent send = new Intent();
            send.setAction(ACTION_ALLOCATE_ID);
            mContext.sendBroadcast(send);

            return true;

        } else if (preference == mCombinedBarAutoHide) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.COMBINED_BAR_AUTO_HIDE, checked ? 1 : 0);
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
        } else if (preference == mNavigationBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_TINT, intHex);
            return true;

        } else if (preference == mNavigationBarGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_TINT, intHex);
            return true;

        } else if (preference == mNavBarButtonQty) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTONS_QTY, val);
            refreshSettings();
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
            Log.e("R", "value: " + val / 100);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                    val / 100);
            return true;
        } else if (preference == mNavigationBarWidth) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int width = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_WIDTH,
                    width);
            toggleBar();
            return true;
        } else if (preference == mNavigationBarHeight) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getContentResolver(), Settings.System.NAVIGATION_BAR_HEIGHT,
                    height);
            toggleBar();
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
        }

        return false;
    }

    public void toggleBar() {
        boolean isBarOn = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_SHOW, 1) == 1;
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_SHOW, isBarOn ? 0 : 1);
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS_SHOW, isBarOn ? 1 : 0);
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
            case 42:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_42);
            case 36:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_36);
            case 30:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_30);
            case 24:
                return getResources().getDimensionPixelSize(R.dimen.navigation_bar_24);
        }
        return -1;
    }

    public static void addButton(Context context, String key) {
        ArrayList<String> enabledToggles = getButtonsStringArray(context);
        enabledToggles.add(key);
        setButtonsFromStringArray(context, enabledToggles);
    }

    public static void removeButton(Context context, String key) {
        ArrayList<String> enabledToggles = getButtonsStringArray(context);
        enabledToggles.remove(key);
        setButtonsFromStringArray(context, enabledToggles);
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
                Log.e(TAG, "Selected image path: " + selectedImageUri.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);

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
                                        R.string.lockscreen_custom_app_icon_successfully),
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

        targetGroup = (PreferenceGroup) findPreference("navbar_widgets");
        targetGroup.removeAll();
        
        // calculate number of Widgets
        String settingWidgets = Settings.System.getString(getContentResolver(),
        		Settings.System.NAVIGATION_BAR_WIDGETS);
        if (settingWidgets != null && settingWidgets.length() > 0) {
        	String[] split = settingWidgets.split("\\|");
        	mWidgetIdQty = split.length;
        } else {
        	mWidgetIdQty = 0;
        }
        widgetIds = new int[mWidgetIdQty+1];
        Log.i(TAG, "widgets: " + settingWidgets);
        if (settingWidgets != null && settingWidgets.length() > 0) {
            String[] split = settingWidgets.split("\\|");
            for (int i = 0; i < split.length; i++) {
                if (split[i].length() > 0)
                    widgetIds[i] = Integer.parseInt(split[i]);
            }
        }

        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAV_BAR,
                Context.MODE_WORLD_WRITEABLE);
        for (int i = 0; i < (mWidgetIdQty); i++) {
            Preference p = new Preference(mContext);
            p.setKey("navbar_widget_" + i);
            p.setTitle(getResources().getString(R.string.navigation_bar_widget) + (i + 1));
            if (widgetIds[i] != -1)
                p.setSummary(prefs.getString("navbar_widget_" + i, "None"));
            targetGroup.addPreference(p);
        }
        // add button to increase widgets
        // set Widget ID to -1 for 'add button'
        widgetIds[mWidgetIdQty] = -1;
        Preference p = new Preference(mContext);
        p.setKey("navbar_widget_add");
        p.setTitle(getResources().getString(R.string.navigation_bar_add_new_widget_title));
        p.setSummary(getResources().getString(R.string.navigation_bar_add_new_widget_summary));
        targetGroup.addPreference(p);

    }

    int widgetIds[];

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
            } else if (uri.equals("**menu**")) {

                return getResources().getDrawable(R.drawable.ic_sysbar_menu_land_big);
            } else if (uri.equals("**kill**")) {

                return getResources().getDrawable(R.drawable.ic_sysbar_killtask);
            } else if (uri.equals("**power**")) {

                return getResources().getDrawable(R.drawable.ic_sysbar_power);
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
            else if (uri.equals("**menu**"))
                return getResources().getString(R.string.navbar_action_menu);
            else if (uri.equals("**kill**"))
                return getResources().getString(R.string.navbar_action_kill);
            else if (uri.equals("**power**"))
                return getResources().getString(R.string.navbar_action_power);
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
        private ListView mButtonList;
        private ButtonAdapter mButtonAdapter;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            mContext = getActivity().getBaseContext();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(
                    R.layout.order_power_widget_buttons_activity, container, false);

            return v;
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mButtonList = this.getListView();
            ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
            mButtonAdapter = new ButtonAdapter(mContext);
            setListAdapter(mButtonAdapter);
        };

        @Override
        public void onDestroy() {
            ((TouchInterceptor) mButtonList).setDropListener(null);
            setListAdapter(null);
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            // reload our buttons and invalidate the views for redraw
            mButtonAdapter.reloadButtons();
            mButtonList.invalidateViews();
        }

        private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                // get the current button list
                ArrayList<String> toggles = getButtonsStringArray(mContext);

                // move the button
                if (from < toggles.size()) {
                    String toggle = toggles.remove(from);

                    if (to <= toggles.size()) {
                        toggles.add(to, toggle);

                        // save our buttons
                        setButtonsFromStringArray(mContext, toggles);

                        // tell our adapter/listview to reload
                        mButtonAdapter.reloadButtons();
                        mButtonList.invalidateViews();
                    }
                }
            }
        };

        private class ButtonAdapter extends BaseAdapter {
            private Context mContext;
            private Resources mSystemUIResources = null;
            private LayoutInflater mInflater;
            private ArrayList<Toggle> mToggles;

            public ButtonAdapter(Context c) {
                mContext = c;
                mInflater = LayoutInflater.from(mContext);

                PackageManager pm = mContext.getPackageManager();
                if (pm != null) {
                    try {
                        mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                    } catch (Exception e) {
                        mSystemUIResources = null;
                        Log.e(TAG, "Could not load SystemUI resources", e);
                    }
                }

                reloadButtons();
            }

            public void reloadButtons() {
                ArrayList<String> toggles = getButtonsStringArray(mContext);

                mToggles = new ArrayList<Toggle>();
                for (String toggle : toggles) {
                    mToggles.add(new Toggle(toggle, 0));
                }
            }

            public int getCount() {
                return mToggles.size();
            }

            public Object getItem(int position) {
                return mToggles.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                final View v;
                if (convertView == null) {
                    v = mInflater.inflate(R.layout.order_power_widget_button_list_item, null);
                } else {
                    v = convertView;
                }

                Toggle toggle = mToggles.get(position);
                final TextView name = (TextView) v.findViewById(R.id.name);
                name.setText(toggle.getId());
                return v;
            }
        }

        public class Toggle {
            private String mId;
            private int mTitleResId;

            public Toggle(String id, int titleResId) {
                mId = id;
                mTitleResId = titleResId;
            }

            public String getId() {
                return mId;
            }

            public int getTitleResId() {
                return mTitleResId;
            }
        }
    }

    public static void setButtonsFromStringArray(Context c, ArrayList<String> newGoodies) {
        String newToggles = "";

        for (String s : newGoodies)
            newToggles += s + "|";

        // remote last |
        newToggles = newToggles.substring(0, newToggles.length() - 1);

        Settings.System.putString(c.getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTONS,
                newToggles);
    }

    public static ArrayList<String> getButtonsStringArray(Context c) {
        String clusterfuck = Settings.System.getString(c.getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS);

        if (clusterfuck == null) {
            clusterfuck = "BACK|HOME|TASKS";
        }

        String[] togglesStringArray = clusterfuck.split("\\|");
        ArrayList<String> iloveyou = new ArrayList<String>();
        for (String s : togglesStringArray) {
            iloveyou.add(s);
        }

        return iloveyou;
    }

    private void saveWidgets() {
        StringBuilder widgetString = new StringBuilder();
        for (int i = 0; i < (mWidgetIdQty); i++) {
            widgetString.append(widgetIds[i]);
            if (i != (mWidgetIdQty - 1))
                widgetString.append("|");
        }
        Settings.System.putString(getContentResolver(), Settings.System.NAVIGATION_BAR_WIDGETS,
                widgetString.toString());
    }
    
    private void resetNavBarWidgets() {
    	for (int i = 0; i < (mWidgetIdQty); i++) {
    		if (widgetIds[i] != -1) {
            	Intent delete = new Intent();
            	delete.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetIds[i]);
                delete.setAction(ACTION_DEALLOCATE_ID);
                mContext.sendBroadcast(delete);
            }
    	}
    	Settings.System.putString(getActivity().getContentResolver(), 
        		Settings.System.NAVIGATION_BAR_WIDGETS,"");
    }

}
