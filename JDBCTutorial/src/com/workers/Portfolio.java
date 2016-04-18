/*
 * This is the base class of both the initial portfolio and all candidate 
 * portfolios
 */

package com.workers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.math3.stat.correlation.Covariance;
//import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
//import org.jdom2.Element;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Entity;
import org.hibernate.annotations.Table;
import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.tradecoach.patenter.db.DBHelperDataImport;
import com.tradecoach.patenter.entity.security.CandleStick;
import com.tradecoach.patenter.entity.security.SecurityInst;
import com.tradecoach.patenter.entity.security.StopTestStats;
import com.tradecoach.patenter.processor.PatentParsingProcessor;
import com.gui.GUI;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Entity}
@Table(name = "portfolio_name")
public  class Portfolio extends PriceCollection implements Serializable {
	@Id 
	@SequenceGenerator(name="identifier", sequenceName="portfolio_id_seq",allocationSize=1) 
	@GeneratedValue(strategy=GenerationType.SEQUENCE,	generator="identifier")
	@Column(name = "id")
	private int id ;
	@Column(name = "portfolio_name")//, unique = true, index = true)
	private String portfolioName;
	private boolean runCurrent = true;
	private static final int NTHREDS = 10;
	private ArrayList<SecurityInst> HoldingSet = new ArrayList<SecurityInst> ();
	private List<MoneyMgmtStrategy> mmsSet = new ArrayList<MoneyMgmtStrategy> ();
	public Map<typeVaR,Double> aVaRs;
	private double corralation2initPortfolio, meanPL, varianceCoVariance;
	private int securityCount;
	private Portfolios belongsTo;
	private MoneyMgmtStrategy mms2bRun;
	private DataLoader dl;
	private boolean alreadyRanMyResults=false; 
    private Set<SecurityInst> securityInsts = new HashSet<SecurityInst>();
    private Config config;
    private DBHelperDataImport dbHelper;
    private Queue<SecurityInst> pairDownloadingQueue = new ArrayBlockingQueue<>(10000);
    private Queue<SecurityInst> pairParsingQueue = new ArrayBlockingQueue<>(10000);
    private Queue<String> prefixesQueue;
    private Queue<SecurityInst> dbQueue;

    private ReportPrinter reportPrinter;
	private PatentParsingProcessor patentParsingProcessor;
	private boolean isStarted;
    private static final Logger logger = LoggerFactory.getLogger(Portfolios.class);
	
	public Portfolio(String fname) {
		 this.setPortfolioName(fname);
		   }
	
	public Portfolio(MarketCalendar mc, String filename) {
		super(mc);
		DataLoader dl = new DataLoader(filename, this);
		List<String> list = dl.getList();
		List<String> names = dl.getNames();
		List<Integer> positions = dl.getPositions();
        for (int i = 0; i < list.size(); i++) {
        	if (i > intialSizeLimit)  break;
          	HoldingSet.add(new SecurityInst(mc, list.get(i), "", positions.get(i)));
        } //for
        this.setSecurityCount(this.getHoldingSet().size());
	} //Portfolio

	public Portfolio(String filename, Portfolios parent) {
		super();
		this.setBelongsTo(parent);//set which Portfolios object this Portfolio belongs too
		this.setRunCurrent(this.getBelongsTo().isRunCurrent());
		dl = new DataLoader(filename, this);
		mmsSet=dl.getMms();
		for (int i = 0; i < mmsSet.size(); i++) {
			SecurityInst si = new SecurityInst(mmsSet.get(i));
			si.setBelongsTo(this);
        	HoldingSet.add(si); 
        } //for 2
        this.setSecurityCount(this.getHoldingSet().size());
	} //Portfolio

	public Portfolio(MarketCalendar mc, List<String> list, List<Integer> positions) {
		super(mc);
		/*
		 * This constructor adds each security name to the portfolio
		 * but does not load the historical prices yet
		 */
	    for (int i = 0; i < list.size(); i++) {
        	HoldingSet.add(new SecurityInst(mc, list.get(i), positions.get(i)));  
        } //for
        this.setSecurityCount(this.getHoldingSet().size());
	} //Portfolio(List list)
	
	public Portfolio(Portfolio p) {
		//super(new MarketCalendar(p.getStartDate(), p.getEndDate()));
		this.setHoldingSet(p.getHoldingSet());
		this.setBelongsTo(p.getBelongsTo());
//		this.setDailyPL(p.getDailyPL());
		this.setSecurityCount(p.getSecurityCount());
		this.setPortfolioValue(p.getPortfolioValue());
	}//Portfolio
	
