/*
DSS.dragBox
DSS.layer.ModelResult
*/

// Helper for computing a model (or viewing data) inside of a user-defined box
//------------------------------------------------------------------------------
Ext.define('DSS.map.BoxModel', {
//------------------------------------------------------------------------------
	extend: 'Ext.Base',
	alias: 'widget.box_model',
	
	//-------------------------------------------------------------------------
	instantiate: function(map) {
		let me = this;
		
		DSS.dragBox = new ol.interaction.DragBox({
			condition: ol.events.condition.platformModifierKeyOnly
		});

		map.addInteraction(DSS.dragBox);
		
		// EVIL:
		// Hijacking prepare frame function so that image layer can update during animations, otherwise it looks really bad
		ol.renderer.canvas.ImageLayer.prototype.prepFrame = ol.renderer.canvas.ImageLayer.prototype.prepareFrame; 
		ol.renderer.canvas.ImageLayer.prototype.prepareFrame = function(frameState) {
			frameState.viewHints[0] = frameState.viewHints[1] = 0
			return this.prepFrame(frameState); 
		}

		// raster image result
		DSS.layer.ModelResult = new ol.layer.Image({
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.ImageStatic({
				imageSmoothing: false,
				projection: 'EPSG:3071',
				// Something is required here or there will be an exception whilst trying to draw this layer
				imageExtent: [
					44240,328120,
					448350,335420
				],
			})
		});
		//  box model outline, area of computation
		DSS.layer.ModelBox = new ol.layer.Vector({
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: new ol.source.Vector(),
			style: new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: 'rgba(255,255,32,0.5)',
					width: 1,
					lineDash: [2,4]
				}),
				fill: new ol.style.Fill({
					color: 'rgba(64,64,64,0.2)'
				})
			})
		});
		DSS.layer.ModelGroup = new ol.layer.Group({
			opacity: DSS.layer['inspector:opacity'],
			visible: DSS.layer['inspector:visibility'],
			layers: [
				DSS.layer.ModelBox,
				DSS.layer.ModelResult,
			]
		})
		map.addLayer(DSS.layer.ModelGroup);
		
		DSS.dragBox.on('boxend', function() {

			let ex = DSS.dragBox.getGeometry().getExtent();	
			
			let pt1 = ol.proj.transform([ex[0],ex[1]], 'EPSG:3857', 'EPSG:3071'),
				pt2 = ol.proj.transform([ex[2],ex[3]], 'EPSG:3857', 'EPSG:3071');

			ex = [pt1[0], pt1[1], pt2[0], pt2[1]];
			
			AppEvents.triggerEvent('set_inspector_bounds', ex);
			// FIXME:
			//DSS.StatsPanel.computeResults(ex, DSS.layer.ModelResult);
		});
		
		return me;
	},
	
});
