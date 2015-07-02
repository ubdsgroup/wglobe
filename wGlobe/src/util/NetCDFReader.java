package util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.MAMath;
import ucar.nc2.*;
import ucar.nc2.dataset.*;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.Calendar;
import ucar.nc2.time.CalendarDate;
import ucar.unidata.geoloc.LatLonRect;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import util.NetcdfColorMap;

public class NetCDFReader {

	public static NetcdfDataset dataset;
	public static GridDataset gridDataset;

	public String getTimebyVariable(String variable) {
		
		String htmlstr = "";
		
		try {
			S3Uploader s3 = new S3Uploader();
			boolean check = s3.checkKeyExistbyVariable(variable);

			if (check) {
				htmlstr = getTimeDimensionbyVariable(variable);
			} else {
				ArrayList<File> fileList = new ArrayList<File>();
				GeoGrid grid = gridDataset.findGridByName(variable);
				int numLatitudes = grid.getYDimension().getLength();
				int numLongitudes = grid.getXDimension().getLength();
				for (int t = 0; t < grid.getTimeDimension().getLength(); t++) {
					ArrayFloat.D2 array = (ArrayFloat.D2) read(grid, t);
					// (grid.readDataSlice(t,
					// -1, -1, -1)).flip(0);
					MAMath.MinMax minMax = grid.getMinMaxSkipMissingData(array);
					// save this as a geotiff
					NetcdfColorMap ncColormap = createColorMap(minMax, variable);

					BufferedImage bufferedImage = new BufferedImage(
							numLongitudes, numLatitudes,
							BufferedImage.TYPE_INT_ARGB);
					int[] pixelArray = ((DataBufferInt) bufferedImage
							.getRaster().getDataBuffer()).getData();
					int cnt = 0;
					for (int i = 0; i < numLatitudes; i++) {
						for (int j = 0; j < numLongitudes; j++) {
							float key = 0;
							try {
								key = array.get(i, j);
							} catch (ArrayIndexOutOfBoundsException e1) {
								e1.printStackTrace();
							}
							if (!Float.isNaN(key) && !Float.isInfinite(key))
								pixelArray[cnt] = ncColormap.getColor(
										(double) key).getRGB();
							cnt++;
						}
					}
					String filename = variable + "_" + t + ".png";
					File f = new File(filename);
					ImageIO.write(bufferedImage, "PNG", f);

					fileList.add(f);
					System.out.println("Image generated for time slice #" + t);

				}

//				ArrayList<String> filestr = s3.putObjectwithFileList(variable,
//						fileList);
//				htmlstr = getTimeDimensionbyVariable(variable,filestr);
			}
		} catch (Exception e) {
			System.err.println("Not found" + e);
			e.printStackTrace();
		}
		
		return htmlstr;

	}

	public Array read(GeoGrid grid, int t) throws IOException {

		boolean xFlip = isAxisIncreasing(grid.getCoordinateSystem(), grid
				.getCoordinateSystem().getXHorizAxis());
		boolean yFlip = isAxisIncreasing(grid.getCoordinateSystem(), grid
				.getCoordinateSystem().getYHorizAxis());
		Array array = grid.readDataSlice(t, -1, -1, -1);
		// check if this array needs to be flipped
		if (!xFlip)
			array = array.flip(grid.getXDimensionIndex());
		if (yFlip)
			array = array.flip(grid.getYDimensionIndex());
		return array;
	}

	public static boolean isAxisIncreasing(GridCoordSystem gridCoordSystem,
			CoordinateAxis axis) {
		if (axis instanceof CoordinateAxis1D) {
			CoordinateAxis1D axis1D = (CoordinateAxis1D) axis;
			return axis1D.getCoordValues().length <= 1
					|| axis1D.getCoordValue(0) < axis1D.getCoordValue(1);
		} else if (axis instanceof CoordinateAxis2D) {
			CoordinateAxis2D axis2D = (CoordinateAxis2D) axis;
			return ((CoordinateAxis2D) axis).getCoordValues().length <= 1
					|| axis2D.getCoordValue(0, 0) < axis2D.getCoordValue(1, 1);
		} else {
			return false;
		}
	}

