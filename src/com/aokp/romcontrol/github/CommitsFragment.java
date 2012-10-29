
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

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;

/**
 * Initial Screen shows all projects for github organization
 */
public class CommitsFragment extends AOKPPreferenceFragment {
    private final String TAG = getClass().getSimpleName();
    private static final boolean DEBUG = false;
    private Config mConfig;
    private Context mContext;
    private FragmentManager mFragmentManager;
    private static PreferenceCategory mCategory;
    private CommitViewerDialog mAlertDialog;

    private String mPath;

    public static final String PATH = "path";
    private static final String PREF_CAT = "dynamic_changelog";
    private static final String PREF_FAVS = "favorite_projects";
    private static final int MENU_ID_PACKAGES = 101;

    private PreferenceCategory mFavProjects;

    public CommitsFragment(CommitViewerDialog alertDialog, FragmentManager fm, String path) {
        this.mAlertDialog = alertDialog;
        this.mFragmentManager = fm;
        this.mPath = path;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = new Config();
        mContext = getActivity().getApplicationContext();
        mConfig.setORGANIZATION(mContext);
        addPreferencesFromResource(R.xml.github_projects_list);
        mCategory = (PreferenceCategory) findPreference(PREF_CAT);
        mFavProjects = (PreferenceCategory) findPreference(PREF_FAVS);
        getPreferenceScreen().removePreference(mFavProjects);
        setHasOptionsMenu(true);
        if (mPath == null) {
            if (DEBUG) Log.d(TAG, "no path sent assuming we are looking for our changelog");
            new GetJSONChangelogTask(mContext, mCategory, mAlertDialog).execute();
        } else {
            if (DEBUG) Log.d(TAG, "path sent: " + mPath);
            new GetCommitsInProjectTask(mContext, mCategory, mAlertDialog).execute(new String[] { mPath, "1" });
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        // remove old menu items
        menu.clear();
        // add projects as option
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
                DisplayProjectsListTask showProjects
                        = new DisplayProjectsListTask(mContext,
                                mFragmentManager,
                                getPreferenceScreen(),
                                mCategory,
                                mFavProjects,
                                mAlertDialog,
                                getId());
                showProjects.execute();
                return true;
            // This should never happen but just in case let the system handle the return
            default:
                return super.onContextItemSelected(item);
        }
    }
}
