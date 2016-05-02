package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.gui.CheckBoxHeader;
import com.gui.GUI;
import com.tradecoach.patenter.entity.security.CandleSticks;
import com.workers.MoneyMgmtStrategy;
import com.workers.Portfolio;
import com.workers.PortfoliosGroup;
import com.workers.SecurityInst;

	public class PortfolioHoldingsTable  extends Tables {
		
	  public PortfolioHoldingsTable(Connection connArg, String dbNameArg, String dbmsArg, PortfoliosGroup belongsTo) {
		    super(connArg, dbNameArg, dbmsArg, belongsTo, "PORTFOLIO_HOLDINGS");
//		    this.setDdlCreate("CREATE TABLE APP.PORTFOLIO_HOLDINGS (	 "+
//		    		"PORTFOLIO_NAME VARCHAR(20) NOT NULL, " +
//		    		"TICKER_SYMBOL VARCHAR(10) NOT NULL, "+
//		    		"OLDEST_ORDER_DATE DATE, "+
//		    		"Primary Key (TICKER_SYMBOL, PORTFOLIO_NAME) "+
//		    	") ");
		    this.setDdlCreate(new String[] {
		    		"CREATE TABLE APP.PORTFOLIO_HOLDINGS ( " +
		    			  "TICKER_SYMBOL VARCHAR(20) NOT NULL, " +
		    			  "PORTFOLIO_NAME VARCHAR(20) NOT NULL, " +
						  "SELECTED BOOLEAN, " +
		    			  "OLDEST_ORDER_DATE DATE  " +
		    			  ")",		    		
		    	//	"create table APP.PORTFOLIO_HOLDINGS as "
		    	//	+ "select distinct th.TICKERSYMBOL as TICKER_SYMBOL, 'DEFAULT_PORTFOLIO' as PORTFOLIO_NAME, CAST('12/31/2012' as date) as OLDEST_ORDER_DATE  " 
		    	//	+ "from APP.TRADE_HISTORY th WITH NO DATA  ", 
		    		 "ALTER TABLE PORTFOLIO_HOLDINGS ADD CONSTRAINT PORTFOLIOS_PK PRIMARY KEY (PORTFOLIO_NAME, TICKER_SYMBOL)  ",
		    		 "ALTER TABLE PORTFOLIO_HOLDINGS ADD CONSTRAINT PORTFOLIO_HOLDINGS_PORTFOLIOS_FK FOREIGN KEY (PORTFOLIO_NAME) REFERENCES PORTFOLIOS (PORTFOLIO_NAME) ",
		    		 "ALTER TABLE PORTFOLIO_HOLDINGS ADD CONSTRAINT PORTFOLIO_HOLDINGS_SECURITIES_FK FOREIGN KEY (TICKER_SYMBOL) REFERENCES SECURITIES (TICKER_SYMBOL)"});
		    this.setDmlPopulate("insert into APP.PORTFOLIO_HOLDINGS " +
					            "(select distinct th.TICKER_SYMBOL, 'DEFAULT PORTFOLIO' as PORTFOLIO_NAME, CAST('12/31/2012' as date) as OLDEST_ORDER_DATE " +
					            "   from TRADE_HISTORY th)");
		    //		    create table APP.PORTFOLIO_HOLDINGS as
//		    select distinct th.TICKERSYMBOL, 'DEFAULT_PORTFOLIO' as PORTFOLIO_NAME, CAST('12/31/2012' as date) as OLDEST_ORDER_DATE
//		      from APP.TRADE_HISTORY th WITH NO DATA ;
//		    insert into APP.PORTFOLIO_HOLDINGS (select distinct th.TICKER_SYMBOL, 'DEFAULT_PORTFOLIO' as PORTFOLIO_NAME, CAST('12/31/2012' as date) as OLDEST_ORDER_DATE  from TRADE_HISTORY th)
		    this.initialize();
	}
	  
	public PortfolioHoldingsTable(PortfoliosGroup belongsTo) {
	  super(belongsTo, "PORTFOLIO_HOLDINGS");
	  this.initialize();
	}

		public void initialize(){
		//	super.initialize(this.getDdlCreate());
			super.initialize();
		}

/*	  public void populateTable() throws SQLException {
		  Statement stmt = null;
		  try {
			  stmt = con.createStatement();
			  stmt.executeUpdate(this.getDmlPopulate());
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close();  }
		  }
	  }*/
  
/*	  public  void loadAllSecuritiesInfo() throws SQLException {		  
		    Statement stmt = null;
		    String query =
		      "select PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from SECURITIES where SELECTED = true";
		    try {
		      stmt = con.createStatement();
		      ResultSet rs = stmt.executeQuery(query);
		      while (rs.next()) {
		    	SecurityInst si = new SecurityInst();
		    	si.setCs(new CandleSticks(si));	
		    	si.setMms(new MoneyMgmtStrategy());
		    	si.setBelongsTo(this.getBelongsTo().getInitialPortfolio());		    	
		        int portfolioID = rs.getInt("PORTFOLIO_ID");
		        si.setPortfolioID(portfolioID);
		        String tickerSymbol = rs.getString("TICKER_SYMBOL");
		        si.setTickerSymbol(tickerSymbol);
		        String instrumentName = rs.getString("COMPANY_NAME");
		        si.setinstrumentName(instrumentName);		        
		        double betaValue = rs.getInt("BETA_VALUE");
		        si.setBetaValue(betaValue);
		        this.getHistoricalPriceTable().loadSecurityPriceData(si);
		        this.getBelongsTo().getInitialPortfolio().getHoldingSet().add(si);
		      }
		    } catch (SQLException e) {
		      JDBCTutorialUtilities.printSQLException(e);
		    } finally {
		      if (stmt != null) { stmt.close(); }
		    }
		  }*/
	  
	  /*public  void loadSecurityInfo(SecurityInst si) throws SQLException {		  
		    Statement stmt = null;
		    String query = String.format(
		      "select PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from APP.SECURITIES where TICKER_SYMBOL = '%s'",si.getTickerSymbol());
		    try {
		      stmt = con.createStatement();
		      ResultSet rs = stmt.executeQuery(query);
		      while (rs.next()) {
		    //	SecurityInst si = new SecurityInst();
		    //	si.setBelongsTo(this.getBelongsTo().getInitialPortfolio());		    	
		        int portfolioID = rs.getInt("PORTFOLIO_ID");
		        si.setPortfolioID(portfolioID);
		        String tickerSymbol = rs.getString("TICKER_SYMBOL");
		        si.setTickerSymbol(tickerSymbol);
		        String instrumentName = rs.getString("COMPANY_NAME");
		        si.setinstrumentName(instrumentName);		        
		        double betaValue = rs.getInt("BETA_VALUE");
		        si.setBetaValue(betaValue);
		        this.getHistoricalPriceTable().loadSecurityPriceData(si);
		    //    this.getBelongsTo().getInitialPortfolio().getHoldingSet().add(si);
		        
		      }
		    } catch (SQLException e) {
		      JDBCTutorialUtilities.printSQLException(e);
		    } finally {
		      if (stmt != null) { stmt.close(); }
		    }
		  }*/
	  
	  private HistoricalPricesTable getHistoricalPriceTable() {
		// TODO Auto-generated method stub
		  return this.getBelongsTo().getHistoricalPricesTable();		  
	}

/*	public void saveAllSecuritiesInfo(){		  
		  Iterator<SecurityInst> i = this.getBelongsTo().getInitialPortfolio().getHoldingSet().iterator();
	        while (i.hasNext()) {
	        	SecurityInst si = i.next();
	        	try {
					if(this.inserOrUpdateSecurityInfo(si)) {
				//		this.saveSecurityInfo(si);  
					} else {
						this.updateSecurityInfo(si);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
	        }  //while  		  
	  }*/

	public void updateSelected(String tickerSymbol, boolean selected)  {
		PreparedStatement stmt = null;
		String sql = "update APP.PORTFOLIO_HOLDINGS SET SELECTED = ? where PORTFOLIO_NAME = ? and TICKER_SYMBOL = ?";
		try {
			con.setAutoCommit(true);
			stmt = con.prepareStatement(sql);
			stmt.setBoolean(1, selected);
			stmt.setString(2, this.getBelongsToGUI().getCurrentProject());
			stmt.setString(3, tickerSymbol);
			stmt.executeUpdate();
		} catch (SQLException e) {
			JDBCTutorialUtilities.printSQLException(e);
		} finally {
			if (stmt != null) { 
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} 
			}
		}
	}
	
	private boolean inserOrUpdateSecurityInfo(SecurityInst si) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(
					"select count(*) as OCCURS from APP.PORTFOLIO_HOLDINGS " + 
					"where TICKER_SYMBOL = ? AND PORTFOLIO_NAME = ? ");
			stmt.setString(1, si.getTickerSymbol());
			stmt.setString(2, this.getBelongsToGUI().getCurrentProject());
			ResultSet rs = stmt.executeQuery();
			rs.next();
			return  rs.getInt("OCCURS")==0;
		} catch (SQLException e) {
			JDBCTutorialUtilities.printSQLException(e);
		} finally {
			if (stmt != null) { stmt.close(); }
		}
		return false;
	}
	
	  public void savePortfolioInfo(Portfolio p) {
		  try {
			con.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		  this.savePortfolioInfo(p, null);
/*			  PreparedStatement stmt = null;
			  try {
				  stmt = con.prepareStatement(
						  "INSERT INTO PORTFOLIOS" + 
								  "(PORTFOLIO_NAME) " +
						  "VALUES ( ? )");
				  con.setAutoCommit(true);
				  stmt.setString(1, p.getName());
				  stmt.executeUpdate();
			  } catch (java.sql.SQLIntegrityConstraintViolationException s) {
			  } catch (SQLException e) {
				  JDBCTutorialUtilities.printSQLException(e);
			  } finally {
				  //if (stmt != null) { stmt.close(); }
			  }*/
	  }	
	  public void savePortfolioInfo(SecurityInst si) {
		  Portfolio p = new Portfolio();
		  p.setPortfolioName(this.getBelongsToGUI().getCurrentProject());
		  this.savePortfolioInfo(p, si);
/*			  PreparedStatement stmt = null;
			  try {
				  stmt = con.prepareStatement(
						  "INSERT INTO PORTFOLIOS" + 
								  "(PORTFOLIO_NAME) " +
						  "VALUES ( ? )");
				  con.setAutoCommit(true);
				  stmt.setString(1, p.getName());
				  stmt.executeUpdate();
			  } catch (java.sql.SQLIntegrityConstraintViolationException s) {
			  } catch (SQLException e) {
				  JDBCTutorialUtilities.printSQLException(e);
			  } finally {
				  //if (stmt != null) { stmt.close(); }
			  }*/
	  }
	  
	  public void savePortfolioInfo(Portfolio p, SecurityInst si) {
		  PreparedStatement stmt = null;
		  try {
			  stmt = con.prepareStatement(
					  "INSERT INTO PORTFOLIOS" + 
							  "(PORTFOLIO_NAME) " +
							  "(TICKER_SYMBOL) " +
					  "VALUES ( ?, ? )");
			//  con.setAutoCommit(false);
			  stmt.setString(1, p==null?"":p.getPortfolioName());
			  stmt.setString(2, si==null?"":si.getinstrumentName());
			  stmt.executeUpdate();
		  } catch (java.sql.SQLIntegrityConstraintViolationException s) {
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  //if (stmt != null) { stmt.close(); }
		  }
	  }
	  
	  public void deletePortfolioInfo(Portfolio p, JFrame frame) {
		  PreparedStatement stmt = null;
		  if(JOptionPane.showConfirmDialog(
				  frame,
				  String.format("Are you sure you want to delete the project named:  \n\n     %s?\n\nWarning!  this process can not be undone",p.getPortfolioName()),
				  "An Inane Question",
				  JOptionPane.YES_NO_OPTION)==0) {
			  try {
				  stmt = con.prepareStatement(
						  "delete from PORTFOLIOS where PORTFOLIO_NAME = ?");
				  con.setAutoCommit(true);
				  stmt.setString(1, p.getPortfolioName());
				  stmt.executeUpdate();


			  } catch (java.sql.SQLIntegrityConstraintViolationException s) {
			  } catch (SQLException e) {
				  JDBCTutorialUtilities.printSQLException(e);
			  } finally {
				  //if (stmt != null) { stmt.close(); }
			  }
		  }
	  }
		
	  private void updateSecurityInfo(SecurityInst si) throws SQLException {
		  PreparedStatement stmt = null;
		  try {
			  stmt = con.prepareStatement(
					  "update APP.SECURITIES " + 
					  "set COMPANY_NAME = ?, BETA_VALUE = ? " +
					  "where TICKER_SYMBOL = ? ");
			  stmt.setString(3, si.getTickerSymbol());
			  stmt.setString(1, si.getinstrumentName());
			  stmt.setDouble(2, si.getBetaValue());
			  stmt.executeUpdate();
		  } catch (java.sql.SQLIntegrityConstraintViolationException s) {
			  JDBCTutorialUtilities.printSQLException(s);	  
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
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
			  stmt.executeUpdate();
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
	  }
	  /*Returns a list of Project names for use in the combox box**/
	  public Object[] getProjectNameList() throws SQLException {
	    Statement stmt = null;
	    String query = "select PORTFOLIO_NAME from PORTFOLIOS";
	    ArrayList<String> names = new ArrayList<String>();
	    int i = 0;
	    try {
	      stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(query);
	      while (rs.next()) {
	        names.add(rs.getString("PORTFOLIO_NAME"));
	      }
	    } catch (SQLException e) {
	      JDBCTutorialUtilities.printSQLException(e);
	    } finally {
	      if (stmt != null) { stmt.close(); }
	    }
		return (Object[]) names.toArray();
	  }
	  
	  public boolean projectNameAlreadyExists(String name){
		  try {
			return this.hasThisAttributeValue("PORTFOLIO_NAME", name) ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;		  
	  }
	  
	  public String getSecuritiesName(String ticker) throws SQLException {
		  PreparedStatement stmt = null;
		  try {
			  stmt = con.prepareStatement("select COMPANY_NAME from SECURITIES where upper(TICKER_SYMBOL) = ?");
			  stmt.setString(1, ticker.trim().toUpperCase());
			//  stmt.execute();
			  ResultSet rs = stmt.executeQuery();
			  return rs.next()?rs.getString("COMPANY_NAME"):"Not Found";
		  } catch (SQLException e) {	    	
			  JDBCTutorialUtilities.printSQLException(e);
			  if (stmt != null) { stmt.close(); }
		  }
		  return null;
	  }
  
	  public void viewTable(DefaultTableModel model) throws SQLException {
		System.out.println("Populating the Securities tab with data from the Securities datsbase");  
		PreparedStatement stmt = null;
	    int rowCount=0;
	    String query =
	  	      "select SELECTED, PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE " +
	  	       "from V_SECURITIES " +
	  	      "where PORTFOLIO_NAME = ?";
	    try {
	      int x = 0;
	      stmt = con.prepareStatement(query);
		  stmt.setString(1, this.getBelongsToGUI().getCurrentProject());
	      ResultSet rs = stmt.executeQuery();
	      while (rs.next()) {
	            rowCount++;
	    	    boolean selected = rs.getBoolean("SELECTED");
		        int portfolioID = rs.getInt("PORTFOLIO_ID");
		        String ticker = rs.getString("TICKER_SYMBOL");
		        String company = rs.getString("COMPANY_NAME");
		        Double beta = rs.getDouble("BETA_VALUE");
		        this.getBelongsTo().getInitialPortfolio().setSelectedSecurityUsingSymbol(ticker, selected);
		    //    System.out.println(ticker + "(" + company + ") Beta Value: " + beta );
	         //System.out.printf("%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s\n",tickerSymbol, date.toString(), close, open, high, low, adjClose, volume.toString());;
		        SwingUtilities.invokeLater(new Runnable() {					
					@Override
					public void run() {
						//textArea.append("And made it here!!!!!!!!!!!!!!!");
						Object rowData[] ={selected, portfolioID, ticker, company, beta.toString()};
	    				model.addRow(rowData);
					}
				});		        
	      }
	    } catch (SQLException e) {	    	
	      JDBCTutorialUtilities.printSQLException(e);
	    } finally {
	    	this.setRowCount(rowCount);	
	      if (stmt != null) { stmt.close(); }
	    }
	  }
	  
	  private GUI getBelongsToGUI() {
		return this.getBelongsTo().getBelongsToGUI();
	}

	public  void viewTable() throws SQLException {
		  
	    Statement stmt = null;
	    String query =
	    //		 "select PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from SECURITIES";
	      "select PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from SECURITIES where SELECTED = true";
	    try {
	      stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(query);
	      while (rs.next()) {
	        int portfolioID = rs.getInt("PORTFOLIO_ID");
	        String ticker = rs.getString("TICKER_SYMBOL");
	        String company = rs.getString("COMPANY_NAME");
	        int beta = rs.getInt("BETA_VALUE");
	        System.out.println(ticker + "(" + company + ") Beta Value: " + beta );
	      }
	    } catch (SQLException e) {
	      JDBCTutorialUtilities.printSQLException(e);
	    } finally {
	      if (stmt != null) { stmt.close(); }
	    }
	  }

}
