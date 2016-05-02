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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

import com.gui.CheckBoxHeader;
import com.gui.GUI;
import com.tradecoach.patenter.entity.security.CandleSticks;
import com.workers.MoneyMgmtStrategy;
import com.workers.PortfolioGroup;
import com.workers.PortfolioGroup;
import com.workers.PortfoliosGroup;
import com.workers.SecurityInst;

public class PortfoliosTable  extends Tables {
	   private static SessionFactory factory; 
	   public static void main(String[] args) {
	      try{
	         factory = new AnnotationConfiguration().
	                   configure().
	                   //addPackage("com.xyz") //add package if used.
	                   addAnnotatedClass(Employee.class).
	                   buildSessionFactory();
	      }catch (Throwable ex) { 
	         System.err.println("Failed to create sessionFactory object." + ex);
	         throw new ExceptionInInitializerError(ex); 
	      }

	      PortfoliosTable PT = new PortfoliosTable();

	      /* Add few employee records in database */
	      String empID1 = PT.addPortfolio("Zara");
	   }
	      
	   public String addPortfolio(String portfolioName){
		      Session session = factory.openSession();
		      Transaction tx = null;
		     // Integer portfolioID = null;
		      try{
		         tx = session.beginTransaction();
		         PortfolioGroup portfolio = new PortfolioGroup();
		         portfolio.setPortfolioName(portfolioName);
		         portfolioName = (String) session.save(portfolio); 
		         tx.commit();
		      }catch (HibernateException e) {
		         if (tx!=null) tx.rollback();
		         e.printStackTrace(); 
		      }finally {
		         session.close(); 
		      }
		      return portfolioName;
		   }

	public PortfoliosTable() {}
	public PortfoliosTable(Connection connArg, String dbNameArg, String dbmsArg, PortfoliosGroup belongsTo) {
		super(connArg, dbNameArg, dbmsArg, belongsTo, "PORTFOLIOS");
		this.setDdlCreate(new String[] {"CREATE TABLE APP.PORTFOLIOS ( " +
				"PORTFOLIO_NAME VARCHAR(20) NOT NULL, "+
				"Primary Key (PORTFOLIO_NAME) "+
		") "});
		this.setDmlPopulate("insert into APP.PORTFOLIOS (PORTFOLIOS.PORTFOLIO_NAME) values ('DEFAULT_PORTFOLIO')");
		this.initialize();
	}

	public PortfoliosTable(PortfoliosGroup belongsTo) {
	  super(belongsTo, "PORTFOLIOS");
	  this.initialize();
	}

	public void initialize(){
		super.initialize();
	}

	private HistoricalPricesTable getHistoricalPriceTable() {
		return this.getBelongsTo().getHistoricalPricesTable();		  
	}

	public void savePortfolioInfo(PortfolioGroup p) {
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(
					"INSERT INTO PORTFOLIOS" + 
							"(PORTFOLIO_NAME) " +
					"VALUES ( ? )");
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

	public void deletePortfolioInfo(PortfolioGroup p, JFrame frame) {
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
	private GUI getBelongsToGUI() {
		return this.getBelongsTo().getBelongsToGUI();
	}
}
