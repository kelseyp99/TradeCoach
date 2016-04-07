package com.junk;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.workers.TransactionData;

public class PeopleTableModel extends AbstractTableModel {

    private List<TransactionData> people;

    public PeopleTableModel() {
        people = new ArrayList<>(20);
    }

    public void addTransaction(TransactionData TransactionData) {
        people.add(TransactionData);
        fireTableRowsInserted(people.size() - 1, people.size() - 1);
    }

    public TransactionData getTransactionAt(int row) {
        return people.get(row);
    }

    public List<TransactionData> getChangedPeople() {
        List<TransactionData> changed = new ArrayList<>(people.size());

        for (TransactionData p : people) {
            if (p.hasChanged()) {
                changed.add(p);
            }
        }

        return changed;    
    }

    @Override
    public int getRowCount() {
        return people.size();
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
        TransactionData p = people.get(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0:
           //     value = p.getFirstName();
                break;
            case 1:
             //   value = p.getLastName();
                break;
        }
        return value;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue instanceof String) {
            TransactionData p = people.get(rowIndex);
            switch (columnIndex) {
                case 0:
             //       p.setFirstName(aValue.toString());
                    break;
                case 1:
               //     p.setLastName(aValue.toString());
                    break;
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}