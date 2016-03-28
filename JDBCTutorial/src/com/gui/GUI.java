package com.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import com.sun.javafx.applet.Splash;
import com.thehowtotutorial.splashscreen.JSplash;
import com.workers.Portfolio;
import com.workers.Portfolios;
import com.workers.Tools;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import java.awt.event.MouseAdapter;
import javax.swing.JTextPane;
import java.awt.Color;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuKeyEvent;

public class GUI  implements com.utilities.GlobalVars , TableModelListener  { 
	private JFrame frmTradecoach;
	private Portfolios portfolios;
	private JTextArea textArea, textArea_1;
	JTextPane tpAnalysis;
	JTextPane tpSenarios;	
	JTextPane tpMyResults;
	private JTextPane tpNewTrades;	
	private Date startDate, endDate;
	private boolean initPortfolioLoaded = false, candidateUnivLoaded = false;
	private JButton btnSolveMyROI2;
	private JButton btnFindBestStrategy2;
	private JMenuItem miFindBestStrategy ;
	private JMenuItem miSolveRoiFor;
	public JButton btnDump;
	public JButton btnLoadMyTradesButton;
	public JButton btnExit;
	String tickerFile =  defaultTickerFile;
	String tickerUFile =  defaultTickerUFile;
	private int newShorts, newLongs;
	private Double maxNewCapital;
	private JLabel lblSecurityCount;
	private Object roiPlugString="Calculating ...";
	GUI gui;	
	private JTable tableMyTrades;
	private JTable tableHistoricalPrices;
	private JTable tableSecurities;
	private JTable tablePortfolos;
	String filename = "C:\\Users\\Phil\\Google Drive\\Stock Market\\Trades2.csv";
	String propertiesfile = "properties/javadb-sample-properties.xml"; 
	protected String[] columnToolTips = {null,
            null,
            "The person's favorite sport to participate in",
            "The number of years the person has played the sport",
            "If checked, the person eats no meat"};
	private boolean sendOutput2Display=true;
	private boolean processingOriginalList;
	private StyleSheet styleSheet;// = new StyleSheet();
	private HTMLDocument htmlDocument;
	private HTMLEditorKit htmlEditorKit;
	private String CurrentProject = "DEFAULT PORTFOLIO";
	private JMenu mnRecentProjects;
	private MRU topMRU;
	private JLabel statusLabel;
	private Icon iconHat;// = (Icon) Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/com/gui/images/hat-bowlhat-icon.png"));

