/**
*
*  Base64 encode / decode
*  http://www.webtoolkit.info/
*
**/
 
var Base64 = {
 
	// private property
	_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
 
	// public method for encoding
	encode : function (input) {
		var output = "";
		var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
		var i = 0;
 
		input = Base64._utf8_encode(input);
 
		while (i < input.length) {
 
			chr1 = input.charCodeAt(i++);
			chr2 = input.charCodeAt(i++);
			chr3 = input.charCodeAt(i++);
 
			enc1 = chr1 >> 2;
			enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
			enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
			enc4 = chr3 & 63;
 
			if (isNaN(chr2)) {
				enc3 = enc4 = 64;
			} else if (isNaN(chr3)) {
				enc4 = 64;
			}
 
			output = output +
			this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
			this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);
 
		}
 
		return output;
	},
 
	// public method for decoding
	decode : function (input) {
		var output = "";
		var chr1, chr2, chr3;
		var enc1, enc2, enc3, enc4;
		var i = 0;
 
		input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
 
		while (i < input.length) {
 
			enc1 = this._keyStr.indexOf(input.charAt(i++));
			enc2 = this._keyStr.indexOf(input.charAt(i++));
			enc3 = this._keyStr.indexOf(input.charAt(i++));
			enc4 = this._keyStr.indexOf(input.charAt(i++));
 
			chr1 = (enc1 << 2) | (enc2 >> 4);
			chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
			chr3 = ((enc3 & 3) << 6) | enc4;
 
			output = output + String.fromCharCode(chr1);
 
			if (enc3 != 64) {
				output = output + String.fromCharCode(chr2);
			}
			if (enc4 != 64) {
				output = output + String.fromCharCode(chr3);
			}
 
		}
 
		output = Base64._utf8_decode(output);
 
		return output;
 
	},
 
	// private method for UTF-8 encoding
	_utf8_encode : function (string) {
		string = string.replace(/\r\n/g,"\n");
		var utftext = "";
 
		for (var n = 0; n < string.length; n++) {
 
			var c = string.charCodeAt(n);
 
			if (c < 128) {
				utftext += String.fromCharCode(c);
			}
			else if((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			}
			else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}
 
		}
 
		return utftext;
	},
 
	// private method for UTF-8 decoding
	_utf8_decode : function (utftext) {
		var string = "";
		var i = 0;
		var c = c1 = c2 = 0;
 
		while ( i < utftext.length ) {
 
			c = utftext.charCodeAt(i);
 
			if (c < 128) {
				string += String.fromCharCode(c);
				i++;
			}
			else if((c > 191) && (c < 224)) {
				c2 = utftext.charCodeAt(i+1);
				string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
				i += 2;
			}
			else {
				c2 = utftext.charCodeAt(i+1);
				c3 = utftext.charCodeAt(i+2);
				string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
				i += 3;
			}
 
		}
 
		return string;
	}
 
}

/*
 * --------------------------------------------------------------------
 * jQuery-Plugin - $.download - allows for simple get/post requests for files
 * by Scott Jehl, scott@filamentgroup.com
 * http://www.filamentgroup.com
 * reference article: http://www.filamentgroup.com/lab/jquery_plugin_for_requesting_ajax_like_file_downloads/
 * Copyright (c) 2008 Filament Group, Inc
 * Dual licensed under the MIT (filamentgroup.com/examples/mit-license.txt) and GPL (filamentgroup.com/examples/gpl-license.txt) licenses.
 * --------------------------------------------------------------------
 */
 
jQuery.download = function(url, data, method){
	//url and data options required
	if( url && data ){ 
		//data can be string of parameters or array/object
		data = typeof data == 'string' ? data : jQuery.param(data);
		//split params into form inputs
		var inputs = '';
		jQuery.each(data.split('&'), function(){ 
			var pair = this.split('=');
			inputs+='<input type="hidden" name="'+ pair[0] +'" value="'+ pair[1] +'" />'; 
		});
		//send request
		jQuery('<form action="'+ url +'" method="'+ (method||'post') +'">'+inputs+'</form>')
		.appendTo('body').submit().remove();
	};
};

// Extension to jquery to add a data-type that allows sorting of scientific notation
jQuery.extend( jQuery.fn.dataTableExt.oSort, {
	"scientific-pre": function ( a ) {
		return parseFloat(a);
	},

	"scientific-asc": function ( a, b ) {
		return ((a < b) ? -1 : ((a > b) ? 1 : 0));
	},

	"scientific-desc": function ( a, b ) {
		return ((a < b) ? 1 : ((a > b) ? -1 : 0));
	}
});

