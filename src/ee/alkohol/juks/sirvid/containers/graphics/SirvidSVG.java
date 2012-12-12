package ee.alkohol.juks.sirvid.containers.graphics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import ee.alkohol.juks.sirvid.containers.DaoKalenderJDBCSqlite;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator.DbIdStatuses;

public class SirvidSVG {
    
    public static final String dataPath = "sirvid/";
    
    static Properties props = new Properties();
    static HashMap<String,String> svgContent = new HashMap<String,String>();
    
    private ICalculator iCalc;
    public ArrayList<String> errorMsgs = new ArrayList<String>();
    
    
    public SirvidSVG(ICalculator iC) {
        
        iCalc = iC;
        
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream(dataPath + "svg_export.properties"));
        }
        catch(Exception e) {
            errorMsgs.add("Failed to open svg_export.properties : " + e.getMessage());
        }
        
        DaoKalenderJDBCSqlite CalendarDAO = new DaoKalenderJDBCSqlite(iCalc.inputData.jbdcConnect);
        if(CalendarDAO.isConnected()) {
            ResultSet commonRunes = CalendarDAO.getRunes(0, ICalculator.DbIdStatuses.MOON_LAST.getDbId());
            if(commonRunes != null) {
                try {
                    while(commonRunes.next()) { 
                        svgContent.put(commonRunes.getString(DaoKalenderJDBCSqlite.DbTables.RUNES.getDescription()),
                                loadSVGcontent(commonRunes.getString(DaoKalenderJDBCSqlite.DbTables.RUNES.getDescription())));
                    }
                }
                catch(SQLException e) {
                    errorMsgs.add("DAO.getRunes() failed : " + e.getMessage());
                }
            }
            System.out.println(svgContent.toString());
        }
        
    }
    
    /**
     * Parse graphics instructions from SVG file
     * 
     * As parsing SVG files with org.w3c.dom.* is extremely slow, it was decided to simply read SVGs thru and extract needed content manually.
     * This however expects title tag to be first child of root svg tag.
     * 
     * @param filename
     * @return content of svg tag without leading title tag
     */
    public String loadSVGcontent(String filename) {
        
        filename = dataPath + "svg/" + filename;
        StringBuilder svgContent =  new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename), "UTF-8"));
            String strLine;
            boolean beginOfGrafix = false; 
            while ((strLine = br.readLine()) != null)   {
                if(beginOfGrafix) {
                    if(strLine.indexOf("</svg>") != -1) { break; }
                    svgContent.append(strLine);
                    svgContent.append("\n");
                } else {
                    if(strLine.indexOf("</title>") != -1) { beginOfGrafix = true; }
                }
            }
            br.close();
        }
        catch(Exception e) {
            errorMsgs.add("Failed to open or parse " + filename + " : " + e.getMessage());
            return null;
        }
        return svgContent.toString();
    }
    
}
