package com.aokp.romcontrol.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import com.aokp.romcontrol.R;

/**
 * Setting toggle which allows choosing multiple items
 * <p/>
 * <ul><b>Supported attributes (in addition to {@link BaseSetting} attributes)</b>
 * <li>android:entryValues
 * <li>android:entryValueEntries
 * </ul>
 */
public class MultiChoiceSetting extends BaseSetting implements OnClickListener {

    private String[] mEntries;
    private String[] mValues;
    private int mMinSelectionCount = 0;
    private int mMaxSelectionCount = 0;
    private ArrayList<String> mAvailableValues = new ArrayList<String>();
    private ArrayList<String> mSelectedValues = new ArrayList<String>();

    public MultiChoiceSetting(Context context) {
        this(context, null);
    }

    public MultiChoiceSetting(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiChoiceSetting(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            TypedArray typedArray = null;

            try {
                int[] attrsMultiChoiceSetting = new int[]{
                        android.R.attr.entries,
                        android.R.attr.entryValues
                        };
                typedArray = context.obtainStyledAttributes(attrs, attrsMultiChoiceSetting);
                int entriesRes = typedArray.getResourceId(0, 0);
                if (entriesRes > 0) {
                    mEntries = getResources().getStringArray(entriesRes);
                } else {
                    mEntries = new String[0];
                }

                int valuesRes = typedArray.getResourceId(1, 0);
                if (valuesRes > 0) {
                    mValues = getResources().getStringArray(valuesRes);

                } else {
                    mValues = new String[0];
                }

            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }

            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiChoiceSetting);

                mMinSelectionCount = typedArray.getInteger(0, mMinSelectionCount);
                mMaxSelectionCount = typedArray.getInteger(1, mMaxSelectionCount);

            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }
        }
        addView(View.inflate(context, R.layout.setting_colorpicker, mRootView));

        setOnClickListener(this);
        setFocusable(true);
    }

    @Override
    public void onClick(View v) {

        final AlertDialog d = new Builder(getContext())
                .setTitle(getTitle())
                .setCancelable(true)
                .setPositiveButton(R.string.toggles_display_close, null)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {

                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode,
                                KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                if (checkSelectionCount()) {
                                    dialog.dismiss();
                                }
                                return true;
                            }
                            return false;
                        }
                    })
                .setMultiChoiceItems(mEntries, getCheckedValues(),
                        new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String selectedValue = mAvailableValues.get(which);
                        if (isChecked) {
                            mSelectedValues.add(selectedValue);
                        } else {
                            mSelectedValues.remove(selectedValue);
                        }
                        setValue(TextUtils.join("|",mSelectedValues));
                    }
                })
                .create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (checkSelectionCount()) {
                            d.dismiss();
                        }
                    }
                });
            }
        });

        d.show();

    }

    public String[] getEntries() {
        return mEntries;
    }

    public void setEntries(String[] entries) {
        mEntries = entries;
    }

    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getStringArray(entriesResId));
    }

    public String[] getEntryValues() {
        return mValues;
    }

    public void setEntryValues(String[] entryValues) {
        mValues = entryValues;
    }

    public void setEntryValues(int entryValuesResId) {
        setEntryValues(getContext().getResources().getStringArray(entryValuesResId));
    }

    private boolean[] getCheckedValues() {
        boolean checkedValues[] = new boolean[mValues.length];

        mSelectedValues.clear();
        mAvailableValues.clear();

        Collections.addAll(mAvailableValues, mValues);

        String values = getValue();

        if (TextUtils.isEmpty(values)) {
            values = getDefaultValue();
        }

        if (!TextUtils.isEmpty(values)) {
            String[] split = TextUtils.split(values, "\\|");

            Collections.addAll(mSelectedValues, split);

            for (int i = 0; i < checkedValues.length; i++) {
                String selectedValue = mAvailableValues.get(i);
                if (mSelectedValues.contains(selectedValue)) {
                    checkedValues[i] = true;
                }
            }
        }

        return checkedValues;
    }

    private boolean checkSelectionCount() {
        Context context = getContext();
        Resources res = getResources();

        if (mMaxSelectionCount > 0 && mSelectedValues.size() > mMaxSelectionCount) {
            Toast toast = Toast.makeText(context,
                    res.getQuantityString(R.plurals.multichoice_at_most, mMaxSelectionCount,
                            mMaxSelectionCount),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        } else if (mMinSelectionCount > 0 && mSelectedValues.size() < mMinSelectionCount) {
            Toast toast = Toast.makeText(context,
                    res.getQuantityString(R.plurals.multichoice_at_least, mMinSelectionCount,
                            mMinSelectionCount),
                    Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        return true;
    }

    public void updateSummary(String summary) {
        setSummary(summary);
    }
}
