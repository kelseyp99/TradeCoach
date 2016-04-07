package com.oracle.tutorial.jdbc;

public class Employee {
	private int id;
	private String companyName; 
	private String betaValue;   
	private int salary;  
	public Employee() {}
	public Employee(String fname, String lname, int salary) {
		this.companyName = fname;
		this.betaValue = lname;
		this.salary = salary;
	}
	public int getId() {
		return id;
	}
	public void setId( int id ) {
		this.id = id;
	}
	public String getFirstName() {
		return companyName;
	}
	public void setFirstName( String first_name ) {
		this.companyName = first_name;
	}
	public String getLastName() {
		return betaValue;
	}
	public void setLastName( String last_name ) {
		this.betaValue = last_name;
	}
	public int getSalary() {
		return salary;
	}
	public void setSalary( int salary ) {
		this.salary = salary;
	}
}

