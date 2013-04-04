
package com.aokp.romcontrol.vibrations;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class VibrationsProvider extends ContentProvider
{
    private static final String TAG = "VibrationsProvider";
    public static final String PROVIDER_NAME =
            "com.aokp.romcontrol.Vibrations";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + PROVIDER_NAME + "/vibrations");

    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String PATTERN = "pattern";

    private static final int VIBRATIONS = 1;
    private static final int VIBRATION_ID = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "vibrations", VIBRATIONS);
        uriMatcher.addURI(PROVIDER_NAME, "vibrations/#", VIBRATION_ID);
    }

    private static final String AOKPVibrationName = "'AOKP'";
    private static final String AOKPVibrationPattern = "'500,150,400,400,400,400,400,400,400,400,400,400,150,150,400,400,150,150,400,400,400,400,150,150,150'";
    private static final String CQDVibrationName = "'CQD'";
    private static final String CQDVibrationPattern = "'500,400,400,150,150,400,400,150,150,400,400,400,400,150,150,400,400,400,400,150,150,150'";
    private static final String defaultVibrationName = "'Default'";
    private static final String defaultVibrationPattern = "'500,1000,1000,1000,1000'";
    // ---because pretty women are tasty treats---
    private static final String NOMVibrationName = "'NOM'";
    private static final String NOMVibrationPattern = "'500,400,400,150,150,400,400,400,400,400,400,400,400,400'";
    private static final String SOSVibrationName = "'S.O.S.'";
    private static final String SOSVibrationPattern = "'500,150,150,150,150,150,400,400,400,400,400,400,400,150,150,150,150,150'";

    // ---for database use---
    private SQLiteDatabase vibrationsDB;
    private static final String DATABASE_NAME = "Vibrations";
    private static final String DATABASE_TABLE = "names";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE +
                    " (_id integer primary key autoincrement, "
                    + "name text not null, pattern text not null);";

    private static final String DATABASE_INIT_AOKP =
            "insert into names (name, pattern) " +
                    "values (" + AOKPVibrationName + ", " + AOKPVibrationPattern + ")";

    private static final String DATABASE_INIT_CQD =
            "insert into names (name, pattern) " +
                    "values (" + CQDVibrationName + ", " + CQDVibrationPattern + ")";

    private static final String DATABASE_INIT_DEFAULT =
            "insert into names (_id, name, pattern) " +
                    "values (" + "0" + ", " + defaultVibrationName + ", " + defaultVibrationPattern
                    + ")";

    private static final String DATABASE_INIT_NOM =
            "insert into names (name, pattern) " +
                    "values (" + NOMVibrationName + ", " + NOMVibrationPattern + ")";

    private static final String DATABASE_INIT_SOS =
            "insert into names (name, pattern) " +
                    "values (" + SOSVibrationName + ", " + SOSVibrationPattern + ")";

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_INIT_AOKP);
            db.execSQL(DATABASE_INIT_CQD);
            db.execSQL(DATABASE_INIT_DEFAULT);
            db.execSQL(DATABASE_INIT_NOM);
            db.execSQL(DATABASE_INIT_SOS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                int newVersion) {
            // db.execSQL("DROP TABLE IF EXISTS names");
            // onCreate(db);
            if (oldVersion == 1) {
                db.execSQL(DATABASE_INIT_AOKP);
                db.execSQL(DATABASE_INIT_CQD);
                db.execSQL(DATABASE_INIT_NOM);
                oldVersion++;
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case VIBRATIONS: // ---get all vibrations---
                return "vnd.android.cursor.dir/vnd.romcontrol.vibrations";
            case VIBRATION_ID: // ---get a particular vibration---
                return "vnd.android.cursor.item/vnd.romcontrol.vibrations";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        vibrationsDB = dbHelper.getWritableDatabase();
        return (vibrationsDB == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);

        if (uriMatcher.match(uri) == VIBRATION_ID) {
            // ---if getting a particular vibration---
            sqlBuilder.appendWhere(
                    _ID + " = " + uri.getLastPathSegment());
        }

        if (sortOrder == null || sortOrder == "")
            sortOrder = NAME;

        Cursor c = sqlBuilder.query(
                vibrationsDB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        // ---register to watch a content URI for changes---
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // ---add a new vibration---
        long rowID = vibrationsDB.insert(
                DATABASE_TABLE, "", values);

        // ---if added successfully---
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // arg0 = uri
        // arg1 = selection
        // arg2 = selectionArgs
        int count = 0;
        switch (uriMatcher.match(arg0)) {
            case VIBRATIONS:
                count = vibrationsDB.delete(
                        DATABASE_TABLE,
                        arg1,
                        arg2);
                break;
            case VIBRATION_ID:
                String id = arg0.getPathSegments().get(1);
                if (Integer.parseInt(id) == 0) {
                    Log.i(TAG, "Not deleting default vibration");
                    return count;
                }
                count = vibrationsDB.delete(
                        DATABASE_TABLE,
                        _ID + " = " + id +
                                (!TextUtils.isEmpty(arg1) ? " AND (" +
                                        arg1 + ')' : ""),
                        arg2);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown URI " + arg0);
        }
        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs)
    {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case VIBRATIONS:
                count = vibrationsDB.update(
                        DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case VIBRATION_ID:
                String id = uri.getPathSegments().get(1);
                if (Integer.parseInt(id) == 0) {
                    Log.i(TAG, "Not updating default vibration");
                    return count;
                }
                count = vibrationsDB.update(
                        DATABASE_TABLE,
                        values,
                        _ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +
                                        selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
