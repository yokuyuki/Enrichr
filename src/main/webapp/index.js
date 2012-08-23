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
						"sTitle": "Dataset",
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
	_changingCategory = false;	// Prevent changing category too fast
});