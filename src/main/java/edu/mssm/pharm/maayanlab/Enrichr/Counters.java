/**
 * Initializes, saves, and responds to requests to retrieve or update the counter for number of lists enriched and shared.
 * 
 * @author		Edward Y. Chen
 * @since		09/12/2012 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import edu.mssm.pharm.maayanlab.FileUtils;
import edu.mssm.pharm.maayanlab.HibernateUtil;

@WebServlet(urlPatterns = {"/count"}, loadOnStartup=1)
public class Counters extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -682732829814620653L;

	private static Counter count;
	private static Counter share;

	@Override
	public void init() throws ServletException {
		// Read counters from SQL
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = null;
		try {
			session = sf.getCurrentSession();
		} catch (HibernateException he) {
			session = sf.openSession();
		}
		session.beginTransaction();
		
		Criteria criteria = session.createCriteria(Counter.class)
				.add(Restrictions.eq("name", "enrichment"));
		count = (Counter) criteria.uniqueResult();
		
		criteria = session.createCriteria(Counter.class)
				.add(Restrictions.eq("name", "share"));
		share = (Counter) criteria.uniqueResult();
		
		session.getTransaction().commit();
		session.close();
				
		// Try to load the initial count from our saved persistent state
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader("/datasets/count"));
			int value = Integer.parseInt(bufferedReader.readLine());
			if (value > count.getCount())
				count.setCount(value);
			bufferedReader.close();
			
			bufferedReader = new BufferedReader(new FileReader("/datasets/share"));
			value = Integer.parseInt(bufferedReader.readLine());
			if (value > share.getCount())
				share.setCount(value);
			
			Counters.updateCounter(count);
			Counters.updateCounter(share);
		}
		catch (FileNotFoundException ignored) { }  // no saved state
		catch (IOException ignored) { }            // problem during read
		catch (NumberFormatException ignored) { }  // corrupt saved state
		finally {
			// Make sure to close the file
			try {
				if (bufferedReader != null)
					bufferedReader.close();
			}
			catch (IOException ignored) { }
		}
		
		getServletContext().setAttribute("enrichment_count", count);
		getServletContext().setAttribute("share_count", share);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain");
		response.getWriter().print(count.getCount());
	}
	
	@Override
	public void destroy() {
		super.destroy();  // entirely optional

		// Try to save the accumulated count
		FileUtils.writeString("/datasets/count", Integer.toString(count.getCount()));
		FileUtils.writeString("/datasets/share", Integer.toString(share.getCount()));
	}
	
	static void updateCounter(Counter counter) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = null;
		try {
			session = sf.getCurrentSession();
		} catch (HibernateException he) {
			session = sf.openSession();
		}
		session.beginTransaction();
		session.update(counter);
		session.getTransaction().commit();
		session.close();
	}
}
