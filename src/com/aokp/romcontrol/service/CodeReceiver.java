
package com.aokp.romcontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.File;

public class CodeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean swaggerInitiated = new File("/data/local/bootanimation.user").exists() || getSwagInitiatedPref(context);
        if (swaggerInitiated) {
            if (new File("/data/local/bootanimation.user").exists()) {
                context.startService(new Intent(context,
                        ExternalCommandService.class)
                        .putExtra("cmd",
                                "mv /data/local/bootanimation.user /data/local/bootanimation.zip"));
            } else {
                context.startService(new Intent(context,
                        ExternalCommandService.class)
                        .putExtra("cmd",
                                "rm /data/local/bootanimation.zip"));
            }
            setSwagInitiatedPref(context, false);
            Toast.makeText(context, ":(", Toast.LENGTH_SHORT).show();
        } else {
            if (new File("/data/local/bootanimation.zip").exists()) {
                context.startService(new Intent(context,
                        ExternalCommandService.class)
                        .putExtra("cmd",
                                "mv /data/local/bootanimation.zip /data/local/bootanimation.user"));
            }
            context.startService(new Intent(context,
                    ExternalCommandService.class)
                    .putExtra("cmd",
                            "cp /system/media/bootanimation-alt.zip /data/local/bootanimation.zip"));
            setSwagInitiatedPref(context, true);
            Toast.makeText(context, ":)", Toast.LENGTH_SHORT).show();
        }
        context.startService(new Intent(context,
                ExternalCommandService.class)
                .putExtra("cmd",
                        "chmod 644 /data/local/bootanimation.zip"));
    }

    private static boolean getSwagInitiatedPref(Context c) {
        return c.getSharedPreferences("bootanimation", Context.MODE_PRIVATE).getBoolean("alt-animation", false);
    }

    public static void setSwagInitiatedPref(Context c, boolean value) {
        c.getSharedPreferences("bootanimation", Context.MODE_PRIVATE).edit().putBoolean("alt-animation", value).commit();
    }
}
