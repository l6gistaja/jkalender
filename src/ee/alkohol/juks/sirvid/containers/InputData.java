package ee.alkohol.juks.sirvid.containers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class InputData {
    
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final String[] supportedTimespans = {"d","m","y"};
    public static final String[] supportedOutputFormats = {""};
    
    public final String latitudeName = "Latitude";
    public final String longitudeName = "Longitude";
    public final String defaultTrue = "1";
    
    public HashMap<String,String> criticalErrors;
    
    private Date date;
    private String timezone;
    private String timespan;
    private String outputFormat;
    private boolean useDynamicTime = true;
    private boolean calculateMoonphases = false;
    private boolean calculateSolistices = false;
    private boolean calculateSunrisesSunsets = false;
    private boolean calculateGregorianEaster = false;
    private boolean calculateJulianEaster = false;
    private Double latitude;
    private Double longitude;
    
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        Double lo = fetchDouble(longitude);
        String memberName = fetchMemberName(Thread.currentThread().getStackTrace()[1].getMethodName());
        if(isCalculateSunrisesSunsets() && ( lo == null || lo.doubleValue() < -180 || lo.doubleValue() > 180)) {
            criticalErrors.put(memberName, longitude);
        } else {
            criticalErrors.remove(memberName);
            this.longitude = lo;
        }
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        Double la = fetchDouble(latitude);
        String memberName = fetchMemberName(Thread.currentThread().getStackTrace()[1].getMethodName());
        if(isCalculateSunrisesSunsets() && !isInRange(la, -90, 90)) {
            criticalErrors.put(memberName, latitude);
        } else {
            criticalErrors.remove(memberName);
            this.latitude = la;
        }
    }
    
    public boolean isCalculateSunrisesSunsets() {
        return calculateSunrisesSunsets;
    }

    public void setCalculateSunrisesSunsets(String calculateSunrisesSunsets) {
        this.calculateSunrisesSunsets = isTrue(calculateSunrisesSunsets);
        if(isCalculateSunrisesSunsets()) {
            
            String memberName = latitudeName;
            if(!isInRange(latitude, -90, 90)) {
                criticalErrors.put(memberName, latitude == null ? "(null)" : latitude.toString());
            } else {
                criticalErrors.remove(memberName);
            }
            
            memberName = longitudeName;
            if(!isInRange(longitude, -180, 180)) {
                criticalErrors.put(memberName, longitude == null ? "(null)" : longitude.toString());
            } else {
                criticalErrors.remove(memberName);
            }
        }
    }

    public boolean isCalculateGregorianEaster() {
        return calculateGregorianEaster;
    }

    public void setCalculateGregorianEaster(String calculateGregorianEaster) {
        this.calculateGregorianEaster = isTrue(calculateGregorianEaster);
    }

    public boolean isCalculateJulianEaster() {
        return calculateJulianEaster;
    }

    public void setCalculateJulianEaster(String calculateJulianEaster) {
        this.calculateJulianEaster = isTrue(calculateJulianEaster);
    }

    
    public boolean isCalculateSolistices() {
        return calculateSolistices;
    }

    public void setCalculateSolistices(String calculateSolistices) {
        this.calculateSolistices = isTrue(calculateSolistices);
    }
    
    public boolean isCalculateMoonphases() {
        return calculateMoonphases;
    }

    public void setCalculateMoonphases(String calculateMoonphases) {
        this.calculateMoonphases = isTrue(calculateMoonphases);
    }
    
    public boolean isUseDynamicTime() {
        return useDynamicTime;
    }

    public void setUseDynamicTime(Object useDynamicTime) {
        this.useDynamicTime = isTrue(useDynamicTime);
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat!= null && Arrays.asList(supportedOutputFormats).contains(outputFormat) ? outputFormat : supportedOutputFormats[0];
    }

    public String getTimespan() {
        return timespan;
    }

    public void setTimespan(String timespan) {
        this.timespan = timespan!= null && Arrays.asList(supportedTimespans).contains(timespan) ? timespan : supportedTimespans[0];
    }

    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone != null && Arrays.asList(getAllAvailableTimezones()).contains(timezone) ? timezone : "GMT";
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(String date) {
        if(date == null) {
            this.date = new Date();
            return;
        }
        String memberName = fetchMemberName(Thread.currentThread().getStackTrace()[1].getMethodName());
        try {
            this.date = dateFormat.parse(date);
            criticalErrors.remove(memberName);
        } catch (Exception e) {
            criticalErrors.put(memberName,date);
        }
    }
    
    public InputData() {
        initialize();
    }
    
    public void initialize() {
        criticalErrors = new HashMap<String,String>();
        //setDate(null);
        setTimezone(null);
        setTimespan(null);
        setOutputFormat(null);
    }
    
    private String fetchMemberName(String setterName) {
        return setterName.substring(3);
    }
    
    private Double fetchDouble(String x) {
        try {
            return new Double(x);
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean isTrue(Object x) {
        return x!= null && (
                x instanceof String && x.equals("1")
            ) ? true : false;
    }
    
    public static String[] getAllAvailableTimezones() {
        return TimeZone.getAvailableIDs();
    }
    
    private boolean isInRange(Double obj, double min, double max) {
        return obj != null && obj.doubleValue() >= min && obj.doubleValue() <= max;
    }
    
    @Override
    public String toString() {
        
        String delimiter = ";\n";
        String propertyDelimiter = "=";
        StringBuffer sb = new StringBuffer();
        String[] mutableFields = {
                "date",
                "timezone",
                "timespan",
                "outputFormat",
                "useDynamicTime",
                "calculateMoonphases",
                "calculateSolistices",
                "calculateSunrisesSunsets",
                "calculateGregorianEaster",
                "calculateJulianEaster",
                "latitude",
                "longitude"
        };
        
        if(this.criticalErrors.size() > 0) {
            sb.append("Invalid fields : ");
            sb.append(criticalErrors.toString());
            sb.append(delimiter);
        }
        
        for(String fieldName : mutableFields) {

            Class<?> clazz = this.getClass();
            String val = "";
            
            try {
                val = clazz.getDeclaredField(fieldName).get(this).toString();
            } catch (Exception e) {
                val = "?";
            }
            sb.append(fieldName);
            sb.append(propertyDelimiter);
            sb.append(val);
            sb.append(delimiter);
        
        }
        
        return sb.toString();
    }
    
}
