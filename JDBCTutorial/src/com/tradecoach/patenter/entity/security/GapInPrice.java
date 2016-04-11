package com.tradecoach.patenter.entity.security;
public class GapInPrice {
    CandleStick endPoint;

    public GapInPrice() {}

    public GapInPrice(CandleStick endPoint) {
        this.endPoint = endPoint;
    }
    
    public double getGapSize() {
       return  endPoint.getPriorCandle().getClosePrice()-endPoint.getOpenPrice();
    }    
}
