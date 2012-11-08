package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.IOException;
import java.io.PrintWriter;
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

	private static final HashMap<String, String> backgroundTypes = new HashMap<String, String>() {{
		put(Enrichment.BIOCARTA, "%1$s is a member of the %2$s.");
		put(Enrichment.CHEA, "%2$s binds to the promoter region of %1$s.");
		put(Enrichment.CCLE, "%1$s is up-regulated in %2$s cells.");
		put(Enrichment.CHROMOSOME_LOCATION, "%1$s is found in chromosome segment %2$s.");
		put(Enrichment.CORUM, "%1$s is part of the %2$s.");
		put(Enrichment.UPREGULATED_CMAP, "In cells treated by the drug %2$s, %1$s is up-regulated.");
		put(Enrichment.DOWNREGULATED_CMAP, "In cells treated by the drug %2$s, %1$s is down-regulated.");
		put(Enrichment.ENCODE, "%2$s binds to the promoter region of %1$s.");
		put(Enrichment.GENESIGDB, "%1$s is found in the table of %2$s.");
		put(Enrichment.GENOME_BROWSER_PWMS, "%2$s binds to the promoter region of %1$s.");
		put(Enrichment.GO_BP, "%1$s is involved in the biological process %2$s.");
		put(Enrichment.GO_CC, "%1$s is found in %2$s.");
		put(Enrichment.GO_MF, "%1$s has the molecular function of %2$s.");
		put(Enrichment.HM, "%1$s is associated with the histone modification, %2$s.");
		put(Enrichment.HMDB_METABOLITES, "%2$s is a co-factor of %1$s.");
		put(Enrichment.COMPLEXOME, "%1$s was found in a complex with the %2$s complexome.");
		put(Enrichment.HUMAN_GENE_ATLAS, "%1$s is up-regulated in %2$s cells.");
		put(Enrichment.KEA, "%2$s phosphorylates %1$s.");
		put(Enrichment.KEGG, "%1$s is a member of the %2$s pathway.");
		put(Enrichment.MGI_MP, "Knockdown of %1$s results in %2$s phenotype.");
		put(Enrichment.MICRORNA, "%1$s is the predicted target of %2$s.");
		put(Enrichment.MOUSE_GENE_ATLAS, "%1$s is up-regulated in %2$s cells.");
		put(Enrichment.NCI60, "%1$s is up-regulated in %2$s cells.");
		put(Enrichment.OMIM_DISEASE, "%1$s is associated with the genetic disease %2$s.");
		put(Enrichment.OMIM_EXPANDED, "%1$s is associated with a subnetwork around the genetic disease %2$s.");
		put(Enrichment.PFAM_INTERPRO, "%1$s has a %2$s protein domain.");
		put(Enrichment.PPI_HUB_PROTEINS, "%1$s directly interacts with the hub protein %2$s.");
		put(Enrichment.REACTOME, "%1$s is a member of the %2$s pathway.");
		put(Enrichment.TRANSFAC_JASPAR, "%2$s has a binding site at the promoter of %1$s.");
		put(Enrichment.VIRUSMINT, "%1$s interacts with a viral protein from %2$s.");
		put(Enrichment.WIKIPATHWAYS, "%1$s is a member of the %2$s pathway.");
	}};
	
	public final HashMap<String, HashMap<String, ArrayList<String>>> geneMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	
	@Override
	public void init() {
		constructMap();
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		searchQuery(request.getParameter("gene"), response);
	}
	
	private void constructMap() {
		for (String backgroundType : backgroundTypes.keySet()) {
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
	
	private void query(String gene, HttpServletResponse response) {
		JSONify json = new JSONify();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		json.add("gene", gene);
	}
	
	private void searchQuery(String gene, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		HashMap<String, ArrayList<String>> backgrounds = geneMap.get(gene);
		
		for (String backgroundType : geneMap.get(gene).keySet()) {
			String format = backgroundTypes.get(backgroundType);
			
			for (String termName : backgrounds.get(backgroundType)) {
				String ans = String.format(format, "<span class=\"gene\">" + gene + "</span>", "<span class=\"term\">" + termName + "</span>");
				out.print(ans);
				out.println("<br>");
			}
		}
	}
}
