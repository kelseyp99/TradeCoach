package com.workers;

import com.utilities.GlobalVars.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.workers.PortfoliosGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

import com.tradecoach.patenter.entity.security.CandleStick;
import com.tradecoach.patenter.entity.security.CandleSticks;
import com.tradecoach.patenter.entity.security.EntityBean;
import com.tradecoach.patenter.entity.security.IEntityBean;
import com.utilities.GlobalVars;

public abstract class PriceCollection extends EntityBean implements GlobalVars, IEntityBean {
 	private int tradingDays;
	private double meanPL, variancePL, stdDevPL, portfolioValue =0;
	protected Map<Date, Double> dailyPL;
	Date startDate, endDate;
        @Resource( name="jdbc/APPDATA" )
		private
        DataSource dataSource;
	
	public PriceCollection(MarketCalendar mc) {
		super();
		this.startDate = mc.getStartDate();
		this.endDate = mc.getEndDate();
        this.intialize();
	//	dailyPL = new HashMap<Date, Double>();
	//	buildCalendar();
	}
	
	public PriceCollection(Date startDate, Date endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
        this.intialize();
	}

	public PriceCollection() {
		// TODO Auto-generated constructor stub
	}

	protected void intialize() {
		dailyPL = new HashMap<Date, Double>();
		buildCalendar();	
	}

	public void buildCalendar() {
		//make a list of all days in time sequence (even weekends)
	//	mc = new MarketCalendar(startDate, endDate);
		MakeListOfDays();
	}
	
	public void AddPriceData(ArrayList<SecurityInst> hs){
		//this is called from a Portfolio to loop through each security and call the overloaded version below.
		double x=0;
	//	for (int i = 0; i < hs.size(); i++) hs.get(i).RunStats();
		for (int i = 0; i < hs.size(); i++) {	
			//add this security's price action to the portfolio object
			AddPriceData(hs.get(i).getCandleSticks(), hs.get(i).getPosition());	
			/*for this security, multiply the last closing price by the # of shares and tally to get a running total of 
			  the overall portfolio value*/
		//	x += hs.get(i).getCandleSticks().getLast().getClosePrice() * hs.get(i).getPosition() ;
			x += hs.get(i).getLastClosePrice() * hs.get(i).getPosition() ;
			//add this security's price action to this security's object.
			hs.get(i).AddPriceData(hs.get(i).getCandleSticks(), hs.get(i).getPosition());
		//	hs.get(i).RunStats();
		} //for
		this.setPortfolioValue(x);
		this.RunStats();
	}

	/*  this is used by the candidate universe class and called from each of its security class instances so 	 
	    as to not regard any holding number.  Thus the number of shares is defaulted to 1	 */
	public void AddPriceData(CandleSticks cs) { AddPriceData(cs, 1); };
	
	public void AddPriceData(CandleSticks cs, int s){
	   Iterator<CandleStick> i = cs.getCandleSticks().iterator();
	   double close = 0.0, priorClose = 0.0, change = 0.0;
	   CandleStick c;
	   Date date = endDate, lastDate;
	   
	   if (DEBUG) {
	       System.out.println("\n\nCurrent Date       Prior Date      Closing      Shares   Change   Value in\n " +
	                          "                                                                  DailyPL");
	       System.out.println("---------------------------------------------------------------------------\n");	
	   }
	   while (i.hasNext()) {
		/*'i' is the holdings object of this portfolio class instance
		   loop through each instrument and then then each daily candlestick
		   and add/subtract its price change to the overall daily P/L of the portfolio.		   
		   NOTE:  loop is from going from most recent date and moving backwards*/ 		   
	       priorClose = close;
		   c = i.next();
		   close = c.getClosePrice();
		   //if prior close was zero, assume market was closed
		   if (priorClose == 0) continue;
		   //if the current and last price a extremely different, assume this is an outlyer and throw out
		   //this would also be the case in a stock split,  make the P/L zzero for that day
		   if ((priorClose/close) > 2 || (close/priorClose) > 2) {
			   change = 0.00d;
		   }
		   else {
			   change = priorClose - close;			   
		   }
			   
		   lastDate = date;
		   date = c.getDate();
		   setDailyPL(lastDate, change * s);
		   if (DEBUG) { System.out.println(df.format(date) + "         " + df.format(lastDate) +
				        "       " + ef.format(priorClose) +  "         " + s + "       " + ef.format(change * s) +  "      " + ef.format(dailyPL.get(lastDate))
				   );
		   }
       } //while i  
	   RunStats();  
	 } //AddPriceData
        
