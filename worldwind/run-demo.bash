#!/bin/bash
# Run a WorldWind Demo
# $Id: run-demo.bash 1 2011-07-16 23:22:47Z dcollins $

echo Running $1
java -Xmx512m -Dsun.java2d.noddraw=true -classpath ./src:./classes:./worldwind.jar:./worldwindx.jar:./jogl.jar:./gluegen-rt.jar:./gdal.jar $*
