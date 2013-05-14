package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.HashSet;

public class Term {

	private String name;
	
	private HashSet<String> geneSet;
	private int numOfTargetBgGenes;
	
	private double mean;
	private double standardDeviation;
	
	public Term(String name, HashSet<String> geneSet) {
		this.name = name;
		this.geneSet = geneSet;
		this.numOfTargetBgGenes = geneSet.size();
	}
	
	public String getName() {
		return this.name;
	}
	
	public HashSet<String> getGeneSet() {
		return this.geneSet;
	}
	
	public int getNumOfTargetBgGenes() {
		return this.numOfTargetBgGenes;
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
