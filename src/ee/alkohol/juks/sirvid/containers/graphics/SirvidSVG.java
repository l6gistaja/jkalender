package ee.alkohol.juks.sirvid.containers.graphics;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import ee.alkohol.juks.sirvid.containers.DaoKalenderJDBCSqlite;
import ee.alkohol.juks.sirvid.containers.PropertiesT;
import ee.alkohol.juks.sirvid.containers.ical.ICalEvent;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;
import ee.alkohol.juks.sirvid.containers.ical.ICalendar;

public class SirvidSVG {
    
    public static final String dataPath = "sirvid/";
    public static final String[] errorTxtTags = { "<text x=\"50\" y=\"60\" fill=\"black\" font-size=\"100\">", "</text>" };
    public static final int J6ULUD = 1221;
    public static final int LIUGUP2EV = 1953;
    public static final int TUHKAP2EV = 1954;
    public static final String[] weekDays = {"P","E","T","K","N","R","L"};
    public static enum  DIM {
        X_MARGIN,
        X_WEEKDAYPADDING,
        X_MONTHLINESEXTENSION,
        
        Y_MARGIN,
        Y_FEASTSHEIGHT,
        Y_MONTHLINEHEIGHT,
        Y_WEEKDAYSHEIGHT,
        Y_MOONPHASESHEIGHT,
        
        Y_MONTHLINEHEIGHT2,
        Y_TOTAL              
    }
    
    public static PropertiesT props = null;
    /**
     * Different measurements/dimensions from svg_export.properties file
     */
    public static HashMap<DIM,Integer> widths = null;
    public static HashMap<Integer,SirvidRune> runes = null;
    /**
     * Maps DB event IDs to DB rune IDs if available
     */
    public static HashMap<Integer,Integer> eventsVsRunes = null;
    /**
     * Most common events mapping from DB event ID to event's title ([0]) and description ([1])
     */
    public static HashMap<Integer,String[]> commonLabels = null;
    /**
     * Months in (I)calculation's timespan
     * @see SirvidSVG#generateMonthIndex(GregorianCalendar)
     */
    public ArrayList<SirvidMonth> months = new ArrayList<SirvidMonth>();
    
    private ICalculator iCalc;
    public ArrayList<String> errorMsgs = new ArrayList<String>();
    /**
     * @see SirvidSVG#generateMonthIndex(GregorianCalendar)
     */
    public int beginMonth;
    
    
    public SirvidSVG(ICalculator iC) {
        
        iCalc = iC;
        
        if(props == null) {
            props = new PropertiesT();
        	try {
                props.load(this.getClass().getClassLoader().getResourceAsStream(dataPath + "svg_export.properties"));
                if(widths == null) { widths = new HashMap<DIM,Integer>(); }
                for (DIM w : DIM.values()) {
                    if(w.equals(DIM.Y_TOTAL) || w.equals(DIM.Y_MONTHLINEHEIGHT2)) { continue; }
                    widths.put(w, props.getPropertyInt(w.toString()));
                }
            }
            catch(Exception e) {
                errorMsgs.add("Failed to open svg_export.properties : " + e.getMessage());
            }
        }
        
        DaoKalenderJDBCSqlite CalendarDAO = iCalc.CalendarDAO;
        
        if(runes == null) {
            runes = new HashMap<Integer,SirvidRune>();
            if(CalendarDAO.isConnected()) {
        		ResultSet commonRunes = CalendarDAO.getRange(0, ICalculator.DbIdStatuses.MOON_LAST.getDbId(), DaoKalenderJDBCSqlite.DbTables.RUNES, null);
                if(commonRunes != null) {
                    try {
                        while(commonRunes.next()) { 
                            Integer runeID = new Integer(commonRunes.getInt(DaoKalenderJDBCSqlite.DbTables.RUNES.getPK()));
                            runes.put(runeID, new SirvidRune(commonRunes));
                        }
                    }
                    catch(Exception e) {
                        errorMsgs.add("SVG runes init failed : " + e.getMessage());
                    }
                }
            }
        }
        
        if(commonLabels == null) {
            commonLabels = new HashMap<Integer,String[]>();
            if(CalendarDAO.isConnected()) {
                    ResultSet wd = CalendarDAO.getRange(0, ICalculator.DbIdStatuses.SUNSET.getDbId(), DaoKalenderJDBCSqlite.DbTables.EVENTS, null);
                    if(wd != null) {
                        try {
                            while(wd.next()) {
                            	commonLabels.put(new Integer(wd.getInt(DaoKalenderJDBCSqlite.DbTables.EVENTS.getPK())), new String[] {
                            		wd.getString(DaoKalenderJDBCSqlite.DbTables.EVENTS.getTitle()),
                            		wd.getString(DaoKalenderJDBCSqlite.DbTables.EVENTS.getDescription())
                            	});
                            }
                        }
                        catch(Exception e) {
                            //errorMsgs.add("SVG weekdays init failed : " + e.getMessage());
                        }
                    }
            }
        }
        
        dummyRunes();
        
        if(eventsVsRunes == null) {
            eventsVsRunes = new HashMap <Integer, Integer>();
            for(Integer runeID : runes.keySet()) { eventsVsRunes.put(runeID, runeID); }
        }
        
        // first day of beginning month
        GregorianCalendar date0 = getSirvBeginning(iCalc.calendarBegin);
        
        beginMonth = generateMonthIndex(date0);
        
        // last day of ending month
        GregorianCalendar date1 = SirvidMonth.getEndOfMonth(calendarAsUTCCalendar(iCalc.calendarEnd));
                
        // init and populate sirvi-containers
        do {
            months.add(new SirvidMonth(date0));
        } while (date0.before(date1));
        
        populateEvents();
        
        prepareEvents();
        
    }
    
