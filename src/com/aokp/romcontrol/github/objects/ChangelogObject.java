
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

import org.json.JSONObject;

/**
 * implemtation of CommitObject that handles parsing of our changelog
 *
 * this implemtation of CommitObject handles fewer values, noteably excluding
 * author/committer gravatar urls needed for CommitViewerDialog
 */
public class ChangelogObject extends CommitObject {
    // changelog constants
    private String TEAM_CREDIT = "team_credit";
    private String PATH = "path";
    private String COMMIT_HASH = "commit_hash";
    private String PARENT_HASHES = "parent_hashes";
    private String AUTHOR_NAME = "author_name";
    private String AUTHOR_DATE = "author_date";
    private String COMMITTER_NAME = "committer_name";
    private String COMMITTER_DATE = "committer_date";
    private String SUBJECT = "subject";
    private String BODY = "body";

    /**
     * Creates an object representing a commit from our formatted changelog
     * @param jsonObject commit in json format
     */
    public ChangelogObject(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * set all available fields with parsed json info
     * @param jsonObject json formatted object to parse
     */
    @Override
    public void parseObject(JSONObject jsonObject) {
        super.parseObject(jsonObject);
        mJsonObject = jsonObject;
        mTeamCredit = parseValue(TEAM_CREDIT);
        mPath = parseValue(PATH);
        mCommitHash = parseValue(COMMIT_HASH);
        mUrl = mConfig.GITHUB_JSON + "repos/" + mPath + "/commits/" + mCommitHash;
        mParentHashes = parseValue(PARENT_HASHES);
        mAuthorName = parseValue(AUTHOR_NAME);
        mAuthorDate = parseValue(AUTHOR_DATE);
        mCommitterName = parseValue(COMMITTER_NAME);
        mCommitterDate = parseValue(COMMITTER_DATE);
        mSubject = parseValue(SUBJECT);
        mBody = parseValue(BODY);
    }
}
