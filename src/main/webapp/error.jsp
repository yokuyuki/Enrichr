<!DOCTYPE html>
<html lang="en">
<head>
	<title>Enrichr</title>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<meta name="author" content="Edward Y. Chen"/>
	<meta name="viewport" content="width=750">
	<link rel="shortcut icon" type="image/x-icon" href="favicon.ico"/>
	<link rel="icon" type="image/ico" href="favicon.ico"/>
	<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/reset/reset-min.css"/>
	<link href='http://fonts.googleapis.com/css?family=Droid+Sans' rel='stylesheet' type='text/css'/>
	<link rel="stylesheet" type="text/css" href="black-widow.css"/>
	<link rel="stylesheet" type="text/css" href="black-widow-login.css"/>
</head>
<body>
	<div id="logo">
		<a href="index.html"><img src="images/enrichr-icon.png"/><span>En</span><span class="red">rich</span><span>r</span></a>
	</div>
	<div class="clear"></div>
	<div id="content" class="beveled">
		<div class="title">Error</div>
		<div>
			<% String error = (String) request.getAttribute("error"); %>
			<% if (error != null) { %>
				<%= error %>
			<% } else { %>
				<% response.sendRedirect("index.html"); %>
			<% } %>
		</div>
	</div>
</body>
</html>