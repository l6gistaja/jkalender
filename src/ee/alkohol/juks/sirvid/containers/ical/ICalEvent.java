package ee.alkohol.juks.sirvid.containers.ical;

import java.util.LinkedHashMap;

public class ICalEvent {
    
    public int dbID = ICalculator.DbIdStatuses.UNDEFINED.getDbId();
    public boolean allDayEvent = false;
    public LinkedHashMap<String,ICalProperty> properties = new LinkedHashMap<String,ICalProperty>();
    
}
