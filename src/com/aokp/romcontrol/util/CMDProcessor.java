/*
 * Copyright (C) 2017 The AOKP Project
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

package com.aokp.romcontrol.util;

import android.util.Log;

// convenience import for quick referencing of this method

public final class CMDProcessor {
    private static final String TAG = "CMDProcessor";

    private CMDProcessor() {
        // Cannot instantiate this class
        throw new AssertionError();
    }

    /* Run a system command with full redirection */
    public static ChildProcess startSysCmd(String[] cmdarray, String childStdin) {
        return new ChildProcess(cmdarray, childStdin);
    }

    public static CommandResult runSysCmd(String[] cmdarray, String childStdin) {
        ChildProcess proc = startSysCmd(cmdarray, childStdin);
        proc.waitFinished();
        return proc.getResult();
    }

    public static ChildProcess startShellCommand(String cmd) {
        String[] cmdarray = new String[3];
        cmdarray[0] = "sh";
        cmdarray[1] = "-c";
        cmdarray[2] = cmd;
        return startSysCmd(cmdarray, null);
    }

    public static CommandResult runShellCommand(String cmd) {
        ChildProcess proc = startShellCommand(cmd);
        proc.waitFinished();
        return proc.getResult();
    }

    public static ChildProcess startSuCommand(String cmd) {
        String[] cmdarray = new String[3];
        cmdarray[0] = "su";
        cmdarray[1] = "-c";
        cmdarray[2] = cmd;
        return startSysCmd(cmdarray, null);
    }

    public static CommandResult runSuCommand(String cmd) {
        ChildProcess proc = startSuCommand(cmd);
        proc.waitFinished();
        return proc.getResult();
    }

    public static boolean canSU() {
        CommandResult r = runShellCommand("id");
        StringBuilder out = new StringBuilder(0);
        out.append(r.getStdout());
        out.append(" ; ");
        out.append(r.getStderr());
        Log.d(TAG, "canSU() su[" + r.getExitValue() + "]: " + out);
        return r.success();
    }
}
