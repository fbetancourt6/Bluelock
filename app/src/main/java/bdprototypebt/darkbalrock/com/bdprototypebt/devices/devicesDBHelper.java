package bdprototypebt.darkbalrock.com.bdprototypebt.devices;
/*
* Clase devicesDBHelper
* Artefacto de acceso y manipulacion de la BD dispositivos
* */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class devicesDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "devices3.db";

    //Instanciamos la clase SQLiteOpenHelper
    public devicesDBHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
    * Creamos la tabla de dispositivos
    * */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        //Create Table
        sqLiteDatabase.execSQL("CREATE TABLE "+ devicesContract.deviceEntry.tableName+ "("
                + devicesContract.deviceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + devicesContract.deviceEntry.ID + " INTEGER, "
                + devicesContract.deviceEntry.name + " TEXT, "
                + devicesContract.deviceEntry.address + " TEXT, "
                + devicesContract.deviceEntry.UUIDs + " TEXT, "
                + devicesContract.deviceEntry.contentDesc + " TEXT, "
                + devicesContract.deviceEntry.time + " TEXT, "
                + devicesContract.deviceEntry.bonded + " TEXT, "
                + devicesContract.deviceEntry.hashCode + " TEXT, "
                + devicesContract.deviceEntry.bloqueado + " TEXT, "
                + " UNIQUE ("+devicesContract.deviceEntry.address+")"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*
    * Genera un artefacto ContentValues para manipular la BD
    * */
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
        values.put(devicesContract.deviceEntry.bloqueado, device.getBloqueado());
        return values;
    }

    /*
    * Guarda un dispositivo en la BD
    * */
    public long saveDevice(device device){
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(devicesContract.deviceEntry.tableName,
                                        null,
                                            toContentValues(device));
    }

    /*
    * Consulta un dispositivo y retorna un Cursor
    * */
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

    /*
     * Consulta todos los dispositivos y retorna un Cursor
     * */
    public Cursor getDevices(){
        String tabla = devicesContract.deviceEntry.tableName,
                selection = null,
                groupBy = null,
                having = null,
                orderBy = null;
        String[] columnas = null, selectionArgs = null;
        Cursor d = getDevice(tabla,columnas,selection,selectionArgs,groupBy,having,orderBy);
        return d;
    }


    /*
     * Elimina todos los dispositivos
     * */
    public void deleteDevices(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "+devicesContract.deviceEntry.tableName);
        db.close();
    }


    /*
     * Actualiza un dispositivo y retorna un boleano
     * */
    public boolean updateDevice(device device){
        boolean result = false;
        try {
            ContentValues cv = new ContentValues();
            cv.put(devicesContract.deviceEntry.name, device.getName());
            cv.put(devicesContract.deviceEntry.UUIDs, device.getUUIDs());
            cv.put(devicesContract.deviceEntry.contentDesc, device.getContentDesc());
            cv.put(devicesContract.deviceEntry.name, device.getName());
            cv.put(devicesContract.deviceEntry.time, device.getTime());
            cv.put(devicesContract.deviceEntry.hashCode, device.getHashCode());

            SQLiteDatabase db = getWritableDatabase();
            db.update(devicesContract.deviceEntry.tableName, cv, devicesContract.deviceEntry.address + "= '" + device.getAddress()+"'", null);
            db.close();
            result = true;
        }catch (Exception e){
            Log.e("deviceDBHelper", "Error actualizando Dispositivo: " + e.toString());
        }
        return result;
    }

    /*
     * Valida el estado del dispositivo y actualiza la columna bloqueado
     * */
    public boolean blockDevice(device dev){
        boolean result = false;
        String estado = "bloqueado";
        try {
            String args [] = new String[1];
            args[0] = dev.getAddress();
            Cursor cursor = this.getDevice(devicesContract.deviceEntry.tableName,null,devicesContract.deviceEntry.address+"=?",args,null,null,null);
            if (cursor.getCount() > 0){
                cursor.moveToFirst();
                String bloqueado = cursor.getString(cursor.getColumnIndex(devicesContract.deviceEntry.bloqueado));
                if(bloqueado == null){
                    estado = "bloqueado";
                    result = true;
                }
                else if(bloqueado.equals("bloqueado")) {
                    estado = "desbloqueado";
                }
                else{
                    estado = "bloqueado";
                    result = true;
                }
            }else{
                Log.e("deviceDBHelper", "Error bloqueando Dispositivo no emparejado:"+dev.getAddress() );
            }
            ContentValues cv = new ContentValues();
            cv.put(devicesContract.deviceEntry.bloqueado, estado);
            SQLiteDatabase db = getWritableDatabase();
            db.update(devicesContract.deviceEntry.tableName, cv, devicesContract.deviceEntry.address + "='" + dev.getAddress()+"'", null);
            db.close();
        }catch (Exception e){
            Log.e("deviceDBHelper", "Error bloqueando Dispositivo: " + e.toString());
        }
        return result;
    }

    /*
    * Generar una consulta rapida retornando un cursor
    * */
    public Cursor raw(String query){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(query, null);
        return c;
    }

}

