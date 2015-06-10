function readXML(url, parent){
	//download xml file form remote server by sending a http get request to getxml.php
	var request = xmlhttp = new XMLHttpRequest();
	xmlhttp.open("GET","getxml.php?url="+url,false);
	xmlhttp.send();
	xmlDoc = xmlhttp.responseXML;

	//after we have downloaded the xml. parse the local xml file.
	var xmlhttp = new XMLHttpRequest();
	xmlhttp.open("GET","data.xml",false);
	xmlhttp.send();
	xmlDoc = xmlhttp.responseXML;
	buildTree(xmlDoc,parent);
}


function buildTree(xml, parent){
	var nodes = xml.children;
	if( nodes.length != 0 ){
		var list = document.createElement('ul');
		for( var i=0; i<nodes.length; i++){
			var curNode = nodes.item(i); 
			var curTagName = curNode.tagName;
			if( curTagName === "catalogRef" ){
				// display the xlink:title attribute
				var item = document.createElement('li');
				var link = document.createElement('a');
				var oc = "readXML('"+fullURL(curNode.getAttribute("xlink:href"))+"',this.parentNode); return false;";
				link.setAttribute("href", "#"); //TODO
				link.setAttribute("onclick", oc)
				link.appendChild(document.createTextNode(curNode.getAttribute("xlink:title")));
				item.appendChild(link);
			}else if( curTagName === "catalog" || curTagName === "dataset" ){
				// display the name attribute 
				var item = document.createElement('li');
				item.appendChild(document.createTextNode(curNode.getAttribute("name")));
			}else{
				continue;
			}

			list.appendChild(item);
			buildTree(curNode, list);
			parent.appendChild(list);		
		}
	}				
}

//completes the url from noaa.gov
function fullURL(url){
	if(url.charAt(0) === '/'){
		return "http://nomads.ncdc.noaa.gov"+url;
	}else{
		return "http://nomads.ncdc.noaa.gov/thredds/"+url;
	}
}