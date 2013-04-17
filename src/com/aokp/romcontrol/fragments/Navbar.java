
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
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.AwesomeConstants.AwesomeConstant;
import com.android.internal.util.aokp.BackgroundAlphaColorDrawable;
import com.android.internal.util.aokp.NavBarHelpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.ROMControlActivity;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.SeekBarPreference;
import com.aokp.romcontrol.fragments.NavRingTargets;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class Navbar extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickerHelper.OnPickListener {

    // move these later
    private static final String PREF_MENU_UNLOCK = "pref_menu_display";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
    private static final String NAVIGATION_BAR_COLOR = "nav_bar_color";
    private static final String PREF_NAV_COLOR = "nav_button_color";
    private static final String NAVIGATION_BAR_ALLCOLOR = "navigation_bar_allcolor";
    private static final String PREF_NAV_GLOW_COLOR = "nav_button_glow_color";
    private static final String PREF_GLOW_TIMES = "glow_times";
    private static final String PREF_NAVBAR_QTY = "navbar_qty";
    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";
    private static final String NAVIGATION_BAR_WIDTH = "navigation_bar_width";
    private static final String NAVIGATION_BAR_WIDGETS = "navigation_bar_widgets";
    private static final String PREF_MENU_ARROWS = "navigation_bar_menu_arrow_keys";
    private static final String NAVBAR_HIDE_ENABLE = "navbar_hide_enable";
    private static final String NAVBAR_HIDE_TIMEOUT = "navbar_hide_timeout";
    private static final String DRAG_HANDLE_OPACITY = "drag_handle_opacity";
    private static final String DRAG_HANDLE_WIDTH = "drag_handle_width";

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;
    private static final int DIALOG_NAVBAR_ENABLE = 203;

    public static final String PREFS_NAV_BAR = "navbar";

    // move these later
    ColorPickerPreference mNavigationColor;
    ColorPickerPreference mNavigationBarColor;
    CheckBoxPreference mColorizeAllIcons;
    ColorPickerPreference mNavigationBarGlowColor;
    ListPreference mGlowTimes;
    ListPreference menuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    ListPreference mNavBarButtonQty;
    CheckBoxPreference mEnableNavigationBar;
    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarHeightLandscape;
    ListPreference mNavigationBarWidth;
    SeekBarPreference mButtonAlpha;
	Preference mWidthHelp;
    SeekBarPreference mWidthPort;
    SeekBarPreference mWidthLand;
    CheckBoxPreference mMenuArrowKeysCheckBox;
    Preference mConfigureWidgets;
    CheckBoxPreference mNavBarHideEnable;
    ListPreference mNavBarHideTimeout;
    SeekBarPreference mDragHandleOpacity;
    SeekBarPreference mDragHandleWidth;

    // NavBar Buttons Stuff
    Resources mResources;
    private ImageView mLeftMenu, mRightMenu;
    private ImageButton mResetButton, mAddButton,mSaveButton;
    private LinearLayout mNavBarContainer;
    private LinearLayout mNavButtonsContainer;
    private int mNumberofButtons = 0;
    private PackageManager mPackMan;
    ArrayList<NavBarButton> mButtons = new ArrayList<NavBarButton>();
    ArrayList<ImageButton> mButtonViews = new ArrayList<ImageButton>();
    String[] mActions;
    String[] mActionCodes;
    private int mPendingButton = -1;
    public final static int SHOW_LEFT_MENU = 1;
    public final static int SHOW_RIGHT_MENU = 0;
    public final static int SHOW_BOTH_MENU = 2;
    public final static int SHOW_DONT = 4;
    public static final float STOCK_ALPHA = .7f;

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
        mPackMan = getPackageManager();
        mResources = mContext.getResources();

        // Get NavBar Actions
        mActionCodes = NavBarHelpers.getNavBarActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
                    for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext, mActionCodes[i]);
        }

        menuDisplayLocation = (ListPreference) findPreference(PREF_MENU_UNLOCK);
        menuDisplayLocation.setOnPreferenceChangeListener(this);
        menuDisplayLocation.setValue(Settings.System.getInt(mContentRes,
                Settings.System.MENU_LOCATION,0) + "");

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);
        mNavBarMenuDisplay.setValue(Settings.System.getInt(mContentRes,
                Settings.System.MENU_VISIBILITY,0) + "");

        mNavBarHideEnable = (CheckBoxPreference) findPreference(NAVBAR_HIDE_ENABLE);
        mNavBarHideEnable.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.NAV_HIDE_ENABLE, false));

        final int defaultDragOpacity = Settings.System.getInt(mContentRes,
                Settings.System.DRAG_HANDLE_OPACITY,50);
        mDragHandleOpacity = (SeekBarPreference) findPreference(DRAG_HANDLE_OPACITY);
        mDragHandleOpacity.setInitValue((int) (defaultDragOpacity));
        mDragHandleOpacity.setOnPreferenceChangeListener(this);

        final int defaultDragWidth = Settings.System.getInt(mContentRes,
                Settings.System.DRAG_HANDLE_WEIGHT, 5);
        mDragHandleWidth = (SeekBarPreference) findPreference(DRAG_HANDLE_WIDTH);
        mDragHandleWidth.setInitValue((int) (defaultDragWidth));
        mDragHandleWidth.setOnPreferenceChangeListener(this);

        mNavBarHideTimeout = (ListPreference) findPreference(NAVBAR_HIDE_TIMEOUT);
        mNavBarHideTimeout.setOnPreferenceChangeListener(this);
        mNavBarHideTimeout.setValue(Settings.System.getInt(mContentRes,
                Settings.System.NAV_HIDE_TIMEOUT, 3000) + "");

        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        mEnableNavigationBar = (CheckBoxPreference) findPreference(ENABLE_NAVIGATION_BAR);
        mEnableNavigationBar.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault));

        mNavigationColor = (ColorPickerPreference) findPreference(NAVIGATION_BAR_COLOR);
        mNavigationColor.setOnPreferenceChangeListener(this);

        mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);

        mColorizeAllIcons = (CheckBoxPreference) findPreference("navigation_bar_allcolor");
        mColorizeAllIcons.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.NAVIGATION_BAR_ALLCOLOR, false));

        mNavigationBarGlowColor = (ColorPickerPreference) findPreference(PREF_NAV_GLOW_COLOR);
        mNavigationBarGlowColor.setOnPreferenceChangeListener(this);

        mGlowTimes = (ListPreference) findPreference(PREF_GLOW_TIMES);
        mGlowTimes.setOnPreferenceChangeListener(this);

        final float defaultButtonAlpha = Settings.System.getFloat(mContentRes,
                Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,0.6f);
        mButtonAlpha = (SeekBarPreference) findPreference("button_transparency");
        mButtonAlpha.setInitValue((int) (defaultButtonAlpha * 100));
        mButtonAlpha.setOnPreferenceChangeListener(this);

        mWidthHelp = (Preference) findPreference("width_help");

        float defaultPort = Settings.System.getFloat(mContentRes,
                Settings.System.NAVIGATION_BAR_WIDTH_PORT,0f);
        mWidthPort = (SeekBarPreference) findPreference("width_port");
        mWidthPort.setInitValue((int) (defaultPort * 2.5f));
        mWidthPort.setOnPreferenceChangeListener(this);

        float defaultLand = Settings.System.getFloat(mContentRes,
                Settings.System.NAVIGATION_BAR_WIDTH_LAND,0f);
        mWidthLand = (SeekBarPreference) findPreference("width_land");
        mWidthLand.setInitValue((int) (defaultLand * 2.5f));
        mWidthLand.setOnPreferenceChangeListener(this);

        mNavigationBarHeight = (ListPreference) findPreference("navigation_bar_height");
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        mNavigationBarHeightLandscape = (ListPreference) findPreference("navigation_bar_height_landscape");
        mNavigationBarHeightLandscape.setOnPreferenceChangeListener(this);

        mNavigationBarWidth = (ListPreference) findPreference("navigation_bar_width");
        mNavigationBarWidth.setOnPreferenceChangeListener(this);
        mConfigureWidgets = findPreference(NAVIGATION_BAR_WIDGETS);

        mMenuArrowKeysCheckBox = (CheckBoxPreference) findPreference(PREF_MENU_ARROWS);
        mMenuArrowKeysCheckBox.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.NAVIGATION_BAR_MENU_ARROW_KEYS, true));

        // don't allow devices that must use a navigation bar to disable it
        if (hasNavBarByDefault) {
            prefs.removePreference(mEnableNavigationBar);
        }
        PreferenceGroup pg = (PreferenceGroup) prefs.findPreference("advanced_cat");
        if (isTablet(mContext)) {
            mNavigationBarHeight.setTitle(R.string.system_bar_height_title);
            mNavigationBarHeight.setSummary(R.string.system_bar_height_summary);
            mNavigationBarHeightLandscape.setTitle(R.string.system_bar_height_landscape_title);
            mNavigationBarHeightLandscape.setSummary(R.string.system_bar_height_landscape_summary);
            pg.removePreference(mNavigationBarWidth);
            mNavBarHideEnable.setEnabled(false);
            mDragHandleOpacity.setEnabled(false);
            mDragHandleWidth.setEnabled(false);
            mNavBarHideTimeout.setEnabled(false);
        } else { // Phones&Phablets don't have SystemBar
            pg.removePreference(mWidthPort);
            pg.removePreference(mWidthLand);
            pg.removePreference(mWidthHelp);
            if (isPhablet(mContext)) { // Phablets don't have NavBar onside
                pg.removePreference(mNavigationBarWidth);
            } else {
                pg.removePreference(mNavigationBarHeightLandscape);
            }
        }

        if (Integer.parseInt(menuDisplayLocation.getValue()) == 4) {
            mNavBarMenuDisplay.setEnabled(false);
        }

        refreshSettings();
        setHasOptionsMenu(true);
        updateGlowTimesSummary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedinstanceState){
       View ll = inflater.inflate(R.layout.navbar, container, false);
       mResetButton = (ImageButton) ll.findViewById(R.id.reset_button);
       mResetButton.setOnClickListener(mCommandButtons);
       mAddButton = (ImageButton) ll.findViewById(R.id.add_button);
       mAddButton.setOnClickListener(mCommandButtons);
       mSaveButton = (ImageButton) ll.findViewById(R.id.save_button);
       mSaveButton.setOnClickListener(mCommandButtons);
       mLeftMenu = (ImageView) ll.findViewById(R.id.left_menu);
       mNavBarContainer = (LinearLayout) ll.findViewById(R.id.navbar_container);
       mNavButtonsContainer = (LinearLayout) ll.findViewById(R.id.button_container);
       mButtonViews.clear();
       for (int i = 0; i < mNavButtonsContainer.getChildCount(); i++) {
           ImageButton ib = (ImageButton) mNavButtonsContainer.getChildAt(i);
           mButtonViews.add(ib);
       }
       mRightMenu = (ImageView) ll.findViewById(R.id.right_menu);
       if (mButtons.size() == 0){
           loadButtons();
       }
       refreshButtons();
       return ll;
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
                Settings.System.putInt(mContentRes,
                        Settings.System.NAVIGATION_BAR_COLOR, -1);
                Settings.System.putInt(mContentRes,
                        Settings.System.NAVIGATION_BAR_TINT, -1);
                Settings.System.putInt(mContentRes,
                        Settings.System.NAVIGATION_BAR_GLOW_TINT, -1);
                Settings.System.putInt(mContentRes,
                        Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3);

                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[0], "**back**");
                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[1], "**home**");
                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[2], "**recents**");

                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[0], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[1], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[2], "**null**");

                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[0], "");
                Settings.System.putString(mContentRes,
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[1], "");
                Settings.System.putString(mContentRes,
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

            Settings.System.putInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_SHOW,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mColorizeAllIcons) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.NAVIGATION_BAR_ALLCOLOR,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
        } else if (preference == mNavBarHideEnable) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.NAV_HIDE_ENABLE,
                    ((CheckBoxPreference) preference).isChecked());
            mDragHandleOpacity.setInitValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.DRAG_HANDLE_OPACITY,50));
            mDragHandleWidth.setInitValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.DRAG_HANDLE_WEIGHT,5));
            mNavBarHideTimeout.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NAV_HIDE_TIMEOUT, 3000) + "");
            refreshSettings();
            return true;
        } else if (preference == mConfigureWidgets) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            WidgetConfigurationFragment fragment = new WidgetConfigurationFragment();
            ft.addToBackStack("config_widgets");
            ft.replace(this.getId(), fragment);
            ft.commit();
            return true;
        } else if (preference == mMenuArrowKeysCheckBox) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.NAVIGATION_BAR_MENU_ARROW_KEYS,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == menuDisplayLocation) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.MENU_LOCATION, val);
            refreshSettings();
            mNavBarMenuDisplay.setEnabled(val < 4 ? true : false);
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            Settings.System.putInt(mContentRes,
                    Settings.System.MENU_VISIBILITY, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavigationBarWidth) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int width = mapChosenDpToPixels(dp);
            Settings.System.putInt(mContentRes, Settings.System.NAVIGATION_BAR_WIDTH,
                    width);
            return true;
        } else if (preference == mNavigationBarHeight) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(mContentRes, Settings.System.NAVIGATION_BAR_HEIGHT,
                    height);
            return true;
        } else if (preference == mNavBarHideTimeout) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.NAV_HIDE_TIMEOUT, val);
            return true;
        } else if (preference == mNavigationBarHeightLandscape) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE,
                    height);
            return true;
        } else if (preference == mNavigationColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex) & 0x00FFFFFF;
            Settings.System.putInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_COLOR, intHex);
            refreshSettings();
            return true;
        } else if (preference == mNavigationBarColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_TINT, intHex);
            refreshSettings();
            return true;
        } else if (preference == mNavigationBarGlowColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_GLOW_TINT, intHex);
            refreshSettings();
            return true;
        } else if (preference == mGlowTimes) {
            // format is (on|off) both in MS
            String value = (String) newValue;
            String[] breakIndex = value.split("\\|");
            int onTime = Integer.valueOf(breakIndex[0]);
            int offTime = Integer.valueOf(breakIndex[1]);

            Settings.System.putInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[0], offTime);
            Settings.System.putInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[1], onTime);
            updateGlowTimesSummary();
            return true;
        } else if (preference == mButtonAlpha) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(mContentRes,
                    Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                    val * 0.01f);
            refreshSettings();
            return true;
       } else if (preference == mDragHandleOpacity) {
            String newVal = (String) newValue;
            int op = Integer.parseInt(newVal);
            Settings.System.putInt(mContentRes,
                    Settings.System.DRAG_HANDLE_OPACITY, op);
            return true;
        } else if (preference == mDragHandleWidth) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            //int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(mContentRes,
                    Settings.System.DRAG_HANDLE_WEIGHT, dp);
            return true;
        } else if (preference == mWidthPort) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(mContentRes,
                    Settings.System.NAVIGATION_BAR_WIDTH_PORT,
                    val * 0.4f);
            return true;
        } else if (preference == mWidthLand) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(mContentRes,
                    Settings.System.NAVIGATION_BAR_WIDTH_LAND,
                    val * 0.4f);
            return true;

        }
        return false;
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        return null;
    }

    private void updateGlowTimesSummary() {
        int resId;
        String combinedTime = Settings.System.getString(mContentRes,
                Settings.System.NAVIGATION_BAR_GLOW_DURATION[1]) + "|" +
                Settings.System.getString(mContentRes,
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

    public void refreshSettings() {
        refreshButtons();
        if (!isTablet(mContext)) {
            mDragHandleOpacity.setEnabled(mNavBarHideEnable.isChecked());
            mDragHandleWidth.setEnabled(mNavBarHideEnable.isChecked());
            mNavBarHideTimeout.setEnabled(mNavBarHideEnable.isChecked());
        }
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mPendingButton + ".png"));

    }

    private String getIconFileName(int index) {
        return "navbar_icon_" + index + ".png";
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private View.OnClickListener mNavBarClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mPendingButton = mButtonViews.indexOf(v);
            if (mPendingButton > -1 && mPendingButton < mNumberofButtons) {
                createDialog(mButtons.get(mPendingButton));
            }
        }
    };

    private void loadButtons(){
        mNumberofButtons =  Settings.System.getInt(mContentRes,
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3);
        mButtons.clear();
        for (int i = 0; i < mNumberofButtons; i++) {
            String click = Settings.System.getString(mContentRes,
                    Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[i]);
            String longclick = Settings.System.getString(mContentRes,
                    Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[i]);
            String iconuri = Settings.System.getString(mContentRes,
                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[i]);
            mButtons.add(new NavBarButton(click, longclick, iconuri));
        }
    }

    public void refreshButtons() {
        if (mNumberofButtons == 0) {
            return;
        }
        int navBarColor = Settings.System.getInt(mContentRes,
                Settings.System.NAVIGATION_BAR_COLOR, -1);
        int navButtonColor = Settings.System.getInt(mContentRes,
                Settings.System.NAVIGATION_BAR_TINT, -1);
        float navButtonAlpha = Settings.System.getFloat(mContentRes,
                Settings.System.NAVIGATION_BAR_BUTTON_ALPHA, STOCK_ALPHA);
        int glowColor = Settings.System.getInt(mContentRes,
                Settings.System.NAVIGATION_BAR_GLOW_TINT, 0);
        float BarAlpha = 1.0f;
        String alphas[];
        String settingValue = Settings.System.getString(mContentRes,
                Settings.System.NAVIGATION_BAR_ALPHA_CONFIG);
        if (!TextUtils.isEmpty(settingValue)) {
            alphas = settingValue.split(";");
            BarAlpha = Float.parseFloat(alphas[0]) / 255;
        }
        int a = Math.round(BarAlpha * 255);
        Drawable mBackground = AwesomeConstants.getSystemUIDrawable(mContext, "com.android.systemui:drawable/nav_bar_bg");
        if (mBackground instanceof ColorDrawable) {
            BackgroundAlphaColorDrawable bacd = new BackgroundAlphaColorDrawable(
                    navBarColor > 0 ? navBarColor : ((ColorDrawable) mBackground).getColor());
            bacd.setAlpha(a);
            mNavBarContainer.setBackground(bacd);
        } else {
            mBackground.setAlpha(a);
            mNavBarContainer.setBackground(mBackground);
        }
        for (int i = 0; i < mNumberofButtons; i++) {
            ImageButton ib = mButtonViews.get(i);
            Drawable d = mButtons.get(i).getIcon();
            if (navButtonColor != -1) {
                d.setColorFilter(navButtonColor, PorterDuff.Mode.SRC_ATOP);
            }
            ib.setImageDrawable(d);
            ib.setOnClickListener(mNavBarClickListener);
            ib.setVisibility(View.VISIBLE);
            ib.setAlpha(navButtonAlpha);
            StateListDrawable sld = new StateListDrawable();
            sld.addState(new int[] { android.R.attr.state_pressed }, new ColorDrawable(glowColor));
            sld.addState(StateSet.WILD_CARD, mNavBarContainer.getBackground());
            ib.setBackground(sld);
        }
        for (int i = mNumberofButtons; i < mButtonViews.size(); i++){
            ImageButton ib = mButtonViews.get(i);
            ib.setVisibility(View.GONE);
        }
        int menuloc = Settings.System.getInt(mContentRes,
                Settings.System.MENU_LOCATION, 0);
        switch (menuloc) {
            case SHOW_BOTH_MENU:
                mLeftMenu.setVisibility(View.VISIBLE);
                mRightMenu.setVisibility(View.VISIBLE);
                break;
            case SHOW_LEFT_MENU:
                mLeftMenu.setVisibility(View.VISIBLE);
                mRightMenu.setVisibility(View.INVISIBLE);
                break;
            case SHOW_RIGHT_MENU:
                mLeftMenu.setVisibility(View.INVISIBLE);
                mRightMenu.setVisibility(View.VISIBLE);
                break;
            case SHOW_DONT:
                mLeftMenu.setVisibility(View.GONE);
                mRightMenu.setVisibility(View.GONE);
                break;
        }
        if (navButtonColor != -1) {
            mLeftMenu.setColorFilter(navButtonColor);
            mRightMenu.setColorFilter(navButtonColor);
        }
    }

    private void saveButtons(){
        Settings.System.putInt(mContentRes,Settings.System.NAVIGATION_BAR_BUTTONS_QTY,
                mNumberofButtons);
        for (int i = 0; i < mNumberofButtons; i++) {
            NavBarButton button = mButtons.get(i);
            Settings.System.putString(mContentRes, Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[i],
                    button.getClickAction());
            Settings.System.putString(mContentRes, Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[i],
                    button.getLongAction());
            Settings.System.putString(mContentRes, Settings.System.NAVIGATION_CUSTOM_APP_ICONS[i],
                    button.getIconURI());
        }
    }

    private void createDialog(final NavBarButton button) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onDialogClick(button, item);
                dialog.dismiss();
                }
            };

        String action = mResources.getString(R.string.navbar_actiontitle_menu);
        action = String.format(action, button.getClickName());
        String longpress = mResources.getString(R.string.navbar_longpress_menu);
        longpress = String.format(longpress, button.getLongName());
        String[] items = {action, longpress,
                mResources.getString(R.string.navbar_icon_menu),
                mResources.getString(R.string.navbar_delete_menu)};
        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(mResources.getString(R.string.navbar_title_menu))
                .setItems(items, l)
                .create();

        dialog.show();
    }

    private void createActionDialog(final NavBarButton button) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onActionDialogClick(button, item);
                dialog.dismiss();
                }
            };

        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(mResources.getString(R.string.navbar_title_menu))
                .setItems(mActions, l)
                .create();

        dialog.show();
    }

    private void onDialogClick(NavBarButton button, int command){
        switch (command) {
            case 0: // Set Click Action
                button.setPickLongPress(false);
                createActionDialog(button);
                break;
            case 1: // Set Long Press Action
                button.setPickLongPress(true);
                createActionDialog(button);
                break;
            case 2: // set Custom Icon
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
                startActivityForResult(intent,REQUEST_PICK_CUSTOM_ICON);
                break;
            case 3: // Delete Button
                mButtons.remove(mPendingButton);
                mNumberofButtons--;
                break;
        }
        refreshButtons();
    }

    private void onActionDialogClick(NavBarButton button, int command){
        if (command == mActions.length -1) {
            // This is the last action - should be **app**
                mPicker.pickShortcut();
        } else { // This should be any other defined action.
            if (button.getPickLongPress()) {
                button.setLongPress(AwesomeConstants.AwesomeActions()[command]);
            } else {
                button.setClickAction(AwesomeConstants.AwesomeActions()[command]);
            }
        }
        refreshButtons();
    }

    private View.OnClickListener mCommandButtons = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int command = v.getId();
            switch (command) {
                case R.id.reset_button:
                    loadButtons();
                    break;
                case R.id.add_button:
                    if (mNumberofButtons < 7) { // Maximum buttons is 7
                        mButtons.add(new NavBarButton("**null**","**null**",""));
                        mNumberofButtons++;
                    }
                    break;
                case R.id.save_button:
                    saveButtons();
                    break;
            }
            refreshButtons();
        }
    };

    private Drawable setIcon(String uri, String action) {
        if (uri != null && uri.length() > 0) {
            File f = new File(Uri.parse(uri).getPath());
            if (f.exists())
                return resize(new BitmapDrawable(mResources, f.getAbsolutePath()));
        }
        if (uri != null && !uri.equals("")
                && uri.startsWith("file")) {
            // it's an icon the user chose from the gallery here
            File icon = new File(Uri.parse(uri).getPath());
            if (icon.exists())
                return resize(new BitmapDrawable(mResources, icon
                        .getAbsolutePath()));

        } else if (uri != null && !uri.equals("")) {
            // here they chose another app icon
            try {
                return resize(mPackMan.getActivityIcon(Intent.parseUri(uri, 0)));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            // ok use default icons here
        }
        return resize(getNavbarIconImage(action));
    }

    private Drawable getNavbarIconImage(String uri) {
        if (uri == null)
            uri = AwesomeConstant.ACTION_NULL.value();
        if (uri.startsWith("**")) {
            return AwesomeConstants.getActionIcon(mContext, uri);
        } else {
            try {
                return mPackMan.getActivityIcon(Intent.parseUri(uri, 0));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return mResources.getDrawable(R.drawable.ic_sysbar_null);
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        NavBarButton button = mButtons.get(mPendingButton);
        boolean longpress = button.getPickLongPress();

        if (!longpress) {
            button.setClickAction(uri);
            if (bmp == null) {
                button.setIconURI("");
            } else {
                String iconName = getIconFileName(mPendingButton);
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput(iconName, Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }
                bmp.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                button.setIconURI(Uri.fromFile(mContext.getFileStreamPath(iconName)).toString());
            }
        } else {
            button.setLongPress(uri);
        }
        refreshButtons();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "RequestCode:"+resultCode);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if (requestCode == REQUEST_PICK_CUSTOM_ICON) {
                String iconName = getIconFileName(mPendingButton);
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
                    return;
                }
                mButtons.get(mPendingButton).setIconURI(Uri.fromFile(
                                new File(mContext.getFilesDir(), iconName)).getPath());

                File f = new File(selectedImageUri.getPath());
                if (f.exists())
                    f.delete();
                refreshButtons();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getProperSummary(String uri) {
        if (uri == null)
            return AwesomeConstants.getProperName(mContext, "**null**");
        if (uri.startsWith("**")) {
            return AwesomeConstants.getProperName(mContext, uri);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
    }

    private Drawable resize(Drawable image) {
        int size = 50;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
                mResources.getDisplayMetrics());

        Bitmap d = ((BitmapDrawable) image).getBitmap();
        if (d == null) {
            return mResources.getDrawable(R.drawable.ic_sysbar_null);
        } else {
            Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, px, px, false);
            return new BitmapDrawable(mResources, bitmapOrig);
        }
    }

    public class NavBarButton {
        String mClickAction;
        String mLongPressAction;
        String mIconURI;
        String mClickFriendlyName;
        String mLongPressFriendlyName;
        Drawable mIcon;
        boolean mPickingLongPress;

        public NavBarButton(String clickaction, String longpress, String iconuri ) {
            mClickAction = clickaction;
            mLongPressAction = longpress;
            mIconURI = iconuri;
            mClickFriendlyName = getProperSummary(mClickAction);
            mLongPressFriendlyName = getProperSummary (mLongPressAction);
            mIcon = setIcon(mIconURI,mClickAction);
        }

        public void setClickAction(String click) {
            mClickAction = click;
            mClickFriendlyName = getProperSummary(mClickAction);
            // ClickAction was reset - so we should default to stock Icon for now
            mIconURI = "";
            mIcon = setIcon(mIconURI,mClickAction);
        }

        public void setLongPress(String action) {
            mLongPressAction = action;
            mLongPressFriendlyName = getProperSummary (mLongPressAction);
        }
        public void setPickLongPress(boolean pick) {
            mPickingLongPress = pick;
        }
        public boolean getPickLongPress() {
            return mPickingLongPress;
        }
        public void setIconURI (String uri) {
            mIconURI = uri;
            mIcon = setIcon(mIconURI,mClickAction);
        }
        public String getClickName() {
            return mClickFriendlyName;
        }
        public String getLongName() {
            return mLongPressFriendlyName;
        }
        public Drawable getIcon() {
            return mIcon;
        }
        public String getClickAction() {
            return mClickAction;
        }
        public String getLongAction() {
            return mLongPressAction;
        }
        public String getIconURI() {
            return mIconURI;
        }
    }
}
