package ee.alkohol.juks.sirvid.exporters.ical;

import java.text.SimpleDateFormat;
import java.util.Map;
import ee.alkohol.juks.sirvid.containers.ICalEvent;
import ee.alkohol.juks.sirvid.containers.ICalProperty;
import ee.alkohol.juks.sirvid.containers.ICalendar;

public abstract class Exporter {
    
	public static final String FILENAME_PREFIX = "jkal_";
    
	ICalendar iCal;
    private String fileExtension;
    private String mimeType;

    public Exporter() {}
    
    public String generate(ICalendar icalendar) {
        
        iCal = icalendar;
        StringBuilder sb = new StringBuilder();
        
        sb.append(beginBody());
        
        for (@SuppressWarnings("rawtypes") Map.Entry entry: iCal.iCalBody.entrySet()) {
            sb.append(generateProperty((String)entry.getKey(),(ICalProperty)entry.getValue()));
        }
        
        int j = 0;
        while (j < iCal.vEvent.size()) {
            ICalEvent event = iCal.vEvent.get(j);
            sb.append(beginEvent());
            for (@SuppressWarnings("rawtypes") Map.Entry entry: event.properties.entrySet()) {
                sb.append(generateProperty((String)entry.getKey(),(ICalProperty)entry.getValue()));
            }
            sb.append(endEvent());
            j++;
        }
        
        sb.append(endBody());
        return sb.toString();
    }
    
    abstract public String beginBody();
    abstract public String endBody();
    abstract public String beginEvent();
    abstract public String endEvent();
    abstract public String generateProperty(String key, ICalProperty iCalProp);

    
    public static String formatOutput(ICalProperty prop) {
        
        Object propVal = prop.value;
        if(propVal != null) {
            
            if(propVal instanceof java.util.Date) {
                String type = prop.parameters.get(ICalendar.Keys.VALUE);
                if(type != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat( type.equals(ICalendar.Values.DATE) ? ICalendar.SDF_DATE : ICalendar.SDF_DATETIME );
                    return dateFormat.format(propVal);
                }
            }
            
            return propVal.toString();
        }
        return "";
    }
    
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }
    
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
    
}
