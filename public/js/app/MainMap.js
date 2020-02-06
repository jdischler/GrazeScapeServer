DSS.utils.addStyle('.popup-eye { opacity: 0.9; overflow: visible!important;background-color: #f8f7ef; border-radius: .5rem; border: 1px solid #999; box-shadow: 0 6px 6px rgba(0,0,0,0.5) }')
DSS.utils.addStyle('.popup-eye:after { transform: rotate(-45deg); overflow: visible!important; display: block; position: absolute; bottom: -0.32rem; left: calc(100px - 0.32rem); content: ""; background-color: #f8f7ef; width: 0.5rem; height: 0.5rem; border-left: 1px solid #999; border-bottom: 1px solid #999; box-shadow: -6px 6px 6px rgba(0,0,0,0.5) }')

//------------------------------------------------------------------------------
Ext.define('DSS.app.MainMap', {
//------------------------------------------------------------------------------
	extend: 'Ext.Component',
	alias: 'widget.main_map',
	
	style: 'background-color: rgb(150,160,120)',
	
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
		DSS.layer.hillshade = new ol.layer.Tile({
			visible: true,
			opacity: 0.8,
			source: new ol.source.TileJSON({
				url: 'https://api.maptiler.com/tiles/hillshades/tiles.json?key=' + me.OSM_KEY,
		        tileSize: 256,
		        crossOrigin: 'anonymous'
			})
		});
		//--------------------------------------------------------------		
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
		}
		//--------------------------------------------------------------
		DSS.layer.fields = new ol.layer.Vector({
			visible: false,
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector({
				format: new ol.format.GeoJSON(),
				url: 'assets/shapeFiles/exampleFields.geojson',
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
		})	;		
		
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
				DSS.layer.contour,
				DSS.layer.fields,
				DSS.layer.farms
			],//------------------------------------------------------------------------
			view: new ol.View({
				center: [-10118000,
					5375100],
				zoom: 12,
				maxZoom: 19,
				minZoom: 11,
				constrainOnlyCenter: true,
				extent:[-10126000, 5360000, -10110000, 5390000]
			})
		});
		
		me.ol_uid = false;
		
		//-----------------------------------------------------------
		me.map.on('click', function(e) {
			console.log(e)
			console.log(me.map.getEventCoordinate(e.originalEvent));
		});
		
		//-----------------------------------------------------------
		me.map.on('pointermove', function(e) {
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
					me.overlay.setPosition(center);
					me.popup.update('Area (ac): ' + area + '<br>' +
							'Boundary Dist (m): ' + len + '<br>' +
							'Cost ($1.25/m): $' + cost + '<br>');					
				}
			})
			
			if (!hitAny) {
				me.overlay.setPosition(false);
			}
			me.map.getViewport().style.cursor = cursor;
		});
	}
});
