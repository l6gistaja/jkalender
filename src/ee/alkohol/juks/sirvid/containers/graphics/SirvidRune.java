package ee.alkohol.juks.sirvid.containers.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;

import ee.alkohol.juks.sirvid.containers.DaoKalenderJDBCSqlite;

public class SirvidRune {

	private int cx;
	private String filename;
	private int width;
	private String svgContent;
	private double rightness;

    public SirvidRune(int cx, String filename, int width) throws SQLException, IOException {
        setCx(cx);
        setWidth(width);
        setRightness(cx/width);
        if(filename != null) {
            setFilename(filename);
            setSvgContent(loadSVGcontent(getFilename()));
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
        
        filename = "svg/" + filename;
        StringBuilder svgContent =  new StringBuilder();

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

        return svgContent.toString();
    }
    
	public int getCx() {
		return cx;
	}

	public void setCx(int cx) {
		this.cx = cx;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getSvgContent() {
		return svgContent;
	}

	public void setSvgContent(String svgContent) {
		this.svgContent = svgContent;
	}
	
    public double getRightness() {
        return rightness;
    }

    public void setRightness(double rightness) {
        this.rightness = rightness;
    }

}