	@SuppressWarnings("null")
	public Portfolio(MarketCalendar mc, List<String> list) {
		super(mc);
		//create a portfolio with all initial holding amounts set to zero
		ArrayList<Integer> positions = new ArrayList<Integer>(Collections.nCopies(list.size(), 0));
		//create a list of zeros the same as the ticker list
		for (int i = 0; i < list.size(); i++)  positions.add(0); 
        for (int i = 0; i < list.size(); i++) {
        	HoldingSet.add(new SecurityInst(mc, list.get(i), positions.get(i))); 
        } //for 2
        this.setSecurityCount(this.getHoldingSet().size());
	}//Portfolio

	@SuppressWarnings("null")
	public Portfolio(MarketCalendar mc, List<MoneyMgmtStrategy> list, int x) {
		super(mc);
		//create a portfolio with all initial holding amounts set to zero
		ArrayList<Integer> positions = new ArrayList<Integer>(Collections.nCopies(list.size(), 0));
		//create a list of zeros the same as the ticker list
		for (int i = 0; i < list.size(); i++)  positions.add(0); 
        for (int i = 0; i < list.size(); i++) {
        	HoldingSet.add(new SecurityInst(mc, list.get(i), 0)); 
        } //for 2
        this.setSecurityCount(this.getHoldingSet().size());
	}//Portfolio

	
	public Portfolio() {
	}
	
	 public Portfolio(Config config) {
	        this.config = config;
	        //Create Redisson object
	     //   org.redisson.Config redisConfig = new org.redisson.Config();
	     //   redisConfig.useSingleServer()
	     //           .setAddress(config.getString("redis_url"));

	     //   redisson = Redisson.create(redisConfig);
	        setDbQueue(redisson.getQueue(this.config.getConfig("dbSerializer").getString("redis_queue_name")));
	       // logger.info("DB queue size:", dbQueue.size());
	    }

	    public Portfolio withDBModule() throws SQLException {

	       	this.setDbHelper(new DBHelperDataImport(getDbQueue(), config.getConfig("dbSerializer")));
	       //this.dbHelper = new DBHelperDataImport(getDbQueue(), config.getConfig("dbSerializer"));
	        
	        return this;
	    }

	    public Portfolio withProcessing() throws ParserConfigurationException {
	    		prefixesQueue = com.tradecoach.patenter.parsers.utils.UtilTools.getXMLFileNames();
	    
	    	patentParsingProcessor = new PatentParsingProcessor(
	    			prefixesQueue,
	    			pairDownloadingQueue,
	    			config.getConfig("patentParser"));
/*	    	pairDownloadingProcessor = new PAIRDownloadingProcessor(
	    			pairDownloadingQueue,
	    			pairParsingQueue,
	    			getDbQueue(),
	    			config.getConfig("PAIRDownloader"));
	    	pairParserProcessor = new PAIRParserProcessor(
	    			pairParsingQueue,
	    			getDbQueue(),
	    			config.getConfig("PAIRParser"));*/

	    //	reportPrinter = new ReportPrinter(pairDownloadingProcessor, patentParsingProcessor, pairParserProcessor, pairDownloadingQueue, pairParsingQueue, prefixesQueue, getDbQueue());
	    	return this;
	    }

	    public boolean start() {
	        if (isStarted){
	            return false;
	        }
	        logger.info("Starting application");
	        this.getDbHelper().start();
	        patentParsingProcessor.start();
	     //    pairDownloadingProcessor.start();
	    //    pairParserProcessor.start();
	       reportPrinter.start();
	        logger.info("Application started");
	        return true;
	    }

	    public boolean stop() {
	        if(isStarted){
	            return false;
	        }
	        logger.info("Stopping application");
	        if (dbHelper != null) dbHelper.shutdown();
	        if (patentParsingProcessor != null) patentParsingProcessor.shutdown();
	   //     redisson.shutdown();
	    //    if (pairParserProcessor != null) pairParserProcessor.shutdown();
	     //   if (pairDownloadingProcessor != null) pairDownloadingProcessor.shutdown();
	        if (reportPrinter != null) reportPrinter.shutdown();
	        logger.info("Application stopped successfully");
	        return true;
	    }

	public void createMMS(){
		for (int i = 0; i < 2; i++) {
          	mmsSet.add(new MoneyMgmtStrategy());
        } //for
	}
	
	public void pushDownMMS(){
		Iterator<SecurityInst> i = HoldingSet.iterator();
        while (i.hasNext()) {
       //  	i.next().createMMS(orderDate, orderQty, entryPrice, stopLossPrice, stopPrice);
        }  //while   
	}
	
	@SuppressWarnings("unused")
	protected void intialize() {
		super.intialize();
		aVaRs = new HashMap<typeVaR, Double>();
	}

