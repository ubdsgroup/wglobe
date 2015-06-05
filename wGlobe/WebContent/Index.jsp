<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<link rel="stylesheet"
	href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
<link rel="stylesheet" href="CSS/Site.css">
<script src="http://worldwindserver.net/webworldwind/worldwindlib.js"
	type="text/javascript"></script>
<script src="JS/jquery-1.11.3.min.js" type="text/javascript"></script>
</head>
<body>
<div class="container">
		<div id="heading" class="hidden-xs">
			<h1>Welcome to wGlobe</h1>
		</div>
		<div class="row">
			<div class="col-sm-6" id="globe">
				<div style="position: absolute; top: 50px;">
					<!-- Create a canvas for Web World Wind. -->
					<canvas id="canvasOne" width="500" height="500">
        				Your browser does not support HTML5 Canvas.
   					</canvas>
				</div>
			</div>
			<div class="col-sm-6 paddingtop50">
				<div class="panel panel-default">
					<div class="panel-heading">Please select the following:</div>
					<div class="panel-body">
						<div class="row">
							<div class="col-md-3">Server:</div>
							<div class="col-md-9">
								<select id="servers">
									<option value="http://localhost:8080/wGlobe/NewFile.xml">http://nomads.ncdc.noaa.gov/thredds/catalog.xml</option>
									<option value="http://localhost:8080/wGlobe/NewFile.xml">http://nomads.ncdc.noaa.gov/thredds/catalog.xml</option>
								</select>
								<div id="treecontent"></div>
							</div>
							<div id="data"></div>
						</div>
					</div>
				</div>
				<form action="Index" method="get">
					<p>
						<input type="submit" value="Go!">
					</p>
					<hr>
				</form>
			</div>
		</div>

	</div>
	<script>
    // Register an event listener to be called when the page is loaded.
    window.addEventListener("load", eventWindowLoaded, false);

    // Define the event listener to initialize Web World Wind.
    function eventWindowLoaded() {
        // Create a World Window for the canvas.
        var wwd = new WorldWind.WorldWindow("canvasOne");

        // Add some image layers to the World Window's globe.
        wwd.addLayer(new WorldWind.BMNGOneImageLayer());
        wwd.addLayer(new WorldWind.BingAerialWithLabelsLayer());

        // Add a compass and some view controls to the World Window.
        wwd.addLayer(new WorldWind.CompassLayer());
        wwd.addLayer(new WorldWind.ViewControlsLayer(wwd));

        // Tell the World Window to redraw.
        wwd.redraw();
    }
    
    $("#servers").change(function() {
        //get the selected value
        var selectedValue = this.value;

        //make the ajax call
        $.ajax({
            url: 'Index',
            type: 'POST',
            data: {option : selectedValue},
            success: function(data) {
            	$('#data').html(data);
            }
        });
    });
    
    
    
	</script>

</body>
</html>