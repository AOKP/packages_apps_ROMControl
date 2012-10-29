
package com.aokp.romcontrol.github.objects;

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

import com.aokp.romcontrol.github.Config;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.StringTokenizer;

/**
 * implemtation of CommitObject that handles commit query responces from github
 *
 * this implemtation of CommitObject handles many more values
 * than the ChangelogObject.  This object is where we pull values
 * for the CommitViewerDialog.
 */
public class GithubObject extends CommitObject {
    Config mConfig;
    public GithubObject(JSONObject jsonObject) {
        super(jsonObject);
        mConfig = new Config();
    }

    /**
     * implemtation of our interface, CommitInterface.
     *
     * Creates an object representing a commit from github organization
     * @param jsonObject json formatted object to be parsed
     */
    @Override
    @SuppressWarnings("UseOfStringTokenizer") // ignore BS warning
    public void parseObject(JSONObject jsonObject) {
        super.parseObject(jsonObject);
        mJsonObject = jsonObject;
        mTeamCredit = Config.ORGANIZATION;
        mUrl = parseValue("url");
        mCommitHash = parseValue("sha");
        // parent hashes
        try {
            mParentHashes = parseToString(mJsonObject.getJSONArray("parents"), "sha");
        } catch (JSONException ignored) {
            // failed to get parent hashes
        }

        // author && committer from commit JSONObject
        // also subject and body
        try {
            JSONObject commitJson = mJsonObject.getJSONObject("commit");
            // author
            try {
                mAuthorName = commitJson.getJSONObject("author").getString("name");
                mAuthorDate = commitJson.getJSONObject("author").getString("date");
            } catch (JSONException ignored) {
                // failed to find author name|date
            }

            // committer
            try {
                mCommitterName = commitJson.getJSONObject("committer").getString("name");
                mCommitterDate = commitJson.getJSONObject("committer").getString("date");
            } catch (JSONException ignored) {
                // failed to find committer|date
            }

            try {
                mBody = commitJson.getString("message");
                mSubject = new StringTokenizer(mBody, "\n\n").nextToken();
            } catch (JSONException ignored) {
                // failed to find body|subject
            }
        } catch (JSONException ignored) {
            // failed to get commit object
        }
        // author avatar url
        try {
            mAuthorGravatar =
                mJsonObject.getJSONObject("author")
                    .getString("avatar_url");
        } catch (JSONException ignored) {
            // failed to find author avatar
        }
        // committer avatar url
        try {
            mCommitterGravatar =
                mJsonObject.getJSONObject("committer")
                    .getString("avatar_url");
        } catch (JSONException ignored) {
            // failed to find committer
        }
        // path
        StringTokenizer pathChunks = new StringTokenizer(mUrl, "/");
        String prevToken = null;
        while (pathChunks.hasMoreTokens()) {
            String stringToken = pathChunks.nextToken();
            // if the token matches "commits" then the
            // previous token represents our path
            if (stringToken != null && "commits".equals(stringToken)) {
                mPath = prevToken;
                break;
            } else
                prevToken = stringToken;
        }
    }
}
