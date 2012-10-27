package com.aokp.romcontrol.performance;

import android.util.Log;
import java.io.File;

public class VoltageControlTables {

private static final String TAG = "VoltageControlTables";
private static File file;
private static int i;
public static String TABLE;

// TODO : Make an exhaustive array of possible locations where the vdd array may exist.
    public static String POSSIBLE_LOCATIONS[] = { "UV_mV_table",
                                                  "vdd_levels",
                                                  "vdd_sysfs_levels"};
    public static String VOLTAGE_TABLE() {
        for (i = 0; i < 3; i = i + 1 ) {
        file = new File("/sys/devices/system/cpu/cpu0/cpufreq/" + POSSIBLE_LOCATIONS[i] );
            if (file.exists()) {
            Log.d(TAG, "/sys/devices/system/cpu/cpu0/cpufreq/" + POSSIBLE_LOCATIONS[i] + " is found and being used");
            TABLE = POSSIBLE_LOCATIONS[i];
            } else {
            Log.d(TAG, "No voltage Table found. Voltage control possibly not implemented in kernel");
            TABLE = "no_voltage_table_found";
            }
        }
    return TABLE;
    }

    public static String TABLE(int tableNumber) {
        return "/sys/devices/system/cpu/cpu" + tableNumber + "/cpufreq/" + VOLTAGE_TABLE();
    }
}