    public static void main(String[] args) {
		/*try {
			JSplash splash = new JSplash(Splash.class.getResource("Capture.png"),true, true, false, "V1", null, Color.red,Color.black);
			splash.splashOn();
			splash.setProgress(20, "init");
			Thread.sleep(1000);
			splash.setProgress(40, "loading");
			Thread.sleep(1000);
			splash.setProgress(60, "applying configs");
			Thread.sleep(1000);
			splash.setProgress(80, "starting app");
			Thread.sleep(1000);
			JLabel jl = new JLabel();
			jl.setText("Count : 0");
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}*/
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               // new TextAreaLogProgram().setVisible(true);                
				try {
					GUI window = new GUI();
					window.tableMyTrades.getModel().addTableModelListener(window);
					window.getFrmTradecoach().setVisible(true);
					window.setGui(window);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });
    }
	public GUI() {
		initialize();
	}

	private void initialize() {
		try {
			portfolios = new  Portfolios(propertiesfile);
		} catch (IOException | SQLException e2) {			
			e2.printStackTrace();
		}
		setFrmTradecoach(new JFrame());
		getFrmTradecoach().setTitle("TradeCoach 1.0");
		getFrmTradecoach().setIconImage(Toolkit.getDefaultToolkit().getImage(GUI.class.getResource("/com/gui/images/hat-bowlhat-icon.png")));
		getFrmTradecoach().setAlwaysOnTop(true);
		getFrmTradecoach().setResizable(false);
		getFrmTradecoach().setBounds(100, 100, 965, 656);
		getFrmTradecoach().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getFrmTradecoach().getContentPane().setLayout(null);
		Font font = new Font(textAreaFont, Font.PLAIN, textAreaFontSize);

        Calendar calendar = Calendar.getInstance(); // this would default to now
        endDate = calendar.getTime();
        this.addDays(endDate, -1);
        this.addDays(endDate, -1);
        calendar.add(Calendar.YEAR, -1);
        setStartDate(calendar.getTime());
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 39, 772, 539);
		getFrmTradecoach().getContentPane().add(tabbedPane);
		tableMyTrades = new JTable() {            
            //Implement table cell tool tips.
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);

                if (realColumnIndex == 2) { //Sport column
                    tip = "This person's favorit"
                    		+ ".e sport to "
                           + "participate in is: "
                           + getValueAt(rowIndex, colIndex);
                } else if (realColumnIndex == 4) { //Veggie column
                    TableModel model = getModel();
                    String firstName = (String)model.getValueAt(rowIndex,0);
                    String lastName = (String)model.getValueAt(rowIndex,1);
                    Boolean veggie = (Boolean)model.getValueAt(rowIndex,4);
                    if (Boolean.TRUE.equals(veggie)) {
                        tip = firstName + " " + lastName
                              + " is a vegetarian";
                    } else {
                        tip = firstName + " " + lastName
                              + " is not a vegetarian";
                    }
                } else { 
                    //You can omit this part if you know you don't 
                    //have any renderers that supply their own tool 
                    //tips.
                    tip = super.getToolTipText(e);
                }
                return tip;
            }

            //Implement table header tool tips. 
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {
                    public String getToolTipText(MouseEvent e) {
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                      // columnToolTips[realIndex];
                        return portfolios.getTradeHistoryTable().getColumnToolTips()[realIndex];
                    }
                };
            }
        };
		tableMyTrades.setFillsViewportHeight(true);
		tableMyTrades.setAutoCreateRowSorter(true);
		JScrollPane scrollPane_2 = new JScrollPane(tableMyTrades,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tabbedPane.addTab("My Trades", null, scrollPane_2, null);
		
		JButton btnNewButton_2 = new JButton("New button");
		scrollPane_2.setColumnHeaderView(btnNewButton_2);
		
		JMenuItem menuItem = new JMenuItem("Load My Trade Data");
		scrollPane_2.setColumnHeaderView(menuItem);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("New menu item");
		scrollPane_2.setColumnHeaderView(mntmNewMenuItem);
		
		JMenuItem mntmLoadTradeDataExcel = new JMenuItem("Load Trade Data from Excel");
		scrollPane_2.setColumnHeaderView(mntmLoadTradeDataExcel);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String filename = "C:\\Users\\Phil\\Google Drive\\Stock Market\\Trades2.csv";
					portfolios.getInitialPortfolio().getDl().loadCSVfile2(filename);
					try {
						portfolios.getTradeHistoryTable().viewTable((DefaultTableModel) tableMyTrades.getModel());
					} catch (SQLException e) {
						
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					
					e.printStackTrace();
				}
			}
		});
		
		tableSecurities = new JTable();
		tableSecurities.setFillsViewportHeight(true);
		tableSecurities.setAutoCreateRowSorter(true);
		JScrollPane scrollPane_3 = new JScrollPane(tableSecurities,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tabbedPane.addTab("Securities", null, scrollPane_3, null);
		
		tableHistoricalPrices = new JTable ();
		tableHistoricalPrices.setFillsViewportHeight(true);
		tableHistoricalPrices.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(tableHistoricalPrices);
		tabbedPane.addTab("Historical Prices", null, scrollPane, null);
		
		tpMyResults = new JTextPane();
		tpMyResults.setForeground(new Color(0, 0, 0));
		tpMyResults.setContentType("text/html");
		//scrollPane_4.setColumnHeaderView(tpMyResults);
		JScrollPane scrollPane_4 = new JScrollPane(tpMyResults);
		tabbedPane.addTab("My Results", null, scrollPane_4, null);
		
		tpSenarios = new JTextPane();
		tpSenarios.setContentType("text/html");

		JScrollPane scrollPane_5 = new JScrollPane(tpSenarios);
		tabbedPane.addTab("Scenarios", null, scrollPane_5, null);
		
		tpAnalysis = new JTextPane();
		tpAnalysis.setContentType("text/html");
		JScrollPane scrollPane_6 = new JScrollPane(tpAnalysis);
		tabbedPane.addTab("Analysis", null, scrollPane_6, null);
		
		setTpNewTrades(new JTextPane());
		getTpNewTrades().setContentType("text/html");
		JScrollPane scrollPane_7 = new JScrollPane(getTpNewTrades());
		tabbedPane.addTab("New Trades", null, scrollPane_7, null);
		
		htmlEditorKit = new HTMLEditorKit();
		tpSenarios.setEditorKit(htmlEditorKit);
		tpMyResults.setEditorKit(htmlEditorKit);
		tpAnalysis.setEditorKit(htmlEditorKit);
		tpNewTrades.setEditorKit(htmlEditorKit);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		tabbedPane.addTab("Activity", null, scrollPane_1, null);
		scrollPane_1.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		textArea = new JTextArea();
		textArea.setFont(font);
		scrollPane_1.setViewportView(textArea);
		
		PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
		
		JToolBar toolBar = new JToolBar();
		//toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));
		toolBar.setRollover(true);
		toolBar.setBounds(10, 0, 390, 40);
		getFrmTradecoach().getContentPane().add(toolBar);
		
		JButton btnNewProjectButton_1 = new JButton("");
		btnNewProjectButton_1.setToolTipText("Create a new project portfolio");
		btnNewProjectButton_1.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/new-document-icon24.png")));
		toolBar.add(btnNewProjectButton_1);
		
		JButton btnOpenProjectButton_1 = new JButton("");
		btnOpenProjectButton_1.setToolTipText("Open existing project portfolio");
		btnOpenProjectButton_1.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/Open-icon (1).png")));
		toolBar.add(btnOpenProjectButton_1);
		
		//btnSolveMyROI2 = new JButton("");
		setBtnSolveMyROI2(new JButton(""));
		getBtnSolveMyROI2().setSelectedIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/calculator-icon.png")));
		getBtnSolveMyROI2().setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/calculator-icon.png")));
		getBtnSolveMyROI2().setToolTipText("Solve for ROI for my existing trades");
		getBtnSolveMyROI2().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadInitPortfolio();
			}
		});
		
		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/Document-Delete-icon.png")));
		btnNewButton_1.setToolTipText("Close Project Portfolio");
		toolBar.add(btnNewButton_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_2);
		
		JButton btnNewButton = new JButton("");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/Print-icon.png")));
		btnNewButton.setToolTipText("Print");
		toolBar.add(btnNewButton);
		
		JSeparator separator_6 = new JSeparator();
		separator_6.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_6);
		toolBar.add(btnSolveMyROI2);
		
		setBtnFindBestStrategy2(new JButton(""));
		getBtnFindBestStrategy2().setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/Magic-wand-icon.png")));
		getBtnFindBestStrategy2().setToolTipText("Find best strategy based on my trades");
		getBtnFindBestStrategy2().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				findBestStrategy();
			}
		});
		toolBar.add(getBtnFindBestStrategy2());
		
		JSeparator separator_7 = new JSeparator();
		separator_7.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_7);
		
		btnDump = new JButton("");
		btnDump.setToolTipText("Export to Excel File");
		btnDump.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/export-excel-icon.png")));
		toolBar.add(btnDump);
		btnDump.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				String sFileName = getFileName();
				portfolios.getTradeHistoryTable().exportData(sFileName);
			}
		});
		
		btnLoadMyTradesButton = new JButton("");
		btnLoadMyTradesButton.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/document-import-icon.png")));
		btnLoadMyTradesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				portfolios.importTradeHistoryDataFromCSVwithDELETE();
			}
		});
		btnLoadMyTradesButton.setToolTipText("Import my trades from Excel file");
		toolBar.add(btnLoadMyTradesButton);
		
		btnExit = new JButton("");
		btnExit.setToolTipText("Exit TradeCoach");
		btnExit.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/Actions-window-close-icon.png")));
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		
		JSeparator separator_8 = new JSeparator();
		separator_8.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_8);
		
		JButton button = new JButton("");
		button.setIcon(new ImageIcon(GUI.class.getResource("/com/gui/images/Help-icon.png")));
		button.setToolTipText("Get Help from TradeCoach");
		toolBar.add(button);
		toolBar.add(btnExit);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBounds(10, 577, 772, 18);
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	//	frmTradecoach.getContentPane().add(statusPanel);
		getFrmTradecoach().getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setPreferredSize(new Dimension(getFrmTradecoach().getWidth(), 16));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel("Project 1");
		statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);
		
		JSeparator separator_9 = new JSeparator();
		statusPanel.add(separator_9);
		
		JProgressBar progressBar = new JProgressBar();
		statusPanel.add(progressBar);
		
		JSeparator separator_10 = new JSeparator();
		statusPanel.add(separator_10);
		
		lblSecurityCount = new JLabel("Security count = 0");
		lblSecurityCount.setFont(new Font("Tahoma", Font.PLAIN, 10));
		statusPanel.add(lblSecurityCount);
		
		JPanel panel = new JPanel();
		panel.setBounds(782, -19, 170, 614);
		getFrmTradecoach().getContentPane().add(panel);
		
		Component verticalGlue = Box.createVerticalGlue();
		panel.add(verticalGlue);
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		
	panel.getHeight();
		int adCount=8,adCountV=8;;
		int adHeight=87*2/3;
		int y=0;//panel.getY();
	//	int spacer = (pHeight-(adHeight*adCountV))/(adCountV-2);
		String[] aURLs = new String[adCount];
		aURLs[0]="http://ad.wsodcdn.com/a5878a3d6f2be40db26311f6f8fb21a3/tda_general_value_moretoolslessfeeshd_nooffer_whytda_180x60_default_button_v2.jpg";
		aURLs[1]="http://cdn.flashtalking.com/xre/145/1454701/1177532/image/1177532.gif?682473939";
		aURLs[2]="http://img-cdn.mediaplex.com/0/12948/Jr_GoldMiners_180x60.gif";
		aURLs[3]="http://www.tfnn.com/_images/eSignal-TFNN-180x60.png";
		aURLs[4]="http://www.tfnn.com/images/ibd180x60.gif";
		aURLs[5]="http://www.tfnn.com/_images/teucrium_091515.png";
		aURLs[6]="http://tfnn.com/OX/www/delivery/avw.php?zoneid=26&cb=INSERT_RANDOM_NUMBER_HERE&n=a023a951";
		aURLs[7]="http://tfnn.com/OX/www/delivery/avw.php?zoneid=4&cb=INSERT_RANDOM_NUMBER_HERE&n=ae8f3f54";
		String[] aHyperlinks = new String[adCount];
		aHyperlinks[0]="http://tdameritrade.demdex.net/event?d_event=click&d_creative=1883&d_rd=https://ad.doubleclick.net/ddm/clk/295776671;122645400;j%3fhttp://ad.wsod.com/click/a5878a3d6f2be40db26311f6f8fb21a3/1596.1883.js.180x60.1446513345.B34365636b01304c02.1641.__312/**;19;1366x768x1;http:_@2F_@2Fwww.tfnn.com_@2F;;";
		aHyperlinks[1]="http://servedby.flashtalking.com/click/8/47521;1454701;1177532;210;0/?ft_impID=2863345B2FBD3A&g=2803368A418925&random=[FT_RANDOM]&ft_width=180&ft_height=60&url=https://www.everbank.com/investing/marketsafe/metals-hedge?c3ch=Agora&c3nid=TFNN-5447_05_15ACQ0046_15_WebBanner180x60_FT.jpg-TFNN180x60&c3creative=5447_05_15ACQ0046_15_WebBanner180x60_FT.jpg&c3size=180x60&c3placement=TFNN180x60&referID=12924&cm_mmc=12924";
		aHyperlinks[2]="http://altfarm.mediaplex.com/ad/nc/12948-118783-28577-3?mpt=1446513343";
		aHyperlinks[3]="http://www.esignal.com/ads/esignal/market-screener1.aspx?CPID=KNC-TFNN-BannerAD-1014&tc=15427";
		aHyperlinks[4]="http://www.investors.com/?tfnn";
		aHyperlinks[5]="http://www.tfnn.com/teucrium_redirect.php?pid=corn";
		aHyperlinks[6]="http://tfnn.com/OX/www/delivery/ck.php?n=a023a951&cb=INSERT_RANDOM_NUMBER_HERE";
		aHyperlinks[7]="http://tfnn.com/OX/www/delivery/ck.php?n=ae8f3f54&cb=7800789078907987890789797907987";
		int i;
		//adCountV=5;
		for(i=0;i<adCountV;i++){
			final int j = i;
		    y+=0;//adHeight;//+(i==0?0:spacer);
		//	y+=91;
			JPanel panel2 = new JPanel();
		//	System.out.printf("x=%d, y=%d, w=%d, l=%d\n", panel.getX(), y, panel.getWidth(), adHeight);
			panel2.setBounds( panel.getX(), y, panel.getWidth(), adHeight);
		//	frmTradecoach.getContentPane().add(panel2);
			panel.add(panel2);
			JLabel label = new JLabel("");
			String path = aURLs[i];
			label.setIcon(this.getWebImage(panel2, path));
			panel2.add(label);
			label.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent arg0) {				
					try {
						Desktop.getDesktop().browse(new URI(aHyperlinks[j]));
					} catch (IOException | URISyntaxException e) {
						
						e.printStackTrace();
					}
				}
			});
