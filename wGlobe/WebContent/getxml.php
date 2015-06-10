<?php
	if( isset($_GET['url'])){
		$content = file_get_contents($_GET['url']);
		file_put_contents("data.xml", $content);
	}else{
		echo "Error: url not set.";
	}
?>