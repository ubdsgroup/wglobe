package util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ucar.nc2.*;
import ucar.nc2.dataset.*;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.io.http.*;

public class NetCDFReader {
	
	public NetcdfDataset dataset;
	
	public void getTime(){
		Variable v;
	}

	public String getVariable(String texturl) {
		
		NetcdfFile nFile = null;
		String out = "";

		try {   
			String extension = texturl.substring(texturl.lastIndexOf(".") + 1,
					texturl.length());

			texturl = replaceHTTPS(texturl);

			nFile = NetcdfFile.open(texturl);
			out += "<ul id=\"tree1\" class=\"tree\"><li>"
					+ texturl.substring(texturl.lastIndexOf("/") + 1,
							texturl.length()) + "<ul>";
			Set<NetcdfDataset.Enhance> enhanceSet = new HashSet<NetcdfDataset.Enhance>();
			enhanceSet.add(NetcdfDataset.Enhance.CoordSystems);
			enhanceSet.add(NetcdfDataset.Enhance.ScaleMissingDefer);

			dataset = new NetcdfDataset(nFile);
			List<Variable> vars = dataset.getVariables();			

			if (extension.equals("grb2")) {
				GridDataset gridDataset = new GridDataset(dataset);
				List<GridDatatype> lGrid = gridDataset.getGrids();
				for (int i = 0; i < lGrid.size(); i++) {
					String name = lGrid.get(i).getName();
					out += "<li><a class=\"vars\" onclick=\"getMeasure(this)\">"
							+ name + "</a></li>";
				}

			} else if (extension.endsWith("nc")) {
//				texturl = texturl.replace("http", "dods");
				GridDataset gridDataset = GridDataset.open(texturl);
				List<GridDatatype> lGrid = gridDataset.getGrids();
				for (int i = 0; i < lGrid.size(); i++) {
					String name = lGrid.get(i).getName();
					out += "<li><a class=\"vars\" onclick=\"getMeasure(this)\">"
							+ name + "</a></li>";
				}
//				for (int i = 0; i < vars.size(); i++) {
//					Boolean isCoorD = vars.get(i).isCoordinateVariable();
//					if (!isCoorD) {
//						Variable var = vars.get(i);
//						out += "<li><a class=\"vars\" onclick=\"getMeasure(this)\">"
//								+ var.getName() + "</a></li>";
//					}
//				}
			}

			out += "</ul></li></ul>";
		} catch (IOException e) {
			System.err.println("Not found" + e);
			e.printStackTrace();
			out = "<p class=\"error\">File is not supported. Please use a valid .nc / .grb2 file.</p>";
			return out;
		}

		return out;
	}

	public String replaceHTTPS(String url) {
		if (url.startsWith("https")) {
			url = "http" + url.substring(5, url.length());
		}
		return url;

	}
}
