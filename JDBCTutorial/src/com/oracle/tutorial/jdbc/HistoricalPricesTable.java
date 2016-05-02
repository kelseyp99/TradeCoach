
	/*
	 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 *
	 *   - Redistributions of source code must retain the above copyright
	 *     notice, this list of conditions and the following disclaimer.
	 *
	 *   - Redistributions in binary form must reproduce the above copyright
	 *     notice, this list of conditions and the following disclaimer in the
	 *     documentation and/or other materials provided with the distribution.
	 *
	 *   - Neither the name of Oracle or the names of its
	 *     contributors may be used to endorse or promote products derived
	 *     from this software without specific prior written permission.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
	 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
	 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
	 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
	 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
	 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
	 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */

package com.oracle.tutorial.jdbc;

import java.io.FileWriter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Vector;
import com.gui.*;
import com.tradecoach.patenter.entity.security.CandleStick;
import com.tradecoach.patenter.entity.security.CandleSticks;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.sql.ordering.antlr.Factory;

import com.workers.PortfoliosGroup;
import com.workers.SecurityInst;
import com.workers.Tools;

public class HistoricalPricesTable extends Tables {

	   private static SessionFactory factory; 

	   public static void main(String[] args) {
		  HistoricalPricesTable hpt = new HistoricalPricesTable();
		  CandleStick cs = new CandleStick(new java.util.Date(), 10d, 10d, 10d, 10d, 10d, 100, null);

		  cs.setTickerSymbol("GOOG");
		  hpt.saveCandleStickPriceData(cs);
	  }
	  public HistoricalPricesTable(){}
	  public HistoricalPricesTable(Connection connArg, String dbNameArg, String dbmsArg, PortfoliosGroup belongsTo) {
		    super(connArg, dbNameArg, dbmsArg, belongsTo, "HISTORICAL_PRICES");
		    this.setDdlCreate("CREATE TABLE APP.HISTORICAL_PRICES (  TICKER_SYMBOL VARCHAR(20) NOT NULL, "+
													    		"TRADEDATE DATE NOT NULL, "+
													    		"CLOSEPRICE DOUBLE NOT NULL, "+
													    		"OPENPRICE DOUBLE NOT NULL, "+
													    		"HIGHPRICE DOUBLE NOT NULL, "+
													    		"LOWPRICE DOUBLE NOT NULL, "+
													    		"ADJCLOSE DOUBLE NOT NULL, "+
													    		"VOLUME INTEGER NOT NULL, "+
															    "SELECTED BOOLEAN, " + 
													    		"Primary Key (TRADEDATE,TICKER_SYMBOL) "+
													    		")");
		    super.initialize(this.getDdlCreate());
	}
	  
