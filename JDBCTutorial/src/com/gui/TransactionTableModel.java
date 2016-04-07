package com.gui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.oracle.tutorial.jdbc.TradeHistoryTable;
import com.workers.TransactionData;

public class TransactionTableModel extends MyTableModel {

	private List<TransactionData> transactionDatas;
	private GUI belongsToGUI;

	public TransactionTableModel(Object[] objects, int i) {
		super(objects, i);	
		transactionDatas = new ArrayList<>(20);		
	}
	public void addTransaction(TransactionData transactionData) {
		transactionDatas.add(transactionData);
		fireTableRowsInserted(transactionDatas.size() - 1, transactionDatas.size() - 1);
	}
	public TransactionData getTransactionAt(int row) {
		return transactionDatas.get(row);
	}
	public List<TransactionData> getChangedTransaction() {
		List<TransactionData> changed = new ArrayList<>(transactionDatas.size());

		for (TransactionData t : transactionDatas) {
			if (t.hasChanged()) {
				changed.add(t);
			}
		}
		return changed;    
	} 	
	public void SaveToDatabase() {
		List<TransactionData> changed = getChangedTransaction();
		
		for (TransactionData t : transactionDatas) {
			if (t.hasChanged()) {
			//	this.getBelongsToGUI().getGui().getPortfolios().getTradeHistoryTable().updateTableCell(t.getTradeID(), columnName, value);
			}
		}	
	}	
//	public void updateCellValueToDatabase(Transaction t, int column){
//	//	this.getTradeHistoryTable().updateTableCell(t.getTradeID(), column, value);
//	}
	private TradeHistoryTable getTradeHistoryTable() {
		return this.getBelongsToGUI().getPortfolios().getTradeHistoryTable();
	}
    public void tableChanged(TableModelEvent e) {
   // 	super.tableChanged(e);
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        String columnName = model.getColumnName(column);
        Object cellData = model.getValueAt(row, column);
    	int i = java.util.Arrays.asList(this.getTradeHistoryTable().getColumnHeader()).indexOf(columnName);
    }

	@Override
	public String getColumnName(int column) {
		String name = null;
		switch (column) {
		case 0:
			name = "First name";
			break;
		case 1:
			name = "First name";
			break;
		}
		return name;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		TransactionData t = transactionDatas.get(rowIndex);
		Object value = null;
		switch (columnIndex) {
		case 0:
			value = t.getInstrumentName();
			break;
		case 1:
			value = t.getInstrumentName();
			break;
		}
		return value;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue instanceof String) {
			TransactionData t = transactionDatas.get(rowIndex);
			switch (columnIndex) {
			case 0:
				t.setInstrumentName(aValue.toString());
				break;
			case 1:
				t.setInstrumentName(aValue.toString());
				break;
			}
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}

	public GUI getBelongsToGUI() {
		return belongsToGUI;
	}

	public void setBelongsToGUI(GUI belongsToGUI) {
		this.belongsToGUI = belongsToGUI;
	}
}