    /**
     * Converts GregorianCalendar months to continuos, gapless sequence of integers. 
     *
     * generateMonthIndex(X) - {@link #beginMonth} is index of month X in months array {@link #months}
     * 
     * @param month as UTC GregorianCalendar
     * @return Month's sequence number
     */
    private static int generateMonthIndex(GregorianCalendar month) {
        return 12*month.get(Calendar.YEAR) + month.get(Calendar.MONTH);
    }
    
    public int calculateY(DIM atPlace) {
        int y = 0;
        switch(atPlace) {
            case Y_TOTAL : y += widths.get(DIM.Y_MOONPHASESHEIGHT);
            case Y_MOONPHASESHEIGHT : y += widths.get(DIM.Y_MONTHLINEHEIGHT);
            case Y_MONTHLINEHEIGHT2 : y += widths.get(DIM.Y_WEEKDAYSHEIGHT);
            case Y_WEEKDAYSHEIGHT : y += widths.get(DIM.Y_MONTHLINEHEIGHT);
            case Y_MONTHLINEHEIGHT : y += widths.get(DIM.Y_FEASTSHEIGHT);
            case Y_FEASTSHEIGHT : y += widths.get(DIM.Y_MARGIN); break;
            default : ;
        }
        return y;
    }
    
    public static String formatDateTimes(GregorianCalendar calUTC, String simpleDateFormat) {
    	SimpleDateFormat sdf = new SimpleDateFormat(simpleDateFormat);
    	sdf.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        return sdf.format(new Date(calUTC.getTimeInMillis()));
    }
    
    public static GregorianCalendar fromUTCtoTz(GregorianCalendar utcCalendar, String tzID) {
    	GregorianCalendar tzc = ICalculator.getCalendar(tzID);
    	tzc.setTimeInMillis(utcCalendar.getTimeInMillis());
		return tzc;
    }
    
    public static GregorianCalendar calendarAsUTCCalendar(GregorianCalendar c) {
    	GregorianCalendar utcc = ICalculator.getCalendar(ICalculator.UTC_TZ_ID);
    	utcc.set(
    			c.get(Calendar.YEAR), 
    			c.get(Calendar.MONTH), 
    			c.get(Calendar.DATE), 
    			c.get(Calendar.HOUR_OF_DAY), 
    			c.get(Calendar.MINUTE), 
    			c.get(Calendar.SECOND));
    	return utcc;
    }
    
    private void populateEvents() {
        int monthIndex;
    	for(ICalEvent event : iCalc.iCal.vEvent) {
    	    
    	    GregorianCalendar timeZoned = event.allDayEvent 
    	            ? (GregorianCalendar) event.properties.get(ICalendar.Keys.EVENT_START).value
    	            :  fromUTCtoTz( (GregorianCalendar) event.properties.get(ICalendar.Keys.EVENT_START).value, iCalc.inputData.getTimezone());
            monthIndex = generateMonthIndex(timeZoned);
            SirvidMonth sM = months.get(monthIndex-beginMonth);
            if(sM == null) { continue; }
            SirvidDay sD = sM.days.get(timeZoned.get(Calendar.DATE)-1);
            if(sD == null) { continue; }
    	    
    		if(event.allDayEvent) {
    		    
    		    initFeastRune(event, sD);
    		    
    		} else {
    		    
    			GregorianCalendar tzdAsUTC = calendarAsUTCCalendar(timeZoned);
    			if(isSolstice(event.dbID)) {
    			    initFeastRune(event, sD);
    			    sM.solstice = tzdAsUTC;
    			}
    			else if(ICalculator.DbIdStatuses.SUNRISE.getDbId() == event.dbID) { sD.sunrise = tzdAsUTC; }
				else if(ICalculator.DbIdStatuses.SUNSET.getDbId() == event.dbID) { sD.sunset = tzdAsUTC; } 
				else { 
					sD.moonphase = tzdAsUTC;
					sD.moonphaseID = event.dbID;
				}
    		}
    	}
    }
    
