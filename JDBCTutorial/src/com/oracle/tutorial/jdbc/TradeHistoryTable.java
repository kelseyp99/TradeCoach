package com.oracle.tutorial.jdbc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.gui.CheckBoxHeader;
import com.gui.GUI;
import com.tradecoach.patenter.entity.security.Portfolio;
import com.utilities.GlobalVars.typeOrder;
import com.workers.DataLoader;
import com.workers.MoneyMgmtStrategy;
import com.workers.PortfoliosGroup;
import com.workers.TransactionData;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session; 
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

	public class TradeHistoryTable extends Tables {	  
	  private static SessionFactory factory; 
	  public TradeHistoryTable(){};
	  public TradeHistoryTable(Connection connArg, String dbNameArg, String dbmsArg, PortfoliosGroup belongsTo) {
		    super(connArg, dbNameArg, dbmsArg, belongsTo, "TRADE_HISTORY");
		    this.setDdlCreate(new String[] {
		    		"CREATE TABLE APP.TRADE_HISTORY (" +		    
				  		  "TRADE_ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
						  "TICKER_SYMBOL VARCHAR(20) NOT NULL, " +
						  "PORTFOLIO_NAME varchar(20) , " +
						  "INSTRUMENTNAME VARCHAR(100) , " +
						  "ORDERDATE DATE NOT NULL, " +
						  "ENTRYPRICE DOUBLE NOT NULL, " +
						  "STOPLOSS DOUBLE , " +
						  "STOP DOUBLE , " +
						  "STOPTRIGGER DOUBLE , " +
						  "STOPTYPE VARCHAR(20) , " +
						  "STOPACTIVATIONDATE DATE,  " +
						  "POSITION INT, " +
						  "SELECTED BOOLEAN, " +
						  "PRIMARY KEY (TRADE_ID)  " +
						  ")"});
		    
		    try {
	    		String[] ddlFKs = new String[] {			  
	    				"ALTER TABLE TRADE_HISTORY ADD CONSTRAINT TRADEHISTORY_PORTFOLIOS_FK FOREIGN KEY (PORTFOLIO_NAME) REFERENCES PORTFOLIOS (PORTFOLIO_NAME) ",
	    				"ALTER TABLE TRADE_HISTORY ADD CONSTRAINT TRADEHISTORY_SECURITIES_FK FOREIGN KEY (TICKER_SYMBOL) REFERENCES SECURITIES (TICKER_SYMBOL)"};

		    	if(this.existsTable(this.getTableName())){

		    		String[] names = new String[] {			  
		    				"TRADEHISTORY_PORTFOLIOS_FK",
		    				"TRADEHISTORY_SECURITIES_FK"};

		    		ResultSet rs = con.getMetaData().getImportedKeys(null,null, "TRADE_HISTORY");
		    		ArrayList<String> rl = new ArrayList<String>();// = (List)rs;
		    		ArrayList<String> r2 = new ArrayList<String>();
		    		while (rs.next()) { 
		    			/*System.out.println( "FK_NAME = " + rs.getString("FK_NAME") + ", FKTABLE_NAME = " +
		    				rs.getString("FKTABLE_NAME") + ", FKCOLUMN_NAME = " +
		    				rs.getString("FKCOLUMN_NAME")); */
		    			rl.add(rs.getString("FK_NAME"));		    		
		    		} 
		    		for(int i=0;i<names.length-1;i++){
		    			if(!rl.contains(names[i])) r2.add(ddlFKs[i]);
		    		}
		    		this.setDdlCreateFKs(r2);
		    	}
		    	else this.setDdlCreateFKs(ddlFKs);
		    } catch (SQLException e) {
		    	e.printStackTrace();
		    }
/*			this.setDdlCreateFKs(new String[] {			  
		    		 "ALTER TABLE TRADE_HISTORY ADD CONSTRAINT TRADEHISTORY_PORTFOLIOS_FK FOREIGN KEY (PORTFOLIO_NAME) REFERENCES PORTFOLIOS (PORTFOLIO_NAME) ",
		    		 "ALTER TABLE TRADE_HISTORY ADD CONSTRAINT TRADEHISTORY_SECURITIES_FK FOREIGN KEY (TICKER_SYMBOL) REFERENCES SECURITIES (TICKER_SYMBOL)"});
*/	
		    
		    this.setDdlCreateTriggers(new String[] {
						  "create trigger INSERT_SECURITY " +
								  "after INSERT on APP.TRADE_HISTORY   " +
								  "referencing NEW as NEW   " +
								  "  FOR EACH ROW MODE DB2SQL    " +
								  "  insert into SECURITIES (TICKER_SYMBOL) values (new.TICKER_SYMBOL)  " , 

						  "create trigger APP.INSERT_PROJECT  " +
								  "after INSERT on TRADE_HISTORY  " +
								  "referencing NEW as NEW  " +
								  "  FOR EACH ROW MODE DB2SQL  " +
								  "  insert into PORTFOLIOS (PORTFOLIO_NAME) values (new.PORTFOLIO_NAME)  "   ,
				  
						  "create trigger APP.INSERT_PROJECT_HOLDINGS " +
								  "after INSERT on TRADE_HISTORY " +
								  "referencing NEW as NEW " +
								  "  FOR EACH ROW MODE DB2SQL " +
								  "  insert into PORTFOLIO_HOLDINGS(PORTFOLIO_NAME, TICKER_SYMBOL) values (new.PORTFOLIO_NAME, new.TICKER_SYMBOL)"
		    });

		    this.initialize();
	  }
	  
		public TradeHistoryTable(PortfoliosGroup belongsTo) {
			super( belongsTo, "TRADE_HISTORY");
			this.initialize();
	}
		public void initialize(){
			try {//it does not appear to be necessayr to save the class type.  Consider removing it
				super.initialize();
			    CheckBoxHeader selected = new CheckBoxHeader(new MyItemListener());
			    this.getTableMap().add(new Columns(selected,true,25,"SELECTED",Boolean.class,"click checkbox to select or deselect"));
			    this.getTableMap().add(new Columns("Trade ID",true,25,"TRADE_ID",Integer.class,"Internal Control Number set when added"));
			    this.getTableMap().add(new Columns("Portfolio",false,25,"PORTFOLIO_NAME",String.class,"Ticker Symbol of Security"));
			    this.getTableMap().add(new Columns("Symbol",true,25,"TICKER_SYMBOL",String.class,"Ticker Symbol of Security"));
			    this.getTableMap().add(new Columns("Name",true,25,"INSTRUMENTNAME",String.class,"Name of Security"));
			    this.getTableMap().add(new Columns("Order Date",true,25," ORDERDATE",Date.class,"Date this order was placed with broker for execution"));
			    this.getTableMap().add(new Columns("Entry Price",true,25," ENTRYPRICE",Double.class,"Price at which order was placed. This is not necessarily the actual price purchased at"));
			    this.getTableMap().add(new Columns("Stop Loss",true,25," STOPLOSS",Double.class,"The stop exit price you placed so as to limit furhter loss"));
			    this.getTableMap().add(new Columns("Stop",true,25," STOP",Double.class,"The stop price (planned exit) at which the intended profit objective is achieved"));
			//    this.getTableMap().add(new Columns("Stp Trig",true,25," STOPTRIGGER",double.class,"The price at which when passed through the stop price order is activated"));
			    this.getTableMap().add(new Columns("Stp Type",true,25," STOPTYPE",String.class,"The type of order describing the stop price (planned exit)"));
			//    this.getTableMap().add(new Columns("Stop Activation Date",true,25," STOPACTIVATIONDATE",String.class,"Stop Price Activiation Date"));
			    this.getTableMap().add(new Columns("Position",true,25," POSITION ",Integer.class,"Number of Shares ordered"));	    
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void populateTable() throws SQLException {
		  Statement stmt = null;
		  String sql =
				  "insert into APP.TRADE_HISTORY " +
						  "(PORTFOLIO_NAME,TICKER_SYMBOL,INSTRUMENTNAME,ORDERDATE,ENTRYPRICE,STOPLOSS,STOP,STOPTRIGGER,STOPTYPE,STOPACTIVATIONDATE,POSITION) " +
						  "values (%s)";
		  String[] aData = new String[4];
		  aData[0]="'DEFAULT PORTFOLIO','DE','Deere & Company','2014-10-15',89.95,93.24,84.41,84.41,'SimpleExit',NULL,-52";
		  aData[1]="'DEFAULT PORTFOLIO','PAAS','Pan American Silver Corp. (USA)','2015-02-12',9.36,8.424,10.54,10.54,'SimpleExit',NULL,524";
		  aData[2]="'DEFAULT PORTFOLIO','IAG','IAMGOLD Corp (USA)','2014-10-15',2.38,2.142,2.34,2.34,'SimpleExit',NULL,403";
		  aData[3]="'DEFAULT PORTFOLIO','SPY','SPDR S&P 500 ETF Trust','2014-10-15',203.56,209.79,197.5,201.98,'Market',NULL,-10";
		  try {
			  for(int i=0;i<aData.length;i++){
				  String sql2 = String.format(sql, aData[i]);

				  stmt = con.createStatement();
				  System.out.println(sql2);
				  
				  stmt.executeUpdate(sql2);
			  }
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); 
			  }
		  }
	  }

	  public  void loadTradeHistoryData(List<MoneyMgmtStrategy> mms) throws SQLException {
		if(this.getBelongsTo().getConfig().getConfig("dbSerializer").getBoolean("use_hibernate_orm"))
			loadTradeHistoryDataViaHibernate(mms);
		else
			loadTradeHistoryDataViaJDBC(mms);
	  }

	  public  void loadTradeHistoryDataViaJDBC(List<MoneyMgmtStrategy> mms) {	  
		  PreparedStatement stmt = null;
		  String query =
				  "select TRADE_ID, TICKER_SYMBOL, INSTRUMENTNAME, ORDERDATE, ENTRYPRICE, STOPLOSS, STOP, STOPTRIGGER, STOPTYPE, STOPACTIVATIONDATE, POSITION " +
						  "from APP.TRADE_HISTORY " +  
						  "where PORTFOLIO_NAME = ? " +
						  "order by ORDERDATE, TRADE_ID";
		  try {
			  stmt = con.prepareStatement(query);
			  stmt.setString(1, this.getCurrentProject());
			  ResultSet rs = stmt.executeQuery();
			  while (rs.next()) {
				  TransactionData ts = new TransactionData();
				  ts.setTradeID(rs.getInt("TRADE_ID"));		    	
				  ts.setTickerSymbol(rs.getString("TICKER_SYMBOL"));
				  ts.setInstrumentName(rs.getString("INSTRUMENTNAME"));
				  ts.setOrderDate(new java.sql.Date(rs.getDate("ORDERDATE").getTime()));
				  ts.setEntryPrice(rs.getDouble("ENTRYPRICE"));
				  ts.setStopLoss(rs.getDouble("STOPLOSS"));
				  ts.setStop(rs.getDouble("STOP"));
				  ts.setStopTrigger(rs.getDouble("STOPTRIGGER"));
				  ts.setStopType(com.workers.DataLoader.parseTypeOrder(rs.getString("STOPTYPE")));
				  if(rs.getDate("STOPACTIVATIONDATE")!=null)
					  ts.setStopActivationDate(new java.sql.Date(rs.getDate("STOPACTIVATIONDATE").getTime()));
				  ts.setPosition(rs.getInt("POSITION"));
				  mms.add(new MoneyMgmtStrategy(ts));
			  }
		  } catch (SQLException e) {     
			  JDBCTutorialUtilities.printSQLException(e);
		  } catch (Exception f){
			  f.printStackTrace();
		  } finally {
			  if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
		  }
	  }

	  public  void loadTradeHistoryDataViaHibernate(List<MoneyMgmtStrategy> mms) {
		  Session session = factory.openSession();
		  try {
			  String hql = "FROM TransactionData t " +
					  	   "WHERE t.portfolio_id = :portfolioID" +
					       "order by ORDERDATE, TRADE_ID";
			  Query query = session.createQuery(hql);
			  query.setParameter("portfolioID",this.getCurrentPortfolio().getId());
			  List<TransactionData> transactions = query.list();
			  transactions.forEach(t->mms.add(new MoneyMgmtStrategy((t))));
		  } catch (Exception e) {
			  e.printStackTrace();
		  } 	
	  }

	   public Integer addTrade(TransactionData ts){
	      Session session = factory.openSession();
	      Transaction tx = null;
	      Integer tradeID = null;
	      try{
	         tx = session.beginTransaction();
	         tradeID = (Integer) session.save(ts); 
	         tx.commit();
	      }catch (HibernateException e) {
	         if (tx!=null) tx.rollback();
	         e.printStackTrace(); 
	      }finally {
	         session.close(); 
	      }
	      return tradeID;
	   }
	  public void saveMyTrade(TransactionData ts) {
		  PreparedStatement stmt = null;		  
		  try {
			  stmt = con.prepareStatement(
					  "INSERT INTO APP.TRADE_HISTORY " + 
							  "(TICKER_SYMBOL, INSTRUMENTNAME, ORDERDATE, ENTRYPRICE, STOPLOSS, STOP, STOPTRIGGER, STOPTYPE, STOPACTIVATIONDATE, POSITION, PORTFOLIO_NAME) " +
					  "VALUES ( ?, ? , ? , ?, ?, ? , ? , ? , ? , ? , ? )");
		//	  stmt.setInt(1, 111 );
			  stmt.setString(1, ts.getTickerSymbol());
			  stmt.setString(2, ts.getInstrumentName());
			  stmt.setDate(3, ts.getOrderDate());
			  stmt.setDouble(4, ts.getEntryPrice());
			  stmt.setDouble(5, ts.getStopLoss());
			  stmt.setDouble(6, ts.getStop());
			  stmt.setDouble(7, ts.getStopTrigger());
			  stmt.setString(8, ts.getStopType().toString());
			  stmt.setDate(9, ts.getStopActivationDate());
			  stmt.setInt(10, ts.getPosition());
			  stmt.setString(11, this.getCurrentProject());
			  stmt.executeUpdate();
		  } catch (java.sql.SQLIntegrityConstraintViolationException s) {
				//  JDBCTutorialUtilities..printSQLException(e);			  
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  //if (stmt != null) { stmt.close(); }
		  }
	  }

	  public void viewTable(DefaultTableModel model2) throws SQLException {
	//	System.out.println("TICKERSYMBOL, TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME ");  
		  model2.setRowCount(0);
		  PreparedStatement stmt = null;
		  String query =
				  "select SELECTED, TRADE_ID, TICKER_SYMBOL, INSTRUMENTNAME, ORDERDATE, ENTRYPRICE, STOPLOSS, STOP, STOPTRIGGER, STOPTYPE, STOPACTIVATIONDATE, POSITION " +
						  "from APP.TRADE_HISTORY " +  
					      "where PORTFOLIO_NAME = ? " +
						  "order by ORDERDATE, TICKER_SYMBOL, TRADE_ID";
		  try {
			  int x = 0;
			  stmt = con.prepareStatement(query);
			  stmt.setString(1, this.getCurrentProject());
			  ResultSet rs = stmt.executeQuery();
			  while (rs.next()) {
				  boolean selected = rs.getBoolean("SELECTED");
				  Integer tradeID = rs.getInt("TRADE_ID");
				  String tickerSymbol = rs.getString("TICKER_SYMBOL");
				  String instrumentName = rs.getString("INSTRUMENTNAME");
				//  if(instrumentName==null) instrumentName=" ";
				  Date orderDate = rs.getDate("ORDERDATE");
				  double entry = rs.getDouble("ENTRYPRICE");
				  double stopLoss = rs.getDouble("STOPLOSS");
				  double stop = rs.getDouble("STOP");
				  double stopTrigger = rs.getDouble("STOPTRIGGER");
				  String stopType = rs.getString("STOPTYPE");
				  Date stopActvDate = rs.getDate("STOPACTIVATIONDATE");
				  String stopActvDateS = stopActvDate==null?"":stopActvDate.toString();
				  Integer pos = rs.getInt("POSITION");
			      this.getBelongsTo().getInitialPortfolio().setSelectedSecurityUsingSymbol(orderDate,tickerSymbol, selected);
				  //System.out.printf("%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s\n",tickerSymbol, date.toString(), close, open, high, low, adjClose, volume.toString());;
				  SwingUtilities.invokeLater(new Runnable() {					
					  @Override
					  public void run() {
						  Object rowData[] ={
								  selected,
								  tradeID,
								  tickerSymbol, 
								  instrumentName, 
								  orderDate.toString(), 
								  Double.toString(entry), 
								  Double.toString(stopLoss), 
								  Double.toString(stop), 
								//  Double.toString(stopTrigger), 
								  stopType, 
								//  stopActvDateS, 
								  pos.toString()};
						  model2.addRow(rowData);
					  }
				  });
			  }
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
	  }
	  /**Opens the file found at <i>filename</i> and loads it line-by-line into memory.  Each line is
	   * added to the <b>TRADE_HISTORY</b> table.  There is referential integrity <i>ri</i> between the <b>TRADE_HISTORY</b>, 
	   * <b>PORTFOLIO_HOLDINGS</b>, <b>SECURITIES</b> and <b>PORTFOLIOS</b> that must be observed.  The current
	   * <i>triggers</i> in Derby are not working. RI is implimented in at the class level.  */
	  public void importData(String filename) throws FileNotFoundException {
		  try {	    		
			  con.setAutoCommit(false);
			  BufferedReader br = new BufferedReader(new FileReader(filename));
			  String line, fields = "", values="?, ";
			  String pn = "Portfolio_Name";
			  String ts = "Ticker_Symbol";
			  String sql2 = "insert into SECURITIES (TICKER_SYMBOL) values (?)";
			  String sql3 = "insert into PORTFOLIOS (PORTFOLIO_NAME) values (?)";
			  String sql4 = "insert into PORTFOLIO_HOLDINGS (TICKER_SYMBOL,PORTFOLIO_NAME) values (?,?)";
			  Boolean started = false;
			  System.out.println("Loading security information from CSV file");
			  PreparedStatement stmt = null,stmt2 = null,stmt3 = null,stmt4 = null;
			  ArrayList<String> alFields = new ArrayList<String>();
			  Map<String,Object> fieldMap = new LinkedHashMap<String,Object>();
			  Queue<String> fieldQue=new LinkedList<String>();
			  int i=0, n=0;
		//	  this.deleteAll();
			  Iterator<String> it = null;
			  while ((line = br.readLine()) != null) {
				  String[] items = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
				  if(!started){
					  started = true;
					  for(String item:items){
						  String s=item.toUpperCase().trim().compareTo("SELECTED")==0?"SELECTED":this.getColumnFeildName(item).trim();
						  alFields.add(s);
						  fieldQue.add(s);
						  fieldMap.put(s, null);
						  if(s.compareTo("TRADE_ID")!=0){
							  fields+=String.format("%s, ",s);						  
						  } else {
							  n++;
						  }
					  }
					//  it=fieldQue.iterator();		
					  //  int n = values.contains("TRADE_ID")?1:0;
					  values=new String(new char[items.length-n]).replace("\0", values);
					  values=values.substring(0, values.length()-2);
					  String sql = String.format("insert into %s (%s) values (%s)",this.getTableName(), fields.substring(0, fields.length()-2), values);
					  stmt = con.prepareStatement(sql);
					  stmt2 = con.prepareStatement(sql2);
					  stmt3 = con.prepareStatement(sql3);
					  stmt4 = con.prepareStatement(sql4);
							  
				  } else {	 
					  it=fieldQue.iterator();
					  for(String item:items)
						  fieldMap.put((String) it.next(), item);

					  Iterator itm=fieldMap.entrySet().iterator();
					  i=0;
					  while(itm.hasNext()){
						  Map.Entry<String, Object> pair=(Map.Entry<String, Object>)itm.next();
						  if(pair.getKey().trim().compareTo("TRADE_ID")!=0){
							  Class<?> classType=this.getColumnTypeUsingFieldName(pair.getKey());						  
							  this.setPreparedStatementElement(stmt, classType, pair.getValue(), ++i);	
					//	boolean inPortfolioTable=
						  }
					  }
					  stmt.addBatch();
					  if(!this.getBelongsTo().getSecurityInstTable().hasThisAttributeValue(ts, (String) fieldMap.get(ts.toUpperCase()))){
						  stmt2.setString(1, (String) fieldMap.get(ts.toUpperCase()));
						  stmt2.addBatch();
					  }
					  if(!this.getBelongsTo().getPortfoliosTable().hasThisAttributeValue(pn, (String) fieldMap.get(pn.toUpperCase()))){
						  stmt3.setString(1, (String) fieldMap.get(pn.toUpperCase()));
						  stmt3.addBatch();
					  }
					  if(!this.getBelongsTo().getPortfolioHoldingsTable().hasThisAttributeValue(new String[]{pn,ts}, new String[]{(String) fieldMap.get(pn.toUpperCase()),(String) fieldMap.get(ts.toUpperCase())})){
						  stmt4.setString(1, (String) fieldMap.get(ts.toUpperCase()));
						  stmt4.setString(2, (String) fieldMap.get(pn.toUpperCase()));
						  stmt4.addBatch();
					  }
				//	  stmt.executeUpdate();
					  //	  br.close();
				  }
				  stmt2.executeBatch();
				  stmt3.executeBatch();
				  stmt4.executeBatch();
				  stmt.executeBatch();
				  con.commit();
			  }
		  } catch (SQLException e) {
			  try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			  e.printStackTrace();
		  } catch (IOException e) {
			  e.printStackTrace();
		  }  finally {
		  }
	  }  

	  public void exportData(String sFileName) {
		  try {
			  FileWriter writer = new FileWriter(sFileName);
			  String theFields = null;
			  String tradeIDcol = "Trade ID,";
			  PreparedStatement stmt = null;
			  String query =
					  "select SELECTED, TRADE_ID, PORTFOLIO_NAME, TICKER_SYMBOL, INSTRUMENTNAME, ORDERDATE, ENTRYPRICE, STOPLOSS, STOP, STOPTRIGGER, STOPTYPE, STOPACTIVATIONDATE, POSITION " +
							  "from APP.TRADE_HISTORY " +  
							  //     "where PORTFOLIO_NAME = ? " +
							  "order by  TICKER_SYMBOL, ORDERDATE, TRADE_ID";
			  stmt = con.prepareStatement(query);
			  Object[] fields = this.getColumnFieldNames(false);
			  for(Object field : fields ){
				  theFields += field.toString()+",";
			  }
			 // theFields= "PORTFOLIO_NAME, "+theFields;
			  writer.append(String.format("Selected,%s \n",theFields.substring(theFields.indexOf(tradeIDcol), theFields.length()-1)));
		//	  writer.append('\n');
			  //	  stmt.setString(1, this.getCurrentProject());
			  ResultSet rs = stmt.executeQuery();
			  while (rs.next()) {
				  String portfolioName = rs.getString("PORTFOLIO_NAME");
				  boolean selected = rs.getBoolean("SELECTED");
				  Integer tradeID = rs.getInt("TRADE_ID");
				  String tickerSymbol = rs.getString("TICKER_SYMBOL");
				  String instrumentName = rs.getString("INSTRUMENTNAME");
				  //  if(instrumentName==null) instrumentName=" ";
				  Date orderDate = rs.getDate("ORDERDATE");
				  double entry = rs.getDouble("ENTRYPRICE");
				  double stopLoss = rs.getDouble("STOPLOSS");
				  double stop = rs.getDouble("STOP");
				  double stopTrigger = rs.getDouble("STOPTRIGGER");
				  String stopType = rs.getString("STOPTYPE");
				  Date stopActvDate = rs.getDate("STOPACTIVATIONDATE");
				  String stopActvDateS = stopActvDate==null?"":stopActvDate.toString();
				  Integer pos = rs.getInt("POSITION");
				  this.getBelongsTo().getInitialPortfolio().setSelectedSecurityUsingSymbol(orderDate,tickerSymbol, selected);
				  writer.append(Boolean.toString(selected)+",");
				  writer.append(tradeID.toString()+",");
				  writer.append(portfolioName.toString()+",");
				  writer.append(tickerSymbol.toString()+",");
				  writer.append(instrumentName.toString()+",");
				  writer.append(orderDate.toString()+",");
				  writer.append(Double.toString(entry)+",");
				  writer.append(Double.toString(stopLoss)+",");
				  writer.append(Double.toString(stop)+",");
				  //  Double.toString(stopTrigger), 
				  writer.append(stopType.toString()+","); 
				  //  stopActvDateS, 								  
				  writer.append( pos.toString());
				  writer.append('\n');
			  }
			  writer.flush();
			  writer.close();
			  System.out.println("Successfully exported Trade History table data");
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } catch (IOException e) {
			  e.printStackTrace();
		  } finally {
			  //  if (stmt != null) { stmt.close(); 
		  }
	  }

		private GUI getBelongsToGUI() {
			return this.getBelongsTo().getBelongsToGUI();
		}
		
		private String getCurrentProject() {
			return this.getBelongsToGUI().getCurrentProject();
		}		
		private Portfolio getCurrentPortfolio() {
			return this.getBelongsToGUI().getCurrentPortfolioGroup().getPortfolioEntity();
		}
		public static void main(String[] args)  {
		      try{
		         factory = new Configuration().configure().buildSessionFactory();
		      }catch (Throwable ex) { 
		         System.err.println("Failed to create sessionFactory object." + ex);
		         throw new ExceptionInInitializerError(ex); 
		      }
		      TradeHistoryTable ME = new TradeHistoryTable();
		      
		      DateTime d = new DateTime();
		      Date date = (Date) d.toDate();
		      
		      TransactionData ts = new TransactionData("GOOG", "Google", typeOrder.Market, date ,date, 99d, 99d, 99d, 99d, 100);
		 //     String tickerSymbol, String instrumentName, typeOrder stopType, Date orderDate,
	//			Date stopActivationDate, Double entryPrice, Double stopLoss, Double stop, Double stopTrigger, Integer position

		      /* Add few employee records in database */
		      Integer empID1 = ME.addTrade(ts);
		   }
	
	}
