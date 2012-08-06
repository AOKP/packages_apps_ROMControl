package com.baked.romcontrol.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.baked.romcontrol.BAKEDPreferenceFragment;
import com.baked.romcontrol.R;

public class About extends BAKEDPreferenceFragment {

    public static final String TAG = "About";

    Preference mSiteUrl;
    Preference mSourceUrl;
    Preference mReviewUrl;
    Preference mIrcUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs_about);
        mSiteUrl = findPreference("baked_website");
        mSourceUrl = findPreference("baked_source");
        mIrcUrl = findPreference("baked_irc");

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://teamicemods.com/");
        } else if (preference == mSourceUrl) {
            launchUrl("http://github.com/teambaked");
        } else if (preference == mIrcUrl) {
            launchUrl("http://webchat.freenode.net/?channels=teamBAKED");
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchIntent = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(launchIntent);
    }
}