	public void loadHistoricalPriceData(){
            /*
             * This loads the daily price data of each security into a individual
             * candlestick object.
             */
		Iterator<SecurityInst> i = HoldingSet.iterator();	
		if (this.isRunCurrent()) {			
			ExecutorService executor = Executors.newFixedThreadPool( NTHREDS);	
			while (i.hasNext()) {
				executor.execute(i.next());
			}  //while 
				// This will make the executor accept no new threads
			// and finish all existing threads in the queue
			executor.shutdown();
			// Wait until all threads are finish
			try {
				executor.awaitTermination(60000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		//	System.out.println("Finished all threads");
		}
		else {            
			while (i.hasNext()) {
				try {
					SecurityInst si = i.next();
					si.setExecutionLevel(si.loadHistoricalPriceData());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}  //while  
		}
	}//loadHistoricalPriceData
	
	/**
	 * public void executeOrders()
	 * <p>This loads the daily price data of each security into a individual candlestick object.</p>
	 */
	@SuppressWarnings("unchecked")
	public void executeOrders(){
		if(!this.isAlreadyRanMyResults())
			Collections.sort(this.getHoldingSet());
		//Tools.drawSeprator();
	//	Tools.drawTitle("Executing orders of initial portfolio");
	//	Tools.drawSeprator();
		this.executeOrders(null);
//		try {
//			this.getBelongsTo().insertMyResultsROI(this.getROI()*100);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//		this.getBelongsTo().insertROI2HtmlTextPane(this.getROI()*100);

//		try {
//			this.getBelongsTo().insertROI("test this ROI", htmlDoc);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	/*public void executeOrders2(){
		
		 * This loads the daily price data of each security in this <b>Portfoio</b> instance into a individual
		 * candlestick object.
		 
		if (this.isRunCurrent()) {
			ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
			Iterator<SecurityInst> i = HoldingSet.iterator();
			Iterator<SecurityInst>  j= HoldingSet.iterator();
			while (i.hasNext()) {
				executor.execute(i.next());
			}  //while 
			// This will make the executor accept no new threads
			// and finish all existing threads in the queue
			executor.shutdown();
			// Wait until all threads are finish
			try {
				executor.awaitTermination(60000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//System.out.println("Finished all threads");
			
			while (j.hasNext()) {
			//	System.out.println("made it here");
				MoneyMgmtStrategy mms = j.next().getMms();
				if(mms.getBelongsTo().getExecutionLevel()==2) {
				//	System.out.println("made it here2");
					System.out.println(mms.toString2());
				}				
			}  //while
			Tools.drawSeprator();
		}
		else {   


			try {
				Iterator<SecurityInst> i = HoldingSet.iterator();
				while (i.hasNext()) {
					i.next().executeOrder();
				}  //while
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Tools.drawSeprator();
		}
		System.out.println(this.toString());

	}//executeOrder
*/	
	/** <p>Executes each <b>Security</b> instance in this <b>Portfolio</b> instance by applying the stop parameters specified in <i>mms</i></p>
	 * @param mms  	 */
	public void executeOrders(MoneyMgmtStrategy mms){
		this.setMms2bRun(mms);
		if (this.isRunCurrent()) {
			ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
			Iterator<SecurityInst> i = HoldingSet.iterator();
			Iterator<SecurityInst> j = HoldingSet.iterator();
			while (i.hasNext()) {
				executor.execute(i.next());
			}  //while 
			// This will make the executor accept no new threads and finish all existing threads in the queue
			executor.shutdown();
			// Wait until all threads are finish
			try {
				executor.awaitTermination(60000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(this.getMms2bRun()==null)  {	//if true this a MyResults loop, else its a Max ROI loop	
				while (j.hasNext()) {
					SecurityInst si = j.next();
					if(si.getExecutionLevel()==3) {
						for(MoneyMgmtStrategy mms1 : si.getMmsSet()) {
							//		if(mms1.getIdNum()==this.belongsTo.getCurrentScenario().getIdNum())
							//				System.out.println(mms1.toString2());
						}
					} 
					else if(si.getExecutionLevel()==2) {	
						this.getBelongsTo().appendToTextPane(si.getMms().toString2(), this.getBelongsToGUI().getMyResultsTextPane());
						si.getMms().resetTradeActivity();
					}
				}  //while
				this.getBelongsTo().appendToTextPane("<hr><hr>"+this.toString()+"<hr>", this.getBelongsToGUI().getMyResultsTextPane());

			} else { //this is  Scenarios loop
				//System.out.println(this.getMms2bRun().toString2());
				this.getBelongsTo().appendToTextPane(this.getMms2bRun().toString2(), this.getBelongsToGUI().getScenariosTextPane());
			//	this.getBelongsTo().appendToTextPane("<hr><hr>"+this.toString()+"<hr>", this.getBelongsToGUI().getScenariosTextPane());
			}
		//	Tools.drawSeprator();	
		} else {   
			try {
				System.out.println("running NOT concurrnet");
				Iterator<SecurityInst> i = HoldingSet.iterator();
				while (i.hasNext()) {
					SecurityInst si = i.next();
					if (si.getExecutionLevel()==1) //1 means has not executed the inital orders yet
						si.setExecutionLevel(si.executeOrder());
					else if (si.getExecutionLevel()==2 && si.getBelongsTo().getMms2bRun().getImposedExitType()!=null) //2 means has not executed candidate scenarios yet
						si.setExecutionLevel(si.executeOrder(si.getBelongsTo().getMms2bRun()));
					//i.next().executeOrder(mms);
				}  //while
			} catch (Exception e) {
				e.printStackTrace();
			}
			Tools.drawSeprator();
		}
		System.out.println(this.toString());

	}//executeOrder
 
	public void resetPortfolio(){
		try {
			System.out.println("resetting of component class instances");
			Iterator<SecurityInst> i = HoldingSet.iterator();
			while (i.hasNext()) {
				SecurityInst si = i.next();
				si.resetSecurity();
			//	si.setExecutionLevel(1);
			//	si.setExecutionLevel(si.executeOrder(si.belongsTo.getMms2bRun()));
			}  //while
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public void saveHistoricalPriceData(){
        /*
         * This loads the daily price data of each security into a individual
         * candlestick object.
         */
        Iterator<SecurityInst> i = HoldingSet.iterator();
        while (i.hasNext()) {
            SecurityInst si = i.next();
            si.DeleteHistory();
            si.LoadHistory();                    
        }  //while      
	}//loadHistoricalPriceData
    
    /**
     * Resets the execution level variables for all securities belonging to this 
     * <b>Portfolio</b> instance such that the next scenario can be executed.
     * @param j
     */
    public void reset4NextSenario() {
    	this.setExecutionLevels(2);
    	try {
			this.getBelongsTo().getCoutLock().reset();
		} catch (NullPointerException e) {
			System.out.printf("CoutLock is null ");
			e.printStackTrace();
		}
    }
    /**
     * Resets the execution level variables for all securities belonging to this 
     * <b>Portfolio</b> instance such that the next scenario can be executed.
     * @param j
     */
    public void reset4MyResultsRerun() {
    	this.resetPortfolio();
    	this.setExecutionLevels(1);
    	try {
			this.getBelongsTo().getCoutLock().reset();
		} catch (NullPointerException e) {
			System.out.printf("CoutLock is null ");
			e.printStackTrace();
		}
    }
	
    
    
    /**
     * Resets the execution level variables for all securities belonging to this 
     * <b>Portfolio</b> instance to the parameter passed <i>j</i>
     * @param j
     */
    public void setExecutionLevels(int j){    
        Iterator<SecurityInst> i = HoldingSet.iterator();
        while (i.hasNext()) {
            i.next().setExecutionLevel(j);                  
        }  //while      
	}//loadHistoricalPriceData
    
	public void sumPL_by_Date() {
		AddPriceData(HoldingSet);		
	}
	
	
	public void doEachPL_by_Date() {
        Iterator<SecurityInst> i = HoldingSet.iterator();
        while (i.hasNext()) {
        	i.next().AddPriceData();
        }  
	}
	
	public void corralateToOtherPortfolio(Portfolio otherPortfolio) {
	//	PearsonsCorrelation pc = new PearsonsCorrelation();
		double x;
		double[] b;
		double[] a = otherPortfolio.getDailyPL_asDouble();
		for (int i = 0; i < HoldingSet.size(); i++) {	
			b = HoldingSet.get(i).getDailyPL_asDouble();
//			x = pc.correlation(a, b); 
//			HoldingSet.get(i).setCorralationToInitPortfolio(x);
		}
		Collections.sort(HoldingSet, new Comparator<SecurityInst>() {
			@Override
			public int compare(SecurityInst arg0, SecurityInst arg1) {
				return  arg0.getCorralationToInitPortfolio().compareTo(arg1.getCorralationToInitPortfolio());
			}
	    });
		
		//sort holding set by corralations
	//	Quicksort qs = new Quicksort();
	//	qs.sort(HoldingSet);
		
		/*		
		while (i.hasNext()) {
	    	x = pc.correlation(otherPortfolio.getPortfolioVaR().getDailyPL_asDouble(), i.next().getVaRs().getDailyPL_asDouble());
	    	//i.previous();
	    	y=i.nextIndex()-1;
	    //	i.next().setCorralationToInitPortfolio(x);
	    	HoldingSet[y].setCorralationToInitPortfolio(x);
	        }	*/
		//sort the holdingSet by thier corralation to the init portfolio

	}//corralateToOtherPortfolio
	
	public void dumpPortfolioData(String fn) {
		double priorClose=0.0d, close=0.0d, posD=0.0d, change=0.0d;
		String name, ts, dt = null, hs, cs = "", ch,  x, ext = null;
		boolean firstPass=true;
		try {  
            FileWriter fr = new FileWriter(fn);  
            BufferedWriter br = new BufferedWriter(fr);  
            PrintWriter out = new PrintWriter(br); 
            x = "Company,Ticker,Shares,Date,Close,Change,Extended\n";
			//if(x != null) out.write(x);    
		    for(int i=0; i < HoldingSet.size(); i++)  {
		    	name = HoldingSet.get(i).getinstrumentName().replaceAll(",", ";");
				ts = HoldingSet.get(i).getTickerSymbol();
				posD = HoldingSet.get(i).getPosition();
				hs = wf.format(posD);
				
				Iterator<CandleStick> j = HoldingSet.get(i).getCandleSticks().getCandleSticks().iterator();
			   	CandleStick c = null;
			   	while (j.hasNext()) {
				/*'j' is the holdings object of this portfolio class instance
				   loop thorough each instrument and then then each daily candlestick
				   and add/subtract its price change to the overall VaR of the portfolio
				   
				   NOTE:  loop is from going from most recent date and moving backwards*/ 
				  c = j.next();
			   	  if (!firstPass) {			   		   
			   		   change = close-c.getClosePrice();			   		   
					   ch = ef.format(change);
					   ext = ef.format(posD * change);
					   x += name + "," + ts + "," + hs +"," + dt + "," + cs  + "," + ch  + "," + ext +"\n";
			   	  }
			   	   firstPass=false;
				   dt = df.format(c.getDate());
				   close = c.getClosePrice();				   
				   cs = ef.format(close);
			   	} //while	
			firstPass=true;   	
			}//for
		    if(x != null) out.write(x); 
		    out.close();
	    } //try
		catch(IOException e){ System.out.println(e); }  
	} //dumpPortfolioData
		
	public void RunStats() {
		super.RunStats();   	    
		aVaRs.put(typeVaR.HistVaR95, this.getMeanPL()-this.getStdDevPL() * 1.960);
		aVaRs.put(typeVaR.HistVaR99, this.getMeanPL()-this.getStdDevPL() * 2.576);
	//	this.doVarianceCovariance();
	}//RunStats

	public void doVarianceCovariance() {
//		Covariance pc = new Covariance();
		double sd, sd1, sd2, cv, x = 0.0d, y = 0.0d, m, w1, w2;
		for (int i = 0; i < HoldingSet.size(); i++) HoldingSet.get(i).RunStats();
		for(int i=0; i < HoldingSet.size(); i++) {
			sd1 = HoldingSet.get(i).getStdDevPrice();
			w1 = HoldingSet.get(i).getPosition() * HoldingSet.get(i).getLastClosePrice() / this.getPortfolioValue();
			x += w1 * sd1 * sd1;
			double[] a1 = HoldingSet.get(i).getDailyPrices_asDouble();
			for(int j=0; j < HoldingSet.size(); j++){
				if(i==j) continue;
		//		System.out.printf("i = %d & j = %d\n", i, j);
				/*
				sd1 = HoldingSet.get(i).getStdDevPL();
				sd2 = HoldingSet.get(j).getStdDevPL();
				double[] a1 = HoldingSet.get(i).getDailyPL_asDouble();
				double[] a2 = HoldingSet.get(i).getDailyPL_asDouble();
				*/				
				sd2 = HoldingSet.get(j).getStdDevPrice();	
				w2 = HoldingSet.get(j).getPosition() * HoldingSet.get(j).getLastClosePrice() / this.getPortfolioValue();
				double[] a2 = HoldingSet.get(j).getDailyPrices_asDouble();
//				cv = pc.covariance(a1, a2);				
//				y += w1 * w2 * sd1 * sd2 * cv;	
			}		
		} //for's
		y = Math.abs(y);
		sd = Math.sqrt(x + (2 * y));
	//	this.setVarianceCoVariance(sd);
	//	m=this.getMeanPL();
	//	aVaRs.put(typeVaR.VarCoVaR95, -sd * 1.960);
	//	aVaRs.put(typeVaR.VarCoVaR99, -sd * 2.576);
		aVaRs.put(typeVaR.VarCoVaR95, this.getMeanPL() - sd * 1.960);
		aVaRs.put(typeVaR.VarCoVaR99, this.getMeanPL() - sd * 2.576);
	}  //doVarianceCovariance
	
	public String toString(MarketCalendar mc) {
		String s = "Portfilio Securities:\n" +
				   "Historical VaR at 95% confidence:  " + this.getaVaRs().get(typeVaR.HistVaR95) + "\n" +
				   "VarianceCovariance VaR at 95% confidence:  " + this.getaVaRs().get(typeVaR.VarCoVaR95) + "\n\n";;
		for (int i = 0; i < HoldingSet.size(); i++) {
			s += HoldingSet.get(i).toString() + "\n";
			s += HoldingSet.get(i).getMms().toString() + "\n\n";
		}		
		s += mc.toString();		
		return s;		
	} //toString
		
	@Override
	public String toString() {
		//Tools.drawSeprator();
	//	String s =String.format("\nPortfolio Weighted Average ROI: %.4f%%\n\n",this.getROI()*100);
		String s =String.format("<p>Portfolio Weighted Average ROI: <b>%.4f%%</b></p>",this.getROI()*100);
		//Tools.drawSeprator();
	//	s +="Portfolio [HoldingSet=" + HoldingSet + "]";
		return s;
	}

	public String toStringPrintSecuritiesAt(int indexFrom, int indexTo) {	
	//	NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
		Double t = 0d, tt = 0d;
		int cnl=0;
		int x1 = 5;
		int x2 = 24;
		int x3 = 12;
		int x4 = 12;
		int x7 = 12;
		int x8 = 12;
		String x5 = x1 + ".2f";
		String x6 = ".20f";
		String ss = null;
		String f = "%-"+ x1 +"s%-" + x2 +  "s%-" + x3 + "s%" + x4 +"s%" + x7 +"s%" + x8 + "s\n";
/*		String s = "\n\nSecurities Recommeded to by Added to Portfolio:\n\n" + 
		            String.format(f,"","Compnany","Ticker","Shares","Last","Extended") +
					String.format(f,"No.","Name","Symbol","to Add","Close","Price") +		
                    StringUtils.repeat("-", x1+x2+x3+x4+x7+x8)+"\n";
		String f2 = "%-"+ x1 + "d%-" + x2 +"s%-" + x3 +"s%" + x4  + "s%" + x7   + "s%" + x8 +"s\n";
		for (int i = indexFrom; i <= indexTo  ; i++) { 
			
			if (this.getHoldingSet().get(i).getPosition() > 0)
				t += this.getHoldingSet().get(i).getPosition() * this.getHoldingSet().get(i).getLastClosePrice();
			else
				tt += this.getHoldingSet().get(i).getPosition() * this.getHoldingSet().get(i).getLastClosePrice();
			
			cnl=Math.max(this.getHoldingSet().get(i).getinstrumentName().length(), cnl);
			ss = String.format(f2, i, this.getHoldingSet().get(i).getinstrumentName(),
									 this.getHoldingSet().get(i).getTickerSymbol(),
									 this.getHoldingSet().get(i).getPosition(),
									 ef.format(this.getHoldingSet().get(i).getLastClosePrice()),
									 ef.format(this.getHoldingSet().get(i).getPosition() * this.getHoldingSet().get(i).getLastClosePrice() )
									 );
			s += ss;
	//		t += t;
		//	tt += tt;
	
        }	//for

		String ts = ef.format(t);
		String tts = ef.format(tt);
		String ait = "Additional Long Investment Total";
		String aitt = "Short Contract Committment Total";
		x3 = Math.max(cnl - aitt.length(),1) ;
		x4 = Math.max(x3 + cnl - aitt.length(),1) ;
		x2 = x2+x3+x4+x7;
		f2 = "%-"+ x1 +"s%-" + x2 +"s%" + x8 + "s\n";
//		s += String.format(f,"","","","","", StringUtils.repeat("-", ts.length()) + "\n\n" + 
//			 String.format(f2,"",ait,ts)) + "\n" +
//			 String.format(f2,"",aitt,tts);*/
		//return s + "\n";
                return "";
	} //toString 
	
	public double getVarianceCoVariance() {
		return varianceCoVariance;
	}

	public void setVarianceCoVariance(double varianceCoVariance) {
		this.varianceCoVariance = varianceCoVariance;
	}

	public ArrayList<SecurityInst> getHoldingSet() {
		return HoldingSet;
	}

	public SecurityInst findSecurityUsingSymbol(String ticker){
		for(SecurityInst si : this.getHoldingSet())
			if(si.getTickerSymbol().compareTo(ticker)==0)
				return si;
			
		return null;
	}

	public void setSelectedSecurityUsingSymbol(String ticker, boolean selected){
		for(SecurityInst si : this.getHoldingSet())
			if(si.getTickerSymbol().compareTo(ticker)==0)
				 si.setSelectedInstrument(selected);		
	}
	public void setSelectedSecurityUsingSymbol(Date orderDate, String ticker, boolean selected){
		for(SecurityInst si : this.getHoldingSet())
			if(si.getMms().getOrder().getOrderDate().compareTo(orderDate)==0&&si.getTickerSymbol().compareTo(ticker)==0)
				 si.setSelectedTrade(selected);		
	}
	public double getROI() {
		//find weighted avg ROI for each security based on days in play
		double roi=0d;
		double x=0d;
		int totalDays=0;
		for (int i = 0; i < HoldingSet.size(); i++) 
		//	if (this.InitialOrderFilled(i, "tempMMS")) {
			 if (HoldingSet.get(i).getMms().getOrder().getOrderStatus()==orderStatus.Filled) {
				 totalDays += HoldingSet.get(i).getMms().getDays();
		//	 System.out.println(HoldingSet.get(i).getMms().getTickerSymbol()+", Days:  "+HoldingSet.get(i).getMms().getDays());
				// System.out.println(HoldingSet.get(i).getMms().getDays());
			 }
		for (int i = 0; i < HoldingSet.size(); i++) 
			if (HoldingSet.get(i).getMms().getOrder().getOrderStatus()==orderStatus.Filled) {
				 x = HoldingSet.get(i).getROIweightedContribution(totalDays);
				roi += x;
		}
		return roi;		
	}//getROI
	
	public double getROI(String mmsType) {
		//find weighted avg ROI for each security based on days in play
		double roi=0d;
		double x=0d;
		int totalDays=0;
		for (int i = 0; i < HoldingSet.size(); i++) 
			 if (HoldingSet.get(i).getMmsByType(mmsType).getOrder().getOrderStatus()==orderStatus.Filled) {
				 totalDays += HoldingSet.get(i).getMmsByType(mmsType).getDays();
		//	 System.out.println(HoldingSet.get(i).getMms().getTickerSymbol()+", Days:  "+HoldingSet.get(i).getMms().getDays());
				// System.out.println(HoldingSet.get(i).getMms().getDays());
			 }
		for (int i = 0; i < HoldingSet.size(); i++) 
			if (HoldingSet.get(i).getMmsByType(mmsType).getOrder().getOrderStatus()==orderStatus.Filled) {
				 x = HoldingSet.get(i).getROIweightedContribution(totalDays);
				roi += x;
		}
		return roi;		
	}//getROI
	

	
	
	public MoneyMgmtStrategy getMms2bRun() {
		return mms2bRun;
	}

	public void setMms2bRun(MoneyMgmtStrategy mms2bRun) {
		this.mms2bRun = mms2bRun;
	}

	public boolean InitialOrderFilled (int i, String sMMS) {
		SecurityInst s = this.getHoldingSet().get(i);
		if(sMMS == "tempMMS") 
			return s.getMmsTemp().getOrder().getOrderStatus()==orderStatus.Filled;
		else
			return s.getMms().getOrder().getOrderStatus()==orderStatus.Filled;		
	}

	public void setHoldingSet(ArrayList<SecurityInst> holdingSet) {
		//HoldingSet = holdingSet;
		//create a new object as Java must pass 'holdingSet' by value of reference to the arraylist
		//class instance.  This seems to accomplish a pass by value to the clone
		HoldingSet = new ArrayList<SecurityInst> (holdingSet);
		for(SecurityInst si : HoldingSet ) {
			si.setBelongsTo(this);
		}
	}
	
	public void addSecurities(SecurityInst s, int shares) {
		SecurityInst s2 = new SecurityInst (s);
		s2.setBelongsTo(this);
		//add the security to the portfolio list ...
		HoldingSet.add(new SecurityInst (s2));
	//	HoldingSet.add(new SecurityInst (s));
		//and adjust the portfolio PL
		AddPriceData(s.getCandleSticks(), shares);		
	}
	
	public Map<typeVaR, Double> getaVaRs() {
		return aVaRs;
	}
	
	
	public String getPortfolioName() {
		return portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}
		
	/**
	 * Loop through the <b>mms</b> of each <b>Security</b> instance in the holdingSet and obtain
	 * the statistics related to its stop.  Add these statistics to the <b>StopTestStats</b> of this
	 * <b>Portfolio</b>.  Pass the <i>currentDate</i> as the cut-off date for which to run the analysis.
	 * A <b>StopTestStats</b> instance is return representing the collective statistical results for 
	 * this <b>Portfolio</b> as to the stops of its memeber securities. 
	 * @param currentDate
	 * @return StopTestStats
	 * @throws Exception 
	 */
	public StopTestStats getStopTestResults(Date currentDate) throws Exception {
		StopTestStats sts = new StopTestStats();
		for (int i = 0; i < HoldingSet.size(); i++) 
			sts.addTestStats(HoldingSet.get(i).getMms().getStopTestResults(currentDate));
		return sts; 
	}
	

	public void setaVaRs(Map<typeVaR, Double> aVaRs) {
		this.aVaRs = aVaRs;
	}

	public int getSecurityCount() {
		return securityCount;
	}

	public void setSecurityCount(int securityCount) {
		this.securityCount = securityCount;
	}

	public Portfolios getBelongsTo() {
		return belongsTo;
	}
	
		
	/**set which <b>Portfolios</b> object this <b>Portfolio</b> ojbect belongs too*/
	public void setBelongsTo(Portfolios belongsTo) {
		this.belongsTo = belongsTo;
	}
	
	public GUI getBelongsToGUI() {
		return this.getBelongsTo().getBelongsToGUI();
	}
	
	public boolean isRunCurrent() {
		return runCurrent;
	}

	public void setRunCurrent(boolean runCurrent) {
		this.runCurrent = runCurrent;
	}
	
	public DataLoader getDl() {
		return dl;
	}

	public void setDl(DataLoader dl) {
		this.dl = dl;
	}
	 
    public void addUser(SecurityInst securityInst) {
        this.securityInsts.add(securityInst);
    }

	public boolean isAlreadyRanMyResults() {
		return alreadyRanMyResults;
	}

	public void setAlreadyRanMyResults(boolean alreadyRanMyResults) {
		this.alreadyRanMyResults = alreadyRanMyResults;
	}

	public DBHelperDataImport getDbHelper() {
		return dbHelper;
	}

	public void setDbHelper(DBHelperDataImport dbHelper) {
		this.dbHelper = dbHelper;
	}

	public Queue<SecurityInst> getDbQueue() {
		return dbQueue;
	}

	public void setDbQueue(Queue<SecurityInst> dbQueue) {
		this.dbQueue = dbQueue;
	}

	public static void main(String[] args) throws InterruptedException {
		List<String> tickers = Arrays.asList("ORCL", "MSFT", "GOOG");
		//List<String> tickers = Arrays.asList("GOOG", "GOOG", "GOOG");
		List<String> instrumentNames = Arrays.asList("Oracle Corp.", "Microsoft Corp.", "Google Inc.");
		List<Integer> positions = Arrays.asList(100, 150, 200);
		List<Double> entryPrices = Arrays.asList(100d, 150d, 200d);
		List<String> orderDates = Arrays.asList("02-25-2014", "02-25-2014", "02-25-2014");
		List<Double> stopLosses = Arrays.asList(100d, 150d, 200d);
		List<Double> stops = Arrays.asList(100d, 150d, 200d);
		
		Date startDate;
		Date endDate;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 6);
        cal.set(Calendar.DATE, 30);
        cal.set(Calendar.YEAR, 2013);
        startDate = cal.getTime();
        cal.set(Calendar.YEAR, 2014);
        endDate = cal.getTime();
		double start = System.currentTimeMillis();
		MarketCalendar mc = new MarketCalendar(startDate, endDate);
		List<MoneyMgmtStrategy> mms = new ArrayList<MoneyMgmtStrategy> ();
		for(int i = 0; i<tickers.size();i++){
			//MoneyMgmtStrategy m = new MoneyMgmtStrategy(tickers.get(i), instrumentNames.get(i), orderDates.get(i), positions.get(i), entryPrices.get(i), stopLosses.get(i), stops.get(i));
			//mms.add(m);
	//		mms.add(new MoneyMgmtStrategy(tickers.get(i), instrumentNames.get(i), orderDates.get(i), positions.get(i), entryPrices.get(i), stopLosses.get(i), stops.get(i), null));
		}
		//Portfolio p = new Portfolio(mc, tickers, positions);
		Portfolio p = new Portfolio(mc, mms, 0);
		p.loadHistoricalPriceData();
		p.executeOrders();
		p.RunStats();
//		p.calcVaR(startDate, endDate);
		double end = System.currentTimeMillis();
		double duration = (end - start) / 1000;
		System.out.println(p.toString(mc));
		System.out.println("Portfolio build took " + duration + " seconds.");
	}

}