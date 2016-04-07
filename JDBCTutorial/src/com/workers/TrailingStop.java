package com.workers;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import com.utilities.GlobalVars;

public class TrailingStop extends Order {
	private Layers currentLayer, initialLayer ;
	private int layerCount;
	private Double trigPrcIncRate, trailingPct, trlPctRate;
	
	public TrailingStop(typeOrder typeOrder) {
		super.setTradePart(GlobalVars.tradePart.Exit);
	}
	
	public TrailingStop(Order oldOrder) {
		super.setTradePart(oldOrder.getTradePart());
    	this.placeOrder(oldOrder.getTriggerPrice(),
    					oldOrder.getPrice(),
    					oldOrder.getQuantity(),
    					oldOrder.getOrderDate(),
    					oldOrder.getOrderLife(), 
    					oldOrder.getTypeOrder(),
    					oldOrder.getOrderStatus(), 
    					oldOrder.getPartOf(), 
    					oldOrder.getParentOrder()
    					);
    	try {
			//initialize the first layer
			this.setCurrentLayer(new Layers(this));
			this.setInitialLayer(this.getCurrentLayer().getFirstLayer());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		String s = String.format("<p>Trailing-Stop layers applied to this position [Layer Count=%s]</p>", this.getLayerCount());
		if(true){
			s ="<table style=\"width:100%; border: 1px solid black;  border-collapse: collapse;\">"; 
			s +="<caption><i>These trailing-stop layers will be applied to this position to atempt to improve ROI</i></caption>";
			Layers l = this.getInitialLayer().getFirstLayer();
			s += l.toStringTH();
			try {
				while (l.hasNextLayer()) {
					l = l.getNextLayer();
					s += l.toStringTR();// + (l.getTriggerPrice()==this.getCurrentLayer().getTriggerPrice()?"-Current Layer":"") + "\n";					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			s +="</table>";
		} else {
			s +="<ul style=\"list-style-type:none\">";
			Layers l = this.getInitialLayer().getFirstLayer();
			s += l.toString() + "\n";
			try {
				while (l.hasNextLayer()) {
					l = l.getNextLayer();
					s += l.toString() + (l.getTriggerPrice()==this.getCurrentLayer().getTriggerPrice()?"-Current Layer":"") + "\n";					
				}
				s +="</ul>";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return s;
	}

	@Override
	/**
	 * 
	 * @param stopTriggerPrice
	 * @param price
	 * @param qty
	 * @param startDate
	 * @param OrderLife
	 * @param TypeOrder
	 * @param OrderStatus
	 * @param mms
	 * @param parentOrder
	 * @param trigPrcIncRate
	 * @param trailingPct
	 * @param trlPctRate
	 * <br><p> calls the super method then adds layers specific to trailing stops </p>
	 */
	public void placeOrder(double stopTriggerPrice, double price, int qty, Date startDate, 
			orderLife OrderLife,
			typeOrder TypeOrder,
			orderStatus OrderStatus, 
			MoneyMgmtStrategy mms,
			Order parentOrder, 
			double trigPrcIncRate, double trailingPct, double trlPctRate) {

		super.placeOrder(stopTriggerPrice, price, qty, startDate, OrderLife,TypeOrder,	OrderStatus, mms, parentOrder  );
		this.setTrailingPct(trailingPct);
		this.setTrlPctRate(trlPctRate);
		this.setTrigPrcIncRate(trigPrcIncRate);
		this.setLayerCount(mms.getLayerStack().size()); 
		//initialize the first layer
		this.setCurrentLayer(new Layers(this));
		this.setPrice(this.getCurrentLayer().getTriggerPrice());
		this.setInitialLayer(this.getCurrentLayer().getFirstLayer());
	}//placeOrder

	/**
	 * Return <i>true</i> if the best price of this <b>CandleStick</b> instance would cause this <b>Order</b> to execute and the trade then to exit.
	 * Alternately, the best price of this <b>CandleStick</b> instance may cause the stop price of this <b>Order</b> to move in the direction of higher profit.
	 * If so, the exit price of this <b>Order</b> is moved unless it has previously been moved to a better price.  Note: the exit price is only made better, it
	 * is never made worse.
	 * 
	 * @param cs
	 * @return boolean
	 */
	@Override
	public boolean compareToCandleStick(CandleStick cs){
		int hierarchy = 1; String s;
		//exit if not active or already filled
		if(this.isFilled() || this.isInActive()) return false;
		//if this candle stick occurred after the initial order date...
		if(Tools.isSameDayOrLater(cs.getDate(),this.getOrderDate())) {
			if(Tools.isSameDayOrLater(cs.getDate(),this.getParentOrder().getFilledCS().getDate())) {
				//... if the trigger price is less (more if short) than the high (low if short) of this candlestick
				//and the order price has not already been moved to a better price
				// then move the stop price of this trailing stop to the new position
				if (this.priceWasBeaten(cs)) {
			//		this.setPriceAndLayer(cs);//the current layer will only be moved if the trigger price of the NEXT layer in sequence was passed
					//unless this layer is not set to not move its price
					if(this.getCurrentLayer().hasNextLayer())
					if((this.isLongPosition() && cs.getHighPrice() > this.getCurrentLayer().getNextLayer().getTriggerPrice()) 
					|| (this.isShortPosition() && cs.getLowPrice() < this.getCurrentLayer().getNextLayer().getTriggerPrice())) 
					{
						//System.out.printf(df2.format(cs.getDate())+":  ");
						//this.getPartOf().cout(df2.format(cs.getDate())+":  ");
						this.makeNextLayerCurrentLayer(cs);
					}					
					if (this.getCurrentLayer().isDynamic()) {
						if (this.isLongPosition()) {
							if(this.getPrice() > cs.getOpenPrice()){
								hierarchy = 1;
								//System.out.println(df2.format(cs.getDate()) + ":  Trade Exited at Open");
								s = String.format("<li>%s:  Trade Exited at Open<li>", df2.format(cs.getDate()));
								this.addKeyPair(cs, s,  hierarchy);
							//	this.getPartOf().getTradeActivityMap().put(cs.getDate(), String.format("<li>%s:  Trade Exited at Open<li>", df2.format(cs.getDate())));
							//	this.getPartOf().getTradeActivityMap().put(dfYYYYMMdd.format(this.getOrderDate())+this.getPartOf().getSeqID(), s);
						//		this.getPartOf().cout("<li>"+df2.format(cs.getDate()) + ":  Trade Exited at Open</li>");
								return this.filledPlus(cs);
							}
							double v = cs.getHighPrice() * (1 - this.getCurrentLayer().getTrailingPct());
							if (v > this.getPrice()) {
//								this.getPartOf().cout(String.format("<li>%s: Daily Low: %s, TrailingStop price moved to %s (%s), High/Open/Close: %s/%s/%s</li>",
//										df2.format(cs.getDate()), cf.format(cs.getHighPrice()),
//										cf.format(v), pf.format(this.getCurrentLayer().getTrailingPct()),
//									    cf.format(cs.getLowPrice()), cf.format(cs.getOpenPrice()),
//									    cf.format(cs.getClosePrice())));
								//keyPair[0] = dfYYYYMMdd.format(this.getDate())+2+p.getPartOf().getSeqID();
								//this.getOrder().getPartOf().getTradeActivityMap().put(keyPair[0],keyPair[1]);;
								//getKeyPair
								hierarchy = 1;
								s = String.format("<li>%s: Daily Low: %s, Trailing-Stop price moved to %s (%s), High/Open/Close: %s/%s/%s</li>",
										df2.format(cs.getDate()), cf.format(cs.getHighPrice()),
										cf.format(v), pf.format(this.getCurrentLayer().getTrailingPct()),
									    cf.format(cs.getLowPrice()), cf.format(cs.getOpenPrice()),
									    cf.format(cs.getClosePrice()));
								this.addKeyPair(cs, s,  hierarchy);
//								this.getPartOf().cout(df2.format(cs.getDate()) +": Daily High:  "+ cf.format(cs.getHighPrice())+",  TrailingStop price moved to " + cf.format(v) 
//										      + " ("  + pf.format(this.getCurrentLayer().getTrailingPct()) 
//										      +"), Low/Open/Close:  " +cf.format(cs.getLowPrice())+"/"+cf.format(cs.getOpenPrice())+"/"+cf.format(cs.getClosePrice()));
								this.setPrice(v);
							}
							if(v > cs.getClosePrice()){
							//	this.getPartOf().cout(df2.format(cs.getDate()) + ":  Trade Exited at Close");
								hierarchy = 1;
								//this.getPartOf().cout(String.format("<li>%s:  Trade Exited at Close</li>",df2.format(cs.getDate())));
								s=String.format("<li>%s:  Trade Exited at Close</li>",df2.format(cs.getDate()));
								this.addKeyPair(cs, s,  hierarchy);
								return this.filledPlus(cs);
							}
							return false;
						}
						if (this.isShortPosition()) {
							//if the stop price has been exceeded over-night, then exit the trade
							if(this.getPrice() < cs.getOpenPrice()){
								s=String.format("<li>%s:  Trade Exited at Open</li>",df2.format(cs.getDate()));
								this.addKeyPair(cs, s,  hierarchy);
								return this.filledPlus(cs);
							}
							if (this.getCurrentLayer().getTriggerPrice() >= cs.getLowPrice()) {//the price went below the buy-to-cover' trigger price
								double v = cs.getLowPrice()	* (1 + this.getCurrentLayer().getTrailingPct());
								if (v < this.getPrice()) {
//									this.getPartOf().cout(String.format("<li>%s: Daily Low: %s, TrailingStop price moved to %s (%s), High/Open/Close: %s/%s/%s</li>",
//																		df2.format(cs.getDate()), cf.format(cs.getLowPrice()),
//																		cf.format(v),pf.format(this.getCurrentLayer().getTrailingPct()),
//																		cf.format(cs.getHighPrice()),cf.format(cs.getOpenPrice()),
//																		cf.format(cs.getClosePrice())));
									s=String.format("<li>%s: Daily Low: %s, Trailing-Stop price moved to %s (%s), High/Open/Close: %s/%s/%s</li>",
											df2.format(cs.getDate()), cf.format(cs.getLowPrice()),
											cf.format(v),pf.format(this.getCurrentLayer().getTrailingPct()),
											cf.format(cs.getHighPrice()),cf.format(cs.getOpenPrice()),
											cf.format(cs.getClosePrice()));
									this.addKeyPair(cs, s,  hierarchy);
									this.setPrice(v);
								}
								if(v < cs.getClosePrice()){
							//		this.getPartOf().cout(String.format("<li>%s:  Trade Exited at Close</li>",df2.format(cs.getDate())));
									s=String.format("<li>%s:  Trade Exited at Close</li>",df2.format(cs.getDate()));
									this.addKeyPair(cs, s,  hierarchy);
									//this.getPartOf().cout(df2.format(cs.getDate()) + ":  Trade Exited at Close");
									return this.filledPlus(cs);
								}
							}
							return false;
						}
					}//if (this.getCurrentLayer().isDynamic()) {
					if(this.getPartOf().getProfitTarget()!=null) {
						if(this.getPartOf().getProfitTarget().compareToCandleStick(cs)) {
						//	this.getPartOf().cout("The trade was exited at " + cf.format(this.getPartOf().getProfitTarget().getPrice()));
							this.getPartOf().cout(String.format("<li>The trade was exited at %s</li>", cf.format(this.getPartOf().getProfitTarget().getPrice())));
							s=String.format("<li>The trade was exited at %s</li>", cf.format(this.getPartOf().getProfitTarget().getPrice()));
							this.addKeyPair(cs, s,  hierarchy);
							return this.filledPlus(cs);
						}
					}						
				}
				else {
					//if stop not moved, see if it executed, basically, it either went up and triggered stop to be moved, or
					//it went down through the stop price and trade was exited or it did neither so nothing was changed
					//this.setTypeOrder(typeOrder.Stop);
					SimpleExit se =  new SimpleExit(this);
					//s.setPrice(this.getPrice());
					//s.setTypeOrder(this.getTypeOrder());
					se.activate();
					se.setOrderDate(cs.getDate());
					if(se.compareToCandleStick(cs)) {
						//this.getPartOf().cout("The trade was exited at " + cf.format(this.getPrice()));
					//	this.getPartOf().cout(String.format("<li>The trade was exited at %s</li>", cf.format(this.getPrice())));
						s=String.format("<li>The trade was exited at %s</li>", cf.format(this.getPrice()));
						this.addKeyPair(cs, s,  hierarchy);
						return this.filledPlus(cs);
					}
				}
			}
		}
		String tx = null;
		//treeMap auto sorts the map by the key.  The key is the date plus an additional sort string (e.g. 20151106 00002)
		Map<String, String> treeMap = new TreeMap<String, String>(this.getPartOf().getTradeActivityMap());
		this.getPartOf().getTradeActivityMap().clear();
		//build a sorted sting representing transactions that are in proper time order and add it to cout
		for (Map.Entry<String, String> entry : treeMap.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
			tx += entry.getValue();	
		}
		this.getPartOf().cout(tx);
		return false;
	}//compareToCandleStick	
	/**adds a <strong>keyPair</strong> to a <i>Map</i>.  Ultimately this map is sorted on its key.  This puts the values in chronological
	 * order.  The entire sorted string of values is then added to the <i>TradeActivity</i> variable through the <i>cout</i> method
	 * @param hierarchy provides a sub-sorting string for values having the same data per the <b>cs</b> instance passed.*/
	private void addKeyPair(CandleStick cs, String s, int hierarchy) {
		String [] keyPair = new String[2];
		keyPair[0] = dfYYYYMMdd.format(cs.getDate())+hierarchy+this.getPartOf().getSeqID();
		keyPair[1]=s;
		this.getPartOf().getTradeActivityMap().put(keyPair[0],keyPair[1]);
	}
	/**The instances of <b>Layers</b> collectively form a <i>linked-list</i>.  This methods advances the current 
	 * layer designation to the next instance of <b>Layers</b> in the <i>linked-list</i>.  However, before it does 
	 * this, it collects the results of the soon to be prior instance of <b>Layers</b> <i>toString()</i> method. 
	 * Next and after the advancement the the next instance of <b>Layers</b> it collects the results new current 
	 * instance of <b>Layers</b> <i>toString()</i> method.  The two are concatenated to form an output string. */ 
	public void makeNextLayerCurrentLayer(CandleStick cs) {	
		String s = this.getCurrentLayer().toString();
		this.setCurrentLayer(this.getCurrentLayer().getNextLayer());
		s = String.format("<li>%s: The Trailing-Stop current layer was moved from/to<ul>%s%s</ul></li>" , 
							df2.format(cs.getDate()), 
							s,
							this.getCurrentLayer().toString());

		this.addKeyPair(cs, s,  1);
	//	this.getPartOf().cout(s);
	}
	
	/**
	 * @param CandleStick cs
	 * @return void
	 * <p>if the best price of <b>cs</b> exceeds the trigger price for this <b>layer</b> instance which is stored in <i>DummyOrder</i> then
	 * change the current <b>layer</b> to the next <b>layer</b> in sequence and set the new trigger price to that of the next layer in sequence</p>
	 *
	 */
	public void setPriceAndLayer(CandleStick cs){
		this.getCurrentLayer().setCurrentLayer(cs);
	}
	public Layers getInitialLayer() {
		return initialLayer;
	}

	public void setInitialLayer(Layers initialLayer) {
		this.initialLayer = initialLayer;
	}

	/**
	 * 
	 * @return Layer
	 * <p>returns the current <i>trailing-stop's</i> cache of parameters</p>
	 */
	public Layers getCurrentLayer() {
		return currentLayer;
	}

	public void setCurrentLayer(Layers currentLayer) {
		this.currentLayer = currentLayer;
	}

	public int getLayerCount() {
		return layerCount;
	}

	public void setLayerCount(int layerCount) {
		this.layerCount = layerCount;
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

}