	  public HistoricalPricesTable(PortfoliosGroup belongsTo) {
	  super(belongsTo, "HISTORICAL_PRICES");
	  this.initialize();
	}
	public void saveAllSecuritiesPriceData(){
		  
		  Iterator<SecurityInst> i = this.getBelongsTo().getInitialPortfolio().getHoldingSet().iterator();
	        while (i.hasNext()) {
	        	saveSecurityPriceData(i.next());                  
	        }  //while  
		  
	  }
	  public  void loadSecurityPriceData(SecurityInst si) throws SQLException {
		  
		    Statement stmt = null;
		    String query =
		      "select TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME " +
		      "from APP.HISTORICAL_PRICES " +
		      "where TICKER_SYMBOL = '"+ si.getTickerSymbol() + "' " +
		      "order by TRADEDATE";
		    try {
		      stmt = con.createStatement();
		      ResultSet rs = stmt.executeQuery(query);
		      while (rs.next()) {
		    	CandleStick cs = new CandleStick();
		    	java.util.Date date = rs.getTimestamp("TRADEDATE");
		  		//Date date = rs.getDate("TRADEDATE");
		        cs.setDate(date);	        
		        double openPrice = rs.getDouble("OPENPRICE");
		        cs.setOpenPrice(openPrice);		        
		        double closePrice = rs.getDouble("CLOSEPRICE");
		        cs.setClosePrice(closePrice);	        
		        double highPrice = rs.getDouble("HIGHPRICE");
		        cs.setHighPrice(highPrice);	        
		        double lowPrice = rs.getDouble("LOWPRICE");
		        cs.setLowPrice(lowPrice);	        
		        double adjustedClosePrice = rs.getDouble("ADJCLOSE");
		        cs.setAdjustedClosePrice(adjustedClosePrice);	    	
		        int volume = rs.getInt("VOLUME");
		        cs.setVolume(volume);
		        
		        cs.setBelongsTo(si.getCandleSticks());
		    //    if(si==null || si.getCandleSticks()==null || si.getCandleSticks().getCandleSticks()==null) 
		        if(si==null)
		        	throw new Exception(String.format("si.getCandleSticks()==null;  Error happened when si = %s,  %s", si.getTickerSymbol(), cs.getDate().toString()));
		        if(si.getCandleSticks()==null)
		        	throw new Exception(String.format("si.getCandleSticks()==null;  Error happened when si = %s,  %s", si.getTickerSymbol(), cs.getDate().toString()));
		        if(si.getCandleSticks().getCandleSticks()==null)
		        	throw new Exception(String.format("si.getCandleSticks().getCandleSticks()==null;  Error happened when si = %s,  %s", si.getTickerSymbol(), cs.getDate().toString()));
		        si.getCandleSticks().getCandleSticks().add(cs);
		        
		        if(si.getOldestCandleStickSaved()==null)
		        	si.setOldestCandleStickSaved(cs);
		        if(si.getNewestCandleStickSaved()==null)
		        	si.setNewestCandleStickSaved(cs);
		        
		        if(!Tools.isSameDayOrLater(cs.getDate(), si.getOldestCandleStickSaved().getDate())) 
		        	si.setOldestCandleStickSaved(cs);
		        if(!Tools.isSameDayOrLater(si.getNewestCandleStickSaved().getDate(),cs.getDate())) 
		        	si.setNewestCandleStickSaved(cs);
		      }
		    } catch (SQLException e) {     
		    	JDBCTutorialUtilities.printSQLException(e);
		    } catch (Exception f){
		    	f.printStackTrace();
		    } finally {
		    	if (stmt != null) { stmt.close(); }
		    }
	  }
	  
	  public void saveSecurityPriceData(SecurityInst si) {			
		  try {
			  CandleSticks cs = si.getCandleSticks();
			  Iterator<CandleStick> i = cs.getCandleSticks().iterator();
			  while (i.hasNext()) {    
				  saveCandleStickPriceData(i.next());
			  }//while
		  } catch (Exception e) {
			  e.printStackTrace();
		  }		
	  }

	  public void saveCandleStickPriceData(CandleStick cs) {		 
		  if(this.getBelongsTo().getUseNativeJDBC()){
			  PreparedStatement stmt = null;
			  try {
				  stmt = con.prepareStatement(
						  "INSERT INTO HISTORICAL_PRICES" + 
								  "(TICKER_SYMBOL, TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME) " +
						  "VALUES (  ? , ? , ?, ? , ?, ?, ?, ?)");
				  //	  stmt.setInt(1, 111 );
				  stmt.setString(1, cs.getBelongsToSecurity().getTickerSymbol());
				  java.sql.Date sqlDate = new java.sql.Date(cs.getDate().getTime());
				  stmt.setDate(2, sqlDate);
				  stmt.setDouble(3, cs.getClosePrice());
				  stmt.setDouble(4, cs.getOpenPrice());
				  stmt.setDouble(5, cs.getHighPrice());
				  stmt.setDouble(6, cs.getLowPrice());
				  stmt.setDouble(7, cs.getAdjustedClosePrice());
				  stmt.setInt(8, cs.getVolume());
				  stmt.executeUpdate();
			  } catch (java.sql.SQLIntegrityConstraintViolationException s) {
				  //  JDBCTutorialUtilities..printSQLException(e);		  

			  } catch (SQLException e) {
				  JDBCTutorialUtilities.printSQLException(e);
			  } finally {
				  //if (stmt != null) { stmt.close(); }
			  }
		  } else {
			  /*  try{
			  factory = new Configuration().configure().buildSessionFactory();
		  }catch (Throwable ex) { 
			  System.err.println("Failed to create sessionFactory object." + ex);
			  throw new ExceptionInInitializerError(ex); 
		  }*/
			/*  Session session =  this.getBelongsTo().getFactory().openSession();
			  //   Session session = factory.openSession();
			  Transaction tx = null;
			  try{
				  tx = session.beginTransaction();
				  session.save(cs); 
				  tx.commit();
			  }catch (HibernateException e) {
				  if (tx!=null) tx.rollback();
				  e.printStackTrace(); 
			  }finally {
				  session.close(); 
			  }*/
			  super.saveObjectInfo(cs);
		  }
	  }

