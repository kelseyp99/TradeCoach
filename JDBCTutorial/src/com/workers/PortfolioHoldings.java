package com.workers;

import java.io.Serializable;
import java.util.Date;

public class PortfolioHoldings implements Serializable{
	
	private String tickerSymbol ;
	private String portfolioName ;
	private Date oldestOrderDate ;
	private boolean selected ;
	
	public PortfolioHoldings() {}

	public String getTickerSymbol() {
		return tickerSymbol;
	}

	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public Date getOldestOrderDate() {
		return oldestOrderDate;
	}

	public void setOldestOrderDate(Date oldestOrderDate) {
		this.oldestOrderDate = oldestOrderDate;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean getSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	
}
