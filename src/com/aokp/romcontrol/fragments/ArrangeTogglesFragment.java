
package com.aokp.romcontrol.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aokp.romcontrol.R;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

public class ArrangeTogglesFragment extends DialogFragment implements OnItemClickListener {

    private static final String TAG = ArrangeTogglesFragment.class.getSimpleName();

    DragSortListView mListView;
    EnabledTogglesAdapter mAdapter;

    ArrayList<String> toggles = new ArrayList<String>();

    static ArrangeTogglesFragment getInstance(Bundle toggleInfo) {
        ArrangeTogglesFragment f = new ArrangeTogglesFragment();
        f.setArguments(toggleInfo);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (StatusBarToggles.sToggles == null) {
            StatusBarToggles.sToggles = getArguments();
        }
        setShowsDialog(true);
    }

    private void updateToggleList() {
        toggles.clear();
        for (String t : StatusBarToggles.getEnabledToggles(getActivity())) {
            toggles.add(t);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        /*
         * create view
         */
        ViewGroup rootView = (ViewGroup) View.inflate(getActivity(),
                R.layout.fragment_configure_toggles, null);

        updateToggleList();
        mListView = (DragSortListView) rootView.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter = new EnabledTogglesAdapter(getActivity(), toggles));

        final DragSortController dragSortController = new ConfigurationDragSortController();
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
        mListView.setOnItemClickListener(this);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return dragSortController.onTouch(view, motionEvent);
            }
        });
        mListView.setItemsCanFocus(true);

        /*
         * finish dialog
         */
        builder.setView(rootView);
        builder.setTitle(R.string.toggle_dialog_arrange_title);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }

    private class EnabledTogglesAdapter extends ArrayAdapter<String> {

        public EnabledTogglesAdapter(Context context, ArrayList<String> toggles) {
            super(context, android.R.id.text1, toggles);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_toggle, parent, false);
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
                    DragSortController.ON_DOWN, 0);
            setRemoveEnabled(false);
            setSortEnabled(true);
            setBackgroundColor(0x363636);
            sParams.height = getActivity().getResources().getDimensionPixelSize(
                    R.dimen.list_toggle_height);
            sParams.width = getActivity().getResources().getDimensionPixelSize(
                    R.dimen.list_toggle_width);
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
            floatView.setLayoutParams(sParams);
            ArrangeTogglesFragment.this.mListView.setFloatAlpha(0.8f);
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = mAdapter.getView(position, null, ArrangeTogglesFragment.this.mListView);
            v.setLayoutParams(sParams);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private static ViewGroup.LayoutParams sParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

}
