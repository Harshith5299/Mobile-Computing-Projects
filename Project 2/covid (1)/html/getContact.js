function downloadFile(urlToSend, sendStr, fileName) {
     var req = new XMLHttpRequest();
     req.open("POST", urlToSend, true);
	 req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
     req.responseType = "blob";
     req.onload = function (event) {
         var blob = req.response;
         //var fileName = req.getResponseHeader('Content-Disposition').split('filename=')[1].split(';')[0];
		 //console.log(req.getAllResponseHeaders())
         var link=document.createElement('a');
         link.href=window.URL.createObjectURL(blob);
         link.download=fileName;
         link.click();
     };
	 
     req.send(sendStr);
}

function submit_by_id() {
	if (validation()) {
		var fileName = "contactGraph.txt"
		var subjectID = document.getElementById("subjectID").value;
		var date = document.getElementById("date").value;
		var params = "pythonFile=getContactGraph.py" + "&subjectID=" + subjectID + "&date=" + date + "&outputFile=" + fileName;
		downloadFile('http://localhost/process.php', params, fileName);
	}
}

function validation() {
	var subjectID = document.getElementById("subjectID").value;
	var date = document.getElementById("date").value;
	var dateReg = /^\d{4}[\/\-](0?[1-9]|1[012])[\/\-](0?[1-9]|[12][0-9]|3[01])$/;
	if (subjectID === '' || date === '') {
		alert("Please fill all fields!");
		return false;
	}
	if (subjectID < 1 || subjectID > 12) {
		alert("Invalid SubjectID! SubjectID should be between 1 and 12");
		return false;
	}
	if (!(date).match(dateReg)) {
		alert("Invalid Date!");
		return false;
	}
	return true;
}