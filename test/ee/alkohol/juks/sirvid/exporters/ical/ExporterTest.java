package ee.alkohol.juks.sirvid.exporters.ical;

import static org.junit.Assert.*;
import org.junit.Test;
import ee.alkohol.juks.sirvid.containers.ICalculator;
import ee.alkohol.juks.sirvid.containers.InputData;

public class ExporterTest {
    
    /*
     * It also tests database connection and ICalculator
     */
    @Test public void generate() {
        
        InputData inputData = new InputData();
        inputData.setDate("2012-09-18");
        inputData.setTimezone("Europe/Tallinn");
        inputData.setUseDynamicTime(InputData.DEFAULT_TRUE);
        inputData.setCalculateMoonphases(InputData.DEFAULT_TRUE);
        inputData.setCalculateSolistices(InputData.DEFAULT_TRUE);
        inputData.setCalculateSunrisesSunsets(InputData.DEFAULT_TRUE);
        inputData.setCalculateGregorianEaster(InputData.DEFAULT_TRUE);
        inputData.setCalculateJulianEaster(InputData.DEFAULT_TRUE);
        inputData.setLatitude("0");
        inputData.setLongitude("0");
        inputData.setTimespan(InputData.FLAGS.PERIOD.YEAR);
        inputData.setCalendarData(InputData.FLAGS.CALDATA.ALL_ESTONIAN);
        
        for(String outputFormat: InputData.SUPPORTED_OUTPUT_FORMATS) {
            inputData.setOutputFormat(outputFormat);
            ICalculator iCalc;
            try {
                iCalc = new ICalculator(inputData);
                iCalc.initExport();
                String output = iCalc.exporter.generate(iCalc.iCal);
                if(output == null || output.trim().equals("")) {
                    fail(outputFormat +" format output is null or empty.");
                }
                
                if(!iCalc.exporter.getFileExtension().equalsIgnoreCase("."+outputFormat)) { 
                    fail(outputFormat +" fileextension unset."); 
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                fail("Generating "+outputFormat +" format throwed exception: " + e.getMessage());
            }
            
        }
        
    }
}
