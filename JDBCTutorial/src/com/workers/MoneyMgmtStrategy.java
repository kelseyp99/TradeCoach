package com.workers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer ;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.gui.GUI;
import com.tradecoach.patenter.entity.security.CandleStick;
import com.tradecoach.patenter.entity.security.FOrder;
import com.tradecoach.patenter.entity.security.Order;
import com.tradecoach.patenter.entity.security.SimpleExit;
import com.tradecoach.patenter.entity.security.StopTestStats;
import com.utilities.GlobalVars;

import com.utilities.GlobalVars.*;
import com.workers.PortfoliosGroup.Pcts4Lyer;

public class MoneyMgmtStrategy implements GlobalVars {
	private String tickerSymbol, tradeActivity="";
	private int idNum, tradeID ,seqID=0;
	private ArrayList<Order> Orders = new ArrayList<Order> (); 
	private Order order;// = new InitialEntry();
	private Order stop;// = new Order();
	private Order profitTarget ;//= new Order();
	private Order exit;
	private CandleStick filledCS, HighestHighCS, LowestLowCS;
	private boolean aStopFiled;
	private double StopStopLossRatio;
	private typeOrder imposedExitType=GlobalVars.typeOrder.TrailingStop;//BUGGG!!! i have to set this or it errors out with stopType = Null for some unexplained 
	// private String instrumentName;
	private MoneyMgmtStrategy mmsBase;
	private SecurityInst belongsTo;
	private boolean ready2Write;
	private Double triggerPrice, trigPrcIncRate, trailingPct, trlPctRate;
	public typeOrder TypeOrder;
	private Stack<Pcts4Lyer> LayerStack = new Stack<Pcts4Lyer>();
	private Map<String,String> TradeActivityMap;

	static class StopTest {
		private static double drawDownPct;
		private static boolean isProfitable;
	}

	public double getDrawDownPct() {
		return StopTest.drawDownPct;
	}
	public void setDrawDownPct(double drawDownPct) {
		StopTest.drawDownPct = drawDownPct;
	}
	public boolean isProfitable() {
		return StopTest.isProfitable;
	}
	public void setProfitable(boolean isProfitable) {
		StopTest.isProfitable = isProfitable;
	}

	public MoneyMgmtStrategy() {}
	/*
    public MoneyMgmtStrategy(String orderDate, int orderQty, double entryPrice, double stopLossPrice, double stopPrice, SecurityInst securityInst) {
    	initialize(orderDate, orderQty, entryPrice, stopLossPrice, 0d, stopPrice);
    }//MoneyMgmtStrategy

    public MoneyMgmtStrategy(String ticker, String instrumentName, String orderDate, int orderQty,
			double entryPrice, double stopLossPrice, double stopPrice, SecurityInst securityInst) {
    	this.setTickerSymbol(ticker);
    	//this.setInstrumentName(instrumentName);
    	initialize(orderDate, orderQty, entryPrice, stopLossPrice, 0d, stopPrice); 	
	}
	 */    
	/**
	 * @param
	 * <p><b>public MoneyMgmtStrategy(MoneyMgmtStrategy mms)</b></p>
	 * <p>Creates a clone instance of <i>mms</i>.  Used when creating a new portfolio for max ROI. </p>
	 *
	 */
	public MoneyMgmtStrategy(MoneyMgmtStrategy mms) {
		this.setBelongsTo(mms.getBelongsTo());
		this.setLayerStack(mms.getLayerStack());
		this.setIdNum(mms.getIdNum());
		this.setTradeActivity(mms.getTradeActivity());
		initialize(df2.format(mms.getOrder().getOrderDate()), 
				mms.getOrder().getQuantity(),
				mms.getOrder().getPrice(), 
				mms.getStop().getPrice(),  
				// mms.getProfitTarget().getTypeOrder(), 
				mms.belongsTo.getMmsTemp().getImposedExitType(),
				df2.format(mms.getOrder().getOrderDate()),
				mms.getProfitTarget()!=null?mms.getProfitTarget().getTriggerPrice():-1,
				mms.getProfitTarget()!=null?mms.getProfitTarget().getPrice():-1
				);
		///this.setMmsBase(mms.getMmsBase());

	}//MoneyMgmtStrategy
	/**
	 * This is called from DataLoader to create the initial mms for the stock
	 * @param ticker
	 * @param instrumentName2
	 * @param orderDate
	 * @param orderQty
	 * @param entryPrice
	 * @param stopLossPrice
	 * @param stopType
	 * @param stopActivationDate
	 * @param stopTriggerPrice
	 * @param stopPrice
	 * @param securityInst
	 */
	public MoneyMgmtStrategy(String ticker, String instrumentName2,
			String orderDate, int orderQty, double entryPrice,
			double stopLossPrice, typeOrder stopType,		
			String stopActivationDate,
			double stopTriggerPrice, double stopPrice, SecurityInst securityInst) 
	{
		this.setBelongsTo(securityInst);
		this.setTickerSymbol(ticker);
		//  	this.setInstrumentName(instrumentName);
		initialize(orderDate, orderQty, entryPrice, stopLossPrice,  stopType,		
				stopActivationDate, stopTriggerPrice, stopPrice); 
	}

	public MoneyMgmtStrategy(TransactionData ts) {
		this.setTickerSymbol(ts.getTickerSymbol());
		this.setBelongsTo(ts.getSecurityInst());
		//  	this.setInstrumentName(instrumentName);
		initialize(ts.getOrderDate(), ts.getPosition(), ts.getEntryPrice(),
				   ts.getStopLoss(),  ts.getStopType(),	ts.getStopActivationDate(),	
				   ts.getStopTrigger(), ts.getStop()); 
	}
	
