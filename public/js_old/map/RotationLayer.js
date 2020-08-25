
//------------------------------------------------------------------------------
Ext.define('DSS.map.RotationLayer', {
//------------------------------------------------------------------------------
	extend: 'Ext.Base',
	alias: 'widget.rotation_layer',
	
	//-------------------------------------------------------------------------
	instantiate: function(map) {
		let me = this;
		
		DSS['rotationStyles'] = { };
		
		DSS.layerSource.fields
		
		let canvas = document.createElement('canvas');
		let context = canvas.getContext('2d');
		
		let createPattern = function(imgSrc, cropCode, strokeColor) {
			let img = new Image();
			img.onload = function() {
				let pattern = context.createPattern(img, 'repeat');
				DSS.rotationStyles[cropCode] = new ol.style.Style({
					stroke: new ol.style.Stroke({
						color: strokeColor,
						width: 1
					}),
					fill: new ol.style.Fill({
						color: pattern
					}),
					zIndex: 0
				});
			}
			img.src = imgSrc;			
		};
		
		createPattern('/assets/images/dairy_rotation_1.png', 	'D1', '#a19');
		createPattern('/assets/images/dairy_rotation_2.png', 	'D2', '#319');
		createPattern('/assets/images/pasture.png', 			'PS', '#380');
		createPattern('/assets/images/dry_lot.png', 			'DL', '#a11');
		createPattern('/assets/images/continuous_corn.png',		'CC', '#770');
		createPattern('/assets/images/cash_grain.png',			'CG', '#079');
		
		DSS.layer.cropOverlay = new ol.layer.Vector({
			visible: DSS.layer['crop:visible'],
			opacity: DSS.layer['crop:opacity'],
			updateWhileAnimating: true,
			updateWhileInteracting: true,
			source: DSS.layerSource.fields,
			style: function(feature, resolution) {
				if (feature && feature.getProperties()) {
					let rot = feature.getProperties()['rotation']; 
					if (rot && DSS.rotationStyles[rot]) {
						return DSS.rotationStyles[rot];
					}
				}
			}
		});	

		map.addLayer(DSS.layer.cropOverlay);
		return me;
	},

});
