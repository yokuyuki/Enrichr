package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Map;
import java.util.Set;

public class FuzzyTerm extends Term {

	private Map<String, Double> geneSet;
	
	public FuzzyTerm(String name, FuzzyGeneSetLibrary geneSetLibrary) {
		super(name, geneSetLibrary);
	}
	
	public FuzzyTerm(String name, FuzzyGeneSetLibrary geneSetLibrary, Map<String, Double> geneSet) {
		this(name, geneSetLibrary);
		setGeneSet(geneSet);
	}
	
	public Map<String, Double> getWeightedGeneSet() {
		return geneSet;
	}
	
	@Override
	public Set<String> getGeneSet() {
		return geneSet.keySet();
	}
	
	public void setGeneSet(Map<String, Double> geneSet) {
		this.geneSet = geneSet;
		this.numOfTermGenes = geneSet.size();
	}
}
