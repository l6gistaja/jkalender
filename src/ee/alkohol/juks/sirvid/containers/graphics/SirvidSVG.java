package ee.alkohol.juks.sirvid.containers.graphics;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import ee.alkohol.juks.sirvid.containers.DaoKalenderJDBCSqlite;
import ee.alkohol.juks.sirvid.containers.PropertiesT;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public class SirvidSVG {
    
    public static final String dataPath = "sirvid/";
    public static final String[] errorTxtTags = { "<text x=\"10\" y=\"10\" fill=\"red\">", "</text>" };
    public static final class PropKeys {
        public static final String WEEKDAYPADDING = "weekdayPaddingX";
    }
    
    public static PropertiesT props = new PropertiesT();
    public static HashMap<Integer,SirvidRune> runes = new HashMap<Integer,SirvidRune>();
    public static String[] weekDays = {"P","E","T","K","N","R","L"};
    public ArrayList<SirvidMonth> months = new ArrayList<SirvidMonth>();
    
    private ICalculator iCalc;
    public ArrayList<String> errorMsgs = new ArrayList<String>();
    public int beginMonth;
    
    
    public SirvidSVG(ICalculator iC) {
        
        iCalc = iC;
        
        if(props.isEmpty()) {
        	try {
                props.load(this.getClass().getClassLoader().getResourceAsStream(dataPath + "svg_export.properties"));
            }
            catch(Exception e) {
                errorMsgs.add("Failed to open svg_export.properties : " + e.getMessage());
            }
        }
        
        DaoKalenderJDBCSqlite CalendarDAO = new DaoKalenderJDBCSqlite(iCalc.inputData.jbdcConnect);
        
        if(CalendarDAO.isConnected()) {   
        	if(runes.isEmpty()) {
        		ResultSet commonRunes = CalendarDAO.getRange(0, ICalculator.DbIdStatuses.MOON_LAST.getDbId(), DaoKalenderJDBCSqlite.DbTables.RUNES, null);
                if(commonRunes != null) {
                    try {
                        while(commonRunes.next()) { 
                            runes.put(new Integer(commonRunes.getInt(DaoKalenderJDBCSqlite.DbTables.RUNES.getPK())),
                                    new SirvidRune(commonRunes));
                        }
                    }
                    catch(Exception e) {
                        errorMsgs.add("SVG runes init failed : " + e.getMessage());
                    }
                }
        	}
        }
        
    	// dummy runes for testing or if anything goes wrong
    	if(runes.isEmpty()) {
    	    int[] dummys = {0, 7, ICalculator.DbIdStatuses.MOON_NEW_M2.getDbId(), ICalculator.DbIdStatuses.MOON_LAST.getDbId()+1};
    	    ICalculator.DbIdStatuses[] dbIDs = ICalculator.DbIdStatuses.values();
    	    for(int i = 0; i < dummys.length; i+=2){
    	        for(int j = dummys[i];  j < dummys[i+1]; j++) {
    	            String rTxt = (i == 0) ? weekDays[j] : dbIDs[j - 9].getName();
                    try {
                        SirvidRune sR = new SirvidRune(0, null, 100);
                        sR.setFilename("dummy" + j + ".svg");
                        sR.setSvgContent(errorTxtTags[0] + rTxt + errorTxtTags[1]);
                        runes.put(new Integer(j), sR);
                    }
                    catch(Exception e) {
                        errorMsgs.add("SVG dummy runes init failed : " + e.getMessage());
                    }
    	            
    	        }
    	    }
    	}
        	
        if(CalendarDAO.isConnected()) {
            if(weekDays[0].equals("P")) {
                ResultSet wd = CalendarDAO.getRange(0, 6, DaoKalenderJDBCSqlite.DbTables.EVENTS, null);
                if(wd != null) {
                    try {
                        while(wd.next()) {
                            weekDays[wd.getInt(DaoKalenderJDBCSqlite.DbTables.EVENTS.getPK())] = 
                                wd.getString(DaoKalenderJDBCSqlite.DbTables.EVENTS.getTitle());
                        }
                    }
                    catch(Exception e) {
                        //errorMsgs.add("SVG weekdays init failed : " + e.getMessage());
                    }
                }
                
            }
            
        } 
        
        // first day of beginning month
        GregorianCalendar date0 = ICalculator.getCalendar(iC.inputData.getTimezone());
        date0.setTime(iC.calendarBegin.getTime());
        date0.set(Calendar.DAY_OF_MONTH, 1);
        
        beginMonth = generateMonthIndex(date0);
        
        // last day of ending month
        GregorianCalendar date1 = SirvidMonth.getEndOfMonth(iC.calendarEnd);

        //System.out.println(iC.calendarBegin.getTime().toString() + " ... " + iC.calendarEnd.getTime().toString());
        //System.out.println(date0.getTime().toString() + " ... " + date1.getTime().toString());
                
        // init and populate sirvi-containers
        do {
            months.add(new SirvidMonth(date0));
            date0.add(Calendar.MONTH, 1);
        } while (date0.before(date1));
        
    }
    
    private static int generateMonthIndex(GregorianCalendar month) {
        return 12*month.get(Calendar.YEAR) + month.get(Calendar.MONTH);
    }
    
}
