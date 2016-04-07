package com.utilities;

public class BuildScripts {	
	/*
	 CREATE VIEW V_SECURITIES (PORTFOLIO_NAME, PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE, SELECTED) AS 
select ph.PORTFOLIO_NAME, s.*  from APP.SECURITIES s,APP.PORTFOLIO_HOLDINGS ph 
where s.TICKER_SYMBOL = ph.TICKER_SYMBOL;
	 */
	public static String V_SECURITIES =	
	//"CREATE VIEW V_SECURITIES (PORTFOLIO_NAME, PORTFOLIO_ID, TICKER_SYMBOL, COMPANY_NAME, BETA_VALUE, SELECTED) AS " +
			"CREATE VIEW V_SECURITIES AS " +
					"select s.TICKER_SYMBOL, ph.PORTFOLIO_NAME, ph.SELECTED, s.BETA_VALUE, s.COMPANY_NAME  " +
					  "from APP.SECURITIES s, APP.PORTFOLIO_HOLDINGS ph " +
					 "where s.TICKER_SYMBOL = ph.TICKER_SYMBOL";
	
	//CREATE VIEW V_SECURITIES (TICKER_SYMBOL, PORTFOLIO_NAME, SELECTED, BETA_VALUE, COMPANY_NAME) AS
	//select s.TICKER_SYMBOL, ph.PORTFOLIO_NAME,ph.SELECTED,  s.BETA_VALUE, s.COMPANY_NAME from APP.SECURITIES s, APP.PORTFOLIO_HOLDINGS ph where s.TICKER_SYMBOL = ph.TICKER_SYMBOL;


	
	public static String V_STRING_PARAMS =	
	"CREATE VIEW V_STRING_PARAMS (PARAM_NAME, VAL) AS " +
					"select param_name, strval as val from parameters where strval is not null";

	public static String[][] getBuildDDL(){
		String[][] buildDDL = new String [][] { 
			{ "V_SECURITIES",V_SECURITIES},
            { "V_STRING_PARAMS", V_STRING_PARAMS} };		
		return buildDDL;
		
	}

}
