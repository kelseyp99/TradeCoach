package com.tradecoach.patenter.entity.security;

import java.util.Date;

import com.utilities.GlobalVars.*;
import com.workers.Tools;

public class Stop extends ExitOrder implements IProfitExit{
	
	public Stop(typeOrder iOT) {}

	public Stop() {}

	public Stop(Order oldOrder) {
		super(oldOrder);
	}

	public void activate() {        
        if (this.getParentOrder().isFilled())  {
        	super.activate();
        }	
	}

	@Override
	public boolean compareToCandleStick(CandleStick cs) {
		if(this.isFilled()) return false;
		if(Tools.isSameDayOrLater(cs.getDate(),this.getOrderDate())&&
		   Tools.isSameDayOrLater(cs.getDate(),this.getParentOrder().getOrderDate())){
			if(this.getDirection()==typeSignal.Sell) {
				//if this is a LONG position...
				if(this.isInActive() && this.getTriggerPrice()<=cs.getHighPrice())
					//...but has not been triggered as active, and the price rose above trigger price
					this.activate();//...then make the order active...
				else if(this.isActive() && this.getPrice()>=cs.getLowPrice()) {//...if order is active...
					this.setFilledCS(cs);
					this.getPartOf().setaStopFiled();
					this.filled();
					return true;
				}
			}
			else {//if this is a SHORT position..
				//if this is a LONG position...
				if(this.isInActive() && this.getTriggerPrice()>=cs.getLowPrice())
					//...but has not been triggered as active, and the price dropped below trigger price
					this.activate();//...then make the order active...
				else if(this.isActive() && this.getPrice()>=cs.getLowPrice()){//...if order is active...
					this.setFilledCS(cs);
					this.filled();
					return true;				
				}
			}
		}
		return false;
	}



}
