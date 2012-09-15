package ee.alkohol.juks.sirvid.web;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import ee.alkohol.juks.sirvid.containers.InputData;

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
      
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    String docType =
      "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
      "Transitional//EN\">\n";
    out.println(docType +
                "<HTML>\n" +
                "<HEAD><TITLE>Hello</TITLE></HEAD>\n" +
                "<BODY BGCOLOR=\"#FDF5E6\">\n" +
                "<H1>InputData</H1>\n" +
                inputData.toString() +
                "</BODY></HTML>");
  }
}