package com.aokp.romcontrol.fragments;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.internal.util.aokp.AokpRibbonHelper;
import com.android.internal.util.aokp.AwesomeAnimationHelper;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavRingHelpers;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.ColorPickerSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

public class AnimationsFragment extends Fragment implements OnSeekBarChangeListener {
    private SingleChoiceSetting mActivityOpen;
    private SingleChoiceSetting mActivityClose;
    private SingleChoiceSetting mTaskOpen;
    private SingleChoiceSetting mTaskClose;
    private SingleChoiceSetting mWallpaperOpen;
    private SingleChoiceSetting mWallpaperClose;
    private SingleChoiceSetting mWallpaperIntraOpen;
    private SingleChoiceSetting mWallpaperIntraClose;
    private SingleChoiceSetting mTaskMoveFront;
    private SingleChoiceSetting mTaskMoveBack;
    private SeekBar mDuration;
    private Context mContext;
    private int mSeekBarProgress;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    public AnimationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get NavRing Actions
        mContext = getActivity();

        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(mContext, mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        View main = inflater.inflate(R.layout.fragment_animation_settings, container, false);

        mActivityOpen = (SingleChoiceSetting) main.findViewById(R.id.activity_open);
        mActivityOpen.setEntryValues(mAnimationsNum);
        mActivityOpen.setEntries(mAnimationsStrings);
        mActivityOpen.updateSummary();

        mActivityClose = (SingleChoiceSetting) main.findViewById(R.id.activity_close);
        mActivityClose.setEntryValues(mAnimationsNum);
        mActivityClose.setEntries(mAnimationsStrings);
        mActivityClose.updateSummary();

        mTaskOpen = (SingleChoiceSetting) main.findViewById(R.id.task_open);
        mTaskOpen.setEntryValues(mAnimationsNum);
        mTaskOpen.setEntries(mAnimationsStrings);
        mTaskOpen.updateSummary();

        mTaskClose = (SingleChoiceSetting) main.findViewById(R.id.task_close);
        mTaskClose.setEntryValues(mAnimationsNum);
        mTaskClose.setEntries(mAnimationsStrings);
        mTaskClose.updateSummary();

        mTaskMoveFront = (SingleChoiceSetting) main.findViewById(R.id.task_move_to_front);
        mTaskMoveFront.setEntryValues(mAnimationsNum);
        mTaskMoveFront.setEntries(mAnimationsStrings);
        mTaskMoveFront.updateSummary();

        mTaskMoveBack = (SingleChoiceSetting) main.findViewById(R.id.task_move_to_back);
        mTaskMoveBack.setEntryValues(mAnimationsNum);
        mTaskMoveBack.setEntries(mAnimationsStrings);
        mTaskMoveBack.updateSummary();

        mWallpaperOpen = (SingleChoiceSetting) main.findViewById(R.id.wallpaper_open);
        mWallpaperOpen.setEntryValues(mAnimationsNum);
        mWallpaperOpen.setEntries(mAnimationsStrings);
        mWallpaperOpen.updateSummary();

        mWallpaperClose = (SingleChoiceSetting) main.findViewById(R.id.wallpaper_close);
        mWallpaperClose.setEntryValues(mAnimationsNum);
        mWallpaperClose.setEntries(mAnimationsStrings);
        mWallpaperClose.updateSummary();

        mWallpaperIntraClose = (SingleChoiceSetting) main.findViewById(R.id.wallpaper_intra_close);
        mWallpaperIntraClose.setEntryValues(mAnimationsNum);
        mWallpaperIntraClose.setEntries(mAnimationsStrings);
        mWallpaperIntraClose.updateSummary();

        mWallpaperIntraOpen = (SingleChoiceSetting) main.findViewById(R.id.wallpaper_intra_open);
        mWallpaperIntraOpen.setEntryValues(mAnimationsNum);
        mWallpaperIntraOpen.setEntries(mAnimationsStrings);
        mWallpaperIntraOpen.updateSummary();

        mDuration = (SeekBar) main.findViewById(R.id.animation_duration);
        mDuration.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), Settings.AOKP.ANIMATION_CONTROLS_DURATION, 50));
        mDuration.setOnSeekBarChangeListener(this);

        return main;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mSeekBarProgress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mDuration) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.ANIMATION_CONTROLS_DURATION, mSeekBarProgress);
        }
    }
}
