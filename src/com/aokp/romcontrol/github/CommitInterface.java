
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

import org.json.JSONObject;

/**
 * Object representing a single commit
 *
 * interface to transparently handle different json formatted
 * commit information
 */
interface CommitInterface {
    /**
     * interface handles both our changelog and github formated json commits
     * the handling is almost invisible to the developer simply use the
     * respective implantation
     * @param jsonObject
     */
    void parseObject(JSONObject jsonObject);
}
