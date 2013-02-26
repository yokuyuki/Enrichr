package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.google.gson.GsonBuilder;

import edu.mssm.pharm.maayanlab.HibernateUtil;
import edu.mssm.pharm.maayanlab.JSONify;

@WebServlet(urlPatterns = {"/account", "/login", "/register", "/status", "/logout"})
public class Account extends HttpServlet {
	
	private static final long serialVersionUID = 19776535963654466L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		JSONify json = new JSONify(new GsonBuilder().registerTypeAdapter(List.class, new ListAdapter()).create());
		
		HttpSession httpSession = request.getSession();
		
		User user = (User) httpSession.getAttribute("user");
		
		if (request.getServletPath().equals("/logout")) {
			httpSession.removeAttribute("user");
			response.sendRedirect("");
			return;
		}
		
		if (user == null) {
			json.add("user", "");
		}
		else {
			json.add("user", (user.getFirst() != null) ? user.getFirst() : user.getEmail());	// Display name
			
			// Get user lists
			if (request.getServletPath().equals("/account")) {
				SessionFactory sf = HibernateUtil.getSessionFactory();
				Session session = null;
				try {
					session = sf.getCurrentSession();
				} catch (HibernateException he) {
					session = sf.openSession();
				}
				session.beginTransaction();
				
				session.update(user);
				json.add("lists", user.getLists());
				
				session.getTransaction().commit();
				session.close();
			}
		}
		
		json.write(response.getWriter());
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Create database session
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = sf.openSession();
		session.beginTransaction();
		
		boolean success;
		if (request.getServletPath().equals("/register"))
			success = register(session, request, response);
		else
			success = login(session, request, response);
		
		session.getTransaction().commit();
		session.close();
		
		if (success)
			response.sendRedirect("account.html");
		else
			request.getRequestDispatcher("account-error.jsp").forward(request, response);
	}
	
	private boolean register(Session session, HttpServletRequest request, HttpServletResponse response) {
		String email = request.getParameter("email");
		
		// Check for existing email
		Criteria criteria = session.createCriteria(User.class)
				.add(Restrictions.eq("email", email).ignoreCase());
		User user = (User) criteria.uniqueResult();
		
		if (user != null) {	// If exists, throw error
			request.setAttribute("error", "The email you entered is already registered.");
			return false;
		}
		else {	// Else, create user
			User newUser = new User(email, 
									request.getParameter("password"),
									request.getParameter("firstname"), 
									request.getParameter("lastname"), 
									request.getParameter("institution"));
			session.save(newUser);				
			request.getSession().setAttribute("user", newUser);
			return true;
		}
	}
	
	private boolean login(Session session, HttpServletRequest request, HttpServletResponse response) {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		Criteria criteria = session.createCriteria(User.class)
				.add(Restrictions.eq("email", email).ignoreCase());
		User user = (User) criteria.uniqueResult();
		
		if (user == null) {
			request.setAttribute("error", "The email you entered does not belong to a registered user.");
			return false;
		}
		else {
			if (user.checkPassword(password)) {
				user.updateAccessed();
				session.update(user);
				request.getSession().setAttribute("user", user);
				return true;
			}
			else {
				request.setAttribute("error", "The password you entered is incorrect.");
				return false;
			}
		}
	}
	
	// Static function to commit new lists to the user so the Enrichr class doesn't make any db calls
	static void updateUser(User user) {
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session session = null;
		try {
			session = sf.getCurrentSession();
		} catch (HibernateException he) {
			session = sf.openSession();
		}
		session.beginTransaction();
		session.update(user);
		session.getTransaction().commit();
		session.close();
	}
}
