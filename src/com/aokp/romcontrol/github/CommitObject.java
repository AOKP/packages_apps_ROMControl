
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommitObject implements CommitInterface {
    private static String DEFAULT = null;
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
        DEFAULT = "";
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
                return out;
            }
        }
        return out;
    }

    public String parseValue(String queryReference) {
        String str;
        try {
            str = mJsonObject.getString(queryReference);
        } catch (JSONException e) {
            str = DEFAULT;
        }
        return str;
    }

    public String getTeamCredit() {
        return mTeamCredit;
    }

    public String getPath() {
        return mPath;
    }

    public String getCommitHash() {
        return mCommitHash;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDefault() {
        return DEFAULT;
    }

    public String getParentHashes() {
        return mParentHashes;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getAuthorDate() {
        return mAuthorDate;
    }

    public String getCommitterName() {
        return mCommitterName;
    }

    public String getCommitterDate() {
        return mCommitterDate;
    }

    public String getSubject() {
        return mSubject;
    }

    public String getBody() {
        return mBody;
    }

    public String getCommitterGravatar() {
        return mCommitterGravatar;
    }

    public String getAuthorGravatar() {
        return mAuthorGravatar;
    }

    @Override
    public void parseObject(JSONObject jsonObject) {
        // stop looking at me swan!!!
    }
}
