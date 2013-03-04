package edu.mssm.pharm.maayanlab.Enrichr;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import edu.mssm.pharm.maayanlab.HibernateUtil;

@WebListener
public class HibernateListener implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent event) {         
		HibernateUtil.getSessionFactory(); // Just call the static initializer of that class        
	}       

	@Override
	public void contextDestroyed(ServletContextEvent event) {         
		HibernateUtil.getSessionFactory().close(); // Free all resources
	}
}