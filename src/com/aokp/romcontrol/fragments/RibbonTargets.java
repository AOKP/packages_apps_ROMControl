
package com.aokp.romcontrol.fragments;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
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
import com.aokp.romcontrol.widgets.SeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class RibbonTargets extends AOKPPreferenceFragment implements
          ShortcutPickerHelper.OnPickListener,
          SeekBar.OnSeekBarChangeListener {

    private TextView mEnableText;
    private Switch mEnableTextSwitch;
    private TextView mEnableVib;
    private Switch mEnableVibSwitch;
    private TextView mIconSizeText;
    private Spinner mIconSize;
    private TextView mLocationText;
    private Spinner mLocation;

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

    private TextView mEnableBottomWarning;
    private Switch mEnableBottomSwitch;
    private TextView mEnableBottomText;
    private Switch mEnableLeftSwitch;
    private TextView mEnableLeftText;
    private Switch mEnableRightSwitch;
    private TextView mEnableRightText;
    private TextView mTimeOutText;
    private Spinner mTimeOut;
    private TextView mDragHandleOpacityText;
    private TextView mDragHandleWidthText;
    private TextView mDragHandleHeightText;
    private TextView mRibbonColorText;
    private TextView mRibbonOpacityText;
    private SeekBar mDragHandleOpacity;
    private SeekBar mDragHandleWidth;
    private SeekBar mDragHandleHeight;
    private SeekBar mRibbonOpacity;
    private Button mRibbonColor;

    private ShortcutPickerHelper mPicker;

    private static final String TAG = "Ribbon Targets";

    private static final LinearLayout.LayoutParams PARAMS_TOGGLE = new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);

    private static final LinearLayout.LayoutParams PARAMS_TOGGLE_SCROLL = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);

    public static enum DialogConstant {
        REMOVE_TARGET  { @Override public String value() { return "**remove**";}},
        LONG_ACTION  { @Override public String value() { return "**long**";}},
        INSERT_TARGET  { @Override public String value() { return "**insert**";}},
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

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);
        mPackMan = getPackageManager();
        mResources = mContext.getResources();

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

       mTimeOutText = ((TextView) ll.findViewById(R.id.timeout_spinner_id));
       mTimeOut = (Spinner) ll.findViewById(R.id.timeout_spinner);
       ArrayAdapter<CharSequence> hideAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       hideAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       final String[] hideEntries = getResources().getStringArray(R.array.hide_navbar_timeout_entries);
       for (int i = 0; i < hideEntries.length ; i++) {
            hideAdapter.add(hideEntries[i]);
       }
        mTimeOut.setAdapter(hideAdapter);
        mTimeOut.post(new Runnable() {
            public void run() {
                mTimeOut.setOnItemSelectedListener(new TimeOutListener());
            }
        });

       final String[] hideValues = getResources().getStringArray(R.array.hide_navbar_timeout_values);

       mTimeOut.setSelection(Arrays.asList(hideValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
            Settings.System.RIBBON_HIDE_TIMEOUT, 5000))));

        mEnableBottomWarning = ((TextView) ll.findViewById(R.id.ribbon_bottom_warning_id));

        mEnableBottomText = ((TextView) ll.findViewById(R.id.ribbon_bottom_enable_id));
        mEnableBottomSwitch = (Switch) ll.findViewById(R.id.ribbon_bottom_enable_switch);
        mEnableBottomSwitch.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_RIBBON_LOCATION[0], false));
        mEnableBottomSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_LOCATION[0], checked);
            }
        });

        mEnableLeftText = ((TextView) ll.findViewById(R.id.ribbon_left_enable_id));
        mEnableLeftSwitch = (Switch) ll.findViewById(R.id.ribbon_left_enable_switch);
        mEnableLeftSwitch.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_RIBBON_LOCATION[1], false));
        mEnableLeftSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_LOCATION[1], checked);
            }
        });

        mEnableRightText = ((TextView) ll.findViewById(R.id.ribbon_right_enable_id));
        mEnableRightSwitch = (Switch) ll.findViewById(R.id.ribbon_right_enable_switch);
        mEnableRightSwitch.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_RIBBON_LOCATION[2], false));
        mEnableRightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_LOCATION[2], checked);
            }
        });    

        mEnableText = ((TextView) ll.findViewById(R.id.enable_ribbon_text_id));
        mEnableTextSwitch = (Switch) ll.findViewById(R.id.enable_ribbon_text);
        mEnableTextSwitch.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_RIBBON_TEXT[arrayNum], false));
        mEnableTextSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_TEXT[arrayNum], checked);
            }
        });

        mEnableVib = ((TextView) ll.findViewById(R.id.enable_ribbon_vibrate_id));
        mEnableVibSwitch = (Switch) ll.findViewById(R.id.enable_ribbon_vibrate);
        mEnableVibSwitch.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.SWIPE_RIBBON_VIBRATE, false));
        mEnableVibSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.SWIPE_RIBBON_VIBRATE, checked);
            }
        });

       mIconSizeText = ((TextView) ll.findViewById(R.id.ribbon_icon_size_id));
       mIconSize = (Spinner) ll.findViewById(R.id.ribbon_icon_size);
       ArrayAdapter<CharSequence> iconAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       final String[] iconEntries = getResources().getStringArray(R.array.ribbon_icon_size_entries);
       for (int i = 0; i < iconEntries.length ; i++) {
            iconAdapter.add(iconEntries[i]);
       }
        mIconSize.setAdapter(iconAdapter);
        mIconSize.post(new Runnable() {
            public void run() {
                mIconSize.setOnItemSelectedListener(new IconSizeListener());
            }
        });

       final String[] iconValues = getResources().getStringArray(R.array.ribbon_icon_size_values);

       mIconSize.setSelection(Arrays.asList(iconValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
            Settings.System.RIBBON_ICON_SIZE[arrayNum], 0))));

       mLocationText = ((TextView) ll.findViewById(R.id.ribbon_handle_location_id));
       mLocation = (Spinner) ll.findViewById(R.id.ribbon_handle_location);
       ArrayAdapter<CharSequence> locAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       final String[] locEntries = getResources().getStringArray(R.array.ribbon_handle_location_entries);
       for (int i = 0; i < locEntries.length ; i++) {
            locAdapter.add(locEntries[i]);
       }
        mLocation.setAdapter(locAdapter);
        mLocation.post(new Runnable() {
            public void run() {
                mLocation.setOnItemSelectedListener(new LocationListener());
            }
        });

       final String[] locValues = getResources().getStringArray(R.array.ribbon_handle_location_values);

       mLocation.setSelection(Arrays.asList(locValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
            Settings.System.RIBBON_DRAG_HANDLE_LOCATION, 0))));

       mDragHandleOpacityText = ((TextView) ll.findViewById(R.id.drag_handle_opacity_id));
       mRibbonOpacityText = ((TextView) ll.findViewById(R.id.ribbon_opacity_id));
       mDragHandleWidthText = ((TextView) ll.findViewById(R.id.drag_handle_width_id));
       mDragHandleHeightText = ((TextView) ll.findViewById(R.id.drag_handle_height_id));
       mDragHandleOpacity = (SeekBar) ll.findViewById(R.id.drag_handle_opacity);
       mRibbonOpacity = (SeekBar) ll.findViewById(R.id.ribbon_opacity);
       mDragHandleWidth = (SeekBar) ll.findViewById(R.id.drag_handle_width);
       mDragHandleHeight = (SeekBar) ll.findViewById(R.id.drag_handle_height);
       mDragHandleOpacity.setOnSeekBarChangeListener(this);
       mRibbonOpacity.setOnSeekBarChangeListener(this);
       mDragHandleWidth.setOnSeekBarChangeListener(this);
       mDragHandleHeight.setOnSeekBarChangeListener(this);
       mDragHandleOpacity.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_OPACITY, 0));
       mRibbonOpacity.setProgress(Settings.System.getInt(mContentRes, Settings.System.SWIPE_RIBBON_OPACITY, 100));
       mDragHandleWidth.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_WEIGHT, 50));
       mDragHandleHeight.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_HEIGHT, 0));
       mRibbonColor = ((Button) ll.findViewById(R.id.ribbon_color));
       mRibbonColorText = ((TextView) ll.findViewById(R.id.ribbon_color_id));

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

    public class TimeOutListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String[] values = getResources().getStringArray(R.array.hide_navbar_timeout_values);
            int tempHide = Integer.parseInt((String) values[pos]);
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_HIDE_TIMEOUT, tempHide);
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class IconSizeListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String[] values = getResources().getStringArray(R.array.ribbon_icon_size_values);
            int tempSize = Integer.parseInt((String) values[pos]);
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SIZE[arrayNum], tempSize);
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class LocationListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String[] values = getResources().getStringArray(R.array.ribbon_handle_location_values);
            int tempLoc = Integer.parseInt((String) values[pos]);
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_LOCATION, tempLoc);
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
    public Dialog onCreateDialog(int dialogId) {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshButtons();
    }

    private void updateSwitches() {
        boolean hasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        boolean navBarAutoHide = Settings.System.getBoolean(mContentRes,
                    Settings.System.NAV_HIDE_ENABLE, false);
        boolean navBarEnabled = Settings.System.getBoolean(mContentRes,
                    Settings.System.NAVIGATION_BAR_SHOW, false);
        if (arrayNum == 2) {
            if ((hasNavBarByDefault || navBarEnabled) && navBarEnabled) {
                mEnableBottomWarning.setVisibility(View.VISIBLE);
                mEnableBottomSwitch.setEnabled(false);
            } else {
                mEnableBottomWarning.setVisibility(View.GONE);
                mEnableBottomSwitch.setEnabled(true);
            }
            mEnableBottomSwitch.setVisibility(View.VISIBLE);
            mEnableBottomText.setVisibility(View.VISIBLE);
            mEnableLeftSwitch.setVisibility(View.VISIBLE);
            mEnableLeftText.setVisibility(View.VISIBLE);
            mEnableRightSwitch.setVisibility(View.VISIBLE);
            mEnableRightText.setVisibility(View.VISIBLE);
            mTimeOut.setVisibility(View.VISIBLE);
            mDragHandleOpacity.setVisibility(View.VISIBLE);
            mDragHandleWidth.setVisibility(View.VISIBLE);
            mDragHandleHeight.setVisibility(View.VISIBLE);
            mRibbonOpacity.setVisibility(View.VISIBLE);
            mDragHandleOpacityText.setVisibility(View.VISIBLE);
            mDragHandleWidthText.setVisibility(View.VISIBLE);
            mDragHandleHeightText.setVisibility(View.VISIBLE);
            mRibbonOpacityText.setVisibility(View.VISIBLE);
            mRibbonColorText.setVisibility(View.VISIBLE);
            mRibbonColor.setVisibility(View.VISIBLE);
            mLocationText.setVisibility(View.VISIBLE);
            mLocation.setVisibility(View.VISIBLE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mEnableVib.setVisibility(View.VISIBLE);
            mEnableVibSwitch.setVisibility(View.VISIBLE);
        } else {
            mEnableBottomWarning.setVisibility(View.GONE);
            mEnableBottomSwitch.setVisibility(View.GONE);
            mEnableBottomText.setVisibility(View.GONE);
            mEnableLeftSwitch.setVisibility(View.GONE);
            mEnableLeftText.setVisibility(View.GONE);
            mEnableRightSwitch.setVisibility(View.GONE);
            mEnableRightText.setVisibility(View.GONE);
            mTimeOut.setVisibility(View.GONE);
            mDragHandleOpacity.setVisibility(View.GONE);
            mDragHandleWidth.setVisibility(View.GONE);
            mDragHandleHeight.setVisibility(View.GONE);
            mRibbonOpacity.setVisibility(View.GONE);
            mDragHandleOpacityText.setVisibility(View.GONE);
            mDragHandleWidthText.setVisibility(View.GONE);
            mDragHandleHeightText.setVisibility(View.GONE);
            mRibbonOpacityText.setVisibility(View.GONE);
            mRibbonColorText.setVisibility(View.GONE);
            mRibbonColor.setVisibility(View.GONE);
            mLocationText.setVisibility(View.GONE);
            mLocation.setVisibility(View.GONE);
            mEnableVib.setVisibility(View.GONE);
            mEnableVibSwitch.setVisibility(View.GONE);
            mTimeOutText.setVisibility(View.GONE);
        }

    }

    public void resetRibbon() {
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum], "");
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum], "");
        Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_TEXT[arrayNum], true);
        Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SIZE[arrayNum], 0);
    }

    public void setupButtons() {
        updateSwitches();
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
        if (b.length() > 0) {
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
        } else {
            Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum], "");
            Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum], "");
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
        case INSERT_TARGET:
            mShortTargets.add(mTargetNum + 1, "**null**");
            mLongTargets.add(mTargetNum + 1, "**null**");
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
                .setItems(entries, l)
                .create();

            dialog.show();
    }

    public void cloneDialog(final String title, final String[] entries, final String[] values) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                int tempInt = Integer.parseInt((String) values[item]);
                Log.d(TAG, String.valueOf(tempInt));
                String tempShort = Settings.System.getString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[tempInt]);
                String tempLong = Settings.System.getString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[tempInt]);
                Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum], tempShort);
                Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum], tempLong);
                setupButtons();
                refreshButtons();
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mDragHandleOpacity) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_OPACITY, progress);
        } else if (seekBar == mRibbonOpacity) {
            Settings.System.putInt(mContentRes, Settings.System.SWIPE_RIBBON_OPACITY, progress);
        } else if (seekBar == mDragHandleWidth) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_WEIGHT, progress);
        } else if (seekBar == mDragHandleHeight) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_HEIGHT, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

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
