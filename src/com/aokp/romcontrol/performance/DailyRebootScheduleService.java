
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

    static boolean instanceActive = false;

    public DailyRebootScheduleService() {
        super(DailyRebootScheduleService.class.getName());
        setIntentRedelivery(true);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        instanceActive = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instanceActive = false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            log("DailyRebootService intent param was null, goodbye");
            return;
        }

        if (!instanceActive) {
            log("duplicate instance avoided");
            return;
        }

        if (OtherSettings.isDailyRebootEnabled(this)) {
            if (intent.hasExtra("reschedule")) {
                if (rescheduledCount > 4) {
                    log("too many reschedule attempts, not rebooting today");
                    // reboot tomorrow!
                    int[] rebootTime = OtherSettings.getUserSpecifiedRebootTime(this);
                    Calendar cal = Calendar.getInstance();
                    boolean nextDay = cal.get(Calendar.HOUR_OF_DAY) > rebootTime[0]
                            && cal.get(Calendar.MINUTE) > rebootTime[1];
                    GregorianCalendar d = new GregorianCalendar(cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH), nextDay ? cal.get(Calendar.DATE) + 1
                                    : cal.get(Calendar.DATE), rebootTime[0], rebootTime[1]);
                    d.add(Calendar.DATE, 1);
                    scheduleReboot(d.getTimeInMillis());
                    log("too many reschedule attempts; rescheduled for tomorrow, at: "
                            + d.getTime().toString());
                } else {
                    rescheduledCount++;
                    // reschedule with backoff
                    Date rescheduleTime = scheduleReboot();
                    log("rescheduled reboot for : " + rescheduleTime.toString());
                }
            } else {
                if (rescheduledCount == 0) {
                    // regular schedule
                    rescheduledCount++;
                    int[] rebootTime = OtherSettings.getUserSpecifiedRebootTime(this);
                    Calendar cal = Calendar.getInstance();
                    boolean nextDay = cal.get(Calendar.HOUR_OF_DAY) >= rebootTime[0]
                            && cal.get(Calendar.MINUTE) >= rebootTime[1];
                    GregorianCalendar d = new GregorianCalendar(cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH), nextDay ? cal.get(Calendar.DATE) + 1
                                    : cal.get(Calendar.DATE), rebootTime[0], rebootTime[1]);
                    scheduleReboot(d.getTimeInMillis());

                    log("setup daily reboot at : " + d.getTime().toString());
                } else {
                    log("already scheduled -- not doing anything (" + rescheduledCount + ")");
                }
            }
        } else {
            rescheduledCount = 0;
            log("unscheduled daily reboots");
        }
    }

    private void scheduleReboot(long when) {
        AlarmManager am = (AlarmManager) this.getSystemService(
                Context.ALARM_SERVICE);
        if (mRebootPendingIntent != null) {
            mRebootPendingIntent.cancel();
            mRebootPendingIntent = null;
        }
        mRebootPendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(RebootReceiver.ACTION_REBOOT), 0);
        am.set(AlarmManager.RTC_WAKEUP, when, mRebootPendingIntent);
    }

    private Date scheduleReboot() {
        int[] rebootTime = OtherSettings.getUserSpecifiedRebootTime(this);
        Calendar cal = Calendar.getInstance();
        boolean nextDay = cal.get(Calendar.HOUR_OF_DAY) > rebootTime[0]
                & cal.get(Calendar.MINUTE) > rebootTime[1];
        GregorianCalendar d = new GregorianCalendar(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), nextDay ? cal.get(Calendar.DATE) + 1
                        : cal.get(Calendar.DATE), rebootTime[0], rebootTime[1]
                        + (30 * rescheduledCount++));
        scheduleReboot(d.getTimeInMillis());
        return d.getTime();
    }

    private static void log(String s) {
        if (DEBUG)
            Log.i(TAG, s);
    }

}
