
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

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.aokp.romcontrol.R;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Displays parsed changelog from our changelog generator
 * located @ ./vendor/aokp/bot/denseChangelog.sh
 */
public class GetJSONChangelogTask extends AsyncTask<Void, Void, Void> {
    private final boolean DEBUG = true;
    private final boolean STATIC_DEBUG = true;
    private final String TAG = getClass().getSimpleName();
    private CommitViewerDialog mAlertDialog;
    private final Context mContext;
    private final PreferenceCategory mCategory;

    Config mConfig;

    /**
     * parses our changelog from json to Preferences
     * @param context application context
     * @param category container to hold commit views
     */
    public GetJSONChangelogTask(Context context, PreferenceCategory category,
                                CommitViewerDialog alertDialog) {
        mContext = context;
        mCategory = category;
        mConfig = new Config();
        mAlertDialog = alertDialog;
    }

    // UI thread
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
            String url = String.valueOf(STATIC_DEBUG
                ? "https://raw.github.com/JBirdVegas/tests/master/example.json"
                : mConfig.CHANGELOG_JSON);
            HttpGet requestWebsite = new HttpGet(url);
            Log.d(TAG, "attempting to connect to: " + url);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            JSONArray projectCommitsArray =
                    new JSONArray(httpClient.execute(requestWebsite, responseHandler));

            // debugging
            if (DEBUG)
                Log.d(TAG, "projectCommitsArray.length() is: "
                        + projectCommitsArray.length());
            if (Config.StaticVars.JSON_SPEW)
                Log.d(TAG, "projectCommitsArray.toString() is: "
                        + projectCommitsArray.toString());

            final ChangelogObject commitObject = new ChangelogObject(new JSONObject());
            for (int i = 0; i < projectCommitsArray.length(); i++) {
                JSONObject projectsObject =
                    (JSONObject) projectCommitsArray.get(i);
                PreferenceScreen newCommitPreference = mCategory.getPreferenceManager()
                    .createPreferenceScreen(mContext);
                commitObject.reParse(projectsObject);
                newCommitPreference.setTitle(commitObject.getSubject());
                newCommitPreference.setSummary(commitObject.getBody());
                newCommitPreference.setKey(commitObject.getCommitHash());
                newCommitPreference.setOnPreferenceClickListener(
                        new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        mAlertDialog.setCommitAndShow(commitObject);
                        return false;
                    }
                });
                mCategory.addPreference(newCommitPreference);
            }
        } catch (HttpResponseException httpError) {
            Log.e(TAG, "bad HTTP response:", httpError);
        } catch (ClientProtocolException e) {
	    Log.d(TAG, "client protocal exception:", e);
        } catch (JSONException e) {
            Log.d(TAG, "bad json interaction:", e);
        } catch (IOException e) {
            Log.d(TAG, "io exception:", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }

    // UI thread
    protected void onPostExecute(Void unused) {
        // meh...
    }
}
