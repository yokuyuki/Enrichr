/**
 * Data structure to represent a gene-set library.
 * 
 * @author		Edward Y. Chen
 * @since		5/14/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import pal.statistics.FisherExact;
import edu.mssm.pharm.maayanlab.common.math.SetOps;

public class GeneSetLibrary {

	protected final HashMap<String, Term> terms = new HashMap<String, Term>();
	protected final HashSet<String> backgroundGenes = new HashSet<String>();	
	protected int numOfBackgroundGenes;
	protected boolean isRanked = false;
	
	public GeneSetLibrary(Collection<String> libraryLines) {
		constructTerms(libraryLines);
		numOfBackgroundGenes = backgroundGenes.size();
	}
	
	public GeneSetLibrary(Collection<String> libraryLines, Collection<String> rankLines) {
		this(libraryLines);
		constructRanks(rankLines);
	}
	
	protected void constructTerms(Collection<String> libraryLines) {
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
	
	protected void constructRanks(Collection<String> rankLines) {
		for (String line : rankLines) {
			String[] splitLine = line.split("\\t");	// Splits into 3 columns: name, average, std
			Term term = terms.get(splitLine[0]);
			term.setMean(Double.parseDouble(splitLine[1]));
			term.setStandardDeviation(Double.parseDouble(splitLine[2]));
		}
		this.isRanked = true;
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
	
	// Filter genes from the input list that are not in the gene-set library
	protected HashSet<String> filterInputGenes(Collection<String> inputGeneList) {
		HashSet<String> inputGenes = new HashSet<String>();
		for (String gene : inputGeneList) {
			gene = gene.toUpperCase();
			if (this.contains(gene))
				inputGenes.add(gene);
		}
		
		return inputGenes;
	}
	
	public ArrayList<EnrichedTerm> getEnrichedTerms(Collection<String> inputGeneList) {
		HashSet<String> inputGenes = filterInputGenes(inputGeneList);
		
		ArrayList<EnrichedTerm> enrichedTerms = new ArrayList<EnrichedTerm>();
		for (Term currentTerm : this.getTerms()) {
			// Intersection of term's gene set and input genes
			Set<String> overlap = SetOps.intersection(currentTerm.getGeneSet(), inputGenes); 
			
			int numOfTermGenes = currentTerm.getNumOfTermGenes();
			int numOfBgGenes = this.getNumOfBackgroundGenes();
			int numOfInputGenes = inputGenes.size();
			int numOfOverlappingGenes = overlap.size();
			
			// Don't bother if there are no target input genes
			if (numOfOverlappingGenes > 0) {
				FisherExact fisherTest = new FisherExact(numOfInputGenes + numOfBgGenes);				
				double pValue = fisherTest.getRightTailedP(numOfOverlappingGenes, numOfInputGenes - numOfOverlappingGenes, numOfTermGenes, numOfBgGenes - numOfTermGenes);
								
				enrichedTerms.add(new EnrichedTerm(currentTerm, overlap, pValue));
			}
		}
		
		return enrichedTerms;
	}
}
