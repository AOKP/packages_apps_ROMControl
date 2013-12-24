package com.aokp.romcontrol.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.aokp.romcontrol.R;

/**
 * Setting toggle which represents a boolean value
 * <p/>
 * <ul><b>Supported attributes (in addition to {@link BaseSetting} attributes)</b>
 * <li>aokp:url - the url to open when clicking
 * </ul>
 */
public class UrlSetting extends BaseSetting implements OnClickListener {

    private String mUrl;

    public UrlSetting(Context context) {
        this(context, null);
    }

    public UrlSetting(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UrlSetting(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {

            TypedArray typedArray = null;
            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.UrlSetting);

                mUrl = typedArray.getString(R.styleable.UrlSetting_url);
            } finally {
                if (typedArray != null) {
                    typedArray.recycle();
                }
            }
        }

        addView(mRootView);
        setFocusable(true);
        if(mUrl != null) {
            setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (mUrl != null) {
            try {
                Intent launchUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
                getContext().startActivity(launchUrlIntent);
            } catch (Exception e) { // ignore
            }
        }
    }
}
