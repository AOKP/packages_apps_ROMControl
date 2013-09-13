package com.aokp.romcontrol.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;

import com.aokp.romcontrol.R;

public class ChangeFastChargeStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String fCHargePath = context
                .getString(com.android.internal.R.string.config_fastChargePath);
        if (fCHargePath == null || fCHargePath.isEmpty() || !new File(fCHargePath).exists()) {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

            Notification noti = new Notification.BigTextStyle(
                    new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.fast_charge_not_supported_title))
                    .setTicker(context.getString(R.string.fast_charge_not_supported_title))
                    .setSmallIcon(R.drawable.ic_stat_notify_rom_control)
                    .setLargeIcon(bm))
                    .bigText(context.getString(R.string.fast_charge_not_supported))
                    .build();

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(2, noti);

            Log.e("ROMControl", "Attempted to change fast charge state but it's not enabled?");
            return;
        }
        final String value = intent.getBooleanExtra("newState", false) ? "1" : "0";
        context.startService(new Intent(context,
                ExternalCommandService.class)
                .putExtra("cmd", "echo " + value + " > " + fCHargePath));
    }
}
