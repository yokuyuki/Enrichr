function geneCount() {
	if ($('textarea#text-area').val())
    	$('span#gene-count').text($('textarea#text-area').val().trim().split(/\r?\n/g).length);
    else
    	$('span#gene-count').text(0);
}

function navigateTo(index, transitionSpeed) {
	if (_changingCategory || $('div.selected').index() == index)
		return;
	else
		_changingCategory = true;
	if (index == 1) { createStats(); }
	if (index == 2) { createAutocomplete(); }
	transitionSpeed = (typeof transitionSpeed === 'undefined') ? 'slow' : transitionSpeed;
	$('#content div.selected').fadeToggle(transitionSpeed, function() {
		$('.selected').removeClass('selected');
		$('#navbar td').eq(index).addClass('selected');
		$('#content > div').eq(index).addClass('selected');
		$('#content div.selected').fadeToggle(transitionSpeed);
		_changingCategory = false;
	});
}

function getTab(name) {
	window.location.hash = name;
}

function validateInput() {
	if ($('input#file-chooser').val())
		return true;
	else if ($('textarea#text-area').val())
		return true;
	else {
		alert("Nothing to analyze. Please select a file to upload or paste in a list of genes.");
		return false;
	}
}

function insertExample() {
	$.get('example_list.txt', function(data) {
		$('textarea#text-area').val(data);
		geneCount();
		$('#description input').val("Sample gene list");
	});	
}

function createStats() {
	if (!$('#stats').hasClass('done')) {
		$.getJSON('json/dataset_statistics.json', function(json) {
			$('#stats').addClass('done');
			$('#stats').dataTable({
				"aaData": json,
				"aoColumns": [
					{ 
						"sTitle": "Gene-set Library",
						"fnRender": function(oObj, sVal) {
							return '<a href="' + oObj.aData[4] + '" target="_blank">' + sVal + '</a>';
						}
					},
					{ 
						"sTitle": "Terms",
						"sClass": "right",
						"asSorting": ["desc", "asc"]
					},
					{ 
						"sTitle": "Gene Coverage",
						"sClass": "right",
						"asSorting": ["desc", "asc"]
					},
					{ 
						"sTitle": "Mean Genes per Term",
						"sClass": "right",
						"asSorting": ["desc", "asc"],
						"fnRender": function(obj) {
							return obj.aData[obj.iDataColumn].toFixed(4);
						}
					},
					{
						"bVisible": false
					}
				],
				"aaSorting": [[1, "desc"]],
				"bPaginate": false,
				"sDom": '<t>'
			});
		});
	}
}

function createAutocomplete() {
	if (!$('#query-gene').hasClass('done')) {
		$.getJSON('json/genemap.json', function(json) {
			$('#query-gene').addClass('done');
			$('#query-gene').autocomplete({
				source: json,
				minLength: 3,
				delay: 500,
				autoFocus: true
			});
		});
	}
}

function queryGene(gene) {
	$('#gene-info').load('genemap?gene=' +  gene);
	$('#gene-info').fadeIn();
}

function hashcheck(onload) {
	var transitionSpeed = (typeof onload !== 'boolean') ? 'slow' : 0;

	var hash = window.location.hash.substring(1);
	if (hash[0] == '!') {
		// Not on find tab
		if (tabList[$('div.selected').index()] != 'find') {
			navigateTo(tabList.indexOf('find'), transitionSpeed);
			if (!onload)
				return;
		}

		// Query gene
		hash = hash.substring(1);
		var parms = hash.split('=');
		if (parms[0] == 'gene')
			queryGene(parms[1]);
	}
	else {
		navigateTo(tabList.indexOf(hash), transitionSpeed);
	}
}

$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	_changingCategory = false;	// Prevent changing category too fast
	tabList = ['', 'stats', 'find', 'about', 'help'];

	// Onload hash check
	hashcheck(true);

	// When swipe changes the hash
	window.onhashchange = hashcheck;

	// Disable user scaling in UIWebView
	if (/(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/i.test(navigator.userAgent))
		$('meta[name=viewport]').attr('content', $('meta[name=viewport]').attr('content') + ', user-scalable=no');

	// Touch gestures
	$.event.special.swipe.durationThreshold = 200;
	$('body').swipeleft(function() {
			var dest = ($('div.selected').index() - 1 + tabList.length) % tabList.length;
			getTab(tabList[dest]);
		}
	);
	$('body').swiperight(function() {
			var dest = ($('div.selected').index() + 1) % tabList.length;
			getTab(tabList[dest]);
		}
	);

	// Load counter
	$.get('count', function(data) {
		$('div#count span').text(data);
		$('div#count').fadeIn('slow');
	})
	.error(function() { $('div#count').remove(); });

	// Bind query-gene button to javascript
	$('#find-gene').submit(function() {
		var gene = $('#query-gene').val()
		window.location.hash = '!gene=' + gene;
		return false;
	});
});