define(function () {
    "use strict";

   
    var wGlobeLayerManager = function (worldWindow) {
        var thisExplorer = this;

        this.wwd = worldWindow;

        this.roundGlobe = this.wwd.globe;

//        this.createProjectionList();
////        $("#projectionDropdown").find("li").on("click", function (e) {
////            thisExplorer.onProjectionClick(e);
////        });
//
        this.synchronizeLayerList();
//
        $("#variablepanel").find("button").on("click", function (e) {
            thisExplorer.onLayerClick($(this));
        });
//        
//        $("#searchBox").find("button").on("click", function (e) {
//            thisExplorer.onSearchButton(e);
//        });
//
//        this.geocoder = new WorldWind.NominatimGeocoder();
//        this.goToAnimator = new WorldWind.GoToAnimator(this.wwd);
//        $("#searchText").on("keypress", function (e) {
//            thisExplorer.onSearchTextKeyPress($(this), e);
//        });
        
        //
        //this.wwd.redrawCallbacks.push(function (worldWindow) {
        //    thisExplorer.updateVisibilityState(worldWindow);
        //});
    };

//    LayerManager.prototype.onProjectionClick = function (event) {
//        var projectionName = event.target.innerText || event.target.innerHTML;
//        $("#projectionDropdown").find("button").html(projectionName + ' <span class="caret"></span>');
//
//        if (projectionName === "3D") {
//            if (!this.roundGlobe) {
//                this.roundGlobe = new WorldWind.Globe(new WorldWind.EarthElevationModel());
//            }
//
//            if (this.wwd.globe !== this.roundGlobe) {
//                this.wwd.globe = this.roundGlobe;
//            }
//        } else {
//            if (!this.flatGlobe) {
//                this.flatGlobe = new WorldWind.Globe2D();
//            }
//
//            if (projectionName === "Equirectangular") {
//                this.flatGlobe.projection = new WorldWind.ProjectionEquirectangular();
//            } else if (projectionName === "Mercator") {
//                this.flatGlobe.projection = new WorldWind.ProjectionMercator();
//            } else if (projectionName === "North Polar") {
//                this.flatGlobe.projection = new WorldWind.ProjectionPolarEquidistant("North");
//            } else if (projectionName === "South Polar") {
//                this.flatGlobe.projection = new WorldWind.ProjectionPolarEquidistant("South");
//            } else if (projectionName === "North UPS") {
//                this.flatGlobe.projection = new WorldWind.ProjectionUPS("North");
//            } else if (projectionName === "South UPS") {
//                this.flatGlobe.projection = new WorldWind.ProjectionUPS("South");
//            }
//
//            if (this.wwd.globe !== this.flatGlobe) {
//                this.wwd.globe = this.flatGlobe;
//            }
//        }
//
//        this.wwd.redraw();
//    };

    wGlobeLayerManager.prototype.onLayerClick = function (layerButton) {
    	var variable = $('#variable').val();
    	var from = parseInt($( "#from" ).val());
    	var to = parseInt($( "#to" ).val());
    	
    	if(from == to){
    		$('#variableError').html("Invalid. Date cannot be the same.");
    	}
    	else if(from > to){
    		$('#variableError').html("Invalid. Please try again with different dates.");
    	}
    	else{
    		var surfaceImage1 = new WorldWind.SurfaceImage(new WorldWind.Sector(-90, 90, -180, 180),
            "https://s3-us-west-2.amazonaws.com/wglobe/netcdfImages/" + variable + "/" + variable + "_" + from + ".png");
    		
    		var surfaceImageLayer = new WorldWind.RenderableLayer();
            surfaceImageLayer.displayName = "Surface Images";
            surfaceImageLayer.addRenderable(surfaceImage1);
            this.wwd.addLayer(surfaceImageLayer);
            
    		
    	}
    	
    	
    	
    	
    	//        var layerName = layerButton.text();
//
//        // Update the layer state for the selected layer.
        for (var i = 0, len = this.wwd.layers.length; i < len; i++) {
            var layer = this.wwd.layers[i];
            if (layer.hide) {
                continue;
            }

            if (layer.displayName === layerName) {
                layer.enabled = !layer.enabled;
                if (layer.enabled) {
                    layerButton.addClass("active");
                } else {
                    layerButton.removeClass("active");
                }
                this.wwd.redraw();
            }
        }
    };
//
//    LayerManager.prototype.onMeasureButtonClick = function (layerButton) {
//        var layerName = layerButton.text();
//
//        
//        // Update the layer state for the selected layer.
//        for (var i = 0, len = this.wwd.layers.length; i < len; i++) {
//            var layer = this.wwd.layers[i];
//            if (layer.hide) {
//                continue;
//            }
//
//            if (layer.displayName === layerName) {
//                layer.enabled = !layer.enabled;
//                if (layer.enabled) {
//                    layerButton.addClass("active");
//                } else {
//                    layerButton.removeClass("active");
//                }
//                this.wwd.redraw();
//            }
//        }
//    };
//    
//
    wGlobeLayerManager.prototype.synchronizeLayerList = function () {
//        var layerListItem = $("#variablepanel");
//        alert("sync");
//        layerListItem.find("button").off("click");
//        layerListItem.find("button").remove();
//
//        // Synchronize the displayed layer list with the World Window's layer list.
//        for (var i = 0, len = this.wwd.layers.length; i < len; i++) {
//            var layer = this.wwd.layers[i];
//            if (layer.hide) {
//                continue;
//            }
//            var layerItem = $('<button class="list-group-item btn btn-block">' + layer.displayName + '</button>');
//            layerListItem.append(layerItem);
//
//            if (layer.enabled) {
//                layerItem.addClass("active");
//            } else {
//                layerItem.removeClass("active");
//            }
//            this.wwd.redraw();
//        }
    };
//    //
//    //LayerManager.prototype.updateVisibilityState = function (worldWindow) {
//    //    var layerButtons = $("#layerList").find("button"),
//    //        layers = worldWindow.layers;
//    //
//    //    for (var i = 0; i < layers.length; i++) {
//    //        var layer = layers[i];
//    //        for (var j = 0; j < layerButtons.length; j++) {
//    //            var button = layerButtons[j];
//    //
//    //            if (layer.displayName === button.innerText) {
//    //                if (layer.inCurrentFrame) {
//    //                    button.innerHTML = "<em>" + layer.displayName + "</em>";
//    //                } else {
//    //                    button.innerHTML = layer.displayName;
//    //                }
//    //            }
//    //        }
//    //    }
//    //};
//
    wGlobeLayerManager.prototype.createProjectionList = function () {
    	var projectionNames = [
            "3D",
            "Equirectangular",
            "Mercator",
            "North Polar",
            "South Polar",
            "North UPS",
            "South UPS"
        ];
        var projectionDropdown = $("#projectionDropdown");

        var dropdownButton = $('<button class="btn btn-info btn-block dropdown-toggle" type="button" data-toggle="dropdown">3D<span class="caret"></span></button>');
        projectionDropdown.append(dropdownButton);

        var ulItem = $('<ul class="dropdown-menu">');
        projectionDropdown.append(ulItem);

        for (var i = 0; i < projectionNames.length; i++) {
        	var projectionItem = $('<li><a >' + projectionNames[i] + '</a></li>');
            ulItem.append(projectionItem);
        }

        ulItem = $('</ul>');
        projectionDropdown.append(ulItem);
    };
//
//    LayerManager.prototype.onSearchButton = function (event) {
//        this.performSearch($("#searchText")[0].value);
//    };
//
//
//    LayerManager.prototype.onSearchTextKeyPress = function (searchInput, event) {
//        if (event.keyCode === 13) {
//            searchInput.blur();
//            this.performSearch($("#searchText")[0].value);
//        }
//    };
//


    return wGlobeLayerManager;
});