    private void initFeastRune(ICalEvent event, SirvidDay sirvidDay) {
        
        boolean runeExists = false;
        int runeID = ICalculator.DbIdStatuses.UNDEFINED.getDbId();
        
        try {
            ResultSet eventR = iCalc.CalendarDAO.getRange(event.dbID, event.dbID, DaoKalenderJDBCSqlite.DbTables.EVENTS, null);
            if(eventR != null) {
                while(eventR.next()) {
                    runeID = eventR.getInt("rune_id");
                    if(runeID == 0) { break; }
                    SirvidRune sR = SirvidSVG.runes.get(new Integer(runeID));
                    if(sR != null) {
                        runeExists = ICalculator.isNotEmptyStr(sR.getSvgContent());
                    } else {
                        ResultSet feastRune = iCalc.CalendarDAO.getRange(runeID, runeID, DaoKalenderJDBCSqlite.DbTables.RUNES, null);
                        if(feastRune != null) {
                            while(feastRune.next()) {
                                sR = new SirvidRune(feastRune);
                                runes.put(new Integer(runeID), (SirvidRune) sR);
                                runeExists = ICalculator.isNotEmptyStr(sR.getSvgContent());
                                eventsVsRunes.put(event.dbID, runeID);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        catch(Exception e) {
            errorMsgs.add("SVG feastrune init failed : " + e.getMessage());
        }
        
        if(runeExists) { 
            sirvidDay.feasts.add(new SirvidFeast(event)); 
            eventsVsRunes.put(event.dbID, runeID);
        }
    }
    
    private void prepareEvents() {
        
        GregorianCalendar date0 = getSirvBeginning(iCalc.calendarBegin);
        GregorianCalendar date1 = SirvidMonth.getEndOfMonth(calendarAsUTCCalendar(iCalc.calendarEnd));
        boolean proceed = true;
        int monthIndex;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfDate"));
        dateFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        SimpleDateFormat monthFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfMonth"));
        monthFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        SimpleDateFormat timeFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfTime"));
        timeFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        SimpleDateFormat datetimeFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfDate") + " " + SirvidSVG.props.getProperty("sdfTime"));
        dateFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        
        do {
            proceed = true;
            monthIndex = generateMonthIndex(date0);
            SirvidMonth sM = months.get(monthIndex-beginMonth);
            if(sM == null) { proceed = false; }
            
            if(proceed) {
                SirvidDay sD = sM.days.get(date0.get(Calendar.DATE)-1);
                if(sD == null) { proceed = false; }
                
                if(proceed) {
                    
                    // feasts
                    if(date0.get(Calendar.MONTH) == Calendar.DECEMBER && date0.get(Calendar.DATE) == 21) {
                        
                        StringBuilder yuleTitle = new StringBuilder();
                        yuleTitle.append(dateFormat.format(new Date(date0.getTimeInMillis())));
                        yuleTitle.append(" - ");
                        yuleTitle.append(dateFormat.format(new Date(sM.days.get(23).date.getTimeInMillis())));
                        yuleTitle.append("\n");
                        for(SirvidFeast yulEvent : sD.feasts ) {
                            if(yulEvent.event.dbID == J6ULUD) {
                                yuleTitle.append(yulEvent.event.properties.get(ICalendar.Keys.SUMMARY).value);
                                break;
                            }
                        }
                        // delete December's solstice
                        if(sM.solstice != null) {
                            SirvidDay solsticeDay = sM.days.get(sM.solstice.get(Calendar.DATE)-1);
                            if(solsticeDay != null) {
                                for (int i = 0; i < solsticeDay.feasts.size(); i++) {
                                    if(isSolstice(solsticeDay.feasts.get(i).event.dbID)) {
                                        yuleTitle.append("\n");
                                        yuleTitle.append(datetimeFormat.format(new Date(sM.solstice.getTimeInMillis())));
                                        yuleTitle.append(" ");
                                        yuleTitle.append(solsticeDay.feasts.get(i).event.properties.get(ICalendar.Keys.SUMMARY).value);
                                        solsticeDay.feasts.remove(i);
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if(sD.feasts.size()==1) { sD.feasts.get(0).label = yuleTitle; }
                        
                        // create Yule rune
                        StringBuilder yuleRune = new StringBuilder();
                        int paddingX = (getRoot(sM.days, 25) - getRoot(sM.days, 24)) >> 1;
                        if(paddingX < widths.get(DIM.X_WEEKDAYPADDING) -1) { paddingX = widths.get(DIM.X_WEEKDAYPADDING) -1; }
                        int beginX1221 = getRoot(sM.days, 21);
                        double zoomRatio = widths.get(DIM.Y_FEASTSHEIGHT).doubleValue() / widths.get(DIM.Y_WEEKDAYSHEIGHT).doubleValue();
                        int x = Integer.MIN_VALUE;
                        int x1221 = Integer.MIN_VALUE;
                        for(int d = 21; d < 25; d++) {
                            x = (int)((paddingX - beginX1221 + getRoot(sM.days, d)) / zoomRatio);
                            yuleRune.append("\n");
                            yuleRune.append(generateLine(x, 100, x, 200));
                            if(x1221 == Integer.MIN_VALUE) { x1221 = x; }
                        }
                        int endX = x + (int)(paddingX/zoomRatio);
                        int yRadius = 150;
                        yuleRune.append("\n<path d=\"M0 ");
                        yuleRune.append(yRadius);
                        yuleRune.append(" L");
                        yuleRune.append(endX);
                        yuleRune.append(" ");
                        yuleRune.append(yRadius);
                        yuleRune.append(" A");
                        yuleRune.append(endX >> 1);
                        yuleRune.append(",");
                        yuleRune.append(yRadius);
                        yuleRune.append(" 0 0,0 0,");
                        yuleRune.append(yRadius);
                        yuleRune.append("\"/>\n");
                        SirvidRune sR;
                        try {
                            sR = new SirvidRune(x1221, null, endX);
                            sR.setSvgContent(yuleRune.toString());
                            runes.put(J6ULUD, sR);
                            eventsVsRunes.put(J6ULUD, J6ULUD);
                        } catch(Exception e) { }
                        
                    } else { // non-yules
                        
                        for(SirvidFeast feast : sD.feasts ) {
                            feast.label.append(dateFormat.format(new Date( ((GregorianCalendar)feast.event.properties.get(ICalendar.Keys.EVENT_START).value).getTimeInMillis() )));
                            if(!feast.event.allDayEvent) {
                                feast.label.append(" ");
                                feast.label.append(timeFormat.format(new Date( ((GregorianCalendar)feast.event.properties.get(ICalendar.Keys.EVENT_START).value).getTimeInMillis() )));
                            }
                            feast.label.append("\n");
                            feast.label.append(feast.event.properties.get(ICalendar.Keys.SUMMARY).value);
                        }
                        
                    }
                    
                    // weekday labels
                    sD.weekdayLabel.append(dateFormat.format(new Date(sD.date.getTimeInMillis())));
                    sD.weekdayLabel.append("\n");
                    sD.weekdayLabel.append(commonLabels.get(sD.weekDay)[0]);
                    if(sD.sunrise != null) {
                        sD.weekdayLabel.append("\n");
                        sD.weekdayLabel.append(timeFormat.format(new Date(sD.sunrise.getTimeInMillis())));
                        sD.weekdayLabel.append(" ");
                        sD.weekdayLabel.append(commonLabels.get(ICalculator.DbIdStatuses.SUNRISE.getDbId())[0]);
                    }
                    if(sD.sunset != null) {
                        sD.weekdayLabel.append("\n");
                        sD.weekdayLabel.append(timeFormat.format(new Date(sD.sunset.getTimeInMillis())));
                        sD.weekdayLabel.append(" ");
                        sD.weekdayLabel.append(commonLabels.get(ICalculator.DbIdStatuses.SUNSET.getDbId())[0]);
                    }
                    
                    // moonphase labels
                    if(sD.moonphase != null ) {
                        sD.moonphaseLabel.append(dateFormat.format(new Date(sD.moonphase.getTimeInMillis())));
                        sD.moonphaseLabel.append(" ");
                        sD.moonphaseLabel.append(timeFormat.format(new Date(sD.moonphase.getTimeInMillis())));
                        sD.moonphaseLabel.append("\n");
                        sD.moonphaseLabel.append(SirvidSVG.commonLabels.get(sD.moonphaseID)[0]);
                        if(ICalculator.isNotEmptyStr(SirvidSVG.commonLabels.get(sD.moonphaseID)[1])){
                            sD.moonphaseLabel.append("\n");
                            sD.moonphaseLabel.append(SirvidSVG.commonLabels.get(sD.moonphaseID)[1]);
                        }
                    }
                    
                    // determine rotation order
                    if(sD.feasts.size() == 2) {
                        boolean notKihlakud = true;
                        for(int i=0; i<2; i++) {
                            if(isKihlakud(sD.feasts.get(i).event.dbID)) {
                                notKihlakud = false;
                                sD.feasts.get((i+1)%2).rotate = sD.feasts.get(i).event.dbID == LIUGUP2EV ? -45 : 45;
                            }
                        }
                        if(notKihlakud) {
                            int rot = getRuneByDbID(sD.feasts.get(0).event.dbID).getRightness() > getRuneByDbID(sD.feasts.get(1).event.dbID).getRightness() ? 0 : 1;
                            sD.feasts.get(rot%2).rotate = -45;
                            sD.feasts.get((1 + rot)%2).rotate = 45;
                        }
                    }
                }
            }
            
            date0.add(Calendar.DATE, 1);
        } while (date0.before(date1));
    }
    
    private int getRoot(ArrayList<SirvidDay> monthDays, int dayNo) {
        return monthDays.get(dayNo-1).beginX + runes.get(monthDays.get(dayNo-1).weekDay).getCx();
    }
    
    public SirvidRune getRuneByDbID(int dbID) {
        return runes.get(eventsVsRunes.get(dbID));
    }
    
    private GregorianCalendar getSirvBeginning(GregorianCalendar x) {
        GregorianCalendar y = calendarAsUTCCalendar(x);
        y.set(Calendar.DAY_OF_MONTH, 1);
        return y;
    }
    /**
     * Dummy runes for testing or if anything goes wrong
     */
    private void dummyRunes() {
        
        boolean emptyRunes = runes.isEmpty();
        boolean emptyCommonLabels = commonLabels.isEmpty();
        
        if(emptyRunes || emptyCommonLabels) {
            int[] dummys = {0, 7, ICalculator.DbIdStatuses.MOON_NEW_M2.getDbId(), ICalculator.DbIdStatuses.MOON_LAST.getDbId()+1};
            ICalculator.DbIdStatuses[] dbIDs = ICalculator.DbIdStatuses.values();
            for(int i = 0; i < dummys.length; i+=2){
                for(int j = dummys[i];  j < dummys[i+1]; j++) {
                    String rTxt = (i == 0) ? weekDays[j] : dbIDs[j - 9].getName();
                    try {
                        if(emptyRunes) {
                            SirvidRune sR = new SirvidRune(0, null, 120);
                            sR.setFilename("dummy" + j + ".svg");
                            sR.setSvgContent(errorTxtTags[0] + rTxt + errorTxtTags[1]);
                            runes.put(new Integer(j), sR);
                        }
                        if(emptyCommonLabels) {
                            commonLabels.put(new Integer(j), new String[] {rTxt, null } );
                        }
                    }
                    catch(Exception e) {
                        errorMsgs.add("SVG dummy runes init failed : " + e.getMessage());
                    }
                    
                }
            }
        }
        
        if(emptyCommonLabels) {
            ICalculator.DbIdStatuses[] other = {ICalculator.DbIdStatuses.SUNRISE, ICalculator.DbIdStatuses.SUNSET};
            for(ICalculator.DbIdStatuses dbID : other) { commonLabels.put(new Integer(dbID.getDbId()), new String[] {dbID.getName(), null } ); }
        }
    }
    
    public static boolean isSolstice(int dbID) {
        return dbID == ICalculator.DbIdStatuses.SOLSTICE.getDbId()
                || dbID == ICalculator.DbIdStatuses.SOLSTICE1.getDbId()
                || dbID == ICalculator.DbIdStatuses.SOLSTICE2.getDbId()
                || dbID == ICalculator.DbIdStatuses.SOLSTICE3.getDbId()
                || dbID == ICalculator.DbIdStatuses.SOLSTICE4.getDbId();
    }
    
    public static boolean isKihlakud(int dbID) {
        return dbID == LIUGUP2EV || dbID == TUHKAP2EV;
    }
    
    public static String generateLine(double x1, double y1, double x2, double y2) {
        StringBuilder sb = new StringBuilder();
        sb.append("<line x1=\"");
        sb.append(x1);
        sb.append("\" y1=\"");
        sb.append(y1);
        sb.append("\" x2=\"");
        sb.append(x2);
        sb.append("\" y2=\"");
        sb.append(y2);
        sb.append("\"/>");
        return sb.toString();
    }
}
