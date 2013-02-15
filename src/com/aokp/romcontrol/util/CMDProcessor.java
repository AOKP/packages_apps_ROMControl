package com.aokp.romcontrol.util;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.nanoTime;

// convenience import for quick referencing of this method

public class CMDProcessor {

    private boolean mDebug = false;
    private Boolean can_su;
    public SH sh;
    public SH su;
    private final String TAG;

    public CMDProcessor() {
        sh = new SH("sh");
        su = new SH("su");
        this.TAG = getClass().getSimpleName();
    }

    public SH suOrSH() {
        return canSU() ? su : sh;
    }

    public CMDProcessor setLogcatDebugging(boolean debug) {
        mDebug = debug;
        return this;
    }

    public boolean canSU() {
        return canSU(false);
    }

    public class SH {
        private String mShell = "sh";

        public SH(String SHELL_in) {
            mShell = SHELL_in;
        }

        @SuppressWarnings("deprecation")
        private String getStreamLines(InputStream is) {
            StringBuffer buffer = null;
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(is);
                if (dis.available() > 0) {
                    buffer = new StringBuffer(dis.readLine());
                    while (dis.available() > 0) {
                        buffer.append('\n')
                                .append(dis.readLine());
                    }
                }
                dis.close();
            } catch (IOException e) {
                Log.e(TAG, "Caught thrown exception e", e);
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException ignored) {
                        // let it go
                    }
                }
            }
            return buffer != null ? buffer.toString() : "";
        }

        /**
         * run a single command; this is depreciated
         * because it now just forms a new Executable object
         * and calls runWaitFor(Executable)
         *
         * @param command single shell command
         * @return result of command as CommandResult object
         */
        @Deprecated
        public CommandResult runWaitFor(String command) {
            return runWaitFor(new Executable(command));
        }

        /**
         * Convenience method to allow execution
         * of shell commands on a worker thread,
         * without blocking the UI thread.
         *
         * @param command shell command to be executed
         *                off the main thread.
         */
        @Deprecated
        public void fireAndForget(String command) {
            fireAndForget(new Executable(command));
        }

        /**
         * run a command or commands from an Executable object
         * on a worker thread allowing the main thread to move on
         *
         * @param executable
         */
        public void fireAndForget(final Executable executable) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runWaitFor(executable);
                }
            }).start();
        }

        // Handle Lists sent to us
        @SuppressWarnings({"CallToRuntimeExec", "CallToRuntimeExecWithNonConstantString"})
        private Process run(Executable script) {
            Process process = null;
            DataOutputStream toProcess = null;
            String currentCommand = null;
            try {
                // open shell here
                process = Runtime.getRuntime()
                        .exec(mShell);
                toProcess = new DataOutputStream(
                        process.getOutputStream());
                for (String command : script.getCommandsArray()) {
                    currentCommand = command + '\n';
                    if (mDebug) {
                        Log.d(TAG, "executing { '" + currentCommand + "' }");
                    }
                    toProcess.writeBytes(currentCommand);
                }
            } catch (IOException e) {
                Log.e(TAG,
                        "Exception while trying to run: '"
                                + currentCommand + "' ", e);
                process = null;
            } finally {
                if (toProcess != null) {
                    try {
                        // pretty sure flush() is redundant as close()
                        // I think will flush() for us
                        toProcess.flush();
                        toProcess.close();
                    } catch (IOException ignored) {
                        // let it go
                    }
                }
            }
            return process;
        }

        public CommandResult runWaitFor(Executable script) {
            long startTime = nanoTime();
            Process process = run(script);
            // notify the script we finished
            script.setFinishTime(nanoTime());
            Integer exit_value = null;
            String stdout = null;
            String stderr = null;
            if (process != null) {
                try {
                    exit_value = process.waitFor();
                    stdout = getStreamLines(process.getInputStream());
                    stderr = getStreamLines(process.getErrorStream());
                } catch (InterruptedException e) {
                    Log.e(TAG, "process was interrupted!", e);
                } catch (NullPointerException e) {
                    Log.e(TAG, "NullPointer while getting streams", e);
                } finally {
                    process.destroy();
                }
            }
            return new CommandResult(
                    script, // executed shell code
                    script.getStartTime(), // begin execution
                    exit_value, // success?
                    stdout, // terminal output /not failure related/
                    stderr, // failure output
                    script.getFinishTime()); // time after execution
        }
    }

    public boolean canSU(boolean force_check) {
        if (can_su == null || force_check) {
            CommandResult r = su.runWaitFor("id");
            StringBuilder out = new StringBuilder(0);
            if (r.stdout != null) {
                out.append(r.stdout).append(" ; ");
            }
            if (r.stderr != null) {
                out.append(r.stderr);
            }
            Log.d(TAG, "canSU() su[" + r.exit_value + "]: " + out);
            can_su = r.success();
        }
        return can_su;
    }
}
