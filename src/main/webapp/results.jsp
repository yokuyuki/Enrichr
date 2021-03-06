<!DOCTYPE html>
<html lang="en">
<%@ page import="edu.mssm.pharm.maayanlab.Enrichr.User" %>
<%@ page import="edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader" %>
<%@ page import="edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentCategory" %>
<%@ page import="edu.mssm.pharm.maayanlab.Enrichr.ResourceLoader.EnrichmentLibrary" %>
<%! private EnrichmentCategory[] categories = ResourceLoader.getInstance().getCategories(); %>
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
	<meta name="author" content="Edward Y. Chen"/>
	<meta name="viewport" content="width=750">
	<link rel="shortcut icon" type="image/x-icon" href="favicon.ico"/>
	<link rel="icon" type="image/ico" href="favicon.ico"/>
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/reset/reset-min.css"/>	
	<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'/>		
	<link rel="stylesheet" type="text/css" href="black-widow.css"/>
	<link rel="stylesheet" type="text/css" href="black-widow-results.css"/>
	<link rel="stylesheet" type="text/css" href="jquery.dataTables.css"/>
	<link rel="stylesheet" type="text/css" href="atooltip.css"/>
	<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.1.6/d3.min.js" defer="defer"></script>
	<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js" defer="defer"></script>
	<script type="text/javascript" src="jquery.mobile.touch.min.js" defer="defer"></script>
	<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js" defer="defer"></script>
	<script type="text/javascript" src="jquery.atooltip.pack.js" defer="defer"></script>
	<script type="text/javascript" src="results.js" defer="defer"></script>
	<script type="text/javascript" src="d3.barGraph.js" defer="defer"></script>
	<script type="text/javascript" src="d3.grid.js" defer="defer"></script>
	<script type="text/javascript" src="d3.gridNetwork.js" defer="defer"></script>
	<script type="text/javascript" src="zCalc.js" defer="defer"></script>
	<title>Enrichr</title>
