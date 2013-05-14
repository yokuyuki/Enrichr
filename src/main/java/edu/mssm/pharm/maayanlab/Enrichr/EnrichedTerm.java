package edu.mssm.pharm.maayanlab.Enrichr;

import java.util.HashSet;

public class EnrichedTerm implements Comparable<EnrichedTerm> {

	private Term associatedTerm;

	private HashSet<String> overlap;
	private int numOfOverlappingGenes;
	
	private double pValue;
	private double adjustedPValue;
	private double zScore;
	private double combinedScore;
	
	public EnrichedTerm(Term associatedTerm, HashSet<String> overlap, double pValue) {
		this.associatedTerm = associatedTerm;
		this.overlap = overlap;
		this.numOfOverlappingGenes = overlap.size();
		this.pValue = (pValue == 0) ? Double.MIN_VALUE : pValue;
	}
	
	public String getName() {
		return associatedTerm.getName();
	}
	
	public double getPValue() {
		return this.pValue;
	}
	
	public double getAdjustedPValue() {
		return this.adjustedPValue;
	}
	
	public double getZScore() {
		return this.zScore;
	}
	
	public double getCombinedScore() {
		return this.combinedScore;
	}
	
	public HashSet<String> getOverlap() {
		return this.overlap;
	}
	
	public void setAdjustedPValue(double adjustedpvalue) {
		this.adjustedPValue = adjustedpvalue;
	}
	
	public void computeScore(int currentRank) {
		double mean = associatedTerm.getMean();
		double standardDeviation = associatedTerm.getStandardDeviation();
		
		if (mean == 0 && standardDeviation == 0)
			zScore = 0;
		else
			zScore = (currentRank - mean)/standardDeviation;
		combinedScore = Math.log(adjustedPValue)*zScore;
	}
	
	@Override
	public String toString() {
		StringBuilder outputString = new StringBuilder();
		outputString.append(associatedTerm.getName()).append("\t");
		outputString.append(numOfOverlappingGenes).append("/").append(associatedTerm.getNumOfTermGenes()).append("\t");
		outputString.append(pValue).append("\t");
		outputString.append(adjustedPValue).append("\t");
		outputString.append(zScore).append("\t");
		outputString.append(combinedScore).append("\t");
		
		boolean firstTarget = true;
		for (String target : overlap) {
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
		if (this.pValue > o.pValue)
			return 1;
		else if (this.pValue < o.pValue)
			return -1;
		else
			return 0;
	}
	
}
