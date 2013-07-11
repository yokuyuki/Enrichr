/**
 * Initializes, saves, and responds to requests to retrieve or update the counter for number of lists enriched and shared.
 * 
 * @author		Edward Y. Chen
 * @since		09/12/2012 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.mssm.pharm.maayanlab.common.web.HibernateUtil;

@WebServlet(urlPatterns = {"/count"}, loadOnStartup=1)
public class Counters extends HttpServlet {

	private static final long serialVersionUID = -682732829814620653L;

	public static final String ENRICHMENT = "enrichment";
	public static final String SHARE = "share";
	
	@Override
	public void init() throws ServletException {
		// Read counter from SQL, only need to keep enrichment count in memory
		getServletContext().setAttribute("enrichment_count", getCounter(ENRICHMENT));
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		response.getWriter().print(getServletContext().getAttribute("enrichment_count"));
	}
	
	public static int getCounter(String counterName) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = null;
		try {
			session = sf.getCurrentSession();
		} catch (HibernateException he) {
			session = sf.openSession();
		}
		session.beginTransaction();
		
		Query query = session.createSQLQuery("SELECT count FROM enrichr.counters where name = :counter").setParameter("counter", counterName);
		List<?> data  = query.list();
		
		Integer counterValue = (Integer) data.get(0);
		
		session.getTransaction().commit();
		session.close();
		
		return counterValue.intValue();
	}
	
	public static int incrementCounter(String counterName) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = null;
		try {
			session = sf.getCurrentSession();
		} catch (HibernateException he) {
			session = sf.openSession();
		}
		session.beginTransaction();
		
		Query query = session.createSQLQuery("CALL enrichr.IncrementCounter(:counter)").setParameter("counter", counterName);
		List<?> data  = query.list();
		
		BigInteger counterValue = (BigInteger) data.get(0);
		
		session.getTransaction().commit();
		session.close();
		
		return counterValue.intValue();
	}	
}
