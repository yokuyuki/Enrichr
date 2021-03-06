/* Form data handling */
function validateReset() {
	var email = $('form#reset input[name=email]');
	var password = $('form#reset input[name=password]');
	var confirm = $('form#reset input[name=confirm]');
	var token = $('form#reset input[name=token]');

	if (email.val().trim() == '') {
		alert('You must specify an email address.');
		email.addClass('error');
	}
	else if (!email.val().trim().match("^[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+@[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+\\.[-0-9A-Za-z!#$%&'*+/=?^_`{|}~.]+")) {
		alert('You must enter a valid email address.');
		email.addClass('error');
	}
	else if (token.val().trim() == '') {
		alert('You must enter a valid token.');
		token.addClass('error');
	}
	else if (password.val() == '') {
		alert('You must enter a password.');
		password.addClass('error');
	}
	else if (password.val() != confirm.val()) {
		alert('Passwords don\'t match.');
		password.addClass('error');
		confirm.addClass('error');
	}
	else
		return true;

	return false;
}

function submitReset() {
	if (validateReset()) {
		$.ajax({
			type: 'POST',
			url: 'reset',
			dataType: 'json',
			data: { 
				email: $('form#reset input[name=email]').val(),
				token: $('form#reset input[name=token]').val(),
				password: $('form#reset input[name=password]').val()
			},
			success: function(json) {
				if (json.message) {
					$('form#reset div.feedback').text(json.message).fadeIn();
				}
				else if (json.redirect) {
					window.location.replace(json.redirect);
				}
			}
		});
	}

	return false;
}

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

$(document).ready(function () {
	$.ajaxSetup({ cache: false });	// Prevent IE from caching GET requests
	globals = {};	// Stores global vars

	// Fill values based on link
	var email = queryString('user');
	if (email) {
		$('form#reset input[name=email]').val(email).next('span.placeholder-text').hide();
	}
	var auth = queryString('token');
	if (auth) {
		$('form#reset input[name=token]').val(auth).next('span.placeholder-text').hide();
	}

	// Enable placeholders and error messages for required fields
	$.expr[':'].emptyValue = function (elem) {
		return $(elem).is(":input") && $(elem).val() === "";
	};
	$('input.has-placeholder:emptyValue').next('span.placeholder-text').show();
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
	$('form#reset input[type=password]').focus(function() {
		$('form#reset label#confirm div.errormsg').hide();
		$('form#reset input[type=password]').removeClass('error');
	})
	$('form#reset input[name=confirm]').blur(function() {
		var input = $(this);
		if ($('form#reset input[name=password]').val() != input.val()) {
			input.siblings('div.errormsg').show();
			$('form#reset input[type=password]').addClass('error');
		}
	})

	$('#reset').submit(submitReset);
});