package ee.alkohol.juks.sirvid.exporters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ee.alkohol.juks.sirvid.containers.graphics.SirvidDay;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidMonth;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidRune;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidSVG;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidSVG.DIM;
import ee.alkohol.juks.sirvid.containers.ical.ICalEvent;
import ee.alkohol.juks.sirvid.containers.ical.ICalculator;
import ee.alkohol.juks.sirvid.containers.ical.ICalendar;
import ee.alkohol.juks.sirvid.containers.ical.ICalendar.Keys;

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
        sb.append("</title>\n");
        
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
        	SimpleDateFormat timeFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfTime"));
        	timeFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        	
        	double mfZoomRatio = SirvidSVG.widths.get(DIM.Y_MOONPHASESHEIGHT).doubleValue() / SirvidSVG.widths.get(DIM.Y_WEEKDAYSHEIGHT).doubleValue();
        	double feastsZoomRatio = SirvidSVG.widths.get(DIM.Y_FEASTSHEIGHT).doubleValue() / SirvidSVG.widths.get(DIM.Y_WEEKDAYSHEIGHT).doubleValue();
        	
        	SirvidSVG.DIM[] monthLines = {SirvidSVG.DIM.Y_MONTHLINEHEIGHT, SirvidSVG.DIM.Y_MONTHLINEHEIGHT2};
        	
        	// stylesheet 
            sb.append("\n<style type=\"text/css\">\n");
            sb.append("<![CDATA[\n");
            sb.append(generateCssClass(SirvidSVG.props.getPropertyInt("strokeWidth"), "g"));
            sb.append(generateCssClass(SirvidSVG.props.getPropertyInt("strokeWidth")/mfZoomRatio, "m"));
            sb.append(generateCssClass(SirvidSVG.props.getPropertyInt("strokeWidth")/feastsZoomRatio, "f"));
            sb.append("]]>\n");
            sb.append("</style>\n");
            
            
            // define runes
            sb.append("\n<defs>\n");
            for(Integer sRdbID : SirvidSVG.runes.keySet()) {
            	SirvidRune sR = SirvidSVG.runes.get(sRdbID);
            	sb.append("<g id=\"r");
            	sb.append(sRdbID);
            	sb.append("\">\n");
            	sb.append(sR.getSvgContent());
            	sb.append("</g>\n");
            }
            sb.append("</defs>\n\n");
            
            // scaling
            sb.append("<g transform=\"scale(");
            sb.append(SirvidSVG.props.getProperty("zoom"));
            sb.append(")\">\n");
            
            int m = -1;
            for(SirvidMonth sM : sSVG.months) {
                
                m++;
                int yBefore = m * sSVG.calculateY(SirvidSVG.DIM.Y_TOTAL);
                int wdHeight = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_WEEKDAYSHEIGHT);
                int mfHeight = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_MOONPHASESHEIGHT);
                int feastsHeight = yBefore + sSVG.calculateY(SirvidSVG.DIM.Y_FEASTSHEIGHT);
                
                // month lines
                int maxX = sM.getMaxX();
                sb.append("\n<g class=\"g\"><title>");
                sb.append(monthFormat.format(new Date(sM.month.getTimeInMillis())));
                sb.append("</title>");
                
                for(SirvidSVG.DIM monthLine : monthLines) {
                    int y = yBefore + sSVG.calculateY(monthLine);
                    sb.append(generateLine(
                            SirvidSVG.widths.get(SirvidSVG.DIM.X_MARGIN) - SirvidSVG.widths.get(SirvidSVG.DIM.X_MONTHLINESEXTENSION), 
                            y, 
                            maxX + SirvidSVG.widths.get(SirvidSVG.DIM.X_MONTHLINESEXTENSION), 
                            y));
                }
                
                sb.append("</g>\n");
                
            	for(SirvidDay sD : sM.days){
            	    
            	    StringBuilder transformSB = new StringBuilder();
            	    transformSB.append("translate(");
            	    transformSB.append(sD.beginX);
            	    transformSB.append(" ");
            	    transformSB.append(wdHeight);
            	    transformSB.append(")");
            	    
            	    StringBuilder titleSB = new StringBuilder();
            	    titleSB.append(dateFormat.format(new Date(sD.date.getTimeInMillis())));
            	    titleSB.append("\n");
            	    titleSB.append(SirvidSVG.commonLabels.get(sD.weekDay)[0]);
                    if(sD.sunrise != null) {
                        titleSB.append("\n");
                        titleSB.append(timeFormat.format(new Date(sD.sunrise.getTimeInMillis())));
                        titleSB.append(" ");
                        titleSB.append(SirvidSVG.commonLabels.get(ICalculator.DbIdStatuses.SUNRISE.getDbId())[0]);
                    }
                    if(sD.sunset != null) {
                        titleSB.append("\n");
                        titleSB.append(timeFormat.format(new Date(sD.sunset.getTimeInMillis())));
                        titleSB.append(" ");
                        titleSB.append(SirvidSVG.commonLabels.get(ICalculator.DbIdStatuses.SUNSET.getDbId())[0]);
                    }
                    
                    sb.append(generateRune(sD.weekDay, titleSB.toString(), "g", transformSB.toString()));
            		
            		// moonphases
            		if(mfZoomRatio > 0 && sD.moonphase != null ) {
            		    
            		    StringBuilder sbWtitle = new StringBuilder();
            		    sbWtitle.append(dateFormat.format(new Date(sD.moonphase.getTimeInMillis())));
            		    sbWtitle.append(" ");
            		    sbWtitle.append(timeFormat.format(new Date(sD.moonphase.getTimeInMillis())));
            		    sbWtitle.append("\n");
            		    sbWtitle.append(SirvidSVG.commonLabels.get(sD.moonphaseID)[0]);
                        if(ICalculator.isNotEmptyStr(SirvidSVG.commonLabels.get(sD.moonphaseID)[1])){
                            sbWtitle.append("\n");
                            sbWtitle.append(SirvidSVG.commonLabels.get(sD.moonphaseID)[1]);
                        }
                        sb.append(generateMoveable(sD, sD.moonphaseID, mfZoomRatio, mfHeight, sbWtitle.toString(), "m"));
            		}
            		
            		// feasts
            		if(feastsZoomRatio > 0 && !sD.feasts.isEmpty()) {
            		    
            		    ICalEvent feast = sD.feasts.get(0);
            		    StringBuilder fTitle = new StringBuilder();
            		    fTitle.append(dateFormat.format(new Date( ((GregorianCalendar)feast.properties.get(ICalendar.Keys.EVENT_START).value).getTimeInMillis() )));
            		    if(feast.dbID == ICalculator.DbIdStatuses.SOLSTICE.getDbId()) {
            		        fTitle.append(" ");
            		        fTitle.append(timeFormat.format(new Date( ((GregorianCalendar)feast.properties.get(ICalendar.Keys.EVENT_START).value).getTimeInMillis() )));
            		    }
            		    fTitle.append("\n");
                        fTitle.append(feast.properties.get(ICalendar.Keys.SUMMARY).value);
                        sb.append(generateMoveable(sD, feast.dbID, feastsZoomRatio, feastsHeight, fTitle.toString(), "f"));
                        
            		}
            		
            	}

            }
            
            
        }
        
        // scaling
        sb.append("\n</g>");
        
        sb.append("\n</svg>");
        return sb.toString();
    }
    
    private String generateCssClass(double strokeZoom) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("{ ");
    	sb.append(SirvidSVG.props.getProperty("commonCss"));
    	sb.append(" stroke-width:");
    	sb.append(strokeZoom);
    	sb.append("; }\n");
		return sb.toString();
    }
    
    private String generateCssClass(double strokeZoom, String className) {
        StringBuilder sb = new StringBuilder();
        sb.append(".");
        sb.append(className);
        sb.append(", .");
        sb.append(className);
        sb.append(" ");
        sb.append(generateCssClass(strokeZoom));
        sb.append(".");
        sb.append(className);
        sb.append(":hover ");
        sb.append(SirvidSVG.props.getProperty("hoverCss"));
        sb.append("\n");
        return sb.toString();
    }
    
    private String generateMoveable(SirvidDay sD, int dbID, double zoomRatio, int y0, String title, String cssClass) {
        StringBuilder transform = new StringBuilder();
        transform.append("translate(");
        transform.append(sD.beginX 
                + SirvidSVG.runes.get(SirvidSVG.eventsVsRunes.get(sD.weekDay)).getCx() 
                - SirvidSVG.runes.get(SirvidSVG.eventsVsRunes.get(dbID)).getCx() * zoomRatio);
        transform.append(" ");
        transform.append(y0);
        transform.append(") scale(");
        transform.append(zoomRatio);
        transform.append(")");
        return generateRune(dbID, title, cssClass, transform.toString());
    }
    
    private String generateRune(int dbID, String title, String cssClass, String transform) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n<g class=\"");
        sb.append(cssClass);
        sb.append("\"");
        if(transform != null && !transform.trim().equals("")) {
            sb.append(" transform=\"");
            sb.append(transform);
            sb.append("\"");
        }
        sb.append(">\n<title content=\"structured text\">");
        sb.append(title);
        sb.append("</title>\n");
        sb.append("<use xlink:href=\"#r");
        sb.append(SirvidSVG.eventsVsRunes.get(dbID));
        sb.append("\"/>\n</g>\n");
        return sb.toString();
    }
    
    private String generateLine(double x1, double y1, double x2, double y2) {
        StringBuilder sb = new StringBuilder();
        sb.append("<line x1=\"");
        sb.append(x1);
        sb.append("\" y1=\"");
        sb.append(y1);
        sb.append("\" x2=\"");
        sb.append(x2);
        sb.append("\" y2=\"");
        sb.append(y2);
        sb.append("\"/>");
        return sb.toString();
    }
    
}
