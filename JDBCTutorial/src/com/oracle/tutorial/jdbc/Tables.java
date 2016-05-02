package com.oracle.tutorial.jdbc;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import com.gui.*;
import com.oracle.tutorial.jdbc.Tables.Columns;
import com.tradecoach.patenter.entity.security.CandleStick;
import com.tradecoach.patenter.entity.security.CandleSticks;
import com.utilities.GlobalVars;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.workers.PortfoliosGroup;
import com.workers.SecurityInst;
import com.workers.Tools;
import com.workers.PortfoliosGroup;

public  class Tables {
	protected String dbName;
	protected Connection con;
	protected String dbms;
	private String tableName;
	private String ddlCreate, dmlPopulate;
	private TableParentModel tabelModel;
	private JTable tableGUI;
	private ArrayList<Columns> tableMap = new ArrayList<Columns>();
	private PortfoliosGroup belongsTo;
	private int rowCount;
	private ArrayList<String> ddlCreates, ddlCreateTriggers, ddlCreateFKs;
	
	public Tables(){};

	public Tables(Connection connArg, String dbNameArg, String dbmsArg, PortfoliosGroup belongsTo, String tableName) {
		super();
		this.con = connArg;
		this.dbName = dbNameArg;
		this.dbms = dbmsArg;
		this.setBelongsTo(belongsTo);
		this.tableName = tableName;		
	}
	public Tables(PortfoliosGroup belongsTo, String tableName) {
		super();
		this.setBelongsTo(belongsTo);
		this.tableName = tableName;		
	}

	public void initialize(String ddlCreate) {
	/*	try {
	//		this.createIfMissing(ddlCreate);
			//System.out.println(ddlCreate);
			this.runRowCount();
		} catch (SQLException e) {
			
			e.printStackTrace();
		}*/
	}	
	public void initialize() {
//		try {
		//	this.createIfMissing();
		//	System.out.println(this.getDdlCreates().toString());
	//		this.runRowCount();
	//	} catch (SQLException e) {			
	//		e.printStackTrace();
	//	}
	}	
	public void initSelectColumn(){
		TableColumn tc = this.getTableGUI().getColumnModel().getColumn(0);
	    tc.setCellEditor(this.getTableGUI().getDefaultEditor(Boolean.class));
	    tc.setCellRenderer(this.getTableGUI().getDefaultRenderer(Boolean.class));
	 //   tc.setHeaderRenderer(portfolios.getSecurityInstTable().getSelected());
	    tc.setHeaderRenderer((TableCellRenderer) (this.getTableMap().get(0).getColumnHeader()));
	}
	public void createIfMissing(String ddlCreate) throws SQLException{
		//	if(this.isMissing()){
		try {
			this.createTable(ddlCreate);
			this.populateTable();
		} catch (SQLException e) {
			if(this.dbms.equals("derby") && DerbyUtils.tableAlreadyExists(e)) { //check if the exception is because of pre-existing table.
				System.out.println("Table APP."+ this.getTableName() +" already exists.");
			} else  {  
				JDBCTutorialUtilities.printSQLException(e);
			}
		}
	}	
	public boolean existsTable(String name)  throws SQLException {
		return this.existsDB_Object("TABLES", name);
	}
	
	public boolean existsFK(String name)  throws SQLException {
		return this.existsDB_Object("FOREIGNKEYS", name);
	}
	
