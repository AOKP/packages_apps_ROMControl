
package com.aokp.romcontrol.fragments;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.widgets.TouchInterceptor;
import com.aokp.romcontrol.widgets.SeekBarPreference;
import com.scheffsblend.smw.Preferences.ImageListPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;

public class StatusBarToggles extends AOKPPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "TogglesLayout";

    private static final String PREF_ENABLE_TOGGLES = "enabled_toggles";
    private static final String PREF_BRIGHTNESS_LOC = "brightness_location";
    private static final String PREF_TOGGLES_STYLE = "toggle_style";
    private static final String PREF_ALT_BUTTON_LAYOUT = "toggles_layout_preference";
    private static final String PREF_TOGGLE_BTN_ENABLED_COLOR = "toggle_btn_enabled_color";
    private static final String PREF_TOGGLE_BTN_DISABLED_COLOR = "toggle_btn_disabled_color";
    private static final String PREF_TOGGLE_BTN_ALPHA = "toggle_btn_alpha";
    private static final String PREF_TOGGLE_BTN_BACKGROUND = "toggle_btn_background";
    private static final String PREF_TOGGLE_TEXT_COLOR = "toggle_text_color";
    private static final String PREF_HAPTIC_FEEDBACK_TOGGLES_ENABLED = "toggles_haptic_feedback";
    private static final String PREF_SETTINGS_BUTTON_BEHAVIOR = "settings_behavior";
    private static final String PREF_TOGGLES_AUTOHIDE = "toggles_autohide";

    Preference mEnabledToggles;
    Preference mLayout;
    ListPreference mBrightnessLocation;
    ImageListPreference mTogglesLayout;
    ListPreference mToggleStyle;
    Preference mResetToggles;
    SeekBarPreference mToggleBtnAlpha;
    SeekBarPreference mBtnBackground;
    ColorPickerPreference mBtnEnabledColor;
    ColorPickerPreference mBtnDisabledColor;
    ColorPickerPreference mToggleTextColor;
    CheckBoxPreference mHapticFeedback;
    CheckBoxPreference mDefaultSettingsButtonBehavior;
    CheckBoxPreference mTogglesAutoHide;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_toggles);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_toggles);

        mHapticFeedback = (CheckBoxPreference) findPreference(PREF_HAPTIC_FEEDBACK_TOGGLES_ENABLED);
        mHapticFeedback.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_TOGGLES_ENABLED, false));

        mEnabledToggles = findPreference(PREF_ENABLE_TOGGLES);

        mBrightnessLocation = (ListPreference) findPreference(PREF_BRIGHTNESS_LOC);
        mBrightnessLocation.setOnPreferenceChangeListener(this);
        mBrightnessLocation.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC, 3)));

        int mTabletui = Settings.System.getInt(mContext.getContentResolver(),
                           Settings.System.TABLET_UI, 0);

        if (mTabletui == 1) {
            ((PreferenceGroup) findPreference("advanced_cat")).removePreference(mBrightnessLocation);
        }

        mToggleStyle = (ListPreference) findPreference(PREF_TOGGLES_STYLE);
        mToggleStyle.setOnPreferenceChangeListener(this);
        mToggleStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_STYLE, 3)));

        mTogglesLayout = (ImageListPreference) findPreference(PREF_ALT_BUTTON_LAYOUT);
        mTogglesLayout.setOnPreferenceChangeListener(this);

        mBtnEnabledColor = (ColorPickerPreference) findPreference(
                PREF_TOGGLE_BTN_ENABLED_COLOR);
        mBtnEnabledColor.setOnPreferenceChangeListener(this);

        mBtnDisabledColor = (ColorPickerPreference) findPreference(
                PREF_TOGGLE_BTN_DISABLED_COLOR);
        mBtnDisabledColor.setOnPreferenceChangeListener(this);

        float btnAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(),
                Settings.System.STATUSBAR_TOGGLES_ALPHA, 0.7f);
        mToggleBtnAlpha = (SeekBarPreference) findPreference(PREF_TOGGLE_BTN_ALPHA);
        mToggleBtnAlpha.setInitValue((int) (btnAlpha * 100));
        mToggleBtnAlpha.setOnPreferenceChangeListener(this);

        float btnBgAlpha = Settings.System.getFloat(getActivity()
                .getContentResolver(),
                Settings.System.STATUSBAR_TOGGLES_BACKGROUND, 0.0f);
        mBtnBackground = (SeekBarPreference) findPreference(PREF_TOGGLE_BTN_BACKGROUND);
        mBtnBackground.setInitValue((int) (btnBgAlpha * 100));
        mBtnBackground.setOnPreferenceChangeListener(this);

        mDefaultSettingsButtonBehavior = (CheckBoxPreference) findPreference(PREF_SETTINGS_BUTTON_BEHAVIOR);
        mDefaultSettingsButtonBehavior.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.STATUSBAR_SETTINGS_BEHAVIOR, true));
            if (mDefaultSettingsButtonBehavior.isChecked()) {
                mDefaultSettingsButtonBehavior.setSummary(R.string.summary_settings_behavior_default);
            } else {
                mDefaultSettingsButtonBehavior.setSummary(R.string.summary_settings_behavior_reverse);
            }

        mTogglesAutoHide = (CheckBoxPreference) findPreference(PREF_TOGGLES_AUTOHIDE);
        mTogglesAutoHide.setChecked(Settings.System.getBoolean(mContext
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_AUTOHIDE, false));

        mToggleTextColor = (ColorPickerPreference) findPreference(
                PREF_TOGGLE_TEXT_COLOR);
        mToggleTextColor.setOnPreferenceChangeListener(this);

        mLayout = findPreference("toggles");

        mResetToggles = findPreference("reset_toggles");

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnabledToggles) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            ArrayList<String> enabledToggles = getTogglesStringArray(getActivity());

            final String[] finalArray = getResources().getStringArray(
                    R.array.available_toggles_entries);
            final String[] values = getResources().getStringArray(R.array.available_toggles_values);

            boolean checkedToggles[] = new boolean[finalArray.length];

            for (int i = 0; i < checkedToggles.length; i++) {
                if (enabledToggles.contains(finalArray[i])) {
                    checkedToggles[i] = true;
                }
            }

            builder.setTitle(R.string.toggles_display_dialog);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.toggles_display_close,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setMultiChoiceItems(values, checkedToggles, new OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    String toggleKey = (finalArray[which]);

                    if (isChecked)
                        addToggle(getActivity(), toggleKey);
                    else
                        removeToggle(getActivity(), toggleKey);
                }
            });

            AlertDialog d = builder.create();

            d.show();

            return true;
        } else if (preference == mHapticFeedback) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_TOGGLES_ENABLED,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mLayout) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            TogglesLayout fragment = new TogglesLayout();
            ft.addToBackStack("toggles_layout");
            ft.replace(this.getId(), fragment);
            ft.commit();

        } else if (preference == mResetToggles) {
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES, "WIFI");
            return true;
        } else if (preference == mTogglesAutoHide) {

            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_AUTOHIDE,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            return true;
        } else if (preference == mDefaultSettingsButtonBehavior) {

            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.STATUSBAR_SETTINGS_BEHAVIOR,
                    ((CheckBoxPreference) preference).isChecked() ? true : false);
            if (mDefaultSettingsButtonBehavior.isChecked()) {
                mDefaultSettingsButtonBehavior.setSummary(R.string.summary_settings_behavior_default);
            } else {
                mDefaultSettingsButtonBehavior.setSummary(R.string.summary_settings_behavior_reverse);
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mBrightnessLocation) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC, val);
        } else if (preference == mToggleStyle) {
            int val = Integer.parseInt((String) newValue);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_STYLE, val);
        } else if (preference == mTogglesLayout) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_STYLE, val == 0 ? 3 : 2);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS,
                    val);
        } else if (preference == mBtnEnabledColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_ENABLED_COLOR, intHex);
        } else if (preference == mBtnDisabledColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_DISABLED_COLOR, intHex);
        } else if (preference == mToggleTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            result = Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_TEXT_COLOR, intHex);
        } else if (preference == mToggleBtnAlpha) {
            float val = Float.parseFloat((String) newValue);
            result = Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_ALPHA, val / 100);
        } else if (preference == mBtnBackground) {
            float val = Float.parseFloat((String) newValue);
            result = Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_BACKGROUND, val / 100);
        }
        return result;
    }

    public static void addToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getTogglesStringArray(context);
        enabledToggles.add(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    public static void removeToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getTogglesStringArray(context);
        enabledToggles.remove(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    public static class TogglesLayout extends ListFragment {

        private ListView mButtonList;
        private ButtonAdapter mButtonAdapter;
        private Context mContext;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            mContext = getActivity().getBaseContext();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(R.layout.order_power_widget_buttons_activity, container,
                    false);

            return v;
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mButtonList = this.getListView();
            ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
            mButtonAdapter = new ButtonAdapter(mContext);
            setListAdapter(mButtonAdapter);
        };

        @Override
        public void onDestroy() {
            ((TouchInterceptor) mButtonList).setDropListener(null);
            setListAdapter(null);
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            // reload our buttons and invalidate the views for redraw
            mButtonAdapter.reloadButtons();
            mButtonList.invalidateViews();
        }

        private TouchInterceptor.DropListener mDropListener = new TouchInterceptor.DropListener() {
            public void drop(int from, int to) {
                // get the current button list
                ArrayList<String> toggles = getTogglesStringArray(mContext);

                // move the button
                if (from < toggles.size()) {
                    String toggle = toggles.remove(from);

                    if (to <= toggles.size()) {
                        toggles.add(to, toggle);

                        // save our buttons
                        setTogglesFromStringArray(mContext, toggles);

                        // tell our adapter/listview to reload
                        mButtonAdapter.reloadButtons();
                        mButtonList.invalidateViews();
                    }
                }
            }
        };

        private class ButtonAdapter extends BaseAdapter {
            private Context mContext;
            private Resources mSystemUIResources = null;
            private LayoutInflater mInflater;
            private ArrayList<Toggle> mToggles;

            public ButtonAdapter(Context c) {
                mContext = c;
                mInflater = LayoutInflater.from(mContext);

                PackageManager pm = mContext.getPackageManager();
                if (pm != null) {
                    try {
                        mSystemUIResources = pm.getResourcesForApplication("com.android.systemui");
                    } catch (Exception e) {
                        mSystemUIResources = null;
                        Log.e(TAG, "Could not load SystemUI resources", e);
                    }
                }

                reloadButtons();
            }

            public void reloadButtons() {
                ArrayList<String> toggles = getTogglesStringArray(mContext);

                mToggles = new ArrayList<Toggle>();
                for (String toggle : toggles) {
                    mToggles.add(new Toggle(toggle, 0));
                }
            }

            public int getCount() {
                return mToggles.size();
            }

            public Object getItem(int position) {
                return mToggles.get(position);
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                final View v;
                if (convertView == null) {
                    v = mInflater.inflate(R.layout.order_power_widget_button_list_item, null);
                } else {
                    v = convertView;
                }

                Toggle toggle = mToggles.get(position);
                final TextView name = (TextView) v.findViewById(R.id.name);
                name.setText(toggle.getId());
                return v;
            }
        }

    }

    public static class Toggle {
        private String mId;
        private int mTitleResId;

        public Toggle(String id, int titleResId) {
            mId = id;
            mTitleResId = titleResId;
        }

        public String getId() {
            return mId;
        }

        public int getTitleResId() {
            return mTitleResId;
        }
    }

    public static void setTogglesFromStringArray(Context c, ArrayList<String> newGoodies) {
        String newToggles = "";

        for (String s : newGoodies)
            newToggles += s + "|";

        // remote last |
        try {
            newToggles = newToggles.substring(0, newToggles.length() - 1);
        } catch (StringIndexOutOfBoundsException e) {
        }

        Settings.System.putString(c.getContentResolver(), Settings.System.STATUSBAR_TOGGLES,
                newToggles);
    }

    public static ArrayList<String> getTogglesStringArray(Context c) {
        String clusterfuck = Settings.System.getString(c.getContentResolver(),
                Settings.System.STATUSBAR_TOGGLES);

        if (clusterfuck == null) {
            Log.e(TAG, "clusterfuck was null");
            // return null;
            clusterfuck = "WIFI|BT|GPS|ROTATE|SWAGGER|VIBRATE|SYNC|SILENT";
        }

        String[] togglesStringArray = clusterfuck.split("\\|");
        ArrayList<String> iloveyou = new ArrayList<String>();
        for (String s : togglesStringArray) {
            if(s != null && s != "") {
                Log.e(TAG, "adding: " + s);
                iloveyou.add(s);
            }
        }
        return iloveyou;
    }
}
