
package com.roman.romcontrol.activities;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Spannable;
import android.util.Log;
import android.view.IWindowManager;
import android.widget.EditText;

import com.roman.romcontrol.R;

public class UserInterface extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new UserInterfacePreferences()).commit();
    }

    public static class UserInterfacePreferences extends PreferenceFragment implements
            OnPreferenceChangeListener {

        private static final String PREF_MENU_UNLOCK = "pref_menu_display";
        private static final String PREF_CRT_ON = "crt_on";
        private static final String PREF_CRT_OFF = "crt_off";
        private static final String PREF_IME_SWITCHER = "ime_switcher";
        private static final String PREF_NAVBAR_LAYOUT = "navbar_layout";
        private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
        private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
        private static final String PREF_LONGPRESS_TO_KILL = "longpress_to_kill";
        private static final String PREF_NAV_COLOR = "nav_button_color";

        ListPreference menuDisplayLocation;
        ListPreference navBarLayout;
        ListPreference mNavBarMenuDisplay;
        CheckBoxPreference mCrtOnAnimation;
        CheckBoxPreference mCrtOffAnimation;
        CheckBoxPreference mShowImeSwitcher;
        CheckBoxPreference mLongPressToKill;
        Preference mCustomLabel;
        ColorPickerPreference mNavigationBarColor;

        String mCustomLabelText = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_ui);

            PreferenceScreen prefs = getPreferenceScreen();

            menuDisplayLocation = (ListPreference) findPreference(PREF_MENU_UNLOCK);
            menuDisplayLocation.setOnPreferenceChangeListener(this);
            menuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.MENU_LOCATION,
                    0) + "");

            navBarLayout = (ListPreference) findPreference(PREF_NAVBAR_LAYOUT);
            navBarLayout.setOnPreferenceChangeListener(this);
            navBarLayout.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.NAVIGATION_BAR_LAYOUT,
                    0) + "");

            mCrtOffAnimation = (CheckBoxPreference) findPreference(PREF_CRT_OFF);
            mCrtOffAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.CRT_OFF_ANIMATION, 1) == 1);

            mCrtOnAnimation = (CheckBoxPreference) findPreference(PREF_CRT_ON);
            mCrtOnAnimation.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.CRT_ON_ANIMATION, 0) == 1);

            mShowImeSwitcher = (CheckBoxPreference) findPreference(PREF_IME_SWITCHER);
            mShowImeSwitcher.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_STATUSBAR_IME_SWITCHER, 0) == 1);

            mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
            mNavBarMenuDisplay.setOnPreferenceChangeListener(this);
            mNavBarMenuDisplay.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.MENU_VISIBILITY,
                    0) + "");

            mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
            updateCustomLabelTextSummary();

            mLongPressToKill = (CheckBoxPreference) findPreference(PREF_LONGPRESS_TO_KILL);
            mLongPressToKill.setChecked(Settings.Secure.getInt(getActivity().getContentResolver(),
                    Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) == 1);

            mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
            mNavigationBarColor.setOnPreferenceChangeListener(this);
            // remove navigation bar options
            IWindowManager mWindowManager = IWindowManager.Stub.asInterface(ServiceManager
                    .getService(Context.WINDOW_SERVICE));
            try {
                if (!mWindowManager.hasNavigationBar()) {
                    PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("nav_bar");
                    if (preferenceGroup != null) {
                        prefs.removePreference(preferenceGroup);
                    }
                } else {
                    // nav bar
                    // ((PreferenceGroup)
                    // findPreference("misc")).removePreference(mLongPressToKill);
                }
            } catch (RemoteException e) {
            }

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

            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == menuDisplayLocation) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
                return true;
            } else if (preference == navBarLayout) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_LAYOUT, Integer.parseInt((String) newValue));
                return true;
            } else if (preference == mNavBarMenuDisplay) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.MENU_VISIBILITY, Integer.parseInt((String) newValue));
                return true;
            } else if (preference == mNavigationBarColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                        .valueOf(newValue)));
                preference.setSummary(hex);

                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_TINT, intHex);
                Log.e("ROMAN", intHex + "");
            }
            return false;
        }
    }

}
