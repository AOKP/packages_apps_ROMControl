package com.aokp.romcontrol.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

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
    ArrayList<String> mAvailableValues = new ArrayList<String>();
    ArrayList<String> mSelectedValues = new ArrayList<String>();

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
                typedArray = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.entries,
                        android.R.attr.entryValues});
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

        setOnClickListener(this);
        setFocusable(true);
    }

    @Override
    public void onClick(View v) {

        new Builder(getContext())
                .setTitle(getTitle())
                .setCancelable(true)
                .setPositiveButton(R.string.toggles_display_close,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
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

    private boolean[] getCheckedValues() {
        boolean checkedValues[] = new boolean[mValues.length];

        mAvailableValues.clear();

        Collections.addAll(mAvailableValues, mValues);

        String values = getValue();

        if (TextUtils.isEmpty(values)) {
            values = getDefaultValue();
        }

        if (!TextUtils.isEmpty(values)) {
            String[] split = TextUtils.split(values, "|");

            mSelectedValues.clear();

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
}
