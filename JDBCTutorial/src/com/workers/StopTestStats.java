package com.workers;

import java.util.Date;

public class StopTestStats {
	
	private  double drawDownPct=0f;
	private  boolean isProfitable;
	private  int ProfitableCount=0;
	private  Date currentDate;
		
	public StopTestStats() {
		super();
	}
		
	/**
	 * Creates an instance of <b>StopTestStats</b> with all parameters set.  This object is used to 
	 * to determine the cost in terms of cummulative % loss associated with all trades failing to
	 *  produce profit for each trade still profitable after the 
	 * number of days from the entry date of the trade to the date defined by <i>currentDate</i>.
	 * @param drawDownPct
	 * @param isProfitable
	 * @param currentDate
	 */
	public StopTestStats(double drawDownPct, boolean isProfitable, Date currentDate) {
		super();
		this.setCurrentDate(currentDate);
		this.setDrawDownPct(drawDownPct);
		this.setProfitable(isProfitable);
	}



	@Override
	public String toString() {
	//	return "StopTestStats [drawDownPct=" + drawDownPct
		//		+ ", ProfitableCount=" + ProfitableCount + "]";
		return "Generated " + this.getProfitableCount() + " profitable trades with a percentage loss" +
		"of " + this.getDrawDownPct() + " :  " + this.getCostPerGoodTrade() +"%";
	}

	public double getCostPerGoodTrade() {
		return this.getProfitableCount()/this.getDrawDownPct();
	}

	/**
	 * public void addTestStats(StopTestStats sts)
	 * <p>combines the loss ROI% if the trade associated with <i>sts</i> is at a loss or increments the 
	 * number of profitable trades for this portfolio by one.  This is used by the <b>Portfolio</b> instance 
	 * owning the <b>mms</b> instance associated with the <b>StopTestStats</b> instance being passed.
	 * @param StopTestStats 
	 */
	public void addTestStats(StopTestStats sts) {
		if(sts.isProfitable) 
			this.ProfitableCount++;
		else 
			this.setDrawDownPct(this.getDrawDownPct()+sts.getDrawDownPct());
	}

	public void ProfitableCount(int i) {
		this.ProfitableCount += this.ProfitableCount;
	}
	
	
	
	public int getProfitableCount() {
		return this.ProfitableCount;
	}



	public void setProfitableCount(int profitableCount) {
		this.ProfitableCount = profitableCount;
	}



	public double getDrawDownPct() {
		return drawDownPct;
	}

	public void setDrawDownPct(double drawDownPct) {
		this.drawDownPct = drawDownPct;
	}

	public boolean isProfitable() {
		return isProfitable;
	}

	public void setProfitable(boolean isProfitable) {
		this.isProfitable = isProfitable;
	}


	public Date getCurrentDate() {
		return currentDate;
	}


	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

}
