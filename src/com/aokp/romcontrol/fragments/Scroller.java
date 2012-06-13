
package com.aokp.romcontrol.fragments;

import java.io.File;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import com.aokp.romcontrol.widgets.SeekBarPreference;

public class Scroller extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String TAG = "Scroller";


    private static final String PREF_SCROLL_FRICTION = "scroll_friction";
    private static final String PREF_CUSTOM_FLING_VELOCITY = "custom_fling_velocity";

    SeekBarPreference mScrollFriction;
    SeekBarPreference mCustomFlingVelocity;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_scroll);

        float defaultFriction = Settings.System.getFloat(getActivity()
                .getContentResolver(), Settings.System.SCROLL_FRICTION,
                0.015f);

        mScrollFriction = (SeekBarPreference) findPreference(PREF_SCROLL_FRICTION);
        mScrollFriction.setInitValue((int) (defaultFriction * 5000));
        mScrollFriction.setOnPreferenceChangeListener(this);

        int defaultVelocity = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.CUSTOM_FLING_VELOCITY,
                8000);

        mCustomFlingVelocity = (SeekBarPreference) findPreference(PREF_CUSTOM_FLING_VELOCITY);
        mCustomFlingVelocity.setInitValue((int) (defaultVelocity / 100));
        mCustomFlingVelocity.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mScrollFriction) {
            float val = Float.parseFloat((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.SCROLL_FRICTION, val / 5000);
            return true;
        } else if (preference == mCustomFlingVelocity) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.CUSTOM_FLING_VELOCITY, val * 100);
            return true;
        }
        return false;
    }

}
