package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.HashSet;

public class Term {

	private String name;
	
	private HashSet<String> geneSet;
	private int numOfTermGenes;
	
	private double mean;
	private double standardDeviation;
	
	public Term(String name, HashSet<String> geneSet) {
		this.name = name;
		this.geneSet = geneSet;
		this.numOfTermGenes = geneSet.size();
	}
	
	public String getName() {
		return this.name;
	}
	
	public HashSet<String> getGeneSet() {
		return this.geneSet;
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
