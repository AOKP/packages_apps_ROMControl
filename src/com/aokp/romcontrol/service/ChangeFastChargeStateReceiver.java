
package com.aokp.romcontrol.service;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ChangeFastChargeStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String fCHargePath = context
                .getString(com.android.internal.R.string.config_fastChargePath);
        if (fCHargePath == null || fCHargePath.isEmpty() || !new File(fCHargePath).exists()) {
            Log.e("ROMControl", "Attempted to change fast charge state but it's not enabled?");
            return;
        }
        final String value = intent.getBooleanExtra("newState", false) ? "1" : "0";
        context.startService(new Intent(context,
                ExternalCommandService.class)
                .putExtra("cmd", "echo " + value + " > " + fCHargePath));
    }
}