// Generate bar graphs
function createBarGraph(dataArray, container) {
	var mode = 1, BARS = 10, sortId = container + ' span.method';

	// Initial data
	var filteredData = dataArray.filter(function(d, i) { return i < BARS; });

	// Size
	var width = 710,
	height = 299;

	// Interpolators
	var x = d3.scale.linear()
		.domain([0, d3.max(filteredData.map(function(d) { return displayValue(d); }))])
		.range([0, width]),
	y = d3.scale.ordinal()
		.domain(d3.range(BARS))
		.rangeBands([0, height], .2),
	color = d3.scale.linear()
		.domain([0, d3.max(filteredData.map(function(d) { return displayValue(d); }))])
		.interpolate(d3.interpolateRgb)
		.range(["#446688", "#88ddff"]);

	// Create graph
	var chart = d3.select(container + ' div.svg-container')
		.append('svg:svg')
		.attr('width', width)
		.attr('height', height);

	// Group that contains bar, shadow, and label
	var barGroup = chart.selectAll('g.bar')
		.data(filteredData)
		.enter().append('svg:g')
		.attr('class', 'bar')
		.attr('transform', function(d, i) { return 'translate(0,' + y(i) + ')'; });

	drawBar(barGroup);

	// chart.on('click', function() {
	// 	// Change mode
	// 	mode = ++mode % 3;
	// 	switch (mode) {
	// 		case 0: $(sortId).text('combined score'); break;
	// 		case 1: $(sortId).text('p-value'); break;
	// 		case 2: $(sortId).text('rank'); break;
	// 	}

	// 	// Adjust data
	// 	dataArray.sort(function(a, b) { return displayValue(b, mode) - displayValue(a, mode); });
	// 	filteredData = dataArray.filter(function(d, i) { return i < BARS; });

	// 	// Remap interpolators
	// 	x.domain([0, d3.max(filteredData.map(function(d) { return displayValue(d); }))]);
	// 	color.domain([0, d3.max(filteredData.map(function(d) { return displayValue(d); }))]);

	// 	// Join with existing data and remove old data
	// 	var newBars = chart.selectAll('g.bar').data(filteredData, function(d) { return d; });
	// 	var newBarGroup = newBars.enter().append('svg:g')
	// 						.attr('class', 'bar')
	// 						.attr('transform', function(d, i) { return 'translate(0,' + height + ')'; });			
	// 	drawBar(newBarGroup);
	// 	newBars.exit().remove();

	// 	// Sort bars
	// 	chart.selectAll('g.bar')
	// 		.sort(function(a, b) { return displayValue(b, mode) - displayValue(a, mode); })
	// 		.transition()
	// 		.duration(1000)
	// 		.delay(500)
	// 		.attr('transform', function(d, i) { return 'translate(0,' + y(i) + ')'; });

	// 	// Adjust bar length
	// 	chart.selectAll('rect.bar')					
	// 		.transition()
	// 		.duration(500)
	// 		.attr("index", function(d, i) {return i})
	// 		.attr('width', function(d) { return x(displayValue(d)); })
	// 		.attr('fill', function(d) { return color(displayValue(d)); });

	// 	// Adjust bar shadow
	// 	chart.selectAll('rect.shadow')					
	// 		.transition()
	// 		.duration(500)
	// 		.delay(1500)
	// 		.attr('opacity', 0)
	// 		.transition()
	// 		.duration(0)
	// 		.delay(2000)
	// 		.attr('width', function(d) { return x(displayValue(d)); })
	// 		.attr('fill', function(d) { return color(displayValue(d)); })
	// 		.transition()
	// 		.duration(0)
	// 		.delay(2500)
	// 		.attr('opacity', 0.3);

	// 	// Adjust label
	// 	chart.selectAll('text.label')
	// 		.transition()
	// 		.text(function(d) { return d[1]; });
	// });

	// Allow displaying of the different sorting method by correcting the value
	function displayValue(value) {
		switch(mode) {
			case 0:
				return value[4]; break;
			case 1:
				return -Math.log(value[2]); break;
			case 2:
				return -value[3]; break;
		}
	}

	// Draws the bar group
	function drawBar(selection) {
		// Bar shadow
		selection.append('svg:rect')
			.attr('class', 'shadow')
			.attr('fill', function(d) { return color(displayValue(d)); })
			.attr('opacity', 0.3)
			.attr('width', function(d) { return x(displayValue(d)); })
			.attr('height', y.rangeBand());

		// Bar
		selection.append('svg:rect')
			.attr('class', 'bar')
			.attr('fill', function(d) { return color(displayValue(d)); })
			.attr('width', function(d) { return x(displayValue(d)); })
			.attr('height', y.rangeBand());

		// Labels
		selection.append('svg:text')
			.attr('class', 'label')
			.text(function (d, i) { return d[1]; })
			.attr('width', function(d) { return displayValue(d); })
			.attr('x', 3)
			.attr('y', y.rangeBand() / 2)
			.attr("dy", "0.35em")
			.attr('fill', 'black');
	}
}

