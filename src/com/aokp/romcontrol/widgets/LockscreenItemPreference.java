
package com.aokp.romcontrol.widgets;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aokp.romcontrol.R;

import java.io.File;

public class LockscreenItemPreference extends ListPreference {

    ImageView customIcon;
    View.OnClickListener listener;

    public LockscreenItemPreference(Context c) {
        super(c);
    }

    public LockscreenItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View v = View.inflate(getContext(), R.layout.lockscreen_item_preference, null);
        customIcon = (ImageView) v.findViewById(android.R.id.icon);
        return v;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (listener != null)
            view.findViewById(android.R.id.icon).setOnClickListener(listener);
    }

    public void setImageListener(View.OnClickListener l) {
        listener = l;
        if (customIcon != null)
            customIcon.setOnClickListener(l);
    }
}
