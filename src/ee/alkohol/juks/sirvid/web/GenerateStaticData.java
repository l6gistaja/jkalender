package ee.alkohol.juks.sirvid.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ee.alkohol.juks.sirvid.containers.DaoKalenderJDBCSqlite;
import ee.alkohol.juks.sirvid.containers.InputData;
import ee.alkohol.juks.sirvid.containers.PropertiesT;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidRune;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidSVG;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public class GenerateStaticData extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6397051823497306570L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      
      InputData inputData = new InputData();
      inputData.webPath = getServletContext().getRealPath("/");
      response.setContentType("text/plain; charset=UTF-8");
      PrintWriter out = response.getWriter();
      String propsDelimiter = "\n####### " + this.getClass().getName() + " ####### ";
      Properties svgExportProps = new Properties();
      ICalculator.loadProperties(this, svgExportProps, ee.alkohol.juks.sirvid.containers.Constants.PATH_DATA 
    		  + ee.alkohol.juks.sirvid.containers.Constants.PATH_DATA_SIRVID
    		  + ee.alkohol.juks.sirvid.containers.Constants.PATH_DATA_SIRVID_PROPS);
      
      try {
    	  ICalculator iCalc = new ICalculator(inputData);
    	  
    	  out.println(propsDelimiter + svgExportProps.getProperty("runeContentProps") + "\n");
    	  ResultSet runes = iCalc.CalendarDAO.getRange(Integer.MIN_VALUE, Integer.MAX_VALUE, DaoKalenderJDBCSqlite.DbTables.RUNES, null);
          while(runes.next()) { 
        	  String filename = runes.getString(DaoKalenderJDBCSqlite.DbTables.RUNES.getDescription());
              out.println(filename + "=" 
            		  + SirvidRune.loadSVGcontent(getServletContext().getResourceAsStream("/sirvid/svg/" + filename))
            		  .replaceAll("([:=\n])","\\\\$1"));
          }
      } catch(Exception e) {
          e.printStackTrace(out);
      }
      
  }
}