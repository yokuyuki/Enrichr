<!DOCTYPE html>
<html lang="en">
<%! private String[][] enrichmentTypes = {	{	"Human_ChEA", 
												"Mouse_ChEA", 
												"Histone_Modifications_ChIP-seq",
												"microRNA",
												"ENCODE_TF_ChIP-seq"	},
											{	"KEGG",
												"WikiPathways",
												"Reactome",
												"Biocarta",
												"Hub_Proteins",
												"KEA",	},
											{	"GO_Biological_Process",
												"GO_Cellular_Component",
												"GO_Molecular_Function",
												"MGI_Mammalian_Phenotype"	},
											{	"HMDB_Metabolites",
												"Pfam_InterPro_Domains"	},
											{	"Chromosome_Location",
												"Up-regulated_CMAP",
												"Down-regulated_CMAP",
												"GeneSigDB",
												"OMIM_Disease",
												"VirusMINT"	}}; %>
<%! private String[] categories = {	"Transcription", 
									"Pathways", 
									"Ontologies",
									"Structure/Metabolites",
									"Disease/Drugs"}; %>
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
	<meta name="author" content="Edward Y. Chen"/>
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/reset/reset-min.css"/>
	<link rel="stylesheet" type="text/css" href="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/css/jquery.dataTables.css"/>
	<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'/>
	<link rel="stylesheet" type="text/css" href="black-widow.css"/>
	<link rel="stylesheet" type="text/css" href="black-widow-results.css"/>
	<script type="text/javascript" src="http://d3js.org/d3.v2.min.js"></script>
	<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
	<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.1/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="results.js"></script>
	<title>Enrichr</title>
</head>
<body>
	<div id="logo">En<span>rich</span>r</div>
	<div class="nav" id="navbar">
		<table>
			<tr>
				<% for (int i = 0; i < categories.length; i++) { %>
				<td <%=(i==0) ? "class=\"shown\"" : ""%>>
					<a href="#" onclick="showCategory(<%=i%>); return false;"><%=categories[i]%></a>
				</td>				
				<% } %>
			</tr>
		</table>
	</div>
	<% for (int i = 0; i < categories.length; i++) { %>
		<div class="<%=(i==0) ? "shown" : "hidden"%> category">
			<% for (String type : enrichmentTypes[i]) { %>
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
							<table class="results_table"></table>
							<div class="clear"></div>
							<div class="export-box"><a href="#" onclick="csvExport('<%=type%>_table', '<%=type%>')">Export to CSV</a></div>
						</div>
					</div>
				</div>
			<% } %>
		</div>
	<% } %>
</body>
</html>