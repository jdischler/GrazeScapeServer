
//------------------------------------------------------------------------------
Ext.define('DSS.state.MapStateTools', {
//------------------------------------------------------------------------------
	
	extend: 'Ext.Base',
    alternateClassName: 'DSS.MapState',
    singleton: true,	

	requires: [
		'DSS.map.Legend'
	],
	
    // Style elements
    //---------------------------------------------------------
    DSS_pinMarker: new ol.style.Style({
		image: new ol.style.Icon({
			anchor: [0.5, 1],
			size: [96,96],
			scale: 0.4,
			src: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGAAAABgCAYAAADimHc4AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyhpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTQ1IDc5LjE2MzQ5OSwgMjAxOC8wOC8xMy0xNjo0MDoyMiAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTkgKE1hY2ludG9zaCkiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QkIwM0UwNDY0RjU3MTFFQUJBREE5QkJBNkZDRjRDMkQiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QkIwM0UwNDc0RjU3MTFFQUJBREE5QkJBNkZDRjRDMkQiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpGQUZFMzY3RjRGNTUxMUVBQkFEQTlCQkE2RkNGNEMyRCIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpGQUZFMzY4MDRGNTUxMUVBQkFEQTlCQkE2RkNGNEMyRCIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PporYv0AAA10SURBVHja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"	
		})
	}),
	
	// Complicated field style components....
    //---------------------------------------------------------
	DSS_fieldStyles: {
		// Full minimal style
		defaultStyle: new ol.style.Style({
			fill: new ol.style.Fill({
//				color: 'rgba(48, 32, 0, 0.45)'
				color: 'rgba(0, 0, 0, 0.2)'
			}),
			stroke: new ol.style.Stroke({
				color: 'rgba(255,204,32,0.9)',
				width: 1
			})
		}),
		// just minimal fill style
		defaultFill: new ol.style.Style({
			fill: new ol.style.Fill({
				color: 'rgba(0, 0, 0, 0.2)'
//				color: 'rgba(48, 32, 0, 0.33)'
			})
		}),
		baseStroke: new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: '#ffcc33',
				width: 2
			})
		}),
		farStroke: new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: 'rgba(255,204,39,0.75)',
				width: 1
			})
		}),
		smallCircle: new ol.style.Style({
			image: new ol.style.Circle({
				radius: 2,
				fill: new ol.style.Fill({
					color: 'rgba(255,204,39,0.5)'
				}),
				stroke: new ol.style.Stroke({
					color: 'rgba(0,0,0,0.75)',
					width: 1
				}),
			}),
			geometry: function(feature) {
				var coordinates = feature.getGeometry().getCoordinates()[0];
				return new ol.geom.MultiPoint(coordinates);
			}
		}),
		bigCircle: new ol.style.Style({
			image: new ol.style.Circle({
				radius: 4,
				fill: new ol.style.Fill({
					color: 'rgba(255,204,39,0.5)'
				}),
				stroke: new ol.style.Stroke({
					color: 'rgba(0,0,0,0.5)',
					width: 2
				}),
			}),
			geometry: function(feature) {
				var coordinates = feature.getGeometry().getCoordinates()[0];
				return new ol.geom.MultiPoint(coordinates);
			}
		})
	},
	
	// Opacity default: 0.5
    //----------------------------------------
    setPinMarker: function(ol_pointCoord, opacity) {
    
    	opacity = opacity || 0.5;
    	
		DSS.markerStyleFunction = function(feature) {
			return DSS.MapState.DSS_pinMarker;
		};
		
		let src = DSS.layer.markers.getSource();
		src.clear();
		src.addFeature(new ol.Feature({
			geometry: new ol.geom.Point(ol_pointCoord)
		}));
		
		DSS.layer.markers.setOpacity(opacity);
		DSS.layer.markers.setVisible(true);		
    },
    
	// Opacity default: 1
    //----------------------------------------
    showFields: function(opacity) {
    	
    	opacity = opacity || 1;
		DSS.layer.fields.setOpacity(opacity);
		DSS.layer.fields.setVisible(true);
    },
    
    // Opacity defaults to opacity for showFields()
    //-------------------------------------------------------------
    showFieldsForFarm: function(farmId, opacity) {
    	
		DSS.layer.fields.getSource().setUrl("get_fields?farm="+ farmId);
		DSS.layer.fields.getSource().refresh();
		this.showFields(opacity);
    },
    
    //
    //----------------------------------------
    disableFieldDraw: function() {
    	
		DSS.draw.setActive(false);
		DSS.snap.setActive(false);
		DSS.modify.setActive(false);    	
    },
    
    //----------------------------------------
    fieldDrawMode: function() {
    	
    	var me = this;
    	
    	// Draw tools on
		DSS.draw.setActive(true);
		DSS.snap.setActive(true);
		DSS.modify.setActive(true);
    	
		// Draw style on
		DSS.fieldStyleFunction = function(feature, resolution) {
		    if (resolution <= 2) {
	    		return [
	    			me.DSS_fieldStyles.defaultFill, 
	    			me.DSS_fieldStyles.baseStroke, 
	    			me.DSS_fieldStyles.bigCircle
	    		];
		    }
		    if (resolution <= 7) {
	    		return [
	    			me.DSS_fieldStyles.defaultFill, 
	    			me.DSS_fieldStyles.baseStroke, 
	    			me.DSS_fieldStyles.smallCircle
	    		];
		    }
		    if (resolution <= 17) {
		    	return [
		    		me.DSS_fieldStyles.defaultFill, 
		    		me.DSS_fieldStyles.baseStroke
		    	];
		    }
		    return me.DSS_fieldStyles.defaultStyle;
		}
		DSS.layer.fields.changed(); //needs to be "poked" after style change...
		
		// Selection can't be on
		DSS.selectionTool.getFeatures().clear();
		DSS.selectionTool.setActive(false);
    },
    
    //-------------------------------------------------------------
    mouseoverFarmHandler: function(evt) {
    	
		let lastF = undefined, lastFp = undefined;
		
		return function(evt) {
			let pixel = DSS.map.getEventPixel(evt.originalEvent);
			let fs = DSS.map.getFeaturesAtPixel(pixel);
			let cursor = '';
			let hitAny = false;
			for (let idx = 0; idx < fs.length; idx++) {
				let f = fs[idx];
				let g = f.getGeometry();
				if (!g) return;
				if (g.getType() === "Point") {
					if (f.get('name') != undefined) {
						cursor = 'pointer';
						hitAny = true;
						if (lastFp !== f) {
							DSS.popupOverlay.setPosition(g.getCoordinates());
							DSS.popupContainer.update('Farm: ' + f.get('name') + '<br>' +
									'Owner: ' + f.get('owner') + '<br>' +
									'Address: ' + f.get('address') + '<br>');
							lastFp = f;
						}
						if (lastF !== f) {
							DSS.layer.fields.getSource().setUrl("get_fields?farm="+ f.get("id"));
							DSS.layer.fields.getSource().refresh();
							DSS.MapState.showFields(0.9);
							lastF = f;
						}
						break;
					}
				}
			}
			if (!hitAny) {
				DSS.popupOverlay.setPosition(false);
			//	lastF = undefined;
				lastFp = undefined;
			}
			DSS.map.getViewport().style.cursor = cursor;
		}
    },
    
    // TODO: deal with multiple overlapping bits
	//	console.log("TODO: many farms at the click location: " + fs.length);
    //-------------------------------------------------------------
    clickActivateFarmHandler: function(evt) {
    	
    	let me = this;
    	
		return function(evt) {
			let pixel = DSS.map.getEventPixel(evt.originalEvent);
			let fs = DSS.map.getFeaturesAtPixel(pixel);
			for (let idx = 0; idx < fs.length; idx++) {
				let f = fs[idx];
				let g = f.getGeometry();
				if (g && g.getType() === "Point") {
					DSS.activeFarm = f.get("id");
					
					let pos = g.getFirstCoordinate()
					me.setPinMarker(pos);
					let ex = ol.extent;
					let extent = [pos[0], pos[1], pos[0], pos[1]];
					DSS.layer.fields.getSource().forEachFeature(function(f) {
						console.log(f.getGeometry().getExtent());
						extent = ex.extend(extent, f.getGeometry().getExtent());
					});
					ex.buffer(extent, 250, extent);
					me.zoomToRealExtent(extent);
					//me.zoomToExtent(g.getFirstCoordinate(),15);
					// if results were already being computed (extents chosen and model), then trigger a recompute
				//	DSS.StatsPanel.computeResults(undefined, DSS.layer.ModelResult);
					DSS.map.getViewport().style.cursor = '';
					AppEvents.triggerEvent('activate_operation')
					console.log(DSS.layer.fields.getSource());
					DSS.ApplicationFlow.instance.showManageOperationPage(f.get("name"));
					break;
				}
			}
		}
    },

    // FIXME: TODO: ideally zoom to extent instead?
    //-------------------------------------------------------------
    zoomToExtent: function(center_, zoom_) {
    	
    	center_ = center_ || [-10118000, 5375100];
    	zoom_ = zoom_ || 12;
    	
    	DSS.map.getView().animate({
    		center: center_,
			zoom: zoom_,
    	});
    },
    
    zoomToRealExtent: function(extent) {
    	DSS.map.getView().fit(extent, {duration: 1000});
    	console.log(extent);
    },
    
    //-------------------------------------------------------------
    mapResize: function(mapComponent) {
    	var me = this;
    	if (me.DSS_legend) {
    		let cmp = Ext.getCmp('ol_map');
    		me.DSS_legend.setX(cmp.getX() + cmp.getWidth() - (me.DSS_legend.getWidth() + 8))
    	}	
    },
    
    //-------------------------------------------------------------
    showContinuousLegend: function(paletteArray, valuesArray) {

    	var me = this;
    	
		me.destroyLegend();
		
		me.DSS_legend = Ext.create('DSS.map.Legend', {
			DSS_colors: paletteArray,
			DSS_values: valuesArray,
			DSS_keys: false
		});
		
		let cmp = Ext.getCmp('ol_map');
		me.DSS_legend.showAt(DSS_viewport.getWidth() - 120, 0);
		me.DSS_legend.setX(cmp.getX() + cmp.getWidth() - (me.DSS_legend.getWidth() + 8))
    },
    
    //-------------------------------------------------------------
    showClassifiedLegend: function(keyArray) {

    	var me = this;
    	
		me.destroyLegend();
		
		me.DSS_legend = Ext.create('DSS.map.Legend', {
			style: 'opacity: 0.8;background-color: rgba(0,0,0,0.5); border: 1px solid rgba(0,0,0,0.1); border-radius: 4px; box-shadow: 0 6px 8px rgba(0,0,0,0.2);',
			height: 380,
			DSS_keys: keyArray,
			DSS_colors: false,
			DSS_values: false
		});
		
		let cmp = Ext.getCmp('ol_map');
		me.DSS_legend.showAt(DSS_viewport.getWidth() - 120, 0);
		me.DSS_legend.setX(cmp.getX() + cmp.getWidth() - (me.DSS_legend.getWidth() + 8))
    },

    
    //-------------------------------------------------------------
    destroyLegend: function() {
    	
    	var me = this;
		if (me.DSS_legend) me.DSS_legend.destroy();
    }
});