	//  public void insertRow(common.SecurityInst si) throws SQLException {
	  public void insertRow() throws SQLException {
		  PreparedStatement stmt = null;

		  try {
			  stmt = con.prepareStatement(
					  "INSERT INTO SECURITIES " + 
							  "(PORTFOLIO_ID, COMPANY_NAME, TICKER_SYMBOL, BETA_VALUE, SEC_ID) " +
					  "VALUES ( ?, ? , ? , ?, ? )");
			  stmt.setInt(1, 111 );
			  stmt.setString(2, "PAAS");
			  stmt.setString(3, "PAAS");
			  stmt.setDouble(4, 10.1d);
			  stmt.setInt(5, 1);
//			  stmt.setString(2, si.getinstrumentName());
//			  stmt.setString(3, si.getTickerSymbol());
//			  stmt.setDouble(4, si.getBetaValue());
			  stmt.executeUpdate();
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
	  }

	  public void viewTable(DefaultTableModel model) throws SQLException {
	//	System.out.println("TICKER_SYMBOL, TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME ");  
	    Statement stmt = null;
	    String query =
	      "select TICKER_SYMBOL, TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME " +
		  "from APP.HISTORICAL_PRICES " + 
	      "order by TICKER_SYMBOL, TRADEDATE"; 
		//	  "where TICKER_SYMBOL = 'DE'";
	    try {
	      int x = 0;
	      stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(query);
	  //    System.out.println("Historical Prices:");
	//      JTable theTable = this.getBelongsTo().getBelongsToGUI().getTable();
	 //     DefaultTableModel model = (DefaultTableModel) theTable.getModel();

	      while (rs.next()) {
	    	   String tickerSymbol = rs.getString("TICKER_SYMBOL");
		        Date date = rs.getDate("TRADEDATE");
		        double close = rs.getDouble("CLOSEPRICE");
		        double open = rs.getDouble("OPENPRICE");
		        double high = rs.getDouble("HIGHPRICE");
		        double low = rs.getDouble("LOWPRICE");
		        double adjClose = rs.getDouble("ADJCLOSE");
		        Integer volume = rs.getInt("VOLUME");
	         //System.out.printf("%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s\n",tickerSymbol, date.toString(), close, open, high, low, adjClose, volume.toString());;
		        SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						//textArea.append("And made it here!!!!!!!!!!!!!!!");
						Object rowData[] ={tickerSymbol, date.toString(), Double.toString(close), Double.toString(open), Double.toString(high), Double.toString(low), Double.toString(adjClose), volume.toString()};
	    				model.addRow(rowData);
					}
				});
		        
	      }

	    } catch (SQLException e) {
	      JDBCTutorialUtilities.printSQLException(e);
	    } finally {
	      if (stmt != null) { stmt.close(); }
	    }
	  }
	  public Object[] getColumnHeader() {
		  Object columnNames[] = {"Symbol","Date","Close","Open","High","Low","Adj Close","Volume"};
		  return columnNames;
	  }

	  public void viewTable(Connection con) throws SQLException {
		System.out.println("TICKER_SYMBOL, TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME ");  
	    Statement stmt = null;
	    String query =
	      "select TICKER_SYMBOL, TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME " +
		  "from APP.HISTORICAL_PRICES " + 
	      "order by TICKER_SYMBOL, TRADEDATE"; 
	//	  "where TICKER_SYMBOL = 'DE'";
	    try {
	      stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(query);
	//      JTable theTable = this.getBelongsTo().getBelongsToGUI().getTable();
	//      DefaultTableModel model = (DefaultTableModel) theTable.getModel();
	      while (rs.next()) {
	        String tickerSymbol = rs.getString("TICKER_SYMBOL");
	        Date date = rs.getDate("TRADEDATE");
	        double close = rs.getDouble("CLOSEPRICE");
	        double open = rs.getDouble("OPENPRICE");
	        double high = rs.getDouble("HIGHPRICE");
	        double low = rs.getDouble("LOWPRICE");
	        double adjClose = rs.getDouble("ADJCLOSE");
	        Integer volume = rs.getInt("VOLUME");
            //Object[][]data={{n,e}};
	            // This will add row from the DB as the last row in the JTable. 
  //          model.insertRow(theTable.getRowCount(), new Object[] {tickerSymbol, date.toString(), close, open, high, low, adjClose, volume.toString()});
	        System.out.printf("%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s\n",tickerSymbol, date.toString(), close, open, high, low, adjClose, volume.toString());;
	      }
	    } catch (SQLException e) {
	      JDBCTutorialUtilities.printSQLException(e);
	    } finally {
	      if (stmt != null) { stmt.close(); }
	    }
	  }
	  
	  public void exportData(String sFileName) {
		  try {
			  FileWriter writer = new FileWriter(sFileName);			    
			  PreparedStatement stmt = null;
			   String query =
					   "select TICKER_SYMBOL, TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME " +
								  "from APP.HISTORICAL_PRICES " + 
							      "order by TICKER_SYMBOL, TRADEDATE"; 
			  stmt = con.prepareStatement(query);
			  //	  stmt.setString(1, this.getCurrentProject());
			  ResultSet rs = stmt.executeQuery();
			  while (rs.next()) {
				  String tickerSymbol = rs.getString("TICKER_SYMBOL");
			        Date date = rs.getDate("TRADEDATE");
			        Double close = rs.getDouble("CLOSEPRICE");
			        Double open = rs.getDouble("OPENPRICE");
			        Double high = rs.getDouble("HIGHPRICE");
			        Double low = rs.getDouble("LOWPRICE");
			        Double adjClose = rs.getDouble("ADJCLOSE");
			        Integer volume = rs.getInt("VOLUME");
				  writer.append(tickerSymbol+",");
				  writer.append(date.toString()+",");
				  writer.append(close.toString()+",");
				  writer.append(open.toString()+",");
				  writer.append(high.toString()+",");
				  writer.append(low.toString()+",");
				  writer.append(adjClose.toString()+",");
				  writer.append(volume.toString()+",");
				  writer.append('\n');
			  }
			  writer.flush();
			  writer.close();
			  System.out.println("Successfully export Historical Prices table data");
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } catch (IOException e) {
			  e.printStackTrace();
		  } finally {
			  //  if (stmt != null) { stmt.close(); 
		  }
	  }

