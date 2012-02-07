
package com.roman.romcontrol.fragments;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.roman.romcontrol.R;
import com.roman.romcontrol.SettingsPreferenceFragment;
import com.roman.romcontrol.util.CMDProcessor;
import com.roman.romcontrol.util.Helpers;

public class UserInterface extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String TAG = "UserInterface";

    private static final String PREF_CRT_ON = "crt_on";
    private static final String PREF_CRT_OFF = "crt_off";
    private static final String PREF_IME_SWITCHER = "ime_switcher";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_LONGPRESS_TO_KILL = "longpress_to_kill";
    private static final String PREF_ROTATION_ANIMATION = "rotation_animation_delay";
    private static final String PREF_180 = "rotate_180";

    private static final int DIALOG_DENSITY = 101;
    private static final int DIALOG_WARN_DENSITY = 102;

    CheckBoxPreference mCrtOnAnimation;
    CheckBoxPreference mCrtOffAnimation;
    CheckBoxPreference mShowImeSwitcher;
    CheckBoxPreference mLongPressToKill;
    CheckBoxPreference mAllow180Rotation;
    CheckBoxPreference mHorizontalAppSwitcher;
    Preference mCustomLabel;
    ListPreference mAnimationRotationDelay;
    ListPreference mLcdDensity;
    CheckBoxPreference mDisableBootAnimation;
    CheckBoxPreference mDisableBugMailer;

    String mCustomLabelText = null;
    int newDensityValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_ui);

        PreferenceScreen prefs = getPreferenceScreen();

        mCrtOffAnimation = (CheckBoxPreference) findPreference(PREF_CRT_OFF);
        mCrtOffAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CRT_OFF_ANIMATION, 1) == 1);

        mCrtOnAnimation = (CheckBoxPreference) findPreference(PREF_CRT_ON);
        mCrtOnAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.CRT_ON_ANIMATION, 0) == 1);

        mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
        mShowImeSwitcher.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_STATUSBAR_IME_SWITCHER, 0) == 1);

        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        mLongPressToKill = (CheckBoxPreference) findPreference(PREF_LONGPRESS_TO_KILL);
        mLongPressToKill.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) == 1);

        mAnimationRotationDelay = (ListPreference) findPreference(PREF_ROTATION_ANIMATION);
        mAnimationRotationDelay.setOnPreferenceChangeListener(this);
        mAnimationRotationDelay.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                200) + "");

        mAllow180Rotation = (CheckBoxPreference) findPreference(PREF_180);
        mAllow180Rotation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION_ANGLES, (1 | 2 | 8)) == (1 | 2 | 4 | 8));

        mHorizontalAppSwitcher = (CheckBoxPreference) findPreference("horizontal_recents_task_panel");
        mHorizontalAppSwitcher.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(),
                Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, 0) == 1);

        mLcdDensity = (ListPreference) findPreference("lcd_density");
        String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        if (currentProperty == null)
            currentProperty = "0";
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (NumberFormatException e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }

        mLcdDensity.setSummary(currentProperty);
        mLcdDensity.setOnPreferenceChangeListener(this);
        mLcdDensity.setValue(newDensityValue + "");

        mDisableBootAnimation = (CheckBoxPreference) findPreference("disable_bootanimation");
        mDisableBootAnimation.setChecked(!new File("/system/media/bootanimation.zip").exists());
        if (mDisableBootAnimation.isChecked())
            mDisableBootAnimation.setSummary(R.string.disable_bootanimation_summary);

        mDisableBugMailer = (CheckBoxPreference) findPreference("disable_bugmailer");
        mDisableBugMailer.setChecked(!new File("/system/bin/bugmailer.sh").exists());

        if (!getResources().getBoolean(com.android.internal.R.bool.config_enableCrtAnimations)) {
            prefs.removePreference((PreferenceGroup) findPreference("crt"));
        } else {
            // can't get this working in ICS just yet
            ((PreferenceGroup) findPreference("crt")).removePreference(mCrtOnAnimation);
        }
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null) {
            mCustomLabel
                    .setSummary("Custom label currently not set. Once you specify a custom one, there's no way back without doing a data wipe.");
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mCrtOffAnimation) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CRT_OFF_ANIMATION, checked ? 1 : 0);
            return true;

        } else if (preference == mCrtOnAnimation) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.CRT_ON_ANIMATION, checked ? 1 : 0);
            return true;
        } else if (preference == mShowImeSwitcher) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_STATUSBAR_IME_SWITCHER, checked ? 1 : 0);
            return true;

        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle("Custom Carrier Label");
            alert.setMessage("Please enter a new one!");

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        } else if (preference == mLongPressToKill) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.KILL_APP_LONGPRESS_BACK, checked ? 1 : 0);
            return true;

        } else if (preference == mAllow180Rotation) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_ANGLES, checked ? (1 | 2 | 4 | 8)
                            : (1 | 2 | 8));
            return true;

        } else if (preference == mHorizontalAppSwitcher) {

            boolean checked = ((CheckBoxPreference) preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, checked ? 1
                            : 0);
            new CMDProcessor().su.runWaitFor("pkill -TERM -f  com.android.systemui");
            return true;

        } else if (preference == mDisableBootAnimation) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.unicorn");
                Helpers.getMount("ro");
                preference.setSummary(R.string.disable_bootanimation_summary);
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/media/bootanimation.unicorn /system/media/bootanimation.zip");
                Helpers.getMount("ro");
            }
        } else if (preference == mDisableBugMailer) {
            boolean checked = ((CheckBoxPreference) preference).isChecked();
            if (checked) {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/bin/bugmailer.sh /system/bin/bugmailer.sh.unicorn");
                Helpers.getMount("ro");
            } else {
                Helpers.getMount("rw");
                new CMDProcessor().su
                        .runWaitFor("mv /system/bin/bugmailer.sh.unicorn /system/bin/bugmailer.sh");
                Helpers.getMount("ro");
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        LayoutInflater factory = LayoutInflater.from(mContext);

        switch (dialogId) {
            case DIALOG_DENSITY:
                final View textEntryView = factory.inflate(
                        R.layout.alert_dialog_text_entry, null);
                return new AlertDialog.Builder(getActivity())
                        .setTitle("Set custom density")
                        .setView(textEntryView)
                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EditText dpi = (EditText) textEntryView.findViewById(R.id.dpi_edit);
                                Editable text = dpi.getText();
                                Log.i(TAG, text.toString());

                                try {
                                    newDensityValue = Integer.parseInt(text.toString());
                                    showDialog(DIALOG_WARN_DENSITY);
                                } catch (Exception e) {
                                    mLcdDensity.setSummary("INVALID DENSITY!");
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                dialog.dismiss();
                            }
                        }).create();
            case DIALOG_WARN_DENSITY:
                return new AlertDialog.Builder(getActivity())
                        .setTitle("WARNING!")
                        .setMessage(
                                "Changing your LCD density can cause unexpected app behavior and cause incompatibility issues with the market. If this occurs, you need to change your density back, reboot, then clear your market data.")
                        .setCancelable(false)
                        .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setLcdDensity(newDensityValue);
                                dialog.dismiss();
                                mLcdDensity.setSummary(newDensityValue + "");

                            }
                        })
                        .setNegativeButton("Return to safety",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create();
        }
        return null;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAnimationRotationDelay) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                    Integer.parseInt((String) newValue));

            return true;
        } else if (preference == mLcdDensity) {
            String strValue = (String) newValue;
            if (strValue.equals("custom")) {
                showDialog(DIALOG_DENSITY);
                return true;
            } else {
                newDensityValue = Integer.parseInt((String) newValue);
                showDialog(DIALOG_WARN_DENSITY);
                return true;
            }
        }

        return false;
    }

    private void setLcdDensity(int newDensity) {
        Helpers.getMount("rw");
        new CMDProcessor().su.runWaitFor("busybox sed -i 's|ro.sf.lcd_density=.*|"
                + "ro.sf.lcd_density" + "=" + newDensity + "|' " + "/system/build.prop");
        Helpers.getMount("ro");
        Toast.makeText(getActivity().getApplicationContext(), "Reboot to see changes",
                Toast.LENGTH_LONG).show();

    }

    public static void addButton(Context context, String key) {
        ArrayList<String> enabledToggles = Navbar
                .getButtonsStringArray(context);
        enabledToggles.add(key);
        Navbar.setButtonsFromStringArray(context, enabledToggles);
    }

    public static void removeButton(Context context, String key) {
        ArrayList<String> enabledToggles = Navbar
                .getButtonsStringArray(context);
        enabledToggles.remove(key);
        Navbar.setButtonsFromStringArray(context, enabledToggles);
    }
}
