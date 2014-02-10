package com.aokp.romcontrol.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.aokp.romcontrol.R;

/**
 * Setting toggle which represents a boolean value
 * <p/>
 * <ul><b>Supported attributes (in addition to {@link BaseSetting} attributes)</b>
 * <li>android:entryValues
 * <li>android:entryValueEntries
 * </ul>
 */
public class SingleChoiceSetting extends BaseSetting implements OnClickListener {

    private String[] mEntries;
    private String[] mValues;

    public SingleChoiceSetting(Context context) {
        this(context, null);
    }

    public SingleChoiceSetting(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleChoiceSetting(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            TypedArray typedArray = null;

            try {
//                typedArray = context.obtainStyledAttributes(attrs, R.styleable.SingleChoiceSetting);

                typedArray = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.entries, android.R.attr.entryValues});
                int entriesRes = typedArray.getResourceId(0, 0); // because R.attr.entries has index 0 in the passed array
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
        }
        addView(View.inflate(context, R.layout.setting_colorpicker, mRootView));


        updateSummary();
        setOnClickListener(this);
        setFocusable(true);
    }

    @Override
    public void onClick(View v) {

        new Builder(getContext())
                .setSingleChoiceItems(mEntries, getCurrentValueIndex(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        if (selectedPosition > -1) {
                            setValue(mValues[selectedPosition]);
                            updateSummary();
                        }
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
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

    private int getCurrentValueIndex() {
        // returns the index of the current value, relative to the given R.array
        String val = getValue();
        if (val != null) {
            for (int i = 0; i < mValues.length; i++) {
                if (val.equals(mValues[i])) {
                    return i;
                }
            }
        }
        else {
            String defaultVal = getDefaultValue();
            if (defaultVal != null) {
                for (int i = 0; i < mValues.length; i++) {
                   if (defaultVal.equals(mValues[i])) {
                        return i;
                    }
                }
            }
        }

        return -1;

    }

    public void updateSummary() {
        if (getDefaultSummary() != null && getValue() == null) {
            // let's not touch it if one was already set
        } else {
            int currentValueIndex = getCurrentValueIndex();
            if (currentValueIndex == -1) {
                setSummary(null);
            } else {
                setSummary(mEntries[currentValueIndex].toString());
            }
        }
    }
}
