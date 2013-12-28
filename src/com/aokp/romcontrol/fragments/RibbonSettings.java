package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aokp.romcontrol.R;

public class RibbonSettings extends Fragment {
    private TextView mText;
    private Context mContext;
    private String mProfile;

    public RibbonSettings(Context context, String profile) {
        mContext = context;
        mProfile = profile;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedinstanceState) {
        View main = inflater.inflate(R.layout.fragment_ribbons_settings, container, false);
        mText = (TextView) main.findViewById(R.id.text);
        mText.setText(mProfile + " ribbon settings");
        return main;
    }
}
