package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Collection;
import java.util.HashMap;

import edu.mssm.pharm.maayanlab.common.math.NumberUtils;

public class FuzzyGeneSetLibrary extends GeneSetLibrary {

	public FuzzyGeneSetLibrary(Collection<String> libraryLines) {
		super(libraryLines);
	}
	
	public FuzzyGeneSetLibrary(Collection<String> libraryLines, Collection<String> rankLines) {
		super(libraryLines, rankLines);
	}

	@Override
	protected void constructTerms(Collection<String> libraryLines) {
		for (String line : libraryLines) {
			// In gmt file, 1st column is key, 2nd column is irrelevant, and the rest are the values
			String[] splitLine = line.split("\\t");
			String termName = splitLine[0];
			// Initialize HashMap to the rounded up to nearest integer of (splitLine.length-2)/0.75 to make one of sufficient size
			HashMap<String, Double> geneSet = new HashMap<String, Double>(NumberUtils.roundUpDivision((splitLine.length-2)*4, 3)); 
			
			for (int i = 2; i < splitLine.length; i++) {
				String[] genePair = splitLine[i].split(",");				
				String gene = genePair[0].toUpperCase();
				backgroundGenes.add(gene);
				geneSet.put(gene, Double.parseDouble(genePair[1]));
			}
			
			FuzzyTerm term = new FuzzyTerm(termName, this, geneSet);
			terms.put(termName, term);
		}
	}
}
