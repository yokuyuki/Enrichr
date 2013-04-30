/**
 * Initializes Hibernate and JSONify when servlet context is created and then destroys them when it is destroyed.
 * 
 * @author		Edward Y. Chen
 * @since		03/01/2013
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import edu.mssm.pharm.maayanlab.HibernateUtil;
import edu.mssm.pharm.maayanlab.JSONify;

@WebListener
public class ContextFinalizer implements ServletContextListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextFinalizer.class);
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		HibernateUtil.getSessionFactory(); // Just call the static initializer of that class
		// Register type adapter with JSONify to serialize List object properly
		JSONify.setConverter(new GsonBuilder().registerTypeAdapter(List.class, new ListAdapter()).setDateFormat("yyyy/MM/dd HH:mm:ss").create());
	}       

	@SuppressWarnings("deprecation")
	@Override
	public void contextDestroyed(ServletContextEvent event) {         
		HibernateUtil.getSessionFactory().close(); // Free all resources
		JSONify.destroyConverter();
		
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();        
		for (Thread t : threadSet) {
			if (t.getName().contains("Abandoned connection cleanup thread")) {
                synchronized(t) {
                	LOGGER.warn("Forcibly stopping thread to avoid memory leak: " + t.getName());
                    t.stop();	//don't complain, it works
                }
            }
		}
	}
}