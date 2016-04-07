package com.gui;

import com.oracle.tutorial.jdbc.Tables;

public class HistoricalPricesTableModel extends TableParentModel {

	public HistoricalPricesTableModel(Tables tableInstance) {
		super(tableInstance);
	}
	
	public HistoricalPricesTableModel(Object[] columnHeader, int i) {
		super(columnHeader, i);
		// TODO Auto-generated constructor stub
	}

	public boolean isCellEditable(int row, int col)	    {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col > 0) {
            return false;
        } else {
            return true;
        }
    }
	
	

}
