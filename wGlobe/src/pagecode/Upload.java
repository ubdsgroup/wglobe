package pagecode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.amazonaws.AmazonClientException;

import util.*;

/**
 * Servlet implementation class Upload
 */
@WebServlet("/Upload")
public class Upload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Upload() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String htmlstr = "";
		String url = "";
	
		if (ServletFileUpload.isMultipartContent(request)) {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				FileItemIterator iterator = upload.getItemIterator(request);
//				
				
				FileItemStream item = iterator.next();
				String name = item.getName();
				String extension = name.substring(name.lastIndexOf(".")+1, name.length());
				if(!extension.equals("nc") && !extension.equals("grb2")){
					htmlstr = "<b><p class=\"redtext\">Could not read uploaded file. Please try again with .nc / .grb2 file.<p></b>";
					response.setContentType("text/plain");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write(htmlstr);
					return;
				}
				InputStream is = item.openStream();
				S3Uploader aws = new S3Uploader();
				url = aws.putObjectwithInputStream(is, name);

				NetCDFReader netcdfR = new NetCDFReader();
				htmlstr = netcdfR.getVariable(url);
				response.getWriter().write(htmlstr);

			} catch (Exception e) {

			}


		} else {
			htmlstr = "No file found. please try again.";
			response.getWriter().write(htmlstr);
		}
	}

}
