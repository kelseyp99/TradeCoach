package com.tradecoach.patenter.entity.security;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.utilities.GlobalVars;
import com.workers.MarketCalendar;
import com.workers.MoneyMgmtStrategy;
import com.workers.Portfolio;
import com.workers.PortfoliosGroup;
import com.workers.PriceCollection;
import com.workers.Tools;

public class Instrument extends PriceCollection implements Runnable, Comparable<Object> {

	private Double meanPrice, stdDevPrice, variancePrice, lastClosePrice;
	private Double corralationToInitPortfolio;
	private String instrumentName;
	private String tickerSymbol;
	private int position;
	private CandleSticks cs;
	private CandleStick OldestCandleStickSaved, NewestCandleStickSaved;
	private MoneyMgmtStrategy mms, mmsTemp;
	private ArrayList<MoneyMgmtStrategy> mmsSet = new ArrayList<MoneyMgmtStrategy> ();
	private boolean isSplit;
	private boolean Selected;
	private Legs Legs;
	private int portfolioID = 1;
	private Portfolio belongsTo;
	private Double betaValue=1.0d;
	private int executionLevel;
	private static String dbURL = "jdbc:derby:C:\\Users\\Phil\\MyDB;create=true;user=test;password=test";
    private static Connection conn = null;
    public Instrument() {
		super();
	}

	public Instrument(MarketCalendar pmc, String tickerSymbol, String instrumentName, Integer position) {
		super(pmc);
		this.tickerSymbol = tickerSymbol;
		//this.instrumentName =  instrumentName;
		this.position =  position;
		//create an array containing all calendar days for the desired period. 
		//not just those having price data for this security
	//	cs = new CandleSticks(this);
		mms = new MoneyMgmtStrategy();
	}

	public Instrument(MarketCalendar pmc, String tickerSymbol, int position) {
		super(pmc);
		this.tickerSymbol = tickerSymbol;
		this.position =  position;
		//create an array containing all calendar days for the desired period. 
		//not just those having price data for this security
		//	buildCalendar(pmc.getStartDate(), pmc.getEndDate());
		//	vVaRs = new VaR(mc);
	//	cs = new CandleSticks(this);

	}

	public Instrument(Instrument s) {
		//creates a clone instance of hte security
		super(s.getStartDate(), s.getEndDate());
		/*create a reference of a clone security class to the original CandleSicks 
		 * since since the price history will remain static*/
		this.cs = s.getCandleSticks();
		//the below are all passed by value to the clone
		this.setDailyPL(s.getDailyPL());
		this.setCorralationToInitPortfolio(s.getCorralationToInitPortfolio());
		this.setEndDate(s.getEndDate());
		this.setTickerSymbol(s.getTickerSymbol());
		this.setinstrumentName(s.getinstrumentName());
		this.setSelected(s.isSelected());
		this.setMeanPL(s.getMeanPL());
		this.setPosition(s.getPosition());
		this.setStartDate(s.getStartDate());
		this.setStdDevPL(s.getStdDevPL());
		this.setVariancePL(s.getVariancePL());
		this.setLastClosePrice(s.getLastClosePrice());
	} //SecurityInst(SecurityInsinclination


	public Instrument(MarketCalendar mc, MoneyMgmtStrategy mms, int i) {
		super(mc);
		this.tickerSymbol = mms.getTickerSymbol();
		//	this.instrumentName =  mms.getInstrumentName();
		this.position =  mms.getOrder().getQuantity();
		//create an array containing all calendar days for the desired period. 
		//not just those having price data for this security
		//	buildCalendar(pmc.getStartDate(), pmc.getEndDate());
		//	vVaRs = new VaR(mc);
	//	cs = new CandleSticks(this);
		this.mms = mms;
	//	this.getMms().setBelongsTo(this);
	}// end SecurityInst

	/**
	 * Create an instance of <b>SecurityInst</b> from <i>mms</i> using the ticker symbol and quantity.
	 * @param mms
	 */
	public Instrument(MoneyMgmtStrategy mms) {
		this.tickerSymbol = mms.getTickerSymbol();
		this.position =  mms.getOrder().getQuantity();
	//	cs = new CandleSticks(this);
		this.mms = mms;
	//	this.getMms().setBelongsTo(this);
	}
	
