
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
import android.content.Intent;
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

import java.io.IOException;

/**
 * Displays most recent commits (30) for provided repository
 */
public class GetCommitsInProjectTask extends AsyncTask<Void, Void, Void> {
    private final String TAG = getClass().getSimpleName();
    private final boolean DEBUG = true;

    private final PreferenceCategory mCategory;
    private final Context mContext;
    private final Config mConfig;

    int PAGE_ = - 1;
    String BRANCH_;
    String PROJECT_;

    /**
     * gets commits from provided project
     * @param context application context
     * @param preferenceCategory container to hold commit views
     */
    public GetCommitsInProjectTask(Context context, PreferenceCategory preferenceCategory) {
        mContext = context;
        mCategory = preferenceCategory;
        mConfig = new Config();
    }
    // inner class constants
    final String DEFAULT_BRANCH = "jb"; // TODO find a way to handle 'jellybean' branches
                                        // at the same time

    // UI thread
    protected void onPreExecute() {
        // show commit after we load next set
        mCategory.setTitle(mContext.getString(R.string.loading_commits));
        if (PAGE_ <= 1)
            mCategory.removeAll();
    }

    // worker thread
    protected Void doInBackground(Void... unused) {
        // so we don't acidentally crash the ui
        if (PROJECT_ == null || PAGE_ == - 1)
            return null;

        // TODO: deal with branches later
        String requestCommits = String.format(mConfig.COMMITS_REQUEST_FORMAT, PROJECT_, PAGE_);

        if (BRANCH_ == null) BRANCH_ = DEFAULT_BRANCH;
        try {
            HttpClient httpClient = new DefaultHttpClient();

            Log.i(TAG, "attempting to connect to: " + requestCommits);
            HttpGet requestWebsite = new HttpGet(requestCommits);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            JSONArray projectCommitsArray = new JSONArray(httpClient.execute(requestWebsite, responseHandler));

            // debugging
            if (DEBUG)
                Log.d(TAG, "projectCommitsArray.length() is: " + projectCommitsArray.length());
            if (Config.StaticVars.JSON_SPEW)
                Log.d(TAG, "projectCommitsArray.toString() is: " + projectCommitsArray.toString());

            // make a PreferenceScreen for all commits in package
            for (int i = 0; i < projectCommitsArray.length(); i++) {
                PreferenceScreen mCommit = mCategory.getPreferenceManager().createPreferenceScreen(mContext);
                // make an object of each commit
                try {
                    final CommitObject commitObject = new GithubObject(projectCommitsArray.getJSONObject(i));

                    // apply info to our preference screen
                    mCommit.setKey(commitObject.getCommitHash());
                    mCommit.setTitle(commitObject.getSubject());
                    mCommit.setSummary(commitObject.getAuthorName());
                    mCommit.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference p) {
                            // set variables
                            Config.StaticVars.AUTHOR_GRAVATAR_URL = commitObject.getAuthorGravatar();
                            Config.StaticVars.COMMITTER_GRAVATAR_URL = commitObject.getCommitterGravatar();
                            Config.StaticVars.COMMIT_COMMITTER = commitObject.getCommitterName();
                            Config.StaticVars.PROJECT = PROJECT_;
                            Config.StaticVars.COMMIT_URL = commitObject.getUrl();
                            Config.StaticVars.COMMIT_AUTHOR = commitObject.getAuthorName();
                            Config.StaticVars.COMMIT_MESSAGE = commitObject.getBody();
                            Config.StaticVars.COMMIT_DATE = commitObject.getAuthorDate();
                            Config.StaticVars.COMMIT_SHA = commitObject.getCommitHash();

                            // launch dialog
                            Intent intent = new Intent();
                            intent.setClass(mContext, CommitViewerDialog.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                            return true;
                        }
                    });

                    mCategory.addPreference(mCommit);
                } catch (JSONException je) {
                    // no author found for commit
                    if (DEBUG) Log.d(TAG, "encountered a null value", je);
                }
            }
            // append next 30 commits onClick()
            final PreferenceScreen mNext = mCategory.getPreferenceManager().createPreferenceScreen(mContext);
            mNext.setTitle(mContext.getString(R.string.next_commits_page_title));
            mNext.setSummary(mContext.getString(R.string.next_commits_page_summary));
            mNext.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference p) {
                    GetCommitsInProjectTask nextList = new GetCommitsInProjectTask(mContext, mCategory);
                    nextList.PAGE_ = PAGE_ + 1; // next page of commits (30)
                    nextList.PROJECT_ = PROJECT_; // stay in same project folder
                    nextList.execute();
                    mCategory.removePreference(mNext); // don't keep in list after we click
                    return true;
                }
            });
            // avoid adding if we don't have commits, prob network fail :-/
            if (mCategory.getPreferenceCount() > 1)
                mCategory.addPreference(mNext);
        } catch (JSONException je) {
            if (DEBUG) Log.e(TAG, "Bad json interaction...", je);
        } catch (IOException ioe) {
            if (DEBUG) Log.e(TAG, "IOException...", ioe);
        } catch (NullPointerException ne) {
            if (DEBUG) Log.e(TAG, "NullPointer...", ne);
        }
        return null;
    }

    // UI thread
    protected void onPostExecute(Void unused) {
        mCategory.setTitle(mContext.getString(R.string.commits_title));
    }
}
