/**
 * Enrichr is a web app that serves the enrichment pages.
 * 
 * @author		Edward Y. Chen
 * @since		8/2/2012 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import edu.mssm.pharm.maayanlab.common.bio.EnrichedTerm;
import edu.mssm.pharm.maayanlab.common.core.FileUtils;
import edu.mssm.pharm.maayanlab.common.web.JSONify;
import edu.mssm.pharm.maayanlab.common.web.PartReader;

@WebServlet(urlPatterns= {"/enrich", "/share", "/export"})
@MultipartConfig
public class Enrichr extends HttpServlet {

	private static final long serialVersionUID = 3310803710142519430L;
	
	protected static final String RESOURCE_PATH = "/datasets/";	// Where to look for stored lists

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		// Read file
		Part fileChunk = request.getPart("file");
		if (fileChunk == null || fileChunk.getSize() == 0)
			fileChunk = request.getPart("list");
		ArrayList<String> inputList = PartReader.readLines(fileChunk);
		
		// Read description
		String description = request.getParameter("description");
		if (description != null && description.trim().length() != 0)
			request.getSession().setAttribute("description", description);
		else
			request.getSession().removeAttribute("description");
		
		// Increment count
		Counter count = (Counter) getServletContext().getAttribute("enrichment_count");
		count.incrementAndGet();
		Counters.updateCounter(count);
		
		postResult(request, response, inputList);
	}
	
	// Handle submitting a list either from input or saved list
	private void postResult(HttpServletRequest request, HttpServletResponse response, ArrayList<String> inputList) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			
			// Write gene count
			session.setAttribute("length", Integer.toString(inputList.size()));
			
			boolean validate = ("true".equals(request.getParameter("validate"))) ? true : false;	// Only submission page validates
			
			Enrichment app = new Enrichment(inputList, validate);
			session.setAttribute("process", app);	// Save the enrichment object as session variable
			request.getRequestDispatcher("results.jsp").forward(request, response);	// Maintain /enrich URL instead of showing results.jsp
		}  catch (ParseException e) {	// Send to custom error page if list can't be parsed
			if (e.getErrorOffset() == -1)
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid input: Input list is empty.");
			else
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		// Redirect to post if reading from file
		String dataset = request.getParameter("dataset");
		if (dataset != null) {
			String resourceUrl = RESOURCE_PATH + dataset + ".txt";
			if ((new File(resourceUrl)).isFile()) {
				ArrayList<String> input = FileUtils.readResource(resourceUrl);
				if (input.get(0).startsWith("#"))	// If input line starts with comment
					session.setAttribute("description", input.remove(0).replaceFirst("#", ""));
				else
					session.removeAttribute("description");
				postResult(request, response, input);
			}
			else {	// Send error if list doesn't exist
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "This dataset doesn't exist.");
			}
			
			return;
		}
		
		Enrichment app = (Enrichment) session.getAttribute("process");
		if (app == null) {	// If session is expired
			getExpired(request, response);
			return;
		}
		
		// TODO: doesn't share too much code, can probably turn RESTful
		if (request.getServletPath().equals("/enrich")) {	// Support legacy paths
			// TODO: remove legacy
			if (request.getParameter("share") == null) {	// If not sharing result
				if (request.getParameter("filename") == null)	// If not exporting file
					getJSONResult(request, response, app);
				else
					getFileResult(request, response, app);
			}
			else {				
				getShared(request, response, app);
			}
			// End of legacy
		}
		else {
			if (request.getServletPath().equals("/share")) {
				getShared(request, response, app);
				return;
			}
			
			if (request.getServletPath().equals("/export")) {
				getFileResult(request, response, app);
				return;
			}			
		}
	}
	
	// Handles expired sessions
	private void getExpired(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		json.add("expired", true);	// Front-end will deal with this
		json.write(response.getWriter());
	}
	
	// Handle share requests
	private void getShared(HttpServletRequest request, HttpServletResponse response, Enrichment app) throws IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession();
		
		// Use share counter to generate unique link id
		Counter share = (Counter) getServletContext().getAttribute("share_count");
		int listNumber = share.getAndIncrement();
		Counters.updateCounter(share);
		String fileId = Shortener.encode(listNumber);
		
		String description = (String) session.getAttribute("description");
		
		// Write shared file
		if (description != null)
			FileUtils.writeFile("/datasets/" + fileId + ".txt", "#" + description, app.getInput());
		else
			FileUtils.writeFile("/datasets/" + fileId + ".txt", app.getInput());
		
		// Add list to the user
		User user = (User) session.getAttribute("user");
		if (user != null) {
			user.getLists().add(new List(listNumber, user, description));
			Account.updateUser(user);
		}
		
		json.add("link_id", fileId);	// Return the link for front-end to display
		json.write(response.getWriter());
	}
	
	// Handle displaying result in JSON and JavaScript
	private void getJSONResult(HttpServletRequest request, HttpServletResponse response, Enrichment app) throws IOException {
		String backgroundType = request.getParameter("backgroundType");
		ArrayList<EnrichedTerm> results = app.enrich(backgroundType);
		
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		json.add(backgroundType, flattenResults(results));
		json.write(response.getWriter());
	}
	
	// Handle displaying result in downloadable file
	private void getFileResult(HttpServletRequest request, HttpServletResponse response, Enrichment app) throws IOException {
		String filename = request.getParameter("filename");
		String backgroundType = request.getParameter("backgroundType");
		ArrayList<EnrichedTerm> results = app.enrich(backgroundType);
		
		// Headers needed to trigger a download instead of opening in browser
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename  + ".txt\"");		
		response.setHeader("Content-Transfer-Encoding", "binary");
		
		FileUtils.write(response.getWriter(), Enrichment.HEADER, results);
	}
	
	// Flatten enrichment results so it can be converted to JSON easily
	private Object[][] flattenResults(ArrayList<EnrichedTerm> results) {
		Object[][] resultsMatrix = new Object[results.size()][6];
		
		int i = 0;
		for (EnrichedTerm enrichedTerm : results) {
			resultsMatrix[i][0] = i+1;
			resultsMatrix[i][1] = enrichedTerm.getName();
			resultsMatrix[i][2] = enrichedTerm.getAdjustedPValue();
			resultsMatrix[i][3] = enrichedTerm.getZScore();
			resultsMatrix[i][4] = enrichedTerm.getCombinedScore();
			resultsMatrix[i][5] = enrichedTerm.getOverlap();
			i++;
		}
		
		return resultsMatrix;
	}
}
