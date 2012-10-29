
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

public class Config {
    /**
     * used by the applet to track variables
     */
    public static class StaticVars {
        public static boolean REMOVE_AUTHOR_LAYOUT;
        public static boolean REMOVE_COMMITTER_LAYOUT;

        // fling speed of scroll
        public static final int DEFAULT_FLING_SPEED = 75;
        public static final boolean JSON_SPEW = false;
    }

    // example of commit list from parser
    // https://api.github.com/repos/hostalerye/TODO_List/commits?per_page=100&sha=cdc04cbd4b61d4fa7992e9c04a523ee698fe6d86
    // example of repo list from parser
    // https://api.github.com/orgs/aokp/repos?page=1&per_page=100

    // github json api addresses
    public final String GITHUB_JSON = "https://api.github.com/";
    public static final String ORGANIZATION = "AOKP/";
    public final String REPO_URL = GITHUB_JSON + "orgs/" + ORGANIZATION + "repos";
    public final String COMMITS_PAGE = "commits?per_page=100";
    public final String INITIAL_COMMITS_REQUEST_FORMAT = GITHUB_JSON
        + "repos/" + ORGANIZATION + "%s/" + COMMITS_PAGE;
    public final String COMMITS_REQUEST_FORMAT = INITIAL_COMMITS_REQUEST_FORMAT + "&sha=%s";
    public final String CHANGELOG_JSON = "https://raw.github.com/JBirdVegas/tests/master/example.json";
}
