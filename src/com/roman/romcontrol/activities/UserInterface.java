
package com.roman.romcontrol.activities;

import java.util.ArrayList;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.util.Log;
import android.view.IWindowManager;
import android.widget.EditText;

import com.android.internal.telephony.Phone;
import com.roman.romcontrol.R;

public class UserInterface extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new UserInterfacePreferences()).commit();
    }

    public class UserInterfacePreferences extends PreferenceFragment implements
            OnPreferenceChangeListener {

        private static final String PREF_CRT_ON = "crt_on";
        private static final String PREF_CRT_OFF = "crt_off";
        private static final String PREF_IME_SWITCHER = "ime_switcher";
        private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
        private static final String PREF_LONGPRESS_TO_KILL = "longpress_to_kill";
        private static final String PREF_ROTATION_ANIMATION = "rotation_animation_delay";
        private static final String PREF_HORIZONTAL_RECENTS = "horizontal_recents";

        CheckBoxPreference mCrtOnAnimation;
        CheckBoxPreference mCrtOffAnimation;
        CheckBoxPreference mShowImeSwitcher;
        CheckBoxPreference mLongPressToKill;
        CheckBoxPreference mHorizontalRecents;
        Preference mCustomLabel;
        ListPreference mAnimationRotationDelay;

        String mCustomLabelText = null;

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

            mHorizontalRecents = (CheckBoxPreference) findPreference(PREF_HORIZONTAL_RECENTS);
            mHorizontalRecents.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, 0) == 1);
            
            mAnimationRotationDelay = (ListPreference) findPreference(PREF_ROTATION_ANIMATION);
            mAnimationRotationDelay.setOnPreferenceChangeListener(this);
            mAnimationRotationDelay.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                    200) + "");

            // can't get this working in ICS just yet
            ((PreferenceGroup) findPreference("crt")).removePreference(mCrtOnAnimation);
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

            } else if (preference == mHorizontalRecents) {

                boolean checked = ((CheckBoxPreference) preference).isChecked();
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.HORIZONTAL_RECENTS_TASK_PANEL, checked ? 1 : 0);
                Log.d("WebAOKP", "Setting WebAOKP to");
                Log.d("WebAOKP", checked ? "True" : "False");
                return true;
            } 
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mAnimationRotationDelay) {

                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ACCELEROMETER_ROTATION_SETTLE_TIME,
                        Integer.parseInt((String) newValue));

                return true;
            }

            return false;
        }
    }

    public static void addButton(Context context, String key) {
        ArrayList<String> enabledToggles = NavbarLayout
                .getButtonsStringArray(context);
        enabledToggles.add(key);
        NavbarLayout.setButtonsFromStringArray(context, enabledToggles);
    }

    public static void removeButton(Context context, String key) {
        ArrayList<String> enabledToggles = NavbarLayout
                .getButtonsStringArray(context);
        enabledToggles.remove(key);
        NavbarLayout.setButtonsFromStringArray(context, enabledToggles);
    }

}
