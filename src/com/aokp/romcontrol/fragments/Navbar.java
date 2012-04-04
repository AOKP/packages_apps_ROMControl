
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.content.res.Resources;
import android.os.Bundle;
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
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.SeekBarPreference;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.TouchInterceptor;
import com.aokp.romcontrol.widgets.NavBarItemPreference;
import com.aokp.romcontrol.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;


public class Navbar extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickerHelper.OnPickListener {

    // move these later
    private static final String PREF_EANBLED_BUTTONS = "enabled_buttons";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
    private static final String PREF_NAV_COLOR = "nav_button_color";
    private static final String PREF_MENU_UNLOCK = "pref_menu_display";
    private static final String PREF_HOME_LONGPRESS = "long_press_home";
    private static final String PREF_NAVBAR_QTY = "navbar_qty";
    
    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;
    
    // move these later
    ColorPickerPreference mNavigationBarColor;
    ListPreference menuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    ListPreference mHomeLongpress;
    ListPreference mGlowTimes;
    ListPreference mNavBarButtonQty;
    Preference mNavBarEnabledButtons;
    Preference mLayout;
    SeekBarPreference mButtonAlpha;

    CheckBoxPreference mEnableNavigationBar;
    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarWidth;
    
    ShortcutPickerHelper mPicker;
    
    String mCustomAppString;

    private final String[] buttons = {
            "HOME", "BACK", "TASKS", "SEARCH", "MENU_BIG"
    };

    private int currentIconIndex;
    private Preference mCurrentCustomActivityPreference;
    private String mCurrentCustomActivityString;

