package ee.alkohol.juks.sirvid.containers;

import java.util.Properties;

/**
 * Generic extension of Java's Properties
 * 
 * Main goal is make Properties.getProperty*() return-type-aware
 */
public class PropertiesT extends Properties {

    private static final long serialVersionUID = -4032721562443143805L;

    public PropertiesT() {
        super();
    }

    public PropertiesT(Properties defaults) {
        super(defaults);
        // TODO Auto-generated constructor stub
    }
    
    public int getPropertyInt(String key) {
        try {
            return Integer.parseInt(super.getProperty(key));
        } catch (Exception e) {
            return 0;
        }
    }
    
}
