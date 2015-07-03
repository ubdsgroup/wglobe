package util;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.data.BufferedImageRaster;
import gov.nasa.worldwind.data.DataRaster;
import gov.nasa.worldwind.data.DataRasterReader;
import gov.nasa.worldwind.data.DataRasterReaderFactory;
import gov.nasa.worldwind.formats.tiff.GeotiffWriter;
import gov.nasa.worldwind.geom.Sector;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class test {

	public static void main(String[] args) {
		String imagepath = "/Users/ERAN/Desktop/wGlobe/tesdata/craterlake-imagery-30m.tif";
		importTiff(imagepath);
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

	public static void importTiff(String imagepath) {
		try {

			// Read the data and save it in a temp file.
			// File sourceFile = ExampleUtil.saveResourceToTempFile(imagepath,
			// ".tif");
			File sourceFile = new File(imagepath);
			// Create a raster reader to read this type of file. The reader is
			// created from the currently
			// configured factory. The factory class is specified in the
			// Configuration, and a different one can be
			// specified there.
			DataRasterReaderFactory readerFactory = (DataRasterReaderFactory) WorldWind
					.createConfigurationComponent(AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
			DataRasterReader reader = readerFactory.findReaderFor(sourceFile,
					null);

			// Before reading the raster, verify that the file contains imagery.
			AVList metadata = reader.readMetadata(sourceFile, null);
			if (metadata == null
					|| !AVKey.IMAGE.equals(metadata
							.getStringValue(AVKey.PIXEL_FORMAT)))
				throw new Exception("Not an image file.");

			// Read the file into the raster. read() returns potentially several
			// rasters if there are multiple
			// files, but in this case there is only one so just use the first
			// element of the returned array.
			DataRaster[] rasters = reader.read(sourceFile, null);
			if (rasters == null || rasters.length == 0)
				throw new Exception("Can't read the image file.");

			DataRaster raster = rasters[0];

			// Determine the sector covered by the image. This information is in
			// the GeoTIFF file or auxiliary
			// files associated with the image file.
			final Sector sector = (Sector) raster.getValue(AVKey.SECTOR);
			if (sector == null)
				throw new Exception("No location specified with image.");

			// Request a sub-raster that contains the whole image. This step is
			// necessary because only sub-rasters
			// are reprojected (if necessary); primary rasters are not.
			int width = raster.getWidth();
			int height = raster.getHeight();

			// getSubRaster() returns a sub-raster of the size specified by
			// width and height for the area indicated
			// by a sector. The width, height and sector need not be the full
			// width, height and sector of the data,
			// but we use the full values of those here because we know the full
			// size isn't huge. If it were huge
			// it would be best to get only sub-regions as needed or install it
			// as a tiled image layer rather than
			// merely import it.
			DataRaster subRaster = raster.getSubRaster(width, height, sector,
					null);

			// Tne primary raster can be disposed now that we have a sub-raster.
			// Disposal won't affect the
			// sub-raster.
			raster.dispose();

			// Verify that the sub-raster can create a BufferedImage, then
			// create one.
			if (!(subRaster instanceof BufferedImageRaster))
				throw new Exception("Cannot get BufferedImage.");
			BufferedImage image = ((BufferedImageRaster) subRaster)
					.getBufferedImage();

			writeImageToFile(sector, image, new File(
					"/Users/ERAN/Desktop/wGlobe/output/output.tiff"));
			// The sub-raster can now be disposed. Disposal won't affect the
			// BufferedImage.
			subRaster.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
