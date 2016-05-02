package com.tradecoach.patenter.entity.security;

//import PatternTypes.IPatternType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.utilities.GlobalVars;
import com.workers.SecurityInst;
@Entity
@Table(name = "price_collections")
public class CandleSticks extends EntityBean  implements IEntityBean, GlobalVars {
	@Id
	@GeneratedValue(generator = "foreigngen")
	@GenericGenerator(strategy = "foreign", name="foreigngen",
	parameters = @Parameter(name = "property", value="security"))
	@Column(name = "ID") private Integer id;
	@Transient private CandleStick highestHighCS;
	@Transient private CandleStick lowestLowCS;
	@OneToOne(mappedBy = "candleSticks") private Securities security;
	@Transient private SecurityInst belongsTo;	

	@OneToMany(mappedBy = "candleSticks", cascade = CascadeType.ALL)
	private Set<CandleStick> candleStick = new HashSet<CandleStick>();

	protected ArrayList<CandleStick> candleSticks;
     	public CandleSticks(SecurityInst si) {
			super();
			candleSticks = new ArrayList<CandleStick> ();
			this.setBelongsTo(si);                      
		}
		
	public ArrayList<CandleStick> getCandleSticks() {
		return candleSticks;
	}

	public void setCandleSticks(ArrayList<CandleStick> candleSticks) {
		this.candleSticks = candleSticks;
	}

        public int getSize() {
            return this.candleSticks.size();
        }
        /**
         * @param Date atDate
         * @return CandleStick
         * 
         * public CandleStick getAtDate(Date atDate)
         * 
         * find the CandleStick that corresponds with atDate
         */ 
        public CandleStick getAtDate(Date atDate) {            
            for(int i=0; i<candleSticks.size();i++ )   {
                if (candleSticks.get(i).getDate()==atDate) {
                    return candleSticks.get(i);
                } //if                
            } //for
            return null;
        }
        
        public CandleStick getAtPriceAfterDate(Date atDate, Double closePrice){
         //  getAtDate(Date atDate) {            
            for(int i=0; i<candleSticks.size();i++ )   {
                if (candleSticks.get(i).getDate()==atDate) 
                    for(int j=i; j<candleSticks.size();j++ )   {
                     if (candleSticks.get(j).getClosePrice()<=closePrice) 
                      return candleSticks.get(i);
                } //if                
            } //for             
            return null;
        }//getAtPriceAfterDate
        /**
         * <ol>
         * <li>loops through all <b>CandleStick</b> instances</li>
         * <li>compares each <b>CandleStick</b> instance to the original order to see if it satisfy the entry/exit criteria</li>
         * <li>if no <b>CandleStick</b> instance is found to satisfy the criteria, the last most recent <b>CandleStick</b> instance is set as the stop price then returned
         * </ol>
         * used to call the  <i>this.getNextPriceHigherThan(order)</i> method for this instance of <b>CandleSticks</b>
         * @param order
         * @return
         */
      /*  public CandleStick getPriceFilled(Order order){
            return this.getNextPriceHigherThan(order);
        }*/        
        public CandleStick getPriceFilled(Order order){        	
            try {
            	if(candleSticks.isEmpty()) throw new Exception(String.format("No candlestick exists for %s at index 0", order.getBelongsTo().getTickerSymbol()));
			//	for(int i=candleSticks.size()-1;i>=0;i-- )   {
            	for(int i=0;i<=candleSticks.size()-1;i++ )   {
				//	System.out.printf("%d)  %s\n",i,candleSticks.get(i).getDate().toString());
				  //  if ( candleSticks.get(i).beatsThisPrice(stopPrice)) {
					if(order.compareToCandleStick(candleSticks.get(i))) {
				    	//save the filled price with the candlestick
				    	candleSticks.get(i).setStopPrice(order.getPrice());				    	
				        return candleSticks.get(i);
				    }//if
				} //for   
		//		candleSticks.get(0).setStopPrice(candleSticks.get(0).getClosePrice());
            	candleSticks.get(candleSticks.size()-1).setStopPrice(candleSticks.get(candleSticks.size()-1).getClosePrice());
				return candleSticks.get(candleSticks.size()-1);//if didnt find a fill price, return the most recent cs as profit is unrealized
            } catch (IndexOutOfBoundsException i ) {
            	i.printStackTrace();
            	System.out.println("Exception thrown  :" + i);
            	   return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}            
        }//getNextPriceHigherThan
        
