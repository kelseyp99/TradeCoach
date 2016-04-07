/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workers;

//import java.util.LinkedList;
import java.util.ListIterator;
public class LinkedList {
	
	public LinkedList l;
	public ListIterator li;
    
    public LinkedList() {
        l=new LinkedList();
    }
 /*          
    public static void main(String[] args) {
    	LinkedListExmpl lle = new LinkedListExmpl();
    	lle.add(3); lle.add(5);  lle.add(7);
    	System.out.println(lle);    	
    }
    
    public String toString () {
		String s = "";		
		li=l.listIterator(0);
		
		while (li.hasNext()) {
			s = s + li.next();
			s = s + li.get();
		} //while	
		return s;   	
    }
  */  
    public void add(int i) { 
    	l.add(i);
    }
    
}

