
package com.aokp.romcontrol.fragments.ribbons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.internal.util.aokp.AokpRibbonHelper;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.NavBarHelpers;
import com.android.internal.util.aokp.NavRingHelpers;
import com.android.internal.util.aokp.RibbonAdapter;
import com.android.internal.util.aokp.RibbonAdapter.RibbonItem;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.fragments.NavRingTargets;
import com.aokp.romcontrol.fragments.NavRingTargets.DialogConstant;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.google.android.apps.dashclock.ui.DragGripView;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public class LockscreenRibbonItems extends Fragment implements ShortcutPickerHelper.OnPickListener,
        OnItemClickListener {

    private static final String TAG = LockscreenRibbonItems.class.getSimpleName();
    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;

    private Context mContext;
    private String mRibbon;
    String[] SETTINGS_AOKP = Settings.AOKP.AOKP_LOCKSCREEN_RIBBON;
    DragSortListView mListView;
    EnabledTogglesAdapter mAdapter;
    DragSortController mDragSortController;
    private ShortcutPickerHelper mPicker;
    private String[] mActions;
    private String[] mActionCodes;
    private boolean mIsLongPress;
    private int mTargetIndex = -1;

    ArrayList<RibbonItem> items = new ArrayList<RibbonItem>();

    public LockscreenRibbonItems() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_ribbon, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_ribbon:
                mTargetIndex = -1;
                createDialog(getResources().getString(R.string.choose_action_short_title),
                        mActions, mActionCodes);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRibbonList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        mActionCodes = NavRingHelpers.getNavRingActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext,
                    mActionCodes[i]);
        }
        mPicker = new ShortcutPickerHelper(this, this);
        ViewGroup rootView = (ViewGroup)
                inflater.inflate(R.layout.fragment_ribbons_item, container, false);

        mListView = (DragSortListView) rootView.findViewById(android.R.id.list);
        mListView.setOnItemClickListener(this);
        updateRibbonList();
        mListView.setAdapter(mAdapter = new EnabledTogglesAdapter(mContext, items));
        mAdapter.notifyDataSetChanged();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    RibbonItem name = items.remove(from);
                    items.add(to, name);
                    saveRibbons(items);
                    updateRibbonList();
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
                                    removeItem(index);
                                }
                                updateRibbonList();
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

        updateRibbonList();
    }

    private void updateRibbonList() {
        items.clear();
        ArrayList<RibbonItem> ribbons = getRibbons();
        for (RibbonItem i : ribbons) {
            items.add(i);
        }
    }

    private class EnabledTogglesAdapter extends ArrayAdapter<RibbonItem> {

        boolean mShowDragGrips = true;

        public EnabledTogglesAdapter(Context context, ArrayList<RibbonItem> ribbonItems) {
            super(context, android.R.id.text1, ribbonItems);
        }

        public void setShowDragGrips(boolean show) {
            this.mShowDragGrips = show;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_ribbon, parent, false);

            TextView titleView = (TextView) convertView.findViewById(android.R.id.text1);
            ImageView image = (ImageView) convertView.findViewById(R.id.image);

            DragGripView dragGripView = (DragGripView) convertView.findViewById(R.id.drag_handle);

            RibbonItem item = getItem(position);
            if (item.mShortAction.equals("**null**")) {
                titleView.setText(NavBarHelpers.getProperSummary(mContext, item.mLongAction));
                image.setBackground(NavBarHelpers.getIconImage(mContext, item.mLongAction));
            } else {
                image.setBackground(NavBarHelpers.getIconImage(mContext, item.mShortAction));
                titleView.setText(NavBarHelpers.getProperSummary(mContext, item.mShortAction));
            }
            String uri = item.mIcon;

            dragGripView.setVisibility(mShowDragGrips ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }

    private class ConfigurationDragSortController extends DragSortController {

        public ConfigurationDragSortController() {
            super(LockscreenRibbonItems.this.mListView, R.id.drag_handle,
                    DragSortController.ON_DRAG,
                    (DragSortController.FLING_LEFT_REMOVE & DragSortController.FLING_RIGHT_REMOVE));
            setBackgroundColor(0x363636);
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = mAdapter.getView(position, null, LockscreenRibbonItems.this.mListView);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
        }

    }

    private void saveRibbons(ArrayList<RibbonItem> ribbonItems) {
        ArrayList<String> saveList = new ArrayList<String>();
        for (RibbonItem item : ribbonItems) {
            saveList.add(item.getString());
        }
        Settings.AOKP.putArrayList(mContext.getContentResolver(),
                SETTINGS_AOKP[AokpRibbonHelper.HORIZONTAL_RIBBON_ITEMS], saveList);
        updateRibbonList();
        mAdapter.notifyDataSetChanged();
    }

    private void addItem(RibbonItem key) {
        ArrayList<RibbonItem> ribbon = getRibbons();
        ribbon.add(key);
        saveRibbons(ribbon);
    }

    private void removeItem(RibbonItem key) {
        ArrayList<RibbonItem> ribbon = getRibbons();
        ribbon.remove(key);
        saveRibbons(ribbon);
    }

    private void removeItem(int pos) {
        ArrayList<RibbonItem> ribbon = getRibbons();
        ribbon.remove(pos);
        saveRibbons(ribbon);
    }

    private ArrayList<RibbonItem> getRibbons() {
        ArrayList<RibbonItem> ribbonList = new ArrayList<RibbonItem>();
        ArrayList<String> joinedList = Settings.AOKP.getArrayList(mContext.getContentResolver(),
                SETTINGS_AOKP[AokpRibbonHelper.HORIZONTAL_RIBBON_ITEMS]);
        for (String item : joinedList) {
            ribbonList.add(new RibbonItem(item));
        }
        return ribbonList;
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap icon, boolean isApplication) {
        if (mTargetIndex < 0) {
            addItem(new RibbonItem(uri, "**null**", ""));
            return;
        }
        if (!mIsLongPress) {
            items.get(mTargetIndex).mShortAction = uri;
        } else {
            items.get(mTargetIndex).mLongAction = uri;
            Toast.makeText(
                    getActivity(),
                    AwesomeConstants.getProperName(mContext, uri)
                            + "  "
                            + getResources().getString(
                                    R.string.action_long_save),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            } else if ((requestCode == REQUEST_PICK_CUSTOM_ICON)
                    || (requestCode == REQUEST_PICK_LANDSCAPE_ICON)) {

                String iconName = getIconFileName();
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput(iconName,
                            Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri selectedImageUri = getTempFileUri();
                try {
                    Log.e(TAG,
                            "Selected image path: "
                                    + selectedImageUri.getPath());
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri
                            .getPath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                } catch (NullPointerException npe) {
                    Log.e(TAG, "SeletedImageUri was null.");
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                items.get(mTargetIndex).mIcon = Uri.fromFile(
                        new File(mContext.getFilesDir(), iconName)).getPath();

                File f = new File(selectedImageUri.getPath());
                if (f.exists()) {
                    f.delete();
                }

                Toast.makeText(
                        getActivity(),
                        mTargetIndex
                                + getResources().getString(
                                        R.string.custom_app_icon_successfully),
                        Toast.LENGTH_LONG).show();
                saveRibbons(items);
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void createDialog(final String title, final String[] entries,
            final String[] values) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onValueChange(values[item]);
                dialog.dismiss();
            }
        };

        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(title).setItems(entries, l).create();
        dialog.show();
    }

    public void onValueChange(String uri) {
        DialogConstant mFromString = NavRingTargets.funcFromString(uri);
        switch (mFromString) {
            case CUSTOM_APP:
                mPicker.pickShortcut();
                break;
            case SHORT_ACTION:
                mIsLongPress = false;
                createDialog(
                        getResources()
                                .getString(R.string.choose_action_short_title),
                        mActions, mActionCodes);
                break;
            case LONG_ACTION:
                mIsLongPress = true;
                createDialog(
                        getResources().getString(R.string.choose_action_long_title),
                        mActions, mActionCodes);
                break;
            case ICON_ACTION:
                int width = 85;
                int height = width;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("image/*");
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", width);
                intent.putExtra("aspectY", height);
                intent.putExtra("outputX", width);
                intent.putExtra("outputY", height);
                intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempFileUri());
                intent.putExtra("outputFormat",
                        Bitmap.CompressFormat.PNG.toString());
                Log.i(TAG, "started for result, should output to: "
                        + getTempFileUri());
                startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON);
                break;
            case NOT_IN_ENUM:
                if (mTargetIndex < 0) {
                    addItem(new RibbonItem(uri, "**null**", ""));
                    break;
                }
                if (!mIsLongPress) {
                    items.get(mTargetIndex).mShortAction = uri;
                } else {
                    items.get(mTargetIndex).mLongAction = uri;
                    Toast.makeText(
                            getActivity(),
                            AwesomeConstants.getProperName(mContext, uri)
                                    + "  "
                                    + getResources().getString(
                                            R.string.action_long_save),
                            Toast.LENGTH_LONG).show();
                }
                break;

        }
        saveRibbons(items);
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + String.valueOf(System.currentTimeMillis()) + ".png"));

    }

    private String getIconFileName() {
        return "ribbon_icon_" + String.valueOf(System.currentTimeMillis()) + ".png";
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mTargetIndex = position;
        final String[] stringArray =
                mContext.getResources().getStringArray(R.array.navring_long_dialog_entries);
        stringArray[0] =
                stringArray[0]
                        + "  :  "
                        + NavBarHelpers
                                .getProperSummary(mContext, items.get(position).mShortAction);
        stringArray[1] =
                stringArray[1] + "  :  "
                        + NavBarHelpers.getProperSummary(mContext, items.get(position).mLongAction);
        createDialog(
                getResources().getString(R.string.choose_action_title), stringArray,
                getResources().getStringArray(R.array.navring_long_dialog_values));

    }
}