	private static void writeImageToFile(Sector sector, BufferedImage image,
			File gtFile) throws IOException {
		AVList params = new AVListImpl();

		params.setValue(AVKey.SECTOR, sector);
		params.setValue(AVKey.COORDINATE_SYSTEM,
				AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
		params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
		params.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

		GeotiffWriter writer = new GeotiffWriter(gtFile);
		try {
			writer.write(BufferedImageRaster.wrapAsGeoreferencedRaster(image,
					params));
		} finally {
			writer.close();
		}
	}

	private static NetcdfColorMap createColorMap(MAMath.MinMax minMax,
			String variableName) {
		NetcdfColorMap colormap;
		try {
			colormap = NetcdfColorMap.createColorMap("rgb_" + variableName,
					(float) minMax.min, (float) minMax.max,
					NetcdfColorMap.DEFAULT_COLORMAP_LOCATION);
			return colormap;
		} catch (XMLStreamException e) {
			Util.getLogger().severe("Could not create color map");
			return null;
		}
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

			if (extension.equals("grb2")) {
				gridDataset = new GridDataset(dataset);
				List<GridDatatype> lGrid = gridDataset.getGrids();
				for (int i = 0; i < lGrid.size(); i++) {
					String name = lGrid.get(i).getName();
					out += "<li><a class=\"vars\">"
							+ name + "</a></li>";
				}

			} else if (extension.endsWith("nc")) {
				gridDataset = GridDataset.open(texturl);
				List<GridDatatype> lGrid = gridDataset.getGrids();
				for (int i = 0; i < lGrid.size(); i++) {
					String name = lGrid.get(i).getName();
					out += "<li><a class=\"vars\">"
							+ name + "</a></li>";
				}

			}

			out += "</ul></li></ul>";
			nFile.close();
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

	public String getTimeDimensionbyVariable(String variable) {
		String html = "<div class=\"col-md-12\"><div class=\"col-md-12\"><div id=\"time\" class=\"col-md-6\""
				+ "style=\"display: block\"><input type=\"hidden\" id=\"variable\" name=\"variable\" value=\"" + variable + "\"><fieldset class=\"scheduler-border\"><legend class=\""
				+ "scheduler-border\">Time</legend><div class=\"col-md-6\"><div class=\"control-group\"><label class=\"control-label input-label\" "
				+ "for=\"minTime\">Min :</label><div class=\"controls\"><select id=\"from\">";

		GeoGrid grid = gridDataset.findGridByName(variable);
		GridCoordSystem coordSystem = grid.getCoordinateSystem();
		if (grid.getTimeDimensionIndex() != -1) {
			CoordinateAxis axis = coordSystem.getTimeAxis();
			CoordinateAxis1DTime taxis;
			if (axis instanceof CoordinateAxis1D) {
				taxis = coordSystem.getTimeAxis1D();
			} else {
				taxis = coordSystem.getTimeAxisForRun(0);
			}
			if (taxis != null) {
				SimpleDateFormat dateformat = new SimpleDateFormat(
						"MM/dd/yyyy hh:mm a");
				List<CalendarDate> tDimension = taxis.getCalendarDates();
				for (int i = 0; i < tDimension.size(); i++) {
					Date d = tDimension.get(i).toDate();
					html += "<option value=\"" + i + "\">"
							+ dateformat.format(d).toString();
					html += "</option>";
				}

				html += "</select></div></div></div><div class=\"col-md-6\"><div class=\"control-group\"><label class=\"control-label input-label\" "
						+ "for=\"maxTime\">Max:</label><div class=\"controls\"><select id=\"to\">";

				for (int i = 0; i < tDimension.size(); i++) {
					Date d = tDimension.get(i).toDate();
					html += "<option value=\"" + i + "\">"
							+ dateformat.format(d).toString();
					html += "</option>";
				}
				html += "</select></div></div></div></fieldset></div>";
				//
				// LatLonRect boundingBox = coordSystem.getLatLonBoundingBox();
				//
				// double lllat = boundingBox.getLowerLeftPoint().getLatitude();
				// double lllon =
				// boundingBox.getLowerLeftPoint().getLongitude();
				// double urlat =
				// boundingBox.getUpperRightPoint().getLatitude();
				// double urlon =
				// boundingBox.getUpperRightPoint().getLongitude();
				//
				// html +=
				// "<div id=\"boundingbox\" class=\"col-md-6\" style=\"display: block\"><fieldset class=\"scheduler-border\"><legend class=\""
				// +
				// "scheduler-border\">Bounding Box</legend><div id=\"bounding\" class=\"control-group\"><input type=\"hidden\" name=\""
				// + "variable\" value=\"" + variable +
				// "\"><div class=\"col-md-6\"><div class=\"control-group\"><label class=\"control-label input-label\" for=\"startTime"
				// +
				// "\">Lower	Left :</label><div class=\"controls bootstrap-timepicker\"><input type=\"text\" class=\"\" type=\"text\" id=\"llLat"
				// + "\" name=\"llLat\" value=\""+ lllat +
				// "\" /> <input type=\"text\" class=\"datetime\" type=\"text\" id=\"llLong\" name=\"llLong"
				// + "\" value=\""+ lllon
				// +"\" /></div></div></div><div class=\"col-md-6\"><div class=\"control-group\"><label class=\"control-label "
				// +
				// "input-label\" for=\"startTime\">Upper Right :</label><div class=\"controls bootstrap-timepicker\"><input type=\"text\" class=\""
				// +
				// "datetime\" type=\"text\" id=\"urlat\" name=\"urlat\" value=\""+
				// urlat
				// +"\" /> <input type=\"text\" class=\"datetime\" type=\""
				// + "text\" id=\"urlong\" name=\"urlong\"	value=\""+ urlon
				// +"\" /></div></div></div></div></fieldset></div></div></div><div class=\"col-md-1\"></div>";
				html += "</div></div>";
				System.out.println(html);
			}
			//
			//

			// // String html =
			// "<div class=\"col-md-1\"></div><div class=\"col-md-6\"><div class=\"col-md-12\"><div class=\"col-md-12\">"
			// +
			// "<div id=\"time\" class=\"col-md-12 well\" style=\"display: none\">"
			// +
			// "<input type=\"hidden\" name=\"variable\"><fieldset class=\"scheduler-border\"><legend class"
			// +
			// "=\"scheduler-border\">Time</legend><div class=\"col-md-6\"><div class=\"control-group\">"
			// +
			// "<label class=\"control-label input-label\" for=\"minTime\">Min :</label>	<div class=\"controls\"><select id=\"from\">";

		}
		return html;
	}
}
