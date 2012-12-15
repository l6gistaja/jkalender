package ee.alkohol.juks.sirvid.exporters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ee.alkohol.juks.sirvid.containers.graphics.SirvidDay;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidMonth;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidRune;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidSVG;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;
import ee.alkohol.juks.sirvid.containers.ical.ICalendar;

public class ExporterSVG extends Exporter {
	
    public ExporterSVG () {
        super();
        this.setFileExtension(".svg");
        this.setMimeType("image/svg+xml");
    }
    
    @Override
    public String generate(ICalculator iC) {
        
        SirvidSVG sSVG = new SirvidSVG(iC);
        StringBuilder sb = new StringBuilder();
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
        sb.append("<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
        sb.append("<title>");
        sb.append(iC.iCal.iCalBody.get(ICalendar.Keys.CALENDAR_NAME).value);
        sb.append(" (under construction)</title>\n");
        
        if(sSVG.errorMsgs.size() > 0) {
        	
            sb.append(SirvidSVG.errorTxtTags[0]);
            for(String errMsg : sSVG.errorMsgs) {
                sb.append(errMsg);
                sb.append("\n");
            }
            sb.append(SirvidSVG.errorTxtTags[1]);
            
        } else {
        	
        	SimpleDateFormat dateFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfDate"));
        	dateFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        	SimpleDateFormat monthFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfMonth"));
        	monthFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        	
        	// stylesheet 
            sb.append("\n<style type=\"text/css\">\n");
            sb.append("<![CDATA[\n");
            sb.append("line {");
            sb.append(sSVG.props.getProperty("strokeCss"));
            sb.append(" }\n");
            sb.append("polyline, path { ");
            sb.append(sSVG.props.getProperty("strokeCss"));
            sb.append(" fill:none; }\n");
            sb.append("]]>\n");
            sb.append("</style>\n");
            
            // define runes
            sb.append("\n<defs>\n");
            for(Integer sRdbID : sSVG.runes.keySet()) {
            	SirvidRune sR = sSVG.runes.get(sRdbID);
            	sb.append("<g id=\"r");
            	sb.append(sRdbID);
            	sb.append("\">\n");
            	sb.append(sR.getSvgContent());
            	sb.append("</g>\n");
            }
            sb.append("</defs>\n\n");
            
            // scaling
            sb.append("<g transform=\"scale(");
            sb.append(sSVG.props.getProperty("zoom"));
            sb.append(")\">\n");
            
            int m = -1;
            for(SirvidMonth sM : sSVG.months) {
                
                m++;
                int yBefore = m * sSVG.calculateY(SirvidSVG.DIM.Y_TOTAL);
                int wdHeight = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_WEEKDAYSHEIGHT);
                
                // month lines
                int maxX = sM.getMaxX();
                sb.append("\n<g><title>");
                sb.append(monthFormat.format(new Date(sM.month.getTimeInMillis())));
                sb.append("</title>");
                
                int y = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_MONTHLINEHEIGHT);
                sb.append("<line y1=\"");
                sb.append(y);
                sb.append("\" y2=\"");
                sb.append(y);
                sb.append("\" x1=\"");
                sb.append(SirvidSVG.widths.get(SirvidSVG.DIM.X_MARGIN) - SirvidSVG.widths.get(SirvidSVG.DIM.X_MONTHLINESEXTENSION));
                sb.append("\" x2=\"");
                sb.append(maxX + SirvidSVG.widths.get(SirvidSVG.DIM.X_MONTHLINESEXTENSION));
                sb.append("\"/>");
                
                y = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_MONTHLINEHEIGHT2);
                sb.append("<line y1=\"");
                sb.append(y);
                sb.append("\" y2=\"");
                sb.append(y);
                sb.append("\" x1=\"");
                sb.append(SirvidSVG.widths.get(SirvidSVG.DIM.X_MARGIN) - SirvidSVG.widths.get(SirvidSVG.DIM.X_MONTHLINESEXTENSION));
                sb.append("\" x2=\"");
                sb.append(maxX + SirvidSVG.widths.get(SirvidSVG.DIM.X_MONTHLINESEXTENSION));
                sb.append("\"/>");
                
                sb.append("</g>\n");
                
            	for(SirvidDay sD : sM.days){
            		
            		//weekdays
            		sb.append("\n<g transform=\"translate(");
            		sb.append(sD.beginX);
            		sb.append(" ");
            		sb.append(wdHeight);
            		sb.append(")\">\n");
            		sb.append("<title content=\"structured text\">");
            		//TODO: decide the TZ format!
            		sb.append(dateFormat.format(new Date(sD.date.getTimeInMillis())));
            		sb.append("\n");
            		sb.append(SirvidSVG.commonLabels.get(sD.weekDay)[0]);
            		sb.append("</title>\n");
            		sb.append("<use xlink:href=\"#r");
            		sb.append(sD.weekDay);
            		sb.append("\"/>\n</g>\n");
            	}

            }
            
            
        }
        
        // scaling
        sb.append("\n</g>");
        
        sb.append("\n</svg>");
        return sb.toString();
    }
    
}
