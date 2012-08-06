<!DOCTYPE html>
<html lang="en">
<%! private String[] enrichmentTypes = {"Biocarta",
										"Chromosome_Location",
										"GeneSigDB",
										"GO_Biological_Process",
										"GO_Cellular_Component",
										"GO_Molecular_Function",
										"HMDB_Metabolites",
										"KEGG",
										"MGI_Mammalian_Phenotype",
										"microRNA",
										"OMIM_Disease",
										"Pfam_InterPro_Domains",
										"Reactome",
										"WikiPathways"}; %>
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/reset/reset-min.css">
	<link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/css/jquery.dataTables.css">		
	<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'>
	<link rel="stylesheet" type="text/css" href="black-widow.css">
	<link rel="stylesheet" type="text/css" href="black-widow-results.css">
	<script type="text/javascript" src="http://d3js.org/d3.v2.min.js"></script>
	<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
	<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="results.js"></script>
	<title>Enrichr</title>
</head>
<body>	
	<div id="logo">En<span>rich</span>r</div>
	<% for (String type : enrichmentTypes) { %>
		<% String name = type.replaceAll("_", " "); %>
		<div class="beveled" id="<%=type%>">
			<div class="header">
				<a href="#" onclick="getResult('<%=type%>'); return false;" class="title"><%=name%></a>
				<table class="nav">
					<tr>
						<td class="selected">
							<a href="#" onclick="navigateTo(0, '#<%=type%>'); return false;">Bar Graph View</a>
						</td>
						<td>
							<a href="#" onclick="navigateTo(1, '#<%=type%>'); return false;">Table View</a>
						</td>
						<!-- <td>
							<a href="#" onclick="subNavigateTo(2, '#Biocarta'); return false;">Grid View</a>
						</td> -->
					</tr>
				</table>
			</div>
			<div class="hidden content">
				<img src="images/loader.gif" class="loader"/>
				<div class="selected bar-graph">
					<div class="downloadbox" title="Export graph">
						<a href="#" onclick="svgExport('#<%=type%> div.bar-graph', '<%=type%>_bar_graph', 'svg'); return false;">SVG</a>
						<a href="#" onclick="svgExport('#<%=type%> div.bar-graph', '<%=type%>_bar_graph', 'png'); return false;">PNG</a>
					</div>
					<div class="svg-container"></div>
				</div>
				<div class="hidden">
					<table id="test" class="results_table"></table>
					<div class="clear"></div>
				</div>
			</div>
		</div>
	<% } %>	
</body>
</html>