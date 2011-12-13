
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;

import com.roman.romcontrol.R;
import com.roman.romcontrol.fragment.LockscreenPreferenceFragment;

public class Lockscreens extends Activity {
    private static final String PREF_MENU = "pref_lockscreen_menu_unlock";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new LockscreenPreferenceFragment()).commit();
    }

   
}
