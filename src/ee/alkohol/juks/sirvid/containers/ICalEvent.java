package ee.alkohol.juks.sirvid.containers;

import java.util.LinkedHashMap;

public class ICalEvent {
    
    public int dbID = ICalculator.DbIdStatuses.UNDEFINED.getDbId();
    public boolean allDayEvent = true;
    public LinkedHashMap<String,ICalProperty> properties = new LinkedHashMap<String,ICalProperty>();
    
}
