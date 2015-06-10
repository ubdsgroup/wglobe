/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>Alias</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLAlias.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class KMLAlias extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLAlias(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getTargetHref()
    {
        return (String) this.getField("targetHref");
    }

    public String getSourceRef()
    {
        return (String) this.getField("sourceHref");
    }
}
