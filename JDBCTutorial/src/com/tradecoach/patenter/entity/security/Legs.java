/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tradecoach.patenter.entity.security;

import com.utilities.GlobalVars.*;
import com.workers.PatternFormation;
import com.workers.SecurityInst;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.utilities.GlobalVars;

public class Legs {
	
	protected ArrayList<Leg> Legs;
        protected ArrayList<PatternFormation> Patterns;
        private Double MeanRationAB2CD, sdRationAB2CD, maxRatioAB2CD, minRatioAB2CD, varRatioAB2CD;
        public LinkedList<Leg> l;
	public ListIterator<Leg> li;
        
        SecurityInst outer;

	public Legs(SecurityInst outer) {
            this.outer = outer;
            Legs = new ArrayList<Leg> ();
    //        LinkedListExmpl lle = new LinkedListExmpl();
	}

        public void FindPatterns() {
            Leg XA, AB, BC, CD;
            boolean isXABC, isXABCD = false, isGartley222 = false, isButterFly = false, isTiger, isInPlay = false;
            Calendar calendar = Calendar.getInstance(); // this would default to now
            /*
             * loop trough all legs in order and find all patterns (if any)
             */
            for(int i=this.getSize(); i > 2; i--) {
                isInPlay = (this.getLeg(i).getEndDate()==calendar.getTime());
                /*    
                XA = this.getLeg(i-3);
                AB = this.getLeg(i-2);
                BC = this.getLeg(i-1);
                CD = this.getLeg(i);
                */
                CD = this.getLeg(i);
                BC = CD.getPriorLeg();
                AB = BC.getPriorLeg();
                XA = AB.getPriorLeg();
                Integer volumeInCDatB = null;
                
                //see if first two legs have formed
            //    if(AB.getHighPrice()>CD.getHighPrice()&& AB.getLowPrice()>CD.getLowPrice()) {
           //       if(AB.getEndPoint()>CD.getHighPrice()&& AB.getLowPrice()>CD.getLowPrice()) {
             if (false) {
                PatternFormation pf = new PatternFormation();
                    pf.setFullyExecuted(!isInPlay);//indicated if this is historical or is a pattern being forecasted
                    pf.setAB(AB);
                    pf.setBC(BC);
                    pf.setCD(CD);
                    pf.setXA(XA);
                    pf.setTypePattern(typePattern.XABC);
                    isXABC = true ;
                    //see if last leg has formed and pattern type
               //     if(isXABC && XA.getLowPrice()<CD.getLowPrice()) {
                //        pf.setTypePattern(typePattern.XABCD);
               //         isXABCD = true;
               //     }
                                       //get the volume on CD as passes through B point level
              //      for(int j=0;j<CD.getBars();j++) 
              //             volumeInCDatB=this.outer.getCandleSticks().getAtPriceAfterDate(AB.getEndDate(),CD.getHighPrice()).getVolume();
                    //if volume is drying up, its a Tiger Gartley222, if volume is increased, its a Tiger ButterFly
              //      isTiger=(AB.getLowPriceVolume()*1.1>volumeInCDatB && isGartley222) || (AB.getLowPriceVolume()*1.1<volumeInCDatB && isButterFly);	
                    if (isXABCD) {                    
                        isGartley222 = isXABCD;
                        pf.setTypePattern(isTiger?typePattern.Gartley222:typePattern.TigerGartley222);
                    }//if
            //        else if (!isGartley222 && isXABC && XA.getLowPrice()>CD.getLowPrice()){
            //             isButterFly = true; 
             //            pf.setTypePattern(isTiger?typePattern.Butterfly:typePattern.TigerButterfly);
                    }//else if    
             //       Patterns.add(pf);
                    
               }//if     
           }//for
    //FindPatterns
        
