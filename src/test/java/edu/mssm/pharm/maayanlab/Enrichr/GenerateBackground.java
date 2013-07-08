package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.ArrayList;
import java.util.HashMap;

import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentCategory;
import edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentLibrary;
import edu.mssm.pharm.maayanlab.common.bio.EnrichedTerm;
import edu.mssm.pharm.maayanlab.common.core.ApprovedSymbols;
import edu.mssm.pharm.maayanlab.common.core.FileUtils;
import edu.mssm.pharm.maayanlab.common.math.Statistics;

public class GenerateBackground {
	
	private final static int REPS = 5000;
	private final static int LENGTH = 300;
	
	public static void main(String[] args) {
//		generateBackgrounds();
		generateBackground("Mouse_Gene_Atlas");
	}
	
	private static ArrayList<String> generateOutputRanks(HashMap<String, ArrayList<Integer>> ranks) {
		ArrayList<String> output = new ArrayList<String>();
		for (String tf : ranks.keySet()) {
			double mean = Statistics.findMean(ranks.get(tf));
			double sd = Statistics.findStandardDeviation(ranks.get(tf), mean);
			output.add(tf + "\t" + mean + "\t" + sd);
		}
		
		return output;
	}
	
	private static void generateBackgrounds() {
		long startTime = System.currentTimeMillis();
		
		for (EnrichmentCategory category : ResourceLoader.getInstance().getCategories())
			for (EnrichmentLibrary library : category.getLibraries())
				generateBackground(library.getName());
		
		long endTime = System.currentTimeMillis();
		System.out.println("Elapsed time: " + (endTime - startTime)/1000.0 + " seconds");
	}
	
	private static void generateBackground(String backgroundType) {
		long startTime = System.currentTimeMillis();
		
		HashMap<String, ArrayList<Integer>> ranks = new HashMap<String, ArrayList<Integer>>();
		for (int i = 0; i < REPS; i++) {
			Enrichment app = new Enrichment(ApprovedSymbols.getInstance().randomSample(LENGTH));
			app.setSetting(Enrichment.SORT_BY, Enrichment.PVALUE);
			ArrayList<EnrichedTerm> enrichedTerms = app.enrich(backgroundType);
			
			int counter = 1;
			for (EnrichedTerm enrichedTerm : enrichedTerms) {
				if (!ranks.containsKey(enrichedTerm.getName()))
					ranks.put(enrichedTerm.getName(), new ArrayList<Integer>());
				ranks.get(enrichedTerm.getName()).add(counter);
				counter++;
			}
			
			System.out.println(backgroundType + " Run: " + i + " (" + (counter-1) + ")");
		}
		
		FileUtils.writeFile(backgroundType + "_ranks.txt", generateOutputRanks(ranks));
		
		long endTime = System.currentTimeMillis();
		System.out.println(backgroundType + " Elapsed time: " + (endTime - startTime)/1000.0 + " seconds");
	}
}
