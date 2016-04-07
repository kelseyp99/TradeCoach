package com.junk;
public class Portfolio {
   private String portfolioName;  
	public Portfolio() {}
	   public Portfolio(String fname) {
	      this.setPortfolioName(fname);
	   }
	public String getPortfolioName() {
		return portfolioName;
	}
	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}
	  
	}

