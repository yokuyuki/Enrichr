package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class FuzzyGeneSetLibrary extends GeneSetLibrary {

	public FuzzyGeneSetLibrary(Collection<String> libraryLines) {
		super();
		constructTerms(libraryLines);
	}
	
	public FuzzyGeneSetLibrary(Collection<String> libraryLines, Collection<String> rankLines) {
		this(libraryLines);
		constructRanks(rankLines);
	}

	@Override
	protected void constructTerms(Collection<String> libraryLines) {
		for (String line : libraryLines) {
			// In gmt file, 1st column is key, 2nd column is irrelevant, and the rest are the values
			String[] splitLine = line.split("\\t");
			String termName = splitLine[0];
			HashMap<String, Double> geneSet = new HashMap<String, Double>((int) Math.ceil((splitLine.length-2)*0.75));
			
			for (int i = 2; i < splitLine.length; i++) {
				String[] genePair = splitLine[i].split(",");				
				String gene = genePair[0].toUpperCase();
				backgroundGenes.add(gene);
				geneSet.put(gene, Double.parseDouble(genePair[1]));
			}
			
			FuzzyTerm term = new FuzzyTerm(termName, geneSet);
			terms.put(termName, term);
		}
	}
}
