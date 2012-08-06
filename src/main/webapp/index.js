function geneCount() {
	if ($('textarea#text-area').val())
    	$('span#gene-count').text($('textarea#text-area').val().trim().split(/\r?\n/g).length);
    else
    	$('span#gene-count').text(0);
}

function navigateTo(index) {
	$('div#form div.selected').fadeToggle('slow', function() {
		$('.selected').removeClass('selected');
		$('div#navbar td').eq(index).addClass('selected');
		$('#form > div').eq(index).addClass('selected');
		$('div#form div.selected').fadeToggle('slow');
	});
}

function validateInput() {
	if ($('input#file-chooser').val())
		return true;
	else if ($('textarea#text-area').val())
		return true;
	else {
		alert("Nothing to analyze. Please select a file to upload or paste in a list of genes.");
		navigateTo(0);
		return false;
	}
}