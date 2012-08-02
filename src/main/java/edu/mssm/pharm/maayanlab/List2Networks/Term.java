package edu.mssm.pharm.maayanlab.List2Networks;

public class Term implements Comparable<Object> {

	private String name;
	private double pvalue;
	private String geneList;
	
	public Term(String name, double pvalue, String geneList) {
		this.name = name;
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
		return name + "\t" + pvalue + "\t" + geneList;
	}
	
	@Override
	public int compareTo(Object o) {
		if (this.pvalue > ((Term) o).pvalue)
			return 1;
		else if (this.pvalue < ((Term) o).pvalue)
			return -1;
		else
			return 0;
	}
	
}
