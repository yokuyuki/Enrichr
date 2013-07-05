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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.mssm.pharm.maayanlab.common.web.HibernateUtil;
import edu.mssm.pharm.maayanlab.common.web.JSONify;

@WebListener
public class EnrichrContext implements ServletContextListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EnrichrContext.class);
	private static Gson gson;
	
	public static JSONify getJSONConverter() {
		return new JSONify(gson);
	}
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		// Just call the static initializer of that class
		HibernateUtil.getSessionFactory();
		ResourceLoader.getInstance();
		
		// Register type adapter with JSONify to serialize List object properly
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(List.class, new ListAdapter());
		gsonBuilder.setDateFormat("yyyy/MM/dd HH:mm:ss");
		EnrichrContext.gson = gsonBuilder.create();
	}       

	@SuppressWarnings("deprecation")
	@Override
	public void contextDestroyed(ServletContextEvent event) {         
		HibernateUtil.getSessionFactory().close(); // Free all resources
		
		//TODO: find memory leak that requires server to be restarted after hot deploying several (3?) times
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