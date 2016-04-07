package com.utilities;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.*;


public interface  GlobalVars{

    //set input/output default files and paths
    String dumpPath = "C:\\Users\\Phil\\Google Drive\\GSU\\workspace\\/StockScreenEclipse\\Dump\\";
    String defaultTickerFile = "C:\\Users\\Phil\\Google Drive\\Stock%20Market\\Trades2.csv";

    String defaultTickerUFile = "C:\\Users\\Phil\\Google Drive\\GSU\\workspace\\StockAnalyst\\other\\CandidateTickers.csv";
    @Resource( name="jdbc/stockmarket" )
//            DataSource dataSource;
    //set limits on the number of shares loaded regardless of tickers listed in portfolio input files
    int intialSizeLimit = 40;
    int canidateSizeLimit = 40;
    //set default max capital to be committed to hedge
    Double defaultMaxNewCapital = 1000000d;
    //set default number of Short and Long positions desired in hedge
    int defaultMaxNewShorts = 5;
    int defaultMaxNewLongs = 5;
    //Set the attributes of the text display are
    int textAreaFontSize = 12;
    String textAreaFont = "Monospaced";
    //toggle whether to run with verbose output or not
    boolean DEBUG = false;
    int TRADING_DAYS_IN_YEAR = 250;
    boolean RUN_CONCURRENT = true;


    /**
     * 
     * <p><b>public enum barSize</b></p>
     * <p>Specifies the time interval of this candles stick</p>
     * <p><i>M5, M30, Hr, Day, Wk, Qtr</i></p>
     *
     */
    public enum barSize {

        M5, M30, Hr, Day, Wk, Qtr
    };

    
    public enum groupType { Layer, Scenario, Scenarios };
    
    public enum typeVaR {

        VarCoVaR95, VarCoVaR99, HistVaR95, HistVaR99
    };

    public enum typePattern {

        Gartley222, ABC, Butterfly, TigerGartley222, XABC, XABCD, TigerButterfly
    };

    /**
     * 
     * <p><b>public enum typeSignal</b></p>
     * <p>Specifies whether this order is a <i>Buy</i> or <i>Sell</i>.  In general,
     * the initial entry order will have an opposite signal than that of its later exit orders.  </p>
     * <p><i>Buy, Sell</i></p>
     *
     */
    public enum typeSignal {

        Buy, Sell
    };

    public enum initOrNew {

        Initial, Recommended
    };
    
    public enum tradePart {

        Entry, Exit
    };
    

    public enum typeLeg {

        XA, AB, BC, CD
    };
    /**
     * 
     * <p><b>public enum typeOrder</b></p>
     * <p>The type of order</p>
     * <p><i>Market, Limit, StopLoss, SimpleExit, Stop, StopLimit, TrailingStop, MOC, LOC </i></p>
     *
     */
    public enum typeOrder {

        Market, Limit, StopLoss, SimpleExit, Stop, StopLimit, TrailingStop, MOC, LOC
    };

    public enum orderLife {

       DAY, GoodTillCancelled, Extended;
    };  
    
    public enum direction {

        UP, DOWN
    };
    
        
    public enum orderStatus {

        Inactive, Active, Executed, Cancelled, Filled
    };
    //data and number display formats
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat df2 = new SimpleDateFormat("MM-dd-yyyy");
    DateFormat dfLongDateAndTime = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm:ss a");
    DateFormat dfYYYYMMdd = new SimpleDateFormat("yyyyMMdd");
    /**<p><b><i>df3</i></b> is used to format a JODA <b>DateTime</b> with pattern of "yyyy-MM-dd"</p>*/
    DateTimeFormatter df3 = DateTimeFormat.forPattern("yyyy-MM-dd");
    /**<p><b><i>df4</i></b> is used to format a JODA <b>DateTime</b> with pattern of "MM-dd-yyyy"</p>*/
    DateTimeFormatter dfMYYYY = DateTimeFormat.forPattern("M-d-yyyy");
    /**<p><b><i>df5</i></b> is used to format a JODA <b>DateTime</b> with pattern of "M-d-yyyy"</p>*/
    DateTimeFormatter dfMdYYYY = DateTimeFormat.forPattern("M-d-yyyy");
//	DecimalFormat ef = new DecimalFormat("$###,###,###.##");
    DecimalFormat ef = new DecimalFormat("'$'###,###,##0.00");
    DecimalFormat wf = new DecimalFormat("###,###,###");
    /**<p><b><i>wf2</i></b> is used to format a <b>Double</b> with pattern of ""###,###,##0.00""</p>  */
    DecimalFormat wf2 = new DecimalFormat("###,###,##0.00");
    /**percentage format %*/
    NumberFormat pf =  NumberFormat.getPercentInstance(Locale.US);
    NumberFormat cf = NumberFormat.getCurrencyInstance(Locale.US);
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    
    SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
    String inputString1 = "23 01 1997";
    String inputString2 = "27 04 1997";
    int outputLineWidth1=75;


}
