
package com.aokp.romcontrol.github;

/*
 * Copyright (C) 2012 The Android Open Kang Project
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

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;

import com.aokp.romcontrol.R;
import com.aokp.romcontrol.github.Config;
import com.aokp.romcontrol.github.GetJSONChangelogTask;

/**
 * Initial Screen shows all projects for github organization
 */
public class GithubViewer extends PreferenceFragment {
    private static final boolean DEBUG = true;
    private final String TAG = getClass().getSimpleName();

    private static final String PREF_CAT = "dynamic_changelog";

    private Config mConfig;
    private Context mContext;
    private static PreferenceCategory mCategory;

    // Menu item ids (101+)
    private static final int MENU_ID_PACKAGES = 101;
    private static final int MENU_ID_COMMITLOG = 102;
    private boolean ARE_IN_PROJECT_PATH;

    /**
     * create applet
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = new Config();
        mContext = getActivity().getApplicationContext();
        mConfig.setORGANIZATION(mContext);

        addPreferencesFromResource(R.xml.dynamic_changelog);
        mCategory = (PreferenceCategory) findPreference(PREF_CAT);
        // important to set ordering before we populate screen
        mCategory.setOrderingAsAdded(false);
        mCategory.setTitle(getString(R.string.dynamic_changelog_cat_title));
        if (savedInstanceState == null) {
            new DisplayProjectsList(mContext, mCategory).execute();
        }
        ARE_IN_PROJECT_PATH = true;
        setHasOptionsMenu(true);
    }

    /**
     * this is the only method called right before every display of the menu
     * here we choose what dynamic content to display for the menu
     */
        public void onPrepareOptionsMenu(Menu menu) {
        // remove old menu items
        menu.clear();

        // cant change branch if we are not viewing a project folder's commits
        if (ARE_IN_PROJECT_PATH)
            menu.add(0, MENU_ID_COMMITLOG, 0, getString(R.string.changelog_menu_commitlog_title));
        else
            menu.add(0, MENU_ID_PACKAGES, 0, getString(R.string.changelog_menu_projects_title));
    }


    /**
     * handle Menu onClick actions
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case MENU_ID_PACKAGES:
                new DisplayProjectsList(mContext, mCategory).execute();
                // reset menu tracker variable
                ARE_IN_PROJECT_PATH = true;
                return true;
            case MENU_ID_COMMITLOG:
                new GetJSONChangelogTask(mContext, mCategory).execute();
                ARE_IN_PROJECT_PATH = false;
                return true;

            // This should never happen but just in case let the system handle the return
            default:
                return super.onContextItemSelected(item);
        }
    }
}
