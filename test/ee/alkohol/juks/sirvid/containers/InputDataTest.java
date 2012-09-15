package ee.alkohol.juks.sirvid.containers;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class InputDataTest {
    
    public InputData inputData;
    
    @Before public void init() {
        inputData = new InputData();
    }
    
    @Test public void setCalculateSunrisesSunsets() {
        
        String[] coordinates = {inputData.latitudeName, inputData.longitudeName};
        for(String memberName: coordinates) {
            assertTrue(memberName + " should NOT cause error, when sunrise/sunset calculation isn't needed.", !isError(memberName));
        }
        
        inputData.setCalculateSunrisesSunsets(inputData.defaultTrue);
        
        for(String memberName: coordinates) {
            assertTrue("Null " + memberName + " should cause error.", isError(memberName));
        }
        
    }
    
    @Test public void setLatitudeLongitude() {
            
        java.lang.reflect.Method method;
        String[] coordinates = {inputData.latitudeName, inputData.longitudeName};
        
        try {
            
            for(String memberName: coordinates) {
                
                String methodName = "set" + memberName;
                method = inputData.getClass().getMethod(methodName, String.class);
                inputData.initialize();
                inputData.setCalculateSunrisesSunsets(null);
                
                assertTrue(methodName + ": At the beginning, there should be no errors:\n\n" + inputData.toString(), !isError(memberName));
                
                method.invoke(inputData, "-961");
                assertTrue(methodName + ": Dont test range when sunrise/sunset calculation isn't needed:\n\n" + inputData.toString(), !isError(memberName));
                
                inputData.setCalculateSunrisesSunsets(inputData.defaultTrue);
                double extreme = (memberName.equals(inputData.latitudeName)) ? 90 : 180;
                
                double value = -(extreme + 0.1);
                method.invoke(inputData, ""+value);
                assertTrue(methodName + " @ " + value + ": should fail:\n\n" + inputData.toString(), isError(memberName));
                
                value = -extreme;
                method.invoke(inputData, ""+value);
                assertTrue(methodName + " @ " + value + ": should NOT fail:\n\n" + inputData.toString(), !isError(memberName));
                
                value = 0;
                method.invoke(inputData, ""+value);
                assertTrue(methodName + " @ " + value + ": should NOT fail:\n\n" + inputData.toString(), !isError(memberName));
                
                value = extreme;
                method.invoke(inputData, ""+value);
                assertTrue(methodName + " @ " + value + ": should NOT fail:\n\n" + inputData.toString(), !isError(memberName));
                
                value = extreme + 1.2;
                method.invoke(inputData, ""+value);
                assertTrue(methodName + " @ " + value + ": should fail:\n\n" + inputData.toString(), isError(memberName));
                
            }
            
        } catch (Exception e) {
            fail("Exception : " +e.getMessage());
        }
        
    }
    
    private boolean isError(String memberName) {
        return inputData.criticalErrors.containsKey(memberName);
    }
        
    
}