	@Override
	public String toString() {

		String x="";
		x +=       "    Trade Direction:                "  + getOrder().getDirection() +"\n"; 
		x +=       "    Shares:                         "  + getOrder().getQuantity() +"\n";
		if(this.orderWasEntered()) 
		{
			x +=   "    Entry Price:                    "  + ef.format(getOrder().getPrice()) +"\n";
			x +=   "    Entry Date:                     " +  df.format(this.getOrder().getFilledCS().getDate()) +"\n" ;

			if(this.orderWasExited()) {
				x +=   "    Exit Price:                     " + ef.format(this.getFilledCS().getStopPrice())+"\n";
				x +=   "    Exit Date:                      " + df.format(this.getFilledCS().getDate()) +"\n" ;
			}
			else { 
				x +=   "    Exit Price:                     none\n";
				x +=   "    Exit Date:                      none\n";
			}


			x +=   "    Trailing Stop Strategy:                     \n" + this.toStringStops();
			/*   
	        x +=       "    Days Open:                      "  + getDays() +"\n";
	        x +=       "    ROI:                            " + wf.format(getROI()) +"%\n" ;
	        x +=       "    Profit(Loss) per share:         "  + ef.format(getProfitAndLoss()) +"\n";
	        x +=       "    Profit(Loss):                   "  + ef.format(this.getProfitAndLossThisTrade()) +"\n";
			 */
		}
		else { 
			x +=   "    Entry Price:                    none\n";
			x +=   "    Entry Date:                     none\n";
			x +=   "                                    Initial Order has not yet filled\n";
			x +=   "    Trailing Stop Strategy:                     \n" + this.toStringStops();
		}




		return x;
	}

	public String toString2() {
		return this.getTradeActivity();
	}
		
	public String toStringStops() {
		String s =  this.getStop().toString();
		return s;
	}
	/**
	 * @param

	 *
	 */
	private void initialize(String orderDate, int orderQty, double entryPrice, double stopLossPrice, typeOrder stopType,		
			String stopActivationDate, double stopTriggerPrice, double stopPrice) {
		Date startDate = dfMdYYYY.parseDateTime(orderDate).toDate();
		Date stopActivationDate2 = dfMdYYYY.parseDateTime(stopActivationDate).toDate();
		initialize(startDate,  orderQty,  entryPrice,  stopLossPrice,
				   stopType, stopActivationDate2,  stopTriggerPrice,  stopPrice);
	}
			
