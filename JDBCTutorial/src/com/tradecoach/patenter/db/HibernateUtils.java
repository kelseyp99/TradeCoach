package com.tradecoach.patenter.db;

import com.github.rholder.retry.RetryException;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tradecoach.patenter.entity.security.EntityBean;
import com.tradecoach.patenter.entity.security.Parameter;
import com.tradecoach.patenter.entity.security.PortfolioHoldings;
import com.typesafe.config.Config;
import com.workers.SecurityInst;
import com.workers.TransactionData;
import  com.tradecoach.patenter.entity.security.*;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Alexander Loginov on 1/28/15.
 */
public class HibernateUtils {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtils.class);
	private SessionFactory sessionFactory;
	private static ServiceRegistry serviceRegistry;     
    private static Properties prop;
    
    protected int FLUSH_BARRIER;
    protected int BATCH_SIZE;
    final Runnable flushProcess = new Runnable() {
        @Override
        public void run() {
            logger.info("Scheduled flushing: Queue size {}", patentsQueue.size());
            if (patentsQueue.size() == 0) {
                return;
            }

            List<SecurityInst> patentsToFlush = Lists.newArrayList();
            while (patentsQueue.size() > 0 && patentsToFlush.size() < BATCH_SIZE) {
                patentsToFlush.add(patentsQueue.poll());
            }

            logger.info("Queue report: Flushing {} patents into DB", patentsToFlush.size());
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                flushToDB(patentsToFlush);
            } catch (Exception e) {
                logger.error("Failed to flush", e);
                logger.info("Rescheduling {} patents back to the queue", patentsToFlush.size());
                patentsToFlush.stream().forEach(patentsQueue::add);
            }
            long l = stopwatch.stop().elapsed(TimeUnit.SECONDS);
            logger.info("Queue report: Flushed successfully in {} s. {} obj/sec", l, ((float) patentsToFlush.size()) / l);
        }
    };
    protected int FORCED_FLUSHING_PERIOD; //in seconds
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    protected Queue<SecurityInst> patentsQueue;
    
    public HibernateUtils() {
		super();
		this.patentsQueue = null;
		FLUSH_BARRIER = 0;
        FORCED_FLUSHING_PERIOD = 0;
        BATCH_SIZE = 0;
	}

	public HibernateUtils(Queue<SecurityInst> inputQueue, Config config) throws SQLException {
		this.patentsQueue = inputQueue;
		FLUSH_BARRIER = config.getInt("flush_barrier");
		FORCED_FLUSHING_PERIOD = config.getInt("forced_flushing_period");
		BATCH_SIZE = config.getInt("batch_size");
		try{
			logger.info("Setting-up DB relationships");
			Stopwatch s = Stopwatch.createStarted();
			this.buildSessionFactory(config);      
		}catch (Throwable ex) { 
			System.err.println("Failed to create sessionFactory object.\n" + ex);
			throw new ExceptionInInitializerError(ex); 
		}
	}

    public void start() {
        scheduledExecutor.scheduleWithFixedDelay(flushProcess, FORCED_FLUSHING_PERIOD, FORCED_FLUSHING_PERIOD, TimeUnit.SECONDS);
        logger.info("DB flush scheduled every {} seconds starting in {} seconds", FORCED_FLUSHING_PERIOD, FORCED_FLUSHING_PERIOD);
    }

    public void shutdown() {
        logger.info("Stopping DB Module");
        scheduledExecutor.shutdown();
        try {
            scheduledExecutor.awaitTermination(120, TimeUnit.SECONDS);
            logger.info("DB Module stopped gracefully");
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            logger.info("Forced DB Module termination");
        }

    }

    public void add(SecurityInst patent) {
        patentsQueue.add(patent);
        if (this.patentsQueue.size() >= FLUSH_BARRIER) {
            scheduledExecutor.submit(flushProcess);
        }
    }

    protected void flushToDB(final List<SecurityInst> patentsBatch) throws Exception {
		
		//TODO: fix this. Seems bug appears only after rescheduling
		List<SecurityInst> securityInsts = patentsBatch.stream().filter(SecurityInst -> SecurityInst != null).collect(Collectors.toList());

		if (securityInsts.size() == 0) {
			return;
		}

		Stopwatch cacheStopWatch = Stopwatch.createStarted();
		logger.debug("Populated from cache in {} ms", cacheStopWatch.stop().elapsed(TimeUnit.MILLISECONDS));
		this.updateOrSave(securityInsts);
	}  	

	public void updateOrSave(SecurityInst si) { 
		Session session = this.getSessionFactory().getCurrentSession();
//		si.getInventors().forEach(inventor->inventor.setPartOf(patent)); 
//		si.getPrimaryExaminer().setPartOf(si);    	  
//		si.getAssistantExaminer().setPartOf(si); 	  
//		si.getAssignee().setPartOf(si);
//		si.setPartOf(si);
//		si.getReferences().forEach(reference->reference.setPartOf(patent));    	
//		si.getInventors().forEach(inventor->setID(inventor));     	
//		setID(si);
//		setID(si.getBelongsTo());
//		setID(si.getAssignee());
//		setID(si);
//		si.getReferences().forEach(reference->setID(reference));
		session.saveOrUpdate(si); 
//		si.getReferences().forEach(reference->session.saveOrUpdate(reference));	
	}

	private  void updateOrSave(List<SecurityInst> sis) {
		Session session = this.getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			sis.forEach(patent->session.saveOrUpdate(patent));
			session.flush();
			session.clear();
			tx.commit();
		}catch (HibernateException e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace(); 
		}finally {
			session.close(); 
		}
	}
	
	public void buildSessionFactory(Config config) {
		try {
			prop = new Properties();	 
            String url = config.getString("url");
            prop.put("hibernate.dialect",  "org.hibernate.dialect.PostgreSQLDialect");
            prop.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            prop.put("hibernate.connection.url", "jdbc:postgresql://" + url);
            prop.put("hibernate.connection.username", config.getString("username"));
            prop.put("hibernate.connection.password", config.getString("password"));	 
            prop.put("hibernate.show_sql", "true");
            prop.put("hibernate.hbm2ddl.auto", "update");
            prop.put("hibernate.connection.isolation", "2");
            prop.put("hibernate.connection.isolation", "true");
            prop.put("hibernate.current_session_context_class", "thread");
			Configuration configuration = new Configuration();
		//	configuration.setProperties(prop);
//			configuration.addPackage("com.tradecoach.patenter.entity.security");//the fully qualified package name
//			configuration.addAnnotatedClass(Securities.class);
////			configuration.addAnnotatedClass(Portfolio.class);
//			configuration.addAnnotatedClass(Parameter.class);
//			configuration.addAnnotatedClass(PortfolioHoldings.class);
//			configuration.addAnnotatedClass(TransactionData.class);
			configuration.configure();
	/*		
			sessionFactory = new AnnotationConfiguration()
                    .addPackage("test.animals") //the fully qualified package name
                    .addAnnotatedClass(Flight.class)
                    .addAnnotatedClass(Sky.class)
                    .addAnnotatedClass(Person.class)
                    .addAnnotatedClass(Dog.class)
                    .addResource("test/animals/orm.xml")
                    .configure()
                    .buildSessionFactory();*/
			
			serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
					configuration.getProperties()).build();  

			this.setSessionFactory(configuration.buildSessionFactory(serviceRegistry));
		} catch (Throwable ex) {
			logger.error("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public void setID( EntityBean entityBean) {
		if(entityBean==null) return;
		int id = 0;
		PreparedStatement pst;
		Session session = this.getSessionFactory().openSession();
		Connection con = ((SessionImplementor) session).connection();		
		try {
		//	if(entityBean.getClassTypeStr().compareTo("Examiner")==0) {
			if(entityBean.getClassType()==SecurityInst.class) {
				SecurityInst bean = (SecurityInst)entityBean;
				String sql = 	"select id from SECURITIES " +
						"where ticker_symbol = ? " +
						"order by id desc limit 1";
				pst = con.prepareStatement(sql);
				pst.setString(1, bean.getTickerSymbol());
				ResultSet rs = pst.executeQuery();
				if (rs != null && rs.next()) {
					bean.setId(rs.getInt("id"));
				} 
			} else if(entityBean.getClassType()==Portfolio.class) {
				Portfolio bean = (Portfolio)entityBean;
				String sql = 	"select id from Portfolios " +
						"where first_name = ? " +
						"and last_name = ? " +
						"order by id desc limit 1";
				pst = con.prepareStatement(sql);
				//pst.setString(1, bean.getPortfolioName());
				ResultSet rs = pst.executeQuery();
				if (rs != null && rs.next()) {
				//	bean.setId(rs.getInt("id"));
				} 
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void delete(SecurityInst patent ){
		Session session = getSessionFactory().getCurrentSession();
		Transaction tx = null;
		try{
			tx = session.beginTransaction();
			session.delete(patent); 
			tx.commit();
		}catch (HibernateException e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace(); 
		}finally {
			session.close(); 
		}
	}
	
	public  SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	} 
}
