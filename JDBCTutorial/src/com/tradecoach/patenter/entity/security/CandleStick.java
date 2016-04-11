/*
 * This holds the daily price action of the security
 */

package com.tradecoach.patenter.entity.security;

import java.util.Date;
import java.io.Serializable;
//import PatternTypes.IPatternType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.joda.time.DateTime;

import com.utilities.GlobalVars;

import com.workers.Tools;

public  class CandleStick implements GlobalVars, Serializable {
	
	private String tickerSymbol;
	Date date;
	double openPrice;
	double closePrice;
	double highPrice;
	double lowPrice;
	double stopPrice;//used to keep filled price when this cs goes through a stop
	double adjustedClosePrice;
	Integer volume;
	private boolean selected;
	barSize timeUnit;
	typePattern candleStickType;  //hangman, doji, etc
	Boolean swingPoint;
	CandleSticks belongsTo;
	CandleStick priorCandle;
	CandleStick nextCandle;
	CandleStick sectorCandle;
	CandleStick majorIndexCandle;
        @Resource( name="jdbc/ADDRESSES" )
        DataSource dataSource;
        	
	public CandleStick(Date date, double openPrice, double closePrice,
			double highPrice, double lowPrice, double adjustedClosePrice,
			int volume, CandleSticks cs) {
		super();
		this.date = date;
		this.openPrice = openPrice;
		this.closePrice = closePrice;
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.adjustedClosePrice = adjustedClosePrice;
		this.volume = volume;
		this.setBelongsTo(cs);
	}
        
        public CandleStick() {
		// TODO Auto-generated constructor stub
	}

		public void solveType(){
             //  this.setCandleStickType(PatternTypes.FPatternType.getPatternType(this));
        }
        
