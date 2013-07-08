/* Format string */
String.prototype.format = function() {
	var args = arguments;
	return this.replace(/{(\d+)}/g, function(match, number) { 
		return typeof args[number] != 'undefined'
			? args[number]
			: match
		;
	});
};

/* Counter for how many analysis has been done */
function geneCount() {
	if ($('textarea#text-area').val())
		$('span#gene-count').text($('textarea#text-area').val().trim().split(/\r?\n/g).length);
	else
		$('span#gene-count').text(0);
}

/* Navigation */
function navigateTo(index, transitionSpeed) {
	if (globals.changingCategory || $('div.selected').index() == index)
		return;
	else
		globals.changingCategory = true;
	if (index == globals.tabList.indexOf('stats')) { createStats(); }
	if (index == globals.tabList.indexOf('find')) { createAutocomplete(); }
	transitionSpeed = (typeof transitionSpeed === 'undefined') ? 'slow' : transitionSpeed;
	$('#content div.selected').fadeToggle(transitionSpeed, function() {
		$('.selected').removeClass('selected');
		$('#navbar td').eq(index).addClass('selected');
		$('#content > div').eq(index).addClass('selected');
		$('#content div.selected').fadeToggle(transitionSpeed);
		globals.changingCategory = false;
	});
}

function getTab(name) {
	window.location.hash = name;
	return false;
}

/* Form data handling */
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
	return false;
}

function insertFuzzyExample() {
	$.get('fuzzy_example_list.txt', function(data) {
		$('textarea#text-area').val(data);
		geneCount();
		$('#description input').val("Sample fuzzy gene list");
	});
	return false;
}

/* Data Statistics Page */
function createStats() {
	if (!$('#stats').hasClass('done')) {
		$.getJSON('json/dataset_statistics.json', function(json) {
			$('#stats').addClass('done');
			$('#stats').dataTable({
				'aaData': json,
				'aoColumns': [
					{ 
						'sTitle': 'Gene-set Library',
						'mRender': function(data, type, full) {
							return '<a href="' + full[4] + '" target="_blank">' + data + '</a>';
						}
					},
					{ 
						'sTitle': 'Terms',
						'sClass': 'right',
						'asSorting': ['desc', 'asc'],
						'bSearchable': false
					},
					{ 
						'sTitle': 'Gene Coverage',
						'sClass': 'right',
						'asSorting': ['desc', 'asc'],
						'bSearchable': false
					},
					{ 
						'sTitle': 'Genes per Term',
						'sClass': 'right',
						'asSorting': ['desc', 'asc'],
						'bSearchable': false,
						'mRender': function(data, type, full) {
							return data.toFixed(4);
						}
					},
					{
						'bVisible': false,
						'bSearchable': false
					}
				],
				'aaSorting': [[1, 'desc']],
				'bPaginate': false,
				'sDom': '<t>'
			});
		});
	}
}

/* Find A Gene Page */
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
	// Clear container first
	$('#gene-info').empty();

	var getParams = { gene: gene, json: true }
	if (typeof globals.categories === 'undefined')
		getParams.setup = true

	$.getJSON('genemap', getParams, function(json) {
		if (typeof globals.categories === 'undefined') {
			globals.categories = json.categories
		}

		if (json.gene == null) {
			$('#gene-info').html('No terms found for gene <span class="failed">' + gene + '</span>.');
			$('#gene-info').fadeIn();
			return;
		}

		var mainDiv = document.getElementById("gene-info");
		var toggleFunc = function(id) { return function() { toggleSection(id); }; };

		for (var i = 0; i < globals.categories.length; i++) {
			var category = globals.categories[i];
			var categoryName = category.name;

			// Pulled in front to edit category to be id friendly
			var categoryText = document.createTextNode(categoryName);
			categoryName = categoryName.replace(/[ //]/g, '-');

			var categoryDiv = document.createElement('div');
			categoryDiv.className = 'category';
			categoryDiv.id = categoryName;
			
			var categoryToggleDiv = document.createElement('div');
			categoryToggleDiv.className = 'toggleIcon';
			categoryToggleDiv.onclick = toggleFunc(categoryName);
			categoryDiv.appendChild(categoryToggleDiv);
			
			categoryDiv.appendChild(categoryText);

			var backgroundTypesDiv = document.createElement('div');
			backgroundTypesDiv.className = 'background-types hidden';
			categoryDiv.appendChild(backgroundTypesDiv);

			mainDiv.appendChild(categoryDiv);

			for (var j = 0; j < category.libraries.length; j++) {
				var library = category.libraries[j];
				var libraryName = library.name;

				if (!(libraryName in json.gene))
					continue;

				var typeDiv = document.createElement('div');
				typeDiv.className = 'background-type';
				typeDiv.id = libraryName;

				var typeToggleDiv = document.createElement('div');
				typeToggleDiv.className = 'toggleIcon';
				typeToggleDiv.onclick = toggleFunc(libraryName);
				typeDiv.appendChild(typeToggleDiv);

				var typeText = document.createTextNode(libraryName.replace(/_/g, ' '));
				typeDiv.appendChild(typeText);

				var resultsDiv = document.createElement('div');
				resultsDiv.className = 'results hidden';
				typeDiv.appendChild(resultsDiv);

				backgroundTypesDiv.appendChild(typeDiv);

				var terms = json.gene[libraryName];
				var formatString = library.format
				var formattedTerms = [];
				for (var k in terms) {
					formattedTerms.push(formatString.format('<span class="gene">' + gene + '</span>', '<span class="term">' + terms[k] + '</span>'));
				}
				resultsDiv.innerHTML = formattedTerms.join('<br/>');
			}
		}

		$('#gene-info').fadeIn();
	});
}

function toggleSection(id) {
	$('#' + id + ' > div.toggleIcon').toggleClass('open');
	$('#' + id + ' > div.background-types').slideToggle();
	$('#' + id + ' > div.results').slideToggle();
}

function hashcheck(onload) {
	var transitionSpeed = (typeof onload !== 'boolean') ? 'slow' : 0;

	var hash = window.location.hash.substring(1);
	if (hash[0] == '!') {
		// Not on find tab
		if (globals.tabList[$('div.selected').index()] != 'find') {
			navigateTo(globals.tabList.indexOf('find'), transitionSpeed);
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
		navigateTo(globals.tabList.indexOf(hash), transitionSpeed);
	}
}

$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	globals = {};	// Stores global vars
	globals.changingCategory = false;	// Prevent changing category too fast
	globals.tabList = ['', 'new', 'stats', 'find', 'about', 'help'];

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
			var dest = ($('div.selected').index() + 1) % globals.tabList.length;
			getTab(globals.tabList[dest]);
		}
	);
	$('body').swiperight(function() {
			var dest = ($('div.selected').index() - 1 + globals.tabList.length) % globals.tabList.length;
			getTab(globals.tabList[dest]);
		}
	);

	$.getJSON('status', function(json) {
		if (json.user == '') {
			$('div#login-prompt').fadeIn('slow');
		}
		else {
			if (json.firstname)
				$('a#account-name').text(json.firstname);
			else
				$('a#account-name').text(json.user)
			$('div#login-status').fadeIn('slow');
		}
	});

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

	// Bind tutorial image enlarge
	$('div.answer img').click(function() { $(this).toggleClass('large'); })
	.attr('title', 'Click to enlarge');
});