package ee.alkohol.juks.sirvid.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ee.alkohol.juks.sirvid.containers.InputData;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;
import ee.alkohol.juks.sirvid.exporters.Exporter;

public class JKalender extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1521372364795750118L;

@Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      
      InputData inputData = new InputData();
      inputData.setDate(request.getParameter("date"));
      inputData.setTimezone(request.getParameter("tz"));
      inputData.setOutputFormat(request.getParameter("f"));
      inputData.setUseDynamicTime(request.getParameter("dt"));
      inputData.setCalculateMoonphases(request.getParameter("mo"));
      inputData.setCalculateSolistices(request.getParameter("sl"));
      inputData.setCalculateSunrisesSunsets(request.getParameter("sr"));
      inputData.setCalculateGregorianEaster(request.getParameter("ge"));
      inputData.setCalculateJulianEaster(request.getParameter("je"));
      inputData.setLatitude(request.getParameter("lat"));
      inputData.setLongitude(request.getParameter("lon"));
      inputData.setTimespan(request.getParameter("ts"));
      inputData.setAddDescription(request.getParameter("ad"));
      inputData.setAddRemark(request.getParameter("ar"));
      inputData.setCalendarData(request.getParameter("ce"));
      inputData.webPath = getServletContext().getRealPath("/");
      inputData.servletContext = getServletContext();
      
      ICalculator iCalc;
      try {
          iCalc = new ICalculator(inputData);
          Exporter xport = Exporter.getExporter(inputData.getOutputFormat());
          response.setContentType(xport.getMimeType() + "; charset=UTF-8");
          if(request.getParameter("dl") != null && request.getParameter("dl").equals("1")) {
        	  response.setHeader("Content-Disposition", "attachment; filename="
        			  + Exporter.FILENAME_PREFIX
        			  + iCalc.timespan
        			  + xport.getFileExtension());
          }
          PrintWriter out = response.getWriter();
          out.println(xport.generate(iCalc));
      } catch(Exception e) {
          response.setContentType("text/plain; charset=UTF-8");
          PrintWriter out = response.getWriter();
          e.printStackTrace(out);
      }
      
  }
}