package com.oracle.tutorial.jdbc;
import java.util.List; 
import java.util.Date;
import java.util.Iterator; 
 
import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.tradecoach.patenter.entity.security.Parameter;
import com.workers.Portfolio;

public class ManageParameter {
   private static SessionFactory factory; 
   public static void main(String[] args) {
      try{
         factory = new Configuration().configure().buildSessionFactory();
      }catch (Throwable ex) { 
         System.err.println("Failed to create sessionFactory object." + ex);
         throw new ExceptionInInitializerError(ex); 
      }
      ManageParameter ME = new ManageParameter();

      /* Add few employee records in database */
      String empID1 = ME.addEmployee("Zara2", "test");
      String empID2 = ME.addEmployee("Daisy2", "test");
      String empID3 = ME.addEmployee("John2", "test");

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
   public String addEmployee(String fname, String strVal){
      Session session = factory.openSession();
      Transaction tx = null;
      String employeeID = "";
      try{ 
         tx = session.beginTransaction();
         Parameter employee = new Parameter();
         employee.setPortfolioName("Zara3");
         employee.setParameterName(fname);
         employee.setStrVal(strVal);    
      //   employeeID = (String) session.save(employee); 
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
         List employees = session.createQuery("FROM Parameter").list(); 
         for (Iterator iterator = 
                           employees.iterator(); iterator.hasNext();){
        	 Parameter employee = (Parameter) iterator.next(); 
            System.out.print("First Name: " + employee.getPortfolioName()); 
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
