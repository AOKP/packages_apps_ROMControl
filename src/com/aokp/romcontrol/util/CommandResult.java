package com.aokp.romcontrol.util;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("AccessOfSystemProperties")
public class CommandResult implements Parcelable {
    private Executable mExecutableScript;
    private final long mStartTime;
    public Integer exit_value;
    public String stdout;
    public String stderr;
    private long mEndTime;
    private long mExecutionTime;
    private String TAG = getClass().getSimpleName();

    public CommandResult(Executable executableScript,
                         long startTime,
                         Integer exit_value_in,
                         String stdout_in,
                         String stderr_in,
                         long endTime) {
        this.mExecutableScript = executableScript;
        this.mStartTime = startTime;
        setExit_value(exit_value_in);
        setStdout(stdout_in);
        setStderr(stderr_in);
        setEndTime(endTime);
    }

    // pretty much just forward the constructor from parcelable to our main
    // loading constructor
    @SuppressWarnings("CastToConcreteClass")
    public CommandResult(Parcel inParcel) {
        this((Executable) inParcel.readParcelable(Executable.class.getClassLoader()),
                inParcel.readLong(),
                inParcel.readInt(),
                inParcel.readString(),
                inParcel.readString(),
                inParcel.readLong());
    }

    public boolean success() {
        return exit_value != null && exit_value == 0;
    }

    public long getEndTime() {
        return new Long(mEndTime);
    }

    public String getStderr() {
        return new String(stderr);
    }

    public String getStdout() {
        return new String(stdout);
    }

    public Integer getExit_value() {
        return new Integer(exit_value);
    }

    public long getStartTime() {
        return new Long(mStartTime);
    }

    // setters
    public CommandResult setExit_value(Integer exit_value) {
        this.exit_value = exit_value;
        return this;
    }

    public CommandResult setStdout(String stdout) {
        this.stdout = stdout;
        return this;
    }

    public CommandResult setStderr(String stderr) {
        this.stderr = stderr;
        return this;
    }

    public CommandResult setEndTime(long endTime) {
        this.mEndTime = endTime;
        this.mExecutionTime = mEndTime - mStartTime;
        Log.d(TAG, "Time to execute: " + this.mExecutionTime + " ns (nanoseconds)");
        // this is set last so log from here
        checkForErrors();
        return this;
    }

    @SuppressWarnings("UnnecessaryExplicitNumericCast")
    private void checkForErrors() {
        if (getExit_value() != 0
                || !"".equals(getStderr().trim())) {
            // don't log the commands that failed
            // because the cpu was offline
            String errorPipe = getStderr();
            boolean skipOfflineCpu =
                    // if core is off locking fails
                    errorPipe.contains("chmod: /sys/devices/system/cpu/cpu")
                            // if core is off applying cpu freqs fails
                            || errorPipe.contains(": can't create /sys/devices/system/cpu/cpu");
            String lineEnding = System.getProperty("line.separator");
            FileWriter errorWriter = null;
            try {
                File errorLogFile = new File(
                        Environment.getExternalStorageDirectory()
                        + "/aokp/error.txt");
                if (!errorLogFile.exists()) {
                    errorLogFile.createNewFile();
                }
                errorWriter = new FileWriter(errorLogFile, true);
                // only log the cpu state as offline while writing
                if (skipOfflineCpu) {
                    errorWriter.write(lineEnding);
                    errorWriter.write("Attempted to write to an offline cpu core (ignore me).");
                } else {
                    errorWriter.write(TAG + " shell error detected!");
                    errorWriter.write(lineEnding);
                    errorWriter.write("CommandResult {" + this.toString() + '}');
                    errorWriter.write(lineEnding);
                }
                errorWriter.write(lineEnding);
            } catch (IOException e) {
                Log.e(TAG, "Failed to write command result to error file", e);
            } finally {
                if (errorWriter != null) {
                    try {
                        errorWriter.close();
                    } catch (IOException ignored) {
                        // let it go
                    }
                }
            }
        }
    }

    // implement parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mExecutableScript, 0);
        parcel.writeLong(mStartTime);
        parcel.writeInt(exit_value);
        parcel.writeString(stdout);
        parcel.writeString(stderr);
        parcel.writeLong(mEndTime);
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "mExecutableScript={" + mExecutableScript + '}' +
                ", mStartTime=" + mStartTime +
                ", exit_value=" + exit_value +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                ", mEndTime=" + mEndTime +
                ", mExecutionTime=" + mExecutionTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandResult)) return false;

        CommandResult that = (CommandResult) o;

        if (mEndTime != that.mEndTime) return false;
        if (mExecutionTime != that.mExecutionTime) return false;
        if (mStartTime != that.mStartTime) return false;
        if (!TAG.equals(that.TAG)) return false;
        if (!exit_value.equals(that.exit_value)) return false;
        if (!mExecutableScript.equals(that.mExecutableScript)) return false;
        if (stderr != null ? !stderr.equals(that.stderr) : that.stderr != null) return false;
        if (stdout != null ? !stdout.equals(that.stdout) : that.stdout != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mExecutableScript.hashCode();
        result = 31 * result + (int) (mStartTime ^ (mStartTime >>> 32));
        result = 31 * result + exit_value.hashCode();
        result = 31 * result + (stdout != null ? stdout.hashCode() : 0);
        result = 31 * result + (stderr != null ? stderr.hashCode() : 0);
        result = 31 * result + (int) (mEndTime ^ (mEndTime >>> 32));
        result = 31 * result + (int) (mExecutionTime ^ (mExecutionTime >>> 32));
        result = 31 * result + TAG.hashCode();
        return result;
    }
}