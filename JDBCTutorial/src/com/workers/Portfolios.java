package com.workers;

import com.gui.GUI;
import com.gui.NewTradeEntry;
import com.oracle.tutorial.jdbc.CoffeesTable;
import com.oracle.tutorial.jdbc.HistoricalPricesTable;
import com.oracle.tutorial.jdbc.JDBCTutorialUtilities;
import com.oracle.tutorial.jdbc.ParametersTable;
import com.oracle.tutorial.jdbc.PortfolioHoldingsTable;
import com.oracle.tutorial.jdbc.PortfoliosTable;
import com.oracle.tutorial.jdbc.SecuritiesTable;
import com.oracle.tutorial.jdbc.TradeHistoryTable;
import com.utilities.BuildScripts;
import com.utilities.GlobalVars;
import com.utilities.GlobalVars.typeOrder;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
 
import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.workers.Portfolios.Pcts4Lyer;

public class Portfolios implements GlobalVars, Runnable{
	private Portfolio initialPortfolio;
	private ArrayList<Portfolio> candidatePortfolios;//potential recommendation hedge
	private Portfolio candidateUniverse, bestCandidatePortfolio;//all stocks to be considered
	private Portfolio testUniverse;//all stocks to be considered
	private MarketCalendar mc;
	private int newShorts, newLongs, idNum, bestScenarioIdNum;
	private Stack<Pcts4Lyer> Scenarios;
	private Stack<Pcts4Lyer> Scenarios2;
	private Pcts4Lyer scenariosHolderClass;
	private Pcts4Lyer currentScenario;
	private GUI belongsToGUI;
	private int scenario2BCreated=5;
	private int layers2BCreated = 5;
	private double firstTriggerPctIncrease=0.04f;
	private double firstTrailingStpPct=0.05f;
	private boolean runCurrent = GlobalVars.RUN_CONCURRENT;
	private CountingSemaphore orderSem = new CountingSemaphore();
	private Lock coutLock;
	private SecuritiesTable securitiesTable;
	private HistoricalPricesTable historicalPricesTable;
	private TradeHistoryTable tradeHistoryTable;
	private PortfoliosTable portfoliosTable;
	private PortfolioHoldingsTable portfolioHoldingsTable;
	private ParametersTable parametersTable;
	private String htmlTextPane;
	private JDBCTutorialUtilities myJDBCTutorialUtilities;
	private Connection myConnection;
	private Boolean refreshOnStart=true,useNativeJDBC=false;
	private static SessionFactory factory; 

