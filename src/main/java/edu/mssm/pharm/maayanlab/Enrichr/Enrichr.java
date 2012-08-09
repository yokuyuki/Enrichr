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
		
		try {
			Enrichment app = new Enrichment(inputList);
			request.getSession().setAttribute("process", app);
			response.sendRedirect("results.jsp");
		} catch (ParseException e) {
			if (e.getErrorOffset() == -1)
				response.getWriter().println("Invalid input: Input list is empty.");
			else
				response.getWriter().println("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		HttpSession session = request.getSession();
		
		if (request.getParameter("unload") != null) {
			session.invalidate();
			return;
		}
		
		String backgroundType = request.getParameter("backgroundType");
		Enrichment app = (Enrichment) session.getAttribute("process");		
		json.add(backgroundType, flattenResults(app.enrich(backgroundType)));		
		json.write(response.getWriter());
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
