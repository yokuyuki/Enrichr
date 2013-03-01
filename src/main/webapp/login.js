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
	if ($('form#login input[name=email]').val().trim() == '') {
		alert('You must specify an email address.');
		return false;
	}

	return true;
}

function submitLogin() {
	if (validateLogin()) {
		$.ajax({
			type: 'POST',
			url: 'login',
			dataType: 'json',
			data: { 
				email: $('form#login input[name=email]').val(),
				password: $('form#login input[name=password]').val()
			},
			success: function(json) {
				if (json.message) {
					$('form#login div.feedback').text(json.message).fadeIn();
				}
				else if (json.redirect) {
					window.location.replace(json.redirect);
				}
			}
		});
	}

	return false;
}

function validateRegister() {
	if ($('form#register input[name=email]').val().trim() == '')
		alert('You must specify an email address.');
	else if (!$('form#register input[name=email]').val().trim().match("^[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+@[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+\\.[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+"))
		alert('You must enter a valid email address.');
	else if ($('form#register input[name=password]').val() != $('form#register input[name=confirm]').val())
		alert('Passwords don\'t match.');
	else
		return true;

	return false;
}

function submitRegister() {
	if (validateRegister()) {
		$.ajax({
			type: 'POST',
			url: 'register',
			dataType: 'json',
			data: { 
				email: $('form#register input[name=email]').val(),
				password: $('form#register input[name=password]').val(),
				firstname: $('form#register input[name=firstname]').val(),
				lastname: $('form#register input[name=lastname]').val(),
				institution: $('form#register input[name=institution]').val()
			},
			success: function(json) {
				if (json.message) {
					$('form#register div.feedback').text(json.message).fadeIn();
				}
				else if (json.redirect) {
					window.location.replace(json.redirect);
				}
			}
		});
	}

	return false;
}

function validateForgot() {
	if ($('form#forgot input[name=email]').val().trim() == '')
		alert('You must specify an email address.');
	else if (!$('form#forgot input[name=email]').val().trim().match("^[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+@[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+\\.[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+"))
		alert('You must enter a valid email address.');
	else
		return true;

	return false;
}

function submitForgot() {
	if (validateForgot()) {
		$.ajax({
			type: 'POST',
			url: 'forgot',
			dataType: 'json',
			data: { email: $('form#forgot input[name=email]').val() },
			success: function(json) {
				if (json.message) {
					$('form#forgot div.feedback').text(json.message).fadeIn();
				}
			}
		});
	}

	return false;
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
	globals.tabList = ['', 'register', 'forgot'];

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

	$('#login').submit(submitLogin);
	$('#register').submit(submitRegister);
	$('#forgot').submit(submitForgot);
});