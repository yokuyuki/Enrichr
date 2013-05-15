package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.HashMap;
import java.util.Set;

public class FuzzyTerm extends Term {

	private HashMap<String, Double> geneSet;
	
	public FuzzyTerm(String name) {
		super(name);
	}
	
	public FuzzyTerm(String name, HashMap<String, Double> geneSet) {
		super(name);
		setGeneSet(geneSet);
	}
	
	public HashMap<String, Double> getWeightedGeneSet() {
		return geneSet;
	}
	
	@Override
	public Set<String> getGeneSet() {
		return geneSet.keySet();
	}
	
	public void setGeneSet(HashMap<String, Double> geneSet) {
		this.geneSet = geneSet;
		this.numOfTermGenes = geneSet.size();
	}
}
