package bdprototypebt.darkbalrock.com.bdprototypebt.devices;
/*
* Clase devicesContract
* Definir un tipo de Objecto BaseColumns para las columnas de la tabla dispositivos
* */

import android.provider.BaseColumns;

public class devicesContract {
    public static abstract class deviceEntry implements BaseColumns{
        public static final String tableName = "devices4";
        public static final String ID = "id";
        public static final String name = "name";
        public static final String address = "address";
        public static final String UUIDs = "UUIDs";
        public static final String contentDesc = "contentDesc";
        public static final String time = "time";
        public static final String bonded = "bonded";
        public static final String hashCode = "hashCode";
        public static final String bloqueado = "bloqueado";
    }
}
