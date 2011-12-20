
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;

import com.roman.romcontrol.fragment.StatusBarBatteryPreferences;

public class StatusBarBattery extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new StatusBarBatteryPreferences()).commit();
    }

}
