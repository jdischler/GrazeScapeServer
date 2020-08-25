DSS.utils.addStyle('.popup-eye { opacity: 0.9; overflow: visible!important;background-color: #f8f7ef; border-radius: .5rem; border: 1px solid #999; box-shadow: 0 6px 6px rgba(0,0,0,0.5);pointer-events:none; }')
DSS.utils.addStyle('.popup-eye:after { transform: rotate(-45deg); overflow: visible!important; display: block; position: absolute; bottom: -0.32rem; left: calc(100px - 0.32rem); content: ""; background-color: #f8f7ef; width: 0.5rem; height: 0.5rem; border-left: 1px solid #999; border-bottom: 1px solid #999; box-shadow: -6px 6px 6px rgba(0,0,0,0.5) }')

DSS.utils.addStyle('path.boundary { fill: #ff00001f; stroke: red;}');
DSS.utils.addStyle('path.boundary:hover { fill: #ff00005f; stroke: red;}');

/*
// Grossly publicly shared OpenLayers access points...
DSS.map
DSS.mouseMoveFunction(event)
DSS.mapClickFunction(event)

DSS.layer {.bingAerial, .bingRoad, .osm, .watershed, .hillshade, .fields, .farms, .markers, .mask}

//-----------------------------
DSS.dragBox
DSS.dragBoxFunction(dragBox)

DSS.markerStyleFunction(feature)

// Generally field edit hijacks
//-----------------------------
DSS.fieldStyleFunction(feature, resolution)

DSS.draw
DSS.drawEndEvent(evt, feature, target, type)

DSS.modify
DSS.modifyEndEvent(evt, features, target, type) // note that features is plural
DSS.snap

//-----------------------------
DSS.selectionTool
DSS.selectionFunction(event)

//-----------------------------
DSS.popupOverlay
DSS.popupContainer

//-----------------------------
*/

