package com.workers;
import java.io.Serializable;

public class SecurityInst2 implements Serializable {
	
	private String tickerSymbol;

	public SecurityInst2()  {
		// TODO Auto-generated constructor stub
	}
	
	public SecurityInst2(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

	public String getTickerSymbol() {
		return tickerSymbol;
	}

	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

}
