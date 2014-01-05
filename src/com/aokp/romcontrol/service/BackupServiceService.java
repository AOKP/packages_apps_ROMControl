package com.aokp.romcontrol.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by jbird on 12/30/13.
 */
public class BackupServiceService extends IntentService {
    public BackupServiceService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
