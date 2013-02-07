package com.aokp.romcontrol.fragments;

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

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.service.FlipService;
import com.aokp.romcontrol.service.HeadphoneService;

public class Sound extends AOKPPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String PREF_ENABLE_VOLUME_OPTIONS = "enable_volume_options";
    private static final String PREF_HEADPHONES_PLUGGED_ACTION = "headphone_audio_mode";
    private static final String PREF_BT_CONNECTED_ACTION = "bt_audio_mode";
    private static final String PREF_FLIP_ACTION = "flip_mode";
    private static final String PREF_USER_TIMEOUT = "user_timeout";
    private static final String PREF_USER_DOWN_MS = "user_down_ms";
    private static final String PREF_PHONE_RING_SILENCE = "phone_ring_silence";
    private static final String PREF_LESS_NOTIFICATION_SOUNDS = "less_notification_sounds";

    SharedPreferences prefs;
    CheckBoxPreference mEnableVolumeOptions;
    ListPreference mHeadphonesPluggedAction;
    ListPreference mBTPluggedAction;
    ListPreference mFlipAction;
    ListPreference mUserDownMS;
    ListPreference mFlipScreenOff;
    ListPreference mPhoneSilent;
    ListPreference mAnnoyingNotifications;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_sound);
        addPreferencesFromResource(R.xml.prefs_sound);
        PreferenceManager.setDefaultValues(mContext, R.xml.prefs_sound, true);
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
        mEnableVolumeOptions.setChecked(Settings.System.getBoolean(getActivity()
                .getContentResolver(),
                Settings.System.ENABLE_VOLUME_OPTIONS, false));

        mAnnoyingNotifications = (ListPreference) findPreference(PREF_LESS_NOTIFICATION_SOUNDS);
        mAnnoyingNotifications.setOnPreferenceChangeListener(this);
        mAnnoyingNotifications.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD,
                0)));

        mFlipAction = (ListPreference) findPreference(PREF_FLIP_ACTION);
        mFlipAction.setOnPreferenceChangeListener(this);
        mFlipAction.setValue((prefs.getString(PREF_FLIP_ACTION, "-1")));

        mUserDownMS = (ListPreference) findPreference(PREF_USER_DOWN_MS);
        mUserDownMS.setEnabled(Integer.parseInt(prefs.getString(PREF_FLIP_ACTION, "-1")) != -1);

        mFlipScreenOff = (ListPreference) findPreference(PREF_USER_TIMEOUT);
        mFlipScreenOff.setEnabled(Integer.parseInt(prefs.getString(PREF_FLIP_ACTION, "-1")) != -1);

        mPhoneSilent = (ListPreference) findPreference(PREF_PHONE_RING_SILENCE);
        mPhoneSilent.setValue((prefs.getString(PREF_PHONE_RING_SILENCE, "0")));
        mPhoneSilent.setOnPreferenceChangeListener(this);

        if (!hasPhoneAbility(mContext)) {
            getPreferenceScreen().removePreference(mPhoneSilent);
        }

        if (HeadphoneService.DEBUG)
            mContext.startService(new Intent(mContext, HeadphoneService.class));

        if (FlipService.DEBUG)
            mContext.startService(new Intent(mContext, FlipService.class));
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

    private void toggleFlipService() {
        if (FlipService.isStarted()) {
            mContext.stopService(new Intent(mContext, FlipService.class));
        }
        mContext.startService(new Intent(mContext, FlipService.class));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlipAction) {
            int val = Integer.parseInt((String) newValue);
            if (val != -1) {
                mUserDownMS.setEnabled(true);
                mFlipScreenOff.setEnabled(true);
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle(getResources().getString(R.string.flip_dialog_title));
                ad.setMessage(getResources().getString(R.string.flip_dialog_msg));
                ad.setPositiveButton(
                        getResources().getString(R.string.flip_action_positive),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                ad.show();
                toggleFlipService();
            } else {
                mUserDownMS.setEnabled(false);
                mFlipScreenOff.setEnabled(false);
            }
            return true;

        } else if (preference == mAnnoyingNotifications) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, val);
            return true;

        } else if (preference == mPhoneSilent) {
            int val = Integer.parseInt((String) newValue);
            if (val != 0) {
                toggleFlipService();
            }
            return true;
        }
        return false;
    }
}
