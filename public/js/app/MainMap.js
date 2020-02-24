DSS.utils.addStyle('.popup-eye { opacity: 0.9; overflow: visible!important;background-color: #f8f7ef; border-radius: .5rem; border: 1px solid #999; box-shadow: 0 6px 6px rgba(0,0,0,0.5) }')
DSS.utils.addStyle('.popup-eye:after { transform: rotate(-45deg); overflow: visible!important; display: block; position: absolute; bottom: -0.32rem; left: calc(100px - 0.32rem); content: ""; background-color: #f8f7ef; width: 0.5rem; height: 0.5rem; border-left: 1px solid #999; border-bottom: 1px solid #999; box-shadow: -6px 6px 6px rgba(0,0,0,0.5) }')

DSS.utils.addStyle('path.boundary { fill: #ff00001f; stroke: red;}');
DSS.utils.addStyle('path.boundary:hover { fill: #ff00005f; stroke: red;}');

//------------------------------------------------------------------------------
Ext.define('DSS.app.MainMap', {
//------------------------------------------------------------------------------
	extend: 'Ext.Component',
	alias: 'widget.main_map',
	
//	style: 'background-color: rgb(150,160,120)',
	style: 'background-color: rgb(75,80,60)',
	
	BING_KEY: 'Au_ohpV01b_LnpbMExJmpmUnamgty20v7Cpl1GvNmwzZPOezhtzegaNM0MNaSPoa',
	OSM_KEY: '8UmAwNixnmOYWs2lqUpR',
	
	listeners: {
		afterrender: function(self) {
			self.instantiateMap()
		},
		resize: function(self, w, h) {
			self.map.setSize([w,h]);
		}
	},
		
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		proj4.defs('urn:ogc:def:crs:EPSG::32615', "+proj=utm +zone=15 +datum=WGS84 +units=m +no_defs");
		proj4.defs('EPSG:32615', "+proj=utm +zone=15 +datum=WGS84 +units=m +no_defs");
		ol.proj.proj4.register(proj4);
		
		me.callParent(arguments);
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
		me.popup = Ext.create('Ext.Container', {
			minWidth: 200,
			cls: 'popup-eye',
			padding: 8,
			floating: true,
			x: -100, y: -75,
		}).show();
		
		me.overlay = new ol.Overlay({
			element: me.popup.getEl().dom,
			autoPan: true,
			autoPanAnimation: {
				duration: 500,
				easing: ol.easing.easeOut
			}
		});
		
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
//			opacity: 0.5,
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
		//--------------------------------------------------------------
/*		DSS.layer.hillshade = new ol.layer.Tile({
			visible: true,
			opacity: 0.8,
			source: new ol.source.TileJSON({
				url: 'https://api.maptiler.com/tiles/hillshades/tiles.json?key=' + me.OSM_KEY,
		        tileSize: 256,
		        crossOrigin: 'anonymous'
			})
		});
*/		
		let extent = [ -10128000, 5358000, -10109000, 5392000];

		DSS.layer.hillshade = new ol.layer.Image({
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			visible: false,
			opacity: 0.5,
			source: new ol.source.ImageStatic({
				url: '/assets/images/hillshade_high.png',
				imageExtent: extent
			})
		})
		
		/*//--------------------------------------------------------------		
		let contourSource = new ol.source.VectorTile({
			visible: false,
			maxZoom: 14,
		    url: 'https://api.maptiler.com/tiles/contours/{z}/{x}/{y}.pbf?key=' + me.OSM_KEY,
		    format: new ol.format.MVT(),
		    style: new ol.style.Style({
		    	stroke: new ol.style.Stroke({
		    		color: 'rgba(255,204,39,0.75)',
		    		width: 1
		    	})
		    })
		});	
		DSS.layer.contour = new ol.layer.VectorTile({
			visible: false,
			opacity: 0.75,
			source: contourSource
		});	
		try {
			olms.stylefunction(DSS.layer.contour, '{"version":8,"sources":{"70a6bc5f-ab1f-4700-ba48-827a60aeabaf":{"type":"vector","url":"https://api.maptiler.com/tiles/contours/tiles.json?key=8UmAwNixnmOYWs2lqUpR"}},"zoom":12.318997928418138,"glyphs":"https://api.maptiler.com/fonts/{fontstack}/{range}.pbf?key=8UmAwNixnmOYWs2lqUpR","id":"da4d3d38-84c4-4201-9abe-4c5b1ccce2dc","name":"Contours","pitch":0,"center":[8.208182906994125,47.017038017246705],"bearing":0,"layers":[{"id":"contour_line","source":"70a6bc5f-ab1f-4700-ba48-827a60aeabaf","type":"line","paint":{"line-color":"rgba(183, 175, 67, 1)"},"layout":{"visibility":"visible","line-cap":"square","line-join":"round"},"minzoom":9,"maxzoom":19,"source-layer":"contour"},{"id":"contour_polygon","source":"70a6bc5f-ab1f-4700-ba48-827a60aeabaf","source-layer":"contour","type":"fill","filter":["==","$type","Polygon"],"paint":{"fill-color":"rgba(187, 106, 68, 1)","fill-opacity":0.6},"layout":{"visibility":"visible"}}]}', 
					'contour_line');
		}
		catch(err) {
			console.log(err);
		}*/
		//--------------------------------------------------------------
		DSS.layer.fields = new ol.layer.Vector({
			visible: true,//false
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				format: new ol.format.GeoJSON(),
				url: 'get_fields?farm=1',
			}),
/*			source: new ol.source.Vector({
				format: new ol.format.GeoJSON(),
				url: 'assets/shapeFiles/exampleFields.geojson',
			}),*/
			style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: 'rgba(255,200,32,0.6)',
					width: 2
				}),
				fill: new ol.style.Fill({
					color: 'rgba(255,128,32,0.2)',
				})
			})
		});	

