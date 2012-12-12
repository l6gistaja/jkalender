package ee.alkohol.juks.sirvid.containers.ical;

import java.util.LinkedHashMap;

public class ICalProperty {
    
    public Object value;
    public LinkedHashMap<String,String> parameters = new LinkedHashMap<String,String>();
    
    public ICalProperty(Object pValue, String[] pParameters) {
        value = pValue;
        if(pParameters != null) { 
            for(int i=0; i < pParameters.length; i+=2) {
                parameters.put(pParameters[i], pParameters[i+1]);
            }
        }
    }
    
}
