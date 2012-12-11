package ee.alkohol.juks.sirvid.containers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import ee.alkohol.juks.sirvid.containers.ICalendar.*;
import ee.alkohol.juks.sirvid.containers.ICalEvent;
import ee.alkohol.juks.sirvid.exporters.ExporterICalendar;
import ee.alkohol.juks.sirvid.exporters.ical.*;
import ee.alkohol.juks.sirvid.math.Astronomy;

public class ICalculator {
    
    public final static String[] LANG = {Keys.LANGUAGE, "ET"};
    public final static String UTC_TZ_ID = "GMT";
    
    public static final class FIELDS {
        public static final String ID = "id";
        public static final String TITLE = "event";
        public static final String DESCRIPTION = "more";
    }
    
    public static enum DbIdStatuses {
        
        UNDEFINED (-1, ""),
        MOON_NEW_M2 (10, "\u25cf -2d"),
        MOON_NEW (11, "\u25cf"),
        MOON_NEW_P2 (12, "\u25cf +2d"),
        MOON_1ST (13, "\u263d"),
        MOON_FULL_M2 (14, "\u25ef -2d"),
        MOON_FULL (15, "\u25ef"),
        MOON_FULL_P2 (16, "\u25ef +2d"),
        MOON_LAST (17, "\u263e"),
        SOLSTICE (20, "\u2295"),
        SUNRISE (30, "\u263c"),
        SUNSET (31, "\u2600"),
        LEAPDAY (229, "+"),
        EASTER (2000, "*");

        private int dbId;
        private String name;
        
        public int getDbId() {
            return dbId;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        
        DbIdStatuses(int dbId, String name) {
            this.dbId = dbId;
            this.name = name;
        }

    }
    
    public static final DbIdStatuses[] MOONPHASES = {DbIdStatuses.MOON_NEW, DbIdStatuses.MOON_1ST, DbIdStatuses.MOON_FULL, DbIdStatuses.MOON_LAST};
    public static final DbIdStatuses[] MOONPHASES_MV = {DbIdStatuses.MOON_NEW_M2, DbIdStatuses.MOON_NEW_P2, DbIdStatuses.MOON_FULL_M2, DbIdStatuses.MOON_FULL_P2};
    
    public InputData inputData;
    public ICalendar iCal;
    public ExporterICalendar exporter;
    public ArrayList<String> errorMsgs;
    public String timespan;
    public GregorianCalendar gregorianEaster;
    public DaoKalenderJDBCSqlite CalendarDAO;
    
