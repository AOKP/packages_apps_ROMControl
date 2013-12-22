package com.aokp.romcontrol.fragments;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.aokp.romcontrol.R;


public class AboutFragment extends Fragment {

    private static final String MGERRIT = "com.jbirdvegas.mgerrit";
    private static final String MGERRIT_MAIN_ENTRY = ".GerritControllerActivity";
    private static final String MGERRIT_AOKP_CHANGELOG = ".AOKPChangelog";
    private static final String MGERRIT_PLAYSTORE = "https://play.google.com/store/apps/details?id=com.jbirdvegas.mgerrit";

    public AboutFragment() {
        // empty fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_aokp, container, false);

        root.findViewById(R.id.aokp_mgerrit_changelog).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // open mGerrit if we can. otherwise launch gerrit url.
                try {
                    launchActivity(MGERRIT, MGERRIT_MAIN_ENTRY);
                } catch (ActivityNotFoundException failToMarket) {
                    launchUrl(MGERRIT_PLAYSTORE);
                }

            }
        });

        root.findViewById(R.id.aokp_review).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // open mGerrit if we can. otherwise launch gerrit url.
                try {
                    launchActivity(MGERRIT, MGERRIT_AOKP_CHANGELOG);
                } catch (ActivityNotFoundException failToMarket) {
                    launchUrl(getString(R.string.url_aokp_gerrit));
                }

            }
        });

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO scramble dev list
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent website = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(website);
    }

    private void launchActivity(String packageName, String activity)
            throws ActivityNotFoundException {
        Intent launch = new Intent();
        launch.setComponent(new ComponentName(packageName, packageName + activity));
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(launch);
    }

}
