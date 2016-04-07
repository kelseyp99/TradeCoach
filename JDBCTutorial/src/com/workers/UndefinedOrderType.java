package com.workers;

public class UndefinedOrderType extends Exception {

	public UndefinedOrderType() {
		super();
		
	}

	public UndefinedOrderType(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public UndefinedOrderType(String message, Throwable cause) {
		super(message, cause);
		
	}

	public UndefinedOrderType(String message) {
		super(message);
		
	}

	public UndefinedOrderType(Throwable cause) {
		super(cause);
		
	}

}
