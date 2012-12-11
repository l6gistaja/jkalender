package ee.alkohol.juks.sirvid.exporters;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import ee.alkohol.juks.sirvid.containers.ICalEvent;
import ee.alkohol.juks.sirvid.containers.ICalProperty;
import ee.alkohol.juks.sirvid.containers.ICalendar;

public abstract class ExporterICalendar {
    
	public static final String FILENAME_PREFIX = "jkal_";
	public static enum components {VCALENDAR, VEVENT, VVENUE}
    
	ICalendar iCal;
    private String fileExtension;
    private String mimeType;
    private HashMap<components,String[]> componentStrings;

	public ExporterICalendar() {
    	componentStrings = new HashMap<components,String[]>();
    }
    
    public String generate(ICalendar icalendar) {
        
        iCal = icalendar;
        StringBuilder sb = new StringBuilder();
        
        sb.append(getComponentBorder(components.VCALENDAR, 0));
        
        for (@SuppressWarnings("rawtypes") Map.Entry entry: iCal.iCalBody.entrySet()) {
            sb.append(generateProperty((String)entry.getKey(),(ICalProperty)entry.getValue()));
        }
        
        if(iCal.vVenue != null) {
        	sb.append(getComponentBorder(components.VVENUE, 0));
        	for (@SuppressWarnings("rawtypes") Map.Entry entry: iCal.vVenue.entrySet()) {
                sb.append(generateProperty((String)entry.getKey(),(ICalProperty)entry.getValue()));
            }
        	sb.append(getComponentBorder(components.VVENUE, 1));
        }
        
        int j = 0;
        while (j < iCal.vEvent.size()) {
            ICalEvent event = iCal.vEvent.get(j);
            sb.append(getComponentBorder(components.VEVENT, 0));
            for (@SuppressWarnings("rawtypes") Map.Entry entry: event.properties.entrySet()) {
                sb.append(generateProperty((String)entry.getKey(),(ICalProperty)entry.getValue()));
            }
            sb.append(getComponentBorder(components.VEVENT, 1));
            j++;
        }
        
        sb.append(getComponentBorder(components.VCALENDAR, 1));
        return sb.toString();
    }
    
    public String getComponentBorder(components name, int index) {
    	return componentStrings.containsKey(name) ? ((String[])componentStrings.get(name))[index] :  "";
    }
    
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
    
    public HashMap<components, String[]> getComponentStrings() {
		return componentStrings;
	}
    
}
