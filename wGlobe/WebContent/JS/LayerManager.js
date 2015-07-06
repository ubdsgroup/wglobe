/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * @exports LayerManager
 * @version $Id: LayerManager.js 3211 2015-06-18 01:04:07Z tgaskins $
 */
define(function() {
	"use strict";

	/**
	 * Constructs a layer manager for a specified {@link WorldWindow}.
	 * 
	 * @alias LayerManager
	 * @constructor
	 * @classdesc Provides a layer manager to interactively control layer
	 *            visibility for a World Window.
	 * @param {WorldWindow}
	 *            worldWindow The World Window to associated this layer manager
	 *            with.
	 */
	var LayerManager = function(worldWindow) {
		var thisExplorer = this;

		this.wwd = worldWindow;

		this.roundGlobe = this.wwd.globe;

		this.createProjectionList();
		$("#projectionDropdown").find("li").on("click", function(e) {
			thisExplorer.onProjectionClick(e);
		});

		this.geocoder = new WorldWind.NominatimGeocoder();
		$("#slider").on("slideStop", function(e) {
			thisExplorer.onSliderStop($(this));
		});

		$("#prev").on("click", function(e) {
			thisExplorer.onPrevClick($(this));
		});

		$("#play").on("click", function(e) {
			thisExplorer.onPlayClick($(this));
		});

		$("#next").on("click", function(e) {
			thisExplorer.onNextClick($(this));
		});
		

		$("#variablepanel").find("button#popupsubmit").on("click", function(e) {
			thisExplorer.onLayerClick($(this));
		});
		

		//
		// this.wwd.redrawCallbacks.push(function (worldWindow) {
		// thisExplorer.updateVisibilityState(worldWindow);
		// });
	};

	LayerManager.prototype.onSliderStop = function(e) {
		var j = 0;
		for (var i = 0; i < this.wwd.layers.length; i++) {
			var layer = this.wwd.layers[i];
			if (layer.displayName == "Surface Images") {
				j++;
			}
		}

		for (var k = 0; k < j; k++) {
			if (k == e.value) {
				layer.enabled = true;
			} else {
				layer.enabled = false;
			}
		}
		
		this.wwd.redraw();
	};

	
	LayerManager.prototype.onLayerClick = function(layerButton) {

		var variable = $('#variable').val();
		var from = parseInt($('#from').val());
		var to = parseInt($('#to').val());	
		var len =  this.wwd.layers.length;
		
		for (var i = 0; i < len; i++) {
			var layer = this.wwd.layers[i];
			if (layer.displayName === "Surface Images") {
				this.wwd.removeLayer(layer);
			}
		}

		if (from == to) {
			$('#variableError').html("Invalid. Date cannot be the same.");
		} else if (from > to) {
			$('#variableError').html(
					"Invalid. Please try again with different dates.");
		} else {
			for (var i = to; i >= from; i--) {
				$('#variableError').html("");
				var surfaceImage1 = new WorldWind.SurfaceImage(
						new WorldWind.Sector(-90, 90, -180, 180),
						"https://s3-us-west-2.amazonaws.com/wglobe/netcdfImages/"
								+ variable + "/" + variable + "_" + i + ".png");

				var surfaceImageLayer = new WorldWind.RenderableLayer();
				surfaceImageLayer.displayName = "Surface Images";
				surfaceImageLayer.addRenderable(surfaceImage1);
				surfaceImageLayer.opcacity = 0.8;
				this.wwd.addLayer(surfaceImageLayer);
			}

			$("#slider").attr('data-slider-max', to);
			$("#slider").attr('data-slider-min', from);
			$("#slider").attr('data-slider-value', from);

			$("#slider").slider();

			$("#sliderpanel").css("display", "block");
			this.wwd.redraw();
		}

	};

	LayerManager.prototype.onProjectionClick = function(event) {

		var projectionName = event.target.innerText || event.target.innerHTML;
		$("#projectionDropdown").find("button").html(
				projectionName + ' <span class="caret"></span>');

		if (projectionName === "3D") {
			if (!this.roundGlobe) {
				this.roundGlobe = new WorldWind.Globe(
						new WorldWind.EarthElevationModel());
			}

			if (this.wwd.globe !== this.roundGlobe) {
				this.wwd.globe = this.roundGlobe;
			}
		} else {
			if (!this.flatGlobe) {
				this.flatGlobe = new WorldWind.Globe2D();
			}

			if (projectionName === "Equirectangular") {
				this.flatGlobe.projection = new WorldWind.ProjectionEquirectangular();
			} else if (projectionName === "Mercator") {
				this.flatGlobe.projection = new WorldWind.ProjectionMercator();
			} else if (projectionName === "North Polar") {
				this.flatGlobe.projection = new WorldWind.ProjectionPolarEquidistant(
						"North");
			} else if (projectionName === "South Polar") {
				this.flatGlobe.projection = new WorldWind.ProjectionPolarEquidistant(
						"South");
			} else if (projectionName === "North UPS") {
				this.flatGlobe.projection = new WorldWind.ProjectionUPS("North");
			} else if (projectionName === "South UPS") {
				this.flatGlobe.projection = new WorldWind.ProjectionUPS("South");
			}

			if (this.wwd.globe !== this.flatGlobe) {
				this.wwd.globe = this.flatGlobe;
			}
		}

		this.wwd.redraw();
	};

	LayerManager.prototype.createProjectionList = function() {
		var projectionNames = [ "3D", "Equirectangular", "Mercator",
				"North Polar", "South Polar", "North UPS", "South UPS" ];
		var projectionDropdown = $("#projectionDropdown");

		var dropdownButton = $('<button class="btn btn-info btn-block dropdown-toggle" type="button" data-toggle="dropdown">3D<span class="caret"></span></button>');
		projectionDropdown.append(dropdownButton);

		var ulItem = $('<ul class="dropdown-menu">');
		projectionDropdown.append(ulItem);

		for (var i = 0; i < projectionNames.length; i++) {
			var projectionItem = $('<li><a >' + projectionNames[i]
					+ '</a></li>');
			ulItem.append(projectionItem);
		}

		ulItem = $('</ul>');
		projectionDropdown.append(ulItem);
	};

	return LayerManager;
});