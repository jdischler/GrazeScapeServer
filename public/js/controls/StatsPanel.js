DSS.utils.addStyle('.info-panel { border-left: 1px solid #222;  border-bottom: 1px solid rgba(0,0,0,0.25); background-color: #555; background-repeat: no-repeat; background-image: linear-gradient(to right, #333 0%, #3f3f3f 25%, #4a4a4a 50%, #535353 80%, #555 100%); background-size: 2rem 100%;');
DSS.utils.addStyle('.x-resizable-handle-west {width: 6px; background-color: rgba(255,255,255,0.25)}');
DSS.utils.addStyle('.box-label-cls {color: #eee; text-shadow: 0 1px rgba(0,0,0,0.2),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');
DSS.utils.addStyle('.small {color: #38b; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');
DSS.utils.addStyle('.light-color {color: #bbb; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2);}');



//--------------------------------------------------------------------------
var filter_task = new Ext.util.DelayedTask(function(){
	DSS.StatsPanel.computeResults(undefined, DSS.layer.ModelResult);
});

var DSS_Refilter = function() {
	filter_task.delay(0);
}
var DSS_RefilterDelayed = function(msDelay) {
	filter_task.delay(msDelay);
}

//------------------------------------------------------------------------------
Ext.define('DSS.controls.StatsPanel', {
//------------------------------------------------------------------------------
	extend: 'Ext.Container',
	alias: 'widget.stats_panel',
    alternateClassName: 'DSS.StatsPanel',
    singleton: true,	
	
	requires: [
		'DSS.controls.Inspector_Limiter',
		'DSS.controls.Inspector_PLoss',
		'DSS.controls.Management'
	],
	
	width: 0,
	region: 'east',
	cls: 'info-panel',
	resizable: {
		dynamic: true,
		maxWidth: 320,
	},
	resizeHandles: 'w',
	maxWidth: 320,
	padding: '8 6',
	scrollable: 'y',
	
	layout: DSS.utils.layout('vbox', 'start', 'stretch'),
	
	listeners: {
		afterrender: function(self) {
			setTimeout(function() {
				self.animate({
					dynamic: true,
					to: {
						width: 220
					},
					callback: function() {
						self.setMinWidth(220);
						// ooof, the Ext resizer doesn't seem to realize when its resize target has a min/max width change
						self.resizer.resizeTracker.minWidth = 220;
					}
				})
			}, 2000);
		}
	},
	
	//--------------------------------------------------------------------------
	initComponent: function() {
		let me = this;

		//-------------------------------------------------------------
		function makeInspectionMode(buttonText, modeName, disabled, controls) {
			return {
				padding: '0 0 0 16',
				text: buttonText,
				disabled: disabled,
				handler: function(self) {
					self.parentMenu.DSS_ownerButton.setText(buttonText);
					me.setMode(modeName);
					me.down('#mode-options').removeAll();
					if (controls) {
						me.down('#mode-options').add({
							xtype: controls,
							DSS_InspectorParent: me
						})
					}
				}
			}
		};
		
		Ext.applyIf(me, {
			defaults: {
				xtype: 'component',
			},
			items: [{
				cls: 'section-title',
				style: 'color: #ddd; text-shadow: 0 1px rgba(0,0,0,0.2)',
				html: 'Map Stats <i class="fas fa-chart-area small"></i>',
			},{
				DSS_ratio: 0.6,
				margin: 4,
				minHeight: 64,
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				listeners: {
					resize: function(self, w, h) {
						if (self.DSS_ratio) {
							self.setHeight(Math.floor(w * self.DSS_ratio));
						}
					}
				},
/*			},{
				DSS_ratio: 0.5,
				margin: 4,
				minHeight: 64,
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				listeners: {
					resize: function(self, w, h) {
						if (self.DSS_ratio) {
							self.setHeight(Math.floor(w * self.DSS_ratio));
						}
					}
				}*/
			},{
				flex: 1
			/*},{
				xtype: 'container',
				hidden: false,
				layout: 'fit',
				margin: 4,
				padding: 4,
				
				//layout: DSS.utils.layout('vbox', 'center', 'stretch'),
				style: 'background-color: white; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',

				items: [{
					xtype: 'management'
				}]*/
			},{
				flex: 1
			},{
				cls: 'section-title',
				style: 'color: #ddd; text-shadow: 0 1px rgba(0,0,0,0.2)',
				html: 'Inspector <i class="fas fa-search small"></i>',
			},{
				xtype: 'container',
				style: 'background-color: #666; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); border-top-color:rgba(255,255,255,0.25); border-bottom-color:rgba(0,0,0,0.3); box-shadow: 0 3px 6px rgba(0,0,0,0.2)',
				layout: DSS.utils.layout('vbox', 'start', 'stretch'),
				//height: 128,
				margin: 4,
				padding: 8,
				items: [{
					xtype: 'component',
					style: 'text-align: center; color: #ccc',
					html: 'Mode'
				},{
					xtype: 'button',
					text: 'Crop Yield',
					listeners: {
						afterrender: function(self) {
							self.setMenu({
								DSS_ownerButton: self,
								plain: true,
								width: 160,
								items: [{
									text: 'Models', disabled: true,
									style: 'border-bottom: 1px solid rgba(0,0,0,0.2);'
								},
									makeInspectionMode('Soil Loss', 	'soil-loss', true, 'inspector_p_loss'),
									makeInspectionMode('P-Loss', 		'p-loss', 	false, 'inspector_p_loss'),
									makeInspectionMode('P-Loss-Real', 	'p-loss-real', false),//, 'inspector_p_loss'),
									makeInspectionMode('Crop Yield', 	'crop-yield', false, 'inspector_p_loss'),
									makeInspectionMode('Bird Habitat', 	'bird-habitat'),
								{
									text: 'Land Properties', disabled: true,
									style: 'border-bottom: 1px solid rgba(0,0,0,0.2);padding-top: 4px'
								}, 
									makeInspectionMode('Slope', 		'slope', 	false, 'inspector_limiter'),
									makeInspectionMode('Soil Depth', 	'soil-depth',false, 'inspector_limiter'),,
									makeInspectionMode('% Sand', 		'perc-sand', false, 'inspector_limiter'),
									makeInspectionMode('% Silt', 		'perc-silt', false, 'inspector_limiter'),
									makeInspectionMode('Distance to Water', 'dist-water', false, 'inspector_limiter'),
									makeInspectionMode('Elevation', 	'dem', 		false, 'inspector_limiter'),
									makeInspectionMode('pH', 			'ph', 		false, 'inspector_limiter'),
									makeInspectionMode('OM', 			'om', 		false, 'inspector_limiter'),
									makeInspectionMode('K', 			'k', 		false, 'inspector_limiter'),
									makeInspectionMode('ksat', 			'ksat', 	false, 'inspector_limiter'),
									makeInspectionMode('LS', 			'ls', 		false, 'inspector_limiter'),
									makeInspectionMode('Slope Length', 	'slope-length', false, 'inspector_limiter'),

								]
							});
						}
					}
				},{
					xtype: 'container',
					itemId: 'mode-options',
					layout: 'fit'
				}]
			},{//------------------------------------------------------------------
				xtype: 'checkbox',
				id: 'DSS_cheatRestrictFarm',
				margin: '0 0 0 8',
				boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
				boxLabel: 'Limit to active operation', checked: true,
				handler: function(self, checked) {
					DSS_Refilter();
					me.down('#DSS_aggregateHider').animate({
						dynamic: true,
						to: {
							height: checked ? 28 : 0
						}
					})
				}
			},{
				xtype: 'container',
				style: 'overflow: hidden!important',
				itemId: 'DSS_aggregateHider',
				height: 28,
				items: [{
					xtype: 'checkbox',
					id: 'DSS_cheatFieldAggregate',
					margin: '0 0 0 24',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Aggregate by field', checked: true,
					handler: function() {
						DSS_Refilter();
					}
				}]
			},{//------------------------------------------------------------------
				xtype: 'checkbox',
				id: 'DSS_maskByCDL',
				margin: '0 0 0 8',
				boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
				boxLabel: 'Limit by landcover', checked: false,
				handler: function(self, checked) {
					DSS_Refilter();
					me.down('#DSS_cdlHider').animate({
						dynamic: true,
						to: {
							height: checked ? 28 : 0
						}
					})
				}
			},{
				xtype: 'checkboxgroup',
				itemId: 'DSS_cdlHider',
				style: 'overflow: hidden!important',
				fieldLabel: 'Match',
				labelWidth: 58,
				labelAlign: 'right',
				labelClsExtra: 'light-color',
				columns: 2,
				height: 0,
				items: [{
					id: 'DSS_cheatRowCropMask',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Crops', checked: true,
					handler: function() {
						DSS_Refilter();
					}
				},{
					id: 'DSS_cheatGrassMask',
					boxLabelCls: 'x-form-cb-label x-form-cb-label-default x-form-cb-label-after box-label-cls',
					boxLabel: 'Grass', checked: true,
					handler: function() {
						DSS_Refilter();
					}
				}]
			}]
		});
		
		me.callParent(arguments);
//		me.setMode('crop-yield');
	},
	
	//-------------------------------------------------------------------------------------------------
	inspectorOptionsChanged: function(options, silent) {
		let me = this;

		me.DSS_options = options;
		if (!silent) {
			me.computeResults(undefined, DSS.layer ? DSS.layer.ModelResult : undefined);
		}
	},
	
	//-------------------------------------------------------------------------------------------------
	setMode: function(mode) {
		let me = this;
		
		me.DSS_mode = mode;
		me.computeResults(undefined, DSS.layer.ModelResult);
	},
	
	//-------------------------------------------------------------------------------------------------
	computeResults: function(extents, modelResultsLayer) {
		let me = this;
		
		// TODO: busy feedback
		if (me.DSS_isWorking) {
			DSS_RefilterDelayed(500);
			return;
		}
		
		if (!extents) {
			extents = me.DSS_extents;
		}
		
		if (!extents) {
			console.log("nothing to do right now?");
			return;
		}
		if (!me.DSS_mode) me.DSS_mode = 'crop-yield';
		
		me['DSS_extents'] = extents;
	
		me.DSS_isWorking = true;
		
		let data = {
			"extent": extents,
			"model": me.DSS_mode,
			"options": me.DSS_options
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
				me.DSS_isWorking = false;

				modelResultsLayer.setSource(new ol.source.ImageStatic({
					url: obj.url,
					imageExtent: obj.extent,
					projection: 'EPSG:3071'
				}))
				modelResultsLayer.setOpacity(0.7);
				modelResultsLayer.setVisible(true);	
				
				DSS.MapState.showLegend(obj.palette, obj.values);
				
				if (obj.fields) {
					DSS.fieldList.addStats(me.DSS_mode, obj.fields)
				}
			},
			
			failure: function(response, opts) {
				me.DSS_isWorking = false;
			}
		});

	}

});

