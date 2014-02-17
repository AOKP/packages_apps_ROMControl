/*
 * Copyright (C) 2013 The Android Open Kang Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aokp.romcontrol.fragments.about;

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
    private static final String MGERRIT_VU_CHANGELOG = ".VUChangelog";
    private static final String MGERRIT_PLAYSTORE = "https://play.google.com/store/apps/details?id=com.jbirdvegas.mgerrit";

    public AboutFragment() {
        // empty fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_aokp, container, false);

        root.findViewById(R.id.vu_mgerrit_changelog).setOnClickListener(new OnClickListener() {
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

        root.findViewById(R.id.vu_review).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // open mGerrit if we can. otherwise launch gerrit url.
                try {
                    launchActivity(MGERRIT, MGERRIT_VU_CHANGELOG);
                } catch (ActivityNotFoundException failToMarket) {
                    launchUrl(getString(R.string.url_vu_gerrit));
                }

            }
        });

        return root;
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