//------------------------------------------------------------------------------
Ext.define('DSS.map.Main', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',//Component',
	alias: 'widget.main_map',
	
	style: 'background-color: rgb(75,80,60)',
	
	BING_KEY: 'Au_ohpV01b_LnpbMExJmpmUnamgty20v7Cpl1GvNmwzZPOezhtzegaNM0MNaSPoa',
	OSM_KEY: '8UmAwNixnmOYWs2lqUpR',
	
	requires: [
		'DSS.map.DrawAndModify',
		'DSS.map.BoxModel',
		'DSS.map.LayerMenu',
		'DSS.map.RotationLayer'
	],
	
	layout: 'border',
	listeners: {
		afterrender: function(self) {
			self.instantiateMap()
		},
		resize: function(self, w, h) {
		//	let mapSize = self.down('#ol_map').getSize();
		//	self.map.setSize([mapSize.width, mapSize.height]);
		}
	},
		
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			items: [{
				xtype: 'component',
				region: 'center',
				id: 'ol_map',
				listeners: {
					resize: function(self, w, h) {
						me.map.setSize([w,h]);
						DSS.MapState.mapResize();
						
						AppEvents.triggerEvent('map_resize');
/*						let cs = Ext.getCmp('crap-state');
						let om = Ext.getCmp('ol_map');
						if (cs && om) {
							cs.setX(om.getX() + (om.getWidth() - cs.getWidth()) * 0.5);
						}*/
					}
				}
			},{
				xtype: 'container',
		//		padding: 8, style: 'background-color: #ff0',
				region: 'south'
			}]
		});
		me.callParent(arguments);
		
		me.DSS_LayerButton = Ext.create('Ext.Component', {
			floating: true,
			shadow: false,
			cls: 'layer-menu',
			tooltip: 'Access map layers',
			html: '<i class="far fa-layer-group"></i>',
			listeners: {
				render: function(c) {
					c.getEl().getFirstChild().el.on({
						click: function(self) {
							let rect = self.target.getBoundingClientRect();
							Ext.create('DSS.map.LayerMenu').showAt(rect.left, rect.top);
						}
					});
				}
			}					
		});
		
		setTimeout(function() {
			me.DSS_LayerButton.showAt(me.getX() + 2,-32);
			me.showLayerButton();
		}, 1000);
		
	},

	//-------------------------------------------------------------------------
	showLayerButton() {
		let me = this;
		me.DSS_LayerButton.animate({
			to: {
				y: 2
			}
		});
	},

	//-------------------------------------------------------------------------
	hideLayerButton() {
		let me = this;
		me.DSS_LayerButton.animate({
			to: {
				y: -32
			}
		});
	},
	
	// DefaultVisibility is a boolean...(but stored as a "0" or a "1"
	// DefaultOpacity is a decimal, e.g. 0.1 (but stored as "0.1")
	//-------------------------------------------------------------------------
	_cookieInternalHelper: function(key, defaultVisibility, defaultOpacity) {
		const layerVisKey = key + ":visible",
				layerOpacKey = key + ":opacity";
		
		if (Ext.util.Cookies.get(layerVisKey) == null) {
			Ext.util.Cookies.set(layerVisKey, defaultVisibility ? "1" : "0");
		}
		DSS.layer[layerVisKey] = Ext.util.Cookies.get(layerVisKey) == "1" ? true : false;
		
		if (Ext.util.Cookies.get(layerOpacKey) == null) {
			// must be a string...
			Ext.util.Cookies.set(layerOpacKey, "" + defaultOpacity);
		}
		DSS.layer[layerOpacKey] = parseFloat(Ext.util.Cookies.get(layerOpacKey)); 
	},
	
	//-------------------------------------------------------------------------
	manageMapLayerCookies: function() {
	
		let me = this;
		
		me._cookieInternalHelper("crop", "1", 0.8);
		me._cookieInternalHelper("inspector", "1", 0.8);
		me._cookieInternalHelper("watershed", "1", 0.6);
		me._cookieInternalHelper("hillshade", "0", 0.5);
		
		// Visible code is the # of the base layer that is visible...
		if (Ext.util.Cookies.get("baselayer:visible") == null) {
			Ext.util.Cookies.set("baselayer:visible", "1");
		}
		DSS.layer['baselayer:visible'] = Ext.util.Cookies.get('baselayer:visible'); 
	},
	
	//-------------------------------------------------------------------------
	instantiateMap: function() {
		let me = this;
		me.DSS_zoomStyles = {};
		
		for (let idx = 3; idx <= 16; idx++) {
			let sw = Math.floor(Math.sqrt(idx));
			if (sw < 1) sw = 1;
			me.DSS_zoomStyles['style' + idx] = new ol.style.Style({
				image: new ol.style.Circle({
					radius: idx,
					fill: new ol.style.Fill({
						color: 'rgba(32,96,160,0.9)'
					}),
					stroke: new ol.style.Stroke({
						color: 'rgba(255,255,255,0.75)',
						width: sw
					}),
				})
			});
		}
		
		DSS.layer = {};
		
		// Cookie-based settings persistence, when available
		me.manageMapLayerCookies();
		
		//---------------------------------------------------------
		DSS.layer.bingAerial = new ol.layer.Tile({
			visible: DSS.layer['baselayer:visible'] == "1" ? true : false,
			source: new ol.source.BingMaps({
				key: me.BING_KEY,
				imagerySet: 'AerialWithLabels',// can be: Aerial, Road, RoadOnDemand, AerialWithLabels, AerialWithLabelsOnDemand, CanvasDark, OrdnanceSurvey  
				hidpi:true,
				maxZoom:18,
				minZoom:11,
			})
		});
		//---------------------------------------------------------
		DSS.layer.bingRoad = new ol.layer.Tile({
			visible: DSS.layer['baselayer:visible'] == "2" ? true : false,
			source: new ol.source.BingMaps({
				key: me.BING_KEY,
				imagerySet: 'Road',  
				maxZoom:18,
				minZoom:11,
			})
		});		
		//--------------------------------------------------------------		
		DSS.layer.osm = new ol.layer.Tile({
			visible: DSS.layer['baselayer:visible'] == "3" ? true : false,
			source: new ol.source.TileJSON({
				url: 'https://api.maptiler.com/tiles/satellite/tiles.json?key=' + me.OSM_KEY,
				tileSize: 256,
				crossOrigin: 'anonymous'
			})
		})	;	
		//--------------------------------------------------------------		
		DSS.layer.watershed = new ol.layer.Vector({
			visible: DSS.layer['watershed:visible'],
			opacity: DSS.layer['watershed:opacity'],
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				format: new ol.format.GeoJSON(),
				url: 'assets/shapeFiles/tainterWatershed.geojson',
			}),
			style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: '#7fff1f',
					width: 4
				})
			})
		});
		let extent = [ -10128000, 5358000, -10109000, 5392000];

		DSS.layer.hillshade = new ol.layer.Image({
			visible: DSS.layer['hillshade:visible'],
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			opacity: DSS.layer['hillshade:opacity'],
			source: new ol.source.ImageStatic({
				url: '/assets/images/hillshade_high.png',
				imageExtent: extent
			})
		})
		
		//---------------------------------------
		let defaultFieldStyle = new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: 'rgba(255,200,32,0.8)',
				width: 2
			}),
			fill: new ol.style.Fill({
				color: 'rgba(0,0,0,0.1)',
			}),
			zIndex: 0
		});
