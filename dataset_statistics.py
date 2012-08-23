import json
import os

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

			datasets.append([file.replace('.gmt', ''), terms, len(unique_genes), average_genes])

with open('dataset_statistics.json', 'w') as out:
	out.write(json.dumps(datasets))