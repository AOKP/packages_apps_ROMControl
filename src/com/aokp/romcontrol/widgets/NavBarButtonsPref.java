package com.aokp.romcontrol.widgets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.aokp.AwesomeConstants;
import com.android.internal.util.aokp.AwesomeConstants.AwesomeConstant;
import com.android.internal.util.aokp.NavBarHelpers;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;


public class NavBarButtonsPref extends Preference implements ShortcutPickerHelper.OnPickListener {

    public final static int SHOW_LEFT_MENU = 1;
    public final static int SHOW_RIGHT_MENU = 0;
    public final static int SHOW_BOTH_MENU = 2;
    public final static int SHOW_DONT = 4;
    public static final int REQUEST_PICK_CUSTOM_ICON = 200;
    public static final float STOCK_ALPHA = .7f;

    public final static String TAG = "NavBarButtonsPref";

    private Context mContext;
    private ContentResolver mContentRes;
    private Resources mResources;
    private Activity mActivity; // This is an ugly hack and Roman won't like it :)
    private ImageView mLeftMenu, mRightMenu;
    private ImageButton mResetButton, mAddButton,mSaveButton;
    private LinearLayout mButtonContainer;
    private int mNumberofButtons;
    private int mPendingButton;
    private PackageManager pm;
    private ShortcutPickerHelper mPicker;
    ArrayList<NavBarButton> mButtons = new ArrayList<NavBarButton>();
    ArrayList<ImageButton> mButtonViews = new ArrayList<ImageButton>();
    String[] mActions;
    String[] mActionCodes;

    public NavBarButtonsPref(Context context) {
        super(context);
        mContext = context;
    }

