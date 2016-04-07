package com.workers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.workers.MoneyMgmtStrategy;
import com.oracle.tutorial.jdbc.TradeHistoryTable;
import com.utilities.*;
 
public  class DataLoader implements GlobalVars {	
	List<String> list;
 	List<String> names;
	List<Integer> positions;
	List<MoneyMgmtStrategy> mms = new ArrayList<MoneyMgmtStrategy> ();
	private ArrayList<String> tickers;
	private ArrayList<String> orderDates;
	private ArrayList<Double> stopLosses;
	private ArrayList<Double> entryPrices;
	private ArrayList<typeOrder> stopType;
	private ArrayList<String> stopActivationDate;
	private ArrayList<Double> stops;
	private ArrayList<Double> stopTriggers;
	private ArrayList<String> instrumentNames;
	private Portfolio belongsTo;
	
    public DataLoader() { super(); }
    
    public DataLoader(String filename, Portfolio belongsTo) {
		super();
		this.setBelongsTo(belongsTo);
		try {
			this.loadTradeHistoryTable();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
    
	@Override
	public String toString() {
		return "DataLoader [mms=" + mms + "]";
	}
    
    public void loadTradeHistoryTable() throws FileNotFoundException {
    	try {
			this.getTradeHistoryTable().loadTradeHistoryData(mms);
		} catch (SQLException e) {
			e.printStackTrace();
		}    
    }
    
    public void loadCSVfile2(String filename) throws FileNotFoundException {
    	//Get scanner instance
    	//Scanner scanner = new Scanner(new File(filename));
    	tickers = new ArrayList<String>();
    	orderDates = new ArrayList<String>();
    	positions = new ArrayList<Integer>();
    	stopLosses = new ArrayList<Double>();
    	entryPrices = new ArrayList<Double>();
    	stopType = new ArrayList<typeOrder>();
    	stopActivationDate = new ArrayList<String>();
    	stops = new ArrayList<Double>();
    	stopTriggers = new ArrayList<Double>();	    
    	instrumentNames = new ArrayList<String>();
    	Boolean firstLine = true;
    	String ls;

        /*	0. Symbol
         * 1.	Description
         * 2.	Shres
         * 3.	Buy Long
         * 4	Sell Short
         * 5	Stop
         * 6	Target One
         * 7	Target Initialize Date
         * 8 	Target Trigger Price
         * 9	Stop Type
         * 10	Stop Moved To
         * 11	Curent Price
         * 12	Yearly ROI on Current Price
         * 13	Today Best Price	
         * 14	Yearly ROI on Best Price
         * 15	Best Price Since Order
         * 16	Best Price Reach On	
         * 17   Days to Best Price	
         * 18	%PL	
         * 19	"Yearly ROI on Best Price
         * 20	Days in Play
         * 21	Risk ps (Loss)
         * 22	Risk (Loss)	Target
         * 23	Total Investment
         * 24	Buy Date
         * 25	Est Target Date
         * 26	Wks to Target
         * 27	Ratio (n:1)	Ratio (n:1) per wk	P/L PS	P/L	%PL	P/L PS	P/L	%PL	P/L PS	P/L	Worst Price	Below Stop	Profit Ticks	STOP Ticks	BE Stop Ticks	Weight	Weight	Weight	RiskPS	Move Missed	Move Missed %		Type	Newsletter
    */
    	
    	
    	try
    	{
    		BufferedReader br = new BufferedReader(new FileReader(filename));
    		String line;
    		Boolean started = false;
    		System.out.println("Loading security information from CSV file");

    		while ((line = br.readLine()) != null) 
    		{
    			if(!started){
    				if(line.startsWith("START")) started = true;
    			} else	{      
    	            String[] items = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    				//if(items.length != 6) continue; // something wrong with line
    				if(line.startsWith("EXIT")) break;
    				/*if(line.startsWith("GPL")) continue;
    				if(line.startsWith("TGB")) continue;
    				if(line.startsWith("HMY")) continue;*/
    			//	if(!line.startsWith("PAAS")) continue;
    				//if(!line.startsWith("PAAS")&&!line.startsWith("DE")) continue;
    			//	if(!line.startsWith("PAAS")&&!line.startsWith("DE")&&!line.startsWith("SPY")) continue;
    			//	if(!line.startsWith("PAAS")&&!line.startsWith("DE")&&!line.startsWith("SPY")&&!line.startsWith("IAG")) continue;
    			//	if(line.startsWith("IAG")) continue;
    				//if(!line.startsWith("AEM")) continue;
    				if(items[0].isEmpty()) continue;
    				ls = items[0];
    				TransactionData ts = new TransactionData();
    				tickers.add(items[0].trim());
    				ts.setTickerSymbol(items[0].trim());
    				
    				//Order Date
    				orderDates.add(this.parseDate(items[25].trim()));
    				DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
    				DateTime dt = formatter.parseDateTime(items[25].trim());
    				ts.setOrderDate(dt);
    				
    				ls = items[5].trim();
    				stopLosses.add(ls.isEmpty()?0.0d:Double.parseDouble(items[5].trim()));
    				ts.setStopLoss(ls.isEmpty()?0.0d:Double.parseDouble(items[5].trim()));
    				ls = items[3].trim();
    				String ss = items[4].trim();    				
    				double l = ls.isEmpty()?0.0d:Double.parseDouble(items[3].trim());
    				double s = ss.isEmpty()?0.0d:Double.parseDouble(items[4].trim());
    				int d = s==0?1:-1;
    				positions.add(d*Integer.parseInt(items[2].trim()));
    				ts.setPosition(d*Integer.parseInt(items[2].trim()));
    				entryPrices.add(l+s);
    				ts.setEntryPrice(l+s);
    				stopType.add(DataLoader.parseTypeOrder(items[9].trim()));
    				ts.setStopType(DataLoader.parseTypeOrder(items[9].trim()));
    				
    				//Stop Activatation Date
    				stopActivationDate.add(this.parseDate(items[7].trim()));
    				DateTime dt2 = formatter.parseDateTime(items[7].trim());
    				ts.setStopActivationDate(dt2);
    				
    				
    				ls = items[6];
    				stops.add(ls.isEmpty()?0.0d:Double.parseDouble(ls));
    				ts.setStop(ls.isEmpty()?0.0d:Double.parseDouble(ls));
    				ls = items[8].trim();
    				stopTriggers.add(ls.isEmpty()?0.0d:Double.parseDouble(ls));
    				ts.setStopTrigger(ls.isEmpty()?0.0d:Double.parseDouble(ls));
    				instrumentNames.add(items[1].trim());
    				ts.setInstrumentName(items[1].trim());
    				this.getBelongsToPortfolios().getTradeHistoryTable().saveMyTrade(ts);
    			}  
    		}
    		br.close();
    	}
    	catch(IOException e)
    	{
    		System.out.println("I/O Error : " + e);
    	}    
    	
    	try {
			this.getTradeHistoryTable().loadTradeHistoryData(mms);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	for(int i=0; i<tickers.size();i++){
    //		mms.add(new MoneyMgmtStrategy(tickers.get(i), instrumentNames.get(i), orderDates.get(i), positions.get(i), entryPrices.get(i), stopLosses.get(i), stopType.get(i), stopActivationDate.get(i),  stopTriggers.get(i), stops.get(i), null));
    	}//for
    	System.out.println("CSZ file successfully loaded");
    	
    }

	private TradeHistoryTable getTradeHistoryTable() {
		return this.getBelongsToPortfolios().getTradeHistoryTable();		
	}
	public static typeOrder parseTypeOrder(String s) {    	

    	switch(s)	{
    		case "Market":	return typeOrder.Market;
    		case "Limit":	return typeOrder.Limit;
    		case "Stop Loss":	return typeOrder.StopLoss;
    		case "StopLoss":	return typeOrder.StopLoss;
    		case "Simple Exit":	return typeOrder.SimpleExit;
    		case "SimpleExit":	return typeOrder.SimpleExit;
    		case "Stop":	return typeOrder.Stop;
    		case "Stop Limit":	return typeOrder.StopLimit;
    		case "StopLimit":	return typeOrder.StopLimit;
    		case "Trailing Stop":	return typeOrder.TrailingStop;
    		case "TrailingStop":	return typeOrder.TrailingStop;
    		case "MOC":	return typeOrder.MOC;
    		case "LOC":	return typeOrder.LOC;
    		}
			return null;	
    }
    
    private String parseDate(String date) {
		return date.replace("/", "-");
    	
    }
    
    public List<String> getList() {
		return list;
	}

	public List<String> getNames() {
		return names;
	}

	public List<Integer> getPositions() {
		return positions;
	}


	public List<MoneyMgmtStrategy> getMms() {
		return mms;
	}

	public void setMms(List<MoneyMgmtStrategy> mms) {
		this.mms = mms;
	}

	public Portfolio getBelongsTo() {
		return belongsTo;
	}

	public void setBelongsTo(Portfolio belongsTo) {
		this.belongsTo = belongsTo;
	}

    private Portfolios getBelongsToPortfolios() {
    	return this.getBelongsTo().getBelongsTo();		
	}
	
	public static void main(String[] args) 
    {
		//String filename = "C:\\Users\\Phil\\Google Drive\\GSU\\workspace\\StockAnalyst\\other\\Tickers.csv";
		String filename = "C:\\Users\\Phil\\Google Drive\\Stock Market\\Trades.csv";
		DataLoader dataloader = new DataLoader();
		try {
			dataloader.loadCSVfile2(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(dataloader.toString());
    }
}