    private ShortcutPickerHelper mPicker;

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
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 0) + "");
        
        mPicker = new ShortcutPickerHelper(this, this);

        mHomeLongpress = (ListPreference) findPreference(PREF_HOME_LONGPRESS);
        mHomeLongpress.setOnPreferenceChangeListener(this);
        int lpv = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_BAR_HOME_LONGPRESS, 0);
        mHomeLongpress.setValue(lpv + "");
        mHomeLongpress.setSummary(getProperSummary(lpv));

        mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
        mNavigationBarColor.setOnPreferenceChangeListener(this);

        mGlowTimes = (ListPreference) findPreference("glow_times");
        mGlowTimes.setOnPreferenceChangeListener(this);
        // mGlowTimes.setValue(Settings.System.getInt(getActivity()
        // .getContentResolver(), Settings.System.NAVIGATION_BAR_HOME_LONGPRESS,
        // 0) + "");

        mNavBarEnabledButtons = findPreference(PREF_EANBLED_BUTTONS);

        float defaultAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                0.6f);
        mButtonAlpha = (SeekBarPreference) findPreference("button_transparency");
        mButtonAlpha.setInitValue((int) (defaultAlpha * 100));
        mButtonAlpha.setOnPreferenceChangeListener(this);

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

        mLayout = findPreference("buttons");

        if (mTablet) {
            Log.e("NavBar", "is tablet");
            prefs.removePreference(mLayout);
            prefs.removePreference(mNavBarEnabledButtons);
            prefs.removePreference(mHomeLongpress);
            prefs.removePreference(mNavBarMenuDisplay);
        }
        refreshSettings();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.nav_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.reset:
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_TINT, Integer.MIN_VALUE);
                Settings.System.putFloat(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BUTTON_ALPHA,
                        0.6f);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_BUTTONS_SHOW, mContext.getResources().getBoolean(
                                com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0);
                mButtonAlpha.setValue(60);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

        if (preference == mNavBarEnabledButtons) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

            ArrayList<String> enabledToggles = getButtonsStringArray(this.getActivity()
                    .getApplicationContext());

            boolean checkedToggles[] = new boolean[buttons.length];

            for (int i = 0; i < checkedToggles.length; i++) {
                if (enabledToggles.contains(buttons[i])) {
                    checkedToggles[i] = true;
                }
            }

            builder.setTitle("Choose which buttons to use");
            builder.setCancelable(false);
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setMultiChoiceItems(buttons,
                    checkedToggles,
                    new OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            String toggleKey = (buttons[which]);

                            if (isChecked)
                                addButton(getActivity(), toggleKey);
                            else
                                removeButton(getActivity(), toggleKey);
                        }
                    });

            AlertDialog d = builder.create();

            d.show();

            return true;
        } else if (preference == mLayout) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            NavbarLayout fragment = new NavbarLayout();
            ft.addToBackStack("navbar_layout");
            ft.replace(this.getId(), fragment);
            ft.commit();
        } else if (preference == mEnableNavigationBar) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTONS_SHOW,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);

            new AlertDialog.Builder(getActivity())
                    .setTitle("Reboot required!")
                    .setMessage("Please reboot to enable/disable the navigation bar properly!")
                    .setNegativeButton("I'll reboot later", null)
                    .setCancelable(false)
                    .setPositiveButton("Reboot now!", new DialogInterface.OnClickListener() {
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

        } else if (preference == mHomeLongpress) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HOME_LONGPRESS,
                    Integer.parseInt((String) newValue));
            int nV = Integer.valueOf(String.valueOf(newValue));
            if (nV == 3) {
                mCustomAppString = Settings.System.NAVIGATION_BAR_HOME_LONGPRESS_CUSTOMAPP;
                mPicker.pickShortcut();
            } else {
                preference.setSummary(getProperSummary(nV));
            }
            return true;
        } if (preference == mNavBarButtonQty) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_BUTTONS_QTY, val);
            refreshSettings();
            return true;

        } else if (preference == mGlowTimes) {
            // format is (on|off) both in MS
            int breakIndex = ((String) newValue).indexOf("|");
            String value = (String) newValue;

            int offTime = Integer.parseInt(value.substring(breakIndex + 1));
            int onTime = Integer.parseInt(value.substring(0, breakIndex));

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[0],
                    offTime);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_GLOW_DURATION[1],
                    onTime);
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
        } else if ((preference.getKey().startsWith("navbar_action")) || (preference.getKey().startsWith("navbar_longpress"))) {
        	boolean longpress = preference.getKey().startsWith("navbar_longpress_");
            int index = Integer.parseInt(preference.getKey().substring(
                    preference.getKey().lastIndexOf("_") + 1));

            if (newValue.equals("**app**")) {
                mCurrentCustomActivityPreference = preference;
                if (longpress)
                	mCurrentCustomActivityString = Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[index];
                else
                	mCurrentCustomActivityString = Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index];
                mPicker.pickShortcut();
            } else {
            	if (longpress) {
            		Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[index], (String) newValue);
            	} else {
            		Settings.System.putString(getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[index], (String) newValue);
            		Settings.System.putString(getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[index], "");
            		Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_LANDSCAPE_APP_ICONS[index], "");
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

            } else if ((requestCode == REQUEST_PICK_CUSTOM_ICON) || (requestCode == REQUEST_PICK_LANDSCAPE_ICON)) {

            	boolean landscape = (requestCode == REQUEST_PICK_LANDSCAPE_ICON);
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput((landscape ? "navbar_land_icon_" : "navbar_icon_") + currentIconIndex + ".png",
                            Context.MODE_WORLD_WRITEABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri selectedImageUri = getTempFileUri();
                Log.e(TAG, "Selected image path: " + selectedImageUri.getPath());
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                
                if (requestCode == REQUEST_PICK_CUSTOM_ICON) 
                	Settings.System.putString(getContentResolver(),
                        Settings.System.NAVIGATION_CUSTOM_APP_ICONS[currentIconIndex],
                        getExternalIconUri(false).toString());
                else
                	Settings.System.putString(getContentResolver(),
                            Settings.System.NAVIGATION_LANDSCAPE_APP_ICONS[currentIconIndex],
                            getExternalIconUri(true).toString());
                Toast.makeText(getActivity(), currentIconIndex + "'s icon set successfully!",
                        Toast.LENGTH_LONG).show();
                refreshSettings();

            }
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
            NavBarItemPreference pAction = new NavBarItemPreference(getActivity());
            NavBarItemPreference pLongpress = new NavBarItemPreference(getActivity());
            String dialogTitle = String.format(
                    getResources().getString(R.string.navbar_action_title), i + 1);
            pAction.setDialogTitle(dialogTitle);
            pAction.setEntries(R.array.navbar_button_entries);
            pAction.setEntryValues(R.array.navbar_button_values);
            String title = String.format(getResources().getString(R.string.navbar_action_title), i + 1);
            pAction.setTitle(title);
            pAction.setKey("navbar_action_" + i);
            pAction.setSummary(getProperSummary(i,false));
            pAction.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(pAction);
            
            dialogTitle = String.format(
                    getResources().getString(R.string.navbar_longpress_title), i + 1);
            pLongpress.setDialogTitle(dialogTitle);
            pLongpress.setEntries(R.array.navbar_button_entries);
            pLongpress.setEntryValues(R.array.navbar_button_values);
            title = String.format(getResources().getString(R.string.navbar_longpress_title), i + 1);
            pLongpress.setTitle(title);
            pLongpress.setKey("navbar_longpress_" + i);
            pLongpress.setSummary(getProperSummary(i,true));
            pLongpress.setOnPreferenceChangeListener(this);
            targetGroup.addPreference(pLongpress);
            
            final int index = i;
            pAction.setImageListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(
                            android.os.Environment.MEDIA_MOUNTED);
                    if (!isSDPresent) {
                        Toast.makeText(v.getContext(), "Insert SD card to use this feature",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentIconIndex = index;

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
                    // intent.putExtra("return-data", false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getExternalIconUri(false));
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

                    Log.i(TAG, "started for result, should output to: " + getExternalIconUri(false));

                    startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON);
                }
            });
            
            pLongpress.setImageListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(
                            android.os.Environment.MEDIA_MOUNTED);
                    if (!isSDPresent) {
                        Toast.makeText(v.getContext(), "Insert SD card to use this feature",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentIconIndex = index;

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
                    // intent.putExtra("return-data", false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getExternalIconUri(true));
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

                    Log.i(TAG, "started for result, should output to: " + getExternalIconUri(true));

                    startActivityForResult(intent, REQUEST_PICK_LANDSCAPE_ICON);
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
                    pAction.setIcon(resize(new BitmapDrawable(getResources(), icon.getAbsolutePath())));

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
                pAction.setIcon(resize(getNavbarIconImage(i,false)));
            }
            
            customIconUri = Settings.System.getString(getContentResolver(),
                    Settings.System.NAVIGATION_LANDSCAPE_APP_ICONS[i]);
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
                    pAction.setIcon(resize(new BitmapDrawable(getResources(), icon.getAbsolutePath())));

            } else if (customIconUri != null && !customIconUri.equals("")) {
                // here they chose another app icon
                try {
                    pLongpress.setIcon(resize(pm.getActivityIcon(Intent.parseUri(customIconUri, 0))));
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                // ok use default icons here
                pLongpress.setIcon(resize(getNavbarIconImage(i,true)));
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
            return getResources().getDrawable(R.drawable.ic_null);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**")) {
            	if (!landscape)
            		return getResources().getDrawable(R.drawable.ic_sysbar_home);
            	else
            		return getResources().getDrawable(R.drawable.ic_sysbar_home_land);
            } else if (uri.equals("**back**")) {
            	if (!landscape)
            		return getResources().getDrawable(R.drawable.ic_sysbar_back);
            	else
            		return getResources().getDrawable(R.drawable.ic_sysbar_back_land);
            } else if (uri.equals("**recents**")) {
            	if (!landscape)
            		return getResources().getDrawable(R.drawable.ic_sysbar_recent);
            	else
            		return getResources().getDrawable(R.drawable.ic_sysbar_recent_land);
            } else if (uri.equals("**search**")) {
            	if (!landscape)
            		return getResources().getDrawable(R.drawable.ic_sysbar_search);
            	else
            		return getResources().getDrawable(R.drawable.ic_sysbar_search_land);
            } else if (uri.equals("**menu**")) {
            	if (!landscape)
            		return getResources().getDrawable(R.drawable.ic_sysbar_menu_big);
            	else
            		return getResources().getDrawable(R.drawable.ic_sysbar_menu_land_big);
            } else if (uri.equals("**kill**")) {
            	if (!landscape)
            		return getResources().getDrawable(R.drawable.ic_sysbar_killtask);
            	else
            		return getResources().getDrawable(R.drawable.ic_sysbar_killtask_land);
            } else if (uri.equals("**power**")) {
            	if (!landscape)
            		return getResources().getDrawable(R.drawable.ic_sysbar_power);
            	else
            		return getResources().getDrawable(R.drawable.ic_sysbar_power_land);
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

        return getResources().getDrawable(R.drawable.ic_null);
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
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        if (Settings.System.putString(getActivity().getContentResolver(),
                mCurrentCustomActivityString, uri)) {

            String i = mCurrentCustomActivityString.substring(mCurrentCustomActivityString
                    .lastIndexOf("_") + 1);
            boolean longpress = mCurrentCustomActivityString.startsWith("navbar_longpress_");
            Log.i(TAG, "shortcut picked, index: " + i);
            Log.i(TAG, uri);
            if (!longpress) {
            	Settings.System.putString(getContentResolver(),
                        Settings.System.NAVIGATION_LANDSCAPE_APP_ICONS[Integer.parseInt(i)], "");
            
            	Settings.System.putString(getContentResolver(),
            			Settings.System.NAVIGATION_CUSTOM_APP_ICONS[Integer.parseInt(i)], "");
            }
            mCurrentCustomActivityPreference.setSummary(friendlyName);        
        }
    }
    
    private Uri getExternalIconUri(boolean landscape) {
        //File dir = mContext.getFilesDir();
        String dir = "/sdcard/data/com.aokp.romcontrol/icons/";
        File icon = new File(dir, (landscape ? "navbar_land_" : "navbar_") + currentIconIndex + ".png");
        icon.getParentFile().mkdirs();
        Log.d(TAG,"GetIcon:"+ icon.getAbsolutePath());
        return Uri.fromFile(icon);
    }
    
    private Uri getTempFileUri() {
        //File dir = mContext.getFilesDir();
        String dir = "/sdcard/data/com.aokp.romcontrol/temp/";
        File wallpaper = new File(dir, "temp");
        wallpaper.getParentFile().mkdirs();
        Log.d(TAG,"GetTemp:"+ wallpaper.getAbsolutePath());
        return Uri.fromFile(wallpaper);
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
}
