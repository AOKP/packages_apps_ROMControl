
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

import android.os.AsyncTask;
import android.util.Log;
import com.aokp.romcontrol.github.objects.GithubObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Simple network communication to get the full all available fields from
 * api.github.com about a single commit
 *
 * The full GithubCommit will be delivered to your UI thread appon completion.
 */
public abstract class FindMissingCommitDataTask extends AsyncTask<String, Void, GithubObject> {
    private final String TAG = getClass().getSimpleName();

    /**
     * gets full GithubObject from github using the commits path and hash to
     * find the full commit data
     * @param params github api url for commit
     * @return GithubObject containing all possible information
     *         (with all fields the possibility of null exists)
     */
    @Override
    protected GithubObject doInBackground(String... params) {
        try {
            Log.i(TAG, "looking for commit data @ " + params[0]);
            // damn almost got it into a oneliner :P
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            return new GithubObject(
                new JSONObject(
                    new DefaultHttpClient().execute(
                        new HttpGet(params[0]), responseHandler)));
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * called when communication with github has finished
     * @param result GithubObject with all possible information
     *        (with all fields the possibility of null exists
     *         along with the possibility we failed and the
     *         Object itself is null, always check)
     */
    protected abstract void onPostExecute(GithubObject result);
}