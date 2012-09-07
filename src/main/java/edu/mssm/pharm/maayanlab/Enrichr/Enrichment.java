/**
 * Performs enrichment on a gene list.
 * 
 * @author		Edward Y. Chen
 * @since		8/2/2012 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import pal.statistics.FisherExact;

import edu.mssm.pharm.maayanlab.FileUtils;

public class Enrichment {
	
	// Paths to gmt files
	@SuppressWarnings("serial")
	private static HashMap<String, String> gmtLocations = new HashMap<String, String>() {{
		put(Enrichment.BIOCARTA, "BioCarta_pathways.gmt");
		put(Enrichment.CHEA, "ChEA.gmt");
		put(Enrichment.CCLE, "Cancer_Cell_Line_Encyclopedia.gmt");
		put(Enrichment.CHROMOSOME_LOCATION, "Chromosome_location.gmt");
		put(Enrichment.CORUM, "CORUM.gmt");
		put(Enrichment.UPREGULATED_CMAP, "Up-regulated_CMAP_100.gmt");
		put(Enrichment.DOWNREGULATED_CMAP, "Down-regulated_CMAP_100.gmt");
		put(Enrichment.ENCODE, "ENCODE_TF_ChIP-seq.gmt");
		put(Enrichment.GENESIGDB, "GeneSigDB.gmt");
		put(Enrichment.GENOME_BROWSER_PWMS, "Genome_Browser_PWMs.gmt");
		put(Enrichment.GO_BP, "GeneOntology_BP.gmt");
		put(Enrichment.GO_CC, "GeneOntology_CC.gmt");
		put(Enrichment.GO_MF, "GeneOntology_MF.gmt");
		put(Enrichment.HM, "Histone_Modifications_ChIP-seq.gmt");
		put(Enrichment.HMDB_METABOLITES, "HMDB_Metabolites.gmt");
		put(Enrichment.COMPLEXOME, "Human_Endogenous_Complexome.gmt");
		put(Enrichment.HUMAN_GENE_ATLAS, "Human_Gene_Atlas.gmt");
		put(Enrichment.KEA, "KEA.gmt");
		put(Enrichment.KEGG, "KEGG_pathways.gmt");
		put(Enrichment.MGI_MP, "MGI_MP_top4.gmt");
		put(Enrichment.MICRORNA, "microRNA.gmt");
		put(Enrichment.MOUSE_GENE_ATLAS, "Mouse_Gene_Atlas.gmt");
		put(Enrichment.NCI60, "NCI60.gmt");
		put(Enrichment.OMIM_DISEASE, "OMIM_disease_genes.gmt");
		put(Enrichment.OMIM_EXPANDED, "OMIM_Expanded.gmt");
		put(Enrichment.PFAM_INTERPRO, "Pfam-InterPro-domains.gmt");
		put(Enrichment.PPI_HUB_PROTEINS, "PPI_Hub_Proteins.gmt");
		put(Enrichment.REACTOME, "Reactome_pathways.gmt");
		put(Enrichment.TRANSFAC_JASPAR, "TRANSFAC_JASPAR.gmt");
		put(Enrichment.VIRUSMINT, "VirusMINT.gmt");
		put(Enrichment.WIKIPATHWAYS, "WikiPathways_pathways.gmt");
	}};
	public static final Set<String> backgroundTypes = gmtLocations.keySet();
	
	// Constants
	public static final String BIOCARTA = "BioCarta";
	public static final String CCLE = "Cancer_Cell_Line_Encyclopedia";
	public static final String CHEA = "ChEA";
	public static final String CHROMOSOME_LOCATION = "Chromosome_Location";
	public static final String CORUM = "CORUM";
	public static final String UPREGULATED_CMAP = "Up-regulated_CMAP";
	public static final String DOWNREGULATED_CMAP = "Down-regulated_CMAP";
	public static final String ENCODE = "ENCODE_TF_ChIP-seq";
	public static final String GENESIGDB = "GeneSigDB";
	public static final String GENOME_BROWSER_PWMS = "Genome_Browser_PWMs";
	public static final String GO_BP = "GO_Biological_Process";
	public static final String GO_CC = "GO_Cellular_Component";
	public static final String GO_MF = "GO_Molecular_Function";
	public static final String HM = "Histone_Modifications_ChIP-seq";
	public static final String HMDB_METABOLITES = "HMDB_Metabolites";
	public static final String HUMAN_GENE_ATLAS = "Human_Gene_Atlas";
	public static final String COMPLEXOME = "Human_Endogenous_Complexome";
	public static final String KEA = "KEA";
	public static final String KEGG = "KEGG";
	public static final String MGI_MP = "MGI_Mammalian_Phenotype";
	public static final String MICRORNA = "microRNA";
	public static final String MOUSE_GENE_ATLAS = "Mouse_Gene_Atlas";
	public static final String NCI60 = "NCI-60_Cancer_Cell_Lines";
	public static final String OMIM_DISEASE = "OMIM_Disease";
	public static final String OMIM_EXPANDED = "OMIM_Expanded";
	public static final String PFAM_INTERPRO = "Pfam_InterPro_Domains";
	public static final String PPI_HUB_PROTEINS = "PPI_Hub_Proteins";
	public static final String REACTOME = "Reactome";
	public static final String TRANSFAC_JASPAR = "TRANSFAC_and_JASPAR_PWMs";
	public static final String VIRUSMINT = "VirusMINT";
	public static final String WIKIPATHWAYS = "WikiPathways";
	
	public static final String HEADER = "Term\tP-value\tGenes";
	
	private Collection<String> geneList; 
	
	public static void main(String[] args) {		
		try {
			Enrichment app = new Enrichment(FileUtils.readFile(args[1]), true);
			FileUtils.writeFile(args[2], HEADER, app.enrich(args[0]));
		} catch (ParseException e) {
			if (e.getErrorOffset() == -1)
				System.err.println("Invalid input: Input list is empty.");
			else
				System.err.println("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
		}
	}
	
	public Enrichment(Collection<String> geneList) {
		this.geneList = geneList;
	}
	
	public Enrichment(Collection<String> geneList, boolean validate) throws ParseException {
		if (validate)	// Check if input list is valid
			FileUtils.validateList(geneList);
		this.geneList = geneList;
	}
	
	public Collection<String> getInput() {
		return geneList;
	}
	
	public LinkedList<Term> enrich(String backgroundType) {
		return enrich(FileUtils.readResource(gmtLocations.get(backgroundType)));
	}
	
	public LinkedList<Term> enrich(Collection<String> backgroundLines) {
		// Linked list of results
		LinkedList<Term> resultTerms = new LinkedList<Term>();
		
		// Background database consisting of key and genes associated with that key
		HashMap<String, HashSet<String>> bgDatabase = new HashMap<String, HashSet<String>>();
		// Unique genes in the database
		HashSet<String> bgGenes = new HashSet<String>();
		for (String line : backgroundLines) {	// Read background into hashmap
			// In gmt file, 1st column is key, 2nd column is irrelevant, and the rest are the values
			String[] splitLine = line.split("\t");
			HashSet<String> values = new HashSet<String>();
			bgDatabase.put(splitLine[0], values);
			
			for (int i = 2; i < splitLine.length; i++) {
				bgGenes.add(splitLine[i]);
				values.add(splitLine[i]);
			}
		}
		
		// Filter genes from input list that are not in the background
		HashSet<String> inputGenes = new HashSet<String>();
		for (String gene : geneList) {
			gene = gene.toUpperCase();
			if (bgGenes.contains(gene))
				inputGenes.add(gene);
		}
		
		for (String key : bgDatabase.keySet()) {
			// Background genes associated with the key
			HashSet<String> targetBgGenes = bgDatabase.get(key);
			// Input genes associated with the key
			HashSet<String> targetInputGenes = new HashSet<String>();
			
			// Target input genes is the intersection of target background genes and input genes
			targetInputGenes.addAll(targetBgGenes);
			targetInputGenes.retainAll(inputGenes);
			
			int numOfTargetBgGenes = targetBgGenes.size();
			int numOfBgGenes = bgGenes.size();
			int numOfInputGenes = inputGenes.size();
			int numOfTargetInputGenes = targetInputGenes.size();
			
			// Don't bother if there are no target input genes
			if (numOfTargetInputGenes > 0) {
				FisherExact fisherTest = new FisherExact(numOfInputGenes + numOfBgGenes);
				
				double pvalue = fisherTest.getRightTailedP(numOfTargetInputGenes, numOfInputGenes - numOfTargetInputGenes, numOfTargetBgGenes, numOfBgGenes - numOfTargetBgGenes);
				
				StringBuilder targets = new StringBuilder();
				for (String targetInputGene: targetInputGenes) {
					if (targets.length() == 0)
						targets.append(targetInputGene);
					else
						targets.append(";").append(targetInputGene);
				}
				
				resultTerms.add(new Term(key, pvalue, targets.toString()));
			}
		}
		
		// Sort by p-value
		Collections.sort(resultTerms);
		
		return resultTerms;
	}
}
