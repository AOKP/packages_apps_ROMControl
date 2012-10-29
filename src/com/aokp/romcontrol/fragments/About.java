
package com.aokp.romcontrol.fragments;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.github.GithubViewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class About extends AOKPPreferenceFragment {

    public static final String TAG = "About";

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
        for(int i = 0; i < devs.size(); i++) {
            Preference p = devs.get(i);
            p.setOrder(i);
            devsGroup.addPreference(p);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://aokp.co/");
        } else if (preference == mReviewUrl) {
            Intent mGerrit = new Intent(getActivity().getApplicationContext(),
                    com.jbirdvegas.mgerrit.GerritControllerActivity.class);
            mGerrit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mGerrit);
        } else if (preference == mIrcUrl) {
            launchUrl("http://webchat.freenode.net/?channels=teamkang");
        } else if (preference == mDynamicChangelog) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            GithubViewer changelogFragment = new GithubViewer();
            transaction.addToBackStack(null);
            transaction.replace(this.getId(), changelogFragment);
            transaction.commit();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
        Intent github = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(github);
    }
}
