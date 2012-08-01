
package com.aokp.romcontrol.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.service.HeadphoneService;

public class Sound extends AOKPPreferenceFragment {

    private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
    private static final String PREF_HEADPHONES_PLUGGED_ACTION = "headphone_audio_mode";
    private static final String PREF_BT_CONNECTED_ACTION = "bt_audio_mode";

    CheckBoxPreference mEnableVolumeOptions;
    ListPreference mHeadphonesPluggedAction;
    ListPreference mBTPluggedAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_sound);
        PreferenceManager.setDefaultValues(mContext, R.xml.prefs_sound, true);

        mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
        mEnableVolumeOptions.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(),
                Settings.System.ENABLE_VOLUME_OPTIONS, false));

        if (HeadphoneService.DEBUG)
            mContext.startService(new Intent(mContext, HeadphoneService.class));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableVolumeOptions) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.ENABLE_VOLUME_OPTIONS, checked);
            return true;

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
