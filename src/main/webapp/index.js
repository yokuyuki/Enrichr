function geneCount() {
	if ($('textarea#text-area').val())
    	$('span#gene-count').text($('textarea#text-area').val().trim().split(/\r?\n/g).length);
    else
    	$('span#gene-count').text(0);
}

function navigateTo(index) {
	if (_changingCategory)
		return;
	else
		_changingCategory = true;
	$('#content div.selected').fadeToggle('slow', function() {
		$('.selected').removeClass('selected');
		$('#navbar td').eq(index).addClass('selected');
		$('#content > div').eq(index).addClass('selected');
		$('#content div.selected').fadeToggle('slow');
		_changingCategory = false;
	});
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

$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	_changingCategory = false;	// Prevent changing category too fast
	// Load counter
	$.get('count', function(data) {
		$('div#count span').text(data);
		$('div#count').fadeIn('slow');
	})
	.error(function() { $('div#count').remove() });

	// Disable focus zoom for iOS
	var enabledZoom = $('meta[name=viewport]').attr('content');	
	var disabledZoom = enabledZoom + ",maximum-scale=1, user-scalable=no";
	var enable = function() { $('meta[name=viewport').attr('content', enabledZoom); };
	var disable = function() { $('meta[name=viewport').attr('content', disabledZoom); }
	$('input[type=text],textarea').focus(disable);
	$('input[type=text],textarea').blur(enable);

	// Touch gestures
	$.event.special.swipe.durationThreshold = 200;
	$('body').swipeleft(function() {
			var dest = ($('div.selected').index() - 1) % 4;
			if (dest == 1)
				createStats();
			navigateTo(dest);
		}
	);
	$('body').swiperight(function() {
			var dest = ($('div.selected').index() + 1) % 4;
			if (dest == 1)
				createStats();
			navigateTo(dest);
		}		
	);
});