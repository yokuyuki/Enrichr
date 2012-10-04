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
		.attr('xmlns', "http://www.w3.org/2000/svg")
		.attr('version', '1.1')
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

/* 
 * Creates a form to force downloads 
 * download( url, data [, method] )
 * data is an associative array
 */
function download(url, data, method) {
	if (url && data) {
		var form = document.createElement('form');
		form.setAttribute('action', url);
		form.setAttribute('method', method || 'post');

		for (var key in data) {
			var inputField = document.createElement('input');
			inputField.setAttribute('type', 'hidden');
			inputField.setAttribute('name', key);
			inputField.setAttribute('value', data[key]);
			form.appendChild(inputField);
		}

		document.body.appendChild(form);
		form.submit();
		document.body.removeChild(form);
	}
}

function svgExport(container, filename, outputType) {
	var b64 = encodeURIComponent(Base64.encode($(container + ' div.svg-container').html()));
	download('/Convertr/convert', {filename: filename, outputType: outputType, data: b64});
}

function tsvExport(filename, backgroundType) {
	download('enrich', {filename: filename, backgroundType: backgroundType}, 'get');
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
	if (_changingTabs)
		return;
	else
		_changingTabs = true;
	$(container + ' div.content div.selected').fadeOut(400, function() {
		$(container + ' .selected').removeClass('selected');
		$(container + ' div.header table.nav td').eq(index).addClass('selected');
		$(container + ' div.content > div').eq(index).addClass('selected');
		$(container + ' div.selected').fadeIn();
		_changingTabs = false;
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
				d3.grid.createGrid('json/' + id + '.json', json[id], 
					idTag + ' div.grid', 
					{
						highlightValue: function(d) { return d[1]; },
						highlightFunction: function(c) {
							var highlightSelection = d3.selectAll(c)
							var highlightColor = d3.scale.linear()
							.domain([0, d3.max(highlightSelection.data().map(function(d) { return -Math.log(d[2]); }))])
							.interpolate(d3.interpolateNumber)
							.range([0.25,1]);

							highlightSelection.attr('title', function(d) { return d[1] + '<br/>' + d[2]; })
							.attr('fill-opacity', function(d) { return highlightColor(-Math.log(d[2])); })
							.selectAll('title').remove();
							$(c).aToolTip({ fixed: true, xOffset: 4, yOffset: 1} );
						},
						clusterFunction: function(z) {
							var container = idTag + ' div.grid td.scores';
							$(container + ' span.zscore').text(z.toPrecision(4));
							var pvalue = (1-poz(-z)).toPrecision(4);
							$(container + ' span.pvalue').text(pvalue);
							if (pvalue < 0.1) {
								$(container + ' span').css('color', '#D90000');
								$(container + ' span.pvalue').attr('title', 'Significant');
							}							
						},
						cache: false
					}
				);
				d3.gridNetwork.createGridNetwork('json/' + id + '.json', json[id], 
					idTag + ' div.grid-network',
					{
						cache: false
					}
				);
				$(idTag + ' div.downloadbox').fadeIn('slow');
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
		centerPopup(selector);
		loadPopup(selector);
		$(selector + ' input').select();
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

/* Functions to open and close modules */
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

/* Looks for the value of a query string in the URL */
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

/* Results page setup */
$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	_changingCategory = false;	// Prevent changing category too fast
	_changingTabs = false;	// Prevent changing tabs too fast

	// Touch gestures
	$.event.special.swipe.durationThreshold = 200;
	$('body').swipeleft(function() {
			if ($('div.active').length == 0) {
				var dest = ($('#navbar td.shown').index() - 1) % 6;
				showCategory(dest);
			}
			else {
				var container = '#' + $('div.active').attr('id');
				var dest = ($(container + ' table.nav td.selected').index() - 1) % $(container + ' table.nav td').length;
				navigateTo(dest, container);
			}
		}
	);
	$('body').swiperight(function() {
			if ($('div.active').length == 0) {
				var dest = ($('#navbar td.shown').index() + 1) % 6;
				showCategory(dest);
			}
			else {
				var container = '#' + $('div.active').attr('id');
				var dest = ($(container + ' table.nav td.selected').index() + 1) % $(container + ' table.nav td').length;
				navigateTo(dest, container);
			}
		}
	);
	$('body').swipeup(function() {
			toggleClose();
		}
	);
});