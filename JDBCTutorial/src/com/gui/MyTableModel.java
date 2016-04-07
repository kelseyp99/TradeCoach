package com.gui;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

import com.oracle.tutorial.jdbc.Tables;
import com.utilities.GlobalVars;

public class MyTableModel extends TableParentModel  {
	private static final boolean DEBUG = true;
	public MyTableModel(Object[] columnHeader, int i) {
		super(columnHeader, i);
		this.setData(new Object[20][20]);
	}
	public MyTableModel(Tables tradeHistoryTable) {super(tradeHistoryTable);}
    public void setValueAt(Object value, int row, int col) {
    	super.setValueAt(value, row, col);
        if (DEBUG) {
            System.out.println("Setting value at " + row + "," + col
                               + " to " + value
                               + " (an instance of "
                               + value.getClass() + ")");
        }
        data[row][col] = value;
        fireTableCellUpdated(row, col);
        String columnName = super.getColumnName(col);
        String fieldName = this.getTableInstance().getColumnFeildName(columnName);
       // fieldName=fieldName==null?"SELECTED":fieldName;
        if(fieldName==null) {
        	fieldName="SELECTED";
	     //   int portfolioID  = (int)this.getValueAt(row, this.getTableInstance().getColumnIndexFromHeader("Portfolio ID"));
        	String tickerSymbol  = (String)this.getValueAt(row, this.getTableInstance().getColumnIndexFromHeader("Symbol"));
	        Date orderDate = null;
			try {
				orderDate = GlobalVars.df.parse((String) this.getValueAt(row, this.getTableInstance().getColumnIndexFromHeader("Order Date")));
			} catch (ParseException e) {
				e.printStackTrace();
			}
	        this.getBelongsToPortfolios().getInitialPortfolio().setSelectedSecurityUsingSymbol(orderDate,tickerSymbol, (Boolean)value);
        }
        int tradeID = (int) this.getValueAt(row, 1);
    	try {
    		this.getTableInstance().updateTableUsingTradeID(tradeID, fieldName , value.toString());
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
        if (DEBUG) {
            System.out.println("New value of data:");
            printDebugData();
        }
    }
    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col == 1) {
           return false;
        } else {
            return true;
        }
    }
    
	protected void printDebugData() {
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
    }
}