
package com.aokp.romcontrol.fragments;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;


import com.android.internal.util.aokp.NavBarHelpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.ROMControlActivity;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.util.ShortcutPickerHelper;

public class RibbonTargets extends AOKPPreferenceFragment implements
          ShortcutPickerHelper.OnPickListener {

    CheckBoxPreference mEnableText;
    //CheckBoxPreference mEnableInNotifications;

    Resources mResources;
    private Spinner mRibbonChooser;
    private ImageButton mResetButton, mAddButton, mSaveButton, mCloneButton;
    private LinearLayout targetsLayout;
    private LinearLayout llbuttons;
    private LinearLayout mButtonContainer;
    private PackageManager mPackMan;
    ArrayList<String> mShortTargets = new ArrayList<String>();
    ArrayList<String> mLongTargets = new ArrayList<String>();
    private int mTargetNum;
    private int arrayNum = 0;
    private boolean longPressChoice;

    private ShortcutPickerHelper mPicker;

    private static final String TAG = "Ribbon Targets";

    private static final LinearLayout.LayoutParams PARAMS_TOGGLE = new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);

    private static final LinearLayout.LayoutParams PARAMS_TOGGLE_SCROLL = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);

    public static enum DialogConstant {
        REMOVE_TARGET  { @Override public String value() { return "**remove**";}},
        LONG_ACTION  { @Override public String value() { return "**long**";}},
        SHORT_ACTION { @Override public String value() { return "**short**";}};
        public String value() { return this.value(); }
    }

    public static DialogConstant funcFromString(String string) {
        DialogConstant[] allTargs = DialogConstant.values();
        for (int i=0; i < allTargs.length; i++) {
            if (string.equals(allTargs[i].value())) {
                return allTargs[i];
            }
        }
        // not in ENUM must be custom
        return DialogConstant.SHORT_ACTION;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_ribbon);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ribbon);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);
        mPackMan = getPackageManager();
        mResources = mContext.getResources();

        mEnableText = (CheckBoxPreference) findPreference("enable_ribbon_text");

      /*  mEnableInNotifications = (CheckBoxPreference) findPreference("enable_ribbon_notifications");
        mEnableInNotifications.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_RIBBON_NOTIFICATIONS, false)); */

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedinstanceState){
       View ll = inflater.inflate(R.layout.ribbon, container, false);
       mResetButton = (ImageButton) ll.findViewById(R.id.reset_button);
       mResetButton.setOnClickListener(mCommandButtons);
       mAddButton = (ImageButton) ll.findViewById(R.id.add_button);
       mAddButton.setOnClickListener(mCommandButtons);
       mSaveButton = (ImageButton) ll.findViewById(R.id.save_button);
       mSaveButton.setOnClickListener(mCommandButtons);
       mCloneButton = (ImageButton) ll.findViewById(R.id.clone_button);
       mCloneButton.setOnClickListener(mCommandButtons);
       mButtonContainer = (LinearLayout) ll.findViewById(R.id.ribbon_container);
       llbuttons = (LinearLayout) ll.findViewById(R.id.ribbon_targets_container);
       mRibbonChooser = (Spinner) ll.findViewById(R.id.spinner);
       ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       final String[] entries = getResources().getStringArray(R.array.ribbon_chooser_entries);
       for (int i = 0; i < entries.length ; i++) {
            spinnerAdapter.add(entries[i]);
       }
        mRibbonChooser.setAdapter(spinnerAdapter);
        mRibbonChooser.post(new Runnable() {
            public void run() {
                mRibbonChooser.setOnItemSelectedListener(new RibbonChooserListener());
            }
        });
       setupButtons();
       return ll;
    }

    public class RibbonChooserListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String[] values = getResources().getStringArray(R.array.ribbon_chooser_values);
            arrayNum = Integer.parseInt((String) values[pos]);
            setupButtons();
            refreshButtons();
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.ribbon, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset:
                resetRibbon();
                setupButtons();
                refreshButtons();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableText) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.ENABLE_RIBBON_TEXT[arrayNum],
                    ((CheckBoxPreference) preference).isChecked());
            return true;
       /* } else if (preference == mEnableInNotifications) {
            Settings.System.putBoolean(mContentRes,
                    Settings.System.ENABLE_RIBBON_NOTIFICATIONS,
                    ((CheckBoxPreference) preference).isChecked()); 
            return true; */
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshButtons();
    }

    public void resetRibbon() {
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum], "");
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum], "");
        Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_TEXT[arrayNum], true);
        //Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_NOTIFICATIONS, false);
    }

    public void setupButtons() {
        mEnableText.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_RIBBON_TEXT[arrayNum], true));
        mShortTargets.clear();
        mLongTargets.clear();
        String sTargets = Settings.System.getString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum]);
        String lTargets = Settings.System.getString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum]);
        if (!TextUtils.isEmpty(sTargets) && !TextUtils.isEmpty(lTargets)) {
            String[] sSplit = sTargets.split("\\|");
            String[] lSplit = lTargets.split("\\|");
            for (String i : sSplit) {
                mShortTargets.add(i);
            }
            for (String i : lSplit) {
                mLongTargets.add(i);
            }
        } else {
            mShortTargets.add("**null**");
            mLongTargets.add("**null**");
        }
    }

    public void refreshButtons() {
        llbuttons.removeAllViews();
        targetsLayout = new LinearLayout(mContext);
        HorizontalScrollView targetScrollView = new HorizontalScrollView(mContext);
        targetsLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        targetScrollView.setHorizontalFadingEdgeEnabled(true);
        for (int i = 0; i < mShortTargets.size(); i++) {
            targetsLayout.addView(getImageButton(mShortTargets.get(i)), PARAMS_TOGGLE_SCROLL);
        }
        targetScrollView.addView(targetsLayout, PARAMS_TOGGLE);
        llbuttons.addView(targetScrollView);
    }

    private ImageButton getImageButton(String uri) {
        ImageButton ib = new ImageButton(mContext);
        ib.setImageDrawable(NavBarHelpers.getIconImage(mContext, uri));
        ib.setBackgroundDrawable(null);
        ib.setOnClickListener(mRibbonClickListener);
        return ib;
    }

    private void saveButtons() {
        StringBuilder b = new StringBuilder();
        StringBuilder c = new StringBuilder();
        for (int i = 0; i < mShortTargets.size(); i++) {
            final String temp = mShortTargets.get(i);
            if (temp.isEmpty()) {
                continue;
            }
            b.append(temp);
            b.append("|");
        }
        for (int i = 0; i < mLongTargets.size(); i++) {
            final String temp = mLongTargets.get(i);
            if (temp.isEmpty()) {
                continue;
            }
            c.append(temp);
            c.append("|");
        }
        if (String.valueOf(b.charAt(b.length() - 1)).equals("!")) {
            b.deleteCharAt(b.length() - 1);
        }
        if (String.valueOf(c.charAt(c.length() - 1)).equals("!")) {
            c.deleteCharAt(c.length() - 1);
        }

        Log.d(TAG, "saving ribbon targets:" + b.toString());
        Log.d(TAG, "saving ribbon targets:" + c.toString());
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum],
                b.toString());
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum],
                c.toString());
        if (arrayNum == 1) {
            //Helpers.restartSystemUI();
        }
    }

    public void onValueChange(String uri) {
        DialogConstant mFromString = funcFromString(uri);
        switch (mFromString) {
        case SHORT_ACTION:
            longPressChoice = false;
            mPicker.pickShortcut();
            break;
        case LONG_ACTION:
            longPressChoice = true;
            mPicker.pickShortcut();
            break;
        case REMOVE_TARGET:
            mShortTargets.remove(mTargetNum);
            mLongTargets.remove(mTargetNum);

            break;
        }
        refreshButtons();
    }

    public void createDialog(final String title, final String[] entries, final String[] values) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onValueChange(values[item]);
                dialog.dismiss();
                }
            };

            final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setSingleChoiceItems(entries, -1, l)
                .create();

            dialog.show();
    }

    public void cloneDialog(final String title, final String[] entries, final String[] values) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum],
                Settings.System.getString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[Integer.parseInt((String) values[item])]));
                setupButtons();
                dialog.dismiss();
                }
            };

            final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setSingleChoiceItems(entries, -1, l)
                .create();

            dialog.show();
    }

    private View.OnClickListener mRibbonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTargetNum = targetsLayout.indexOfChild(v);
            String sText = mShortTargets.get(mTargetNum);
            String lText = mLongTargets.get(mTargetNum);
            final String[] stringArray = mContext.getResources().getStringArray(R.array.ribbon_dialog_entries);
            stringArray[0] = stringArray[0] + "  :  " + NavBarHelpers.getProperSummary(mContext, sText);
            stringArray[1] = stringArray[1] + "  :  " + NavBarHelpers.getProperSummary(mContext, lText);
            createDialog(
                getResources().getString(R.string.choose_action_title), stringArray,
                getResources().getStringArray(R.array.ribbon_dialog_values));
        }
    };

    private View.OnClickListener mCommandButtons = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int command = v.getId();
            switch (command) {
                case R.id.reset_button:
                    setupButtons();
                    break;
                case R.id.add_button:
                    mShortTargets.add("**null**");
                    mLongTargets.add("**null**");
                    break;
                case R.id.save_button:
                    saveButtons();
                    break;
                case R.id.clone_button:
                    cloneDialog(getResources().getString(R.string.clone_title),
                        getResources().getStringArray(R.array.ribbon_chooser_entries),
                        getResources().getStringArray(R.array.ribbon_chooser_values));
                    break;
            }
            refreshButtons();
        }
    };

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        if (longPressChoice) {
            mLongTargets.set(mTargetNum, uri);
        } else {
            mShortTargets.set(mTargetNum, uri);
        }
        refreshButtons();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            
            } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
