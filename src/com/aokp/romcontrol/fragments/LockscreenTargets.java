/*
 * Copyright (C) 2013 Android Open Kang Project
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
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import static com.android.internal.util.aokp.AwesomeConstants.*;
import com.android.internal.util.aokp.LockScreenHelpers;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.TargetDrawable;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.Helpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.ROMControlActivity;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.lang.NumberFormatException;

public class LockscreenTargets extends Fragment implements
        ShortcutPickerHelper.OnPickListener, GlowPadView.OnTriggerListener {
    private static final String TAG = "Lockscreen Targets";
    private static final boolean DEBUG = false;

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;

    private Context mContext;

    private ContentResolver cr;

    private GlowPadView mGlowPadView;
    private Switch mLongPressStatus;

    private ShortcutPickerHelper mPicker;
    private String[] targetActivities = new String[8];
    private String[] longActivities = new String[8];
    private String[] customIcons = new String[8];
    private ViewGroup mContainer;

    private boolean mBoolLongPress;
    private int mTargetIndex;
    private int mTarget = 0;


    public static enum DialogConstant {
        ICON_ACTION  { @Override public String value() { return "**icon**";}},
        LONG_ACTION  { @Override public String value() { return "**long**";}},
        SHORT_ACTION { @Override public String value() { return "**short**";}},
        CUSTOM_APP   { @Override public String value() { return "**app**";}},
        NOT_IN_ENUM  { @Override public String value() { return "**notinenum**";}};
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
        return DialogConstant.NOT_IN_ENUM;
    }

    private String mString;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContainer = container;
        setHasOptionsMenu(true);
        mContext = getActivity();
        cr = mContext.getContentResolver();

        mPicker = new ShortcutPickerHelper(this, this);
        return inflater.inflate(R.layout.lockscreen_targets, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGlowPadView = ((GlowPadView) getActivity().findViewById(R.id.lock_target));
        mGlowPadView.setOnTriggerListener(this);

        mLongPressStatus = (Switch) getActivity().findViewById(R.id.longpress_switch);
        mLongPressStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                Settings.System.putBoolean(cr, Settings.System.LOCKSCREEN_TARGETS_LONGPRESS, checked);
                updateDrawables();
            }
        });
        updateDrawables();
    }

    private void setDrawables() {
        final Context context = getActivity();
        mLongPressStatus.setChecked(mBoolLongPress);

        if (mUnlockCounter() < 1) {
            targetActivities[0] = AwesomeConstant.ACTION_UNLOCK.value();
        }

        // Custom Targets
        ArrayList<TargetDrawable> storedDraw = new ArrayList<TargetDrawable>();

        // Add User Targets
        for (int i = 0; i < 8; i++) {
            TargetDrawable drawable;
            if (!TextUtils.isEmpty(customIcons[i])) {
                drawable = LockScreenHelpers.getCustomDrawable(mContext, customIcons[i]);
            } else {
                drawable = LockScreenHelpers.getTargetDrawable(mContext, targetActivities[i]);
            }
            drawable.setEnabled(true);
            storedDraw.add(drawable);
        }
        mGlowPadView.setTargetResources(storedDraw);
        maybeSwapSearchIcon();
    }

    private void maybeSwapSearchIcon() {
        // Update the search icon with drawable from the search .apk
        Intent intent = ((SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE))
               .getAssistIntent(mContext, UserHandle.USER_CURRENT);
        if (intent != null) {
            ComponentName component = intent.getComponent();
            boolean replaced = mGlowPadView.replaceTargetDrawablesIfPresent(component,
                    ASSIST_ICON_METADATA_NAME + "_google",
                    com.android.internal.R.drawable.ic_action_assist_generic);
            if (!replaced && !mGlowPadView.replaceTargetDrawablesIfPresent(component,
                        ASSIST_ICON_METADATA_NAME,
                            com.android.internal.R.drawable.ic_action_assist_generic)) {
                Log.v(TAG, "Couldn't grab icon from package " + component);
            }
        }
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
                Toast.makeText(getActivity(), R.string.lockscreen_target_save, Toast.LENGTH_LONG).show();
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
                .setTitle(R.string.lockscreen_target_reset_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.lockscreen_target_reset_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int i = 0; i < 8; i++) {
                            Settings.System.putString(cr,
                                Settings.System.LOCKSCREEN_TARGETS_SHORT[i], null);
                            Settings.System.putString(cr,
                                Settings.System.LOCKSCREEN_TARGETS_LONG[i], null);
                            Settings.System.putString(cr,
                                Settings.System.LOCKSCREEN_TARGETS_ICON[i], null);

                        }
                        updateDrawables();
                        Toast.makeText(getActivity(),
                                R.string.lockscreen_target_reset,
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
        if (mUnlockCounter() > 0) { 
            for (int i = 0; i < 8; i++) {
                Settings.System.putString(cr,
                        Settings.System.LOCKSCREEN_TARGETS_SHORT[i], targetActivities[i]);
                Settings.System.putString(cr,
                        Settings.System.LOCKSCREEN_TARGETS_LONG[i], longActivities[i]);
                Settings.System.putString(cr,
                        Settings.System.LOCKSCREEN_TARGETS_ICON[i], customIcons[i]);
            }
        } else {
        Toast.makeText(getActivity(), getResources()
                 .getString(R.string.save_error), Toast.LENGTH_LONG).show();
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
        for (int i = 0; i < 8; i++) {
             targetActivities[i] = Settings.System.getString(cr, Settings.System.LOCKSCREEN_TARGETS_SHORT[i]);
             longActivities[i] = Settings.System.getString(cr, Settings.System.LOCKSCREEN_TARGETS_LONG[i]);
             customIcons[i] = Settings.System.getString(cr, Settings.System.LOCKSCREEN_TARGETS_ICON[i]);
        }
        mBoolLongPress = (Settings.System.getBoolean(cr, Settings.System.LOCKSCREEN_TARGETS_LONGPRESS, false));
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
            mString = Settings.System.LOCKSCREEN_TARGETS_SHORT[mTargetIndex];
            createDialog(
                getResources().getString(R.string.choose_action_short_title),
                getResources().getStringArray(R.array.lockscreen_dialog_entries),
                getResources().getStringArray(R.array.lockscreen_dialog_values));
            break;
        case LONG_ACTION:
            mTarget = 1;
            mString = Settings.System.LOCKSCREEN_TARGETS_LONG[mTargetIndex];
            createDialog(
                getResources().getString(R.string.choose_action_long_title),
                getResources().getStringArray(R.array.lockscreen_dialog_entries),
                getResources().getStringArray(R.array.lockscreen_dialog_values));
            break;
        case ICON_ACTION:
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
        case NOT_IN_ENUM:
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
            break;

        }
        setDrawables();
    }

    @Override
    public void onTrigger(View v, final int target) {
        mTargetIndex = target;
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

    private String getProperSummary(String uri) {

        if (TextUtils.isEmpty(uri) || AwesomeConstant.ACTION_NULL.equals(uri)) {
                return getResources().getString(R.string.none);
        }

        String newSummary = mContext.getResources().getString(R.string.none);
        AwesomeConstant stringEnum = fromString(uri);
        switch (stringEnum) {
        case ACTION_UNLOCK:
                newSummary = getResources().getString(R.string.lockscreen_unlock);
                break;
        case ACTION_CAMERA:
                newSummary = getResources().getString(R.string.lockscreen_camera);
                break;
        case ACTION_ASSIST:
                newSummary = getResources().getString(R.string.google_now);
                break;
        case ACTION_APP:
                newSummary = mPicker.getFriendlyNameForUri(uri);
                break;
        }
        return newSummary;
   }

    private int mUnlockCounter() {
        int counter = 0;
        for (int i = 0; i < 8 ; i++) {
            if (!TextUtils.isEmpty(targetActivities[i])) {
                if (targetActivities[i].equals(AwesomeConstant.ACTION_UNLOCK.value())) {
                    counter += 1;
                }
            }
        }
        return counter;
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mTargetIndex + ".png"));

    }

    private String getIconFileName(int index) {
        return "lockscreen_icon_" + index + ".png";
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