    public ICalculator(InputData inputData) throws SQLException {
        
        this.inputData = inputData;
        Date t0 = new Date();
        
        // initialize current time
        String tzID = this.inputData.getTimezone();
        GregorianCalendar cal = getCalendar(tzID);
        cal.setTime(inputData.getDate());
        goodNight(cal);
        boolean isLeapYear = cal.isLeapYear(cal.get(Calendar.YEAR));
        
        // initialize period of calculation
        GregorianCalendar calendarBegin = getCalendar(tzID);
        GregorianCalendar calendarEnd = getCalendar(tzID);
        StringBuilder calName = new StringBuilder();
        calName.append(cal.get(Calendar.YEAR));
        if(inputData.getTimespan().equals(InputData.FLAGS.PERIOD.MONTH)
        		|| inputData.getTimespan().equals(InputData.FLAGS.PERIOD.DAY)) {
        	
		        	calName.append(String.format("-%02d", cal.get(Calendar.MONTH)+1));
		        	if(inputData.getTimespan().equals(InputData.FLAGS.PERIOD.DAY)) { // day
		            	calName.append(String.format("-%02d", cal.get(Calendar.DATE)));
		            	calendarBegin.setTime(inputData.getDate());
		            	calendarEnd.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), 23, 59, 59);
		            } else { // month
		            	calendarBegin.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1);
		            	calendarEnd.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
		            }
		        	
        } else { // year
        	calendarBegin.set(cal.get(Calendar.YEAR), 0, 1);
        	calendarEnd.set(cal.get(Calendar.YEAR), 11, 31, 23, 59, 59);
        }
        
        goodNight(calendarBegin);
        this.timespan = calName.toString();
        
        // initialize calendar container
        LinkedHashMap<String,ICalProperty> initData = new LinkedHashMap<String,ICalProperty>();
        initData.put(Keys.CALENDAR_NAME, new ICalProperty("JKalender " + this.timespan, null));
        initData.put(Keys.CALENDAR_TIMEZONE, new ICalProperty(inputData.getTimezone(), null));
        iCal = new ICalendar(initData);
        
        // initialize DB connection
        CalendarDAO = new DaoKalenderJDBCSqlite(inputData.jbdcConnect);
        
        HashMap<String,String> eventTranslations = new HashMap<String,String> ();
        if(CalendarDAO.dbConnection != null) {
            ResultSet eventTrRS = CalendarDAO.getAstronomicalEventTranslations();
            while(eventTrRS.next()) { 
            	eventTranslations.put(eventTrRS.getString("dbid"), eventTrRS.getString("name"));
            }
        }
        for (DbIdStatuses dbids : DbIdStatuses.values()) {
            String dbKey = "" + dbids.getDbId();
            if(eventTranslations.get(dbKey) == null) { eventTranslations.put(dbKey, dbids.getName()); }
        }
        
        // sunsets and sunrises
        if(inputData.isCalculateSunrisesSunsets()) {
            
        	GregorianCalendar currentDay = getCalendar(tzID);
        	currentDay.setTime(calendarBegin.getTime());
        	goodNight(currentDay);
        	
        	String coordinates = "" + inputData.getLatitude() + ";" + inputData.getLongitude();
        	String[] driverData = {
                    Astronomy.Keys.J_RISE, eventTranslations.get("" +DbIdStatuses.SUNRISE.getDbId()),
                    Astronomy.Keys.J_SET, eventTranslations.get("" +DbIdStatuses.SUNSET.getDbId())
            };
        	
        	iCal.vVenue = new LinkedHashMap<String,ICalProperty>();
        	iCal.vVenue.put(Keys.UID, new ICalProperty("a_" + inputData.getLatitude()+ "_o_" + inputData.getLongitude() + iCal.generateUID(null), null));
        	iCal.vVenue.put(Keys.GEOGRAPHIC_COORDINATES, new ICalProperty(coordinates, null));
        	
        	while(true) {
                
                HashMap<String,Double> results = Astronomy.gregorianSunrise(
                        Astronomy.gregorian2JDN(currentDay.get(Calendar.YEAR), currentDay.get(Calendar.MONTH)+1, currentDay.get(Calendar.DATE)), 
                        -inputData.getLongitude(), inputData.getLatitude()
                );
                
                for(int i=0; i < driverData.length; i+=2) {
                	
                    int[] sg = Astronomy.JD2calendarDate(results.get(driverData[i]));
                    GregorianCalendar sunCal = getCalendar(UTC_TZ_ID);
                    sunCal.set(sg[0], sg[1]-1, sg[2], sg[3], sg[4], sg[5]);
                    
                    ICalEvent event = new ICalEvent();
                    event.dbID = driverData[i].equals(Astronomy.Keys.J_RISE) 
                            ? DbIdStatuses.SUNRISE.getDbId()
                            : DbIdStatuses.SUNSET.getDbId();
                    event.properties.put(Keys.SUMMARY, new ICalProperty(driverData[i+1], getLanguageDescriptor()));
                    event.properties.put(Keys.UID, 
                            new ICalProperty("d_"
                            		+ String.format("%d%02d%02d", sg[0], sg[1], sg[2])
                            		+ "_" +iCal.generateUID(event.dbID), null));
                    event.properties.put(Keys.EVENT_START, new ICalProperty(sunCal.getTime(), new String[]{Keys.VALUE, Values.DATETIME}));
                    // use VVENUE component instead
                    //event.properties.put(Keys.GEOGRAPHIC_COORDINATES, new ICalProperty(coordinates, null));
                    event.allDayEvent = false;
                    iCal.vEvent.add(event);
                }
                
                currentDay.add(Calendar.DATE, 1);
                if(currentDay.after(calendarEnd)) { break; }
        	}
        	
        }
        
        // solstices
        if(inputData.isCalculateSolistices()) {
        	ArrayList<int[]> sol = new ArrayList<int[]>();
        	if(inputData.getTimespan().equals(InputData.FLAGS.PERIOD.YEAR)) {
        		for(short m = 3; m < 13; m+=3) {
        			sol.add(Astronomy.JD2calendarDate(Astronomy.solstice(cal.get(Calendar.YEAR), m, inputData.isUseDynamicTime())));
        		}
        	} else {
        	    short m = (short)(1 + cal.get(Calendar.MONTH));
        	    if(m > 2 && (m % 3 == 0)) {
        	        sol.add(Astronomy.JD2calendarDate(Astronomy.solstice(cal.get(Calendar.YEAR), m, inputData.isUseDynamicTime())));
        	    }
        	}
        	String solsticeLabel = eventTranslations.get("" +DbIdStatuses.SOLSTICE.getDbId());
        	for(int[] solistice : sol) {
        	    GregorianCalendar solCal = getCalendar(UTC_TZ_ID);
                solCal.set(solistice[0], solistice[1]-1, solistice[2], solistice[3], solistice[4], solistice[5]);
                if(!solCal.before(calendarBegin) && !solCal.after(calendarEnd)) {
                    ICalEvent event = new ICalEvent();
                    event.dbID = DbIdStatuses.SOLSTICE.getDbId();
                    event.properties.put(Keys.SUMMARY, new ICalProperty(solsticeLabel, getLanguageDescriptor()));
                    event.properties.put(Keys.UID, new ICalProperty("m_" + solistice[0] + String.format("%02d",solistice[1]) + "_" + iCal.generateUID(event.dbID), null));
                    event.properties.put(Keys.EVENT_START, new ICalProperty(solCal.getTime(), new String[]{Keys.VALUE, Values.DATETIME}));
                    event.allDayEvent = false;
                    iCal.vEvent.add(event);
                }
        	}
        }
        
        
        // moonphases
        if(inputData.isCalculateMoonphases()) {
            
            long lunStartY = calendarBegin.get(Calendar.YEAR);
            short lunStartM = (short)(calendarBegin.get(Calendar.MONTH) -1);
            short lunF = 0;
            if(lunStartM < 0) {
                lunStartY --;
                lunStartM = 11;
            }
            
            double k  = Astronomy.getLunationNumber(lunStartY, (short)(lunStartM +1), lunF);
            
            GregorianCalendar moonCal = getCalendar(UTC_TZ_ID);
            goodNight(moonCal);
            do {
                
                int[] mfd = Astronomy.JD2calendarDate(Astronomy.moonPhaseK(k + (0.25 * lunF), (short)(lunF % 4), inputData.isUseDynamicTime()));
                moonCal.set(mfd[0], mfd[1]-1, mfd[2], mfd[3], mfd[4], mfd[5]);
                if(!moonCal.before(calendarBegin) && !moonCal.after(calendarEnd)) {
                    ICalEvent event = new ICalEvent();
                    event.dbID = MOONPHASES[lunF % 4].getDbId();
                    event.properties.put(Keys.SUMMARY, new ICalProperty(eventTranslations.get("" +event.dbID), getLanguageDescriptor()));
                    event.properties.put(Keys.UID, new ICalProperty("l_" + (k + (0.25 * lunF)) + "_" + iCal.generateUID(event.dbID), null));
                    event.properties.put(Keys.EVENT_START, new ICalProperty(moonCal.getTime(), new String[]{Keys.VALUE, Values.DATETIME}));
                    event.allDayEvent = false;
                    iCal.vEvent.add(event);
                }
                
                if(inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA) && lunF % 2 == 0) {
                    moonCal.add(Calendar.DATE, -2);
                    for(int i = 0; i < 2; i++) {
                        moonCal.add(Calendar.DATE, i*4);
                        if(!moonCal.before(calendarBegin) && !moonCal.after(calendarEnd)) {
                            ICalEvent event = new ICalEvent();
                            event.dbID = MOONPHASES_MV[(lunF%4) + i].getDbId();
                            event.properties.put(Keys.SUMMARY, new ICalProperty(eventTranslations.get("" +event.dbID), getLanguageDescriptor()));
                            event.properties.put(Keys.UID, new ICalProperty("l_" + (k + (0.25 * lunF)) + "_" + iCal.generateUID(event.dbID), null));
                            event.properties.put(Keys.EVENT_START, new ICalProperty(moonCal.getTime(), new String[]{Keys.VALUE, Values.DATETIME}));
                            event.allDayEvent = false;
                            iCal.vEvent.add(event);
                        }
                    }
                }

                lunF ++;
                
            } while (!moonCal.after(calendarEnd));
            
        }
        
        // gregorian easter
        
        if(inputData.isCalculateGregorianEaster() || !inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.NONE)) {
            
            int year = cal.get(Calendar.YEAR);
            int[] gE = Astronomy.gregorianEaster(year);
            gregorianEaster = getCalendar(tzID);
            gregorianEaster.set(year, gE[0]-1, gE[1]);
            goodNight(gregorianEaster);
            
            if(inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.NONE) && !gregorianEaster.before(calendarBegin) && !gregorianEaster.after(calendarEnd)) {
                ICalEvent event = new ICalEvent();
                event.dbID = DbIdStatuses.EASTER.getDbId();
                event.properties.put(Keys.SUMMARY, new ICalProperty(eventTranslations.get("" +event.dbID), null));
                event.properties.put(Keys.UID, new ICalProperty("y_" + year + "_e_g_" + iCal.generateUID(event.dbID), null));
                event.properties.putAll(generateAllDayEvent(gregorianEaster.get(Calendar.YEAR), gregorianEaster.get(Calendar.MONTH) +1, gregorianEaster.get(Calendar.DATE)));
                iCal.vEvent.add(event);
            }
            
        }
        
        // nothing to do further, if there is no DB connection
        if(CalendarDAO.dbConnection != null) { 
            
            // if calendar dates are required
            if(!inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.NONE)) {
            	
    	        String[] nameFields = inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA)
    	        	? new String[]{"maausk"}
    	        	: new String[]{FIELDS.TITLE,"maausk"};
    	        String nameDelimiter = "; ";
    	        
    	        // anniversaries
    	        ResultSet anniversaries = CalendarDAO.getEvents(
    	                (calendarBegin.get(Calendar.MONTH) + 1)*100 + calendarBegin.get(Calendar.DAY_OF_MONTH), 
    	                (calendarEnd.get(Calendar.MONTH) + 1)*100 + calendarEnd.get(Calendar.DAY_OF_MONTH),
    	        		inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA));
    	        if(anniversaries != null) {
    	            while(anniversaries.next())
    	            {
    	              int dbID = anniversaries.getInt(FIELDS.ID);
    	              if(dbID == DbIdStatuses.LEAPDAY.getDbId() && !isLeapYear) { continue; }
    	              
    	              ICalEvent event = new ICalEvent();
    	              event.dbID = dbID;
    	              
    	              event.properties.put(Keys.SUMMARY, new ICalProperty(generateDayName(anniversaries, nameFields, nameDelimiter), LANG));
    	              event.properties.put(Keys.UID, new ICalProperty(iCal.generateUID(dbID), null));
    	              event.properties.putAll(generateAllDayEvent(cal.get(Calendar.YEAR), (int)(dbID / 100), dbID % 100));
    	              String descr = anniversaries.getString(FIELDS.DESCRIPTION);
    	              if(isNotEmptyStr(descr)) {
    	                  event.properties.put(Keys.DESCRIPTION, new ICalProperty(descr, LANG));
    	              }
    	              // obvious
    	              //if(dbID != DbIdStatuses.LEAPDAY.getDbId()) { event.properties.put("rrule", new ICalProperty("FREQ=YEARLY", null));}
    	              
    	              iCal.vEvent.add(event);
    	            }
    	        }
    	        
    	        // Gregorian Easter related moveable feasts
    	        ResultSet gEasterEvents = CalendarDAO.getEvents(1635, 2365, inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA));
                if(gEasterEvents != null) {
                    while(gEasterEvents.next())
                    {
                      int dbID = gEasterEvents.getInt(FIELDS.ID);
                      GregorianCalendar easterEventDate = getCalendar(tzID);
                      easterEventDate.setTime(gregorianEaster.getTime());
                      goodNight(easterEventDate);
                      easterEventDate.add(Calendar.DATE, dbID - DbIdStatuses.EASTER.getDbId());
                      
                      if(!easterEventDate.before(calendarBegin) && !easterEventDate.after(calendarEnd)) {
                          ICalEvent event = new ICalEvent();
                          event.dbID = dbID;
                          event.properties.put(Keys.SUMMARY, new ICalProperty(generateDayName(gEasterEvents, nameFields, nameDelimiter), LANG));
                          event.properties.put(Keys.UID, new ICalProperty("y_" + easterEventDate.get(Calendar.YEAR) + "_e_g_" + iCal.generateUID(event.dbID), null));
                          event.properties.putAll(generateAllDayEvent(easterEventDate.get(Calendar.YEAR), easterEventDate.get(Calendar.MONTH) +1, easterEventDate.get(Calendar.DATE)));
                          String descr = gEasterEvents.getString(FIELDS.DESCRIPTION);
                          if(isNotEmptyStr(descr)) {
                              event.properties.put(Keys.DESCRIPTION, new ICalProperty(descr, LANG));
                          }
                          iCal.vEvent.add(event);
                      }

                    }
                }
                
                // moveables
                ResultSet weekdayNths = CalendarDAO.getEvents(10110, 11256, inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA));
                if(weekdayNths != null) {
                    while(weekdayNths.next())
                    {
                      int dbID = weekdayNths.getInt(FIELDS.ID);
                      int week = (int)(dbID / 10) % 10;
                      
                      GregorianCalendar weekdayNthDate = getCalendar(tzID);
                      goodNight(weekdayNthDate);
                      weekdayNthDate.set(Calendar.YEAR,cal.get(Calendar.YEAR));
                      weekdayNthDate.set(Calendar.MONTH, (int)(dbID / 100) % 100 - 1);
                      weekdayNthDate.set(Calendar.DAY_OF_WEEK, dbID % 10 + 1);
                      weekdayNthDate.set(Calendar.DAY_OF_WEEK_IN_MONTH, week > 4 ? -1 : week);
                      
                      if(!weekdayNthDate.before(calendarBegin) && !weekdayNthDate.after(calendarEnd)) {
                          ICalEvent event = new ICalEvent();
                          event.dbID = dbID;
                          event.properties.put(Keys.SUMMARY, new ICalProperty(generateDayName(weekdayNths, nameFields, nameDelimiter), LANG));
                          event.properties.put(Keys.UID, new ICalProperty("y_" + weekdayNthDate.get(Calendar.YEAR) + "_" + iCal.generateUID(event.dbID), null));
                          event.properties.putAll(generateAllDayEvent(weekdayNthDate.get(Calendar.YEAR), weekdayNthDate.get(Calendar.MONTH) +1, weekdayNthDate.get(Calendar.DATE)));
                          String descr = weekdayNths.getString(FIELDS.DESCRIPTION);
                          if(isNotEmptyStr(descr)) {
                              event.properties.put(Keys.DESCRIPTION, new ICalProperty(descr, LANG));
                          }
                          iCal.vEvent.add(event);
                      }

                    }
                }
                
                // advents
                ResultSet advents = CalendarDAO.getEvents(3010, 3536, inputData.getCalendarData().equals(InputData.FLAGS.CALDATA.MAAVALLA));
                if(advents != null) {
                    
                    GregorianCalendar xmasDay = getCalendar(tzID);
                    xmasDay.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                    xmasDay.set(Calendar.MONTH, 11);
                    xmasDay.set(Calendar.DATE, 25);
                    goodNight(xmasDay);
                    int xwDay = xmasDay.get(Calendar.DAY_OF_WEEK) - 1;
                    
                    while(advents.next()) {
                        
                        int dbID = advents.getInt(FIELDS.ID);
                        int wDay = dbID % 10;
                        int scrollback = ( wDay < xwDay ) ? xwDay - wDay : 7;
                        scrollback += 7 * ( ((int)(dbID/10) % 100) -1 ) ;
                        
                        GregorianCalendar adventDate = getCalendar(tzID);
                        adventDate.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                        adventDate.set(Calendar.MONTH, 11);
                        adventDate.set(Calendar.DATE, 25);
                        goodNight(adventDate);
                        adventDate.add(Calendar.DATE, - scrollback);
                        
                        if(!adventDate.before(calendarBegin) && !adventDate.after(calendarEnd)) {
                            ICalEvent event = new ICalEvent();
                            event.dbID = dbID;
                            event.properties.put(Keys.SUMMARY, new ICalProperty(generateDayName(advents, nameFields, nameDelimiter), LANG));
                            event.properties.put(Keys.UID, new ICalProperty("y_" + adventDate.get(Calendar.YEAR) + "_" + iCal.generateUID(event.dbID), null));
                            event.properties.putAll(generateAllDayEvent(adventDate.get(Calendar.YEAR), adventDate.get(Calendar.MONTH) +1, adventDate.get(Calendar.DATE)));
                            String descr = advents.getString(FIELDS.DESCRIPTION);
                            if(isNotEmptyStr(descr)) {
                                event.properties.put(Keys.DESCRIPTION, new ICalProperty(descr, LANG));
                            }
                            iCal.vEvent.add(event);
                        }
                    }
                }
                
                
            }
        }
        
        Date t1 = new Date();
        iCal.iCalBody.put(Keys.GENERATION_TIME, new ICalProperty(t1.getTime() - t0.getTime(), null));
        
    }
    
    public void initExport() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        @SuppressWarnings("rawtypes")
        Class clazz = Class.forName("ee.alkohol.juks.sirvid.exporters.Exporter"+inputData.getOutputFormat().toUpperCase());
        exporter = (ExporterICalendar)clazz.newInstance();
    }
    
    // helpers
    
    private String[] getLanguageDescriptor() {
        return CalendarDAO.dbConnection == null ? null : LANG;
    }
    
    public static LinkedHashMap<String,ICalProperty> generateAllDayEvent(int year, int month, int date) {
        LinkedHashMap<String,ICalProperty> y = new LinkedHashMap<String,ICalProperty>();
        GregorianCalendar cal = getCalendar(UTC_TZ_ID);
        cal.set(year, month-1, date, 0, 0, 0);
        y.put(Keys.EVENT_START, new ICalProperty(cal.getTime(), new String[]{Keys.VALUE,Values.DATE}));
        cal.add(Calendar.DATE, 1);
        y.put(Keys.EVENT_END, new ICalProperty(cal.getTime(), new String[]{Keys.VALUE,Values.DATE}));
        return y;
    }
    
    public static boolean isNotEmptyStr(String str) {
        return str != null && !str.trim().equals("");
    }
    
    public static String generateDayName(ResultSet eventRow, String[] nameFields, String nameDelimiter) {
        StringBuilder y = new StringBuilder();
        String value = null;
        for(String name: nameFields) {
            try { value = eventRow.getString(name); }
                catch(SQLException e) { e.printStackTrace(); }
            if(isNotEmptyStr(value)) {
                if(y.length()>0) { y.append(nameDelimiter); }
                y.append(value);
            }
        }
        return y.toString();
    }
    
    public static GregorianCalendar getCalendar(String TzID) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(TzID));
        cal.setGregorianChange(new Date(Long.MIN_VALUE));
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }
    
    public void goodNight(GregorianCalendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
    
}	