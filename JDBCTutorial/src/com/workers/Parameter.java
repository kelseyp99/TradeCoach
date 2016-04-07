package com.workers;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity @IdClass(ParameterId.class)
@Table(name = "PARAMETERS")
public class Parameter implements Serializable {
    @Id private String portfolioName;
    @Id private String parameterName;
	private int intVal;
	private String strVal; 
	boolean boolVal;
	public Parameter() {}
	
	public Parameter(String project, String name) {
		this.portfolioName = portfolioName;
		this.parameterName = parameterName;
	}
	public Parameter(int intVal,String strVal, boolean boolVal){
		this.setIntVal(intVal);
		this.setStrVal(strVal);
		this.setBoolVal(boolVal);
	}
	public Parameter(String portfolioName, String parameterName, int intVal, String strVal, boolean boolVal) {
		super();
		this.portfolioName = portfolioName;
		this.parameterName = parameterName;
		this.strVal = strVal;
		this.intVal = intVal;
		this.boolVal = boolVal;
	}
	@Id
	@GeneratedValue
	@Column(name = "PORTFOLIO_NAME")
	public String getPortfolioName() {return this.portfolioName;	}
	@Column(name = "PARAM_NAME")
	public String getParameterName() {return this.parameterName;	}
	public int getIntVal() {return intVal;}
	public void setIntVal(int intVal) {this.intVal = intVal;}
	public String getStrVal() {return strVal;}
	public void setStrVal(String strVal) {this.strVal = strVal;	}
	public boolean isBoolVal() {return boolVal;	}
	public void setBoolVal(boolean boolVal) {this.boolVal = boolVal;}
	public void setPortfolioName(String portfolioName) {this.portfolioName = portfolioName;}
	public void setParameterName(String parameterName) {this.parameterName = parameterName;}

}

class ParameterId {
	String parameterName;
	String portfolioName;
}

