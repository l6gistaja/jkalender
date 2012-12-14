package ee.alkohol.juks.sirvid.exporters;

import java.text.SimpleDateFormat;

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
        	
        	SimpleDateFormat dateFormat = new SimpleDateFormat(sSVG.props.getProperty("sdfDate"));
        	SimpleDateFormat monthFormat = new SimpleDateFormat(sSVG.props.getProperty("sdfMonth"));
        	
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
            	sb.append("<g id=\"");
            	sb.append(sR.getFilename());
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
                SirvidDay lastDay = sM.days.get(sM.days.size()-1);
                int maxX = lastDay.beginX + sSVG.runes.get(lastDay.weekDay).getWidth();
                sb.append("\n<g>\n<title>");
                sb.append(monthFormat.format(sM.month));
                sb.append("</title>\n");
                
                int y = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_MONTHLINEHEIGHT);
                sb.append("<line y1=\"");
                sb.append(y);
                sb.append("\" y2=\"");
                sb.append(y);
                sb.append("\" x1=\"");
                sb.append(SirvidSVG.widths.get(SirvidSVG.DIM.X_MARGIN));
                sb.append("\" x2=\"");
                sb.append(maxX);
                sb.append("\"/>");
                
                y = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_MONTHLINEHEIGHT2);
                sb.append("<line y1=\"");
                sb.append(y);
                sb.append("\" y2=\"");
                sb.append(y);
                sb.append("\" x1=\"");
                sb.append(SirvidSVG.widths.get(SirvidSVG.DIM.X_MARGIN));
                sb.append("\" x2=\"");
                sb.append(maxX);
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
            		sb.append(dateFormat.format(sD.date));
            		sb.append("\n");
            		sb.append(sSVG.weekDays[sD.weekDay]);
            		sb.append("</title>\n");
            		sb.append("<use xlink:href=\"#");
            		sb.append(sSVG.runes.get(new Integer(sD.weekDay)).getFilename());
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
