
package com.aokp.romcontrol.github.tasks;

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

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.ImageView;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.github.CommitViewerDialog;
import com.aokp.romcontrol.github.CommitsFragment;
import com.aokp.romcontrol.github.Config;
import com.aokp.romcontrol.github.FavPackagesStorage;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * displays all projects from github organization
 */
public class DisplayProjectsListTask extends AsyncTask<Void, Void, Void> {
    private final String TAG = getClass().getSimpleName();
    private final boolean DEBUG = false;

    private CommitViewerDialog mAlertDialog;
    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private PreferenceScreen mPreferenceScreen;
    private final PreferenceCategory mCategory;
    private final PreferenceCategory mFavProjects;
    private final Config mConfig;
    private final int mId;

    private FavPackagesStorage mFavPackagesStorage;
    private JSONArray repoProjectsArray;
    private ArrayList<JSONArray> mPrefsList;
    private ProgressDialog mProgressDialog;

    public DisplayProjectsListTask(Context context, FragmentManager fm,
                                   PreferenceScreen preferenceScreen,
                                   PreferenceCategory category,
                                   PreferenceCategory favProjects,
                                   CommitViewerDialog alertDialog,
                                   int id) {
        this.mContext = context;
        this.mFragmentManager = fm;
        this.mPreferenceScreen = preferenceScreen;
        this.mCategory = category;
        this.mFavProjects = favProjects;
        this.mConfig = new Config();
        this.mAlertDialog = alertDialog;
        this.mId = id;
        if (mPrefsList == null)
            mPrefsList = new ArrayList<JSONArray>(0);
    }

