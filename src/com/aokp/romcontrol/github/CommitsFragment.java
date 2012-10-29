
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

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.aokp.romcontrol.AOKPPreferenceFragment;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.github.tasks.DisplayProjectsListTask;
import com.aokp.romcontrol.github.tasks.GetCommitsInProjectTask;
import com.aokp.romcontrol.github.tasks.GetJSONChangelogTask;

/**
 * Initial Screen shows all projects for github organization
 */
public class CommitsFragment extends AOKPPreferenceFragment {
    public static final String TAG = CommitsFragment.class.getSimpleName();
    private static final boolean DEBUG = false;
    private Context mContext;
    private static PreferenceCategory mCategory;
    private CommitViewerDialog mAlertDialog;
    private String mPath;
    private static final String PREF_CAT = "dynamic_changelog";
    private static final String PREF_FAVS = "favorite_projects";
    private PreferenceCategory mFavProjects;

    public CommitsFragment(CommitViewerDialog alertDialog, String path) {
        this.mAlertDialog = alertDialog;
        this.mPath = path;
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
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
            new GetCommitsInProjectTask(mContext, mCategory, mAlertDialog).execute(mPath, null);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        // remove old menu items
        menu.clear();
        // add projects as option
        // cant change branch if we are not viewing a project folder's commits
        if (GithubViewer.ARE_IN_PROJECT_PATH)
            menu.add(0, GithubViewer.MENU_ID_COMMITLOG, 0, getString(R.string.changelog_menu_commitlog_title));
        else
            menu.add(0, GithubViewer.MENU_ID_PACKAGES, 0, getString(R.string.changelog_menu_projects_title));
    }

    /**
     * handle Menu onClick actions
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case GithubViewer.MENU_ID_PACKAGES:
                new DisplayProjectsListTask(mContext,
                        getFragmentManager(),
                        getPreferenceScreen(),
                        mCategory,
                        mFavProjects,
                        mAlertDialog,
                        getId()).execute();
                // reset menu tracker variable
                GithubViewer.ARE_IN_PROJECT_PATH = true;
                return true;
            case GithubViewer.MENU_ID_COMMITLOG:
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                CommitsFragment commitFragment = new CommitsFragment(mAlertDialog, null);
                //transaction.addToBackStack(null);
                transaction.replace(this.getId(), commitFragment, "changelog").commit();
                GithubViewer.ARE_IN_PROJECT_PATH = false;
                return true;
            // This should never happen but just in case let the system handle the return
            default:
                return super.onContextItemSelected(item);
        }
    }
}