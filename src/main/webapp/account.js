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
function validateModify() {
	var email = $('form#modify input[name=email]');
	var password = $('form#modify input[name=password]');
	var newpassword = $('form#modify input[name=newpassword]');
	var confirm = $('form#modify input[name=confirm]');

	if (email.val().trim() == '') {
		alert('You must specify an email address.');
		email.addClass('error');
	}
	else if (!email.val().trim().match("^[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+@[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+\\.[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+")) {
		alert('You must enter a valid email address.');
		email.addClass('error');
	}
	else if (password.val() == '') {
		alert('You must enter a password.');
		password.addClass('error');
	}
	else if (newpassword.val() != confirm.val()) {
		alert('Passwords don\'t match.');
		newpassword.addClass('error');
		confirm.addClass('error');
	}
	else
		return true;

	return false;
}

function submitModify() {
	if (validateModify()) {
		$.ajax({
			type: 'POST',
			url: 'account',
			dataType: 'json',
			data: { 
				email: $('form#modify input[name=email]').val(),
				password: $('form#modify input[name=password]').val(),
				newpassword: $('form#modify input[name=newpassword]').val(),
				firstname: $('form#modify input[name=firstname]').val(),
				lastname: $('form#modify input[name=lastname]').val(),
				institution: $('form#modify input[name=institution]').val()
			},
			success: function(json) {
				if (json.message) {
					$('form#modify div.feedback').text(json.message).fadeIn();
				}
				else if (json.redirect) {
					window.location.replace(json.redirect);
				}
			}
		});
	}

	return false;
}

function validateContribute() {
	var description = $('form#contribute textarea[name=description]');

	if (description.val().trim() == '') {
		alert('You must enter some kind of description for the list.');
		description.addClass('error');
	}
	else
		return true;

	return false;
}

function submitContribute() {
	if (validateContribute()) {
		$.ajax({
			type: 'POST',
			url: 'contribute',
			dataType: 'json',
			data: {
				listId: $('form#contribute input[name=listId]').val(),
				description: $('form#contribute textarea[name=description]').val(),
				privacy: !$('form#contribute input[name=privacy]').prop('checked')
			},
			success: function(json) {
				alert('Thanks for contributing and making Enrichr better!');
				disableAllPopup();
			}
		});
	}	

	return false;
}

// Create tables
function createTable(dataArray, container) {
	$(container).dataTable({
		'aaData': dataArray,
		'aoColumns': [
			{
				'mData': 'created',
				'sTitle': 'Created On',
				'sWidth': '25%',
				'sClass': 'left',
				'sDefaultContent': 'Now',
				'bSearchable': false
			},
			{ 
				'mData': 'list_id',
				'bSearchable': false,
				'bVisible': false
			},
			{
				'mData': 'description',
				'sTitle': 'Description',
				'sClass': 'left',
				'mRender': function(data, type, full) {
					return '<a href="' + 'enrich?dataset=' + full['list_id'] + '">' + data + '</a>';
				}
			},
			{
				'mData': 'list_id',
				'sTitle': 'Actions',
				'sClass': 'left',
				'sWidth': '10%',
				'bSearchable': false,
				'bSortable': false,
				'mRender': function(data, type, full) {
					return '<a href="#" onclick="contributePopup(\'' + full['description'] + '\', \'' + data + '\');" class="action"><img src="images/contribute.png" title="Contribute this list to a crowdsourced gene-set library"/></a>&nbsp;<a href="#" onclick="sharePopup(\'' + data + '\');" class="action"><img src="images/share_black.png" title="Share this list to collaborators"/></a>';
				}
			}
		],
		'aaSorting': [[0, 'desc'], [1, 'desc']],
		'oLanguage': {
			'sLengthMenu': '_MENU_ lists per page',
			'sInfo': 'Showing _START_ to _END_ of _TOTAL_ lists'
		}
	});	
}

function contributePopup(description, listId) {
	var popup = $('#contribute-form');
	var blanket = $('#blanket');

	centerPopup(popup);
	loadPopup(popup, blanket);
	popup.find('#short-desc input[name=shortDescription]').val(description);
	popup.find('input[name=listId]').val(listId);

	return false;
}

function sharePopup(listId) {
	var popup = $('#share-link');
	var blanket = $('#blanket');

	centerPopup(popup);
	loadPopup(popup, blanket);
	var shareText = popup.find('input');
	shareText.val(window.location.protocol + '//' + window.location.host + '/Enrichr/enrich?dataset=' + listId);
	shareText.select();

	return false;
}

function loadPopup(popup, blanket) {
	blanket.css({'opacity': '0.65'});
	blanket.fadeIn();
	popup.fadeIn();
}

function disableAllPopup() {
	$('#blanket').fadeOut();
	$('.popup').fadeOut();
}

function centerPopup(popup) {
	//request data for centering
	var windowWidth = document.documentElement.clientWidth;
	var windowHeight = document.documentElement.clientHeight;
	var popupHeight = popup.height();
	var popupWidth = popup.width();
	//centering
	popup.css({		
		'margin-top': -1*popupHeight/2,
		'margin-left': -1*popupWidth/2
	});
}

function hashcheck(onload) {
	var transitionSpeed = (typeof onload !== 'boolean') ? 'slow' : 0;

	var hash = window.location.hash.substring(1);
	navigateTo(globals.tabList.indexOf(hash), transitionSpeed);
}

$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	globals = {};	// Stores global vars
	globals.changingCategory = false;	// Prevent changing category too fast
	globals.tabList = ['', 'settings'];

	// Define new selector for empty input fields
	$.expr[':'].emptyValue = function (elem) {
		return $(elem).is(":input") && $(elem).val() === "";
	};

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

	$.getJSON('account', function(json) {
		if (json.user == '') {
			window.location.replace("/Enrichr");
		}
		else {
			if (json.firstname)
				$('a#account-name').text(json.firstname);
			else
				$('a#account-name').text(json.user)
			$('div#login-status').fadeIn('slow');
			$('img.loader').remove();
			createTable(json.lists, '#list_table');

			populateFields = function(name, value) {
				if (value)
					$('form#modify input[name=' + name + ']').val(value).focus();
			}

			populateFields('email', json.user);
			populateFields('firstname', json.firstname);
			populateFields('lastname', json.lastname);
			populateFields('institution', json.institution);
			$('.has-placeholder:emptyValue').next('span.placeholder-text').show();
		}
	});

	// Enable placeholders and error messages for required fields
	$('.has-placeholder, .required').focus(function() {
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
	$('form#modify input[type=password]:not(input[name=password])').focus(function() {
		$('form#modify label#confirm div.errormsg').hide();
		$('form#modify input[type=password]:not(input[name=password])').removeClass('error');
	})
	$('form#modify input[name=confirm]').blur(function() {
		var input = $(this);
		if ($('form#modify input[name=newpassword]').val() != input.val()) {
			input.siblings('div.errormsg').show();
			$('form#modify input[type=password]:not(input[name=password])').addClass('error');
		}
	})

	$('#modify').submit(submitModify);
	$('#contribute').submit(submitContribute);
});