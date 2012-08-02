package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import pal.statistics.FisherExact;
import edu.mssm.pharm.maayanlab.FileUtils;
import edu.mssm.pharm.maayanlab.Settings;
import edu.mssm.pharm.maayanlab.SettingsChanger;
import edu.mssm.pharm.maayanlab.SimpleXMLWriter;

public class List2Networks implements SettingsChanger {
	
	private static Logger log = Logger.getLogger("maayanlab");

	// progress tracking
	private SwingWorker<Void, Void> task = null;
	int progress = 0;
	String note = "";
	private boolean isCancelled = false;
	
	// Paths to gmt files
	private final static String BIOCARTA_LOC = "BioCarta_pathways.gmt";
	private final static String CHROMOSOME_LOC = "Chromosome_location.gmt";
	private final static String GENESIGDB_LOC = "GeneSigDB.gmt";
	private final static String GO_BP_LOC = "GeneOntology_BP.gmt";
	private final static String GO_CC_LOC = "GeneOntology_CC.gmt";
	private final static String GO_MF_LOC = "GeneOntology_MF.gmt";
	private final static String HMDB_METABOLITES_LOC = "HMDB_Metabolites.gmt";
	private final static String KEGG_LOC = "KEGG_pathways.gmt";
	private final static String MGI_MP_LOC = "MGI_MP_top4.gmt";
	private final static String MICRORNA_LOC = "microRNA.gmt";
	private final static String OMIM_DISEASE_LOC = "OMIM_disease_genes.gmt";
	private final static String PFAM_INTERPRO_LOC = "Pfam-InterPro-domains.gmt";
	private final static String REACTOME_LOC = "Reactome_pathways.gmt";
	private final static String WIKIPATHWAYS_LOC = "WikiPathways_pathways.gmt";
	
	// Default settings
	private final Settings settings = new Settings() {
		{			
			// Boolean: enable enrichment using BioCarta pathways. [true/false]
			set(List2Networks.ENABLE_BIOCARTA, true);
			// Boolean: enable enrichment using chromosome location. [true/false]
			set(List2Networks.ENABLE_CHROMOSOME, true);
			// Boolean: enable enrichment using GeneSigDB. [true/false]
			set(List2Networks.ENABLE_GENESIGDB, true);
			// Boolean: enable enrichment using Gene Ontology's biological process namespace. [true/false] 
			set(List2Networks.ENABLE_GO_BP, true);
			// Boolean: enable enrichment using Gene Ontology's cellular component namespace. [true/false]
			set(List2Networks.ENABLE_GO_CC, true);
			// Boolean: enable enrichment using Gene Ontology's molecular function namespace. [true/false]
			set(List2Networks.ENABLE_GO_MF, true);
			// Boolean: enable enrichment using HMDB metabolites. [true/false]
			set(List2Networks.ENABLE_HMDB_METABOLITES, true);
			// Boolean: enable enrichment using KEGG pathways. [true/false]
			set(List2Networks.ENABLE_KEGG, true);
			// Boolean: enable enrichment using the top 4 terms from Mouse Genome Informatics' Mammalian Phenotype. [true/false]
			set(List2Networks.ENABLE_MGI_MP, true);
			// Boolean: enable enrichment using microRNA. [true/false]
			set(List2Networks.ENABLE_MICRORNA, true);
			// Boolean: enable enrichment using OMIM disease genes. [true/false]
			set(List2Networks.ENABLE_OMIM_DISEASE, true);
			// Boolean: enable enrichment using Pfam InterPro domains. [true/false]
			set(List2Networks.ENABLE_PFAM_INTERPRO, true);
			// Boolean: enable enrichment using Reactome pathways. [true/false]
			set(List2Networks.ENABLE_REACTOME, true);
			// Boolean: enable enrichment using WikiPathways pathways. [true/false]
			set(List2Networks.ENABLE_WIKIPATHWAYS, true);
		}
	};
	
	public final static String ENABLE_BIOCARTA = "enrich using BioCarta";
	public final static String ENABLE_CHROMOSOME = "enrich using chromosome location";
	public final static String ENABLE_GENESIGDB = "enrich using GeneSigDB";
	public final static String ENABLE_GO_BP = "enrich using GO biological process";
	public final static String ENABLE_GO_CC = "enrich using GO cellular component";
	public final static String ENABLE_GO_MF = "enrich using GO molecular function";
	public final static String ENABLE_HMDB_METABOLITES = "enrich using HMDB metabolites";
	public final static String ENABLE_KEGG = "enrich using KEGG";
	public final static String ENABLE_MGI_MP = "enrich using MGI Mammalian Phenotype";
	public final static String ENABLE_MICRORNA = "enrich using microRNA";
	public final static String ENABLE_OMIM_DISEASE = "enrich using OMIM disease genes";
	public final static String ENABLE_PFAM_INTERPRO = "enrich using Pfam InterPro domains";
	public final static String ENABLE_REACTOME = "enrich using Reactome";
	public final static String ENABLE_WIKIPATHWAYS = "enrich using WikiPathways";
	
