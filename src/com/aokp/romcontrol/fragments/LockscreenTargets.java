package com.aokp.romcontrol.fragments;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.multiwaveview.GlowPadView;
import com.android.internal.widget.multiwaveview.TargetDrawable;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.util.IconPicker;
import com.aokp.romcontrol.util.IconPicker.OnIconPickListener;

import java.lang.reflect.*;

public class LockscreenTargets extends AOKPPreferenceFragment implements ShortcutPickerHelper.OnPickListener,
    GlowPadView.OnTriggerListener, OnIconPickListener {

    /**
     * Default stock configuration for three lockscreen targets
     * @hide
     */
    public final static String DEFAULT_TRI =
            "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.android.systemui/.LockscreenUnlock;S.icon_resource=ic_lockscreen_unlock;" +
            "end|#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.google.android.googlequicksearchbox/.SearchActivity;S.icon_resource=ic_lockscreen_google_normal;" +
            "end|#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.android.gallery3d/com.android.camera.CameraLauncher;S.icon_resource=ic_lockscreen_camera_normal;end";

    /**
     * Default stock configuration for lockscreen targets with Google now at left
     * @hide
     */
    public final static String DEFAULT_LEFT =
            "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.android.systemui/.LockscreenUnlock;S.icon_resource=ic_lockscreen_unlock;" +
            "end|empty|empty|#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.google.android.googlequicksearchbox/.SearchActivity;S.icon_resource=ic_lockscreen_google_normal;" +
            "end";

    /**
     * Default stock configuration for lockscreen targets with 5 0r 8 targets
     * @hide
     */
    public final static String DEFAULT_NORMAL =
            "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.android.systemui/.LockscreenUnlock;S.icon_resource=ic_lockscreen_unlock;" +
            "end|empty|#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.google.android.googlequicksearchbox/.SearchActivity;S.icon_resource=ic_lockscreen_google_normal;" +
            "end|empty|#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;" +
            "component=com.android.gallery3d/com.android.camera.CameraLauncher;S.icon_resource=ic_lockscreen_camera_normal;end";

    public final static String[] NO_LOCK_CMP = {
        "com.android.systemui.RingVibSilentToggle",
        "com.android.systemui.RingVibToggle",
        "com.android.systemui.RingSilentToggle",
    };

    private final static List<String> NO_LOCK_LIST = Arrays.asList(NO_LOCK_CMP);

    private ImageButton mDialogIcon;
    private Button mDialogLabel;
    private ShortcutPickerHelper mPicker;
    private IconPicker mIconPicker;
    private ArrayList<TargetInfo> mTargetStore = new ArrayList<TargetInfo>();
    private int mTargetInset;
    private boolean mIsLandscape;
    private boolean mIsScreenLarge;
    private ViewGroup mContainer;
    private View mUnlockWidget;
    private GlowPadView glowPadView;
    private Activity mActivity;
    private Resources mResources;
    private File mImageTmp;
    private int mTargetIndex = 0;
    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static String EMPTY_LABEL;
    private int maxTargets;

    class TargetInfo {
        String uri, pkgName;
        StateListDrawable icon;
        Drawable defaultIcon;
        String iconType;
        String iconSource;
        TargetInfo(StateListDrawable target) {
            icon = target;
        }
        TargetInfo(String in, StateListDrawable target, String iType, String iSource, Drawable dI) {
            uri = in;
            icon = target;
            defaultIcon = dI;
            iconType = iType;
            iconSource = iSource;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mContainer = container;
        setHasOptionsMenu(true);
        mActivity = getActivity();
        mIsScreenLarge = isScreenLarge();
        mResources = getResources();
        mIsLandscape = mResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        mTargetInset = mResources.getDimensionPixelSize(com.android.internal.R.dimen.lockscreen_target_inset);
        mIconPicker = new IconPicker(mActivity, this);
        mPicker = new ShortcutPickerHelper(this, this);
        mImageTmp = new File(mActivity.getCacheDir() + "/target.tmp");
        EMPTY_LABEL = mActivity.getResources().getString(R.string.lockscreen_target_empty);
        maxTargets = Settings.System.getInt(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGET_AMOUNT, 3);
        if(maxTargets < 3){
            maxTargets = 3;
        }

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.lockscreen_targets, container, false);
        Configuration config = mResources.getConfiguration();
        try {
            Class kscClass = Class.forName("com.android.internal.policy.impl.KeyguardScreenCallback");
            Object ksc = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{kscClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return false;
                }
            });
            Class kumClass = Class.forName("com.android.internal.policy.impl.KeyguardUpdateMonitor");
            Constructor kumConstructor = kumClass.getConstructor(Context.class);
            kumConstructor.setAccessible(true);
            Object kum = kumConstructor.newInstance(mActivity);
            Class lpuClass = Class.forName("com.android.internal.widget.LockPatternUtils");
            Constructor lpuConstructor = lpuClass.getConstructor(Context.class);
            lpuConstructor.setAccessible(true);
            Object lpu = lpuConstructor.newInstance(mActivity);
            Class lockscreenClass = Class.forName("com.android.internal.policy.impl.LockScreen");
            Constructor lockscreenConstructor = lockscreenClass.getDeclaredConstructors()[0];
            lockscreenConstructor.setAccessible(true);
            View lockscreen = (View) lockscreenConstructor.newInstance(mActivity, config, lpu, kum, ksc);
            view.addView(lockscreen);
            //return lockscreen;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUnlockWidget = mActivity.findViewById(com.android.internal.R.id.unlock_widget);
        createUnlockMethods(mUnlockWidget);
        initializeView(Settings.System.getString(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGETS));
    }

    private void createUnlockMethods(View unlockWidget) {
        if (unlockWidget instanceof GlowPadView) {
            glowPadView = (GlowPadView) unlockWidget;
            glowPadView.setOnTriggerListener(this);
        } else {
            throw new IllegalStateException("Unrecognized unlock widget: " + unlockWidget);
        }
    }

    /**
     * Create a layered drawable
     * @param back - Background image to use when target is active
     * @param front - Front image to use for target
     * @param inset - Target inset padding
     * @param frontBlank - Whether the front image for active target should be blank
     * @return StateListDrawable
     */
    private StateListDrawable getLayeredDrawable(Drawable back, Drawable front, int inset, boolean frontBlank) {
        front.mutate();
        back.mutate();
        InsetDrawable[] inactivelayer = new InsetDrawable[2];
        InsetDrawable[] activelayer = new InsetDrawable[2];
        Drawable activeFront = frontBlank ? mResources.getDrawable(android.R.color.transparent) : front;
        Drawable inactiveBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_lock_pressed);
        inactivelayer[0] = new InsetDrawable(inactiveBack, 0, 0, 0, 0);
        inactivelayer[1] = new InsetDrawable(front, inset, inset, inset, inset);
        activelayer[0] = new InsetDrawable(back, 0, 0, 0, 0);
        activelayer[1] = new InsetDrawable(activeFront, inset, inset, inset, inset);
        StateListDrawable states = new StateListDrawable();
        LayerDrawable inactiveLayerDrawable = new LayerDrawable(inactivelayer);
        inactiveLayerDrawable.setId(0, 0);
        inactiveLayerDrawable.setId(1, 1);
        LayerDrawable activeLayerDrawable = new LayerDrawable(activelayer);
        activeLayerDrawable.setId(0, 0);
        activeLayerDrawable.setId(1, 1);
        states.addState(TargetDrawable.STATE_INACTIVE, inactiveLayerDrawable);
        states.addState(TargetDrawable.STATE_ACTIVE, activeLayerDrawable);
        states.addState(TargetDrawable.STATE_FOCUSED, activeLayerDrawable);
        return states;
    }

    private void initializeView(String input) {
        if (input == null) {
            input = DEFAULT_TRI;
        }
        mTargetStore.clear();
        final PackageManager packMan = mActivity.getPackageManager();
        final Drawable activeBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
        final String[] targetStore = input.split("\\|");

        int total = 0;
        if (maxTargets <= 3) {
            total = 4;
        } else if (maxTargets == 4 || maxTargets == 6) {
            total = 6;
        } else if (maxTargets == 5 || maxTargets == 8) {
            total = 8;
        }

        for (int cc = 0; cc < total; cc++) {
            String uri = GlowPadView.EMPTY_TARGET;
            Drawable front = null;
            Drawable back = activeBack;
            boolean frontBlank = false;
            String iconType = null;
            String iconSource = null;
            int tmpInset = mTargetInset;
            if (cc < targetStore.length && cc < maxTargets) {
                uri = targetStore[cc];
                if (!uri.equals(GlowPadView.EMPTY_TARGET)) {
                    try {
                        Intent in = Intent.parseUri(uri, 0);
                        if (in.hasExtra(GlowPadView.ICON_FILE)) {
                            String rSource = in.getStringExtra(GlowPadView.ICON_FILE);
                            File fPath = new File(rSource);
                            if (fPath != null) {
                                if (fPath.exists()) {
                                    front = new BitmapDrawable(getResources(), getRoundedCornerBitmap(BitmapFactory.decodeFile(rSource)));
                                }
                            }
                        } else if (in.hasExtra(GlowPadView.ICON_RESOURCE)) {
                            String rSource = in.getStringExtra(GlowPadView.ICON_RESOURCE);
                            String rPackage = in.getStringExtra(GlowPadView.ICON_PACKAGE);
                            if (rSource != null) {
                                if (rPackage != null) {
                                    try {
                                        Context rContext = mActivity.createPackageContext(rPackage, 0);
                                        int id = rContext.getResources().getIdentifier(rSource, "drawable", rPackage);
                                        front = rContext.getResources().getDrawable(id);
                                        id = rContext.getResources().getIdentifier(rSource.replaceAll("_normal", "_activated"),
                                                "drawable", rPackage);
                                        back = rContext.getResources().getDrawable(id);
                                        tmpInset = 0;
                                        frontBlank = true;
                                    } catch (NameNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (NotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    front = mResources.getDrawable(mResources.getIdentifier(rSource, "drawable", "android"));
                                    back = mResources.getDrawable(mResources.getIdentifier(
                                            rSource.replaceAll("_normal", "_activated"), "drawable", "android"));
                                    tmpInset = 0;
                                    frontBlank = true;
                                }
                            }
                        }
                        if (front == null) {
                            ActivityInfo aInfo = in.resolveActivityInfo(packMan, PackageManager.GET_ACTIVITIES);
                            if (aInfo != null) {
                                front = aInfo.loadIcon(packMan);
                            } else {
                                front = mResources.getDrawable(android.R.drawable.sym_def_app_icon).mutate();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (cc >= maxTargets) {
                mTargetStore.add(new TargetInfo(null));
                continue;
            }
            if (back == null || front == null) {
                Drawable emptyIcon = mResources.getDrawable(R.drawable.ic_empty).mutate();
                front = emptyIcon;
            }
            mTargetStore.add(new TargetInfo(uri, getLayeredDrawable(back,front, tmpInset, frontBlank), iconType,
                    iconSource, front.getConstantState().newDrawable().mutate()));
        }
        ArrayList<TargetDrawable> tDraw = new ArrayList<TargetDrawable>();
        for (TargetInfo i : mTargetStore) {
            if (i != null) {
                tDraw.add(new TargetDrawable(mResources, i.icon));
            } else {
                tDraw.add(new TargetDrawable(mResources, null));
            }
        }
        glowPadView.setTargetResources(tDraw);
    }

    @Override
    public void onResume() {
        super.onResume();
        // If running on a phone, remove padding around container
        if (!mIsScreenLarge) {
            mContainer.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.profile_reset_title)
            .setIcon(R.drawable.ic_settings_backup) // use the backup icon
            .setAlphabeticShortcut('r')
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, MENU_SAVE, 0, R.string.wifi_save)
            .setIcon(R.drawable.ic_menu_save)
            .setAlphabeticShortcut('s')
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetAll();
                return true;
            case MENU_SAVE:
                saveAll();
                Toast.makeText(mActivity, R.string.lockscreen_target_save, Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    /**
     * Resets the target layout to stock
     */
    private void resetAll() {
        new AlertDialog.Builder(mActivity)
        .setTitle(R.string.lockscreen_target_reset_title)
        .setIconAttribute(android.R.attr.alertDialogIcon)
        .setMessage(R.string.lockscreen_target_reset_message)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String targets = DEFAULT_TRI;
                if (maxTargets == 3) {
                     targets = DEFAULT_TRI;
                } else if (maxTargets == 4 || maxTargets == 6) {
                     targets = DEFAULT_LEFT;
                } else if (maxTargets == 5 || maxTargets == 8) {
                     targets = DEFAULT_NORMAL;
                }
                initializeView(targets);
                saveAll();
                Toast.makeText(mActivity, R.string.lockscreen_target_reset, Toast.LENGTH_LONG).show();
            }
        }).setNegativeButton(R.string.cancel, null)
        .create().show();
    }

    /**
     * Save targets to settings provider
     */
    private void saveAll() {
        StringBuilder targetLayout = new StringBuilder();
        ArrayList<String> existingImages = new ArrayList<String>();
        int maxTargets = Settings.System.getInt(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGET_AMOUNT, 3);
        if(maxTargets < 3){
            maxTargets = 3;
        }

        boolean foundUnlock = false;
        for (int i = 0; i <= maxTargets - 1; i++) {
            String uri = mTargetStore.get(i).uri;
            if(uri != null && !uri.equals(GlowPadView.EMPTY_TARGET)){
                String className = "";
                try{
                    className = Intent.parseUri(uri, 0).getComponent().getClassName();
                }catch(Exception ee){}
                if(!NO_LOCK_LIST.contains(className)){
                    foundUnlock = true;
                    break;
                }
            }
        }

        if(foundUnlock){
            for (int i = 0; i <= maxTargets - 1; i++) {
                String uri = mTargetStore.get(i).uri;
                String type = mTargetStore.get(i).iconType;
                String source = mTargetStore.get(i).iconSource;
                existingImages.add(source);
                if (!uri.equals(GlowPadView.EMPTY_TARGET) && type != null) {
                    try {
                        Intent in = Intent.parseUri(uri, 0);
                        in.putExtra(type, source);
                        String pkgName = mTargetStore.get(i).pkgName;
                        if (pkgName != null) {
                            in.putExtra(GlowPadView.ICON_PACKAGE, mTargetStore.get(i).pkgName);
                        } else {
                            in.removeExtra(GlowPadView.ICON_PACKAGE);
                        }
                        uri = in.toUri(0);
                    } catch (URISyntaxException e) {
                    }
                }
                targetLayout.append(uri);
                targetLayout.append("|");
            }
            targetLayout.deleteCharAt(targetLayout.length() - 1);
            Settings.System.putString(mActivity.getContentResolver(), Settings.System.LOCKSCREEN_TARGETS, targetLayout.toString());
            for (File pic : mActivity.getFilesDir().listFiles()) {
                if (pic.getName().startsWith("lockscreen_") && !existingImages.contains(pic.toString())) {
                    pic.delete();
                }
            }
        }else{
            new AlertDialog.Builder(mActivity)
            .setTitle(R.string.lockscreen_target_save_error_title)
            .setIconAttribute(android.R.attr.alertDialogIcon)
            .setMessage(R.string.lockscreen_target_save_error_msg)
            .setPositiveButton(R.string.ok, null)
            .create().show();
        }
    }

    /**
     * Updates a target in the GlowPadView
     */
    private void setTarget(int position, String uri, Drawable draw, String iconType, String iconSource, String pkgName) {
        TargetInfo item = mTargetStore.get(position);
        StateListDrawable state = (StateListDrawable) item.icon;
        LayerDrawable inActiveLayer = (LayerDrawable) state.getStateDrawable(0);
        LayerDrawable activeLayer = (LayerDrawable) state.getStateDrawable(1);
        inActiveLayer.setDrawableByLayerId(1, draw);
        boolean isSystem = iconType != null && iconType.equals(GlowPadView.ICON_RESOURCE);
        if (!isSystem) {
            final Drawable activeBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
            activeLayer.setDrawableByLayerId(0, new InsetDrawable(activeBack, 0, 0, 0, 0));
            activeLayer.setDrawableByLayerId(1, draw);
        } else {
            InsetDrawable empty = new InsetDrawable(mResources.getDrawable(android.R.color.transparent), 0, 0, 0, 0);
            activeLayer.setDrawableByLayerId(1, empty);
            int activeId = mResources.getIdentifier(iconSource.replaceAll("_normal", "_activated"), "drawable", "android");
            Drawable back = null;
            if (activeId != 0) {
                back = mResources.getDrawable(activeId);
                activeLayer.setDrawableByLayerId(0, back);
            } else {
                final Drawable activeBack = mResources.getDrawable(com.android.internal.R.drawable.ic_lockscreen_target_activated);
                activeLayer.setDrawableByLayerId(0, new InsetDrawable(activeBack, 0, 0, 0, 0));
            }
        }
        item.defaultIcon = mDialogIcon.getDrawable().getConstantState().newDrawable().mutate();
        item.uri = uri;
        item.iconType = iconType;
        item.iconSource = iconSource;
        item.pkgName = pkgName;
    }

    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
        try {
            Intent i = Intent.parseUri(uri, 0);
            PackageManager pm = mActivity.getPackageManager();
            ActivityInfo aInfo = i.resolveActivityInfo(pm, PackageManager.GET_ACTIVITIES);
            Drawable icon = null;
            if (aInfo != null) {
                icon = aInfo.loadIcon(pm).mutate();
            } else {
                icon = mResources.getDrawable(android.R.drawable.sym_def_app_icon);
            }
            mDialogLabel.setText(friendlyName);
            mDialogLabel.setTag(uri);
            mDialogIcon.setImageDrawable(resizeForDialog(icon));
            mDialogIcon.setTag(null);
        } catch (Exception e) {
        }
    }

    private Drawable resizeForDialog(Drawable image) {
        int size = (int) mResources.getDimension(R.dimen.target_icon_size);
        Bitmap d = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, size, size, false);
        return new BitmapDrawable(mResources, bitmapOrig);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
            bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 24;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    public boolean isScreenLarge() {
        final int screenSize = Resources.getSystem().getConfiguration().screenLayout &
                   Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean isScreenLarge = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                    screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        return isScreenLarge;
        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String shortcut_name = null;
        if (data != null) {
            shortcut_name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        }
        if (shortcut_name != null && shortcut_name.equals(EMPTY_LABEL)) {
            mDialogLabel.setText(EMPTY_LABEL);
            mDialogLabel.setTag(GlowPadView.EMPTY_TARGET);
            mDialogIcon.setImageResource(R.drawable.ic_empty);
        } else if (requestCode == IconPicker.REQUEST_PICK_SYSTEM || requestCode == IconPicker.REQUEST_PICK_AOKP || requestCode == IconPicker.REQUEST_PICK_GALLERY
                || requestCode == IconPicker.REQUEST_PICK_ICON_PACK) {
            mIconPicker.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode != Activity.RESULT_CANCELED && resultCode != Activity.RESULT_CANCELED){
            mPicker.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onGrabbed(View v, int handle) {
    }

    @Override
    public void onReleased(View v, int handle) {
    }

    @Override
    public void onTargetChange(View view, final int target) {
    }

    @Override
    public void onTrigger(View v, final int target) {
        mTargetIndex = target;
        if (target >= 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.lockscreen_target_edit_title);
            builder.setMessage(R.string.lockscreen_target_edit_msg);
            View view = View.inflate(mActivity, R.layout.lockscreen_shortcut_dialog, null);
            view.findViewById(R.id.icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mDialogLabel.getText().equals(EMPTY_LABEL)) {
                        try {
                            mImageTmp.createNewFile();
                            mImageTmp.setWritable(true, false);
                            mIconPicker.pickIcon(getId(), mImageTmp);
                        } catch (IOException e) {
                        }
                    }
                }
            });
            view.findViewById(R.id.label).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPicker.pickShortcut();
                }
            });
            mDialogIcon = ((ImageButton) view.findViewById(R.id.icon));
            mDialogLabel = ((Button) view.findViewById(R.id.label));
            TargetInfo item = mTargetStore.get(target);
            mDialogIcon.setImageDrawable(mTargetStore.get(target).defaultIcon.mutate());
            TargetInfo tmpIcon = new TargetInfo(null);
            tmpIcon.iconType = item.iconType;
            tmpIcon.iconSource = item.iconSource;
            tmpIcon.pkgName = item.pkgName;
            mDialogIcon.setTag(tmpIcon);
            if (mTargetStore.get(target).uri.equals(GlowPadView.EMPTY_TARGET)) {
                mDialogLabel.setText(EMPTY_LABEL);
            } else {
                mDialogLabel.setText(mPicker.getFriendlyNameForUri(mTargetStore.get(target).uri));
            }
            mDialogLabel.setTag(mTargetStore.get(target).uri);
            builder.setView(view);
            builder.setPositiveButton(R.string.ok,  new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TargetInfo vObject = (TargetInfo) mDialogIcon.getTag();
                    String type = null, source = null, pkgName = null;
                    int targetInset = mTargetInset;
                    if (vObject != null) {
                        type = vObject.iconType;
                        source = vObject.iconSource;
                        pkgName = vObject.pkgName;
                    }
                    if (type != null && type.equals(GlowPadView.ICON_RESOURCE)) {
                        targetInset = 0;
                    }
                    InsetDrawable pD = new InsetDrawable(mDialogIcon.getDrawable(), targetInset,
                            targetInset, targetInset, targetInset);
                    setTarget(mTargetIndex, mDialogLabel.getTag().toString(), pD, type, source, pkgName);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
            ((TextView)dialog.findViewById(android.R.id.message)).setTextAppearance(mActivity,
                    android.R.style.TextAppearance_DeviceDefault_Small);
        }
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
    }

    @Override
    public void iconPicked(int requestCode, int resultCode, Intent in) {
        Drawable ic = null;
        String iconType = null;
        String pkgName = null;
        String iconSource = null;
        if (requestCode == IconPicker.REQUEST_PICK_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                File mImage = new File(mActivity.getFilesDir() + "/lockscreen_" + System.currentTimeMillis() + ".png");
                if (mImageTmp.exists()) {
                    mImageTmp.renameTo(mImage);
                }
                mImage.setReadable(true, false);
                iconType = GlowPadView.ICON_FILE;
                iconSource = mImage.toString();
                ic = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(mImage.toString()));
            } else {
                if (mImageTmp.exists()) {
                    mImageTmp.delete();
                }
                return;
            }
        } else if (requestCode == IconPicker.REQUEST_PICK_SYSTEM) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            ic = mResources.getDrawable(mResources.getIdentifier(resourceName, "drawable", "android")).mutate();
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else if (requestCode == IconPicker.REQUEST_PICK_AOKP) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            ic = mResources.getDrawable(mResources.getIdentifier(resourceName, "drawable", "android")).mutate();
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else if (requestCode == IconPicker.REQUEST_PICK_ICON_PACK && resultCode == Activity.RESULT_OK) {
            String resourceName = in.getStringExtra(IconPicker.RESOURCE_NAME);
            pkgName = in.getStringExtra(IconPicker.PACKAGE_NAME);
            try {
                Context rContext = mActivity.createPackageContext(pkgName, 0);
                int id = rContext.getResources().getIdentifier(resourceName, "drawable", pkgName);
                ic = rContext.getResources().getDrawable(id);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            iconType = GlowPadView.ICON_RESOURCE;
            iconSource = resourceName;
        } else {
            return;
        }
        TargetInfo tmpIcon = new TargetInfo(null);
        tmpIcon.iconType = iconType;
        tmpIcon.iconSource = iconSource;
        tmpIcon.pkgName = pkgName;
        mDialogIcon.setTag(tmpIcon);
        mDialogIcon.setImageDrawable(ic);
    }

    @Override
    public void onFinishFinalAnimation() {
    }
}
