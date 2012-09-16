package com.aokp.romcontrol.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.util.NavRingPreference;

import java.util.ArrayList;
import java.util.List;

public class NavRingTargets extends AOKPPreferenceFragment 
        implements Preference.OnPreferenceChangeListener {
    Context mContext;
    ContentResolver mResolver;
    LayoutInflater mInflate;
    Preference mRingGroup1;
    Preference mRingGroup2;
    Preference mRingGroup3;
    Preference mRingGroup4;
    Preference mRingGroup5;
    NavRingPreference mRing1;
    NavRingPreference mRing2;
    NavRingPreference mRing3;
    NavRingPreference mRing4;
    NavRingPreference mRing5;
    Resources mRes;
    public List<AppPackage> components;
    public AppArrayAdapter mAdapter;
    public ListView mListView;
    private int mNavRingAmount;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (Context) getActivity();
        mRes = mContext.getResources();
        mResolver = mContext.getContentResolver();
        populateActionAdapter();
        PreferenceScreen prefs = getPreferenceScreen();

        addPreferencesFromResource(R.xml.navring_settings);

        mRing1 = (NavRingPreference) findPreference("interface_navring_1_release");
        mRing1.setTargetUri(Settings.System.SYSTEMUI_NAVRING_1, new WidgetListener());

        mRing2 = (NavRingPreference) findPreference("interface_navring_2_release");
        mRing2.setTargetUri(Settings.System.SYSTEMUI_NAVRING_2, new WidgetListener());

        mRing3 = (NavRingPreference) findPreference("interface_navring_3_release");
        mRing3.setTargetUri(Settings.System.SYSTEMUI_NAVRING_3, new WidgetListener());

        mRing4 = (NavRingPreference) findPreference("interface_navring_4_release");
        mRing4.setTargetUri(Settings.System.SYSTEMUI_NAVRING_4, new WidgetListener());

        mRing5 = (NavRingPreference) findPreference("interface_navring_5_release");
        mRing5.setTargetUri(Settings.System.SYSTEMUI_NAVRING_5, new WidgetListener());

        mNavRingAmount = Settings.System.getInt(mContext.getContentResolver(),
                         Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1);
        
        PreferenceGroup RingGroup1 = (PreferenceGroup) findPreference("interface_navring_1");
        PreferenceGroup RingGroup2 = (PreferenceGroup) findPreference("interface_navring_2");
        PreferenceGroup RingGroup4 = (PreferenceGroup) findPreference("interface_navring_4");
        PreferenceGroup RingGroup5 = (PreferenceGroup) findPreference("interface_navring_5");

        if (mNavRingAmount == 1) {
        RingGroup1.removeAll();
        RingGroup2.removeAll();
        RingGroup4.removeAll();
        RingGroup5.removeAll();
        } else if (mNavRingAmount == 2) {
        RingGroup1.removeAll();
        RingGroup4.removeAll();
        RingGroup5.removeAll();
        } else if (mNavRingAmount == 3) {
        RingGroup1.removeAll();
        RingGroup5.removeAll();
        } else if (mNavRingAmount == 4) {
        RingGroup5.removeAll();
        }

        String target3 = Settings.System.getString(mContext.getContentResolver(), Settings.System.SYSTEMUI_NAVRING_3);
        if (target3 == null || target3.equals("")) {
            Settings.System.putString(mContext.getContentResolver(), Settings.System.SYSTEMUI_NAVRING_3, "assist");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    class WidgetListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            String tag = (String) v.getTag();
            NavRingPreference n = null;
            if (tag != null) {
                if (tag.equals(mRing1.getTargetUri())) {
                    n = mRing1;
                } else if (tag.equals(mRing2.getTargetUri())) {
                    n = mRing2;
                } else if (tag.equals(mRing3.getTargetUri())) {
                    n = mRing3;
                } else if (tag.equals(mRing4.getTargetUri())) {
                    n = mRing4;
                } else if (tag.equals(mRing5.getTargetUri())) {
                    n = mRing5;
                }
                callInitDialog(n);
            }
        }
    }

    public void callInitDialog(final NavRingPreference preference) {
        final NavRingPreference pref = (NavRingPreference) preference;
        final CharSequence[] item_entries = mRes.getStringArray(R.array.navring_dialog_entries);
        final CharSequence[] item_values = mRes.getStringArray(R.array.navring_dialog_values);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mRes.getString(R.string.interface_softkeys_activity_dialog_title))
                .setNegativeButton(mRes.getString(com.android.internal.R.string.cancel),
                        new Dialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        })
                .setItems(item_entries, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        String pressed = (String) item_values[which];
                        if (pressed.equals(item_values[item_values.length - 1])) {
                            callActivityDialog(pref);
                        } else {
                            pref.loadCustomApp(pressed);
                            pref.setTargetValue(pressed);

                        }
                    }
                }).create().show();
    }

    public void populateActionAdapter() {
        components = new ArrayList<AppPackage>();

        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo info : activities) {
            AppPackage ap = new AppPackage(info, pm);
            components.add(ap);
        }
        mAdapter = new AppArrayAdapter(mContext, components);
        View dialog = View.inflate(mContext, R.layout.activity_dialog, null);
        mListView = (ListView) dialog.findViewById(R.id.dialog_list);
        mListView.setAdapter(mAdapter);
    }

    public void callActivityDialog(final NavRingPreference caller) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setAdapter(mListView.getAdapter(), new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                AppPackage app = (AppPackage) mListView.getAdapter().getItem(which);
                caller.setResourcesFromPackage(app);
            }
        })
                .setTitle(mRes.getString(R.string.interface_softkeys_activity_dialog_title))
                .setNegativeButton(mRes.getString(com.android.internal.R.string.cancel),
                        new Dialog.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        })
                .create().show();
    }

    public class AppPackage {
        public ComponentName component;
        public String appName;
        public Drawable icon;

        AppPackage(ResolveInfo ri, PackageManager pm) {
            component = new ComponentName(ri.activityInfo.packageName,
                    ri.activityInfo.name);
            appName = ri.activityInfo.loadLabel(pm).toString();
            icon = ri.activityInfo.loadIcon(pm);
        }

        public String getComponentName() {
            return component.flattenToString();
        }

        public Drawable getIcon() {
            return icon;
        }

        public String getName() {
            return appName;
        }
    }

    public class AppArrayAdapter extends ArrayAdapter {
        public final List<AppPackage> apps;
        public final Context mContext;

        public AppArrayAdapter(Context context, List<AppPackage> objects) {
            super(context, R.layout.activity_item, objects);
            this.mContext = context;
            this.apps = objects;
            // TODO Auto-generated constructor stub
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View itemRow = convertView;
            AppPackage ap = (AppPackage) apps.get(position);

            itemRow = ((LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.activity_item, null);

            ((ImageView) itemRow.findViewById(R.id.icon)).setImageDrawable(ap.getIcon());
            ((TextView) itemRow.findViewById(R.id.title)).setText(ap.getName());

            return itemRow;
        }
    }
}