	private boolean existsDB_Object(String ojectType, String name)  throws SQLException {
		  int rowCount = 0;
		  PreparedStatement stmt = null;
		  String sql = String.format("select count(*) AS ROWCOUNT from SYS.SYS%s where TABLENAME = ?",ojectType);
		  try {
			  stmt = con.prepareStatement(sql);
			  stmt.setString(1, name);
			  ResultSet rs = stmt.executeQuery();
			  rs.next();
			  rowCount = rs.getInt("ROWCOUNT");			  
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
	//	  this.setRowCount(rowCount);
		return rowCount>0;
	  }
	
	public void createIfMissing() throws SQLException{
		//	if(this.isMissing()){
		try {
			if(!this.existsTable(this.getTableName()))
				this.createTable();
		//	this.populateTable();
		} catch (SQLException e) {
			if(this.dbms.equals("derby") && DerbyUtils.tableAlreadyExists(e)) { //check if the exception is because of pre-existing table.
				System.out.println("Table APP."+ this.getTableName() +" already exists.");
			} else  {  
				JDBCTutorialUtilities.printSQLException(e);
			}
		}
	}
	public void createViewIfMissing(String ddlCreate[]) {
		try {
			this.createView(ddlCreate[1]);
		} catch (SQLException e) {
			if(this.dbms.equals("derby") && DerbyUtils.tableAlreadyExists(e)) { //check if the exception is because of pre-existing table.
				System.out.println(String.format("View %s already exists.", ddlCreate[0]));
			} else  {  
				JDBCTutorialUtilities.printSQLException(e);
			}
		}
	}
	protected void saveObjectInfo(Object si) {
		Session session =  this.getBelongsTo().getFactory().openSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.saveOrUpdate(si); 
			tx.commit();
		}catch (HibernateException e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace(); 
		}finally {
			session.close(); 
		}
	}
	public boolean isMissing() throws SQLException {
		boolean isThere = true;
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			if (this.dbms.equals("mysql")) {
				System.out.println("Dropping table APP."+ this.getTableName() +" from MySQL");
				stmt.executeUpdate("DROP TABLE IF EXISTS " + this.getTableName() );
			} else if (this.dbms.equals("derby")) {
				stmt.execute("SELECT * FROM APP." + this.getTableName());
				isThere = false;
			}
		} catch (SQLException e) {
			if(this.dbms.equals("derby") && DerbyUtils.tableDoesNotExist(e)) { //check if the exception is because of pre-existing table.
				System.out.println("Table APP."+ this.getTableName() +" does not exist.");
			} else  {  
				JDBCTutorialUtilities.printSQLException(e);
			}
		} finally {
			if (stmt != null) { stmt.close(); }
			return isThere;
		}
	}
	
	public void deleteSelected() {
	//	boolean isThere = true;
		Statement stmt = null;
		try {
			int n = JOptionPane.showConfirmDialog(
				    this.getBelongsTo().getBelongsToGUI().getFrmTradecoach(),
				    "<html>Are you sure you want to delete the selected transaction rows?<br>"
				    + "This operation can not be undone later",
				    "Delete Transaction Data",
				    JOptionPane.YES_NO_OPTION);
			if(n==1) return;
			stmt = con.createStatement();
			if (this.dbms.equals("mysql")) {
		//		System.out.println("Dropping table APP."+ this.getTableName() +" from MySQL");
				stmt.executeUpdate(String.format("delete from %s where SELECTED" , this.getTableName()));
			} else if (this.dbms.equals("derby")) {
				stmt.executeUpdate(String.format("delete from %s where SELECTED" , this.getTableName()));
		//		isThere = false;
			}
		} catch (SQLException e) {
			if(this.dbms.equals("derby") && DerbyUtils.tableDoesNotExist(e)) { //check if the exception is because of pre-existing table.
				System.out.println("Table APP."+ this.getTableName() +" does not exist.");
			} else  {  
				JDBCTutorialUtilities.printSQLException(e);
			}
		} finally {
			if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
		//	return isThere;
		}
	}
	
	public void deleteAll() {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			System.out.println(String.format("delete from %s " , this.getTableName()));
			if (this.dbms.equals("mysql")) {
				//		System.out.println("Dropping table APP."+ this.getTableName() +" from MySQL");
				stmt.executeUpdate(String.format("delete from %s " , this.getTableName()));
			} else if (this.dbms.equals("derby")) {
				stmt.executeUpdate(String.format("delete from %s" , this.getTableName()));
			}
		}
		catch (SQLException e) {
			if(this.dbms.equals("derby") && DerbyUtils.tableDoesNotExist(e)) { //check if the exception is because of pre-existing table.
				System.out.println("Table APP."+ this.getTableName() +" does not exist.");
			} else  {  
				JDBCTutorialUtilities.printSQLException(e);
			}
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
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDdlCreate() {
		return ddlCreate;
	}

	public void setDdlCreate(String ddlCreate) {
		this.ddlCreate = ddlCreate;
	}
	public void setDdlCreate(ArrayList<String> ddlCreate) {
		this.ddlCreates = ddlCreate;
	}
	public void setDdlCreate(String[] ddlCreate) {
	//	for(String ddl:ddlCreate)
		//	this.getDdlCreates().add(ddl); 
		ddlCreates = new ArrayList<String>(Arrays.asList(ddlCreate));
	}	
	public void appendDdlCreate(String[] ddl) {
	//	for(String ddl:ddlCreate)
		//	this.getDdlCreates().add(ddl); 
		ArrayList<String> newDDL = new ArrayList<String>(Arrays.asList(ddl));
		this.getDdlCreates().addAll(newDDL);
	}
	public ArrayList<String> getDdlCreates() {
		return ddlCreates;
	}	
	public ArrayList<String> getDdlCreateTriggers() {
		return ddlCreateTriggers;
	}
	public void setDdlCreateTriggers(ArrayList<String> ddlCreateTriggers) {
		this.ddlCreateTriggers = ddlCreateTriggers;
	}
	public void setDdlCreateTriggers(String[] ddl) {
		ddlCreateTriggers = new ArrayList<String>(Arrays.asList(ddl));
	}	
	public ArrayList<String> getDdlCreateFKs() {
		return ddlCreateFKs;
	}
	public void setDdlCreateFKs(ArrayList<String> ddlCreateFKs) {
		this.ddlCreateFKs = ddlCreateFKs;
	}
	public void setDdlCreateFKs(String[] ddl) {
		ddlCreateFKs = new ArrayList<String>(Arrays.asList(ddl));
//		for(String d:ddlCreateFKs) {
//			String name = d.substring(d.indexOf("CONSTRAINT", d.indexOf(" ", d.indexOf("CONSTRAINT"+2))));
//			if(this)
//		}		
	}
	public void dropTable() throws SQLException {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			if (this.dbms.equals("mysql")) {
				System.out.println("Dropping table "+ this.getTableName() +" from MySQL");
				stmt.executeUpdate("DROP TABLE IF EXISTS "+ this.getTableName() );
			} else if (this.dbms.equals("derby")) {
				stmt.executeUpdate("DROP TABLE "+ this.getTableName() );
			}
		} catch (SQLException e) {
			if(this.dbms.equals("derby") && DerbyUtils.tableDoesNotExist(e)) { //check if the exception is because of pre-existing table.
				System.out.println("Table APP.+ this.getTableName() + does not exist.  No need to DROP");
			} else  {  
				JDBCTutorialUtilities.printSQLException(e);
			}
		} finally {
			if (stmt != null) { stmt.close(); }
		}
	}

	public void runDDL(String ddl)throws SQLException {
		Statement stmt = null;
			stmt = con.createStatement();
			stmt.executeUpdate(ddl);
	}

	public void runDDL_Batch(ArrayList<String> DDL) throws SQLException {
		if(DDL==null) DDL=this.getDdlCreates();
		if(DDL==null) return;
		Statement stmt = null;
		stmt = con.createStatement();
		for (String ddl : DDL) {
			stmt.addBatch(ddl);
			System.out.println(ddl);
		}
		stmt.executeBatch();
		stmt.close();
	}
	
	public void runDDL_Batch() throws SQLException {
		this.runDDL_Batch(null);
	}
	
	public void runDDLtriggers_Batch() throws SQLException {
		this.runDDL_Batch(this.getDdlCreateTriggers());
	}
	 
	public void runDDL_FKs_Batch() throws SQLException {
		this.runDDL_Batch(this.getDdlCreateFKs());
	}
	
	public ArrayList<Columns> getTableMap() {
		return tableMap;
	}

	public void setTableMap(ArrayList<Columns> tableMap) {
		this.tableMap = tableMap;
	}

	  public Object[] getColumnHeader() {
		  return this.getColumnFieldNames(true);
		//  Object columnNames[] = {"Select","Portfilio ID","Symbol","Name","Beta"};
		//  return columnNames;
	  }

	  public String getColumnFeildName(String columnHeader) {
		  for (Columns col : this.getTableMap()) {
			  if(col.getColumnHeader() instanceof CheckBoxHeader) 
				  continue;		
			  if (columnHeader.trim().compareTo(((String) col.getColumnHeader()).trim())==0)
				  return col.getColumnFeildName();			
		  }
		  return null;
	  }
	
	/*public String[] getFeildNames() {
		 // Object columnNames[] = {"Trade ID","Symbol","Name", "Order Date","Entry Price", "Stop Loss","Stop","Stp Trig","Stp Type","Stop Activation\n Date","Position"};
		  String[] feildNames = {"TICKERSYMBOL", "INSTRUMENTNAME", "ORDERDATE", "ENTRYPRICE", "STOPLOSS", "STOP", "STOPTRIGGER", "STOPTYPE", "STOPACTIVATIONDATE", "POSITION " };
		  
		  return feildNames;
	  }*/
	
	  public String getFieldNameAtPosition(int p) {
		 // String[] s = this.getFeildNames();
	//	return s[p];
		  return this.getTableMap().get(p).getColumnFeildName();
	  }
	  
	  public Class<?> getColumnTypeUsingFieldName(String fieldName) {
		if(fieldName.trim().toUpperCase().compareTo("SELECTED")==0)
			return Boolean.class;
		for (Columns col : this.getTableMap()) {			
			//if(col.getColumnHeader()==fieldName){
			if(col.getColumnFeildName().trim().compareTo(fieldName)==0){
				return col.getColumnType();
			}			
		}
		return String.class;
	}
	public int getColumnIndexFromHeader(String columnHeader) {
		int index = 0;
		for (Columns col : this.getTableMap()) {			
			if(col.getColumnHeader()==columnHeader){
				return index;
			}			
			if(col.isVisible()) index++;
		}
		return -1;
	}
	public int getColumnTableWidth(String columnHeader) {
		int i =this.getColumnIndexFromHeader(columnHeader);
		return this.getColumnTableWidthAtPosition(i);
	}
	public int getColumnTableWidthAtPosition(int p) {
		return this.getTableMap().get(p).getColumnWidth();
	}		
	public String[] getColumnToolTips() {
		List<String> list =new ArrayList<String>();
		for (Columns col : this.getTableMap()) {			
			list.add(col.getColumnToolTip());
		}
		return list.toArray(new String[list.size()]);
	}	
	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public String getDbms() {
		return dbms;
	}

	public void setDbms(String dbms) {
		this.dbms = dbms;
	}

	public void createTable(String ddlCreate) throws SQLException {
		this.runDDL(ddlCreate);
	}
	
	public void createTable() throws SQLException {
		this.runDDL_Batch();
	}
	
	public void createTable(ArrayList<String> ddlCreate) throws SQLException {
		this.runDDL_Batch(ddlCreate);
	}

	public void createView(String ddlCreate) throws SQLException {
		this.runDDL(ddlCreate);
	}
	public void populateTable() throws SQLException {
		  Statement stmt = null;
		  try {
			  stmt = con.createStatement();
			  stmt.executeUpdate(this.getDmlPopulate());
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close();  }
		  }
	}

	public PortfoliosGroup getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(PortfoliosGroup belongsTo) {
		this.belongsTo = belongsTo;
	}

	public TableParentModel getTabelModel() {
		return tabelModel;
	}

	public void setTabelModel(TableParentModel tabelModel) {
		this.tabelModel = tabelModel;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
	
	protected void setPreparedStatementElement(PreparedStatement stmt, Class<?> classType, Object value, int place) throws SQLException {
		  if (classType==String.class) {
			  stmt.setString(place, (String) value);
		  }
		  else if (classType==int.class) {
			  stmt.setInt(place, (int) value);
		  }
		  else if (classType==Integer.class) {
			  stmt.setInt(place, Integer.parseInt((String) value));
		  }
		  else if (classType==double.class) {
			  stmt.setDouble(place, (double) value);
		  }
		  else if (classType==Double.class) {
			  stmt.setDouble(place, Double.parseDouble((String) value));
		  }
		  else if (classType==float.class) {
			  stmt.setFloat(place, (float) value);
		  }
		  else if (classType==boolean.class) {
			  stmt.setBoolean(place, value.toString().compareTo("true")==0);
		  }
		  else if (classType==Boolean.class) {
			  stmt.setBoolean(place, value.toString().compareTo("true")==0);
		  }
		  else if (classType==Date.class) {
			  try {
				  String sd = value.toString();
				  java.util.Date d = (java.util.Date) GlobalVars.df.parse(sd);
				  java.sql.Date dd =  new java.sql.Date(d.getTime());
				  stmt.setDate(place, dd);
			  } catch (ParseException e) {
				  e.printStackTrace();
			  }
		  }
		  else  {
			  stmt.setString(place, (String) value);
		  }            	
	}

	public class Columns{
		private Object columnHeader;
		private String columnFeildName, columnToolTip;
		private Class<?> columnType;
		private int columnWidth;
		private boolean visible;
		public Columns() {};
		public Columns(Object columnHeader, boolean visible, int columnWidth, String columnFeildName, Class<?> columnType, String columnToolTip) {
			super();
			this.columnHeader = columnHeader;
			this.columnFeildName = columnFeildName;
			this.columnType = columnType;
			this.columnToolTip = columnToolTip;
			this.setColumnWidth(columnWidth);
			this.setVisible(visible);
		}
		public String getColumnFeildName() {return columnFeildName;	}
		public Object getColumnHeader() {return columnHeader;}
		public void setColumnFeildName(String columnFeildName) {this.columnFeildName = columnFeildName;}
		public Class<?> getColumnType() {return columnType;}
		public void setColumnType(Class<?> columnType) {this.columnType = columnType;}
		public String getColumnToolTip() {return columnToolTip;}
		public void setColumnToolTip(String columnToolTip) {this.columnToolTip = columnToolTip;}
		public void setColumnHeader(String columnHeader) {this.columnHeader = columnHeader;}
		public int getColumnWidth() {return columnWidth;		}
		public void setColumnWidth(int columnWidth) {this.columnWidth = columnWidth;}
		public boolean isVisible() {return visible;}
		public void setVisible(boolean visible) {this.visible = visible;}
	}

	public class MyItemListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e) {
			Object source = e.getSource();
			if (source instanceof AbstractButton == false) return;
		//	boolean checked = e.getStateChange() == ItemEvent.SELECTED;
			boolean checked = e.getStateChange() == ItemEvent.SELECTED;
			for(int x = 0, y = rowCount; x < y; x++)
			{
				getTableGUI().setValueAt(new Boolean(checked),x,0);
			}
		}
	}

	public Object[] getColumnFieldNames(boolean visableOnly) {
	//	Object[] columnNames = new Object[this.getTableMap().size()];
		ArrayList<Object> columnNamesAL = new ArrayList<Object>();
	//	int i=0;
		for (Columns col : this.getTableMap()) {
			if((visableOnly && col.isVisible())||!visableOnly) {
		//		columnNames[i]=col.getColumnHeader();
			//	i++;
				columnNamesAL.add(col.getColumnHeader());
			}
		}
	//	Object[] cc = {"Select","Portfilio ID","Symbol","Name","Beta"};//
		return columnNamesAL.toArray();	
	//	return cc;
	}

	public JTable getTableGUI() {
		return tableGUI;
	}

	public void setTableGUI(JTable tableGUI) {
		this.tableGUI = tableGUI;
	}
	
	public String getDmlPopulate() {
		return dmlPopulate;
	}
	public void setDmlPopulate(String dmlPopulate) {
		this.dmlPopulate = dmlPopulate;
	}

	public class  KeyPairs{
	private	ArrayList KeyPairs;
	private	String key;
	private	Object value;
	private	Class<?> classType;
	}
	   
