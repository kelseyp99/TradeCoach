create table SECURITIES
  (
  PORTFOLIO_ID integer NOT NULL,
  TICKER_SYMBOL varchar(10) NOT NULL,
  COMPANY_NAME varchar(40) NOT NULL,
  BETA_VALUE double,
  PRIMARY KEY (TICKER_SYMBOL));
  
  
  CREATE TABLE HISTORICAL_PRICES (

  	TICKERSYMBOL VARCHAR(20) NOT NULL,
	TRADEDATE DATE NOT NULL,
	CLOSEPRICE DOUBLE NOT NULL,
	OPENPRICE DOUBLE NOT NULL,
	HIGHPRICE DOUBLE NOT NULL,
	LOWPRICE DOUBLE NOT NULL,
	ADJCLOSE DOUBLE NOT NULL,
	VOLUME INTEGER NOT NULL,
  	PRIMARY KEY (TICKERSYMBOL, TRADEDATE));

create table SUPPLIERS
  (SUP_ID integer NOT NULL,
  SUP_NAME varchar(40) NOT NULL,
  STREET varchar(40) NOT NULL,
  CITY varchar(20) NOT NULL,
  STATE char(2) NOT NULL,
  ZIP char(5),
  PRIMARY KEY (SUP_ID));
  
create table COFFEES
  (COF_NAME varchar(32) NOT NULL,
  SUP_ID int NOT NULL,
  PRICE numeric(10,2) NOT NULL,
  SALES integer NOT NULL,
  TOTAL integer NOT NULL,
  PRIMARY KEY (COF_NAME),
  FOREIGN KEY (SUP_ID) REFERENCES SUPPLIERS (SUP_ID));
  
create table COFFEE_DESCRIPTIONS
  (COF_NAME varchar(32) NOT NULL,
  COF_DESC clob NOT NULL,
  PRIMARY KEY (COF_NAME),
  FOREIGN KEY (COF_NAME) REFERENCES COFFEES (COF_NAME));

create table RSS_FEEDS
  (RSS_NAME varchar(32) NOT NULL,
  RSS_FEED_XML xml NOT NULL,
  PRIMARY KEY (RSS_NAME));
  
create table COF_INVENTORY
  (WAREHOUSE_ID integer NOT NULL,
  COF_NAME varchar(32) NOT NULL,
  SUP_ID int NOT NULL,
  QUAN int NOT NULL,
  DATE_VAL timestamp NOT NULL,
  FOREIGN KEY (COF_NAME) REFERENCES COFFEES (COF_NAME),
  FOREIGN KEY (SUP_ID) REFERENCES SUPPLIERS (SUP_ID));
  
create table MERCH_INVENTORY
  (ITEM_ID integer NOT NULL,
  ITEM_NAME varchar(20) NOT NULL,
  SUP_ID int NOT NULL,
  QUAN int NOT NULL,
  DATE_VAL timestamp NOT NULL,
  PRIMARY KEY (ITEM_ID),
  FOREIGN KEY (SUP_ID) REFERENCES SUPPLIERS (SUP_ID));
  
create table COFFEE_HOUSES
  (STORE_ID integer NOT NULL,
  CITY varchar(32),
  COFFEE int NOT NULL,
  MERCH int NOT NULL,
  TOTAL int NOT NULL,
  PRIMARY KEY (STORE_ID));
  
create table DATA_REPOSITORY
  (DOCUMENT_NAME varchar(50),
  URL varchar(200));  

  
  