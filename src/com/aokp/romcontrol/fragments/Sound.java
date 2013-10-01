package com.aokp.romcontrol.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.service.FlipService;
import com.aokp.romcontrol.service.HeadphoneService;
import com.aokp.romcontrol.widgets.VibDurationPreference;

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
    private static final String GENERIC_VIBRATE_INTENSITY = "generic_vibrate_intensity";
    private static final String VIBRATE_CATEGORY = "vibrate_category";
    private static final String PREF_INCREASING_RING = "increasing_ring";

    SharedPreferences prefs;
    CheckBoxPreference mEnableVolumeOptions;
    ListPreference mHeadphonesPluggedAction;
    ListPreference mBTPluggedAction;
    ListPreference mFlipAction;
    ListPreference mUserDownMS;
    ListPreference mFlipScreenOff;
    ListPreference mPhoneSilent;
    ListPreference mAnnoyingNotifications;
    Preference mIncreasingRing;
    VibDurationPreference mVibtationIntensity;

    Vibrator vib;

    private int mCallPref;
    private int mFlipPref;

    private boolean mTactileFeedbackEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_sound);
        addPreferencesFromResource(R.xml.prefs_sound);
        PreferenceManager.setDefaultValues(mContext, R.xml.prefs_sound, true);
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        vib = (Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE);

        mHeadphonesPluggedAction = (ListPreference) findPreference(PREF_HEADPHONES_PLUGGED_ACTION);

        mBTPluggedAction = (ListPreference) findPreference(PREF_BT_CONNECTED_ACTION);

        mEnableVolumeOptions = (CheckBoxPreference) findPreference(PREF_ENABLE_VOLUME_OPTIONS);
        mEnableVolumeOptions.setChecked(Settings.System.getBoolean(mContentRes,
                Settings.System.ENABLE_VOLUME_OPTIONS, false));

        mAnnoyingNotifications = (ListPreference) findPreference(PREF_LESS_NOTIFICATION_SOUNDS);
        mAnnoyingNotifications.setOnPreferenceChangeListener(this);
        mAnnoyingNotifications.setValue(Integer.toString(Settings.System.getInt(mContentRes,
                Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, 0)));

        mFlipAction = (ListPreference) findPreference(PREF_FLIP_ACTION);
        mFlipAction.setOnPreferenceChangeListener(this);
        mFlipAction.setValue((prefs.getString(PREF_FLIP_ACTION, "-1")));
        mFlipPref = Integer.parseInt(prefs.getString(PREF_FLIP_ACTION, "-1"));

        mUserDownMS = (ListPreference) findPreference(PREF_USER_DOWN_MS);

        mFlipScreenOff = (ListPreference) findPreference(PREF_USER_TIMEOUT);

        mPhoneSilent = (ListPreference) findPreference(PREF_PHONE_RING_SILENCE);
        mPhoneSilent.setValue((prefs.getString(PREF_PHONE_RING_SILENCE, "0")));
        mPhoneSilent.setOnPreferenceChangeListener(this);
        mCallPref = Integer.parseInt(prefs.getString(PREF_PHONE_RING_SILENCE, "-1"));

        mIncreasingRing = (Preference) findPreference(PREF_INCREASING_RING);

        mTactileFeedbackEnabled = Settings.System.getIntForUser(mContentRes,
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 1, UserHandle.USER_CURRENT) != 0;
        final int vibIntensity = Settings.System.getInt(mContentRes,
                Settings.System.GENERIC_VIBRATE_INTENSITY, 0);
        mVibtationIntensity = (VibDurationPreference) findPreference(GENERIC_VIBRATE_INTENSITY);
        mVibtationIntensity.setInitValue((int) (vibIntensity));
        mVibtationIntensity.setOnPreferenceChangeListener(this);

        if (mFlipPref != -1) {
            mUserDownMS.setEnabled(true);
            mFlipScreenOff.setEnabled(true);
            mUserDownMS.setSummary(R.string.summary_down_sec);
            mFlipScreenOff.setSummary(R.string.summary_timeout_sec);
        } else {
            mUserDownMS.setEnabled(false);
            mFlipScreenOff.setEnabled(false);
            mUserDownMS.setSummary(R.string.enable_audio_mode);
            mFlipScreenOff.setSummary(R.string.enable_audio_mode);
        }

        if (!hasPhoneAbility(mContext)) {
            getPreferenceScreen().removePreference(mPhoneSilent);
            getPreferenceScreen().removePreference(mIncreasingRing);
        }

        if (HeadphoneService.DEBUG) {
            mContext.startService(new Intent(mContext, HeadphoneService.class));
        }

        if (FlipService.DEBUG) {
            mContext.startService(new Intent(mContext, FlipService.class));
        }

        if (!hasVibration) {
            getPreferenceScreen().removePreference(((PreferenceGroup) findPreference(VIBRATE_CATEGORY)));
            String[] noVibEntries = {
                    getResources().getString(R.string.headphones_mode_no_action),
                    getResources().getString(R.string.headphones_mode_silent)};
            String[] noVibEntriesValues = {"-1", "0"};
            mHeadphonesPluggedAction.setEntries(noVibEntries);
            mHeadphonesPluggedAction.setEntryValues(noVibEntriesValues);
            mBTPluggedAction.setEntries(noVibEntries);
            mBTPluggedAction.setEntryValues(noVibEntriesValues);
            mFlipAction.setEntries(noVibEntries);
            mFlipAction.setEntryValues(noVibEntriesValues);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mEnableVolumeOptions) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putBoolean(mContentRes,
                    Settings.System.ENABLE_VOLUME_OPTIONS, checked);
            return true;

        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFlipAction) {
            mFlipPref = Integer.parseInt((String) newValue);
            if (mFlipPref != -1) {
                mUserDownMS.setEnabled(true);
                mFlipScreenOff.setEnabled(true);
                mUserDownMS.setSummary(R.string.summary_down_sec);
                mFlipScreenOff.setSummary(R.string.summary_timeout_sec);
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
            } else {
                mUserDownMS.setEnabled(false);
                mFlipScreenOff.setEnabled(false);
                mUserDownMS.setSummary(R.string.enable_audio_mode);
                mFlipScreenOff.setSummary(R.string.enable_audio_mode);
            }
            flipServiceCheck();
            return true;
        } else if (preference == mAnnoyingNotifications) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(mContentRes,
                    Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, val);
            return true;
        } else if (preference == mPhoneSilent) {
            mCallPref = Integer.parseInt((String) newValue);
            flipServiceCheck();
            return true;
        } else if (preference == mVibtationIntensity) {
            String newVal = (String) newValue;
            int val = Integer.parseInt(newVal);
            Settings.System.putInt(mContentRes,
                    Settings.System.GENERIC_VIBRATE_INTENSITY, val);
            if ((val % 5 == 0) && mTactileFeedbackEnabled && vib != null) {
                vib.vibrate(10);
            }
            return true;
        }
        return false;
    }

    private void flipServiceCheck() {
        if (mCallPref != 0 || mFlipPref != -1) {
            mContext.startService(new Intent(mContext, FlipService.class));
        } else {
            mContext.stopService(new Intent(mContext, FlipService.class));
        }
    }
}
