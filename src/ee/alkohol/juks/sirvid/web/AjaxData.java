package ee.alkohol.juks.sirvid.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ee.alkohol.juks.sirvid.containers.InputData;

public class AjaxData extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 909985970777005493L;

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ajaxdata>\n");
            response.setContentType("text/xml; charset=UTF-8");
            if(request.getParameter("type") != null && request.getParameter("type").equals("available_timezones")) {
                String[] tzs = InputData.getAllAvailableTimezones();
                java.util.Arrays.sort(tzs);
                for(String tz : tzs) {
                    if(request.getParameter("onlyplaces") != null && tz.matches("^((Etc|SystemV).*|[^\\/]+)$")) {
                        continue;
                    }
                    sb.append("<z>");
                    sb.append(tz);
                    sb.append("</z>");
                }
            } else {
                sb.append("<errormsg>Missing type parameter.</errormsg>\n");
            }
            sb.append("</ajaxdata>\n");
            PrintWriter out = response.getWriter();
            out.println(sb.toString());
        } catch(Exception e) {
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter out = response.getWriter();
            e.printStackTrace(out);
        }
        
    }
    
}
