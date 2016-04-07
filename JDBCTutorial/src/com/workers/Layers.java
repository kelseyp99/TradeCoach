package com.workers;

import java.util.Stack;

import com.utilities.GlobalVars;

import com.workers.Portfolios.Pcts4Lyer;

public class Layers implements GlobalVars {	
	private Layers NextLayer, PriorLayer;
	private int layerCount;
	private int applicationOrder;
	private TrailingStop parentStop;
	private Double triggerPrice, trailingPct, trlTriggerPct;
	private boolean dynamic;
	private String rowStyle = "";
	/**
	 * @param CandleStick cs
	 * @return void
	 * 
	 * used to create the additional layers
	 */
	public Layers(TrailingStop parentStop, Layers PriorLayer) {
		super();
		initialize(parentStop, PriorLayer);
		}
	/**
	 * @param
	 * @return void
	 * 
	 * used to create the initial layer
	 * <br><br>Layers(TrailingStop order) </br>
	 */
	public Layers(TrailingStop order) {
		super();
		//create the initial layer node with PriorLayer is null
		initialize(order,  null);
		}		
	/**
	 * @param
	 * @return void
	 * 
	 * used to create to clone the current layer
	 * <br><br>Layers(Layers layers) </br>
	 * 
	 */	
	public Layers(Layers layers) {
		initialize(layers.getParentStop(), layers);
	}
	@Override
	public String toString() {
		String s;
		if(this.getParentStop().getBelongsTo().getBetaValue()!=0 && !this.isDynamic())
			s =  String.format("<li>Layer: [triggerPrice=%s (%s), trailingPct=%s, Dynamic:  %s ]</li>",cf.format(triggerPrice),pf.format(this.getTrlTriggerPct()),pf.format(this.getTrailingPct()),this.isDynamic());
		else
			s =  String.format("<li>Layer: [triggerPrice=%s (%s), trailingPct=%s, Dynamic:  %s, Beta=%.2f ]</li>",cf.format(triggerPrice),pf.format(this.getTrlTriggerPct()),pf.format(this.getTrailingPct()),this.isDynamic(),this.getParentStop().getBelongsTo().getBetaValue());
			
		return s;
	}
	public String toStringTR() {
				String s;
				if(this.getParentStop().getBelongsTo().getBetaValue()!=0 && !this.isDynamic())					
					s=String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",this.getApplicationOrder(),cf.format(triggerPrice),pf.format(this.getTrlTriggerPct()),pf.format(this.getTrailingPct()),this.isDynamic());
				//	s =  String.format("<li>Layer: [triggerPrice=%s (%s), trailingPct=%s, Dynamic:  %s ]</li>",cf.format(triggerPrice),pf.format(this.getTrlTriggerPct()),pf.format(this.getTrailingPct()),this.isDynamic());
				else
					s=String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",this.getApplicationOrder(),cf.format(triggerPrice),pf.format(this.getTrlTriggerPct()),pf.format(this.getTrailingPct()),this.isDynamic(),this.getParentStop().getBelongsTo().getBetaValue());
					
				return s;
			}
	public static String toStringTH() {
				String s;
				String col1 ="Layer";
				String col2 ="Trigger<br>Price";
				String col3 ="Trailing<br>Trigger<br>Percentage";
				String col4 ="Trailing<br>Percentage";
				String col5 ="Dynamic";
				String col6 ="Beta<br>Value";
				s=String.format("<tr><th>%s</th><th>%s</th><th>%s</th><th>%s</th><th>%s</th><th>%s</th></tr>",col1,col2,col3,col4,col5,col6);
				return s;
			}
	/**	
	 * called from constructors.  Calls itself recursively creating a linked list of <b>Layer</b> objects instances specified by the value of <i>layerCount</i> passed initially
	 * 
	 * @param
	 * @return void
	 */	
	private void initialize(TrailingStop parentStop, Layers PriorLayer) {
		try {
			if(!parentStop.getBelongsTo().getMmsTemp().getLayerStack().empty()) {//if this is the first layer in the linked-list
				this.setParentStop(parentStop);			
				if(PriorLayer!=null) {				
					PriorLayer.setNextLayer(this);
					this.setPriorLayer(PriorLayer);	
					this.setApplicationOrder(PriorLayer.getApplicationOrder()+1);//set ascending number to be displayed in the table
				}	
				//now get the next layer of the stack for this scenario
				Pcts4Lyer pl = (Pcts4Lyer) parentStop.getPartOf().getLayerStack().pop();
				this.setDynamic(pl.isDynamic());				
				this.setTrlTriggerPct(pl.getTrlTriggerPct());
				if(this.isDynamic()) {
					Double beta = Math.abs(this.getBelongsToSecurity().getBetaValue());
					this.setTrailingPct(pl.getTrlStopPct()*(beta==0?1:beta));
				}
				else
					this.setTrailingPct(pl.getTrlStopPct());
				/*set the trigger price to be a percentage increase/decrease relative to the initial order entry price.  if the stop 
				 was a buy (buy to cover) then the trigger price decrease.  If it is a sell (sell-to-close) then the trigger price increases */
				this.setTriggerPrice(parentStop.getPartOf().getOrder().getPrice()*(1+(this.getTrlTriggerPct())*(this.getParentStop().getDirection()==typeSignal.Buy?-1:1)));
				//this.setTrlPctRate(pl.getTrlTriggerPct());
				//keep creating additional layer nodes until specified node count is reached which is when the stack containing all 
				//the layer percentages is empty
				if(!parentStop.getPartOf().getLayerStack().empty()) 
					this.setNextLayer(new Layers(this));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	//end initialize
	/**
	 * ///DEPRICATED///
	 * <p>if this is not the last node in the linked-list sequence of <b>Layers</b> and 
	 * the best price of <b>cs</b> exceeds the trigger price for the <i>NEXT</i> <b>Layers</b> instance which is stored in <b>DummyOrder</b> then
	 * change the current layer to the next layer in sequence and set the new trigger price to that of the next layer in sequence.</p>
	 * <p>Note: the <b>DummyOrder</b> is owned by the parent trailing-stop.  It serves only to store the trigger price at which the <i>NEXT</i>
	 * <b>Layer</b> instance in sequence is made to be the current layer.</p>
	 * @param CandleStick cs
	 * @return void
	 */
	public void setCurrentLayer(CandleStick cs){  ///DEPRICATED///
		//if this CandleStick's price exceeded the trigger price for the next layer ...
		//Note:  the DummyOrder is sent as beatThisPrice receives an order type not a layer though the next layer trigger price is stored in the next layer
		try {
			if(this.getNextLayer()!=null && cs.beatsThisPrice(this.getParentStop().getDummyOrder())){
					//...then set the current layer to the next layer in sequence ...
					this.getParentStop().setCurrentLayer(this.getNextLayer());
					//and set the new trigger price to that of the next layer in sequence
					this.getParentStop().getDummyOrder().setPrice(this.getNextLayer().getTriggerPrice());
				}
		} catch (UndefinedOrderType e) {
			e.printStackTrace();
		}//if
	}
	/** return the first individual <b>Layers</b> instance in the linked-list of <b>Layers</b>.*/
	public Layers getFirstLayer() {
		Layers l = this;
		while (l.getPriorLayer() != null) {
				l = this.getPriorLayer();
		}
		return l;		
	}
	/** return the last individual <b>Layers</b> instance in the linked-list of <b>Layers</b>.*/	
	public Layers getLastLayer() {
		Layers l = this;
		while (l.getNextLayer() != null) {
				l = this.getNextLayer();
		}
		return l;		
	}	
    /**<p><b>public Layers getNextLayer()</b></p>
     * <p>Returns the next layer instance in the linked-list of layers for this order</p>
     * @param
     * @return     */
	public Layers getNextLayer() {
		return NextLayer;
	}

	public void setNextLayer(Layers nextLayer) {
		NextLayer = nextLayer;
	}
	
	public boolean hasNextLayer() {
		return this.getNextLayer() != null;
		
	}

	public Layers getPriorLayer() {
		return PriorLayer;
	}

	public void setPriorLayer(Layers priorLayer) {
		PriorLayer = priorLayer;
	}

	public int getLayerCount() {
		return layerCount;
	}

	public void setLayerCount(int layerCount) {
		this.layerCount = layerCount;
	}

	public TrailingStop getParentStop() {
		return parentStop;
	}

	public void setParentStop(TrailingStop parentStop) {
		this.parentStop = parentStop;
	}
	public Double getTriggerPrice() {
		return triggerPrice;
	}
	public void setTriggerPrice(Double triggerPrice) {
		this.triggerPrice = triggerPrice;
	}
	public Double getTrlTriggerPct() {
		return trlTriggerPct;
	}
	public void setTrlTriggerPct(Double trlTriggerPct) {
		this.trlTriggerPct = trlTriggerPct;
	}
	public Double getTrailingPct() {
		return trailingPct;
	}
	public void setTrailingPct(Double trailingPct) {
		this.trailingPct = trailingPct;
	}
	/**returns <i>true</i> if this trailing-stops trailing percentage rate moves as the price moves.  Basically, if it is <i>false</i>
	 * then it functions as a regular stop.  This used to make a trailing-stop mimic the users original stop */
	public boolean isDynamic() {
		return dynamic;
	}
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	private SecurityInst getBelongsToSecurity() {
		return this.getParentStop().getBelongsTo();
	}
	public String getRowStyle() {
		return rowStyle;
	}
	public void setRowStyle(String rowStyle) {
		this.rowStyle = rowStyle;
	}
	public int getApplicationOrder() {
		return applicationOrder;
	}
	public void setApplicationOrder(int applicationOrder) {
		this.applicationOrder = applicationOrder;
	}
}
