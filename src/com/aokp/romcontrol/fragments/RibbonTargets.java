
package com.aokp.romcontrol.fragments;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import com.android.internal.util.aokp.AwesomeAnimationHelper;
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
    private TextView mWindowColumnsText;
    private Spinner mWindowColumns;

    Resources mResources;
    private Spinner mRibbonChooser;
    private ImageButton mResetButton, mAddButton, mSaveButton, mCloneButton;
    private LinearLayout targetsLayout;
    private LinearLayout llbuttons;
    private LinearLayout mButtonContainer;
    private LinearLayout mButtonInstructions;
    private LinearLayout mCommandButtonsCon;
    private PackageManager mPackMan;
    ArrayList<String> mShortTargets = new ArrayList<String>();
    ArrayList<String> mLongTargets = new ArrayList<String>();
    ArrayList<String> mCustomIcons = new ArrayList<String>();
    private int mTargetNum;
    private int arrayNum = 0;
    private int mChoice = 0;
    private MenuItem mMenuRearrange;
    private MenuItem mMenuReset;
    private MenuItem mMenuToggles;

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
    private TextView mTextColorText;
    private TextView mRibbonIconSpaceText;
    private SeekBar mRibbonIconSpace;
    private Switch mRibbonIconVibrate;
    private TextView mRibbonIconVibrateText;
    private TextView mButtonColorizeText;
    private Switch mButtonColorize;
    private TextView mWindowColorText;
    private Button mWindowColor;
    private TextView mTextWindowColorText;
    private Button mTextWindowColor;
    private TextView mWindowOpacityText;
    private SeekBar mWindowOpacity;
    private TextView mRibbonLongSwipeText;
    private Spinner mRibbonLongSwipe;
    private TextView mRibbonLongPressText;
    private Spinner mRibbonLongPress;
    private TextView mRibbonDismissText;
    private Spinner mRibbonDismiss;
    private TextView mRibbonAnimDurText;
    private SeekBar mRibbonAnimDur;
    private TextView mWindowAnimDurText;
    private SeekBar mWindowAnimDur;
    private TextView mRibbonAnimationText;
    private Spinner mRibbonAnimation;
    private TextView mAppWindowSpaceText;
    private SeekBar mAppWindowSpace;
    private TextView mTogglesButtonText;
    private Switch mTogglesButton;
    private TextView mRibbonHideImeText;
    private Switch mRibbonHideIme;


    private int textColor;
    private int ribbonColor;
    private int windowTextColor;
    private int windowColor;

    private int colorPref;
    private int ribbonNumber = 0;

    private DisplayMetrics metrics;
    private WindowManager wm;
    private IntentFilter filter;
    private RibbonDialogReceiver reciever;

    private BroadcastReceiver mReceiver;
    private ArrayList<String> allToggles = new ArrayList<String>();
    private ArrayList<String> allTogglesStrings = new ArrayList<String>();
    private Bundle sToggles = new Bundle();

    private ArrayList<String> mGoodName = new ArrayList<String>();
    private ArrayList<String> mHiddenApps = new ArrayList<String>();
    private ArrayList<String> mSelectedApps = new ArrayList<String>();
    private ArrayList<String> mApps = new ArrayList<String>();

    private String[] mActions;
    private String[] mActionCodes;

    private int[] mAnimations;
    private String[] mAnimationsStrings;

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
        setHasOptionsMenu(true);
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

        Intent request = new Intent("com.android.systemui.statusbar.toggles.ACTION_REQUEST_TOGGLES");
        mContext.sendBroadcast(request);

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

        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        mAnimationsStrings = new String[mAnimations.length];
        int animqty = mAnimations.length;
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(mContext, mAnimations[i]);
        }
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
       setHasOptionsMenu(true);
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
       mButtonInstructions = (LinearLayout) ll.findViewById(R.id.ribbon_targets_instructions);
       mCommandButtonsCon = (LinearLayout) ll.findViewById(R.id.ribbon_command_buttons);
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

        mWindowColorText = ((TextView) ll.findViewById(R.id.window_color_id));
        mWindowColor = ((Button) ll.findViewById(R.id.window_color));
        mWindowColor.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPref = 2;
                ColorPickerDialog picker = new ColorPickerDialog(mContext, windowColor);
                picker.setOnColorChangedListener(RibbonTargets.this);
                picker.show();
            }
        });

        mTextWindowColorText = ((TextView) ll.findViewById(R.id.window_text_color_id));
        mTextWindowColor = ((Button) ll.findViewById(R.id.window_text_color));
        mTextWindowColor.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPref = 3;
                ColorPickerDialog picker = new ColorPickerDialog(mContext, windowTextColor);
                picker.setOnColorChangedListener(RibbonTargets.this);
                picker.show();
            }
        });

        mTextColorText = ((TextView) ll.findViewById(R.id.text_color_id));
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

       ArrayAdapter<CharSequence> actionsAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       actionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       for (int i = 0; i < mActions.length ; i++) {
            actionsAdapter.add(mActions[i]);
       }

       mRibbonLongSwipeText = ((TextView) ll.findViewById(R.id.ribbon_long_swipe_id));
       mRibbonLongSwipe = (Spinner) ll.findViewById(R.id.ribbon_long_swipe);
       mRibbonLongSwipe.setAdapter(actionsAdapter);
       mRibbonLongSwipe.post(new Runnable() {
            public void run() {
                mRibbonLongSwipe.setOnItemSelectedListener(new RibbonLongSwipeListener());
            }
        });

       mRibbonLongPressText = ((TextView) ll.findViewById(R.id.ribbon_long_press_id));
       mRibbonLongPress = (Spinner) ll.findViewById(R.id.ribbon_long_press);
       mRibbonLongPress.setAdapter(actionsAdapter);
       mRibbonLongPress.post(new Runnable() {
            public void run() {
                mRibbonLongPress.setOnItemSelectedListener(new RibbonLongPressListener());
            }
        });

       mRibbonDismissText = ((TextView) ll.findViewById(R.id.ribbon_dismiss_id));
       ArrayAdapter<CharSequence> dismissAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       dismissAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       final String[] dismissEntries = getResources().getStringArray(R.array.ribbon_dismiss_entries);
       for (int i = 0; i < dismissEntries.length ; i++) {
            dismissAdapter.add(dismissEntries[i]);
       }
       mRibbonDismiss = (Spinner) ll.findViewById(R.id.ribbon_dismiss);
       mRibbonDismiss.setAdapter(dismissAdapter);
       mRibbonDismiss.post(new Runnable() {
            public void run() {
                mRibbonDismiss.setOnItemSelectedListener(new RibbonDismissListener());
            }
        });

       mWindowColumnsText = ((TextView) ll.findViewById(R.id.window_columns_id));
       mWindowColumns = (Spinner) ll.findViewById(R.id.window_columns);
       ArrayAdapter<CharSequence> columnsAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       columnsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       final String[] columnsEntries = getResources().getStringArray(R.array.window_columns_entries);
       for (int i = 0; i < columnsEntries.length ; i++) {
            columnsAdapter.add(columnsEntries[i]);
       }
        mWindowColumns.setAdapter(columnsAdapter);
        mWindowColumns.post(new Runnable() {
            public void run() {
                mWindowColumns.setOnItemSelectedListener(new ColumnsListener());
            }
        });

        final String[] columnsValues = getResources().getStringArray(R.array.window_columns_values);

        mWindowColumns.setSelection(Arrays.asList(columnsValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
                Settings.System.APP_WINDOW_COLUMNS, 5))));

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

        mRibbonHideImeText = ((TextView) ll.findViewById(R.id.ribbon_hide_ime_id));
        mRibbonHideIme = (Switch) ll.findViewById(R.id.ribbon_hide_ime);
        mRibbonHideIme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.RIBBON_HIDE_IME[ribbonNumber], checked);
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
        mEnableVibSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.SWIPE_RIBBON_VIBRATE[ribbonNumber], checked);
            }
        });

        mRibbonIconVibrateText = ((TextView) ll.findViewById(R.id.ribbon_icon_vibrate_id));
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

        mTogglesButtonText = ((TextView) ll.findViewById(R.id.ribbon_toggles_button_id));
        mTogglesButton = (Switch) ll.findViewById(R.id.ribbon_toggles_button);
        mTogglesButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(mContentRes, Settings.System.RIBBON_TOGGLE_BUTTON_LOCATION[ribbonNumber], checked);
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

       mRibbonAnimationText = ((TextView) ll.findViewById(R.id.ribbon_animation_type_id));
       mRibbonAnimation = (Spinner) ll.findViewById(R.id.ribbon_animation_type);
       ArrayAdapter<CharSequence> animAdapter = new ArrayAdapter<CharSequence>(
            getActivity(), android.R.layout.simple_spinner_item);
       animAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       for (int i = 0; i < mAnimationsStrings.length ; i++) {
            animAdapter.add(mAnimationsStrings[i]);
       }
        mRibbonAnimation.setAdapter(animAdapter);
        mRibbonAnimation.post(new Runnable() {
            public void run() {
                mRibbonAnimation.setOnItemSelectedListener(new AnimationListener());
            }
        });

       mIconLocationText = ((TextView) ll.findViewById(R.id.ribbon_icon_location_id));
       mIconLocation = (Spinner) ll.findViewById(R.id.ribbon_icon_location);
       mIconLocation.setAdapter(locAdapter);
       mIconLocation.post(new Runnable() {
            public void run() {
                mIconLocation.setOnItemSelectedListener(new IconLocationListener());
            }
        });

       mAppWindowSpaceText = ((TextView) ll.findViewById(R.id.app_window_space_id));
       mDragHandleOpacityText = ((TextView) ll.findViewById(R.id.drag_handle_opacity_id));
       mRibbonOpacityText = ((TextView) ll.findViewById(R.id.ribbon_opacity_id));
       mRibbonAnimDurText = ((TextView) ll.findViewById(R.id.ribbon_animation_duration_id));
       mWindowAnimDurText = ((TextView) ll.findViewById(R.id.ribbon_animation_duration_app_id));
       mDragHandleWidthText = ((TextView) ll.findViewById(R.id.drag_handle_width_id));
       mDragHandleHeightText = ((TextView) ll.findViewById(R.id.drag_handle_height_id));
       mRibbonIconSpaceText = ((TextView) ll.findViewById(R.id.ribbon_icon_space_id));
       mDragHandleOpacity = (SeekBar) ll.findViewById(R.id.drag_handle_opacity);
       mAppWindowSpace = (SeekBar) ll.findViewById(R.id.app_window_space);
       mRibbonOpacity = (SeekBar) ll.findViewById(R.id.ribbon_opacity);
       mRibbonAnimDur = (SeekBar) ll.findViewById(R.id.ribbon_animation_duration);
       mWindowAnimDur = (SeekBar) ll.findViewById(R.id.ribbon_animation_duration_app);
       mDragHandleWidth = (SeekBar) ll.findViewById(R.id.drag_handle_width);
       mDragHandleHeight = (SeekBar) ll.findViewById(R.id.drag_handle_height);
       mRibbonIconSpace = (SeekBar) ll.findViewById(R.id.ribbon_icon_space);
       mAppWindowSpace.setOnSeekBarChangeListener(this);
       mDragHandleOpacity.setOnSeekBarChangeListener(this);
       mRibbonOpacity.setOnSeekBarChangeListener(this);
       mRibbonAnimDur.setOnSeekBarChangeListener(this);
       mWindowAnimDur.setOnSeekBarChangeListener(this);
       mDragHandleWidth.setOnSeekBarChangeListener(this);
       mDragHandleHeight.setOnSeekBarChangeListener(this);
       mRibbonIconSpace.setOnSeekBarChangeListener(this);
       mWindowOpacityText = ((TextView) ll.findViewById(R.id.window_opacity_id));
       mWindowOpacity = (SeekBar) ll.findViewById(R.id.window_opacity);
       mWindowOpacity.setOnSeekBarChangeListener(this);
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

    public class RibbonDismissListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String[] values = getResources().getStringArray(R.array.ribbon_dismiss_values);
            int temp = Integer.parseInt((String) values[pos]);
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DISMISS[ribbonNumber], temp);
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class RibbonLongSwipeListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String temp = mActionCodes[pos];
            if (temp.equals(DialogConstant.CUSTOM_APP.value())) {
                mChoice = 2;
                mPicker.pickShortcut();
            } else {
                Settings.System.putString(mContentRes, Settings.System.RIBBON_LONG_SWIPE[ribbonNumber], temp);
            }
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class RibbonLongPressListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String temp = mActionCodes[pos];
            if (temp.equals(DialogConstant.CUSTOM_APP.value())) {
                mChoice = 3;
                mPicker.pickShortcut();
            } else {
                Settings.System.putString(mContentRes, Settings.System.RIBBON_LONG_PRESS[ribbonNumber], temp);
            }
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class ColumnsListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String[] values = getResources().getStringArray(R.array.window_columns_values);
            int tempColumns = Integer.parseInt((String) values[pos]);
            Settings.System.putInt(mContentRes, Settings.System.APP_WINDOW_COLUMNS, tempColumns);
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class AnimationListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            int temp = mAnimations[pos];
            if (arrayNum < 10) {
                Settings.System.putInt(mContentRes, Settings.System.RIBBON_ANIMATION_TYPE[ribbonNumber], temp);
            } else {
                Settings.System.putInt(mContentRes, Settings.System.APP_WINDOW_ANIMATION_TYPE, temp);
            }
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
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_LOCATION[ribbonNumber], tempLoc);
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
        mMenuRearrange = menu.findItem(R.id.rearrange);
        mMenuReset = menu.findItem(R.id.reset);
        mMenuToggles = menu.findItem(R.id.ribbon_toggles);
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
                if (arrayNum < 10) {
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
                } else {
                    showHideAppsDialog();
                }
                return true;
            case R.id.ribbon_toggles:
                if (arrayNum == 2 || arrayNum == 4) {
                    allTogglesStrings.clear();
                    for (int i = 0; i < allToggles.size(); i++) {
                        allTogglesStrings.add(lookupToggle(allToggles.get(i)));
                    }
                    ArrayList<String> mToggles = Settings.System.getArrayList(mContentRes, Settings.System.SWIPE_RIBBON_TOGGLES[ribbonNumber]);
                    ArrangeRibbonTogglesFragment fragment = new ArrangeRibbonTogglesFragment();
                    fragment.setResources(mContext, mContentRes, allToggles, allTogglesStrings,
                        mToggles, ribbonNumber);
                    fragment.show(getFragmentManager(), "toggles");
                } else {
                    Toast.makeText(mContext, R.string.menu_ribbon_toggles_error, Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private String lookupToggle(String ident) {
        if (sToggles != null) {
            return sToggles.getString(ident.toUpperCase());
        }
        return ident;
    }

    private void onTogglesUpdate(Bundle toggleInfo) {
        allToggles.clear();
        sToggles.clear();
        allToggles = toggleInfo.getStringArrayList("toggles");
        sToggles = toggleInfo;
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

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
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
            mMenuRearrange.setTitle(getResources().getString(R.string.menu_ribbon_rearrange));
            mMenuReset.setTitle(getResources().getString(R.string.menu_ribbon_reset));
            mMenuToggles.setTitle(getResources().getString(R.string.menu_ribbon_na));
            if (hasNavBarByDefault || navBarEnabled) {
                mEnableBottomWarning.setVisibility(View.VISIBLE);
            } else {
                mEnableBottomWarning.setVisibility(View.GONE);
            }
            mRibbonHideImeText.setVisibility(View.VISIBLE);
            mRibbonHideIme.setVisibility(View.VISIBLE);
            mRibbonAnimDurText.setVisibility(View.VISIBLE);
            mRibbonAnimDur.setVisibility(View.VISIBLE);
            mRibbonAnimationText.setVisibility(View.VISIBLE);
            mRibbonAnimation.setVisibility(View.VISIBLE);
            mRibbonDismissText.setVisibility(View.VISIBLE);
            mRibbonDismiss.setVisibility(View.VISIBLE);
            mTogglesButtonText.setVisibility(View.GONE);
            mTogglesButton.setVisibility(View.GONE);
            mRibbonLongSwipeText.setVisibility(View.VISIBLE);
            mRibbonLongSwipe.setVisibility(View.VISIBLE);
            mRibbonLongPressText.setVisibility(View.VISIBLE);
            mRibbonLongPress.setVisibility(View.VISIBLE);
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
            mLocationText.setVisibility(View.GONE);
            mLocation.setVisibility(View.GONE);
            mIconLocationText.setVisibility(View.GONE);
            mIconLocation.setVisibility(View.GONE);
            mTimeOutText.setVisibility(View.VISIBLE);
            mEnableVib.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mEnableVibSwitch.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mButtonInstructions.setVisibility(View.VISIBLE);
            mCommandButtonsCon.setVisibility(View.VISIBLE);
            llbuttons.setVisibility(View.VISIBLE);
            mRibbonIconVibrateText.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mRibbonIconVibrate.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mTextColorText.setVisibility(View.VISIBLE);
            mTextColor.setVisibility(View.VISIBLE);
            mIconSizeText.setVisibility(View.VISIBLE);
            mIconSize.setVisibility(View.VISIBLE);
            mRibbonIconSpaceText.setVisibility(View.VISIBLE);
            mRibbonIconSpace.setVisibility(View.VISIBLE);
            mEnableTextSwitch.setVisibility(View.VISIBLE);
            mEnableText.setVisibility(View.VISIBLE);
            mButtonColorizeText.setVisibility(View.VISIBLE);
            mButtonColorize.setVisibility(View.VISIBLE);
            mWindowColorText.setVisibility(View.GONE);
            mWindowColor.setVisibility(View.GONE);
            mTextWindowColorText.setVisibility(View.GONE);
            mTextWindowColor.setVisibility(View.GONE);
            mWindowOpacityText.setVisibility(View.GONE);
            mWindowOpacity.setVisibility(View.GONE);
            mWindowColumnsText.setVisibility(View.GONE);
            mWindowColumns.setVisibility(View.GONE);
            mWindowAnimDurText.setVisibility(View.GONE);
            mWindowAnimDur.setVisibility(View.GONE);
            mAppWindowSpaceText.setVisibility(View.GONE);
            mAppWindowSpace.setVisibility(View.GONE);
            break;
        case 4:
            mMenuRearrange.setTitle(getResources().getString(R.string.menu_ribbon_rearrange));
            mMenuReset.setTitle(getResources().getString(R.string.menu_ribbon_reset));
            mMenuToggles.setTitle(getResources().getString(R.string.menu_ribbon_toggles));
            mRibbonHideImeText.setVisibility(View.VISIBLE);
            mRibbonHideIme.setVisibility(View.VISIBLE);
            mTogglesButtonText.setVisibility(View.VISIBLE);
            mTogglesButton.setVisibility(View.VISIBLE);
            mRibbonAnimDurText.setVisibility(View.VISIBLE);
            mRibbonAnimDur.setVisibility(View.VISIBLE);
            mRibbonAnimationText.setVisibility(View.VISIBLE);
            mRibbonAnimation.setVisibility(View.VISIBLE);
            mRibbonDismissText.setVisibility(View.VISIBLE);
            mRibbonDismiss.setVisibility(View.VISIBLE);
            mRibbonLongSwipeText.setVisibility(View.VISIBLE);
            mRibbonLongSwipe.setVisibility(View.VISIBLE);
            mRibbonLongPressText.setVisibility(View.VISIBLE);
            mRibbonLongPress.setVisibility(View.VISIBLE);
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
            mEnableVib.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mEnableVibSwitch.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mButtonInstructions.setVisibility(View.VISIBLE);
            mCommandButtonsCon.setVisibility(View.VISIBLE);
            llbuttons.setVisibility(View.VISIBLE);
            mRibbonIconVibrateText.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mRibbonIconVibrate.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mTextColorText.setVisibility(View.VISIBLE);
            mTextColor.setVisibility(View.VISIBLE);
            mIconSizeText.setVisibility(View.VISIBLE);
            mIconSize.setVisibility(View.VISIBLE);
            mRibbonIconSpaceText.setVisibility(View.VISIBLE);
            mRibbonIconSpace.setVisibility(View.VISIBLE);
            mEnableTextSwitch.setVisibility(View.VISIBLE);
            mEnableText.setVisibility(View.VISIBLE);
            mButtonColorizeText.setVisibility(View.VISIBLE);
            mButtonColorize.setVisibility(View.VISIBLE);
            mWindowColorText.setVisibility(View.GONE);
            mWindowColor.setVisibility(View.GONE);
            mTextWindowColorText.setVisibility(View.GONE);
            mTextWindowColor.setVisibility(View.GONE);
            mWindowOpacityText.setVisibility(View.GONE);
            mWindowOpacity.setVisibility(View.GONE);
            mWindowColumnsText.setVisibility(View.GONE);
            mWindowColumns.setVisibility(View.GONE);
            mWindowAnimDurText.setVisibility(View.GONE);
            mWindowAnimDur.setVisibility(View.GONE);
            mAppWindowSpaceText.setVisibility(View.GONE);
            mAppWindowSpace.setVisibility(View.GONE);
            break;
        case 2:
            mMenuRearrange.setTitle(getResources().getString(R.string.menu_ribbon_rearrange));
            mMenuReset.setTitle(getResources().getString(R.string.menu_ribbon_reset));
            mMenuToggles.setTitle(getResources().getString(R.string.menu_ribbon_toggles));
            mRibbonHideImeText.setVisibility(View.VISIBLE);
            mRibbonHideIme.setVisibility(View.VISIBLE);
            mTogglesButtonText.setVisibility(View.VISIBLE);
            mTogglesButton.setVisibility(View.VISIBLE);
            mRibbonAnimDurText.setVisibility(View.VISIBLE);
            mRibbonAnimDur.setVisibility(View.VISIBLE);
            mRibbonAnimationText.setVisibility(View.VISIBLE);
            mRibbonAnimation.setVisibility(View.VISIBLE);
            mRibbonDismissText.setVisibility(View.VISIBLE);
            mRibbonDismiss.setVisibility(View.VISIBLE);
            mRibbonLongSwipeText.setVisibility(View.VISIBLE);
            mRibbonLongSwipe.setVisibility(View.VISIBLE);
            mRibbonLongPressText.setVisibility(View.VISIBLE);
            mRibbonLongPress.setVisibility(View.VISIBLE);
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
            mEnableVib.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mEnableVibSwitch.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mButtonInstructions.setVisibility(View.VISIBLE);
            mCommandButtonsCon.setVisibility(View.VISIBLE);
            llbuttons.setVisibility(View.VISIBLE);
            mRibbonIconVibrateText.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mRibbonIconVibrate.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mTextColorText.setVisibility(View.VISIBLE);
            mTextColor.setVisibility(View.VISIBLE);
            mIconSizeText.setVisibility(View.VISIBLE);
            mIconSize.setVisibility(View.VISIBLE);
            mRibbonIconSpaceText.setVisibility(View.VISIBLE);
            mRibbonIconSpace.setVisibility(View.VISIBLE);
            mEnableTextSwitch.setVisibility(View.VISIBLE);
            mEnableText.setVisibility(View.VISIBLE);
            mButtonColorizeText.setVisibility(View.VISIBLE);
            mButtonColorize.setVisibility(View.VISIBLE);
            mWindowColorText.setVisibility(View.GONE);
            mWindowColor.setVisibility(View.GONE);
            mTextWindowColorText.setVisibility(View.GONE);
            mTextWindowColor.setVisibility(View.GONE);
            mWindowOpacityText.setVisibility(View.GONE);
            mWindowOpacity.setVisibility(View.GONE);
            mWindowColumnsText.setVisibility(View.GONE);
            mWindowColumns.setVisibility(View.GONE);
            mWindowAnimDurText.setVisibility(View.GONE);
            mWindowAnimDur.setVisibility(View.GONE);
            mAppWindowSpaceText.setVisibility(View.GONE);
            mAppWindowSpace.setVisibility(View.GONE);
            break;
        case 10 :
            mMenuRearrange.setTitle(getResources().getString(R.string.menu_ribbon_hide));
            mMenuReset.setTitle(getResources().getString(R.string.menu_ribbon_apps));
            mMenuToggles.setTitle(getResources().getString(R.string.menu_ribbon_na));
            mRibbonHideImeText.setVisibility(View.GONE);
            mRibbonHideIme.setVisibility(View.GONE);
            mTogglesButtonText.setVisibility(View.GONE);
            mTogglesButton.setVisibility(View.GONE);
            mRibbonAnimDurText.setVisibility(View.GONE);
            mRibbonAnimDur.setVisibility(View.GONE);
            mRibbonAnimationText.setVisibility(View.VISIBLE);
            mRibbonAnimation.setVisibility(View.VISIBLE);
            mRibbonDismissText.setVisibility(View.GONE);
            mRibbonDismiss.setVisibility(View.GONE);
            mRibbonLongSwipeText.setVisibility(View.GONE);
            mRibbonLongSwipe.setVisibility(View.GONE);
            mRibbonLongPressText.setVisibility(View.GONE);
            mRibbonLongPress.setVisibility(View.GONE);
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
            //mButtonContainer.setVisibility(View.GONE);
            mButtonInstructions.setVisibility(View.GONE);
            mCommandButtonsCon.setVisibility(View.GONE);
            llbuttons.setVisibility(View.GONE);
            mRibbonIconVibrateText.setVisibility(View.GONE);
            mRibbonIconVibrate.setVisibility(View.GONE);
            mTextColorText.setVisibility(View.GONE);
            mTextColor.setVisibility(View.GONE);
            mIconSizeText.setVisibility(View.GONE);
            mIconSize.setVisibility(View.GONE);
            mRibbonIconSpaceText.setVisibility(View.GONE);
            mRibbonIconSpace.setVisibility(View.GONE);
            mEnableTextSwitch.setVisibility(View.GONE);
            mEnableText.setVisibility(View.GONE);
            mButtonColorizeText.setVisibility(View.GONE);
            mButtonColorize.setVisibility(View.GONE);
            mWindowColorText.setVisibility(View.VISIBLE);
            mWindowColor.setVisibility(View.VISIBLE);
            mTextWindowColorText.setVisibility(View.VISIBLE);
            mTextWindowColor.setVisibility(View.VISIBLE);
            mWindowOpacityText.setVisibility(View.VISIBLE);
            mWindowOpacity.setVisibility(View.VISIBLE);
            mWindowColumnsText.setVisibility(View.VISIBLE);
            mWindowColumns.setVisibility(View.VISIBLE);
            mWindowAnimDurText.setVisibility(View.VISIBLE);
            mWindowAnimDur.setVisibility(View.VISIBLE);
            mAppWindowSpaceText.setVisibility(View.VISIBLE);
            mAppWindowSpace.setVisibility(View.VISIBLE);
            break;
        default :
            if (mMenuRearrange != null) {
                mMenuRearrange.setTitle(getResources().getString(R.string.menu_ribbon_rearrange));
                mMenuReset.setTitle(getResources().getString(R.string.menu_ribbon_reset));
                mMenuToggles.setTitle(getResources().getString(R.string.menu_ribbon_na));
            }
            mRibbonHideImeText.setVisibility(View.GONE);
            mRibbonHideIme.setVisibility(View.GONE);
            mTogglesButtonText.setVisibility(View.GONE);
            mTogglesButton.setVisibility(View.GONE);
            mRibbonAnimDurText.setVisibility(View.GONE);
            mRibbonAnimDur.setVisibility(View.GONE);
            mRibbonAnimationText.setVisibility(View.GONE);
            mRibbonAnimation.setVisibility(View.GONE);
            mRibbonDismissText.setVisibility(View.GONE);
            mRibbonDismiss.setVisibility(View.GONE);
            mRibbonLongSwipeText.setVisibility(View.GONE);
            mRibbonLongSwipe.setVisibility(View.GONE);
            mRibbonLongPressText.setVisibility(View.GONE);
            mRibbonLongPress.setVisibility(View.GONE);
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
            mButtonInstructions.setVisibility(View.VISIBLE);
            mCommandButtonsCon.setVisibility(View.VISIBLE);
            llbuttons.setVisibility(View.VISIBLE);
            mRibbonIconVibrateText.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mRibbonIconVibrate.setVisibility(hasVibration ? View.VISIBLE : View.GONE);
            mTextColorText.setVisibility(View.VISIBLE);
            mTextColor.setVisibility(View.VISIBLE);
            mIconSizeText.setVisibility(View.VISIBLE);
            mIconSize.setVisibility(View.VISIBLE);
            mRibbonIconSpaceText.setVisibility(View.VISIBLE);
            mRibbonIconSpace.setVisibility(View.VISIBLE);
            mEnableTextSwitch.setVisibility(View.VISIBLE);
            mEnableText.setVisibility(View.VISIBLE);
            mButtonColorizeText.setVisibility(View.VISIBLE);
            mButtonColorize.setVisibility(View.VISIBLE);
            mWindowColorText.setVisibility(View.GONE);
            mWindowColor.setVisibility(View.GONE);
            mTextWindowColorText.setVisibility(View.GONE);
            mTextWindowColor.setVisibility(View.GONE);
            mWindowOpacityText.setVisibility(View.GONE);
            mWindowOpacity.setVisibility(View.GONE);
            mWindowColumnsText.setVisibility(View.GONE);
            mWindowColumns.setVisibility(View.GONE);
            mWindowAnimDurText.setVisibility(View.GONE);
            mWindowAnimDur.setVisibility(View.GONE);
            mAppWindowSpaceText.setVisibility(View.GONE);
            mAppWindowSpace.setVisibility(View.GONE);
            break;
        }

    }

    public void resetRibbon() {
        if (arrayNum < 10) {
            Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum], "");
            Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum], "");
            Settings.System.putBoolean(mContentRes, Settings.System.ENABLE_RIBBON_TEXT[arrayNum], true);
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SIZE[arrayNum], 0);
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_TEXT_COLOR[arrayNum], -1);
            Settings.System.putString(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[arrayNum], "");
        } else {
            Settings.System.putString(mContentRes, Settings.System.APP_WINDOW_HIDDEN_APPS, "");
        }
    }

    public void setupButtons() {
        if (arrayNum < 10) {   
            getRibbonNumber();
            updateSwitches();
            mShortTargets.clear();
            mLongTargets.clear();
            mCustomIcons.clear();
            mShortTargets = Settings.System.getArrayList(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum]);
            mLongTargets = Settings.System.getArrayList(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum]);
            mCustomIcons = Settings.System.getArrayList(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[arrayNum]);

            mRibbonIconSpace.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[arrayNum], 5));
            mRibbonAnimDur.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_ANIMATION_DURATION[ribbonNumber], 50));
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

            final String[] dismissValues = getResources().getStringArray(R.array.ribbon_dismiss_values);
            mRibbonDismiss.setSelection(Arrays.asList(dismissValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
                    Settings.System.RIBBON_DISMISS[ribbonNumber], 1))));

            mRibbonLongSwipe.setSelection(Arrays.asList(mActionCodes).indexOf(Settings.System.getString(mContentRes,
                    Settings.System.RIBBON_LONG_SWIPE[ribbonNumber])));

            mRibbonLongPress.setSelection(Arrays.asList(mActionCodes).indexOf(Settings.System.getString(mContentRes,
                    Settings.System.RIBBON_LONG_PRESS[ribbonNumber])));

            mTogglesButton.setChecked(Settings.System.getBoolean(mContentRes,
                    Settings.System.RIBBON_TOGGLE_BUTTON_LOCATION[ribbonNumber], false));

            mRibbonHideIme.setChecked(Settings.System.getBoolean(mContentRes,
                    Settings.System.RIBBON_HIDE_IME[ribbonNumber], false));

            mRibbonAnimation.setSelection(mAnimations[Settings.System.getInt(mContentRes,
                    Settings.System.RIBBON_ANIMATION_TYPE[ribbonNumber], 0)]);

            final String[] locValues = getResources().getStringArray(R.array.ribbon_handle_location_values);
            if (ribbonNumber < 2) {
                mIconLocation.setSelection(Arrays.asList(locValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes,
                    Settings.System.RIBBON_ICON_LOCATION[ribbonNumber], 0))));
            }
            mLocation.setSelection(Arrays.asList(locValues).indexOf(String.valueOf(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_LOCATION[ribbonNumber], 0))));
            mRibbonOpacity.setProgress(Settings.System.getInt(mContentRes, Settings.System.SWIPE_RIBBON_OPACITY[ribbonNumber], 100));
            mDragHandleOpacity.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_OPACITY[ribbonNumber], 0));
            mDragHandleWidth.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_WEIGHT[ribbonNumber], 30));
            mDragHandleHeight.setProgress(Settings.System.getInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_HEIGHT[ribbonNumber], 50));
            mEnableVibSwitch.setChecked(Settings.System.getBoolean(mContentRes, Settings.System.SWIPE_RIBBON_VIBRATE[ribbonNumber], false));
        } else {
            updateSwitches();
            windowColor = Settings.System.getInt(mContentRes,
                    Settings.System.APP_WINDOW_COLOR_BG, Color.BLACK);
            mWindowColor.setBackgroundColor(windowColor);
            windowTextColor = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.APP_WINDOW_COLOR_TEXT, Color.CYAN);
            mTextWindowColor.setBackgroundColor(windowTextColor);
            mAppWindowSpace.setProgress(Settings.System.getInt(mContentRes, Settings.System.APP_WINDOW_SPACING, 5));
            mWindowOpacity.setProgress(Settings.System.getInt(mContentRes, Settings.System.APP_WINDOW_OPACITY, 100));
            mWindowAnimDur.setProgress(Settings.System.getInt(mContentRes, Settings.System.APP_WINDOW_ANIMATION_DURATION, 75));
            mRibbonAnimation.setSelection(mAnimations[Settings.System.getInt(mContentRes,
                    Settings.System.APP_WINDOW_ANIMATION_TYPE, 0)]);
        }
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
            mChoice = 0;
            createDialog(
                getResources().getString(R.string.choose_action_title),
                mActions, mActionCodes);
            break;
        case LONG_ACTION:
            mChoice = 1;
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
            if (mChoice == 1) {
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
                int tempSpace = Settings.System.getInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[tempInt], 5);
                Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[arrayNum], tempSpace);
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
                    showMultiSelectDialog();
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
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_OPACITY[ribbonNumber], progress);
        } else if (seekBar == mRibbonOpacity) {
            Settings.System.putInt(mContentRes, Settings.System.SWIPE_RIBBON_OPACITY[ribbonNumber], progress);
        } else if (seekBar == mWindowOpacity) {
            Settings.System.putInt(mContentRes, Settings.System.APP_WINDOW_OPACITY, progress);
        } else if (seekBar == mDragHandleWidth) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_WEIGHT[ribbonNumber], progress);
        } else if (seekBar == mDragHandleHeight) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_DRAG_HANDLE_HEIGHT[ribbonNumber], progress);
        } else if (seekBar == mRibbonIconSpace) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_ICON_SPACE[arrayNum], progress);
        } else if (seekBar == mRibbonAnimDur) {
            Settings.System.putInt(mContentRes, Settings.System.RIBBON_ANIMATION_DURATION[ribbonNumber], progress);
        } else if (seekBar == mWindowAnimDur) {
            Settings.System.putInt(mContentRes, Settings.System.APP_WINDOW_ANIMATION_DURATION, progress);
        } else if (seekBar == mAppWindowSpace) {
            Settings.System.putInt(mContentRes, Settings.System.APP_WINDOW_SPACING, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void showMultiSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mGoodName.clear();
        mSelectedApps.clear();
        mApps.clear();
        ArrayList<String> apps = new ArrayList<String>();
        PackageManager pm = mContext.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < packs.size(); i++) {
            ResolveInfo p = packs.get(i);
            ActivityInfo activity = p.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
            Intent intent = new Intent(Intent.ACTION_MAIN);

            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setComponent(name);
            if (intent != null) {
                apps.add(intent.toUri(0));
            }
        }

        for (int i = 0; i < apps.size(); i++) {
            mGoodName.add(NavBarHelpers.getProperSummary(mContext, apps.get(i)));
        }

        for (int i = 0; i < mGoodName.size(); i++) {
            mApps.add(mGoodName.get(i));
        }

        Collections.sort(mApps, String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < mApps.size(); i++) {
            int j = mGoodName.indexOf(mApps.get(i));
            mApps.set(i, apps.get(j));
        }
        Collections.sort(mGoodName, String.CASE_INSENSITIVE_ORDER);

       // mActions;
       // mActionCodes;
        for (int i = (mActions.length - 3); i > -1; i--) {
            mApps.add(0, mActionCodes[i]);
            mGoodName.add(0, mActions[i]);
        }

        // build arrays for dialog
        final String itemStrings[] = new String[mGoodName.size()];
        final boolean checkedItems[] = new boolean[mGoodName.size()];

        // set strings
        for (int i = 0; i < itemStrings.length; i++) {
            itemStrings[i] = mGoodName.get(i);
        }

        // check hidden apps
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = false;
        }

        builder.setTitle(R.string.multi_select_title);
        builder.setCancelable(true);
        builder.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                for (int i = 0; i < mSelectedApps.size(); i++) {
                    mShortTargets.add(mSelectedApps.get(i));
                    mLongTargets.add("**null**");
                    mCustomIcons.add("**null**");
                }
                refreshButtons();
            }
        });
        builder.setPositiveButton(R.string.toggles_display_close, null);
        builder.setMultiChoiceItems(itemStrings, checkedItems,
                new OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String toggleKey = mApps.get(which);
                        if (isChecked)
                            mSelectedApps.add(toggleKey);
                        else
                            mSelectedApps.remove(toggleKey);
                    }
                });
        AlertDialog d = builder.create();
        d.show();
    }

    private void showHideAppsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mGoodName.clear();
        mHiddenApps.clear();
        mHiddenApps = Settings.System.getArrayList(
            mContentRes, Settings.System.APP_WINDOW_HIDDEN_APPS);
        ArrayList<String> apps = new ArrayList<String>();
        PackageManager pm = mContext.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, 0);
        for (int i = 0; i < packs.size(); i++) {
            ResolveInfo p = packs.get(i);
            ActivityInfo activity = p.activityInfo;
            ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
            Intent intent = new Intent(Intent.ACTION_MAIN);

            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setComponent(name);
            if (intent != null) {
                apps.add(intent.toUri(0));
            }
        }

        for (int i = 0; i < apps.size(); i++) {
            mGoodName.add(NavBarHelpers.getProperSummary(mContext, apps.get(i)));
        }
        Collections.sort(mGoodName, String.CASE_INSENSITIVE_ORDER);


        // build arrays for dialog
        final String itemStrings[] = new String[mGoodName.size()];
        final boolean checkedItems[] = new boolean[mGoodName.size()];

        // set strings
        for (int i = 0; i < itemStrings.length; i++) {
            itemStrings[i] = mGoodName.get(i);
        }

        // check hidden apps
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = mHiddenApps.contains(itemStrings[i]);
        }

        builder.setTitle(R.string.window_app_hide_title);
        builder.setCancelable(true);
        builder.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                Settings.System.putArrayList(mContentRes, Settings.System.APP_WINDOW_HIDDEN_APPS,
                    mHiddenApps);
            }
        });
        builder.setPositiveButton(R.string.toggles_display_close, null);
        builder.setMultiChoiceItems(itemStrings, checkedItems,
                new OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String toggleKey = mGoodName.get(which);
                        if (isChecked)
                            mHiddenApps.add(toggleKey);
                        else
                            mHiddenApps.remove(toggleKey);
                    }
                });
        AlertDialog d = builder.create();
        d.show();
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        switch (mChoice) {
            case 0:
                mShortTargets.set(mTargetNum, uri);
                break;
            case 1:
                mLongTargets.set(mTargetNum, uri);
                break;
            case 2:
                Settings.System.putString(mContentRes,
                    Settings.System.RIBBON_LONG_SWIPE[ribbonNumber], uri);
                break;
            case 3:
                Settings.System.putString(mContentRes,
                    Settings.System.RIBBON_LONG_PRESS[ribbonNumber], uri);
                break;
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
        case 2:
            Settings.System.putInt(mContentRes,
                    Settings.System.APP_WINDOW_COLOR_BG, color);
            windowColor = color;
            mWindowColor.setBackgroundColor(windowColor);
            break;
        case 3:
            Settings.System.putInt(mContentRes,
                    Settings.System.APP_WINDOW_COLOR_TEXT, color);
            windowTextColor = color;
            mTextWindowColor.setBackgroundColor(windowTextColor);
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
