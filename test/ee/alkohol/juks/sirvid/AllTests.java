package ee.alkohol.juks.sirvid;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
    ee.alkohol.juks.sirvid.math.AstronomyTest.class,
    ee.alkohol.juks.sirvid.containers.InputDataTest.class,
    ee.alkohol.juks.sirvid.exporters.ExporterTest.class
})
public class AllTests {} 