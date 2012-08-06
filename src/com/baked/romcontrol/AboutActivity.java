package com.baked.romcontrol;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.baked.romcontrol.R;

public class AboutActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_contributors);
    }
}
