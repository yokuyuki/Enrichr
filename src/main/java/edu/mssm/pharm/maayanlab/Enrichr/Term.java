package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.Set;

public class Term {

	private String name;
	protected GeneSetLibrary geneSetLibrary;
	
	private Set<String> geneSet;
	protected int numOfTermGenes;
	
	private double mean;
	private double standardDeviation;
	
	public Term(String name, GeneSetLibrary geneSetLibrary) {
		this.name = name;
		this.geneSetLibrary = geneSetLibrary;
	}
	
	public Term(String name, GeneSetLibrary geneSetLibrary, Set<String> geneSet) {
		this(name, geneSetLibrary);
		setGeneSet(geneSet);
	}
	
	public String getName() {
		return this.name;
	}
	
	public Set<String> getGeneSet() {
		return this.geneSet;
	}
	
	public void setGeneSet(Set<String> geneSet) {
		this.geneSet = geneSet;
		this.numOfTermGenes = geneSet.size();
	}
	
	public int getNumOfTermGenes() {
		return this.numOfTermGenes;
	}
	
	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStandardDeviation() {
		return standardDeviation;
	}

	public void setStandardDeviation(double standardDeviation) {
		this.standardDeviation = standardDeviation;
	}
}
