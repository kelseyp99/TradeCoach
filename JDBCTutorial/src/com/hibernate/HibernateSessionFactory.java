package com.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.jboss.logging.Logger;

public class HibernateSessionFactory {

    private static final ThreadLocal threadLocal = new ThreadLocal();
    private static org.hibernate.SessionFactory sessionFactory;

    private static Configuration configuration = new Configuration();
    private static ServiceRegistry serviceRegistry; 

    private static final Logger log = Logger.getLogger(HibernateSessionFactory.class);

 /*   static {
        try {
            configuration.configure();
            serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties())
            . buildServiceRegistry();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            log.error("Error Creating SessionFactory",e);

        }
    }*/

    private HibernateSessionFactory() {
    }

/*    public static Session getSession() throws HibernateException {
        Session session = (Session) threadLocal.get(); 
        if (session == null || !session.isOpen()) {
            if (sessionFactory == null) {
                rebuildSessionFactory();
            }
            session = (sessionFactory != null) ? sessionFactory.openSession()
            : null;

            threadLocal.set(session);
        }
        return session;
    }*/

 /*   public static void rebuildSessionFactory() {

        try {
            configuration.configure();
            serviceRegistry = new ServiceRegistryBuilder(). applySettings(configuration.getProperties()) 
            .buildServiceRegistry();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            log.error("Error Creating SessionFactory",e);
        }
    }*/
    
    
    

    public static void closeSession() throws HibernateException {
        Session session = (Session) threadLocal.get();
        threadLocal.set(null);
        if (session != null) {
            session.flush();
            session.close();

        }
    }

    public static org.hibernate.SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

}