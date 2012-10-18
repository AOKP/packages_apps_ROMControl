
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

public class StatusBarToggles extends AOKPPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "TogglesLayout";

    private static final String PREF_ENABLE_TOGGLES = "enabled_toggles";
    private static final String PREF_TOGGLES_PER_ROW = "toggles_per_row";

    Preference mEnabledToggles;
    Preference mLayout;
    ListPreference mTogglesPerRow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_statusbar_toggles);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_statusbar_toggles);

        mEnabledToggles = findPreference(PREF_ENABLE_TOGGLES);

        mTogglesPerRow = (ListPreference) findPreference(PREF_TOGGLES_PER_ROW);
        mTogglesPerRow.setOnPreferenceChangeListener(this);
        mTogglesPerRow.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.QUICK_TOGGLES_PER_ROW, 3) + "");
        mLayout = findPreference("toggles");

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTogglesPerRow) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QUICK_TOGGLES_PER_ROW, val);
        }
        return false;
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
        } else if (preference == mLayout) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            TogglesLayout fragment = new TogglesLayout();
            ft.addToBackStack("toggles_layout");
            ft.replace(this.getId(), fragment);
            ft.commit();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);

    }

    public void addToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getTogglesStringArray(context);
        enabledToggles.add(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    public void removeToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getTogglesStringArray(context);
        enabledToggles.remove(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    public class TogglesLayout extends ListFragment {

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

    public void setTogglesFromStringArray(Context c, ArrayList<String> newGoodies) {
        String newToggles = "";

        for (String s : newGoodies)
            newToggles += s + "|";

        // remote last |
        try {
            newToggles = newToggles.substring(0, newToggles.length() - 1);
        } catch (StringIndexOutOfBoundsException e) {
        }

        Settings.System.putString(c.getContentResolver(), Settings.System.QUICK_TOGGLES,
                newToggles);
    }

    public ArrayList<String> getTogglesStringArray(Context c) {
        String clusterfuck = Settings.System.getString(c.getContentResolver(),
                Settings.System.QUICK_TOGGLES);

        if (clusterfuck == null) {
            Log.e(TAG, "clusterfuck was null");
            // return null;
            clusterfuck = getResources().getString(R.string.toggle_default_entries);
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