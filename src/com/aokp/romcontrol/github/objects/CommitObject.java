
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

import com.aokp.romcontrol.github.CommitInterface;
import com.aokp.romcontrol.github.Config;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommitObject implements CommitInterface {
    private final String TAG = getClass().getSimpleName();
    private static final boolean DEBUG = false;
    private static String DEFAULT = "aokp_default";
    public JSONObject mJsonObject = null;
    public String mTeamCredit = null;
    public String mPath = null;
    public String mCommitHash = null;
    public String mUrl = null;
    public String mParentHashes = null;
    public String mAuthorName = null;
    public String mAuthorGravatar = null;
    public String mAuthorDate = null;
    public String mCommitterName = null;
    public String mCommitterGravatar = null;
    public String mCommitterDate = null;
    public String mSubject = null;
    public String mBody = null;

    public Config mConfig;

    /**
     * base initialization for our 'Commit Objects'
     * @param jsonObject
     */
    protected CommitObject(JSONObject jsonObject) {
        mConfig = new Config();
        parseObject(jsonObject);
    }

    /**
     * provides an method for reusing the same object
     *
     * useful if used in loop implemtation
     *
     * returns this CommitObject so this method
     * can be used to string commands
     * @param jsonObject
     * @return
     */
    public CommitObject reParse(JSONObject jsonObject) {
        parseObject(jsonObject);
        return this;
    }

    /**
     * parses the JSONArray represented by the key into human
     * readable string return split by ', ' delimiter
     * @param array JSONArray to be parsed
     * @param key reference to required value within array
     * @return values formatted into readable String
     */
    public static String parseToString(JSONArray array, String key) {
        String out = "";
        for (int i = 0; array.length() > i; i++) {
            try {
                out += array.getJSONObject(i).getString(key);
            } catch (JSONException e) {
                return DEFAULT;
            }
        }
        return out;
    }

    /**
     * gets a String value held by key provided
     * @param queryReference key used to find value
     * @return value found or DEFAULT if exception occurred
     */
    public String parseValue(String queryReference) {
        String str;
        try {
            str = mJsonObject.getString(queryReference);
        } catch (JSONException e) {
            str = DEFAULT;
        }
        return str;
    }

    /**
     * Team the project's commit was pulled from
     * @return Team association of commit
     */
    public String getTeamCredit() {
        return mTeamCredit;
    }

    /**
     * getter for path of the project
     * @return path of project containing commit
     */
    public String getPath() {
        return mPath;
    }

    /**
     * sha-1 tag used to ensure data integrity and source
     * @return hash tag associated with this commit to this project
     */
    public String getCommitHash() {
        return mCommitHash;
    }

    /**
     * human readable URL of commit hosted on github
     * @return
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * the default value returned from parseToString if an exception is thrown
     * @return
     */
    public String getDefault() {
        return DEFAULT;
    }

    /**
     * sha hash tags of the commit's parent/parents
     * @return returns a single line representation of these hashes if more than
     *         one exists
     */
    public String getParentHashes() {
        return mParentHashes;
    }

    /**
     * name of commit author
     * @return Authors public name associated with their account
     */
    public String getAuthorName() {
        return mAuthorName;
    }

    /**
     * date of authorship
     * @return String formatted date of the commit
     *         when committed to authors local machine
     */
    public String getAuthorDate() {
        return mAuthorDate;
    }

    /**
     * name of commit committer
     * @return Committers public name associated with their account
     */
    public String getCommitterName() {
        return mCommitterName;
    }

    /**
     * date merged
     * @return date commit was merged with main build tree
     */
    public String getCommitterDate() {
        return mCommitterDate;
    }

    /**
     * Commits subject can be equated to a title
     * @return commit subject
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * full commit message
     * @return string of the full commit message
     */
    public String getBody() {
        return mBody;
    }

    /**
     * get image associated with committer github
     * @return url of gravatar image
     */
    public String getCommitterGravatar() {
        return mCommitterGravatar;
    }

    /**
     * get image associated with author github
     * @return url of gravatar image
     */
    public String getAuthorGravatar() {
        return mAuthorGravatar;
    }

    @Override
    public String toString() {
        String dlimit = "; ";
        return TAG + " { "
                + "default:" + DEFAULT + dlimit
                + "teamCredit:" + mTeamCredit + dlimit
                + "path:" + mPath + dlimit
                + "commitHash:" + mCommitHash + dlimit
                + "url:" + mUrl + dlimit
                + "parentHashes:" + mParentHashes + dlimit
                + "authorName:" + mAuthorName + dlimit
                + "authorGravatar:" + mAuthorGravatar + dlimit
                + "authorDate:" + mAuthorDate + dlimit
                + "committerName:" + mCommitterName + dlimit
                + "committerGravatar:" + mCommitterGravatar + dlimit
                + "committerDate:" + mCommitterDate + dlimit
                + "subject:" + mSubject + dlimit
                + "body:" + mBody + dlimit
                + "raw_commit_json:" + mJsonObject.toString()
                + " }";
    }

    @Override
    public boolean equals(Object obj) {
        try {
            CommitObject commit = (CommitObject) obj;
            return this.getTeamCredit().equals(commit.getTeamCredit())
                    && this.mPath.equals(commit.getPath())
                    && this.mCommitHash.equals(commit.getCommitHash())
                    && this.mUrl.equals(commit.getUrl())
                    && this.mParentHashes.equals(commit.getParentHashes())
                    && this.mAuthorName.equals(commit.getAuthorName())
                    && this.mAuthorGravatar.equals(commit.getAuthorGravatar())
                    && this.mAuthorDate.equals(commit.getAuthorDate())
                    && this.mCommitterName.equals(commit.getCommitterName())
                    && this.mCommitterGravatar.equals(commit.getCommitterGravatar())
                    && this.mCommitterDate.equals(commit.getCommitterDate())
                    && this.mSubject.equals(commit.getSubject())
                    && this.mBody.equals(commit.getBody());
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void parseObject(JSONObject jsonObject) {
        // stop looking at me swan!!!
    }
}
