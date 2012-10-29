
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
import com.aokp.romcontrol.R;

public class Config {
    /**
     * used by the applet to track variables
     */
    static class StaticVars {
        public static String ORGANIZATION;
        public static boolean REMOVE_AUTHOR_LAYOUT;
        public static boolean REMOVE_COMMITTER_LAYOUT;

        // fling speed of scroll
        public static final int DEFAULT_FLING_SPEED = 60;

        // Dialogs (1001+)
        public static final int COMMIT_INFO_DIALOG = 1001;

        public static final boolean JSON_SPEW = false;
    }

    public Config() {
        // thats it
    }

    public void setORGANIZATION(Context context) {
        StaticVars.ORGANIZATION = context.getString(R.string.github_organization) + "/";
    }

    // example of commit list from parser
    // https://api.github.com/repos/aokp/frameworks_base/commits?page=1
    // example of repo list from parser
    // https://api.github.com/orgs/aokp/repos?page=1&per_page=100

    // github json api addresses
    public final String GITHUB_JSON = "https://api.github.com/";
    public final String ORGANIZATION = StaticVars.ORGANIZATION;
    public final String REPO_URL = GITHUB_JSON + "orgs/" + ORGANIZATION + "repos";
    public final String COMMITS_PAGE = "commits?page="; //later... + PAGE_NUMBER (30 returns by default)
    public final String COMMITS_REQUEST_FORMAT = GITHUB_JSON
        + "repos/" + ORGANIZATION + "%s/" + COMMITS_PAGE + "%s";
    public final String CHANGELOG_JSON = "https://raw.github.com/JBirdVegas/tests/master/example.json";
}
