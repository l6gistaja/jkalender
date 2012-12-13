package ee.alkohol.juks.sirvid.containers.graphics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import ee.alkohol.juks.sirvid.containers.DaoKalenderJDBCSqlite;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public class SirvidSVG {
    
    public static final String dataPath = "sirvid/";
    
    public static Properties props = new Properties();
    public static HashMap<Integer,SirvidRune> runes = new HashMap<Integer,SirvidRune>();
    public static String[] weekDays = {"P","E","T","K","N","R","L"};
    public ArrayList<ArrayList<SirvidDay>> days = new ArrayList<ArrayList<SirvidDay>>();
    
    private ICalculator iCalc;
    public ArrayList<String> errorMsgs = new ArrayList<String>();
    
    
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
        
        GregorianCalendar m0 = ICalculator.getCalendar(ICalculator.UTC_TZ_ID);
        m0.setTime(iC.calendarBegin.getTime());
        m0.set(Calendar.DATE, 1);
        GregorianCalendar m1 = ICalculator.getCalendar(ICalculator.UTC_TZ_ID);
        m1.setTime(iC.calendarEnd.getTime());
        m1.set(Calendar.DATE, iC.calendarEnd.getActualMaximum(Calendar.DATE));
        
        System.out.println(weekDays[0]);
    }
    
    
}
