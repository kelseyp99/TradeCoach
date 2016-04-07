package com.workers;

import java.util.Date;

import com.workers.Tools;

/**
 * @author Phil
 *
 */
public class SimpleExit extends ExitOrder implements IProfitExit {
	
    public SimpleExit(typeOrder typeOrder) {
		super(typeOrder);
	}

	public SimpleExit() {
		super();
	}

	public SimpleExit(Order oldOrder) {
		super(oldOrder);
		
	}
    /**
     * @return
     * set the <b>orderStatus</b> of this order to <i>Active</i> if and only if the parent order was filled
     */ 
	public void activate() {          
        if (this.getParentOrder().isFilled())  {
        	super.activate();
        }	
	}
	/** (non-Javadoc)
	 * @see common.Order#compareToCandleStick(common.CandleStick)
	 * if this order is not yet filed 
	 * 	and the CandleStick cs is dated after the date this order was placed 
	 * 	and the CandleStick price is better than or equal to the order price
	 * 		attach this CandleStick to this order for latter reference as the filled price
	 * 		and return  true
	 *  else return false
	 */
	public boolean compareToCandleStick(CandleStick cs){
		if(this.isFilled() || this.isInActive()) return false;
		if(Tools.isSameDayOrLater(cs.getDate(),this.getOrderDate())) {
		   if(Tools.isSameDayOrLater(cs.getDate(),this.getParentOrder().getFilledCS().getDate())){	
					if (this.priceWasBeaten(cs)){
						this.getPartOf().setaStopFiled();
						return this.filledPlus(cs);
					}
			}
	} return false;	}	
}
