
package com.aokp.romcontrol.fragments;

import android.app.DialogFragment;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.aokp.romcontrol.R;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

public class ArrangeTogglesFragment extends DialogFragment implements OnItemClickListener,
        OnCheckedChangeListener {

    private static final String TAG = ArrangeTogglesFragment.class.getSimpleName();
    private static final String PREF_HANDLE_KEY = "toggles_arrange_right_handle";

    Switch mToggle;
    DragSortListView mListView;
    EnabledTogglesAdapter mAdapter;

    private int mHeight = -1;

    ArrayList<String> toggles = new ArrayList<String>();

    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    static ArrangeTogglesFragment getInstance(Bundle toggleInfo) {
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
        params.height = getActivity().getResources().getDimensionPixelSize(
                R.dimen.list_toggle_height);
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
        mListView.setOnItemClickListener(this);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return dragSortController.onTouch(view, motionEvent);
            }
        });
        mListView.setItemsCanFocus(true);

        mToggle = (Switch) rootView.findViewById(R.id.handle_switch);
        mToggle.setChecked(useRightSideLayout());
        mToggle.setOnCheckedChangeListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Resources r = getActivity().getResources();
        int width = r.getDimensionPixelSize(R.dimen.list_toggle_width);

        Point display = new Point();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(display);

        final int titleHeight = Math.round(r
                .getDimensionPixelSize(R.dimen.toggle_rearrange_dialog_title_height) * 1.5f);
        final int rowHeight = Math.round(r.getDimensionPixelSize(R.dimen.list_toggle_height) * 1.2f);
        final int totalApproxDialogHeight = (mAdapter.getCount() * rowHeight)
                + (titleHeight)
                + (mToggle.getHeight());

        int height = Math.min(Math.round(display.y * 0.8f), totalApproxDialogHeight);

        getDialog().getWindow().setLayout(width, height);
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
                    DragSortController.ON_DOWN, 0);
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
        ArrangeTogglesFragment f = ArrangeTogglesFragment.getInstance(getArguments());
        f.show(getFragmentManager(), getTag());
    }

}
