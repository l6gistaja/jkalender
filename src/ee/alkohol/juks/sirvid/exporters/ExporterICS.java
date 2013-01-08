package ee.alkohol.juks.sirvid.exporters;

import java.util.Map;

import ee.alkohol.juks.sirvid.containers.ical.ICalProperty;
import ee.alkohol.juks.sirvid.containers.ical.ICalendar;

public class ExporterICS extends ExporterICalendar{
    
    public ExporterICS () {
        super();
        this.setFileExtension(".ics");
        this.setMimeType("text/calendar");
        this.getComponentStrings().put(components.VCALENDAR, new String[]{"BEGIN:VCALENDAR\n","END:VCALENDAR\n"});
        this.getComponentStrings().put(components.VEVENT, new String[]{"BEGIN:VEVENT\n","END:VEVENT\n"});
        this.getComponentStrings().put(components.VVENUE, new String[]{"BEGIN:VVENUE\n","END:VVENUE\n"});
    }
    
    @Override
    public String generateProperty(String key, ICalProperty iCalProp) {
        StringBuilder sb = new StringBuilder();
        sb.append(key.toUpperCase());
        for (@SuppressWarnings("rawtypes") Map.Entry entry: iCalProp.parameters.entrySet()) {
            sb.append(";");
            sb.append(entry.getKey().toString().toUpperCase());
            sb.append("=");
            sb.append(entry.getValue());
        }
        sb.append(":");
        sb.append(formatOutput(iCalProp));
        sb.append("\n");
        return sb.toString();
    }
    
    /**
     * @see <a href="http://tools.ietf.org/html/rfc5545#section-3.3.11">RFC 5545 : Property Value Data Types : TEXT</a>
     */
    public String formatOutput(ICalProperty prop) {
        String valueType = prop.parameters.get(ICalendar.Keys.VALUE);
    	if(valueType != null && valueType.equals(ICalendar.Values.TEXT)) {
    		Object propVal = prop.value;
    		if(propVal != null) {
        		return propVal.toString().replace("\n","\\n");
        	}
        }
        return super.formatOutput(prop);
    }
}
