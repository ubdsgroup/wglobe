package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParser {

	private String baseUrl = "";
	private String curUrl = "";

	public void setBaseUrl(String url)  throws MalformedURLException{
	       
	    URL netUrl = new URL(url);
	    String host = netUrl.getHost();
	    if(!host.startsWith("http") && !host.startsWith("https")){
	         this.baseUrl = "http://" + host;
	    } 
	    System.out.println(host);
	}

	public void setcurUrl(String url){
	       
	    this.curUrl = url;
	    
	}
	
	public void read(String url) {
		
		File file = new File("/Users/ERAN/Desktop/New.txt");
		
		try {

			file.createNewFile();
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document dom = dBuilder.parse(new URL(url).openStream());

			Element doc = dom.getDocumentElement();

		
			NodeList nList = doc.getChildNodes();

			FileWriter fw = new FileWriter("/Users/ERAN/Desktop/New.txt",true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			
			buildTree(nList, bw,url);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkExist(){
		return true;
	}

	public void buildTree(NodeList nList, BufferedWriter bw, String curUrl) {

		String newUrl = "";
		try {
			curUrl = this.curUrl;
			for (int i = 0; i < nList.getLength(); i++) {

				Node curNode = nList.item(i);

				if (nList.item(i).getNodeName() == "dataset") {
					Element eElement = (Element) curNode;
					String name = eElement.getAttribute("name");
					if (!bw.toString().contains(name)) {
						bw.write("<li>" + name);
					}
					if(nList.item(i).hasChildNodes()){
						for(int j =0 ; j < nList.item(i).getChildNodes().getLength() ; j++){
							if(nList.item(i).getChildNodes().item(j).getNodeName() == "catalogRef" || nList.item(i).getChildNodes().item(j).getNodeName() == "dataset")
							{
								if (!bw.toString().contains(name)) {
									bw.write("<li>" + name);
								}
								NodeList childList = nList.item(i).getChildNodes();	
								bw.write("<ul>");
								buildTree(childList, bw,curUrl);
								bw.write("</ul>");
								break;
							}
							
							
						}
						
					}
					bw.write("</li>");
				} 
				else if (nList.item(i).getNodeName() == "catalogRef")
				{
					Element eCRElement = (Element) curNode;
					String crname = eCRElement.getAttribute("xlink:title");
					String newpath = eCRElement.getAttribute("xlink:href");

					if (curNode.hasChildNodes()) {
						bw.write("<ul>");
					}
					bw.write("<li>" + crname);

					newUrl = fullURL(curUrl, newpath);
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document dom = dBuilder.parse(new URL(newUrl)
							.openStream());
					Element doc = dom.getDocumentElement();

					NodeList nCRList = doc.getChildNodes();
					buildTree(nCRList, bw,newUrl);
				}
			
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public String fullURL(String url, String newpath) {
		
		if(newpath.startsWith("/"))
		{
			// Start with "/"
			url = baseUrl + newpath;
			curUrl = url;
			System.out.println("current url is " + url );
			if(url.contains("197901"))
			{
				String test = "s";
				System.out.println(test);
			}
			return url;
		}
		else{
			// Does not start with "/"
			url = url.substring(0 , url.lastIndexOf('/')+1);
			url = url + newpath;
			System.out.println("current url is " + url );
			if(url.contains("197901"))
			{
				String test = "s";
				System.out.println(test);
			}
			return url;
		}
	}
}
