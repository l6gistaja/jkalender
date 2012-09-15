package ee.alkohol.juks.sirvid.containers;

public class ICalendar {
    
    public String[][] iCalBody = {
            {"calscale","GREGORIAN"},
            {"prodid","//j.a.e//Java (sirvi)kalender//"}
    };
    
    public String[][][] vEvent = {};
    
    public String comment;
    
    public ICalendar() {
        
    }
    
}
