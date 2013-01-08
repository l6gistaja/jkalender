package ee.alkohol.juks.sirvid.containers.graphics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ee.alkohol.juks.sirvid.containers.ical.ICalculator;

public class SirvidDay {
	
	public GregorianCalendar date;
	public int weekDay;
	public int beginX;
	public GregorianCalendar sunrise;
	public GregorianCalendar sunset;
	public GregorianCalendar moonphase;
	public int moonphaseID = 0;
	public ArrayList<SirvidFeast> feasts = new ArrayList<SirvidFeast>();
	public StringBuilder weekdayLabel = new StringBuilder();
	public StringBuilder moonphaseLabel = new StringBuilder();
	public boolean today = false;
	
	public SirvidDay(GregorianCalendar day, int beginX) {
		this.date = (GregorianCalendar) day.clone();
		this.beginX = beginX;
		this.weekDay = day.get(Calendar.DAY_OF_WEEK) - 1;
		if(ICalculator.getCalendar(ICalculator.UTC_TZ_ID).equals(this.date)) { today = true; }
	}
	
}
