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

import edu.mssm.pharm.maayanlab.FileUtils;
import edu.mssm.pharm.maayanlab.JSONify;

@WebServlet(urlPatterns = {"/genemap"}, loadOnStartup=1)
public class GeneMap extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5506636247908679426L;

	private static final HashMap<String, String> formats = new HashMap<String, String>();
	static {
		formats.put(Enrichment.BIOCARTA, "{0} is a member of the {1}.");
		formats.put(Enrichment.CHEA, "{1} binds to the promoter region of {0}.");
		formats.put(Enrichment.CCLE, "{0} is up-regulated in {1} cells.");
		formats.put(Enrichment.CHROMOSOME_LOCATION, "{0} is found in chromosome segment {1}.");
		formats.put(Enrichment.CORUM, "{0} is part of the {1}.");
		formats.put(Enrichment.UPREGULATED_CMAP, "In cells treated by the drug {1}, {0} is up-regulated.");
		formats.put(Enrichment.DOWNREGULATED_CMAP, "In cells treated by the drug {1}, {0} is down-regulated.");
		formats.put(Enrichment.ENCODE, "{1} binds to the promoter region of {0}.");
		formats.put(Enrichment.GENESIGDB, "{0} is found in the table of {1}.");
		formats.put(Enrichment.GENOME_BROWSER_PWMS, "{1} binds to the promoter region of {0}.");
		formats.put(Enrichment.GO_BP, "{0} is involved in the biological process {1}.");
		formats.put(Enrichment.GO_CC, "{0} is found in {1}.");
		formats.put(Enrichment.GO_MF, "{0} has the molecular function of {1}.");
		formats.put(Enrichment.HISTONE_MODIFICATIONS, "{0} is associated with the histone modification, {1}.");
		formats.put(Enrichment.HMDB_METABOLITES, "{1} is a co-factor of {0}.");
		formats.put(Enrichment.HUMAN_ENDOGENOUS_COMPLEXOME, "{0} was found in a complex with the {1} complexome.");
		formats.put(Enrichment.HUMAN_GENE_ATLAS, "{0} is up-regulated in {1} cells.");
		formats.put(Enrichment.KEA, "{1} phosphorylates {0}.");
		formats.put(Enrichment.KEGG, "{0} is a member of the {1} pathway.");
		formats.put(Enrichment.MGI_MP, "Knockdown of {0} results in {1} phenotype.");
		formats.put(Enrichment.MICRORNA, "{0} is the predicted target of {1}.");
		formats.put(Enrichment.MOUSE_GENE_ATLAS, "{0} is up-regulated in {1} cells.");
		formats.put(Enrichment.NCI60, "{0} is up-regulated in {1} cells.");
		formats.put(Enrichment.OMIM_DISEASE, "{0} is associated with the genetic disease {1}.");
		formats.put(Enrichment.OMIM_EXPANDED, "{0} is associated with a subnetwork around the genetic disease {1}.");
		formats.put(Enrichment.PFAM_INTERPRO, "{0} has a {1} protein domain.");
		formats.put(Enrichment.PPI_HUB_PROTEINS, "{0} directly interacts with the hub protein {1}.");
		formats.put(Enrichment.REACTOME, "{0} is a member of the {1} pathway.");
		formats.put(Enrichment.SILAC, "{0} is phosphorylated in the {1} condition.");
		formats.put(Enrichment.TRANSFAC_AND_JASPAR_PWMS, "{1} has a binding site at the promoter of {0}.");
		formats.put(Enrichment.VIRUSMINT, "{0} interacts with a viral protein from {1}.");
		formats.put(Enrichment.WIKIPATHWAYS, "{0} is a member of the {1} pathway.");
	}
	
	public final HashMap<String, HashMap<String, ArrayList<String>>> geneMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	
	@Override
	public void init() {
		constructMap();
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getParameter("json") == null)
			searchQuery(request.getParameter("gene"), response);
		else
			query(request.getParameter("gene"), (request.getParameter("setup") == null) ? false : true, response);
	}
	
	private void constructMap() {
		for (String backgroundType : formats.keySet()) {
			// Read background list and ranks
			Collection<String> backgroundLines = FileUtils.readResource(backgroundType + ".gmt");
			
			for (String line : backgroundLines) {
				String[] splitLine = line.split("\t");
				String termName = splitLine[0];
				
				for (int i = 2; i < splitLine.length; i++) {
					String gene = splitLine[i].toUpperCase();
					if (!geneMap.containsKey(gene))
						geneMap.put(gene, new HashMap<String, ArrayList<String>>());
					if (!geneMap.get(gene).containsKey(backgroundType))
						geneMap.get(gene).put(backgroundType, new ArrayList<String>());
					geneMap.get(gene).get(backgroundType).add(termName);
				}
			}
		}
				
		for (Iterator<String> geneItr = geneMap.keySet().iterator(); geneItr.hasNext(); ) {
			String gene = geneItr.next();
			if (geneMap.get(gene).size() < 3)
				geneItr.remove();
		}
	}
	
	private void query(String gene, boolean outputSetupVariables, HttpServletResponse response) throws IOException {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		json.add("gene", geneMap.get(gene));
		
		if (outputSetupVariables) {
			json.add("categories", Enrichment.categories);
			json.add("backgroundTypes", Enrichment.categorizedEnrichmentTypes);
			json.add("formats", formats);
		}
		
		json.write(response.getWriter());
	}
	
	private void searchQuery(String gene, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		HashMap<String, ArrayList<String>> backgrounds = geneMap.get(gene);
		
		if (backgrounds == null) {
			out.println("No terms found for gene " + gene + ".");
		}
		else {
			for (String backgroundType : geneMap.get(gene).keySet()) {
				String format = formats.get(backgroundType);
				
				for (String termName : backgrounds.get(backgroundType)) {
					String ans = MessageFormat.format(format, "<span class=\"gene\">" + gene + "</span>", "<span class=\"term\">" + termName + "</span>");
					out.print(ans);
					out.println("<br>");
				}
			}
		}
	}
}
