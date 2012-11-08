function geneCount() {
	if ($('textarea#text-area').val())
    	$('span#gene-count').text($('textarea#text-area').val().trim().split(/\r?\n/g).length);
    else
    	$('span#gene-count').text(0);
}

function navigateTo(index, speed) {
	if (_changingCategory)
		return;
	else
		_changingCategory = true;
	if (index == 1) { createStats(); }
	speed = (typeof speed === 'undefined') ? 'slow' : speed;
	$('#content div.selected').fadeToggle(speed, function() {
		$('.selected').removeClass('selected');
		$('#navbar td').eq(index).addClass('selected');
		$('#content > div').eq(index).addClass('selected');
		$('#content div.selected').fadeToggle(speed);
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
		$.getJSON('dataset_statistics.json', function(json) {
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

function queryGene(gene) {
	$('#gene-info').load('genemap?gene=ESRRB');
}

$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	_changingCategory = false;	// Prevent changing category too fast
	tabList = ['', 'stats', 'gene', 'about', 'help'];

	var name = window.location.hash.substring(1);
	var index = tabList.indexOf(name);
	if (name != '' && index >= 0) {
		navigateTo(index, 0);
	}
	window.onhashchange = function() {
		var name = window.location.hash.substring(1);
		navigateTo(tabList.indexOf(name));
	};

	// Disable user scaling in UIWebView
	if (/(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/i.test(navigator.userAgent))
		$('meta[name=viewport]').attr('content', $('meta[name=viewport]').attr('content') + ', user-scalable=no');

	// Touch gestures
	$.event.special.swipe.durationThreshold = 200;
	$('body').swipeleft(function() {
			var dest = ($('div.selected').index() - 1 + 4) % 4;
			getTab(tabList[dest]);
		}
	);
	$('body').swiperight(function() {
			var dest = ($('div.selected').index() + 1) % 4;
			getTab(tabList[dest]);
		}		
	);

	// Load counter
	$.get('count', function(data) {
		$('div#count span').text(data);
		$('div#count').fadeIn('slow');
	})
	.error(function() { $('div#count').remove(); });
});