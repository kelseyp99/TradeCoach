/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workers;

import com.tradecoach.patenter.entity.security.Leg;
import com.tradecoach.patenter.entity.security.Legs;
import com.utilities.GlobalVars;

public class PatternFormation implements GlobalVars {
   
    private typePattern typePattern;
    private Leg XA;
    private Leg AB;
    private Leg BC;
    private Leg CD;
    private boolean fullyExecuted;
                
    public PatternFormation() {
    }
    
    public Double getRatioAB2CD() {
        return (CD.getDollarSpan()/AB.getDollarSpan());
    }
    
    public double estimateNextSwing(Legs outer, double confidence) {
        /*
         * estimated the statically derived next swing point of this
         * pattern formation at a confidence interval.
         */
        if (this.getTypePattern()==typePattern.TigerGartley222 || this.getTypePattern()==typePattern.TigerButterfly ) {
//            double meanRatioAB2CD = outer.getMeanRatioAB2CD();
//            double sdRatioAB2CD = outer.getSdRatioAB2CD();
            double dollarSpanAB = this.getAB().getDollarSpan();
            double x = confidence * 1.960 / 95;//desired standard deviation fraction
//            x = meanRatioAB2CD + (sdRatioAB2CD * x);//estimated ratio of CD to AB at confidence interval
            double dollarSpanCDestimate = dollarSpanAB * x;//estimated length in dollars of CD
//            double highPrice = this.getCD().getHighPrice();
 //           double nextSwing = highPrice - dollarSpanCDestimate;//estimate turning point.
  //          return nextSwing;
            return 0.00d;
        }//if
        //return -1 if this is not a tradable pattern
        else  return -1d ;        
    }//estimateNextSwing
    
    public typePattern getTypePattern() {
        return typePattern;
    }

    public void setTypePattern(typePattern typePattern) {
        this.typePattern = typePattern;
    }

    public Leg getXA() {
        return XA;
    }

    public void setXA(Leg XA) {
        this.XA = XA;
    }

    public Leg getAB() {
        return AB;
    }

    public void setAB(Leg AB) {
        this.AB = AB;
    }

    public Leg getBC() {
        return BC;
    }

    public void setBC(Leg BC) {
        this.BC = BC;
    }

    public Leg getCD() {
        return CD;
    }

    public void setCD(Leg CD) {
        this.CD = CD;
    }

    public boolean isFullyExecuted() {
        return fullyExecuted;
    }

    public void setFullyExecuted(boolean fullyExecuted) {
        this.fullyExecuted = fullyExecuted;
    }
    
    

    
    
    
    
    
}
