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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
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

import com.android.internal.util.aokp.AwesomeConstants;
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

public class NavRingTargets extends Fragment implements
        ShortcutPickerHelper.OnPickListener, GlowPadView.OnTriggerListener {
    private static final String TAG = "NavRing";
    private static final boolean DEBUG = false;

    private static final String ASSIST_ICON_METADATA_NAME = "com.android.systemui.action_assist_icon";

    private GlowPadView mGlowPadView;
    private ShortcutPickerHelper mPicker;
    private String[] targetActivities = new String[5];
    private String[] longActivities = new String[5];
    private ViewGroup mContainer;

    private int mTargetIndex = 0;
    private int startPosOffset;
    private int endPosOffset;
    private int mNavRingAmount;
    private int mCurrentUIMode;
    private boolean mLefty;
    private boolean mBoolLongPress;
    private boolean mSearchPanelLock;
    private boolean mLongPress = false;
    private boolean mLongTarget;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;

    private ArrayList<Integer> intentList = new ArrayList<Integer>();
    private int intentCounter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContainer = container;
        setHasOptionsMenu(true);

        mPicker = new ShortcutPickerHelper(this, this);

        return inflater.inflate(R.layout.navigation_ring_targets, container, false);
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
        mLongPress = false;
        mSearchPanelLock = false;
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
            final TargetDrawable drawable = NavRingHelpers.getTargetDrawable(context, targetActivities[i]);
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
            int i = j + middleStart;
            final TargetDrawable drawable = NavRingHelpers.getTargetDrawable(context, targetActivities[i]);
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
                            Settings.System.putInt(cr,
                                Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1);
                        }
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
        }
        updateDrawables();
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        if (!mLongTarget) {
            targetActivities[mTargetIndex] = uri;
        } else {
            longActivities[mTargetIndex] = uri;
        }
        setDrawables();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateDrawables() {
        for (int i = 0; i < 5; i++) {
             targetActivities[i] = Settings.System.getString(
                 getActivity().getContentResolver(), Settings.System.SYSTEMUI_NAVRING[i]);
              longActivities[i] = Settings.System.getString(
                 getActivity().getContentResolver(), Settings.System.SYSTEMUI_NAVRING_LONG[i]);
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

    public void onValueChange(String uri, boolean longTarget) {
        mLongTarget = longTarget;
        if (uri.equals("**app**")) {
            final String label = getResources().getString(R.string.lockscreen_target_empty);
            final ShortcutIconResource iconResource =
                    ShortcutIconResource.fromContext(getActivity(), android.R.drawable.ic_delete);
            mPicker.pickShortcut();
        } else {
            if (!mLongTarget) {
                targetActivities[mTargetIndex] = uri;
            } else {
                longActivities[mTargetIndex] = uri;
            }
        }
        mLongPress = true;
        setDrawables();
    }

    @Override
    public void onTrigger(View v, final int target) {
        mTargetIndex = intentList.get(target);
        if (!mLongPress) {

            final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    final String[] values = getResources().getStringArray(R.array.navring_dialog_values);
                    onValueChange(values[item], false);
                    dialog.dismiss();
                }
            };

            final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.choose_action_title)
                    .setSingleChoiceItems(R.array.navring_dialog_entries, -1, l)
                    .create();

            dialog.show();
            mHandler.removeCallbacks(SetLongPress);
        }
    }

    final Runnable SetLongPress = new Runnable () {
        public void run() {
            if (!mSearchPanelLock) {
                mLongPress = true;
                final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        final String[] values = getResources().getStringArray(R.array.navring_dialog_values);
                        onValueChange(values[item], true);
                        dialog.dismiss();
                    }
                };

                final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.choose_action_title)
                    .setSingleChoiceItems(R.array.navring_dialog_entries, -1, l)
                    .create();

                dialog.show();
                mSearchPanelLock = true;
            }
        }
    };

    @Override
    public void onGrabbed(View v, int handle) {
        mSearchPanelLock = false;
    }

    @Override
    public void onReleased(View v, int handle) {
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
        mHandler.removeCallbacks(SetLongPress);
        mLongPress = false;
    }

    public void onTargetChange(View v, final int target) {
         if (target == -1) {
            mHandler.removeCallbacks(SetLongPress);
            mLongPress = false;
         } else {
            if (mBoolLongPress) {
                mTargetIndex = intentList.get(target);
                mHandler.postDelayed(SetLongPress, ViewConfiguration.getLongPressTimeout());
            }
         }
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