</head>
<body>
	<div id="header">
		<div id="logo">
			<a href="index.html"><img src="images/enrichr-icon.png"/><span>En</span><span class="red">rich</span><span>r</span></a>
		</div>		
		<% User user = (User) session.getAttribute("user"); %>
		<% if (user != null) { %>
			<div id="login-status" class="account">
				Hi, <a href="account.html" id="account-name"><%=(user.getFirst() != null) ? user.getFirst() : user.getEmail() %></a>! | <a href="logout">Logout</a>
			</div>
		<% } else { %>
			<div id="login-prompt" class="account">
				<a href="login.html">Login</a> | <a href="login.html#register">Register</a>
			</div>
		<% } %>
	</div>
	<div class="clear"></div>
	<div class="nav" id="navbar">		
		<table>
			<tr>
				<% for (int i = 0; i < categories.length; i++) { %>
				<td <%=(i==0) ? "class=\"shown\"" : ""%>>
					<a href="#" onclick="showCategory(<%=i%>); return false;"><%=categories[i].getName() %></a>
				</td>
				<% } %>
			</tr>
		</table>
	</div>
	<div id="blanket" onclick="sharePopup(); return false;"></div>
	<div class="popup beveled" id="share-link">
		<div id="advice">Use Ctrl+C or Option+C to copy the link below. Your link will last 30 days.</div>
		<input type="text" />
	</div>
	<table id="description">
		<tr>
			<td id="red">Description</td>
			<td id="black"><%
				if (session.getAttribute("description") != null)
					out.print(session.getAttribute("description"));
				else
					out.print("No description available");
				if (session.getAttribute("length") != null)
					out.print(" (" +  session.getAttribute("length") + " genes)");
			%></td>
			<% if (user != null) { %>
				<td id="save">
					<!-- WPZOOM Developer Icon Set by WPZOOM is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License. -->
					<a href='#' onclick="saveResult(); return false;" title="Save this result">
						<img src="images/save.png"/>
					</a>
				</td>
			<% } %>
			<td id="share">
				<!-- Share designed by Share Icon Project from The Noun Project -->
				<a href="#" onclick="shareResult(); return false;" title="Share this result">
					<img src="images/share.png"/>
				</a>
			</td>
		</tr>		
	</table>
	<a href="index.html">
		<div class="hidden" id="session-warning">
			<img src="images/warning-icon.png" height="24px" width="24px"/>
			<span>Uh oh! Your session has expired. Please re-submit your gene list to continue.</span>
		</div>
	</a>
	<% for (int i = 0; i < categories.length; i++) { %>
		<div class="<%=(i==0) ? "shown" : "hidden"%> category">
			<% for (EnrichmentLibrary library : categories[i].getLibraries()) { %>
				<% String type = library.getName(); %>
				<% String name = type.replaceAll("_", " "); %>
				<div class="beveled" id="<%=type%>">
					<div class="header">
						<a href="#" onclick="openResult('<%=type%>'); return false;" class="title"><%=name%></a>
						<table class="nav">
							<tr>
								<td class="selected">
									<a href="#" onclick="navigateTo(0, '#<%=type%>'); return false;">Bar Graph</a>
								</td>
								<td>
									<a href="#" onclick="navigateTo(1, '#<%=type%>'); return false;">Table</a>
								</td>
								<% if (library.hasGrid()) { %>
									<td>
										<a href="#" onclick="navigateTo(2, '#<%=type%>'); return false;">Grid</a>
									</td>
									<td>
										<a href="#" onclick="navigateTo(3, '#<%=type%>'); return false;">Network</a>
									</td>
								<% } %>
								<td class="settings">
									<a href="#" onclick="toggleSettings('#<%=type%>'); return false;">
										<!-- Per CC Attribution Share Alike, the cog icon is attributed to P.J. Onori from the Iconic icon pack. -->
										<img src="images/cog.png" width="16" height="16"/>
									</a>
								</td>
							</tr>
						</table>
					</div>
					<div class="hidden content">
						<img src="images/loader.gif" class="loader"/>
						<div class="selected bar-graph">
							<div class="hidden method">Click the bars to sort. Now sorted by <span>combined score</span>.</div>
							<div class="hidden downloadbox" title="Export graph">
								<a href="#" onclick="svgExport('#<%=type%> div.bar-graph', '<%=type%>_bar_graph', 'svg'); return false;">SVG</a>
								<a href="#" onclick="svgExport('#<%=type%> div.bar-graph', '<%=type%>_bar_graph', 'png'); return false;">PNG</a>
								<a href="#" onclick="svgExport('#<%=type%> div.bar-graph', '<%=type%>_bar_graph', 'jpg'); return false;">JPG</a>
							</div>
							<div class="svg-container"></div>
						</div>
						<div class="table hidden">
							<div class="method">Hover each row to see the overlapping genes.</div>
							<table class="results_table"></table>
							<div class="clear"></div>
							<div class="export">
								<strong>&nbsp;|</strong>
								<a href="#" onclick="tsvExport('<%=type%>_table', '<%=type%>')">Export entries to table</a>
							</div>
						</div>
						<% if (library.hasGrid()) { %>
							<div class="grid hidden">
								<table>
									<tr>
										<td>
											<div class="hidden downloadbox" title="Export grid">
												<a href="#" onclick="svgExport('#<%=type%> div.grid', '<%=type%>_grid', 'svg'); return false;">SVG</a>
												<a href="#" onclick="svgExport('#<%=type%> div.grid', '<%=type%>_grid', 'png'); return false;">PNG</a>
												<a href="#" onclick="svgExport('#<%=type%> div.grid', '<%=type%>_grid', 'jpg'); return false;">JPG</a>
											</div>
										</td>
									</tr>
									<tr>
										<td>
											<div class="svg-container"></div>
										</td>
										<td class="scores">
											Z-score: <span class="zscore" title="Lower is better">0</span><br/>
											P-value: <span class="pvalue" title="Not significant">0</span>
										</td>
									</tr>
								</table>
							</div>
							<div class="grid-network hidden">
								<div class="hidden downloadbox" title="Export network">
									<a href="#" onclick="svgExport('#<%=type%> div.grid-network', '<%=type%>_grid_network', 'svg'); return false;">SVG</a>
									<a href="#" onclick="svgExport('#<%=type%> div.grid-network', '<%=type%>_grid_network', 'png'); return false;">PNG</a>
									<a href="#" onclick="svgExport('#<%=type%> div.grid-network', '<%=type%>_grid_network', 'jpg'); return false;">JPG</a>
								</div>
							</div>
						<% } %>
						<div class="settings hidden" title="Change the color scheme"></div>
					</div>
				</div>
			<% } %>
		</div>
	<% } %>
</body>
</html>