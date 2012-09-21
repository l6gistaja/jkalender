package ee.alkohol.juks.sirvid.exporters.ical;

import java.util.Map;
import ee.alkohol.juks.sirvid.containers.ICalProperty;

public class ExporterXCS extends Exporter{
    
    private String FILE_EXTENSION = ".xcs";
    private String mimeType = "text/xml";
    
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
        sb.append("\r\n");
        return sb.toString();
    }

    @Override
    public String beginBody() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<iCalendar>\r\n<vcalendar>\r\n";
    }

    @Override
    public String endBody() {
        return "</vcalendar>\r\n</iCalendar>\r\n";
    }

    @Override
    public String beginEvent() {
        return "<vevent>\r\n";
    }

    @Override
    public String endEvent() {
        return "</vevent>\r\n";
    }

    @Override
    public String getFileExtension() {
        return FILE_EXTENSION;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
    

}
