package com.baked.romcontrol.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.R;
import com.baked.romcontrol.service.FlipService;
import com.baked.romcontrol.service.HeadphoneService;

public class Sound extends BAKEDPreferenceFragment implements OnPreferenceChangeListener {

    private static final String PREF_HEADPHONES_PLUGGED_ACTION = "headphone_audio_mode";
    private static final String PREF_BT_CONNECTED_ACTION = "bt_audio_mode";
    private static final String PREF_FLIP_ACTION = "flip_mode";
    private static final String PREF_USER_TIMEOUT = "user_timeout";
    private static final String PREF_USER_DOWN_MS = "user_down_ms";

    SharedPreferences prefs;
    ListPreference mHeadphonesPluggedAction;
    ListPreference mBTPluggedAction;
    ListPreference mFlipAction;
    ListPreference mUserDownMS;
    ListPreference mFlipScreenOff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_sound);
        PreferenceManager.setDefaultValues(mContext, R.xml.prefs_sound, true);
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        mFlipAction = (ListPreference) findPreference(PREF_FLIP_ACTION);
        mFlipAction.setOnPreferenceChangeListener(this);
        mFlipAction.setValue((prefs.getString(PREF_FLIP_ACTION, "-1")));

        mUserDownMS = (ListPreference) findPreference(PREF_USER_DOWN_MS);
        mUserDownMS.setEnabled(Integer.parseInt(prefs.getString(PREF_FLIP_ACTION, "-1")) != -1);

        mFlipScreenOff = (ListPreference) findPreference(PREF_USER_TIMEOUT);
        mFlipScreenOff.setEnabled(Integer.parseInt(prefs.getString(PREF_FLIP_ACTION, "-1")) != -1);

        if (HeadphoneService.DEBUG)
            mContext.startService(new Intent(mContext, HeadphoneService.class));

        if (FlipService.DEBUG)
            mContext.startService(new Intent(mContext, FlipService.class));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlipAction) {
            int val = Integer.parseInt((String) newValue);
            if (val != -1) {
                mContext.stopService(new Intent(mContext, FlipService.class));
                mUserDownMS.setEnabled(true);
                mFlipScreenOff.setEnabled(true);
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle(getResources().getString(R.string.flip_dialog_title));
                ad.setMessage(getResources().getString(R.string.flip_dialog_msg));
                ad.setPositiveButton(getResources().getString(R.string.flip_action_positive),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
                mContext.startService(new Intent(mContext, FlipService.class));
            } else {
                mUserDownMS.setEnabled(false);
                mFlipScreenOff.setEnabled(false);
            }
            return true;
        }
        return false;
    }
}
