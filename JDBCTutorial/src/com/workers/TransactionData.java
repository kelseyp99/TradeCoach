package com.workers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.joda.time.DateTime;

import com.gui.GUI;
import com.utilities.GlobalVars.typeOrder;

public class TransactionData {
	private String tickerSymbol, instrumentName, portfolioName;
	private typeOrder stopType;
	private Date orderDate, stopActivationDate;
	private Double entryPrice,stopLoss,stop, stopTrigger;
	private Integer tradeID, position ;
	private SecurityInst securityInst;
	private boolean hasChanged = false, selected;

	public TransactionData() {}
	public TransactionData(String tickerSymbol, String instrumentName, typeOrder stopType, Date orderDate,
			Date stopActivationDate, Double entryPrice, Double stopLoss, Double stop, Double stopTrigger, Integer position) {
		super();
		this.tickerSymbol = tickerSymbol;
		this.instrumentName = instrumentName;
		this.stopType = stopType;
		this.orderDate = orderDate;
		this.stopActivationDate = stopActivationDate;
		this.entryPrice = entryPrice;
		this.stopLoss = stopLoss;
		this.stop = stop;
		this.stopTrigger = stopTrigger;
		this.position = position;
	}
	public boolean hasChanged() {
		return hasChanged;
	}
	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}
	public Integer getTradeID() {
		return tradeID;
	}
	public void setTradeID(Integer tradeID) {
		this.tradeID = tradeID;
	}
	public String getTickerSymbol() {
		return tickerSymbol;
	}
	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}
	public String getInstrumentName() {
		return instrumentName;
	}
	public void setInstrumentName(String instrumentName) {
		if (instrumentName == null ? this.getInstrumentName() != null : !instrumentName.equals(this.getInstrumentName())) {
			this.instrumentName = instrumentName;
			hasChanged = true;
		}
	}
	public typeOrder getStopType() {
		return stopType;
	}
	public void setStopType(typeOrder stopType) {
		this.stopType = stopType;
	}
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	public void setOrderDate(DateTime orderDate) {
		this.orderDate = new java.sql.Date(orderDate.toDate().getTime());		
	}
	public Date getStopActivationDate() {
		return stopActivationDate;
	}
	public void setStopActivationDate(Date stopActivationDate) {
		this.stopActivationDate = stopActivationDate;
	}
	public void setStopActivationDate(DateTime stopactivationDate) {
		this.stopActivationDate = new java.sql.Date(stopactivationDate.toDate().getTime());		
	}
	public Double getEntryPrice() {
		return entryPrice;
	}
	public void setEntryPrice(Double entryPrice) {
		this.entryPrice = entryPrice;
	}
	public Double getStopLoss() {
		return stopLoss;
	}
	public void setStopLoss(Double stopLoss) {
		this.stopLoss = stopLoss;
	}
	public Double getStop() {
		return stop;
	}
	public void setStop(Double stop) {
		this.stop = stop;
	}
	public Double getStopTrigger() {
		return stopTrigger;
	}
	public void setStopTrigger(Double stopTrigger) {
		this.stopTrigger = stopTrigger;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
	public void setPosition(int position) {
		this.position = position;		
	}
	public SecurityInst getSecurityInst() {
		return securityInst;
	}
	public void setSecurityInst(SecurityInst securityInst) {
		this.securityInst = securityInst;
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
	public String getPortfolioName() {
		return portfolioName;
	}
	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}
}

