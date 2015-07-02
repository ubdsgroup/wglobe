package pagecode;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.NetCDFReader;
import util.S3Uploader;

/**
 * Servlet implementation class Variable
 */
@WebServlet("/Variable")
public class Variable extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Variable() {
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
		String variable = request.getParameter("variable");
		String htmlstr = null;
		NetCDFReader netcdfR = new NetCDFReader();
		htmlstr = netcdfR.getTimebyVariable(variable);
		
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(htmlstr);
		
	}

}
