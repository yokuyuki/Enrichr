package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import edu.mssm.pharm.maayanlab.FileUtils;
import edu.mssm.pharm.maayanlab.Statistics;

public class GenerateBackground {
	
	private final static String approvedSymbols = "src/test/resources/approved_symbols.txt";
	
	private final static int REPS = 1000;
	private final static int LENGTH = 300;
	
	private final static Random rng = new Random();
	
	public static void main(String[] args) {
		generateBackground();
	}
	
	private static Collection<String> generateRandomSample(ArrayList<String> list, int samples) {
		HashSet<String> sampleList = new HashSet<String>();
		while (sampleList.size() < samples)
			sampleList.add(list.get(rng.nextInt(list.size())));
		return sampleList;
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
	
	private static void generateBackground() {
		long startTime = System.currentTimeMillis();
		
		ArrayList<String> genes = FileUtils.readFile(approvedSymbols);
		HashMap<String, HashMap<String, ArrayList<Integer>>> rankMap = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
		for (String backgroundType : Enrichment.backgroundTypes)
			rankMap.put(backgroundType, new HashMap<String, ArrayList<Integer>>());
		
		for (int i = 0; i < REPS; i++) {
			Enrichment app = new Enrichment(generateRandomSample(genes, LENGTH));
			
			for (String backgroundType: Enrichment.backgroundTypes) {
				LinkedList<Term> terms = app.enrich(backgroundType);
//				app.setSetting(ChEA.SORT_BY, ChEA.PVALUE);8
				
				int counter = 1;
				for (Term term : terms) {
					if (!rankMap.get(backgroundType).containsKey(term.getName()))
						rankMap.get(backgroundType).put(term.getName(), new ArrayList<Integer>());
					rankMap.get(backgroundType).get(term.getName()).add(counter);
					counter++;
				}

				System.out.println(backgroundType + " Run: " + i + " (" + (counter-1) + ")");
			}
		}
		
		for (String backgroundType: Enrichment.backgroundTypes)
			FileUtils.writeFile(backgroundType + "_ranks.txt", generateOutputRanks(rankMap.get(backgroundType)));		
		
		long endTime = System.currentTimeMillis();
		System.out.println("Elapsed time: " + (endTime - startTime)/1000.0 + " seconds");
	}
	
}
