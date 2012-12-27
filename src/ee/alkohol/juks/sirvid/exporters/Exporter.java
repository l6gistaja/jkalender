package ee.alkohol.juks.sirvid.exporters;

import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public abstract class Exporter {
    
    public static final String FILENAME_PREFIX = "jkal_";
    
    private String fileExtension;
    private String mimeType;
    
    public Exporter() { }
    
    public static Exporter getExporter(String extension) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        @SuppressWarnings("rawtypes")
        Class clazz = Class.forName("ee.alkohol.juks.sirvid.exporters.Exporter"+extension.toUpperCase());
        return (Exporter)clazz.newInstance();
    }
    
    abstract public String generate(ICalculator icalendar);
    
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
