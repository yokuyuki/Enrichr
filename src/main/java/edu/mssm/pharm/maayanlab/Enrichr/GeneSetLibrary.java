package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class GeneSetLibrary {

	private HashMap<String, Term> terms = new HashMap<String, Term>();
	private HashSet<String> backgroundGenes = new HashSet<String>();
	private int numOfBackgroundGenes;
	
	public GeneSetLibrary(Collection<String> libraryLines) {
		constructTerms(libraryLines);
		numOfBackgroundGenes = backgroundGenes.size();
	}
	
	public GeneSetLibrary(Collection<String> libraryLines, Collection<String> rankLines) {
		this(libraryLines);
		constructRanks(rankLines);
	}
	
	private void constructTerms(Collection<String> libraryLines) {
		for (String line : libraryLines) {
			// In gmt file, 1st column is key, 2nd column is irrelevant, and the rest are the values
			String[] splitLine = line.split("\\t");
			String termName = splitLine[0];
			HashSet<String> targets = new HashSet<String>();
			
			for (int i = 2; i < splitLine.length; i++) {
				String gene = splitLine[i].toUpperCase();
				backgroundGenes.add(gene);
				targets.add(gene);
			}
			
			Term term = new Term(termName, targets);
			terms.put(termName, term);
		}
	}
	
	private void constructRanks(Collection<String> rankLines) {
		for (String line : rankLines) {
			String[] splitLine = line.split("\\t");	// Splits into 3 columns: name, average, std
			terms.get(splitLine[0]).setRankStats(Double.parseDouble(splitLine[1]), Double.parseDouble(splitLine[2]));
		}
	}

	public int getNumOfBackgroundGenes() {
		return numOfBackgroundGenes;
	}	
}