	private void initialize(Date startDate, Integer orderQty, Double entryPrice, Double stopLossPrice,
							typeOrder stopType, Date stopActivationDate2, Double stopTriggerPrice, Double stopPrice) {
		TradeActivityMap = new TreeMap<String, String> ();		
		if(stopType==null)
			try {
				throw new Exception("stopType is Null thus Imposed Exit passed was null");
			} catch (Exception e) {
				e.printStackTrace();
			}	

		try {


			if(stopActivationDate2==null || Tools.isSameDayOrLater(startDate,stopActivationDate2))	{
				//	String titleBar = "Data Input Error";
				/*				String infoMessage = String.format(
						"The Stop (profit target) order activation you entered is before the\n "
						+ "initial trade entry date for the ticker symbol %s.  This is not \n"
						+ "supported in this version. This activation date of of %s has\n"
						+ " been changed to the trade entry date of %s.  Please make\n"
						+ " necesary changes to your input media to avoid this message \n"
						+ "in the future." , this.belongsTo.getTickerSymbol(), stopActivationDate, orderDate )	;	*/
				stopActivationDate2=startDate;
				//	Tools.infoBox(infoMessage, titleBar);
			}

			this.setOrder(FOrder.getObject(typeOrder.Limit));
			this.getOrders().add(this.getOrder());
			getOrder().placeOrder(	entryPrice, 
					orderQty, startDate,
					orderLife.GoodTillCancelled, 
					typeOrder.Limit,
					orderStatus.Active,
					this);

			//add a stop loss
			this.setStop(FOrder.getObject(typeOrder.StopLoss));
			this.getOrders().add(this.getStop());
			getStop().placeOrder(entryPrice, stopLossPrice, 		
					-orderQty, startDate,
					orderLife.GoodTillCancelled, 
					typeOrder.StopLoss,
					orderStatus.Inactive,
					this,
					this.getOrder());

			//add a profit target stop
			switch (stopType) {
			case Stop:  {
				if(stopTriggerPrice!=-1) {
					this.setProfitTarget(FOrder.getObject(typeOrder.Stop));
					this.getOrders().add(this.getProfitTarget());
					getProfitTarget().placeOrder(stopTriggerPrice, stopPrice, 
							-orderQty, stopActivationDate2,
							orderLife.GoodTillCancelled, 
							typeOrder.Stop,
							orderStatus.Inactive,
							this,
							this.getOrder());
				}
				break;
			}
			case SimpleExit: {
				if(stopTriggerPrice!=-1) {
					this.setProfitTarget( FOrder.getObject(stopType));
					this.getOrders().add(this.getProfitTarget());
					getProfitTarget().placeOrder(stopTriggerPrice, stopPrice, 
							-orderQty, stopActivationDate2,
							orderLife.GoodTillCancelled, 
							stopType,
							orderStatus.Inactive,
							this,
							this.getOrder());
				}
				break;
			}
			case TrailingStop: {
				if(stopTriggerPrice!=-1) {
					trigPrcIncRate = 10d;
					trailingPct = 5d;
					trlPctRate = 2d;
					this.setStop( FOrder.getObject(stopType));
					this.getOrders().add(this.getStop());
					this.getStop().placeOrder(stopTriggerPrice, 
							stopPrice, 
							-orderQty, 
							stopActivationDate2,
							orderLife.GoodTillCancelled, 
							stopType,
							orderStatus.Inactive,
							this,
							this.getOrder(),
							trigPrcIncRate,
							trailingPct,
							trlPctRate);
				}
			}

			default:
				break;
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	

	/**
	 * <b>public double getProfitAndLossThisTrade()</b><p>
	 * returns the $ total profit of the this trade respective of <i>typeSignal</i> (Buy/Sell).  The initial order must
	 * have first executed.
	 * 
	 * @param
	 * @return
	 */
	public double getProfitAndLossThisTrade() {
		return getProfitAndLoss()*Math.abs(getOrder().getQuantity());
	}

	/**
	 * <b>public double getProfitAndLoss()</b><p>
	 * returns the $/share profit of the this trade respective of <i>typeSignal</i> (Buy/Sell).  The initial order must
	 * have first executed.
	 * 
	 * @param
	 * @return
	 */
	/**
	 * @return
	 */
	public double getProfitAndLoss() {
		try {
			if(this.getOrder()==null)throw new CustomException("the orignal order object is null");
			
			if (getOrder().getOrderStatus()==orderStatus.Filled) {
				if(this.getFilledCS()==null)throw new Exception("there is no filled candlestick");
				if (getOrder().getDirection()==typeSignal.Sell) {
					return this.getOrder().getPrice()-this.getFilledCS().getStopPrice();
					//insert the last known price less the order filled price.  it is unrealized
				}
				else //for Shorts
					//	if(this.getFilledCS()!=null)
					//v=this.getFilledCS().getStopPrice()-this.getOrder().getPrice();
					return this.getFilledCS().getStopPrice()-this.getOrder().getPrice();
			}
			else return 0f; //initial order was not filled
		} catch (CustomException c) {
			
			c.printStackTrace();
		
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return 0f;
	}//getProfitAndLoss() ;


	/**
	 * <b>public double getProfitAndLossAtPointInTime()</b><p>
	 * returns the $/share profit of the this trade respective of <i>typeSignal</i> (Buy/Sell).  The initial order must
	 * have first executed.
	 * 
	 * @param
	 * @return
	 */
	public double getProfitAndLossAtPointInTime(Date currentDate) {
		//		double v;
		if (getOrder().getOrderStatus()==orderStatus.Filled) {
			if (getOrder().getDirection()==typeSignal.Sell) {
				return this.getOrder().getPrice()-this.getTradeStatusAtPointInTime(currentDate).getBestPrice(typeSignal.Buy);
				//insert the last known price less the order filled price.  it is unrealized
			}
			else //for Shorts
				return this.getTradeStatusAtPointInTime(currentDate).getBestPrice(typeSignal.Sell)-this.getOrder().getPrice();
		}
		else return 0f; //initial order was not filled
	}//getProfitAndLoss() 

	/**
	 * <b>public double getProfitAndLossAtPointInTime()</b><p>
	 * returns the absolute % profit (non-annualized) of the this trade respective of <i>typeSignal</i> (Buy/Sell).  The initial order must
	 * have first executed.
	 * 
	 * @param
	 * @return
	 */
	public double getAbsoluteROIAtPointInTime(Date currentDate) {    	
		return this.getProfitAndLossAtPointInTime(currentDate)/this.getOrder().getPrice();
	}//getProfitAndLoss() 	

	/*
	public void executeOrder() {
		this.getOrder().executeOrder();
		this.setFilledCS(getExitOrLastCandle());
	}
	 */
	private CandleStick getExitOrLastCandle() {
		try {
			if (getOrder().isFilled()) {	
				if (this.bothStopsReached())
					if (this.getStop().getFilledCS().getDate().after(getProfitTarget().getFilledCS().getDate())) {
				//		this.cout(this.getProfitTarget().getFilledCS().toStringWhenFilled(this.getProfitTarget()));
						this.setExit(this.getProfitTarget());
						return this.getProfitTarget().getFilledCS();
					}
					else {
				//		this.cout(this.getStop().getFilledCS().toStringWhenFilled(this.getStop())); 
						this.setExit(this.getStop());
						return getStop().getFilledCS();
					}
				//if the StopLoss was filled but not the Stop, use the StopLoss
				else if (this.onlyStopLossFilled()){
				//	this.cout(this.getStop().getFilledCS().toStringWhenFilled(this.getStop()));
					this.setExit(this.getStop());
					return getStop().getFilledCS();
				}
				//if the StopLoss was not filled but the Stop was filled, use the Stop
				else if (this.onlyStopFilled()){
				//	this.cout(this.getProfitTarget().getFilledCS().toStringWhenFilled(this.getProfitTarget()));
					this.setExit(this.getProfitTarget());
					return getProfitTarget().getFilledCS();
				}
				//if neither where filled, use the last last candlestick.  It is attached to both orders
				else if(getProfitTarget()!=null) {
				//	this.cout(this.getStop().getFilledCS().toStringWhenStillOpen(this.getStop()));
			//		this.setExit(this.getStop());//
					this.setExit(this.getProfitTarget());
				    return getProfitTarget().getFilledCS();
				}
				else if(this.getStop()!=null) {
				//	this.cout(this.getStop().getFilledCS().toStringWhenStillOpen(this.getStop()));
			//		this.setExit(this.getProfitTarget());
					this.setExit(this.getStop());
				    return this.getStop().getFilledCS();
				}
				else {
					return this.getLastCandleStick();
				}
			//	else throw new Exception("Both Stop and Profit Target Objects are null");
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return null;   
	}
	private CandleStick getLastCandleStick() {
		return this.getCandleSticks().getLastCandleStick();
	}
	private MoneyMgmtStrategy getCandleSticks() {
		// TODO Auto-generated method stub
		return this.getCandleSticks();
	}
	/**
	 * Sets variables for this mms indicating whether the trade was profitable a point in time represented by <i>currentDate</i>.  If the trade is profitable, a boolean is 
	 * set to <b>true</b>.  If the trade is not profitable, then the percent loss is saved.  Later, the count and sum total of these two measures will be tallied in 
	 * the <b>Portfolio</b> to which this mms is a part.
	 * @param currentDate
	 * @return 
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public void setStopResults(Date currentDate) throws Exception {
		if(this.getTradeStatusAtPointInTime(currentDate).beatsThisPrice(this.getOrder())) {
			this.setProfitable(true);	
			this.setDrawDownPct((Double) null);
		}
		else {
			this.setProfitable(false);
			this.setDrawDownPct(this.getAbsoluteROIAtPointInTime(currentDate));
		}
	}

	/**
	 * 
	 * @param currentDate
	 * @return
	 * @throws Exception 
	 */
	public StopTestStats getStopTestResults(Date currentDate) throws Exception {
		if(this.getTradeStatusAtPointInTime(currentDate).beatsThisPrice(this.getOrder())) 
			return new StopTestStats((Double) null, true, currentDate);
		else {
			double d = this.getAbsoluteROIAtPointInTime(currentDate);
			return new StopTestStats(d, false, currentDate);
		}
	}



	/**
	 * <p><b>private CandleStick getTradeStatusAtPointInTime(Date currentDate)</b></p>
	 * <p>see if after 5 days  the trade is either stopped-out or it is  above 0%. </p>
	 * @param
	 */
	private CandleStick getTradeStatusAtPointInTime(Date currentDate) {
		/* if the current date is before exit date of the stop return the candlestick corresponding to the current date else return the candlestick corresponding
    	to the exit date*/
		if(Tools.isSameDayOrLater(this.getExitOrLastCandle().getDate(), currentDate))
			return this.getBelongsTo().getCandleSticks().getAtDate(currentDate);
		else
			return this.getExitOrLastCandle(); 
	}

	private boolean onlyStopLossFilled() {
		if(getProfitTarget()==null) 
			return getStop().isFilled();	
		else
			return getStop().isFilled() && !getProfitTarget().isFilled();
			
	}

	private boolean onlyStopFilled() {
		if(getProfitTarget()==null) return false;
		return getStop().getOrderStatus()!=orderStatus.Filled && getProfitTarget().getOrderStatus()!=orderStatus.Filled;		
	}
	/** If both the profit target and the stop loss orders would have been reached if either was run by itself then return <tt>true</tt>*/
	private boolean bothStopsReached() {
		if(getProfitTarget()==null) return false;
		return getStop().getOrderStatus()==orderStatus.Filled && getProfitTarget().getOrderStatus()==orderStatus.Filled;		
	}
	/** If NEITHER the profit target and the stop loss orders would have been reached if either was run by itself then return true*/
	private boolean neitherExitReached() {
		if(getProfitTarget()!=null) {
			return !(getStop().getOrderStatus()==orderStatus.Filled) && !(getProfitTarget().getOrderStatus()!=orderStatus.Filled);	
		}
		return !(getStop().getOrderStatus()==orderStatus.Filled);
	}
	
	/**
	 * If not running threads, then this is the same as calling <i>System.out.println</i>.  If using threads
	 * then this will accumulate the day-to-day trading activity narratives into a <i>String</i> variable.  When all threads
	 * are finished, this <i>String</i> is used to display activity in the GUI.  Else, all the lines get out-of-order
	 * when displayed
	 * @param s
	 */
	public void cout(String s) {
		if(this.getBelongsToPortfolio().isRunCurrent()){
			if(this.getBelongsToSecurity().getExecutionLevel()==1) 
				this.appendTradeActivity(s);
			else 				
				if(this.isReady2Write()) {
					try {
						this.getLock().lock(this.getBelongsToSecurity());
					//	this.getBelongsToPortfolios().getOrderSem().take(this.getBelongsToSecurity());
						this.getBelongsToPortfolio().getMms2bRun().appendTradeActivity(this.getTradeActivity());					
					//	this.getBelongsToPortfolios().getOrderSem().release();
						this.resetReady2Write();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					finally {
						try {
							this.getLock().unlock();
						} catch (NullPointerException e) {
							System.out.printf("CoutLock is null for %s\n", this.getBelongsTo().getTickerSymbol());
							e.printStackTrace();
						}
					}
				} else if(s!=null){
					this.appendTradeActivity(s);
			}				
		}
		else
			System.out.println(s);
	}
	
	/**
	 * <p>calls the <i>executeOrder</i> function of the <b>Order</b> instance associated with the initial order. If successful, the <i>stop</i> for this order is activated
	 * and the <i>executeOrder()</i> of its associated <b>Order</b> instance is called.  Next, if the associated <b>Order</b> instance of the <i>profit target</i> is of class type 
	 * <i>SimpleExit</i> or <i>TrailingStop</i>, the respective instance variables are set to <i>active</i> and the <i>executeOrder()</i> of its associated <b>Order</b> instance is called.
	 * Finally, the <b>CandleStick</b> instance associated with the trade exit (if any) or last <b>CandleStick</b> instance as the last date being tested is saved to this 
	 * <b>mms</b> instance   </p>
	 */
	public void executeOrder() {
		String [] keyPair = null;
		try {
			String s=this.getBelongsToGUI().isProcessingOriginalList()?"Your original list":"Scenario #" +this.getIdNum();	
			s=String.format("<p>Executing trade orders for <b>%s</b> (%s): <i>%s</i><ol>", this.belongsTo.getTickerSymbol(),this.getBelongsTo().getinstrumentName(),s);			
			s=(this.getBelongsToGUI().isProcessingOriginalList()?"<hr>":"")+s;
			this.cout(s);
			//place the order for execution 
			this.getOrder().executeOrder();
			//if filled, save date and price (candlestick) and activate the stopLoss
			if (this.getOrder().isFilled()) {//tableModel.getClass().equals(TableModel2.class)				
			//	String key = dfYYYYMMdd.format(this.getOrder().getFilledCS().getDate())+this.getOrder().getPartOf().getSeqID();
				keyPair = this.getOrder().getFilledCS().toStringWhenFilled(this.getOrder());
		//		this.getOrder().getFilledCS().toStringWhenFilled(this.getOrder(), key, value);
				this.getOrder().getPartOf().getTradeActivityMap().put(keyPair[0],keyPair[1]);
			//	this.cout(this.getOrder().getFilledCS().toStringWhenFilled(this.getOrder()));
				getStop().activate();
				getStop().executeOrder();
				if(this.getProfitTarget()!=null ) {
					if(this.getProfitTarget().getClass().equals(SimpleExit.class))
						this.getProfitTarget().activate();
					this.getProfitTarget().executeOrder();
				}
			}
			try {//Individual water classes sxephil channel 6 of the money management prepare a member of if they're not able to feel then this will be blank in the order still open
				if(this.getFilledCS()==null || !this.iSaStopFiled()) {//the trade is still open so accrue profit as of current date
					this.setFilledCS(this.getExitOrLastCandle());
					if(this.getFilledCS()==null)throw new Exception(String.format("No filled CandleStick for %s",this.belongsTo.getTickerSymbol()));
				//	this.cout(this.getFilledCS().toStringWhenStillOpen(this.getOrder()));
					keyPair = this.getFilledCS().toStringWhenStillOpen(this.getExit());
					this.getOrder().getPartOf().getTradeActivityMap().put(keyPair[0],keyPair[1]);;
				} else {					
					this.setFilledCS(this.getExitOrLastCandle());
					if(this.getFilledCS()==null)throw new Exception(String.format("No filled CandleStick for %s",this.belongsTo.getTickerSymbol()));
					if(this.getExit()==null)throw new Exception(String.format("No exit order named for %s",this.belongsTo.getTickerSymbol()));
				//	key = dfYYYYMMdd.format(this.getExit().getOrderDate())+this.getExit().getPartOf().getSeqID();
				//	this.getExit().getFilledCS().toStringWhenFilled(this.getOrder());
					keyPair = this.getExit().getFilledCS().toStringWhenFilled(this.getExit());
					//String value = this.getExit().getFilledCS().toStringWhenFilled(this.getExit());
					this.getOrder().getPartOf().getTradeActivityMap().put(keyPair[0],keyPair[1]);;
				//	this.cout(this.getFilledCS().toStringWhenFilled(this.getExit()));	
					
				}
			} catch (CustomException e) {
				e.printStackTrace();
			}
			
			String tx = null;
			Map<String, String> treeMap = new TreeMap<String, String>(this.getTradeActivityMap());
			this.getTradeActivityMap().clear();
			for (Map.Entry<String, String> entry : treeMap.entrySet()) {
				System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
				tx += entry.getValue()!=null?entry.getValue():"";				
			}
			
		/*	Map<Date,String> tradeActivityTreeMap = new TreeSet<Date, String>(
					new Comparator<Date>(){
						
						@Override
						public int compare(Date o1, Date o2){
							return o2.compareTo(o1);
						}
					});*/
		//	this.cout(treeMap.values().toString());
			this.cout(tx);
			NumberFormat pf4 = NumberFormat.getPercentInstance();
			pf.setMaximumFractionDigits(4);
			String font = this.getProfitAndLossThisTrade()<0?"red":"black";
			this.cout(String.format("</ol><ul style=\"list-style-type:none\"><li>Proft(<i>Loss</i>) per share:  <font color=\"%s\">%s</font>, Total Profit(<i>Loss</i>):  <font color=\"%s\">%s</font></li>",font,cf.format(this.getProfitAndLoss()),font,cf.format(this.getProfitAndLossThisTrade())));
		//	this.cout("</ol>Proft(Loss) per share:  "+cf.format(this.getProfitAndLoss())+", Total Profit(Loss):  " + cf.format(this.getProfitAndLossThisTrade()));
			this.cout("<li>"+df2.format(this.getFilledCS().getDate())+":  ROI for this individual trade = "+ 
					  pf.format(this.getROI()) +" for " + this.getDays() + 
					  " days ("+df.format(this.getOrder().getFilledCS().getDate())+" to "+ df.format(this.getFilledCS().getDate())+"),</li></ol> "+this.getROIstring()); 
/*			System.out.println(df2.format(this.getFilledCS().getDate())+":  ROI for this individual trade = "+ 
								pf4.format(this.getROI()) +" for " + this.getDays() + 
								" days ("+df.format(this.getOrder().getFilledCS().getDate())+" to "+ df.format(this.getFilledCS().getDate())+")"); 
			System.out.println("Proft(Loss) per share:  "+cf.format(this.getProfitAndLoss())+", Total Profit(Loss):  " + cf.format(this.getProfitAndLossThisTrade()));*/
			String s1 = String.format("</ol><ul style=\"list-style-type:none\"><li>Proft(Loss) per share: %s, Total Profit(Loss): %s</li><li>%s: ROI for this individual trade = -2,500 for 1 days (2015-11-09 to 2015-11-09), </li><li>r = (a*x/b)/c = (250 days*($0.94)/1 days)/$9.36 </li><ul style=\"list-style-type:none\"><li>a=trading days in a year</li><li>b=days open</li><li>c=entry price</li><li>x=profit(loss) per share null</li></ul></ul>",
					cf.format(this.getProfitAndLoss()),cf.format(this.getProfitAndLossThisTrade()),df2.format(this.getFilledCS().getDate()));
			this.flushCout();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}//executeOrder

	/** return <i>true</i> if initial order to enter this trade was filled
	 * 	getOrder().getOrderStatus()==orderStatus.Filled;
	 * @return
	 */
	public boolean orderWasEntered(){
		return getOrder().getOrderStatus()==orderStatus.Filled;
	}
	/**
	 * @return
	 * return true if this trade was exited via either of the stops or stop losses
	 * If not then assume the order is still in play if opened prior
	 * 
	 *	getStop().getOrderStatus()==orderStatus.Filled 
	 *
	 *	or
	 *
	 *  getProfitTarget().getOrderStatus()==orderStatus.Filled
	 */	
	public boolean orderWasExited(){
		return getStop().getOrderStatus()==orderStatus.Filled ||
				getProfitTarget().getOrderStatus()==orderStatus.Filled;
	}

	/**
	 * returns the number of days between the time the entry date of this trade and the exit date whether profitable
	 * or by stop loss.  This is specific to to the scenario in this <b>MoneyMgmtStrat</b> instance. 
	 * @return
	 */
	public int getDays() {
		try {
			int x = Math.abs(Tools.daysBetween(this.getOrder().getFilledCS().getDate(), this.getFilledCS().getDate()));
			return Math.max(x,1);
		} 
		catch (NullPointerException n) {
			this.cout("WARNING! Days between could not be calculated due to lack of exit data");
		}
		
		catch (Exception e) {
			
			e.printStackTrace();
		}
		return -1;
	}
	
    /**
     * <p>Returns the annualized return on investment for this <b>MoneyMgmtStrat</b> instance.</p>
     * return double
     */
	public String getROIstring() {
		String r = "";
		try {
			String a = Integer.toString(GlobalVars.TRADING_DAYS_IN_YEAR);
			String b=Integer.toString(this.getDays()) ;
			String c=cf.format(this.getOrder().getPrice());
			String x=cf.format(this.getProfitAndLoss());
			int l=15;
			r += StringUtils.repeat(" ", l)+String.format("<ul style=\"list-style-type:none\"><li>r = (a*x/b)/c = (%s days*%s/%s days)/%s</li>",a,x,b,c);
			l+=2;
			r+=StringUtils.repeat(" ", l)+("<ul style=\"list-style-type:none\"><li>a=trading days in a year</li>");
			r+=StringUtils.repeat(" ", l)+("<li>b=days open</li>");
			r+=StringUtils.repeat(" ", l)+("<li>c=entry price</li>");
			r+=StringUtils.repeat(" ", l)+("<li>x=profit(loss) per share</li></ol></ol>");
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return r;
		// return (100*GlobalVars.TRADING_DAYS_IN_YEAR*this.getProfitAndLoss()/this.getDays())/this.getOrder().getPrice();
	}
	
	
    /**
     * <p>Returns the annualized return on investment for this <b>MoneyMgmtStrat</b> instance.</p>
     * return double
     */
	public double getROI() {
		double r=9999;
		try {
			double a = GlobalVars.TRADING_DAYS_IN_YEAR;
			double b=this.getDays() ;
			double c=this.getOrder().getPrice();
			double x = this.getProfitAndLoss();
			r = (a*x/b)/c;
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return r;
		// return (100*GlobalVars.TRADING_DAYS_IN_YEAR*this.getProfitAndLoss()/this.getDays())/this.getOrder().getPrice();
	}

	public double getROIweightedContribution(int totalDays) {
		//	double c=this.getOrder().getPrice();
		double x=11111110;
		try {
			double a =  this.getROI();
			double b=this.getDays() ;
			x = a*( b/totalDays);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return x;
	}
	public void setOrders(ArrayList<Order> Orders) {
		this.Orders = Orders;
	}

	public Order getOrder() {
		return order;
	}

	public ArrayList<Order> getOrders() {
		return Orders;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
	
	private GUI getBelongsToGUI() {
		// TODO Auto-generated method stub
		return this.getBelongsToPortfolios().getBelongsToGUI();
	}

	private PortfoliosGroup getBelongsToPortfolios() {
		// TODO Auto-generated method stub
		return this.getBelongsToPortfolio().getBelongsTo();
	}

	private Portfolio getBelongsToPortfolio() {
		// TODO Auto-generated method stub
		return this.getBelongsToSecurity().getBelongsTo();
	}

	private SecurityInst getBelongsToSecurity() {
		// TODO Auto-generated method stub
		return this.getBelongsTo();
	}
	
	public Order getStop() {
		return stop;
	}

	public void setStop(Order order) {
		this.stop = order;
	}

	public Order getProfitTarget() {
		return profitTarget;
	}

	public void setProfitTarget(Order order) {
		this.profitTarget = order;
	}
/**contains the candlestick at which this order either filed or the last candlestick if the order is still open*/
	public CandleStick getFilledCS() {
		return filledCS;
	}

	public void setFilledCS(CandleStick filledCS) {
		this.filledCS = filledCS;
	}   



	public MoneyMgmtStrategy getMmsBase() {
		return mmsBase;
	}

	public void setMmsBase(MoneyMgmtStrategy mmsBase) {
		this.mmsBase = mmsBase;
	}

	public String getTickerSymbol() {
		return tickerSymbol;
	}

	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

	public String getInstrumentName() {
		return this.getBelongsTo().getinstrumentName();
	}

	/**
	 * 
	 * @return
	 * 
	 * a double 
	 * <p>gets the ratio to be applied of the stop-loss to the profit exit.  In other words,
	 * the profit exit is set as a ratio of the stop-loss.</p>
	 * 
	 */
	public double getStopStopLossRatio() {
		return StopStopLossRatio;
	}
	/**
	 * 
	 * @param stopStopLossRatio
	 * 
	 * <p>sets the ratio to be applied of the stop-loss to the profit exit.  In other words,
	 * the profit exit is set as a ratio of the stop-loss.</p>
	 * 
	 */
	public void setStopStopLossRatio(double stopStopLossRatio) {
		StopStopLossRatio = stopStopLossRatio;
	}



	public CandleStick getHighestHighCS() {
		return HighestHighCS;
	}

	public void setHighestHighCS(CandleStick highestHighCS) {
		HighestHighCS = highestHighCS;
	}

	public CandleStick getLowestLowCS() {
		return LowestLowCS;
	}

	public void setLowestLowCS(CandleStick lowestLowCS) {
		LowestLowCS = lowestLowCS;
	}

	/**
	 * 
	 * <b>public SecurityInst getBelongsTo()</b>
	 * @return
	 * <p>returns the security instance to which this mms belongs to</p>
	 */
	public SecurityInst getBelongsTo() {
		return belongsTo;
	}
	/**
	 * 
	 * @param
	 * 
	 * 
	 * <b>public void setBelongsTo(SecurityInst belongsTo)</b>
	 * @return
	 * <p>sets the security instance to which this mms belongs to</p>
	 */
	public void setBelongsTo(SecurityInst belongsTo) {
		this.belongsTo = belongsTo;
	}

	public Order getExit() {
		return exit;
	}

	public void setExit(Order order) {
		this.exit = order;
	}

	public typeOrder getImposedExitType() {
		return imposedExitType;
	}
	public void setImposedExitType(typeOrder imposedExitType) {
		this.imposedExitType = imposedExitType;
	}
	public Double getTriggerPrice() {
		return triggerPrice;
	}
	public void setTriggerPrice(Double triggerPrice) {
		this.triggerPrice = triggerPrice;
	}
	public Double getTrigPrcIncRate() {
		return trigPrcIncRate;
	}
	public void setTrigPrcIncRate(Double trigPrcIncRate) {
		this.trigPrcIncRate = trigPrcIncRate;
	}
	public Double getTrailingPct() {
		return trailingPct;
	}
	public void setTrailingPct(Double trailingPct) {
		this.trailingPct = trailingPct;
	}
	public Double getTrlPctRate() {
		return trlPctRate;
	}
	public void setTrlPctRate(Double trlPctRate) {
		this.trlPctRate = trlPctRate;
	}


	public Stack getLayerStack() {
		return LayerStack;
	}

	public void setLayerStack(Stack layerStack) {
		LayerStack = layerStack;
	}



	public int getIdNum() {
		return idNum;
	}
	public void setIdNum(int idNum) {
		this.idNum = idNum;
	}
	public String getTradeActivity() {
		return tradeActivity;
	}
	public void setTradeActivity(String tradeActivity) {
		this.tradeActivity = tradeActivity;
	}
	
	public int getTradeID() {
		return tradeID;
	}
	public void setTradeID(int tradeID) {
		this.tradeID = tradeID;
	}
	public Map<String, String> getTradeActivityMap() {
		return TradeActivityMap;
	}
	public void setTradeActivityMap(Map<String, String> tradeActivityMap) {
		TradeActivityMap = tradeActivityMap;
	}
	/**
	 * resets the variable written to by cout 
	 */
	public void resetTradeActivity() {
		this.tradeActivity = "";
	}
	
	/**Adds the text passed in <i>tradeActivity</i> as a new line to the instance variable <i>tradeActivity</i>*/
	public void appendTradeActivity(String tradeActivity) {
		this.setTradeActivity(String.format("%s%s", this.getTradeActivity(),tradeActivity));	
	}	
	
	public boolean isReady2Write() {
		return ready2Write;
	}
	public void setReady2Write(boolean ready2Write) {
		this.ready2Write = ready2Write;
	}
	public String getSeqID() {
		return String.format("%05d", ++seqID);
	}
	public void setSeqID(int seqID) {
		this.seqID = seqID;
	}
	private void makeReady2Write() {
		this.setReady2Write(true);
	}
	
	private void resetReady2Write() {
		this.setReady2Write(false);
	}
	
	private void flushCout() {
		this.makeReady2Write();
		this.cout(null);
	}
	
	private PortfoliosGroup.Lock getLock() {
		try {
			return this.getBelongsToPortfolios().getCoutLock();
		} catch (NullPointerException e) {
			System.out.printf("CoutLock is null for %s\n", this.getBelongsTo().getTickerSymbol());
			e.printStackTrace();
		}
		return null;
	}
	/**this is sad only by exit type orders then again at least one stop was executed there for the money market strategy was executed*/
	public boolean iSaStopFiled() {
		return aStopFiled;
	}
	public void setaStopFiled(boolean aStopFiled) {
		this.aStopFiled = aStopFiled;
	}
	public void setaStopFiled() {
		this.aStopFiled = true;
	}
	public void resetaStopFiled() {
		this.aStopFiled = false;
	}
	public static void main(String[] args) throws InterruptedException {
		/*"AXP", "BA", "CAT", "CSCO", "CVX", "DD", "DIS", "GE", "GS", "HD", "IBM", "INTC", "JNJ", "JPM",
		 *  "KO", "MCD", "MMM", "MRK", "MSFT", "NKE", "PFE", "PG", "T", "TRV", "UNH", "UTX", "V", "VZ", "WMT", "XOM"); 
		 */
		String ticker = "GOOG";
		Date startDate;
		Date endDate;
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 5, 30);
		startDate = cal.getTime();
		cal.set(Calendar.YEAR, 2016);
		endDate = cal.getTime();
		double start = System.currentTimeMillis();
		MarketCalendar mc = new MarketCalendar(startDate, endDate);
		SecurityInst s = new SecurityInst(mc, ticker, 100);
		s.getStockInfo();
		s.loadHistoricalPriceData(mc.getStartDate(), mc.getEndDate());

		String orderDate = "02-25-2014";

		//Example neither StopLoss or Stop get filled
		double entryPrice = 560f;
		int orderQty = 100;
		double stopLossPrice = 450f;
		double stopPrice = 961f;      
		//       s.createMMS(orderDate, orderQty, entryPrice, stopLossPrice, stopPrice);
		//     s.executeOrder();

		//Example Stop fills for a $1 profit Long
		//    stopPrice = 561f;
		//   s.createMMS(orderDate, orderQty, entryPrice, stopLossPrice, stopPrice);
		//     s.executeOrder();

		//Example Stoploss fills for a Loss
		//   stopPrice = 961f;
		stopLossPrice = 559f;
		//  s.createMMS(orderDate, orderQty, entryPrice, stopLossPrice, stopPrice);
		s.executeOrder();


		/*
        //add an order to the stocks's mms 
        String delims ="-";
        String[] tokens = orderDate.split(delims);
      //  cal.set(2014, 2, 25);
        cal.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
        startDate = cal.getTime();        

        s.getMms().getOrder().placeOrder(entryPrice, 100, startDate,
        								 GlobalVars.orderLife.GoodTillCancelled, 
						        		 GlobalVars.typeOrder.Limit,
						        		 GlobalVars.orderStatus.Active );
        //add a stop loss
        s.getMms().getStop().placeOrder(stopLossPrice, -100, startDate,
						        	     GlobalVars.orderLife.GoodTillCancelled, 
							       		 GlobalVars.typeOrder.StopLoss,
							       		 GlobalVars.orderStatus.Inactive );        
        //add a profit target stop
        s.getMms().getProfitTarget().placeOrder(stopPrice, -100, startDate,
							       	     GlobalVars.orderLife.GoodTillCancelled, 
								       	 GlobalVars.typeOrder.Stop,
								       	 GlobalVars.orderStatus.Inactive );         

        s.getMms().getOrder().setPrice(500f);
        s.getMms().getOrder().setQuantity(100);
        s.getMms().getOrder().setOrderLife(GlobalVars.orderLife.Extended);   
        s.getMms().getOrder().setTypeOrder(GlobalVars.typeOrder.Limit);
        s.getMms().getOrder().setOrderDate(startDate);
        s.getMms().getOrder().setOrderStatus(GlobalVars.orderStatus.Active);
        //add a stop loss
        // if (s.getMms().getStop().getOrderStatus()==GlobalVars.orderStatus.Active) {
        s.getMms().getStop().setPrice(450f);
        s.getMms().getStop().setQuantity(-100);
        s.getMms().getStop().setOrderLife(GlobalVars.orderLife.GoodTillCancelled);   
        s.getMms().getStop().setTypeOrder(GlobalVars.typeOrder.Stop);
        s.getMms().getStop().setOrderDate(startDate);
        s.getMms().getStop().setOrderStatus(GlobalVars.orderStatus.Inactive);       
        //add a profit target stop
        //  if (s.getMms().getProfitTarget().getOrderStatus()==GlobalVars.orderStatus.Active) {
        s.getMms().getProfitTarget().setPrice(650f);
        s.getMms().getProfitTarget().setQuantity(-100);
        s.getMms().getProfitTarget().setOrderLife(GlobalVars.orderLife.GoodTillCancelled);   
        s.getMms().getProfitTarget().setTypeOrder(GlobalVars.typeOrder.Stop);
        s.getMms().getProfitTarget().setOrderDate(startDate);
        s.getMms().getProfitTarget().setOrderStatus(GlobalVars.orderStatus.Inactive);*/ 



		//place the order for execution 

		//if filled, save date and price (candlestick) and activate the stops
		/*
        if (s.getMms().getOrder().executeOrder(s)==GlobalVars.orderStatus.Filled) {
            s.getMms().getStop().setOrderStatus(GlobalVars.orderStatus.Active);
            s.getMms().getProfitTarget().setOrderStatus(GlobalVars.orderStatus.Active);
            s.getMms().getStop().executeOrder(s);
            s.getMms().getProfitTarget().executeOrder(s);
        }


        //run stops to see which filled first if any
        //if either filled save candlestick
        //if (s.getMms().getOrder().executeOrder(s)==GlobalVars.orderStatus.Filled) {
        if (s.getMms().getOrder().getOrderStatus()==GlobalVars.orderStatus.Filled) {

        	//if both Stop and StopLoss would have filled, the one that filed first is use
            if (s.getMms().getStop().getOrderStatus()==GlobalVars.orderStatus.Filled &&
                s.getMms().getProfitTarget().getOrderStatus()==GlobalVars.orderStatus.Filled)
                   if (s.getMms().getStop().getFilledCS().getDate().before(s.getMms().getProfitTarget().getFilledCS().getDate())) 
                       s.getMms().setFilledCS(s.getMms().getProfitTarget().getFilledCS());
                   else s.getMms().setFilledCS(s.getMms().getStop().getFilledCS());
            //if the StopLoss was filled but not the Stop, use the StopLoss
            else if (s.getMms().getStop().getOrderStatus()==GlobalVars.orderStatus.Filled &&
                s.getMms().getProfitTarget().getOrderStatus()!=GlobalVars.orderStatus.Filled)
                   s.getMms().setFilledCS(s.getMms().getStop().getFilledCS());
          //if the StopLoss was not filled but the Stop was filled, use the Stop
            else if (s.getMms().getStop().getOrderStatus()!=GlobalVars.orderStatus.Filled &&
                    s.getMms().getProfitTarget().getOrderStatus()==GlobalVars.orderStatus.Filled)
                       s.getMms().setFilledCS(s.getMms().getProfitTarget().getFilledCS());
           //if neither where filled, use the last last candlestick.  It is attached to both orders
            else s.getMms().setFilledCS(s.getMms().getProfitTarget().getFilledCS());
        }            
		 */
		//write P&L and ROI to memory

		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000;
		System.out.println("Info retreial took " + duration + " seconds.\n");

		/*
         double roi = s.getMms().getROI();

        double pl = s.getMms().getProfitAndLoss();
        System.out.println("ROI: " + GlobalVars.wf.format(roi) +"%" );
        System.out.println("Profit(Loss) per share:  "  + GlobalVars.ef.format(pl));
        System.out.println("Profit(Loss):  "  + GlobalVars.ef.format(pl*s.getMms().getOrder().getQuantity()));
        System.out.println("**************************");
		 */
		System.out.println(s.getMms().toString());

	} // main
	public void resetMMS() {
		this.setFilledCS(null);
		this.resetaStopFiled();
		//this.in
		if(this.getOrder()!=null) this.getOrder().resetOrder();
		if(this.getProfitTarget()!=null) this.getProfitTarget().resetOrder();
		if(this.getExit()!=null) this.getExit().resetOrder();
		for(Order o : this.getOrders()){
			if(o!=null) o.resetOrder();
		}
	}
}
