package ee.alkohol.juks.sirvid.exporters.ical;

import java.util.Map;
import ee.alkohol.juks.sirvid.containers.ICalProperty;

public class ExporterICS extends Exporter{
    
    public ExporterICS () {
        super();
        this.setFileExtension(".ics");
        this.setMimeType("text/calendar");
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

    @Override
    public String beginBody() {
        return "BEGIN:VCALENDAR\n";
    }

    @Override
    public String endBody() {
        return "END:VCALENDAR\n";
    }

    @Override
    public String beginEvent() {
        return "BEGIN:VEVENT\n";
    }

    @Override
    public String endEvent() {
        return "END:VEVENT\n";
    }

}
