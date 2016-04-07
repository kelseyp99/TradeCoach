package com.workers;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MarketCalendar {
//	protected Map<Date, Double> dailyPL;

	Date startDate, endDate;
	
	public MarketCalendar() {
		super();
//		dailyPL = new HashMap<Date, Double>();
	}
	
	public MarketCalendar(Date startDate, Date endDate) {
		super();
//		dailyPL = new HashMap<Date, Double>();
		this.startDate = startDate;
		this.endDate = endDate;
	}

//	public void MakeListOfDays (){
		/*
		 * build a complete set of bars (days) between the start and end date.  Then populate with
		 * the price data.  This is necesary to corralate with other securities or portfilio as
		 */		
/*		int barSize = 1;
		switch(barSize)
		{
		case 1:
			int year = 1900 + endDate.getYear();
			int month = endDate.getMonth() + 1;
			int day = endDate.getDay();  
			day = 30;  
			GregorianCalendar calendar = new GregorianCalendar(year, month, day);
			Date theDay = endDate;
			while (startDate.before(theDay)) {
					calendar.add(Calendar.DATE, -1);
					theDay = calendar.getTime();
					dailyPL.put( theDay, 0.00d);
				}
			}
	//	break;
	}
	*/
/*
	public Map<Date, Double> getDailyPL() {
		return dailyPL;
	}
	
	@Override
	public String toString() {		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat ef = new DecimalFormat("#.##");
		String sDate;
		
		String s = "Date               Change\n" +
	               "-------------------------\n\n";
				
		Iterator<Date> keySetIterator = this.dailyPL.keySet().iterator();

		while(keySetIterator.hasNext()){
		  Date key = keySetIterator.next();
		  sDate = df.format(key);
		  s = s + sDate + "      :      $" + ef.format(dailyPL.get(key)) + "\n";
		}		
		return s;		
	}
*/
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
