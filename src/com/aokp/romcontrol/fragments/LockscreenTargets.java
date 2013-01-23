/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.TargetDrawable;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.util.IconPicker;
import com.aokp.romcontrol.util.IconPicker.OnIconPickListener;

public class LockscreenTargets extends AOKPPreferenceFragment implements ShortcutPickerHelper.OnPickListener,
    GlowPadView.OnTriggerListener, OnIconPickListener {

    private static final String TAG = LockscreenTargets.class.getSimpleName();
    private GlowPadView mWaveView;
    private ImageButton mDialogIcon;
    private Button mDialogLabel;
    private ShortcutPickerHelper mPicker;
    private IconPicker mIconPicker;
    private ArrayList<TargetInfo> mTargetStore = new ArrayList<TargetInfo>();
    private ArrayList<TargetDrawable> mViewTargets = new ArrayList<TargetDrawable>();;
    private int mTargetOffset;
    private int mTargetInset;
    private boolean mIsLandscape;
    private boolean mIsScreenLarge;
    private ViewGroup mContainer;
    private Activity mActivity;
    private Resources mResources;
    private File mImageTmp;
    private int mTargetIndex = 0;
    private boolean mTargetChanged = false;
    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static String EMPTY_LABEL;

    class TargetInfo {
        String uri, pkgName;
        StateListDrawable icon;
        Drawable defaultIcon;
        String iconType;
        String iconSource;
        TargetInfo(StateListDrawable target) {
            icon = target;
        }
        TargetInfo(String in, StateListDrawable target, String iType, String iSource, Drawable dI, String iPkgName) {
            uri = in;
            icon = target;
            defaultIcon = dI;
            iconType = iType;
            iconSource = iSource;
            pkgName = iPkgName;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContainer = container;
        setHasOptionsMenu(true);
        mActivity = getActivity();
        mIsScreenLarge = isScreenLarge();
        mResources = getResources();
        mIsLandscape = mResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        mTargetInset = mResources.getDimensionPixelSize(com.android.internal.R.dimen.lockscreen_target_inset);
        mIconPicker = new IconPicker(mActivity, this);
        mPicker = new ShortcutPickerHelper(this, this);
        mImageTmp = new File(mActivity.getCacheDir() + "/target.tmp");
        EMPTY_LABEL = mActivity.getResources().getString(R.string.lockscreen_target_empty);
        return inflater.inflate(R.layout.lockscreen_targets, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWaveView = ((GlowPadView) mActivity.findViewById(R.id.lock_target));
        mWaveView.setOnTriggerListener(this);
        initializeView(Settings.System.getString(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGETS));
    }

    private StateListDrawable getLayeredDrawable(Drawable back, Drawable front, int inset, boolean frontBlank) {
        Resources res = getResources();
        InsetDrawable[] inactivelayer = new InsetDrawable[2];
        InsetDrawable[] activelayer = new InsetDrawable[2];
        //maxwen: dont like that circle around
        //inactivelayer[0] = new InsetDrawable(res.getDrawable(com.android.internal.R.drawable.ic_lockscreen_lock_pressed), 0, 0, 0, 0);
        // just use an "empty" image
        inactivelayer[0] = new InsetDrawable(res.getDrawable(com.android.internal.R.drawable.ic_lockscreen_empty), 0, 0, 0, 0);
        inactivelayer[1] = new InsetDrawable(front, inset, inset, inset, inset);
        activelayer[0] = new InsetDrawable(back, 0, 0, 0, 0);
        activelayer[1] = new InsetDrawable(frontBlank ? res.getDrawable(android.R.color.transparent) : front, inset, inset, inset, inset);
        StateListDrawable states = new StateListDrawable();
        LayerDrawable inactiveLayerDrawable = new LayerDrawable(inactivelayer);
        inactiveLayerDrawable.setId(0, 0);
        inactiveLayerDrawable.setId(1, 1);
        LayerDrawable activeLayerDrawable = new LayerDrawable(activelayer);
        activeLayerDrawable.setId(0, 0);
        activeLayerDrawable.setId(1, 1);
        states.addState(TargetDrawable.STATE_INACTIVE, inactiveLayerDrawable);
        states.addState(TargetDrawable.STATE_ACTIVE, activeLayerDrawable);
        states.addState(TargetDrawable.STATE_FOCUSED, activeLayerDrawable);
        return states;
    }

    private void initializeView(String input) {
        if (input == null) {
            input = GlowPadView.EMPTY_TARGET;
        }
        mTargetStore.clear();
        mViewTargets.clear();
        
        final int maxTargets = mIsScreenLarge ? GlowPadView.MAX_TABLET_TARGETS : GlowPadView.MAX_PHONE_TARGETS;
        final PackageManager packMan = mActivity.getPackageManager();
        final Drawable activeBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
        final String[] targetStore = input.split("\\|");
        //Shift by 2 targets for phones in landscape
        //mTargetOffset = mIsLandscape && !mIsScreenLarge ? 2 : 0;
        //maxwen TODO check why
        mTargetOffset = 0;
        if (mTargetOffset == 2) {
            mTargetStore.add(new TargetInfo(null));
            mTargetStore.add(new TargetInfo(null));
        }
        //Add the unlock icon
        Drawable unlockFront = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_unlock_normal);
        Drawable unlockBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_unlock_activated);
        mTargetStore.add(new TargetInfo(getLayeredDrawable(unlockBack, unlockFront, 0, true)));
        for (int cc = 0; cc < 8 - mTargetOffset - 1; cc++) {
            String uri = GlowPadView.EMPTY_TARGET;
            Drawable front = null;
            Drawable back = activeBack;
            boolean frontBlank = false;
            String iconType = null;
            String iconSource = null;
            String pkgName = null;
            int tmpInset = mTargetInset;
            if (cc < targetStore.length && cc < maxTargets) {
                uri = targetStore[cc];
                if (!uri.equals(GlowPadView.EMPTY_TARGET)) {
                    try {
                        Intent in = Intent.parseUri(uri, 0);
                        if (in.hasExtra(GlowPadView.ICON_FILE)) {
                            String rSource = in.getStringExtra(GlowPadView.ICON_FILE);
                            File fPath = new File(rSource);
                            if (fPath != null) {
                                if (fPath.exists()) {
                                    front = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(rSource));
                                }
                            }
                        } else if (in.hasExtra(GlowPadView.ICON_RESOURCE)) {
                            String rSource = in.getStringExtra(GlowPadView.ICON_RESOURCE);
                            String rPackage = in.getStringExtra(GlowPadView.ICON_PACKAGE);
                            if (rSource != null) {
                                if (rPackage != null) {
                                    try {
                                        Context rContext = mActivity.createPackageContext(rPackage, 0);
                                        int id = rContext.getResources().getIdentifier(rSource, "drawable", rPackage);
                                        front = rContext.getResources().getDrawable(id);
                                        id = rContext.getResources().getIdentifier(rSource.replaceAll("_normal", "_activated"),
                                                "drawable", rPackage);
                                        back = rContext.getResources().getDrawable(id);
                                        tmpInset = 0;
                                        frontBlank = true;
                                    } catch (NameNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (NotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    front = mResources.getDrawable(mResources.getIdentifier(rSource, "drawable", "android"));
                                    back = mResources.getDrawable(mResources.getIdentifier(
                                            rSource.replaceAll("_normal", "_activated"), "drawable", "android"));
                                    tmpInset = 0;
                                    frontBlank = true;
                                }
                            }
                        }
                        if (front == null) {
                            ActivityInfo aInfo = in.resolveActivityInfo(packMan, PackageManager.GET_ACTIVITIES);
                            if (aInfo != null) {
                                front = aInfo.loadIcon(packMan);
                            } else {
                                front = mResources.getDrawable(android.R.drawable.sym_def_app_icon).mutate();
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } else if (cc >= maxTargets) {
                mTargetStore.add(new TargetInfo(null));
                continue;
            }
            if (back == null || front == null) {
                Drawable emptyIcon = mResources.getDrawable(R.drawable.ic_empty).mutate();
                front = emptyIcon;
            }
            mTargetStore.add(new TargetInfo(uri, getLayeredDrawable(back,front, tmpInset, frontBlank), iconType,
                    iconSource, front.getConstantState().newDrawable().mutate(), pkgName));
        }

        int j=0;
        for (TargetInfo i : mTargetStore) {
            if (i.icon != null) {
                mViewTargets.add(new TargetDrawable(mResources, i.icon));
            } else {
                mViewTargets.add(new TargetDrawable(mResources, null));
            }
            j++;
        }
        mWaveView.setTargetResources(mViewTargets);
    }

    @Override
    public void onResume() {
        super.onResume();
        // If running on a phone, remove padding around container
        if (!mIsScreenLarge) {
            mContainer.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
            .setIcon(R.drawable.ic_settings_backup) // use the backup icon
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
                Toast.makeText(mActivity, R.string.lockscreen_target_save, Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    /**
     * Resets the target layout to stock
     */
    private void resetAll() {
        new AlertDialog.Builder(mActivity)
        .setTitle(R.string.lockscreen_target_reset_title)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setMessage(R.string.lockscreen_target_reset_message)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                initializeView(null);
                Settings.System.putString(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGETS, null);
                Toast.makeText(mActivity, R.string.lockscreen_target_reset, Toast.LENGTH_LONG).show();
            }
        }).setNegativeButton(R.string.cancel, null)
        .create().show();
    }

    /**
     * Save targets to settings provider
     */
    private void saveAll() {
        StringBuilder targetLayout = new StringBuilder();
        ArrayList<String> existingImages = new ArrayList<String>();
        final int maxTargets = mIsScreenLarge ? GlowPadView.MAX_TABLET_TARGETS : GlowPadView.MAX_PHONE_TARGETS;
        for (int i = mTargetOffset + 1; i <= mTargetOffset + maxTargets; i++) {
            String uri = mTargetStore.get(i).uri;
            String type = mTargetStore.get(i).iconType;
            String source = mTargetStore.get(i).iconSource;
            existingImages.add(source);
            if (!uri.equals(GlowPadView.EMPTY_TARGET) && type != null) {
                try {
                    Intent in = Intent.parseUri(uri, 0);
                    in.putExtra(type, source);
                    String pkgName = mTargetStore.get(i).pkgName;
                    if (pkgName != null) {
                        in.putExtra(GlowPadView.ICON_PACKAGE, mTargetStore.get(i).pkgName);
                    } else {
                        in.removeExtra(GlowPadView.ICON_PACKAGE);
                    }
                    uri = in.toUri(0);
                } catch (URISyntaxException e) {
                }
            }
            targetLayout.append(uri);
            targetLayout.append("|");
        }
        targetLayout.deleteCharAt(targetLayout.length() - 1);
        Settings.System.putString(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGETS, targetLayout.toString());
        for (File pic : mActivity.getFilesDir().listFiles()) {
            if (pic.getName().startsWith("lockscreen_") && !existingImages.contains(pic.toString())) {
                pic.delete();
            }
        }
    }
    
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        try {
            Intent i = Intent.parseUri(uri, 0);
            PackageManager pm = mActivity.getPackageManager();
            ActivityInfo aInfo = i.resolveActivityInfo(pm, PackageManager.GET_ACTIVITIES);
            Drawable icon = null;
            if (aInfo != null) {
                icon = aInfo.loadIcon(pm).mutate();
            } else {
                icon = mResources.getDrawable(android.R.drawable.sym_def_app_icon);
            }
            mDialogLabel.setText(friendlyName);
            mDialogLabel.setTag(uri);
            mDialogIcon.setImageDrawable(resizeForDialog(icon));
            mDialogIcon.setTag(null);
        } catch (Exception e) {
        }
    }

    private Drawable resizeForDialog(Drawable image) {
        int size = (int) mResources.getDimension(R.dimen.target_icon_size);
        Bitmap d = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, size, size, false);
        return new BitmapDrawable(mResources, bitmapOrig);
    }

    public boolean isScreenLarge() {
        final int screenSize = Resources.getSystem().getConfiguration().screenLayout &
                   Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean isScreenLarge = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                    screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        return isScreenLarge;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String shortcut_name = null;
        if (data != null) {
            shortcut_name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        }
        if (shortcut_name != null && shortcut_name.equals(EMPTY_LABEL)) {
            mDialogLabel.setText(EMPTY_LABEL);
            mDialogLabel.setTag(GlowPadView.EMPTY_TARGET);
            mDialogIcon.setImageResource(R.drawable.ic_empty);
        } else if (requestCode == IconPicker.REQUEST_PICK_SYSTEM || requestCode == IconPicker.REQUEST_PICK_AOKP || requestCode == IconPicker.REQUEST_PICK_GALLERY
                || requestCode == IconPicker.REQUEST_PICK_ICON_PACK) {
            mIconPicker.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode != Activity.RESULT_CANCELED && resultCode != Activity.RESULT_CANCELED){
            mPicker.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onGrabbed(View v, int handle) {
    }

    @Override
    public void onReleased(View v, int handle) {
    }

    @Override
    public void onTargetChange(View view, final int target) {
    }

    @Override
    public void onTrigger(View v, final int target) {
    	mTargetChanged = false;
        mTargetIndex = target;
        if ((target != 0 && (mIsScreenLarge || !mIsLandscape)) || (target != 2 && !mIsScreenLarge && mIsLandscape)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.lockscreen_target_edit_title);
            builder.setMessage(R.string.lockscreen_target_edit_msg);
            View view = View.inflate(mActivity, R.layout.lockscreen_shortcut_dialog, null);
            view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mDialogLabel.getText().equals(EMPTY_LABEL)) {
                        try {
                            mImageTmp.createNewFile();
                            mImageTmp.setWritable(true, false);
                            mIconPicker.pickIcon(getId(), mImageTmp);
                            mTargetChanged = true;
                        } catch (IOException e) {
                        }
                    }
                }
            });
            view.findViewById(R.id.label).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPicker.pickShortcut();
                    mTargetChanged = true;
                }
            });
            mDialogIcon = ((ImageButton) view.findViewById(R.id.icon));
            mDialogLabel = ((Button) view.findViewById(R.id.label));
            TargetInfo item = mTargetStore.get(target);
            mDialogIcon.setImageDrawable(mTargetStore.get(target).defaultIcon.mutate());
            TargetInfo tmpIcon = new TargetInfo(null);
            tmpIcon.iconType = item.iconType;
            tmpIcon.iconSource = item.iconSource;
            tmpIcon.pkgName = item.pkgName;
            mDialogIcon.setTag(tmpIcon);
            if (mTargetStore.get(target).uri.equals(GlowPadView.EMPTY_TARGET)) {
                mDialogLabel.setText(EMPTY_LABEL);
            } else {
                mDialogLabel.setText(mPicker.getFriendlyNameForUri(mTargetStore.get(target).uri));
            }
            mDialogLabel.setTag(mTargetStore.get(target).uri);
            builder.setView(view);
            builder.setPositiveButton(R.string.ok,  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	if(!mTargetChanged){
                		return;
                	}
                    TargetInfo vObject = (TargetInfo) mDialogIcon.getTag();
                    String type = null, source = null, pkgName = null;
                    int targetInset = mTargetInset;
                    if (vObject != null) {
                        type = vObject.iconType;
                        source = vObject.iconSource;
                        pkgName = vObject.pkgName;
                    }
                    boolean frontBlank = false;
                    if (type != null && type.equals(GlowPadView.ICON_RESOURCE)) {
                        targetInset = 0;
                        frontBlank = true;
                    }
                    
            		// replace existing one with new
                    Drawable back = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
            		Drawable front = mDialogIcon.getDrawable().mutate();
               		StateListDrawable drawable=getLayeredDrawable(back, front, targetInset, frontBlank);
            		TargetInfo changed = new TargetInfo(mDialogLabel.getTag().toString(), drawable, type, source, front.getConstantState().newDrawable().mutate(), pkgName);

					mTargetStore.set(mTargetIndex, changed);
					mViewTargets.set(mTargetIndex, new TargetDrawable(mResources, changed.icon));
            		mWaveView.setTargetResources(mViewTargets);
                }
            });
            builder.setNeutralButton(R.string.lockscreen_target_empty,  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	// already empty
                	if(mDialogLabel.getTag().equals(GlowPadView.EMPTY_TARGET)){
                		return;
                	}
            		
            		// replace existing one with new empty
            		String type = null, source = null, pkgName = null;;
            		boolean frontBlank = false;
            		Drawable back = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
            		Drawable front = mResources.getDrawable(R.drawable.ic_empty).mutate();
               		StateListDrawable drawable=getLayeredDrawable(back, front, mTargetInset, frontBlank);
            		TargetInfo empty = new TargetInfo(GlowPadView.EMPTY_TARGET, drawable, type, source, front.getConstantState().newDrawable().mutate(), pkgName);

					mTargetStore.set(mTargetIndex, empty);
					mViewTargets.set(mTargetIndex, new TargetDrawable(mResources, empty.icon));
            		mWaveView.setTargetResources(mViewTargets);
                }
            });

            builder.setNegativeButton(R.string.cancel, null);
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
            ((TextView)dialog.findViewById(android.R.id.message)).setTextAppearance(mActivity,
                    android.R.style.TextAppearance_DeviceDefault_Small);
        }
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
    }

    @Override
    public void iconPicked(int requestCode, int resultCode, Intent in) {
        Drawable ic = null;
        String iconType = null;
        String pkgName = null;
        String iconSource = null;
        if (requestCode == IconPicker.REQUEST_PICK_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                File mImage = new File(mActivity.getFilesDir() + "/lockscreen_" + System.currentTimeMillis() + ".png");
                if (mImageTmp.exists()) {
                    mImageTmp.renameTo(mImage);
                }
                mImage.setReadable(true, false);
                iconType = GlowPadView.ICON_FILE;
                iconSource = mImage.toString();
                ic = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(mImage.toString()));
            } else {
                if (mImageTmp.exists()) {
                    mImageTmp.delete();
                }
                return;
            }
        } else if (requestCode == IconPicker.REQUEST_PICK_SYSTEM) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            ic = mResources.getDrawable(mResources.getIdentifier(resourceName, "drawable", "android")).mutate();
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else if (requestCode == IconPicker.REQUEST_PICK_AOKP) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            ic = mResources.getDrawable(mResources.getIdentifier(resourceName, "drawable", "android")).mutate();
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else if (requestCode == IconPicker.REQUEST_PICK_ICON_PACK && resultCode == Activity.RESULT_OK) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            pkgName = in.getStringExtra(IconPicker.PACKAGE_NAME);
            try {
                Context rContext = mActivity.createPackageContext(pkgName, 0);
                int id = rContext.getResources().getIdentifier(resourceName, "drawable", pkgName);
                ic = rContext.getResources().getDrawable(id);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else {
            return;
        }
        TargetInfo tmpIcon = new TargetInfo(null);
        tmpIcon.iconType = iconType;
        tmpIcon.iconSource = iconSource;
        tmpIcon.pkgName = pkgName;
        mDialogIcon.setTag(tmpIcon);
        mDialogIcon.setImageDrawable(ic);
    }

    @Override
    public void onFinishFinalAnimation() {
    }
}
