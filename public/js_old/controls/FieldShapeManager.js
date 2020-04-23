
DSS.utils.addStyle('.button-margin { margin: 0.3rem 3.75rem 1.1rem;}')
DSS.utils.addStyle('.button-text-pad { padding: 0.5rem;}')

//------------------------------------------------------------------------------
Ext.define('DSS.controls.FieldShapeManager', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.field_shape_mgr',

	layout: DSS.utils.layout('vbox', 'center', 'stretch'),
	cls: 'section',
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;
		
		Ext.applyIf(me, {
			defaults: {
				margin: '2rem',
			},
			items: [{
				xtype: 'component',
				cls: 'section-title',
				html: 'Field Shapes'
			},{ 
				xtype: 'container',
				layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				items: [{
					xtype: 'component',
					cls: 'information',
					html: 'Toggle field draw and edit mode'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Edit Fields',
					DSS_toggledText: 'Disable Drawing',
					toggleGroup: 'field_draw',
					toggleHandler: function(self, active) {
						if (active) {
							me.activateFieldDrawMode();
							self.DSS_originalText = self.getText();
							self.setText(self.DSS_toggledText);
						}
						else {
							me.deactivateFieldDrawMode()
							self.setText(self.DSS_originalText);
						}
					}
				},{
					xtype: 'component',
					cls: 'information',
					html: 'Advanced edit mode lets users split fields but only operates on a single selected field at a time'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Advanced Edit Mode',
				},{
					xtype: 'component',
					margin: '16 0 0 0',
					cls: 'information',
					html: 'Upload fields from a file'
				},{
					xtype: 'button',
					cls: 'button-text-pad',
					componentCls: 'button-margin',
					text: 'Upload Fields',
				}]
			}]
		});
		
		me.callParent(arguments);
		me.createFieldStyes();
	},
	
	// Complicated field style components....
	//-----------------------------------------------------
	createFieldStyes: function() {
		let me = this;
		
		me.DSS_defaultStyle = new ol.style.Style({
			fill: new ol.style.Fill({
				color: 'rgba(48, 32, 0, 0.33)'
			}),
			stroke: new ol.style.Stroke({
				color: 'rgba(255,204,39,0.75)',
				width: 1
			})
		});
		//---------------------------------------
		me.DSS_defaultFill = new ol.style.Style({
			fill: new ol.style.Fill({
				color: 'rgba(48, 32, 0, 0.33)'
			})
		});
		//---------------------------------------
		me.DSS_farStroke = new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: 'rgba(255,204,39,0.75)',
				width: 1
			})
		});
		//---------------------------------------
		me.DSS_baseStroke = new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: '#ffcc33',
				width: 2
			})
		});
		//---------------------------------------
		me.DSS_smallCircle = new ol.style.Style({
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
		});
		//---------------------------------------
		me.DSS_bigCircle = new ol.style.Style({
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
		});
	},
	
	//-----------------------------------------------------
	activateFieldDrawMode: function() {
		
		let me = this;
		
		DSS.MapState.showFields(1);
		DSS.MapState.fieldDrawMode();

		DSS.mouseMoveFunction = undefined;
		DSS.mapClickFunction = undefined;
		
		// FIXME: TODO:
		DSS.activeFarm =  DSS.activeFarm || 27;
		
		DSS.drawEndEvent = function(evt, feature) {
			me.addField(feature, DSS.activeFarm);
		}
		
		// TODO: FIXME: should check for a field ID on ModifyStart. Otherwise newly drawn fields that haven't persisted
		//	and returned their ID for binding will not properly work on modifyEnd
		DSS.modifyEndEvent = function(evt, segments) {
			me.modifyField(segments);
		} 
	},
	
	//-----------------------------------------------------
	deactivateFieldDrawMode: function() {
		
		// Disable draw/edit
		DSS.MapState.showFields(0.8);
		DSS.draw.setActive(false);
		DSS.modify.setActive(false);
		DSS.fieldStyleFunction = undefined;	DSS.layer.fields.changed();
		
		// Enable selection
		DSS.selectionTool.getFeatures().clear();
		DSS.selectionTool.setActive(true);
		
		DSS.mapClickFunction = undefined;
		
		DSS.map.getViewport().style.cursor = '';
		
		DSS.mouseMoveFunction = function(evt) {
			let pixel = DSS.map.getEventPixel(evt.originalEvent);
			let fs = DSS.map.getFeaturesAtPixel(pixel);
			let cursor = '';
			let hitAny = false;
			fs.forEach(function(f) {
				let g = f.getGeometry();
				if (!g) return;
				if (g.getType() === "Polygon") {
					cursor = 'pointer';
					hitAny = true;
					let fmt = Ext.util.Format.number;
					let area 	= fmt(ol.sphere.getArea(g) / 4046.856, '0,0.#');
					let len 	= ol.sphere.getLength(g);
					let cost 	= fmt(len * 1.25, '0,0.##');
					len 		= fmt(len, '0,0.#');
					console.log(f);
					let extent = g.getExtent();
					let center = ol.extent.getCenter(extent);
					center[1] += (ol.extent.getHeight(extent) / 2);
					center = g.getClosestPoint(center);
					DSS.map.getView().cancelAnimations();
					DSS.popupOverlay.setPosition(center);
					DSS.popupContainer.update('Area (ac): ' + area + '<br>' +
							'Boundary Dist (m): ' + len + '<br>' +
							'Cost ($1.25/m): $' + cost + '<br>');					
				}
			})
			
			if (!hitAny) {
				DSS.popupOverlay.setPosition(false);
			}
			DSS.map.getViewport().style.cursor = cursor;
		}
	},
	
	//-----------------------------------------------------
	addField: function(feature, farm_id) {
		
		let wkt = new ol.format.WKT();
		
		var obj = Ext.Ajax.request({
			url: location.href + 'add_field',
			jsonData: {
				farm_id: farm_id,
				wkt: wkt.writeFeature(feature,{decimals:1}) 
			},
			timeout: 30 * 1000, // 30 seconds
			
			success: function(response, opts) {
				
				var obj= JSON.parse(response.responseText);
				feature.setProperties({'f_id': obj.f_id});
			},
			
			failure: function(respose, opts) {
				console.log('fieldShape.addField failure');
			}
		});
		
	},

	// segments are an array [changed,changed,...] where each "changed" is an array of items [object, 0], [object, 1]
	//	and object has a feature which is what we want to save. Probably use a hashmap with the f_id so we only collect the
	//	features once...
	//-----------------------------------------------------
	modifyField: function(segments) {
		
		// TODO: maybe these are light-weight, but otherwise maybe cache one of these writers?
		let wkt = new ol.format.WKT();
		
		let fs = {};
		for (let idx = 0; idx < segments.length; idx++) {
			let feat = segments[idx][0].feature;
			fs[('_' + feat.get("f_id"))] = feat;
		}
		let finalSet = []
		for (f in fs) {
			let realF  = fs[f];
			finalSet.push({
				wkt: wkt.writeFeature(realF,{decimals:1}),
				field_id: realF.get("f_id")
			})
		};
		
		console.log(finalSet);
		var obj = Ext.Ajax.request({
			url: location.href + 'modify_fields',
			jsonData: finalSet,
			timeout: 30 * 1000, // 30 seconds
			
			success: function(response, opts) {
				
				var obj= JSON.parse(response.responseText);
				console.log(obj);
			},
			
			failure: function(respose, opts) {
				console.log('fieldShape.modifyField failure');
			}
		});
		
	},
	

});
