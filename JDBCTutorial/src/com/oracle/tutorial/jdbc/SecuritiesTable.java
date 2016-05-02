package com.oracle.tutorial.jdbc;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.gui.CheckBoxHeader;
import com.gui.GUI;
import com.tradecoach.patenter.entity.security.CandleSticks;
import com.workers.MoneyMgmtStrategy;
import com.workers.PortfoliosGroup;
import com.workers.SecurityInst;

	public class SecuritiesTable extends Tables {

	  public SecuritiesTable(Connection connArg, String dbNameArg, String dbmsArg, PortfoliosGroup belongsTo) {
		    super(connArg, dbNameArg, dbmsArg, belongsTo, "SECURITIES");
		    this.setDdlCreate("CREATE TABLE APP.SECURITIES (" +
								    	//	"PORTFOLIO_ID INTEGER , "+
								    		"TICKER_SYMBOL VARCHAR(20) NOT NULL, " +
								    		"COMPANY_NAME VARCHAR(40) , " +
								    		"BETA_VALUE DECIMAL(7, 4), " +
											"SELECTED BOOLEAN, " +
								    		"Primary Key (TICKER_SYMBOL) " +
		    				  				") ");
		    this.initialize();
	}	  
    public SecuritiesTable(PortfoliosGroup belongsTo) {
	  super(belongsTo, "SECURITIES");
	  this.initialize();
	 }
		public void initialize(){
			super.initialize(this.getDdlCreate());
			try {
				CheckBoxHeader selected = new CheckBoxHeader(new MyItemListener());
				this.getTableMap().add(new Columns(selected,true,25,"SELECTED",Boolean.class,"click checkbox to select or deselect"));
			//	this.getTableMap().add(new Columns("select",true,25,"SELECTED",Boolean.class,"click checkbox to select or deselect"));
			//	this.getTableMap().add(new Columns("Portfolio ID",false,25,"PORTFOLIO_NAME",Integer.class,"The project this is a member of"));
				this.getTableMap().add(new Columns("Symbol",true,55,"INSTRUMENTNAME",String.class,"Name of Security"));
				this.getTableMap().add(new Columns("Name",true,255,"COMPANY_NAME",String.class,"Comapany name associated with this security"));
				this.getTableMap().add(new Columns("Beta",true,55,"BETA_VALUE",Double.class,"Beta value"));				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
/*	  public void populateTable() throws SQLException {
		  Statement stmt = null;
		  try {
			  stmt = con.createStatement();
			  stmt.executeUpdate("insert into SECURITY_INIST " +
					  "values(49, 'PAAS', 'PAAS', " +
					  "1.0')");
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); 
			  }
		  }
	  }*/
/*	  public  void loadAllSecuritiesInfo() throws SQLException {		  
		    Statement stmt = null;
		    String query =
		      "select TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from SECURITIES where SELECTED = true";
		    try {
		      stmt = con.createStatement();
		      ResultSet rs = stmt.executeQuery(query);
		      while (rs.next()) {
		    	SecurityInst si = new SecurityInst();
		    	si.setCs(new CandleSticks(si));	
		    	si.setMms(new MoneyMgmtStrategy());
		    	si.setBelongsTo(this.getBelongsTo().getInitialPortfolio());		    	
		      //  int portfolioID = rs.getInt("PORTFOLIO_ID");
		     //   si.setPortfolioID(portfolioID);
		        String TICKER_SYMBOL = rs.getString("TICKER_SYMBOL");
		        si.setTickerSymbol(TICKER_SYMBOL);
		        String instrumentName = rs.getString("COMPANY_NAME");
		        si.setinstrumentName(instrumentName);		        
		        double betaValue = rs.getDouble("BETA_VALUE");
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
	  
	  public  void loadSecurityInfo(SecurityInst si) throws SQLException {		  
		    Statement stmt = null;
		    String query = String.format(
		      "select TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from APP.SECURITIES where TICKER_SYMBOL = '%s'",si.getTickerSymbol());
		    try {
		      stmt = con.createStatement();
		      ResultSet rs = stmt.executeQuery(query);
		      while (rs.next()) {
		        String tickerSymbol = rs.getString("TICKER_SYMBOL");
		        si.setTickerSymbol(tickerSymbol);
		        String instrumentName = rs.getString("COMPANY_NAME");
		        si.setinstrumentName(instrumentName);		        
		        double betaValue = rs.getDouble("BETA_VALUE");
		        si.setBetaValue(betaValue);
		        this.getHistoricalPriceTable().loadSecurityPriceData(si);		        
		      }
		    } catch (SQLException e) {
		      JDBCTutorialUtilities.printSQLException(e);
		    } finally {
		      if (stmt != null) { stmt.close(); }
		    }
		  }
	  
	  private HistoricalPricesTable getHistoricalPriceTable() {
		  return this.getBelongsTo().getHistoricalPricesTable();		  
	}

	public void saveAllSecuritiesInfo(){		  
			Iterator<SecurityInst> i = this.getBelongsTo().getInitialPortfolio().getHoldingSet().iterator();
		
	        while (i.hasNext()) {
	        	SecurityInst si = i.next();
	        	try {
					if(this.inserOrUpdateSecurityInfo(si)) {
						this.saveSecurityInfo(si);  
					} else {
						this.updateSecurityInfo(si);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
	        }  //while  		  
	  
	
	}
/**return <tt>true</tt> if the <b>SecurityInst</b> instance '<i>si</i>' is not found in the 
 * SECURITIES table */
	private boolean inserOrUpdateSecurityInfo(SecurityInst si) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(
					"select count(*) as OCCURS from APP.SECURITIES " + 
					"where TICKER_SYMBOL = ? ");
			stmt.setString(1, si.getTickerSymbol());
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
	
	private void saveSecurityInfo(SecurityInst si) {
		if(this.getBelongsTo().getUseNativeJDBC()){
			PreparedStatement stmt = null;
			try {
				con.setAutoCommit(false);
				stmt = con.prepareStatement(
						"INSERT INTO SECURITIES" + 
								"( TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE) " +
						"VALUES (  ?, ?, ? )");
				//	  stmt.setInt(1, 1);
				stmt.setString(1, si.getTickerSymbol());
				stmt.setString(2, si.getinstrumentName());
				stmt.setDouble(3, si.getBetaValue());
				stmt.executeUpdate();

				this.getPortfolioHoldingsTable().savePortfolioInfo(si);
				con.commit();
				con.setAutoCommit(true);

			} catch (java.sql.SQLIntegrityConstraintViolationException s) {
				//  JDBCTutorialUtilities..printSQLException(e);		  
			} catch (SQLException e) {
				JDBCTutorialUtilities.printSQLException(e);
			} finally {
				//if (stmt != null) { stmt.close(); }
			}
		} else {
			super.saveObjectInfo(si);
		} 
	}

	  public void updateSelected(String tickerSymbol, Boolean selected)  {
		  this.getPortfolioHoldingsTable().updateSelected(tickerSymbol, selected);
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
/*	  public void insertRow() throws SQLException {
		  PreparedStatement stmt = null;

		  try {
			  stmt = con.prepareStatement(
					  "INSERT INTO SECURITIES " + 
							  "(COMPANY_NAME, TICKER_SYMBOL, BETA_VALUE, SEC_ID) " +
					  "VALUES (  ? , ? , ?, ? )");
			//  stmt.setInt(1, 111 );
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
	  }*/
/*	  public void viewSecurities() throws SQLException {

	    Statement stmt = null;
	    String query = "select SUP_NAME, SUP_ID from SECURITIES where SELECTED = true";
	    try {
	      stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(query);

	      System.out.println("Suppliers and their ID Numbers:");

	      while (rs.next()) {
	        String s = rs.getString("SUP_NAME");
	        int n = rs.getInt("SUP_ID");
	        System.out.println(s + "   " + n);
	      }

	    } catch (SQLException e) {
	      JDBCTutorialUtilities.printSQLException(e);
	    } finally {
	      if (stmt != null) { stmt.close(); }
	    }
	  }*/
	  
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
		  if(this.getBelongsTo().getUseNativeJDBC()){
			  System.out.println("Populating the Securities tab with data from the Securities datsbase");  
			  PreparedStatement stmt = null;
			  int rowCount=0;
			  String query =
					  "select SELECTED, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE " +
							  "from V_SECURITIES " +
							  "where PORTFOLIO_NAME = ?";
			  try {
				  //      int x = 0;
				  stmt = con.prepareStatement(query);
				  stmt.setString(1, this.getBelongsToGUI().getCurrentProject());
				  ResultSet rs = stmt.executeQuery();
				  while (rs.next()) {
					  rowCount++;
					  boolean selected = rs.getBoolean("SELECTED");
					  //    int portfolioID = rs.getInt("PORTFOLIO_ID");
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
							  Object rowData[] ={selected, ticker, company, beta.toString()};
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
		  } else {
			  Session session = this.getBelongsTo().getFactory().openSession();
			  try{
				  Query qry= session
						  .createQuery("s.tickerSymbol,ph.portfolioName,ph.selected,s.betaValue,s.instrumentName " +
								  		"from SecurityInst s Join PortfolioHoldings ph");
				  List l = qry.list();
				  Iterator it=l.iterator();			 
				  while(it.hasNext())	{
					  Object rows[] = (Object[])it.next();
					  System.out.println(rows[0]+ " -- " +rows[1]);
				  }
			  }catch (HibernateException e) {
				  e.printStackTrace(); 
			  }finally {
				  session.close(); 
			  }
		  }
	  }
	  
	  private GUI getBelongsToGUI() {
		return this.getBelongsTo().getBelongsToGUI();
	}

	public  void viewTable() throws SQLException {		  
	    Statement stmt = null;
	    String query =
	    //		 "select PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from SECURITIES";
	      "select  TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE from SECURITIES where SELECTED = true";
	    try {
	      stmt = con.createStatement();
	      ResultSet rs = stmt.executeQuery(query);
	      while (rs.next()) {
	     //   int portfolioID = rs.getInt("PORTFOLIO_ID");
	        String ticker = rs.getString("TICKER_SYMBOL");
	        String company = rs.getString("COMPANY_NAME");
	        double beta = rs.getDouble("BETA_VALUE");
	        System.out.println(ticker + "(" + company + ") Beta Value: " + beta );
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
					  "select SELECTED, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE " +
					  	       "from V_SECURITIES " +
					  	      "where PORTFOLIO_NAME = ?";
			  stmt = con.prepareStatement(query);
			  ResultSet rs = stmt.executeQuery();
			  while (rs.next()) {
				  boolean selected = rs.getBoolean("SELECTED");
			 //       int portfolioID = rs.getInt("PORTFOLIO_ID");
			        String ticker = rs.getString("TICKER_SYMBOL");
			        String company = rs.getString("COMPANY_NAME");
			        Double beta = rs.getDouble("BETA_VALUE");
				  writer.append(Boolean.toString(selected)+",");
				//  writer.append(Integer.toString(portfolioID)+",");
				  writer.append(ticker.toString()+",");
				  writer.append(company.toString()+",");
				  writer.append(beta.toString()+",");
				  writer.append('\n');
			  }
			  writer.flush();
			  writer.close();
			  System.out.println("Successfully export Securities table data");
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } catch (IOException e) {
			  e.printStackTrace();
		  } finally {
			  //  if (stmt != null) { stmt.close(); 
		  }
	  }
	  private PortfolioHoldingsTable getPortfolioHoldingsTable() {
		  return this.getBelongsTo().getPortfolioHoldingsTable();		
	  }
}
