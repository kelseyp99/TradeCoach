package com.gui;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.oracle.tutorial.jdbc.JDBCTutorialUtilities;
import com.oracle.tutorial.jdbc.SecuritiesTable;

	public class SecuritiesTableModel extends TableParentModel  {
//		private ArrayList<Columns> tableMap = new ArrayList<Columns>();

		public SecuritiesTableModel(Object[] columnHeader, int i) {
			super(columnHeader, i);
		}
		
		public SecuritiesTableModel(SecuritiesTable securityInstTable) {super(securityInstTable);}

		public void setValueAt(Object value, int row, int col) {
	    	super.setValueAt(value, row, col);
	    	if(col != 0) return;
	        if (DEBUG) {
	            System.out.println("Setting value at " + row + "," + col
	                               + " to " + value
	                               + " (an instance of "
	                               + value.getClass() + ")");
	        }
	        data[row][col] = value;
	        fireTableCellUpdated(row, col);
	    //    int portfolioID  = (int)this.getValueAt(row, this.getTableInstance().getColumnIndexFromHeader("Portfolio ID"));
	        String tickerSymbol  = (String)this.getValueAt(row, this.getTableInstance().getColumnIndexFromHeader("Symbol"));
			this.getBelongsToPortfolios().getInitialPortfolio().setSelectedSecurityUsingSymbol(tickerSymbol, (Boolean)value);
			((SecuritiesTable) this.getTableInstance()).updateSelected(tickerSymbol, (Boolean)value);
	        if (DEBUG) {
	            System.out.println("New value of data:");
	            printDebugData();
	        }
	    }
		
	    public boolean isCellEditable(int row, int col) {
	        //Note that the data/cell address is constant,
	        //no matter where the cell appears onscreen.
	        if (col > 0) {
	            return false;
	        } else {
	            return true;
	        }
	    }
	}