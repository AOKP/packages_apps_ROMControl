
package com.aokp.romcontrol.util;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.fragments.NavRingTargets.AppPackage;

public class NavRingPreference extends Preference {
    private Context mContext;
    private ContentResolver mResolver;
    private Resources mRes;
    private String mTargetUri;
    private ImageView mWidget = null;
    private View.OnClickListener mListener;

    private static String REBOOT_VAL = Settings.System.SYSTEMUI_SOFTKEY_REBOOT;
    private static String SCREENSHOT_VAL = Settings.System.SYSTEMUI_SOFTKEY_SCREENSHOT;
    private static String IMESWITCHER_VAL = Settings.System.SYSTEMUI_SOFTKEY_IME_SWITCHER;
    private static String RINGVIB_VAL = Settings.System.SYSTEMUI_SOFTKEY_RING_VIB;
    private static String RINGSILENT_VAL = Settings.System.SYSTEMUI_SOFTKEY_RING_SILENT;
    private static String RINGVIBSILENT_VAL = Settings.System.SYSTEMUI_SOFTKEY_RING_VIB_SILENT;
    private static String KILLCURRENT_VAL = Settings.System.SYSTEMUI_SOFTKEY_KILL_PROCESS;
    private static String SCREENOFF_VAL = Settings.System.SYSTEMUI_SOFTKEY_SCREENOFF;
    private static String ASSIST_VAL = Settings.System.SYSTEMUI_NAVRING_ASSIST;

    private static int REBOOT_TITLE = R.string.interface_softkeys_reboot_title;
    private static int SCREENSHOT_TITLE = R.string.interface_softkeys_screenshot_title;
    private static int IMESWITCHER_TITLE = R.string.interface_softkeys_ime_switcher_title;
    private static int RINGVIB_TITLE = R.string.interface_softkeys_ring_vib_title;
    private static int RINGSILENT_TITLE = R.string.interface_softkeys_ring_silent_title;
    private static int RINGVIBSILENT_TITLE = R.string.interface_softkeys_ring_vib_silent_title;
    private static int KILLCURRENT_TITLE = R.string.interface_softkeys_kill_process_title;
    private static int SCREENOFF_TITLE = R.string.interface_softkeys_screenoff_title;
    private static int ASSIST_TITLE = R.string.interface_navring_assist_title;

    private static int REBOOT_ICON = R.drawable.ic_navbar_power;
    private static int KILLCURRENT_ICON = R.drawable.ic_navbar_killtask;
    private static int IMESWITCHER_ICON = R.drawable.ic_sysbar_ime_switcher;
    private static int RINGVIB_ICON = R.drawable.ic_navbar_vib;
    private static int RINGSILENT_ICON = R.drawable.ic_navbar_silent;
    private static int RINGVIBSILENT_ICON = R.drawable.ic_navbar_ring_vib_silent;
    private static int SCREENSHOT_ICON = R.drawable.ic_navbar_screenshot;
    private static int SCREENOFF_ICON = R.drawable.ic_navbar_power;
    private static int ASSIST_ICON = R.drawable.ic_navbar_googlenow;

    private static int CUSTOM_SUMMARY = R.string.interface_navring_custom_pref_summary;
    private static int DEFAULT_SUMMARY = R.string.interface_navring_pref_default_summary;
    private static int PACKAGE_NOT_FOUND_SUMMARY = R.string.interface_softkey_package_removed;
    private static int DEFAULT_TITLE = R.string.interface_softkeys_pref_default_title;
    private static int DEFAULT_ICON = com.android.internal.R.drawable.sym_def_app_icon;

    public NavRingPreference(Context context) {
        this(context, null);
    }

    public NavRingPreference(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
        mRes = mContext.getResources();
        mResolver = mContext.getContentResolver();
    }

    public void setTargetUri(String uri, View.OnClickListener listener) {
        mTargetUri = uri;
        mListener = listener;
        setResources(getUriValue(uri));
    }

    public String getTargetUri() {
        return mTargetUri;
    }

    @Override
    public void onBindView(View v) {
        super.onBindView(v);
        mWidget = (ImageView) v.findViewById(R.id.configure_settings);
        mWidget.setTag(mTargetUri);
        mWidget.setOnClickListener(mListener);
    }

    private String getUriValue(String uri) {
        return Settings.System.getString(mResolver, uri);
    }

