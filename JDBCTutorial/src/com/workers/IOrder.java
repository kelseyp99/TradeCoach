package com.workers;
import java.util.Date;

import com.utilities.*;
import com.utilities.GlobalVars.*;

public interface IOrder {
	abstract void executeOrder() ;

	abstract double getTriggerPrice();

	abstract double getPrice();

	abstract boolean isFilled();

	abstract CandleStick getFilledCS();

	abstract orderStatus getOrderStatus();

	abstract void placeOrder(double stopTriggerPrice, double stopPrice, int i,
			Date startDate, orderLife goodtillcancelled, typeOrder stop,
			orderStatus inactive, MoneyMgmtStrategy moneyMgmtStrategy,
			Order order);
	
	abstract void placeOrder(double entryPrice, int orderQty, Date startDate,
			orderLife goodtillcancelled, typeOrder limit, orderStatus active,
			MoneyMgmtStrategy moneyMgmtStrategy);

	abstract void setPrice(double d);

	abstract void activate();

	abstract int getQuantity();

	abstract Date getOrderDate();

	abstract typeSignal getDirection();



	abstract int getHighestHighPriceVolume();

	abstract Date getHighestHighPriceDate();

	abstract double getLowestLowPrice();

	abstract int getLowestLowPriceVolume();

	abstract Date getLowestLowPriceDate();

	abstract double getHighestHighPrice();

}
