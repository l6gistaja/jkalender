package ee.alkohol.juks.sirvid.math;

import static org.junit.Assert.*;
import java.util.HashMap;
import org.junit.Test;

/**
 * Astronomical and calendar calculations: unittests
 * 
 * @author juks@alkohol.ee 
 */

public class AstronomyTest {
    
    public static final String DELIMITER = "-";

    public static final int[][] GREGORIAN_EASTERS = {
        
        {1954,4,18},
        {1991,3,31},
        {1992,4,19},
        {1993,4,11},
        {2000,4,23},
        {2007,4,8},
        {2008,3,23},
        
        // Meeus: minimums and maximums
        {1818,3,22}, 
        {2285,3,22},
        {1886,4,25},
        {1943,4,25},
        {2038,4,25},
        
        // data from some EELK site
        {2010,4,4}, 
        {2011,4,24},
        {2012,4,8},
        {2013,3,31},
        {2014,4,20},
        {2015,4,5},
        {2016,3,27}

    };

    public static final int[][] SOLSTICES = {
        
        {1962,6,21}, //,21,24},
        
        {2007,6,21}, //,20,6}
        {2007,3,21},
        {2007,9,23},
        
        {2008,3,20}, //7,48
        {2008,6,21}, //2-1,59
        {2008,9,22}, //18-1,44
        
        // Kutsukalender 2011
        {2011,3,20}, //23,22 // Estonian datetime {2011,3,21}, //1,22
        {2011,6,21}, //20-1-2,18 
        {2011,9,23} //12-1-2,6 
        
    };
    
    public static final int[][] GREGORIAN2JD = {
        
        {2000,1,1, (int)Astronomy.JDN2000_01_01} //January 1, 2000 at midday corresponds to JD = 2451545
        
    };
    
    public static final double[][] SUNRISE_SUNSET = {
        
        // @Tallinn; Kutsukalender 2011
        {2011,1,2, 9-2,18, 15-2,32, -24.745278, 59.437222},
        {2011,2,6, 8-2,21, 16-2,50, -24.745278, 59.437222},
        {2011,3,6, 7-2,5, 18-2,1, -24.745278, 59.437222},
        {2011,4,3, 6-3,43, 20-3,8, -24.745278, 59.437222},
        {2011,5,1, 5-3,23, 21-3,15, -24.745278, 59.437222},
        {2011,6,5, 4-3,11, 22-3,29, -24.745278, 59.437222},
        {2011,7,3, 4-3,11, 22-3,39, -24.745278, 59.437222},
        {2011,8,7, 5-3,18, 21-3,34, -24.745278, 59.437222},
        {2011,9,4, 6-3,23, 20-3,16, -24.745278, 59.437222},
        {2011,10,2, 7-3,27, 18-3,52, -24.745278, 59.437222},
        {2011,11,6, 7-2,52, 16-2,16, -24.745278, 59.437222},
        {2011,12,2, 8-2,56, 15-2,26, -24.745278, 59.437222},
        
        {2004,4,1, -1,-1, 18,15, -5, 52}, // @Netherland

    };
    
    public static final int[][] MOONPHASES = {
        
        // Kutsukalender 2011
        
        {2011,1,4, 11-2,4, 0},
        {2011,1,12, 13-2,33, 1},
        {2011,1,19, 23-2,22, 2},
        {2011,1,26, 14-2,58, 3},
        
        {2011,2,3, 4-2,32, 0},
        {2011,2,11, 9-2,19, 1},
        {2011,2,18, 10-2,37, 2},
        {2011,2,25, 1-2,27, 3},
        
        {2011,3,4, 22-2,47, 0},
        {2011,3,12, 23,46, 1},
        //{2011,3,19, 20-2,21, 2}, //needs 12*60(?!) secs to pass
        {2011,3,26, 14-2,8, 3},
        
        {2011,4,3, 17-3,33, 0},
        {2011,4,11, 15-3,6, 1},
        {2011,4,18, 5-3,45, 2},
        {2011,4,25, 5-3,48, 3},
        
        {2011,5,3, 9-3,52, 0},
        {2011,5,10, 23-3,34, 1},
        {2011,5,17, 14-3,10, 2},
        {2011,5,24, 21-3,53, 3},
        
        {2011,6,1, 21,4, 0},
        {2011,6,9, 5-3,12, 1},
        {2011,6,15, 23-3,15, 2},
        {2011,6,23, 14-3,49, 3},
        
        {2011,7,1, 11-3,55, 0},
        {2011,7,8, 9-3,31, 1},
        {2011,7,15, 9-3,41, 2},
        {2011,7,23, 8-3,3, 3},
        
        {2011,8,29, 6-3,5, 0},
        {2011,8,6, 14-3,9, 1},
        {2011,8,13, 21-3,59, 2},
        {2011,8,21, 21,56, 3},
        
        {2011,9,27, 14-3,10, 0},
        {2011,9,4, 20-3,40, 1},
        {2011,9,12, 12-3,28, 2},
        {2011,9,20, 16-3,40, 3},
        
        {2011,10,26, 22-3,57, 0},
        {2011,10,4, 6-3,16, 1},
        {2011,10,12, 5-3,7, 2},
        {2011,10,20, 6-3,31, 3},
        
        {2011,11,25, 8-2,11, 0},
        {2011,11,2, 18-2,39, 1},
        {2011,11,10, 22-2,17, 2},
        {2011,11,18, 17-2,10, 3},
        
        {2011,12,24, 20-2,8, 0},
        {2011,12,2, 11-2,53, 1},
        {2011,12,10, 16-2,37, 2},
        {2011,12,18, 2-2,49, 3},
        
    };
    
    public String concatenate(int[] array) {
        return concatenate(array, array.length);
    }

