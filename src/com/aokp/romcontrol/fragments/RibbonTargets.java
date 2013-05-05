
package com.aokp.romcontrol.fragments;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavBarHelpers;
import com.android.internal.util.aokp.LockScreenHelpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.ROMControlActivity;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.SeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerDialog;

public class RibbonTargets extends AOKPPreferenceFragment implements
          ShortcutPickerHelper.OnPickListener,
          ColorPickerDialog.OnColorChangedListener,
          SeekBar.OnSeekBarChangeListener {

    private TextView mEnableText;
    private Switch mEnableTextSwitch;
    private TextView mEnableVib;
    private Switch mEnableVibSwitch;
    private TextView mIconSizeText;
    private Spinner mIconSize;
    private TextView mLocationText;
    private Spinner mLocation;
    private TextView mIconLocationText;
    private Spinner mIconLocation;

    Resources mResources;
    private Spinner mRibbonChooser;
    private ImageButton mResetButton, mAddButton, mSaveButton, mCloneButton;
    private LinearLayout targetsLayout;
    private LinearLayout llbuttons;
    private LinearLayout mButtonContainer;
    private PackageManager mPackMan;
    ArrayList<String> mShortTargets = new ArrayList<String>();
    ArrayList<String> mLongTargets = new ArrayList<String>();
    ArrayList<String> mCustomIcons = new ArrayList<String>();
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
    private TextView mRibbonOpacityText;
    private SeekBar mDragHandleOpacity;
    private SeekBar mDragHandleWidth;
    private SeekBar mDragHandleHeight;
    private SeekBar mRibbonOpacity;
    private TextView mRibbonColorText;
    private Button mRibbonColor;
    private Button mTextColor;
    private TextView mRibbonIconSpaceText;
    private SeekBar mRibbonIconSpace;
    private Switch mRibbonIconVibrate;
    private TextView mButtonColorizeText;
    private Switch mButtonColorize;

    private int textColor;
    private int ribbonColor;

    private int colorPref;
    private int ribbonNumber = 0;

    private DisplayMetrics metrics;
    private WindowManager wm;
    private IntentFilter filter;
    private RibbonDialogReceiver reciever;

    private String[] mActions;
    private String[] mActionCodes;

    private ShortcutPickerHelper mPicker;

    private static final String TAG = "Ribbon Targets";

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;

    private static final LinearLayout.LayoutParams PARAMS_TOGGLE = new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);

    private static final LinearLayout.LayoutParams PARAMS_TOGGLE_SCROLL = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);

    public static enum DialogConstant {
        REMOVE_TARGET  { @Override public String value() { return "**remove**";}},
        LONG_ACTION  { @Override public String value() { return "**long**";}},
        INSERT_TARGET  { @Override public String value() { return "**insert**";}},
        CUSTOM_ICON  { @Override public String value() { return "**icon**";}},
        CUSTOM_APP  { @Override public String value() { return "**app**";}},
        AWESOME_ACTION  { @Override public String value() { return "**awesome**";}},
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
        return DialogConstant.AWESOME_ACTION;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_ribbon);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);
        mPackMan = getPackageManager();
        mResources = mContext.getResources();

        reciever = new RibbonDialogReceiver();
        filter = new IntentFilter();
        filter.addAction(RibbonDialogReceiver.ACTION_RIBBON_DIALOG_DISMISS);
        mContext.registerReceiver(new RibbonDialogReceiver(), filter);

        metrics = new DisplayMetrics();
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        // Get NavBar Actions
        mActionCodes = NavBarHelpers.getNavBarActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext, mActionCodes[i]);
        }

        setHasOptionsMenu(true);
    }

    private void getRibbonNumber() {
        if (arrayNum == 5) {
            ribbonNumber = 2;
        } else if (arrayNum == 2) {
            ribbonNumber = 0;
        } else if (arrayNum == 4) {
            ribbonNumber = 1;
        }
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


        mRibbonColorText = ((TextView) ll.findViewById(R.id.ribbon_color_id));
        mRibbonColor = ((Button) ll.findViewById(R.id.ribbon_color));
        mRibbonColor.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPref = 0;
                ColorPickerDialog picker = new ColorPickerDialog(mContext, ribbonColor);
                picker.setOnColorChangedListener(RibbonTargets.this);
                picker.show();
            }
        });

        mTextColor = ((Button) ll.findViewById(R.id.text_color));
        mTextColor.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPref = 1;
                ColorPickerDialog picker = new ColorPickerDialog(mContext, textColor);
                picker.setOnColorChangedListener(RibbonTargets.this);
                picker.show();
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

        mRibbonIconVibrate = (Switch) ll.findViewById(R.id.ribbon_icon_vibrate_switch);
        mRibbonIconVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.RIBBON_ICON_VIBRATE[arrayNum], checked);
            }
        });

        mButtonColorizeText = ((TextView) ll.findViewById(R.id.enable_button_colorize_id));
        mButtonColorize = (Switch) ll.findViewById(R.id.enable_button_colorize);
        mButtonColorize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.RIBBON_ICON_COLORIZE[arrayNum], checked);
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

       mIconLocationText = ((TextView) ll.findViewById(R.id.ribbon_icon_location_id));
       mIconLocation = (Spinner) ll.findViewById(R.id.ribbon_icon_location);
       mIconLocation.setAdapter(locAdapter);
       mIconLocation.post(new Runnable() {
            public void run() {
                mIconLocation.setOnItemSelectedListener(new IconLocationListener());
            }
        });

       mDragHandleOpacityText = ((TextView) ll.findViewById(R.id.drag_handle_opacity_id));
       mRibbonOpacityText = ((TextView) ll.findViewById(R.id.ribbon_opacity_id));
       mDragHandleWidthText = ((TextView) ll.findViewById(R.id.drag_handle_width_id));
       mDragHandleHeightText = ((TextView) ll.findViewById(R.id.drag_handle_height_id));
       mRibbonIconSpaceText = ((TextView) ll.findViewById(R.id.ribbon_icon_space_id));
       mDragHandleOpacity = (SeekBar) ll.findViewById(R.id.drag_handle_opacity);
       mRibbonOpacity = (SeekBar) ll.findViewById(R.id.ribbon_opacity);
       mDragHandleWidth = (SeekBar) ll.findViewById(R.id.drag_handle_width);
       mDragHandleHeight = (SeekBar) ll.findViewById(R.id.drag_handle_height);
       mRibbonIconSpace = (SeekBar) ll.findViewById(R.id.ribbon_icon_space);
       mDragHandleOpacity.setOnSeekBarChangeListener(this);
       mRibbonOpacity.setOnSeekBarChangeListener(this);
       mDragHandleWidth.setOnSeekBarChangeListener(this);
       mDragHandleHeight.setOnSeekBarChangeListener(this);
       mRibbonIconSpace.setOnSeekBarChangeListener(this);
       mDragHandleOpacity.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_OPACITY, 0));
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
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_HIDE_TIMEOUT[ribbonNumber], tempHide);
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

    public class IconLocationListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String[] values = getResources().getStringArray(R.array.ribbon_handle_location_values);
            int tempLoc = Integer.parseInt((String) values[pos]);
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_LOCATION[ribbonNumber], tempLoc);
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
            case R.id.rearrange:
                ArrayList<String> aTargets = new ArrayList<String>();
                for (int i = 0; i < mShortTargets.size(); i++) {
                    if (mShortTargets.get(i).equals("**null**")) {
                        aTargets.add(NavBarHelpers.getProperSummary(mContext, mLongTargets.get(i)));
                    } else {
                        aTargets.add(NavBarHelpers.getProperSummary(mContext, mShortTargets.get(i)));
                    }
                }
                ArrangeRibbonFragment fragment = new ArrangeRibbonFragment();
                fragment.setResources(mContext, mContentRes, aTargets,
                    mShortTargets, mLongTargets, mCustomIcons, arrayNum);
                fragment.show(getFragmentManager(), "rearrange");
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
        switch (arrayNum) {
        case 5:
            if (hasNavBarByDefault || navBarEnabled) {
                mEnableBottomWarning.setVisibility(View.VISIBLE);
            } else {
                mEnableBottomWarning.setVisibility(View.GONE);
            }
            mEnableBottomSwitch.setVisibility(View.VISIBLE);
            mEnableBottomText.setVisibility(View.VISIBLE);
            mEnableLeftSwitch.setVisibility(View.GONE);
            mEnableLeftText.setVisibility(View.GONE);
            mEnableRightSwitch.setVisibility(View.GONE);
            mEnableRightText.setVisibility(View.GONE);
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
            mIconLocationText.setVisibility(View.GONE);
            mIconLocation.setVisibility(View.GONE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mEnableVib.setVisibility(View.VISIBLE);
            mEnableVibSwitch.setVisibility(View.VISIBLE);
            break;
        case 4:
            mEnableBottomWarning.setVisibility(View.GONE);
            mEnableBottomSwitch.setVisibility(View.GONE);
            mEnableBottomText.setVisibility(View.GONE);
            mEnableLeftSwitch.setVisibility(View.GONE);
            mEnableLeftText.setVisibility(View.GONE);
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
            mIconLocationText.setVisibility(View.VISIBLE);
            mIconLocation.setVisibility(View.VISIBLE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mEnableVib.setVisibility(View.VISIBLE);
            mEnableVibSwitch.setVisibility(View.VISIBLE);
            break;
        case 2:
            mEnableBottomWarning.setVisibility(View.GONE);
            mEnableBottomSwitch.setVisibility(View.GONE);
            mEnableBottomText.setVisibility(View.GONE);
            mEnableLeftSwitch.setVisibility(View.VISIBLE);
            mEnableLeftText.setVisibility(View.VISIBLE);
            mEnableRightSwitch.setVisibility(View.GONE);
            mEnableRightText.setVisibility(View.GONE);
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
            mIconLocationText.setVisibility(View.VISIBLE);
            mIconLocation.setVisibility(View.VISIBLE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mEnableVib.setVisibility(View.VISIBLE);
            mEnableVibSwitch.setVisibility(View.VISIBLE);
            break;
        default :
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
            mIconLocationText.setVisibility(View.GONE);
            mIconLocation.setVisibility(View.GONE);
            mEnableVib.setVisibility(View.GONE);
            mEnableVibSwitch.setVisibility(View.GONE);
            mTimeOutText.setVisibility(View.GONE);
            break;
        }

    }

    public void resetRibbon() {
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum], "");
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum], "");
        Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_TEXT[arrayNum], true);
        Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SIZE[arrayNum], 0);
        Settings.System.putInt(mContentRes, Settings.System.RIBBON_TEXT_COLOR[arrayNum], -1);
        Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[arrayNum], "");
    }

    public void setupButtons() {
        getRibbonNumber();
        updateSwitches();
        mShortTargets.clear();
        mLongTargets.clear();
        mCustomIcons.clear();
        mShortTargets = Settings.System.getArrayList(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum]);
        mLongTargets = Settings.System.getArrayList(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum]);
        mCustomIcons = Settings.System.getArrayList(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[arrayNum]);
        if (mShortTargets.size() < 1) {
            mShortTargets.add("**null**");
            mLongTargets.add("**null**");
            mCustomIcons.add("**null**");
        }

        mRibbonIconSpace.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[arrayNum], 5));
        mEnableTextSwitch.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_RIBBON_TEXT[arrayNum], true));
        textColor = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.RIBBON_TEXT_COLOR[arrayNum], Color.WHITE);
        mTextColor.setBackgroundColor(textColor);
        final String[] iconValues = getResources().getStringArray(R.array.ribbon_icon_size_values);

        mIconSize.setSelection(Arrays.asList(iconValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
          Settings.System.RIBBON_ICON_SIZE[arrayNum], 0))));

        mRibbonIconVibrate.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.RIBBON_ICON_VIBRATE[arrayNum], true));

        boolean colorize = Settings.System.getBoolean(mContentRes,
                Settings.System.RIBBON_ICON_COLORIZE[arrayNum], false);
        mButtonColorize.setChecked(colorize);

        ribbonColor = Settings.System.getInt(mContentRes,
                Settings.System.SWIPE_RIBBON_COLOR[ribbonNumber], Color.BLACK);
        mRibbonColor.setBackgroundColor(ribbonColor);

        final String[] hideValues = getResources().getStringArray(R.array.hide_navbar_timeout_values);

        mTimeOut.setSelection(Arrays.asList(hideValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
            Settings.System.RIBBON_HIDE_TIMEOUT[ribbonNumber], 5000))));

        if (ribbonNumber < 2) {
            final String[] locValues = getResources().getStringArray(R.array.ribbon_handle_location_values);
            mIconLocation.setSelection(Arrays.asList(locValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
                Settings.System.RIBBON_ICON_LOCATION[ribbonNumber], 0))));
        }

        mRibbonOpacity.setProgress(Settings.System.getInt(mContentRes, Settings.System.SWIPE_RIBBON_OPACITY[ribbonNumber], 100));

    }

    public void refreshButtons() {
        llbuttons.removeAllViews();
        targetsLayout = new LinearLayout(mContext);
        HorizontalScrollView targetScrollView = new HorizontalScrollView(mContext);
        targetsLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        targetScrollView.setHorizontalFadingEdgeEnabled(true);
        int length = mShortTargets.size();
        for (int i = 0; i < length; i++) {
            if (mCustomIcons.size() > 0) {
                if (!mCustomIcons.get(i).equals("**null**")) {
                    targetsLayout.addView(getCustomIcon(mCustomIcons.get(i)), PARAMS_TOGGLE_SCROLL);
                } else {
                    Drawable mIcon = NavBarHelpers.getIconImage(mContext,
                        mShortTargets.get(i).equals("**null**") ? mLongTargets.get(i) : mShortTargets.get(i));
                    int desiredSize = (int) (48 * metrics.density);
                    int width = mIcon.getIntrinsicWidth();
                    if (width > desiredSize) {
                        Bitmap bm = ((BitmapDrawable) mIcon).getBitmap();
                        if (bm != null) {
                            Bitmap bitmapOrig = Bitmap.createScaledBitmap(bm, desiredSize, desiredSize, false);
                            mIcon = new BitmapDrawable(mContext.getResources(), bitmapOrig);
                        }
                    }

                    targetsLayout.addView(getImageButton(mIcon), PARAMS_TOGGLE_SCROLL);
                }
            } else {
                targetsLayout.addView(getImageButton(mShortTargets.get(i)), PARAMS_TOGGLE_SCROLL);
            }
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

    private ImageButton getImageButton(Drawable d) {
        ImageButton ib = new ImageButton(mContext);
        ib.setImageDrawable(d);
        ib.setBackgroundDrawable(null);
        ib.setOnClickListener(mRibbonClickListener);
        return ib;
    }

    private ImageButton getCustomIcon(String uri) {
        ImageButton ib = new ImageButton(mContext);
        ib.setImageDrawable(getCustomDrawable(mContext, uri));
        ib.setBackgroundDrawable(null);
        ib.setOnClickListener(mRibbonClickListener);
        return ib;
    }

    private void saveButtons() {
        Log.d(TAG, "saving ribbon targets:" + TextUtils.join("|", mShortTargets));
        Log.d(TAG, "saving ribbon targets:" + TextUtils.join("|", mLongTargets));
        Log.d(TAG, "saving ribbon targets:" + TextUtils.join("|", mCustomIcons));
        Settings.System.putArrayList(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum],
                mShortTargets);
        Settings.System.putArrayList(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum],
                mLongTargets);
        Settings.System.putArrayList(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[arrayNum],
                mCustomIcons);
    }

    public void onValueChange(String uri) {
        DialogConstant mFromString = funcFromString(uri);
        switch (mFromString) {
        case SHORT_ACTION:
            longPressChoice = false;
            createDialog(
                getResources().getString(R.string.choose_action_title),
                mActions, mActionCodes);
            break;
        case LONG_ACTION:
            longPressChoice = true;
            createDialog(
                getResources().getString(R.string.choose_action_title),
                mActions, mActionCodes);
            break;
        case CUSTOM_APP:
            mPicker.pickShortcut();
            break;
        case REMOVE_TARGET:
            mShortTargets.remove(mTargetNum);
            mLongTargets.remove(mTargetNum);
            mCustomIcons.remove(mTargetNum);
            break;
        case CUSTOM_ICON:
            int width = 90;
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
        case INSERT_TARGET:
            mShortTargets.add(mTargetNum + 1, "**null**");
            mLongTargets.add(mTargetNum + 1, "**null**");
            mCustomIcons.add(mTargetNum + 1, "**null**");
            break;
        case AWESOME_ACTION:
            if (longPressChoice) {
                mLongTargets.set(mTargetNum, uri);
            } else {
                mShortTargets.set(mTargetNum, uri);
            }
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
                String tempIcons = Settings.System.getString(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[tempInt]);
                Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum], tempShort);
                Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum], tempLong);
                try {
                int tempSpace = Settings.System.getInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[tempInt]);
                Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[arrayNum], tempSpace);
                } catch (SettingNotFoundException e) {
                // compiler says there might be an error here.... no sure how though.
                }
                Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[arrayNum], tempIcons);
                setupButtons();
                refreshButtons();
                dialog.dismiss();
                }
            };

            final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(title)
                .setItems(entries, l)
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
                    mCustomIcons.add("**null**");
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
            Settings.System.putInt(mContentRes, Settings.System.SWIPE_RIBBON_OPACITY[ribbonNumber], progress);
        } else if (seekBar == mDragHandleWidth) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_WEIGHT, progress);
        } else if (seekBar == mDragHandleHeight) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_HEIGHT, progress);
        } else if (seekBar == mRibbonIconSpace) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[arrayNum], progress);
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


    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + String.valueOf(mTargetNum) + String.valueOf(arrayNum) + ".png"));

    }

    private String getIconFileName() {
        return "ribbon_icon_" + String.valueOf(mTargetNum) + String.valueOf(arrayNum) + ".png";
    }

    @Override
    public void onColorChanged(int color) {
        switch (colorPref) {
        case 0:
            Settings.System.putInt(mContentRes,
                    Settings.System.SWIPE_RIBBON_COLOR[ribbonNumber], color);
            ribbonColor = color;
            mRibbonColor.setBackgroundColor(ribbonColor);
            break;
        case 1:
            Settings.System.putInt(mContentRes,
                    Settings.System.RIBBON_TEXT_COLOR[arrayNum], color);
            textColor = color;
            mTextColor.setBackgroundColor(textColor);
            break;
        }
    }

    public static Drawable getCustomDrawable(Context context, String action) {
        final Resources res = context.getResources();

        File f = new File(Uri.parse(action).getPath());
        Drawable front = new BitmapDrawable(res,
                         getRoundedCornerBitmap(BitmapFactory.decodeFile(f.getAbsolutePath())));
        return front;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
            bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 24;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if (requestCode == REQUEST_PICK_CUSTOM_ICON) {

                String iconName = getIconFileName();
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
                mCustomIcons.set(mTargetNum, Uri
                        .fromFile(new File(mContext.getFilesDir(), iconName)).getPath());

                File f = new File(selectedImageUri.getPath());
                if (f.exists())
                    f.delete();

                refreshButtons();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class RibbonDialogReceiver extends BroadcastReceiver {
        public static final String ACTION_RIBBON_DIALOG_DISMISS = "com.aokp.romcontrol.ACTION_RIBBON_DIALOG_DISMISS";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_RIBBON_DIALOG_DISMISS.equals(action)) {
                setupButtons();
                refreshButtons();
            }
        }
    }
}