        public boolean SaveHistory(String pkey)  {
         Connection connection = null;
         PreparedStatement saveHistory = null;
         //CandleStick c;
         try {
       //   Iterator<CandleStick> i = cs.candleSticks.iterator();
       //   while (i.hasNext()) {    
       //     c = i.next();
            connection = dataSource.getConnection();
            saveHistory = connection.prepareStatement(
               "INSERT INTO CANDLESTICKS " + 
               "(SEC_INST_ID, TRADE_DATE, OPEN_PRICE, CLOSE_PRICE, HIGH_PRICE, LOW_PRICE, ADJUSTED_CLOSE_PRICE, " +     
               "VOLUME, BAR_SIZE, TIME_UNIT, CANDLESTICK_PATTERN, SWING_POINT, PRIOR_CANDLESTICK, NEXT_CANDLESTICK) " +
               "VALUES ( ? , ? , ? , ? , ? , ?, ? )");
            saveHistory.setString( 1, pkey );
            saveHistory.setDate(2, (java.sql.Date) this.getDate());
            saveHistory.setDouble( 4, this.getOpenPrice() );
            saveHistory.setDouble( 3, this.getClosePrice());
            saveHistory.setDouble( 5, this.getHighPrice() );
            saveHistory.setDouble( 6, this.getLowPrice() );
         //   saveHistory.setDouble( 6, this);
            saveHistory.setDouble( 7, this.getVolume() );
            saveHistory.executeUpdate();
       //     }//while
            return true;
         } // end try
         catch ( SQLException e ) {
            e.printStackTrace();
            return false;
         } // end catch
         catch ( Exception e ) {
            e.printStackTrace();
            return false;
         } // end catch
         finally {
            try {
               saveHistory.close();
               connection.close();
            } // end try
            catch ( Exception e ) {
               e.printStackTrace();
               return false;
            } // end catch
           } // end finally
        }  //SaveHistory 
        
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getOpenPrice() {
		return openPrice;
	}
	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}
	public double getClosePrice() {
		return closePrice;
	}
	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}
	public double getHighPrice() {
		return highPrice;
	}
	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}
	public double getLowPrice() {
		return lowPrice;
	}
	public double getBestPrice(GlobalVars.typeSignal typeSignal) {
		return typeSignal==GlobalVars.typeSignal.Buy?this.getHighPrice():this.getLowPrice();
	}
	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}
	public Integer getVolume() {
		return volume;
	}
	@Override
	public String toString() {
		return "CandleStick \n date=" + df2.format(this.getDate()) + ",\n openPrice=" + cf.format(this.getOpenPrice())
				+ ",\n closePrice=" + cf.format(this.getClosePrice()) + ",\n highPrice=" + cf.format(this.getHighPrice())
				+ ",\n lowPrice=" + cf.format(this.getLowPrice()) + ",\n adjustedClosePrice="
				+ cf.format(this.getAdjustedClosePrice()) + ",\n volume=" + wf.format(this.getVolume()) ;
	}
	
	public String [] toStringWhenFilled(Order p) {
	/*	
		return(s);*/
	//	String s = null;
		String [] keyPair = new String[2];
		try {
			if(p==null) throw new Exception("Order passed was null");
			/* s =   "<li>"+df2.format(this.getDate())+":  "
				//	+Tools.PastTense(p.getDirection().toString())+" "
					+Tools.getTradeDescriptionPast(p) +" "
					+wf.format(p.getQuantity())+" shares of "
					+p.getBelongsTo().getTickerSymbol()
					+" at "+cf.format(p.getPrice()) 
					+", Open/High/Low/Close:  " +cf.format(this.getOpenPrice())
					+"/"+cf.format(this.getHighPrice())
					+"/"+cf.format(this.getLowPrice())
					+"/"+cf.format(this.getClosePrice())+"</li>" ;*/
			keyPair[0] = dfYYYYMMdd.format(this.getDate())+2+p.getPartOf().getSeqID();
			keyPair[1] = String.format("<li>%s:  %s %s shares of <b>%s</b> at %s, Open/High/Low/Close:  %s/%s/%s/%s</li>",
					 df2.format(this.getDate()), 
					 Tools.getTradeDescriptionPast(p),
					 wf.format(p.getQuantity()),
					 p.getBelongsTo().getTickerSymbol(),
					 cf.format(p.getPrice()),
					 cf.format(this.getOpenPrice()),
					 cf.format(this.getHighPrice()),
					 cf.format(this.getLowPrice()),
					 cf.format(this.getClosePrice())) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyPair;
	}
	
	public String [] toStringWhenStillOpen(Order p) {
		String [] keyPair = new String[2];
		/*return   "<li>"+df2.format(this.getDate())+":  "
				+"Trade still open:  " 
				+wf.format(p.getQuantity())+" shares of "
				+p.getBelongsTo().getTickerSymbol()
				+" at "+cf.format(p.getPrice()) 
				+", Open/High/Low/Close:  " +cf.format(this.getOpenPrice())
				+"/"+cf.format(this.getHighPrice())
				+"/"+cf.format(this.getLowPrice())
				+"/"+cf.format(this.getClosePrice())+"</li>" ;*/
		keyPair[0] = dfYYYYMMdd.format(this.getDate())+2+p.getPartOf().getSeqID();
		keyPair[1] = String.format("<li>%s:  Trade still open: %s shares of <b>%s</b> at %s, Open/High/Low/Close:  %s/%s/%s/%s</li>",
					 df2.format(this.getDate()), 
				//	 Tools.getTradeDescriptionPast(p),
					 wf.format(-p.getQuantity()),
					 p.getBelongsTo().getTickerSymbol(),
					 cf.format(this.getClosePrice()),
					 cf.format(this.getOpenPrice()),
					 cf.format(this.getHighPrice()),
					 cf.format(this.getLowPrice()),
					 cf.format(this.getClosePrice())) ;
		return keyPair;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}
	public barSize getTimeUnit() {
		return timeUnit;
	}
	public void setTimeUnit(barSize timeUnit) {
		this.timeUnit = timeUnit;
	}
        
        public double getRangeHighLow() {
            return this.getHighPrice()-this.getLowPrice();
        }
        
        public GlobalVars.direction getOpen2CloseUpDown() {
            return this.getOpenPrice()>this.getClosePrice()?GlobalVars.direction.DOWN:GlobalVars.direction.UP;        }

        public CandleStick getPriorCandle() {
            return priorCandle;
        }

        public void setPriorCandle(CandleStick priorCandle) {
            this.priorCandle = priorCandle;
        }

        public CandleStick getNextCandle() {
            return nextCandle;
        }

        public void setNextCandle(CandleStick nextCandle) {
            this.nextCandle = nextCandle;
        }

    public typePattern getCandleStickType() {
        return candleStickType;
    }

    public void setCandleStickType(typePattern candleStickType) {
        this.candleStickType = candleStickType;
    }
   /**
    * determine whether this <b>CandlesSick</b> instance exceeded the best entry or exit price of <i>order</i>.  The date of this <b>CandlesSick</b> instance must have occurred 
    * after the activation date
    * associated with <i>order</i>.  The order type of <i>order</i> further determines the operations performed.
    * @param order
    * @return boolean
    */
    public boolean beatsThisPrice(Order order) throws UndefinedOrderType {

    	try {
    		// 	System.out.print("if "+df.format(this.getDate()));
    		// 	System.out.println(" on or after " + df.format(order.getOrderDate()));

    		//if this candlestick occurs after the date order placed...
    		if(Tools.isSameDayOrLater(this.getDate(),order.getOrderDate())){//.after(order.getOrderDate())) {
    			//if this is the initial order...
    			if(order.getTypeOrder()==typeOrder.Limit) {
    				return order.compareToCandleStick(this);
    				/*//if this is a LONG position...
	    			if(order.getDirection()==typeSignal.Buy) 
	    				return order.getPrice()>=this.getLowPrice(); 
	    			else //if this is a SHORT position...
	    				return order.getPrice()<=this.getHighPrice();*/ }    		
    			else if(order.getTypeOrder()==typeOrder.StopLoss) {
    				/*if(this.getDate().after(order.getParentOrder().getFilledCS().getDate())) {
	            	//if this is the StopLoss order...	
	    			if(order.getDirection()==typeSignal.Sell) 
	    			//if this is a LONG position...
	    				return order.getPrice()>=this.getLowPrice(); 
	    			else //if this is a SHORT position...
	    				return order.getPrice()<=this.getHighPrice(); 
	    			}*/
    			} 
    			else if(order.getTypeOrder()==typeOrder.Stop) {
    				if(this.getDate().after(order.getParentOrder().getFilledCS().getDate())) {
    					//if this is the Stop order...	
    					if(order.getDirection()==typeSignal.Sell) 
    						//if this is a LONG position...
    						if(order.isInActive() && order.getTriggerPrice()<=this.getHighPrice())
    							//...but has not been triggered as active, and the price rose above trigger price
    							order.activate();//...then make the order active...
    						else if(order.isActive())//...if order is active...
    							return order.getPrice()>=this.getLowPrice();//tell whether it executed
    							else 
    								return false;
    					else //if this is a SHORT position..
    						//if this is a LONG position...
    						if(order.isInActive() && order.getTriggerPrice()>=this.getLowPrice())
    							//...but has not been triggered as active, and the price dropped below trigger price
    							order.activate();//...then make the order active...
    						else if(order.isActive())//...if order is active...
    							return order.getPrice()<=this.getHighPrice();//tell whether it executed
    						else 
    							return false;
    					//return order.getPrice()>=this.getLowPrice(); 
    				}
    				else if(order.getTypeOrder()==typeOrder.SimpleExit) {
    					order.compareToCandleStick(this);
    				}
    			}
    			else if(order.getTypeOrder()==typeOrder.TrailingStop) {///DEPRICATED///
    				//order is the DummyOrder.  Get its parent order
    				if(this.getDate().after(order.getParentOrder().getFilledCS().getDate())) {
    					//if this is the TrailingStop order...	
    					if(order.getDirection()==typeSignal.Sell) {	//if this is a LONG position...
    						if(order.getPrice()<=this.getHighPrice()) {
    							//if the highest price of this cs exceeds the current exit price of this trailingStop then increase 
    							//the exit price of this trailingStop to the the cs high less the trailStop percentage lag 
    							//??this portion here apears deprecated as the dummy orders price (represented by order) is being set again at the calling function
    						//	order.setPrice(this.getHighPrice()*(1-((TrailingStop)order).getCurrentLayer().getTrailingPct()));
    							return false; 
    							}
    					}
    					else if(order.getDirection()==typeSignal.Buy) { //if this is a SHORT position...
    						if(order.getPrice()>=this.getLowPrice()) {
    						//		order.setPrice(this.getHighPrice()*(1-((TrailingStop)order).getCurrentLayer().getTrailingPct()));
    							return false; }
    					}
    					else throw new UndefinedOrderType("typeSignal is empty or not recognized");
    					//if stop not moved, see if it executed
    					order.setTypeOrder(typeOrder.Stop);
    					if(this.beatsThisPrice(order)) {
    						order.setTypeOrder(typeOrder.TrailingStop);
    						return true;
    					} else {
    						order.setTypeOrder(typeOrder.TrailingStop);
    						return false;  				
    					}
    				}
    			}

    			else return false;

    		}


    		/*

	        return this.getDate().after(order.getOrderDate()) && (
	               (order.getPrice()>=this.getLowPrice() && order.getDirection()==GlobalVars.typeSignal.Buy)
	               || 
	               (order.getPrice()<=this.getHighPrice() && order.getDirection()==GlobalVars.typeSignal.Sell)
	               );*/
    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	return false;
    }  //beatsThisPrice     
    
    public void resetCandleStick(){
    	this.setStopPrice(0d);
    }

	public double getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(double stopPrice) {
		this.stopPrice = stopPrice;
	}

	public double getAdjustedClosePrice() {
		return adjustedClosePrice;
	}

	public void setAdjustedClosePrice(double adjustedClosePrice) {
		this.adjustedClosePrice = adjustedClosePrice;
	}

	public Boolean getSwingPoint() {
		return swingPoint;
	}

	public void setSwingPoint(Boolean swingPoint) {
		this.swingPoint = swingPoint;
	}

	public CandleStick getSectorCandle() {
		return sectorCandle;
	}

	public void setSectorCandle(CandleStick sectorCandle) {
		this.sectorCandle = sectorCandle;
	}

	public CandleStick getMajorIndexCandle() {
		return majorIndexCandle;
	}

	public void setMajorIndexCandle(CandleStick majorIndexCandle) {
		this.majorIndexCandle = majorIndexCandle;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public CandleSticks getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(CandleSticks belongsTo) {
		this.belongsTo = belongsTo;
	}
	
	public SecurityInst getBelongsToSecurity() {
		return this.getBelongsTo().getBelongsTo();
	}

	public String getTickerSymbol() {
		return this.getBelongsToSecurity().getTickerSymbol();
	}

	public void setTickerSymbol(String tickerSymbol) {
		this.getBelongsToSecurity().setTickerSymbol(tickerSymbol);
	}

	public boolean getSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
    
    
}


