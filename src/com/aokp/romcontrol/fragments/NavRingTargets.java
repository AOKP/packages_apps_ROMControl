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
import android.content.Intent.ShortcutIconResource;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Toast;

import static com.android.internal.util.aokp.AwesomeConstants.*;
import com.android.internal.util.aokp.NavRingHelpers;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.TargetDrawable;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.ROMControlActivity;
import android.text.TextUtils;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;

public class NavRingTargets extends Fragment implements
        ShortcutPickerHelper.OnPickListener, GlowPadView.OnTriggerListener {
    private static final String TAG = "NavRing";
    private static final boolean DEBUG = false;

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;

    private Context mContext;

    private GlowPadView mGlowPadView;
    private ShortcutPickerHelper mPicker;
    private String[] targetActivities = new String[5];
    private String[] longActivities = new String[5];
    private String[] customIcons = new String[5];
    private ViewGroup mContainer;

    private int mTargetIndex = 0;
    private int startPosOffset;
    private int endPosOffset;
    private int mNavRingAmount;
    private int mCurrentUIMode;
    private boolean mLefty;
    private boolean mBoolLongPress;
    private int mTarget = 0;

    private String CUSTOM_APP = "**app**";
    private String SHORT_ACTION = "**short**";
    private String LONG_ACTION = "**long**";
    private String ICON_ACTION = "**icon**";
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

        mPicker = new ShortcutPickerHelper(this, this);
        boolean tabletui = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CURRENT_UI_MODE, 0) == 1;
        return inflater.inflate(tabletui ? R.layout.navigation_ring_targets_tablet
                                : R.layout.navigation_ring_targets, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGlowPadView = ((GlowPadView) getActivity().findViewById(R.id.navring_target));
        mGlowPadView.setOnTriggerListener(this);
        updateDrawables();
    }

    private void setDrawables() {
        final Context context = getActivity();
        intentCounter = 0;

        // Custom Targets
        ArrayList<TargetDrawable> storedDraw = new ArrayList<TargetDrawable>();

        int endPosOffset = 0;
        int middleBlanks = 0;

        switch (mCurrentUIMode) {
            case 0 : // Phone Mode
                if (isScreenPortrait()) { // NavRing on Bottom
                    startPosOffset =  1;
                    endPosOffset =  (mNavRingAmount) + 1;
                } else if (mLefty) { // either lefty or... (Ring is actually on right side of screen)
                        startPosOffset =  1 - (mNavRingAmount % 2);
                        middleBlanks = mNavRingAmount + 2;
                        endPosOffset = 0;

                } else { // righty... (Ring actually on left side of tablet)
                    startPosOffset =  (Math.min(1,mNavRingAmount / 2)) + 2;
                    endPosOffset =  startPosOffset - 1;
                }
                break;
            case 1 : // Tablet Mode
                if (mLefty) { // either lefty or... (Ring is actually on right side of screen)
                    startPosOffset =  (mNavRingAmount) + 1;
                    endPosOffset =  (mNavRingAmount *2) + 1;
                } else { // righty... (Ring actually on left side of tablet)
                    startPosOffset =  1;
                    endPosOffset = (mNavRingAmount * 3) + 1;
                }
                break;
            case 2 : // Phablet Mode - Search Ring stays at bottom
                startPosOffset =  1;
                endPosOffset =  (mNavRingAmount) + 1;
                break;
         }

         int middleStart = mNavRingAmount;
         int tqty = middleStart;
         int middleFinish = 0;

         if (middleBlanks > 0) {
             middleStart = (tqty/2) + (tqty%2);
             middleFinish = (tqty/2);
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
                drawable = NavRingHelpers.getCustomDrawable(mContext, customIcons[i]);
            } else {
                drawable = NavRingHelpers.getTargetDrawable(mContext, targetActivities[i]);
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
                drawable = NavRingHelpers.getCustomDrawable(mContext, customIcons[i]);
            } else {
                drawable = NavRingHelpers.getTargetDrawable(mContext, targetActivities[i]);
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
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
            .setIcon(R.drawable.ic_settings_backup)
            .setAlphabeticShortcut('r')
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENU_SAVE, 0, R.string.wifi_save)
            .setIcon(R.drawable.ic_menu_save)
            .setAlphabeticShortcut('s')
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetAll();
                return true;
            case MENU_SAVE:
                saveAll();
                Toast.makeText(getActivity(), R.string.navring_target_save, Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    /**
     * Resets the target layout to stock
     */
    private void resetAll() {
        final ContentResolver cr = getActivity().getContentResolver();
        final AlertDialog d = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.lockscreen_target_reset_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.navring_target_reset_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < 5; i++) {
                            Settings.System.putString(cr,
                                Settings.System.SYSTEMUI_NAVRING[i], null);
                            Settings.System.putString(cr,
                                Settings.System.SYSTEMUI_NAVRING_LONG[i], null);
                            Settings.System.putString(cr,
                                Settings.System.SYSTEMUI_NAVRING_ICON[i], null);

                        }
                        Settings.System.putString(cr,
                                Settings.System.SYSTEMUI_NAVRING[0], ACTION_ASSIST);
                        Settings.System.putInt(cr,
                                Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1);
                        updateDrawables();
                        Toast.makeText(getActivity(),
                                R.string.navring_target_reset,
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        d.show();
    }

    /**
     * Save targets to settings provider
     */
    private void saveAll() {
        final ContentResolver cr = getActivity().getContentResolver();
        for (int i = 0; i < 5; i++) {
            Settings.System.putString(cr,
                    Settings.System.SYSTEMUI_NAVRING[i], targetActivities[i]);
            Settings.System.putString(cr,
                    Settings.System.SYSTEMUI_NAVRING_LONG[i], longActivities[i]);
            Settings.System.putString(cr,
                    Settings.System.SYSTEMUI_NAVRING_ICON[i], customIcons[i]);
        }
        updateDrawables();
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        switch (mTarget) {
            case 0:
                targetActivities[mTargetIndex] = uri;
                break;
            case 1:
                longActivities[mTargetIndex] = uri;
                Toast.makeText(getActivity(), getProperSummary(uri) + "  "
                     + getResources().getString(R.string.action_long_save),
                               Toast.LENGTH_LONG).show();
                break;
            default:
                break;
            }

        setDrawables();
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
                customIcons[mTargetIndex] = Uri.fromFile(new File(mContext.getFilesDir(), iconName)).getPath();

                File f = new File(selectedImageUri.getPath());
                if (f.exists())
                    f.delete();

                Toast.makeText(
                        getActivity(),
                        mTargetIndex
                                + getResources().getString(
                                        R.string.custom_app_icon_successfully),
                        Toast.LENGTH_LONG).show();
                setDrawables();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateDrawables() {
        for (int i = 0; i < 5; i++) {
             targetActivities[i] = Settings.System.getString(
                 getActivity().getContentResolver(), Settings.System.SYSTEMUI_NAVRING[i]);
             longActivities[i] = Settings.System.getString(
                 getActivity().getContentResolver(), Settings.System.SYSTEMUI_NAVRING_LONG[i]);
             customIcons[i] = Settings.System.getString(
                 getActivity().getContentResolver(), Settings.System.SYSTEMUI_NAVRING_ICON[i]);
        }
        mBoolLongPress = (Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE, false));

        mNavRingAmount = Settings.System.getInt(getActivity().getContentResolver(),
                         Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1);
        // Not using getBoolean here, because CURRENT_UI_MODE can be 0,1 or 2
        mCurrentUIMode = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CURRENT_UI_MODE, 0);
        setDrawables();
    }

    public void onValueChange(String uri) {
        if (uri.equals(CUSTOM_APP)) {
            mPicker.pickShortcut();
        } else if (uri.equals(SHORT_ACTION)) {
            mTarget = 0;
            mString = Settings.System.SYSTEMUI_NAVRING[mTargetIndex];
            createDialog(
                getResources().getString(R.string.choose_action_short_title),
                getResources().getStringArray(R.array.navring_dialog_entries),
                getResources().getStringArray(R.array.navring_dialog_values));
        } else if (uri.equals(LONG_ACTION)) {
            mTarget = 1;
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[mTargetIndex];
            createDialog(
                getResources().getString(R.string.choose_action_long_title),
                getResources().getStringArray(R.array.navring_dialog_entries),
                getResources().getStringArray(R.array.navring_dialog_values));
        } else if (uri.equals(ICON_ACTION)) {
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
        } else {
            switch (mTarget) {
                case 0:
                    targetActivities[mTargetIndex] = uri;
                    break;
                case 1:
                    longActivities[mTargetIndex] = uri;
                    Toast.makeText(getActivity(), getProperSummary(uri)
                         + "  " + getResources().getString(R.string.action_long_save),
                               Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
        setDrawables();
    }

    @Override
    public void onTrigger(View v, final int target) {
        mTargetIndex = intentList.get(target);
        if (mBoolLongPress) {
            final String[] stringArray = mContext.getResources().getStringArray(R.array.navring_long_dialog_entries);
            stringArray[0] = stringArray[0] + "  :  " + getProperSummary(targetActivities[mTargetIndex]);
            stringArray[1] = stringArray[1] + "  :  " + getProperSummary(longActivities[mTargetIndex]);
            createDialog(
                getResources().getString(R.string.choose_action_title), stringArray,
                getResources().getStringArray(R.array.navring_long_dialog_values));
        } else {
            final String[] stringArray = mContext.getResources().getStringArray(R.array.navring_short_dialog_entries);
            stringArray[0] = stringArray[0] + "  :  " + getProperSummary(targetActivities[mTargetIndex]);
            createDialog(
                getResources().getString(R.string.choose_action_title), stringArray,
                getResources().getStringArray(R.array.navring_short_dialog_values)); 
        }
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

    private void maybeSwapSearchIcon() {
        final Context context = getActivity();
        Intent intent = ((SearchManager) context.getSystemService(Context.SEARCH_SERVICE))
                .getAssistIntent(context, UserHandle.USER_CURRENT);
        if (intent != null) {
            ComponentName component = intent.getComponent();
            if (component == null || !mGlowPadView.replaceTargetDrawablesIfPresent(component,
                    ASSIST_ICON_METADATA_NAME,
                    com.android.internal.R.drawable.ic_action_assist_generic)) {
                if (DEBUG) Log.v(TAG, "Couldn't grab icon for component " + component);
            }
        }
    }

    public boolean isScreenPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private String getProperSummary(String uri) {

        if (TextUtils.isEmpty(uri) || uri.equals(ACTION_NULL)) {
                return getResources().getString(R.string.none);
        } else if (uri.equals(ACTION_SCREENSHOT)) {
                return getResources().getString(R.string.take_screenshot);
        } else if (uri.equals(ACTION_IME)) {
                return getResources().getString(R.string.open_ime_switcher);
        } else if (uri.equals(ACTION_VIB)) {
                return getResources().getString(R.string.ring_vib);
        } else if (uri.equals(ACTION_SILENT)) {
                return getResources().getString(R.string.ring_silent);
        } else if (uri.equals(ACTION_SILENT_VIB)) {
                return getResources().getString(R.string.ring_vib_silent);
        } else if (uri.equals(ACTION_KILL)) {
                return getResources().getString(R.string.kill_app);
        } else if (uri.equals(ACTION_LAST_APP)) {
                return getResources().getString(R.string.lastapp);
        } else if (uri.equals(ACTION_POWER)) {
                return getResources().getString(R.string.screen_off);
        } else if (uri.equals(ACTION_ASSIST)) {
                return getResources().getString(R.string.google_now);
        } else {
                return mPicker.getFriendlyNameForUri(uri);
        }
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
}
