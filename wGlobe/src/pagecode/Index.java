package pagecode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.NetCDFReader;
import util.XmlParser;

/**
 * Servlet implementation class Index
 */
@WebServlet("/Index")
public class Index extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Index() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		
		String servers = request.getParameter("servers");
//		String selectedServer = servers[0].toString();
		if(servers.equals("others")){
			String textUrl = request.getParameter("textUrl");
			if(textUrl.substring(textUrl.lastIndexOf(".")+1, textUrl.length()).equals("nc") || textUrl.subSequence(textUrl.lastIndexOf(".")+1, textUrl.length()).equals("grb2")){
				NetCDFReader ncdfR = new NetCDFReader();
				String htmlstr = ncdfR.getVariable(textUrl);
				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(htmlstr);
			}
			else{
				String htmlstr = "<b><p class=\"redtext\">The url provided is not supported. Please try again.<p></b>";
				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(htmlstr);
			}
		}
		else{
			
		}
//		System.out.println(selectedServer.substring(selectedServer.lastIndexOf(".")+1, selectedServer.length()));
		
//		uploadxml(url);
		
//		String htmlstr = "<b>" + url + "</b>";
		
//		request.setAttribute("sum", url);
//		//out.println(test);
		
//		response.setContentType("text/plain");
//		response.setCharacterEncoding("UTF-8");
//		response.getWriter().write(htmlstr);
	}
	
	public void uploadxml(String url){
			
			try {
				XmlParser xmlparser = new XmlParser();
				xmlparser.setBaseUrl(url);
				xmlparser.read(url);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
