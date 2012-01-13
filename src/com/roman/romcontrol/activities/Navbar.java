
package com.roman.romcontrol.activities;

import java.util.ArrayList;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.app.Activity;
import android.app.AlertDialog;
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

public class Navbar extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new NavbarPreferences()).commit();
    }

    public class NavbarPreferences extends PreferenceFragment implements
            OnPreferenceChangeListener {

        // move these later
        private static final String PREF_EANBLED_BUTTONS = "enabled_buttons";
        private static final String PREF_NAVBAR_MENU_DISPLAY = "navbar_menu_display";
        private static final String PREF_NAV_COLOR = "nav_button_color";
        private static final String PREF_MENU_UNLOCK = "pref_menu_display";
        private static final String PREF_HOME_LONGPRESS = "long_press_home";

        // move these later
        ColorPickerPreference mNavigationBarColor;
        ListPreference menuDisplayLocation;
        ListPreference mNavBarMenuDisplay;
        ListPreference mHomeLongpress;
        Preference mNavBarEnabledButtons;

        private final String[] buttons = {
                "HOME", "BACK", "TASKS", "SEARCH"
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.prefs_navbar);

            PreferenceScreen prefs = getPreferenceScreen();

            menuDisplayLocation = (ListPreference) findPreference(PREF_MENU_UNLOCK);
            menuDisplayLocation.setOnPreferenceChangeListener(this);
            menuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.MENU_LOCATION,
                    0) + "");

            mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
            mNavBarMenuDisplay.setOnPreferenceChangeListener(this);
            mNavBarMenuDisplay.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.MENU_VISIBILITY,
                    0) + "");

            mHomeLongpress = (ListPreference) findPreference(PREF_HOME_LONGPRESS);
            mHomeLongpress.setOnPreferenceChangeListener(this);
            mHomeLongpress.setValue(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.NAVIGATION_BAR_HOME_LONGPRESS,
                    0) + "");

            mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
            mNavigationBarColor.setOnPreferenceChangeListener(this);

            mNavBarEnabledButtons = findPreference(PREF_EANBLED_BUTTONS);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {

            if (preference == mNavBarEnabledButtons) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

                ArrayList<String> enabledToggles = NavbarLayout
                        .getButtonsStringArray(this.getActivity().getApplicationContext());

                boolean checkedToggles[] = new boolean[buttons.length];

                for (int i = 0; i < checkedToggles.length; i++) {
                    if (enabledToggles.contains(buttons[i])) {
                        checkedToggles[i] = true;
                    }
                }

                builder.setTitle("Choose which buttons to use");
                builder.setCancelable(false);
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setMultiChoiceItems(buttons,
                        checkedToggles,
                        new OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                String toggleKey = (buttons[which]);

                                if (isChecked)
                                    addButton(getApplicationContext(), toggleKey);
                                else
                                    removeButton(getApplicationContext(), toggleKey);
                            }
                        });

                AlertDialog d = builder.create();

                d.show();

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
                return true;

            } else if (preference == mHomeLongpress) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.NAVIGATION_BAR_HOME_LONGPRESS,
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
