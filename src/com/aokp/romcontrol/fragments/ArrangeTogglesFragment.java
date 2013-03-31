
package com.aokp.romcontrol.fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;

public class ArrangeTogglesFragment extends DialogFragment implements OnItemClickListener,
        OnCheckedChangeListener {

    private static final String TAG = ArrangeTogglesFragment.class.getSimpleName();
    private static final String PREF_HANDLE_KEY = "toggles_arrange_right_handle";

    Button mAddToggles;
    Button mClose;
    Switch mToggle;
    DragSortListView mListView;
    EnabledTogglesAdapter mAdapter;

    ArrayList<String> toggles = new ArrayList<String>();

    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    static ArrangeTogglesFragment newInstance(Bundle toggleInfo) {
        ArrangeTogglesFragment f = new ArrangeTogglesFragment();
        f.setArguments(toggleInfo);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Dialog_MinWidth);
        super.onCreate(savedInstanceState);
        setShowsDialog(true);

        if (StatusBarToggles.sToggles == null) {
            StatusBarToggles.sToggles = getArguments();
        }
        params.width = getActivity().getResources().getDimensionPixelSize(
                R.dimen.list_toggle_width);
    }

    private void updateToggleList() {
        toggles.clear();
        for (String t : StatusBarToggles.getEnabledToggles(getActivity())) {
            toggles.add(t);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup)
                inflater.inflate(R.layout.fragment_configure_toggles,
                        container, false);

        updateToggleList();
        mListView = (DragSortListView) rootView.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter = new EnabledTogglesAdapter(getActivity(),
                toggles));

        final DragSortController dragSortController = new
                ConfigurationDragSortController();
        mListView.setFloatViewManager(dragSortController);
        mListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                String name = toggles.remove(from);
                toggles.add(to, name);
                StatusBarToggles.setTogglesFromStringArray(getActivity(), toggles);
                mAdapter.notifyDataSetChanged();
            }
        });
        // final SwipeDismissListViewTouchListener swipeDismissTouchListener =
        // new SwipeDismissListViewTouchListener(
        // mListView,
        // new SwipeDismissListViewTouchListener.DismissCallbacks() {
        // public boolean canDismiss(int position) {
        // return position < mAdapter.getCount();
        // }
        //
        // public void onDismiss(ListView listView, int[]
        // reverseSortedPositions) {
        // for (int index : reverseSortedPositions) {
        // StatusBarToggles.removeToggle(getActivity(),
        // mAdapter.getItem(index));
        // }
        // updateToggleList();
        // mAdapter.notifyDataSetChanged();
        // }
        // });
        mListView.setOnItemClickListener(this);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return dragSortController.onTouch(view, motionEvent);
                // || (!dragSortController.isDragging()
                // && swipeDismissTouchListener.onTouch(view, motionEvent));
            }
        });
        mListView.setItemsCanFocus(true);

        mToggle = (Switch) rootView.findViewById(R.id.handle_switch);
        mToggle.setChecked(useRightSideLayout());
        mToggle.setOnCheckedChangeListener(this);

        mClose = (Button) rootView.findViewById(R.id.close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrangeTogglesFragment.this.dismiss();
            }
        });

        mAddToggles = (Button) rootView.findViewById(R.id.add_toggles);
        mAddToggles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToggleSelectionDialog();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private class EnabledTogglesAdapter extends ArrayAdapter<String> {

        public EnabledTogglesAdapter(Context context, ArrayList<String> toggles) {
            super(context, android.R.id.text1, toggles);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(useRightSideLayout()
                                ? R.layout.list_item_toggle
                                : R.layout.list_item_toggle_left,
                                parent, false);
            }

            TextView titleView = (TextView) convertView.findViewById(android.R.id.text1);
            TextView descriptionView = (TextView) convertView
                    .findViewById(android.R.id.text2);

            titleView.setText(StatusBarToggles.lookupToggle(getActivity(),
                    mAdapter.getItem(position)));
            descriptionView.setText(mAdapter.getItem(position));

            return convertView;
        }
    }

    private class ConfigurationDragSortController extends DragSortController {

        public ConfigurationDragSortController() {
            super(ArrangeTogglesFragment.this.mListView, R.id.drag_handle,
                    DragSortController.ON_DRAG, 0);
            setRemoveEnabled(false);
            setSortEnabled(true);
            setBackgroundColor(0x363636);
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
            floatView.setLayoutParams(params);
            ArrangeTogglesFragment.this.mListView.setFloatAlpha(0.8f);
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = mAdapter.getView(position, null, ArrangeTogglesFragment.this.mListView);
            v.setLayoutParams(params);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
        }

    }

    private void showToggleSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final ArrayList<String> allToggles = StatusBarToggles.sToggles
                .getStringArrayList("toggles");
        Collections.sort(allToggles);

        // build arrays for dialog
        final String items[] = new String[allToggles.size()];
        final String itemStrings[] = new String[allToggles.size()];
        final boolean checkedItems[] = new boolean[allToggles.size()];

        // set strings
        for (int i = 0; i < items.length; i++) {
            items[i] = allToggles.get(i);
            itemStrings[i] = StatusBarToggles.lookupToggle(getActivity(), items[i]);
        }

        // check current toggles
        for (int i = 0; i < checkedItems.length; i++) {
            checkedItems[i] = toggles.contains(items[i]);
        }

        builder.setTitle(R.string.toggle_dialog_add_toggles);
        builder.setCancelable(true);
        builder.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                updateToggleList();
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setPositiveButton(R.string.back, null);
        builder.setMultiChoiceItems(itemStrings, checkedItems,
                new OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String toggleKey = allToggles.get(which);
                        if (isChecked)
                            StatusBarToggles.addToggle(getActivity(), toggleKey);
                        else
                            StatusBarToggles.removeToggle(getActivity(), toggleKey);
                    }
                });
        AlertDialog d = builder.create();
        d.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private boolean useRightSideLayout() {
        return getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(PREF_HANDLE_KEY, true);
    }

    private void setUseRightSideHandle(boolean right) {
        getActivity().getPreferences(Context.MODE_PRIVATE).edit()
                .putBoolean(PREF_HANDLE_KEY, right).commit();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setUseRightSideHandle(isChecked);
        dismiss();
        ArrangeTogglesFragment f = ArrangeTogglesFragment.newInstance(getArguments());
        f.show(getFragmentManager(), getTag());
    }

}
