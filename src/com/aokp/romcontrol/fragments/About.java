package com.aokp.romcontrol.fragments;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

import java.util.ArrayList;
import java.util.Collections;

public class About extends AOKPPreferenceFragment {
    public static final String TAG = "About";

    private static final String AOKP = "http://aokp.co/";
    private static final String MGERRIT = "com.jbirdvegas.mgerrit";
    private static final String MGERRIT_PLAYSTORE = "https://play.google.com/store/apps/details?id=com.jbirdvegas.mgerrit";
    private static final String MGERRIT_MAIN_ENTRY = ".GerritControllerActivity";
    private static final String MGERRIT_AOKP_CHANGELOG = ".AOKPChangelog";
    private static final String TEAMKANG_IRC = "http://webchat.freenode.net/?channels=teamkang";

    Preference mSiteUrl;
    Preference mReviewUrl;
    Preference mIrcUrl;
    Preference mDynamicChangelog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_about);
        addPreferencesFromResource(R.xml.prefs_about);
        mSiteUrl = findPreference("aokp_website");
        mReviewUrl = findPreference("aokp_review");
        mIrcUrl = findPreference("aokp_irc");
        mDynamicChangelog = findPreference("aokp_dynamic_changelog");

        PreferenceGroup devsGroup = (PreferenceGroup) findPreference("devs");
        ArrayList<Preference> devs = new ArrayList<Preference>();
        for (int i = 0; i < devsGroup.getPreferenceCount(); i++) {
            devs.add(devsGroup.getPreference(i));
        }
        devsGroup.removeAll();
        devsGroup.setOrderingAsAdded(false);
        Collections.shuffle(devs);
        for (int i = 0; i < devs.size(); i++) {
            Preference p = devs.get(i);
            p.setOrder(i);
            devsGroup.addPreference(p);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl(AOKP);
            return true;
        } else if (preference == mReviewUrl) {
            try {
                launchActivity(MGERRIT, MGERRIT_MAIN_ENTRY);
            } catch(ActivityNotFoundException failToMarket) {
                launchUrl(MGERRIT_PLAYSTORE);
            }
            return true;
        } else if (preference == mIrcUrl) {
            launchUrl(TEAMKANG_IRC);
            return true;
        } else if (preference == mDynamicChangelog) {
            try {
                launchActivity(MGERRIT, MGERRIT_AOKP_CHANGELOG);
            } catch (ActivityNotFoundException failToMarket) {
                launchUrl(MGERRIT_PLAYSTORE);
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchActivity(String packageName, String activity)
            throws ActivityNotFoundException {
       Intent launch = new Intent();
       launch.setComponent(new ComponentName(packageName, packageName + activity));
       launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       getActivity().startActivity(launch);
   }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent website = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(website);
    }
}