        public CandleStick getLast() {
		//return candleSticks.get(candleSticks.size()-1);
		//price date is being held in descending order
		try {
			return candleSticks.get(0);			
		}
		catch(Exception ee) {
			
		}
		return null;		
	}//getLast
/**
 * <p>resets all the attributes in the <b>CandleSticks</b> and the <b>CandleStick</b> instances</p>
 */
    public void resetCandleSticks(){
    	//currently there are no attributes to reset in the CandleSticks instances
    	for(int i=candleSticks.size()-1;i>=0;i-- )   {
    		candleSticks.get(i).resetCandleStick();
    	} //for   
    }
    
    public SecurityInst getBelongsTo() {
			return belongsTo;
		}

		public void setBelongsTo(SecurityInst belongsTo) {
			this.belongsTo = belongsTo;
		}
		
		public CandleStick getLastCandleStick() {
			return this.getCandleSticks().get(this.getCandleSticks().size()-1);
		}
		
/**loops through the candlesticks in the date range of this order and finds the highest and lowest price.
 * the resultant prices are saved to feils of the order instance*/
	public void setPriceMaxMin(Order order){
    	double highestHigh = 0d, lowestLow=99999999999999999d;
    	CandleStick highestHighCS = null, lowestLowCS = null;
        for(int i=candleSticks.size()-1;i>=0;i-- )   {
        	if (order.getOrderDate().before(candleSticks.get(i).getDate())) {
	            if ( candleSticks.get(i).getHighPrice()>highestHigh) {
	            	highestHigh = candleSticks.get(i).getHighPrice();
	            	highestHighCS = candleSticks.get(i);
	            } //if
	            if ( candleSticks.get(i).getLowPrice()<lowestLow) {
	            	lowestLow = candleSticks.get(i).getLowPrice();
	            	lowestLowCS = candleSticks.get(i);
	            } //if
        	} //if
        } //for 
        order.setHighestHighCS(highestHighCS);
        order.setLowestLowCS(lowestLowCS);
    }//getNextPriceHigherThan   
    
    public void set3X3DMA(Order order){
    	int daysDisplayed = 3, days = 3; 
    	double c = 0d, lastClose = 0d, dma=0d;
    	CandleStick highestHighCS = null, lowestLowCS = null;
   	
        for(int i=candleSticks.size()-1;i>=0;i-- )   {
        	if (order.getOrderDate().before(candleSticks.get(i).getDate())) {
        		for(int x=daysDisplayed;x>0;x--) {
        			for(int y=days;y>0;y--) 
        				c +=  candleSticks.get(i-x-y).getClosePrice(); 
        			lastClose = c/3;
        		}
        	} //if
        } //for 
    }//set3X3DMA   
    
    public void setMACD(Order order){
    	int daysDisplayed = 3, days = 3; 
    	double c = 0d, lastClose = 0d, dma=0d;
    	CandleStick highestHighCS = null, lowestLowCS = null;
   	
        for(int i=candleSticks.size()-1;i>=0;i-- )   {
        	if (order.getOrderDate().before(candleSticks.get(i).getDate())) {
        		for(int x=daysDisplayed;x>0;x--) {
        			for(int y=days;y>0;y--) 
        				c +=  candleSticks.get(i-x-y).getClosePrice(); 
        			lastClose = c/3;
        		}
        	} //if
        } //for 
    }//set3X3DMA   

    public void setCsTypes() {
         for(int i=0; i<candleSticks.size();i++ )   {
                candleSticks.get(i).solveType();
                } //if                
            } //for

	public CandleStick getHighestHighCS() {
		return highestHighCS;
	}

	public void setHighestHighCS(CandleStick highestHighCS) {
		this.highestHighCS = highestHighCS;
	}

	public CandleStick getLowestLowCS() {
		return lowestLowCS;
	}

	public void setLowestLowCS(CandleStick lowestLowCS) {
		this.lowestLowCS = lowestLowCS;
	}

    
    
    }
        
  