	@Override
	public String toString() {
		
		String s = null;
		try {
			s = String.format("<hr><p><i>Information:</i><br> Security: <b>%s</b> (%s) TrailingStop</p>", this.getTickerSymbol(), this.getinstrumentName());
		//	s = "Information:\n" +
			//		"Security: " + this.getinstrumentName() + "(" +this.getTickerSymbol() + ")\n"  ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return s;
	}
	
	
	public String toString2() {
		
		String s = null;
		try {
			s = "Information:\n" +
					"    Name:                           " + this.getinstrumentName() + "\n" +
					"    Ticker:                         " + this.getTickerSymbol() + "\n" +
					//  "    Shares:                         " + this.getPosition() + "\n" +
					"    Last Close Price :              " + ef.format(this.getLastClosePrice()) + "\n" +
					"    last volume:                    " + this.getCandleSticks().getLast().getVolume() + "\n" + 
					"    Highest High Price :            " + ef.format(this.getMms().getOrder().getHighestHighPrice()) + "\n" +
					"    Highest High Price volume:      " + this.getMms().getOrder().getHighestHighPriceVolume() + "\n" +
					"    Highest High Price date:        " + df.format(this.getMms().getOrder().getHighestHighPriceDate()) + "\n" +
					"    Lowest Low Price :              " + ef.format(this.getMms().getOrder().getLowestLowPrice()) + "\n" +
					"    Lowest Low Price volume:        " + this.getMms().getOrder().getLowestLowPriceVolume() + "\n" +
					"    Lowest Low Price date:          " + df.format(this.getMms().getOrder().getLowestLowPriceDate()) + "\n";
					
					s += this.getMms().toString() + "\n" ;
		//			s += this.getMMSofOptimalPortfolio().toString() + "\n" ;
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		return s;
	}
	
	@Override
	public void run() {
		if(this.getExecutionLevel()==0)
			try {
				this.setExecutionLevel(this.loadHistoricalPriceData());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		else if (this.getExecutionLevel()==1 && this.isSelected()) //1 means has not executed the initial orders yet
			this.setExecutionLevel(this.executeOrder());
		else if (this.getExecutionLevel()==2 && this.isSelected()) //2 means has not executed candidate scenarios yet
			this.setExecutionLevel(this.executeOrder(this.belongsTo.getMms2bRun()));
	}

	public double getROIweightedContribution(int totalDays) {
		double x = this.getMms().getROIweightedContribution(totalDays);
		return x;
	}
	/*
    public void createMMS(String orderDate, int orderQty, double entryPrice, double stopLossPrice, double stopPrice) {
    	mms = new MoneyMgmtStrategy(orderDate, orderQty, entryPrice, stopLossPrice, stopPrice, this);
    	mms.setBelongsTo(this);
    }
	 */
	public void createMMS(MoneyMgmtStrategy mms) {
		mms = new MoneyMgmtStrategy();
		//mms.setBelongsTo(this);
	}

	/** 
	 * gets the <b>mms</b> instance associated with this <b>SecurityInst</b> and calls its <i>executeOrder()</i> function.
	 */
	public int executeOrder() {	
		try {
			//if filled, save date and price (candlestick) and activate the stops
			getMms().executeOrder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 2;
	}
	/**
	 * <p>executes this <b>SecurityInst</b> based on the stop parameters passed to it in <i>mms</i>.  If this
	 * results in an improvement to ROI when compared to that saved to the temp <i>mms</i> as the base 
	 * <i>mms</i> the last time this function was called, then replace the base <i>mms</i> with this temp 
	 * <i>mms</i> as it is the better <i>mms</i> </p>
	 * 
	 * @param mms
	 */
	@SuppressWarnings("finally")
	public int executeOrder(MoneyMgmtStrategy mms) {	
		//the temp mms is a complete mms that with an incrementally adjusting trailing stop values
		//execute the temp mms and if a better ROI results then make save this temp mms to the 
		//base mms.  the base mms will now have a complete set of trailing stop values
		try {
//			this.getMmsTemp().resetTradeActivity();
//			this.getMms().resetTradeActivity();
//			this.belongsTo.getMms2bRun().resetTradeActivity();
			if(this.getMmsTemp().getImposedExitType()==null) throw new Exception("the imposed exit type is null");
			String s = String.format("%s<br>%s", this.toString(), this.getMmsTemp().toStringStops() );
			this.getMmsTemp().cout(s);
			this.getMmsTemp().executeOrder();
			this.getMmsSet().add(new MoneyMgmtStrategy(this.getMmsTemp()));
			if(this.getMmsTemp().getROI()>this.getMms().getROI()){
				this.getMmsTemp().setMmsBase(mms);
			}//if
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			return 3;
		}

		//add this mms to an ArrayList of mms's.  It may not be the best mms for this particular security but 
		//it must be saved.  Later the generic mms initiated in Portfolios that produces the best portfoliowide-ROI
		//even consider the individual stock ROI that weren't hte best will have to retrieve this ROI
//		this.getMmsSet().add(new MoneyMgmtStrategy(this.getMmsTemp()));
	}
	public boolean LoadHistory()  {
		Connection connection = null;
		PreparedStatement loadHistory = null;
		try {
			connection = getDataSource().getConnection();
			loadHistory = connection.prepareStatement(
					"SELECT TRADEDATE, CLOSEPRICE, OPENPRICE, HIGHPRICE, LOWPRICE, ADJCLOSE, VOLUME " +
							"FROM PRICEHISTORY " + 
					"WHERE TICKERSYMBOL = ?");
			loadHistory.setString( 1, this.getTickerSymbol() );
			ResultSet resultSet = loadHistory.executeQuery();

			// if requested seat is available, reserve it
			if ( resultSet.next() ) {
				//    sDate = resultSet.getInt( 1 ).getText();
				//    Date date =  df.parse(sDate); 
				Date date = resultSet.getDate( 1 );
				double open = resultSet.getInt( 3 );
				double high = resultSet.getInt( 4 );
				double low = resultSet.getInt( 5 );
				double close = resultSet.getInt( 2 );
				long volume = resultSet.getInt( 7 );
				double adjClose = resultSet.getInt( 6 );
				CandleStick c = new CandleStick(date, open, close, high, low, adjClose, (int) volume, cs) ;
				cs.candleSticks.add(c) ;
				return true;                  
			} // end if
			return false;
		} // end try
		catch ( SQLException e ) {
			e.printStackTrace();
			return false;
		} // end catch
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		} // end catch
		finally {
			try {
				loadHistory.close();
				connection.close();
			} // end try
			catch ( Exception e )
			{
				e.printStackTrace();
				return false;
			} // end catch
		} // end finally
	} // end WebMethod reserve

	public boolean DeleteHistory()  {
		Connection connection = null;
		PreparedStatement saveHistory = null;
		try {
			connection = getDataSource().getConnection();
			saveHistory = connection.prepareStatement(
					"DELETE FROM CANDLESTICKS " + 
					"WHERE SEC_INST_ID = ?");
			saveHistory.setString( 1, this.getPrimaryKeyThisTicker() );            
			saveHistory.executeUpdate();
			return true;
		} // end try
		catch ( SQLException e ) {
			e.printStackTrace();
			return false;
		} // end catch
		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		} // end catch
		finally {
			try {
				saveHistory.close();
				connection.close();
			} // end try
			catch ( Exception e ) {
				e.printStackTrace();
				return false;
			} // end catch
		} // end finally
	}  //DeleteHistory  

	public boolean SaveHistory()  {
		Connection connection = null;
		PreparedStatement saveHistory = null;
		CandleStick c;

		try {
			Iterator<CandleStick> i = cs.candleSticks.iterator();
			while (i.hasNext()) {    
				c = i.next();
				c.SaveHistory(this.getPrimaryKeyThisTicker());
			}//while
				return true;
		} // end try

		catch ( Exception e ) {
			e.printStackTrace();
			return false;
		} // end catch
		finally {
			try {
				saveHistory.close();
				connection.close();
			} // end try
			catch ( Exception e ) {
				e.printStackTrace();
				return false;
			} // end catch
		} // end finally
	}  //SaveHistory    

	public ArrayList<CandleStick> getHistory() {
		return this.getCandleSticks().getCandleSticks();
	} // end method getAddresses

	public String getPrimaryKeyThisTicker() {
		Connection connection = null;
		PreparedStatement loadHistory = null;
		try {
			connection = getDataSource().getConnection();
			loadHistory = connection.prepareStatement(
					"SELECT SEC_INST_ID " +
							"FROM SECURITY_INST " + 
					"WHERE TICKERSYMBOL = ?");
			loadHistory.setString( 1, this.getTickerSymbol() );
			ResultSet resultSet = loadHistory.executeQuery();

			// if requested seat is available, reserve it
			if ( resultSet.first() ) return Integer.toString(resultSet.getInt( 1 ));
			return this.setUpSecurity(Integer.toString(this.portfolioID));
		} // end try
		catch ( SQLException e ) {
			e.printStackTrace();
			return null;
		} // end catch
		catch ( Exception e ) {
			e.printStackTrace();
			return null;
		} // end catch            
	} // end getPrimaryKeyThisTicker

	public String setUpSecurity(String pkey) {
		Connection conn = null;
		PreparedStatement saveHistory = null;

		try {
		//	connection = dataSource.getConnection();
			createConnection();
	//        insertRestaurants(5, "LaVals", "Berkeley");
	 //       selectRestaurants();
	   //     shutdown();
			saveHistory = conn.prepareStatement(
					"INSERT INTO SECURITY_INST " + 
							"(PORTFOLIO_ID, COMPANY_NAME, TICKER_SYMBOL, BETA_VALUE) " +
					"VALUES ( ? , ? , ?, ? )");
			saveHistory.setString(1, pkey );
			saveHistory.setString(2, this.getinstrumentName());
			saveHistory.setString(3, this.getTickerSymbol());
			saveHistory.setDouble(4, this.getBetaValue());
			saveHistory.executeUpdate();
		} // end try
		catch ( SQLException e ) {
			e.printStackTrace();
			return null;
		} // end catch
		catch ( Exception e ) {
			e.printStackTrace();
			return null;
		} // end catch
		return this.getPrimaryKeyThisTicker();
	}  //setUpSecurity 

	public void SolveCandlestickType(){
		this.getCandleSticks().setCsTypes();
	}

	public void FindSwingPoints() {
		//            Legs.FindSwingPoints(this);
	}

	public void RunStats() {
		/*
		 * for this individual security instrument, calculate the needed statistical values.
		 */
		//	   super.RunStats();
		double sum = 0.0d;
		int size = cs.getCandleSticks().size();
		Math.signum(this.position);
		for (int i = 0; i < size; i++) 
			sum += cs.getCandleSticks().get(i).getClosePrice() * this.getPosition() ;
		//close price by shares to get value of postion to sum with others	   
		double mean = sum/size;
		this.setMeanPrice(mean);
		double temp = 0, d = 0;
		for (int i = 0; i < size; i++) {
			d =  cs.getCandleSticks().get(i).getClosePrice() * this.getPosition() ;	 
			temp += (mean-d)*(mean-d);
		}    
		Double variance = temp/size; 
		this.setVariancePrice(variance);
		Double sd = Math.sqrt(variance);
		this.setStdDevPrice(sd);
		//      setVaR95(mean - sd * 1.960);
		//     setVaR99(mean - sd * 2.576);	   
	}//RunStats

	public double[] getDailyPrices_asDouble() {
		if (DEBUG) System.out.println("Value in\ndailyPrice\n\n");			
		int size = cs.getCandleSticks().size();
		double[] dd = new double[size];
		for (int i = 0; i < size; i++) {
			dd[i] = cs.getCandleSticks().get(i).getClosePrice();
			if (DEBUG) System.out.println(ef.format(dd[i]));
		}
		return dd;
	} //getDailyPrices_asDouble()

	public void FindPatterns() {
		this.getLegs().FindPatterns();
	}

	public double estimateNextSwing() {
		//            if (this.getLegs().getInPlayPattern() != null) {
		///             return this.getLegs().getInPlayPattern().estimateNextSwing(Legs, confidence);
		//          }
		return 0;
	}

	public MoneyMgmtStrategy getMmsByType(String mmsType) {
		return mmsType=="tempMMS"?this.getMmsTemp():this.getMms();
		
	}
	
	public CandleSticks getCandleSticks() {
		return cs;
	}

	public void setCandleSticks(ArrayList<CandleStick> candleSticks) {
		this.cs.candleSticks = candleSticks;
	}


	public String getinstrumentName() {
		return instrumentName;
	}

	public void setinstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}


	public String getTickerSymbol() {
		return tickerSymbol;
	}

	public void setTickerSymbol(String tickerSymbol) {
		this.tickerSymbol = tickerSymbol;
	}

	//Create a method to construct the URL given the startDate, endDate, tickerSymbol as input
	/**
	 * Goes out to yahoo's website and gets basic information about the stock such as company name and the Beta value
	 * @throws InterruptedException 
	 */
	public void getStockInfo() throws InterruptedException{		
		while (true) {//loop until connection is made.  Then break
			int x = 0,y=1000;
			int timeOut=3000;
			try {
				if(++x>y) break;
				String url="http://finance.yahoo.com/q?s="+this.getTickerSymbol();
				org.jsoup.nodes.Document doc = Jsoup.connect(url).timeout(timeOut).get();
				for (org.jsoup.nodes.Element table : doc.select("div.yfi_quote_summary")) {
					//for (org.jsoup.nodes.Element table : doc.select("table.yfnc_datamodoutline1")) {
					for (org.jsoup.nodes.Element row : table.select("tr")) {
						Elements ths = row.select("th");
						Elements tds = row.select("td");						
						if (ths.get(0).text().contains("Beta")) {
						//	System.out.printf("%s Beta text is %s\n",this.getTickerSymbol(), tds.get(0).text());
						//	Double b = Double.parseDouble(tds.get(0).text());
						//	this.setBetaValue(b==null?1.0f:b);
							this.setBetaValue(Double.parseDouble(tds.get(0).text())); 
							break;
							//				System.out.println(ths.get(0).text() + ":" + tds.get(0).text());
						}
					}
				}
				break;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Thread.sleep(1000);
				timeOut+=1000;
				//e.printStackTrace();
			}
		}
		
		while (true) {
			int x = 0,y=100;
			try {
				if(++x>y)break;;
				// 1. Retrieve the XML file
				String prefix = "http://query.yahooapis.com/v1/public/yql?q=";
				String query = "select * from yahoo.finance.quote where symbol = \"" + this.getTickerSymbol() + "\"";
				String suffix = "&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
				String s = prefix + query.replaceAll(" ", "%20") + suffix;
				URL url = new URL(s);
				URLConnection urlConn = url.openConnection();
				InputStreamReader inputStreamReader = new InputStreamReader(urlConn.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				final StringBuffer buffer = new StringBuffer();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					buffer.append(line);
				}
				bufferedReader.close();
				inputStreamReader.close();

				//
				// 2. Parse XML file
				//
				SAXBuilder sxb = new SAXBuilder();
				Document document = sxb.build(new ByteArrayInputStream(buffer.toString().getBytes()));
				Element query0 = document.getRootElement();
				Element results = query0.getChild("results");
				List<?> quotes = results.getChildren();
				Iterator<?> i = quotes.iterator();
				while (i.hasNext()) {
					Element quote = (Element) i.next();
					if (StringUtils.isEmpty(this.getinstrumentName()) || this.getinstrumentName() == "not available") 
							this.setinstrumentName(quote.getChild("Name").getText());
					double lastClosePrice = Double.parseDouble(quote.getChild("LastTradePriceOnly").getText());	 
					this.setLastClosePrice(lastClosePrice);
				}
				break;
			} catch (Exception e) {
				Thread.sleep(1000);
				e.printStackTrace();
			}
		}
	}
	public int loadHistoricalPriceData() throws InterruptedException{
		Calendar cal = Calendar.getInstance();
		cal.set(this.getMms().getOrder().getOrderDate().getYear(),this.getMms().getOrder().getOrderDate().getMonth(),this.getMms().getOrder().getOrderDate().getDay());
		cal.set(this.getMms().getOrder().getOrderDate().getYear(),this.getMms().getOrder().getOrderDate().getMonth(),1);
		startDate = this.getMms().getOrder().getOrderDate();
		cal.set(Calendar.YEAR, 2016);
		endDate = cal.getTime();
		
		//load security info from derby database
//		this.getBelongsToPortfolios().getSecurityInstTable().loadSecurityInfo(this);
		
		boolean refreshOnStart=false;//toggle on an off whetehr to refresh from Yahoo
		if(refreshOnStart==true) {
			boolean runIt=this.getNewestCandleStickSaved()==null||this.getOldestCandleStickSaved()==null;

			/*if the specified date range extends beyond (either older or newer) than
			 * that stored in derby, then query YQL for new activity, if not then reduce 
			 * the time window size.  However, the YQL may not run regardless*/
			if(!runIt){
				if(Tools.isLater(endDate, this.getNewestCandleStickSaved().getDate()))
					runIt=true;
				else 
					endDate = this.getOldestCandleStickSaved().getDate();
			}
			if(!runIt){
				if(!Tools.isLater(startDate, this.getOldestCandleStickSaved().getDate()))
					runIt=true;
				else 
					startDate = this.getNewestCandleStickSaved().getDate();
			}
			//get latest stock info from YQL
			if(runIt) 
				loadHistoricalPriceData(startDate, endDate);
		}
		return 1;
	}

	//Create a method to construct the URL given the startDate, endDate, tickerSymbol as input
	public void loadHistoricalPriceData(Date startDate, Date endDate) throws InterruptedException{
		String sDate = df.format(startDate);
		String eDate = df.format(endDate);
		Double priorClose = 0.00d;
		CandleStick lastCS = null;
		this.getStockInfo();
		while(true) {
			Integer x = 0,y=100;
			try {
				if((++x>y) && (cs.candleSticks.isEmpty())) 
					throw new Exception(String.format("Failed after %f attempts for %s to load beta data.", y.toString(), this.getTickerSymbol()));
				//System.out.println(this.getinstrumentName()+"("+this.getTickerSymbol() +"):  " + df2.format(startDate)+" to "+df2.format(endDate));
				//System.out.printf("Beta = %f\n",this.getBetaValue());
				System.out.printf("%s (%s):  %s to %s\n     Beta = %.2f\n", this.getinstrumentName(),this.getTickerSymbol(),df2.format(startDate),df2.format(endDate),this.getBetaValue());
				// 1. Retrieve the XML file
				//getM_tickerSymbol
				String prefix = "http://query.yahooapis.com/v1/public/yql?q=";
				String query = "select * from yahoo.finance.historicaldata where symbol = \"" + this.getTickerSymbol() + "\" and startDate = \"" + sDate + "\" and endDate = \"" + eDate + "\"";
				String suffix = "&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
				String s = prefix + query.replaceAll(" ", "%20") + suffix;
				URL url = new URL(s);
				URLConnection urlConn = url.openConnection();
				InputStreamReader inputStreamReader = new InputStreamReader(urlConn.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				final StringBuffer buffer = new StringBuffer();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					buffer.append(line);
				}
				bufferedReader.close();
				inputStreamReader.close();

				//
				// 2. Parse XML file
				//
				SAXBuilder sxb = new SAXBuilder();
				Document document = sxb.build(new ByteArrayInputStream(buffer.toString().getBytes()));
				Element query0 = document.getRootElement();
				Element results = query0.getChild("results");
				List<?> quotes = results.getChildren();
				Iterator<?> i = quotes.iterator();

				while (i.hasNext()) {
					org.jdom2.Element quote = (org.jdom2.Element) i.next();
					sDate = quote.getChild("Date").getText();
					Date date = df3.parseDateTime(sDate).toDate();
					if(this.dateAlreadyLoaded(date)) continue;
					double open = Double.parseDouble(quote.getChild("Open").getText());
					double high = Double.parseDouble(quote.getChild("High").getText());
					double low = Double.parseDouble(quote.getChild("Low").getText());
					double close = Double.parseDouble(quote.getChild("Close").getText());
					long volume = Long.parseLong(quote.getChild("Volume").getText());
					double adjClose = Double.parseDouble(quote.getChild("Adj_Close").getText()); 
					CandleStick c = new CandleStick(date, open, close, high, low, adjClose, (int) volume, cs ) ;                            
					c.setPriorCandle(lastCS);
					if(lastCS!=null)
						c.getPriorCandle().setNextCandle(c);
					cs.candleSticks.add(c);	
					this.getBelongsToPortfolios().getHistoricalPricesTable().saveCandleStickPriceData(c);
					lastCS = c;
					//if the current and last price a extremely different, assume this is an outlyer and throw out
					//this would also be the case in a stock split,  make the P/L zzero for that day
					if (priorClose != 0)  
						if ((priorClose/close) > 2 || (close/priorClose) > 2) {
							this.setSplit(true);
							String w = this.getinstrumentName() + " (" + this.getTickerSymbol() + " ) \n\n" +
									"either split during the look-back period or is considered to volatile \n" +
									"and will not be considered in this hedge construction exercise.  \n" +
									"If this is a member of your initial portfolio, the change in P/L over \n " +
									"the split day or outlier event has been disregarded for purposes of \n" +
									"calclating VaR";
							System.out.println(w);
							/*   JOptionPane.showMessageDialog(null,
		        				    w,
		        				    "Outlyer warning",
		        				    JOptionPane.WARNING_MESSAGE);
							 */
						}

					priorClose = close;
				}//while
				//now create a table of the daily price changes for this security
				//     AddPriceData(cs);	
				break;
			} catch (Exception e) {
				Thread.sleep(1000);
				e.printStackTrace();
			}
		}//end while
	}

	private boolean dateAlreadyLoaded(Date date) {
		Iterator<CandleStick> i = cs.candleSticks.iterator();
		while (i.hasNext()) {    
			if(i.next().getDate().equals(date)) return true;
		}//while
		return false;
	}

	/**
	 * 	return the instance of <b>MoneyMgmtStrategy</b> with the ArrayList of <b>MoneyMgmtStrategy</b>'s that has the same <i>idNum</i> as that passed to 
	 * this function. 
	 * @param idNum
	 * @return MoneyMgmtStrategy
	 */
	public MoneyMgmtStrategy getMMSbyID(int idNum)	 {
		try {
			ArrayList<MoneyMgmtStrategy> mmsSet = this.getMmsSet();
			for (MoneyMgmtStrategy s : mmsSet) {
				if(s.getIdNum()==idNum) 
					return s;	
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}		
		return null;
	}

	public void resetSecurity() {
		this.setExecutionLevel(1);
		this.getMms().resetMMS();
		this.getCandleSticks().resetCandleSticks();
	}
	
	public MoneyMgmtStrategy getMMSofOptimalPortfolio() {
		return this.getMMSbyID(this.getBelongsTo().getBelongsTo().getBestScenarioIdNum());

	}
	
	public PortfoliosGroup getBelongsToPortfolios() {
		return this.getBelongsTo().getBelongsTo();
	}
	
    private static void createConnection()
    {
        try
        {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            //Get a connection
            conn = DriverManager.getConnection(dbURL); 
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
    }

	public double getROI() {
		return mms.getROI();
	}

	public void AddPriceData() {
		AddPriceData(cs);		
	}

	public Double getLastClosePrice() {
		return lastClosePrice;
	}

	public void setLastClosePrice(Double lastClosePrice) {
		this.lastClosePrice = lastClosePrice;
	}

	public double[] getPriceTimeSet(String timeIncremenet, String startDate, String endDate) {
		return null;

	}
	public Double getCorralationToInitPortfolio() {
		return corralationToInitPortfolio;
	}

	public void setCorralationToInitPortfolio(Double corralationToInitPortfolio) {
		this.corralationToInitPortfolio = corralationToInitPortfolio;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Double getMeanPrice() {
		return meanPrice;
	}

	public void setMeanPrice(Double meanPrice) {
		this.meanPrice = meanPrice;
	}

	public Double getStdDevPrice() {
		return stdDevPrice;
	}

	public void setStdDevPrice(Double stdDevPrice) {
		this.stdDevPrice = stdDevPrice;
	}

	public Double getVariancePrice() {
		return variancePrice;
	}

	public void setVariancePrice(Double variancePrice) {
		this.variancePrice = variancePrice;
	}

	public CandleSticks getCs() {
		return cs;
	}

	public void setCs(CandleSticks cs) {
		this.cs = cs;
	}

	public boolean isSplit() {
		return isSplit;
	}

	public void setSplit(boolean isSplit) {
		this.isSplit = isSplit;
	}

	public Legs getLegs() {
		return Legs;
	}

	public void setLegs(Legs Legs) {
		this.Legs = Legs;
	}   

	public int getPortfolioID() {
		return portfolioID;
	}

	public void setPortfolioID(int portfolioID) {
		this.portfolioID = portfolioID;
	}

	public MoneyMgmtStrategy getMms() {
		return mms;
	}


	public void setMms(MoneyMgmtStrategy mms) {
		this.mms = mms;
	}

	public MoneyMgmtStrategy getMmsTemp() {
		return mmsTemp;
	}

	public void setMmsTemp(MoneyMgmtStrategy mmsTemp) {
		this.mmsTemp = mmsTemp;
	}

	public ArrayList<MoneyMgmtStrategy> getMmsSet() {
		return mmsSet;
	}

	public void setMmsSet(ArrayList<MoneyMgmtStrategy> mmsSet) {
		this.mmsSet = mmsSet;
	}

	public Portfolio getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(Portfolio belongsTo) {
		this.belongsTo = belongsTo;
	}

	public Double getBetaValue() {
		return betaValue;
	}

	public void setBetaValue(Double betaValue) {
		this.betaValue = betaValue;
	}

	public int getExecutionLevel() {
		return executionLevel;
	}

	public void setExecutionLevel(int executionLevel) {
		this.executionLevel = executionLevel;
	}

	public CandleStick getOldestCandleStickSaved() {
		return OldestCandleStickSaved;
	}

	public void setOldestCandleStickSaved(CandleStick oldestCandleStickSaved) {
		OldestCandleStickSaved = oldestCandleStickSaved;
	}

	public CandleStick getNewestCandleStickSaved() {
		return NewestCandleStickSaved;
	}

	public void setNewestCandleStickSaved(CandleStick newestCandleStickSaved) {
		NewestCandleStickSaved = newestCandleStickSaved;
	}

	public boolean isSelected() {
		return Selected;
	}

	public void setSelected(boolean selected) {
		Selected = selected;
	}

	public static void main(String[] args) throws InterruptedException {

		/*"AXP", "BA", "CAT", "CSCO", "CVX", "DD", "DIS", "GE", "GS", "HD", "IBM", "INTC", "JNJ", "JPM",
		 *  "KO", "MCD", "MMM", "MRK", "MSFT", "NKE", "PFE", "PG", "T", "TRV", "UNH", "UTX", "V", "VZ", "WMT", "XOM"); 
		 */
		String ticker = null;
		String instrumentName = null;
		String orderDate = null;    
		//Example neither StopLoss or Stop get filled
		double entryPrice = 0;
		int orderQty = 0;
		double stopLossPrice = 0;
		double stopPrice = 0;  
		double stopTriggerPrice = 0;

		int select = 10;
		switch(select)
		{
		case 1:			
			ticker = "GOOG";
			instrumentName = "Google, Inc.";
			orderDate = "02-25-2014";    
			//Example neither StopLoss or Stop get filled
			entryPrice = 560f;
			orderQty = 100;
			stopLossPrice = 450f;
			stopPrice = 961f;  
			//Example Stop fills for a $1 profit Long
			//    stopPrice = 561f;
			//   s.createMMS(orderDate, orderQty, entryPrice, stopLossPrice, stopPrice);
			//     s.executeOrder();



			//Example Stoploss fills for a Loss
			//   stopPrice = 961f;
			stopLossPrice = 559f;	        
			break;
		case 2:
			ticker = "ORCL";
			instrumentName = "Oracle Corp.";
			orderDate = "02-25-2014";    
			entryPrice = 38.5f;
			orderQty = 100;
			stopLossPrice = 35f;
			stopPrice = 45f;         
			break;
		case 3:
			ticker = "MSFT";
			instrumentName = "Microsoft Corp.";
			orderDate = "02-25-2014";    
			entryPrice = 39.97f;
			orderQty = 100;
			stopLossPrice = 25f;
			stopPrice = 40f;         
			break;
		case 4:
			ticker = "SPY";
			instrumentName = "SPDR S&P 500 ETF (SPY)";
			orderDate = "02-25-2014";    
			entryPrice = 184.20f;
			orderQty = 100;
			stopLossPrice = 125f;
			stopPrice = 187f;         
			break;

		case 5:
			ticker = "SDS";
			instrumentName = "ProShares UltraShort S&P500 (SDS)";
			orderDate = "02-25-2014";    
			entryPrice = 28.95f;
			orderQty = 100;
			stopLossPrice = 15f;
			stopPrice = 30f;         
			break;	
		case 6:
			ticker = "SDS";
			instrumentName = "ProShares UltraShort S&P500 (SDS)";
			orderDate = "01-13-2015";    
			entryPrice = 22.38f;
			orderQty = 70;
			stopLossPrice = 21.02f;
			stopPrice = 30f;         
			break;
		case 7:
			ticker = "SPY";
			instrumentName = "SPDR S&P 500 ETF (SPY)";
			orderDate = "01-13-2015";   
			entryPrice = 203.56f;
			orderQty = -10;
			stopLossPrice = 208.98f;
			stopPrice = 187f;         
			break;				
		case 8:
			ticker = "PAAS";
			instrumentName = "Pan American Silver Corp. (USA)";
			orderDate = "12-30-2014";   
			entryPrice = 9.36f;
			orderQty = 102;
			stopLossPrice = 8.39f;
			stopPrice = 12.27d;         
			break;	
		case 9:
			ticker = "SLV";
			instrumentName = "iShares Silver Trust (ETF)";
			orderDate = "01-5-2015";   
			entryPrice = 16.42f;
			orderQty = 92;
			stopLossPrice = 15.38f;
			stopPrice = 22.27d;         
			break;
		case 10:
			ticker = "AEM";
			instrumentName = "iShares Silver Trust (ETF)";
			orderDate = "11-11-2014";   
			entryPrice = 24.12f;
			orderQty = 92;
			stopLossPrice = 21.708f;
			stopPrice = 32.27d;         
			break;
		case 11:
			ticker = "FNV";
			instrumentName = "Franco-Nevada Corporation";
			orderDate = "12-16-2014";   
			entryPrice = 46.54f;
			orderQty = 92;
			stopLossPrice = 45f;
			stopPrice = 53.45d;   
			stopTriggerPrice = 58.83d;   
			break;
		default:

			break;
		} //end Case

		Date startDate;
		Date endDate;
		GlobalVars.typeOrder TypeOrder =  typeOrder.Limit;
		Calendar cal = Calendar.getInstance();
		String delims ="-";
		String[] tokens = orderDate.split(delims);
		cal.set(Integer.parseInt(tokens[2])-1, Integer.parseInt(tokens[0])-1, Integer.parseInt(tokens[1]));
		startDate = cal.getTime();
		cal.set(Calendar.YEAR, Integer.parseInt(tokens[2])+2);
		endDate = cal.getTime();
		double start = System.currentTimeMillis();
		MarketCalendar mc = new MarketCalendar(startDate, endDate);
		//   mms.add(                new MoneyMgmtStrategy(tickers,instrumentNames, orderDates, positions, entryPrices, stopLosses,     stopType,  stopActivationDate,    stopTriggers, stops.get(i), null));
		MoneyMgmtStrategy mms = new MoneyMgmtStrategy(ticker, instrumentName,  orderDate,  orderQty,  entryPrice,  stopLossPrice,   TypeOrder, orderDate,             stopTriggerPrice, stopPrice, null);
		//   SecurityInst s = new SecurityInst(mc, ticker, orderQty);
		Instrument s = new Instrument( mc,  mms, 0);
	//	mms.setBelongsTo(s);
		s.getStockInfo();
		s.loadHistoricalPriceData(mc.getStartDate(), mc.getEndDate());
		s.executeOrder();


		// String ticker = "AXP";
		/*            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, 6);
            cal.set(Calendar.DATE, 30);
            cal.set(Calendar.YEAR, 2013);
            startDate = cal.getTime();
            cal.set(Calendar.YEAR, 2014);
            endDate = cal.getTime();
		 */
		//         start = System.currentTimeMillis();
		//         MarketCalendar mc = new MarketCalendar(startDate, endDate);
		//         SecurityInst s = new SecurityInst(mc, ticker, 100);
		//        s.GetStockInfo();
		//        s.loadHistoricalPriceData(mc.getStartDate(), mc.getEndDate());
		//		p.calcVaR(startDate, endDate);
		//      s.SolveCandlestickType();
		//       s.FindSwingPoints();
		//     s.FindPatterns();
		//       s.RunStats();
		//       double buyAt = s.estimateNextSwing();
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000;
		System.out.println(s.toString());
		//   System.out.println(s.getMms().toString());
		System.out.println("Info retreial took " + duration + " seconds.");
	}//main

	@Override
	public int compareTo(Object si) {
		int i = 0;
		try {
			if(si==null)
				throw new Exception(String.format("si==null;  Error happened when si = %s", ((Instrument)si).getTickerSymbol()));
     
			Date f = ((Instrument)si).getMms().getOrder().getOrderDate();
			i = Tools.daysBetween(f, this.getMms().getOrder().getOrderDate());
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return i;
	}


}

/*
 * quote.Ask = GetDecimal(q.Element("Ask").Value);
                quote.Bid = GetDecimal(q.Element("Bid").Value);
                quote.AverageDailyVolume = GetDecimal(q.Element("AverageDailyVolume").Value);
                quote.BookValue = GetDecimal(q.Element("BookValue").Value);
                quote.Change = GetDecimal(q.Element("Change").Value);
                quote.DividendShare = GetDecimal(q.Element("DividendShare").Value);
                quote.LastTradeDate = GetDateTime(q.Element("LastTradeDate").Value + " " + q.Element("LastTradeTime").Value);
                quote.EarningsShare = GetDecimal(q.Element("EarningsShare").Value);
                quote.EpsEstimateCurrentYear = GetDecimal(q.Element("EPSEstimateCurrentYear").Value);
                quote.EpsEstimateNextYear = GetDecimal(q.Element("EPSEstimateNextYear").Value);
                quote.EpsEstimateNextQuarter = GetDecimal(q.Element("EPSEstimateNextQuarter").Value);
                quote.DailyLow = GetDecimal(q.Element("DaysLow").Value);
                quote.DailyHigh = GetDecimal(q.Element("DaysHigh").Value);
                quote.YearlyLow = GetDecimal(q.Element("YearLow").Value);
                quote.YearlyHigh = GetDecimal(q.Element("YearHigh").Value);
                quote.MarketCapitalization = GetDecimal(q.Element("MarketCapitalization").Value);
                quote.Ebitda = GetDecimal(q.Element("EBITDA").Value);
                quote.ChangeFromYearLow = GetDecimal(q.Element("ChangeFromYearLow").Value);
                quote.PercentChangeFromYearLow = GetDecimal(q.Element("PercentChangeFromYearLow").Value);
                quote.ChangeFromYearHigh = GetDecimal(q.Element("ChangeFromYearHigh").Value);
                quote.LastTradePrice = GetDecimal(q.Element("LastTradePriceOnly").Value);
                quote.PercentChangeFromYearHigh = GetDecimal(q.Element("PercebtChangeFromYearHigh").Value); //missspelling in yahoo for field name
                quote.FiftyDayMovingAverage = GetDecimal(q.Element("FiftydayMovingAverage").Value);
                quote.TwoHunderedDayMovingAverage = GetDecimal(q.Element("TwoHundreddayMovingAverage").Value);
                quote.ChangeFromTwoHundredDayMovingAverage = GetDecimal(q.Element("ChangeFromTwoHundreddayMovingAverage").Value);
                quote.PercentChangeFromTwoHundredDayMovingAverage = GetDecimal(q.Element("PercentChangeFromTwoHundreddayMovingAverage").Value);
                quote.PercentChangeFromFiftyDayMovingAverage = GetDecimal(q.Element("PercentChangeFromFiftydayMovingAverage").Value);
                quote.Name = q.Element("Name").Value;
                quote.Open = GetDecimal(q.Element("Open").Value);
                quote.PreviousClose = GetDecimal(q.Element("PreviousClose").Value);
                quote.ChangeInPercent = GetDecimal(q.Element("ChangeinPercent").Value);
                quote.PriceSales = GetDecimal(q.Element("PriceSales").Value);
                quote.PriceBook = GetDecimal(q.Element("PriceBook").Value);
                quote.ExDividendDate = GetDateTime(q.Element("ExDividendDate").Value);
                quote.PeRatio = GetDecimal(q.Element("PERatio").Value);
                quote.DividendPayDate = GetDateTime(q.Element("DividendPayDate").Value);
                quote.PegRatio = GetDecimal(q.Element("PEGRatio").Value);
                quote.PriceEpsEstimateCurrentYear = GetDecimal(q.Element("PriceEPSEstimateCurrentYear").Value);
                quote.PriceEpsEstimateNextYear = GetDecimal(q.Element("PriceEPSEstimateNextYear").Value);
                quote.ShortRatio = GetDecimal(q.Element("ShortRatio").Value);
                quote.OneYearPriceTarget = GetDecimal(q.Element("OneyrTargetPrice").Value);
                quote.Volume = GetDecimal(q.Element("Volume").Value);
                quote.StockExchange = q.Element("StockExchange").Value;
 * 
 */

