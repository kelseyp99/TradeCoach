package com.workers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.table.AbstractTableModel;
import org.joda.time.DateTime;

import com.gui.GUI;
import com.tradecoach.patenter.entity.security.CandleSticks;
import com.tradecoach.patenter.entity.security.Portfolio;
import com.tradecoach.patenter.entity.security.Securities;
import com.utilities.GlobalVars.typeOrder;
@Entity
@Table(name = "trade_history")
public class TransactionData {	
	@Id 
	@SequenceGenerator(name="identifier", sequenceName="trade_history_id_seq",allocationSize=1) 
	@GeneratedValue(strategy=GenerationType.SEQUENCE,	generator="identifier")
	@Column(name = "id") private int id ;
	@ManyToOne(cascade = CascadeType.ALL) @JoinColumn(name = "security_id") private Securities security;
	//@ManyToOne private Securities security;
	@ManyToOne(cascade = CascadeType.ALL) @JoinColumn(name = "portfolio_id") private Portfolio portfolio;
	//@ManyToOne private Portfolio portfolio;
	@Column(name = "ticker_symbol")private String tickerSymbol;
	@Column(name = "instrumentname")private String instrumentName;
	@Column(name = "portfolio_name")private String portfolioName;
	private typeOrder stopType;
	private Date orderDate, stopActivationDate;
	private Double entryPrice,stopLoss,stop, stopTrigger;
	private Integer tradeID;
	private boolean selected;
	@Column(name = "shares_traded")private Integer position ;
	@Transient private SecurityInst securityInst;
	@Transient private boolean hasChanged = false;

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

