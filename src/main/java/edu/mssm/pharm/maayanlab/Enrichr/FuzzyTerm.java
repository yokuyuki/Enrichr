package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Map;
import java.util.Set;

import edu.mssm.pharm.maayanlab.common.math.FuzzyEnrichment;
import edu.mssm.pharm.maayanlab.common.math.SetOps;

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
	
	@Override
	public EnrichedTerm getEnrichedTerm(Set<String> inputGenes) {
		EnrichedTerm enrichedTerm = null;
		
		// Intersection of term's gene set and input genes
		Set<String> overlap = SetOps.intersection(this.getGeneSet(), inputGenes);
		
		if (overlap.size() > 0) {
			return new EnrichedTerm(this, overlap, FuzzyEnrichment.unweightedToWeighted(inputGenes, this.getWeightedGeneSet()));
		}
		
		return enrichedTerm;
	}
}
