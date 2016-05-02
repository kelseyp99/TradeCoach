package com.tradecoach.patenter.entity.security;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity 
//@IdClass(ParameterId.class)
@Table(name = "PARAMETERS")
public class Parameter extends EntityBean  implements IEntityBean, Serializable {
//    @Id private String portfolioName;
//    @Id private String parameterName;
	@Id 
	@SequenceGenerator(name="identifier", sequenceName="parameters_id_seq",allocationSize=1) 
	@GeneratedValue(strategy=GenerationType.SEQUENCE,	generator="identifier")
	@Column(name = "id") private int id ;
	@Column(name = "PARAM_NAME") private String parameterName;
	private int intVal;
	private String strVal; 
	boolean boolVal;   
	@ManyToOne(cascade = CascadeType.ALL)@JoinColumn(name = "ID")private Portfolio portfolio;
	
	public Parameter() {}	
	/*public Parameter(String project, String name) {
		this.portfolioName = portfolioName;
		this.parameterName = parameterName;
	}*/
	public Parameter(int intVal,String strVal, boolean boolVal){
		this.setIntVal(intVal);
		this.setStrVal(strVal);
		this.setBoolVal(boolVal);
	}
	public Parameter(String portfolioName, String parameterName, int intVal, String strVal, boolean boolVal) {
		super();
		//this.portfolioName = portfolioName;
		this.parameterName = parameterName;
		this.strVal = strVal;
		this.intVal = intVal;
		this.boolVal = boolVal;
	}
//	@Id
//	@GeneratedValue
//	@Column(name = "PORTFOLIO_NAME")
//	public String getPortfolioName() {return this.portfolioName;	}

	public String getParameterName() {return this.parameterName;	}
	public int getIntVal() {return intVal;}
	public void setIntVal(int intVal) {this.intVal = intVal;}
	public String getStrVal() {return strVal;}
	public void setStrVal(String strVal) {this.strVal = strVal;	}
	public boolean isBoolVal() {return boolVal;	}
	public void setBoolVal(boolean boolVal) {this.boolVal = boolVal;}
//	public void setPortfolioName(String portfolioName) {this.portfolioName = portfolioName;}
	public void setParameterName(String parameterName) {this.parameterName = parameterName;}

}

//class ParameterId {
//	String parameterName;
//	String portfolioName;
//}


