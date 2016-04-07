package com.junk;

import java.sql.SQLException;
import com.gui.*;
import com.oracle.tutorial.jdbc.Tables;

public class MyTableModel extends TableParentModel  {	
	private static final boolean DEBUG = false;
//	private Object[][] data;
//	private ArrayList<Columns> tableMap = new ArrayList<Columns>();

	public MyTableModel(Object[] columnHeader, int i) {
		super(columnHeader, i);
		this.setData(new Object[20][20]);
	//	this.initialize();
	}
	
	public MyTableModel(Tables instaTable) {super(instaTable);}

	public void initialize(){
/*		try {
			tableMap.add(new Columns("Trade ID","TRADE_ID","int","none"));
			tableMap.add(new Columns("Symbol","TICKERSYMBOL","String","Ticker Symbol of Security"));
			tableMap.add(new Columns("Name","INSTRUMENTNAME","String","Name of Security"));
			tableMap.add(new Columns("Order Date"," ORDERDATE","String","Date this order was placed with broker for execution"));
			tableMap.add(new Columns("Entry Price"," ENTRYPRICE","date","Price at which order was placed. This is not necessarily the actual price purchased at"));
			tableMap.add(new Columns("Stop Loss"," STOPLOSS","double","The stop exit price you placed so as to limit furhter loss"));
			tableMap.add(new Columns("Stop"," STOP","double","The stop price (planned exit) at which the intended profit objective is achieved"));
			tableMap.add(new Columns("Stp Trig"," STOPTRIGGER","double","The price at which when passed through the stop price order is activated"));
			tableMap.add(new Columns("Stp Type"," STOPTYPE","double","The type of order describing the stop price (planned exit)"));
			tableMap.add(new Columns("Stop Activation Date"," STOPACTIVATIONDATE","double","Stop Price Activiation Date"));
			tableMap.add(new Columns("Position"," POSITION ","int","Number of Shares ordered"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

*/
	}
	
    public void setValueAt(Object value, int row, int col) {
    	super.setValueAt(value, row, col);
        System.out.println("made it hereA");
        if (DEBUG) {
            System.out.println("wwSetting value at " + row + "," + col
                               + " to " + value
                               + " (an instance of "
                               + value.getClass() + ")");
        }

        System.out.println("made it here");
        data[row][col] = value;
        System.out.println("made it here1");
        fireTableCellUpdated(row, col);
        String columnName = super.getColumnName(col);
        System.out.println("made it here2");
        String fieldName = this.getTableInstance().getColumnFeildName(columnName);
        int tradeID = (int) this.getValueAt(row, 1);
        System.out.println("made it here3");
    //	int i = java.util.Arrays.asList(this.getBelongsToPortfolios().getTradeHistoryTable().getColumnHeader()).indexOf(columnName);
    	try {
    //		this.getBelongsToPortfolios().getTradeHistoryTable().updateFeildAtPosition(row, i, value.toString());
    		this.getTableInstance().updateTableUsingTradeID(tradeID, fieldName , value);
		} catch (SQLException e) {
			e.printStackTrace();
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
    /*  public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
   //     if (col < 2) {
     //       return false;
      //  } else {
            return true;
       // }
    }
    
	private void printDebugData() {
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
	public GUI getBelongsToGUI() {
		return belongsToGUI;
	}

	public void setBelongsToGUI(GUI belongsToGUI) {
		this.belongsToGUI = belongsToGUI;
	}

	public Object[][] getData() {
		return data;
	}

	public void setData(Object[][] data) {
		this.data = data;
	}
	
	public Portfolios getBelongsToPortfolios() {
		return this.getBelongsToGUI().getPortfolios();
	}
	
	public String getColumnFeildName(String columnHeader) {
		for (Columns col : this.getTableMap()) {
			if(col.getColumnHeader()==columnHeader){
				return col.getColumnFeildName();
			}				
		}
		return null;
	}*/
	/*
	public ArrayList<Columns> getTableMap() {
		return tableMap;
	}

	public void setTableMap(ArrayList<Columns> tableMap) {
		this.tableMap = tableMap;
	}

	public class Columns{

		String columnHeader, columnFeildName, columnType, columnToolTip;

		public Columns() {};

		public Columns(String columnHeader, String columnFeildName, String columnType, String columnToolTip) {
			super();
			this.columnHeader = columnHeader;
			this.columnFeildName = columnFeildName;
			this.columnType = columnType;
			this.columnToolTip = columnToolTip;
		}
		

		public String getColumnFeildName() {
			return columnFeildName;
		}

		public String getColumnHeader() {
			return columnHeader;
		}
		
		public void setColumnFeildName(String columnFeildName) {
			this.columnFeildName = columnFeildName;
		}

		public String getColumnType() {
			return columnType;
		}

		public void setColumnType(String columnType) {
			this.columnType = columnType;
		}

		public String getColumnToolTip() {
			return columnToolTip;
		}

		public void setColumnToolTip(String columnToolTip) {
			this.columnToolTip = columnToolTip;
		}

		public void setColumnHeader(String columnHeader) {
			this.columnHeader = columnHeader;
		}
	}
*/
}