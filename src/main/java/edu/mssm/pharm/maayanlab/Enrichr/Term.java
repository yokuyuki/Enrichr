package edu.mssm.pharm.maayanlab.Enrichr;

public class Term implements Comparable<Term> {

	private String name;
	private int targetInputGenes;
	private int targetGenes;
	private double pvalue;
	private String geneList;
	
	public Term(String name, int targetInputGenes, int targetGenes, double pvalue, String geneList) {
		this.name = name;
		this.targetInputGenes = targetInputGenes;
		this.targetGenes = targetGenes;
		this.pvalue = pvalue;
		this.geneList = geneList;
	}
	
	public String getName() {
		return this.name;
	}
	
	public double getPValue() {
		return this.pvalue;
	}
	
	@Override
	public String toString() {
		return name + "\t" + targetInputGenes + "/" + targetGenes + "\t" + pvalue + "\t" + geneList;
	}
	
	@Override
	public int compareTo(Term o) {
		if (this.pvalue > o.pvalue)
			return 1;
		else if (this.pvalue < o.pvalue)
			return -1;
		else
			return 0;
	}
	
}
