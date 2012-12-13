package ee.alkohol.juks.sirvid.exporters;

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
        sb.append("<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" >\n");
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
        	String strokes = "stroke: " + sSVG.props.getProperty("strokeColor") + "; stroke-width : " + sSVG.props.getProperty("strokeWidth") + ";";
            sb.append("<style type=\"text/css\">\n");
            sb.append("<![CDATA[\n");
            sb.append("line {");
            sb.append(strokes);
            sb.append(" }\n");
            sb.append("polyline, path { ");
            sb.append(strokes);
            sb.append(" fill: none; }\n");
            sb.append("]]>\n");
            sb.append("</style>\n");
            
        }
        
        sb.append("</svg>");
        return sb.toString();
    }
    
}
