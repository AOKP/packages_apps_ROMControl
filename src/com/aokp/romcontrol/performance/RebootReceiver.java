
package com.aokp.romcontrol.performance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {

    public static final String ACTION_REBOOT = "com.aokp.romcontrol.ACTION_DAILY_REBOOT";
    public static final String ACTION_WARN = "com.aokp.romcontrol.ACTION_DAILY_REBOOT_WARNING";

    static long lastScreenOffAt = System.currentTimeMillis();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (ACTION_REBOOT.equals(action)) {
            Log.e("RC", "ROMControl daily reboot starting!");
            if (System.currentTimeMillis() - lastScreenOffAt > (1000 * 60 * 60)) {
                // only reboot if screen has been off for at last an hour
                PowerManager pm = (PowerManager) context
                        .getSystemService(Context.POWER_SERVICE);
                pm.reboot(null);
            } else {
                Intent reschedule = new Intent(context, DailyRebootService.class);
                reschedule.putExtra("reschedule", true);
                context.startService(reschedule);
            }
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            lastScreenOffAt = System.currentTimeMillis();
        }
    }
}
