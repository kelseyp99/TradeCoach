package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.hibernate.SessionFactory;

import com.gui.CheckBoxHeader;
import com.gui.GUI;
import com.sun.glass.ui.MenuItem;
import com.workers.CandleSticks;
import com.workers.MoneyMgmtStrategy;
import com.workers.Parameter;
import com.workers.Portfolio;
import com.workers.Portfolios;
import com.workers.SecurityInst;

	public class ParametersTable  extends Tables {
	//	private boolean RefreshOnlineDafault;
	//	private Map parameters(String, Parameter;
		private HashMap<String, Parameter> parameters = new HashMap<String, Parameter>();
		private static SessionFactory factory; 
		
	  public ParametersTable(Connection connArg, String dbNameArg, String dbmsArg, Portfolios belongsTo) {
		    super(connArg, dbNameArg, dbmsArg, belongsTo, "PARAMETERS");
		    this.setDdlCreate("CREATE TABLE APP.PARAMETERS (	 " +
		    		"PORTFOLIO_NAME varchar(20) , " +
		    		"PARAM_NAME VARCHAR(30) NOT NULL, " +
		    		"STRVAL VARCHAR(30), "+
		    		"INTVAL INTEGER, "+
		    		"BOOLVAL BOOLEAN, "+
		    		"Primary Key (PARAM_NAME) "+
		    	") ");
		    this.initialize();
	}	 
		public void initialize(){
			super.initialize(this.getDdlCreate());			
		}
		public void addMRU(String fileName) throws SQLException{
			if(this.getMRU(0)==fileName) return;
			int MRUCount=5;
			ArrayList<String> items = new ArrayList<String>();
			String b4MRUfn;
			for(int i=MRUCount;i>0;i--){
				b4MRUfn=this.getMRU(i-1);
				if(b4MRUfn!="") {
					this.setMRU(i, b4MRUfn);
					items.add(b4MRUfn);
				}
			}
			this.setMRU(0, fileName);
			this.getBelongsToGUI().getMnRecentProjects().removeAll();
		//	for(int j=items.size();j>0;j--){
			ListIterator li = items.listIterator(items.size());
			// Iterate in reverse.
			while(li.hasPrevious()) {
				String s = li.previous().toString();
				JMenuItem mi = new JMenuItem(s);
				mi.addMenuKeyListener(new MenuKeyListener() {
					public void menuKeyPressed(MenuKeyEvent arg0) {
						SwingUtilities.invokeLater(new Runnable() {    						
							@Override
							public void run() {	
								if ((mi.getText() != null) && (mi.getText().length() > 0)) {
									getBelongsToGUI().setCurrentProject(mi.getText());
									getBelongsToGUI().getStatusLabel().setText(mi.getText());
									return;
								}
							}
						});
					}
					public void menuKeyReleased(MenuKeyEvent arg0) {
					}
					public void menuKeyTyped(MenuKeyEvent arg0) {
					}
				});
				this.getBelongsToGUI().getMnRecentProjects().add(new JMenuItem(fileName));			
			}
		}
		public void setMRU(int index, String fileName) throws SQLException{
			this.updateParameterGlobalValue( String.format("MRU%s",index),fileName);
		}
		public String getMRU(int index) {
			return this.getStringParameter(String.format("MRU%s",index));
		}
		public void setRefreshOnlineDafault(boolean value) throws SQLException{
			this.updateParameterValue( "RefreshOnlineDafault",value);
		}
		public boolean isRefreshOnlineDafault() {
			return this.getBoolParameter("RefreshOnlineDafault");
		}
		public void setMaxTrailingStopLayers(int value) throws SQLException {
		this.updateParameterValue("MaxTrailingStopLayers", value);
		}
		public int getMaxTrailingStopLayers() {
			return this.getIntegerParameter("MaxTrailingStopLayers");
		}
		/**Takes the parameter value passed and determines if the key/value pair is already present for 
		 * this portfolio.  If so, the parameter is updated, else it is inserted into the 
		 * <b>PARAMETER</b> table
		 * @throws SQLException */
		private void updateParameterValue(String name, Object value) throws SQLException  {
			this.updateParameterValue(name, value, this.getCurrentProject());
		}
		private void updateParameterGlobalValue(String name, Object value) throws SQLException  {
			this.updateParameterValue(name, value, "Global");
		}
		private void updateParameterValue(String name, Object value, String project) throws SQLException {
			String sqlUpdate= "update PARAMETERS set %s=? where PORTFOLIO_NAME=? and PARAM_NAME=?";
			String sqlInsert= "insert into PARAMETERS (%s, PORTFOLIO_NAME,PARAM_NAME) values (?,?,?)";
			String[] keys = {"PORTFOLIO_NAME","PARAM_NAME"};
			String[] values = {this.getCurrentProject(),name};
			String sql = this.hasThisAttributeValue(keys, values)?sqlUpdate:sqlInsert;
			String key = null;
			Parameter p = new Parameter(project, name);			

			if(name=="MaxTrailingStopLayers")
				key="INTVAL";
			else if(name=="RefreshOnlineDafault")
				key="BOOLVAL";

			if(key=="STRVAL") {
				p.setStrVal((String)value);
				value = (String)value;				
				this.getParameters().put(name, new Parameter(project, name, 0, (String) value, (Boolean) null));
			}
			else if(key=="INTVAL") {
				p.setIntVal((int)value);
				value = (int)value;				
				this.getParameters().put(name, new Parameter(project, name, (int) value, "", (Boolean) null));
			}
			else if(key=="BOOLVAL") {
				p.setBoolVal((boolean)value);
				value = (boolean)value;				
				this.getParameters().put(name, new Parameter(project, name, 0, "", (boolean) value));
			}			
			else
				value = (String)value;
			 
			if(this.getBelongsTo().getUseNativeJDBC()){
				sql=String.format(sql, key);
				PreparedStatement stmt = null;
				try {
					stmt = con.prepareStatement(sql);
					stmt.setString(2, project);
					stmt.setString(3, name);
					this.setPreparedStatementElement(stmt, value.getClass(), value, 1);
					stmt.executeUpdate();
				} catch (java.sql.SQLIntegrityConstraintViolationException s) {
					JDBCTutorialUtilities.printSQLException(s);	  
				} catch (SQLException e) {
					JDBCTutorialUtilities.printSQLException(e);
				} finally {
					if (stmt != null) { stmt.close(); }
				}
			} else {
				saveObjectInfo(p);
			}
		} 
		public  void loadParameters() throws SQLException {		  
			PreparedStatement stmt = null;
			String sql = "select PARAM_NAME,INTVAL, STRVAL, BOOLVAL from PARAMETERS where PORTFOLIO_NAME =?"+
						 "union all " +
						 "select PARAM_NAME,INTVAL, STRVAL, BOOLVAL from PARAMETERS where PORTFOLIO_NAME ='' ";
			
			try {
				stmt = con.prepareStatement(sql);
				stmt.setString(1, this.getCurrentProject());
				ResultSet rs = stmt.executeQuery();			      
				while (rs.next()) {
					String paramName = rs.getString("PARAM_NAME");
					Integer intVal = rs.getInt("INTVAL");
					String strVal = rs.getString("STRVAL");
					boolean boolVal = rs.getBoolean("BOOLVAL");
					Parameter p = new Parameter(intVal,strVal,boolVal);
					getParameters().put(paramName, p);
				}
			} catch (SQLException e) {
				JDBCTutorialUtilities.printSQLException(e);
			} finally {
				if (stmt != null) { stmt.close(); }
			}
		}
	public HashMap<String, Parameter> getParameters() {
			return this.parameters;
		}

		public void setParameters(HashMap<String, Parameter> parameters) {
			this.parameters = parameters;
		}

	private String getCurrentProject() {
		return this.getBelongsToGUI().getCurrentProject();
	}

	  private GUI getBelongsToGUI() {
		return this.getBelongsTo().getBelongsToGUI();
	}

	
	public int getIntegerParameter(String name) {
		return this.getParameters().get(name).getIntVal();
	}	
	public String getStringParameter(String name) {
		if(this.getParameters().get(name).getStrVal()==null) 
			return "";
		return this.getParameters().get(name).getStrVal();
	}	
	public boolean getBoolParameter(String name) {
		return this.getParameters().get(name).isBoolVal();
	}
/*
	private class Parameter {
		public Parameter(){}
		private int intVal;private String strVal; boolean boolVal;
		public Parameter(int intVal,String strVal, boolean boolVal){
			this.setIntVal(intVal);
			this.setStrVal(strVal);
			this.setBoolVal(boolVal);
		}
		public int getIntVal() {return intVal;}
		public void setIntVal(int intVal) {this.intVal = intVal;}
		public String getStrVal() {return strVal;}
		public void setStrVal(String strVal) {this.strVal = strVal;	}
		public boolean isBoolVal() {return boolVal;	}
		public void setBoolVal(boolean boolVal) {this.boolVal = boolVal;}	
	}
*/
}
