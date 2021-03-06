/**
 * Handles user accounts.
 * 
 * @author		Edward Y. Chen
 * @since		02/08/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

import edu.mssm.pharm.maayanlab.common.core.FileUtils;
import edu.mssm.pharm.maayanlab.common.web.HibernateUtil;
import edu.mssm.pharm.maayanlab.common.web.JSONify;
import edu.mssm.pharm.maayanlab.common.math.HashFunctions;

@WebServlet(urlPatterns = {"/account", "/login", "/register", "/forgot", "/reset", "/status", "/logout", "/contribute", "/delete"})
public class Account extends HttpServlet {
	
	private static final long serialVersionUID = 19776535963654466L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		JSONify json = EnrichrContext.getJSONConverter();	// Get special JSON converter to handle serialization of List model
		
		HttpSession httpSession = request.getSession();
		
		User user = (User) httpSession.getAttribute("user");
		
		if (request.getServletPath().equals("/logout")) {
			httpSession.removeAttribute("user");
			response.sendRedirect("");	// Redirect to home page
			return;
		}
		
		if (user == null) {
			json.add("user", "");	// Front-end will deal with empty user
		}
		else {
			json.add("user", user.getEmail());	// Common info shared between any GET
			json.add("firstname", user.getFirst());
			
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
				
				json.add("lastname", user.getLast());	// Additional info on account page
				json.add("institution", user.getInstitute());
				json.add("lists", user.getLists());
				
				session.getTransaction().commit();
				session.close();
			}
		}
		
		json.write(response.getWriter());
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// All form submissions are ajax and take json responses
		response.setContentType("application/json");
		JSONify json = new JSONify();
		
		// Create database session
		SessionFactory sf = HibernateUtil.getSessionFactory();
		Session dbSession = sf.openSession();
		dbSession.beginTransaction();
		
		// All these tasks require db
		if (request.getServletPath().equals("/login"))
			login(request, response, dbSession, json);
		else if (request.getServletPath().equals("/register"))
			register(request, response, dbSession, json);
		else if (request.getServletPath().equals("/forgot"))
			forgot(request, response, dbSession, json);
		else if (request.getServletPath().equals("/reset"))			
			reset(request, response, dbSession, json);
		else if (request.getServletPath().equals("/account"))
			modify(request, response, dbSession, json);
		else if (request.getServletPath().equals("/contribute"))
			contribute(request, response, dbSession, json);
		else if (request.getServletPath().equals("/delete"))
			delete(request, response, dbSession, json);
		
		// Close database session and write out json
		dbSession.getTransaction().commit();
		dbSession.close();		
		json.write(response.getWriter());
	}
	
	// Handle account registration
	private void register(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
		// Check for existing email
		String email = request.getParameter("email");
		Criteria criteria = dbSession.createCriteria(User.class)
				.add(Restrictions.eq("email", email).ignoreCase());
		User user = (User) criteria.uniqueResult();
		
		if (user != null) {	// If exists, display error
			json.add("message", "The email you entered is already registered.");
		}
		else {	// Else, create user
			User newUser = new User(email, 
									request.getParameter("password"),
									request.getParameter("firstname"), 
									request.getParameter("lastname"), 
									request.getParameter("institution"));
			dbSession.save(newUser);
			request.getSession().setAttribute("user", newUser);
			json.add("redirect", "index.html");
		}
	}
	
	// Handle account login
	private void login(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		
		Criteria criteria = dbSession.createCriteria(User.class)
				.add(Restrictions.eq("email", email).ignoreCase());
		User user = (User) criteria.uniqueResult();
		
		if (user == null) {	// If user doesn't exist, display error
			json.add("message", "The email you entered does not belong to a registered user.");
		}
		else if (user.checkPassword(password)) {
			user.updateAccessed();	// Sets accessed field to NULL so db can auto-update it based on access timestamp
			dbSession.update(user);
			request.getSession().setAttribute("user", user);
			json.add("redirect", "account.html");	// Login successful, redirect
		}
		else {	// If password is incorrect, display error
			json.add("message", "The password you entered is incorrect.");
		}
	}
	
	// Handle password reset request
	private void forgot(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
		String email = request.getParameter("email");
		Criteria criteria = dbSession.createCriteria(User.class)
				.add(Restrictions.eq("email", email).ignoreCase());
		User user = (User) criteria.uniqueResult();
		
		if (user == null) {	// If user doesn't exist, display error
			json.add("message", "The email you entered does not belong to a registered user.");
		}
		else {
			// One day token for password reset
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String token = user.getEmail() + user.getSalt() + formatter.format(Calendar.getInstance().getTime());
			token = HashFunctions.md5(token);
			
			Properties props = new Properties();
			props.put("mail.smtp.host", "mail.maayanlab.net");
			props.put("mail.smtp.auth", "true");
			javax.mail.Session mailSession = javax.mail.Session.getInstance(props, new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					// TODO: pull this out to a settings file so not hardcoded
					return new PasswordAuthentication("amp@maayanlab.net", "1amp1");
				}
			});
			
			// Web app has no concept of domain so link is hardcoded
			MimeMessage message = new MimeMessage(mailSession);
			try {
				message.setFrom(new InternetAddress("Enrichr@amp.pharm.mssm.edu"));
				message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
				message.setSubject("Enrichr Password Reset");
				message.setSentDate(new Date());
				message.setText("Reset your password at http://amp.pharm.mssm.edu/Enrichr/reset.html?user=" + email + "&token=" + token + ".\n\nIf you did not request this password reset, please ignore this email.");
				Transport.send(message);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			json.add("message", "Password reset request sent! Check your email for a reset link.");
		}
	}
	
	// Handle password reset
	private void reset(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
		String email = request.getParameter("email");
		Criteria criteria = dbSession.createCriteria(User.class)
				.add(Restrictions.eq("email", email).ignoreCase());
		User user = (User) criteria.uniqueResult();
		
		if (user == null) {	// If user doesn't exist, display error
			json.add("message", "The email you entered does not belong to a registered user.");			
		}
		else {
			// Generate today and yesterday's tokens just in case they generate close to midnight
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			String today = user.getEmail() + user.getSalt() + formatter.format(calendar.getTime());
			calendar.add(Calendar.DATE, -1);
			String yesterday = user.getEmail() + user.getSalt() + formatter.format(calendar.getTime());
			
			String token = request.getParameter("token");		
			if (!token.equalsIgnoreCase(HashFunctions.md5(today)) && !token.equalsIgnoreCase(HashFunctions.md5(yesterday))) {
				json.add("message", "Your reset token has expired. Please request a new one.");
			}
			else {
				user.updatePassword(request.getParameter("password"));
				dbSession.update(user);
				json.add("redirect", "login.html");	// Direct them to login
			}
		}
	}
	
	// Handle modification of account info
	private void modify(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {		
		User user = (User) request.getSession().getAttribute("user");
		String password = request.getParameter("password");
		
		if (user == null) {	// If idled to logout, redirect them to login
			json.add("redirect", "login.html");
		}
		else if (user.checkPassword(password)) {	// Check password in order to change
			boolean changed = user.updateUser(request.getParameter("email"), 
							request.getParameter("newpassword"), 
							request.getParameter("firstname"), 
							request.getParameter("lastname"),
							request.getParameter("institution"));
			if (changed) {	// Don't update unless necessary, save db write
				dbSession.update(user);
				json.add("message", "Changes saved.");
			}
			else {
				json.add("message", "No changes found.");
			}
		}
		else {
			json.add("message", "The password you entered is incorrect.");
		}
	}
	
	// Handle contributing list to crowdsourced lists
	private void contribute(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
		HttpSession httpSession = request.getSession();		
		User user = (User) httpSession.getAttribute("user");
		
		if (user == null) {	// If idled to logout, redirect them to login
			json.add("redirect", "login.html");
			return;
		}
		dbSession.update(user);
		
		// Make sure the user does own the list and list isn't already shared
		String sharedListEncodedId = request.getParameter("listId");
		int sharedListId = Shortener.decode(sharedListEncodedId);
		List list = (List) dbSession.get(List.class, sharedListId);
		SharedList sharedList = (SharedList) dbSession.get(SharedList.class, sharedListId);
		if (sharedList != null) {
			json.add("message", "You've already contributed that list.");
		}
		else if (list != null && list.getUser().equals(user)) {
			sharedList = new SharedList(sharedListId, 
					user, 
					request.getParameter("description"), 
					Boolean.parseBoolean(request.getParameter("privacy")));
			
			// Look for list on path
			String resourceUrl = Enrichr.RESOURCE_PATH + sharedListEncodedId + ".txt";
			if (!(new File(resourceUrl)).isFile()) {
				throw new IOException("List doesn't exist");	// Somehow file is missing?
			}
			
			// Read file
			ArrayList<String> input = FileUtils.readResource(resourceUrl);
			if (input.get(0).startsWith("#"))	// If input line starts with comment
				input.remove(0).replaceFirst("#", "");
			
			// Add each gene in list
			Set<SharedGene> sharedGenes = sharedList.getSharedGenes();
			for (String gene : input) {
				sharedGenes.add(new SharedGene(sharedList, gene));
			}
			
			dbSession.save(sharedList);		
			json.add("listId", sharedListEncodedId);
		}
		else {
			json.add("message", "The list doesn't belong to you.");			
		}
	}
	
	// Handle deleting of lists
	private void delete(HttpServletRequest request, HttpServletResponse response, Session dbSession, JSONify json) throws IOException {
		HttpSession httpSession = request.getSession();		
		User user = (User) httpSession.getAttribute("user");
		
		if (user == null) {	// If idled to logout, redirect them to login
			json.add("redirect", "login.html");
			return;
		}
		dbSession.update(user);
		
		String listEncodedId = request.getParameter("listId");
		List list = (List) dbSession.get(List.class, Shortener.decode(listEncodedId));
		if (list != null && list.getUser().equals(user)) {	// Make sure list exists and owner owns the list
			user.getLists().remove(list);
			dbSession.update(user);
			
			String resourceUrl = Enrichr.RESOURCE_PATH + listEncodedId + ".txt";
			File file = new File(resourceUrl);
			if (!file.delete()) {
				throw new IOException("Delete failed");
			}
		}
		
		json.add("redirect", "account.html");	// Refresh account page
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
