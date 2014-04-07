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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.aokp.AokpRibbonHelper;
import com.android.internal.util.aokp.AwesomeAnimationHelper;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavRingHelpers;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.settings.BaseSetting.OnSettingChangedListener;
import com.aokp.romcontrol.settings.CheckboxSetting;
import com.aokp.romcontrol.settings.ColorPickerSetting;
import com.aokp.romcontrol.settings.SingleChoiceSetting;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

public class AnimationsFragment extends Fragment implements OnSeekBarChangeListener,
        OnSettingChangedListener {
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
    private SingleChoiceSetting mToastAnimation;
    private SeekBar mAnimationDuration;
    private SeekBar mProgressBarSpeed;
    private SeekBar mProgressBarWidth;
    private SeekBar mProgressBarLength;
    private SeekBar mProgressBarCount;
    private ProgressBar mProgressBarSample;
    private CheckboxSetting mProgressBarMirror;
    private CheckboxSetting mProgressBarReverse;
    private ColorPickerSetting mProgressBarColor1;
    private ColorPickerSetting mProgressBarColor2;
    private ColorPickerSetting mProgressBarColor3;
    private ColorPickerSetting mProgressBarColor4;

    private Context mContext;
    private int mSeekBarProgress;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    private boolean mInit;

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

        mInit = true;

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

        mAnimationDuration = (SeekBar) main.findViewById(R.id.animation_duration);
        mAnimationDuration.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), Settings.AOKP.ANIMATION_CONTROLS_DURATION, 50));
        mAnimationDuration.setOnSeekBarChangeListener(this);

        mProgressBarSpeed = (SeekBar) main.findViewById(R.id.progressbar_speed);
        mProgressBarSpeed.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_SPEED, 4));
        mProgressBarSpeed.setOnSeekBarChangeListener(this);

        mProgressBarWidth = (SeekBar) main.findViewById(R.id.progressbar_width);
        mProgressBarWidth.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_WIDTH, 4));
        mProgressBarWidth.setOnSeekBarChangeListener(this);

        mProgressBarLength = (SeekBar) main.findViewById(R.id.progressbar_length);
        mProgressBarLength.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_LENGTH, 10));
        mProgressBarLength.setOnSeekBarChangeListener(this);

        mProgressBarCount = (SeekBar) main.findViewById(R.id.progressbar_count);
        mProgressBarCount.setProgress(Settings.AOKP.getInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_COUNT, 6));
        mProgressBarCount.setOnSeekBarChangeListener(this);

        mToastAnimation =  (SingleChoiceSetting) main.findViewById(R.id.toast_animation);
        mToastAnimation.setOnSettingChangedListener(this);

        mProgressBarSample = (ProgressBar) main.findViewById(R.id.sample_progressBar);

        mProgressBarMirror = (CheckboxSetting) main.findViewById(R.id.progressbar_mirror);
        mProgressBarMirror.setOnSettingChangedListener(this);

        mProgressBarReverse = (CheckboxSetting) main.findViewById(R.id.progressbar_reverse);
        mProgressBarReverse.setOnSettingChangedListener(this);

        mProgressBarColor1 = (ColorPickerSetting) main.findViewById(R.id.progressbar_color_1);
        mProgressBarColor1.setOnSettingChangedListener(this);

        mProgressBarColor2 = (ColorPickerSetting) main.findViewById(R.id.progressbar_color_2);
        mProgressBarColor2.setOnSettingChangedListener(this);

        mProgressBarColor3 = (ColorPickerSetting) main.findViewById(R.id.progressbar_color_3);
        mProgressBarColor3.setOnSettingChangedListener(this);

        mProgressBarColor4 = (ColorPickerSetting) main.findViewById(R.id.progressbar_color_4);
        mProgressBarColor4.setOnSettingChangedListener(this);

        mInit = false;

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
        if (seekBar == mAnimationDuration) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.ANIMATION_CONTROLS_DURATION, mSeekBarProgress);
        } else if (seekBar == mProgressBarSpeed) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_SPEED, mSeekBarProgress);
            recreateProgressBarSample();
        } else if (seekBar == mProgressBarWidth) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_WIDTH, mSeekBarProgress);
            recreateProgressBarSample();
        } else if (seekBar == mProgressBarLength) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_LENGTH, mSeekBarProgress);
            recreateProgressBarSample();
        } else if (seekBar == mProgressBarCount) {
            Settings.AOKP.putInt(mContext.getContentResolver(), Settings.AOKP.PROGRESSBAR_COUNT, mSeekBarProgress);
            recreateProgressBarSample();
        }
    }

    @Override
    public void onSettingChanged(String table, String key, String oldValue, String value) {
        if (key.equals("toast_animation")) {
            if (!mInit) {
                Toast toast = Toast.makeText(mContext, mContext.getResources()
                        .getString(R.string.toast_animation_title), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else if (key.equals("progressbar_mirror") || key.equals("progressbar_reverse") ||
                key.equals("progressbar_color_1") || key.equals("progressbar_color_2") ||
                key.equals("progressbar_color_3") || key.equals("progressbar_color_4") ) {
            recreateProgressBarSample();
        }
    }

    private void recreateProgressBarSample() {
        // This is needed as the options are set in the contructor of the ProgressBar
        ViewGroup parent = (ViewGroup) mProgressBarSample.getParent();
        int index = parent.indexOfChild(mProgressBarSample);
        parent.removeView(mProgressBarSample);
        mProgressBarSample = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
        mProgressBarSample.setIndeterminate(true);
        mProgressBarSample.setProgress(android.R.integer.config_longAnimTime);
        parent.addView(mProgressBarSample, index);
    }
}
