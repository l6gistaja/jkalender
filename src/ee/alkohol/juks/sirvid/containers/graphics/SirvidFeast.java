package ee.alkohol.juks.sirvid.containers.graphics;

import ee.alkohol.juks.sirvid.containers.ical.ICalEvent;

public class SirvidFeast {
    
    public ICalEvent event;
    public StringBuilder label = new StringBuilder();
    public int rotate = 0;
    public StringBuilder xtraSVG = new StringBuilder();
    
    public SirvidFeast(ICalEvent e) {
        event = e;
    }
}
