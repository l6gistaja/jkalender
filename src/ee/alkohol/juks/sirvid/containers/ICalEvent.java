package ee.alkohol.juks.sirvid.containers;

import java.util.LinkedHashMap;

public class ICalEvent {
    
    public static final class DBID_STATUSES {
        public static final int UNDEFINED = -1;
        public static final int SUNRISE = -2;
        public static final int SUNSET = -3;
    }
    
    public int dbID = DBID_STATUSES.UNDEFINED;
    public boolean allDayEvent = true;
    public LinkedHashMap<String,ICalProperty> properties = new LinkedHashMap<String,ICalProperty>();
    
}
