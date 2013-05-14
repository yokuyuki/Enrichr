package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.HashSet;

public class EnrichedTerm implements Comparable<EnrichedTerm> {

	private String name;
	private int numOfTargetInputGenes;
	private int numOfTargetBgGenes;
	
	private double mean;
	private double standardDeviation;
	
	private double pvalue;
	private double adjustedpvalue;
	private double zscore;
	private double combinedScore;
	
	private HashSet<String> targets;
	
	public EnrichedTerm(String name, HashSet<String> targets) {
		this.name = name;
		this.targets = targets;
		this.numOfTargetBgGenes = targets.size();
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getPValue() {
		return this.pvalue;
	}
	
	public double getAdjustedPValue() {
		return this.adjustedpvalue;
	}
	
	public double getZScore() {
		return this.zscore;
	}
	
	public double getCombinedScore() {
		return this.combinedScore;
	}
	
	public HashSet<String> getTargets() {
		return this.targets;
	}
	
	public int getNumOfTargetBgGenes() {
		return this.numOfTargetBgGenes;
	}
	
	public void setPValue(double pvalue) {
		if (pvalue == 0)
			pvalue = Double.MIN_VALUE;
		this.pvalue = pvalue;
	}
	
	public void setAdjustedPValue(double adjustedpvalue) {
		this.adjustedpvalue = adjustedpvalue;
	}
	
	public void setEnrichedTargets(HashSet<String> enrichedTargets) {
		this.targets = enrichedTargets;
		this.numOfTargetInputGenes = enrichedTargets.size();
	}
	
	public void setRankStats(double mean, double standardDeviation) {
		this.mean = mean;
		this.standardDeviation = standardDeviation;
	}
	
	public void computeScore(int currentRank) {
		if (mean == 0 && standardDeviation == 0)
			zscore = 0;
		else
			zscore = (currentRank - mean)/standardDeviation;
		combinedScore = Math.log(adjustedpvalue)*zscore;
	}
	
	@Override
	public String toString() {
		StringBuilder outputString = new StringBuilder();
		outputString.append(name).append("\t");
		outputString.append(numOfTargetInputGenes).append("/").append(numOfTargetBgGenes).append("\t");
		outputString.append(pvalue).append("\t");
		outputString.append(adjustedpvalue).append("\t");
		outputString.append(zscore).append("\t");
		outputString.append(combinedScore).append("\t");
		
		boolean firstTarget = true;
		for (String target : targets) {
			if (firstTarget) {
				outputString.append(target);
				firstTarget = false;
			}
			else
				outputString.append(";").append(target);
		}
		
		return outputString.toString();
	}
	
	@Override
	public int compareTo(EnrichedTerm o) {
		if (this.pvalue > o.pvalue)
			return 1;
		else if (this.pvalue < o.pvalue)
			return -1;
		else
			return 0;
	}
	
}
