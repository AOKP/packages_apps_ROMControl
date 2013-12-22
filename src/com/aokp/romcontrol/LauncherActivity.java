package com.aokp.romcontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent launchRC = new Intent(this, MainActivity.class);
        startActivity(launchRC);
        finish();
    }
}
