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
            String errorMsg = null;
            
            if(request.getParameter("type") != null) {
            	
            	String[] sList = null;
            	String sListTag = "undef";
            	
            	if(request.getParameter("type").equals("available_timezones")) {
            		sList = InputData.getAllAvailableTimezones();
            		sListTag = "z";
            	}
            	
            	if(request.getParameter("type").equals("supported_formats")) {
            		sList = InputData.SUPPORTED_OUTPUT_FORMATS.clone();
            		sListTag = "f";
            	}
                
            	if(sListTag.equals("undef")) { errorMsg = "Nonexisting 'type' parameter, should be 'available_timezones' or 'supported_formats'."; }
            	
            	if(sList != null) { 
                    if(request.getParameter("sort") != null) { java.util.Arrays.sort(sList); }
                    for(String item : sList) {
                        if(request.getParameter("onlyplaces") != null && item.matches("^((Etc|SystemV).*|[^\\/]+)$")) {
                            continue;
                        }
                        sb.append("<");
                        sb.append(sListTag);
                        sb.append(">");
                        sb.append(item);
                        sb.append("</");
                        sb.append(sListTag);
                        sb.append(">\n");
                    }
            	}

            } else {
            	errorMsg = "Missing 'type' parameter.";
            }
            
            if(errorMsg != null) {
            	sb.append("<errormsg>");
            	sb.append(errorMsg);
            	sb.append("</errormsg>\n");
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
