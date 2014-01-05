package com.aokp.romcontrol.service;

import android.annotation.ManagedSetting;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.aokp.romcontrol.models.AokpSetting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jbird on 12/30/13.
 */
public class BackupService extends Service {
    public static final String TAG = BackupService.class.getSimpleName();
    public static final String ACTION_LOAD_SETTINGS = BackupService.class
            .getCanonicalName() + ".load_settings";
    private static final boolean DEBUG = true;
    public static final String NOTIFICATION = BackupService.class.getCanonicalName() + ".receiver";
    public static final String ERROR = "error";
    public static final String BACKUP_RESPONSE = "json_backup";
    public static final String UPDATE_FREQUENCY = "update_frequency";
    private String mQuery;
    private ContentResolver mResolver;
    private UpdateFrequency mFrequency;
    private Gson mGson;
    private List<AokpSetting> mAokpSettingList;

    public enum UpdateFrequency {
        OnCompletion, Immediate
    }

    public BackupService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGson = new GsonBuilder().create();
        mFrequency = UpdateFrequency.valueOf(intent.getStringExtra(
                UPDATE_FREQUENCY));
        mResolver = getApplicationContext().getContentResolver();
        try {
            List<AokpSetting> aokpSettingsList = getAokpSettingsList(getTableClasses());
            JSONArray denseList = convertSettinsListToJSON(aokpSettingsList);
            if (UpdateFrequency.OnCompletion == mFrequency) {
                publish(false, denseList.toString(DEBUG ? 4 : 0));
            } else {
                Log.d(TAG, "Service finished. Results sent as they were parsed");
            }
        } catch (ClassNotFoundException e) {
            publish(true, e.getMessage());
        } catch (JSONException e) {
            publish(true, e.getMessage());
        }
        return Service.START_NOT_STICKY;
    }

    private JSONArray convertSettinsListToJSON(List<AokpSetting> aokpSettingsList) {
        JSONArray results = new JSONArray();
        for (AokpSetting setting : aokpSettingsList) {
            Gson gson = new GsonBuilder().create();
            results.put(gson.toJson(setting));
        }
        return results;
    }

    private void publish(boolean isError, String message) {
        Intent intent = new Intent(NOTIFICATION);
        if (isError) {
            intent.putExtra(ERROR, message);
        } else {
            intent.putExtra(BACKUP_RESPONSE, message);
        }
        intent.putExtra(UPDATE_FREQUENCY, mFrequency);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    private Class[] getTableClasses() throws ClassNotFoundException {
        Class clazz = Class.forName("android.provider.Settings");
        return clazz.getDeclaredClasses();
    }

    private List<AokpSetting> getAokpSettingsList(Class[] classes) {
        mAokpSettingList = new ArrayList<AokpSetting>(0);
        try {

            // search all tables in the SettingsProvider
            for (Class foundClasses : classes) {
                // get all the fields
                Field[] fields = foundClasses.getDeclaredFields();

                // get a reference to our getter to invoke later
                Method getValueMethod = foundClasses.getDeclaredMethod
                        ("getString", ContentResolver.class, String.class);

                // Evaluate each field for presence of Annotations
                for (Field field : fields) {
                    // name of field
                    String fieldName = field.getName();

                    // for now we only care about Annotated fields
                    // TODO: Should we record the entire db?
                    if (!field.isAnnotationPresent(ManagedSetting.class)) {
                        continue;
                    }
                    ManagedSetting annotation = field.getAnnotation(
                            ManagedSetting.class);

                    // all values we manage should start with UPPERCASE
                    // letters
                    char firstLetter = fieldName.charAt(0);
                    if (!Character.isLowerCase(firstLetter)) {
                        // populate ArrayList with valid entries
                        try {
                            Object value = getValueMethod.invoke(mResolver,
                                    field.getName());

                            AokpSetting aokpSetting = new AokpSetting(
                                    field.getName(),
                                    String.valueOf(value),
                                    annotation);
                            handleSetting(aokpSetting);
                            mAokpSettingList.add(aokpSetting);
                        } catch (IllegalAccessException e) {
                            Log.e(TAG, "Access to field was is restricted: "
                                    + fieldName);
                        } catch (InvocationTargetException e) {
                            Log.e(TAG, "getString threw an exception while " +
                                    "executing.", e.getCause());
                        }
                        if (DEBUG)
                            Log.d(TAG, "found field: " + field.getName());
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            Log.wtf(TAG, "Failed to find getString method via Reflection");
        } catch (SecurityException e) {
            Log.wtf(TAG, "Permission to get method was denied");
        }
        return mAokpSettingList;
    }

    private void handleSetting(AokpSetting aokpSetting) {
        if (UpdateFrequency.Immediate == mFrequency) {
            publish(false, mGson.toJson(aokpSetting));
        } else if (UpdateFrequency.OnCompletion == mFrequency) {
            mAokpSettingList.add(aokpSetting);
        }
    }
}
