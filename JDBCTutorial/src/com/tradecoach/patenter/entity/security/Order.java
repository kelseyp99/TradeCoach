package com.tradecoach.patenter.entity.security;

import com.gui.GUI;
import com.utilities.*;
import com.workers.MoneyMgmtStrategy;
import com.workers.Portfolio;
import com.workers.Portfolios;
import com.workers.Tools;
import org.joda.time.DateTime;

import java.util.Date;

import javax.swing.JFrame;

public  class Order implements GlobalVars {
    int quantity;
    double price, triggerPrice;
    CandleStick filledCS, highestHighCS, lowestLowCS;
    Date orderDate;
    private typeOrder typeOrder;
    private orderLife orderLife;
    private orderStatus orderStatus;
    private Order parentOrder;
    private Order childOf; 
    private MoneyMgmtStrategy partOf;
    /**<i>tradePart</i> determines whether this order is the entry or an exit*/
    public tradePart tradePart;
    Order dummyOrder;

    public Order(typeOrder typeOrder) {
        this.typeOrder = typeOrder;
        this.setTradePart(this);
    }

    public Order()  {}
    
    public Order(Order order) {
    	this.placeOrder(order.getTriggerPrice(), order.getPrice(), order.getQuantity(), 
    					order.getOrderDate(), order.getOrderLife(), order.getTypeOrder(),
    					order.getOrderStatus(), order.getPartOf(), order.getParentOrder());
    	this.setTradePart(this);
    }
    
    public void placeOrder(double price, int qty, Date startDate,
    						orderLife OrderLife, 
    						typeOrder TypeOrder,
    						orderStatus OrderStatus, 
    						MoneyMgmtStrategy mms ) { 
    	placeOrder(0d, price, qty, startDate, OrderLife, TypeOrder, OrderStatus, mms, null );
		this.setTradePart(this);
    }//placeOrder
    
	/**
	 * sets as an instance each parameter being passed in this signature<br><br>
	 * @param 
	 * 
	 * 
	 * */	
	public void placeOrder( double stopTriggerPrice, double price, int qty, Date startDate, 
							orderLife OrderLife,
							typeOrder TypeOrder,
							orderStatus OrderStatus, 
							MoneyMgmtStrategy mms,
							Order parentOrder ) {
		this.setTriggerPrice(stopTriggerPrice);
		this.setPrice(price);
		this.setQuantity(qty);
		this.setOrderLife(OrderLife);   
		this.setTypeOrder(TypeOrder);
		this.setOrderDate(startDate);
		this.setOrderStatus(OrderStatus);	
		this.setParentOrder(parentOrder);
		this.setPartOf(mms);
		}//placeOrder
	
	/**
	 * <p><i>this method is overridden in each of the order subclasses which extend <b>Order</b></i> so if you 
	 * hyperlink to it you will reach the parent class that will be overridden</p>	
	 * <p><ul><li>if this order is not yet filed 
	 * 	<li>and the instance of <b>CandleStick</b> <i>cs</i> is dated after the date this order was placed </li>
	 * 	<li>and the <b>CandleStick</b> price is better than or equal to the order price</li></ul>
	 * 		then attach this <b>CandleStick</b> instance to this order for later reference as the filled price
	 * 		and return <i>true</i>
	 *  else return <i>false</i></p>
	 * @param CandleStick cs
     * @return boolean true
	 */
	public boolean compareToCandleStick(CandleStick cs){
		return false;		
	}
	/**
	 * <ol>
	 * <li>Creates a <i>String</i> description of the entry order and adds it to the <i>tradeDescription</i> string.</li>
	 * <li>Determines the filled <b>CandleStick</b> instance for the initial order and saves it</li>
	 * <li>if order was filled, set the <i>orderStatus</i> to <i>Filled</i></li>
	 * </ol>
	 */
	public void executeOrder() {	
		try {
			String s;
			if(this.getPrice()!=0){
				s = String.format("<li>%s:  Enter <i>%s</i> order to %s %s shares of <b>%s</b> @ Price: %s</li>",
						df2.format(this.getOrderDate()),
						this.getTypeOrder(),
						Tools.getTradeDescription(this) ,
						this.getQuantity(),
						this.getBelongsTo().getTickerSymbol(),
						cf.format(this.getPrice())); 

				this.setFilledCS(this.getCandleSticks().getPriceFilled(this));
				if(this.getFilledCS().getStopPrice()!=0f) this.setOrderStatus(orderStatus.Filled);
			} else {//this would be the case if there is no stop price enter on the history screen
				s = String.format("<li>%s:  No <i>%s</i> order to %s %s shares of <b>%s</b> found, Price: %s</li>",
						df2.format(this.getOrderDate()),
						this.getTypeOrder(),
						Tools.getTradeDescription(this) ,
						this.getQuantity(),
						this.getBelongsTo().getTickerSymbol(),
						cf.format(this.getPrice())); 	
				//if didn't find a stop order, return the most recent cs as profit is unrealized
				this.setFilledCS(this.getCandleSticks().getLastCandleStick());//you can't do this because its use as a test to determine whether the trade was filled in another area and if its not feel if you sell it then it cost a test to fail
			}
			this.getCandleSticks().setPriceMaxMin(this);
			this.getPartOf().getTradeActivityMap().put(dfYYYYMMdd.format(this.getOrderDate())+1+this.getPartOf().getSeqID(), s);
			//	this.setFilledCS(this.getCandleSticks().getCandleSticks().get(0));//if didnt find a stop order, return the most recent cs as profit is unrealized
		} catch (Exception e) {
			e.printStackTrace();
		}       
	}
    /**
     * Returns the <b>Security</b> instance to which this <b>Order</b> instance belongs
     * @return
     */
    private CandleSticks getCandleSticks() {
		return this.getBelongsTo().getCandleSticks();
	}
    /**
     * @return
     * <p>returns <i>true</i> if <b>exit</b> order is to <i>close</i> a <b>long</b> position.  Or if <b>entry</b> order is
     * to <i>open</i> a <b>long</b> position</p>
     */    
    public boolean isLongPosition(){
    //	if(this instanceof  SimpleExit || this instanceof  Stop || this instanceof  StopLoss || this instanceof  TrailingStop)  
    	if(Tools.isExit(this))
    		return this.getDirection()==typeSignal.Sell;
    	else return this.getDirection()==typeSignal.Buy;
    }
    
