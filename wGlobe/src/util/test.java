package util;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import ucar.ma2.*;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonRect;

import javax.xml.stream.XMLStreamException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

public class test {

	 public static void main(String [] args)
	    {
	        String filename = "/Users/ERAN/Downloads/air.mon.mean.nc";
	        NetcdfDataset dataset;
	        Set<NetcdfDataset.Enhance> enhanceSet = new HashSet<NetcdfDataset.Enhance>();
	        enhanceSet.add(NetcdfDataset.Enhance.CoordSystems);
	        enhanceSet.add(NetcdfDataset.Enhance.ScaleMissingDefer);
	        String paramName = "air";
	        String dirname = "/Users/ERAN/Desktop/";
	        ArrayList<File> fileList = new ArrayList<File>();
	        try {
	            dataset = NetcdfDataset.openDataset(filename, enhanceSet, -1, null, null);
	            GridDataset gridDataset = new GridDataset(dataset);
	            GeoGrid grid = gridDataset.findGridByName(paramName);
	            int numLatitudes = grid.getYDimension().getLength();
	            int numLongitudes = grid.getXDimension().getLength();
	            for(int t = 0; t < grid.getTimeDimension().getLength();t++) {
	                ArrayFloat.D2 array = (ArrayFloat.D2) (grid.readDataSlice(t, -1, -1, -1)).flip(0);
	                MAMath.MinMax minMax = grid.getMinMaxSkipMissingData(array);
	                //save this as a geotiff
	                NetcdfColorMap ncColormap = createColorMap(minMax, paramName);

	                BufferedImage bufferedImage = new BufferedImage(numLongitudes, numLatitudes, BufferedImage.TYPE_INT_ARGB);
	                int[] pixelArray = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
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
	                            pixelArray[cnt] = ncColormap.getColor((double) key).getRGB();
	                        cnt++;
	                    }
	                }
	                GridCoordSys gcs = (GridCoordSys) grid.getCoordinateSystem();
	                LatLonRect rect = gcs.getLatLonBoundingBox();
	                Sector sector = new Sector(Angle.fromDegrees(rect.getLatMin()), Angle.fromDegrees(rect.getLatMax()),
	                        Angle.fromDegrees(rect.getLonMin()), Angle.fromDegrees(rect.getLonMax()));

	                String filepart = paramName+"_"+t+".tiff";
	                File f = new File(dirname+filepart);
	                fileList.add(f);
	                writeImageToFile(sector, bufferedImage, f);
	                System.out.println("Image generated for time slice #"+t);
	            }
	            System.out.println("Zipping archive");
	            writeZipFile(new File(dirname),fileList);
	        }
	        catch (Exception e)
	        {
	            System.err.println("Not found"+e);
	            e.printStackTrace();
	        }
	    }

	    private static NetcdfColorMap createColorMap(MAMath.MinMax minMax, String variableName) {
	        NetcdfColorMap colormap;
	        try {
	            colormap = NetcdfColorMap.createColorMap("rgb_"+variableName,(float) minMax.min, (float) minMax.max,NetcdfColorMap.DEFAULT_COLORMAP_LOCATION);
	            return colormap;
	        } catch (XMLStreamException e) {
	            Util.getLogger().severe("Could not create color map");
	            return null;
	        }
	    }

	    public static void writeZipFile(File directoryToZip, List<File> fileList) {

	        try {
	            FileOutputStream fos = new FileOutputStream(directoryToZip.getName() + ".zip");
	            ZipOutputStream zos = new ZipOutputStream(fos);

	            for (File file : fileList) {
	                if (!file.isDirectory()) { // we only zip files, not directories
	                    addToZip(directoryToZip, file, zos);
	                }
	            }

	            zos.close();
	            fos.close();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
	            IOException {

	        FileInputStream fis = new FileInputStream(file);

	        // we want the zipEntry's path to be a relative path that is relative
	        // to the directory being zipped, so chop off the rest of the path
	        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
	                file.getCanonicalPath().length());
	        System.out.println("Writing '" + zipFilePath + "' to zip file");
	        ZipEntry zipEntry = new ZipEntry(zipFilePath);
	        zos.putNextEntry(zipEntry);

	        byte[] bytes = new byte[1024];
	        int length;
	        while ((length = fis.read(bytes)) >= 0) {
	            zos.write(bytes, 0, length);
	        }

	        zos.closeEntry();
	        fis.close();
	    }


	    private static void writeImageToFile(Sector sector, BufferedImage image, File gtFile)
	            throws IOException
	    {
	        AVList params = new AVListImpl();

	        params.setValue(AVKey.SECTOR, sector);
	        params.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);
	        params.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
	        params.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

	        GeotiffWriter writer = new GeotiffWriter(gtFile);
	        try
	        {
	            writer.write(BufferedImageRaster.wrapAsGeoreferencedRaster(image, params));
	        }
	        finally
	        {
	            writer.close();
	        }
	    }

}
