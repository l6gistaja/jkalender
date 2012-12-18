package ee.alkohol.juks.sirvid.containers.graphics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public class SirvidMonth {
    
    public GregorianCalendar month;
    public ArrayList<SirvidDay> days = new ArrayList<SirvidDay>();
    public GregorianCalendar solstice;
    
    /**
     * Init and populate month container
     * @param month should be first date of month
     */
    public SirvidMonth(GregorianCalendar m) {
        
        month = (GregorianCalendar) m.clone();
        GregorianCalendar monthEnd = getEndOfMonth(m);
        
        int beginX;
        int index = 0;
        do {
            beginX = SirvidSVG.widths.get(SirvidSVG.DIM.X_MARGIN);
            if(index > 0) {
                SirvidDay previousDay = days.get(index -1);
                beginX = previousDay.beginX 
                        + SirvidSVG.runes.get(SirvidSVG.eventsVsRunes.get(previousDay.weekDay)).getWidth() 
                        + SirvidSVG.widths.get(SirvidSVG.DIM.X_WEEKDAYPADDING);
            }
            days.add(new SirvidDay(m, beginX));
            index ++;
            m.add(Calendar.DATE, 1);
        } while (m.before(monthEnd));
        
    }
    
    public static GregorianCalendar getEndOfMonth(GregorianCalendar m) {
        GregorianCalendar monthEnd = ICalculator.getCalendar(m.getTimeZone().getID());
        monthEnd.set(Calendar.YEAR, m.get(Calendar.YEAR));
        monthEnd.set(Calendar.MONTH, m.get(Calendar.MONTH));
        monthEnd.set(Calendar.DAY_OF_MONTH, m.getActualMaximum(Calendar.DAY_OF_MONTH));
        monthEnd.set(Calendar.HOUR_OF_DAY, 23);
        monthEnd.set(Calendar.MINUTE, 59);
        monthEnd.set(Calendar.SECOND, 59);
        monthEnd.set(Calendar.MILLISECOND, 999);
        return monthEnd;
    }
    
    public int getMaxX() {
    	SirvidDay lastDay = days.get(days.size()-1);
    	return lastDay.beginX + SirvidSVG.runes.get(lastDay.weekDay).getWidth();
    }
}
