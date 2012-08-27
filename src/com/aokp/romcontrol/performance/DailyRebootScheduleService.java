
package com.aokp.romcontrol.performance;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DailyRebootScheduleService extends IntentService {

    static final boolean DEBUG = true;
    static final String TAG = "DailyRebootService";

    static PendingIntent mRebootPendingIntent;
    static PendingIntent mWarningPendingIntent;
    static int rescheduledCount = 0;


    public DailyRebootScheduleService() {
        super(DailyRebootScheduleService.class.getName());
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            log("DailyRebootService intent param was null, goodbye");
            return;
        }

        if (mRebootPendingIntent != null) {
            mRebootPendingIntent.cancel();
            mRebootPendingIntent = null;
        }

        if (isDailyRebootEnabled(this)) {
            if (intent.hasExtra("reschedule")) {
                if (rescheduledCount > 4) {
                    log("too many reschedule attempts, not rebooting today");
                    // reboot tomorrow!
                    int[] rebootTime = getUserSpecifiedRebootTime(this);
                    Calendar cal = Calendar.getInstance();
                    boolean nextDay = cal.get(Calendar.HOUR_OF_DAY) > rebootTime[0];
                    GregorianCalendar d = new GregorianCalendar(cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH), nextDay ? cal.get(Calendar.DATE) + 1
                                    : cal.get(Calendar.DATE), rebootTime[0], rebootTime[1]);
                    d.add(Calendar.DATE, 1);
                    scheduleReboot(d.getTimeInMillis());
                    return;
                }
                rescheduledCount++;
                // reschedule with backoff
                Date rescheduleTime = scheduleReboot();
                log("rescheduled reboot for : " + rescheduleTime.toString());
            } else {
                // regular schedule
                rescheduledCount = 0;
                int[] rebootTime = getUserSpecifiedRebootTime(this);
                Calendar cal = Calendar.getInstance();
                boolean nextDay = cal.get(Calendar.HOUR_OF_DAY) > rebootTime[0];
                GregorianCalendar d = new GregorianCalendar(cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH), nextDay ? cal.get(Calendar.DATE) + 1
                                : cal.get(Calendar.DATE), rebootTime[0], rebootTime[1]);
                scheduleReboot(d.getTimeInMillis());

                if (DEBUG) {
                    java.text.DateFormat dateFormat = DateFormat
                            .getDateFormat(this);
                    log("setup daily reboot at : " + dateFormat.format(d.getTime()));
                }
            }
        } else {
            log("unscheduled daily reboots");
        }
    }

    private void scheduleReboot(long when) {
        AlarmManager am = (AlarmManager) this.getSystemService(
                Context.ALARM_SERVICE);
        mRebootPendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(RebootReceiver.ACTION_REBOOT), 0);
        am.set(AlarmManager.RTC_WAKEUP, when, mRebootPendingIntent);
    }

    private Date scheduleReboot() {
        int[] rebootTime = getUserSpecifiedRebootTime(this);
        Calendar cal = Calendar.getInstance();
        boolean nextDay = cal.get(Calendar.HOUR_OF_DAY) > rebootTime[0];
        GregorianCalendar d = new GregorianCalendar(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), nextDay ? cal.get(Calendar.DATE) + 1
                        : cal.get(Calendar.DATE), rebootTime[0], rebootTime[1]
                        + (30 * ++rescheduledCount));
        scheduleReboot(d.getTimeInMillis());
        return d.getTime();
    }

    public static boolean isDailyRebootEnabled(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(OtherSettings.KEY_DAILY_REBOOT, false);
    }

    public static int[] getUserSpecifiedRebootTime(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        int[] time = new int[2];
        time[0] = prefs.getInt(OtherSettings.KEY_DAILY_REBOOT + "_hour", 1);
        time[1] = prefs.getInt(OtherSettings.KEY_DAILY_REBOOT + "_minute", 0);
        return time;
    }

    public static void setUserSpecifiedRebootTime(Context c, int hour, int minutes) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putInt(OtherSettings.KEY_DAILY_REBOOT + "_hour", hour).
                putInt(OtherSettings.KEY_DAILY_REBOOT + "_minute", minutes).commit();
    }
    
    private static void log(String s) {
        if (DEBUG)
            Log.e(TAG, s);
    }

}