// Create tables
function createTable(dataArray, container) {
	$(container).dataTable({
		"aaData": dataArray,
		"fnDrawCallback": function ( oSettings ) {
			var that = this;

			if ( oSettings.bSorted && !$('div.active div.dataTables_filter input').val())				
			{
				this.$('td:first-child', {"filter":"applied"}).each( function (i) {
					that.fnUpdate( i+1, this.parentNode, 0, false, false );
				} );
			}
		},
		"aoColumns": [
			{ "sTitle" : "Index", "sClass": "center", "sWidth": "5%"},
			{ "sTitle": "Name", "sClass": "left" },
			{ 
				"sTitle": "P-value",
				"sClass": "right",
				"sWidth": "15%",
				"sType": "scientific",
				"asSorting": ["asc"],
				"fnRender": function(obj) {
					return obj.aData[obj.iDataColumn].toPrecision(4);
				}
			// },
			// { 
			// 	"sTitle": "Z-score", 
			// 	"sClass": "right",
			// 	"sWidth": "15%",
			// 	"asSorting": ["asc"],
			// 	"fnRender": function(obj) {
			// 		return obj.aData[obj.iDataColumn].toFixed(2);
			// 	}
			// },
			// { 
			// 	"sTitle": "Combined Score", 
			// 	"sClass": "right",
			// 	"sWidth": "25%",
			// 	"asSorting": ["desc"],
			// 	"fnRender": function(obj) {
			// 		return obj.aData[obj.iDataColumn].toFixed(2);
			// 	}
			}
		],
		"aoColumnDefs": [
			{ "bSortable": false, "aTargets": [ 0 ] }
		],
		"aaSorting": [[2, "asc"]]
	});
}

