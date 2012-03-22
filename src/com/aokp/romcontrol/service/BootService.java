
package com.aokp.romcontrol.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.tools.Voltage;
import com.aokp.romcontrol.tools.VoltageControl;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.KernelUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BootService extends Service {

    static final String TAG = "Liberty Settings Service";
    private static final String CUR_GOV = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    private static final String MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    private static final String MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    private static final String KEY_FASTCHARGE = "fast_charge_boot";
    private static final String FAST_CHARGE_DIR = "/sys/kernel/fast_charge";
	private static final String FAST_CHARGE_FILE = "force_fast_charge";
    private final BootService service = this;
    public static SharedPreferences preferences;
    private Thread bootThread;
    
    private static final String[] colorFILE_PATH = new String[] {
        "/sys/class/misc/samoled_color/red_multiplier",
        "/sys/class/misc/samoled_color/green_multiplier",
        "/sys/class/misc/samoled_color/blue_multiplier"
    };
    // Align MAX_VALUE with Voodoo Control settings
    private static final int colorMAX_VALUE = Integer.MAX_VALUE - 2;
    
    private static final String[] gammaFILE_PATH = new String[] {
        "/sys/class/misc/samoled_color/red_v1_offset",
        "/sys/class/misc/samoled_color/green_v1_offset",
        "/sys/class/misc/samoled_color/blue_v1_offset"
    };
    private static final int gammaMAX_VALUE = 80;
    
    public void onStart(Intent intent, int startId) {
        preferences = PreferenceManager.getDefaultSharedPreferences(service);
        super.onStart(intent, startId);
        Log.i(TAG, "Starting set-on-boot");
        bootThread = new Thread() {
            @Override
            public void run() {
                final CMDProcessor cmd = new CMDProcessor();
                if (preferences.getBoolean("cpu_boot", false)) {
                    final String max = preferences.getString("max_cpu", null);
                    final String min = preferences.getString("min_cpu", null);
                    final String gov = preferences.getString("gov", null);
                    if (max != null && min != null && gov != null) {
                        cmd.su.runWaitFor("busybox echo " + max + " > " + MAX_FREQ);
                        cmd.su.runWaitFor("busybox echo " + min + " > " + MIN_FREQ);
                        cmd.su.runWaitFor("busybox echo " + gov + " > " + CUR_GOV);
                        if (new File("/sys/devices/system/cpu/cpu1").exists()) {
                            cmd.su.runWaitFor("busybox echo " + max + " > "
                                    + MAX_FREQ.replace("cpu0", "cpu1"));
                            cmd.su.runWaitFor("busybox echo " + min + " > "
                                    + MIN_FREQ.replace("cpu0", "cpu1"));
                            cmd.su.runWaitFor("busybox echo " + gov + " > "
                                    + CUR_GOV.replace("cpu0", "cpu1"));
                        }
                    }
                }
                if (preferences.getBoolean("free_memory_boot", false)) {
                    final String values = preferences.getString("free_memory", null);
                    if (!values.equals(null)) {
                        cmd.su.runWaitFor("busybox echo " + values
                                + " > /sys/module/lowmemorykiller/parameters/minfree");
                    }
                }
                if (preferences.getBoolean(VoltageControl.KEY_APPLY_BOOT, false)) {
                    final List<Voltage> volts = VoltageControl.getVolts(preferences);
                    final StringBuilder sb = new StringBuilder();
                    String logInfo = "Setting Volts: ";
                    for (final Voltage volt : volts) {
                        sb.append(volt.getSavedMV() + " ");
                        logInfo += volt.getFreq() + "=" + volt.getSavedMV() + " ";
                    }
                    Log.i(TAG, logInfo);
                    new CMDProcessor().su.runWaitFor("busybox echo " + sb.toString() + " > "
                            + VoltageControl.MV_TABLE0);
                    if (new File(VoltageControl.MV_TABLE1).exists()) {
                        new CMDProcessor().su.runWaitFor("busybox echo " + sb.toString() + " > "
                                + VoltageControl.MV_TABLE1);
                    }
                }
            }
        };
        
        //  Let's set fast_charge from preference
        boolean FChargeOn = preferences.getBoolean(KEY_FASTCHARGE, false); 
        Log.d("FChargeBoot","Setting at Boot:" + FChargeOn);
        try{
    		File fastcharge = new File(FAST_CHARGE_DIR,FAST_CHARGE_FILE);
    		FileWriter fwriter = new FileWriter(fastcharge);
    		BufferedWriter bwriter = new BufferedWriter(fwriter);
    		bwriter.write(FChargeOn ? "1" : "0");
    		bwriter.close();
    		Intent i = new Intent();
    		i.setAction("com.roman.romcontrol.FCHARGE_CHANGED");
    		getApplicationContext().sendBroadcast(i);
    	} catch (IOException e) {
    		Log.e("FChargeBoot","Couldn't write fast_charge file");
    	}	
        
        // Let's restore color & gamma settings
        restoreColor();
        restoreGamma();
        
        if (Settings.System.getInt(getContentResolver(), Settings.System.USE_WEATHER, 0) != 0) {
            Intent startRefresh = new Intent(getApplicationContext(),
                    WeatherRefreshService.class);
            getApplicationContext().startService(startRefresh);
            
            Intent getWeatherNow = new Intent(getApplicationContext(), WeatherService.class);
            getWeatherNow.setAction(WeatherService.INTENT_REQUEST_WEATHER);
            getApplicationContext().startService(getWeatherNow);
        }
        
        bootThread.start();
        // Stop the service
        stopSelf();
    }
    
    public static void restoreColor() {
        int iValue, iValue2;
        if (!isSupported(colorFILE_PATH)) {
            return;
        }

        for (String filePath : colorFILE_PATH) {
            String sDefaultValue = KernelUtils.readOneLine(filePath);
            Log.d(TAG,"INIT: " + sDefaultValue);
            try {
                iValue2 = Integer.parseInt(sDefaultValue);
            } catch (NumberFormatException e) {
                iValue2 = colorMAX_VALUE;
            }
            try {
                iValue = preferences.getInt(filePath, iValue2);
                Log.d(TAG, "restore: iValue: " + iValue + " File: " + filePath);
            } catch (NumberFormatException e) {
                iValue = iValue2;
                Log.e(TAG, "restore ERROR: iValue: " + iValue + " File: " + filePath);
            }
            KernelUtils.writeColor(filePath, (int) iValue);
        }
    }
    
    public static void restoreGamma() {
        if (!isSupported(gammaFILE_PATH)) {
            return;
        }
        for (String filePath : gammaFILE_PATH) {
            String sDefaultValue = KernelUtils.readOneLine(filePath);
            int iValue = preferences.getInt(filePath, Integer.valueOf(sDefaultValue));
            KernelUtils.writeValue(filePath, String.valueOf((long) iValue));
        }
    }

    /**
     * Check whether the running kernel supports color/gamma tuning or not.
     * 
     * @return Whether color/gamma tuning is supported or not
     */
    public static boolean isSupported(String[] filecheck) {
        boolean supported = true;
        for (String filePath : filecheck) {
            if (!KernelUtils.fileExists(filePath)) {
                supported = false;
            }
        }

        return supported;
    }


    @Override
    public IBinder onBind(final Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
