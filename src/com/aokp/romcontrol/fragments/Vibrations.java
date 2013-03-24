
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.VibrationPickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.VibrationPattern;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.vibrations.VibrationRecorder;
import com.aokp.romcontrol.R;

public class Vibrations extends AOKPPreferenceFragment {
    private static final String TAG = "Vibrations";

    private ViewGroup mContainer;
    private Activity mActivity;
    private Resources mResources;

    private final int DIALOG_SAVE = 0;
    private final int DIALOG_HELP = 1;

    private final int VIB_OK = 10;
    private final int VIB_CANCEL = 11;
    private final int VIB_DEL = 12;

    VibrationRecorder mRecorder;
    Button mTapButton;
    Button mRecButton;
    Button mPlayButton;
    Button mStopButton;
    Button mSaveButton;
    Button mNewButton;
    Button mLoadButton;
    Button mDelButton;
    Button mHelpButton;
    TextView mCurLoadedText;
    SharedPreferences sharedPrefs;
    LinearLayout mPatternBar;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            VibrationPattern mPattern = (VibrationPattern) msg.obj;
            if (mPattern == null) {
                msg.what = VIB_CANCEL;
            }
            switch (msg.what) {
                case VIB_OK:
                    mRecorder.loadPattern(mPattern.getUri());
                    mCurLoadedText.setText(mPattern.getName());
                    updatePatternBar(mPattern);
                    mTapButton.setEnabled(false);
                    mStopButton.setEnabled(false);
                    mPlayButton.setEnabled(true);
                    mNewButton.setEnabled(true);
                    mSaveButton.setEnabled(true);
                    break;
                case VIB_CANCEL:
                    break;
                case VIB_DEL:
                    mRecorder.delPattern(mPattern);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = container;
        mActivity = getActivity();
        mResources = getResources();
        return inflater.inflate(R.layout.vibration_manager, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.setTitle(R.string.vibrations);
        sharedPrefs = mActivity.getSharedPreferences("vibrations", 0);
        mRecorder = new VibrationRecorder(mActivity);

        mTapButton = (Button) mActivity.findViewById(R.id.button_tap);
        mRecButton = (Button) mActivity.findViewById(R.id.button_rec);
        mPlayButton = (Button) mActivity.findViewById(R.id.button_play);
        mStopButton = (Button) mActivity.findViewById(R.id.button_stop);
        mSaveButton = (Button) mActivity.findViewById(R.id.button_save);
        mLoadButton = (Button) mActivity.findViewById(R.id.button_load);
        mDelButton = (Button) mActivity.findViewById(R.id.button_del);
        mNewButton = (Button) mActivity.findViewById(R.id.button_new);
        mHelpButton = (Button) mActivity.findViewById(R.id.help_vibrations);
        mCurLoadedText = (TextView) mActivity.findViewById(R.id.vib_cur_loaded_name);
        mPatternBar = (LinearLayout) mActivity.findViewById(R.id.pattern_bar);
        mCurLoadedText.setText("-");
        mTapButton.setEnabled(false);
        mStopButton.setEnabled(false);
        mSaveButton.setEnabled(false);
        mPlayButton.setEnabled(false);

        mTapButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mTapButton.isEnabled()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        mRecorder.processTime(event.getEventTime());
                        mRecorder.startVibration();
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        mRecorder.processTime(event.getEventTime());
                        mRecorder.stopVibration();
                    }
                }
                return false;
            }
        });

        mRecButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTapButton.setEnabled(true);
                mStopButton.setEnabled(true);
                mLoadButton.setEnabled(false);
                mDelButton.setEnabled(false);
                mPlayButton.setEnabled(false);
                mNewButton.setEnabled(false);
                mSaveButton.setEnabled(false);
                mRecorder.startRecording();
            }
        });

        mStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTapButton.setEnabled(false);
                mStopButton.setEnabled(false);
                mLoadButton.setEnabled(true);
                mDelButton.setEnabled(true);
                mPlayButton.setEnabled(true);
                mNewButton.setEnabled(true);
                mSaveButton.setEnabled(true);
                mRecorder.stopRecording();
                mCurLoadedText.setText(mRecorder.getLoadedPatternName());
                updatePatternBar(mRecorder.getCurrentPattern());
            }
        });

        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTapButton.setEnabled(false);
                mRecorder.playCapturedPattern();
            }
        });

        mNewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecorder.resetCapture();
                mSaveButton.setEnabled(false);
                mStopButton.setEnabled(false);
                mPlayButton.setEnabled(false);
                mPatternBar.setVisibility(View.INVISIBLE);
                mCurLoadedText.setText("-");
            }
        });

        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDia(DIALOG_SAVE);
                mTapButton.setEnabled(false);
                mStopButton.setEnabled(false);
                mPlayButton.setEnabled(true);
                mNewButton.setEnabled(true);
                mSaveButton.setEnabled(true);
            }
        });

        mLoadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickerDialog(false);
            }
        });

        mDelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickerDialog(true);
            }
        });

        if (sharedPrefs.getBoolean("firststart", true)) {
            sharedPrefs.edit().putBoolean("firststart", false).apply();
            showDia(DIALOG_HELP);
        }

        setHasOptionsMenu(true);
    }

    @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vibrations, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help_vibrations:
                showDia(DIALOG_HELP);
                return true;
            default:
                return false;
        }
    }

    protected void showDia(int id) {
        switch (id) {
            case DIALOG_SAVE:
                LayoutInflater factory = LayoutInflater.from(mActivity);
                final View textEntryView = factory.inflate(R.layout.vib_dialog_text_entry, null);
                final EditText name = (EditText) textEntryView.findViewById(R.id.vib_edit);
                name.setText(mRecorder.getLoadedPatternName());
                final AlertDialog saveDialog = new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.vib_dialog_text_entry)
                        .setView(textEntryView)
                        .setPositiveButton(com.android.internal.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String pattern = name.getText().toString();
                                        mRecorder.saveCapturedPattern(pattern);
                                        mCurLoadedText.setText(pattern);
                                    }
                                })
                        .setNegativeButton(com.android.internal.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // cancel, do nothing
                                    }
                                }).create();
                name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            saveDialog.getWindow().setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                name.requestFocus();
                saveDialog.show();
                break;

            case DIALOG_HELP:
                final ScrollView sView = new ScrollView(mActivity);
                sView.setPadding(8, 8, 8, 8);
                final TextView helpView = new TextView(mActivity);
                helpView.setText(R.string.vibration_detailed_help);
                helpView.setTextSize(12);
                sView.addView(helpView);
                new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.help)
                        .setPositiveButton(android.R.string.ok, null)
                        .setView(sView).show();
                break;
            default:
                break;
        }

    }

    void showPickerDialog(boolean isDel) {
        DialogFragment newFragment = VibrationPickerDialog.newInstance(mHandler, isDel, null);
        newFragment.show(getFragmentManager(), "dialog");
    }

    void updatePatternBar(VibrationPattern pattern) {
        mPatternBar.removeAllViews();
        if (pattern == null) {
            mPatternBar.setVisibility(View.INVISIBLE);
            return;
        }
        double fullWidth = mPatternBar.getWidth();
        double fullLength = ((double) pattern.getLength()) / 10;
        double ratio = fullWidth/fullLength;
        Log.d(TAG, "fullwidth = " + Double.toString(fullWidth) + " fullLength = " + Double.toString(fullLength) + " ratio = " + Double.toString(ratio));
        for (int i = 0; i<pattern.getPattern().length; i++) {
            final int mWidth = (int) (((double) pattern.getPattern()[i] / 10) * ratio);
            Log.d(TAG, "mWidth = " + Integer.toString(mWidth));
            final View view = new View(mActivity) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    // Adjust width as necessary
                    int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
                    if (mWidth > 0) {
                        int measureMode = MeasureSpec.getMode(widthMeasureSpec);
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mWidth, measureMode);
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            };
            if (i % 2 == 0) {
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));
            } else {
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.buzz));
            }
            mPatternBar.addView(view, new LinearLayout.LayoutParams(mWidth, -1));
        }
        mPatternBar.setVisibility(View.VISIBLE);
    }
}