    public NavBarButtonsPref(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.navbar_buttons_pref);
        mContext = context;
        mContentRes = mContext.getContentResolver(); 
        mResources = mContext.getResources();
        mActionCodes = NavBarHelpers.getNavBarActions();
        mActions = new String[mActionCodes.length];
        int actionqty = mActions.length;
        for (int i = 0; i < actionqty; i++) {
            mActions[i] = AwesomeConstants.getProperName(mContext, mActionCodes[i]);
        }
    }

    public void setPicker(ShortcutPickerHelper picker) {
        mPicker = picker;
    }

    public void setActivity(Activity act){
        mActivity = act;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        // Set our custom views inside the layout
        mResetButton = (ImageButton) view.findViewById(R.id.reset_button);
        mResetButton.setOnClickListener(mCommandButtons);
        mAddButton = (ImageButton) view.findViewById(R.id.add_button);
        mAddButton.setOnClickListener(mCommandButtons);
        mSaveButton = (ImageButton) view.findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(mCommandButtons);
        mLeftMenu = (ImageView) view.findViewById(R.id.left_menu);
        mButtonContainer = (LinearLayout) view.findViewById(R.id.navbar_container);
        LinearLayout llbuttons = (LinearLayout) view.findViewById(R.id.button_container);
        mButtonViews.clear();
        for (int i = 0; i < llbuttons.getChildCount(); i++) {
            ImageButton ib = (ImageButton) llbuttons.getChildAt(i);
            mButtonViews.add(ib);
        }
        mRightMenu = (ImageView) view.findViewById(R.id.right_menu);
        if (mButtons.size() == 0){
            loadButtons();
        }
        refreshButtons();
    }

    private View.OnClickListener mNavBarClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mPendingButton = mButtonViews.indexOf(v);
            if (mPendingButton > -1 && mPendingButton < mNumberofButtons) {
                createDialog(mButtons.get(mPendingButton));
            }
        }
    };

    private View.OnTouchListener mGlowListener = new View.OnTouchListener() {
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG,"OnTouch:" + event);
            int glowColor = Settings.System.getInt(mContentRes,
                    Settings.System.NAVIGATION_BAR_GLOW_TINT,0);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                v.setBackgroundColor(glowColor);
            } else {
                v.setBackgroundColor(0);
            }
            return false;
        }
    };

    private void loadButtons(){
        mNumberofButtons =  Settings.System.getInt(mContentRes,
                Settings.System.NAVIGATION_BAR_BUTTONS_QTY, 3);
        mButtons.clear();
        for (int i = 0; i < mNumberofButtons; i++) {
            String click = Settings.System.getString(mContentRes,
                    Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[i]);
            String longclick = Settings.System.getString(mContentRes,
                    Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[i]);
            String iconuri = Settings.System.getString(mContentRes,
                    Settings.System.NAVIGATION_CUSTOM_APP_ICONS[i]);
            mButtons.add(new NavBarButton(click,longclick,iconuri));
        }
    }

    public void refreshButtons() {
        int navBarColor = Settings.System.getInt(mContentRes,
                Settings.System.NAVIGATION_BAR_COLOR,-1);
        int navButtonColor = Settings.System.getInt(mContentRes,
                Settings.System.NAVIGATION_BAR_TINT,-1);
        float navButtonAlpha = Settings.System.getFloat(mContentRes,
                Settings.System.NAVIGATION_BAR_ALPHA, STOCK_ALPHA);
        for (int i = 0;i < mNumberofButtons;i++) {
            ImageButton ib = mButtonViews.get(i);
            ib.setImageDrawable(mButtons.get(i).getIcon());
            ib.setOnClickListener(mNavBarClickListener);
            ib.setOnTouchListener(mGlowListener);
            ib.setVisibility(View.VISIBLE);
            ib.setAlpha(navButtonAlpha);
            if (navBarColor != -1){
                ib.setBackgroundColor(navBarColor);
            }
            if (navButtonColor != -1){
                ib.setColorFilter(navButtonColor);
            }
        }
        for (int i = mNumberofButtons; i < 7; i++){
            ImageButton ib = mButtonViews.get(i);
            ib.setVisibility(View.GONE);
        }
        int menuloc = Settings.System.getInt(mContentRes,
                Settings.System.MENU_LOCATION, 0);
        switch (menuloc) {
            case SHOW_BOTH_MENU:
                mLeftMenu.setVisibility(View.VISIBLE);
                mRightMenu.setVisibility(View.VISIBLE);
                break;
            case SHOW_LEFT_MENU:
                mLeftMenu.setVisibility(View.VISIBLE);
                mRightMenu.setVisibility(View.INVISIBLE);
                break;
            case SHOW_RIGHT_MENU:
                mLeftMenu.setVisibility(View.INVISIBLE);
                mRightMenu.setVisibility(View.VISIBLE);
                break;
            case SHOW_DONT:
                mLeftMenu.setVisibility(View.GONE);
                mRightMenu.setVisibility(View.GONE);
                break;
        }
    }

    private void saveButtons(){
        ContentResolver cr = mContext.getContentResolver();
        Settings.System.putInt(cr,Settings.System.NAVIGATION_BAR_BUTTONS_QTY,
                mNumberofButtons);
        for (int i = 0; i < mNumberofButtons; i++) {
            NavBarButton button = mButtons.get(i);
            Settings.System.putString(cr, Settings.System.NAVIGATION_CUSTOM_ACTIVITIES[i],
                    button.getClickAction());
            Settings.System.putString(cr, Settings.System.NAVIGATION_LONGPRESS_ACTIVITIES[i],
                    button.getLongAction());
            Settings.System.putString(cr, Settings.System.NAVIGATION_CUSTOM_APP_ICONS[i],
                    button.getIconURI());
        }
    }

    private void createDialog(final NavBarButton button) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onDialogClick(button, item);
                dialog.dismiss();
                }
            };

        String action = mResources.getString(R.string.navbar_actiontitle_menu);
        action = String.format(action, button.getClickName());
        String longpress = mResources.getString(R.string.navbar_longpress_menu);
        longpress = String.format(longpress, button.getLongName());
        String[] items = {action,longpress,
                mResources.getString(R.string.navbar_icon_menu),
                mResources.getString(R.string.navbar_delete_menu)};
        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(mResources.getString(R.string.navbar_title_menu))
                .setSingleChoiceItems(items, -1, l)
                .create();

        dialog.show();
    }

    private void createActionDialog(final NavBarButton button) {
        final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                onActionDialogClick(button, item);
                dialog.dismiss();
                }
            };

        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(mResources.getString(R.string.navbar_title_menu))
                .setSingleChoiceItems(mActions, -1, l)
                .create();

        dialog.show();
    }

    private void onDialogClick(NavBarButton button, int command){
        switch (command) {
            case 0: // Set Click Action
                button.setPickLongPress(false);
                createActionDialog(button);
                break;
            case 1: // Set Long Press Action
                button.setPickLongPress(true);
                createActionDialog(button);
                break;
            case 2: // set Custom Icon
                int width = 100;
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
                intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
                Log.i(TAG, "started for result, should output to: " + getTempFileUri());
                mActivity.startActivityForResult(intent,REQUEST_PICK_CUSTOM_ICON);
                break;
            case 3: // Delete Button
                mButtons.remove(mPendingButton);
                mNumberofButtons--;
                break;
        }
        refreshButtons();
    }

    private void onActionDialogClick(NavBarButton button, int command){
        if (command == mActions.length -1) {
            // This is the last action - should be **app**
                mPicker.pickShortcut();
        } else { // This should be any other defined action.
            if (button.getPickLongPress()) {
                button.setLongPress(AwesomeConstants.AwesomeActions()[command]);
            } else {
                button.setClickAction(AwesomeConstants.AwesomeActions()[command]);
            }
        }
        refreshButtons();
    }

    private View.OnClickListener mCommandButtons = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int command = v.getId();
            switch (command) {
                case R.id.reset_button:
                    loadButtons();
                    break;
                case R.id.add_button:
                    if (mNumberofButtons < 7) { // Maximum buttons is 7
                        mButtons.add(new NavBarButton("**null**","**null**",""));
                        mNumberofButtons++;
                    }
                    break;
                case R.id.save_button:
                    saveButtons();
                    break;
            }
            refreshButtons();
        }
    };

    private Drawable setIcon(String uri, String action) {
        if (uri != null && uri.length() > 0) {
            File f = new File(Uri.parse(uri).getPath());
            if (f.exists())
                return resize(new BitmapDrawable(mResources, f.getAbsolutePath()));
        }
        if (uri != null && !uri.equals("")
                && uri.startsWith("file")) {
            // it's an icon the user chose from the gallery here
            File icon = new File(Uri.parse(uri).getPath());
            if (icon.exists())
                return resize(new BitmapDrawable(mResources, icon
                        .getAbsolutePath()));

        } else if (uri != null && !uri.equals("")) {
            // here they chose another app icon
            try {
                return resize(pm.getActivityIcon(Intent.parseUri(uri, 0)));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            // ok use default icons here
        }
        return resize(getNavbarIconImage(action));
    }

    private Drawable getNavbarIconImage(String uri) {
        if (uri == null)
            uri = AwesomeConstant.ACTION_NULL.value();
        if (uri.startsWith("**")) {
            return AwesomeConstants.getActionIcon(mContext, uri);
        } else {
            try {
                return mContext.getPackageManager().getActivityIcon(Intent.parseUri(uri, 0));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return mResources.getDrawable(R.drawable.ic_sysbar_null);
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        NavBarButton button = mButtons.get(mPendingButton);
        boolean longpress = button.getPickLongPress();

        if (!longpress) {
            button.setClickAction(uri);
            if (bmp == null) {
                button.setIconURI("");
            } else {
                String iconName = getIconFileName(mPendingButton);
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput(iconName, Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }
                bmp.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                button.setIconURI(Uri.fromFile(mContext.getFileStreamPath(iconName)).toString());
            }
        } else {
            button.setLongPress(uri);
        }
        refreshButtons();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "RequestCode:"+resultCode);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            } else if (requestCode == REQUEST_PICK_CUSTOM_ICON) {
                String iconName = getIconFileName(mPendingButton);
                FileOutputStream iconStream = null;
                try {
                    iconStream = mContext.openFileOutput(iconName, Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    return; // NOOOOO
                }

                Uri selectedImageUri = getTempFileUri();
                try {
                    Log.e(TAG, "Selected image path: " + selectedImageUri.getPath());
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImageUri.getPath());
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, iconStream);
                } catch (NullPointerException npe) {
                    Log.e(TAG, "SeletedImageUri was null.");
                    return;
                }
                mButtons.get(mPendingButton).setIconURI(Uri.fromFile(
                                new File(mContext.getFilesDir(), iconName)).getPath());

                File f = new File(selectedImageUri.getPath());
                if (f.exists())
                    f.delete();
                refreshButtons();
            }
        }
    }

    private Uri getTempFileUri() {
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                "tmp_icon_" + mPendingButton + ".png"));

    }

    private String getIconFileName(int index) {
        return "navbar_icon_" + index + ".png";
    }

    private String getProperSummary(String uri) {
        if (uri == null)
            return AwesomeConstants.getProperName(mContext, "**null**");
        if (uri.startsWith("**")) {
            return AwesomeConstants.getProperName(mContext, uri);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
    }

    private Drawable resize(Drawable image) {
        int size = 50;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
                mResources.getDisplayMetrics());

        Bitmap d = ((BitmapDrawable) image).getBitmap();
        if (d == null) {
            return mResources.getDrawable(R.drawable.ic_sysbar_null);
        } else {
            Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, px, px, false);
            return new BitmapDrawable(mResources, bitmapOrig);
        }
    }

    public class NavBarButton {
        String mClickAction;
        String mLongPressAction;
        String mIconURI;
        String mClickFriendlyName;
        String mLongPressFriendlyName;
        Drawable mIcon;
        boolean mPickingLongPress;

        public NavBarButton(String clickaction, String longpress, String iconuri ) {
            mClickAction = clickaction;
            mLongPressAction = longpress;
            mIconURI = iconuri;
            mClickFriendlyName = getProperSummary(mClickAction);
            mLongPressFriendlyName = getProperSummary (mLongPressAction);
            mIcon = setIcon(mIconURI,mClickAction);
        }

        public void setClickAction(String click) {
            mClickAction = click;
            mClickFriendlyName = getProperSummary(mClickAction);
            // ClickAction was reset - so we should default to stock Icon for now
            mIconURI = "";
            mIcon = setIcon(mIconURI,mClickAction);
        }

        public void setLongPress(String action) {
            mLongPressAction = action;
            mLongPressFriendlyName = getProperSummary (mLongPressAction);
        }
        public void setPickLongPress(boolean pick) {
            mPickingLongPress = pick;
        }
        public boolean getPickLongPress() {
            return mPickingLongPress;
        }
        public void setIconURI (String uri) {
            mIconURI = uri;
            mIcon = setIcon(mIconURI,mClickAction);
        }
        public String getClickName() {
            return mClickFriendlyName;
        }
        public String getLongName() {
            return mLongPressFriendlyName;
        }
        public Drawable getIcon() {
            return mIcon;
        }
        public String getClickAction() {
            return mClickAction;
        }
        public String getLongAction() {
            return mLongPressAction;
        }
        public String getIconURI() {
            return mIconURI;
        }
    }
 }