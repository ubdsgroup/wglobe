package util;

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
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;

public class NetCDFReader {
	
	public String getVariable(String texturl){
		 NetcdfDataset dataset;
		 NetcdfFile nFile = null;
	     String out = "";
	        
	        try {
	        	String extension = texturl.substring(texturl.lastIndexOf(".")+1, texturl.length());
	        	
	        	if(extension.equals("grb2")){
	        		 //i cant read nc but i can read grb2
	        		 //i cannot read aws
	        		 // i cannot read dropbox
//	        		dataset = NetcdfDataset.openDataset(texturl);
	        		nFile = NetcdfFile.open(texturl);
	        	}
	        	else if(extension.equals("nc")){
	        		//i could read nc but i cant read grb2
	        		//i can read aws / grb2 no / nc yes
	        		//i can read dropbox 
	        		nFile = new NetcdfFile(new URL (texturl));
	        	}
	        	else{
	        		out = "<p class=\"error\">File is not supported. Please use .nc / .grb2 file.</p>";
	        		return out;
	        	}
	        	out += "<ul id=\"tree1\" class=\"tree\"><li>" + texturl.substring(texturl.lastIndexOf("/")+1, texturl.length()) + "<ul>";
	        	Set<NetcdfDataset.Enhance> enhanceSet = new HashSet<NetcdfDataset.Enhance>();
	  	        enhanceSet.add(NetcdfDataset.Enhance.CoordSystems);
	  	        enhanceSet.add(NetcdfDataset.Enhance.ScaleMissingDefer);
	        	
	            dataset = new NetcdfDataset(nFile);
	            GridDataset gridDataset = new GridDataset(dataset);
	            List<GridDatatype> lGrid = gridDataset.getGrids();
	            for(int i = 0 ; i < lGrid.size() ; i++){
	            	String name = lGrid.get(i).getName();
	            	out += "<li><a class=\"vars\" onclick=\"getMeasure(this)\">" + name + "</a></li>";
	            }
	        	
	            out += "</ul></li></ul>"; 
	        }
	        catch (IOException e)
	        {
	            System.err.println("Not found"+e);
	            e.printStackTrace();
	            out = "<p class=\"error\">File is not supported. Please use .nc / .grb2 file.</p>";
        		return out;
	        }
	       
	        return out;
	}
//	public NetcdfDataset getDataset(String filename){
//        NetcdfDataset ncd = null;
//        try {
//          ncd = NetcdfDataset.openDataset(filename);
//        } catch (IOException ioe) {
//          log("trying to open " + filename, ioe);
//        } finally { 
//          if (null != ncd) try {
//            ncd.close();
//          } catch (IOException ioe) {
//            log("trying to close " + filename, ioe);
//          }
//        }
//        
//        return ncd;
//    }
    
//    public String getVars() throws NullPointerException{
//           StringBuilder sb = new StringBuilder();
//           java.util.List<VariableSimpleIF> vars;
//           NetcdfDataset dataset = getDataset(this.url);
//           String targetPage = "target.jsp"; // change this
//           String encURL = null;
//                 
//           try {
//        	   GridDataset gdata = new GridDataset(dataset);
//        	   vars = gdata.getDataVariables();
//        	   
//           } catch(NullPointerException npe){
//        	   return "URL invalid";
//           } catch(IOException ioe){ 
//        	   return "Error";
//           } 
//           
//           try {
//        	   encURL = URLEncoder.encode(url, "UTF-8");
//           } catch(UnsupportedEncodingException uee){
//        	   log("url encoding not supported");
//           }
//           
//           if(vars != null){
//               for(VariableSimpleIF v: vars){ 	  
//            	  sb.append("<li><a href=\""+targetPage+"?url="+encURL+"&varName="+v.getName()+"\">");
//                  sb.append(v.getName());
//                  sb.append("</li>");
//               }
//           }else{
//               return null;
//           }
//           
//           return sb.toString();
//    }
}
