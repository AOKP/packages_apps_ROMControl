
package com.aokp.romcontrol.fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
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

import com.android.internal.util.aokp.NavBarHelpers;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.ROMControlActivity;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.Collections;

public class ArrangeRibbonFragment extends DialogFragment implements OnItemClickListener,
        OnCheckedChangeListener {

    private static final String TAG = ArrangeRibbonFragment.class.getSimpleName();
    private static final String PREF_HANDLE_KEY = "toggles_arrange_right_handle";

    ViewGroup rootView;
    Button mSave;
    Button mClose;
    Switch mToggle;
    DragSortListView mListView;
    EnabledTargetsAdapter mAdapter;
    ContentResolver mContentRes;
    Context mContext;
    int arrayNum;

    ArrayList<String> aTargets = new ArrayList<String>();
    ArrayList<String> sTargets = new ArrayList<String>();
    ArrayList<String> lTargets = new ArrayList<String>();
    ArrayList<String> cTargets = new ArrayList<String>();

    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);



    @Override
    public void onCreate(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Dialog_MinWidth);
        super.onCreate(savedInstanceState);
        setShowsDialog(true);

        params.width = getActivity().getResources().getDimensionPixelSize(
                R.dimen.list_toggle_width);
    }

    public void setResources(Context context, ContentResolver res, ArrayList<String> aList, ArrayList<String> sList,
            ArrayList<String> lList, ArrayList<String> cList, int num) {
        mContext = context;
        mContentRes = res;
        aTargets = aList;
        sTargets = sList;
        lTargets = lList;
        cTargets = cList;
        arrayNum = num;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        rootView = (ViewGroup)
                inflater.inflate(R.layout.fragment_configure_ribbon,
                        container, false);

        mListView = (DragSortListView) rootView.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter = new EnabledTargetsAdapter(getActivity(),
                aTargets));

        final DragSortController dragSortController = new
                ConfigurationDragSortController();
        mListView.setFloatViewManager(dragSortController);
        mListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                String aName = aTargets.remove(from);
                aTargets.add(to, aName);

                String sName = sTargets.remove(from);
                sTargets.add(to, sName);

                String lName = lTargets.remove(from);
                lTargets.add(to, lName);

                String cName = cTargets.remove(from);
                cTargets.add(to, cName);

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

        mClose = (Button) rootView.findViewById(R.id.close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent refreshRibbon = new Intent(RibbonTargets.RibbonDialogReceiver.ACTION_RIBBON_DIALOG_DISMISS);
                mContext.sendBroadcast(refreshRibbon);
                ArrangeRibbonFragment.this.dismiss();
            }
        });

        mSave = (Button) rootView.findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtons();
            }
        });
        return rootView;
    }

    private void saveButtons() {
        Settings.System.putArrayList(mContentRes, Settings.System.RIBBON_TARGETS_SHORT[arrayNum],
                sTargets);
        Settings.System.putArrayList(mContentRes, Settings.System.RIBBON_TARGETS_LONG[arrayNum],
                lTargets);
        Settings.System.putArrayList(mContentRes, Settings.System.RIBBON_TARGETS_ICONS[arrayNum],
                cTargets);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private class EnabledTargetsAdapter extends ArrayAdapter<String> {

        public EnabledTargetsAdapter(Context context, ArrayList<String> targets) {
            super(context, android.R.id.text1, targets);
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

            titleView.setText(mAdapter.getItem(position));
            descriptionView.setText(mAdapter.getItem(position));

            return convertView;
        }
    }

    private class ConfigurationDragSortController extends DragSortController {

        public ConfigurationDragSortController() {
            super(ArrangeRibbonFragment.this.mListView, R.id.drag_handle,
                    DragSortController.ON_DRAG, 0);
            setRemoveEnabled(false);
            setSortEnabled(true);
            setBackgroundColor(0x363636);
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
            floatView.setLayoutParams(params);
            ArrangeRibbonFragment.this.mListView.setFloatAlpha(0.8f);
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = mAdapter.getView(position, null, ArrangeRibbonFragment.this.mListView);
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
        ArrangeRibbonFragment f = new ArrangeRibbonFragment();
        f.setResources(mContext, mContentRes, aTargets, sTargets,
            lTargets, cTargets, arrayNum);
        dismiss();
        f.show(getFragmentManager(), getTag());
    }

}
