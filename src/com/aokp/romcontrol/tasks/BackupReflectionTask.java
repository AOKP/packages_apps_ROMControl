package com.aokp.romcontrol.tasks;

import android.annotation.ManagedSetting;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;
import com.aokp.romcontrol.models.AokpSetting;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jbird on 12/29/13.
 */
public abstract class BackupReflectionTask extends AsyncTask<Class[], Void,
        List<AokpSetting>> {
    private static final boolean DEBUG = true;
    private static final String TAG = BackupReflectionTask.class.getSimpleName();
    private final ContentResolver mResolver;

    public BackupReflectionTask(ContentResolver resolver) {
        mResolver = resolver;
    }
    @Override
    protected List<AokpSetting> doInBackground(Class[]... classes) {
        List<AokpSetting> returnList = new ArrayList<AokpSetting>(0);
        try {

            // search all tables in the SettingsProvider
            for (Class foundClasses : classes[0]) {
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

                    // all wanted values should have UPPERCASE letters
                    char firstLetter = fieldName.charAt(0);
                    if (!Character.isLowerCase(firstLetter)) {
                        // populate ArrayList with valid entries
                        try {
                            Object value = getValueMethod.invoke(mResolver,
                                            field.getName());
                            returnList.add(new AokpSetting(
                                    field.getName(),
                                    String.valueOf(value),
                                    annotation));
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
        return returnList;
    }

    @Override
    protected void onPostExecute(List<AokpSetting> settings) {
        super.onPostExecute(settings);
        deliverResults(settings);
    }

    public abstract void deliverResults(List<AokpSetting> settings);
}