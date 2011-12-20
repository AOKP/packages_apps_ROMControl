
package com.roman.romcontrol.activities;

import android.app.Activity;
import android.os.Bundle;

import com.roman.romcontrol.fragment.LEDPreferences;

public class LED extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new LEDPreferences()).commit();
    }

}
