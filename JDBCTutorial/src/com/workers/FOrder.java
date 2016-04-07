package com.workers;
import com.utilities.*;

public class FOrder implements GlobalVars{
	int iOT;
	public static Order getObject(  typeOrder iOT)  {
		switch (iOT) { 
			case Limit: return (Order) new InitialEntry(iOT); 
			case Market: return (Order) new InitialEntry(iOT); 
			case StopLoss: return (Order) new StopLoss(iOT); 
			case SimpleExit: return (Order) new SimpleExit(iOT); 
			case Stop: return (Order) new Stop(iOT); 
			case TrailingStop: return (Order) new TrailingStop(iOT); 
		}
		return null;
	}
    
    /**
     * <p>returns a clone of the <i>oldOrder</i> of the type of order: <i>Market, Limit, StopLoss, SimpleExit, Stop, StopLimit, TrailingStop, MOC, LOC</i>
     * and changes the newly cloned order's orderType to <i>iOT</i></p>
     * @param 
     * @return Order
     * 
     */ 	
	public static Order getObject(  typeOrder iOT, Order oldOrder)  {
		Order order;
		switch (iOT) { 
			case Limit: order = (Order) new InitialEntry(oldOrder); 
			case Market: order =   (Order) new InitialEntry(oldOrder); 
			case StopLoss: order =  (Order) new StopLoss(oldOrder); 
			case SimpleExit: order =  (Order) new SimpleExit(oldOrder); 
			case Stop: order =  (Order) new Stop(oldOrder); 
			case TrailingStop: order =  (Order) new TrailingStop(oldOrder); 
			order.setTypeOrder(iOT);
			return order;
		}
		return null;
	}
	
	
	
}