    public void makeLongPosition(){
	//	this.setd(typeSignal.Sell);
    }
    /**
     * @return
     * <p>returns <i>true</i> if <b>exit</b> order is to <i>close</i> a <b>short</b> position.  Or if <b>entry</b> order is
     * to <i>open</i> a <b>short</b> position</p>
     */     
    public boolean isShortPosition(){
    	if(Tools.isExit(this))	
    		return this.getDirection()==typeSignal.Buy;
    	else 
    		return this.getDirection()==typeSignal.Sell;    		
    }
    /**
     * returns <tt>true</tt> if this is a long position and the high price of the <b>CandleStick</b> instance
     * is greater than the order price or this is a short position and the low price is 
     * less than the order price
     * @param CandleStick cd
     * @return
     */   
    public boolean priceWasBeaten(double cs){    
     return 
     	this.isLongPosition() && this.getPrice()<=cs ||
		this.isShortPosition() && this.getPrice()>=cs;
    }
    /**
     * returns <tt>true</tt> if this is a long position and the high price of the <b>CandleStick</b> instance
     * is greater than the order price or this is a short position and the low price is 
     * less than the order price
     * @param CandleStick cd
     * @return
     */   
    public boolean priceWasBeaten(CandleStick cs){    
     return 
     	this.isLongPosition() && this.getPrice()<=cs.getHighPrice() ||
		this.isShortPosition() && this.getPrice()>=cs.getLowPrice();
    }
    /**
     * @param 
     * @return typeOrder
     * returns the type of Order: Market, Limit, StopLoss, SimpleExit, Stop, StopLimit, TrailingStop, MOC, LOC
     */       
	public typeOrder getTypeOrder() {
        return typeOrder;
    }

    public void setTypeOrder(typeOrder typeOrder) {
        this.typeOrder = typeOrder;
    }

