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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

import pal.statistics.FisherExact;
import edu.mssm.pharm.maayanlab.common.core.FileUtils;
import edu.mssm.pharm.maayanlab.common.core.Settings;
import edu.mssm.pharm.maayanlab.common.core.SettingsChanger;

public class Enrichment implements SettingsChanger {
	
	// Default settings
	private final Settings settings = new Settings() {
		{
			// String: rank the Enrichment by the Fisher Exact test's p-value, rank against the background of random genes, or combined score of the two. [combined score/p-value/rank]
			set(Enrichment.SORT_BY, Enrichment.COMBINED_SCORE);
		}
	};
	// Settings variables
	public final static String SORT_BY = "sort enrichment by";
	public final static String COMBINED_SCORE = "combined score";
	public final static String PVALUE = "p-value";
	public final static String RANK = "rank";
	
	public static final String HEADER = "Term\tOverlap\tP-value\tAdjusted P-value\tZ-score\tCombined Score\tGenes";
	
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
	
	@Override
	public void setSetting(String key, String value) {
		settings.set(key, value);
	}
	
	public Collection<String> getInput() {
		return geneList;
	}
	
	public LinkedList<Term> enrich(String backgroundType) {
		// Read background list and ranks
		Collection<String> backgroundLines = FileUtils.readResource(backgroundType + ".gmt");
		Collection<String> rankLines = FileUtils.readResource(backgroundType + "_ranks.txt");
		
		return enrich(backgroundLines, rankLines);
	}
	
	public LinkedList<Term> enrich(Collection<String> backgroundLines, Collection<String> rankLines) {
		// List of background terms
		LinkedList<Term> termList = new LinkedList<Term>();
		
		// Rank database
		HashMap<String, String> rankMap = null;
		if (rankLines != null) {
			rankMap = new HashMap<String, String>((int) Math.ceil(rankLines.size() / 0.75));
			for (String line : rankLines) {
				String[] splitLine = line.split("\\t", 2);
				rankMap.put(splitLine[0], splitLine[1]);
			}
		}
		
		// Unique genes in the database
		HashSet<String> bgGenes = new HashSet<String>();
		for (String line : backgroundLines) {	// Read background into hashmap
			// In gmt file, 1st column is key, 2nd column is irrelevant, and the rest are the values
			String[] splitLine = line.split("\t");
			String termName = splitLine[0];
			HashSet<String> targets = new HashSet<String>();
			
			for (int i = 2; i < splitLine.length; i++) {
				bgGenes.add(splitLine[i].toUpperCase());
				targets.add(splitLine[i].toUpperCase());
			}
			
			Term term = new Term(termName, targets);
			termList.add(term);
			
			// Add rank data
			if (rankLines != null && rankMap.containsKey(termName)) {
				String[] splitRank = rankMap.get(termName).split("\\t");
				term.setRankStats(Double.parseDouble(splitRank[0]), Double.parseDouble(splitRank[1]));
			}
		}
		
		// Filter genes from input list that are not in the background
		HashSet<String> inputGenes = new HashSet<String>();
		for (String gene : geneList) {
			gene = gene.toUpperCase();
			if (bgGenes.contains(gene))
				inputGenes.add(gene);
		}		
		
		for (ListIterator<Term> termIterator = termList.listIterator(); termIterator.hasNext(); ) {
			Term currentTerm = termIterator.next();
			
			// Input genes associated with the key
			HashSet<String> targetInputGenes = new HashSet<String>();
			
			// Target input genes is the intersection of target background genes and input genes
			targetInputGenes.addAll(currentTerm.getTargets());	// Background genes associated with the term
			targetInputGenes.retainAll(inputGenes);
			
			int numOfTargetBgGenes = currentTerm.getNumOfTargetBgGenes();
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
				
				currentTerm.setEnrichedTargets(targetInputGenes);
				currentTerm.setPValue(pvalue);
			}
			else {
				termIterator.remove();
			}
		}
		
		// Sort by p-value
		Collections.sort(termList);
		Collections.reverse(termList);	// Reverse to do Benjamini-Hochberg
		
		// Calculate adjusted p-value
		int rank = termList.size();		
		double previousValue = 1;	// Prevent adjusted p-value to be > 1
		for (Term term : termList) {
			double adjustedpvalue = Math.min(previousValue, term.getPValue() * termList.size() / rank);	// Ensure monotonicity
			previousValue = adjustedpvalue;
			term.setAdjustedPValue(adjustedpvalue);
			if (rankLines != null)
				term.computeScore(rank--);	// Count current rank and compute z-score			
		}
		
		if (rankLines != null && settings.get(SORT_BY).equals(COMBINED_SCORE)) {
			Collections.sort(termList, new Comparator<Term>() {
				@Override
				public int compare(Term o1, Term o2) {
					if (o1.getCombinedScore() < o2.getCombinedScore())				
						return 1;
					else if (o1.getCombinedScore() > o2.getCombinedScore())
						return -1;
					else
						return 0;
				}
			});
		}
		
		return termList;
	}
}