    // can use UI thread here
    protected void onPreExecute() {
        // start with a clean view, always
        mFavProjects.removeAll();
        mCategory.removeAll();
        mPrefsList.removeAll(mPrefsList);
        mCategory.setTitle(mContext.getString(R.string.loading_projects));
        mCategory.setOrderingAsAdded(false);
        mFavPackagesStorage = new FavPackagesStorage();
        if (mFavPackagesStorage.getFavProjects().size() > 0) {
            mPreferenceScreen.addPreference(mFavProjects);
        } else {
            mPreferenceScreen.removePreference(mFavProjects);
        }
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mAlertDialog.getContext());
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.octacat);
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.github_octacat);
        }
    }

    // worker thread
    protected Void doInBackground(Void... unused) {
        HttpClient httpClient = null;
        try {
            httpClient = new DefaultHttpClient();
            Log.i(TAG, "requesting projects list from url: "
                + mConfig.REPO_URL + "?page=1&per_page=100");

            // request first 30 projects
            HttpGet requestWebsite = new HttpGet(mConfig.REPO_URL + "?page=1&per_page=100");
            // construct that handles relieving web streams to strings
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            // hold the response in a JSONArray
            repoProjectsArray = new JSONArray(httpClient.execute(requestWebsite, responseHandler));

            // debugging
            if (DEBUG) {
                Log.d(TAG, "repoProjectsArray.length() is: " + repoProjectsArray.length());
                Log.d(TAG, "was invalid test found: " + mFavPackagesStorage.isFavProject("test"));
                mFavPackagesStorage.addProject("test");
                Log.d(TAG, "was valid test found: " + mFavPackagesStorage.isFavProject("test"));
                mFavPackagesStorage.removeProject("test");
                Log.d(TAG, "was invalid test found: " + mFavPackagesStorage.isFavProject("test"));
            }

            int projectsReturned = repoProjectsArray.length();
            int page = 1; // because we returned the first 100 already
            while (projectsReturned <= 100) {
                HttpGet moreProjects = new HttpGet(mConfig.REPO_URL
                    + "?page=" + page++ + "&per_page=100");
                // construct that handles recieving web streams to strings
                ResponseHandler<String> responseHandler_ = new BasicResponseHandler();
                // hold the response in a JSONArray
                JSONArray moreProjectsArray = new JSONArray(
                    httpClient.execute(moreProjects, responseHandler_));
                mPrefsList.add(moreProjectsArray);
                // drop the loop when we find a return with < 100 projects listed
                if (moreProjectsArray.length() < 100) {
                    if (mProgressDialog != null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    break;
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "Bad json interaction...", je);
        } catch (IOException ioe) {
            Log.e(TAG, "IOException...", ioe);
        } catch (NullPointerException ne) {
            Log.e(TAG, "NullPointer...", ne);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }

    private void loadProjectsToArray(JSONArray repoProjectsArray) {
        // scroll through each item in array (projects in repo organization)
        for (int i = 0; i < repoProjectsArray.length(); i++) {
            try {
                final JSONObject projectsObject = (JSONObject) repoProjectsArray.get(i);
                final Preference mProject = mCategory.getPreferenceManager().createPreferenceScreen(mContext);
                // extract info about each project
                final String projectName = projectsObject.getString("name");
                String projectDescription = projectsObject.getString("description");
                int githubProjectId = projectsObject.getInt("id");
                // apply info to our preference screen
                mProject.setKey(projectName);
                if (projectDescription.contains("") || projectDescription == null) {
                    mProject.setTitle(projectName);
                    mProject.setSummary(projectDescription);
                } else {
                    mProject.setTitle(projectDescription);
                    mProject.setSummary(projectName);
                }
                mProject.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference p) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(mAlertDialog.getContext());
                        if (!mFavPackagesStorage.isFavProject(projectName)) {
                            adb.setNegativeButton(R.string.changelog_add_to_favs_list, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "fav packages size==" + mFavPackagesStorage.getFavProjects().size());
                                    if (mFavPackagesStorage.getFavProjects().size() > 0) {
                                        mPreferenceScreen.addPreference(mFavProjects);
                                    }
                                    mFavPackagesStorage.addProject(projectName);
                                    mCategory.removePreference(mProject);
                                    mFavProjects.addPreference(mProject);
                                }
                            }).setMessage(R.string.add_favs_or_view);
                        } else {
                            adb.setNegativeButton(R.string.changelog_remove_from_favs_list, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mFavPackagesStorage.removeProject(projectName);
                                    mFavProjects.removePreference(mProject);
                                    mCategory.addPreference(mProject);
                                    Log.d(TAG, "fav packages size==" + mFavPackagesStorage.getFavProjects().size());
                                    if (mFavPackagesStorage.getFavProjects().size() == 1) {
                                        mPreferenceScreen.removePreference(mFavProjects);
                                    }
                                }
                            }).setMessage(R.string.remove_favs_or_view);
                        }
                        adb.setPositiveButton(R.string.changelog_view_commits, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                                CommitsFragment commitFragment
                                    = new CommitsFragment(mAlertDialog, projectName);
                                transaction.addToBackStack(null);
                                transaction.replace(mId, commitFragment, projectName);
                                transaction.commit();
                            }
                        }).create().show();
                        return true;
                    }
                });
                if (mFavPackagesStorage.isFavProject(projectName)) {
                    if (mFavProjects.findPreference(projectName) == null) {
                        Log.d(TAG, "found Favorite Project: " + projectName);
                        mFavProjects.addPreference(mProject);
                    }
                } else {
                    if (DEBUG) Log.d(TAG, "adding normal project: " + projectName);
                    mCategory.addPreference(mProject);
                }
            } catch (JSONException badJsonRequest) {
                Log.e(TAG, "failed to parse required info about project", badJsonRequest);
            }
        }
        if (mFavPackagesStorage.getFavProjects().size() > 0)
            mPreferenceScreen.addPreference(mFavProjects);
    }

    private void addPropertiesToPreference(PreferenceCategory mProject, JSONObject projectsObject) {
        try {
            // extract info about each project
            final String projectName = projectsObject.getString("name");
            String projectDescription = projectsObject.getString("description");
            int githubProjectId = projectsObject.getInt("id");
            // apply info to our preference screen
            mProject.setKey(projectName);
            if (projectDescription.contains("") || projectDescription == null) {
                mProject.setTitle(projectName);
                mProject.setSummary(projectDescription);
            } else {
                mProject.setTitle(projectDescription);
                mProject.setSummary(projectName);
            }
            mProject.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference p) {
                    FragmentTransaction transaction = mFragmentManager.beginTransaction();
                    CommitsFragment commitFragment
                        = new CommitsFragment(mAlertDialog, projectName);
                    transaction.addToBackStack(null);
                    transaction.replace(mId, commitFragment, projectName);
                    transaction.commit();
                    return true;
                }
            });
        } catch (JSONException badJsonRequest) {
            Log.e(TAG, "failed to parse required info about project", badJsonRequest);
        }
    }

    // can use UI thread here
    protected void onPostExecute(Void unused) {
        for (int i = 0; mPrefsList.size() > i; i++) {
            loadProjectsToArray(mPrefsList.get(i));
        }

        mCategory.setTitle(mContext.getString(R.string.org_projects));
        mFavProjects.setTitle(mContext.getString(R.string.changelog_favorite_cat_title));
    }
}
