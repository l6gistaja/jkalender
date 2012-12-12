package ee.alkohol.juks.sirvid.exporters;

import java.util.Map;
import ee.alkohol.juks.sirvid.containers.ical.ICalProperty;

public class ExporterXCS extends ExporterICalendar{
    
    public ExporterXCS () {
        super();
        this.setFileExtension(".xcs");
        this.setMimeType("text/xml");
        this.getComponentStrings().put(components.VCALENDAR, new String[]{
        		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<iCalendar>\n<vcalendar>\n",
        		"</vcalendar>\n</iCalendar>\n"});
        this.getComponentStrings().put(components.VEVENT, new String[]{"<vevent>\n","</vevent>\n"});
        this.getComponentStrings().put(components.VVENUE, new String[]{"<vvenue>\n","</vvenue>\n"});
    }
    
    @Override
    public String generateProperty(String key, ICalProperty iCalProp) {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(key.toLowerCase());
        for (@SuppressWarnings("rawtypes") Map.Entry entry: iCalProp.parameters.entrySet()) {
            sb.append(" ");
            sb.append(entry.getKey().toString().toLowerCase());
            sb.append("=\"");
            sb.append(entry.getValue());
            sb.append("\"");
        }
        sb.append(">");
        sb.append(formatOutput(iCalProp));
        sb.append("</");
        sb.append(key.toLowerCase());
        sb.append(">");
        sb.append("\n");
        return sb.toString();
    }

}
