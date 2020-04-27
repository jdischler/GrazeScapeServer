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
		
		setTimeout(function() {
			// EVIL:
			// Hijacking prepare frame function so that image layer can update during animations, otherwise it looks really bad
			ol.renderer.canvas.ImageLayer.prototype.prepFrame = ol.renderer.canvas.ImageLayer.prototype.prepareFrame; 
			ol.renderer.canvas.ImageLayer.prototype.prepareFrame = function(frameState) {
				frameState.viewHints[0] = frameState.viewHints[1] = 0
				return this.prepFrame(frameState); 
			}
		}, 2000);

		DSS.layer.ModelResult = new ol.layer.Image({
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			opacity: 0.7,
			visible: false,
			source: new ol.source.ImageStatic({
				projection: 'EPSG:3071'
			})
		})

		map.addLayer(DSS.layer.ModelResult);
		
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
