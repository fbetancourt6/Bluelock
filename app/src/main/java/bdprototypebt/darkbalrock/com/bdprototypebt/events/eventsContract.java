package bdprototypebt.darkbalrock.com.bdprototypebt.events;

import android.provider.BaseColumns;

public class eventsContract {

    public static abstract class eventEntry implements BaseColumns{
        public static final String tableName = "events";
        public static final String id = "id";
        public static final String eventLog = "eventLog";
        public static final String Time = "Time";
        public static final String uriData = "uriData";
        public static final String Host = "Host";
        public static final String Path = "Path";
        public static final String Query = "Query";
        public static final String Scheme = "Scheme";
        public static final String Port = "Port";
        public static final String userInfo = "userInfo";
        public static final String hashCode = "hashCode";
    }

}
