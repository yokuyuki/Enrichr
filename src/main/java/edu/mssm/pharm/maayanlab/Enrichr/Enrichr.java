/**
 * Enrichr is a web app that serves the enrichment pages.
 * 
 * @author		Edward Y. Chen
 * @since		8/2/2012 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import edu.mssm.pharm.maayanlab.FileUtils;
import edu.mssm.pharm.maayanlab.JSONify;
import edu.mssm.pharm.maayanlab.PartReader;

@WebServlet(urlPatterns= {"/enrich"})
@MultipartConfig
public class Enrichr extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3310803710142519430L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		// Read file
		Part fileChunk = request.getPart("file");
		if (fileChunk == null || fileChunk.getSize() == 0)
			fileChunk = request.getPart("list");
		ArrayList<String> inputList = PartReader.readLines(fileChunk);
		
		postResult(request, response, inputList);
	}
	
	private void postResult(HttpServletRequest request, HttpServletResponse response, ArrayList<String> inputList) throws IOException {
		try {
			HttpSession session = request.getSession();
			
			Enrichment app = new Enrichment(inputList, true);
			session.setAttribute("process", app);			
			response.sendRedirect("results.jsp");
			
			if (session.getAttribute("filecount") == null) {
				session.setAttribute("filecount", new Integer(0));
			}
			else {
				int filecount = (Integer) session.getAttribute("filecount");
				session.setAttribute("filecount", ++filecount);
			}
		}  catch (ParseException e) {
			if (e.getErrorOffset() == -1)
				response.getWriter().println("Invalid input: Input list is empty.");
			else
				response.getWriter().println("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Redirect to post if reading from file
		String dataset = request.getParameter("dataset");
		if (dataset != null) {
			postResult(request, response, FileUtils.readResource("/datasets/" + dataset + ".txt"));
			return;
		}
		
		Enrichment app = (Enrichment) request.getSession().getAttribute("process");
		
		if (app == null) {
			getExpired(request, response);
		}
		else {
			if (request.getParameter("share") == null) {
				if (request.getParameter("filename") == null)
					getJSONResult(request, response, app);
				else
					getFileResult(request, response, app);
			}
			else {				
				getShared(request, response, app);
			}
		}
	}
	
	private void getExpired(HttpServletRequest request, HttpServletResponse response) throws IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		json.add("expired", true);
		json.write(response.getWriter());
	}
	
	private void getShared(HttpServletRequest request, HttpServletResponse response, Enrichment app) throws IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		int filecount = (Integer) request.getSession().getAttribute("filecount");
		String fileId = request.getSession().getId() + "-" + filecount;
		FileUtils.writeFile("/datasets/" + fileId + ".txt", app.getInput());
		
		json.add("link_id", fileId);
		json.write(response.getWriter());
	}
	
	private void getJSONResult(HttpServletRequest request, HttpServletResponse response, Enrichment app) throws IOException {
		String backgroundType = request.getParameter("backgroundType");
		LinkedList<Term> results = app.enrich(backgroundType);
		
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		json.add(backgroundType, flattenResults(results));		
		json.write(response.getWriter());
	}
	
	private void getFileResult(HttpServletRequest request, HttpServletResponse response, Enrichment app) throws IOException {
		String filename = request.getParameter("filename");
		String backgroundType = request.getParameter("backgroundType");
		LinkedList<Term> results = app.enrich(backgroundType);
		
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename  + ".txt\"");		
		response.setHeader("Content-Transfer-Encoding", "binary");
		
		FileUtils.write(response.getWriter(), Enrichment.HEADER, results);
	}
	
	private Object[][] flattenResults(LinkedList<Term> results) {
		Object[][] resultsMatrix = new Object[results.size()][3];
		
		int i = 0;
		for (Term term : results) {
			resultsMatrix[i][0] = i+1;
			resultsMatrix[i][1] = term.getName();
			resultsMatrix[i][2] = term.getPValue();
			i++;
		}
		
		return resultsMatrix;
	}
}
