/*
 * Copyright (C) 2013 Android Open Kang Project
 * Copyright (C) 2013 The Cyanogenmod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavBarHelpers;
import com.android.internal.util.aokp.NavRingHelpers;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.TargetDrawable;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.android.internal.util.aokp.AwesomeConstants.ASSIST_ICON_METADATA_NAME;
import static com.android.internal.util.aokp.AwesomeConstants.AwesomeConstant;

public class NavRingTargets extends Fragment implements
        ShortcutPickerHelper.OnPickListener, GlowPadView.OnTriggerListener,
        OnSettingChangedListener {
    private static final String TAG = "NavRing";
    private static final boolean DEBUG = false;

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;

    private Context mContext;

    private ContentResolver cr;

    private GlowPadView mGlowPadView;
    private SingleChoiceSetting mRingAmount;

    private ShortcutPickerHelper mPicker;
    private String[] targetActivities = new String[5];
    private String[] longActivities = new String[5];
    private String[] customIcons = new String[5];
    private ViewGroup mContainer;

    private String[] mActions;
    private String[] mActionCodes;

    private int mTargetIndex = 0;
    private int startPosOffset;
    private int endPosOffset;
    private int mNavRingAmount;
    private int mTarget = 0;

    public static enum DialogConstant {
        ICON_ACTION {
            @Override
            public String value() {
                return "**icon**";
            }
        },
        LONG_ACTION {
            @Override
            public String value() {
                return "**long**";
            }
        },
        SHORT_ACTION {
            @Override
            public String value() {
                return "**short**";
            }
        },
        CUSTOM_APP {
            @Override
            public String value() {
                return "**app**";
            }
        },
        NOT_IN_ENUM {
            @Override
            public String value() {
                return "**notinenum**";
            }
        };

        public String value() {
            return this.value();
        }
    }

    public static DialogConstant funcFromString(String string) {
        DialogConstant[] allTargs = DialogConstant.values();
        for (int i = 0; i < allTargs.length; i++) {
            if (string.equals(allTargs[i].value())) {
                return allTargs[i];
            }
        }
        // not in ENUM must be custom
        return DialogConstant.NOT_IN_ENUM;
    }

    private String mString;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;

    private ArrayList<Integer> intentList = new ArrayList<Integer>();
    private int intentCounter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContainer = container;
        setHasOptionsMenu(true);
        mContext = getActivity();
        cr = mContext.getContentResolver();

        // Get NavRing Actions
        mActionCodes = NavRingHelpers.getNavRingActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext,
                    mActionCodes[i]);
        }

        mPicker = new ShortcutPickerHelper(this, this);
        return inflater.inflate(R.layout.navigation_ring_targets, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGlowPadView = ((GlowPadView) getActivity().findViewById(
                R.id.navring_target));
        mGlowPadView.setOnTriggerListener(this);
        mRingAmount = (SingleChoiceSetting) getActivity().findViewById(
                R.id.ring_amount);
        mRingAmount.setOnSettingChangedListener(this);
        updateDrawables();
    }

    private void setDrawables() {
        final Context context = getActivity();
        intentCounter = 0;
        intentList.clear();

        // Custom Targets
        ArrayList<TargetDrawable> storedDraw = new ArrayList<TargetDrawable>();

        int endPosOffset = 0;
        int middleBlanks = 0;

        if (isScreenPortrait() || NavRingHelpers.isScreenLarge(getResources())) { // NavRing
                                                                                  // on
                                                                                  // Bottom
            startPosOffset = 1;
            endPosOffset = (mNavRingAmount) + 1;

        } else { // Right... (Ring actually on left side of tablet)
            startPosOffset = (Math.min(1, mNavRingAmount / 2)) + 2;
            endPosOffset = startPosOffset - 1;
        }

        int middleStart = mNavRingAmount;
        int tqty = middleStart;
        int middleFinish = 0;

        if (middleBlanks > 0) {
            middleStart = (tqty / 2) + (tqty % 2);
            middleFinish = (tqty / 2);
        }

        // Add Initial Place Holder Targets
        for (int i = 0; i < startPosOffset; i++) {
            intentList.add(-1);
            storedDraw.add(NavRingHelpers.getTargetDrawable(context, null));
        }
        // Add User Targets
        for (int i = 0; i < middleStart; i++) {
            TargetDrawable drawable;
            if (!TextUtils.isEmpty(customIcons[i])) {
                drawable = NavRingHelpers.getCustomDrawable(mContext,
                        customIcons[i]);
            } else {
                drawable = NavRingHelpers.getTargetDrawable(mContext,
                        targetActivities[i]);
            }
            drawable.setEnabled(true);
            storedDraw.add(drawable);
            intentList.add(intentCounter);
            intentCounter = intentCounter + 1;
        }

        // Add middle Place Holder Targets
        for (int j = 0; j < middleBlanks; j++) {
            intentList.add(-1);
            storedDraw.add(NavRingHelpers.getTargetDrawable(context, null));
        }

        // Add Rest of User Targets for leftys
        for (int j = 0; j < middleFinish; j++) {
            TargetDrawable drawable;
            int i = j + middleStart;
            if (!TextUtils.isEmpty(customIcons[i])) {
                drawable = NavRingHelpers.getCustomDrawable(mContext,
                        customIcons[i]);
            } else {
                drawable = NavRingHelpers.getTargetDrawable(mContext,
                        targetActivities[i]);
            }
            drawable.setEnabled(true);
            storedDraw.add(drawable);
            intentList.add(intentCounter);
            intentCounter = intentCounter + 1;
        }

        // Add End Place Holder Targets
        for (int i = 0; i < endPosOffset; i++) {
            intentList.add(-1);
            storedDraw.add(NavRingHelpers.getTargetDrawable(context, null));
        }

        mGlowPadView.setTargetResources(storedDraw);
        maybeSwapSearchIcon();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup)
                .setAlphabeticShortcut('r')
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_IF_ROOM
                                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetAll();
                return true;
            default:
                return false;
        }
    }

    /**
     * Resets the target layout to stock
     */
    private void resetAll() {
        final AlertDialog d = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.reset)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.navring_target_reset_message)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                for (int i = 0; i < 5; i++) {
                                    Settings.AOKP.putString(cr,
                                            Settings.AOKP.SYSTEMUI_NAVRING[i],
                                            null);
                                    Settings.AOKP
                                            .putString(
                                                    cr,
                                                    Settings.AOKP.SYSTEMUI_NAVRING_LONG[i],
                                                    null);
                                    Settings.AOKP
                                            .putString(
                                                    cr,
                                                    Settings.AOKP.SYSTEMUI_NAVRING_ICON[i],
                                                    null);

                                }
                                Settings.AOKP.putString(cr,
                                        Settings.AOKP.SYSTEMUI_NAVRING[0],
                                        AwesomeConstant.ACTION_ASSIST.value());
                                Settings.AOKP.putInt(cr,
                                        Settings.AOKP.SYSTEMUI_NAVRING_AMOUNT,
                                        1);
                                updateDrawables();
                                Toast.makeText(getActivity(),
                                        R.string.navring_target_reset,
                                        Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton(R.string.cancel, null).create();

        d.show();
    }

    /**
     * Save targets to settings provider
     */
    private void saveAll() {
        for (int i = 0; i < 5; i++) {
            Settings.AOKP.putString(cr, Settings.AOKP.SYSTEMUI_NAVRING[i],
                    targetActivities[i]);
            Settings.AOKP.putString(cr, Settings.AOKP.SYSTEMUI_NAVRING_LONG[i],
                    longActivities[i]);
            Settings.AOKP.putString(cr, Settings.AOKP.SYSTEMUI_NAVRING_ICON[i],
                    customIcons[i]);
        }
        updateDrawables();
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp,
            boolean isApplication) {
        switch (mTarget) {
            case 0:
                targetActivities[mTargetIndex] = uri;
                break;
            case 1:
                longActivities[mTargetIndex] = uri;
                Toast.makeText(
                        getActivity(),
                        AwesomeConstants.getProperName(mContext, uri)
                                + "  "
                                + getResources().getString(
                                        R.string.action_long_save),
                        Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        saveAll();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if ((requestCode == REQUEST_PICK_CUSTOM_ICON)
                    || (requestCode == REQUEST_PICK_LANDSCAPE_ICON)) {

                String iconName = getIconFileName(mTargetIndex);
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput(iconName,
                            Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                    return; // NOOOOO
                }

                Uri selectedImageUri = getTempFileUri();
                Bitmap bitmap;
                if (data != null) {
                    Uri mUri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                mContext.getContentResolver(), mUri);
                        Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                        resizedbitmap.compress(Bitmap.CompressFormat.PNG, 100,
                                iconStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.e(TAG,
                                "Selected image path: "
                                        + selectedImageUri.getPath());
                        bitmap = BitmapFactory.decodeFile(selectedImageUri
                                .getPath());
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                                iconStream);
                    } catch (NullPointerException npe) {
                        Log.e(TAG, npe.getMessage());
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                }
                customIcons[mTargetIndex] = Uri.fromFile(
                        new File(mContext.getFilesDir(), iconName)).getPath();

                File f = new File(selectedImageUri.getPath());
                if (f.exists()) {
                    f.delete();
                }

                Toast.makeText(
                        getActivity(),
                        mTargetIndex
                                + getResources().getString(
                                        R.string.custom_app_icon_successfully),
                        Toast.LENGTH_LONG).show();
                saveAll();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateDrawables() {
        for (int i = 0; i < 5; i++) {
            targetActivities[i] = Settings.AOKP.getString(cr,
                    Settings.AOKP.SYSTEMUI_NAVRING[i]);
            longActivities[i] = Settings.AOKP.getString(cr,
                    Settings.AOKP.SYSTEMUI_NAVRING_LONG[i]);
            customIcons[i] = Settings.AOKP.getString(cr,
                    Settings.AOKP.SYSTEMUI_NAVRING_ICON[i]);
        }

        mNavRingAmount = Settings.AOKP.getInt(cr,
                Settings.AOKP.SYSTEMUI_NAVRING_AMOUNT, 1);
        // Not using getBoolean here, because CURRENT_UI_MODE can be 0,1 or 2
        setDrawables();
    }

    public void onValueChange(String uri) {
        DialogConstant mFromString = funcFromString(uri);
        switch (mFromString) {
            case CUSTOM_APP:
                mPicker.pickShortcut();
                break;
            case SHORT_ACTION:
                mTarget = 0;
                mString = Settings.AOKP.SYSTEMUI_NAVRING[mTargetIndex];
                createDialog(
                        getResources()
                                .getString(R.string.choose_action_short_title),
                        mActions, mActionCodes);
                break;
            case LONG_ACTION:
                mTarget = 1;
                mString = Settings.AOKP.SYSTEMUI_NAVRING_LONG[mTargetIndex];
                createDialog(
                        getResources().getString(R.string.choose_action_long_title),
                        mActions, mActionCodes);
                break;
            case ICON_ACTION:
                int width = 85;
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
                intent.putExtra("outputFormat",
                        Bitmap.CompressFormat.PNG.toString());
                Log.i(TAG, "started for result, should output to: "
                        + getTempFileUri());
                startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON);
                break;
            case NOT_IN_ENUM:
                switch (mTarget) {
                    case 0:
                        targetActivities[mTargetIndex] = uri;
                        saveAll();
                        break;
                    case 1:
                        longActivities[mTargetIndex] = uri;
                        Toast.makeText(
                                getActivity(),
                                AwesomeConstants.getProperName(mContext, uri)
                                        + "  "
                                        + getResources().getString(
                                                R.string.action_long_save),
                                Toast.LENGTH_LONG).show();
                        saveAll();
                        break;
                    default:
                        break;
                }
                break;

        }
    }

    @Override
    public void onTrigger(View v, final int target) {
        mTargetIndex = intentList.get(target);
        final String[] stringArray = mContext.getResources().getStringArray(
                R.array.navring_long_dialog_entries);
        stringArray[0] = stringArray[0]
                + "  :  "
                + NavBarHelpers.getProperSummary(mContext,
                        targetActivities[mTargetIndex]);
        stringArray[1] = stringArray[1]
                + "  :  "
                + NavBarHelpers.getProperSummary(mContext,
                        longActivities[mTargetIndex]);
        createDialog(
                getResources().getString(R.string.choose_action_title),
                stringArray,
                getResources().getStringArray(
                        R.array.navring_long_dialog_values));
    }

    @Override
    public void onGrabbed(View v, int handle) {
    }

    @Override
    public void onReleased(View v, int handle) {
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
    }

    public void onTargetChange(View v, final int target) {
    }

    public void createDialog(final String title, final String[] entries,
            final String[] values) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onValueChange(values[item]);
                dialog.dismiss();
            }
        };

        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(title).setItems(entries, l).create();

        dialog.show();
    }

    private void maybeSwapSearchIcon() {
        final Context context = getActivity();
        Intent intent = ((SearchManager) context
                .getSystemService(Context.SEARCH_SERVICE)).getAssistIntent(
                context, true, UserHandle.USER_CURRENT);
        if (intent != null) {
            ComponentName component = intent.getComponent();
            if (component == null
                    || !mGlowPadView
                            .replaceTargetDrawablesIfPresent(
                                    component,
                                    ASSIST_ICON_METADATA_NAME,
                                    com.android.internal.R.drawable.ic_action_assist_generic)) {
                if (DEBUG) {
                    Log.v(TAG, "Couldn't grab icon for component " + component);
                }
            }
        }
    }

    public boolean isScreenPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mTargetIndex + ".png"));

    }

    private String getIconFileName(int index) {
        return "navring_icon_" + index + ".png";
    }

    private class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
            }
        }
    }

    private H mHandler = new H();

    @Override
    public void onFinishFinalAnimation() {
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue,
            String value) {
        updateDrawables();
    }
}
