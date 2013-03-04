
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.AwesomeConstants.AwesomeConstant;
import com.android.internal.util.aokp.NavBarHelpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.objects.EasyPair;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.widgets.CustomTogglePref;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class StatusBarToggles extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener, ShortcutPickerHelper.OnPickListener {

    private static final String TAG = "TogglesLayout";

    private static final String PREF_ENABLE_TOGGLES = "enabled_toggles";
    private static final String PREF_TOGGLES_PER_ROW = "toggles_per_row";
    private static final String PREF_TOGGLES_STYLE = "toggles_style";
    private static final String PREF_TOGGLE_FAV_CONTACT = "toggle_fav_contact";
    private static final String PREF_ENABLE_FASTTOGGLE = "enable_fast_toggle";
    private static final String PREF_CHOOSE_FASTTOGGLE_SIDE = "choose_fast_toggle_side";
    private static final String PREF_SCREENSHOT_DELAY = "screenshot_delay";
    private static final String PREF_SET_BOOT_ACTION = "set_boot_action";
    private static final String PREF_MATCH_ICON_ACTION = "match_icon_action";
    private static final String PREF_ADV_TOGGLE_ACTIONS = "advanced_toggle_actions";
    private static final String PREF_COLLAPSE_BAR = "collapse_bar";
    private static final String PREF_DCLICK_ACTION = "dclick_action";
    private static final String PREF_CUSTOM_TOGGLE = "custom_toggle_pref";

    private final int PICK_CONTACT = 1;

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;

    private Resources mResources;
    private PackageManager mPackMan;

    Preference mEnabledToggles;
    Preference mLayout;
    ListPreference mTogglesPerRow;
    ListPreference mTogglesStyle;
    Preference mFavContact;
    CheckBoxPreference mFastToggle;
    CheckBoxPreference mAdvancedStates;
    CheckBoxPreference mBootState;
    CheckBoxPreference mMatchAction;
    ListPreference mChooseFastToggleSide;
    ListPreference mScreenshotDelay;
    ListPreference mCollapseShade;
    ListPreference mOnDoubleClick;
    ListPreference mNumberOfActions;
    CustomTogglePref mCustomToggles;

    BroadcastReceiver mReceiver;
    ArrayList<String> mToggles;

    // Custom Toggle Stuff
    private int mNumberofToggles = 0;
    ArrayList<ToggleButton> mButtons = new ArrayList<ToggleButton>();
    ArrayList<ImageButton> mButtonViews = new ArrayList<ImageButton>();
    String[] mActions;
    String[] mActionCodes;
    private int mPendingToggle = -1;
    private ImageButton mAddButton, mResetButton, mSaveButton;
    private ShortcutPickerHelper mPicker;

    static Bundle sToggles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("toggle_bundle")) {
                    onTogglesUpdate(intent.getBundleExtra("toggle_bundle"));
                }
            }
        };
        mContext.registerReceiver(mReceiver,
                new IntentFilter("com.android.systemui.statusbar.toggles.ACTION_BROADCAST_TOGGLES"));
        requestAvailableToggles();
        setTitle(R.string.title_statusbar_toggles);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_toggles);

        mPicker = new ShortcutPickerHelper(this, this);
        mPackMan = getPackageManager();
        mResources = mContext.getResources();

        // Get Toggle Actions
        mActionCodes = NavBarHelpers.getNavBarActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext, mActionCodes[i]);
        }

        boolean isAdvanced = Settings.System.getBoolean(getContentResolver(),
                Settings.System.CUSTOM_TOGGLE_ADVANCED, false);

        mEnabledToggles = findPreference(PREF_ENABLE_TOGGLES);

        mTogglesPerRow = (ListPreference) findPreference(PREF_TOGGLES_PER_ROW);
        mTogglesPerRow.setOnPreferenceChangeListener(this);
        mTogglesPerRow.setValue(Settings.System.getInt(mContentRes,
                Settings.System.QUICK_TOGGLES_PER_ROW, 3) + "");

        mTogglesStyle = (ListPreference) findPreference(PREF_TOGGLES_STYLE);
        mTogglesStyle.setOnPreferenceChangeListener(this);
        mTogglesStyle.setValue(String.valueOf(Settings.System.getInt(mContentRes,
                Settings.System.TOGGLES_STYLE, 0)));

        mLayout = findPreference("toggles");

        mFavContact = findPreference(PREF_TOGGLE_FAV_CONTACT);

        mFastToggle = (CheckBoxPreference) findPreference(PREF_ENABLE_FASTTOGGLE);
        mFastToggle.setOnPreferenceChangeListener(this);

        mChooseFastToggleSide = (ListPreference) findPreference(PREF_CHOOSE_FASTTOGGLE_SIDE);
        mChooseFastToggleSide.setOnPreferenceChangeListener(this);
        mChooseFastToggleSide.setValue(Settings.System.getInt(mContentRes,
                Settings.System.CHOOSE_FASTTOGGLE_SIDE, 1) + "");

        mScreenshotDelay = (ListPreference) findPreference(PREF_SCREENSHOT_DELAY);
        mScreenshotDelay.setOnPreferenceChangeListener(this);
        mScreenshotDelay.setValue(String.valueOf(Settings.System.getInt(mContentRes,
                Settings.System.SCREENSHOT_TOGGLE_DELAY, 5000)));

        mAdvancedStates = (CheckBoxPreference) findPreference(PREF_ADV_TOGGLE_ACTIONS);
        mAdvancedStates.setOnPreferenceChangeListener(this);

        mBootState = (CheckBoxPreference) findPreference(PREF_SET_BOOT_ACTION);
        mBootState.setOnPreferenceChangeListener(this);

        mMatchAction = (CheckBoxPreference) findPreference(PREF_MATCH_ICON_ACTION);
        mMatchAction.setOnPreferenceChangeListener(this);

        mCollapseShade = (ListPreference) findPreference(PREF_COLLAPSE_BAR);
        mCollapseShade.setOnPreferenceChangeListener(this);
        mCollapseShade.setValue(Settings.System.getInt(mContentRes,
                Settings.System.COLLAPSE_SHADE, 10) + "");

        mOnDoubleClick = (ListPreference) findPreference(PREF_DCLICK_ACTION);
        mOnDoubleClick.setOnPreferenceChangeListener(this);
        mOnDoubleClick.setValue(Settings.System.getInt(mContentRes,
                Settings.System.DCLICK_TOGGLE_REVERT, 0) + "");

        mCustomToggles = (CustomTogglePref) findPreference(PREF_CUSTOM_TOGGLE);
        mCustomToggles.setParent(this);

        if (isSW600DPScreen(mContext) || isTablet(mContext)) {
            getPreferenceScreen().removePreference(mFastToggle);
            getPreferenceScreen().removePreference(mChooseFastToggleSide);
        }

        if (Integer.parseInt(mTogglesStyle.getValue()) > 1) {
            mFastToggle.setEnabled(false);
        }

        if (!isAdvanced) {
            mMatchAction.setEnabled(false);
        }
        refreshSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                Settings.System.putInt(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_QTY, 3);

                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_PRESS_TOGGLE[0], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_PRESS_TOGGLE[1], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_PRESS_TOGGLE[2], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_PRESS_TOGGLE[3], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_PRESS_TOGGLE[4], "**null**");

                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_LONGPRESS_TOGGLE[0], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_LONGPRESS_TOGGLE[1], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_LONGPRESS_TOGGLE[2], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_LONGPRESS_TOGGLE[3], "**null**");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_LONGPRESS_TOGGLE[4], "**null**");

                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_ICONS[0], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_ICONS[1], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_ICONS[2], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_ICONS[3], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_ICONS[4], "");

                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_TEXT[0], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_TEXT[1], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_TEXT[2], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_TEXT[3], "");
                Settings.System.putString(mContentRes,
                        Settings.System.CUSTOM_TOGGLE_TEXT[4], "");
                refreshSettings();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    static ArrayList<EasyPair<String, String>> buildToggleMap(Bundle toggleInfo) {
        ArrayList<String> _toggleIdents = toggleInfo.getStringArrayList("toggles");
        ArrayList<EasyPair<String, String>> _toggles = new ArrayList<EasyPair<String, String>>();
        for (String _ident : _toggleIdents) {
            _toggles.add(new EasyPair<String, String>(_ident, toggleInfo.getString(_ident)));
        }
        return _toggles;
    }

    private void onTogglesUpdate(Bundle toggleInfo) {
        mToggles = toggleInfo.getStringArrayList("toggles");
        sToggles = toggleInfo;
        if (mToggles.contains("FAVCONTACT")) {
            if (mFavContact != null) {
                mFavContact.setEnabled(true);
            }
        } else {
            if (mFavContact != null) {
                getPreferenceScreen().removePreference(mFavContact);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requestAvailableToggles();
        refreshSettings();
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    private void requestAvailableToggles() {
        Intent request = new Intent("com.android.systemui.statusbar.toggles.ACTION_REQUEST_TOGGLES");
        mContext.sendBroadcast(request);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTogglesPerRow) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.QUICK_TOGGLES_PER_ROW, val);
        } else if (preference == mTogglesStyle) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.TOGGLES_STYLE, val);
            mTogglesStyle.setValue((String) newValue);
            mFastToggle.setEnabled(val > 1 ? false : true);
            Helpers.restartSystemUI();
        } else if (preference == mScreenshotDelay) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.SCREENSHOT_TOGGLE_DELAY, val);
            mScreenshotDelay.setValue((String) newValue);
        } else if (preference == mFastToggle) {
            boolean val = (Boolean) newValue;
            Settings.System.putBoolean(mContentRes,
                    Settings.System.FAST_TOGGLE, val);
            mContentRes.notifyChange(Settings.System.getUriFor(Settings.System.FAST_TOGGLE), null);
            return true;
        } else if (preference == mChooseFastToggleSide) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.CHOOSE_FASTTOGGLE_SIDE, val);
            mContentRes.notifyChange(
                    Settings.System.getUriFor(Settings.System.CHOOSE_FASTTOGGLE_SIDE), null);
            mChooseFastToggleSide.setValue(Settings.System.getInt(mContentRes,
                    Settings.System.CHOOSE_FASTTOGGLE_SIDE, 1) + "");
        } else if (preference == mAdvancedStates) {
            boolean val = (Boolean) newValue;
            Settings.System.putBoolean(mContentRes,
                    Settings.System.CUSTOM_TOGGLE_ADVANCED, val);
            mContentRes.notifyChange(
                    Settings.System.getUriFor(Settings.System.CUSTOM_TOGGLE_ADVANCED), null);
            AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
            ad.setTitle(getResources().getString(R.string.custom_toggle_action));
            ad.setMessage(getResources().getString(R.string.custom_toggle_desc));
            ad.setPositiveButton(
                    getResources().getString(R.string.custom_toggle_okay),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            ad.show();
            mMatchAction.setEnabled(val == false ? false : true);
            refreshSettings();
            return true;
        } else if (preference == mBootState) {
            boolean val = (Boolean) newValue;
            Settings.System.putBoolean(mContentRes,
                    Settings.System.CUSTOM_TOGGLE_REVERT, val);
            mContentRes.notifyChange(
                    Settings.System.getUriFor(Settings.System.CUSTOM_TOGGLE_REVERT), null);
            return true;
        } else if (preference == mMatchAction) {
            boolean val = (Boolean) newValue;
            Settings.System.putBoolean(mContentRes,
                    Settings.System.MATCH_ACTION_ICON, val);
            mContentRes.notifyChange(Settings.System.getUriFor(Settings.System.MATCH_ACTION_ICON),
                    null);
            return true;
        } else if (preference == mCollapseShade) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.COLLAPSE_SHADE, val);
            return true;
        } else if (preference == mOnDoubleClick) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.DCLICK_TOGGLE_REVERT, val);
            return true;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnabledToggles) {
            if (mToggles == null || mToggles.isEmpty()) {
                return false;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final ArrayList<String> userToggles = getEnabledToggles(mContext);
            final ArrayList<String> availableToggles = new ArrayList<String>();
            for (String t : mToggles) {
                availableToggles.add(t);
            }

            // final String[] finalArray = getResources().getStringArray(
            // R.array.available_toggles_entries);
            final String[] toggleValues = new String[availableToggles.size()];
            for (int i = 0; i < availableToggles.size(); i++) {
                toggleValues[i] = StatusBarToggles.lookupToggle(mContext, availableToggles.get(i));
            }

            final boolean checkedToggles[] = new boolean[availableToggles.size()];

            boolean anyChecked = false;
            for (int i = 0; i < checkedToggles.length; i++) {
                String selectedToggle = availableToggles.get(i);
                if (userToggles.contains(selectedToggle)) {
                    Log.d(TAG, "found toggle: " + selectedToggle);
                    checkedToggles[i] = true;
                    anyChecked = true;
                }
            }
            if (!anyChecked) {
                // no toggles are checked, wipe the setting to be sure
                Settings.System.putString(mContentRes, Settings.System.QUICK_TOGGLES, "");
            }

            builder.setTitle(R.string.toggles_display_dialog);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.toggles_display_close,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setMultiChoiceItems(toggleValues, checkedToggles,
                    new OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            String toggleKey = availableToggles.get(which);

                            if (isChecked)
                                StatusBarToggles.addToggle(getActivity(), toggleKey);
                            else
                                StatusBarToggles.removeToggle(getActivity(), toggleKey);

                            if ("FAVCONTACT".equals(toggleKey)) {
                                mFavContact.setEnabled(isChecked);
                            }
                        }
                    });

            AlertDialog d = builder.create();

            d.show();

            return true;
        } else if (preference == mLayout) {
            ArrangeTogglesFragment fragment = ArrangeTogglesFragment.newInstance(sToggles);
            fragment.show(getFragmentManager(), "arrange");
        }
        else if (preference == mFavContact) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void refreshSettings() {
        refreshButtons();
    }

    public void setupToggleViews(LinearLayout container) {
        mResetButton = (ImageButton) container.findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(mCommandButtons);
        mAddButton = (ImageButton) container.findViewById(R.id.add_button);
        mAddButton.setOnClickListener(mCommandButtons);
        mSaveButton = (ImageButton) container.findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(mCommandButtons);
        mButtonViews.clear();
        LinearLayout llbuttons = (LinearLayout) container.findViewById(R.id.toggle_container);
        for (int i = 0; i < llbuttons.getChildCount(); i++) {
            ImageButton ib = (ImageButton) llbuttons.getChildAt(i);
            mButtonViews.add(ib);
        }
        if (mButtons.size() == 0) {
            loadButtons();
        }
        refreshButtons();
    }

    private View.OnClickListener mToggleClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mPendingToggle = mButtonViews.indexOf(v);
            if (mPendingToggle > -1 && mPendingToggle < mNumberofToggles) {
                createDialog(mButtons.get(mPendingToggle));
            }
        }
    };

    private void loadButtons() {
        mNumberofToggles = Settings.System.getInt(mContentRes,
                Settings.System.CUSTOM_TOGGLE_QTY, 3);
        mButtons.clear();
        for (int i = 0; i < mNumberofToggles; i++) {
            String click = Settings.System.getString(mContentRes,
                    Settings.System.CUSTOM_PRESS_TOGGLE[i]);
            String longclick = Settings.System.getString(mContentRes,
                    Settings.System.CUSTOM_LONGPRESS_TOGGLE[i]);
            String iconuri = Settings.System.getString(mContentRes,
                    Settings.System.CUSTOM_TOGGLE_ICONS[i]);
            mButtons.add(new ToggleButton(click, longclick, iconuri));
        }
    }

    public void refreshButtons() {
        if (mNumberofToggles == 0) {
            return;
        }
        for (int i = 0; i < mNumberofToggles; i++) {
            ImageButton ib = mButtonViews.get(i);
            Drawable d = mButtons.get(i).getIcon();
            ib.setImageDrawable(d);
            ib.setOnClickListener(mToggleClickListener);
            ib.setVisibility(View.VISIBLE);
        }
        for (int i = mNumberofToggles; i < mButtonViews.size(); i++) {
            ImageButton ib = mButtonViews.get(i);
            ib.setVisibility(View.GONE);
        }
    }

    private void saveButtons() {
        Settings.System.putInt(mContentRes, Settings.System.CUSTOM_TOGGLE_QTY,
                mNumberofToggles);
        for (int i = 0; i < mNumberofToggles; i++) {
            ToggleButton button = mButtons.get(i);
            Settings.System.putString(mContentRes, Settings.System.CUSTOM_PRESS_TOGGLE[i],
                    button.getClickAction());
            Settings.System.putString(mContentRes, Settings.System.CUSTOM_LONGPRESS_TOGGLE[i],
                    button.getLongAction());
            Settings.System.putString(mContentRes, Settings.System.CUSTOM_TOGGLE_ICONS[i],
                    button.getIconURI());
            Settings.System.putString(mContentRes, Settings.System.CUSTOM_TOGGLE_TEXT[i],
                    button.getClickName());
        }
    }

    private void createDialog(final ToggleButton button) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onDialogClick(button, item);
                dialog.dismiss();
            }
        };

        boolean isAdvanced = Settings.System.getBoolean(getContentResolver(),
                Settings.System.CUSTOM_TOGGLE_ADVANCED, false);

        String action = mResources.getString(R.string.navbar_actiontitle_menu);
        if (!isAdvanced) {
            action = mResources.getString(R.string.navbar_longpress_menu);
        }
        String longpress = mResources.getString(R.string.navbar_longpress_menu);
        longpress = String.format(longpress, button.getLongName());
        action = String.format(action, button.getClickName());
        String[] items = {
                action, longpress,
                mResources.getString(R.string.navbar_icon_menu),
                mResources.getString(R.string.navbar_delete_menu)
        };
        String[] basicitems = {
                action,
                mResources.getString(R.string.navbar_icon_menu),
                mResources.getString(R.string.navbar_delete_menu)
        };
        if (isAdvanced) {
            final AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle(mResources.getString(R.string.navbar_title_menu))
                    .setItems(items, l)
                    .create();
            dialog.show();
        } else {
            final AlertDialog dialog = new AlertDialog.Builder(mContext)
                    .setTitle(mResources.getString(R.string.navbar_title_menu))
                    .setItems(basicitems, l)
                    .create();
            dialog.show();
        }
    }

    private void createActionDialog(final ToggleButton button) {
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

    private void onDialogClick(ToggleButton button, int command) {
        boolean isAdvanced = Settings.System.getBoolean(getContentResolver(),
                Settings.System.CUSTOM_TOGGLE_ADVANCED, false);
        if (isAdvanced) {
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
                    startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON);
                    break;
                case 3: // Delete Button
                    mButtons.remove(mPendingToggle);
                    mNumberofToggles--;
                    break;
            }
        } else {
            switch (command) {
                case 0: // Set Click Action
                    button.setPickLongPress(false);
                    createActionDialog(button);
                    break;
                case 1: // set Custom Icon
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
                    break;
                case 2: // Delete Button
                    mButtons.remove(mPendingToggle);
                    mNumberofToggles--;
                    break;
            }
        }
        refreshButtons();
    }

    private void onActionDialogClick(ToggleButton button, int command) {
        if (command == mActions.length - 1) {
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
                    if (mNumberofToggles < 5) { // Maximum Toggles is 5
                        mButtons.add(new ToggleButton("**null**", "**null**", ""));
                        mNumberofToggles++;
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
        ToggleButton button = mButtons.get(mPendingToggle);
        boolean longpress = button.getPickLongPress();

        if (!longpress) {
            button.setClickAction(uri);
            if (bmp == null) {
                button.setIconURI("");
            } else {
                String iconName = getIconFileName(mPendingToggle);
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
        Log.i(TAG, "RequestCode:" + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_CONTACT) {
                Uri contactData = data.getData();
                String[] projection = new String[] {
                        ContactsContract.Contacts.LOOKUP_KEY
                };
                String selection = ContactsContract.Contacts.DISPLAY_NAME + " IS NOT NULL";
                CursorLoader cursorLoader = new CursorLoader(getActivity().getBaseContext(),
                        contactData, projection, selection, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            String lookup_key = cursor.getString(cursor
                                    .getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                            Settings.System.putString(mContentRes,
                                    Settings.System.QUICK_TOGGLE_FAV_CONTACT, lookup_key);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }

            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if (requestCode == REQUEST_PICK_CUSTOM_ICON) {
                String iconName = getIconFileName(mPendingToggle);
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
                mButtons.get(mPendingToggle).setIconURI(Uri.fromFile(
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
        int size = 64;
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

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mPendingToggle + ".png"));

    }

    private String getIconFileName(int index) {
        return "navbar_icon_" + index + ".png";
    }

    static synchronized void addToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getEnabledToggles(context);
        if (enabledToggles.contains(key)) {
            enabledToggles.remove(key);
        }
        enabledToggles.add(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    static synchronized ArrayList<String> getEnabledToggles(Context context) {
        try {
            ArrayList<String> userEnabledToggles = new ArrayList<String>();
            String userToggles = Settings.System.getString(context.getContentResolver(),
                    Settings.System.QUICK_TOGGLES);

            String[] splitter = userToggles.split("\\|");
            for (String toggle : splitter) {
                userEnabledToggles.add(toggle);
            }
            return userEnabledToggles;
        } catch (Exception e) {
            if (sToggles != null && sToggles.containsKey("default_toggles")) {
                return sToggles.getStringArrayList("default_toggles");
            }
        }
        return new ArrayList<String>();
    }

    static synchronized void setTogglesFromStringArray(Context c, List<String> enabledToggles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < enabledToggles.size(); i++) {
            final String _toggle = enabledToggles.get(i);
            if (_toggle.isEmpty()) {
                continue;
            }
            b.append(_toggle);
            b.append("|");
        }
        if (String.valueOf(b.charAt(b.length() - 1)).equals("!")) {
            b.deleteCharAt(b.length() - 1);
        }
        Log.d(TAG, "saving toggles:" + b.toString());
        Settings.System.putString(c.getContentResolver(), Settings.System.QUICK_TOGGLES,
                b.toString());
    }

    static synchronized void removeToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getEnabledToggles(context);
        enabledToggles.remove(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    static String lookupToggle(Context c, String ident) {
        if (sToggles != null) {
            return sToggles.getString(ident.toUpperCase());
        }
        return ident;
    }

    public class ToggleButton {
        String mClickAction;
        String mLongPressAction;
        String mIconURI;
        String mClickFriendlyName;
        String mLongPressFriendlyName;
        Drawable mIcon;
        boolean mPickingLongPress;

        public ToggleButton(String clickaction, String longpress, String iconuri) {
            mClickAction = clickaction;
            mLongPressAction = longpress;
            mIconURI = iconuri;
            mClickFriendlyName = getProperSummary(mClickAction);
            mLongPressFriendlyName = getProperSummary(mLongPressAction);
            mIcon = setIcon(mIconURI, mClickAction);
        }

        public void setClickAction(String click) {
            mClickAction = click;
            mClickFriendlyName = getProperSummary(mClickAction);
            // ClickAction was reset - so we should default to stock Icon for
            // now
            mIconURI = "";
            mIcon = setIcon(mIconURI, mClickAction);
        }

        public void setLongPress(String action) {
            mLongPressAction = action;
            mLongPressFriendlyName = getProperSummary(mLongPressAction);
        }

        public void setPickLongPress(boolean pick) {
            mPickingLongPress = pick;
        }

        public boolean getPickLongPress() {
            return mPickingLongPress;
        }

        public void setIconURI(String uri) {
            mIconURI = uri;
            mIcon = setIcon(mIconURI, mClickAction);
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
