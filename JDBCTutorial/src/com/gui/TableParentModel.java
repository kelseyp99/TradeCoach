package com.gui;

	import java.awt.Component;
import java.sql.Date;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import com.oracle.tutorial.jdbc.Tables;
import com.workers.PortfoliosGroup;
import com.oracle.tutorial.jdbc.Tables.Columns;;

	public class TableParentModel extends DefaultTableModel  {
		protected static final boolean DEBUG = false;
		private GUI belongsToGUI;
		protected Object[][] data;
		private Tables tableInstance;

		public TableParentModel(Object[] columnHeader, int i) {
			super(columnHeader, i);
			int row=this.getTableInstance().getRowCount();
			int col=this.getTableInstance().getTableMap().size();
			this.setData(new Object[row][col]);
	//		this.initialize();
		}
		
		public TableParentModel(Tables tableInstance) {
			super(tableInstance.getColumnFieldNames(true), 0);			
			this.setTableInstance(tableInstance);
			int row=this.getTableInstance().getRowCount();
			int col=this.getTableInstance().getTableMap().size();
			this.setData(new Object[row][col]);
		//	this.initialize();
		}

		public void initialize() {
			SwingUtilities.invokeLater(new Runnable() {    						
				@Override
				public void run() {	
					try {
						setAllignment();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
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
		
		public PortfoliosGroup getBelongsToPortfolios() {
			return this.getTableInstance().getBelongsTo();
		}

		public Tables getTableInstance() {
			return tableInstance;
		}

		public void setTableInstance(Tables tableInstance) {
			this.tableInstance = tableInstance;
		}
		
        public Class<?> getColumnClass(int column) {
//        	return this.getTableMap().get(column).getColumnType();
    //    	return getValueAt(0, column).getClass();
        	Class<?> x = getValueAt(0, column).getClass();
        	return x;
        }
  
    	public void setAllignment() {
    		JTable table=this.getTableInstance().getTableGUI();
    		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
    		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    		DefaultTableCellRenderer floatRightRenderer = new DefaultTableCellRenderer();
    		DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer();
    		DecimalAlignRenderer decimalAlignRenderer = new DecimalAlignRenderer();
    		rightRenderer.setHorizontalAlignment(JLabel.RIGHT) ;
    		leftRenderer.setHorizontalAlignment(JLabel.LEFT) ;
    		centerRenderer.setHorizontalAlignment(JLabel.CENTER) ;
    		floatRightRenderer.setHorizontalAlignment(JLabel.RIGHT) ;
    		int j = 0;
    		for(int i = 0; i<this.getTableMap().size(); i++) {
    			if(this.getTableMap().get(i).isVisible()){
    				Class<?> c = this.getTableMap().get(i).getColumnType();
    				if(c==Integer.class||c==int.class)
    					table.getColumnModel().getColumn(j).setCellRenderer(rightRenderer);
    				else if(c==Double.class||c==double.class)
    					table.getColumnModel().getColumn(j).setCellRenderer(rightRenderer);
    				else if(c==String.class)
    					table.getColumnModel().getColumn(j).setCellRenderer(leftRenderer);
    				//	else if(c==Date.class)
    				//	table.getColumnModel().getColumn(j).setCellRenderer(dateRenderer);
    				j++;//no visible fields cause the tablemap and actual JTable indexes to loose sync
    			}
    		}
    	}
        
		private ArrayList<Columns> getTableMap() {
			return this.getTableInstance().getTableMap();
		}	
		
		private static class DecimalAlignRenderer implements TableCellRenderer {
	        private static final float POS = 40f;
	        private static final int ALIGN = TabStop.ALIGN_DECIMAL;
	        private static final int LEADER = TabStop.LEAD_NONE;
	        private static final SimpleAttributeSet ATTRIBS = new SimpleAttributeSet();
	        private static final TabStop TAB_STOP = new TabStop(POS, ALIGN, LEADER);
	        private static final TabSet TAB_SET = new TabSet(new TabStop[] { TAB_STOP });

	        private StyledDocument document = new DefaultStyledDocument();
	        private JTextPane pane = new JTextPane(document);

	        public DecimalAlignRenderer() {
	            StyleConstants.setTabSet(ATTRIBS, TAB_SET);
	            pane.setParagraphAttributes(ATTRIBS, false);
	        }

	        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
	                    boolean hasFocus, int row, int column) {
	            if (value == null) {
	                pane.setText("\t0.0");
	            } else {
	                pane.setText("\t" + value.toString());
	            }
	            return pane;
	        }
	    }
	}
