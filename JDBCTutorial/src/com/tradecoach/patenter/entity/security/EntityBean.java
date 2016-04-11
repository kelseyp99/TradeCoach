package com.tradecoach.patenter.entity.security;

	public class EntityBean<T> {
	private Class<T> classType;
	private SecurityInst partOf;
	public EntityBean() {
		super();
	}
	
	public void initialize(){
	//	this.setClassType();
		
	}
	
	public String getClassTypeStr(){
		String c = this.getClassType().toString();
		c = c.substring(c.lastIndexOf(".")+1,c.length());
		return c;
	}
	
	public void setClassType(){
		this.setClassType(this.getClass());
	}
	
	public Class<?> getClassType() {
		return classType;
	}
	public void setClassType(Class<?> classType) {
		this.classType = (Class<T>) classType;
	}

	public SecurityInst getPartOf() {
		return partOf;
	}

	public void setPartOf(SecurityInst si) {
		this.partOf = si;
	}
	
	
	
	
	

}
