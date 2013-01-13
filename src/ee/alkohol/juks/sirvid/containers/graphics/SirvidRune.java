package ee.alkohol.juks.sirvid.containers.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import ee.alkohol.juks.sirvid.containers.DaoKalenderJDBCSqlite;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public class SirvidRune {

	public int cx;
	public String filename;
	public int width;
	public String svgContent;
	public double rightness;
	public static Properties runeTable;

    public SirvidRune(int cx, String filename, int width) throws SQLException, IOException {
        this.cx =cx;
        this.width = width;
        this.rightness = cx/width;
        if(filename != null) {
            this.filename = filename;
            this.svgContent = loadSVGcontent(this.filename);
        }
    }
	
	public SirvidRune(ResultSet rune) throws SQLException, IOException {
	    this(rune.getInt("cx"), rune.getString(DaoKalenderJDBCSqlite.DbTables.RUNES.getDescription()), rune.getInt("width"));
	}

    /**
     * Parse graphics instructions from SVG file
     * 
     * As parsing SVG files with org.w3c.dom.* is extremely slow, it was decided to simply read SVGs thru and extract needed content manually.
     * This however expects title tag to be first child of root svg tag.
     * 
     * @param filename
     * @return content of svg tag without leading title tag
	 * @throws IOException 
     */
	
    public String loadSVGcontent(String filename) throws IOException {
    	try {
    		if(false) { //SirvidSVG.props.getProperty("isWAR").equals("1")) {
    			if(runeTable == null) {
    				ICalculator.loadProperties(this, runeTable, 
    						ee.alkohol.juks.sirvid.containers.Constants.PATH_DATA 
    			    		  + ee.alkohol.juks.sirvid.containers.Constants.PATH_DATA_SIRVID
    			    		  + SirvidSVG.props.getProperty("runeContentProps"));
    			}
    			return runeTable.getProperty(filename);
    		} else {
    			filename = SirvidSVG.props.getProperty("pathSVG") + filename;
                return loadSVGcontent(this.getClass().getClassLoader().getResourceAsStream(filename));
    		}
    	} catch (Exception e) {
    		return "<text fill=\"cyan\">" + this.getClass().getName() + ".loadSVGcontent(String filename) : " + e.getMessage() + "</text>";
    	}
        
    }
    
    public static String loadSVGcontent(InputStream is) throws IOException {
        
        StringBuilder svgContent =  new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
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

        return svgContent.toString();
    }
}