    	public void RunStats() {
		/*
		 * for this portfolio or individual security instrument, calculate the needed statistical values.
		 */
		   double sum = 0.0d;
	//	   double lastPL = 0.0d;
		   int size = 0;
		   for (Double d : dailyPL.values()) {
			   if(d != 0d) {
			        sum += d;
			        size++;
		//	        lastPL = d;
			   } //if
		   } //for
		   this.setTradingDays(size);
		  // int size = dailyPL.size();
		//		   int size = 250;	   
		   double mean = sum/size;
		   this.setMeanPL(mean);
		   double temp = 0;
	       for(Double d : dailyPL.values()) temp += (mean-d)*(mean-d);
	       Double variance = temp/size; 
	       setVariancePL(variance);
	       Double sd = Math.sqrt(variance);
	       setStdDevPL(sd)	;
	 //      setVaR95(mean - sd * 1.960);
	  //     setVaR99(mean - sd * 2.576);	   
	}//RunStats
	
	public void setDailyPL(Date marketDate, double change) {
		/*
		 * increase/decrease the Profit/Loss value for the portfolio
		 * for this day
		 */
	//	mc.dailyPL.put(date, mc.dailyPL.get(date) + change);
		double x = 0.00d;
		
		if (dailyPL.containsKey(marketDate)) {
			x = dailyPL.get(marketDate);
		}
		
		x += change;
		//!!! this is not dailyLoss so dont check for values there and think its failing!!!
		dailyPL.put(marketDate, x);
	}

	public double[] getDailyPL_asDouble() {
	//	Double[] dd = (Double[])(Object[])dailyLoss.values().toArray();
		if (DEBUG) System.out.println("Value in\ndailyPL\n\n");		
		Map<Date,Double> treeMap = new TreeMap<Date, Double>(getDailyPL());
		List<Double> dl = new ArrayList<Double>(treeMap.values());
		double[] dd = new double[dl.size()];
	//	for (int i = 0; i < dl.size(); i++) {
		for (int i = dl.size()-1; i >= 0; i--) {
			dd[i] = dl.get(i).doubleValue();
			if (DEBUG) System.out.println(ef.format(dd[i]));
			}
		return dd;
			//	Double[] dd = (Double[]) dl.toArray();
	//	Double[] dd = null;
	//	(for int i; i < 
		
	//	HashMap map = new HashMap();
	/*	Object[][] arr = new Object[dailyLoss.size()][2];
		Set<Entry<Date, Double>> entries = dailyLoss.entrySet();
		Iterator<Entry<Date, Double>> entriesIterator = entries.iterator();

		int i = 0;
		while(entriesIterator.hasNext()){

		    Entry<Date, Double> mapping = entriesIterator.next();

		    arr[i][0] = mapping.getKey();
		    arr[i][1] = mapping.getValue();
		    dd[i] = (Double) arr[i][1];

		    i++;
		}
		*/
		//double[] primitives = ArrayUtils.toPrimitive(dd);			
		//return primitives;	
	}
	
	public void MakeListOfDays (){
		/*
		 * build a complete set of bars (days) between the start and end date.  Then populate with
		 * the price data.  This is necessary to correlate with other securities or portfilio as
		 */		
		int barSize = 1;
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
	
	public double getMeanPL() {
		return meanPL;
	}


	public void setMeanPL(double meanPL) {
		this.meanPL = meanPL;
	}


	public double getVariancePL() {
		return variancePL;
	}


	public void setVariancePL(double variancePL) {
		this.variancePL = variancePL;
	}


	public double getStdDevPL() {
		return stdDevPL;
	}


	public void setStdDevPL(double stdDevPL) {
		this.stdDevPL = stdDevPL;
	}

	public double getPortfolioValue() {
		return portfolioValue;
	}

	public void setPortfolioValue(double portfolioValue) {
		this.portfolioValue = portfolioValue;
	}

	public Map<Date, Double> getDailyPL() {
		return dailyPL;
	}

	public void setDailyPL(Map<Date, Double> dailyPL) {
	//	this.dailyPL = dailyPL;
		//create a new object as Java must pass 'dailyPL' by value of reference to the map
		//class instance.  This seems to accomplish a pass by value to the clone
		this.dailyPL =  new HashMap<Date, Double> (dailyPL);
	}

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

	public int getTradingDays() {
		return tradingDays;
	}

	public void setTradingDays(int tradingDays) {
		this.tradingDays = tradingDays;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
}
