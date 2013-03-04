/* Navigation */
function navigateTo(index, transitionSpeed) {
	if (globals.changingCategory || $('div.selected').index() == index)
		return;
	else
		globals.changingCategory = true;
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
}

/* Form data handling */
function validateLogin() {
	if ($('form#register input[name=email]').val().trim() == '')
		alert('You must specify an email address.');
}

function validateRegister() {
	if ($('form#register input[name=email]').val().trim() == '')
		alert('You must specify an email address.');
	else if ($('form#register input[name=password]').val() != $('form#register input[name=confirm]').val())
		alert('Passwords don\'t match.');
	else
		return true;

	return false;
}

// Create tables
function createTable(dataArray, container) {
	$(container).dataTable({
		"aaData": dataArray,
		"aoColumns": [
			{
				"mData": "description",
				"sTitle": "Description",
				"sClass": "left",
				"sWidth": "75%",
				"mRender": function(data, type, full) {
					return '<a href="' + 'enrich?dataset=' + full["list_id"] + '">' + data + '</a>';
				}
			},
			{
				"mData": "created",
				"sTitle": "Created On",
				"sClass": "left"
			}
		],
		"aoColumnDefs": [
			{ "bSearchable": false, "aTargets": [1]}
		],
		"aaSorting": [[1, "desc"]]
	});	
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
	globals.tabList = ['', 'settings'];

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
			var dest = ($('div.selected').index() - 1 + globals.tabList.length) % globals.tabList.length;
			getTab(globals.tabList[dest]);
		}
	);
	$('body').swiperight(function() {
			var dest = ($('div.selected').index() + 1) % globals.tabList.length;
			getTab(globals.tabList[dest]);
		}
	);

	$.getJSON('account', function(json) {
		if (json.user == '') {
			window.location.replace("/Enrichr");
		}
		else {
			$('a#account-name').text(json.user)
			$('div#login-status').fadeIn('slow');
			$('img.loader').remove();
			createTable(json.lists, '#list_table');
		}
	});

	// Enable placeholders and error messages for required fields
	$('input.has-placeholder:text[value=""]').next('span.placeholder-text').show();
	$('input.has-placeholder').focus(function() {
		var input = $(this);
		input.next('span.placeholder-text').hide();
		if (input.hasClass('required')) {
			input.siblings('div.errormsg').hide();
			input.removeClass('error');
		}
	}).blur(function() {
		var input = $(this);
		if (input.val() == '') {
			input.next('span.placeholder-text').show();
			if (input.hasClass('required')) {
				input.siblings('div.errormsg').show();
				input.addClass('error');
			}
		}
	});	

	// Error messages for password checking
	$('form#register input[type=password]').focus(function() {
		$('form#register label#confirm div.errormsg').hide();
		$('form#register input[type=password]').removeClass('error');
	})
	$('form#register input[name=confirm]').blur(function() {
		var input = $(this);
		if ($('form#register input[name=password]').val() != input.val()) {
			input.siblings('div.errormsg').show();
			$('form#register input[type=password]').addClass('error');
		}
	})	
});