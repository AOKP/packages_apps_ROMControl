
package com.aokp.romcontrol.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;

import java.net.URISyntaxException;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.util.ShortcutPickerHelper;
import com.aokp.romcontrol.widgets.NavBarItemPreference;

public class NavRingTargets extends AOKPPreferenceFragment implements
        ShortcutPickerHelper.OnPickListener, OnPreferenceChangeListener {

    public static final int NAVRING_ONE = 1;
    public static final int NAVRING_TWO = 2;
    public static final int NAVRING_THREE = 3;
    public static final int NAVRING_FOUR = 4;
    public static final int NAVRING_FIVE = 5;

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;

    private int mNavRingAmount;
    private boolean mNavRingLong;

    NavBarItemPreference mRing1;
    NavBarItemPreference mRing2;
    NavBarItemPreference mRing3;
    NavBarItemPreference mRing4;
    NavBarItemPreference mRing5;
    NavBarItemPreference mLongRing1;
    NavBarItemPreference mLongRing2;
    NavBarItemPreference mLongRing3;
    NavBarItemPreference mLongRing4;
    NavBarItemPreference mLongRing5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_navring);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs_navring);

        PreferenceScreen prefs = getPreferenceScreen();

        mPicker = new ShortcutPickerHelper(this, this);

        String target3 = Settings.System.getString(mContext.getContentResolver(), Settings.System.SYSTEMUI_NAVRING[0]);
        if (target3 == null || target3.equals("")) {
            Settings.System.putString(mContext.getContentResolver(), Settings.System.SYSTEMUI_NAVRING[0], "**assist**");
        }

        mRing1 = (NavBarItemPreference) findPreference("interface_navring_1_release");
        mRing1.setOnPreferenceChangeListener(this);
        mRing1.setSummary(getProperSummary(mRing1));
        mRing1.setIcon(resize(getNavbarIconImage(mRing1)));

        mRing2 = (NavBarItemPreference) findPreference("interface_navring_2_release");
        mRing2.setOnPreferenceChangeListener(this);
        mRing2.setSummary(getProperSummary(mRing2));
        mRing2.setIcon(resize(getNavbarIconImage(mRing2)));

        mRing3 = (NavBarItemPreference) findPreference("interface_navring_3_release");
        mRing3.setOnPreferenceChangeListener(this);
        mRing3.setSummary(getProperSummary(mRing3));
        mRing3.setIcon(resize(getNavbarIconImage(mRing3)));

        mRing4 = (NavBarItemPreference) findPreference("interface_navring_4_release");
        mRing4.setOnPreferenceChangeListener(this);
        mRing4.setSummary(getProperSummary(mRing4));
        mRing4.setIcon(resize(getNavbarIconImage(mRing4)));

        mRing5 = (NavBarItemPreference) findPreference("interface_navring_5_release");
        mRing5.setOnPreferenceChangeListener(this);
        mRing5.setSummary(getProperSummary(mRing5));
        mRing5.setIcon(resize(getNavbarIconImage(mRing5)));

        mLongRing1 = (NavBarItemPreference) findPreference("interface_navring_1_long");
        mLongRing1.setOnPreferenceChangeListener(this);
        mLongRing1.setSummary(getProperSummary(mLongRing1));
        mLongRing1.setIcon(resize(getNavbarIconImage(mLongRing1)));

        mLongRing2 = (NavBarItemPreference) findPreference("interface_navring_2_long");
        mLongRing2.setOnPreferenceChangeListener(this);
        mLongRing2.setSummary(getProperSummary(mLongRing2));
        mLongRing2.setIcon(resize(getNavbarIconImage(mLongRing2)));

        mLongRing3 = (NavBarItemPreference) findPreference("interface_navring_3_long");
        mLongRing3.setOnPreferenceChangeListener(this);
        mLongRing3.setSummary(getProperSummary(mLongRing3));
        mLongRing3.setIcon(resize(getNavbarIconImage(mLongRing3)));

        mLongRing4 = (NavBarItemPreference) findPreference("interface_navring_4_long");
        mLongRing4.setOnPreferenceChangeListener(this);
        mLongRing4.setSummary(getProperSummary(mLongRing4));
        mLongRing4.setIcon(resize(getNavbarIconImage(mLongRing4)));

        mLongRing5 = (NavBarItemPreference) findPreference("interface_navring_5_long");
        mLongRing5.setOnPreferenceChangeListener(this);
        mLongRing5.setSummary(getProperSummary(mLongRing5));
        mLongRing5.setIcon(resize(getNavbarIconImage(mLongRing5)));

        mNavRingAmount = Settings.System.getInt(mContext.getContentResolver(),
                         Settings.System.SYSTEMUI_NAVRING_AMOUNT, 1);

        mNavRingLong = Settings.System.getBoolean(mContext.getContentResolver(),
                         Settings.System.SYSTEMUI_NAVRING_LONG_ENABLE, false);

        if (mNavRingLong) {
            switch (mNavRingAmount) {
            case NAVRING_ONE:
                prefs.removePreference(mRing2);
                prefs.removePreference(mRing3);
                prefs.removePreference(mRing4);
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing2);
                prefs.removePreference(mLongRing3);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            case NAVRING_TWO:
                prefs.removePreference(mRing3);
                prefs.removePreference(mRing4);
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing3);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            case NAVRING_THREE:
                prefs.removePreference(mRing4);
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            case NAVRING_FOUR:
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing5);
            default:
                //leave them all
            }
        } else {
            switch (mNavRingAmount) {
            case NAVRING_ONE:
                prefs.removePreference(mRing2);
                prefs.removePreference(mRing3);
                prefs.removePreference(mRing4);
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing1);
                prefs.removePreference(mLongRing2);
                prefs.removePreference(mLongRing3);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            case NAVRING_TWO:
                prefs.removePreference(mRing3);
                prefs.removePreference(mRing4);
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing1);
                prefs.removePreference(mLongRing2);
                prefs.removePreference(mLongRing3);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            case NAVRING_THREE:
                prefs.removePreference(mRing4);
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing1);
                prefs.removePreference(mLongRing2);
                prefs.removePreference(mLongRing3);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            case NAVRING_FOUR:
                prefs.removePreference(mRing5);
                prefs.removePreference(mLongRing1);
                prefs.removePreference(mLongRing2);
                prefs.removePreference(mLongRing3);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            default:
                prefs.removePreference(mLongRing1);
                prefs.removePreference(mLongRing2);
                prefs.removePreference(mLongRing3);
                prefs.removePreference(mLongRing4);
                prefs.removePreference(mLongRing5);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;

        if (preference == mRing1) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING[0];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING[0], (String) newValue);
            mRing1.setSummary(getProperSummary(mRing1));
            mRing1.setIcon(resize(getNavbarIconImage(mRing1)));
            }
        } else if (preference == mRing2) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING[1];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING[1], (String) newValue);
            mRing2.setSummary(getProperSummary(mRing2));
            mRing2.setIcon(resize(getNavbarIconImage(mRing2)));
            }
        } else if (preference == mRing3) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING[2];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING[2], (String) newValue);
            mRing3.setSummary(getProperSummary(mRing3));
            mRing3.setIcon(resize(getNavbarIconImage(mRing3)));
            }
        } else if (preference == mRing4) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING[3];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING[3], (String) newValue);
            mRing4.setSummary(getProperSummary(mRing4));
            mRing4.setIcon(resize(getNavbarIconImage(mRing4)));
            }
        } else if (preference == mRing5) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING[4];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING[4], (String) newValue);
            mRing5.setSummary(getProperSummary(mRing5));
            mRing5.setIcon(resize(getNavbarIconImage(mRing5)));
            }
        } else if (preference == mLongRing1) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[0];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING_LONG[0], (String) newValue);
            mLongRing1.setSummary(getProperSummary(mLongRing1));
            mLongRing1.setIcon(resize(getNavbarIconImage(mLongRing1)));
            }
        } else if (preference == mLongRing2) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[1];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING_LONG[1], (String) newValue);
            mLongRing2.setSummary(getProperSummary(mLongRing2));
            mLongRing2.setIcon(resize(getNavbarIconImage(mLongRing2)));
            }
        } else if (preference == mLongRing3) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[2];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING_LONG[2], (String) newValue);
            mLongRing3.setSummary(getProperSummary(mLongRing3));
            mLongRing3.setIcon(resize(getNavbarIconImage(mLongRing3)));
            }
        } else if (preference == mLongRing4) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[3];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING_LONG[3], (String) newValue);
            mLongRing4.setSummary(getProperSummary(mLongRing4));
            mLongRing4.setIcon(resize(getNavbarIconImage(mLongRing4)));
            }
        } else if (preference == mLongRing5) {
            mPreference = preference;
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[4];
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(),
                       Settings.System.SYSTEMUI_NAVRING_LONG[4], (String) newValue);
            mLongRing5.setSummary(getProperSummary(mLongRing5));
            mLongRing5.setIcon(resize(getNavbarIconImage(mLongRing5)));
            }
        }
        return result;
    }

    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
          mPreference.setSummary(friendlyName);
          Settings.System.putString(getContentResolver(), mString, (String) uri);
          mPreference.setIcon(resize(getNavbarIconImage(mPreference)));
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getProperSummary(Preference preference) {
        if (preference == mRing1) {
            mString = Settings.System.SYSTEMUI_NAVRING[0];
        } else if (preference == mRing2) {
            mString = Settings.System.SYSTEMUI_NAVRING[1];
        } else if (preference == mRing3) {
            mString = Settings.System.SYSTEMUI_NAVRING[2];
        } else if (preference == mRing4) {
            mString = Settings.System.SYSTEMUI_NAVRING[3];
        } else if (preference == mRing5) {
            mString = Settings.System.SYSTEMUI_NAVRING[4];
        } else if (preference == mLongRing1) {
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[0];
        } else if (preference == mLongRing2) {
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[1];
        } else if (preference == mLongRing3) {
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[2];
        } else if (preference == mLongRing4) {
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[3];
        } else if (preference == mLongRing5) {
            mString = Settings.System.SYSTEMUI_NAVRING_LONG[4];
        }

        String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
        String empty = "**null*";

        if (uri == null)
            return empty;

        if (uri.equals("**null**")) {
                return getResources().getString(R.string.none);
        } else if (uri.equals("**ime**")) {
                return getResources().getString(R.string.open_ime_switcher);
        } else if (uri.equals("**ring_vib**")) {
                return getResources().getString(R.string.ring_vib);
        } else if (uri.equals("**ring_silent**")) {
                return getResources().getString(R.string.ring_silent);
        } else if (uri.equals("**ring_vib_silent**")) {
                return getResources().getString(R.string.ring_vib_silent);
        } else if (uri.equals("**kill**")) {
                return getResources().getString(R.string.kill_app);
        } else if (uri.equals("**lastapp**")) {
                return getResources().getString(R.string.lastapp);
        } else if (uri.equals("**power**")) {
                return getResources().getString(R.string.screen_off);
        } else if (uri.equals("**assist**")) {
                return getResources().getString(R.string.google_now);
        } else {
                return mPicker.getFriendlyNameForUri(uri);
        }
   }

    private Drawable getNavbarIconImage(Preference preference) {
        if (preference == mRing1) {
            mString = Settings.System.SYSTEMUI_NAVRING[0];
        } else if (preference == mRing2) {
            mString = Settings.System.SYSTEMUI_NAVRING[1];
        } else if (preference == mRing3) {
            mString = Settings.System.SYSTEMUI_NAVRING[2];
        } else if (preference == mRing4) {
            mString = Settings.System.SYSTEMUI_NAVRING[3];
        } else if (preference == mRing5) {
            mString = Settings.System.SYSTEMUI_NAVRING[4];
        }

        String uri = Settings.System.getString(getActivity().getContentResolver(),mString);

        if (uri == null)
            return getResources().getDrawable(R.drawable.ic_sysbar_null);


            if (uri.equals("**null**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_null);
            } else if (uri.equals("**ime**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_ime_switcher);
            } else if (uri.equals("**ring_vib**")) {
                return getResources().getDrawable(R.drawable.ic_navbar_vib);
            } else if (uri.equals("**ring_silent**")) {
                return getResources().getDrawable(R.drawable.ic_navbar_silent);
            } else if (uri.equals("**ring_vib_silent**")) {
                return getResources().getDrawable(R.drawable.ic_navbar_ring_vib_silent);
            } else if (uri.equals("**kill**")) {
                return getResources().getDrawable(R.drawable.ic_navbar_killtask);
            } else if (uri.equals("**lastapp**")) {
                return getResources().getDrawable(R.drawable.ic_sysbar_lastapp);
            } else if (uri.equals("**power**")) {
                return getResources().getDrawable(R.drawable.ic_navbar_power);
            } else if (uri.equals("**assist**")) {
                return getResources().getDrawable(R.drawable.ic_navbar_googlenow);
            } else {
                try {
                   return mContext.getPackageManager().getActivityIcon(Intent.parseUri(uri, 0));
                } catch (NameNotFoundException e) {
                   e.printStackTrace();
                } catch (URISyntaxException e) {
                   e.printStackTrace();
                }
            }
        return getResources().getDrawable(R.drawable.ic_sysbar_null);
     }

    private Drawable resize(Drawable image) {
        int size = 50;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, getResources()
                .getDisplayMetrics());

        Bitmap d = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapOrig = Bitmap.createScaledBitmap(d, px, px, false);
        return new BitmapDrawable(mContext.getResources(), bitmapOrig);
    }
}
