
package com.aokp.romcontrol.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.aokp.romcontrol.fragments.ColorTuningPreference;
import com.aokp.romcontrol.fragments.GammaTuningPreference;
import com.aokp.romcontrol.R;
import com.aokp.romcontrol.tools.Voltage;
import com.aokp.romcontrol.tools.VoltageControl;
import com.aokp.romcontrol.util.CMDProcessor;
import com.aokp.romcontrol.util.KernelUtils;

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

        // Let's set fast_charge from preference
        boolean FChargeOn = preferences.getBoolean(KEY_FASTCHARGE, false);
        Log.d("FChargeBoot", "Setting at Boot:" + FChargeOn);
        try {
            File fastcharge = new File(FAST_CHARGE_DIR, FAST_CHARGE_FILE);
            FileWriter fwriter = new FileWriter(fastcharge);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(FChargeOn ? "1" : "0");
            bwriter.close();
            Intent i = new Intent();
            i.setAction("com.roman.romcontrol.FCHARGE_CHANGED");
            getApplicationContext().sendBroadcast(i);
        } catch (IOException e) {
            Log.e("FChargeBoot", "Couldn't write fast_charge file");
        }

        // add notification to warn user they can only charge
        if (FChargeOn) {
            CharSequence contentTitle = getApplicationContext().getText(
                    R.string.fast_charge_notification_title);
            CharSequence contentText = getApplicationContext().getText(
                    R.string.fast_charge_notification_message);

            Notification n = new Notification.Builder(getApplicationContext())
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_rom_control_general)
                    .setWhen(System.currentTimeMillis())
                    .getNotification();

            NotificationManager nm = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(1337, n);
        }

        // Let's restore color & gamma settings
        ColorTuningPreference.restore(service);
        GammaTuningPreference.restore(service);

        if (Settings.System.getInt(getContentResolver(), Settings.System.USE_WEATHER, 0) != 0) {
            sendLastWeatherBroadcast();
            Intent startRefresh = new Intent(getApplicationContext(),
                    WeatherRefreshService.class);
            getApplicationContext().startService(startRefresh);
        }

        bootThread.start();
        // Stop the service
        stopSelf();
    }

    private void sendLastWeatherBroadcast() {
        SharedPreferences settings = 
            getApplicationContext().getSharedPreferences(WeatherService.PREFS_NAME, 0);

        Intent broadcast = new Intent(WeatherService.INTENT_WEATHER_UPDATE);
        try {
            broadcast.putExtra(WeatherService.EXTRA_CITY, settings.getString("city", ""));
            broadcast.putExtra(WeatherService.EXTRA_CONDITION, settings.getString("condition", ""));
            broadcast.putExtra(WeatherService.EXTRA_LAST_UPDATE, settings.getString("timestamp", ""));
            broadcast.putExtra(WeatherService.EXTRA_CONDITION_CODE, settings.getString("condition_code", ""));
            broadcast.putExtra(WeatherService.EXTRA_FORECAST_DATE, settings.getString("forecast_date", ""));
            broadcast.putExtra(WeatherService.EXTRA_HUMIDITY, settings.getString("humidity", ""));
            broadcast.putExtra(WeatherService.EXTRA_TEMP, settings.getString("temp", ""));
            broadcast.putExtra(WeatherService.EXTRA_WIND, settings.getString("wind", ""));
            broadcast.putExtra(WeatherService.EXTRA_LOW, settings.getString("low", ""));
            broadcast.putExtra(WeatherService.EXTRA_HIGH, settings.getString("high", ""));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        getApplicationContext().sendBroadcast(broadcast);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
