/*
 * Copyright (C) 2013 The Android Open Kang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;

import java.util.ArrayList;

public class AutoImmersiveSettingsFragment extends Fragment {

    private Context mContext;

    private ArrayList<String> appsPackageList = new ArrayList<String>();
    private ArrayList<String> appsNameList = new ArrayList<String>();
    private ArrayAdapter<String> mArrayAdapter;

    public AutoImmersiveSettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_auto_immersive_settings, container, false);
        mContext = getActivity();
        appsPackageList = Settings.AOKP.getArrayList(mContext.getContentResolver(),
                Settings.AOKP.KEY_AUTO_IMMERSIVE_ARRAY);
        appsNameList = generateApplicationsNameList(appsPackageList);
        ListView mAppsListView = (ListView) v.findViewById(R.id.listview_auto_immersive);
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        mAppsListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mArrayAdapter.remove(mArrayAdapter.getItem(position));
                                    appsPackageList.remove(position);
                                }
                                mArrayAdapter.notifyDataSetChanged();
                                Settings.AOKP.putArrayList(mContext.getContentResolver(),
                                        Settings.AOKP.KEY_AUTO_IMMERSIVE_ARRAY, appsPackageList);
                            }
                        });
        mAppsListView.setOnTouchListener(touchListener);
        mAppsListView.setOnScrollListener(touchListener.makeScrollListener());
        mArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, appsNameList);
        mAppsListView.setAdapter(mArrayAdapter);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.auto_immersive, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_app:
                showAppChooserDialog();
                return true;
            default:
                return false;
        }
    }

    private void showAppChooserDialog() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
        this.startActivityForResult(pickIntent, ShortcutPickerHelper.REQUEST_PICK_APPLICATION);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ShortcutPickerHelper.REQUEST_PICK_APPLICATION:
                    ComponentName componentName = data.getComponent();
                    final String packageName = componentName.getPackageName();
                    if (!appsPackageList.contains(packageName)) {
                        appsPackageList.add(packageName);
                        appsNameList.add(getApplicationName(mContext.getPackageManager(), packageName));
                        Settings.AOKP.putArrayList(mContext.getContentResolver(), Settings.AOKP.KEY_AUTO_IMMERSIVE_ARRAY, appsPackageList);
                        mArrayAdapter.notifyDataSetChanged();
                        break;
                    }
            }
        }
    }

    private ArrayList<String> generateApplicationsNameList (ArrayList<String> appsPackageList) {

        final PackageManager pm = mContext.getPackageManager();
        ArrayList<String> appsNames = new ArrayList<String>();
        for (String mPackage : appsPackageList) {
            appsNames.add(getApplicationName(pm, mPackage));
        }
        return appsNames;
    }

    private String getApplicationName(PackageManager pm, String packageName) {
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : packageName);
    }
}