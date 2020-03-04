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
Ext.define('DSS.app.MainMap', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',//Component',
	alias: 'widget.main_map',
	
	style: 'background-color: rgb(75,80,60)',
	
	BING_KEY: 'Au_ohpV01b_LnpbMExJmpmUnamgty20v7Cpl1GvNmwzZPOezhtzegaNM0MNaSPoa',
	OSM_KEY: '8UmAwNixnmOYWs2lqUpR',
	
	requires: [
		'DSS.controls.StatsPanel'
	],
	
	layout: 'border',
	listeners: {
		afterrender: function(self) {
			self.instantiateMap()
		},
		resize: function(self, w, h) {
			let mapSize = self.down('#ol_map').getSize();
			self.map.setSize([mapSize.width, mapSize.height]);
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
					}
				},
			},
			DSS.StatsPanel/*{ // Directly add the singleton instance...
				xtype: 'stats_panel',
			}*/]
		});
		me.callParent(arguments);
		
		proj4.defs('urn:ogc:def:crs:EPSG::3071', "+proj=tmerc +lat_0=0 +lon_0=-90 +k=0.9996 +x_0=520000 +y_0=-4480000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs");
		proj4.defs('EPSG:3071', "+proj=tmerc +lat_0=0 +lon_0=-90 +k=0.9996 +x_0=520000 +y_0=-4480000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs");
		ol.proj.proj4.register(proj4);		
	},

	//-------------------------------------------------------------------------
	instantiateMap: function() {
		let me = this;
		me.DSS_zoomStyles = {};
		
		for (let idx = 3; idx <= 23; idx++) {
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
		//---------------------------------------------------------
		DSS.layer.bingAerial = new ol.layer.Tile({
			visible: true,
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
			visible: false,
			source: new ol.source.BingMaps({
				key: me.BING_KEY,
				imagerySet: 'Road',  
				maxZoom:18,
				minZoom:11,
			})
		});		
		//--------------------------------------------------------------		
		DSS.layer.osm = new ol.layer.Tile({
			visible: false,
			source: new ol.source.TileJSON({
				url: 'https://api.maptiler.com/tiles/satellite/tiles.json?key=' + me.OSM_KEY,
				tileSize: 256,
				crossOrigin: 'anonymous'
			})
		})	;	
		//--------------------------------------------------------------		
		DSS.layer.watershed = new ol.layer.Vector({
			visible: true,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				format: new ol.format.GeoJSON(),
				url: 'assets/shapeFiles/tainterWatershed.geojson',
			}),
			style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: 'rgba(32,128,255,0.6)',
					width: 8
				})
			})
		});
		let extent = [ -10128000, 5358000, -10109000, 5392000];

		DSS.layer.hillshade = new ol.layer.Image({
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			visible: true,
			opacity: 0.5,
			source: new ol.source.ImageStatic({
				url: '/assets/images/hillshade_high.png',
				imageExtent: extent
			})
		})
		
		//---------------------------------------
		let defaultFieldStyle = new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: 'rgba(255,200,32,0.6)',
				width: 2
			}),
			fill: new ol.style.Fill({
				color: 'rgba(255,128,32,0.2)',
			})
		});
		
		DSS.layer.fields = new ol.layer.Vector({
			visible: false,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				format: new ol.format.GeoJSON(),
			}),
			style: function(feature, resolution) {
				
				if (DSS.fieldStyleFunction) {
					return DSS.fieldStyleFunction(feature, resolution);
				}
				else return defaultFieldStyle;
			},
		});	

		//--------------------------------------------------------------
		DSS.layer.farms = new ol.layer.Vector({
			visible: true,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				format: new ol.format.GeoJSON(),
				url: location.href + 'get_farms',
			}),
			style: function(feature, resolution) {
				let r = 1.0 - resolution / 86.0;
				if (r < 0) r = 0
				else if (r > 1) r = 1
				// value from 3 to 23
				r = Math.round(Math.pow(r, 3) * 20 + 3)
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
				DSS.layer.fields,
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
			me.dataForPoint(coords[0], coords[1]);
			
			
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

		me.addDrawTools(me.map);
		me.addTestBoxSelectWithImage(me.map);
		me.addMarkerLayer(me.map);
		me.addWorkAreaMask(me.map);
		me.addSelectionTools(me.map);
	},
	
	//----------------------------------------------------------------------
	dataForPoint: function(x, y) {
		Ext.Ajax.request({
			url: location.href + 'data_from_point',
			jsonData: {
				x: x.toFixed(3),
				y: y.toFixed(3)
			},
			timeout: 10 * 1000, // 30 seconds
		});
	},
	
	//-------------------------------------------------------
	addTestBoxSelectWithImage: function(map) {
		let me = this;
		let extent = [0, 0, 1024, 968];
		let dragBox = DSS.dragBox = new ol.interaction.DragBox({
			  condition: ol.events.condition.platformModifierKeyOnly
		});

		map.addInteraction(dragBox);
		
		ol.renderer.canvas.ImageLayer.prototype.prepFrame = ol.renderer.canvas.ImageLayer.prototype.prepareFrame; 
		ol.renderer.canvas.ImageLayer.prototype.prepareFrame = function(frameState) {
			frameState.viewHints[0] = frameState.viewHints[1] = 0
			return this.prepFrame(frameState); 
		}

		DSS.layer.Image = new ol.layer.Image({
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			opacity: 1,
			source: new ol.source.ImageStatic({
				imageExtent: extent,
				projection: 'EPSG:3071'
			})
		})
		let working = false;
		
		map.addLayer(DSS.layer.Image);
		
		dragBox.on('boxend', function() { // 'boxend
			//if (!DSS.dragBoxFunction) return;
			if (working) return;
			working = true;
			
			let ex = dragBox.getGeometry().getExtent();	
			
			let pt1 = ol.proj.transform([ex[0],ex[1]], 'EPSG:3857', 'EPSG:3071'),
				pt2 = ol.proj.transform([ex[2],ex[3]], 'EPSG:3857', 'EPSG:3071');

			ex = [pt1[0], pt1[1], pt2[0], pt2[1]];
			
			let data = {
				"extent" : ex,
				"model": Ext.getCmp('DSS_cheat').getValue()['model']
			};
			if (Ext.getCmp('DSS_cheatRestrictFarm').getValue() && DSS.activeFarm) {
				data["farm_id"] = DSS.activeFarm;
				data["mode"] = Ext.getCmp('DSS_cheatFieldAggregate').getValue() ? 2 : 1;
			}
			if (Ext.getCmp('DSS_maskByCDL').getValue()) {
				data['row_crops'] = Ext.getCmp('DSS_cheatRowCropMask').getValue() ? true : false;
				data['grasses'] = Ext.getCmp('DSS_cheatGrassMask').getValue() ? true : false;
			}
			
			var obj = Ext.Ajax.request({
				url: location.href + 'fetch_image',
				jsonData: data,
				timeout: 10000,
				success: function(response, opts) {
					var obj = JSON.parse(response.responseText);
					console.log(obj);
					working = false;
					DSS.layer.Image.setSource(new ol.source.ImageStatic({
						url: obj.url,
						imageExtent: obj.extent,
						projection: 'EPSG:3071'
					}))
					DSS.layer.Image.setOpacity(0.7);
					DSS.layer.Image.setVisible(true);	
					
					DSS.MapState.showLegend(obj.palette, obj.values);
				},
				
				failure: function(response, opts) {
					working = false;
				}
			});
		});
	},

	//--------------------------------------------------------------------------
	addDrawTools: function(map) {

		// Ordering is important. If modify is inserted last, it takes priority over drawing
		const modify = DSS.modify = new ol.interaction.Modify({
			source: DSS.layer.fields.getSource(),
			style: new ol.style.Style({
				image: new ol.style.Circle({
					radius: 3,
					stroke: new ol.style.Stroke({
						color: 'rgba(255, 255, 255, 0.8)'
					}),
					fill: new ol.style.Fill({
						color: 'rgba(255, 255, 0, 0.8)'
					})
				})
			})	
		});
		map.addInteraction(modify); modify.setActive(false);

		// Yuck, returns all features no matter whether they were edited or not
/*		DSS.modify.on('modifyend', function(evt) {
			if (DSS.modifyEndEvent) {
				console.log("modifyend was", evt)
				DSS.modifyEndEvent(evt, evt.features, evt.target, evt.type);
			}
		})
*/
		// EVIL event hack to get edited segments ONLY
		DSS.modify.handleUpEvent = function(evt) {
			for (let i = this.dragSegments_.length - 1; i >= 0; --i) {
				const segmentData = this.dragSegments_[i][0];
				const geometry = segmentData.geometry;
				this.rBush_.update(ol.extent.boundingExtent(segmentData.segment), segmentData);
			}
			if (this.modified_) {
				if (DSS.modifyEndEvent) {
					DSS.modifyEndEvent(evt, this.dragSegments_);
				}
				this.modified_ = false;
			}
			return false;
		}
		DSS.modify.removePoint = function() {
			if (this.lastPointerEvent_ && this.lastPointerEvent_.type != "pointerdrag") {
				const evt = this.lastPointerEvent_;
				this.willModifyFeatures_(evt);
				const copySegs = this.dragSegments_.slice(); // remove vertext unhelpfully blows away dragSegments_
				const removed = this.removeVertex_();
				if (DSS.modifyEndEvent) {
					DSS.modifyEndEvent(evt, copySegs);
				}
				this.modified_ = false;
				return removed;
			}
			return false;
		}
		
		DSS.draw = new ol.interaction.Draw({
			source: DSS.layer.fields.getSource(),
			type: 'Polygon',
			style: [
				new ol.style.Style({
					image: new ol.style.RegularShape({
						points: 4,
						radius1: 4,
						radius2: 0,
						angle: 1.570796326794897,
						rotation: 1.570796326794897,
						stroke: new ol.style.Stroke({
							color: '#000',
							width: 3
						})
					})
				}),
				new ol.style.Style({
					stroke: new ol.style.Stroke({
						color: 'rgba(255, 255, 255, 1)',
						width: 2
					}),
					geometry: function(feature) {
						if (feature.getGeometry().getType() === 'Polygon') {
							return false;
						}
						return feature.getGeometry();
					}
				}),
				new ol.style.Style({
					fill: new ol.style.Fill({
						color: 'rgba(128, 64, 0, 0.33)'
					}),
					stroke: new ol.style.Stroke({
						color: 'rgba(255, 255, 255, 0.5)',
						lineDash: [4, 4],
						width: 2
					}),
					image: new ol.style.RegularShape({
						points: 4,
						radius1: 4,
						radius2: 0,
						angle: 1.570796326794897,
						rotation: 1.570796326794897,
						stroke: new ol.style.Stroke({
							color: '#fff',
							width: 1
						})
					}),
					geometry: function(feature) {
						if (feature.getGeometry().getType() === 'LineString') {
							return false;
						}
						return feature.getGeometry();
					}
				}),
				new ol.style.Style({ // adds points to dashed line
					image: new ol.style.Circle({
						radius: 3,
						fill: new ol.style.Fill({
							color: 'rgba(175,225,255,0.5)'
						}),
						stroke: new ol.style.Stroke({
							color: 'rgba(0,0,0,0.75)',
							width: 1
						}),
					}),
					geometry: function(feature) {
						if (feature.getGeometry().getType() === 'Polygon') {
							var coordinates = feature.getGeometry().getCoordinates()[0];
							return new ol.geom.MultiPoint(coordinates);
						}
						return false;
					}
				})		
			]
		});
		let isDrawing = false;
		DSS.draw.on('drawstart', function(evt) {
			isDrawing = true;
		})
		DSS.draw.on('drawend', function(evt) {
			isDrawing = false
			if (DSS.drawEndEvent) {
				DSS.drawEndEvent(evt, evt.feature, evt.target, evt.type);
			}
		})
		map.addInteraction(DSS.draw);	
		DSS.draw.setActive(false);

		document.addEventListener('keyup', function(e) {
			if (e.keyCode == 27) {
				if (isDrawing && DSS.draw.getActive()) {
					DSS.draw.setActive(false);
					DSS.draw.setActive(true);
					isDrawing = false;
				}
			}
		});

		const snap = DSS.snap = new ol.interaction.Snap({
			source: DSS.layer.fields.getSource(),
			//pixelTolerance: 8,
			style: new ol.style.Style({
				image: new ol.style.Circle({
					radius: 3,
					stroke: new ol.style.Stroke({
						color: 'rgba(255, 255, 255, 0.8)'
					}),
					fill: new ol.style.Fill({
						color: 'rgba(255, 255, 0, 0.8)'
					})
				})
			})	
		});
		map.addInteraction(snap);		
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

