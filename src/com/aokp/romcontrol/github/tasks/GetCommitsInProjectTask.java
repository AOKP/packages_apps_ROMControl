
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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.ImageView;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.github.CommitViewerDialog;
import com.aokp.romcontrol.github.Config;
import com.aokp.romcontrol.github.objects.CommitObject;
import com.aokp.romcontrol.github.objects.GithubObject;
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
public class GetCommitsInProjectTask extends AsyncTask<String, Void, Void> {
    private final String TAG = getClass().getSimpleName();
    private final boolean DEBUG = false;

    private final CommitViewerDialog mAlertDialog;
    private final PreferenceCategory mCategory;
    private final Context mContext;
    private final Config mConfig;
    private ProgressDialog mProgressDialog;

    String LAST_SHA_;
    String BRANCH_;
    String PROJECT_;

    /**
     * gets commits from provided project
     * @param context application context
     * @param preferenceCategory container to hold commit views
     */
    public GetCommitsInProjectTask(Context context, PreferenceCategory preferenceCategory,
                                   CommitViewerDialog alertDialog) {
        mContext = context;
        mCategory = preferenceCategory;
        mConfig = new Config();
        mAlertDialog = alertDialog;
    }
    // inner class constants
    final String DEFAULT_BRANCH = "jb"; // TODO find a way to handle 'jellybean' branches
                                         // at the same time

    // UI thread
    protected void onPreExecute() {
        // show commit after we load next set
        mCategory.setTitle(mContext.getString(R.string.loading_commits));
        mCategory.setOrderingAsAdded(true);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mAlertDialog.getContext());
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.octacat);
            mProgressDialog.show();
            mProgressDialog.setContentView(R.layout.github_octacat);
        }
    }

    // worker thread
    protected Void doInBackground(String... params) {
        PROJECT_ = params[0];
        LAST_SHA_ = params[1];
        String requestCommits;
        if (LAST_SHA_ == null) {
            mCategory.removeAll();
            requestCommits = String.format(mConfig.INITIAL_COMMITS_REQUEST_FORMAT, PROJECT_);
        } else {
            requestCommits = String.format(mConfig.COMMITS_REQUEST_FORMAT, PROJECT_, LAST_SHA_);
        }
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

            String lastSha = null;
            // make a PreferenceScreen for all commits in package
            for (int i = 0; i < projectCommitsArray.length(); i++) {
                // make an object of each commit
                try {
                    final CommitObject commitObject =
                            new GithubObject(projectCommitsArray.getJSONObject(i));
                    // if we are looking for the next set the we skip the first commit
                    // as it is represented as the last commit from the first commit
                    // loading loop
                    //if (LAST_SHA_ != null && i == 1) {
                        //continue;
                    //}
                    // debugging
                    if (Config.StaticVars.JSON_SPEW)
                        Log.d(TAG, "commitObject.toString() is: " + commitObject.toString());

                    if ("merge".contains(commitObject.getSubject().substring(0, 5).toLowerCase()))
                        continue;
                    PreferenceScreen mCommit = mCategory.getPreferenceManager().createPreferenceScreen(mContext);
                    // apply info to our preference screen
                    mCommit.setKey(commitObject.getCommitHash());
                    mCommit.setTitle(commitObject.getSubject());
                    mCommit.setSummary(commitObject.getAuthorName());
                    mCommit.setOnPreferenceClickListener(
                            new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference p) {
                            mAlertDialog.setCommitAndShow(commitObject);
                            return true;
                        }
                    });
                    mCategory.addPreference(mCommit);
                    // most recent non null sha hash will be to iterate throught commits list
                    if (commitObject.getCommitHash() != null) {
                        lastSha = commitObject.getCommitHash();
                    }
                } catch (JSONException je) {
                    // no author found for commit
                    if (DEBUG) Log.d(TAG, "encountered a null value", je);
                }
            }

            if (projectCommitsArray.length() == 100) {
                // append next 100 commits onClick()
                final PreferenceScreen mNext = mCategory.getPreferenceManager()
                        .createPreferenceScreen(mContext);
                mNext.setTitle(mContext.getString(R.string.next_commits_page_title));
                mNext.setSummary(mContext.getString(R.string.next_commits_page_summary));
                final String finalString = lastSha;
                mNext.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference p) {
                        GetCommitsInProjectTask nextList = new GetCommitsInProjectTask(mContext, mCategory, mAlertDialog);
                        Log.d(TAG, "Sending project: " + PROJECT_ + " with last sha of " + finalString);
                        nextList.execute(PROJECT_, finalString);
                        // remove last entry it will be first entry in next list
                        mCategory.removePreference(mCategory.findPreference(finalString));
                        // don't keep in list after we click
                        mCategory.removePreference(mNext);
                        return true;
                    }
                });
                mCategory.addPreference(mNext);
            }
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
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
