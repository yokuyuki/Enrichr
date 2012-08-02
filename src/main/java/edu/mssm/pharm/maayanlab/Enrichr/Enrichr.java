/**
 * Enrichr is a web app that serves the enrichment pages.
 * 
 * @author		Edward Y. Chen
 * @since		8/2/2012 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
		if (fileChunk.getSize() == 0)
			fileChunk = request.getPart("list");
		ArrayList<String> inputList = PartReader.readLines(fileChunk);
		
		Enrichment app = new Enrichment();
		request.getSession().setAttribute("process", app);
		request.getSession().setAttribute("input", inputList);
		
		response.sendRedirect("results.html");
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		
	}
}
