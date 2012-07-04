package com.aokp.romcontrol.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.aokp.romcontrol.util.CMDProcessor.CommandResult;

public class Helpers {

    private static final String TAG = "Helpers";

    /**
     * Checks device for SuperUser permission
     * 
     * @return If SU was granted or denied
     */
    public static boolean checkSu() {
        if (!new File("/system/bin/su").exists()
                && !new File("/system/xbin/su").exists()) {
            Log.e(TAG, "su does not exist!!!");
            return false; // tell caller to bail...
        }

        try {
            if ((new CMDProcessor().su
                    .runWaitFor("ls /data/app-private")).success()) {
                Log.i(TAG, " SU exists and we have permission");
                return true;
            } else {
                Log.i(TAG, " SU exists but we dont have permission");
                return false;
            }
        } catch (final NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage().toString());
            return false;
        }
    }

    /**
     * Checks device for network connectivity
     *
     * @return If the device has data connectivity
    */
    public static boolean isNetworkAvailable(final Context c) {
        boolean state = false;
        if (c != null) {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if(netInfo != null && netInfo.isConnected()) {
                Log.i(TAG, "The device currently has data connectivity");
                state = true;
            } else {
                Log.i(TAG, "The device does not currently have data connectivity");
                state = false;
            }
        }
        return state;
    }

    /**
     * Checks to see if Busybox is installed in "/system/"
     * 
     * @return If busybox exists
     */
    public static boolean checkBusybox() {
        if (!new File("/system/bin/busybox").exists()
                && !new File("/system/xbin/busybox").exists()) {
            Log.e(TAG, "Busybox not in xbin or bin!");
            return false;
        }

        try {
            if (!new CMDProcessor().su.runWaitFor("busybox mount").success()) {
                Log.e(TAG, " Busybox is there but it is borked! ");
                return false;
            }
        } catch (final NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage().toString());
            return false;
        }
        return true;
    }

    public static String[] getMounts(final String path)
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("/proc/mounts"), 256);
            String line = null;
            while ((line = br.readLine()) != null)
            {
                if (line.contains(path))
                {
                    return line.split(" ");
                }
            }
            br.close();
        } 
        catch (FileNotFoundException e) {
            Log.d(TAG, "/proc/mounts does not exist");
        } 
        catch (IOException e) {
            Log.d(TAG, "Error reading /proc/mounts");
        }
        return null;
    }
    
    public static boolean getMount(final String mount)
    {
        final CMDProcessor cmd = new CMDProcessor();
        final String mounts[] = getMounts("/system");
        if (mounts != null
                && mounts.length >= 3)
        {
            final String device = mounts[0];
            final String path = mounts[1];
            final String point = mounts[2];
            if (cmd.su.runWaitFor("mount -o " + mount + ",remount -t " + point + " " + device + " " + path).success())
            {
                return true;
            }
        }
        return ( cmd.su.runWaitFor("busybox mount -o remount," + mount + " /system").success() );
    }
    
    public static String getFile(final String filename) {
        String s = "";
        final File f = new File(filename);

        if (f.exists() && f.canRead()) {
            try {
                final BufferedReader br = new BufferedReader(new FileReader(f),
                        256);
                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    s += buffer + "\n";
                }

                br.close();
            } catch (final Exception e) {
                Log.e(TAG, "Error reading file: " + filename, e);
                s = null;
            }
        }
        return s;
    }
    
    public static void writeNewFile(String filePath, String fileContents) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
        }

        try{
            // Create file 
            FileWriter fstream = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(fileContents);
            //Close the output stream
            out.close();
        }catch (Exception e){
            Log.d( TAG, "Failed to create " + filePath + " File contents: " + fileContents);  
        }
    }
    
    /**
     * Long toast message
     * 
     * @param c Application Context
     * @param msg Message to send
     */
    public static void msgLong(final Context c, final String msg) {
        if (c != null && msg != null) {
            Toast.makeText(c, msg.trim(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Short toast message
     * 
     * @param c Application Context
     * @param msg Message to send
     */
    public static void msgShort(final Context c, final String msg) {
        if (c != null && msg != null) {
            Toast.makeText(c, msg.trim(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Long toast message
     * 
     * @param c Application Context
     * @param msg Message to send
     */
    public static void sendMsg(final Context c, final String msg) {
        if (c != null && msg != null) {
            msgLong(c, msg);
        }
    }

    /**
     * Return a timestamp
     * 
     * @param c Application Context
     */
    public static String getTimestamp(final Context context) {
        String timestamp;
        timestamp = "unknown";
        Date now = new Date();
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        if(timeFormat != null) {
            timestamp = timeFormat.format(now);
        }
        return timestamp;
    }
    
    public static boolean isPackageInstalled(final String packageName,
            final PackageManager pm)
    {
        String mVersion;
        try {
            mVersion = pm.getPackageInfo(packageName, 0).versionName;           
            if (mVersion.equals(null)) {
                return false;
            }
        } catch (NameNotFoundException e) {
            return false;
        }       
        return true;
    }

    public static void restartSystemUI() {
        new CMDProcessor().su.run("pkill -TERM -f com.android.systemui");
    }

    public static void setSystemProp(String prop, String val) {
        new CMDProcessor().su.run("setprop " + prop + " " + val);
    }

    public static String getSystemProp(String prop, String def) {
        String result = getSystemProp(prop);
        return result == null ? def : result;
    }

    private static String getSystemProp(String prop) {
        CommandResult cr = new CMDProcessor().sh.runWaitFor("getprop " + prop);
        if (cr.success()) {
            return cr.stdout;
        } else {
            return null;
        }
    }
}
