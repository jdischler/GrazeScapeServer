/*
DSS.draw
DSS.drawEndEvent(evt, feature, target, type)

DSS.modify
DSS.modifyEndEvent(evt, features, target, type) // note that features is plural
DSS.snap
*/

// Helper to add Draw, Snap, Modify widgets
//------------------------------------------------------------------------------
Ext.define('DSS.map.DrawAndModify', {
//------------------------------------------------------------------------------
	extend: 'Ext.Base',
	alias: 'widget.draw_modify',
	
	//-------------------------------------------------------------------------
	instantiate: function(map, fieldLayerSource) {
		let me = this;
		
		// Ordering is important. Inserting modify first makes it *lower* priority than drawing
		me.addModifyTool(map, fieldLayerSource);
		me.addDrawTool(map, fieldLayerSource);
		me.addSnapTool(map, fieldLayerSource);
		
		return me;
	},
	
	//--------------------------------------------------------------------------
	addModifyTool: function(map, fieldLayerSource) {

		DSS.modify = new ol.interaction.Modify({
			source: fieldLayerSource,
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
		
		map.addInteraction(DSS.modify); 
		DSS.modify.setActive(false);

		// This does not work as expected. All features in the collection are returned during modification/edit events, regardless
		//	of whether they were edited.
/*		DSS.modify.on('modifyend', function(evt) {
			if (DSS.modifyEndEvent) {
				console.log("modifyend was", evt)
				DSS.modifyEndEvent(evt, evt.features, evt.target, evt.type);
			}
		})
*/
		// Evil: -------------------------------------------------------------------------------------------------------------------
		//	This directly modifies the OL Modify interaction since it stupidly doesn't return only the features that have been edited
		//	 - it would normally always return every feature
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
		// Evil:-------------------------------------------------------------------------------------------------------------------
		//	This directly modifies the OL Modify interaction since it also doesn't return only modified features during vertex removal
		//	 - it would normally always return every feature
		DSS.modify.removePoint = function() {
			if (this.lastPointerEvent_ && this.lastPointerEvent_.type != "pointerdrag") {
				const evt = this.lastPointerEvent_;
				this.willModifyFeatures_(evt);
				const copySegs = this.dragSegments_.slice(); // remove vertex unhelpfully blows away dragSegments_
				const removed = this.removeVertex_();
				if (DSS.modifyEndEvent) {
					DSS.modifyEndEvent(evt, copySegs);
				}
				this.modified_ = false;
				return removed;
			}
			return false;
		}

	},
	
	//--------------------------------------------------------------------------
	addDrawTool: function(map, fieldLayerSource) {
		
		DSS.draw = new ol.interaction.Draw({
			source: fieldLayerSource,
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
		
		map.addInteraction(DSS.draw);	
		DSS.draw.setActive(false);
		
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

		document.addEventListener('keyup', function(e) {
			if (e.keyCode == 27) {
				if (isDrawing && DSS.draw.getActive()) {
					DSS.draw.setActive(false);
					DSS.draw.setActive(true);
					isDrawing = false;
				}
			}
		});
	},
	
	//----------------------------------------------------------------------------------------
	addSnapTool: function(map, fieldLayerSource) {
		
		DSS.snap = new ol.interaction.Snap({
			source: fieldLayerSource,
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
		map.addInteraction(DSS.snap);
	}

	
});
