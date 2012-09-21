package ee.alkohol.juks.sirvid.exporters.ical;

import java.util.Map;
import ee.alkohol.juks.sirvid.containers.ICalProperty;

public class ExporterICS extends Exporter{
    
    private String fileExtension = ".ics";
    private String mimeType = "text/calendar";
    
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
        sb.append("\r\n");
        return sb.toString();
    }

    @Override
    public String beginBody() {
        return "BEGIN:VCALENDAR\r\n";
    }

    @Override
    public String endBody() {
        return "END:VCALENDAR\r\n";
    }

    @Override
    public String beginEvent() {
        return "BEGIN:VEVENT\r\n";
    }

    @Override
    public String endEvent() {
        return "END:VEVENT\r\n";
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }
    

}
