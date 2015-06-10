package gov;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url = "http://nomads.ncdc.noaa.gov/thredds/catalog.xml";
		test test1 = new test();
		test1.uploadxml(url);
	}

	public void uploadxml(String url) {
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document dom = dBuilder.parse(new URL(url).openStream());

			Element doc = dom.getDocumentElement();

			System.out.println("Root element :"
					+ dom.getDocumentElement().getNodeName());

			NodeList nList = doc.getChildNodes();

			System.out.println("----------------------------");

			String out = "";
			out = buildTree(nList, out, url);
			System.out.println(out);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String buildTree(NodeList nList, String out, String curUrl) {

		try {
			for (int i = 0; i < nList.getLength(); i++) {

				Node curNode = nList.item(i);

				if (nList.item(i).getNodeName() == "dataset") {
					Element eElement = (Element) curNode;
					String name = eElement.getAttribute("name");
					if (!out.contains(name)) {
						out += "<li>" + name;
					}
					if (curNode.hasChildNodes()) {
						out += "<ul>";
						NodeList childNList = nList.item(i).getChildNodes();
						for (int j = 0; j < childNList.getLength(); j++) {
							Node curChildNode = childNList.item(j);
							if (curChildNode.getNodeName() == "dataset") {
								buildTree(childNList, out, curUrl);
							} else if (curChildNode.getNodeName() == "catalogRef") {
								Element eCRElement = (Element) curChildNode;
								String crname = eCRElement
										.getAttribute("xlink:title");
								String newpath = eCRElement
										.getAttribute("xlink:href");
								out += "<li>" + crname;

								curUrl = fullURL(curUrl, newpath);
								DocumentBuilderFactory dbFactory = DocumentBuilderFactory
										.newInstance();
								DocumentBuilder dBuilder = dbFactory
										.newDocumentBuilder();
								Document dom = dBuilder.parse(new URL(
										curUrl).openStream());
								Element doc = dom.getDocumentElement();

								NodeList nCRList = doc.getChildNodes();
								buildTree(nCRList, out,curUrl);
							}
						}
						
					}
					out += "</li>";
				} else if (nList.item(i).getNodeName() == "catalogRef") {
					Element eCRElement = (Element) curNode;
					String crname = eCRElement.getAttribute("xlink:title");
					String newpath = eCRElement.getAttribute("xlink:href");

					if (curNode.hasChildNodes()) {
						out += "<ul>";
					}
					out += "<li>" + crname;

					curUrl = fullURL(curUrl, newpath);
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document dom = dBuilder.parse(new URL(curUrl)
							.openStream());
					Element doc = dom.getDocumentElement();

					NodeList nCRList = doc.getChildNodes();
					buildTree(nCRList, out, curUrl);
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return out;
	}

	public String fullURL(String url, String newpath) {
		
		String baseUrl = url.substring(0 , url.lastIndexOf('/')+1);

		if (url.charAt(0) == '/') {
			url = baseUrl + newpath;
			return url;
		} else {
			url = baseUrl + newpath;
			return url;
		}
	}

}
