/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;

import com.tradecoach.patenter.entity.security.InitialEntry;
import com.tradecoach.patenter.entity.security.Order;
import com.tradecoach.patenter.entity.security.SimpleExit;
import com.tradecoach.patenter.entity.security.Stop;
import com.tradecoach.patenter.entity.security.StopLoss;
import com.tradecoach.patenter.entity.security.TrailingStop;
import com.utilities.GlobalVars;
import com.utilities.GlobalVars.typeSignal;

import antlr.StringUtils;

public class Tools {
	/**
	 * 
	 * returns the number of days between the two dates passed, <i>date</i> and <i>date2</i>.
	 * 
	 * @param date1
	 * @param date2
	 * @return in
	 */
	public static int daysBetween(Date date1, Date date2) {
		return (int) (long) ((date2.getTime() - date1.getTime())/(1000*60*60*24));
	}

	/**
	 * @param date1
	 * @param date2
	 * @return
	 * returns true if date1 is on or after date2.&nbsp  Both dates are truncated back to midnight the night before.
	 */
	public static boolean isSameDayOrLater(Date date1, Date date2) {
		GregorianCalendar cal1 = new GregorianCalendar();
		GregorianCalendar cal2 = new GregorianCalendar();
		cal1.setTime(date1);
		cal2.setTime(date2);
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal2.set(Calendar.HOUR_OF_DAY, 0);
		cal2.set(Calendar.MINUTE, 0);
		cal2.set(Calendar.SECOND, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		return cal1.compareTo(cal2)>=0;
	}
	
	/**
	 * @param date1
	 * @param date2
	 * @return
	 * returns true if date1 is on or after date2.&nbsp  Both dates are truncated back to midnight the night before.
	 */
	public static boolean isLater(Date date1, Date date2) {
		GregorianCalendar cal1 = new GregorianCalendar();
		GregorianCalendar cal2 = new GregorianCalendar();
		cal1.setTime(date1);
		cal2.setTime(date2);
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		cal2.set(Calendar.HOUR_OF_DAY, 0);
		cal2.set(Calendar.MINUTE, 0);
		cal2.set(Calendar.SECOND, 0);
		cal2.set(Calendar.MILLISECOND, 0);
		return cal1.compareTo(cal2)>0;
	}
	
	/**
	 * 
	 * @param order
	 * @return 
	 * <p>return <i>true</i> if this is an <b>exit</b> type order</p> 
	 */
	public static boolean isExit(Order order) {
		return order instanceof  SimpleExit || order instanceof  Stop || order instanceof  StopLoss || order instanceof  TrailingStop;
	}
	/**
	 * 
	 * @param order
	 * @return 
	 * <p>return <i>true</i> if this is an <b>entry</b> type order</p> 
	 */
	public static boolean isEntry(Order order) {
		return order instanceof InitialEntry;
	}

	public static String PastTense(String s) {
        String pts = null;
        switch (s) {
            case "Buy":  pts = "Bought";
                     break;
            case "Sell":  pts = "Sold";
                     break;
        }
		
		return pts;
	}
	
	
    public static String getEntryDescription(Order p){
		return p.getDirection()==typeSignal.Buy?"Buy Long ":"Sell Short";
    	
    }
    
    public static String getEntryDescriptionPast(Order p){
		return p.getDirection()==typeSignal.Buy?"Bought Long ":"Sold Short";
    	
    }
    
    public static String getExitDescription(Order p){
 		return p.getDirection()!=typeSignal.Buy?"Sell ":"Buy to Close";
     	
     }
     
     public static String getExitDescriptionPast(Order p){
 		return p.getDirection()!=typeSignal.Buy?"Sold ":"Bought to Close";
     	
     }
     

 	public static String getTradeDescription(Order p) {
 		// TODO Auto-generated method stub
 		return p.getTradePart()==GlobalVars.tradePart.Exit?getExitDescription(p):getEntryDescription(p);
 	}
 	
	public static String getTradeDescriptionPast(Order p) {
 		// TODO Auto-generated method stub
 		return p.getTradePart()==GlobalVars.tradePart.Exit?getExitDescriptionPast(p):getEntryDescriptionPast(p);
 	}
	
	public static void createSectionHeader(String sTitle, String sDiscussion) {
		Tools.drawTitle(sTitle);
		System.out.println();
		System.out.println(sDiscussion);
		System.out.println();
		Tools.drawSeprator();		
		Tools.carriageReturn();
	}
	
	public static void createSectionFooter(String sTitle) {
		System.out.println();
		System.out.println(StringUtils.repeat("*", GlobalVars.outputLineWidth1));
		System.out.println("END OF " + sTitle);
		System.out.println(StringUtils.repeat("*", GlobalVars.outputLineWidth1));
		System.out.println(StringUtils.repeat("*", GlobalVars.outputLineWidth1)+"\n\n\n\n\n");		
	
	}

	/**Draws a row of "*"'s to output*/
	public static void drawSeprator() {
		System.out.println(StringUtils.repeat("*", GlobalVars.outputLineWidth1));		
	}
	
	/**Draws a row of "*"'s to output*/
	public static void drawTitle(String title) {
		int l = title.length();
		l=((GlobalVars.outputLineWidth1-l)/2)-1;
		String s = StringUtils.repeat("*", l);
		s = String.format("%s %s %s", s, title, s);
        Tools.carriageReturn(2);
		Tools.drawSeprator();
		System.out.println(s);
		Tools.drawSeprator();	
		Tools.carriageReturn(2);
	}
	
	/**Insert a blank line in the output*/
	public static void carriageReturn() {
			System.out.println();
		}
	/**Inserts blank line(s) in the output by the vale of <i>i</i>*/
	public static void carriageReturn(int i) {
		for (int j = 0; j < i; j++) {
			System.out.println();
		}
		
	}
	
    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	

}
