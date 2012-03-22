
package com.aokp.romcontrol.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

public class About extends AOKPPreferenceFragment {

    public static final String TAG = "About";

    Preference mSiteUrl;
    Preference mSourceUrl;
    Preference mIrcUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs_about);
        mSiteUrl = findPreference("aokp_website");
        mSourceUrl = findPreference("aokp_source");
        mIrcUrl = findPreference("aokp_irc");

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://aokp.co/");
        } else if (preference == mSourceUrl) {
            launchUrl("http://github.com/aokp");
        } else if (preference == mIrcUrl) {
            launchUrl("http://webchat.freenode.net/?channels=teamkang");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
    }
}
