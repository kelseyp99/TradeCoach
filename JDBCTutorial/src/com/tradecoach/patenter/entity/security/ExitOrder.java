package com.tradecoach.patenter.entity.security;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.utilities.GlobalVars;
import com.workers.Tools;

public class ExitOrder extends Order implements IStopLossExit {

	public ExitOrder(com.utilities.GlobalVars.typeOrder typeOrder) {
		super(typeOrder);
		// TODO Auto-generated constructor stub
	}

	public ExitOrder() {
		// TODO Auto-generated constructor stub
	}

	public ExitOrder(Order order) {
		super(order);
	}
	@Override
	public void executeOrder() {
		String s = String.format("Exit price of the <i>%s</i> order does not result in a %s.<br>  This funftionality is not supported. The <i>%s</i> will be disregarded.<br>",
				this.getTypeOrder(),
				this instanceof IProfitExit?"profit":"loss",
				this.getTypeOrder()	);
				
	    s = String.format("<html><p>%s</p><br><p>%s:  Enter <span style=\"color:red;\"><i>%s</i></span> order to %s %s shares of <b>%s</b> @ Price: <span style=\"color:red;\"><i>%s</i></span></p></html>",
				s,
				df2.format(this.getOrderDate()),
				this.getTypeOrder(),
				Tools.getTradeDescription(this) ,
				this.getQuantity(),
				this.getBelongsTo().getTickerSymbol(),
				cf.format(this.getPrice())); 
		if(this instanceof IProfitExit){
			if(this.priceWasBeaten(this.getParentOrder().getPrice())){
			JOptionPane.showMessageDialog(this.getFrameGUI(),
					s,
					"Warning",
					JOptionPane.WARNING_MESSAGE);

			this.setPrice(0d);
			}
		} else if(this instanceof IStopLossExit) {
			if(!this.priceWasBeaten(this.getParentOrder().getPrice())){
	/*		String s = "Exit price of the <i>StopLoss</i> order does not result in a loss.<br>  This funftionality is not supported. The <i>StopLoss</i> will be disregarded.<br>";
			s = String.format("<html><p>%s</p><br><p>%s:  Enter <span style=\"color:red;\"><i>%s</i></span> order to %s %s shares of <b>%s</b> @ Price: <span style=\"color:red;\"><i>%s</i></span></p></html>",
					s,
					df2.format(this.getOrderDate()),
					this.getTypeOrder(),
					Tools.getTradeDescription(this) ,
					this.getQuantity(),
					this.getBelongsTo().getTickerSymbol(),
					cf.format(this.getPrice())); 
			*/
					JOptionPane.showMessageDialog(this.getFrameGUI(),
					s,
					"Warning",
					JOptionPane.WARNING_MESSAGE);

			this.setPrice(0d);	
					}
		} //else {
			super.executeOrder();
	//	}
	}

}
