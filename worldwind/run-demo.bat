REM Windows Batch file for Running a WorldWind Demo
REM $Id: run-demo.bat 6 2011-07-17 16:20:15Z dcollins $

@echo Running %1
java -Xmx512m -Dsun.java2d.noddraw=true -classpath .\src;.\classes;.\worldwind.jar;.\worldwindx.jar;.\jogl.jar;.\gluegen-rt.jar;.\gdal.jar %*