	public Portfolios()  {}
	/**creates a <b>DataLoader</b> instance from the <i>csv</i> file represented by <i>fileName</i>.  An initial <b>Portfilio</b> 
	 * object is created and populated with historial data.
	 * @param fileName
	 * @throws IOException 
	 * @throws InvalidPropertiesFormatException 
	 * @throws FileNotFoundException 
	 * @throws SQLException 
	 */
	public Portfolios(String propertiesFileName) throws FileNotFoundException, InvalidPropertiesFormatException, IOException, SQLException {
		this.setMyJDBCTutorialUtilities(new JDBCTutorialUtilities(propertiesFileName));
		this.setMyConnection(myJDBCTutorialUtilities.getConnection());
	//	this.intialize(fileName);
	}//Portfolios
	public Portfolios(Date startDate, Date endDate, String fileName) {
		//make a list of all days in time sequence (even weekends)
		mc = new MarketCalendar(startDate, endDate);
		this.setInitialPortfolio(fileName);
		this.loadPriceDataInitPort();
	}//Portfolios
	public Portfolios(Date startDate, Date endDate, List<MoneyMgmtStrategy> list) {
		//make a list of all days in time sequence (even weekends)
		mc = new MarketCalendar(startDate, endDate);
		this.initialPortfolio = new Portfolio(mc, list, 0);
		this.loadPriceDataInitPort();
	}//Portfolios
	public Portfolios(String filename, String propertiesFileName, GUI gui) {
		try {
			this.setBelongsToGUI(gui);
			this.setMyJDBCTutorialUtilities(new JDBCTutorialUtilities(propertiesFileName));
			this.setMyConnection(myJDBCTutorialUtilities.getConnection());
			buildTableInstances();
			this.intialize(filename);
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		}
	}
	public void intialize(String fileName) {		
		try {
			if(!this.getUseNativeJDBC())
				this.setFactory(this.initHibernate());
			//System.out.println("Creating Data Loader instance\n");
			this.appendToTextArea("Creating Data Loader class instance\n");
			this.setInitialPortfolio(fileName);
			//	System.out.println("\n\nExtracting online price date from Yahoo Finance");
			this.appendToTextArea("Extracting online price date from Yahoo Finance\n");
			this.loadPriceDataInitPort();
			coutLock = new Lock(this);
		} catch (Exception e) {
			System.out.println("Error occurred in intializing");
		}
	}
	public String toString2() {
		String s = 
				"                                                   Variance\n" +
						"                            Historical            Co-Variance\n" +
						"                              Method                Method\n" +
						"                           ------------            -----------\n" +
						"Existing Portfolio:\n" +
						"    VaR @ 95% confidence:  " + ef.format(initialPortfolio.getaVaRs().get(typeVaR.HistVaR95))  + "             "
						+ ef.format(initialPortfolio.getaVaRs().get(typeVaR.VarCoVaR95)) + "\n" +
						"    VaR @ 99% confidence:  " + ef.format(initialPortfolio.getaVaRs().get(typeVaR.HistVaR99))  + "             "
						+ ef.format(initialPortfolio.getaVaRs().get(typeVaR.VarCoVaR99)) + "\n" +
						"\n\nRecommended Portfolio:\n" ;
		if (candidatePortfolios.size() > 0) {
			int x = candidatePortfolios.size()-1;
			s +=
					"    VaR @ 95% confidence:  " + ef.format(candidatePortfolios.get(x).getaVaRs().get(typeVaR.HistVaR95)) + "             "
							+ ef.format(candidatePortfolios.get(x).getaVaRs().get(typeVaR.VarCoVaR95)) + "\n" +
							//             + "n/a\n" +
							"    VaR @ 99% confidence:  " + ef.format(candidatePortfolios.get(x).getaVaRs().get(typeVaR.HistVaR99)) + "             "
							+ ef.format(candidatePortfolios.get(x).getaVaRs().get(typeVaR.VarCoVaR99)) + "\n\n";
			//	+ "n/a\n\n";

			int iFrom  = initialPortfolio.getSecurityCount() + 1 ;	
			int iTo = candidatePortfolios.get(x).getHoldingSet().size() - 1;
			s += candidatePortfolios.get(x).toStringPrintSecuritiesAt(iFrom, iTo );
		} // if
		else s += "     No candidate portfilio could be found"; 
		//if
		return s;
	}
	public String toString() {
		String s = "Initial Portfolio:\n\n";
		s+=this.getInitialPortfolio().toString();
		s+="\n\n*********************************************\n\n";
		s+= "Recommended Portfolio:\n\n";
//		if (candidatePortfolios.size() > 0) {
//			int x = candidatePortfolios.size()-1;
//			s+=this.getCandidatePortfolios().get(x).toString();
//		}//if
		s+=this.getBestCandidatePortfolio().toString();
		return  s;
	}
	private SessionFactory initHibernate(){
		try{
			return  new Configuration().configure().buildSessionFactory();
		}catch (Throwable ex) { 
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex); 
		}
	}
	
	public void buildCalendar(Date startDate, Date endDate) {
		//make a list of all days in time sequence (even weekends)
		mc = new MarketCalendar(startDate, endDate);
	}//buildCalendar
	@SuppressWarnings("unchecked")
	public void setInitialPortfolio(List<String> list, List<Integer> positions) {
		//create the initial portfilio from the tickers supplied by the user
		this.initialPortfolio = new Portfolio(mc, list, positions);
	}
	/**
	 * Create the initial <b>Portfolio</b> instance from the tickers supplied by the user
	 * @param fileName
	 */
	@SuppressWarnings("unchecked")
	public void setInitialPortfolio(String fileName) {
		//create the initial portfilio from the tickers supplied by the user
		this.initialPortfolio = new Portfolio(fileName, this);
	}

	/**
	 * This loads the daily price data of each <b>SecurityInst</b> of the <b>initialPortfolio</b> instance into a individual
	 * <b>Candlestick</b> object.
	 * 
	 * @return boolean
	 */
	public boolean loadPriceDataInitPort(){
		try {
			/*
			 * This loads the daily price data of each security into a individual
			 * candlestick object.
			 */		
			//get to daily price for each ticker symbol from Yahoo
			initialPortfolio.loadHistoricalPriceData();
			this.getSecurityInstTable().saveAllSecuritiesInfo();
			//sum the total change in portfolio value by date
			//initialPortfolio.sumPL_by_Date();
			//initialPortfolio.doVarianceCovariance();
			return true;
		} catch (Exception e) {
			
			e.printStackTrace();

		}
		return false;
	}

	public void loadPriceDataUniverse(){
		/*
		 * This loads the daily price data of each security into a individual
		 * candlestick object.
		 */
		candidateUniverse.loadHistoricalPriceData();
		candidateUniverse.doEachPL_by_Date();
		//candidateUniverse.doVarianceCovariance();
	}

	public void loadPriceTestUniverse(){
		/*
		 * This loads the daily price data of each security into a individual
		 * candlestick object.
		 */
		testUniverse.loadHistoricalPriceData();
		testUniverse.saveHistoricalPriceData();
	}
		
	public void buildTableInstances() {
		this.appendToTextArea("Creating Table class instances\n");
		try {
			tradeHistoryTable =
					new TradeHistoryTable(getMyConnection(), myJDBCTutorialUtilities.dbName,
							myJDBCTutorialUtilities.dbms, this);
			securitiesTable =
					new SecuritiesTable(getMyConnection(), myJDBCTutorialUtilities.dbName,
							myJDBCTutorialUtilities.dbms, this);
			setPortfoliosTable(new PortfoliosTable(getMyConnection(), myJDBCTutorialUtilities.dbName,
					myJDBCTutorialUtilities.dbms, this));	
			historicalPricesTable =
					new HistoricalPricesTable(this.getMyConnection(), myJDBCTutorialUtilities.dbName,
							myJDBCTutorialUtilities.dbms, this); 
			setPortfolioHoldingsTable(new PortfolioHoldingsTable(getMyConnection(), myJDBCTutorialUtilities.dbName,
					myJDBCTutorialUtilities.dbms, this));

			setParametersTable(new ParametersTable(getMyConnection(), myJDBCTutorialUtilities.dbName,
					myJDBCTutorialUtilities.dbms, this));

			String[][] DDL = BuildScripts.getBuildDDL();		      
			for(String[] ddl:DDL) this.getPortfoliosTable().createViewIfMissing(ddl);

		//	tradeHistoryTable.runDDLtriggers_Batch();
			tradeHistoryTable.runDDL_FKs_Batch();;
			
			this.getParametersTable().loadParameters();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			//     JDBCTutorialUtilities.closeConnection(myConnection);
		}
	}

	public void appendToTextArea(String s) {
		if(this.getBelongsToGUI()==null)
			System.out.println(s);
		else {
			JTextArea ta = this.getBelongsToGUI().getTextArea();
			SwingUtilities.invokeLater(new Runnable() {    						
				@Override
				public void run() {	ta.append(s);		}
			});
		}
	}
	public void insertROI2HtmlTextPane (double roi) {
		String sROI=String.format("Anualized Estimated Return On Investment<b>%.4f%%</b>", roi);
		this.setHtmlTextPane(this.getHtmlTextPane().replace((CharSequence) this.getBelongsToGUI().getRoiPlugString(), sROI));
	}
	public void appendHtmlTextPane (String s) {
		this.setHtmlTextPane(this.getHtmlTextPane()+s.replace("null", ""));
	}
		
	public void appendToTextPane(String s, JTextPane tp) {
		Portfolios p = this;
	//	JTextPane tp = this.getBelongsToGUI().getMyResultsTextPane();
		this.appendHtmlTextPane(s);
		final String ssf =this.getHtmlTextPane();
		SwingUtilities.invokeLater(new Runnable() {    						
			@Override				
			public void run() {	
				tp.setText(ssf);
				tp.setCaretPosition(0);
				try {				
					HTMLDocument htmlDoc = (HTMLDocument) tp.getDocument();
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
		});
	}	
	public void insertMyResultsROI() throws Exception {
		try {				
			HTMLDocument htmlDoc = (HTMLDocument) this.getBelongsToGUI().getMyResultsTextPane().getDocument();
			Element d =htmlDoc.getElement("ROI");			
			htmlDoc.setInnerHTML(d, String.format("<div id=\"ROI\">Portfolio Weighted Average ROI: <span style=\"color:blue\"><b>%.4f%%</b></span></div>", this.getInitialPortfolio().getROI()*100));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//Element htmlElement = this.getBelongsToGUI().getHtmlDocument().getRootElements()[0];
//		Element htmlElement = htmlDoc.getRootElements()[0];
	//	Element bodyElement = htmlElement.getElement(0);
//		Element contentElement = bodyElement.getElement(bodyElement.getElementCount() - 1);
	//	StringBuffer sbHtml = new StringBuffer();
//		sbHtml.append("<span>" + content + "</span><br>");
//		htmlDoc.insertBeforeEnd(contentElement, sbHtml.toString());
	}
	
	public void insertScenariosROI() throws Exception {
		try {				
			HTMLDocument htmlDoc = (HTMLDocument) this.getBelongsToGUI().getScenariosTextPane().getDocument();
			Element d =htmlDoc.getElement("ROI");			
			String s = String.format("The best scenario was Scenario no. %s with an <i>ROI</i> of <span style=\"color:blue\"><b>%.4f%%</b></span>" ,this.getBestScenarioIdNum(),this.getBestCandidatePortfolio().getROI()*100);
			htmlDoc.setInnerHTML(d, String.format("<div id=\"ROI\">%s</div>", s));
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}		
	
	public void buildCorraliations() {
		candidateUniverse.corralateToOtherPortfolio(initialPortfolio);
	}

	public void buildTestPortfolio () {
		//            testUniverse = new Portfolio(this.getMC());

	}

	public void buildCandidatePortfolios(int longs, int shorts, double Maxcapital) {	
		//	public void buildCandidatePortfolios(double Maxcapital) {
		//	int longs = this.getNewLongs();
		//		int shorts = this.getNewShorts();
		// make a list of potential portfolios to be recommended
		candidatePortfolios = new ArrayList<Portfolio>();		
		//.... starting with 1/10th of the lesser of the portfolio's current value & allowed maximum capital ...
		double pv = initialPortfolio.getPortfolioValue();
		int div = 10;
		double capital = Math.min(Maxcapital, pv)/div;
		//... and use the 95% VaR of the initial portfolio as a benchmark
		double lastVar = initialPortfolio.getaVaRs().get(typeVaR.HistVaR95);
		Portfolio p;
		while (capital <= Maxcapital) {
			//... create copies of the initial portfolio then add new positions but only enough
			// new shares (long & short) as can be bought with this amount of capital  ...
			p = buildCandidatePortfolio(longs, shorts, capital);
			// ... calculate the new portfolio's VaR
			p.RunStats();
			p.doVarianceCovariance();
			if (DEBUG) {
				System.out.println("\n\nInitial     New\n" +
						" P&L        P&L\n" +
						"---------------\n\n");	
				Map<Date,Double> treeMap2 = new TreeMap<Date, Double>(p.getDailyPL());
				Map<Date,Double> treeMap1 = new TreeMap<Date, Double>(initialPortfolio.getDailyPL());
				int s = treeMap1.size()-1;
				String t;

				try{  
					FileWriter fr = new FileWriter(dumpPath + "PLcomps.csv");  
					BufferedWriter br = new BufferedWriter(fr);  
					PrintWriter out = new PrintWriter(br); 

					for(int i = 0; i < s-1; i++) {
						t = ef.format(treeMap1.values().toArray()[i]) + "," + ef.format(treeMap2.values().toArray()[i]);
						System.out.println(t);	
						//	aCompare[i][0] = (Double) treeMap1.values().toArray()[i]; 
						//	aCompare[i][1] = (Double) treeMap2.values().toArray()[i];
						if(t != null)  
							out.write(t);  
						out.write("\n");         
					}  
					out.close();  
				} //for
				catch(IOException e){  
					System.out.println(e);     
				}
			} //if Debug     

			if (lastVar < p.getaVaRs().get(typeVaR.HistVaR95)) {
				//... and if it is an improvement (less negative) save it to the list ...
				candidatePortfolios.add(p);
				// ... keeping its VaR for comparison to the next larger portfolio ...
				//lastVar = p.getPortfolioVaR().getVaR95();
				lastVar = p.getaVaRs().get(typeVaR.HistVaR95);
			}
			else {
				// ... but if the VaR is getting worse, then don't save that portfolio
				// but increase the divisor by a factor of 10 so that the capital  ...
				div *= 10;
			}
			if (div > 1000) break;
			// ... will only increase by a specified amount
			capital += (Math.min(Maxcapital, pv)/div);	
			//thus we keep increasing capital by a smaller amount until the best VaR is found
			//but stop when the the increase (div) is small
		}	//while	
	}

	public Portfolio buildCandidatePortfolio(int longs, int shorts, double capital) {
		/*
		 * this method is called to create a clone of the initial portfolio and add additional holdings
		 * based on the specified amounts of long and short positions
		 */
		Portfolio candidatePortfolio = new Portfolio(initialPortfolio);	
		int s = candidateUniverse.getHoldingSet().size();
		/*
		 * limit the total positions to be added to no bigger than the current number of ticker symbols 
		 * in the list of acceptable securities so as to not throw a null pointer error.
		 */		
		int totalPos = (int) Math.min(longs + shorts, (double) s);
		double avgPosAmt = capital/totalPos;
		/*
		 * limit the number of short positions to be added in this candidate portfolio to the lesser of half the
		 * total positions constrained above or the # parsed as a parameter.  The same constraint is then placed on
		 * the long positions to be taken.
		 */
		shorts = Math.min(totalPos/2, shorts);
		longs = Math.min(totalPos-shorts, longs);		
		/*
		 * Loop through the list of acceptable security potential additions and add the specified number of long and short
		 * positions.  Note, this list has been sorted in order of each security's correlation coefficient compared to the
		 * initial portfolio's daily profit and loss.  Since short positions will have the opposite effect on daily P/L than
		 * their underlying security, start from the most correlated security.  The reverse is true for long positions
		 * to be added as the intent is to add stocks that are up when the initial portfolio is down and vice versa. This
		 * is not enough to create a hedge, however, as the VaR must improve which requires the loss at the tails of the 
		 * distribution to improve.
		 */
		for ( int i = s-1; i >= s - shorts ; i--) {	
			int b = 0;
			//if this security was a split, then get next
			if(candidateUniverse.getHoldingSet().get(i).isSplit()) {
				b++;
			} //if			
			updatePortfolio(candidatePortfolio, i, -avgPosAmt);	
			//		for (int i = 0; i < shorts ; i++) {		

			/*
			 * a relatively equal $ amount will be added for each new position added.  To accomplish this, 
			 * take $'s allocated to this ticker and divide the last closing price and round down the next whole number.  
			 * ...
			 */
			//	lastClosePrice = candidateUniverse.getHoldingSet().get(i).getCandleSticks().getLast().getClosePrice();
			//	lastClosePrice = candidateUniverse.getHoldingSet().get(i).getLastClosePrice();
			/*
			 * but negative for short positions so that their price changes will have an opposite effect on the daily 
			 * profit or loss.
			 */
			//	posD = (int) (-avgPosAmt/lastClosePrice);
			//	updatePortfolio(candidatePortfolio, i, -avgPosAmt);
			//... and make this the new position size for this security in the candidate list of securities ...
			//		candidateUniverse.getHoldingSet().get(i).setPosition(posD);
			//... and then add this new security included it number of share (position) to the new candidate portfolio.
			//		candidatePortfolio.addSecurities(candidateUniverse.getHoldingSet().get(i), posD);
			//		candidatePortfolio.setPortfolioValue(candidatePortfolio.getPortfolioValue()+lastClosePrice*posD);
		}
		for (int i = 0; i < longs ; i++) {
			int b = 0;
			//if this security was a split, then get next
			if(candidateUniverse.getHoldingSet().get(i).isSplit()) {
				b++;
			} //if
			updatePortfolio(candidatePortfolio, i+b, avgPosAmt);	
			//	for ( int i = s-1; i >= s - longs ; i--) {
			/* the logic here for long positions is relatively the same except the position value is positive			 */
			//	lastClosePrice = candidateUniverse.getHoldingSet().get(i).getCandleSticks().getLast().getClosePrice();
			//		lastClosePrice = candidateUniverse.getHoldingSet().get(i).getLastClosePrice();
			//		posD = (int) (avgPosAmt/lastClosePrice);
			//		candidateUniverse.getHoldingSet().get(i).setPosition(posD);
			//		candidatePortfolio.addSecurities(candidateUniverse.getHoldingSet().get(i), posD);
			//		candidatePortfolio.setPortfolioValue(candidatePortfolio.getPortfolioValue()+lastClosePrice*posD);
		}
		return candidatePortfolio;
	} //buildCandidatePortfolio


	/**
	 * so watch what happens, the calling function creates a <b>mms</b> that contains just a few parameters and passes it here as "<b>mms</b>"
	 * next, this is saved temporarily to the security at each iteration in the loop as the <i>tempMMS</i> just to make it work.  
	 * next, the original <b>mms</b> of that security is cloned to <b>mms2</b>  the trigger price of the original <b>mms</b> is obtained and increased
	 * and then saved to <i>q</i>.
	 * next the <b>ProfitTarget</b> of the the original <b>mms</b> is duplicated now in the <b>mms2</b> is cloned but the exit type is changed to
	 * the imposed exit type such as trailing stop.
	 * next the new <b>ProfitTarget</b> object of the exit are increased by a factor of <i>q</i>
	 * 
	 * @param
	 * 
	 */
	@SuppressWarnings("unchecked")
	public Portfolio buildCandidatePortfolioForROI(MoneyMgmtStrategy mms) {
		/* this method is called to create a clone of the initial portfolio and add additional holdings
		 * based on the specified amounts of long and short positions		 */
		Portfolio candidatePortfolio = new Portfolio(initialPortfolio);	
		candidatePortfolio.setBelongsTo(this);
		MoneyMgmtStrategy mms2;		
		for ( int i=0; i < candidatePortfolio.getHoldingSet().size(); i++) {				
			//temporarily set mmsTemp of this security to mms, this mms only has the basic settings as set in the function that
			//called this function.  See it use in SecuirtyInst class at executeOrder(MoneyMgmtStrategy mms)
			candidatePortfolio.getHoldingSet().get(i).setMmsTemp(mms);	
			//create a reference for convince of this security's mms and name it mms2
			mms2 = candidatePortfolio.getHoldingSet().get(i).getMms();
			mms2.setIdNum(this.getCurrentScenario().getIdNum());
			mms2.setImposedExitType(mms.getImposedExitType());
			Pcts4Lyer cs = new Pcts4Lyer(this.getCurrentScenario());
			//Not loading the stack is LIFO
			while (cs.moreLayers()) {
				mms2.getLayerStack().push(cs.getNextLayer());
			}
			//if scenarios intends to use the original StopLoss at the start then create a TrailingStop that mimic it
			if (this.getCurrentScenario().isUseOriginalStopLoss()){
				double x = -(mms2.getOrder().getPrice()-mms2.getStop().getPrice())/mms2.getOrder().getPrice();
				//mimic the existing stop as a trailing stop.  basically, if the trigger and trailing %s are equal
				//to the percent loss allowed in the initial stop, then setting both parameter of the trailing stop 
				//will result in the same outcome.  Setting dynamic to false causes the trailing $ to not change while this layer is current.
				mms2.getLayerStack().push(new Pcts4Lyer(x, x, false));
			}
			//since mms was set temporally as teh MmsTemp, it will need a LayerStack in order to clone mms2 in the subsequent step
			mms.setLayerStack(mms2.getLayerStack());
			//create a clone instance of the object ref of mms2 and make it the new tempMms.  We are going to make changes to it
			//and i don't want to overwrite any of the original settings.
			candidatePortfolio.getHoldingSet().get(i).setMmsTemp(new MoneyMgmtStrategy(mms2));	
		} //if			
		return candidatePortfolio;
	} //buildCandidatePortfolioForROI

	/**
	 * <p>creates a series of <b>Portfilio</b> instances which are saved to an array.  Various combinations of stops are applied to determine
	 * the optimal stop set to maximize profit</p>
	 * @return void
	 * @param none
	 * 
	 */
	public void buildCandidatePortfoliosForMaxROI() {	
		String sTitle ="Strategy Optimizer Battery I:  <strong>Optimze for Maximum Return on Investment</strong>";
		String sDiscussion ="This part of the optimizer attempts to find the strategy to maximize annualized Return on Investment (<i>ROI</i>).  "
				+ "The <i>ROI</i> approaches the ideal only.  No regard is given to opportunity cost of investible capital"
				+ "when not being place in an active trade.  It assumes that capital is immediately invested in the next"
				+ " trade upon exiting the prior trade.  It assumes the amount of capital applied to each trade is equal."
				+ " None of these conditions is possible, however, the variables removed may be considered noisy"
				+ " and thus the strategy recommended may be the trader’s preferred choice.";
		//Tools.createSectionHeader(sTitle, sDiscussion);
		this.appendHtmlTextPane(String.format("<p>%s</p><p>%s</p>",sTitle, sDiscussion));
		// make a list of potential portfolios to be recommended
		candidatePortfolios = new ArrayList<Portfolio>();		
		this.createScenarios();	
		//System.out.println(this.getScenarios().toString());	
		this.appendHtmlTextPane(this.getScenariosHolderClass().toString());	
		//... use the ROI of the initial portfolio as a benchmark
		double lastROI = initialPortfolio.getROI();
		Portfolio p;
		try {
			while (this.moreScenarios()) {
				MoneyMgmtStrategy mms = new MoneyMgmtStrategy() ;
				this.setCurrentScenario(this.getNextScenario());
			//	Pcts4Lyer cs = new Pcts4Lyer (this.getCurrentScenario());
				//... create copies of the initial portfolio then add new stops  ...
				mms.setImposedExitType(this.getCurrentScenario().getExitType());		
				//create a new portfolio instance and save it to p.
				//this portfolio will have the same entry and stop exit but will have a trailing-stop order with
				//incrementally-adjusting values
				p = buildCandidatePortfolioForROI(mms);
				// ... execute the new portfolio with the stop parameters specified by the mms
		   //     System.out.println(StringUtils.repeat("*", GlobalVars.outputLineWidth1));
	//	        Tools.carriageReturn(2);
		//		Tools.drawSeprator();
				//System.out.printf("Begin Scenario #%s\n", this.getCurrentScenario().getIdNum());
			//	Tools.drawTitle(String.format("Begin Scenario #%s", this.getCurrentScenario().getIdNum()));
				this.appendHtmlTextPane(String.format("<p><h2>Begin Scenario #%s</h2></p>", this.getCurrentScenario().getIdNum()));
		  //      Tools.drawSeprator();
				p.executeOrders(mms);
				double roi = p.getROI("tempMMS");
		//		System.out.printf("<p>Scenario #%s ROI = %s</p> ", this.getCurrentScenario().getIdNum(), pf.format(roi));
				this.appendHtmlTextPane(String.format("<p><hr><h2>Scenario #%s <i>ROI</i>:  <b>%s</b></h2><hr><hr></p> ", this.getCurrentScenario().getIdNum(), pf.format(roi)));
				if (lastROI < roi) {
					this.setBestScenarioIdNum(this.getCurrentScenario().getIdNum());
					//... and if it is an improvement save it to the list ...
					candidatePortfolios.add(p);
					this.setBestCandidatePortfolio(p); 
					// ... keep its ROI for comparison to the next larger portfolio ...
					lastROI = roi;
				}
				p.reset4NextSenario();
			}	//while	
	   //     Tools.drawSeprator();
			//**********Start Recommendations Section *************************/
			this.setHtmlTextPane("<h1>Analysis and Recommendations from Trade<i>Coach</i></h1>");
			this.appendHtmlTextPane(String.format("%s<br><br>",dfLongDateAndTime.format(new Date())));
			if(this.getBestCandidatePortfolio()==null) {
				//System.out.println("The intitial portfilio could not be improved upon");
				//this.appendToAnalysisTextArea("<p>The intitial portfilio could not be improved upon</p>");
				this.appendToTextPane("<p>The intitial portfilio could not be improved upon</p>", this.getBelongsToGUI().getAnalysisTextPane());
			}
			else {
		      //  System.out.printf("\nThe best scenario was #%s with an <i>ROI</i> of %s\n" ,this.getBestScenarioIdNum(),pf.format(this.getBestCandidatePortfolio().getROI()));
			//	System.out.println("We recommend using the following scenario " + this.getBestScenario().toString());
			//	System.out.println("Multiplying each trailing stop percent by |Beta|");
				String s = String.format("<p>The best scenario was Scenario no. %s with an <i>ROI</i> of %s</p>" ,this.getBestScenarioIdNum(),pf.format(this.getBestCandidatePortfolio().getROI()));
				s += String.format("<p>We recommend using the following scenario %s</p>",this.getBestScenario().toString());
				s += String.format("<p>Multiplying each trailing stop percent by absolute value of |<i>Beta</i>|</p>");
			//	this.appendToAnalysisTextArea(s);
				this.appendToTextPane(s, this.getBelongsToGUI().getAnalysisTextPane());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Tools.createSectionFooter(sTitle);
		}
	}
	/**Creates three hierarchies of instances of <i>Pcts4Lyer</i>.  This bottom instance of this 
	 * hiarchy is the individual <b>layers</b> instance.  Several <b>layers</b> instances are created with 
	 * varying parameters.  These <b>layers</b> instances are added to a instance of <i>Pcts4Lyer</i> referred
	 * to as the <b>scenario</b> instance.  In turn, several <b>scenario</b> instances are created holding different
	 * combinations of layers and added to a master instance of <i>Pcts4Lyer</i> referred
	 * to as the <b>scenario<i><strong>s</i></strong></b> instance and a <i>stack</i> object of instances of 
	 * <i>Pcts4Lyer</i>.  This <i>stack</i> object is redundant and is intended to be depreciated */
	public void createScenarios() {
		Scenarios = new Stack<Pcts4Lyer>();
		double w=firstTrailingStpPct;
		double z=firstTriggerPctIncrease;
		//Stack<Pcts4Lyer> Scenarios2 = new Stack<Pcts4Lyer>()
		for (int i = scenario2BCreated; i > 0; i--) {
			Pcts4Lyer scenario = new Pcts4Lyer();
			scenario.setIdNum(i);//set scenario id number to be displayed
			scenario.useOriginalStopLoss();
			scenario.setExitType(typeOrder.TrailingStop);
			z +=.01;
			double y=z;
			double x=w;
			for (int j = 0; j < layers2BCreated; j++) {
				scenario.addLayer(new Pcts4Lyer(x, y, true));
				x -= 0.01;//decrease the trailing stop percentage to get tighter and tigher
				y += 0.01;//increase the amount 'into the money' by which the new stop is initiated
			}
			Scenarios.add(scenario);//this will be deprecated
			//scenariosHolderClass.addScenario(scenario);//this will be the new way of doing it
		}
		@SuppressWarnings("unchecked")
		Stack<Pcts4Lyer> s = (Stack<Pcts4Lyer>)this.getScenarios().clone();
		this.setScenarios2(s);
		scenariosHolderClass = new Pcts4Lyer(GlobalVars.groupType.Scenarios, this);
	}
	public void updatePortfolio (Portfolio candidatePortfolio, int i, double avgPosAmt) {
		double lastClosePrice;
		int posD;
		lastClosePrice = candidateUniverse.getHoldingSet().get(i).getLastClosePrice();

		/*
		 * but negative for short positions so that their price changes will have an opposite effect on the daily 
		 * profit or loss.
		 */
		posD = (int) (avgPosAmt/lastClosePrice);
		//... and make this the new position size for this security in the candidate list of securities ...
		candidateUniverse.getHoldingSet().get(i).setPosition(posD);
		//... and then add this new security included it number of share (position) to the new candidate portfolio.
		candidatePortfolio.addSecurities(candidateUniverse.getHoldingSet().get(i), posD);
		candidatePortfolio.setPortfolioValue(candidatePortfolio.getPortfolioValue()+ (lastClosePrice*posD) );
	}
	
	public void buildCandidatePortfoliosForMaxProfit() {	
		String sTitle ="Strategy Optimizer Battery II:  Optimze for Maximum Profit on $10,000 investment";
		String sDiscussion ="This part of the optimizer attempts to find the strategy to maximize annualized Return on Investment\n (ROI).  The ROI approaches the ideal only.  No regard is given to opportunity cost of investible capital\n when not being place in an active trade.  It assumes that capital is immediately invested in the next\n trade upon exiting the prior trade.  It assumes the amount of capital applied to each trade is equal.\n None of these conditions is possible, however, the variables removed may be considered noisy\n and thus the strategy recommended may be the trader’s preferred choice.";
		Tools.createSectionHeader(sTitle, sDiscussion);
	}
	
	public void buildCandidatePortfoliosKnapsack() {	
		String sTitle ="Strategy Optimizer Battery III:  Optimze for Maximum Profit on investible capital";
		String sDiscussion ="This part of the optimizer attempts to find the strategy to maximize annualized Return on Investment\n (ROI).  The ROI approaches the ideal only.  No regard is given to opportunity cost of investible capital\n when not being place in an active trade.  It assumes that capital is immediately invested in the next\n trade upon exiting the prior trade.  It assumes the amount of capital applied to each trade is equal.\n None of these conditions is possible, however, the variables removed may be considered noisy\n and thus the strategy recommended may be the trader’s preferred choice.";
		Tools.createSectionHeader(sTitle, sDiscussion);
	}
	
	public void critiqueTrades() {	
		String sTitle ="Trade Critic:  Show notably aspects of your trades showing opportunity for improvement";
		String sDiscussion ="This part of the optimizer attempts to find the strategy to maximize annualized Return on Investment\n (ROI).  The ROI approaches the ideal only.  No regard is given to opportunity cost of investible capital\n when not being place in an active trade.  It assumes that capital is immediately invested in the next\n trade upon exiting the prior trade.  It assumes the amount of capital applied to each trade is equal.\n None of these conditions is possible, however, the variables removed may be considered noisy\n and thus the strategy recommended may be the trader’s preferred choice.";
		Tools.createSectionHeader(sTitle, sDiscussion);
	}
	

	public void executeOrders() {

	}

	public void dumpPortfolioData() {
		/*
		 * This the data for the initial and recommended (if one exists) portfolio into a CSV file which is read into Excel
		 * for testing purposes.
		 */
		String filename = dumpPath + "InitPortfolio.csv";
		this.getInitialPortfolio().dumpPortfolioData(filename);
		if (candidatePortfolios.size() > 0) {
			filename = dumpPath + "NewPortfolio.csv";
			int x = candidatePortfolios.size()-1;
			candidatePortfolios.get(x).dumpPortfolioData(filename);	
		} //if
	} //dumpPortfolioData

	public void dumpPortfolioData(String initPortfoliofilename, String newPortfoliofilename) {
		/*
		 * This the data for the initial and recommended (if one exists) portfolio into a CSV file which is read into Excel
		 * for testing purposes.
		 */
		//	String filename = dumpPath + "InitPortfolio.csv";
		this.getInitialPortfolio().dumpPortfolioData(initPortfoliofilename);
		if (candidatePortfolios.size() > 0) {
			//	filename = dumpPath + "NewPortfolio.csv";
			int x = candidatePortfolios.size()-1;
			candidatePortfolios.get(x).dumpPortfolioData(newPortfoliofilename);	
		} //if
	} //dumpPortfolioData
	
	public Portfolio getCandidateUniverse() {
		return candidateUniverse;
	}

	public void setCandidateUniverse(String fileName) {
		this.candidateUniverse = new Portfolio(mc, fileName);
	}	

	public void setCandidateUniverse(List<String> list) {
		this.candidateUniverse = new Portfolio(mc, list);
	}

	public Portfolio getInitialPortfolio() {
		return initialPortfolio;
	}

	public ArrayList<Portfolio> getCandidatePortfolios() {
		return candidatePortfolios;
	}

	public void setCandidatePortfolios(ArrayList<Portfolio> candidatePortfolios) {
		this.candidatePortfolios = candidatePortfolios;
	}

	public MarketCalendar getMC() {
		return mc;
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

	public Portfolio getTestUniverse() {
		return testUniverse;
	}

	public Pcts4Lyer getScenariosHolderClass() {
		return scenariosHolderClass;
	}

	public void setScenariosHolderClass(Pcts4Lyer scenariosHolderClass) {
		this.scenariosHolderClass = scenariosHolderClass;
	}

	public Stack<Pcts4Lyer> getScenarios() {
		return Scenarios;
	}

	public void setScenarios(Stack<Pcts4Lyer> scenarios) {
		Scenarios = scenarios;
	}

	public Stack<Pcts4Lyer> getScenarios2() {
		return Scenarios2;
	}

	public void setScenarios2(Stack<Pcts4Lyer> scenarios2) {
		Scenarios2 = scenarios2;
	}

	public Pcts4Lyer getNextScenario() {
		return this.getScenarios().pop();
	}

	/** 
	 * returns <tt>true</tt> if there are more scenarios in the <i>Stack</i>.
	 * @return
	 */
	public boolean moreScenarios() {
		return !this.getScenarios().empty();
	}

	public void setTestUniverse(Portfolio testUniverse) {
		this.testUniverse = testUniverse;
	}


	public Portfolio getBestCandidatePortfolio() {
		return bestCandidatePortfolio;
	}

	public void setBestCandidatePortfolio(Portfolio bestCandidatePortfolio) {
		this.bestCandidatePortfolio = bestCandidatePortfolio;
	}

	public Pcts4Lyer getCurrentScenario() {
		return currentScenario;
	}

	public void setCurrentScenario(Pcts4Lyer currentScenario) {
		this.currentScenario = currentScenario;
	}

//
//	public int getIdNum() {
//		return idNum;
//	}
//
//	public void setIdNum(int idNum) {
//		this.idNum = idNum;
//	}
//

	public int getBestScenarioIdNum() {
		return bestScenarioIdNum;
	}


	public Pcts4Lyer getScenarioByIdNum(int idNum) {
		return this.getScenarios2().elementAt(this.getScenarios2().size()-idNum);
		
	}

	public Pcts4Lyer getBestScenario() {
		return this.getScenarioByIdNum(this.getBestScenarioIdNum());
	}

	public void setBestScenarioIdNum(int bestScenarioIdNum) {
		this.bestScenarioIdNum = bestScenarioIdNum;
	}

	/**	 
	 * Class that holds the below variables and a stack that contains instances of this same class.  The stack pop method 
	 * removes the next <b>Pcts4Lyer</b> instance from the top of the stack.
	 * There are two stacks implemented by this class.  The first in the hiearchary is a stack of scenarios.  It, in turn, holds
	 * a stack of layers relative to each individual scenario.
	 * <ol><li>private typeOrder ExitType;</li>
		<li>private double trlStopPct, trlTriggerPct;</li>
		<li>private boolean dynamic, useOriginalStopLoss;</li>
		<li>private int layerCount, idNum;</li>
		<li>private groupType groupType;</li></ol>
	 */
	protected class Pcts4Lyer { 
		private typeOrder ExitType;
		private groupType groupType;
		private double trlStopPct, trlTriggerPct;
		private boolean dynamic, useOriginalStopLoss;
		private int layerCount, idNum;
		Stack<Pcts4Lyer> stack = new Stack<Pcts4Lyer>();
		private ArrayList<Pcts4Lyer> list;
		private Portfolios belongsTo;

		/**Used to create a stack of Scenarios.  See the class description */
		public Pcts4Lyer() {
			this.setGroupType(GlobalVars.groupType.Scenario);
		}
		
		/**Used to create a holding class of <b>Scenario</b> instances.  See the class description */
		public Pcts4Lyer(GlobalVars.groupType type, Portfolios belongsTo) {
			this.setGroupType(type);
			this.setBelongsTo(belongsTo);
			if(type==GlobalVars.groupType.Scenarios) 	
				this.setList(new ArrayList(this.getBelongsTo().getScenarios()));
		}
		/**<p>Used to create a stack of Layers which are in turn held with a previously created stack of scenarios.  See the class description</p>
		 *  <p>Note: the <i>dynamic</i> variable is set here</p>*/
		public Pcts4Lyer(double trlStopPct, double trlTriggerPct, boolean dynamic) {
			this.setTrlStopPct(trlStopPct);
			this.setTrlTriggerPct(trlTriggerPct);
			this.setDynamic(dynamic);
			this.setGroupType(GlobalVars.groupType.Layer);
		}	
		/**Used to create a clone of the instance containing either Scenarios or layers.  See the class description */
		public Pcts4Lyer(Pcts4Lyer pl) {
			this.setExitType(pl.getExitType());
			this.setTrlStopPct(pl.getTrlStopPct());
			this.setTrlTriggerPct(pl.getTrlTriggerPct());
			this.setDynamic(pl.isDynamic());
			this.setUseOriginalStopLoss(pl.isUseOriginalStopLoss());
			this.setLayerCount(pl.getLayerCount());
			this.setIdNum(pl.getIdNum());
			this.setGroupType(pl.getGroupType());			
			@SuppressWarnings("unchecked")
			Stack<Pcts4Lyer> copiedStack = (Stack<Pcts4Lyer>)pl.getTheStack().clone();
			this.setTheStack(copiedStack);
		}		
		public Pcts4Lyer(double trlStopPct, double trlTriggerPct) {//currently not being used
			this.setTrlStopPct(trlStopPct);
			this.setTrlTriggerPct(trlTriggerPct);
			this.setDynamic(true);
			this.setGroupType(GlobalVars.groupType.Layer);
		}
		public Pcts4Lyer(int layerCount) {//currently not being used
			this.setLayerCount(layerCount);
			this.setGroupType(GlobalVars.groupType.Scenario);
		}
		/**Prints the header for the analysis tab.  For example<br>
		 * <p><br><table style="width:100%; border: 1px solid black;  border-collapse: collapse;"><caption><i>These Scenarios will be applied to this position to atttend to improve ROI</i></caption><tr><th>Layer</th><th>Trailing<br>Trigger<br>Percentage</th><th>Trailing<br>Percentage</th></tr><p><strong>Scenario No. 1</strong></p><tr><td>Layer No. : 1</td><td>9%</td><td>5%</td></tr><tr><td>Layer No. : 2</td><td>10%</td><td>4%</td></tr><tr><td>Layer No. : 3</td><td>11%</td><td>3%</td></tr><tr><td>Layer No. : 4</td><td>12%</td><td>2%</td></tr><tr><td>Layer No. : 5</td><td>13%</td><td>1%</td></tr><br><p><strong>Scenario No. 2</strong></p><tr><td>Layer No. : 1</td><td>8%</td><td>5%</td></tr><tr><td>Layer No. : 2</td><td>9%</td><td>4%</td></tr><tr><td>Layer No. : 3</td><td>10%</td><td>3%</td></tr><tr><td>Layer No. : 4</td><td>11%</td><td>2%</td></tr><tr><td>Layer No. : 5</td><td>12%</td><td>1%</td></tr><br><p><strong>Scenario No. 3</strong></p><tr><td>Layer No. : 1</td><td>7%</td><td>5%</td></tr><tr><td>Layer No. : 2</td><td>8%</td><td>4%</td></tr><tr><td>Layer No. : 3</td><td>9%</td><td>3%</td></tr><tr><td>Layer No. : 4</td><td>10%</td><td>2%</td></tr><tr><td>Layer No. : 5</td><td>11%</td><td>1%</td></tr><br><p><strong>Scenario No. 4</strong></p><tr><td>Layer No. : 1</td><td>6%</td><td>5%</td></tr><tr><td>Layer No. : 2</td><td>7%</td><td>4%</td></tr><tr><td>Layer No. : 3</td><td>8%</td><td>3%</td></tr><tr><td>Layer No. : 4</td><td>9%</td><td>2%</td></tr><tr><td>Layer No. : 5</td><td>10%</td><td>1%</td></tr><br><p><strong>Scenario No. 5</strong></p><tr><td>Layer No. : 1</td><td>5%</td><td>5%</td></tr><tr><td>Layer No. : 2</td><td>6%</td><td>4%</td></tr><tr><td>Layer No. : 3</td><td>7%</td><td>3%</td></tr><tr><td>Layer No. : 4</td><td>8%</td><td>2%</td></tr><tr><td>Layer No. : 5</td><td>9%</td><td>1%</td></tr><br></table></p>*/
		public String toString() {
			String s;
			if (this.getGroupType()==GlobalVars.groupType.Layer) {
			//	s =  "Pcts4Lyer: [trlTriggerPct=" + pf.format(this.getTrlTriggerPct()) +  ", trlStopPct=" + pf.format(this.getTrlStopPct())  + (this.isDynamic()?"":", FreezeStopPrice") + "]";
				s=String.format("<td>%s</td><td>%s</td>",pf.format(this.getTrlTriggerPct()),pf.format(this.getTrlStopPct()));
			} else if (this.getGroupType()==GlobalVars.groupType.Scenario) {
				//s="Scenario #"+this.getIdNum()+"\n";
				s=String.format("<p><strong>Scenario No. %s</strong></p>",this.getIdNum());
				for (int j = 0; j < this.getLayers().size() ; j++) {
				//	s+=this.getExitType()+":  ";
					s+=String.format("<tr><td>Layer No. : %d</td>%s</tr>", j+1 ,this.getLayers().get(j).toString());
				}
			} else if (this.getGroupType()==GlobalVars.groupType.Scenarios) {
				//Note: There is a raw Stack type object created with the Portfolios instance and it native toString() method is called
				//s="Scenarios";
				s ="<br><table style=\"width:100%; border: 1px solid black;  border-collapse: collapse;\">"; 
				s +="<caption><i>These Scenarios will be applied to this position to atttend to improve ROI</i></caption>";
				s+=this.toStringTH();
				for (int j = this.getList().size()-1; j >= 0 ; j--) {
					s+=this.getList().get(j).toString()+"<br>";
				}
				s +="</table>";
			} else {
				s = "toString not defined for this groupType or groupType has not be defined<br>";
			}
			return s;			
		}		

		public String toStringTH() {
			String s;
			String col1 ="Layer";
			String col2 ="Trailing<br>Trigger<br>Percentage";
			String col3 ="Trailing<br>Percentage";
			s=String.format("<tr><th>%s</th><th>%s</th><th>%s</th></tr>",col1,col2,col3);
			return s;
		}

		public groupType getGroupType() {
			return groupType;
		}

		public void setGroupType(groupType groupType) {
			this.groupType = groupType;
		}
		
		public void addStack(Pcts4Lyer stackItem) {
			stack.push(stackItem);
		}

		public Stack<Pcts4Lyer> getScenarios() {
			return stack;
		}

		public Stack<Pcts4Lyer> getLayers() {
			return stack;
		}
		
		public Stack<Pcts4Lyer> getTheStack() {
			return stack;
		}
		
		public void setTheStack(Stack stk) {
			this.stack=stk;
		}

		public ArrayList getList() {
			return list;
		}

		public void setList(ArrayList list) {
			this.list = list;
		}

		/** 
		 * returns <tt>true</tt> if there are no more scenarios in the <i>Stack</i>.
		 * @return
		 */
		public boolean noMoreStackItems() {
			return this.getScenarios().empty();
		}
		/** 
		 * returns <tt>true</tt> if there are more scenarios in the <i>Stack</i>.
		 * @return
		 */
		public boolean moreScenarios() {
			return !this.getScenarios().empty();
		}

		/** 
		 * returns <tt>true</tt> if there are more layers in the <i>Stack</i>.
		 * @return
		 */
		public boolean moreLayers() {
			return !this.getLayers().empty();
		}

		public boolean isUseOriginalStopLoss() {
			return useOriginalStopLoss;
		}

		public void setUseOriginalStopLoss(boolean useOriginalStopLoss) {
			this.useOriginalStopLoss = useOriginalStopLoss;
		}

		/**
		 * causes scenario to initiate the trade using the StopLoss of the original trade as the first layer
		 * of the new trailing-stop scenario
		 */
		public void useOriginalStopLoss() {
			this.setUseOriginalStopLoss(true);
		}		
		/**
		 * resets variable to false that when true causes scenario to initiate the trade using the StopLoss of the original trade as the first layer
		 * of the new trailigStop scenario
		 */
		public void unUseOriginalStopLoss() {
			this.setUseOriginalStopLoss(false);
		}		
		public Pcts4Lyer getNextScenario() {
			return (Pcts4Lyer)stack.pop();
		}

		public Pcts4Lyer getNextLayer() {
			return (Pcts4Lyer)stack.pop();
		}
		
//		public Pcts4Lyer getScenarioByIdNum(int idNum) {
//			return this.getScenarios().elementAt(idNum);
//			
//		}
		public int getLayerCount() {
			return layerCount;
		}

		public void setLayerCount(int layerCount) {
			this.layerCount = layerCount;
		}
		/**Add a instance of <b>Pcts4Lyer</b> as a layer to the current scenario stack*/
		public void addLayer(Pcts4Lyer layer) {
			this.addStack(layer);
		}

		public void addScenario(Pcts4Lyer Scenario) {
			this.addStack(Scenario);
		}


		public double getTrlStopPct() {
			return trlStopPct;
		}

		public void setTrlStopPct(double trlStopPct) {
			this.trlStopPct = trlStopPct;
		}

		public double getTrlTriggerPct() {
			return trlTriggerPct;
		}

		public void setTrlTriggerPct(double trlTriggerPct) {
			this.trlTriggerPct = trlTriggerPct;
		}

		public boolean isDynamic() {
			return dynamic;
		}

		public void setDynamic(boolean dynamic) {
			this.dynamic = dynamic;
		}

		public GlobalVars.typeOrder getExitType() {
			return ExitType;
		}

		public void setExitType(GlobalVars.typeOrder exitType) {
			ExitType = exitType;
		}
		
		public int getIdNum() {
			return idNum;
		}

		public void setIdNum(int idNum) {
			this.idNum = idNum;
		}

		public Portfolios getBelongsTo() {
			return belongsTo;
		}

		public void setBelongsTo(Portfolios belongsTo) {
			this.belongsTo = belongsTo;
		}
	} //class pctLyer   

	public int getScenario2BCreated() {
		return scenario2BCreated;
	}

	public void setScenario2BCreated(int scenario2bCreated) {
		scenario2BCreated = scenario2bCreated;
	}

	public int getLayers2BCreated() {
		return layers2BCreated;
	}

	public void setLayers2BCreated(int layers2bCreated) {
		layers2BCreated = layers2bCreated;
	}

	public double getFirstTriggerPctIncrease() {
		return firstTriggerPctIncrease;
	}

	public void setFirstTriggerPctIncrease(double firstTriggerPctIncrease) {
		this.firstTriggerPctIncrease = firstTriggerPctIncrease;
	}

	public double getFirstTrailingStpPct() {
		return firstTrailingStpPct;
	}

	public void setFirstTrailingStpPct(double firstTrailingStpPct) {
		this.firstTrailingStpPct = firstTrailingStpPct;
	}

	public boolean isRunCurrent() {
		return runCurrent;
	}

	public void setRunCurrent(boolean runCurrent) {
		this.runCurrent = runCurrent;
	}

	@Override
	public void run() {
//		System.out.println("Executing orders of initial portfolio" );
//		this.getInitialPortfolio().executeOrders();
//		System.out.println("\nStarting Candidate Portfolio Optimizer\n");            	
//		this.buildCandidatePortfoliosForMaxROI();
		
	}
	public CountingSemaphore getOrderSem() {
		return orderSem;
	}

	public void setOrderSem(CountingSemaphore orderSem) {
		this.orderSem = orderSem;
	}
	
	public Lock getCoutLock() throws NullPointerException  {
	//	if(coutLock==null) throw new NullPointerException();
		return coutLock;
	}

	public void setCoutLock(Lock coutLock) {
		this.coutLock = coutLock;
	}


	public class Lock{
		private int scenarioNum = 0;
		Stack<SecurityInst> st = new Stack<SecurityInst>();
		Portfolios belongsTo;
		SecurityInst csi;
		private boolean isLocked = false;

		public  Lock(Portfolios p) {
			this.setBelongsTo(p);
			this.init();
		}
		
		public void init(){
	//		System.out.println("Initial Holding Set Order");
	//		System.out.println(this.getBelongsTo().getInitialPortfolio().getHoldingSet());	
			
		/*	Collections.sort(this.getBelongsTo().getInitialPortfolio().getHoldingSet(), new Comparator<SecurityInst>() {
				@Override
				public int compare(SecurityInst arg0, SecurityInst arg1) {
					return  arg0.getMms().getOrder().getOrderDate().compareTo(arg1.getMms().getOrder().getOrderDate());
				}
		    });*/
			st.addAll(this.getBelongsTo().getInitialPortfolio().getHoldingSet());
		//	System.out.println("\nLock Holding Set Order");
		//	System.out.println(this.getBelongsTo().getInitialPortfolio().getHoldingSet());
		//	System.out.println(this.getBelongsTo().getCurrentScenario().toString());
			csi=st.pop();	
//			System.out.println(st.pop());	
//			System.out.println(st.pop());	
//			System.out.println(st.pop());	
//			System.out.println(st.pop());
			isLocked = false;
			
		}
		public void reset(){
			this.init();
			
		}

		public synchronized void lock()	throws InterruptedException{
			while(isLocked){
				wait();
			}
			isLocked = true;
		}

		public synchronized void unlock(){
			isLocked = false;
			notify();
		}

		public synchronized void lock(SecurityInst si) throws InterruptedException {
		//	if(this.getCsi().equals(si))
		//		System.out.println("securities match");
		
			
//			while(!this.getCsi().equals(si)){
//				wait();
//			}
			
			while(isLocked){
				wait();
			}
			if(!st.isEmpty())
				this.setCsi(st.pop());
			
			isLocked = true;			
		}

		/**set which <b>Portfolios</b> object this <b>Portfolio</b> object belongs too*/
		public Portfolios getBelongsTo() {
			return belongsTo;
		}
		/**set which <b>Portfolios</b> object this <b>Portfolio</b> object belongs too*/
		public void setBelongsTo(Portfolios belongsTo) {
			this.belongsTo = belongsTo;
		}

		
 		public SecurityInst getCsi() {
			return csi;
		}

		public void setCsi(SecurityInst csi) {
			this.csi = csi;
		}
	}


	public class CountingSemaphore {
		private int signals = 0;
		private int scenarioNum = 0;
		Stack<SecurityInst> st = new Stack<SecurityInst>();
		Portfolios belongsTo;
		SecurityInst csi;
		
		public void CountingSemaphore() {
		}
		
		public void CountingSemaphore(Portfolios p) {
			this.setBelongsTo(p);
			st.addAll(this.getBelongsTo().getInitialPortfolio().getHoldingSet());
			csi=st.pop();
		}

		public synchronized void take() {
			this.signals++;
			this.notify();
		}

		public synchronized void take(SecurityInst si) {
		//	this.signals++;
			if(this.getCsi().equals(si))
				this.notify();
		}
		
		
		public synchronized void release() throws InterruptedException{
		//	while(this.signals == 0) wait();
		//	this.signals--;
		}
		
		/**set which <b>Portfolios</b> object this <b>Portfolio</b> object belongs too*/
		public Portfolios getBelongsTo() {
			return belongsTo;
		}
		/**set which <b>Portfolios</b> object this <b>Portfolio</b> object belongs too*/
		public void setBelongsTo(Portfolios belongsTo) {
			this.belongsTo = belongsTo;
		}
		
		public SecurityInst getCsi() {
			return csi;
		}

		public void setCsi(SecurityInst csi) {
			this.csi = csi;
		}
	}

	public com.oracle.tutorial.jdbc.SecuritiesTable getSecurityInstTable() {
		return securitiesTable;
	}

	public void setSecurityInstTable(com.oracle.tutorial.jdbc.SecuritiesTable securityInstTable) {
		this.securitiesTable = securityInstTable;
	}

	public com.oracle.tutorial.jdbc.HistoricalPricesTable getHistoricalPricesTable() {
		return historicalPricesTable;
	}

	public void setHistoricalPricesTable(com.oracle.tutorial.jdbc.HistoricalPricesTable historicalPricesTable) {
		this.historicalPricesTable = historicalPricesTable;
	}

	public com.oracle.tutorial.jdbc.TradeHistoryTable getTradeHistoryTable() {
		return tradeHistoryTable;
	}

	public void setTradeHistoryTable(com.oracle.tutorial.jdbc.TradeHistoryTable tradeHistoryTable) {
		this.tradeHistoryTable = tradeHistoryTable;
	}

	public PortfoliosTable getPortfoliosTable() {
		return portfoliosTable;
	}
	public void setPortfoliosTable(PortfoliosTable portfoliosTable) {
		this.portfoliosTable = portfoliosTable;
	}
	public PortfolioHoldingsTable getPortfolioHoldingsTable() {
		return portfolioHoldingsTable;
	}
	public void setPortfolioHoldingsTable(PortfolioHoldingsTable portfolioHoldingsTable) {
		this.portfolioHoldingsTable = portfolioHoldingsTable;
	}
	public ParametersTable getParametersTable() {
		return parametersTable;
	}
	public void setParametersTable(ParametersTable parametersTable) {
		this.parametersTable = parametersTable;
	}
	public GUI getBelongsToGUI() {
		return belongsToGUI;
	}

	public void setBelongsToGUI(GUI belongsToGUI) {
		this.belongsToGUI = belongsToGUI;
	}

	public String getHtmlTextPane() {
		return htmlTextPane;
	}

	public void setHtmlTextPane(String htmlTextPane) {
		this.htmlTextPane = htmlTextPane;
	}

	public JDBCTutorialUtilities getMyJDBCTutorialUtilities() {
		return myJDBCTutorialUtilities;
	}

	public void setMyJDBCTutorialUtilities(JDBCTutorialUtilities myJDBCTutorialUtilities) {
		this.myJDBCTutorialUtilities = myJDBCTutorialUtilities;
	}

	public Connection getMyConnection() {
		return myConnection;
	}

	public void setMyConnection(Connection myConnection) {
		this.myConnection = myConnection;
	}

public Boolean getRefreshOnStart() {
		return refreshOnStart;
	}
	public Boolean getUseNativeJDBC() {
	return useNativeJDBC;
}
public void setUseNativeJDBC(Boolean useNativeJDBC) {
	this.useNativeJDBC = useNativeJDBC;
}
	public void setRefreshOnStart(Boolean refreshOnStart) {
		this.refreshOnStart = refreshOnStart;
	}
	/*	public static void main(String[] args) throws InterruptedException {
//		String filename = "C:\\Users\\Phil\\Google Drive\\Stock Market\\Trades2.csv";
//		System.out.println("Building Initial Portfolio, This may take awhile" );
//		Thread t = new Thread(new Portfolios(filename));
//        t.start();
		Portfolios p;
		String filename = "C:\\Users\\Phil\\Google Drive\\Stock Market\\Trades2.csv";
		System.out.println("Building Initial Portfolio" );
		
	    if (args == null) {
	    	  String propertiesfile = "properties/javadb-sample-properties.xml";
		      System.err.println("Properties file not specified at command line");
		      p = new  Portfolios(filename, propertiesfile,null);	
		    } else {
		      try {
		    	  p = new  Portfolios(filename, args[0],null);	
		    //    myJDBCTutorialUtilities = new JDBCTutorialUtilities(args[0]);
		      } catch (Exception e) {
		        System.err.println("Problem reading properties file " + args[0]);
		        e.printStackTrace();
		        return;
		      }
		    }
	//	p.getSecurityInstTable().saveAllSecuritiesInfo();
	//	p.getHistoricalPricesTable().saveAllSecuritiesPriceData();
		try {
			p.getSecurityInstTable().viewTable();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Executing orders of initial portfolio" );
		p.getInitialPortfolio().setRunCurrent(true);
		p.getInitialPortfolio().executeOrders();
		int scenario2BCreated=5;
		int layers2BCreated = 5;
		double firstTriggerPctIncrease=0.04f;
		double firstTrailingStpPct=0.05f;
		p.setScenario2BCreated(scenario2BCreated);
		p.setLayers2BCreated(layers2BCreated);
		p.setFirstTrailingStpPct(firstTrailingStpPct);
		p.setFirstTriggerPctIncrease(firstTriggerPctIncrease);
		p.getInitialPortfolio().setRunCurrent(false);
		p.buildCandidatePortfoliosForMaxROI();
	}
*/
	public void createTradeAdvise(NewTradeEntry newTradeEntry) {
		String html ="<p><h1>These are the Trailing Stops for you to apply</h1></p>";
		int rows = newTradeEntry.getTable().getModel().getRowCount();
		int cols = newTradeEntry.getTable().getModel().getColumnCount();
		Portfolio p = null;
		MoneyMgmtStrategy mms = null;
		TableModel tm = newTradeEntry.getTable().getModel();
		java.sql.Date today =  new java.sql.Date(Calendar.getInstance().getTime().getTime());
		for(int i = 0; i<rows; i++){// for(int j = 0; j<cols-1; j++){		
			//String ticker = newTradeEntry.getTfSymbol().getText();
			String ticker = ((String) tm.getValueAt(i, 0)).toUpperCase().trim();
			if(ticker==null||ticker=="") continue;
			html +="<br><table style=\"width:100%; border: 1px solid black;  border-collapse: collapse;\">"; 
			html +=String.format("<caption><i><b>%s</b></i></caption>",ticker);
			//Integer pos =Integer.parseInt(newTradeEntry.getTfPosition().getText()); 
		//	Object k = tm.getValueAt(i, 1);
			int pos = Integer.valueOf((String) tm.getValueAt(i, 1));
		//	int pos = (int) k;
			//Integer pos = (int) tm.getValueAt(i, 1);
			//double price = Double.parseDouble(newTradeEntry.getTfPrice().getText());
			//double price = (Double) tm.getValueAt(i, 2);
			double price = Double.valueOf((String) tm.getValueAt(i, 2));
			TransactionData ts = new TransactionData();
			ts.setTradeID(1);		    	
			ts.setTickerSymbol(ticker);
			ts.setInstrumentName("");
			ts.setOrderDate(today);
			ts.setEntryPrice(price);
			ts.setStopLoss(price);
			ts.setStop(price);
			ts.setStopTrigger(price);
			ts.setStopType(GlobalVars.typeOrder.Limit);
			//	if(rs.getDate("STOPACTIVATIONDATE")!=null)
			//		ts.setStopactivationDate(new java.sql.Date(rs.getDate("STOPACTIVATIONDATE").getTime()));
			ts.setPosition(pos);
			mms = new MoneyMgmtStrategy(ts) ;
			this.setCurrentScenario(this.getBestScenario());
			mms.setImposedExitType(this.getCurrentScenario().getExitType());	
			SecurityInst si = new SecurityInst(mms);
			si.setMmsTemp(mms);
			mms.setIdNum(this.getCurrentScenario().getIdNum());
			Pcts4Lyer cs = new Pcts4Lyer(this.getCurrentScenario());
			html +=Layers.toStringTH();
			//Not loading the stack is LIFO		
			//		String col1 ="Layer";
			//		String col2 ="Trigger<br>Price";
			//		String col3 ="Trailing<br>Trigger<br>Percentage";
			//		String col4 ="Trailing<br>Percentage";
			//		String col5 ="Dynamic";
			//		String col6 ="Beta<br>Value";//
			int j=0;
			while (cs.moreLayers()) {
				//		mms.getLayerStack().push(cs.getNextLayer());
				Pcts4Lyer layer =cs.getNextLayer();
				html += String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", 
						++j,cf.format(price * (1.0d+layer.getTrlTriggerPct())), pf.format(layer.getTrlTriggerPct()),pf.format(layer.getTrlStopPct()),"true", si.getBetaValue());
			}
			si.setBelongsTo(p);
			html +="</table>";
		}
		//create a new portfolio instance and save it to p.
		//this portfolio will have the same entry and stop exit but will have a trailing-stop order with
		//incrementally-adjusting values
		p = buildCandidatePortfolioForROI(mms);
		this.getBelongsToGUI().getTpNewTrades().setText(html);
	}
	public void importTradeHistoryDataFromCSVwithDELETE() {
		this.getTradeHistoryTable().deleteAll();
		this.importTradeHistoryDataFromCSV();
	}
	public void importTradeHistoryDataFromCSV() {
		String sFileName = this.getBelongsToGUI().getFileName();
		try {
			boolean refreshOnStartOLD = this.getRefreshOnStart();
			this.setRefreshOnStart(true);
			this.getTradeHistoryTable().importData(sFileName);		
			((DefaultTableModel) this.getBelongsToGUI().getGui().getTableMyTrades().getModel()).setRowCount(0);//setting setRowCount to zero clears the table display
			this.getTradeHistoryTable().viewTable((DefaultTableModel) this.getBelongsToGUI().getTableMyTrades().getModel());
			this.getSecurityInstTable().viewTable((DefaultTableModel) this.getSecurityGUITable().getModel());
			this.getHistoricalPricesTable().viewTable((DefaultTableModel) this.getHistoricalPricesGUITable().getModel());
			this.getSecurityGUITable();
			this.setRefreshOnStart(refreshOnStartOLD);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	private JTable getHistoricalPricesGUITable() {
		return this.getHistoricalPricesTable().getTableGUI();
	}
	private JTable getSecurityGUITable() {
		return this.getSecurityInstTable().getTableGUI();
	}
	public SessionFactory getFactory() {
		return factory;
	}
	public void setFactory(SessionFactory factory) {
		this.factory = factory;
	}	
}
