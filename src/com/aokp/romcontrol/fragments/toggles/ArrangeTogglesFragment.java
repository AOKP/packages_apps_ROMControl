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

package com.aokp.romcontrol.fragments.toggles;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.aokp.romcontrol.R;
import com.google.android.apps.dashclock.ui.DragGripView;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;


public class ArrangeTogglesFragment extends Fragment {

    private static final String TAG = ArrangeTogglesFragment.class.getSimpleName();
    private static final String PREF_HANDLE_KEY = "toggles_arrange_right_handle";

    DragSortListView mListView;
    EnabledTogglesAdapter mAdapter;
    DragSortController mDragSortController;

    static Bundle sToggles;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("toggle_bundle")) {
                onTogglesUpdate(intent.getBundleExtra("toggle_bundle"));
                if (mAdapter == null) {
                    updateToggleList();
                    mListView.setAdapter(mAdapter = new EnabledTogglesAdapter(getActivity(),
                            toggles));
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    ArrayList<String> mToggles;

    ArrayList<String> toggles = new ArrayList<String>();

    public ArrangeTogglesFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toggle_setup, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_toggle:
                showAddTogglesDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAddTogglesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        ArrayList<String> availableToggles = sToggles.getStringArrayList("toggles");

        final ArrayList<String> userToggles = getEnabledToggles(getActivity());
        final ArrayList<String> togglesNotYetAdded = new ArrayList<String>();

        for (String availableToggle : availableToggles) {
            if (!userToggles.contains(availableToggle)) {
                togglesNotYetAdded.add(availableToggle);
            }
        }

        // final String[] finalArray = getResources().getStringArray(
        // R.array.available_toggles_entries);
        final ArrayList<String> toggleValuesArrayList = new ArrayList<String>();
        for (int i = 0; i < togglesNotYetAdded.size(); i++) {
            toggleValuesArrayList.add(togglesNotYetAdded.get(i));
        }


        builder.setTitle(R.string.toggle_dialog_add_toggles);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.toggles_display_close,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        EnabledTogglesAdapter adapter = new EnabledTogglesAdapter(getActivity(), toggleValuesArrayList);
        adapter.setShowDragGrips(false);
        builder.setSingleChoiceItems(adapter, -1, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                String toggleKey = togglesNotYetAdded.get(i);

                addToggle(getActivity(), toggleKey);
                updateToggleList();
                mAdapter.notifyDataSetChanged();
            }
        });
        AlertDialog d = builder.create();

        d.show();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver,
                new IntentFilter("com.android.systemui.statusbar.toggles.ACTION_BROADCAST_TOGGLES"));
        requestAvailableToggles();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        mListView.setAdapter(null);
        mAdapter = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)
                inflater.inflate(R.layout.fragment_arrange_toggles, container, false);

        mListView = (DragSortListView) rootView.findViewById(android.R.id.list);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    String name = toggles.remove(from);
                    toggles.add(to, name);
                    setTogglesFromStringArray(getActivity(), toggles);
                    updateToggleList();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        final SwipeDismissListViewTouchListener swipeOnTouchListener =
                new SwipeDismissListViewTouchListener(
                        mListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {

                            public boolean canDismiss(int position) {
                                return position < mAdapter.getCount();
                            }

                            public void onDismiss(ListView listView, int[]
                                    reverseSortedPositions) {
                                for (int index : reverseSortedPositions) {
                                    removeToggle(getActivity(),
                                            mAdapter.getItem(index));
                                }
                                updateToggleList();
                                mAdapter.notifyDataSetChanged();
                            }
                        });
        mListView.setFloatViewManager(mDragSortController = new ConfigurationDragSortController());
        mListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mDragSortController.onTouch(view, motionEvent)
                        || (!mDragSortController.isDragging()
                        && swipeOnTouchListener.onTouch(view, motionEvent));
            }
        });
        mListView.setOnScrollListener(swipeOnTouchListener.makeScrollListener());
        mListView.setItemsCanFocus(true);
        mListView.setDragEnabled(true);
        mListView.setFloatAlpha(0.8f);

        updateToggleList();
    }


    private void updateToggleList() {
        toggles.clear();
        for (String t : getEnabledToggles(getActivity())) {
            toggles.add(t);
        }
    }

    private class EnabledTogglesAdapter extends ArrayAdapter<String> {

        boolean mShowDragGrips = true;

        public EnabledTogglesAdapter(Context context, ArrayList<String> toggles) {
            super(context, android.R.id.text1, toggles);
        }

        public void setShowDragGrips(boolean show) {
            this.mShowDragGrips = show;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_toggle, parent, false);

            TextView titleView = (TextView) convertView.findViewById(android.R.id.text1);

            String text = lookupToggle(getItem(position));
            titleView.setText(text);

            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            DragGripView dragGripView = (DragGripView) convertView.findViewById(R.id.drag_handle);
            if (sToggles != null) {
                int anInt = sToggles.getInt("toggle_" + getItem(position) + "_image", 0);
                Drawable d = getActivity().getPackageManager().getDrawable("com.android.systemui", anInt, null);
                if (d != null) {
                    image.setImageDrawable(d);
                }

                dragGripView.setVisibility(mShowDragGrips ? View.VISIBLE : View.GONE);
            }
            return convertView;
        }
    }

    private class ConfigurationDragSortController extends DragSortController {

        public ConfigurationDragSortController() {
            super(ArrangeTogglesFragment.this.mListView, R.id.drag_handle,
                    DragSortController.ON_DRAG, (DragSortController.FLING_LEFT_REMOVE & DragSortController.FLING_RIGHT_REMOVE));
            setBackgroundColor(0x363636);
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = mAdapter.getView(position, null, ArrangeTogglesFragment.this.mListView);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
        }

    }


    private void requestAvailableToggles() {
        Intent request =
                new Intent("com.android.systemui.statusbar.toggles.ACTION_REQUEST_TOGGLES");
        getActivity().sendBroadcast(request);
    }

    private void onTogglesUpdate(Bundle toggleInfo) {
        mToggles = toggleInfo.getStringArrayList("toggles");
        sToggles = toggleInfo;
    }

    static ArrayList<String> getEnabledToggles(Context context) {
        try {
            ArrayList<String> userEnabledToggles = new ArrayList<String>();
            String userToggles = Settings.AOKP.getString(context.getContentResolver(),
                    Settings.AOKP.QUICK_TOGGLES);

            String[] splitter = userToggles.split("\\|");
            for (String toggle : splitter) {
                if (!toggle.trim().isEmpty()) {
                    userEnabledToggles.add(toggle);
                }
            }
            return userEnabledToggles;
        } catch (Exception e) {
            if (sToggles != null && sToggles.containsKey("default_toggles")) {
                return sToggles.getStringArrayList("default_toggles");
            }
        }
        return new ArrayList<String>();
    }

    static void setTogglesFromStringArray(Context c, List<String> enabledToggles) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < enabledToggles.size(); i++) {
            final String _toggle = enabledToggles.get(i).trim();
            if (_toggle.isEmpty()) {
                continue;
            }
            b.append(_toggle);
            b.append("|");
        }
        if (b.length() > 0) {
            if (b.charAt(b.length() - 1) == '|') {
                b.deleteCharAt(b.length() - 1);
            }
        }
        Log.d(TAG, "saving toggles:" + b.toString());
        Settings.AOKP.putString(c.getContentResolver(), Settings.AOKP.QUICK_TOGGLES,
                b.toString());
    }

    static void addToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getEnabledToggles(context);
        if (enabledToggles.contains(key)) {
            enabledToggles.remove(key);
        }
        enabledToggles.add(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    static void removeToggle(Context context, String key) {
        ArrayList<String> enabledToggles = getEnabledToggles(context);
        enabledToggles.remove(key);
        setTogglesFromStringArray(context, enabledToggles);
    }

    static String lookupToggle(String ident) {
        if (sToggles != null) {
            return sToggles.getString(ident.toUpperCase());
        }
        return ident;
    }

}
