/*
 * Copyright (C) 2011 Sergey Margaritov
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

package com.aokp.romcontrol.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import com.aokp.romcontrol.R;
import net.margaritov.preference.colorpicker.ColorPickerDialog;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

import java.lang.NumberFormatException;

/**
 * Setting toggle which represents a boolean value
 * <p/>
 * <ul><b>Supported attributes (in addition to {@link BaseSetting} attributes)</b>
 * <li>aokp:colorPickerDefaultValue - default color.
 * <li>aokp:colorPickerShowAlphaSlider - a @string reference, which will be set as the summary when disabled.
 * </ul>
 */
public class ColorPickerSetting extends BaseSetting implements
        OnClickListener, OnColorChangedListener {

    int mDefaultValue = Color.BLACK;
    private int mValue = Color.BLACK;
    private float mDensity = 0;
    private boolean mAlphaSliderEnabled = false;

    private EditText mEditText;

    @Override
    public boolean isInEditMode() {
        return true;
    }

    public ColorPickerSetting(Context context) {
        this(context, null);
    }

    public ColorPickerSetting(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.settingStyle);
    }

    public ColorPickerSetting(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mDensity = getContext().getResources().getDisplayMetrics().density;
        if (attrs != null) {
            TypedArray typedArray = null;

            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerSetting);

                mDefaultValue = typedArray.getColor(R.styleable.ColorPickerSetting_colorPickerDefaultValue, mDefaultValue);
                mAlphaSliderEnabled = typedArray.getBoolean(R.styleable.ColorPickerSetting_colorPickerShowAlphaSlider, false);
            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }
        }

        addView(View.inflate(context, R.layout.setting_colorpicker, mRootView));

        /**
         * Setup initial logic
         */
        String value = getValue();
        if (value != null && !value.isEmpty()) {
            try {
                mValue = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                mValue = mDefaultValue;
            }
        } else {
            mValue = mDefaultValue;
        }

        setPreviewColor();
        setOnClickListener(this);
    }

    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        onColorChanged(restoreValue ? getColor() : (Integer) defaultValue);
    }

    private void setPreviewColor() {

        ImageView iView = (ImageView) findViewById(R.id.color_preview);

//        iView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
        iView.setImageBitmap(getPreviewBitmap());
    }

    private Bitmap getPreviewBitmap() {
        int d = (int) (mDensity * 31); // 30dip
        int color = getColor();
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c = (i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2) ? Color.GRAY : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }

    public int getColor() {
        return mValue;
    }

    @Override
    public void onColorChanged(int color) {
        String hex = convertToARGB(color);
        int intHex = convertToColorInt(hex);
        setValue(String.valueOf(intHex));
        mValue = color;
        setPreviewColor();
//        try {
//            getOnPreferenceChangeListener().onPreferenceChange(this, color);
//        } catch (NullPointerException e) {
//        }
        try {
            mEditText.setText(Integer.toString(color, 16));
        } catch (NullPointerException e) {
        }
    }


    @Override
    public void onClick(View v) {
        ColorPickerDialog picker = new ColorPickerDialog(getContext(), getColor());
        picker.setOnColorChangedListener(this);
        if (mAlphaSliderEnabled) {
            picker.setAlphaSliderVisible(true);
        }
        picker.show();
    }


    /**
     * Toggle Alpha Slider visibility (by default it's disabled)
     *
     * @param enable
     */
    public void setAlphaSliderEnabled(boolean enable) {
        mAlphaSliderEnabled = enable;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     *
     * @param color
     * @author Unknown
     */
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     *
     * @param argb
     * @throws NumberFormatException
     * @author Unknown
     */
    public static int convertToColorInt(String argb) throws NumberFormatException {

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }

}
