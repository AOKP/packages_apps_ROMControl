/*
 * Copyright (C) 2011 The CyanogenMod Project
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

package com.roman.romcontrol.activities;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.roman.romcontrol.R;
import com.roman.romcontrol.widgets.TouchInterceptor;

public class NavbarLayout extends ListActivity {
    private static final String TAG = "NavbarLayout";

    private ListView mButtonList;
    private ButtonAdapter mButtonAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.order_power_widget_buttons_activity);

        mButtonList = getListView();
        ((TouchInterceptor) mButtonList).setDropListener(mDropListener);
        mButtonAdapter = new ButtonAdapter(this);
        setListAdapter(mButtonAdapter);
    }

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
            ArrayList<String> toggles = getButtonsStringArray(getApplicationContext());

            // move the button
            if (from < toggles.size()) {
                String toggle = toggles.remove(from);

                if (to <= toggles.size()) {
                    toggles.add(to, toggle);

                    // save our buttons
                    setButtonsFromStringArray(getApplicationContext(), toggles);

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
            ArrayList<String> toggles = getButtonsStringArray(getApplicationContext());

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

    public static void setButtonsFromStringArray(Context c, ArrayList<String> newGoodies) {
        String newToggles = "";

        for (String s : newGoodies)
            newToggles += s + "|";

        // remote last |
        newToggles = newToggles.substring(0, newToggles.length() - 1);

        Settings.System.putString(c.getContentResolver(), Settings.System.NAVIGATION_BAR_BUTTONS,
                newToggles);
    }

    public static ArrayList<String> getButtonsStringArray(Context c) {
        String clusterfuck = Settings.System.getString(c.getContentResolver(),
                Settings.System.NAVIGATION_BAR_BUTTONS);

        if (clusterfuck == null) {
            Log.e(TAG, "clusterfudge was null");
            clusterfuck = "BACK|HOME|TASKS";
        }

        String[] togglesStringArray = clusterfuck.split("\\|");
        ArrayList<String> iloveyou = new ArrayList<String>();
        for (String s : togglesStringArray) {
            Log.e(TAG, "adding: " + s);
            iloveyou.add(s);
        }

        return iloveyou;
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
}
