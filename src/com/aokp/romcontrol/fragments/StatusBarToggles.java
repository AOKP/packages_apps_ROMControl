
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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.objects.EasyPair;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.widgets.NavBarItemPreference;

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
    private static final String ACTION_QTY = "action_qty";
    private static final String RESET_ACTION = "reset_action";

    private final int PICK_CONTACT = 1;

    Preference mEnabledToggles;
    Preference mLayout;
    ListPreference mTogglesPerRow;
    ListPreference mTogglesStyle;
    Preference mFavContact;
    CheckBoxPreference mFastToggle;
    ListPreference mChooseFastToggleSide;
    ListPreference mNumberOfActions;
    CheckBoxPreference mResetOnDoubleClick;
    BroadcastReceiver mReceiver;
    ArrayList<String> mToggles;

    private int mPendingIconIndex = -1;
    private NavBarCustomAction mCustomAction = null;

    private static class CustomAction {
        String activitySettingName;
        Preference preference;
        int iconIndex = -1;
    }

    Preference mPendingPreference;
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

        mEnabledToggles = findPreference(PREF_ENABLE_TOGGLES);

        mTogglesPerRow = (ListPreference) findPreference(PREF_TOGGLES_PER_ROW);
        mTogglesPerRow.setOnPreferenceChangeListener(this);
        mTogglesPerRow.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QUICK_TOGGLES_PER_ROW, 3) + "");

        mTogglesStyle = (ListPreference) findPreference(PREF_TOGGLES_STYLE);
        mTogglesStyle.setOnPreferenceChangeListener(this);
        mTogglesStyle.setValue(String.valueOf(Settings.System.getInt(getActivity()
                .getContentResolver(),
                Settings.System.TOGGLES_STYLE, 0)));

        mLayout = findPreference("toggles");

        mFavContact = findPreference(PREF_TOGGLE_FAV_CONTACT);

        mFastToggle = (CheckBoxPreference) findPreference(PREF_ENABLE_FASTTOGGLE);
        mFastToggle.setOnPreferenceChangeListener(this);

        mChooseFastToggleSide = (ListPreference) findPreference(PREF_CHOOSE_FASTTOGGLE_SIDE);
        mChooseFastToggleSide.setOnPreferenceChangeListener(this);
        mChooseFastToggleSide.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CHOOSE_FASTTOGGLE_SIDE, 1) + "");

        mNumberOfActions = (ListPreference) findPreference(PREF_ACTION_QTY);
        mNumberOfActions.setOnPreferenceChangeListener(this);
        mNumberOfActions.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CUSTOM_TOGGLE_QTY, 3) + "");

        mResetOnDoubleClick = (CheckBoxPreference) findPreference(RESET_ACTION);
        mResetOnDoubleClick.setOnPreferenceChangeListener(this);

        if (isTablet(mContext) || isPhablet(mContext)) {
            getPreferenceScreen().removePreference(mFastToggle);
            getPreferenceScreen().removePreference(mChooseFastToggleSide);
        }
        refreshSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_TOGGLE_QTY, 3);

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_PRESS_TOGGLE[0], "**null**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_PRESS_TOGGLE[1], "**null**");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_PRESS_TOGGLE[2], "**null**");

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_LONGPRESS_TOGGLE, "**null**");

                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_TOGGLE_ICONS[0], "");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_TOGGLE_ICONS[1], "");
                Settings.System.putString(getActivity().getContentResolver(),
                        Settings.System.CUSTOM_TOGGLE_ICONS[2], "");
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
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QUICK_TOGGLES_PER_ROW, val);
        } else if (preference == mTogglesStyle) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.TOGGLES_STYLE, val);
            mTogglesStyle.setValue((String) newValue);
            Helpers.restartSystemUI();
        } else if (preference == mFastToggle) {
            boolean val = (Boolean) newValue;
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.FAST_TOGGLE, val);
            getActivity().getBaseContext().getContentResolver()
                    .notifyChange(Settings.System.getUriFor(Settings.System.FAST_TOGGLE), null);
            return true;
        } else if (preference == mChooseFastToggleSide) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CHOOSE_FASTTOGGLE_SIDE, val);
            getActivity()
                    .getBaseContext()
                    .getContentResolver()
                    .notifyChange(
                            Settings.System.getUriFor(Settings.System.CHOOSE_FASTTOGGLE_SIDE), null);
            mChooseFastToggleSide.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(),
                    Settings.System.CHOOSE_FASTTOGGLE_SIDE, 1)
                    + "");
        }
        return true;
        } else if (preference == mNumberOfActions) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CUSTOM_TOGGLE_QTY, val);
            refreshSettings();
            return true;
        } else if ((preference.getKey().startsWith("custom_action"))
                || (preference.getKey().startsWith("custom_longpress"))) {
            boolean longpress = preference.getKey().startsWith("custom_longpress_");
            int index = Integer.parseInt(preference.getKey().substring(
                    preference.getKey().lastIndexOf("_") + 1));

            if (newValue.equals("**app**")) {
                mCustomAction = new CustomAction();
                mCustomAction.preference = preference;
                if (longpress) {
                    mCustomAction.activitySettingName = Settings.System.CUSTOM_LONGPRESS_TOGGLE;
                    mCustomAction.iconIndex = -1;
                } else {
                    mCustomAction.activitySettingName = Settings.System.CUSTOM_PRESS_TOGGLE[index];
                    mCustomAction.iconIndex = index;
                }
                mPicker.pickShortcut();
            } else {
                if (longpress) {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.CUSTOM_LONGPRESS_TOGGLE,
                            (String) newValue);
                } else {
                    Settings.System.putString(getContentResolver(),
                            Settings.System.CUSTOM_PRESS_TOGGLE[index],
                            (String) newValue);
                    Settings.System.putString(getContentResolver(),
                            Settings.System.CUSTOM_TOGGLE_ICONS[index], "");
                }
            }
            refreshSettings();
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
                Settings.System.putString(getContentResolver(), Settings.System.QUICK_TOGGLES, "");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                            Settings.System.putString(getActivity().getContentResolver(),
                                    Settings.System.QUICK_TOGGLE_FAV_CONTACT, lookup_key);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                        Settings.System.CUSTOM_TOGGLE_ICONS[mPendingIconIndex], "");
                Settings.System.putString(
                        getContentResolver(),
                        Settings.System.CUSTOM_TOGGLE_ICONS[mPendingIconIndex],
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

        int customQuantity = Settings.System.getInt(getContentResolver(),
                Settings.System.CUSTOM_TOGGLE_QTY, 3);

        PreferenceGroup targetGroup = (PreferenceGroup) findPreference("custom_buttons");
        targetGroup.removeAll();

        PackageManager pm = mContext.getPackageManager();
        Resources res = mContext.getResources();

        for (int i = 0; i < customQuantity; i++) {
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
            title = String.format(getResources().getString(R.string.navbar_longpress_title));
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
                    Settings.System.CUSTOM_TOGGLE_ICONS[i]);
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
        if (d == null) {
            return getResources().getDrawable(R.drawable.ic_sysbar_null);
        } else {
            Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, px, px, false);
            return new BitmapDrawable(mContext.getResources(), bitmapOrig);
        }
    }

    private Drawable getNavbarIconImage(int index, boolean landscape) {
        String uri = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_PRESS_TOGGLE[index]);

        if (uri == null)
            return getResources().getDrawable(R.drawable.ic_sysbar_null);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_home);
            } else if (uri.equals("**back**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_back);
            } else if (uri.equals("**recents**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_recent);
            } else if (uri.equals("**recentsgb**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_recent_gb);
            } else if (uri.equals("**search**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_search);
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
            } else if (uri.equals("**lastapp**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_lastapp);
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
                    Settings.System.CUSTOM_LONGPRESS_TOGGLE);
        else
            uri = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.CUSTOM_PRESS_TOGGLE[i]);
        if (uri == null)
            return getResources().getString(R.string.navbar_action_none);

        if (uri.startsWith("**")) {
            if (uri.equals("**home**"))
                return getResources().getString(R.string.navbar_action_home);
            else if (uri.equals("**back**"))
                return getResources().getString(R.string.navbar_action_back);
            else if (uri.equals("**recents**"))
                return getResources().getString(R.string.navbar_action_recents);
            else if (uri.equals("**recentsgb**"))
                return getResources().getString(R.string.navbar_action_recents_gb);
            else if (uri.equals("**search**"))
                return getResources().getString(R.string.navbar_action_search);
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
            else if (uri.equals("**lastapp**"))
                return getResources().getString(R.string.navbar_action_lastapp);
            else if (uri.equals("**null**"))
                return getResources().getString(R.string.navbar_action_none);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        if (Settings.System.putString(getActivity().getContentResolver(),
                mCustomAction.activitySettingName, uri)) {
            if (mCustomAction.iconIndex != -1) {
                if (bmp == null) {
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.CUSTOM_TOGGLE_ICONS[mCustomAction.iconIndex],
                                    "");
                } else {
                    String iconName = getIconFileName(mCustomAction.iconIndex);
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
                                    Settings.System.CUSTOM_TOGGLE_ICONS[mCustomAction.iconIndex], "");
                    Settings.System
                            .putString(
                                    getContentResolver(),
                                    Settings.System.CUSTOM_TOGGLE_ICONS[mCustomAction.iconIndex],
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
            if(sToggles != null && sToggles.containsKey("default_toggles")) {
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

}
