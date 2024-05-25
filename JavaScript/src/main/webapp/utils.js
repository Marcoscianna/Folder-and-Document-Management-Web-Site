/**
 * AJAX call management
 */

function makeCall(method, url, formElement, cback, reset = true) {
	let req = new XMLHttpRequest(); // visible by closure
	req.onreadystatechange = function() {
		cback(req)
	}; // closure
	req.open(method, url);
	if (formElement == null) {
		req.send();
	} else {
		req.send(new FormData(formElement));
	}
	if (formElement !== null && reset === true) {
		formElement.reset();
	}
}

function makeCall2(method, url, data, callback) {
	let xhr = new XMLHttpRequest();
	xhr.onreadystatechange = function() {
		if (xhr.readyState === XMLHttpRequest.DONE) {
			callback(xhr);
		}
	};
	xhr.open(method, url, true);
	xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	let formattedData = `folderId=${encodeURIComponent(data)}`; 

	xhr.send(formattedData);
}

