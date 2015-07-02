/*
 * Copyright (c) 2011. This product includes software produced by UT-Battelle, LLC under
 * Contract No. DE-AC05-00OR22725 with the Department of Energy.
 *
 * UT-Battelle, LLC and the government make no representations and disclaim all warranties,
 * both expressed and implied. There are not expressed or implied warranties of
 * merchantability or fitness for a particular purpose, or that the use of the software
 * will not infringe any patent, copyright, trademark, or other proprietary rights, or that
 * the software will accomplish the intended results or that the software or its use will
 * not result in injury or damage. The user assumes responsibility for all liabilities,
 * penalties, fines, claims, causes of action, and costs and expenses, caused by,
 * resulting from, or arising out of, in whole or in part the use, storage or
 * disposal of the software.
 */

package util;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.util.xml.BasicXMLEventParserContext;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.awt.*;
import java.io.File;
import java.util.*;

/**
 * @author Varun Chandola
 * @version $Id$
 */
public class NetcdfColorMap {
	private String name;
    private NavigableMap<Float, Color> ct;
    private String shaderFileName;
    private boolean shaderCreated;
    private Number minValue;
    private Number maxValue;
    public static String DEFAULT_COLORMAP_LOCATION = "config/fullcolorvalues.xml";

    public NetcdfColorMap(String name,Number minValue,Number maxValue) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.ct = new TreeMap<Float, Color>();
        this.shaderFileName = null;
        this.shaderCreated = false;
    }// end constructor

    /**
     * Adds a new {@link Color} entry to the {@link TreeMap} for a given key.
     *
     * @param key   Floating point key at which the new entry is added.
     * @param color The new {@link Color} entry.
     */
    public void addEntry(float key, Color color) {
        ct.put(key, color);
    }// end function

    /**
     * Returns the {@link Color} entry for the key. If exact match is not found then the color is interpolated between
     * the two nearest {@link Color} entries (up and down).
     *
     * @param key The floating point key.
     * @return The interpolated {@link Color} value.
     */
    public Color getColor(Number key) {

        Color upColor = ct.ceilingEntry(key.floatValue()).getValue();
        Color downColor = ct.floorEntry(key.floatValue()).getValue();
        float fraction = 0;
        if (!ct.ceilingKey(key.floatValue()).equals(ct.floorKey(key.floatValue())))
            fraction = (key.floatValue() - ct.floorKey(key.floatValue())) / (ct.ceilingKey(key.floatValue()) - ct.floorKey(key.floatValue()));

        return new Color((int) linInterpolate(fraction, downColor.getRed(), upColor.getRed()),
                (int) linInterpolate(fraction, downColor.getGreen(), upColor.getGreen()),
                (int) linInterpolate(fraction, downColor.getBlue(), upColor.getBlue()));
    }// end function

   public Color getMinColor()
   {
       return this.getColor(this.minValue);
   }

    public Color getMaxColor()
    {
        return this.getColor(this.maxValue);
    }

    /**
     * Returns the linear interpolation by a fraction between two values.
     *
     * @param f Floating point fraction by which to interpolate
     * @param a First floating point bound (upper or lower)
     * @param b Second floating point bound (upper or lower)
     * @return The interpolated floating point value.
     */
    public float linInterpolate(float f, float a, float b) {
        if (a > b)
            return (a - f * (a - b));
        if (a < b)
            return (a + f * (b - a));
        return a;
    }

    /**
     * Gets the current size of the {@link java.util.TreeMap}
     *
     * @return Size of the {@link java.util.TreeMap}
     */
    public int size() {
        return ct.size();
    }

    /**
     * Static method used to create a new <code>NCColormap</code> object. The method creates a
     * colormap for values ranging between specified lower and upper limits. The color information is read from a
     * specified XML color file.
     *
     * @param name     Name for the color map
     * @param low      Lower floating point limit of the range of values.
     * @param high     Upper floating point limit of the range of values.
     * @param filename XML file containing color information. See {@link NetcdfColorMap#parse(String)} for XML syntax.
     * @return The new {@link NetcdfColorMap} object.
     * @throws javax.xml.stream.XMLStreamException
     */
    public static NetcdfColorMap createColorMap(String name, float low, float high, String filename) throws XMLStreamException
    {
        NetcdfColorMap colormap = new NetcdfColorMap(name,low,high);
        ArrayList<int[]> arrayList = colormap.parse(filename);
        int numcolors = arrayList.size() - 2;
        float range = high - low;
        float width = range / (numcolors - 3);
        float value;
        int c = 0;
        if (low > 0)
            value = low / 2;
        else if (low == 0)
            value = -1;
        else
            value = 2 * low;
        int[] color = arrayList.get(0);
        colormap.addEntry(value, new Color(color[0], color[1], color[2]));
        for (int i = 0; i < numcolors; i++) {
            value = Math.min(low + c * width, high);
            c++;
            color = arrayList.get(i + 1);
            colormap.addEntry(value, new Color(color[0], color[1], color[2]));
        }
        if (high > 0) value = high * 2;
        else if (high == 0) value = 1;
        else value = high / 2;
        color = arrayList.get(arrayList.size() - 1);
        colormap.addEntry(value, new Color(color[0], color[1], color[2]));
        return colormap;
    }

    /**
     * Parses an XML file that contains color information. The colors are read in the order they are specified in the
     * XML file. The default color document is <code>config/fullcolorvalues.xml</code>. This can be changed by setting
     * the <code>gov.ornl.iglobe.colormapfile</code> property to a different file.
     * <br/>
     * The XML document is enclosed within <code>colors</code> tag. Each color is specified as a <code>color</code> XML
     * element. The <code>color</code> element needs to have three attributes: <code>red</code>, <code>blue</code>,
     * and <code>green</code> with integer values between 0 and 255.
     *
     * @param filename File name containing the colormap information
     * @return {@link ArrayList} of consisting of three length integer arrays, each array specifies red, blue, and green
     *         component of the array.
     * @throws XMLStreamException
     */
    public ArrayList<int[]> parse(String filename) throws XMLStreamException {
        ArrayList<int[]> arrayList = new ArrayList<int[]>();
        XMLEventReader eventReader = WWXML.openEventReader(filename);
        XMLEventParserContext parserContext = new BasicXMLEventParserContext(eventReader);
        QName COLORS = new QName("", "colors");
        QName COLOR = new QName("", "color");
        QName RED = new QName("", "red");
        QName BLUE = new QName("", "blue");
        QName GREEN = new QName("", "green");

        for (XMLEvent event = parserContext.nextEvent(); parserContext.hasNext(); event = parserContext
                .nextEvent()) {
            if (parserContext.isStartElement(event, COLORS)) {
                for (XMLEvent event1 = parserContext.nextEvent(); !parserContext.isEndElement(event1,
                        event); event1 = parserContext.nextEvent()) {
                    if (parserContext.isStartElement(event1, COLOR)) {
                        int[] color = new int[3];

                        Iterator iter = event1.asStartElement().getAttributes();
                        while (iter.hasNext()) {
                            Attribute attribute = (Attribute) iter.next();
                            if (attribute.getName().equals(RED))
                                color[0] = Integer.parseInt(attribute.getValue());
                            if (attribute.getName().equals(BLUE))
                                color[1] = Integer.parseInt(attribute.getValue());
                            if (attribute.getName().equals(GREEN))
                                color[2] = Integer.parseInt(attribute.getValue());
                        }
                        arrayList.add(color);
                    }
                }
            }
        }

        return arrayList;
    }

    public String getName() {
        return this.name;
    }

    public Number getMinValue()
    {
        return this.minValue;
    }

    public Number getMaxValue()
    {
        return this.maxValue;
    }
    public void createFragmentShaderFile()
    {
        this.shaderFileName = WorldWind.getDataFileStore().getWriteLocation().getAbsolutePath() + File.separator + name+".glsl";
        String data = "uniform sampler2D tile_image;\n" +
                "uniform float HighClip = "+this.maxValue+";\n" +
                "uniform float LowClip = "+this.minValue+";\n\n" +
                "void main(void)\n" +
                "{\n" +
                "\t vec4 hsi = texture2D(tile_image, gl_TexCoord[0].st);\n" +
                "\t if(hsi.r < LowClip || hsi.r > HighClip) discard;\n" +
                "\tvec3 rgb;\n";

        Object [] setIt = this.ct.keySet().toArray();
        for(int i = 0; i < setIt.length-1; i++)
        {

            float f = (Float) setIt[i];
            float f1 = (Float) setIt[i+1];
            data += "\tif(hsi.r > "+f+" && hsi.r < "+ f1 +") rgb = vec3("+this.ct.get(f).getRed()+","+this.ct.get(f).getGreen()+","+this.ct.get(f).getBlue()+")\n";

        }

        data += "\tgl_FragColor = vec4(rgb, 0);\n";
        data += "}\n";

        WWIO.writeTextFile(data, new File(this.shaderFileName));
        shaderCreated = true;


    }

    public boolean isShaderCreated()
    {
        return shaderCreated;
    }

    public String getShaderFileName()
    {
        return this.shaderFileName;
    }
}
