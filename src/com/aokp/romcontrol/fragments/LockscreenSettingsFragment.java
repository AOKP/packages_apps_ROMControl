package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.ColorPickerSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;

public class LockscreenSettingsFragment extends Fragment implements OnSettingChangedListener {

    CheckboxSetting mLockscreenNotifications, mPocketMode, mShowAlways, mWakeOnNotification,
        mHideLowPriority, mHideNonClearable, mDismissAll, mPrivacyMode, mExpandedView, mExpandedViewForce;
    ColorPickerSetting mNotificationColor;
    SingleChoiceSetting mOffsetTop, mNotificationHeight;
    boolean mHasProximitySensor;

    public LockscreenSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHasProximitySensor = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lockscreen_settings, container, false);

        mLockscreenNotifications = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications);
        mPocketMode = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_pocket_mode);
        mShowAlways = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_show_always);
        if (!mHasProximitySensor) {
            mPocketMode.setVisibility(View.GONE);
            mShowAlways.setVisibility(View.GONE);
        }
        mWakeOnNotification = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_wake_on_notification);
        mHideLowPriority = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_hide_low_priority);
        mHideNonClearable = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_hide_non_clearable);
        mDismissAll = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_dismiss_all);
        mOffsetTop = (SingleChoiceSetting) v.findViewById(R.id.lockscreen_notifications_offset_top);
        mNotificationHeight = (SingleChoiceSetting) v.findViewById(R.id.lockscreen_notifications_height);
        mNotificationColor = (ColorPickerSetting) v.findViewById(R.id.lockscreen_notifications_color);
        mPrivacyMode = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_privacy_mode);
        mExpandedView = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_expanded_view);
        mExpandedViewForce = (CheckboxSetting) v.findViewById(R.id.lockscreen_notifications_force_expanded_view);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLockscreenNotifications.setOnSettingChangedListener(this);
        mPocketMode.setOnSettingChangedListener(this);
        mHideNonClearable.setOnSettingChangedListener(this);
        mPrivacyMode.setOnSettingChangedListener(this);
        mExpandedView.setOnSettingChangedListener(this);
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if ("aokp".equals(table)) {
            if (mHasProximitySensor) {
                mPocketMode.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
                // Display only if pocket mode is enabled
                mShowAlways.setVisibility(mLockscreenNotifications.isChecked() ? (mPocketMode.isChecked() ? View.VISIBLE : View.GONE) : View.GONE);
            } else {
                mPocketMode.setVisibility(View.GONE);
                mShowAlways.setVisibility(View.GONE);
            }
            mWakeOnNotification.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mHideLowPriority.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mHideNonClearable.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            // Display only if hide non clearable is disabled
            mDismissAll.setVisibility(mLockscreenNotifications.isChecked() ? (mHideNonClearable.isChecked() ? View.GONE : View.VISIBLE) : View.GONE);
            mOffsetTop.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mNotificationHeight.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mNotificationColor.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            mPrivacyMode.setVisibility(mLockscreenNotifications.isChecked() ? View.VISIBLE : View.GONE);
            // Display only if privacy mode is disabled
            mExpandedView.setVisibility(mLockscreenNotifications.isChecked() ? (mPrivacyMode.isChecked() ? View.GONE : View.VISIBLE) : View.GONE);
            // Display only if expanded view is enabled
            mExpandedViewForce.setVisibility(mLockscreenNotifications.isChecked() ? (mExpandedView.isChecked() ? View.VISIBLE : View.GONE) : View.GONE);
        }
    }

}