/*		private void printDebugData() {
	        int numRows = getRowCount();
	        int numCols = getColumnCount();

	        for (int i=0; i < numRows; i++) {
	            System.out.print("    row " + i + ":");
	            for (int j=0; j < numCols; j++) {
	                System.out.print("  " + data[i][j]);
	            	
	            }
	            System.out.println();
	        }
	        System.out.println("--------------------------");
	    }*/
	  public void runRowCount() throws SQLException {
		  int rowCount = 0;
		  Statement stmt = null;
		  String sql = String.format("select count(*) AS ROWCOUNT from APP.%s", this.getTableName()) ;
		  try {
		//	  con.setAutoCommit(true);
			  stmt = con.createStatement();
			  ResultSet rs = stmt.executeQuery(sql);
			  rs.next();
			  rowCount = rs.getInt("ROWCOUNT");			  
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
		  this.setRowCount(rowCount);
	  }
	  
	  public boolean hasThisAttributeValue(Map.Entry<String, Object> pair) throws SQLException {
		  return this.hasThisAttributeValue(pair.getKey(), (String) pair.getValue());
	  }
	  
	  public boolean hasThisAttributeValue(String key, String value) throws SQLException {
		  int rowCount = 0;
		  PreparedStatement stmt = null;
		  String sql = String.format("select count(*) AS ROWCOUNT from APP.%s where %s = ?", this.getTableName(), key) ;
		  try {
			  stmt = con.prepareStatement(sql);
			  stmt.setString(1, value);
			  ResultSet rs = stmt.executeQuery();
			  rs.next();
			  rowCount = rs.getInt("ROWCOUNT");			  
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
		  this.setRowCount(rowCount);
		return rowCount>0;
	  }
	  public boolean hasThisAttributeValue(String[] key, String[] value) throws SQLException {
		  int rowCount = 0;
		  PreparedStatement stmt = null;
		  String sql = String.format("select count(*) AS ROWCOUNT from APP.%s where %s = ? and %s = ?", this.getTableName(), key[0], key[1]) ;
		  try {
			  stmt = con.prepareStatement(sql);
			  stmt.setString(1, value[0]);
			  stmt.setString(2, value[1]);
			  ResultSet rs = stmt.executeQuery();
			  rs.next();
			  rowCount = rs.getInt("ROWCOUNT");			  
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
		  this.setRowCount(rowCount);
		return rowCount>0;
	  }
	    
	  public void updateTableCell(int tradeID, String columnName, String value) throws SQLException {
		  PreparedStatement stmt = null;
		  String sql = String.format("update APP.TRADE_HISTORY SET %s = ? where TRADE_ID = ? ", columnName) ;
		  try {
			  con.setAutoCommit(true);
			  stmt = con.prepareStatement(sql);
		//	  stmt = con.prepareStatement("update APP.TRADE_HISTORY SET STOPTYPE = ? where TRADE_ID = ? ");
			  stmt.setString(1, value);
			  stmt.setInt(2, tradeID);
			  stmt.executeUpdate();
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
	  }
	  
	  public void updateTableUsingTradeID(int tradeID, String fieldName, Object value) throws SQLException {
		  PreparedStatement stmt = null;
		  String sql = String.format("update APP.%s SET %s = ? where TRADE_ID = ? ", this.getTableName(),fieldName) ;
		  try {
			  con.setAutoCommit(true);
			  stmt = con.prepareStatement(sql);
			  Class<?> classType=this.getColumnTypeUsingFieldName(fieldName);
			  if (classType==String.class) {
				  stmt.setString(1, (String) value);
			  }
			  else if (classType==int.class) {
				  stmt.setInt(1, (int) value);
			  }
			  else if (classType==double.class) {
				  stmt.setDouble(1, (double) value);
			  }
			  else if (classType==float.class) {
				  stmt.setFloat(1, (float) value);
			  }
			  else if (classType==boolean.class) {
				  stmt.setBoolean(1, (boolean) value);
			  }
			  else if (classType==Date.class) {
				  stmt.setDate(1, (Date) value);
			  }
			  else  {
				  stmt.setString(1, (String) value);
			  }            
			  stmt.setInt(2, tradeID);
			  stmt.executeUpdate();
		  } catch (SQLException e) {
			  JDBCTutorialUtilities.printSQLException(e);
		  } finally {
			  if (stmt != null) { stmt.close(); }
		  }
	  }
/*
	public void updateSelected(int portfolioID, String tickerSymbol, boolean selected)  {
		PreparedStatement stmt = null;
		String sql = "update APP.SECURITIES SET SELECTED = ? where PORTFOLIO_ID = ? and TICKER_SYMBOL = ?";
		try {
			con.setAutoCommit(true);
			stmt = con.prepareStatement(sql);
			stmt.setBoolean(1, selected);
			stmt.setInt(2, portfolioID);
			stmt.setString(3, tickerSymbol);
			stmt.executeUpdate();
		} catch (SQLException e) {
			JDBCTutorialUtilities.printSQLException(e);
		} finally {
			if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			} }
		}
	}
	
	public void updateSelected(String portfolioName, String tickerSymbol, boolean selected)  {
		PreparedStatement stmt = null;
		String sql = "update APP.SECURITIES SET SELECTED = ? where PORTFOLIO_NAME = ? and TICKER_SYMBOL = ?";
		try {
			con.setAutoCommit(true);
			stmt = con.prepareStatement(sql);
			stmt.setBoolean(1, selected);
			stmt.setString(2, portfolioName);
			stmt.setString(3, tickerSymbol);
			stmt.executeUpdate();
		} catch (SQLException e) {
			JDBCTutorialUtilities.printSQLException(e);
		} finally {
			if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			} }
		}
	}*/

}