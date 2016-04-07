package com.oracle.tutorial.jdbc;
import java.util.List; 
import java.util.Date;
import java.util.Iterator; 
 
import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.workers.Portfolio;
import com.workers.SecurityInst;
//import com.workers.Portfolio;
//import com.workers.SecurityInst2;

public class ManageSecurities {
   private static SessionFactory factory; 
   public static void main(String[] args) {
      try{
         factory = new Configuration().configure().buildSessionFactory();
      }catch (Throwable ex) { 
         System.err.println("Failed to create sessionFactory object." + ex);
         throw new ExceptionInInitializerError(ex); 
      }
      ManageSecurities ME = new ManageSecurities();
      
      

      /* Add few employee records in database */
      String empID1 = ME.addEmployee("Zara2");
      String empID2 = ME.addEmployee("Daisy2");
      String empID3 = ME.addEmployee("John2");

      /* List down all the employees */
      ME.listEmployees();

      /* Update employee's records */
 //   ME.updateEmployee(empID1, 5000);

      /* Delete an employee from the database */
  //  ME.deleteEmployee(empID2);

      /* List down new list of the employees */
   // ME.listEmployees();
   }
   /* Method to CREATE an employee in the database */
   public String addEmployee(String fname){
      Session session = factory.openSession();
      Transaction tx = null;
      String employeeID = "";
      try{ 
         tx = session.beginTransaction();
         SecurityInst employee = new SecurityInst();
         employee.setTickerSymbol(fname);
         session.delete(employee);
         session.save(employee); 
         tx.commit();
      }catch (HibernateException e) {
         if (tx!=null) tx.rollback();
         e.printStackTrace(); 
      }finally {
         session.close(); 
      }
      return employeeID;
   }
   /* Method to  READ all the employees */
   public void listEmployees( ){
      Session session = factory.openSession();
      Transaction tx = null;
      try{
         tx = session.beginTransaction();
         List employees = session.createQuery("FROM SecurityInst").list(); 
         for (Iterator iterator = 
                           employees.iterator(); iterator.hasNext();){
        	 SecurityInst employee = (SecurityInst) iterator.next(); 
            System.out.print("First Name: " + employee.getTickerSymbol()); 
         }
         tx.commit();
      }catch (HibernateException e) {
         if (tx!=null) tx.rollback();
         e.printStackTrace(); 
      }finally {
         session.close(); 
      }
   }
   /* Method to UPDATE salary for an employee */
   public void updateEmployee(Integer EmployeeID, int salary ){
      Session session = factory.openSession();
      Transaction tx = null;
      try{
         tx = session.beginTransaction();
         Portfolio employee = 
                    (Portfolio)session.get(Portfolio.class, EmployeeID); 
  //       employee.setSalary( salary );
//		 session.update(employee); 
 //        tx.commit();
      }catch (HibernateException e) {
         if (tx!=null) tx.rollback();
         e.printStackTrace(); 
      }finally {
         session.close(); 
      }
   }
   /* Method to DELETE an employee from the records */
/*   public void deleteEmployee(Integer EmployeeID){
      Session session = factory.openSession();
      Transaction tx = null;
      try{
         tx = session.beginTransaction();
         Employee2 employee = 
                   (Employee2)session.get(Employee2.class, EmployeeID); 
         session.delete(employee); 
         tx.commit();
      }catch (HibernateException e) {
         if (tx!=null) tx.rollback();
         e.printStackTrace(); 
      }finally {
         session.close(); 
      }
   }*/
}
