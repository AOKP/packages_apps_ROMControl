/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;

/**
 * Base class for Settings fragments, with some helper functions and dialog management.
 */
public class AOKPPreferenceFragment extends PreferenceFragment implements DialogCreatable {

    private static final String TAG = "SettingsPreferenceFragment";
    protected Context mContext;

    private SettingsDialogFragment mDialogFragment;
    protected ActionBar mActionBar;
    protected boolean mShortcutFragment;
    protected boolean hasTorch;
    protected boolean hasHardwareButtons;
    protected boolean hasFastCharge;
    protected boolean hasColorTuning;
    protected boolean hasVibration = false;
    protected ContentResolver mContentRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasTorch = getResources().getBoolean(R.bool.has_torch);
        hasHardwareButtons = getResources().getBoolean(R.bool.has_hardware_buttons);
        hasFastCharge = getResources().getBoolean(R.bool.has_fast_charge);
        hasColorTuning = getResources().getBoolean(R.bool.has_color_tuning);
        mContext = getActivity();
        mActionBar = getActivity().getActionBar();
        mContentRes = getActivity().getContentResolver();
        if(getArguments() != null) {
            mShortcutFragment = getArguments().getBoolean("started_from_shortcut", false);
        }
        if(!mShortcutFragment)
            mActionBar.setDisplayHomeAsUpEnabled(true);

        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null && mVibrator.hasVibrator()) {
            hasVibration = true;
        }
    }

    public static boolean isTablet(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.CURRENT_UI_MODE,0) == 1;
    }

    public static boolean isPhablet(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.CURRENT_UI_MODE,0) == 2;
    }

    public static boolean hasPhoneAbility(Context context)
    {
       TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
       if(telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE)
           return false;

       return true;
    }

    public static boolean isSW600DPScreen(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int widthPixels = displayMetrics.widthPixels;
        float density = displayMetrics.density;
        return ((widthPixels / density) >= 600);
    }

    public void setTitle(int resId) {
        getActivity().setTitle(resId);
    }

    /*
     * The name is intentionally made different from Activity#finish(), so that users won't
     * misunderstand its meaning.
     */
    public final void finishFragment() {
        getActivity().onBackPressed();
    }

    // Some helpers for functions used by the settings fragments when they were activities

    /**
     * Returns the ContentResolver from the owning Activity.
     */
    protected ContentResolver getContentResolver() {
        return getActivity().getContentResolver();
    }

    /**
     * Returns the specified system service from the owning Activity.
     */
    protected Object getSystemService(final String name) {
        return getActivity().getSystemService(name);
    }

    /**
     * Returns the PackageManager from the owning Activity.
     */
    protected PackageManager getPackageManager() {
        return getActivity().getPackageManager();
    }

    @Override
    public void onDetach() {
        if (isRemoving()) {
            if (mDialogFragment != null) {
                mDialogFragment.dismiss();
                mDialogFragment = null;
            }
        }
        super.onDetach();
    }

    // Dialog management

    protected void showDialog(int dialogId) {
        if (mDialogFragment != null) {
            Log.e(TAG, "Old dialog fragment not null!");
        }
        mDialogFragment = new SettingsDialogFragment(this, dialogId);
        mDialogFragment.show(getActivity().getFragmentManager(), Integer.toString(dialogId));
    }

    public Dialog onCreateDialog(int dialogId) {
        return null;
    }

    protected void removeDialog(int dialogId) {
        // mDialogFragment may not be visible yet in parent fragment's onResume().
        // To be able to dismiss dialog at that time, don't check
        // mDialogFragment.isVisible().
        if (mDialogFragment != null && mDialogFragment.getDialogId() == dialogId) {
            mDialogFragment.dismiss();
        }
        mDialogFragment = null;
    }

    /**
     * Sets the OnCancelListener of the dialog shown. This method can only be called after
     * showDialog(int) and before removeDialog(int). The method does nothing otherwise.
     */
    protected void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        if (mDialogFragment != null) {
            mDialogFragment.mOnCancelListener = listener;
        }
    }

    /**
     * Sets the OnDismissListener of the dialog shown. This method can only be called after
     * showDialog(int) and before removeDialog(int). The method does nothing otherwise.
     */
    protected void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        if (mDialogFragment != null) {
            mDialogFragment.mOnDismissListener = listener;
        }
    }

    public static class SettingsDialogFragment extends DialogFragment {
        private static final String KEY_DIALOG_ID = "key_dialog_id";
        private static final String KEY_PARENT_FRAGMENT_ID = "key_parent_fragment_id";

        private int mDialogId;

        private Fragment mParentFragment;

        private DialogInterface.OnCancelListener mOnCancelListener;
        private DialogInterface.OnDismissListener mOnDismissListener;

        public SettingsDialogFragment() {
            /* do nothing */
        }

        public SettingsDialogFragment(DialogCreatable fragment, int dialogId) {
            mDialogId = dialogId;
            if (!(fragment instanceof Fragment)) {
                throw new IllegalArgumentException("fragment argument must be an instance of "
                        + Fragment.class.getName());
            }
            mParentFragment = (Fragment) fragment;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (mParentFragment != null) {
                outState.putInt(KEY_DIALOG_ID, mDialogId);
                outState.putInt(KEY_PARENT_FRAGMENT_ID, mParentFragment.getId());
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                mDialogId = savedInstanceState.getInt(KEY_DIALOG_ID, 0);
                int mParentFragmentId = savedInstanceState.getInt(KEY_PARENT_FRAGMENT_ID, -1);
                if (mParentFragmentId > -1) {
                    mParentFragment = getFragmentManager().findFragmentById(mParentFragmentId);
                    if (!(mParentFragment instanceof DialogCreatable)) {
                        throw new IllegalArgumentException(
                                KEY_PARENT_FRAGMENT_ID + " must implement "
                                        + DialogCreatable.class.getName());
                    }
                }
                // This dialog fragment could be created from non-SettingsPreferenceFragment
                if (mParentFragment instanceof AOKPPreferenceFragment) {
                    // restore mDialogFragment in mParentFragment
                    ((AOKPPreferenceFragment) mParentFragment).mDialogFragment = this;
                }
            }
            return ((DialogCreatable) mParentFragment).onCreateDialog(mDialogId);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (mOnCancelListener != null) {
                mOnCancelListener.onCancel(dialog);
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss(dialog);
            }
        }

        public int getDialogId() {
            return mDialogId;
        }

        @Override
        public void onDetach() {
            super.onDetach();

            // This dialog fragment could be created from non-SettingsPreferenceFragment
            if (mParentFragment instanceof AOKPPreferenceFragment) {
                // in case the dialog is not explicitly removed by removeDialog()
                if (((AOKPPreferenceFragment) mParentFragment).mDialogFragment == this) {
                    ((AOKPPreferenceFragment) mParentFragment).mDialogFragment = null;
                }
            }
        }
    }

    protected boolean hasNextButton() {
        return ((ButtonBarHandler) getActivity()).hasNextButton();
    }

    protected Button getNextButton() {
        return ((ButtonBarHandler) getActivity()).getNextButton();
    }

    public void finish() {
        getActivity().onBackPressed();
    }

    public boolean startFragment(
            Fragment caller, String fragmentClass, int requestCode, Bundle extras) {
        if (getActivity() instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) getActivity();
            preferenceActivity.startPreferencePanel(fragmentClass, extras,
                    R.string.app_name, null, caller, requestCode);
            return true;
        } else {
            Log.w(TAG, "Parent isn't PreferenceActivity, thus there's no way to launch the "
                    + "given Fragment (name: " + fragmentClass + ", requestCode: " + requestCode
                    + ")");
            return false;
        }
    }

    protected boolean isCheckBoxPrefernceChecked(Preference p) {
        if(p instanceof CheckBoxPreference) {
            return ((CheckBoxPreference) p).isChecked();
        } else {
            return false;
        }
    }

}
