import json
import os

gmt_lookup = dict()
gmt_lookup['BioCarta_pathways.gmt'] = ('BioCarta', 'http://pid.nci.nih.gov/download.shtml')
gmt_lookup['ChEA.gmt'] = ('ChEA', 'http://amp.pharm.mssm.edu/lib/cheadownload.jsp')
gmt_lookup['Chromosome_location.gmt'] = ('Chromosome Location', 'http://hgdownload.cse.ucsc.edu/downloads.html')
gmt_lookup['Down-regulated_CMAP_100.gmt'] = ('Down-regulated CMAP', 'http://www.broadinstitute.org/cmap/')
gmt_lookup['ENCODE_TF_ChIP-seq.gmt'] = ('ENCODE TF ChIP-seq', 'http://genome.ucsc.edu/ENCODE/downloads.html')
gmt_lookup['GeneOntology_BP.gmt'] = ('GO Biological Process', 'http://www.geneontology.org/GO.downloads.annotations.shtml')
gmt_lookup['GeneOntology_CC.gmt'] = ('GO Cellular Component', 'http://www.geneontology.org/GO.downloads.annotations.shtml')
gmt_lookup['GeneOntology_MF.gmt'] = ('GO Molecular Function', 'http://www.geneontology.org/GO.downloads.annotations.shtml')
gmt_lookup['GeneSigDB.gmt'] = ('GeneSigDB', 'http://compbio.dfci.harvard.edu/genesigdb/downloadall.jsp')
gmt_lookup['Genome_Browser_PWMs.gmt'] = ('Genome Browser PWMs', 'http://hgdownload.cse.ucsc.edu/goldenPath/hg18/database/')
gmt_lookup['Histone_Modifications_ChIP-seq.gmt'] = ('Histone Modifications ChIP-seq', 'http://www.ncbi.nlm.nih.gov/geo/roadmap/epigenomics/')
gmt_lookup['HMDB_Metabolites.gmt'] = ('HMDB Metabolites', 'http://www.hmdb.ca/downloads')
gmt_lookup['Human_Gene_Atlas.gmt'] = ('Human Gene Atlas', 'http://biogps.org/downloads/')
gmt_lookup['KEA.gmt'] = ('KEA', 'http://amp.pharm.mssm.edu/lib/keacommandline.jsp')
gmt_lookup['KEGG_pathways.gmt'] = ('KEGG', 'http://www.kegg.jp/kegg/download/')
gmt_lookup['MGI_MP_top3.gmt'] = ('MGI Mammalian Phenotype Top 3', 'ftp://ftp.informatics.jax.org/pub/reports/index.html#pheno')
gmt_lookup['MGI_MP_top4.gmt'] = ('MGI Mammalian Phenotype Top 4', 'ftp://ftp.informatics.jax.org/pub/reports/index.html#pheno')
gmt_lookup['microRNA.gmt'] = ('microRNA', 'http://www.microrna.org/microrna/getDownloads.do')
gmt_lookup['Mouse_Gene_Atlas.gmt'] = ('Mouse Gene Atlas', 'http://biogps.org/downloads/')
gmt_lookup['NCI60.gmt'] = ('NCI-60 Cancer Cell Lines', 'http://biogps.org/downloads/')
gmt_lookup['OMIM_disease_genes.gmt'] = ('OMIM Disease', 'http://www.omim.org/downloads')
gmt_lookup['OMIM_Expanded.gmt'] = ('OMIM Expanded', 'http://www.omim.org/downloads')
gmt_lookup['Pfam-InterPro-domains.gmt'] = ('Pfam InterPro Domains', 'ftp://ftp.ebi.ac.uk/pub/databases/interpro/')
gmt_lookup['PPI_Hub_Proteins.gmt'] = ('PPI Hub Proteins', 'http://amp.pharm.mssm.edu/genes2networks/')
gmt_lookup['Reactome_pathways.gmt'] = ('Reactome', 'http://www.reactome.org/download/index.html')
gmt_lookup['TRANSFAC_JASPAR.gmt'] = ('TRANSFAC/JASPAR PWMs', 'http://jaspar.genereg.net/html/DOWNLOAD/')
gmt_lookup['Up-regulated_CMAP_100.gmt'] = ('Up-regulated CMAP', 'http://www.broadinstitute.org/cmap/')
gmt_lookup['VirusMINT.gmt'] = ('VirusMINT', 'http://mint.bio.uniroma2.it/virusmint/download.do')
gmt_lookup['WikiPathways_pathways.gmt'] = ('WikiPathways', 'http://www.wikipathways.org/index.php/Download_Pathways')

datasets = []
for file in os.listdir('src/main/resources'):
	if file.endswith('.gmt'):
		with open('src/main/resources/' + file) as sig_file:
			terms = 0
			average_genes = 0
			unique_genes = set()

			for line in sig_file:
				terms += 1
				split_line = line.rstrip().split('\t')
				average_genes += len(split_line[2:])
				unique_genes = unique_genes.union(set(split_line[2:]))
			average_genes /= float(terms)

			datasets.append([gmt_lookup[file][0], terms, len(unique_genes), average_genes, gmt_lookup[file][1]])

with open('dataset_statistics.json', 'w') as out:
	out.write(json.dumps(datasets))