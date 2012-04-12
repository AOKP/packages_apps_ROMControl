
package com.aokp.romcontrol.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.aokp.romcontrol.util.CMDProcessor;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Intent i = new Intent(context, BootService.class);
        context.startService(i);

        if (Build.BOARD.equals("tuna")) {
            Log.i("ROMControl", "tuna board detected, executing signal fix");
            new CMDProcessor().su.run("echo 0 > /sys/kernel/debug/smartreflex/sr_core/autocomp");
        }
    }
}