//		let rotationStyles = { };
/*		DSS.fieldStyleFunction = function(feature, resolution, defaultStyle) {
			if (feature && feature.getProperties()) {
				let rot = feature.getProperties()['rotation']; 
				if (rot && rotationStyles[rot]) {
					return rotationStyles[rot];
				}
			}
			return defaultStyle;
		}
*/		

		DSS['layerSource'] = {};
		DSS.layerSource['fields'] = new ol.source.Vector({
			format: new ol.format.GeoJSON()
		}); 
		DSS.layer.fields = new ol.layer.Vector({
			visible: true,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: DSS.layerSource.fields,
			style: function(feature, resolution) {
				
				if (DSS.fieldStyleFunction) {
					return DSS.fieldStyleFunction(feature, resolution);
				}
				else return defaultFieldStyle;
			},
		});	

		// Populate grid
/*		DSS.layer.fields.getSource().on('change', function(evt) {
			
			let fd = Ext.StoreMgr.lookup('field_data');
			let records = [];
			let fid = 1;
			DSS.layer.fields.getSource().forEachFeature(function(f) {
				records.push({
					id: f.get('f_id'),
					name: 'field' + fid,
					acres: ol.sphere.getArea(f.getGeometry()) / 4046.8564224
				});
				fid++;
			})
			fd.loadRawData(records);
		})
*/		
		let farmSource = new ol.source.Vector({
			format: new ol.format.GeoJSON(),
			loader: function(extent, resolution, projection) {
				let url = location.href + 'get_farms'
				let xhr = new XMLHttpRequest();
				xhr.open('GET', url);
				var onError = function() {
					farmSource.removeLoadedExtent(extent);
				}
				xhr.onerror = onError;
				xhr.onload = function() {
					if (xhr.status == 200) {
						farmSource.addFeatures(farmSource.getFormat().readFeatures(xhr.responseText));
						DSS.viewModel.master.set("farm_count", farmSource.getFeatures().length);
					}
					else {
						onError();
					}
				}
				xhr.send();
			},
		}) 
		//--------------------------------------------------------------
		DSS.layer.farms = new ol.layer.Vector({
			visible: true,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: farmSource,
			style: function(feature, resolution) {
				let r = 1.0 - resolution / 94.0;
				if (r < 0) r = 0
				else if (r > 1) r = 1
				// value from 3 to 16
				r = Math.round(Math.pow(r, 3) * 13 + 3)
				return me.DSS_zoomStyles['style' + r];
			}
		});
		
		//--------------------------------------------------------------
		me.map = DSS.map = new ol.Map({
			target: me.down('#ol_map').getEl().dom,
			layers: [
				DSS.layer.bingAerial,
				DSS.layer.bingRoad,
				DSS.layer.osm,
				DSS.layer.watershed,             
				DSS.layer.hillshade,
//				DSS.layer.fields,
				DSS.layer.farms,
			],//------------------------------------------------------------------------
			view: new ol.View({
				center: [-10118000,5375100],
				zoom: 12,
				maxZoom: 19,
				minZoom: 10,
			//	constrainRotation: false,
			//	rotation: 0.009,
				constrainOnlyCenter: true,
				extent:[-10132000, 5353000, -10103000, 5397000]
			})
		});

		me.map.addControl(new ol.control.ScaleLine({
			bar: true, 
			minWidth: 112,
			units: 'us',
//			units: 'metric'
		}));
		proj4.defs('urn:ogc:def:crs:EPSG::3071', "+proj=tmerc +lat_0=0 +lon_0=-90 +k=0.9996 +x_0=520000 +y_0=-4480000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs");
		proj4.defs('EPSG:3071', "+proj=tmerc +lat_0=0 +lon_0=-90 +k=0.9996 +x_0=520000 +y_0=-4480000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs");
		ol.proj.proj4.register(proj4);	
		
		me.popup = DSS.popupContainer = Ext.create('Ext.Container', {
			minWidth: 200,
			cls: 'popup-eye',
			padding: 8,
			floating: true,
			x: -100, y: -75,
		}).show();
		
		me.overlay = DSS.popupOverlay = new ol.Overlay({
			element: me.popup.getEl().dom,
			autoPan: true,
			autoPanAnimation: {
				duration: 500,
				easing: ol.easing.easeOut
			}
		});
		
		me.map.addOverlay(me.overlay);
		
		me.ol_uid = false;
		
		//-----------------------------------------------------------
		me.map.on('click', function(e) {
			let coords = me.map.getEventCoordinate(e.originalEvent);
			
			console.log(e, coords, ol.proj.transform(coords, 'EPSG:3857', 'EPSG:3071'));  
			if (DSS.mapClickFunction) DSS.mapClickFunction(e, coords);
		});
	
		//-----------------------------------------------------------
		me.map.on('pointermove', function(e) {
			if (DSS.mouseMoveFunction) {
				DSS.mouseMoveFunction(e);
				return;
			}
		});
		me.drawTools 	= Ext.create('DSS.map.DrawAndModify').instantiate(me.map, DSS.layer.fields.getSource());
		
		me.boxModelTool = Ext.create('DSS.map.BoxModel').instantiate(me.map);
		
		me.addMarkerLayer(me.map);
		me.addWorkAreaMask(me.map);
		me.addSelectionTools(me.map);
		me.map.addLayer(DSS.layer.fields);
		
		me.cropRotationOverlay = Ext.create('DSS.map.RotationLayer').instantiate(me.map);
		
	/*	// Convenience: TODO: re-evalutate need 
		DSS.layer.MouseOver = new ol.layer.Vector({
			visible: false,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			useSpatialIndex: false,
			source: new ol.source.Vector({
				//features: new ol.Collection()
			}),
			style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: '#a00',
					width: 4
				})
			})
		});
		me.map.addLayer(DSS.layer.MouseOver);*/
	},
	
	
	//---------------------------------------------------------------
	addWorkAreaMask: function(map) {
		let me = this;
		let spotStyle = new ol.style.Style({
		    stroke: new ol.style.Stroke({
		        color: 'rgba(0, 0, 0, 0.9)',
		        width: 2
		    }),
		    fill: new ol.style.Fill({
			    color: 'rgba(0, 32, 0, 0.8)'
			})
		});

		DSS.layer.mask = new ol.layer.Vector({
			source: new ol.source.Vector(),
			style: spotStyle,
			opacity: 0.7,
			// these potentially reduce performance but looks better
			updateWhileAnimating: true, 
			updateWhileInteracting: true
		});	
		
		let multiPoly = [[ 
			[
				[ -10220000, 5280000 ], 
				[ -10220000, 5470000 ], 
				[ -10000000, 5470000 ], 
				[ -10000000, 5280000 ], 
				[ -10220000, 5280000 ] 
			],[ // inner - counter-clockwise
				[ -10128539.23, 5356917.38 ], 
				[ -10128962.9, 5392788.13 ], 
				[ -10108301.0, 5393011.78 ], 
				[ -10107956.73, 5357138.36 ], 
				[ -10128539.23, 5356917.38 ]
			] 
		]];
		
		var spot = new ol.geom.MultiPolygon(multiPoly);
	    
		DSS.layer.mask.getSource().addFeature(new ol.Feature(spot));
		map.addLayer(DSS.layer.mask);                        
	},
	
	//---------------------------------------------------------------
	addMarkerLayer: function(map) {
		
		let spotStyle = new ol.style.Style({
			image: new ol.style.Circle({
				radius: 8,
			    stroke: new ol.style.Stroke({
			        color: 'white',
			        width: 2
			    }),
			    fill: new ol.style.Fill({
				    color: 'rgba(32, 100, 128, 0.8)'
				})
			})
		});
		
		let markerOverlay = DSS.layer.markers = new ol.layer.Vector({
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector(),
			renderOrder: function(feature1, feature2) {
				let g1 = feature1.getGeometry(), g2 = feature2.getGeometry();
				return g2.getCoordinates()[1] - g1.getCoordinates()[1];
			},
			opacity: 0.9,
			style: function(feature) {
				if (!DSS.markerStyleFunction) {
					return spotStyle;
				}
				else return DSS.markerStyleFunction(feature);
			}
		})
		
		map.addLayer(markerOverlay);
	},
	
	//---------------------------------------------
	addSelectionTools: function(map) {
		const select = DSS.selectionTool = new ol.interaction.Select({
			features: new ol.Collection(),
			toggleCondition: ol.events.condition.never,
			style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: 'white',
					width: 4
				}),
				fill: new ol.style.Fill({
					color: 'rgba(0,0,0,0)'
				}),
				zIndex: 5
			})
		});
		select.on('select', function(evt) {
			if (DSS.selectionFunction) {
				DSS.selectionFunction(evt);
			}
		});
		
		select.setActive(false);
		map.addInteraction(select);
	}
	
});


