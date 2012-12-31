package com.aokp.romcontrol.widgets;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.aokp.romcontrol.R;

public class NavBarItemPreference extends ListPreference {

    ImageView customIcon;
    View.OnClickListener imagelistener, shortlistener, longlistener;

    public NavBarItemPreference(Context c) {
        super(c);
    }

    public NavBarItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View v = View.inflate(getContext(), R.layout.navbar_item_preference, null);
        customIcon = (ImageView) v.findViewById(android.R.id.icon);
        return v;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        if (imagelistener != null)
            view.findViewById(android.R.id.icon).setOnClickListener(imagelistener);

    }

    public void setImageListener(View.OnClickListener l) {
        imagelistener = l;
        if (customIcon != null)
            customIcon.setOnClickListener(l);
    }

}
