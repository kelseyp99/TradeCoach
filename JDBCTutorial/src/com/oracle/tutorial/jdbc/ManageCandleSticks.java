package com.oracle.tutorial.jdbc;

import java.util.List; 
import java.util.Date;
import java.util.Iterator; 
 
import org.hibernate.HibernateException; 
import org.hibernate.Session; 
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.tradecoach.patenter.entity.security.CandleStick;
import com.workers.TransactionData;

public class ManageCandleSticks {
   private static SessionFactory factory; 
   public static void main(String[] args) {
      try{
         factory = new Configuration().configure().buildSessionFactory();
      }catch (Throwable ex) { 
         System.err.println("Failed to create sessionFactory object." + ex);
         throw new ExceptionInInitializerError(ex); 
      }
      ManageCandleSticks ME = new ManageCandleSticks();

      /* Add few employee records in database */
      Integer empID1 = ME.addEmployee("Zara", new Date(), 1000);
   //   Integer empID2 = ME.addEmployee("Daisy", "Das", 5000);
    //  Integer empID3 = ME.addEmployee("John", "Paul", 10000);

      /* List down all the employees */
      ME.listEmployees();

      /* Update employee's records */
   //   ME.updateEmployee(empID1, 5000);

      /* Delete an employee from the database */
    //  ME.deleteEmployee(empID2);

      /* List down new list of the employees */
    //  ME.listEmployees();
   }
   /* Method to CREATE an employee in the database */
   public Integer addEmployee(String fname, Date date, int salary){
      Session session = factory.openSession();
      Transaction tx = null;
      Integer employeeID = 0;
      try{
         tx = session.beginTransaction();
         CandleStick employee = new CandleStick();
         employee.setTickerSymbol(fname);
         employee.setDate(date);
         employee.setVolume(salary);
       //  employeeID = (Integer) 
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
         List employees = session.createQuery("FROM CandleStick").list(); 
         for (Iterator iterator = 
                           employees.iterator(); iterator.hasNext();){
        	 CandleStick employee = (CandleStick) iterator.next(); 
            System.out.print("First Name: " + employee.getTickerSymbol()); 
      //      System.out.print("  Last Name: " + employee.getInstrumentName()); 
         //   System.out.println("  Salary: " + employee.geti); 
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
         Employee employee = 
                    (Employee)session.get(Employee.class, EmployeeID); 
         employee.setSalary( salary );
		 session.update(employee); 
         tx.commit();
      }catch (HibernateException e) {
         if (tx!=null) tx.rollback();
         e.printStackTrace(); 
      }finally {
         session.close(); 
      }
   }
   /* Method to DELETE an employee from the records */
   public void deleteEmployee(Integer EmployeeID){
      Session session = factory.openSession();
      Transaction tx = null;
      try{
         tx = session.beginTransaction();
         Employee employee = 
                   (Employee)session.get(Employee.class, EmployeeID); 
         session.delete(employee); 
         tx.commit();
      }catch (HibernateException e) {
         if (tx!=null) tx.rollback();
         e.printStackTrace(); 
      }finally {
         session.close(); 
      }
   }
}
