
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
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aokp.romcontrol.widgets.TouchInterceptor;
import com.aokp.romcontrol.R;

import java.util.ArrayList;

public class StatusBarToggles extends PreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "TogglesLayout";

    private static final String PREF_ENABLE_TOGGLES = "enable_toggles";
    private static final String PREF_BRIGHTNESS_LOC = "brightness_location";
    private static final String PREF_TOGGLES_STYLE = "toggle_style";
    private static final String PREF_ALT_BUTTON_LAYOUT = "alternate_button_layout";

    Preference mEnabledToggles;
    Preference mLayout;
    ListPreference mBrightnessLocation;
    CheckBoxPreference mAlternateButtonLayout;
    ListPreference mToggleStyle;
    Preference mResetToggles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_toggles);

        mEnabledToggles = findPreference(PREF_ENABLE_TOGGLES);

        mBrightnessLocation = (ListPreference) findPreference(PREF_BRIGHTNESS_LOC);
        mBrightnessLocation.setOnPreferenceChangeListener(this);
        mBrightnessLocation.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_BRIGHTNESS_LOC, 1)));

        mToggleStyle = (ListPreference) findPreference(PREF_TOGGLES_STYLE);
        mToggleStyle.setOnPreferenceChangeListener(this);
        mToggleStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_TOGGLES_STYLE, 3)));

        mAlternateButtonLayout = (CheckBoxPreference) findPreference(PREF_ALT_BUTTON_LAYOUT);
        mAlternateButtonLayout.setChecked(Settings.System.getInt(
                getActivity().getContentResolver(), Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS,
                0) == 1);

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

            builder.setTitle("Choose which toggles to use");
            builder.setCancelable(true);
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

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
        } else if (preference == mAlternateButtonLayout) {

            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_TOGGLES_USE_BUTTONS,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
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
            clusterfuck = "WIFI|BT|GPS|ROTATE";
        }

        String[] togglesStringArray = clusterfuck.split("\\|");
        ArrayList<String> iloveyou = new ArrayList<String>();
        for (String s : togglesStringArray) {
            Log.e(TAG, "adding: " + s);
            iloveyou.add(s);
        }

        return iloveyou;
    }

}
