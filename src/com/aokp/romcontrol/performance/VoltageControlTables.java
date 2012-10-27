package com.aokp.romcontrol.performance;

import android.content.Context;
import com.aokp.romcontrol.R;
import android.util.Log;
import java.io.File;

public class VoltageControlTables {

private static final String TAG = "VoltageControlTables";

public static String TABLE0;
public static String TABLE1;
public static String TABLE2;
public static String TABLE3;
// TODO : Make an array of possible locations where the vdd array may exist. example below
//public static String POSSIBLE_LOCATIONS[] {
//    "UV_mV_table",
//    "vdd_levels",
//    "vdd_sysfs_levels"
//    }

public static String VOLTAGE_TABLE() {
    File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");
    if(file.exists()) {
            return "UV_mV_table";
    } else {
        file = new File("/sys/devices/system/cpu/cpu0/cpufreq/vdd_levels");
        if(file.exists()) {
                return "vdd_levels";
        } else {
                return "no_voltage_array_found";
        }
    }
}

public static String TABLE0() {
    TABLE0 = "/sys/devices/system/cpu/cpu0/cpufreq/" + VOLTAGE_TABLE();
    Log.d(TAG, TABLE0 + " is being used");
    return TABLE0;
    }
public static String TABLE1() {
    TABLE1 = "/sys/devices/system/cpu/cpu1/cpufreq/" + VOLTAGE_TABLE();
    Log.d(TAG, TABLE1 + " is being used");
    return TABLE1;
    }
public static String TABLE2() {
    TABLE2 = "/sys/devices/system/cpu/cpu2/cpufreq/" + VOLTAGE_TABLE();
    Log.d(TAG, TABLE2 + " is being used");
    return TABLE2;
    }
public static String TABLE3() {
    TABLE3 = "/sys/devices/system/cpu/cpu3/cpufreq/" + VOLTAGE_TABLE();
    Log.d(TAG, TABLE3 + " is being used");
    return TABLE3;
    }
    
}

