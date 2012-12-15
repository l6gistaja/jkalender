package ee.alkohol.juks.sirvid.containers.graphics;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SirvidDay {
	
	public GregorianCalendar date;
	public int weekDay;
	public int beginX;
	public GregorianCalendar sunrise;
	public GregorianCalendar sunset;
	public GregorianCalendar moonphase;
	public int moonphaseID = 0;
	
	public SirvidDay(GregorianCalendar day, int beginX) {
		this.date = (GregorianCalendar) day.clone();
		this.beginX = beginX;
		this.weekDay = day.get(Calendar.DAY_OF_WEEK) - 1;
	}
	
}
