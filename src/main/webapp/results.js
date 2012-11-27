/**
 *  Base64 encode / decode
 *  http://www.webtoolkit.info/
 */ 
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
			},
			{ 
				"sTitle": "Z-score", 
				"sClass": "right",
				"sWidth": "15%",
				"asSorting": ["asc"],
				"fnRender": function(obj) {
					return obj.aData[obj.iDataColumn].toFixed(2);
				}
			},
			{ 
				"sTitle": "Combined Score", 
				"sClass": "right",
				"sWidth": "15%",
				"asSorting": ["desc"],
				"fnRender": function(obj) {
					return obj.aData[obj.iDataColumn].toFixed(2);
				}
			}
		],
		"aoColumnDefs": [
			{ "bSortable": false, "aTargets": [ 0 ] }
		],
		"aaSorting": [[4, "desc"]]
	});
}

// Creates the color wheel for changing the color of the bar graph, grid, and network
function createColorWheel(pixels, container, startingColor) {
	var sqrt3 = Math.sqrt(3);
	var rows = [['#00ffff','#33ccff','#3399ff','#6699ff'],['#66ffcc','#66ffff','#66ccff','#99ccff','#9999ff'],['#66ff99','#99ffcc','#ccffff','#ccccff','#cc99ff','#cc66ff'],['#66ff66','#99ff99','#ccffcc','#ffffff','#ffccff','#ff99ff','#ff66ff'],['#99ff66','#ccff99','#ffffcc','#ffcccc','#ff99cc','#ff66cc'],['#ccff66','#ffff99','#ffcc99','#ff9999','#ff6699'],['#ffff66','#ffcc66','#ff9966','#ff6666']];

	var svg = d3.select(container + ' div.settings').append('svg:svg')
		.attr('height', 7 * pixels * sqrt3)
		.attr('width', 7 * pixels * sqrt3);
	var mid = Math.floor(rows.length/2);

	for (var r = 0; r < rows.length; r++) {
		var shift = Math.abs(mid - r);
		for (var hex = 0; hex < rows[r].length; hex++) {
			var columnOrigin = sqrt3 * shift / 2 * pixels + hex * sqrt3 * pixels
			var rowOrigin = pixels / 2 + ((r+1) * 1.5 * pixels);
			var up = pixels / 2;
			var right = sqrt3 * pixels / 2;
			var path = ["M", columnOrigin, rowOrigin, 
						"l", right, up, 
						"l",  right, -up, 
						"l", 0, -pixels,  
						"l", -right, -up,  
						"l", -right, up, "Z"].join(" ");

			svg.append('svg:path').attr('d', path)
				.style('fill', rows[r][hex])
				.style('stroke', '#000000')
				.style('stroke-width', function(d) { return (rows[r][hex] === startingColor) ? '1px' : '0px'; } )
				.on('click', function() {
					d3.selectAll(container + ' div.settings svg path').style('stroke-width', '0px');
					this.style.strokeWidth = '1px';
					recolor(container, this.style.fill);
					toggleSettings(container);
				}
			);
		}
	}
}

function recolor(container, color) {
	d3.barGraph.recolor(container + ' div.bar-graph', color);
	d3.grid.recolor(container + ' div.grid', color);
	d3.gridNetwork.recolor(container + ' div.grid-network', color);
}

function toggleSettings(container) {
	$(container + ' div.settings').fadeToggle();
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

function batchExport(modules, colors, visualizations, fileType) {
	exportQueue = []

	for (var i = 0; i < modules.length; i++) {
		getResult(modules[i]);
		exportQueue.push([modules[i], colors[i], visualizations, fileType]);
	}
}

function startExport(speed) {
	function delayExport(container, visualization, fileType) {
		return function() {
			if (visualization == 'bar-graph')
				svgExport(container + ' div.bar-graph', container.substring(1) + '_bar_graph', fileType);
			else if (visualization == 'grid')
				svgExport(container + ' div.grid', container.substring(1) + '_grid', fileType);
			else if (visualization == 'alt-grid') {
				d3.select(container + ' div.grid svg').on('click')();
				setTimeout(function() {
					svgExport(container + ' div.grid', container.substring(1) + '_alt_grid', fileType);
				}, speed);
			}
			else if (visualization == 'grid-network')
				svgExport(container + ' div.grid', container.substring(1) + '_grid_network', fileType);
		};
	}

	var delay = 0;

	for (var i = 0; i < exportQueue.length; i++) {
		var container = '#' + exportQueue[i][0];
		var color = exportQueue[i][1]
		var visualizations = exportQueue[i][2]
		var fileType = exportQueue[i][3]

		recolor(container, color);
		
		for (var j = 0; j < visualizations.length; j++) {
			setTimeout(delayExport(container, visualizations[j], (typeof fileType === 'undefined') ? 'png' : fileType), delay);
			delay += speed;
		}
		
	}
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

function openResult(id) {
	if ($('div.active').attr('id') == id) {
		toggleClose();
	}
	else {
		toggleClose();
		toggleOpen(id);
		getResult(id);
	}
}

function getResult(id) {
	var idTag = '#' + id;	

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

				d3.barGraph.createBarGraph(json[id], idTag + ' div.bar-graph', 
					{
						minColor: '#713939',
						maxColor: '#ff6666',
						modes: [function(d) { return d[4]; },
								function(d) { return -Math.log(d[2]); },
								function(d) { return -d[3]; }],
						modeFunction: function(options) {
							var mode = options.modeDisplays.shift();
							options.modeDisplays.push(mode);
							mode = options.modeDisplays[0];

							$(idTag + ' div.bar-graph div.method span').text(mode);
						},
						modeDisplays: ['combined score', 'p-value', 'rank']
					}
				);
				$(idTag + ' div.bar-graph div.method').fadeIn('slow');

				createTable(json[id], idTag + ' .results_table');

				d3.grid.createGrid('json/' + id + '.json', json[id], 
					idTag + ' div.grid', 
					{
						highlightCount: 10,
						highlightName: function(d) { return d[1]; },
						highlightValue: function(d) { return d[4]; },
						highlightFunction: function(c) {
							var highlightSelection = d3.selectAll(c);

							highlightSelection.attr('title', function(d) { return d[1] + '<br/>' + d[4]; })
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
						nodeColor: '#ff6666',
						cache: false
					}
				);

				$(idTag + ' div.downloadbox').fadeIn('slow');
				createColorWheel(10, idTag, '#ff6666');
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
				var dest = ($('#navbar td.shown').index() - 1 + 6) % 6;
				showCategory(dest);
			}
			else {
				var container = '#' + $('div.active').attr('id');
				var tabCount = $(container + ' table.nav td:not(td.settings)').length;
				var dest = ($(container + ' table.nav td.selected').index() - 1 + tabCount) % tabCount;
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
				var dest = ($(container + ' table.nav td.selected').index() + 1) % $(container + ' table.nav td:not(td.settings)').length;
				navigateTo(dest, container);
			}
		}
	);
});