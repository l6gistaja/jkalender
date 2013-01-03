package ee.alkohol.juks.sirvid.containers.graphics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidSVG.DIM;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public class SirvidMonth {
    
    public GregorianCalendar month;
    public ArrayList<SirvidDay> days = new ArrayList<SirvidDay>();
    public GregorianCalendar solstice;
    public boolean needsExtraSpace4Feasts = false;
    public double feastsZoomRatio;
    
    /**
     * Init and populate month container
     * @param month should be first date of month
     */
    public SirvidMonth(GregorianCalendar m, double feastsZoom) {
        
        month = (GregorianCalendar) m.clone();
        feastsZoomRatio = feastsZoom;
        GregorianCalendar monthEnd = getEndOfMonth(m);
        
        int beginX;
        int index = 0;
        do {
            beginX = SirvidSVG.widths.get(SirvidSVG.DIM.X_MARGIN);
            if(index > 0) {
                SirvidDay previousDay = days.get(index -1);
                beginX = previousDay.beginX 
                        + SirvidSVG.runes.get(SirvidSVG.eventsVsRunes.get(previousDay.weekDay)).width 
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
    	return lastDay.beginX + SirvidSVG.runes.get(lastDay.weekDay).width;
    }
    
    public int calculateY(DIM atPlace) {
        int y = 0;
        switch(atPlace) {
            case Y_TOTAL : y += SirvidSVG.widths.get(DIM.Y_MOONPHASESHEIGHT);
            case Y_MOONPHASESHEIGHT : y += SirvidSVG.widths.get(DIM.Y_MONTHLINEHEIGHT);
            case Y_MONTHLINEHEIGHT2 : y += SirvidSVG.widths.get(DIM.Y_WEEKDAYSHEIGHT);
            case Y_WEEKDAYSHEIGHT : y += SirvidSVG.widths.get(DIM.Y_MONTHLINEHEIGHT);
            case Y_MONTHLINEHEIGHT : y += SirvidSVG.widths.get(DIM.Y_FEASTSHEIGHT) + getXtraY();
            case Y_FEASTSHEIGHT : y += SirvidSVG.widths.get(DIM.Y_MARGIN); break;
            default : ;
        }
        return y;
    }
    
    public int getXtraY() {
        return needsExtraSpace4Feasts ? (int)(SirvidSVG.widths.get(DIM.Y_FEASTSEXTRA)*feastsZoomRatio) : 0;
    }
}