    public void setResources(String uriValue) {
        if (uriValue == null || uriValue.equals("") || uriValue.equals(" ")) {
            setTargetValue("none");
            uriValue = "none";
            setDefaultSettings(false);
        } else if (uriValue.startsWith("app:")) {
            setResourcesFromUri(uriValue);
        } else {
            loadCustomApp(uriValue);
        }
    }

   public void loadCustomApp(String uriValue) {
        if (uriValue.equals(REBOOT_VAL)) {
            setTitle(mRes.getString(REBOOT_TITLE));
            setIcon(mRes.getDrawable(REBOOT_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(REBOOT_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(SCREENSHOT_VAL)) {
            setTitle(mRes.getString(SCREENSHOT_TITLE));
            setIcon(mRes.getDrawable(SCREENSHOT_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(SCREENSHOT_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(IMESWITCHER_VAL)) {
            setTitle(mRes.getString(IMESWITCHER_TITLE));
            setIcon(mRes.getDrawable(IMESWITCHER_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(IMESWITCHER_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(KILLCURRENT_VAL)) {
            setTitle(mRes.getString(KILLCURRENT_TITLE));
            setIcon(mRes.getDrawable(KILLCURRENT_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(KILLCURRENT_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(RINGVIB_VAL)) {
            setTitle(mRes.getString(RINGVIB_TITLE));
            setIcon(mRes.getDrawable(RINGVIB_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(RINGVIB_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(RINGSILENT_VAL)) {
            setTitle(mRes.getString(RINGSILENT_TITLE));
            setIcon(mRes.getDrawable(RINGSILENT_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(RINGSILENT_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(RINGVIBSILENT_VAL)) {
            setTitle(mRes.getString(RINGVIBSILENT_TITLE));
            setIcon(mRes.getDrawable(RINGVIBSILENT_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(RINGVIBSILENT_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(SCREENOFF_VAL)) {
            setTitle(mRes.getString(SCREENOFF_TITLE));
            setIcon(mRes.getDrawable(SCREENOFF_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(SCREENOFF_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals(ASSIST_VAL)) {
            setTitle(mRes.getString(ASSIST_TITLE));
            setIcon(mRes.getDrawable(ASSIST_ICON));
            StringBuilder builder = new StringBuilder();
            builder.append(mRes.getString(CUSTOM_SUMMARY))
                    .append(" ")
                    .append(mRes.getString(ASSIST_TITLE));
            setSummary(builder.toString());
        } else if (uriValue.equals("none")) {
            setDefaultSettings(false);
        } else {
            setDefaultSettings(false);
        }
    }


    public void setTargetValue(String uriValue) {
        Settings.System.putString(mResolver, mTargetUri, uriValue);
    }

    private void setDefaultSettings(boolean packageNotFound) {
        setTitle(mRes.getString(DEFAULT_TITLE));
        setIcon(mRes.getDrawable(DEFAULT_ICON));
        setSummary(packageNotFound
                ? mRes.getString(PACKAGE_NOT_FOUND_SUMMARY)
                : mRes.getString(DEFAULT_SUMMARY));
    }

    private void setResourcesFromUri(String uri) {
        if (uri.startsWith("app:")) {
            String activity = uri.substring(4);
            PackageManager pm = mContext.getPackageManager();
            ComponentName component = ComponentName.unflattenFromString(activity);
            ActivityInfo activityInfo = null;
            Boolean noError = false;
            try {
                activityInfo = pm.getActivityInfo(component, PackageManager.GET_RECEIVERS);
                noError = true;
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                noError = false;
                setDefaultSettings(true);
                Toast.makeText(mContext, "The selected application could not be found",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            if (noError) {
                setIcon(activityInfo.loadIcon(pm));
                String title = activityInfo.loadLabel(pm).toString();
                setTitle(title);
                StringBuilder builder = new StringBuilder();
                builder.append(mRes.getString(CUSTOM_SUMMARY))
                        .append(" ")
                        .append(title);
                setSummary(builder.toString());
            }
        } else {
            setDefaultSettings(false);
        }
    }

    public void setResourcesFromPackage(AppPackage app) {
        setTitle(app.getName());
        setIcon(app.getIcon());
        StringBuilder builder = new StringBuilder();
        builder.append(mRes.getString(CUSTOM_SUMMARY))
                .append(" ")
                .append(app.getName());
        setSummary(builder.toString());
        String tmp = "app:" + app.getComponentName();
        setTargetValue(tmp);
    }

}
