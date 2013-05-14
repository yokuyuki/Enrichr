/**
 * Data structure to represent a gene-set library.
 * 
 * @author		Edward Y. Chen
 * @since		5/14/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class GeneSetLibrary {

	private final HashMap<String, Term> terms = new HashMap<String, Term>();
	private final HashSet<String> backgroundGenes = new HashSet<String>();	
	private int numOfBackgroundGenes;
	private boolean isRanked = false;
	
	public GeneSetLibrary(Collection<String> libraryLines) {
		constructTerms(libraryLines);
		numOfBackgroundGenes = backgroundGenes.size();
	}
	
	public GeneSetLibrary(Collection<String> libraryLines, Collection<String> rankLines) {
		this(libraryLines);
		constructRanks(rankLines);
		this.isRanked = true;
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
			Term term = terms.get(splitLine[0]);
			term.setMean(Double.parseDouble(splitLine[1]));
			term.setStandardDeviation(Double.parseDouble(splitLine[2]));
		}
	}

	public Collection<Term> getTerms() {
		return terms.values();
	}
	
	public int getNumOfBackgroundGenes() {
		return numOfBackgroundGenes;
	}
	
	public boolean contains(String gene) {
		return backgroundGenes.contains(gene);
	}

	public boolean isRanked() {
		return isRanked;
	}
}
