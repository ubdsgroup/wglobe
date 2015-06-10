/*
Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.servers.wms.security;

/**
 * @author Lado Garakanidze
 * @version $Id: SecurityManager.java 1 2011-07-16 23:22:47Z dcollins $
 */

public interface SecurityManager
{
    public boolean allow( Object o ) throws SecurityException;
}