// Create grid for ChEA and KEA
function createGrid(section) {
	if(typeof cDict != 'undefined') {
		if (!draw_tf && section === 'tf') {
			visualizeIt(0);
			fill(0, highlights[0]);
			visualizeIt(1);
			fill(1, highlights[1]);
			draw_tf = true;
		}
		else if (!draw_kinase && section === 'kinase') {
			visualizeIt(2);
			fill(2, highlights[2]);
			visualizeIt(3);
			fill(3, highlights[2]);
			draw_kinase = true;
		}
	}
	else {
		cDict = new Array();
		container = new Array();
		$.getJSON("grid.json", function(json) {
			cDict[0] = json.ChEA_Direct;
			cDict[1] = json.ChEA_Substrate;
			cDict[2] = json.KK_Direct;
			cDict[3] = json.KK_Substrate;
			createGrid(section);
		});		
	}

	function visualizeIt(svgIndex) {
		// Track Canvas-Related Global Variables
		weights = cDict[svgIndex]['weights'];
		textArray = cDict[svgIndex]['texts'];
		
		G_VAR = {	
			checkHex: "",
			// Text Attributes
			
			textVisible: 0,
			textColor: "#FFFFFF",
			textSize: 10,

			// SVG Canvas Attributes
			//nodes Display
			
			nodes: new Array(),
			width: Math.sqrt(weights.length),
			canvasSize: 0,
			DEFAULTPIXELS: 15,
			invertColor: 0,
			scale: 1,
			canvasHex : "#00FFFF",
			canvasRGB : [0,255,255],
		}
		G_VAR.checkHex = /^(#)?([0-9a-fA-F]{3})([0-9a-fA-F]{3})?$/;
		G_VAR.canvas_size = 225;
		G_VAR.pixels = 225 / G_VAR.width;
		
		totalWeight = 0;
		for (index in weights){
			totalWeight += weights[index];
		}
		
		avgWeight = totalWeight/weights.length;

		if (avgWeight != 8.0){
			G_VAR.scale = Math.log(0.2)/Math.log(avgWeight/8);
		}

		for (index in weights){
			var node = new NodeObj(index, weights[index], textArray[index]);
			node.colorizer();
			G_VAR.nodes.push(node)
		}
		
		createCanvas(svgIndex);	
		weight_visualize();
		container[svgIndex] = G_VAR;
	}

	function circleMake(circles) {
		pixels = G_VAR.pixels;
		circles.attr("cx", function(d,i) {return (d.columnPixels) + pixels/2;})
				.attr("cy", function(d,i) {return (d.rowPixels) + pixels/2;})
				.attr("fill", function(d) { return d.indicator;})
				.attr("fill-opacity", function(d) { return (d.indicatorOpacity);})
				.transition()
					.duration(2000)
					.ease(Math.sqrt)
					.attr("r", Math.floor(pixels/3));
		
		circles.append("title").text(function(d) { return d.text; })	
	}
		
	function rectMake(rects) {
		rects.attr("x", function(d,i) {return (d.columnPixels);})
			.attr("y", function(d,i) {return (d.rowPixels);})
			.attr("width", G_VAR.pixels)
			.attr("height", G_VAR.pixels)
			.attr("fill", function(d) { return d.color;});
		rects.append("title")
			.text(function(d) { return d.text; })
	}		

	function weight_visualize() {
		/* Create and recreate canvas */
		canvas.selectAll("rect").data([]).exit().remove();
		canvas.selectAll("circle").data([]).exit().remove();
		rectMake(rect.data(G_VAR.nodes).enter().append("svg:rect"));
		circleMake(indicate.data(G_VAR.nodes).enter().append("svg:circle"));
	}

	function fill(svgIndex, elementList) {
		var hexCode = "#FF0000";

		G_VAR = container[svgIndex];
		for (i in G_VAR.nodes){			
			if (elementList.indexOf(G_VAR.nodes[i].searchText) > -1){
				G_VAR.nodes[i].indicator = hexCode;
				G_VAR.nodes[i].indicatorOpacity = 1;
			}
		}
		canvas = d3.select("svg#canvas"+svgIndex);
		canvas.selectAll("circle").data([]).exit().remove();
		indicate = canvas.selectAll("circle");
		circleMake(indicate.data(G_VAR.nodes).enter().append("svg:circle"));
	}

	function createCanvas(svgIndex) {
		span = "#SVG" +svgIndex;
		canvas = d3.select(span)
					.append("svg:svg")
					.attr("id","canvas"+svgIndex)
					.attr("width", G_VAR.canvas_size)
					.attr("height", G_VAR.canvas_size);

				

		// Fill Canvas with Weights and Names
		rect = canvas.selectAll("rect");
		indicate = canvas.selectAll("circle");
	}

	function NodeObj(index, weigh, text) {
		this.index = index;
		this.weight = weigh;
		this.text = text;
		this.searchText = text.toUpperCase();
		
		this.column = index%G_VAR.width;
		this.row = Math.floor(index/G_VAR.width);
		
		this.columnPixels = index%G_VAR.width * G_VAR.pixels;
		this.rowPixels = Math.floor(index/G_VAR.width) * G_VAR.pixels;


		this.color = 0;
		this.indicator = 0;
		this.indicatorOpacity = 0;
		
		this.colorizer = function () {
			var hexArray =["#"];
			var canvasRGB = G_VAR.canvasRGB;
			var scale = G_VAR.scale;
				for (i=0; i<3; i++){
					if (canvasRGB[i] === 0){
						hexArray.push("00");
					} else {
						var oriNum = (G_VAR.invertColor === 0) ? Math.floor(canvasRGB[i]*Math.pow(this.weight, scale)/Math.pow(8, scale)) : Math.floor(canvasRGB[i]-canvasRGB[i]*Math.pow(this.weight, scale)/Math.pow(8, scale));
						var hexNum = oriNum.toString(16);
						if (hexNum.length === 1){
							hexNum = '0' + hexNum;
						}
						hexArray.push(hexNum);
					}
				}
			var hexCode = hexArray.join("");
			this.color = hexCode;
		}
			
		this.canvasChange = function() {
			this.columnPixels = this.column * G_VAR.pixels;
			this.rowPixels = this.row * G_VAR.pixels;
		}
	}
}

function distance(x1, y1, x2, y2, width) {
	// Correct x and y distances for torus
	if (x1 > x2) {
		if ((x2 + width - x1) < (x1 - x2)) {
			x2 += width;
		}
	}
	else {
		if ((x1 + width - x2) < (x2 - x1)) {
			x1 += width;
		}
	}

	if (y1 > y2) {
		if ((y2 + width - y1) < (y1 - y2)) {
			y2 += width;
		}
	}
	else {
		if ((y1 + width - y2) < (y2 - y1)) {
			y1 += width;
		}
	}

	return Math.sqrt(Math.pow(Math.abs(x1-x2),2) + Math.pow(Math.abs(y1-y2), 2));
}

function svgExport(container, filename, outputType) {
	$(container + ' svg').attr({ version: '1.1' , xmlns:"http://www.w3.org/2000/svg"});	
	var b64 = encodeURIComponent(Base64.encode($(container + ' div.svg-container').html()));
	$.download('/Convertr/convert', 'filename=' + filename +'&outputType=' + outputType + '&data=' + b64);
}

function tsvExport(filename, backgroundType) {
	$.download('enrich', 'filename=' + filename + '&backgroundType=' + backgroundType, 'get');
}

// Shows the category
function showCategory(index) {
	if (_changingCategory)
		return;
	else
		_changingCategory = true;
	toggleClose();
	$('div.shown').fadeOut('slow', function() {		
		$('.shown').removeClass('shown');
		$('div.category').eq(index).addClass('shown')
		$('#navbar td').eq(index).addClass('shown');
		$('div.shown').fadeIn('slow');
		_changingCategory = false;
	});
}

// Animates the transition between different tabs
function navigateTo(index, container) {	
	$(container + ' div.content div.selected').fadeToggle('slow', function() {
		$(container + ' .selected').removeClass('selected');
		$(container + ' div.header table.nav td').eq(index).addClass('selected');
		$(container + ' div.content > div').eq(index).addClass('selected');
		$(container + ' div.selected').fadeToggle('slow');
	});
}

function getResult(id) {
	var idTag = '#' + id;
	if ($('div.active').attr('id') == id) {
		toggleClose();
		return;
	}
	else {
		toggleClose();
		toggleOpen(id);
	}

	if(!$(idTag + ' div.content').hasClass('done')) {
		var dataUrl = queryString('q');
		dataUrl = (dataUrl) ? dataUrl : 'enrich';
		$.getJSON(dataUrl, { backgroundType: id }, function(json) {
			if (json.expired) {				
				$('#session-warning').slideDown('fast', function() {
					toggleClose();
				});
			}
			else {
				$(idTag + ' div.content img.loader').remove();
				$(idTag + ' div.content').addClass('done');
				createBarGraph(json[id], idTag + ' div.bar-graph');
				createTable(json[id], idTag + ' .results_table');
			}
		});
	}
}

/**
 * Share results and display a popup with the link.
 */

function shareResult() {
	if ($('#share-link input').val()) {
		sharePopup();
	}
	else {
		var dataUrl = queryString('q');
		dataUrl = (dataUrl) ? dataUrl : 'enrich';
		$.getJSON(dataUrl, { share: true }, function(json) {
			if (json.expired) {
				$('#session-warning').slideDown('fast', function() {
					toggleClose();
				});
			}
			else {
				var url = window.location.protocol + '//' + window.location.host + '/Enrichr/enrich?dataset=' + json.link_id;
				$('#share-link input').val(url);
				sharePopup();
			}
		});
	}
}

function sharePopup() {
	var selector = '#share-link'
	if ($(selector).css('display') == 'none') {
		$(selector + ' input').select();
		centerPopup(selector);
		loadPopup(selector);
	}
	else {
		disablePopup(selector);
	}
}

function loadPopup(selector) {
	$("#blanket").css({"opacity": "0.65"});
	$("#blanket").fadeIn("slow");
	$(selector).fadeIn("slow");
}

function disablePopup(selector) {
	$("#blanket").fadeOut("slow");
	$(selector).fadeOut("slow");
}

function centerPopup(selector) {
	//request data for centering
	var windowWidth = document.documentElement.clientWidth;
	var windowHeight = document.documentElement.clientHeight;
	var popupHeight = $(selector).height();
	var popupWidth = $(selector).width();
	//centering
	$(selector).css({
		"margin-top": -1*popupHeight/2,
		"margin-left": -1*popupWidth/2		
	});
}

function toggleClose() {
	$('div.active div.content').slideUp();
	$('div.active table.nav').fadeOut();
	$('div.active').removeClass('active');
}

function toggleOpen(id) {
	$('#' + id).addClass('active');
	$('div.active div.content').slideDown();
	$('div.active table.nav').fadeIn();
}

function queryString(search_for) {
	var query = window.location.search.substring(1);
	var parms = query.split('&');
	for (var i=0; i<parms.length; i++) {
		var pos = parms[i].indexOf('=');
		if (pos > 0  && search_for == parms[i].substring(0,pos)) {
			return parms[i].substring(pos+1);;
		}
	}
	return "";
}

$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	_changingCategory = false;	// Prevent changing category too fast
});