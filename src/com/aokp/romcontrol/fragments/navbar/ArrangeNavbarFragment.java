package com.aokp.romcontrol.fragments.navbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Settings.AOKP;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.AwesomeConstants.AwesomeConstant;
import com.android.internal.util.aokp.NavBarHelpers;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.util.ShortcutPickerHelper.OnPickListener;
import com.google.android.apps.dashclock.ui.DragGripView;
import com.google.android.apps.dashclock.ui.SwipeDismissListViewTouchListener;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class ArrangeNavbarFragment extends Fragment implements OnPickListener {

    private static final String TAG = ArrangeNavbarFragment.class.getSimpleName();

    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final int REQUEST_PICK_LANDSCAPE_ICON = 201;

    DragSortListView mListView;
    NavbarButtonsAdapter mAdapter;
    DragSortController mDragSortController;

    private ArrayList<AwesomeButtonInfo> mNavButtons = new ArrayList<AwesomeButtonInfo>();

    private ShortcutPickerHelper mPicker;
    private int mTargetIndex = 0;
    private int mTarget = 0;
    DialogConstant mActionTypeToChange;
    AwesomeButtonInfo mSelectedButton;
    private String[] mActions;
    private String[] mActionCodes;

    public static enum DialogConstant {
        ICON_ACTION {
            @Override
            public String value() {
                return "**icon**";
            }
        },
        LONG_ACTION {
            @Override
            public String value() {
                return "**long**";
            }
        },
        DOUBLE_TAP_ACTION {
            @Override
            public String value() {
                return "**double**";
            }
        },
        SHORT_ACTION {
            @Override
            public String value() {
                return "**short**";
            }
        },
        CUSTOM_APP {
            @Override
            public String value() {
                return "**app**";
            }
        },
        NOT_IN_ENUM {
            @Override
            public String value() {
                return "**notinenum**";
            }
        };

        public String value() {
            return this.value();
        }
    }

    public static DialogConstant funcFromString(String string) {
        DialogConstant[] allTargs = DialogConstant.values();
        for (int i = 0; i < allTargs.length; i++) {
            if (string.equals(allTargs[i].value())) {
                return allTargs[i];
            }
        }
        // not in ENUM must be custom
        return DialogConstant.NOT_IN_ENUM;
    }

    public ArrangeNavbarFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.navbar_setup, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_button:
                mNavButtons.add(new AwesomeButtonInfo(null, null, null, null));
                saveUserConfig();
                mAdapter.notifyDataSetChanged();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Get NavRing Actions
        mActionCodes = NavBarHelpers.getNavBarActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(getActivity(),
                    mActionCodes[i]);
        }

        mPicker = new ShortcutPickerHelper(this, this);
        readUserConfig();
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

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
                    AwesomeButtonInfo remove = mNavButtons.remove(from);
                    mNavButtons.add(to, remove);
                    saveUserConfig();
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
                                    mNavButtons.remove(index);
                                }
                                saveUserConfig();
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
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postition, long id) {
                mSelectedButton = mAdapter.getItem(postition);

                final String[] entries = getActivity().getResources()
                        .getStringArray(R.array.navbar_dialog_entries);
                entries[0] = entries[0]
                        + "  :  "
                        + NavBarHelpers.getProperSummary(getActivity(),
                        mSelectedButton.singleAction);
                entries[1] = entries[1]
                        + "  :  "
                        + NavBarHelpers.getProperSummary(getActivity(),
                        mSelectedButton.longPressAction);
                entries[2] = entries[2]
                        + "  :  "
                        + NavBarHelpers.getProperSummary(getActivity(),
                        mSelectedButton.doubleTapAction);

                final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        onValueChange(getResources().getStringArray(R.array.navbar_dialog_values)[item]);
                        dialog.dismiss();
                    }
                };

                final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.choose_action_title))
                        .setItems(entries, l)
                        .create();

                dialog.show();
            }
        });
        mListView.setOnScrollListener(swipeOnTouchListener.makeScrollListener());
        mListView.setItemsCanFocus(true);
        mListView.setDragEnabled(true);
        mListView.setFloatAlpha(0.8f);
        mListView.setAdapter(mAdapter = new NavbarButtonsAdapter(getActivity(), mNavButtons));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListView.setAdapter(null);
        mAdapter = null;
    }

    public void onValueChange(String uri) {
        DialogConstant dConstant = funcFromString(uri);
        switch (dConstant) {
            case CUSTOM_APP:
                mPicker.pickShortcut();
                break;
            case SHORT_ACTION:
            case LONG_ACTION:
            case DOUBLE_TAP_ACTION:
                mActionTypeToChange = dConstant;
                createDialog(
                        getResources()
                                .getString(R.string.choose_action_short_title),
                        mActions, mActionCodes);
                break;
            case ICON_ACTION:
                mActionTypeToChange = dConstant;
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
                // action was selected, uri should be the value
                // mSelectedButton
                switch (mActionTypeToChange) {
                    case SHORT_ACTION:
                        mSelectedButton.singleAction = uri;
                        break;
                    case LONG_ACTION:
                        mSelectedButton.longPressAction = uri;
                        break;
                    case DOUBLE_TAP_ACTION:
                        mSelectedButton.doubleTapAction = uri;
                        break;
                    case ICON_ACTION:
                        mSelectedButton.iconUri = uri;
                        break;
                }
                saveUserConfig();
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    private class NavbarButtonsAdapter extends ArrayAdapter<AwesomeButtonInfo> {

        boolean mShowDragGrips = true;

        public NavbarButtonsAdapter(Context context, ArrayList<AwesomeButtonInfo> toggles) {
            super(context, android.R.id.text1, toggles);
        }

        public void setShowDragGrips(boolean show) {
            this.mShowDragGrips = show;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_toggle, parent, false);

            TextView titleView = (TextView) convertView.findViewById(android.R.id.text1);

            AwesomeButtonInfo button = getItem(position);
            String text = NavBarHelpers.getProperSummary(getContext(), button.singleAction);
            ImageView image = (ImageView) convertView.findViewById(R.id.image);
            DragGripView dragGripView = (DragGripView) convertView.findViewById(R.id.drag_handle);

            titleView.setText(text);
            image.setImageDrawable(NavBarHelpers.getIconImage(getContext(),
                    button.iconUri.isEmpty() ? button.singleAction : button.iconUri));


            return convertView;
        }

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

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(title).setItems(entries, l).create();

        dialog.show();
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp,
                               boolean isApplication) {
        switch (mActionTypeToChange) {
            case SHORT_ACTION:
                mSelectedButton.singleAction = uri;
                break;
            case LONG_ACTION:
                mSelectedButton.longPressAction = uri;
                break;
            case DOUBLE_TAP_ACTION:
                mSelectedButton.doubleTapAction = uri;
                break;
            case ICON_ACTION:
                mSelectedButton.iconUri = uri;
                break;
        }
        saveUserConfig();
        mAdapter.notifyDataSetChanged();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if ((requestCode == REQUEST_PICK_CUSTOM_ICON)
                    || (requestCode == REQUEST_PICK_LANDSCAPE_ICON)) {

                String iconName = getIconFileName(mNavButtons.indexOf(mSelectedButton));
                FileOutputStream iconStream = null;
                try {
                    iconStream = getActivity().openFileOutput(iconName,
                            Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri tempSelectedUri = getTempFileUri();
                Bitmap bitmap;
                if (data != null) {
                    Uri mUri = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                getActivity().getContentResolver(), mUri);
                        Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                        resizedbitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        bitmap = BitmapFactory.decodeFile(tempSelectedUri.getPath());
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                    } catch (NullPointerException npe) {
                        Log.e(TAG, "SeletedImageUri was null.");
                        super.onActivityResult(requestCode, resultCode, data);
                        return;
                    }
                }

                String imageUri = Uri.fromFile(
                        new File(getActivity().getFilesDir(), iconName)).getPath();

                switch (mActionTypeToChange) {
                    case SHORT_ACTION:
                        mSelectedButton.singleAction = imageUri;
                        break;
                    case LONG_ACTION:
                        mSelectedButton.longPressAction = imageUri;
                        break;
                    case DOUBLE_TAP_ACTION:
                        mSelectedButton.doubleTapAction = imageUri;
                        break;
                    case ICON_ACTION:
                        mSelectedButton.iconUri = imageUri;
                        break;
                }

                File f = new File(tempSelectedUri.getPath());
                if (f.exists()) {
                    f.delete();
                }

                saveUserConfig();
                mAdapter.notifyDataSetChanged();
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mNavButtons.indexOf(mSelectedButton) + ".png"));

    }

    private String getIconFileName(int index) {
        return "navbar_icon_" + index + ".png";
    }

    private class ConfigurationDragSortController extends DragSortController {

        public ConfigurationDragSortController() {
            super(ArrangeNavbarFragment.this.mListView, R.id.drag_handle,
                    DragSortController.ON_DRAG, (DragSortController.FLING_LEFT_REMOVE & DragSortController.FLING_RIGHT_REMOVE));
            setBackgroundColor(0x363636);
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = mAdapter.getView(position, null, ArrangeNavbarFragment.this.mListView);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
        }

    }

    private void saveUserConfig() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < mNavButtons.size(); i++) {
            s.append(mNavButtons.get(i).toString());
            if (i != mNavButtons.size() - 1) {
                s.append("|");
            }
        }
        Settings.AOKP.putString(getActivity().getContentResolver(), AOKP.NAVIGATION_BAR_BUTTONS, s.toString());
    }

    private void readUserConfig() {
        mNavButtons.clear();
        String buttons = Settings.AOKP.getString(getActivity().getContentResolver(), Settings.AOKP.NAVIGATION_BAR_BUTTONS);
        if (buttons == null || buttons.isEmpty()) {
            // use default buttons
            mNavButtons.add(new AwesomeButtonInfo(
                    AwesomeConstant.ACTION_BACK.value(),    /* short press */
                    null,                                   /* double press */
                    null,                                   /* long press */
                    null                                    /* icon */
            ));
            mNavButtons.add(new AwesomeButtonInfo(
                    AwesomeConstant.ACTION_HOME.value(),           /* short press */
                    null,                                          /* double press */
                    null,                                          /* long press */
                    null                                           /* icon */
            ));
            mNavButtons.add(new AwesomeButtonInfo(
                    AwesomeConstant.ACTION_RECENTS.value(),        /* short press */
                    null,                                          /* double press */
                    null,                                          /* long press */
                    null                                           /* icon */
            ));
        } else {
            /**
             * Format:
             *
             * singleTapAction,doubleTapAction,longPressAction,iconUri|singleTap...
             */
            String[] userButtons = buttons.split("\\|");
            if (userButtons != null) {
                for (String button : userButtons) {
                    String[] actions = button.split(",", 4);
                    mNavButtons.add(new AwesomeButtonInfo(actions[0], actions[1], actions[2], actions[3]));
                }
            }
        }
    }

    public static class AwesomeButtonInfo {
        String singleAction, doubleTapAction, longPressAction, iconUri;

        public AwesomeButtonInfo(String singleTap, String doubleTap, String longPress, String uri) {
            this.singleAction = singleTap;
            this.doubleTapAction = doubleTap;
            this.longPressAction = longPress;
            this.iconUri = uri;

            if (singleAction == null) {
                singleAction = "";
            }
            if (doubleTapAction == null) {
                doubleTapAction = "";
            }
            if (longPressAction == null) {
                longPressAction = "";
            }

            if (iconUri == null) {
                iconUri = "";
            }
        }

        @Override
        public String toString() {
            return singleAction + "," + doubleTapAction + "," + longPressAction + "," + iconUri;
        }
    }
}