/*
	public static void main(String[] args) {

	    JDBCTutorialUtilities myJDBCTutorialUtilities;
	    Connection myConnection = null;

	    if (args[0] == null) {
	      System.err.println("Properties file not specified at command line");
	      return;
	    } else {
	      try {
	        myJDBCTutorialUtilities = new JDBCTutorialUtilities(args[0]);
	      } catch (Exception e) {
	        System.err.println("Problem reading properties file " + args[0]);
	        e.printStackTrace();
	        return;
	      }
	    }
	    try {
	      myConnection = myJDBCTutorialUtilities.getConnection();

	      // Java DB does not have an SQL create database command; it does require createDatabase
//	      JDBCTutorialUtilities.createDatabase(myConnection,
//	                                           myJDBCTutorialUtilities.dbName,
//	                                           myJDBCTutorialUtilities.dbms);
	//
//	      JDBCTutorialUtilities.initializeTables(myConnection,
//	                                             myJDBCTutorialUtilities.dbName,
//	                                             myJDBCTutorialUtilities.dbms);
	      
	      HistoricalPricesTable mySecurityInstTable =
	    	        new HistoricalPricesTable(myConnection, myJDBCTutorialUtilities.dbName,
	    	                         myJDBCTutorialUtilities.dbms, null);
	      
	      System.out.println("\nContents of HISTORICAL_PRICES table:");
	      
	//      System.out.println("\nInserting a new row:");
	//      mySecurityInstTable.insertRow();
	      mySecurityInstTable.viewTable(myConnection);
	      
	 //     HistoricalPricesTable.viewTable(myConnection);

	    } catch (SQLException e) {
	      JDBCTutorialUtilities.printSQLException(e);
	    } finally {
	      JDBCTutorialUtilities.closeConnection(myConnection);
	    }
	  }


	

	  */
}