        public void RunStats() {
            double sum = 0.0d, minRatioAB2CD=0, maxRatioAB2CD=0, temp = 0;;
            int size = 0;
            for (Iterator<PatternFormation> it = Patterns.iterator(); it.hasNext();) {
                PatternFormation pf = it.next();
                sum += pf.getRatioAB2CD();
                size++;
                minRatioAB2CD=(minRatioAB2CD==0)?minRatioAB2CD:Math.min(minRatioAB2CD, pf.getRatioAB2CD());
                maxRatioAB2CD=(maxRatioAB2CD==0)?maxRatioAB2CD:Math.max(maxRatioAB2CD, pf.getRatioAB2CD());
            }//for
            this.setMeanRatioAB2CD(sum/size);
            this.setMinRatioAB2CD(minRatioAB2CD);
            this.setMaxRatioAB2CD(maxRatioAB2CD);

            for (Iterator<PatternFormation> it = Patterns.iterator(); it.hasNext();) {
               PatternFormation pf = it.next();
               temp += (this.getMeanRatioAB2CD()-pf.getRatioAB2CD()) * (this.getMeanRatioAB2CD()-pf.getRatioAB2CD());
             }//for  
            this.setVarRatioAB2CD(temp/size);
            this.setSdRatioAB2CD(Math.sqrt(this.getVarRatioAB2CD()));            
	}//RunStats
            
                
        public void FindSwingPoints(SecurityInst outer) {
            this.outer = outer;
            CandleSticks cs = outer.getCandleSticks();
            CandleStick c;
            CandleStick nextC;
            CandleStick lastGapEndPoint;
            CandleStick endPointIfUp, endPointIfDown;
            Leg lastLeg = null;
            double closePrice =0.00d;
            double lastPrice =0.00d;
            double hightestPrice =0.00d;
            double lowestPrice =100000000.00d;
            double higherHigh = 0, lowerLow;
            int hightestPriceVolume =0;
            int lowestPriceVolume =0;
            double avgPrice26Day =0.00d, avgPrice12Day = 0.00d;
            double MA = 0d;
            double MAlast = 0d;
            int bars=0;
            /*
             * loop dialy prices and deterine 15 and 3 day moiving average
             * if the two cross, then determine that the highest/lowest price
             * were swing points
             */
            try { 
                Iterator<CandleStick> i = cs.candleSticks.iterator();
                while (i.hasNext()) {                      
                    c = i.next(); 

                    bars++;
                    
                    if (c.getClosePrice()>hightestPrice) {
                        hightestPrice=c.getClosePrice();                        
                        hightestPriceVolume=c.getVolume();                        
                    }
                    if (c.getClosePrice()<lowestPrice) {
                        lowestPrice=c.getClosePrice();
                        lowestPriceVolume=c.getVolume();                        
                    }
                    for(int j=0; j<26; j++) {
                        nextC=c;
                        avgPrice26Day += c.nextCandle.getClosePrice();  
                        nextC=c.nextCandle;
                    }
                    avgPrice26Day /= 12; 
                    for(int j=12; j>0; j--) {
                        nextC=c;
                        avgPrice12Day += c.nextCandle.getClosePrice();  
                        nextC=c.nextCandle;                        
                    }
                    avgPrice12Day /= 3;
                    MA = avgPrice12Day/avgPrice26Day;
                    
                    int k=0;
                    higherHigh = c.getClosePrice(); 
                    lowerLow = higherHigh;
                    for(k=0; k<5; k++) {
                        nextC=c;
                        higherHigh = Math.max(c.nextCandle.getClosePrice(),higherHigh); 
                        lowerLow = Math.min(c.nextCandle.getClosePrice(),lowerLow);  
                        nextC=c.nextCandle;
                    }
                    //this probably is not necesary as it was determined in the last leg that the
                    //highest higer or lowest low from that point in time had been hit.
                    for(; k<=0; k--) {
                        nextC=c;
                        higherHigh = Math.max(c.getPriorCandle().getClosePrice(),higherHigh); 
                        lowerLow = Math.min(c.getPriorCandle().getClosePrice(),lowerLow);  
                        nextC=c.getPriorCandle();
                    }                    
                    
             //       if((MA < MAlast)&&(lastLeg.getMoveDirection()== GlobalVars.direction.DOWN) ||
              //         (MA > MAlast)&&(lastLeg.getMoveDirection()== GlobalVars.direction.UP)     ||
                    if((c.getClosePrice()==higherHigh)&&(lastLeg.getMoveDirection()== GlobalVars.direction.DOWN) ||
                       (c.getClosePrice()==lowerLow)&&(lastLeg.getMoveDirection()== GlobalVars.direction.UP)     ||
                        lastLeg == null) {
                      //      closePrice = c.getClosePrice();
                            Leg leg = new Leg();
                            leg.setEndPoint(c);
                          //  leg.setStartDate(c.getDate());
                         //   leg.setEndDate(c.getDate()); 
                         //   leg.setLowPrice(lowestPrice);
                         //   leg.setHighPrice(hightestPrice);
                        //    leg.setLowPriceVolume(lowestPriceVolume);
                         //   leg.setHighPriceVolume(hightestPriceVolume);
                       //     leg.setBarSize(GlobalVars.barSize.Day);
                            leg.setBars(bars);
                            leg.setPriorLeg(lastLeg);
                          //  if(lastLeg==null) 
                        //        leg.setMoveDirection(hightestPrice>lowestPrice?GlobalVars.direction.DOWN:GlobalVars.direction.UP);
                            //else 
                          //      leg.setMoveDirection(leg.getPriorLeg().getMoveDirection()==GlobalVars.direction.UP?GlobalVars.direction.DOWN:GlobalVars.direction.UP);
                            leg.getPriorLeg().setNextLeg(leg);
                            leg.getPriorLeg().getMoveDirection();
                            if(c.getPriorCandle().closePrice!=c.getOpenPrice()) {
                                GapInPrice gap = new GapInPrice(c);
                                leg.getGaps().add(gap);
                             //   lastGapEndPoint = c;
                            }
                            lastLeg = new Leg();   
                            //now reset the measures for the next leg
                       //     closePrice =0.00d;
                       //     lastPrice =0.00d;
                       //     lowestPrice =hightestPrice;
                       //     hightestPrice =0.00d;
                       //     hightestPriceVolume =0;
                       //     lowestPriceVolume =0;
                            avgPrice26Day =0.00d;
                            avgPrice12Day =0.00d;
                            MA = 0d;
                            MAlast = 0d;
                            bars=0;
                    }//if

                }//while
            }//try  
            catch ( Exception e ) {
                e.printStackTrace();
             } // end catch
         finally {}
        }//FindSwingPoints
        
        
        public PatternFormation getInPlayPattern() {
            /*
             * get the pattern, if any, of this secuirty where the pattern is currently being
             * played out.  If there is none, reutrn null.  Note: just because a patern is
             * return does not mean it is tradable.
             */
            PatternFormation pf = Patterns.get(Patterns.size()-1);
            return !pf.isFullyExecuted()?pf:null;
        }
        
