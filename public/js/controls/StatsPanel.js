DSS.utils.addStyle('.info-panel { border-left: 1px solid #222;  border-bottom: 1px solid rgba(0,0,0,0.25); background-color: #555; background-repeat: no-repeat; background-image: linear-gradient(to right, #333 0%, #3f3f3f 25%, #4a4a4a 50%, #535353 80%, #555 100%); background-size: 2rem 100%;');
DSS.utils.addStyle('.x-resizable-handle-west {width: 6px; background-color: rgba(255,255,255,0.25)}');
DSS.utils.addStyle('.box-label-cls {color: #eee; text-shadow: 0 1px rgba(0,0,0,0.2),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');
DSS.utils.addStyle('.small {color: #38b; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2); font-size: 1rem}');
DSS.utils.addStyle('.light-color {color: #bbb; text-shadow: 0 1px rgba(0,0,0,0.3),1px 0 rgba(0,0,0,0.2);}');
DSS.utils.addStyle('.drop {overflow: visible!important}');
DSS.utils.addStyle('.drop:after {overflow: visible!important; display: block; position: absolute; bottom: -8px; left: calc(50% - 8px); content: ""; background-color: transparent; border-top: 8px solid #666; width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent;}');



//--------------------------------------------------------------------------
var filter_task = new Ext.util.DelayedTask(function(){
	if (!DSS.layer) return;
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
		'DSS.controls.Inspector_Mode',
		'DSS.controls.Inspector_Limiter',
		'DSS.controls.Inspector_PLoss',
		'DSS.controls.Inspector_Yield',
		'DSS.controls.Management',
		'DSS.controls.Inspector_RestrictResults'
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

		Ext.applyIf(me, {
			defaults: {
				xtype: 'component',
			},
			items: [{
				cls: 'section-title',
				style: 'color: #ddd; text-shadow: 0 1px rgba(0,0,0,0.2)',
				html: 'Inspector <i class="fas fa-search small"></i>',
			},{
				xtype: 'inspector_mode',
				DSS_Parent: me
			},{
				xtype: 'inspector_restrict_results'
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
			DSS_Refilter();//
//			me.computeResults(undefined, DSS.layer ? DSS.layer.ModelResult : undefined);
		}
	},
	
	//-------------------------------------------------------------------------------------------------
	setMode: function(mode) {
		let me = this;
		
		me.DSS_mode = mode;
//		me.computeResults(undefined, DSS.layer.ModelResult);
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
		console.log(data);
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
				console.log("Image request",obj);
				me.DSS_isWorking = false;

				modelResultsLayer.setSource(new ol.source.ImageStatic({
					url: obj.url,
					imageExtent: obj.extent,
					projection: 'EPSG:3071'
				}))
				modelResultsLayer.setOpacity(0.7);
				modelResultsLayer.setVisible(true);	
				
				if (obj.key) {
					DSS.MapState.showClassifiedLegend(obj.key)
				}
				else {
					DSS.MapState.showContinuousLegend(obj.palette, obj.values);
				}
				
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

