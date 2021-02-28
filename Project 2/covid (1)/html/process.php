<?php

$file = $_POST['outputFile'];
$pythonFile = $_POST['pythonFile'];
$subjectID = $_POST['subjectID'];
$date = $_POST['date'];

$output = shell_exec('C:\Python27\python.exe ' . $pythonFile . ' ' . $subjectID . ' ' . $date . ' ' . $file);

if (file_exists($file)) {
	
	header("Access-Control-Allow-Origin: *");
	header('Access-Control-Allow-Credentials: true');    
	header("Access-Control-Allow-Methods: GET, POST, OPTIONS"); 
    header('Content-Type: text/plain');
    header('Expires: 0');
    header('Cache-Control: must-revalidate');
    header('Pragma: public');
    readfile($file);
	unlink($file);
}

?>
