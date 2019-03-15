package bdprototypebt.darkbalrock.com.bdprototypebt.devices;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class devicesDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "devices.db";

    public devicesDBHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        //Create Table
        sqLiteDatabase.execSQL("CREATE TABLE "+ devicesContract.deviceEntry.tableName+ "("
                + devicesContract.deviceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + devicesContract.deviceEntry.ID + " INT, "
                + devicesContract.deviceEntry.name + " TEXT, "
                + devicesContract.deviceEntry.address + " TEXT, "
                + devicesContract.deviceEntry.UUIDs + " TEXT, "
                + devicesContract.deviceEntry.contentDesc + " TEXT, "
                + devicesContract.deviceEntry.time + " TEXT, "
                + devicesContract.deviceEntry.bonded + " TEXT, "
                + devicesContract.deviceEntry.hashCode + " TEXT, "
                + " UNIQUE ("+devicesContract.deviceEntry.address+")"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ContentValues toContentValues(device device){
        ContentValues values = new ContentValues();
        values.put(devicesContract.deviceEntry.ID, device.getId());
        values.put(devicesContract.deviceEntry.name, device.getName());
        values.put(devicesContract.deviceEntry.address, device.getAddress());
        values.put(devicesContract.deviceEntry.UUIDs, device.getUUIDs());
        values.put(devicesContract.deviceEntry.contentDesc, device.getContentDesc());
        values.put(devicesContract.deviceEntry.time, device.getTime());
        values.put(devicesContract.deviceEntry.bonded, device.getBonded());
        values.put(devicesContract.deviceEntry.hashCode, device.getHashCode());
        return values;
    }

    public long saveDevice(device device){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(devicesContract.deviceEntry.tableName,
                                        null,
                                            toContentValues(device));
    }

    public Cursor getDevice(String tabla, String[] columnas, String selection, String[] selectionArgs, String groupBy, String having, String orderBy){
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor c = sqLiteDatabase.query(
                tabla,
                columnas,
                selection,
                selectionArgs,
                groupBy,
                having,
                orderBy
        );
        return c;
    }

}

