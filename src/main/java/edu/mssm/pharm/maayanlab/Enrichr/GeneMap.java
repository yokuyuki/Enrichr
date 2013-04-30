package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.mssm.pharm.maayanlab.FileUtils;
import edu.mssm.pharm.maayanlab.JSONify;
import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentCategory;
import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.GeneSetLibrary;

@WebServlet(urlPatterns = {"/genemap"}, loadOnStartup=1)
public class GeneMap extends HttpServlet {

	private static final long serialVersionUID = 5506636247908679426L;

	private final HashMap<String, HashMap<String, ArrayList<String>>> geneMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	private final EnrichmentCategory[] categories = ResourceLoader.getInstance().getCategories();
	private final String categoriesJSON = new Gson().toJson(categories);

	@Override
	public void init() {
		constructMap();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("json") == null)	// Search engine query via _escaped_fragment_ doesn't use json parameter
			searchQuery(request.getParameter("gene"), response);
		else
			query(request.getParameter("gene"), (request.getParameter("setup") == null) ? false : true, response);
	}

	// Maps genes to a dictionary that maps libraries to terms
	private void constructMap() {
		for (EnrichmentCategory category : categories) {
			for (GeneSetLibrary library : category.getLibraries()) {
				String libraryName = library.getName();
				
				// Read gene set library
				Collection<String> libraryLines = FileUtils.readResource(libraryName + ".gmt");

				for (String line : libraryLines) {
					String[] splitLine = line.split("\t");
					String termName = splitLine[0];

					// Parse gmt file
					for (int i = 2; i < splitLine.length; i++) {
						String gene = splitLine[i].toUpperCase();
						if (!geneMap.containsKey(gene))	// If gene doesn't exist in outer map, create dictionary
							geneMap.put(gene, new HashMap<String, ArrayList<String>>());
						if (!geneMap.get(gene).containsKey(libraryName))	// If library doesn't exist in inner map, create term list
							geneMap.get(gene).put(libraryName, new ArrayList<String>());
						geneMap.get(gene).get(libraryName).add(termName);
					}
				}
			}
		}
		
		// Remove gene from map if gene is associated with less than 3 libraries aka the gene name is uncommon 
		for (Iterator<String> geneItr = geneMap.keySet().iterator(); geneItr.hasNext();) {
			String gene = geneItr.next();
			if (geneMap.get(gene).size() < 3)
				geneItr.remove();
		}
	}

	// Normal AJAX query
	private void query(String gene, boolean outputSetupVariables, HttpServletResponse response) throws IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		json.add("gene", geneMap.get(gene));

		if (outputSetupVariables) {	// Output categories data structure because searching for first time
			json.addRaw("categories", categoriesJSON);
		}

		json.write(response.getWriter());
	}

	// Search engine query
	private void searchQuery(String gene, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		HashMap<String, ArrayList<String>> backgrounds = geneMap.get(gene);

		if (backgrounds == null) {
			out.println("No terms found for gene " + gene + ".");
		} 
		else {
			for (EnrichmentCategory category : categories) {
				for (GeneSetLibrary library : category.getLibraries()) {
					String libraryName = library.getName();

					if (backgrounds.containsKey(libraryName)) {
						String format = library.getFormat();

						for (String termName : backgrounds.get(libraryName)) {
							String ans = MessageFormat.format(format, "<span class=\"gene\">" + gene + "</span>", "<span class=\"term\">" + termName + "</span>");
							out.print(ans);
							out.println("<br>");
						}
					}
				}
			}
		}
	}
}
