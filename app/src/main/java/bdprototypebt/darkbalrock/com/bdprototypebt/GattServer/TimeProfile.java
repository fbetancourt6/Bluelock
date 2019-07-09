package bdprototypebt.darkbalrock.com.bdprototypebt.GattServer;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

/*
* Implementacion de Bluetooth GATT Time Profile
* https://www.bluetooth.com/specifications/adopted-specifications
* */
public class TimeProfile {
    private static final String TAG = TimeProfile.class.getSimpleName();

    /*Servicio de hora actual UUID*/
    public static UUID TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    /*Característica obligatoria de la información en tiempo actual*/
    public static UUID CURRENT_TIME = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
    /*Característica opcional de información de hora local*/
    public static UUID LOCAL_TIME_INFO = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb");
    /*Descriptor de configuración de características del cliente obligatorio*/
    public static UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /*Banderas de Ajuste*/
    public static final byte ADJUST_NONE = 0X0;
    public static final byte ADJUST_MANUAL = 0X1;
    public static final byte ADJUST_EXTERNAL = 0X2;
    public static final byte ADJUST_TIMEZONE = 0X4;
    public static final byte ADJUST_DST = 0X8;

    /*
    * Retorna una instancia de {@link BluetoothGattService} para el servicio de tiempo actual
    * */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BluetoothGattService createTimeService(){
        BluetoothGattService service = new BluetoothGattService(TIME_SERVICE,BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //Característica de tiempo actual
        BluetoothGattCharacteristic currentTime = new BluetoothGattCharacteristic(CURRENT_TIME,
                //Característica de solo lectura, soporta notificaciones.
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(CURRENT_TIME,
                //Descriptor de lectura lectura/escritura
                BluetoothGattDescriptor.PERMISSION_READ|BluetoothGattDescriptor.PERMISSION_WRITE);
        currentTime.addDescriptor(configDescriptor);

        //Característica de información de hora local
        BluetoothGattCharacteristic localTime = new BluetoothGattCharacteristic(LOCAL_TIME_INFO,
                //Característica de solo lectura
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
                );

        service.addCharacteristic(currentTime);
        service.addCharacteristic(localTime);

        return service;
    }

    /*
    * Construye los valores de campo para una característica de Tiempo Actual
    * Del momento dado la marca de tiempo y la razón de ajuste.
    *
    * */
    public static byte[] getExactTime(long timestamp, byte adjustReason){
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timestamp);
        byte[] momento = new byte[10];
        //año
        int year = time.get(Calendar.YEAR);
        momento[0] = (byte) (year & 0xFF);
        momento[1] = (byte) ((year >> 8) & 0xFF);
        //mes
        momento[2] = (byte) (time.get(Calendar.MONTH)+1);
        //dia
        momento[3] = (byte) time.get(Calendar.DATE);
        //horas
        momento[4] = (byte) time.get(Calendar.HOUR_OF_DAY);
        //minutos
        momento[5] = (byte) time.get(Calendar.MINUTE);
        //segundos
        momento[6] = (byte) time.get(Calendar.SECOND);
        //dia de la semana (1-7)
        momento[7] = (byte) time.get(Calendar.DAY_OF_WEEK);
        //Fracciones de milisegundos 256
        momento[8] = (byte) (time.get(Calendar.MILLISECOND)/256);

        momento[9] = adjustReason;

        return momento;
    }

    /*Constantes de tiempo para la información de hora local*/
    private static final int FIFTEEN_MINUTES_MILLIS = 900000;
    private static final int HALF_HOUR_MILLIS = 1800000;

    /*
    * Construya los valores de campo para una característica de información
    * de hora local a partir de la marca de tiempo de la época dada.
    * */
    public static byte[] getLocalTimeInfo(long timestamp){
        Calendar time = Calendar.getInstance();
        TimeZone mTimeZone = time.getTimeZone();
        time.setTimeInMillis(timestamp);

        byte[] timeInfo = new byte[2];

        //Zona horaria
        int zoneOffset = time.get(Calendar.ZONE_OFFSET)/FIFTEEN_MINUTES_MILLIS; //Intervalos de 15 minutos
        timeInfo[0] = (byte) zoneOffset;

        //DST offset
        timeInfo[1] = (byte) mTimeZone.getRawOffset();

        return timeInfo;
    }

    /* Códigos Bluetooth de día de la semana*/
    private static final byte DAY_UNKNOWN = 0;
    private static final byte DAY_MONDAY = 1;
    private static final byte DAY_TUESDAY = 2;
    private static final byte DAY_WEDNESDAY = 3;
    private static final byte DAY_THURSDAY = 4;
    private static final byte DAY_FRIDAY = 5;
    private static final byte DAY_SATURDAY = 6;
    private static final byte DAY_SUNDAY = 7;

    /*
    * Convierte el valor de un día de la semana en el correspondiente
    * Código del día de la semana de Bluetooth.
    * */
    private static byte getDayOfWeekCode(int dayOfWeek){
        switch(dayOfWeek){
            case Calendar.MONDAY:
                return DAY_MONDAY;
            case Calendar.TUESDAY:
                return DAY_TUESDAY;
            case Calendar.WEDNESDAY:
                return DAY_WEDNESDAY;
            case Calendar.THURSDAY:
                return DAY_THURSDAY;
            case Calendar.FRIDAY:
                return DAY_FRIDAY;
            case Calendar.SATURDAY:
                return DAY_SATURDAY;
            case Calendar.SUNDAY:
                return DAY_SUNDAY;
            default:
                return DAY_UNKNOWN;
        }
    }

    /*Códigos OffSet de Bluetooth DST*/
    private static final byte DST_STANDARD = 0x0;
    private static final byte DST_HALF = 0x02;
    private static final byte DST_SINGLE = 0x04;
    private static final byte DST_DOUBLE = 0x08;
    private static final byte DST_UNKNOWN = (byte) 0xFF;

    /*
     * Convierte un desplazamiento de horario de verano sin procesar (en intervalos de 30 minutos) al
     * código de compensación DST Bluetooth correspondiente
    * */
    private static byte getDstOffsetCode(int rawOffset){
        switch (rawOffset){
            case 0:
                return DST_STANDARD;
            case 1:
                return DST_HALF;
            case 2:
                return DST_SINGLE;
            case 4:
                return DST_DOUBLE;
            default:
                return DST_UNKNOWN;
        }
    }

}