/*		setTimeout(function() {

			var snappedFeatures = [];
			var fsc = DSS.layer.fields.getSource().getFeatures();
			for (var idx = 0; idx < fsc.length; idx++) {
				var el = fsc[idx];
				var newEl = el.clone();
				var geo = el.getGeometry().getCoordinates()[0];
				var newGeo = []
				for (var slop = 0; slop < geo.length; slop++) {
					var pt = geo[slop];
					newGeo.push([
						Math.round(pt[0] / 10.0) * 10.0,
						Math.round(pt[1] / 10.0) * 10.0
					])
				}
				newEl.getGeometry().setCoordinates([newGeo]);
				console.log(newGeo);
				snappedFeatures.push(newEl);
			}
			console.log(snappedFeatures);
			//--------------------------------------------------------------
			DSS.layer.snappedFields = new ol.layer.Vector({
				visible: true,//false
				updateWhileAnimating: true,
				updateWhileInteracting: true,
				source: new ol.source.Vector({
					features: snappedFeatures,
				}),
				style: new ol.style.Style({
					stroke: new ol.style.Stroke({
						color: '#000',
						width: 2
					}),
					fill: new ol.style.Fill({
						color: 'rgba(255,0,0,0)'
					})
				})
			});
			
			me.map.addLayer(DSS.layer.snappedFields)
		}, 4000);
*/		
		//--------------------------------------------------------------
		DSS.layer.farms = new ol.layer.Vector({
			visible: false,
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
			target: me.getEl().dom,
			overlays: [me.overlay],
			layers: [
				DSS.layer.bingAerial,
				DSS.layer.bingRoad,
				DSS.layer.osm,
				DSS.layer.watershed,
				DSS.layer.hillshade,
				//DSS.layer.contour,
				DSS.layer.fields,
				DSS.layer.farms,
			],//------------------------------------------------------------------------
			view: new ol.View({
				center: [-10118000,
					5375100],
				zoom: 12,
				maxZoom: 19,
				minZoom: 10,
				constrainOnlyCenter: true,
				extent:[-10130000, 5355000, -10105000, 5395000]
			})
		});
		
		me.ol_uid = false;
		
		//-----------------------------------------------------------
		me.map.on('click', function(e) {
			console.log(e)
			console.log(me.map.getEventCoordinate(e.originalEvent));
		});
		
		//-----------------------------------------------------------
		/*me.map.on('pointermove', function(e) {
			let pixel = me.map.getEventPixel(e.originalEvent);
			let fs = me.map.getFeaturesAtPixel(pixel);
			let cursor = '';
			let hitAny = false;
			fs.forEach(function(f) {
				let g = f.getGeometry();
				if (!g) return;
				if (g.getType() === "Point") {
					cursor = 'pointer';
					hitAny = true;
					me.overlay.setPosition(g.getCoordinates());
					me.popup.update('Farm: ' + f.get('name') + '<br>' +
						'Address: ' + f.get('address') + '<br>' +
						'Owner: ' + f.get('owner') + '<br>');
				}
				else if (g.getType() === "Polygon") {
					cursor = 'pointer';
					hitAny = true;
					console.log(f);
					let fmt = Ext.util.Format.number;
					let area 	= fmt(ol.sphere.getArea(g) / 4046.856, '0,0.#');
					let len 	= ol.sphere.getLength(g);
					let cost 	= fmt(len * 1.25, '0,0.##');
					len 		= fmt(len, '0,0.#');
					
					let extent = g.getExtent();
					let center = ol.extent.getCenter(extent);
					center[1] += (ol.extent.getHeight(extent) / 2);
					center = g.getClosestPoint(center);
					me.map.getView().cancelAnimations();
					//me.overlay.setPosition(center);
					me.popup.update('Area (ac): ' + area + '<br>' +
							'Boundary Dist (m): ' + len + '<br>' +
							'Cost ($1.25/m): $' + cost + '<br>');					
				}
			})
			
			if (!hitAny) {
				me.overlay.setPosition(false);
			}
			me.map.getViewport().style.cursor = cursor;
		});*/

		//me.addDrawTools(me.map);	
		
		//me.addTestBoxSelect()
		me.addTestBoxSelectWithImage();
		//me.addTestProjection();
		
		
		me.addWorkAreaMask();
	},
	
	addTestProjection: function() {
	
		let me = this;
//		let projectionName = 'EPSG:32615';
		// Extents? -96.00, 0.00, -90.00, 84.00
//		proj4.defs(projectionName, "+proj=utm +zone=15 +datum=WGS84 +units=m +no_defs");
		
		//--------------------------------------------------------------
		DSS.layer.hexes = new ol.layer.Vector({
			visible: true,//false
			//opacity: 0.2,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				format: new ol.format.GeoJSON({
				}),
				url: 'assets/shapeFiles/hexes.json',
			}),
			style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: 'rgba(255,200,32,0.6)',
					width: 2
				}),
				fill: new ol.style.Fill({
					color: 'rgba(255,128,32,0.2)',
				})
			})
		});	
		
		me.map.addLayer(DSS.layer.hexes);
		
	},
	
	//-----------------------------------------------------------------------------
	addTestBoxSelect() {
		
		let me = this;
		let colors = ['#00429d', '#3b71b1', '#61a2c1', '#8fd4c9', '#ebffaf', '#f4c072', '#dd863e', '#bc4d15', '#910b00'];
		
		me.DSS_fieldStyles = [];
		
		for (let idx = 0; idx < 9; idx++) {
			me.DSS_fieldStyles.push(new ol.style.Style({
				fill: new ol.style.Fill({
					color: colors[idx] + 'cf'
				}),
				/*stroke: new ol.style.Stroke({
					color: 'rgba(0,0,0,0.0)',
					width: 1
				})*/
			}));
		}
		
		var dragBox = new ol.interaction.DragBox({
			  condition: ol.events.condition.platformModifierKeyOnly
		});

		me.map.addInteraction(dragBox);
		DSS.layer.test = new ol.layer.Vector({
			visible: true,
			opacity: 0.75,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				features: undefined
			}),
			style: function(feature) {
				let  val = feature.get('val');
				val = Math.floor(val);
				return me.DSS_fieldStyles[val];
			}
	/*		style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: '#0000002f',
					width: 1
				}),
				fill: {
					getColor: function(feature) {
						return '#ff0';
					}
				}
			})*/
		});
		me.map.addLayer(DSS.layer.test);
		
		/*
		dragBox.on('blurfed', function() { // 'boxend
			var extent = dragBox.getGeometry().getExtent();			
			let source = {
					"type": "FeatureCollection",
					"name": "TainterSimpleBoundary",
					"crs": { 
						"type": "name", "properties": { "name": "urn:ogc:def:crs:EPSG::3857" }
					},
					"features": []
			};
			console.log(extent);
			let strideX = Math.round((extent[2] - extent[0]) / 50);
			let strideY = Math.round((extent[3] - extent[1]) / 50);
			
			let stride = strideX < strideY ? strideY : strideX;
			if (stride < 10) stride = 10;
			
			for (var x = extent[0]; x <= extent[2]; x += stride) {
				for (var y = extent[1]; y <= extent[3]; y += stride) {
					let val = {
						"type":"Feature",
						"properties": {
							val: ((Math.sin((x / 100000.0) * (y / 100000.0) + y/10000.0) + 1) * 4.5 + (Math.random() * 4.5)) * (9.5/14.5),
						},
						"geometry": {
							"type":"Polygon",
							"coordinates":[[
								[x,				y],
								[x + stride,	y],
								[x + stride,	y + stride],
								[x,				y + stride],
								[x,			y]
							]]
						}
					};

					source.features.push(val);
				}
			}
			let ofg = new ol.format.GeoJSON();			
			let ffs = ofg.readFeatures(source);
			
			DSS.layer.test.getSource().clear(true);
			DSS.layer.test.getSource().addFeatures(ffs);
		});*/
		
		let working = false;
		
		dragBox.on('boxend', function() { // 'boxend
		//	if (working) return;
			working = true;
			
			var extent = dragBox.getGeometry().getExtent();			

			console.log(extent);
			let max = ol.proj.transform([extent[0],extent[1]], "EPSG:3857", "EPSG:32615");
			let min = ol.proj.transform([extent[2],extent[3]], "EPSG:3857", "EPSG:32615");
			
			extent = [max[0],max[1],min[0],min[1]];
//			extent = ol.proj.transform(extent, "EPSG:3857", "EPSG:32615");
			/*extent[0] = ol.proj.transform(extent[0], "EPSG:3857", "EPSG:32615");
			extent[1] = ol.proj.transform(extent[1], "EPSG:3857", "EPSG:32615");
			extent[2] = ol.proj.transform(extent[2], "EPSG:3857", "EPSG:32615");
			extent[3] = ol.proj.transform(extent[3], "EPSG:3857", "EPSG:32615");*/
			console.log(extent);
//			extent[2] = extent[0] - 4000;
			
			var obj = Ext.Ajax.request({
				url: location.href + 'test_fetch',
				jsonData: {
//					extent,
					"extent" : extent // FIXME: TODO: transform extent
				},
				timeout: 10000,
				//method: 'GET',
				success: function(response, opts) {
					var obj = JSON.parse(response.responseText);
					console.log(obj);
					working = false;
					let source = {
						"type": "FeatureCollection",
						"name": "developed",
						"crs": { 
							"type": "name", "properties": { "name": "EPSG:32615" }//urn:ogc:def:crs:EPSG::32615" }
						},
						"features": []
					};
					let stride = obj.stride * 10;
					obj = obj.data;
					for (let idx = 0; idx < obj.length; idx++) {
						let el = obj[idx];
						let val = {
							"type":"Feature",
							"properties": {
								val: Math.round(el.val / 100.0  * 9)
							},
							"geometry": {
								"type":"Polygon",
								"coordinates":[[
									[el.x,			el.y],
									[el.x + stride,	el.y],
									[el.x + stride,	el.y + stride],
									[el.x,			el.y + stride],
									[el.x,			el.y]
								]]
							}
						};

						source.features.push(val);
					}
					let ofg = new ol.format.GeoJSON({
						//dataProjection: 'urn:ogc:def:crs:EPSG::32615'
						dataProjection: 'EPSG:32615',
						featureProjection: 'EPSG:3857'
						//featureProjection: 'urn:ogc:def:crs:EPSG::32615'
					});			
					let ffs = ofg.readFeatures(source);
					
					DSS.layer.test.getSource().clear(true);
					DSS.layer.test.getSource().addFeatures(ffs);
				},
				
				failure: function(response, opts) {
					working = false;
				}
			});
		});
	},
	
	addTestBoxSelectWithImage: function() {
		let me = this;
		let extent = [0, 0, 1024, 968];
		let dragBox = new ol.interaction.DragBox({
			  condition: ol.events.condition.platformModifierKeyOnly
		});

		me.map.addInteraction(dragBox);
		
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
				imageExtent: extent
			})
		})
		let working = false;
		
		me.map.addLayer(DSS.layer.Image);
		
		dragBox.on('boxend', function() { // 'boxend
			if (working) return;
			working = true;
			
			let ex = dragBox.getGeometry().getExtent();			

			var obj = Ext.Ajax.request({
				url: location.href + 'fetch_image',
				jsonData: {
					"extent" : ex
				},
				timeout: 10000,
				success: function(response, opts) {
					var obj = JSON.parse(response.responseText);
					working = false;
					DSS.layer.Image.setSource(new ol.source.ImageStatic({
						url: obj.url,
						imageExtent: obj.extent
					}))
				},
				
				failure: function(response, opts) {
					working = false;
				}
			});
		});
	},
	
	//--------------------------------------------------------------------------
	addDrawTools: function(map) {
		
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
		})
		map.addInteraction(DSS.draw);	DSS.draw.setActive(true);

		document.addEventListener('keyup', function(e) {
			if (isDrawing && e.keyCode == 27) {
				DSS.draw.setActive(false);	DSS.draw.setActive(true);
			}
		});

		const snap = new ol.interaction.Snap({
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
		
		const modify = new ol.interaction.Modify({
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
		map.addInteraction(modify); modify.setActive(true);		
	},
	
	addWorkAreaMask: function() {
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
				[ -10210000, 5290000 ], 
				[ -10210000, 5460000 ], 
				[ -10010000, 5460000 ], 
				[ -10010000, 5290000 ], 
				[ -10210000, 5290000 ] 
			],[ // inner - counter-clockwise
				[ -10128000, 5358000 ],
				[ -10109000, 5358000 ], 
				[ -10109000, 5392000 ],
				[ -10128000, 5392000 ], 
				[ -10128000, 5358000 ]
			] 
		]];
		
		var spot = new ol.geom.MultiPolygon(multiPoly);
	    
		DSS.layer.mask.getSource().addFeature(new ol.Feature(spot));
		me.map.addLayer(DSS.layer.mask);                        
		
	}
	
	
});


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