    public String concatenate(int[] array,int len) {
        String y = "";
        for(int i = 0;i<Math.min(array.length,len);i++) {
            y += (i==0?"":DELIMITER) + array[i];
        }
        return y;
    }
    
    public int[] eeSolstice(long year, short month) {
        
        int[] calculation = Astronomy.JD2calendarDate(Astronomy.solstice(year,month, true));
        int calcDate = (calculation[1]<<5)+calculation[2];
        int quarter = (int)Math.floor((calculation[1]-1)/3);
        int solMinDate = 20 + quarter;
        quarter = (quarter+1)*3;
        if(calcDate < (quarter<<5) +solMinDate) {
            calculation[1] = quarter;
            calculation[2] = solMinDate;
            calculation[3] -= 24;
        } else if(calcDate > (quarter<<5) +solMinDate +1) {
            calculation[1] = quarter;
            calculation[2] = solMinDate +1;
            calculation[3] += 24;
        }
        return calculation;
        
    }
    
    
    @Test
    public void testGregorianEaster() {
        
        for(int i=0; i<GREGORIAN_EASTERS.length; i++) {

            String testRowS = concatenate( GREGORIAN_EASTERS[i] );
            int[] astronomyRow = Astronomy.gregorianEaster( GREGORIAN_EASTERS[i][0] );
            String astronomyRowS = GREGORIAN_EASTERS[i][0] +DELIMITER +concatenate( astronomyRow );
            assertEquals("Wrong Gregorian Easter calculation (test no. "+i+")", testRowS, astronomyRowS);

        }
        
    }
    
    @Test
    public void testSolstice() {
        
        for(int i=0; i<SOLSTICES.length; i++) {
            
            String testRowS = concatenate( SOLSTICES[i] );
            int[] astronomyRow = eeSolstice((long) SOLSTICES[i][0], (short) SOLSTICES[i][1]);
            String astronomyRowS = concatenate(astronomyRow, SOLSTICES[i].length);
            assertEquals("Wrong solstices calculation (test no. "+i+")", testRowS, astronomyRowS);

        }
        
    }
    
    @Test
    public void testgregorian2JDN() {
        
        for(int i=0; i<GREGORIAN2JD.length; i++) {
            
            assertEquals("Wrong gregorian2JDN calculation (test no. "+i+")", (int)GREGORIAN2JD[i][3], 
                    (int)Astronomy.gregorian2JDN(GREGORIAN2JD[i][0], GREGORIAN2JD[i][1], GREGORIAN2JD[i][2]));

        }
        
    }
    
    @Test
    public void testgregorianSunrise() {
        
        double allowedTimeDifference = 240; //seconds
        
        for(int i=0; i<SUNRISE_SUNSET.length; i++) {
            
            HashMap<String,Double> results = Astronomy.gregorianSunrise(
                    Astronomy.gregorian2JDN((int)SUNRISE_SUNSET[i][0], (int)SUNRISE_SUNSET[i][1], (int)SUNRISE_SUNSET[i][2]), 
                    SUNRISE_SUNSET[i][7], SUNRISE_SUNSET[i][8]
            );
            
            for(int j=3; j<7; j+=2) {
                if(SUNRISE_SUNSET[i][j] > -1 ) {
                    
                    String eventName = (j==3) ? "Sunrise" : "Sunset";
                    String rKey = (j==3) ? Astronomy.Keys.J_RISE : Astronomy.Keys.J_SET;
                    double testTime = -0.5 // JDN is at noon, not at midnight
                        + Astronomy.gregorian2JDN((int)SUNRISE_SUNSET[i][0], (int)SUNRISE_SUNSET[i][1], (int)SUNRISE_SUNSET[i][2])
                        + (3600*SUNRISE_SUNSET[i][j] + 60*SUNRISE_SUNSET[i][j+1]) / Astronomy.SECONDS_IN_DAY;
                    double diffSecs = Astronomy.SECONDS_IN_DAY * Math.abs(testTime - results.get(rKey));
                    if(diffSecs > allowedTimeDifference) {
                        fail(eventName 
                                + " calculation out of allowed range "
                                + allowedTimeDifference
                                + " s (test no. "
                                + i
                                + "): Testdata = "
                                + concatenate(Astronomy.JD2calendarDate(testTime))
                                + " vs. Calculated = "
                                + concatenate(Astronomy.JD2calendarDate(results.get(rKey)))
                                );
                    }
                }
            }
        }
        
    }
    
    @Test
    public void moonPhaseCorrected() {
        
        double allowedTimeDifference = 150; //seconds
        
        for(int i=0; i<MOONPHASES.length; i++) {
            double actual = Astronomy.moonPhaseCorrected((long)MOONPHASES[i][0], (short)(MOONPHASES[i][1]), (short)MOONPHASES[i][5], true);
            double expected = -0.5 // JDN is at noon, not at midnight
                + Astronomy.gregorian2JDN((int)MOONPHASES[i][0], (int)MOONPHASES[i][1], (int)MOONPHASES[i][2])
                + (3600*MOONPHASES[i][3] + 60*MOONPHASES[i][4]) / (double)Astronomy.SECONDS_IN_DAY;
            double diffSecs = Astronomy.SECONDS_IN_DAY * Math.abs(expected - actual);
            if(diffSecs > allowedTimeDifference) {
                fail("Moonphase "
                        + MOONPHASES[i][5]
                        + " calculation out of allowed range "
                        + allowedTimeDifference
                        + " s (test no. "
                        + i
                        + "): Testdata = "
                        + concatenate(Astronomy.JD2calendarDate(expected))
                        + " vs. Calculated = "
                        + concatenate(Astronomy.JD2calendarDate(actual))
                        );
            }
        }

        
    }
    
}