//			Component verticalGlue1 = Box.createVerticalGlue();
//			verticalGlue1.setBounds(0, 0, 1, 1);
//			panel.add(verticalGlue1);
		}
		JMenuBar menuBar = new JMenuBar();
		getFrmTradecoach().setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New Project");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String s = (String)JOptionPane.showInputDialog(
									getFrmTradecoach(),
				                    "Enter a name for the new project",
				                    "Customized Dialog",
				                    JOptionPane.PLAIN_MESSAGE,
				                    iconHat,
				                    null,//null result in textbox instead of combobox
				                    "");		
				if ((s != null) && (s.length() > 0)) {
					if(portfolios.getPortfoliosTable().projectNameAlreadyExists(s)){
						JOptionPane.showMessageDialog(getFrmTradecoach(),
							    String.format("Project with name '%s' already exists.\n  No new project was created", s),
							    "Inane warning",
							    JOptionPane.ERROR_MESSAGE);
						
					} else {
						try {
							setCurrentProject(s);
							Portfolio p = new Portfolio();
							p.setPortfolioName(getCurrentProject());
							portfolios.getPortfoliosTable().savePortfolioInfo(p);
							statusLabel.setText(getCurrentProject());							
							portfolios.getParametersTable().addMRU(s);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					    return;
					}

				} else {
					JOptionPane.showMessageDialog(getFrmTradecoach(),
						    "Project 'Name' can not be blank.\n  No new project was created",
						    "Inane warning",
						    JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpenProject = new JMenuItem("Open Project");
		mntmOpenProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			//	Object[] possibilities = {"Default Portfolio", "spam", "yam"};
				Object[] possibilities = null;
				try {
					possibilities = portfolios.getPortfoliosTable().getProjectNameList();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				String s = (String)JOptionPane.showInputDialog(
									getFrmTradecoach(),
				                    "Select the Project to be openned",
				                    "Customized Dialog",
				                    JOptionPane.PLAIN_MESSAGE,
				                    iconHat,
				                    possibilities,
				                    "Default Portfolio");		
				if ((s != null) && (s.length() > 0)) {
					try {
						setCurrentProject(s);
						statusLabel.setText(s);					
						portfolios.getParametersTable().addMRU(s);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				    return;
				}
			}
		});
		mnFile.add(mntmOpenProject);
		
		JMenuItem mntmCloseProject = new JMenuItem("Close Project");
		mnFile.add(mntmCloseProject);
		
		JMenuItem mntmDeleteProject = new JMenuItem("Delete Project");
		mntmDeleteProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Object[] possibilities = null;
				try {
					possibilities = portfolios.getPortfoliosTable().getProjectNameList();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				String s = (String)JOptionPane.showInputDialog(
						getFrmTradecoach(),
						"Select the Project to be deleted",
						"Customized Dialog",
						JOptionPane.PLAIN_MESSAGE,
						iconHat,
						possibilities,
						"");		
				if ((s != null) && (s.length() > 0)) {
					if (s.compareToIgnoreCase("DEFAULT PORTFOLIO")==0) {
						JOptionPane.showMessageDialog(getFrmTradecoach(),
								String.format("'Default Portfolio' can not be deleted", s),
								"Inane warning",
								JOptionPane.ERROR_MESSAGE);
						return;
					} else if (s.compareToIgnoreCase(getCurrentProject())==0)	{					
						setCurrentProject("Default Portfolio");
						statusLabel.setText("Default Portfolio");
					}						
					//	statusLabel.setText(s);
					Portfolio p = new Portfolio();					
					p.setPortfolioName(s);
					portfolios.getPortfoliosTable().deletePortfolioInfo(p, getFrmTradecoach());
				}
				return;
			}	
		});
		mnFile.add(mntmDeleteProject);
		
		JSeparator separator_5 = new JSeparator();
		mnFile.add(separator_5);
		
		JMenu menu = new JMenu("Import Data");
		mnFile.add(menu);
		
		JMenuItem mntmLoadTradeData = new JMenuItem("Load Trade Data from csv file");
		menu.add(mntmLoadTradeData);
		mntmLoadTradeData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {			
				portfolios.importTradeHistoryDataFromCSVwithDELETE();
			}
		});
		
		JMenuItem mntmAppendDataTo = new JMenuItem("Append Data to Existing Project from csv file");
		menu.add(mntmAppendDataTo);
		
		JMenuItem mntmLoadTradeDataaExcel = new JMenuItem("Load Trade Data from Excel");
		menu.add(mntmLoadTradeDataaExcel);
		
		JMenuItem mntmAppendDataTo_1 = new JMenuItem("Append Data to an Existing Project from Excel");
		menu.add(mntmAppendDataTo_1);
		mntmAppendDataTo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				portfolios.importTradeHistoryDataFromCSV();
			}
		});
		
		
		JMenu menu_1 = new JMenu("Export Data To Other Applications");
		mnFile.add(menu_1);
		
		JMenuItem menuItem_3 = new JMenuItem("Export to Excel");
		menu_1.add(menuItem_3);
		
		JMenuItem menuItem_4 = new JMenuItem("Export to Google Sheets");
		menu_1.add(menuItem_4);
		
		JMenuItem menuItem_5 = new JMenuItem("Export Text File");
		menu_1.add(menuItem_5);
		
		JSeparator separator_3 = new JSeparator();
		mnFile.add(separator_3);
		
		JMenuItem mntmPageSetup = new JMenuItem("Page Setup");
		mnFile.add(mntmPageSetup);
		
		JMenuItem mntmPrint = new JMenuItem("Print");
		mnFile.add(mntmPrint);
		
		JMenuItem mntmPrintPreview = new JMenuItem("Print Preview");
		mnFile.add(mntmPrintPreview);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		setMnRecentProjects(new JMenu("Recent Projects"));
		mnFile.add(getMnRecentProjects());
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("New menu item");
		mntmNewMenuItem_1.addMenuKeyListener(new MenuKeyListener() {
			public void menuKeyPressed(MenuKeyEvent arg0) {
			}
			public void menuKeyReleased(MenuKeyEvent arg0) {
			}
			public void menuKeyTyped(MenuKeyEvent arg0) {
			}
		});
		getMnRecentProjects().add(mntmNewMenuItem_1);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mntmSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JDialog f = new Settings(gui);
				f.setLocationRelativeTo(getFrmTradecoach()); //relativeTo is the name of parent frame
			    f.show();
			}
		});
		mnEdit.add(mntmSettings);
		
		JSeparator separator_11 = new JSeparator();
		mnEdit.add(separator_11);
		
		JMenuItem mntmUndo = new JMenuItem("Undo");
		mnEdit.add(mntmUndo);
		
		JSeparator separator_12 = new JSeparator();
		mnEdit.add(separator_12);
		
		JMenuItem mntmCut = new JMenuItem("Cut");
		mnEdit.add(mntmCut);
		
		JMenuItem mntmCopy = new JMenuItem("Copy");
		mnEdit.add(mntmCopy);
		
		JMenuItem mntmPaste = new JMenuItem("Paste");
		mnEdit.add(mntmPaste);
		
		JSeparator separator_13 = new JSeparator();
		mnEdit.add(separator_13);
		
		JMenuItem mntmFind = new JMenuItem("Find");
		mnEdit.add(mntmFind);
		
		JMenu mnData = new JMenu("Data");
		menuBar.add(mnData);
		
		JMenuItem mntmHarvestInterimDay = new JMenuItem("Harvest Interim Day Price Data");
		mnData.add(mntmHarvestInterimDay);
		
		JMenuItem mntmEnterNewSecurities = new JMenuItem("Get Advice of Specific Trade");
		mntmEnterNewSecurities.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JDialog f = new NewTradeEntry(getFrmTradecoach(), gui);
				f.setLocationRelativeTo(getFrmTradecoach()); //relativeTo is the name of parent frame
			    f.show();
			}
		});
		
		JMenuItem mntmUpdateHistoricalPrice = new JMenuItem("Update Historical Price Data");
		mntmUpdateHistoricalPrice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				portfolios.getInitialPortfolio().loadHistoricalPriceData();
			}
		});
		mnData.add(mntmUpdateHistoricalPrice);
		mnData.add(mntmEnterNewSecurities);
		
		JMenu mnAnalyze = new JMenu("Analyze");
		menuBar.add(mnAnalyze);
		
		setMiSolveRoiFor(new JMenuItem("Solve ROI for my current Portfolio"));
		getMiSolveRoiFor().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadInitPortfolio();
			}
		});
		mnAnalyze.add(getMiSolveRoiFor());
		
		JMenu mnFindBestStrategy = new JMenu("Find Best Strategy");
		mnAnalyze.add(mnFindBestStrategy);
		
		setMiFindBestStrategy(new JMenuItem("Maximum ROI"));
		getMiFindBestStrategy().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findBestStrategy();
			}
		});
		mnFindBestStrategy.add(getMiFindBestStrategy());
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmContents = new JMenuItem("Contents");
		mnHelp.add(mntmContents);
		
		JMenuItem mntmIndex = new JMenuItem("Index");
		mnHelp.add(mntmIndex);
		
		JMenuItem mntmContactingTradecoach = new JMenuItem("Contacting TradeCoach");
		mnHelp.add(mntmContactingTradecoach);
		
		JSeparator separator_4 = new JSeparator();
		mnHelp.add(separator_4);
		
		JMenuItem mntmAbout = new JMenuItem("About TradeCoach");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JDialog f = new AboutDialog(getFrmTradecoach());
				f.setLocationRelativeTo(getFrmTradecoach()); //relativeTo is the name of parent frame
			    f.show();
			}
		});
		mnHelp.add(mntmAbout);
        if(sendOutput2Display) {
        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);
        }
		loadTables();        
	}
	
	public static void addDays(Date d, int days)
	{
	    Calendar c = Calendar.getInstance();
	    c.setTime(d);
	    c.add(Calendar.DATE, days);
	    d.setTime( c.getTime().getTime() );
	}
	
	public String getFileName() {
		String filename = "C:\\Users\\Phil\\Google Drive\\Stock Market\\Trades2.csv";
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileNameExtensionFilter("text files", "txt", "text"));
		fc.setFileFilter(new FileNameExtensionFilter("Excel files ", "xls", "xlxs",  "xlxm"));
		fc.setFileFilter(new FileNameExtensionFilter("comma-seperated value file", "csv"));
		fc.setSelectedFile(new File(filename));
		int val = fc.showOpenDialog(getFrmTradecoach());
	//	int val = fc.showOpenDialog(null);
		if (val == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile().getPath();
		} else
			return null;
	} //getFileName
	
    private void loadInitPortfolio() {
    	gui.setButtonState(false);
    	gui.setProcessingOriginalList(true);
    	if( tickerFile==null) prompt4Filename("initial portfolio ticker file");
    	this.textArea.setText(null);
		this.createStyleSheet();
    	try {
    		Thread thread = new Thread(new Runnable() {			

				@Override
    			public void run() {
    			//	System.out.println("Time now is " + (new Date()));
    			//	double start = System.currentTimeMillis();
					//Tools.carriageReturn();
					//Tools.drawSeprator();
					//Tools.carriageReturn();
					portfolios.setHtmlTextPane("<h1>Results of My Trading Activities</h1>");
					portfolios.appendHtmlTextPane(String.format("<i>%s</i><br><br>",dfLongDateAndTime.format(new Date())));
					portfolios.appendHtmlTextPane(String.format("<div id=\"ROI\">Portfolio Weighted Average ROI: <span style=\"color:red\">%s</span></div>",getRoiPlugString()));
					if(portfolios.getInitialPortfolio().isAlreadyRanMyResults()){
						portfolios.getInitialPortfolio().reset4MyResultsRerun();
					//	getMyResultsTextPane().setText("");
				//		portfolios.setHtmlMyResultsTextPane("");
					}
					portfolios.getInitialPortfolio().setAlreadyRanMyResults(true);
					portfolios.getInitialPortfolio().executeOrders();
					
					SwingUtilities.invokeLater(new Runnable() {    						
						@Override
						public void run() {	
							try {
								portfolios.insertMyResultsROI();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					gui.getMyResultsTextPane().setCaretPosition(0);
					//Tools.carriageReturn(3);
					//double end = System.currentTimeMillis();
					//double duration = (end - start) / 1000;
					//System.out.println("Initial portfolio securities took " + duration + " seconds.");
					getBtnFindBestStrategy2().setEnabled(true);
					getBtnSolveMyROI2().setEnabled(true);
				//	frmTradecoach.getContentPane()..lblSecurityCount
					
			//		btnSolveMyROI2.setText("ReSolve");
    			}
    		});
    		thread.start();	    		
    	} //try
    	catch(Exception ee) {
    		btnDump.setEnabled(false);
    		textArea.append("FAILED: Initial portfolio securities load.\n\n" );
    	} //catch
    	finally {
        	gui.setButtonState(true);
    	}
    } //loadInitPortfolio

	private void setButtonState(boolean state) {
    	gui.getMiFindBestStrategy().setEnabled(state);
    	gui.getMiSolveRoiFor().setEnabled(state);
    	gui.getBtnSolveMyROI2().setEnabled(state); 
    	gui.getBtnFindBestStrategy2().setEnabled(state);		
	}
	private void createStyleSheet() {
	//	styleSheet = new StyleSheet();
		styleSheet = htmlEditorKit.getStyleSheet();
		styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
		styleSheet.addRule("h1 {color: #ff0000;}");
		styleSheet.addRule("h2 {color: #ff0000;}");
		//styleSheet.addRule("th { border: 2px solid #ff0000; }");//text-decoration: underline;
		styleSheet.addRule("th { border-bottom:1px; }");
	//	styleSheet.addRule("td { border: 2px solid #ff0000; text-align: center; }");
		styleSheet.addRule("td { text-align: center; }");
		styleSheet.addRule("pre {font : 10px monaco; color : black; background-color : #fafafa; }");
	//	Document doc = kit.createDefaultDocument();
		htmlEditorKit.setStyleSheet(styleSheet);
		htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
		tpSenarios.setDocument(htmlDocument);
	}
        
    private void loadTables() {
    //	button_1.setEnabled(false);   	
   // 	btnFindBestStrategy.setEnabled(false);
  //  	if( tickerFile==null) prompt4Filename("initial portfolio ticker file");
    	this.textArea.setText(null);
    	try {
    		Thread thread = new Thread(new Runnable() {
    			@Override
    			public void run() {
    				System.out.println("Time now is " + (new Date()));
    				try {
    					double start = System.currentTimeMillis();
    					Thread.sleep(1000);
    					
    					SwingUtilities.invokeLater(new Runnable() {    						
    						@Override
    						public void run() {	textArea.append("Building Initial Portfolio\n");		}
    					});
   	
    					portfolios.setBelongsToGUI(gui);
    				//	portfolios.buildTableInstances(propertiesfile);
    					portfolios.buildTableInstances();
    					portfolios.intialize(filename);
    					portfolios.getTradeHistoryTable().setTableGUI(tableMyTrades);
    					portfolios.getHistoricalPricesTable().setTableGUI(tableHistoricalPrices);
    					//HistoricalPricesTableModel modelHistoricalPrices = new HistoricalPricesTableModel(portfolios.getHistoricalPricesTable());
    					DefaultTableModel modelHistoricalPrices = new DefaultTableModel(portfolios.getHistoricalPricesTable().getColumnHeader(), 0);
    					//HistoricalPricesTableModel modelHistoricalPrices = new HistoricalPricesTableModel(portfolios.getHistoricalPricesTable().getColumnHeader(), 0);
    					//MyTableModel modelMyTrades = new MyTableModel(portfolios.getTradeHistoryTable().getColumnHeader(), 0);
    					MyTableModel modelMyTrades = new MyTableModel(portfolios.getTradeHistoryTable());
    					modelMyTrades.setBelongsToGUI(gui);
    			//		SecuritiesTableModel modelSecurities = new SecuritiesTableModel(portfolios.getSecurityInstTable().getColumnHeader(), 0);
						portfolios.getSecurityInstTable().setTableGUI(tableSecurities);
    					SecuritiesTableModel modelSecurities = new SecuritiesTableModel(portfolios.getSecurityInstTable());
    			//		Object[] cc = {"Select","Portfilio ID","Symbol","Name","Beta"};
    				//	DefaultTableModel modelSecurities = new DefaultTableModel(cc,0);
    			//		SecuritiesTableModel modelSecurities = new SecuritiesTableModel(cc,0);
    				//	Tools.carriageReturn();
    				//	Tools.drawSeprator();
    				//	Tools.carriageReturn();
    					SwingUtilities.invokeLater(new Runnable() {
    						@Override
    						public void run() {
    							try {
    							//	portfolios.getTradeHistoryTable().setTabelModel(modelMyTrades);
    								portfolios.getTradeHistoryTable().viewTable(modelMyTrades);
    								tableMyTrades.setModel(modelMyTrades);
    							//	modelMyTrades.initialize();
    								portfolios.getHistoricalPricesTable().viewTable(modelHistoricalPrices);
    								tableHistoricalPrices.setModel(modelHistoricalPrices);
    								portfolios.getSecurityInstTable().viewTable(modelSecurities);
    								tableSecurities.setModel(modelSecurities);    								
    								
    							    TableColumn tc = tableSecurities.getColumnModel().getColumn(0);
    							    tc.setCellEditor(tableSecurities.getDefaultEditor(Boolean.class));
    							    tc.setCellRenderer(tableSecurities.getDefaultRenderer(Boolean.class));
    							    tc.setHeaderRenderer((TableCellRenderer)portfolios.getSecurityInstTable().getTableMap().get(0).getColumnHeader());
    							  //  tc.getHeaderRenderer().getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5)
    							    //   tc.setHeaderRenderer(new CheckBoxHeader(new MyItemListener()));
    							    tableSecurities.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    							    tableSecurities.getColumnModel().getColumn(0).setPreferredWidth(portfolios.getSecurityInstTable().getColumnTableWidthAtPosition(0));
    							    tableSecurities.getColumnModel().getColumn(1).setPreferredWidth(portfolios.getSecurityInstTable().getColumnTableWidth("Symbol"));
    							    tableSecurities.getColumnModel().getColumn(2).setPreferredWidth(portfolios.getSecurityInstTable().getColumnTableWidth("Name"));
    							    tableSecurities.getColumnModel().getColumn(3).setPreferredWidth(portfolios.getSecurityInstTable().getColumnTableWidth("Beta"));
    							 //   tableSecurities.getColumnModel().getColumn(4).setPreferredWidth(portfolios.getSecurityInstTable().getColumnIndexFromHeader("Symbol"));

    							    TableColumn tc2 = tableMyTrades.getColumnModel().getColumn(0);
    							    tc2.setCellEditor(tableMyTrades.getDefaultEditor(Boolean.class));
    							    tc2.setCellRenderer(tableMyTrades.getDefaultRenderer(Boolean.class));
    							    tc2.setHeaderRenderer((TableCellRenderer)portfolios.getTradeHistoryTable().getTableMap().get(0).getColumnHeader());
    						    	
    							    int[] colLengths={25,55,65,355,100,65,65,65,105,65};
    							    for(int i=0; i<colLengths.length; i++)  
    							    	tableMyTrades.getColumnModel().getColumn(i).setPreferredWidth(colLengths[i]);
    							    
    							    SwingUtilities.invokeLater(new Runnable() {    						
    		    						@Override
    		    						public void run() {	
    		    							try {
    		    								lblSecurityCount.setText(String.format("Security count = %s",tableSecurities.getRowCount()));
    		    							} catch (Exception e) {
    		    								e.printStackTrace();
    		    							}
    		    						}
    		    					});
    							    
    								int q = portfolios.getTradeHistoryTable().getColumnIndexFromHeader("Stp Type")-0;
    								setOrderTypeColumn(tableMyTrades, tableMyTrades.getColumnModel().getColumn(q));//set combo box combobox
    							    modelSecurities.setAllignment();    							    
    							    modelMyTrades.setAllignment();
        							//    ((TableParentModel) modelHistoricalPrices).setAllignment()
    							} catch (SQLException e) {
    								e.printStackTrace();
    							}
    						}
    					});    					
    					double end = System.currentTimeMillis();
    		    		double duration = (end - start) / 1000;
    					System.out.println("Initial portfolio securities took " + duration + " seconds.");
     				} catch (InterruptedException ex) {
    					ex.printStackTrace();
    				}
    			}
    		});
    		thread.start();	    		
    	} //try
    	catch(Exception ee) {
    		textArea.append("FAILED: Initial portfolio securities load.\n\n" );
    	} //catch
    } //loadInitPortfolio

    private void findBestStrategy() {
    	gui.setProcessingOriginalList(false);
    	try {
    		Thread thread = new Thread(new Runnable() {
    			@Override
    			public void run() {
    				System.out.println("Time now is " + (new Date()));
    				try {
    					double start = System.currentTimeMillis();
    					Thread.sleep(1000);
    					//System.out.println("\nStarting Portfolio strategy optimizer\n");
    					portfolios.setHtmlTextPane("<h1>Test Scenarios for ROI Maximization</h1>");
    					portfolios.appendHtmlTextPane(String.format("<i>%s</i><br><br>",dfLongDateAndTime.format(new Date())));
    					portfolios.appendHtmlTextPane(String.format("<div id=\"ROI\">Best Resulting Weighted Average ROI: <span style=\"color:red\">%s</span></div>",getRoiPlugString()));

    			//		if(portfolios.getInitialPortfolio().isAlreadyRanMyResults()){
    				//		portfolios.getInitialPortfolio().reset4MyResultsRerun();
    					//	getMyResultsTextPane().setText("");
    				//		portfolios.setHtmlMyResultsTextPane("");
    				//	}
    					portfolios.getInitialPortfolio().setAlreadyRanMyResults(true);
    					int scenario2BCreated=5;
    					int layers2BCreated = 5;
    					double firstTriggerPctIncrease=0.04f;
    					double firstTrailingStpPct=0.05f;
    					portfolios.setScenario2BCreated(scenario2BCreated);
    					portfolios.setLayers2BCreated(layers2BCreated);
    					portfolios.setFirstTrailingStpPct(firstTrailingStpPct);
    					portfolios.setFirstTriggerPctIncrease(firstTriggerPctIncrease);
    					
    					portfolios.buildCandidatePortfoliosForMaxROI();
    					SwingUtilities.invokeLater(new Runnable() {    						
    						@Override
    						public void run() {	
    							try {
    								portfolios.insertScenariosROI();
    							} catch (Exception e) {
    								e.printStackTrace();
    							}
    						}
    					});
    					
    					portfolios.buildCandidatePortfoliosForMaxProfit();
    					portfolios.critiqueTrades();
    					portfolios.buildCandidatePortfoliosKnapsack();
    					double end = System.currentTimeMillis();
    		    		double duration = (end - start) / 1000;
    					System.out.println("Find the optimal strategy took " + duration + " seconds.");
    				} catch (InterruptedException ex) {
    					ex.printStackTrace();
    				}
    			}
    		});
    		thread.start();	
    	} //try
    	catch(Exception ee) {
    	//	btnCorralateCandidates2.setEnabled(false);
    		btnDump.setEnabled(false);
    		textArea.append("FAILED: Initial portfolio securities load.\n\n" );
    	} //catch
    } //loadInitPortfolio
    
	public void buildCandidatePortfoliosForMaxROI(){
		double start = System.currentTimeMillis();		
		try {
			textArea.append("Determing most optimal stragey for maximum ROI\n\n" );
			portfolios.buildCandidatePortfoliosForMaxROI();
			System.out.println(portfolios.toString2());
			textArea.setText(null);
			textArea.append(portfolios.toString2());
			this.setCandidateUnivLoaded(true);
			double end = System.currentTimeMillis();
			double duration = (end - start) / 1000;
			System.out.println("Optimal strategy successfully determined" + duration + " seconds.");
			textArea.append("Optimal strategy successfully determined\nDetermination took " + duration + " seconds.\n" );
		} //try
		catch(Exception ee) {
			this.setCandidateUnivLoaded(false);
		//	btnCorralateCandidates2.setEnabled(false);
			btnDump.setEnabled(false);
			textArea.append("FAILED: Determination of optimal strategy.\n\n" );
		} //catch
	    finally {
	    	textArea.append("*****************************************************************\n\n" );
	    	System.out.println("*****************************************************************\n\n" );
		} //finally
	} //loadCandidateUniverse
	
		
	public void loadCandidateUniverse(){
		double start = System.currentTimeMillis();		
		if( tickerUFile==null) prompt4Filename("candidate securities tickers file");
		try {
		//	Thread t = new Thread(p);
		//	t.start();
		//	t.join();
			textArea.append("Loading list of candidate securities for potential hedge\n\n" );
//			p.setCandidateUniverse(tickerUFile2.getText());
			textArea.append("Loading historical prices for candidate securites\n\n" );
			portfolios.loadPriceDataUniverse();
		//	textArea.append("Candidate securities successfully loaded\n\n" );
			
			this.setCandidateUnivLoaded(true);
			if(this.isInitPortfolioLoaded()) {
		//		btnCorralateCandidates2.setEnabled(true);
				btnDump.setEnabled(true);
			}
			double end = System.currentTimeMillis();
			double duration = (end - start) / 1000;
			System.out.println("Candidate securities successfully loaded " + duration + " seconds.");
			textArea.append("Candidate securities successfully loaded.\nloading took " + duration + " seconds.\n" );
		} //try
		catch(Exception ee) {
			this.setCandidateUnivLoaded(false);
	//		btnCorralateCandidates2.setEnabled(false);
			btnDump.setEnabled(false);
			textArea.append("FAILED: Candidate securities load.\n\n" );
		} //catch
	    finally {
	    	textArea.append("*****************************************************************\n\n" );
	    	System.out.println("*****************************************************************\n\n" );
		} //finally
	} //loadCandidateUniverse
	
	

	public void buildHedge() throws IOException {
		textArea.append("Corralating hedge candidates with your current portfolio\n\n" );
		portfolios.getCandidateUniverse().corralateToOtherPortfolio( portfolios.getInitialPortfolio());
		textArea.append("Building optimal portfolio to minimize VaR\n" );
	//	int l = Integer.parseInt(tfNewLongs.getText());
	//	int s = Integer.parseInt(tfNewShorts.getText());
	//	String newStr = oldStr.replaceAll("[^\\d.]+", "")
	//	Double c = Double.parseDouble(panel_1.tfMaxNewCapital.getText().replaceAll("[^\\d.]+", ""));
	//	p.buildCandidatePortfolios(l, s, c);
	//	p.buildCandidatePortfolios(Integer.parseInt(tfNewLongs.getText()), Integer.parseInt(tfNewShorts.getText()), Double.parseDouble(tfMaxNewCapital.getText()));
		textArea.setText(null);
		textArea.append(portfolios.toString());
	//	((Appendable) textPane).append(p.toString());
    	textArea.append("*****************************************************************\n\n" );
    	System.out.println("*****************************************************************\n\n" );
	}
	
	private void prompt4Filename(String filenameDescription) {
		final JDialog dialog2 = new JDialog();
		dialog2.setBounds(0, 0, 300,300);
		dialog2.getContentPane().setLayout(new FlowLayout());		
			dialog2.getContentPane().add(new JLabel("You must select a " + filenameDescription + " first"));
			JButton jb = new JButton("OK");
			jb.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent e) { dialog2.setVisible(false);	}
				});
			dialog2.getContentPane().add(jb);
			dialog2.setVisible(true);
	}
	
	public JFrame getFrmTradecoach() {
		return frmTradecoach;
	}
	public void setFrmTradecoach(JFrame frmTradecoach) {
		this.frmTradecoach = frmTradecoach;
	}
	private ImageIcon getWebImage(JPanel panel, String path){
		try {
            URL url = new URL(path);
            BufferedImage image = ImageIO.read(url);
    		int width = panel.getWidth();
    		int height = panel.getHeight();
    		Image newimg = image.getScaledInstance(width, height,  Image.SCALE_DEFAULT);
            return new ImageIcon(newimg);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
		return null;
	}
	
	
	public boolean isCandidateUnivLoaded() {
		return candidateUnivLoaded;
	}

	public void setCandidateUnivLoaded(boolean candidateUnivLoaded) {
		this.candidateUnivLoaded = candidateUnivLoaded;
	}

	public boolean isInitPortfolioLoaded() {
		return initPortfolioLoaded;
	}

	public void setInitPortfolioLoaded(boolean initPortfolioLoaded) {
		this.initPortfolioLoaded = initPortfolioLoaded;
	}

	public int getNewShorts() {
		return newShorts;
	}

	public void setNewShorts(int newShorts) {
		this.newShorts = newShorts;
	}

	public int getNewLongs() {
		return newLongs;
	}

	public void setNewLongs(int newLongs) {
		this.newLongs = newLongs;
	}

	public Double getMaxNewCapital() {
		return maxNewCapital;
	}

	public void setMaxNewCapital(Double maxNewCapital) {
		this.maxNewCapital = maxNewCapital;
	}
	
    public JTextArea getTextArea() {
		return textArea;
	}

	public JTable getTable() {
		return tableHistoricalPrices;
	}

	public void setTable(JTable table) {
		this.tableHistoricalPrices = table;
	}

	public GUI getGui() {
		return gui;
	}

	public void setGui(GUI gui) {
		this.gui = gui;
	}

	public Portfolios getPortfolios() {
		return portfolios;
	}

	public void setPortfolios(Portfolios portfolios) {
		this.portfolios = portfolios;
	}

	public JTextPane getTextArea_2() {
		return tpSenarios;
	}

	public void setTextArea_2(JTextPane textArea_2) {
		this.tpSenarios = textArea_2;
	}

	public JTextArea getMyResultsTextArea() {
		return textArea_1;
	}

	public void setMyResultsTextArea(JTextArea textArea) {
		this.textArea_1 = textArea;
	}
	
	public JTextPane getMyResultsTextPane() {
		return tpMyResults;
	}

	public void setMyResultsTextPane(JTextPane tpMyResults) {
		this.tpMyResults = tpMyResults;
	}

	public JTextPane getScenariosTextPane() {
		return tpSenarios;
	}

	public void setScenariosTextPane(JTextPane textArea) {
		this.tpSenarios = textArea;
	}
	
	public JTextPane getAnalysisTextPane() {
		return tpAnalysis;
	}

	public JTextPane getTpNewTrades() {
		return tpNewTrades;
	}

	public void setTpNewTrades(JTextPane tpNewTrades) {
		this.tpNewTrades = tpNewTrades;
	}

	public void setAnalysisTextPane(JTextPane textArea) {
		this.tpAnalysis = textArea;
	}
	public JTextArea getActivityTextArea() {
		return textArea;
	}

	public void setActivityTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}
	
	
	
	/**
     * This class extends from OutputStream to redirect output to a JTextArrea
     * @author www.codejava.net
     *
     */
    public class CustomOutputStream extends OutputStream {
        private JTextArea textArea;
         
        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
         
        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
    
    public class MRU{
    	private MRU priorMRU = null;
    	private MRU nextMRU = null;
    	private String description;
    	MRU(){ }
    	MRU(String description){
    		this.setDescription(description);
    		this.push(this);
    	}
    	public String toString(){
    		return this.getDescription();
    	}
    	public void push(MRU mru){    		
    		if(getTopMRU()==null){
    			setTopMRU(mru);
    		}else{
    			MRU temp = getTopMRU();
    			for(int i=0;i<4;i++){
    				if(temp.getPriorMRU()!=null)
    					temp=temp.getPriorMRU();

    				temp.setPriorMRU(null);
    				mru.setPriorMRU(getTopMRU());
    				getTopMRU().setNextMRU(mru);
    				setTopMRU(mru); 
    			}
    		}
    		this.refreshMenu(getMnRecentProjects());
    	}
    	private void refreshMenu(JMenu menu) {
    		MRU temp = getTopMRU();
    		while (temp.getPriorMRU()!=null){
    			JMenuItem mntmNewMenuItem = new JMenuItem(temp.getDescription());
    			temp=temp.getPriorMRU();
    			menu.add(mntmNewMenuItem);	
    		}
    	}
		public MRU getPriorMRU() {
			return priorMRU;
		}
		public void setPriorMRU(MRU priorMRU) {
			this.priorMRU = priorMRU;
		}
		public MRU getNextMRU() {
			return nextMRU;
		}
		public void setNextMRU(MRU nextMRU) {
			this.nextMRU = nextMRU;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}    	
    }
    
    public void setOrderTypeColumn(JTable table, TableColumn sportColumn) {
        //Set up the editor for the trade type.
        JComboBox comboBox = new JComboBox(com.utilities.GlobalVars.typeOrder.values());       
        sportColumn.setCellEditor(new DefaultCellEditor(comboBox));
        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for a list of supported order types");
        sportColumn.setCellRenderer(renderer);
    }

	/**
	 * Return true if iitial order is still being processed.  Used to set proper verbage
	 * @return
	 */
    public boolean isProcessingOriginalList() {
		return processingOriginalList;
	}

	public void setProcessingOriginalList(boolean processingOriginalList) {
		this.processingOriginalList = processingOriginalList;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public StyleSheet getStyleSheet() {
		return styleSheet;
	}

	public void setStyleSheet(StyleSheet styleSheet) {
		this.styleSheet = styleSheet;
	}

	public HTMLDocument getHtmlDocument() {
		return htmlDocument;
	}

	public void setHtmlDocument(HTMLDocument htmlDocument) {
		this.htmlDocument = htmlDocument;
	}

	public String getCurrentProject() {
		return CurrentProject;
	}
	public void setCurrentProject(String currentProject) {
		CurrentProject = currentProject;
		this.setTopMRU(new MRU(this.getCurrentProject()));
		
	}
	public JTable getTableMyTrades() {
		return tableMyTrades;
	}
	public void setTableMyTrades(JTable tableMyTrades) {
		this.tableMyTrades = tableMyTrades;
	}
	public JTable getTablePortfolos() {
		return tablePortfolos;
	}
	public void setTablePortfolos(JTable tablePortfolos) {
		this.tablePortfolos = tablePortfolos;
	}
	public JMenu getMnRecentProjects() {
		return mnRecentProjects;
	}
	public void setMnRecentProjects(JMenu mnRecentProjects) {
		this.mnRecentProjects = mnRecentProjects;
	}
	public MRU getTopMRU() {
		return topMRU;
	}
	public void setTopMRU(MRU topMRU) {
		this.topMRU = topMRU;
	}
	public Object getRoiPlugString() {
		return roiPlugString;
	}
	public void setRoiPlugString(Object roiPlugString) {
		this.roiPlugString = roiPlugString;
	}
	public JButton getBtnFindBestStrategy2() {
		return btnFindBestStrategy2;
	}
	public void setBtnFindBestStrategy2(JButton btnFindBestStrategy2) {
		this.btnFindBestStrategy2 = btnFindBestStrategy2;
	}
	public JMenuItem getMiSolveRoiFor() {
		return miSolveRoiFor;
	}
	public void setMiSolveRoiFor(JMenuItem mntmSolveRoiFor) {
		this.miSolveRoiFor = mntmSolveRoiFor;
	}
	public JMenuItem getMiFindBestStrategy() {
		return miFindBestStrategy;
	}
	public void setMiFindBestStrategy(JMenuItem miFindBestStrategy) {
		this.miFindBestStrategy = miFindBestStrategy;
	}
	public JButton getBtnSolveMyROI2() {
		return btnSolveMyROI2;
	}
	public JLabel getStatusLabel() {
		return statusLabel;
	}
	public void setStatusLabel(JLabel statusLabel) {
		this.statusLabel = statusLabel;
	}
	public void setBtnSolveMyROI2(JButton btnSolveMyROI2) {
		this.btnSolveMyROI2 = btnSolveMyROI2;
	}
	@Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        String columnName = model.getColumnName(column);
        model.getValueAt(row, column);

    	java.util.Arrays.asList(portfolios.getTradeHistoryTable().getColumnHeader()).indexOf(columnName);
        
        
    }
}
