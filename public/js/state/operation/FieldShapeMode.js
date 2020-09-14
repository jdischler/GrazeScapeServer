
//------------------------------------------------------------------------------
Ext.define('DSS.state.operation.FieldShapeMode', {
//------------------------------------------------------------------------------
	extend: 'Ext.button.Segmented', // Ext.container
	alias: 'widget.state_field_shape_mode',
	
	singleton: true,
	
//	padding: '0 6 6 6',
	floating: true,
	shadow: false,
	hidden: false,
	
	style: 'border-radius: 4px; box-shadow: 0 4px 8px rgba(0,0,0,0.5); background-color: rgba(0,0,0,0.5)',
	layout: DSS.utils.layout('hbox', 'start'),
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		Ext.applyIf(me, {
			defaults: {
 				xtype: 'button',
				toggleGroup: 'field-shape-mode',
				padding: '4 0 0 0',
				height: 30,
				allowDepress: false,
				frame: false
			},
			items: [{
				text: 'Draw',
				tooltip: 'Draw field shapes',
				width: 68,
				toggleHandler: function(self, pressed) {
					if (pressed) {
						DSS.MapState.fieldDrawMode();
						DSS.mouseMoveFunction = undefined;
						DSS.mapClickFunction = undefined;
						DSS.drawEndEvent = function(evt, feature) {
							me.addField(feature, DSS.activeFarm);
						}
						
						// TODO: FIXME: should check for a field ID on ModifyStart. Otherwise newly drawn fields that haven't persisted
						//	and returned their ID for binding will not properly work on modifyEnd
						DSS.modifyEndEvent = function(evt, segments) {
							console.log("field Shaped modify", evt)
							me.modifyField(segments);
						} 
						
						DSS.DrawFieldShapes.addModeControl();
					}
					else {
						DSS.MapState.disableFieldDraw();
						DSS.draw.setActive(false);
						DSS.modify.setActive(false);
						DSS.fieldStyleFunction = undefined;	DSS.layer.fields.changed();
					}
				}
			},{
				text: 'Split',
				tooltip: 'Split field shapes',
				width: 64,
				toggleHandler: function(self, pressed) {
					if (pressed) {
						DSS.SplitFieldShapes.addModeControl();
					}
				}
			},{
				text: 'Join',
				tooltip: 'Merge adjacent field shapes',
				width: 64,
				toggleHandler: function(self, pressed) {
					if (pressed) {
						DSS.JoinFieldShapes.addModeControl();
					}
				}
			},{
				text: 'Delete',
				tooltip: 'Delete field shapes',
				width: 78,
				toggleHandler: function(self, pressed) {
					if (pressed) {
						DSS.DeleteFieldShapes.addModeControl(me)
					}
					else {
						DSS.mouseMoveFunction = undefined;
						DSS.mapClickFunction = undefined;
					}
				}
			},{
				html: '<i class="fas fa-search"></i>',
				tooltip: 'Activate Inspector <i class="fas fa-search accent-text"></i> mode',
				width: 48,
				pressed: true,
				toggleHandler: function(self, pressed) {
					if (pressed) {
						DSS.Inspector.addModeControl()
					}
				}
			}]
		});
		
		me.callParent(arguments);
		
		me.showAt(400, -38); me.setHidden(true);
		
		AppEvents.registerListener('show_field_shape_mode', function() {
			let om = Ext.getCmp('ol_map');
			let x = om.getX() + (om.getWidth() - /*me.getWidth()*/258) * 0.5;
			me.setHidden(false);
			me.setX(x);
			me.stopAnimation().animate({
				duration: 300,
				to: {
					y: -4
				}
			})
		})
		AppEvents.registerListener('hide_field_shape_mode', function() {
			me.stopAnimation().animate({
				duration: 300,
				to: {
					y: -38
				},
				callback: function() {
					me.setHidden(true);
				}
			})
		})
		AppEvents.registerListener('map_resize', function() {
			if (!me.isHidden()) {
				let om = Ext.getCmp('ol_map');
				me.setX(om.getX() + (om.getWidth() - me.getWidth()) * 0.5);
			}
		})
	},
	
	//-----------------------------------------------------
	addField: function(feature, farm_id) {
		
		let wkt = new ol.format.WKT();
		
		var obj = Ext.Ajax.request({
			url: location.origin + '/add_field',
			jsonData: {
				farm_id: farm_id,
				wkt: wkt.writeFeature(feature,{decimals:1}),
				field_settings: DSS.viewModel.drawAndApply.getData()
			},
			timeout: 30 * 1000, // 30 seconds
			
			success: function(response, opts) {
				
				var obj= JSON.parse(response.responseText);
				console.log(obj);
				feature.setProperties({'f_id': obj.f_id});
				DSS_RefilterDelayed(25);
				DSS.layerSource.fields.refresh();
//				DSS.layer.cropOverlay.changed(); //needs to be "poked" after add??
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
			url: location.origin + '/modify_fields',
			jsonData: finalSet,
			timeout: 30 * 1000, // 30 seconds
			
			success: function(response, opts) {
				var obj= JSON.parse(response.responseText);
				DSS_RefilterDelayed(25);
			},
			
			failure: function(respose, opts) {
				console.log('fieldShape.modifyField failure');
			}
		});
		
	},
	
	// Takes an array of fields in [fID#, fID#, fID#...] format
	//-----------------------------------------------------
	deleteFields: function(fields, farm_id) {
		
		let wkt = new ol.format.WKT();
		
		let d_fields = [];
		Ext.each(fields, function(item) {
			d_fields.push(item.f_id);
		})
		console.log(d_fields);
		
		var obj = Ext.Ajax.request({
			url: location.origin + '/delete_fields',
			jsonData: {
				farm_id: farm_id,
				fields: d_fields
			},
			timeout: 30 * 1000, // 30 seconds
			
			success: function(response, opts) {
				DSS.layerSource.fields.refresh();
//				DSS.layerSource.fields.refresh();
//				DSS.layer.cropOverlay.changed(); //needs to be "poked" after add??
			},
			
			failure: function(respose, opts) {
				console.log('fieldShape.deleteField failure');
				alert("Delete error");
			}
		});
		
	},
	
});
