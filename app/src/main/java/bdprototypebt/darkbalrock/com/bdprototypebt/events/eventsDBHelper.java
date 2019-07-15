package bdprototypebt.darkbalrock.com.bdprototypebt.events;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class eventsDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "events2.db";

    public eventsDBHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + eventsContract.eventEntry.tableName+ " ("
                + eventsContract.eventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + eventsContract.eventEntry.id + " INTEGER ,"
                + eventsContract.eventEntry.eventLog + " TEXT ,"
                + eventsContract.eventEntry.Time + " TEXT ,"
                + eventsContract.eventEntry.uriData + " TEXT ,"
                + eventsContract.eventEntry.Host + " TEXT ,"
                + eventsContract.eventEntry.Path + " TEXT ,"
                + eventsContract.eventEntry.Query + " TEXT ,"
                + eventsContract.eventEntry.Scheme + " TEXT ,"
                + eventsContract.eventEntry.Port + " TEXT ,"
                + eventsContract.eventEntry.userInfo + " TEXT ,"
                + eventsContract.eventEntry.hashCode + " TEXT ,"
                + "UNIQUE (" + eventsContract.eventEntry._ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No hay operaciones
    }

    public ContentValues toContentValues(event evt){
        ContentValues values = new ContentValues();
        values.put(eventsContract.eventEntry.id, evt.getId());
        values.put(eventsContract.eventEntry.eventLog, evt.getEventLog());
        values.put(eventsContract.eventEntry.Time, evt.getTime());
        values.put(eventsContract.eventEntry.uriData, evt.getUriData());
        values.put(eventsContract.eventEntry.Host, evt.getHost());
        values.put(eventsContract.eventEntry.Path, evt.getPath());
        values.put(eventsContract.eventEntry.Query, evt.getQuery());
        values.put(eventsContract.eventEntry.Scheme, evt.getScheme());
        values.put(eventsContract.eventEntry.Port, evt.getPort());
        values.put(eventsContract.eventEntry.userInfo, evt.getUserInfo());
        values.put(eventsContract.eventEntry.hashCode, evt.getHashCode());
        return values;
    }

    public long saveEvent(event evt){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(eventsContract.eventEntry.tableName,
                null,
                toContentValues(evt));
    }

    public Cursor getEvent(String tabla, String[] columnas, String selection, String[] selectionArgs, String groupBy, String having, String orderBy){
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

    /*
     * Elimina todos los eventos
     * */
    public void deleteEvents(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "+eventsContract.eventEntry.tableName);
        db.close();
    }
}
