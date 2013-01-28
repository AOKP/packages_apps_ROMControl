
package com.aokp.romcontrol.widgets;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.aokp.romcontrol.R;

public class AlphaSeekBar extends LinearLayout implements OnSeekBarChangeListener {

    private static final String SUPERSTATE = "superstate";
    private static final String ALPHA = "alpha";

    private static final int MAX_VALUE = 255;
    int defaultValue = 255;

    private TextView mAlphaText;
    private TextView mPercentText;
    private SeekBar mSeekBar;

    public AlphaSeekBar(Context context) {
        this(context, null);
    }

    public AlphaSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlphaSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Activity parent = ((Activity) getContext());
        parent.getLayoutInflater().inflate(R.layout.alpha_seekbar, this, true);

        mAlphaText = (TextView) findViewById(R.id.alpha);
        mPercentText = (TextView) findViewById(R.id.percent);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setMax(MAX_VALUE);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(defaultValue);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        seekBar.setProgress(progress);
        mAlphaText.setText(String.valueOf(progress));
        int percent = Math.round((new Float(progress) / new Float(MAX_VALUE)) * 100);
        mPercentText.setText(percent + "%");
    }

    public int getCurrentAlpha() {
        return mSeekBar.getProgress();
    }

    public void setCurrentAlpha(int p) {
        if (p < 0) {
            p = 0;
        } else if (p > 255) {
            p = 255;
        }
        if (mSeekBar != null)
            mSeekBar.setProgress(p);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mSeekBar.setEnabled(enabled);
        mAlphaText.setEnabled(enabled);
        mPercentText.setEnabled(enabled);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