    public int getQuantity()  {
    	
    	try {
			if(quantity==0) throw new Exception(String.format("quantity for %s %s %s is null", this.getBelongsTo().getTickerSymbol(),this.getTypeOrder(), this.getTradePart()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    	
    	return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    /**
     * @param CandleStick cs
     * @return boolean
     * 
     * public typeSignal getDirection()
     * 
     * determine whether this trade is a Buy or a Sell
     */
    public typeSignal getDirection() {
        return this.getQuantity()>0?typeSignal.Buy:typeSignal.Sell;
    }
    

    public GlobalVars.orderLife getOrderLife() {
        return orderLife;
    }

    public void setOrderLife(GlobalVars.orderLife orderLife) {
        this.orderLife = orderLife;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public GlobalVars.orderStatus getOrderStatus() {
        return orderStatus;
    }
    /**
     * @return
     * returns true if the order is active
     */
    public boolean isActive() {
        return this.getOrderStatus()==orderStatus.Active;
    }
    
    /**
     * @return
     * returns true if the order is not yet active
     */
    public boolean isInActive() {
        return this.getOrderStatus()==orderStatus.Inactive;
    }
    /**
     * @return
     * returns true if the order has been filed
     */
    public boolean isFilled() {
        return this.getOrderStatus()==orderStatus.Filled;
    }
        
    public void setOrderStatus(orderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
    public void resetOrderStatus() {
        this.orderStatus = orderStatus.Active;
    }
    public void activate() {
        this.setOrderStatus(orderStatus.Active);
    }
    public void deactivate() {
        this.setOrderStatus(orderStatus.Inactive);;
    }
    /**
     * sets this order's status to fillex
     */
    public void filled() {
        this.setOrderStatus(orderStatus.Filled);
    }
    /**
     * @param CandleStick cs
     * @return <tt>true</tt>
     *  and sets this order's status to filled and set the
     *  filled CandleStick for this order to cs
     */
    public boolean filledPlus(CandleStick cs) {
	    this.setFilledCS(cs);
		this.filled();
		return true;
    }
    /**
     * sets this order's status to cancelled
     */
    public void cancel() {
        this.setOrderStatus(orderStatus.Cancelled);;
    }
    public CandleStick getFilledCS() {
        return filledCS;
    }

    public void setFilledCS(CandleStick filledCS) {
        this.filledCS = filledCS;
        this.getPartOf().setFilledCS(this.filledCS);
    }

    public void resetOrder(){
    	this.filledCS = null;
    	this.resetOrderStatus();
        this.getPartOf().setFilledCS(null);
    }
    
	public double getTriggerPrice() {
		return triggerPrice;
	}

	public void setTriggerPrice(double triggerPrice) {
		this.triggerPrice = triggerPrice;
	}

	public CandleStick getHighestHighCS() {
		return highestHighCS;
	}
	
	public double getHighestHighPrice() {
		return this.getHighestHighCS().getHighPrice();
	}
	
	public int getHighestHighPriceVolume() {
		return this.getHighestHighCS().getVolume();
	}

	public void setHighestHighCS(CandleStick highestHighCS) {
		this.highestHighCS = highestHighCS;
	}

	public Date getHighestHighPriceDate() {
		return this.getHighestHighCS().getDate();
	}
	
	public CandleStick getLowestLowCS() {
		return lowestLowCS;
	}

	public void setLowestLowCS(CandleStick lowestLowCS) {
		this.lowestLowCS = lowestLowCS;
	}
	
	public double getLowestLowPrice() {
		return this.getLowestLowCS().getLowPrice();
	}
	
	public int getLowestLowPriceVolume() {
		return this.getLowestLowCS().getVolume();
	}
	
	public Date getLowestLowPriceDate() {
		return this.getLowestLowCS().getDate();
	}

	public Order getParentOrder() {
		return parentOrder;
	}

	public void setParentOrder(Order parentOrder) {
		this.parentOrder = parentOrder;
	}
public Order getDummyOrder() {
		return dummyOrder;
	}

	public void setDummyOrder(Order dummyOrder) {
		this.dummyOrder = dummyOrder;
	}

//
//	public Order getChildOf() {
//		return childOf;
//	}
//
//	public void setChildOf(Order childOf) {
//		this.childOf = childOf;
//	}
/**
 * 
 * @return
 * <p>returns the mms that this order is a part of
 */
	public MoneyMgmtStrategy getPartOf() {
		return partOf;
	}
	/**
	 * @return
			 * <p>sets the mms that this order is a part of
			 */
	public void setPartOf(MoneyMgmtStrategy partOf) {
		this.partOf = partOf;
	}
	
	protected SecurityInst getBelongsTo() {
		return this.getPartOf().getBelongsTo();
	}
	
	protected Portfolio getBelongsToPortfolio() {
		return this.getBelongsTo().getBelongsTo();
	}
	protected Portfolios getBelongsToPortfolios() {
		return this.getBelongsToPortfolio().getBelongsTo();
	}
	protected GUI getBelongsToGUI() {
		return this.getBelongsToPortfolios().getBelongsToGUI();
	}
	protected JFrame getFrameGUI() {
		return this.getBelongsToGUI().getFrmTradecoach();
	}
	public tradePart getTradePart() {
		return tradePart;
	}

	public void setTradePart(tradePart tradePart) {
		this.tradePart = tradePart;
	}
	
	public void setTradePart(Order order) {
		this.tradePart = order.getClass().equals(InitialEntry.class)?GlobalVars.tradePart.Entry:GlobalVars.tradePart.Exit;
	}
	
	

	public void placeOrder(double stopTriggerPrice, double stopPrice, int i,
			Date stopActivationDate2, orderLife goodtillcancelled,
			typeOrder stopType, orderStatus inactive,
			MoneyMgmtStrategy moneyMgmtStrategy, Order order,
			double trigPrcIncRate, double trailingPct, double trlPctRate) {
		
	}



	

    
}
