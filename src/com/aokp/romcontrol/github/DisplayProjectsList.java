
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
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.aokp.romcontrol.R;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * displays all projects from github organization
 */
public class DisplayProjectsList extends AsyncTask<Void, Void, Void> {
    private final String TAG = getClass().getSimpleName();
    private final boolean DEBUG = false;

    private CommitViewerDialog mAlertDialog;
    private final Context mContext;
    private final PreferenceCategory mCategory;
    private final Config mConfig;

    public DisplayProjectsList(Context context, PreferenceCategory category,
                               CommitViewerDialog alertDialog) {
        mContext = context;
        mCategory = category;
        mConfig = new Config();
        mAlertDialog = alertDialog;
    }

    // can use UI thread here
    protected void onPreExecute() {
        // start with a clean view, always
        mCategory.removeAll();
        mCategory.setTitle(mContext.getString(R.string.loading_projects));
    }

    // worker thread
    protected Void doInBackground(Void... unused) {
        HttpClient httpClient = null;
        try {
            httpClient = new DefaultHttpClient();
            Log.i(TAG, "attempting to connect to: " + mConfig.REPO_URL + "?page=1&per_page=100");

            // request first 30 projects
            HttpGet requestWebsite = new HttpGet(mConfig.REPO_URL + "?page=1&per_page=100");
            // construct that handles recieving web streams to strings
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            // hold the response in a JSONArray
            JSONArray repoProjectsArray =
                    new JSONArray(httpClient.execute(requestWebsite, responseHandler));

            // debugging
            if (DEBUG)
                Log.d(TAG, "repoProjectsArray.length() is: " + repoProjectsArray.length());

            int projectsReturned = repoProjectsArray.length();
            int page = 1; // because we returned the first 100 already
            while (projectsReturned <= 100) {
                HttpGet moreProjects = new HttpGet(mConfig.REPO_URL + "?page=" + page++ + "&per_page=100");
                // construct that handles recieving web streams to strings
                ResponseHandler<String> responseHandler_ = new BasicResponseHandler();
                // hold the response in a JSONArray
                JSONArray moreProjectsArray = new JSONArray(httpClient.execute(moreProjects, responseHandler_));
                loadProjectsToScreen(moreProjectsArray);

                // drop the loop when we find a return with < 100 projects listed
                projectsReturned = moreProjectsArray.length();
                if (projectsReturned < 100) {
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

    private void loadProjectsToScreen(JSONArray repoProjectsArray) {
        try {
            // scroll through each item in array (projects in repo organization)
            for (int i = 0; i < repoProjectsArray.length(); i++) {
                PreferenceScreen mProject = mCategory.getPreferenceManager()
                        .createPreferenceScreen(mContext);
                // make an object of each repo
                JSONObject projectsObject = (JSONObject) repoProjectsArray.get(i);

                // extract info about each project
                final String projectName = projectsObject.getString("name");
                String projectDescription = projectsObject.getString("description");
                int githubProjectId = projectsObject.getInt("id");

                // apply info to our preference screen
                mProject.setKey(githubProjectId + "");
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
                        GetCommitsInProjectTask listFirstCommits =
                                new GetCommitsInProjectTask(mContext.getApplicationContext(),
                                mCategory, mAlertDialog);
                        listFirstCommits.execute(new String[] { projectName, "1" });
                        return true;
                    }
                });
                mCategory.addPreference(mProject);
            }
        } catch (JSONException badJsonRequest) {
            Log.e(TAG, "failed to parse required info about project", badJsonRequest);
        }
    }

    // can use UI thread here
    protected void onPostExecute(Void unused) {
        mCategory.setTitle(mContext.getString(R.string.org_projects));
    }
}
