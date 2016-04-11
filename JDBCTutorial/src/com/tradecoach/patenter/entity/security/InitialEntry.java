package com.tradecoach.patenter.entity.security;

import java.util.Date;

import com.workers.Tools;




public class InitialEntry extends Order {
	

	public InitialEntry() {
		super();
	}

	public InitialEntry(typeOrder typeOrder) {
		super(typeOrder);
	}

	public InitialEntry(Order oldOrder) {
		super(oldOrder);
		//super.setTradePart(GlobalVars.tradePart.Entry);
	}

	@Override
	public boolean compareToCandleStick(CandleStick cs) {
		if(this.isFilled()) return false;
		if(Tools.isSameDayOrLater(cs.getDate(),this.getOrderDate())){
			//if this is a LONG position...
			if(this.getDirection()==typeSignal.Buy && this.getPrice()>=cs.getLowPrice()) {
				return this.filledPlus(cs);
			}
			 //if this is a SHORT position...
			else if(this.getDirection()==typeSignal.Sell && this.getPrice()<=cs.getHighPrice()) {
				return this.filledPlus(cs);
				}
		}
		return false;
	}

	
	
	
	

	


		
	
	}
