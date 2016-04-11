/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tradecoach.patenter.entity.security;

import java.util.Date;

import com.utilities.GlobalVars.*;
import com.workers.MoneyMgmtStrategy;
import com.workers.Tools;



public class StopLoss extends ExitOrder implements IStopLossExit {

	public StopLoss() {	}	
	
	public StopLoss(typeOrder typeOrder) {
		super(typeOrder);
	}


	public StopLoss(Order oldOrder) {
		super(oldOrder);
	}

	public void placeOrder(double stopTriggerPrice, double stopPrice, int i,
			Date startDate, orderLife goodtillcancelled,
			typeOrder stop,
			orderStatus inactive,
			MoneyMgmtStrategy moneyMgmtStrategy, StopLoss order) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean compareToCandleStick(CandleStick cs) {
		if(this.isFilled()) return false;
		if(Tools.isSameDayOrLater(cs.getDate(),this.getOrderDate())){
			if(this.getDirection()==typeSignal.Sell && this.getPrice()>= cs.getLowPrice()) {
				this.setFilledCS(cs);
				this.filled();
				this.getPartOf().setaStopFiled();
				//if this is a LONG position...hi dad
				return true; 
			}
			else if(this.getDirection()==typeSignal.Buy && this.getPrice()<=cs.getHighPrice()) {//if this is a SHORT position... 
				this.setFilledCS(cs);
				this.filled();
				//if this is a LONG position...hi dad
				return true; 
			}
		}
		return false;
	}
}
    
    
    