     //   public SaveLegs {
                
       // }
        
	public ArrayList<Leg> getLegs() {
		return Legs;
	}
        
        public Leg getLeg(int index) {
            return this.Legs.get(index);
        }

	public void setLegs(ArrayList<Leg> Legs) {
		this.Legs = Legs;
	}

        public int getSize() {
            return this.Legs.size();
        }
        
	public Leg getLast() {
		//return Legs.get(Legs.size()-1);
		//price date is being held in descending order
		try {
			return Legs.get(0);			
		}
		catch(Exception ee) {
			
		}
		return null;
		
	}
        
        public void add(int i) { 
  //          l.add(i);
        }

        public ArrayList<PatternFormation> getPatterns() {
            return Patterns;
        }

        public void setPatterns(ArrayList<PatternFormation> Patterns) {
            this.Patterns = Patterns;
        }

        public Double getMeanRatioAB2CD() {
            return MeanRationAB2CD;
        }

        public void setMeanRatioAB2CD(Double MeanRationAB2CD) {
            this.MeanRationAB2CD = MeanRationAB2CD;
        }

    public Double getSdRatioAB2CD() {
        return sdRationAB2CD;
    }

    public void setSdRatioAB2CD(Double sdRationAB2CD) {
        this.sdRationAB2CD = sdRationAB2CD;
    }

    public Double getMaxRatioAB2CD() {
        return maxRatioAB2CD;
    }

    public void setMaxRatioAB2CD(Double maxRationAB2CD) {
        this.maxRatioAB2CD = maxRationAB2CD;
    }

    public Double getMinRatioAB2CD() {
        return minRatioAB2CD;
    }

    public void setMinRatioAB2CD(Double minRationAB2CD) {
        this.minRatioAB2CD = minRationAB2CD;
    }

    public Double getVarRatioAB2CD() {
        return varRatioAB2CD;
    }

    public void setVarRatioAB2CD(Double varRatioAB2CD) {
        this.varRatioAB2CD = varRatioAB2CD;
    }
        

}
