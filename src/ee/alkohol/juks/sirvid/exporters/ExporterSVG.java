package ee.alkohol.juks.sirvid.exporters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ee.alkohol.juks.sirvid.containers.graphics.SirvidDay;
import ee.alkohol.juks.sirvid.containers.graphics.SirvidFeast;
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
        
        SirvidSVG sSVG;
		sSVG = new SirvidSVG(iC);

        StringBuilder sb = new StringBuilder();
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
        sb.append("<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
        sb.append("<title>");
        sb.append(iC.iCal.iCalBody.get(ICalendar.Keys.CALENDAR_NAME).value);
        sb.append("</title>\n");
        
        if(sSVG.errorMsgs.size() > 0) {
        	
            sb.append(SirvidSVG.props.getProperty("errorTxtTags0"));
            sb.append("\n<title content=\"structured text\">CLASSPATH = ");
            sb.append(System.getProperty("java.class.path"));
            sb.append("</title>\n");
            for(String errMsg : sSVG.errorMsgs) {
                sb.append(errMsg);
                sb.append("\n");
            }
            sb.append(SirvidSVG.props.getProperty("errorTxtTags1"));
            
        } else {
            
            SimpleDateFormat monthFormat = new SimpleDateFormat(SirvidSVG.props.getProperty("sdfMonth"));
            monthFormat.setTimeZone(TimeZone.getTimeZone(ICalculator.UTC_TZ_ID));
        	
        	SirvidSVG.DIM[] monthLines = {SirvidSVG.DIM.Y_MONTHLINEHEIGHT, SirvidSVG.DIM.Y_MONTHLINEHEIGHT2};
        	
        	// stylesheet 
            sb.append("\n<style type=\"text/css\">\n");
            sb.append("<![CDATA[\n");
            sb.append(generateCssClass(SirvidSVG.props.getPropertyInt("strokeWidth"), "g"));
            sb.append(generateCssClass(SirvidSVG.props.getPropertyInt("strokeWidth")/sSVG.mfZoomRatio, "m"));
            sb.append(generateCssClass(SirvidSVG.props.getPropertyInt("strokeWidth")/sSVG.feastsZoomRatio, "f"));
            sb.append("]]>\n");
            sb.append("</style>\n");
            
            
            // define runes
            sb.append("\n<defs>\n");
            int maxPresentRuneID = sSVG.iCalc.inputData.isCalculateMoonphases() ? ICalculator.DbIdStatuses.MOON_LAST.getDbId() : ICalculator.DbIdStatuses.MOON_NEW_M2.getDbId() - 1;
            for(Integer sRdbID : SirvidSVG.runes.keySet()) {
            	if((sRdbID > ICalculator.DbIdStatuses.UNDEFINED.getDbId() && sRdbID <= maxPresentRuneID) || sSVG.usedRunes.contains(sRdbID)) {
                	SirvidRune sR = SirvidSVG.runes.get(sRdbID);
                	sb.append("<g id=\"r");
                	sb.append(sRdbID);
                	sb.append("\">\n");
                	sb.append(sR.svgContent);
                	sb.append("</g>\n");
            	}
            }
            sb.append("</defs>\n\n");
            
            // scaling
            sb.append("<g transform=\"scale(");
            sb.append(SirvidSVG.props.getProperty("zoom"));
            sb.append(")\">\n");
            
            int yBefore = 0;
            for(SirvidMonth sM : sSVG.months) {
                
                int wdHeight = yBefore + sM.calculateY(SirvidSVG.DIM.Y_WEEKDAYSHEIGHT);
                int mfHeight = yBefore + sM.calculateY(SirvidSVG.DIM.Y_MOONPHASESHEIGHT);
                int feastsHeight = yBefore + sM.calculateY(SirvidSVG.DIM.Y_FEASTSHEIGHT);
                
                // month lines
                int maxX = sM.getMaxX();
                sb.append("\n<g class=\"g\"><title>");
                sb.append(monthFormat.format(new Date(sM.month.getTimeInMillis())));
                sb.append("</title>");
                
                for(SirvidSVG.DIM monthLine : monthLines) {
                    int y = yBefore + sM.calculateY(monthLine);
                    sb.append(SirvidSVG.generateLine(
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
            	                        
                    sb.append(generateRune(sD.weekDay, sD.weekdayLabel.toString(), "g", transformSB.toString(), ""));
            		
            		// moonphases
            		if(sSVG.mfZoomRatio > 0 && sD.moonphase != null ) {
            		    sb.append(generateMoveable(sD, sD.moonphaseID, sSVG.mfZoomRatio, mfHeight, sD.moonphaseLabel.toString(), "m", "", ""));
            		}
            		
            		// feasts
            		int xtraY;
            		if(sSVG.feastsZoomRatio > 0 && !sD.feasts.isEmpty()) {
            		    for(int i = 0; i < sD.feasts.size(); i++) {
            		        
            		        StringBuilder addToTransform = new StringBuilder();
            		        SirvidFeast feast = sD.feasts.get(i);
            		        xtraY = sM.getXtraY();
            		        
        		            if(feast.rotate != 0) {
        		                addToTransform.append(" rotate(");
                                addToTransform.append(feast.rotate);
                                addToTransform.append(" ");
                                addToTransform.append(sSVG.getRuneByDbID(feast.event.dbID).cx);
                                addToTransform.append(" ");
                                addToTransform.append(SirvidSVG.widths.get(SirvidSVG.DIM.Y_WEEKDAYSHEIGHT) + SirvidSVG.widths.get(SirvidSVG.DIM.Y_FEASTSEXTRA));
                                addToTransform.append(")");
                                xtraY = 0;
        		            }
            		            
                            sb.append(generateMoveable(sD, feast.event.dbID, sSVG.feastsZoomRatio, feastsHeight + xtraY, feast.label.toString(), "f", addToTransform.toString(), feast.xtraSVG.toString()));
            		    }
            		}
            		
            	}
            	
            	yBefore += sM.calculateY(SirvidSVG.DIM.Y_TOTAL);
            }
            
            // scaling
            sb.append("\n</g>");
        }
        
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
    
    private String generateMoveable(SirvidDay sD, int dbID, double zoomRatio, int y0, String title, String cssClass, String addToTransform, String xtraSVG) {
        StringBuilder transform = new StringBuilder();
        transform.append("translate(");
        transform.append(sD.beginX 
                + SirvidSVG.runes.get(SirvidSVG.eventsVsRunes.get(sD.weekDay)).cx 
                - SirvidSVG.runes.get(SirvidSVG.eventsVsRunes.get(dbID)).cx * zoomRatio);
        transform.append(" ");
        transform.append(y0);
        transform.append(") scale(");
        transform.append(zoomRatio);
        transform.append(")");
        transform.append(addToTransform);
        return generateRune(dbID, title, cssClass, transform.toString(), xtraSVG);
    }
    
    private String generateRune(int dbID, String title, String cssClass, String transform, String xtraSVG) {
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
        sb.append("\"/>\n");
        sb.append(xtraSVG);
        sb.append("\n</g>\n");
        return sb.toString();
    }
    
}
