package ee.alkohol.juks.sirvid.containers;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ICalendar {
    
    // Constants
    public final String ID_SITE = "juks.alkohol.ee";
    
    public static final String SDF_DATE = "yyyyMMdd";
    public static final String SDF_DATETIME = "yyyyMMdd'T'HHmmss'Z'";
    
    public static final class Keys {
        public static final String CALENDAR_TYPE = "calscale";
        public static final String PRODUCT_ID = "prodid";
        public static final String CALENDAR_NAME = "x-wr-calname";
        public static final String CALENDAR_TIMEZONE  = "x-wr-timezone";
        public static final String SUMMARY  = "summary";
        public static final String UID  = "uid";
        public static final String DESCRIPTION  = "description";
        public static final String LANGUAGE  = "language";
        public static final String EVENT_START  = "dtstart";
        public static final String EVENT_END  = "dtend";
        public static final String VALUE  = "value";
        public static final String GEOGRAPHIC_COORDINATES  = "geo";
        public static final String ICAL_VERSION  = "version";
    }
    
    public static final class Values {
        public static final String DATE = "DATE";
        public static final String DATETIME = "DATE-TIME";
    }
    
    // Structure
    
    public LinkedHashMap<String,ICalProperty> iCalBody = new LinkedHashMap<String,ICalProperty>();
    public LinkedList<ICalEvent> vEvent = new LinkedList<ICalEvent>();
    public LinkedHashMap<String,ICalProperty> vVenue;
    
    // etc
    
    public InputData inputData;
    
    public ICalendar(LinkedHashMap<String,ICalProperty> initData) {
        iCalBody.put(Keys.CALENDAR_TYPE, new ICalProperty("GREGORIAN",null));
        iCalBody.put(Keys.PRODUCT_ID, new ICalProperty("-//" + ID_SITE + "//NONSGML Java (sirvi)kalender//ET", null));
        iCalBody.put(Keys.ICAL_VERSION, new ICalProperty("2.0", null));
        if(initData != null) { iCalBody.putAll(initData); }
    }
    
    public String generateUID(String id) {
        return "kdbid_" + id + "@" + ID_SITE;
    }
    
}
