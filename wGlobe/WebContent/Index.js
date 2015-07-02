/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */
/**
 * Illustrates how to display and pick SurfaceImages.
 *
 * @version $Id: SurfaceImage.js 3121 2015-05-28 02:42:13Z tgaskins $
 * ['./src/WorldWind',
        './JS/LayerManager',
        './JS/CoordinateController',
        './JS/wGlobeLayerManager']
 */

requirejs(['./src/WorldWind',
           './JS/LayerManager',
           './JS/CoordinateController',
           './JS/wGlobeLayerManager'],
        function (ww,
                LayerManager,
                CoordinateController,
                wGlobeLayerManager) {
          "use strict";

          // Tell World Wind to log only warnings and errors.
          WorldWind.Logger.setLoggingLevel(WorldWind.Logger.LEVEL_WARNING);

          // Create the World Window.
          var wwd = new WorldWind.WorldWindow("canvasOne");

          /**
           * Added imagery layers.
           */
          var layers = [
              {layer: new WorldWind.BMNGLayer(), enabled: true},
              {layer: new WorldWind.BMNGLandsatLayer(), enabled: false},
              {layer: new WorldWind.BingAerialWithLabelsLayer(null), enabled: true},
              {layer: new WorldWind.OpenStreetMapImageLayer(null), enabled: false},
              {layer: new WorldWind.CompassLayer(), enabled: true},
              {layer: new WorldWind.ViewControlsLayer(wwd), enabled: true}
          ];

          for (var l = 0; l < layers.length; l++) {
              layers[l].layer.enabled = layers[l].enabled;
              wwd.addLayer(layers[l].layer);
          }

          // Create a surface image using a static image.
//          var surfaceImage1 = new WorldWind.SurfaceImage(new WorldWind.Sector(-90, 90, -180, 180),
//          "https://s3-us-west-2.amazonaws.com/wglobe/netcdfImages/air/air_0.png");
//
//          // Add the surface images to a layer and the layer to the World Window's layer list.
//          var surfaceImageLayer = new WorldWind.RenderableLayer();
//          surfaceImageLayer.displayName = "Surface Images";
//          surfaceImageLayer.addRenderable(surfaceImage1);
//          wwd.addLayer(surfaceImageLayer);

          // Draw the World Window for the first time.
          wwd.redraw();

          // Create a layer manager for controlling layer visibility.
          var layerManger = new LayerManager(wwd);
          
//          var wGlobeLayerManager = new wGlobeLayerManager(wwd);

          // Create a coordinate controller to update the coordinate overlay elements.
          var coordinateController = new CoordinateController(wwd);

          // Now set up to handle picking.

          // The common pick-handling function.
          var handlePick = function (o) {
              // The input argument is either an Event or a TapRecognizer. Both have the same properties for determining
              // the mouse or tap location.
              var x = o.clientX,
                  y = o.clientY;

              // Perform the pick. Must first convert from window coordinates to canvas coordinates, which are
              // relative to the upper left corner of the canvas rather than the upper left corner of the page.
              var pickList = wwd.pick(wwd.canvasCoordinates(x, y));

              if (pickList.objects.length > 0) {
                  for (var p = 0; p < pickList.objects.length; p++) {
                      if (pickList.objects[p].userObject instanceof WorldWind.SurfaceImage) {
                          console.log("Surface image picked");
                      }
                  }
              }
          };

          // Listen for mouse moves and highlight the placemarks that the cursor rolls over.
          wwd.addEventListener("mousemove", handlePick);

          // Listen for taps on mobile devices and highlight the placemarks that the user taps.
          var tapRecognizer = new WorldWind.TapRecognizer(wwd, handlePick);
      });