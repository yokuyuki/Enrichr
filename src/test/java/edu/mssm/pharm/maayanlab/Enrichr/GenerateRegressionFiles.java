package edu.mssm.pharm.maayanlab.Enrichr;

import edu.mssm.pharm.maayanlab.FileUtils;

public class GenerateRegressionFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Enrichment app = new Enrichment(FileUtils.readResource("test_list.txt"));
		for (int i = 0; i < Enrichment.categories.length; i++)
			for (String bgType : Enrichment.categorizedEnrichmentTypes[i])
				FileUtils.writeFile("test_list." + bgType + "_table.txt", Enrichment.HEADER, app.enrich(bgType));
	}

}