	// Formatter
	private final DecimalFormat scientificNotation = new DecimalFormat("0.##E0");
	
	// Output header
	protected final String HEADER = "Term\tP-value\tGenes"; 
	
	private HashMap<String, LinkedList<Term>> resultsMap = new HashMap<String, LinkedList<Term>>(18);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Set logger display level
		if (!Boolean.getBoolean("verbose"))
            log.setLevel(Level.WARNING);
		
		if (args.length == 2) {
			List2Networks l2n = new List2Networks();
			l2n.run(args[0]);
			l2n.writeFile(args[1]);
		}
		else if (args.length > 2) {
			String[] bgFiles = new String[args.length-2];
			System.arraycopy(args, 1, bgFiles, 0, args.length-2);
			List2Networks l2n = new List2Networks();
			l2n.run(args[0], bgFiles);
			l2n.writeFile(args[args.length-1]);
		}
		else
			log.warning("Usage: java -jar L2N.jar gene_list [background_file...] output");
	}
	
	// By default, load settings from file
	public List2Networks() {
		settings.loadSettings();
	}
	
	// Load external settings, primarily for use with X2K
	public List2Networks(Settings externalSettings) {
		settings.loadSettings(externalSettings);
	}
	
	// Task methods
	public void setTask(SwingWorker<Void, Void> task) {
		this.task = task;
	}
	
	private void setProgress(int progress, String note) throws InterruptedException {
		if (task != null) {
			if (isCancelled)
				throw new InterruptedException("Task cancelled at " + progress + "%!");
			task.firePropertyChange("progress", this.progress, progress);
			task.firePropertyChange("note", this.note, note);
			this.progress = progress;
			this.note = note;
		}
	}
	
	public void cancel() {
		isCancelled = true;
	}

	@Override
	// Used for other methods to set settings	
	public void setSetting(String key, String value) {
		settings.set(key, value);
	}
	
	public HashMap<String, LinkedList<Term>> getEnrichmentResults() {
		return resultsMap;
	}
	
	public void writeFile(String filename) {
		// Prefix for individual files
		String outputPrefix = filename.replaceFirst("\\.\\w+$", "");
		
		SimpleXMLWriter sxw = new SimpleXMLWriter(filename);
		sxw.startPlainElement("Enrichment");
		
		sxw.startPlainElement("Summary");
		LinkedList<Term> combinedTerms = new LinkedList<Term>();
		for (LinkedList<Term> termList : resultsMap.values())
			combinedTerms.addAll(termList);
		Collections.sort(combinedTerms);
		
		// Filter down to top 10
		while (combinedTerms.size() > 10)
			combinedTerms.removeLast();
		
		for (Term term : combinedTerms)
			sxw.listElement("Term", term.getName(), "p-value", scientificNotation.format(term.getPValue()));
		
		sxw.endElement();
		
		for (String bgType : resultsMap.keySet()) {
			// Write XML summary output
			sxw.startElementWithAttributes("Background", "name", bgType);
			int i = 1;
			for (Term term : resultsMap.get(bgType)) {
				sxw.listElement("Term",	term.getName(), "p-value", scientificNotation.format(term.getPValue()));
				// Stop after 10 entries
				if (i++ >= 10)
					break;
			}
			sxw.endElement();
					
			// Write individual enrichment tsv outputs
			FileUtils.writeFile(outputPrefix + "_" + bgType.replaceAll(" ", "_") + ".tsv", HEADER, resultsMap.get(bgType));
		}
		
		sxw.close();
	}
	
	// Run from cli with custom database
	public void run(String geneList, String[] backgroundFiles) {
		log.info("Running with custom database");
		ArrayList<String> inputList = FileUtils.readFile(geneList);
		
		try {
			if (FileUtils.validateList(inputList)) {
				HashMap<String, String> bgList = new HashMap<String, String>();
				for (String backgroundFile : backgroundFiles)
					bgList.put((new File(backgroundFile)).getName().replaceFirst("\\.\\w+$", ""), backgroundFile);
				computeEnrichment(bgList, inputList);
			}
		} catch (ParseException e) {
			if (e.getErrorOffset() == -1)
				log.warning("Invalid input: Input list is empty.");
			else
				log.warning("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
			System.exit(-1);	
		} catch (InterruptedException e) {
			log.severe("CLI should never throw this error due to lack of progress bar");
		}
	}
	
	// Run for file names
	public void run(String geneList) {
		ArrayList<String> inputList = FileUtils.readFile(geneList);
		
		try {
			if (FileUtils.validateList(inputList))
				run(inputList);
		} catch (ParseException e) {
			if (e.getErrorOffset() == -1)
				log.warning("Invalid input: Input list is empty.");
			else
				log.warning("Invalid input: " + e.getMessage() + " at line " + (e.getErrorOffset() + 1) + " is not a valid Entrez Gene Symbol.");
			System.exit(-1);	
		}
	}
	
	// Run for calling from other methods and pass in collection
	public void run(Collection<String> geneList) {
		// Initial capacity 18 * 0.75 load factor = 13.5
		HashMap<String, String> bgList = new HashMap<String, String>(18);
		
		if (settings.getBoolean(ENABLE_BIOCARTA))
			bgList.put("BioCarta Pathways", BIOCARTA_LOC);
		if (settings.getBoolean(ENABLE_CHROMOSOME))
			bgList.put("Chromosome Location", CHROMOSOME_LOC);
		if (settings.getBoolean(ENABLE_GENESIGDB))
			bgList.put("GeneSigDB", GENESIGDB_LOC);
		if (settings.getBoolean(ENABLE_GO_BP))
			bgList.put("GO Biological Process", GO_BP_LOC);
		if (settings.getBoolean(ENABLE_GO_CC))
			bgList.put("GO Cellular Component", GO_CC_LOC);
		if (settings.getBoolean(ENABLE_GO_MF))
			bgList.put("GO Molecular Function", GO_MF_LOC);
		if (settings.getBoolean(ENABLE_HMDB_METABOLITES))
			bgList.put("HMDB Metabolites", HMDB_METABOLITES_LOC);
		if (settings.getBoolean(ENABLE_KEGG))
			bgList.put("KEGG Pathways", KEGG_LOC);
		if (settings.getBoolean(ENABLE_MGI_MP))
			bgList.put("MGI Mammalian Phenotype", MGI_MP_LOC);
		if (settings.getBoolean(ENABLE_MICRORNA))
			bgList.put("microRNA", MICRORNA_LOC);
		if (settings.getBoolean(ENABLE_OMIM_DISEASE))
			bgList.put("OMIM Disease", OMIM_DISEASE_LOC);
		if (settings.getBoolean(ENABLE_PFAM_INTERPRO))
			bgList.put("PFAM InterPro Domains", PFAM_INTERPRO_LOC);
		if (settings.getBoolean(ENABLE_REACTOME))
			bgList.put("Reactome Pathways", REACTOME_LOC);
		if (settings.getBoolean(ENABLE_WIKIPATHWAYS))
			bgList.put("WikiPathways Pathways", WIKIPATHWAYS_LOC);
		
		try {
			setProgress(0, "Enriching terms...");
			computeEnrichment(bgList, geneList);
			setProgress(90, "Writing results...");
		} catch (InterruptedException e) {
			log.info(e.getMessage());
			return;
		}
	}

	public void computeEnrichment(HashMap<String, String> backgroundList, Collection<String> geneList) throws InterruptedException {
		int iteration = 0;
		int increment = 80 / backgroundList.keySet().size();
		
		for (String bgType : backgroundList.keySet()) {
			try {
				setProgress(5+increment*iteration, bgType + " enrichment...");
				iteration++;
			} catch (InterruptedException e) {
				throw new InterruptedException(e.getMessage());
			}
			
			// Read background file into an array of lines
			ArrayList<String> bgFile = FileUtils.readResource(backgroundList.get(bgType));
			// Linked List of results
			LinkedList<Term> resultTerms = new LinkedList<Term>();
			
			// Background database consisting of key and genes associated with that key
			HashMap<String, HashSet<String>> bgDatabase = new HashMap<String, HashSet<String>>();
			// Unique genes in the database
			HashSet<String> bgGenes = new HashSet<String>();
			for (String line : bgFile) {	// Read background into hashmap
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
							targets.append(",").append(targetInputGene);
					}
					
					resultTerms.add(new Term(key, pvalue, targets.toString()));
				}
			}
			
			// Sort by p-value
			Collections.sort(resultTerms);
			
			// Only add to results if there are actual results
			if (!resultTerms.isEmpty())
				resultsMap.put(bgType, resultTerms);
		}
	}
	
}
