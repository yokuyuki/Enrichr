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

	// Touch gestures
	$('#content div').eq(0).swipe({
		swipeRight: function() {
			if (validateInput())
				document.forms['enrich'].submit();
		},
		fingers: 1
	});
	$('body').swipe({
		swipe: function(event, direction, distance, duration, fingerCount) {
			if (fingerCount == 2) {
				if (direction == 'left')
					var dest = ($('div.selected').index() - 1) % 3
				else if (direction == 'right')
					var dest = ($('div.selected').index() + 1) % 3
				if (dest == 1)
					createStats();

				navigateTo(dest);
			}
		},
		fingers: 2
	});
});