//---------------------------------------------------------------
var CanvasLayer = /*@__PURE__*/(function (Layer) {
	
	function CanvasLayer(options) {
		
		Layer.call(this, options);
		this.features = options.features;
		this.svg = d3.select(document.createElement('div')).append('svg').style('position', 'absolute');
		this.svg.append('path').datum(this.features).attr('class', 'boundary');
	}
	
	if (Layer) CanvasLayer.__proto__ = Layer;
	CanvasLayer.prototype = Object.create(Layer && Layer.prototype);
	CanvasLayer.prototype.constructor = CanvasLayer;
	
	CanvasLayer.prototype.getSourceState = function getSourceState () {
		return "ready";//ol.source.State.READY;
	};

	CanvasLayer.prototype.render = function render(frameState) {
		var width = frameState.size[0];
		var height = frameState.size[1];
		var projection = frameState.viewState.projection;
		var d3Projection = d3.geoMercator().scale(1).translate([0, 0]);
		var d3Path = d3.geoPath().projection(d3Projection);
		
		var pixelBounds = d3Path.bounds(this.features);
		var pixelBoundsWidth = pixelBounds[1][0] - pixelBounds[0][0];
		var pixelBoundsHeight = pixelBounds[1][1] - pixelBounds[0][1];
		
		var geoBounds = d3.geoBounds(this.features);
		var geoBoundsLeftBottom = ol.proj.fromLonLat(geoBounds[0], projection);
		var geoBoundsRightTop = ol.proj.fromLonLat(geoBounds[1], projection);
		var geoBoundsWidth = geoBoundsRightTop[0] - geoBoundsLeftBottom[0];
		if (geoBoundsWidth < 0) {
			geoBoundsWidth += ol.extent.getWidth(projection.getExtent());
		}
	    var geoBoundsHeight = geoBoundsRightTop[1] - geoBoundsLeftBottom[1];
	
	    var widthResolution = geoBoundsWidth / pixelBoundsWidth;
	    var heightResolution = geoBoundsHeight / pixelBoundsHeight;
	    var r = Math.max(widthResolution, heightResolution);
	    var scale = r / frameState.viewState.resolution;
	
	    var center = ol.proj.toLonLat(ol.extent.getCenter(frameState.extent), projection);
	    d3Projection.scale(scale).center(center).translate([width / 2, height / 2]);
	
	    d3Path = d3Path.projection(d3Projection);
	    d3Path(this.features);
	
	    this.svg.attr('width', width);
	    this.svg.attr('height', height);

	    this.svg.select('path').attr('d', d3Path);

	    return this.svg.node();
	};

	return CanvasLayer;
}(ol.layer.Tile));

