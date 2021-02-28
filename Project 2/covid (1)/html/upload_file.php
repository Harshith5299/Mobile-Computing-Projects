<?php $file_path = "D:\androidDBs";

if(!file_exists($file_path)){
    mkdir($file_path, 0777, true);
}

//print_r($_FILES);

$file = $file_path ."\\". basename($_FILES['fileUpload']['name']);

if (move_uploaded_file($_FILES['fileUpload']['tmp_name'], $file)) {
	echo "DB Uploaded Successfully to the Server";
} else {
    echo "DB Upload to the Server Failed";
}

//print_r($file);

?>