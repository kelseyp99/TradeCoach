package com.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.SwingUtilities;
import com.workers.PortfoliosGroup;
import com.workers.TransactionData;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.oracle.tutorial.jdbc.HistoricalPricesTable;
import com.oracle.tutorial.jdbc.JDBCTutorialUtilities;
import com.oracle.tutorial.jdbc.Tables;
import com.workers.Portfolio;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;
import javax.swing.SwingConstants;

public class NewTradeEntry extends JDialog implements TableModelListener {
	private final JPanel contentPanel = new JPanel();
	private JTable tableNewTrades;
	private JTextField tfPosition;
	private JTextField tfPrice;
	private JTextField tfQuickString;
	private JTextField tfSymbol;
	private int rowNum=0, maxRowNum=0;
	private JPanel panel;
	private GUI belongsTo;
	private JTextField tfCompanyName;

	public static void main(String[] args) {
		try {
			NewTradeEntry dialog = new NewTradeEntry(new JFrame(), new GUI());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Create the dialog.
	 * @param frmTradecoach 
	 * @param gui2 
	 */
	public NewTradeEntry(JFrame frmTradecoach, GUI gui) {
		super(frmTradecoach);
		this.setBelongsTo(gui);
		setTitle("New Trade Entry");
		setIconImage(Toolkit.getDefaultToolkit().getImage(NewTradeEntry.class.getResource("/com/gui/images/hat-bowlhat-icon.png")));
		setBounds(100, 100, 477, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(null);
			{
				JLabel lblNewLabel_3 = new JLabel("Symbol");
				lblNewLabel_3.setHorizontalAlignment(SwingConstants.RIGHT);
				lblNewLabel_3.setAlignmentX(Component.RIGHT_ALIGNMENT);
				lblNewLabel_3.setBounds(3, 8, 49, 14);
				panel.add(lblNewLabel_3);
			}
			{
				tfSymbol = new JTextField();
				tfSymbol.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent arg0) {
						tableNewTrades.setValueAt(tfSymbol.getText(), getRowNum(), 0);
			            try {
							String companyName = getBelongsTo().getPortfoliosGroup().getSecurityInstTable().getSecuritiesName(tfSymbol.getText());
							tfCompanyName.setText(companyName);
						} catch (SQLException e) {
							e.printStackTrace();
						}
						//super.setValueAt(value, row, col);
					}
				});
				tfSymbol.setBounds(56, 5, 56, 20);
				panel.add(tfSymbol);
				tfSymbol.setColumns(10);
			}
			{
				JLabel lblNewLabel = new JLabel("Position");
				lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				lblNewLabel.setBounds(3, 33, 59, 14);
				panel.add(lblNewLabel);
			}
			{
				tfPosition = new JTextField();
				tfPosition.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent arg0) {
						tableNewTrades.setValueAt(tfPosition.getText(), getRowNum(), 1);
					}
				});
				tfPosition.setBounds(80, 33, 34, 20);
				panel.add(tfPosition);
				tfPosition.setColumns(10);
			}
			{
				JLabel lblNewLabel_1 = new JLabel("Entry $/share");
				lblNewLabel_1.setAlignmentX(Component.RIGHT_ALIGNMENT);
				lblNewLabel_1.setBounds(144, 33, 79, 14);
				panel.add(lblNewLabel_1);
			}
			{
				tfPrice = new JTextField();
				tfPrice.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent arg0) {
						tableNewTrades.setValueAt(tfPrice.getText(), getRowNum(), 2);
					}
				});
				tfPrice.setBounds(233, 30, 37, 20);
				panel.add(tfPrice);
				tfPrice.setColumns(10);
			}
			{
				JLabel lblNewLabel_2 = new JLabel("Quick Entry String");
				lblNewLabel_2.setBounds(3, 66, 86, 14);
				panel.add(lblNewLabel_2);
				lblNewLabel_2.setToolTipText("");
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBounds(111, 39, 1, 1);
				panel.add(panel_1);
				panel_1.setLayout(null);
			}
			{
				tfQuickString = new JTextField();
				tfQuickString.setBounds(105, 63, 324, 20);
				panel.add(tfQuickString);
				tfQuickString.setColumns(35);
			}
			{
				JLabel lblEgSpyqqqibm = new JLabel("e.g. SPY,95,$202.15;QQQ, $101;IBM");
				lblEgSpyqqqibm.setBounds(103, 88, 182, 14);
				panel.add(lblEgSpyqqqibm);
			}
			{
				//	contentPanel.add(table);
				//				JScrollPane scrollPane = new JScrollPane();
				//				scrollPane.setBounds(302, 61, 2, 2);
				//				panel.add(scrollPane);
			}

			String[] columnNames = {"Symbol", "Position", "Price $/share"};
			Object[][] data ={{"","",""},{"","",""},{"","",""},{"","",""},{"","",""},{"","",""},{"","",""},{"","",""},{"","",""}};
			/*     {
	            {"DE", new Integer(1), new Double(1.11)},
	            {"SPY", new Integer(1), new Double(2.22)},
	            {"PAAS", new Integer(1), new Double(3.33)},
	            {"IAG", new Integer(1), new Double(4.44)}
	        };*/

						
			DefaultTableModel model = new DefaultTableModel(data, columnNames);
			model.addTableModelListener( this );
			tableNewTrades = new JTable( model )
			{
				//  Returning the Class of each column will allow different
				//  renderers to be used based on Class
				public Class getColumnClass(int column)
				{
					return getValueAt(0, column).getClass();
				}
				//  The Cost is not editable
				public boolean isCellEditable(int row, int column)
				{
					int modelColumn = convertColumnIndexToModel( column );
					return (modelColumn == 3) ? false : true;
				}
			};
			//	tableNewTrades = new JTable();
			Connection connArg = this.getBelongsToPortfolios().getMyConnection();
			String dbNameArg =this.getBelongsToPortfolios().getMyJDBCTutorialUtilities().dbName;
			String dbmsArg =this.getBelongsToPortfolios().getMyJDBCTutorialUtilities().dbms;
			NewTradeTable newTradeTable = new NewTradeTable(connArg, dbNameArg, dbmsArg, this.getBelongsToPortfolios());
			NewTradeTableModel newTradeTableModel = new NewTradeTableModel(newTradeTable);
			newTradeTableModel.setBelongsToGUI(this.belongsTo);
			newTradeTableModel.addTableModelListener( this );
			//	tableNewTrades.setModel(newTradeTableModel);
		//	tableNewTrades = new JTable(model);
		//	tableNewTrades = new JTable(newTradeTableModel);
			tableNewTrades.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent event1) {
					//    if (event1.getButton() == MouseEvent.BUTTON3) {
					Point point = event1.getPoint();
					int column = tableNewTrades.columnAtPoint(point);
					int row = tableNewTrades.rowAtPoint(point);

					//   table.setColumnSelectionInterval(column, column);
					//  table.setRowSelectionInterval(row, row);
					tfSymbol.setText(tableNewTrades.getValueAt(row, 0).toString());
					tfPosition.setText(tableNewTrades.getValueAt(row, 1).toString());
					tfPrice.setText(tableNewTrades.getValueAt(row, 2).toString());
					setRowNum(row);
					//       System.out.printf("column, row:  %s, %s\n", column, row);
					//  }
				}
			});
			//       table.setPreferredScrollableViewportSize(table.getPreferredSize());
			//		scrollPane.setColumnHeaderView(table);
		}
		{
			JScrollPane scrollPane = new JScrollPane(tableNewTrades);
			scrollPane.setBounds(0, 110, 451, 108);
			panel.add(scrollPane);
			
			tfCompanyName = new JTextField();
			tfCompanyName.setBounds(233, 5, 196, 20);
			panel.add(tfCompanyName);
			tfCompanyName.setColumns(10);
			
			JLabel lblComapnyName = new JLabel("Comapny Name");
			lblComapnyName.setBounds(142, 8, 95, 14);
			panel.add(lblComapnyName);
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);

			JButton btnFirst = new JButton("<< First");
			btnFirst.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setRowNum(0);
				}
			});
			buttonPane.add(btnFirst);

			JButton btnPrior = new JButton("< Prior");
			btnPrior.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					decreaseRowNum();
				}
			});
			buttonPane.add(btnPrior);

			JButton btnNext = new JButton("Next >");
			btnNext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					increaseMaxRowNum();
				}
			});
			buttonPane.add(btnNext);

			JButton btnLast = new JButton("Last >>");
			btnLast.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setRowNum(getMaxRowNum());
				}
			});
			buttonPane.add(btnLast);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				{
					JButton btnAdvise = new JButton("Advise");   						
					NewTradeEntry nte=this;
					btnAdvise.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							SwingUtilities.invokeLater(new Runnable() {  
								@Override
								public void run() {	
									getBelongsToPortfolios().createTradeAdvise( nte);		}
							});
						}
					});
					buttonPane.add(btnAdvise);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public GUI getBelongsTo() {
		return belongsTo;
	}

	public PortfoliosGroup getBelongsToPortfolios() {
		return belongsTo.getPortfoliosGroup();
	}

	public Portfolio getInitialPortfolio() {
		return belongsTo.getPortfoliosGroup().getInitialPortfolio();
	}

	public void setBelongsTo(GUI belongsTo) {
		this.belongsTo = belongsTo;
	}

	public void increaseRowNum(){
		int rowNum = this.getRowNum();
		this.setRowNum(rowNum++);
	}	
	public void decreaseRowNum(){
		int rowNum = this.getRowNum();
		this.setRowNum(rowNum--);
	}

	public int getMaxRowNum() {
		return maxRowNum;
	}

	public void setMaxRowNum(int maxRowNum) {
		this.maxRowNum = maxRowNum;
	}

	public JTable getTable() {
		return tableNewTrades;
	}

	public void setTable(JTable table) {
		this.tableNewTrades = table;
	}

	public JTextField getTfPosition() {
		return tfPosition;
	}

	public void setTfPosition(JTextField tfPosition) {
		this.tfPosition = tfPosition;
	}

	public JTextField getTfPrice() {
		return tfPrice;
	}

	public void setTfPrice(JTextField tfPrice) {
		this.tfPrice = tfPrice;
	}

	public JTextField getTfQuickString() {
		return tfQuickString;
	}

	public void setTfQuickString(JTextField tfQuickString) {
		this.tfQuickString = tfQuickString;
	}

	public JTextField getTfSymbol() {
		return tfSymbol;
	}

	public void setTfSymbol(JTextField tfSymbol) {
		this.tfSymbol = tfSymbol;
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public void increaseMaxRowNum(){
		int maxRowNum = this.getMaxRowNum();
		this.setMaxRowNum(maxRowNum++);
	}

	public class NewTradeTableModel extends TableParentModel  {
		private static final boolean DEBUG = true;
		public NewTradeTableModel(Object[] columnHeader, int i) {
			super(columnHeader, i);
			this.setData(new Object[20][20]);
		}
		public NewTradeTableModel(Tables newTradeTable) {super(newTradeTable);}
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
			fieldName=fieldName==null?"SELECTED":fieldName;
			int tradeID = (int) this.getValueAt(row, 1);
			try {
				//		this.getBelongsToPortfolios().getTradeHistoryTable().updateFeildAtPosition(row, i, value.toString());
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
	}	
	public class NewTradeTable extends Tables {	  
		//private Connection myConnection = myJDBCTutorialUtilities.getConnection();
		
		public NewTradeTable(Connection connArg, String dbNameArg, String dbmsArg, PortfoliosGroup belongsTo) {
			super(connArg, dbNameArg, dbmsArg, belongsTo, "TRADE_HISTORY");
			this.initialize();
		}

		public void initialize(){
			try {
				//    super.initialize(this.getDdlCreate());
				CheckBoxHeader selected = new CheckBoxHeader(new MyItemListener());
				this.getTableMap().add(new Columns(selected,true,25,"SELECTED",Boolean.class,"click checkbox to select or deselect"));
		//		this.getTableMap().add(new Columns("Trade ID",true,25,"TRADE_ID",int.class,"none"));
				this.getTableMap().add(new Columns("Symbol",true,25,"TICKERSYMBOL",String.class,"Ticker Symbol of Security"));
		//		this.getTableMap().add(new Columns("Name",true,"INSTRUMENTNAME",String.class,"Name of Security"));
				this.getTableMap().add(new Columns("Order Date",true,25," ORDERDATE",String.class,"Date this order was placed with broker for execution"));
				this.getTableMap().add(new Columns("Entry Price",true,25," ENTRYPRICE",double.class,"Price at which order was placed. This is not necessarily the actual price purchased at"));
				this.getTableMap().add(new Columns("Stop Loss",true,25," STOPLOSS",double.class,"The stop exit price you placed so as to limit furhter loss"));
				this.getTableMap().add(new Columns("Stop",true,25," STOP",double.class,"The stop price (planned exit) at which the intended profit objective is achieved"));
				this.getTableMap().add(new Columns("Stp Trig",true,25," STOPTRIGGER",double.class,"The price at which when passed through the stop price order is activated"));
				this.getTableMap().add(new Columns("Stp Type",true,25," STOPTYPE",String.class,"The type of order describing the stop price (planned exit)"));
				this.getTableMap().add(new Columns("Stop Activation Date",true,25," STOPACTIVATIONDATE",String.class,"Stop Price Activiation Date"));
				this.getTableMap().add(new Columns("Position",true,25," POSITION ",int.class,"Number of Shares ordered"));
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

		public void saveMyTrade(TransactionData ts) {
			PreparedStatement stmt = null;

			try {
				stmt = con.prepareStatement(
						"INSERT INTO APP.TRADE_HISTORY " + 
								"(TICKERSYMBOL, INSTRUMENTNAME, ORDERDATE, ENTRYPRICE, STOPLOSS, STOP, STOPTRIGGER, STOPTYPE, STOPACTIVATIONDATE, POSITION) " +
						"VALUES ( ?, ? , ? , ?, ?, ? , ? , ? , ? , ? )");
				//	  stmt.setInt(1, 111 );
				stmt.setString(1, ts.getTickerSymbol());
				//  java.sql.Date sqlDate = new java.sql.Date(cs.getDate().getTime());
				stmt.setString(2, ts.getInstrumentName());
				stmt.setDate(3, ts.getOrderDate());
				stmt.setDouble(4, ts.getEntryPrice());
				stmt.setDouble(5, ts.getStopLoss());
				stmt.setDouble(6, ts.getStop());
				stmt.setDouble(7, ts.getStopTrigger());
				stmt.setString(8, ts.getStopType().toString());
				stmt.setDate(9, ts.getStopActivationDate());
				stmt.setInt(10, ts.getPosition());
				stmt.executeUpdate();
			} catch (java.sql.SQLIntegrityConstraintViolationException s) {
				//  JDBCTutorialUtilities..printSQLException(e);		  

			} catch (SQLException e) {
				JDBCTutorialUtilities.printSQLException(e);
			} finally {
				//if (stmt != null) { stmt.close(); }
			}
		}
	}
}
