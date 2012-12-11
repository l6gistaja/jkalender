package ee.alkohol.juks.sirvid.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ee.alkohol.juks.sirvid.containers.ICalculator;
import ee.alkohol.juks.sirvid.containers.InputData;
import ee.alkohol.juks.sirvid.exporters.ExporterICalendar;

public class JKalender extends HttpServlet {


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
      inputData.setCalendarData(request.getParameter("ce"));
      inputData.jbdcConnect = "jdbc:sqlite:" + getServletContext().getRealPath("/kalender.sdb");
      
      ICalculator iCalc;
      try {
          iCalc = new ICalculator(inputData);
          iCalc.initExport();
          response.setContentType(iCalc.exporter.getMimeType() + "; charset=UTF-8");
          if(request.getParameter("dl") != null && request.getParameter("dl").equals("1")) {
        	  response.setHeader("Content-Disposition", "attachment; filename="
        			  + ExporterICalendar.FILENAME_PREFIX
        			  + iCalc.timespan
        			  + iCalc.exporter.getFileExtension());
          }
          PrintWriter out = response.getWriter();
          out.println(iCalc.exporter.generate(iCalc));
      } catch(Exception e) {
          response.setContentType("text/plain; charset=UTF-8");
          PrintWriter out = response.getWriter();
          e.printStackTrace(out);
      }
      
  }
}